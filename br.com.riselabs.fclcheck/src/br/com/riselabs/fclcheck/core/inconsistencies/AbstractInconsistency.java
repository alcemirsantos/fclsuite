package br.com.riselabs.fclcheck.core.inconsistencies;

import java.util.Properties;

import br.com.riselabs.fclcheck.enums.ConstraintType;
import br.com.riselabs.fclcheck.enums.ViolationProperties;

public abstract class AbstractInconsistency {
	protected final String sourceFeature;
	protected final String targetFeature;
	protected final Integer lineNumber;

	protected AbstractInconsistency(String sourceFeature, String targetFeature,
			Integer lineNumber) {
		super();
		this.sourceFeature = sourceFeature;
		this.targetFeature = targetFeature;
		this.lineNumber = lineNumber;
	}

	public String getSourceFeature() {
		return this.sourceFeature;
	}

	public String getTargetFeature() {
		return this.targetFeature;
	}

	public Integer getLineNumber() {
		return lineNumber;
	}


	public Properties props() {
		Properties props = new Properties();
		props.put(ViolationProperties.SOURCE_FEATURE_NAME.getKey(), this.sourceFeature);
		props.put(ViolationProperties.TARGET_FEATURE_NAME.getKey(), this.targetFeature);
		props.put(ViolationProperties.LINE_NUMBER.getKey(),
				(this.lineNumber != null) ? this.lineNumber.toString() : "");
		return props;
	}

	public final boolean equals(AbstractInconsistency other) {
		return (this.getType().equals(other.getType()) && this.targetFeature
				.equals(other.targetFeature));
	}

	public abstract ConstraintType getType();

	public abstract String toShortString();
}
