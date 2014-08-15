package br.com.riselabs.fclcheck.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

//import br.ufal.ic.colligens.controllers.CoreController;
//import br.ufal.ic.colligens.controllers.ProjectExplorerException;
//import br.ufal.ic.colligens.models.TypeChefException;

public class FCLCheckJob extends Job {

	private IProgressMonitor monitor;
	
	public FCLCheckJob(String name) {
		super(name);
	}

	public IProgressMonitor getMonitor(){
		return this.monitor;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		this.monitor = monitor;

		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;
//	
//		.......>> Example extracted from Colligens  <<.....    //
//
//		try {
//			// checks files in ProjectExplorer or PackageExplorer
//			projectExplorerController.run();
//
//			if (monitor.isCanceled())
//				return Status.CANCEL_STATUS;
//			// get files to analyze e run
//			typeChef.run(projectExplorerController.getList());
//
//			// returns the result to view
//			syncWithPluginView();
//
//		} catch (TypeChefException e) {
//			e.printStackTrace();
//			return Status.CANCEL_STATUS;
//		} catch (ProjectExplorerException e) {
//			e.printStackTrace();
//			return Status.CANCEL_STATUS;
//		} finally {
//
//			monitor.done();
//			CoreController.monitor = null;
//
//		}
//
		return Status.OK_STATUS;
	}

}
