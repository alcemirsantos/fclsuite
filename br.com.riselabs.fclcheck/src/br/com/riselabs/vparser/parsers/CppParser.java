package br.com.riselabs.vparser.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import br.com.riselabs.fclcheck.builder.ConsistencyErrorHandler;
import br.com.riselabs.vparser.lexer.Lexer;
import br.com.riselabs.vparser.lexer.beans.Token;
import br.com.riselabs.vparser.lexer.enums.LexerErrorType;

public class CppParser implements SourceCodeParser {

	@Override
	public void parse(InputStream input, ConsistencyErrorHandler reporter) {
		Map<Integer, String> inputLines = getLinesMappingFrom(input);
		printMap(inputLines);

		check(inputLines);

	}

	private void check(Map<Integer, String> inputLines) {
		for (int i = 1; i <= inputLines.size(); i++) {
			List<Token> tokens = Lexer.tokenize(inputLines.get(i),
					Lexer.FileType.CPP);
			
			if (tokens.isEmpty() || tokens == null)
				continue;
			else{
				// TODO check tokens
				System.out.println(">>> found tokens at line "+i+": ");
				for (Token token : tokens) {
					System.out.print(token.getValue()+" ");
				}
				
			}
		}
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
