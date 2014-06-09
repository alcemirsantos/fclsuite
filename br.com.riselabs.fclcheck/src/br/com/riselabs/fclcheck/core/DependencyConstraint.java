package br.com.riselabs.fclcheck.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import br.com.riselabs.fclcheck.dependencies.Dependency;
import br.com.riselabs.fclcheck.enums.Constraint;
import br.com.riselabs.fclcheck.enums.ConstraintType;

public class DependencyConstraint implements Comparable<DependencyConstraint> {
	private final String sourceFeature;
	private final String targetFeature;
	private final Constraint constraint;

	public DependencyConstraint(String sourceFeature, String targetFeature, Constraint constraint) {
		super();
		this.sourceFeature = sourceFeature;
		this.targetFeature = targetFeature;
		this.constraint = constraint;
	}

	public List<ArchitecturalDrift> validate(String className, final Map<String, String> modules, Set<String> projectClasses,
			Collection<Dependency> dependencies, IProject project) throws CoreException {
		switch (this.constraint.getConstraintType()) {
		case CANNOT:
			return this.validateCannot(className, targetFeature, this.constraint.getDependencyType().getDependencyClass(), modules,
					projectClasses, dependencies, project);
		case REQUIRE:
			
			return this.validateRequire();
		}

		return null;
	}

	/**
	 * cannot
	 */
	private List<ArchitecturalDrift> validateCannot(String className, String moduleDescriptionB,
			Class<? extends Dependency> dependencyClass, Map<String, String> modules, Set<String> projectClasses,
			Collection<Dependency> dependencies, IProject project) {
		List<ArchitecturalDrift> architecturalDrifts = new LinkedList<ArchitecturalDrift>();
		/* For each dependency */
		for (Dependency d : dependencies) {
			if (dependencyClass.isAssignableFrom(d.getClass())) {
				if (d.getClassNameB().equals(d.getClassNameA())) {
					continue;
				}
				/* We disregard indirect dependencies to divergences */
				if (d instanceof ExtendIndirectDependency || d instanceof ImplementIndirectDependency){
					continue;
				}
				
				if (DCLUtil.hasClassNameByDescription(d.getClassNameB(), moduleDescriptionB, modules, projectClasses, project)) {
					architecturalDrifts.add(new DivergenceArchitecturalDrift(this, d));
				}
			}
		}
		return architecturalDrifts;
	}
	
	/**
	 * validate require constraint
	 */
	private List<ArchitecturalDrift> validateRequire(String className, String moduleDescriptionB,
			Class<? extends Dependency> dependencyClass, Map<String, String> modules, Set<String> projectClasses,
			Collection<Dependency> dependencies, IProject project) {
		List<ArchitecturalDrift> architecturalDrifts = new LinkedList<ArchitecturalDrift>();
		/* For each dependency */
		for (Dependency d : dependencies) {
			if (dependencyClass.isAssignableFrom(d.getClass())) {
				if (d.getClassNameB().equals(d.getClassNameA())) {
					continue;
				}
				/* We disregard indirect dependencies to divergences */
				if (d instanceof ExtendIndirectDependency || d instanceof ImplementIndirectDependency){
					continue;
				}
				
				if (DCLUtil.hasClassNameByDescription(d.getClassNameB(), moduleDescriptionB, modules, projectClasses, project)) {
					architecturalDrifts.add(new DivergenceArchitecturalDrift(this, d));
				}
			}
		}
		return architecturalDrifts;
	}


	@Override
	public String toString() {
		return  this.sourceFeature + " "
				+ this.constraint.getValue() + " " + this.targetFeature;
	}

	public int compareTo(DependencyConstraint o) {
		return this.toString().compareTo(o.toString());
	}

	public Constraint getConstraint() {
		return this.constraint;
	}

	public String getModuleDescriptionA() {
		return this.sourceFeature;
	}

	public String getModuleDescriptionB() {
		return this.targetFeature;
	}

	/**
	 * DCL2 Class that stores the crucial informations about the architectural
	 * drift
	 */
	public static abstract class ArchitecturalDrift {
		public static final String DIVERGENCE = "DIVERGENCE";
		public static final String ABSENCE = "ABSENCE";

		protected final DependencyConstraint violatedConstraint;

		protected ArchitecturalDrift(DependencyConstraint violatedConstraint) {
			super();
			this.violatedConstraint = violatedConstraint;
		}

		public final DependencyConstraint getViolatedConstraint() {
			return this.violatedConstraint;
		}

		public abstract String getDetailedMessage();

		public abstract String getInfoMessage();

		public abstract String getViolationType();

	}

	public static class DivergenceArchitecturalDrift extends ArchitecturalDrift {
		private final Dependency forbiddenDependency;

		public DivergenceArchitecturalDrift(DependencyConstraint violatedConstraint, Dependency forbiddenDependency) {
			super(violatedConstraint);
			this.forbiddenDependency = forbiddenDependency;
		}

		public final Dependency getForbiddenDependency() {
			return this.forbiddenDependency;
		}

		@Override
		public String getDetailedMessage() {
			return this.forbiddenDependency.toString();
		}

		@Override
		public String getInfoMessage() {
			return this.forbiddenDependency.toShortString();
		}

		@Override
		public String getViolationType() {
			return DIVERGENCE;
		}
	}

	public static class AbsenceArchitecturalDrift extends ArchitecturalDrift {
		private final String classNameA;
		private final String moduleDescriptionB;

		public AbsenceArchitecturalDrift(DependencyConstraint violatedConstraint, String classNameA, String moduleDescriptionB) {
			super(violatedConstraint);
			this.classNameA = classNameA;
			this.moduleDescriptionB = moduleDescriptionB;
		}

		public final String getClassNameA() {
			return this.classNameA;
		}

		public String getModuleNameB() {
			return this.moduleDescriptionB;
		}

		@Override
		public String getDetailedMessage() {
			return this.classNameA + " does not " + this.violatedConstraint.getConstraint().getDependencyType().getValue()
					+ " any type in " + this.violatedConstraint.getModuleDescriptionB();
		}

		@Override
		public String getInfoMessage() {
			switch (this.violatedConstraint.getConstraint().getDependencyType()) {

			case ACCESS:
				return "The access of fiels or methods of " + this.violatedConstraint.getModuleDescriptionB()
						+ " is required for this location w.r.t. the architecture";
			case DECLARE:
				return "The declaration of " + this.violatedConstraint.getModuleDescriptionB()
						+ " is required for this location w.r.t. the architecture";
			case HANDLE:
				return "The access or declaration (handling) of " + this.violatedConstraint.getModuleDescriptionB()
						+ " is required for this location w.r.t. the architecture";
			case CREATE:
				return "The creation of " + this.violatedConstraint.getModuleDescriptionB()
						+ " is required for this location w.r.t. the architecture";
			case THROW:
				return "The throwing of " + this.violatedConstraint.getModuleDescriptionB()
						+ " is required for this location w.r.t. the architecture";
			case DERIVE:
			case EXTEND:
			case IMPLEMENT:
				return "The inheritance of " + this.violatedConstraint.getModuleDescriptionB()
						+ " is required for this location w.r.t. the architecture";
			case USEANNOTATION:
				return "The annotation @" + this.violatedConstraint.getModuleDescriptionB()
						+ " is required for this location w.r.t. the architecture";
			default:
				return "The dependency with " + this.violatedConstraint.getModuleDescriptionB()
						+ " is required for this location w.r.t. the architecture";
			}
		}

		@Override
		public String getViolationType() {
			return ABSENCE;
		}
	}

}
