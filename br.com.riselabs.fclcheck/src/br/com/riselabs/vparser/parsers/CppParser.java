package br.com.riselabs.vparser.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import br.com.riselabs.fclcheck.exceptions.PluginException;
import br.com.riselabs.vparser.beans.CCVariationPoint;
import br.com.riselabs.vparser.lexer.Lexer;
import br.com.riselabs.vparser.lexer.beans.Token;

public class CppParser implements ISourceCodeParser {

	@Override
	public List<CCVariationPoint> parse(IFile file) throws PluginException {
		Map<Integer, String> inputLines;
		try {
			inputLines = getLinesMappingFrom(file.getContents());
		} catch (CoreException e) {
			throw new PluginException(
					"This method fails. Reasons include: \n"
							+ "This resource does not exist.\n"
							+ "This resource is not local. \n"
							+ "The file-system resource is not a file. \n"
							+ "The workspace is not in sync with the corresponding "
							+ "location in the local file system.");
		}
//		printMap(inputLines);

		Map<Integer, List<Token>> map = getVariationPointsTokenized(inputLines);

		List<CCVariationPoint> list = new ArrayList<>();
		for (Entry<Integer, List<Token>> e: map.entrySet()) {
			list.add(new CCVariationPoint(file.getLocation(), e.getKey(), e.getValue()));
		}
		
		return list; 
	}

	private Map<Integer, List<Token>> getVariationPointsTokenized(Map<Integer, String> inputLines) {
		HashMap<Integer, List<Token>> map = new HashMap<>();
		for (int i = 1; i <= inputLines.size(); i++) {
			List<Token> tokens = Lexer.tokenize(inputLines.get(i),
					Lexer.FileType.CPP);
			if (tokens.isEmpty() || tokens == null)
				continue;
			else{
				map.put(i, tokens); 
			}
		}
		return map;
	}

	private void printMap(Map<Integer, String> inputLines) {
		for (int i = 1; i <= inputLines.size(); i++) {
			System.out.print("line " + i + ": " + inputLines.get(i));
		}
	}

	/**
	 * Reads the <code>InputSteam</code> provided and returns a map between line
	 * number and its content.
	 * 
	 * @param input
	 *            - the <code>InputStream</code> to be read.
	 * @return the map between line and its content.
	 */
	private Map<Integer, String> getLinesMappingFrom(InputStream input) {
		Map<Integer, String> map = new HashMap<>();
		int i, lineCounter = 0;
		char c;
		String buffer = "";
		try {
			// reads till the end of the stream
			while ((i = input.read()) != -1) {
				// converts integer to character
				c = (char) i;

				if (c == '\n') {
					lineCounter++;
					buffer += c;
					// maps each line
					map.put(lineCounter, buffer);
					buffer = "";
				} else {
					buffer += c;
				}
			}
		} catch (Exception e) {
			// if any I/O error occurs
			e.printStackTrace();
		} finally {

			// releases system resources associated with this stream
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return map;
	}

	@Deprecated
	private void printInputStream(InputStream input) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
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

	}

}
