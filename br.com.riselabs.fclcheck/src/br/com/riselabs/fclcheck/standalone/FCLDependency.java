/**
 * 
 */
package br.com.riselabs.fclcheck.standalone;

/**
 * @author Alcemir Santos
 *
 */
public enum FCLDependency {
	INCLUDES("includes"), EXCLUDES("excludes"), MUTUALLY_EXCLUSIVE(
			"mutally-exclusive"), IFF("if-only-if");

	private String value;

	 FCLDependency(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
