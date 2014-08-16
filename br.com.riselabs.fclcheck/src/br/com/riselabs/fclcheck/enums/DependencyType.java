package br.com.riselabs.fclcheck.enums;

import br.com.riselabs.fclcheck.core.inconsistencies.AbstractInconsistency;
import br.com.riselabs.fclcheck.core.inconsistencies.CoExistInconsistency;
import br.com.riselabs.fclcheck.core.inconsistencies.ExclusionInconsistency;
import br.com.riselabs.fclcheck.core.inconsistencies.InclusionInconsistency;


public enum DependencyType {

	COEXIST("coexist", CoExistInconsistency.class),
	EXCLUSION("exclusion", ExclusionInconsistency.class),
	INCLUSION("inclusion", InclusionInconsistency.class);
	
	
	private final String value;
	private final Class<? extends AbstractInconsistency> dependencyClass;

	private DependencyType(String value, Class<? extends AbstractInconsistency> dependencyClass) {
		this.value = value;
		this.dependencyClass = dependencyClass;
	}

	public String getValue() {
		return this.value;
	}

	public Class<? extends AbstractInconsistency> getDependencyClass() {
		return this.dependencyClass;
	}

	public final AbstractInconsistency createGenericDependency(String classNameA, String classNameB) {
		if (this == COEXIST) {
			return new CoExistInconsistency(classNameA, classNameB, null);
		} else if (this == EXCLUSION) {
			return new ExclusionInconsistency(classNameA, classNameB, null);
		} else if (this == INCLUSION) {
			return new InclusionInconsistency(classNameA, classNameB, null);
		}
		return null;
	}
}
