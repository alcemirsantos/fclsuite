package br.com.riselabs.vparser.lexer.enums;

/**
 * Tipo de cada Erro encontrado pelo Compilador.
 * 
 * @author Alcemir Rodrigues Santos  (06N10340) 
 * @author Janielton de Sousa Veloso (06N10242)
 *
 */
public enum LexerErrorType {

	InvalidCharacter ("Invalid character!"),
	WrongCharacter ("Expected another character!"),
	NumberExpected ("Expected number instead!"),
	LetterExpected ("Expected letter instead!"),
	InvalidToken ("Invalid token!"), 
	Unknown ("Unknown error!"),
	InvalidMacro ("Invalid conditional compilation macro!"); 
	
	private String message;
	
	LexerErrorType(String msg){
		this.message = msg;
	}
	
	public String getMessage(){return this.message;}
}
