/**
 * 
 */
package br.com.riselabs.fclcheck.standalone;

import br.com.riselabs.fclcheck.standalone.FCLDependencyType;

/**
 * @author  Alcemir Santos
 */
public class FCLConstraint {
	private String leftTerm;
	private String rightTerm;
	private FCLDependencyType type;

	/**
	 * @param right 
	 * @param dependency 
	 * @param left 
	 * 
	 */
	public FCLConstraint(String left, FCLDependencyType dependency, String right) {
		this.leftTerm = left;
		this.rightTerm = right;
		this.type = dependency;
	}

	/**
	 * @return the leftTerm
	 */
	public String getLeftTerm() {
		return leftTerm;
	}

	/**
	 * @param leftTerm the leftTerm to set
	 */
	public void setLeftTerm(String leftTerm) {
		this.leftTerm = leftTerm;
	}

	/**
	 * @return the rightTerm
	 */
	public String getRightTerm() {
		return rightTerm;
	}

	/**
	 * @param rightTerm the rightTerm to set
	 */
	public void setRightTerm(String rightTerm) {
		this.rightTerm = rightTerm;
	}

	/**
	 * @return the type
	 */
	public FCLDependencyType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(FCLDependencyType type) {
		this.type = type;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (!(o instanceof FCLConstraint))
			return false;
		FCLConstraint other = (FCLConstraint) o;
		if (getLeftTerm().equals(other.getLeftTerm())
				&& getRightTerm().equals(other.getRightTerm())
				&& getType() == other.getType())
			return true;
		else
			return false;
	}
	
	@Override
	public String toString(){
		return leftTerm+" "+type.getValue()+" "+rightTerm+";";
	}
}