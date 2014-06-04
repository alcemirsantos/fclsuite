package br.com.riselabs.fclcheck.enums;

import br.com.riselabs.fclcheck.dependencies.CoExistDependency;
import br.com.riselabs.fclcheck.dependencies.Dependency;
import br.com.riselabs.fclcheck.dependencies.ExclusionDependency;
import br.com.riselabs.fclcheck.dependencies.InclusionDependency;


public enum DependencyType {

	COEXIST("coexist", CoExistDependency.class),
	EXCLUSION("exclusion", ExclusionDependency.class),
	INCLUSION("inclusion", InclusionDependency.class);
	
	
	private final String value;
	private final Class<? extends Dependency> dependencyClass;

	private DependencyType(String value, Class<? extends Dependency> dependencyClass) {
		this.value = value;
		this.dependencyClass = dependencyClass;
	}

	public String getValue() {
		return this.value;
	}

	public Class<? extends Dependency> getDependencyClass() {
		return this.dependencyClass;
	}

	public final Dependency createGenericDependency(String classNameA, String classNameB) {
		if (this == COEXIST) {
			return new CoExistDependency(classNameA, classNameB, null);
		} else if (this == EXCLUSION) {
			return new ExclusionDependency(classNameA, classNameB, null);
		} else if (this == INCLUSION) {
			return new InclusionDependency(classNameA, classNameB, null);
		}
		return null;
	}
}
