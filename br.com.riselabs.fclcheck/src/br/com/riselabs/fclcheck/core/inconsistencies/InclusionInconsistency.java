package br.com.riselabs.fclcheck.core.inconsistencies;

import br.com.riselabs.fclcheck.enums.ConstraintType;

public class InclusionInconsistency extends AbstractInconsistency {

	public InclusionInconsistency(String classNameA, String classNameB,
			Integer lineNumberA) {
		super(classNameA, classNameB, lineNumberA);
	}

	@Override
	public ConstraintType getType() {
		return ConstraintType.REQUIRE;
	}

	@Override
	public String toShortString() {
		return "The feature "+this.sourceFeature+ " requires the inclusion of"
				+ " the feature"+ this.targetFeature +" w.r.t. the feature modeling constraints.";
	}

}
