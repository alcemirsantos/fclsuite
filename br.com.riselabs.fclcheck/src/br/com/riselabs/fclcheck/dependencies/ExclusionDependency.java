package br.com.riselabs.fclcheck.dependencies;

import br.com.riselabs.fclcheck.enums.ConstraintType;

public class ExclusionDependency extends Dependency {

	public ExclusionDependency(String classNameA, String classNameB,
			Integer lineNumberA) {
		super(classNameA, classNameB, lineNumberA);
	}

	@Override
	public ConstraintType getType() {
		return ConstraintType.REQUIRE;
	}

	@Override
	public String toShortString() {
		return "The feature "+this.sourceFeature+" requires the exclusion of"
				+ " the feature "+this.targetFeature+ " w.r.t. the feature modeling constraints.";
	}

}
