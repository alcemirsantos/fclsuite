package br.com.riselabs.fclcheck.enums;




public enum Constraint {

//	CAN_COEXIST("can-coexist",ConstraintType.CAN, DependencyType.COEXIST), 
	
	CANNOT_COEXIST("cannot-coexist",ConstraintType.CANNOT, DependencyType.COEXIST),
	
	MUTUALLY_EXCLUSIVE("mutually-exclusive",ConstraintType.REQUIRE, DependencyType.EXCLUSION),
	
	REQUIRE_EXCLUSION("excludes",ConstraintType.REQUIRE, DependencyType.EXCLUSION),
	REQUIRE_INCLUSION("includes",ConstraintType.REQUIRE, DependencyType.INCLUSION);
	
	
	private final String value;
	private final ConstraintType constraintType;
	private final DependencyType dependencyType;
	
	
	private Constraint(String value, ConstraintType type, DependencyType dependencyType) {
        this.value = value;
        this.constraintType = type;
        this.dependencyType = dependencyType;
    }
	
	public String getValue(){
		return this.value;
	}
	
	public ConstraintType getConstraintType() {
		return this.constraintType;
	}
	
	public DependencyType getDependencyType() {
		return this.dependencyType;
	}
	
	/**
	 * DCL2 Returns the referee constraint by the constraint in DCL syntax.
	 * 
	 * The strings have their character "-" replace by "_", because the
	 * user write "A cannot-depend B", but the enumeration is "CANNOT_DEPEND"
	 * and not "CANNOT-DEPEND".
	 * 
	 * @param value The constraint text in DCL syntax
	 * @return The referee constraint
	 */
	public static Constraint getConstraint(String value){
		return Constraint.valueOf(value.toUpperCase().replaceAll("-", "_"));
	}
}
