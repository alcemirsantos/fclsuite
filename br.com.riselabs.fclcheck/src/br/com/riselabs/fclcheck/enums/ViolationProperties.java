package br.com.riselabs.fclcheck.enums;

public enum ViolationProperties {
	VIOLATION_TYPE("violationType","Type of violation"),
	VIOLATED_CONSTRAINT("violatedConstraint","Violated Architectural Constraint"),
	CONSTRAINT("constraint","Constraint"),
	DEPENDENCY_TYPE("dependencyType","Dependency Type"),
	SOURCE_FEATURE_NAME("sourceFeatureName","Source Feature Name"),
	TARGET_FEATURE_NAME("targetFeatureName","Target Feature Name"),
	LINE_NUMBER("lineNumber","Line Number"),
	DETAILED_MESSAGE("detailedMessage","Detailed Message");
	
	private final String key;
	private final String label;
	
	private ViolationProperties(String key, String label){
		this.key = key;
		this.label = label;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public String getLabel() {
		return this.label;
	}
	
}
