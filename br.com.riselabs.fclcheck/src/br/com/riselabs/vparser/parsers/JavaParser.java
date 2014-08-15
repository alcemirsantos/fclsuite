package br.com.riselabs.vparser.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import br.com.riselabs.fclcheck.builder.ConsistencyErrorHandler;

public class JavaParser implements SourceCodeParser {

	@Override
	public void parse(InputStream input, ConsistencyErrorHandler reporter) {
		// TODO Auto-generated method stub
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
