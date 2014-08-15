/**
 * 
 */
package br.com.riselabs.fclcheck.checkers;

import org.eclipse.jdt.core.IJavaProject;

/**
 * @author Alcemir Santos
 *
 */
public class JavaProjectChecker extends AbstractChecker {

	private IJavaProject target;
	
	public JavaProjectChecker(IJavaProject javaProject) {
		this.target = javaProject;
	}

	@Override
	protected void loadConstraints() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void checkTarget() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void markConstraints() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void fillInconsistenciesView() {
		// TODO Auto-generated method stub
		
	}

}
