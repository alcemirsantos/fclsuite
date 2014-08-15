package br.com.riselabs.fclcheck.builder;

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

import br.com.riselabs.fclcheck.exceptions.PluginException;
import br.com.riselabs.vparser.parsers.CppParser;
import br.com.riselabs.vparser.parsers.JavaParser;
import br.com.riselabs.vparser.parsers.SourceCodeParser;

public class FCLCheckBuilder extends IncrementalProjectBuilder {


	public static final String BUILDER_ID = "br.com.riselabs.fclcheck.fclcheckBuilder";

	private static final String MARKER_TYPE = "br.com.riselabs.fclcheck.fclcheckProblem";

	static void addMarker(IFile file, String message, int lineNumber,
			int severity) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
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

	protected void clean(IProgressMonitor monitor) throws CoreException {
		// delete markers set and files created
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}

	void fclCheckThis(IResource resource) throws PluginException {
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			deleteMarkers(file);
			ConsistencyErrorHandler reporter = new ConsistencyErrorHandler(file);
			if (resource.getName().endsWith(".java")) {
				try {
					getParser(".java").parse(file.getContents(), reporter);
				} catch (PluginException e) {
				} catch (CoreException e) {
					throw new PluginException(
							"This method fails. Reasons include: \n"
									+ "This resource does not exist.\n"
									+ "This resource is not local. \n"
									+ "The file-system resource is not a file. \n"
									+ "The workspace is not in sync with the corresponding "
									+ "location in the local file system.");
				}
			} else if (resource.getName().endsWith(".c")
					|| resource.getName().endsWith(".cpp")
					|| resource.getName().endsWith(".h")) {
				try {
					getParser(".c").parse(file.getContents(), reporter);
				} catch (PluginException e) {
				} catch (CoreException e) {
					throw new PluginException(
							"This method fails. Reasons include: \n"
									+ "This resource does not exist.\n"
									+ "This resource is not local. \n"
									+ "The file-system resource is not a file. \n"
									+ "The workspace is not in sync with the corresponding "
									+ "location in the local file system.");
				}
			}
		}

	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}


	private SourceCodeParser getParser(String fileExtension)
			throws PluginException {
		switch (fileExtension) {
		case ".c":
		case ".h":
			return new CppParser();
		case ".java":
			return new JavaParser();
		default:
			throw new PluginException("Sorry! It is not possible to parse "
					+ fileExtension + " yet.");
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
}
