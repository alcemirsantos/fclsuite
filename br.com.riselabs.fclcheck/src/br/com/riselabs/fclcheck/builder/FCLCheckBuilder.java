package br.com.riselabs.fclcheck.builder;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import br.com.riselabs.fclcheck.core.VariabilityModel;
import br.com.riselabs.fclcheck.exceptions.PluginException;
import br.com.riselabs.fclcheck.standalone.FCLConstraint;
import br.com.riselabs.fclcheck.views.InconsistenciesView;
import br.com.riselabs.vparser.beans.CCVariationPoint;
import br.com.riselabs.vparser.lexer.beans.Token;
import br.com.riselabs.vparser.lexer.enums.TokenType;
import br.com.riselabs.vparser.parsers.CppParser;
import br.com.riselabs.vparser.parsers.JavaParser;

public class FCLCheckBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "br.com.riselabs.fclcheck.fclcheckBuilder";

	public static final String MARKER_TYPE = "br.com.riselabs.fclcheck.fclcheckProblem";

	private VariabilityModel vmodel;

	private FCLCheckJob checkJob = new FCLCheckJob("Starting checking of consistency...");
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (vmodel == null) {
			VariabilityModel.getInstance().setProject(getProject());
			vmodel = VariabilityModel.getInstance();
		}
		
		if (kind == FULL_BUILD) {
//			fullBuild(monitor);
			checkJob.setFullBuild(true);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
//				fullBuild(monitor);
				checkJob.setFullBuild(true);
			} else {
//				incrementalBuild(delta, monitor);
				checkJob.setFullBuild(false);
				checkJob.setDelta(delta);
				
			}
		}
		checkJob.setUser(true);
		checkJob.schedule();
		return null;
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {
		// delete markers set and files created
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
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
					checkJob.setName("Checking: " + getProject().getName()+ File.separator + resource.getProjectRelativePath());
					checkJob.fclCheckThis(resource);
				} catch (PluginException e) {
				}
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				try {
					checkJob.setName("Checking: " + getProject().getName()+ File.separator + resource.getProjectRelativePath());
					checkJob.fclCheckThis(resource);
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
				checkJob.setName("Checking: " + getProject().getName()+ File.separator + resource.getProjectRelativePath());
				checkJob.fclCheckThis(resource);
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

	class FCLCheckJob extends Job {

		private IProgressMonitor monitor;
		private IResourceDelta delta;
		private boolean isIncrementalBuild;

		public FCLCheckJob(String name) {
			super(name);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			this.monitor = monitor;
			
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			
			try {
				if(isIncrementalBuild)
					incrementalBuild(delta, monitor);
				else
					fullBuild(monitor);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			
			return Status.OK_STATUS;
		}
		
		public void setDelta(IResourceDelta delta) {
			this.delta = delta;
		}

		public void setFullBuild(boolean b) {
			this.isIncrementalBuild = !b;
		}

		public IProgressMonitor getMonitor() {
			return this.monitor;
		}

		private void fullBuild(final IProgressMonitor monitor)
				throws CoreException {
			try {
				getProject().accept(new SampleResourceVisitor());
			} catch (CoreException e) {
			}
		}

		private void incrementalBuild(IResourceDelta delta,
				IProgressMonitor monitor) throws CoreException {
			// the visitor does the work.
			delta.accept(new SampleDeltaVisitor());
		}

		private void deleteMarkers(IFile file) {
			try {
				file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
			} catch (CoreException ce) {
			}
		}

		private Token getFeature(List<Token> tokens) {
			for (Token token : tokens) {
				if (token.getLexeme() == TokenType.TAG)
					return token;
			}
			return null;
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

		
		public void fclCheckThis(IResource resource) throws PluginException {
			if (resource instanceof IFile) {
				IFile file = (IFile) resource;
				if (!isSourceFile(file.getFileExtension()))
					return ;
				deleteMarkers(file);
				ConsistencyErrorHandler reporter = new ConsistencyErrorHandler(
						file);

				List<CCVariationPoint> vps = new LinkedList<>();
				switch (resource.getFileExtension()) {
				case "java":
					try {
						vps = new JavaParser().parse(file);
					} catch (PluginException e) {
						e.printStackTrace();
					}
					break;
				case "c":
				case "cpp":
				case "h":
					try {
						vps = new CppParser().parse(file);
					} catch (PluginException e) {
						e.printStackTrace();
					}
					break;
				}

				// TODO comprare(vmc, vps);
				for (CCVariationPoint vp : vps) {
					if (!vp.isSingleVP(vp.getTokens()))
						continue;
					String f = getFeature(vp.getTokens()).getValue();

					for (FCLConstraint constraint : vmodel.getVMConstraints()) {
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
	
	}
}
