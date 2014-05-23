package br.com.riselabs.vparser.lexer.beans;

import br.com.riselabs.vparser.lexer.enums.LexerErrorType;

/**
 * Representação dos erros encontrados pelo Compilador
 * 
 * @author Alcemir Rodrigues Santos  (06N10340) 
 * @author Janielton de Sousa Veloso (06N10242)
 * 
 */
public class LexerError {

	private int column;
	private String charExpected;
	private String actualChar;
	private LexerErrorType type;
	private String message;

	public LexerError() {
		this(0," ", " ", LexerErrorType.Unknown);
	}
	
	public LexerError(int column, String expectedChararacter, String actualChararacter, LexerErrorType type) {
		setColumn(column);
		setCharExpected(expectedChararacter);
		setActualChar(actualChararacter);
		setType(type);
		setMessage(type.getMessage());
	}
	
	private void setActualChar(String c) {
		this.actualChar = c;
	}
	
	public String getActualChar(){
		return this.actualChar;
	}

	public int getColumn() {
		return column;
	}
	
	public void setColumn(int column) {
		this.column = column;
	}
	
	public String getCharExpected() {
		return charExpected;
	}
	
	public void setCharExpected(String string) {
		this.charExpected = string;
	}
	
	public LexerErrorType getType() {
		return type;
	}
	
	public void setType(LexerErrorType type) {
		this.type = type;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}		
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (!(o instanceof LexerError))
			return false;
		LexerError other = (LexerError) o;
		if (getColumn() == other.getColumn()
				&& getActualChar().equals(other.getActualChar())
				&& getMessage() == other.getMessage()
				&& getType() == other.getType())
			return true;
		else
			return false;
	}
	@Override
	public String toString(){
		return this.message+" We expected "+this.charExpected+", but was '"+this.actualChar+"'.";
	}
}//fim da classe
