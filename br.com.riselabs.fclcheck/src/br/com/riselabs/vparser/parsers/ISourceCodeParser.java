package br.com.riselabs.vparser.parsers;

import java.util.List;

import org.eclipse.core.resources.IFile;

import br.com.riselabs.fclcheck.exceptions.PluginException;
import br.com.riselabs.vparser.beans.CCVariationPoint;

public interface ISourceCodeParser extends IParser{
	
	public List<CCVariationPoint> parse(IFile file) throws PluginException;
}
