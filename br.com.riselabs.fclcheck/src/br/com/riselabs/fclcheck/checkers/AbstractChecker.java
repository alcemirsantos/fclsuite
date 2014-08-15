package br.com.riselabs.fclcheck.checkers;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.CContainer;
import org.eclipse.cdt.internal.core.model.SourceRoot;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import br.com.riselabs.fclcheck.jobs.FCLCheckJob;
import br.ufal.ic.colligens.controllers.ProjectExplorerException;

public abstract class AbstractChecker<T> {

	private IStructuredSelection selection;
	private List<T> targetSelected;
	private static IProgressMonitor monitor;

	protected AbstractChecker(){}
	protected AbstractChecker(IStructuredSelection selection){
		this.selection = selection;
		this.targetSelected = (List<T>) start(); 
	}
	/**
	 * Performs the check itself.
	 * 
	 */
	public void execute() {
		FCLCheckJob job = new FCLCheckJob("Checking selection...");
		monitor = job.getMonitor(); 
		loadConstraints();
		checkTarget();
		markConstraints();
		fillInconsistenciesView();
		
		job.setUser(true);
		job.schedule();
	}

	/**
	 * Reads the from the specified path
	 */
	protected abstract void loadConstraints();

	/**
	 * Checks the chosen target against the previously loaded constraints.
	 * Before you call <code>checkTarget()</code>, please make sure you have the
	 * constraints loaded.
	 */
	protected abstract void checkTarget();

	/**
	 * Marks the constraints in the source code editor.
	 */
	protected abstract void markConstraints();

	/**
	 * Fills the constraint view with the inconsistencies found.
	 */
	protected abstract void fillInconsistenciesView();

	
	/**
	 * @return
	 * @throws PluginException
	 */
	public List<IResource> start() throws PluginException {
		
		iResources.clear();
		
		if (selection == null) {
			throw new PluginException("Select a valid file or directory.");
		}

		List<IResource> iResources = new LinkedList<IResource>();

		@SuppressWarnings("unchecked")
		List<Object> list = selection.toList();

		for (Object object : list) {
			if (object instanceof SourceRoot) {
				iResources.add(((SourceRoot) object).getResource());
			} else if (object instanceof CContainer) {
				iResources.add(((CContainer) object).getResource());
			} else if (object instanceof ITranslationUnit) {
				iResources.add(((ITranslationUnit) object).getResource());
			} else if (object instanceof IFile) {
				iResources.add((IResource) object);
			} else if (object instanceof IFolder) {
				iResources.add((IResource) object);
			}
		}

		if (iResources.isEmpty()) {
			throw new PluginException("Select a valid file or directory.");
		}

		return iResources;
	}
	
	
	/**
	 * Pop-up an information dialog to show a message the user.
	 * 
	 * @param title 
	 * 			- the title of the window 
	 * @param message
	 * 			- the message the be shown
	 */
	public static void showMessage(String title, String message) {
		MessageDialog.openInformation(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), title, message);
	}
}// class ending
