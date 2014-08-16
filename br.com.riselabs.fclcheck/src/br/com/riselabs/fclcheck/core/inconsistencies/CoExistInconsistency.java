package br.com.riselabs.fclcheck.core.inconsistencies;

import br.com.riselabs.fclcheck.enums.ConstraintType;

public class CoExistInconsistency extends AbstractInconsistency {

	public CoExistInconsistency(String classNameA, String classNameB,
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
