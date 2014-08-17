package br.com.riselabs.vparser.parsers;

import java.util.List;

import org.eclipse.core.resources.IFile;

import br.com.riselabs.fclcheck.exceptions.PluginException;

public interface IParser {

	/**
	 * Parses a file and return a list of a type. 
	 * @param file
	 * @return
	 * @throws PluginException
	 */
	public List<?> parse(IFile file) throws PluginException;

}
