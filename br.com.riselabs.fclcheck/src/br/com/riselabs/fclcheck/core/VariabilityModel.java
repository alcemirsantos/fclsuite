package br.com.riselabs.fclcheck.core;

import java.util.LinkedList;
import java.util.List;

import javax.print.attribute.standard.Severity;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import br.com.riselabs.fclcheck.builder.FCLCheckBuilder;
import br.com.riselabs.fclcheck.builder.FCLCheckNature;
import br.com.riselabs.fclcheck.exceptions.PluginException;
import br.com.riselabs.fclcheck.standalone.FCLConstraint;
import br.com.riselabs.vparser.parsers.ConstraintsParser;
import br.com.riselabs.vparser.parsers.FCLParser;

public class VariabilityModel {
	
	private IProject project;
	private List<FCLConstraint> vmConstraints = new LinkedList<>();
	private static VariabilityModel sharedInstance;
	
	private VariabilityModel() {}
	
	public static VariabilityModel getInstance(){
		if (sharedInstance == null) {
			sharedInstance = new VariabilityModel();
		}
		return sharedInstance;
	}
	
	public List<FCLConstraint> getVMConstraints(){
		if (vmConstraints.isEmpty() || vmConstraints == null) {
			loadVMConstraints();
		}
		return vmConstraints;
	}
	
	private IFile getConstraintsFile() {
		return getProject().getFile(FCLCheckNature.CONSTRAINTS_FILENAME);
	}
	
	private void loadVMConstraints() {
		try {
			switch (getConstraintsFile().getFileExtension()) {
			case "fcl":
				vmConstraints = new FCLParser().parse(getConstraintsFile());
				break;
			case "constraints":
				vmConstraints = new ConstraintsParser().parse(getConstraintsFile());
				break;
			default:
				throw new PluginException("Sorry! It is not possible to parse "
						+ getConstraintsFile().getFileExtension() + " yet.");
			}
		} catch (PluginException e) {
			try {
				String message = "A constraints file is missing.";
				addErrorMarker(getProject(), message);
				throw new PluginException(message);
			} catch (CoreException e1) {
				e1.printStackTrace();
			} catch (PluginException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
	
	private void addErrorMarker(IProject project, String message)
			throws CoreException {
		IMarker marker = project.createMarker(FCLCheckBuilder.MARKER_TYPE);
		marker.setAttribute(IMarker.SEVERITY, Severity.ERROR.getValue());
		marker.setAttribute(IMarker.MESSAGE, message);
	}
	
	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

}
