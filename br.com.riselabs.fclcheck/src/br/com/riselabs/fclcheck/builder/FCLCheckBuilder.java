package br.com.riselabs.fclcheck.builder;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.Severity;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import br.com.riselabs.fclcheck.FCLCheck;
import br.com.riselabs.fclcheck.exceptions.PluginException;
import br.com.riselabs.fclcheck.jobs.InconsistenciesViewUpdateJob;
import br.com.riselabs.fclcheck.standalone.FCLConstraint;
import br.com.riselabs.fclcheck.views.InconsistenciesView;
import br.com.riselabs.vparser.beans.CCVariationPoint;
import br.com.riselabs.vparser.lexer.beans.Token;
import br.com.riselabs.vparser.lexer.enums.TokenType;
import br.com.riselabs.vparser.parsers.ConstraintsParser;
import br.com.riselabs.vparser.parsers.CppParser;
import br.com.riselabs.vparser.parsers.FCLParser;
import br.com.riselabs.vparser.parsers.JavaParser;

public class FCLCheckBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "br.com.riselabs.fclcheck.fclcheckBuilder";

	public static final String MARKER_TYPE = "br.com.riselabs.fclcheck.fclcheckProblem";

	private List<FCLConstraint> vmc = new LinkedList<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {

		loadVariabilityModelConstraints();

		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	private void loadVariabilityModelConstraints() {
		try {
			switch (getConstraintsFile().getFileExtension()) {
			case "fcl":
				vmc = new FCLParser().parse(getConstraintsFile());
				break;
			case "constraints":
				vmc = new ConstraintsParser().parse(getConstraintsFile());
				break;
			default:
				throw new PluginException("Sorry! It is not possible to parse "
						+ getConstraintsFile().getFileExtension() + " yet.");
			}
		} catch (PluginException e) {
			try {
				String message = "A constraints file is missing.";
				addErrorMarker(getProject(), message);
				throw new PluginException(message);
			} catch (CoreException e1) {
				e1.printStackTrace();
			} catch (PluginException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	void addMarker(IFile file, String message, int lineNumber, int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
	}

	private void addErrorMarker(IProject project, String message)
			throws CoreException {
		IMarker marker = project.createMarker(MARKER_TYPE);
		marker.setAttribute(IMarker.SEVERITY, Severity.ERROR.getValue());
		marker.setAttribute(IMarker.MESSAGE, message);
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {
		// delete markers set and files created
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}

	void fclCheckThis(IResource resource) throws PluginException {
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			if (!isSourceFile(file.getFileExtension()))
				return;
			deleteMarkers(file);
			ConsistencyErrorHandler reporter = new ConsistencyErrorHandler(file);

			List<CCVariationPoint> vps = new LinkedList<>();
			switch (resource.getFileExtension()) {
			case "java":
				vps = new JavaParser().parse(file);
				break;
			case "c":
			case "cpp":
			case "h":
				vps = new CppParser().parse(file);
				break;
			}

			// TODO comprare(vmc, vps);
			for (CCVariationPoint vp : vps) {
				if (!vp.isSingleVP(vp.getTokens()))
					continue;
				String f = getFeature(vp.getTokens()).getValue();

				for (FCLConstraint constraint : vmc) {
					switch (constraint.getType()) {
					case INCLUDES:
						if (constraint.getLeftTerm().contains(f))
							reporter.warning(new ConsistencyException(
									"The feature "
											+ f
											+ " includes "
											+ constraint.getRightTerm()
											+ ". Make sure your are not introducing an inconsistency.",
									vp.getLineNumber()));
						break;
					case EXCLUDES:
						if (constraint.getRightTerm().contains(f))
							reporter.warning(new ConsistencyException(
									"The featue " + f + " is excluded by: "
											+ constraint.toString(), vp
											.getLineNumber()));
						break;
					case MUTUALLY_EXCLUSIVE:
					case IFF:
						reporter.fatalError(new ConsistencyException(
								"dumb programmer did not implemented this shit yet.",
								vp.getLineNumber()));
						break;
					default:
						break;
					}
				}
			}

			InconsistenciesView.sync();
		}

	}

	private boolean isSourceFile(String fileExtension) {
		if (fileExtension == null)
			return false;
		switch (fileExtension) {
		case "java":
		case "c":
		case "cpp":
		case "h":
			return true;
		}
		return false;
	}

	private Token getFeature(List<Token> tokens) {
		for (Token token : tokens) {
			if (token.getLexeme() == TokenType.TAG)
				return token;
		}
		return null;
	}

	private IFile getConstraintsFile() {
		return getProject().getFile(FCLCheckNature.CONSTRAINTS_FILENAME);
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		try {
			getProject().accept(new SampleResourceVisitor());
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new SampleDeltaVisitor());
	}

	class SampleDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				try {
					fclCheckThis(resource);
				} catch (PluginException e) {
				}
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				try {
					fclCheckThis(resource);
				} catch (PluginException e) {
				}
				break;
			}
			// return true to continue visiting children.
			return true;
		}
	}

	class SampleResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			try {
				fclCheckThis(resource);
			} catch (PluginException e) {
			}
			// return true to continue visiting children.
			return true;
		}
	}

	class ConsistencyErrorHandler {

		private IFile file;

		public ConsistencyErrorHandler(IFile file) {
			this.file = file;
		}

		public void error(ConsistencyException exception) {
			addMarker(exception, IMarker.SEVERITY_ERROR);
		}

		public void fatalError(ConsistencyException exception) {
			addMarker(exception, IMarker.SEVERITY_ERROR);
		}

		public void warning(ConsistencyException exception) {
			addMarker(exception, IMarker.SEVERITY_WARNING);
		}

		private void addMarker(ConsistencyException e, int severity) {
			FCLCheckBuilder.this.addMarker(file, e.getMessage(),
					e.getLineNumber(), severity);
		}

	}

}
