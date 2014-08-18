package br.com.riselabs.fclcheck.jobs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import br.com.riselabs.fclcheck.builder.FCLCheckBuilder;
import br.com.riselabs.fclcheck.builder.FCLCheckNature;
import br.com.riselabs.fclcheck.views.InconsistenciesView;

public class InconsistenciesViewUpdateJob extends Job {
	public InconsistenciesViewUpdateJob(String name) {
		super(name);
	}

	private List<IMarker> markerList;
	private InconsistenciesView inconsistenciesView;

	public List<IMarker> getMarkerList() {
		return markerList;
	}


	public void setInconsistenciesView(InconsistenciesView problemsView) {
		this.inconsistenciesView = problemsView;
	}
	
	private List<IProject> getFCLProejcts(){
		IProject[] allprojs = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		List<IProject> fclprojs = new ArrayList<IProject>();
		for (IProject iProject : allprojs) {
			try {
				if(iProject.isOpen() && iProject.isNatureEnabled(FCLCheckNature.NATURE_ID))
					fclprojs.add(iProject);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return fclprojs;
	}

	protected IStatus run(IProgressMonitor monitor) {
		try {
			inconsistenciesView.clearMarkers();

			List<IMarker> allMarkers = new ArrayList<IMarker>();
			for (IProject p : getFCLProejcts()) {
				IMarker[] markers;
				try {
					markers = p.findMarkers(null, true, IResource.DEPTH_INFINITE);
					for (IMarker iMarker : markers) {
						if (iMarker.getType().equals(FCLCheckBuilder.MARKER_TYPE)) {
							allMarkers.add(iMarker);
						}
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			
			if (allMarkers != null && allMarkers.size() > 0) {
				inconsistenciesView.addMarkers(allMarkers);
			}

			return Status.OK_STATUS;
		} finally {
			monitor.done();
//			schedule(2000); // runs every 2 seconds
		}
	}

}
