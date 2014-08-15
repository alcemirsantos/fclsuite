/**
 * 
 */
package br.com.riselabs.fclcheck.checkers;

import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;

import br.com.riselabs.vparser.lexer.Lexer;
import br.com.riselabs.vparser.lexer.beans.Token;

/**
 * @author Alcemir Santos
 * 
 */
public class CppProjectChecker extends AbstractChecker {

	private ICProject target;

	public CppProjectChecker(ICProject cProject) {
		this.target = cProject;
	}
	public CppProjectChecker(IStructuredSelection selection) {
		super(selection);
	}

	@Override
	protected void loadConstraints() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void checkTarget() {
		try {
			checkProject(target);
		} catch (CModelException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void markConstraints() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void fillInconsistenciesView() {
		// TODO Auto-generated method stub
	}
	
	private void checkProject(ICProject cProject) throws CModelException {

		try {
			IIndex index = CCorePlugin.getIndexManager().getIndex(cProject);
			
			for (IIndexFile iif : index.getAllFiles()) {
				String aPath = iif.getLocation().getFullPath();
				if (aPath==null) continue;
//				System.out.println("\n----->"+aPath+"\n");
				IPath path = new Path(aPath);
				IFile file = ResourcesPlugin.getWorkspace().getRoot()
						.getFile(path);
				lexTokenizer(file);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Given a srcRoot this mehtod walks throuht all the files.
	 * 
	 * @param srcRoot
	 * @throws CModelException
	 */
	private void lexTokenizer( IFile file) throws CModelException {
		StringTokenizer tkn = getCFileTokenized(file);	
		if (tkn != null) {
				while (tkn.hasMoreElements()) {
					String s = (String) tkn.nextElement();
					List<Token> tokens = Lexer.tokenize(s, Lexer.FileType.CPP);
					if (!tokens.isEmpty()){
//						addCSVRecord(s, file.getFullPath().toString());
						System.out.println(s);
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
		String stream = "";
		
		try {/*
			 * I learned this trick from "Stupid Scanner tricks" article. The reason it
			 * works is because Scanner iterates over tokens in the stream, and in this
			 * case we separate tokens using "beginning of the input boundary" (\A) thus
			 * giving us only one token for the entire contents of the stream.
			 * 
			 * Source:
			 * http://stackoverflow.com/questions/309424/read-convert-an-inputstream
			 * -to-a-string
			 * 
			 */
			Scanner s = new Scanner(element.getContents()).useDelimiter("\\A");
			stream = s.hasNext() ? s.next() : "";
			s.close();
			
		} catch (CoreException e) {
			showMessage("CoreException", e.getMessage());
			e.printStackTrace();
		}
		StringTokenizer tkn = new StringTokenizer(stream, "\n");
		return tkn;
	}
	

}
