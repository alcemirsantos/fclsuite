package br.com.riselabs.vparser.lexer.beans;

import br.com.riselabs.vparser.lexer.enums.TokenType;

public class Token {

	private TokenType lexeme;
	private String value;
	private int startColumn;

	public Token(TokenType lexeme, String buffer, int startColumn) {
		setLexeme(lexeme);
		setValue(buffer);
		setStartColumn(startColumn);
	}

	public TokenType getLexeme() {
		return lexeme;
	}

	public void setLexeme(TokenType lexeme) {
		this.lexeme = lexeme;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getStartColumn() {
		return startColumn;
	}

	public void setStartColumn(int startColumn) {
		this.startColumn = startColumn;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (!(o instanceof Token))
			return false;
		Token other = (Token) o;
		if (getLexeme() == other.getLexeme()
				&& getValue().equals(other.getValue())
				&& getStartColumn() == other.getStartColumn())
			return true;
		else
			return false;
	}
	
	@Override
	public String toString(){
		return "Value: "+getValue()+"; Type: "+getLexeme()+"; Start column: "+getStartColumn();
	}
}
