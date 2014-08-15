package br.com.riselabs.fclcheck.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.xml.sax.SAXException;

public class ConsistencyErrorHandler {

	private IFile file;

	public ConsistencyErrorHandler(IFile file) {
		this.file = file;
	}

	private void addMarker(ConsistencyException e, int severity) {
		FCLCheckBuilder.addMarker(file, e.getMessage(),
				e.getLineNumber(), severity);
	}

	public void error(ConsistencyException exception) throws SAXException {
		addMarker(exception, IMarker.SEVERITY_ERROR);
	}

	public void fatalError(ConsistencyException exception) throws SAXException {
		addMarker(exception, IMarker.SEVERITY_ERROR);
	}

	public void warning(ConsistencyException exception) throws SAXException {
		addMarker(exception, IMarker.SEVERITY_WARNING);
	}

}
