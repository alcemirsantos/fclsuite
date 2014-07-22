package br.com.riselabs.fclcheck.handlers;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
import br.com.riselabs.vparser.util.CSVUtil;

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

		selectedProject = ((IProject) objectSelected).getProject();

		return selectedProject;
	}

	private void checkProject(IProject project) throws CoreException,
			JavaModelException {
		System.out
				.println("// ============================== //\nChecking project: "
						+ project.getName());

		// check if we have a Java project
		if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
			IJavaProject javaProject = JavaCore.create(project);
			walkThroughJavaPackages(javaProject);

			// Check if we have C or C++ project
		} else if (project.isNatureEnabled("org.eclipse.cdt.core.cnature")
				|| project.isNatureEnabled("org.eclipse.cdt.core.ccnature")) {
			ICProject cProject = (CoreModel.create(project.getWorkspace()
					.getRoot())).getCProject(project.getName());
			walkThroughPackages(cProject);

			// In the the case the 'project' has a different nature from Java or
			// C/C++
		} else {
			showMessage(
					"Invalide project nature",
					"FCLcheck is only available for projects of"
							+ "both natures: C/C++ and Java.\n"
							+ "We expect to support additional natures in the furture.");
		}
		CSVUtil.writeCSV();
	}

	private void walkThroughPackages(ICProject cProject) throws CModelException {

		try {
			IIndex index = CCorePlugin.getIndexManager().getIndex(cProject);
			for (IIndexFile iif : index.getAllFiles()) {
				String aPath = iif.getLocation().getFullPath();
				if (aPath==null) continue;
				System.out.println("\n----->"+aPath+"\n");
				IPath path = new Path(aPath);
				IFile file = ResourcesPlugin.getWorkspace().getRoot()
						.getFile(path);
				lexTokenizer(getCFileTokenized(file),file);
				
				// ITranslationUnit tu= (ITranslationUnit)
				// CoreModel.getDefault().create(file);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		// ISourceRoot[] packages = cProject.getSourceRoots();
		// for (ISourceRoot srcRoot : packages) {
		// System.out.println("source root: " + srcRoot.getElementName());
		// walThroughFiles(srcRoot);
		//
		// }
	}

	/**
	 * Given a srcRoot this mehtod walks throuht all the files.
	 * 
	 * @param srcRoot
	 * @throws CModelException
	 */
	private void lexTokenizer(StringTokenizer tkn, IFile file) throws CModelException {
			if (tkn != null) {
				while (tkn.hasMoreElements()) {
					String s = (String) tkn.nextElement();
//					System.out.println("icelement buffer: " + s);
					List<Token> tokens = Lexer.tokenize(s, true);
					if (!tokens.isEmpty()){
						System.out.println("icelement buffer: " + s);
						for (Token token : tokens) {
							System.out.println(token.toString());
						}
						addCSVRecord(s, file.getFullPath().toString());
					}
				}
			}
	}
	/**
	 * Given a srcRoot this mehtod walks throuht all the files.
	 * 
	 * @param srcRoot
	 * @throws CModelException
	 */
	private void walThroughFiles(ISourceRoot srcRoot) throws CModelException {
		for (ICElement element : srcRoot.getChildren()) {
			StringTokenizer tkn = getCFileTokenized(element);
			if (tkn != null) {
				while (tkn.hasMoreElements()) {
					String s = (String) tkn.nextElement();
					System.out.println("icelement buffer: " + s);
					List<Token> tokens = Lexer.tokenize(s, true);
					for (Token token : tokens) {
						System.out.println(token.toString());
					}
				}
			}
		}
	}

	/**
	 * Returns a StringTokenizer extracted from the stream found in the C file
	 * 
	 * @param element
	 * @return
	 */
	private StringTokenizer getCFileTokenized(IFile element) {
		System.out.println("icelement: " + element.getName());

		String stream = "";
		try {
			stream = convertStreamToString(element.getContents());
		} catch (CoreException e) {
			showMessage("CoreException", e.getMessage());
			e.printStackTrace();
		}
		StringTokenizer tkn = new StringTokenizer(stream, "\n");
		return tkn;
	}

	/**
	 * Returns a StringTokenizer extracted from the stream found in the C file
	 * 
	 * @param element
	 * @return
	 */
	private StringTokenizer getCFileTokenized(ICElement element) {
		System.out.println("icelement: " + element.getResource().getName());

		IFile f = element.getUnderlyingResource().getType() == IResource.FILE ? (IFile) element
				.getUnderlyingResource() : null;
		if (f == null) {
			return null;
		} else {
			String stream = "";
			try {
				stream = convertStreamToString(f.getContents());
			} catch (CoreException e) {
				showMessage("CoreException", e.getMessage());
				e.printStackTrace();
			}
			StringTokenizer tkn = new StringTokenizer(stream, "\n");
			return tkn;
		}
	}

	/**
	 * I learned this trick from "Stupid Scanner tricks" article. The reason it
	 * works is because Scanner iterates over tokens in the stream, and in this
	 * case we separate tokens using "beginning of the input boundary" (\A) thus
	 * giving us only one token for the entire contents of the stream.
	 * 
	 * Note, if you need to be specific about the input stream's encoding, you
	 * can provide the second argument to Scanner constructor that indicates
	 * what charset to use (e.g. "UTF-8").
	 * 
	 * Source:
	 * http://stackoverflow.com/questions/309424/read-convert-an-inputstream
	 * -to-a-string
	 * 
	 * @param is
	 * @return
	 */
	private String convertStreamToString(InputStream is) {
		Scanner s = new Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	private void walkThroughJavaPackages(IJavaProject javaProject)
			throws JavaModelException {
		IPackageFragment[] packages = javaProject.getPackageFragments();
		for (IPackageFragment mypackage : packages) {
			// Package fragments include all packages in the classpath
			// We will only look at the package from the source folder
			// K_BINARY would include also included JARS, e.g. rt.jar
			if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
				checkJavaICompilationUnitInfo(mypackage);
			}
		}
	}

	private void checkJavaICompilationUnitInfo(IPackageFragment mypackage)
			throws JavaModelException {
		for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			checkJavaCompilationUnitDetails(unit);
		}
	}

	private void checkJavaCompilationUnitDetails(ICompilationUnit unit)
			throws JavaModelException {
		System.out.println("Source file " + unit.getElementName());
		Document doc = new Document(unit.getSource());

		String str = "";
		int start = 0, end = 0;
		for (int i = 0; i < doc.getNumberOfLines(); i++) {
			try {
				end = doc.getLineLength(i);
				str = doc.get(start, end);
				List<Token> tokens = null;
				if (!str.isEmpty()) {
					tokens = Lexer.tokenize(str, false);
					if(!tokens.isEmpty()) {
						System.out.println("\nLine: "+str);
						addCSVRecord(str, unit.getResource().getFullPath().toString());
					}
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
	 * add record to the .csv file with the macros found.
	 * @param str
	 * @param filepath
	 */
	private void addCSVRecord(String str, String filepath){
		String id = String.valueOf(CSVUtil.csvRecordCount());
		CSVUtil.addCSVRecord(new String[]{id, str, filepath});
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