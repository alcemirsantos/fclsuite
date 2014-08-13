/**
 * 
 */
package br.com.riselabs.fclcheck.standalone;

import java.util.List;

import br.com.riselabs.fclcheck.standalone.FCLConstraint;
import br.com.riselabs.vparser.lexer.beans.Token;

/**
 * @author  Alcemir Santos
 */
public class FCLWrapper {
	/**
	 * 
	 */
	private FCLConstraint constraint;
	/**
	 * 
	 */
	private List<Token> dependency;

	/**
	 * 
	 */
	public FCLWrapper() {
	}

	/**
	 * @return the constraint
	 */
	public FCLConstraint getConstraint() {
		return constraint;
	}

	/**
	 * @param constraint the constraint to set
	 */
	public void setConstraint(FCLConstraint constraint) {
		this.constraint = constraint;
	}

	/**
	 * @return the dependency
	 */
	public List<Token> getDependency() {
		return dependency;
	}

	/**
	 * @param dependency the dependency to set
	 */
	public void setDependency(List<Token> dependency) {
		this.dependency = dependency;
	}
}