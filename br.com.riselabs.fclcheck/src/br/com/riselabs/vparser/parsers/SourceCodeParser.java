package br.com.riselabs.vparser.parsers;

import java.io.InputStream;

import br.com.riselabs.fclcheck.builder.ConsistencyErrorHandler;

public interface SourceCodeParser {
	
	public void parse(InputStream input, ConsistencyErrorHandler reporter);
}
