package br.com.riselabs.vparser.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import br.com.riselabs.fclcheck.exceptions.PluginException;
import br.com.riselabs.vparser.beans.CCVariationPoint;

public class JavaParser implements ISourceCodeParser {

	@Override
	public List<CCVariationPoint> parse(IFile input) throws PluginException {
		// TODO parse input file to find variation points.
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(input.getContents()));
		} catch (CoreException e) {
			throw new PluginException(
					"This method fails. Reasons include: \n"
							+ "This resource does not exist.\n"
							+ "This resource is not local. \n"
							+ "The file-system resource is not a file. \n"
							+ "The workspace is not in sync with the corresponding "
							+ "location in the local file system.");
		}
		StringBuilder out = new StringBuilder();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				out.append(line);
			}
			// Prints the string content read from input stream
			System.out.println(out.toString()); 
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


}
