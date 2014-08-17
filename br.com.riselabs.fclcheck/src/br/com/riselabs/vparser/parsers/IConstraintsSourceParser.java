package br.com.riselabs.vparser.parsers;

import java.util.List;

import org.eclipse.core.resources.IFile;

import br.com.riselabs.fclcheck.exceptions.PluginException;
import br.com.riselabs.fclcheck.standalone.FCLConstraint;

public interface IConstraintsSourceParser extends IParser{
	public List<FCLConstraint> parse(IFile file) throws PluginException;
}
