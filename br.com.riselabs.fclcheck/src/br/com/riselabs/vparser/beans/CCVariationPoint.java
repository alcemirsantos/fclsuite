package br.com.riselabs.vparser.beans;

import java.util.List;

import org.eclipse.core.runtime.IPath;

import br.com.riselabs.vparser.lexer.beans.Token;
import br.com.riselabs.vparser.lexer.enums.TokenType;
import br.com.riselabs.vparser.parsers.ISourceCodeParser;

/**
 * This class represents each variation point found in the source code. For
 * instance, there is one always when a <code>#ifdef</code> in the source file.
 * <p>
 * Instances of this class may be created by the parsers implementing the
 * <code>{@link ISourceCodeParser}</code> interface.
 * 
 * @author Alcemir Santos
 */
public class CCVariationPoint {
	private IPath filePath;
	private int lineNumber;
	private List<Token> tokens;

	public CCVariationPoint(IPath filepath, int linenumber,
			List<Token> tokenized) {
		this.filePath = filepath;
		this.lineNumber = linenumber;
		this.tokens = tokenized;
	}

	/**
	 * @return the filePath
	 */
	public IPath getFilePath() {
		return filePath;
	}

	/**
	 * @param filePath
	 *            the filePath to set
	 */
	public void setFilePath(IPath filePath) {
		this.filePath = filePath;
	}

	/**
	 * @return the lineNumber
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * @param lineNumber
	 *            the lineNumber to set
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	/**
	 * @return the tokens
	 */
	public List<Token> getTokens() {
		return tokens;
	}

	/**
	 * @param tokens
	 *            the tokens to set
	 */
	public void setTokens(List<Token> tokens) {
		this.tokens = tokens;
	}

	/**
	 * Returns <code>true</code> whether there is only one feature in the
	 * variation point.
	 * 
	 * @param tokens
	 * @return
	 */
	public boolean isSingleVP(List<Token> tokens) {
		int count = 0;
		for (Token token : tokens) {
			if (token.getLexeme() == TokenType.TAG)
				count++;
		}
		return (count == 1) ? true : false;
	}

}
