package br.com.riselabs.fclcheck.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import br.com.riselabs.vparser.lexer.Lexer;
import br.com.riselabs.vparser.lexer.beans.Token;

public class FclCheckHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject project = getSelectedProject(event);

		try {
			checkProject(project);
		} catch (JavaModelException e) {
			showMessage("JavaModelException", e.getMessage());
			e.printStackTrace();
		} catch (CoreException e) {
			showMessage("CoreException", e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * This method returns the selected project in the ProjectExplorer View
	 * 
	 * @param event
	 * @return
	 * @throws ExecutionException
	 */
	private IProject getSelectedProject(ExecutionEvent event)
			throws ExecutionException {
		IProject selectedProject = null;

		// get workbench window
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		// set selection service
		ISelectionService service = window.getSelectionService();
		// set structured selection
		IStructuredSelection structured = (IStructuredSelection) service
				.getSelection();

		Object objectSelected = structured.iterator().next();

		// XX TODO test also for C nature and not cast it to a IJavaProject
		// directly. XX
		//
		// if (project.hasNature(JavaCore.NATURE_ID)) {
		// targetProject = JavaCore.create(project);
		// }

		selectedProject = ((IJavaProject) objectSelected).getProject();

		return selectedProject;
	}

	private void checkProject(IProject project) throws CoreException,
			JavaModelException {
		System.out.println("// ======= //\n Checking project: " + project.getName());
		// check if we have a Java project
		if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
			IJavaProject javaProject = JavaCore.create(project);
			walkThroughPackages(javaProject);
		}
	}

	private void walkThroughPackages(IJavaProject javaProject)
			throws JavaModelException {
		IPackageFragment[] packages = javaProject.getPackageFragments();
		for (IPackageFragment mypackage : packages) {
//	    Package fragments include all packages in the classpath
//		We will only look at the package from the source folder
//		K_BINARY would include also included JARS, e.g. rt.jar
			if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
				System.out.println("Package " + mypackage.getElementName());
				checkICompilationUnitInfo(mypackage);
			}
		}
	}

	private void checkICompilationUnitInfo(IPackageFragment mypackage)
			throws JavaModelException {
		for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			checkCompilationUnitDetails(unit);
		}
	}
	
	private void checkCompilationUnitDetails(ICompilationUnit unit)
			throws JavaModelException {
		System.out.println("Source file " + unit.getElementName());
		Document doc = new Document(unit.getSource());
		
		String str="";
		int start =0, end =0;
		for (int i = 0; i < doc.getNumberOfLines(); i++) {
			try {
				end = doc.getLineLength(i);
				str = doc.get(start, end);
				List<Token> tokens=null;
				if (!str.isEmpty()) {
					tokens = Lexer.tokenize(str);
					for (Token token : tokens) {
						System.out.println(token.toString());					
					}
				}
				start += end;
			} catch (BadLocationException e) {
				showMessage("BadLocationException", e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Mosta um diálogo de informação com a <code>String</code> informada. os
	 * parâmetros se referem ao título do diálogo e a mensagem a ser informada.
	 * 
	 * @param title
	 * @param message
	 */
	public static void showMessage(String title, String message) {
		MessageDialog.openInformation(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), title, message);
	}
}