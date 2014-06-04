package br.com.riselabs.fclcheck.dependencies;

import br.com.riselabs.fclcheck.enums.ConstraintType;

public class CoExistDependency extends Dependency {

	public CoExistDependency(String classNameA, String classNameB,
			Integer lineNumberA) {
		super(classNameA, classNameB, lineNumberA);
	}

	@Override
	public ConstraintType getType() {
		return ConstraintType.CANNOT;
	}

	@Override
	public String toShortString() {
		return "The feature "+ this.sourceFeature + " cannot coexist with the feature "+
				this.targetFeature + " w.r.t. the feature modeling constraints.";
	}

}
