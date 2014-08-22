package br.com.riselabs.fclcheck.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import br.com.riselabs.fclcheck.core.VariabilityModel;
import br.com.riselabs.fclcheck.exceptions.PluginException;
import br.com.riselabs.fclcheck.standalone.FCLConstraint;
import br.com.riselabs.fclcheck.standalone.FCLDependencyType;
import br.com.riselabs.fclcheck.views.InconsistenciesView;
import br.com.riselabs.vparser.beans.CCVariationPoint;
import br.com.riselabs.vparser.lexer.beans.Token;
import br.com.riselabs.vparser.lexer.enums.TokenType;
import br.com.riselabs.vparser.parsers.CppParser;
import br.com.riselabs.vparser.parsers.JavaParser;

public class FCLCheckBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "br.com.riselabs.fclcheck.fclcheckBuilder";

	public static final String MARKER_TYPE = "br.com.riselabs.fclcheck.fclcheckProblem";

	private VariabilityModel vmodel;

	private FCLCheckJob checkJob;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (vmodel == null) {
			VariabilityModel.getInstance().setProject(getProject());
			vmodel = VariabilityModel.getInstance();
		}
		checkJob = new FCLCheckJob("Checking " + getProject().getName()
				+ " for inconsistencies.");
		if (kind == FULL_BUILD) {
			// fullBuild(monitor);
			checkJob.setFullBuild(true);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				// fullBuild(monitor);
				checkJob.setFullBuild(true);
			} else {
				// incrementalBuild(delta, monitor);
				checkJob.setFullBuild(false);
				checkJob.setDelta(delta);
			}
		}
		checkJob.setUser(true);
		checkJob.schedule();
		return null;
	}

	protected void clean(IProgressMonitor monitor) throws CoreException {
		// delete markers set and files created
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}

	void addMarker(IFile file, String message, int lineNumber, int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LOCATION, file.getFullPath()
					.toOSString());
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
	}

	class SampleDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (checkJob.getMonitor().isCanceled())
				return false;
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				try {
					checkJob.getMonitor().subTask(
							"checking " + resource.getFullPath());
					// checkJob.setName("Checking: " + getProject().getName()+
					// File.separator + resource.getProjectRelativePath());
					checkJob.fclCheckThis(resource);
					checkJob.getMonitor().worked(1);
				} catch (PluginException e) {
				}
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				try {
					checkJob.getMonitor().subTask(
							"Checking: " + resource.getFullPath());
					// checkJob.setName("Checking: " + getProject().getName()+
					// File.separator + resource.getProjectRelativePath());
					checkJob.fclCheckThis(resource);
					checkJob.getMonitor().worked(1);
				} catch (PluginException e) {
				}
				break;
			}
			// return true to continue visiting children.
			return true;
		}
	}

	class SampleResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			if (checkJob.getMonitor().isCanceled())
				return false;
			try {
				checkJob.getMonitor().subTask(
						"checking: " + resource.getFullPath());
				// checkJob.setName("Checking: " + getProject().getName()+
				// File.separator + resource.getProjectRelativePath());
				checkJob.fclCheckThis(resource);
				checkJob.getMonitor().worked(1);
			} catch (PluginException e) {
			}
			// return true to continue visiting children.
			return true;
		}
	}

	class ConsistencyErrorHandler {

		private IFile file;

		public ConsistencyErrorHandler(IFile file) {
			this.file = file;
		}

		public void error(ConsistencyException exception) {
			addMarker(exception, IMarker.SEVERITY_ERROR);
		}

		public void fatalError(ConsistencyException exception) {
			addMarker(exception, IMarker.SEVERITY_ERROR);
		}

		public void warning(ConsistencyException exception) {
			addMarker(exception, IMarker.SEVERITY_WARNING);
		}

		private void addMarker(ConsistencyException e, int severity) {
			FCLCheckBuilder.this.addMarker(file, e.getMessage(),
					e.getLineNumber(), severity);
		}

	}

	class FCLCheckJob extends Job {

		private IProgressMonitor monitor;
		private IResourceDelta delta;
		private boolean isIncrementalBuild;

		public FCLCheckJob(String name) {
			super(name);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			this.monitor = monitor;

			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;

			try {
				if (isIncrementalBuild) {
					monitor.beginTask("Incremental building", 1);
					incrementalBuild(delta, monitor);
				} else {
					monitor.beginTask("Full building",
							getProject().members().length);

					long initTime = System.nanoTime();
					vpStats = new Statistics("VP");
					resourceStats = new Statistics("Resources");
					fullBuild(monitor);
					long totalTime = System.nanoTime() - initTime;
					System.out.println("===\n" + getProject().getName()
							+ " full build took: " + totalTime / 1000000000.0
							+ "s");
					System.out.println(vpStats.toString());
					System.out.println(resourceStats.toString());
					System.out.println(resourceStats.getInconsistencies().size()+" inconsistencies found.");
					System.out.println(resourceStats.getInconsistenciesAvgPerVP()+" inconsistencies per VP in average.");
					System.out.println(resourceStats.getInconsistenciesAvgPerConstraint()+" inconsistencies per Constraint in average.");
					System.out.println(resourceStats.getInconsistenciesPerCType().get(FCLDependencyType.INCLUDES)+" inconsistencies originated from INCLUDES.");
					System.out.println(resourceStats.getInconsistenciesPerCType().get(FCLDependencyType.EXCLUDES)+" inconsistencies originated from EXCLUDES.");
					System.out.println(resourceStats.getInconsistenciesPerCType().get(FCLDependencyType.MUTUALLY_EXCLUSIVE)+" inconsistencies originated from MUTUALLY EXCLUSIVE.");
					System.out.println(resourceStats.getInconsistenciesPerCType().get(FCLDependencyType.IFF)+" inconsistencies originated from IFF.");
				}
			} catch (CoreException e) {
				e.printStackTrace();
			} finally {
				monitor.done();
			}
			InconsistenciesView.getDefault().sync();
			return Status.OK_STATUS;
		}

		public void setDelta(IResourceDelta delta) {
			this.delta = delta;
		}

		public void setFullBuild(boolean b) {
			this.isIncrementalBuild = !b;
		}

		public IProgressMonitor getMonitor() {
			return this.monitor;
		}

		private void fullBuild(final IProgressMonitor monitor)
				throws CoreException {
			try {
				getProject().accept(new SampleResourceVisitor());
			} catch (CoreException e) {
			}
		}

		private void incrementalBuild(IResourceDelta delta,
				IProgressMonitor monitor) throws CoreException {
			// the visitor does the work.
			delta.accept(new SampleDeltaVisitor());
		}

		private void deleteMarkers(IFile file) {
			try {
				file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
			} catch (CoreException ce) {
			}
		}

		private String getTAGValue(List<Token> tokens) {
			for (Token token : tokens) {
				if (token.getLexeme() == TokenType.TAG)
					return token.getValue();
			}
			return null;
		}

		private boolean isSourceFile(String fileExtension) {
			if (fileExtension == null)
				return false;
			switch (fileExtension) {
			case "java":
			case "c":
			case "cpp":
			case "h":
				return true;
			}
			return false;
		}

		private Statistics resourceStats;
		private Statistics vpStats;

		public void fclCheckThis(IResource resource) throws PluginException {

			if (resource instanceof IFile) {
				IFile file = (IFile) resource;
				if (!isSourceFile(file.getFileExtension()))
					return;
				deleteMarkers(file);
				ConsistencyErrorHandler reporter = new ConsistencyErrorHandler(
						file);

				long resourceInitTime = System.nanoTime();
				List<CCVariationPoint> vps = new LinkedList<>();
				switch (resource.getFileExtension()) {
				case "java":
					try {
						vps = new JavaParser().parse(file);
					} catch (PluginException e) {
						e.printStackTrace();
					}
					break;
				case "c":
				case "cpp":
				case "h":
					try {
						vps = new CppParser().parse(file);
					} catch (PluginException e) {
						e.printStackTrace();
					}
					break;
				}
				List<ConsistencyException> iList = new ArrayList<>();
				resourceStats.setConstraintsNumber(vmodel.getVMConstraints().size());
				for (CCVariationPoint vp : vps) {
					long vpInitTime = System.nanoTime();
					if (!vp.isSingleVP(vp.getTokens()))
						continue;
					String f = getTAGValue(vp.getTokens());
					if (!isFeature(f))
						continue;
					ConsistencyException ce;
					for (FCLConstraint constraint : vmodel.getVMConstraints()) {
						switch (constraint.getType()) {
						case INCLUDES:
							if (constraint.getLeftTerm().equals(f)) {
								ce = new ConsistencyException(
										"The feature "
												+ f
												+ " includes "
												+ constraint.getRightTerm()
												+ ". Make sure you are not introducing an inconsistency. "
												+ "You should assert that "
												+ constraint.getRightTerm()
												+ " is declared.",
										vp.getLineNumber());
								iList.add(ce);
								resourceStats.increment(FCLDependencyType.INCLUDES);
								reporter.warning(ce);
							}
							break;
						case EXCLUDES:
							if (constraint.getRightTerm().equals(f)) {
								ce = new ConsistencyException(
										"The feature "
												+ f
												+ " is excluded by: "
												+ constraint.getLeftTerm()
												+ ". Make sure you are not introducing an inconsistency. "
												+ "You should assert that "
												+ constraint.getLeftTerm()
												+ " is not declared.",
										vp.getLineNumber());
								iList.add(ce);
								resourceStats.increment(FCLDependencyType.EXCLUDES);
								reporter.warning(ce);
							}
							break;
						case MUTUALLY_EXCLUSIVE:
							if (constraint.getLeftTerm().equals(f)) {
								ce = new ConsistencyException(
										"The feature "
												+ f
												+ " is mutually exclusive with "
												+ constraint.getRightTerm()
												+ ". Make sure your are not introducing an inconsistency. "
												+ "You should assert that "
												+ constraint.getRightTerm()
												+ " is NOT enabled.",
										vp.getLineNumber());
								iList.add(ce);
								resourceStats.increment(FCLDependencyType.MUTUALLY_EXCLUSIVE);
								reporter.warning(ce);
							} else if (constraint.getRightTerm().equals(f)) {
								ce = new ConsistencyException(
										"The feature "
												+ f
												+ " is mutually exclusive with "
												+ constraint.getLeftTerm()
												+ ". Make sure your are not introducing an inconsistency. "
												+ "You should assert that "
												+ constraint.getLeftTerm()
												+ " is NOT enabled.",
										vp.getLineNumber());
								iList.add(ce);
								resourceStats.increment(FCLDependencyType.MUTUALLY_EXCLUSIVE);
								reporter.warning(ce);
							}
							break;
						case IFF:
							if (constraint.getLeftTerm().equals(f)) {
								ce = new ConsistencyException(
										"The feature "
												+ f
												+ " must exist if, and only if "
												+ constraint.getRightTerm()
												+ " also exists. Make sure your are not introducing an inconsistency. "
												+ "You should assert that "
												+ constraint.getRightTerm()
												+ " IS enabled.",
										vp.getLineNumber());
								iList.add(ce);
								resourceStats.increment(FCLDependencyType.IFF);
								reporter.warning(ce);
							} else if (constraint.getRightTerm().equals(f)) {
								ce = new ConsistencyException(
										"The feature "
												+ f
												+ " must exist if, and only if "
												+ constraint.getLeftTerm()
												+ " also exists. Make sure your are not introducing an inconsistency. "
												+ "You should assert that "
												+ constraint.getLeftTerm()
												+ " IS enabled.",
										vp.getLineNumber());
								iList.add(ce);
								resourceStats.increment(FCLDependencyType.IFF);
								reporter.warning(ce);
							}
							break;
						default:
							break;
						}
					}
					vpStats.add(new TimeTaking(vpInitTime, System.nanoTime()));
					resourceStats.add(iList, vp);
				}
				resourceStats.add(new TimeTaking(resourceInitTime, System
						.nanoTime()));
				resourceStats.add(iList, file);
				// InconsistenciesView.sync();
			}
		}

		private List<String> features = getFeatures(vmodel.getVMConstraints());

		private boolean isFeature(String f) {
			if (features.contains(f)) {
				return true;
			}
			return false;
		}

		private List<String> getFeatures(List<FCLConstraint> vmConstraints) {
			List<String> l = new ArrayList<>();
			if (features == null) {
				features = new ArrayList<>();
			}
			for (FCLConstraint fclConstraint : vmConstraints) {
				if (!features.contains(fclConstraint.getLeftTerm())) {
					l.add(fclConstraint.getLeftTerm());
				}
				if (!features.contains(fclConstraint.getRightTerm())) {
					l.add(fclConstraint.getRightTerm());
				}

			}
			return l;
		}

	}

	class TimeTaking {

		public TimeTaking(String s, long init, long finish) {
			setFinishTime(finish);
			setInitTime(init);
			setProject(s);
		}

		public TimeTaking(long init, long finish) {
			setFinishTime(finish);
			setInitTime(init);
			setProject("none");
		}

		public String getProject() {
			return project;
		}

		public void setProject(String project) {
			this.project = project;
		}

		public long getInitTime() {
			return initTime;
		}

		public void setInitTime(long initTime) {
			this.initTime = initTime;
		}

		public long getFinishTime() {
			return finishTime;
		}

		public void setFinishTime(long finishTime) {
			this.finishTime = finishTime;
		}

		private String project;
		private long initTime;
		private long finishTime;

		public double getTotalTimeInSeconds() {
			return (finishTime - initTime) / 1000000000.0;
		}

		@Override
		public String toString() {
			return this.project + " took " + getTotalTimeInSeconds() + "s.";
		}
	}

	class Statistics {
		private String variable;
		private List<TimeTaking> times;
		private Map<IFile, List<ConsistencyException>> mInconsistenciesFile;
		private Map<CCVariationPoint, List<ConsistencyException>> mInconsistenciesVP;
		private int numConstraints;
		private Map<FCLDependencyType, Integer> numInconsistenciesPerCType;
		
		
		public Statistics(String var) {
			this.variable = var;
		}

		public void increment(FCLDependencyType type) {
			if(numInconsistenciesPerCType==null){
				numInconsistenciesPerCType = new HashMap<>();
			}
			if (numInconsistenciesPerCType.containsKey(type)) {
				int i = numInconsistenciesPerCType.get(type);
				numInconsistenciesPerCType.put(type, i+1);
			}else{
				numInconsistenciesPerCType.put(type, 1);
			}
		}
		
		public Map<FCLDependencyType, Integer> getInconsistenciesPerCType(){
			return numInconsistenciesPerCType;
		}
		
		public void setConstraintsNumber(int n){
			this.numConstraints = n;
		}
		
		public double getInconsistenciesAvgPerConstraint() {
			return ((double) getInconsistencies().size())/((double)numConstraints);
		}

		public double getInconsistenciesAvgPerVP() {
			double vp=(double) mInconsistenciesVP.keySet().size();
			double ce= (double)getInconsistencies().size();
			return vp/ce;
		}
		
		public void add(List<ConsistencyException> iList, IFile f) {
			if (mInconsistenciesFile ==null || mInconsistenciesFile.isEmpty()) {
				mInconsistenciesFile = new HashMap<>();
			}
			this.mInconsistenciesFile.put(f, iList);
		}
		public void add(List<ConsistencyException> iList, CCVariationPoint vp) {
			if (mInconsistenciesVP == null || mInconsistenciesVP.isEmpty()) {
				mInconsistenciesVP = new HashMap<>();
			}
			this.mInconsistenciesVP.put(vp, iList);
		}

		public void add(TimeTaking t) {
			if (times == null) {
				times = new ArrayList<>();
			}
			times.add(t);
		}

		public List<ConsistencyException> getInconsistencies(){
			ArrayList<ConsistencyException> inconsistencies = new ArrayList<>();
			for (Entry<IFile, List<ConsistencyException>> e : mInconsistenciesFile.entrySet()) {
				inconsistencies.addAll(e.getValue());
			}
			return inconsistencies;
		}
		
		public Map<IFile,List<ConsistencyException>> getInconsistenciesPerFile(){
			return mInconsistenciesFile;
		}
		
		public Map<CCVariationPoint, List<ConsistencyException>> getInconsistenciesPerVP(){
			return mInconsistenciesVP;
		}
		public double timeAvg() {
			if (times == null || times.isEmpty())
				return 0.0;
			double time = 0.0;
			for (TimeTaking t : times) {
				time += t.getTotalTimeInSeconds();
			}
			return time / times.size();
		}

		@Override
		public String toString() {
			return times.size()+" "+variable + " takes in average " + timeAvg() + "s to build.";
		}
	}

}
