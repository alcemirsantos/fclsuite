package br.com.riselabs.vparser.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.riselabs.vparser.lexer.beans.LexerError;
import br.com.riselabs.vparser.lexer.beans.Token;
import br.com.riselabs.vparser.lexer.enums.LexerErrorType;
import br.com.riselabs.vparser.lexer.enums.TokenType;

/**
 * This class intends to be lexer model. 
 * 
 * @author Alcemir Santos
 */
public class Lexer {

	private static boolean isInputEnd = false;
	private static String input;
	private static String c = "";
	private static String buffer = "";
	private static int lookahead = -1;
	private static int status = 0;
	private static int lexemeStartPoint = 0;
	private static List<LexerError> errors;

	
	
	/**
	 * Returns a <code>{@link List}</code> of <code>{@link Token}</code> for the
	 * string used as parameter.
	 * 
	 * @param str
	 *            - the <code>String</code> to be tokenized
	 * @return - tokens recognized in the string
	 */
	public static List<Token> tokenize(String str, FileType ft) {
		init();
		Token token = null;
		setInput(str);
		List<Token> result = new ArrayList<Token>();
		errors = new ArrayList<LexerError>();

		while (!isInputEnd) {
			token = nextToken();

			if (token != null) {
				result.add(token);
			} else { // found an error
				result.clear();
				// System.gc(); /* Suggesting the execution of the Garbage
				// Collector */
				break;
			}

			switch (ft) {
			
			// testing for a C/C++ macro expression line
			case CPP:
				if (result.size() > 1)
					continue;
				else if (result.size() == 1) {
					if (result.get(0).getLexeme() != TokenType.TAG_MACRO
							&& result.get(0).getLexeme() != TokenType.EXP_MACRO) {
						result.clear();
						break;
					}
				}
				break;

			// testing for a Java macro expression line
			case JAVA:
				if (result.size() > 2)
					continue;
				else if (result.size() == 1) {
					if (result.get(0).getLexeme() != TokenType.DOUBLE_SLASH) {
						result.clear();
						break;
					} else
						continue;
				} else if (result.size() == 2) {
					if (result.get(1).getLexeme() != TokenType.TAG_MACRO
							&& result.get(1).getLexeme() != TokenType.EXP_MACRO) {
						result.clear();
						break;
					}
				}
				break;
			case CLAFER:
			case CONSTRAINTS:
			default:
				break;
			}
		}
		return result;
	}

	private static void init() {
		isInputEnd = false;
		input = "";
		c = "";
		buffer = "";
		lookahead = -1;
		status = 0;
		lexemeStartPoint = 0;
		// errors.clear();
	}

	private static void setInput(String str) {
		input = str;
	}

	public static List<LexerError> getErrors() {
		return errors;
	}

	/**
	 * Recognizes the next token in the input.
	 * 
	 * @return an instance of <code>{@link Token}</code> representing the
	 *         recognized token.
	 * 
	 */
	private static Token nextToken() {
		Token t;

		while (true) {
			switch (status) {
			case 0:
				c = nextChar();
				if (isInputEnd == true) { // end of the input
					return null;
				}
				if (compare(c, Symbols.WhiteSpace)) {
					// c is a white space
					status = 0;
					lexemeStartPoint = lookahead + 1;
				} else if (compare(c, Symbols.Return)) {
					isInputEnd = true;
					return null;
				} else if (compare(c, Symbols.Letter)) {
					// c is a letter, t may be TAG
					status = 10;
					buffer += c;
				} else if (c.equals("_")) {
					// c is a letter, t may be TAG
					status = 10;
					buffer += c;
				} else if (c.equals("#")) {
					// c is a sharp, may be a TAG_MACRO or a EXP_MACRO
					status = 20;
					buffer += c;
				} else if (c.equals("/")) {
					// c is a slash, t, may be DOUBLE_SLASH
					status = 40;
					buffer += c;
				} else if (c.equals("(")) {
					// c is a '(', t may be LEFT_PAR
					status = 42;
					buffer += c;
				} else if (c.equals(")")) {
					// c is a ')', t may be RIGHT_PAR
					status = 43;
					buffer += c;
				} else if (c.equals("&")) {
					// c is a '&', t may be BINOP
					status = 44;
					buffer += c;
				} else if (c.equals("|")) {
					// c is a '|', t may be BINOP
					status = 46;
					buffer += c;
				} else if (c.equals("!")) { // t may be a UNOP
					status = 48;
					buffer += c;
				} else if (c.equals("<")) { // t may be an <=>
					status = 50;
					buffer += c;
					break;
				} else if (c.equals("=")) { // t may be an =>
					status = 51;
					buffer += c;
					break;
				} else if (c.equals(";") || compare(c, Symbols.Return)
						|| input.length() == lookahead) { // end of the input
					isInputEnd = true;
					break;
				} else {
					// unknown character
					status = 99;
				}

				break;
			case 10: // it may be a TAG
				c = nextChar();
				if (c == null) {
					status = 11;
					break;
				}
				if (compare(c, Symbols.Letter))
					buffer += c;
				else if (compare(c, Symbols.Digit))
					buffer += c;
				else if (c.equals("_"))
					buffer += c;
				else if (c.equals("-"))
					buffer += c;
				else if (compare(c, Symbols.Return)
						|| input.length() == lookahead) { // end of the input
					isInputEnd = true;
					status = 11;
				} else
					status = 11;
				break;
			case 11: // end of the token TAG
				rollback();
				t = new Token(TokenType.TAG, buffer, lexemeStartPoint);
				reset();
				return t;

			case 20:// it may be a TAG_MACRO or a EXP_MACRO
				c = nextChar();
				if (c == null) {
					return null;
				}
				if (c.equals("i")) {
					buffer += c;
					status = 21;
				} else if (c.equals("e")) {
					buffer += c;
					status = 28;
				} else {
					// error: esperava um i ou e
					errors.add(new LexerError(lookahead, "'i' ou 'e'", c,
							LexerErrorType.WrongCharacter));
					rollback();
					return null;
				}
				break;
			case 21:
				c = nextChar();
				if (c.equals("f")) {
					buffer += c;
					status = 22;
				} else {
					// error: esperava um f
					errors.add(new LexerError(lookahead, "f", c,
							LexerErrorType.WrongCharacter));
					rollback();
					return null;
				}
				break;
			case 22:
				c = nextChar();
				if (compare(c, Symbols.WhiteSpace)) { // #if
					status = 23;
				} else if (c.equals("d")) { // #ifd
					buffer += c;
					status = 24;
				} else if (c.equals("n")) { // #ifn
					buffer += c;
					status = 25;
				} else {
					// error: esperava espa��o branco, n ou d
					errors.add(new LexerError(lookahead, "' ', 'n', or 'd'", c,
							LexerErrorType.WrongCharacter));
					rollback();
					return null;
				}
				break;
			case 23:
				// rollback();
				t = new Token(TokenType.EXP_MACRO, buffer, lexemeStartPoint);
				reset();
				return t;
			case 24:
				c = nextChar();
				if (c.equals("e")) { // #ifde or #ifnde or #elifnde or #elifde
					buffer += c;
					status = 26;
				} else {
					// error: esperava e
					errors.add(new LexerError(lookahead, "'e'", c,
							LexerErrorType.WrongCharacter));
					rollback();
					return null;
				}
				break;
			case 25:
				c = nextChar();
				if (c.equals("d")) { // #ifnd
					buffer += c;
					status = 24;
				} else {
					// error: esperava n
					errors.add(new LexerError(lookahead, "'n'", c,
							LexerErrorType.WrongCharacter));
					rollback();
					return null;
				}
				break;
			case 26:
				c = nextChar();
				if (c.equals("f")) { // #ifdef or #ifndef or #elifndef or
										// #elifdef
					buffer += c;
					status = 27;
				} else {
					// error: esperava f
					errors.add(new LexerError(lookahead, "'f'", c,
							LexerErrorType.WrongCharacter));
					rollback();
					return null;
				}
				break;
			case 27:
				// rollback();
				t = new Token(chooseTokenType(buffer), buffer, lexemeStartPoint);
				reset();
				return t;
			case 28:
				c = nextChar();
				if (c.equals("l")) { // #el
					buffer += c;
					status = 29;
				} else {
					// error: esperava l
					errors.add(new LexerError(lookahead, "'l'", c,
							LexerErrorType.WrongCharacter));
					rollback();
					return null;
				}
				break;
			case 29:
				c = nextChar();
				if (c.equals("i")) { // #eli
					buffer += c;
					status = 30;
				} else {
					// error: esperava i
					errors.add(new LexerError(lookahead, "'i'", c,
							LexerErrorType.WrongCharacter));
					rollback();
					return null;
				}
				break;
			case 30:
				c = nextChar();
				if (c.equals("f")) {// #elif
					buffer += c;
					status = 31;
				} else {
					// error: esperava f
					errors.add(new LexerError(lookahead, "'f'", c,
							LexerErrorType.WrongCharacter));
					rollback();
					return null;
				}
				break;
			case 31:
				c = nextChar();
				if (c.equals("n")) { // #elifn
					buffer += c;
					status = 25;
				} else if (c.equals("d")) { // #elifd
					buffer += c;
					status = 24;
				} else if (compare(c, Symbols.WhiteSpace)) {
					status = 27;
				} else {
					// error: esperava espa��o, n ou d
					errors.add(new LexerError(lookahead, "' ', 'n', or 'd'", c,
							LexerErrorType.WrongCharacter));
					rollback();
					return null;
				}
				break;
			case 40:
				c = nextChar();
				if (c == null) {
					return null;
				}
				if (c.equals("/")) {
					buffer += c;
					status = 41;
				} else {
					// error: esperava /
					errors.add(new LexerError(lookahead, "'/'", c,
							LexerErrorType.WrongCharacter));
					rollback();
					return null;
				}
				break;
			case 41: // end of a DOUBLE_SLASH
				// rollback();
				t = new Token(TokenType.DOUBLE_SLASH, buffer, lexemeStartPoint);
				reset();
				return t;
			case 42:// end of a LEFT_PAR
				// rollback();
				t = new Token(TokenType.LEFT_PAR, buffer, lexemeStartPoint);
				reset();
				return t;
			case 43:
				c = nextChar();
				if (c == null || compare(c, Symbols.Return)
						|| input.length() == lookahead) {
					isInputEnd = true;
					status = 431;
				} else {
					status = 431;
					rollback();
				}
				break;
			case 431: // end of a RIGHT_PAR
				// rollback();
				t = new Token(TokenType.RIGHT_PAR, buffer, lexemeStartPoint);
				reset();
				return t;
			case 44:
				c = nextChar();
				if (c == null) {
					return null;
				}
				if (c.equals("&")) {
					buffer += c;
					status = 45;
				} else {
					// error: esperava &
					errors.add(new LexerError(lookahead, "'&'", c,
							LexerErrorType.WrongCharacter));
					rollback();
					return null;
				}
				break;
			case 45: // end of a AND
				// rollback();
				t = new Token(TokenType.BINOP, buffer, lexemeStartPoint);
				reset();
				return t;
			case 46:
				c = nextChar();
				if (c == null)
					return null;
				else if (c.equals("|")) {
					buffer += c;
					status = 47;
				} else {
					// error: esperava |
					errors.add(new LexerError(lookahead, "'|'", c,
							LexerErrorType.WrongCharacter));
					rollback();
					return null;
				}
				break;
			case 47: // end of a OR
				// rollback();
				t = new Token(TokenType.BINOP, buffer, lexemeStartPoint);
				reset();
				return t;
			case 48:
				c = nextChar();
				if (compare(c, Symbols.Letter) || c.equals("(")
						|| compare(c, Symbols.WhiteSpace)) {
					status = 49;
				} else {
					// error: expected TAG or (
					errors.add(new LexerError(lookahead, "<Letter> or '('", c,
							LexerErrorType.WrongCharacter));
					rollback();
					return null;
				}
			case 49:
				rollback();
				t = new Token(TokenType.UNOP, buffer, lexemeStartPoint);
				reset();
				return t;
				
			case 50:
				c=nextChar();
				if(c.equals("=")) {
					status = 51;
					buffer += c;
				}
				else{
					// error: expected <=> 
					errors.add(new LexerError(lookahead, "'='", c, LexerErrorType.WrongCharacter));
					rollback();
					return null;
				}
				break;
			case 51:
				c=nextChar();
				if(c.equals(">")){
					status = 52;
					buffer += c;
				}
				else{
					// error: expected <=> or =>
					errors.add(new LexerError(lookahead, "'>'", c, LexerErrorType.WrongCharacter));
					rollback();
					return null;
				}
				break;
			case 52:
				t = new Token(TokenType.BINOP, buffer, lexemeStartPoint);
				reset();
				return t;
			case 99: // Error: Unknown character
				errors.add(new LexerError(lookahead,
						"<Letter>, '/', '#', '&' or '|'", c,
						LexerErrorType.InvalidCharacter));
				rollback();
				buffer = "";
				status = 0;
				return null;
			default:
				break;
			} // end switch
		} // end while
	} // end method

	/**
	 * Recognizes type of the token in the parameter <code>buffer</code>.
	 * 
	 * @param buffer
	 *            - the buffer to be recognized.
	 * @return the type of the token.
	 */
	private static TokenType chooseTokenType(String buffer) {
		if (buffer.equals("#elif") || buffer.equals("#if")|| buffer.equals("defined"))
			return TokenType.EXP_MACRO;
		else if (buffer.equals("#ifdef") || buffer.equals("#ifndef")
				|| buffer.equals("#elifdef") || buffer.equals("#elifndef"))
			return TokenType.TAG_MACRO;
		else
			// TODO error: não devia ter outro
			errors.add(new LexerError(lexemeStartPoint,
					"'#if', '#elif', '#ifdef', "
							+ "'#ifndef', '#elifdef' or '#elifndef' ", c,
					LexerErrorType.InvalidMacro));
		return null;
	}

	private static void reset() {
		lexemeStartPoint = lookahead + 1;
		status = 0;
		c = "";
		buffer = "";
	}

	private static void rollback() {
		lookahead--;
	}

	/**
	 * Returns the character in the <code>lookahead</code> position.
	 * 
	 * @return
	 */
	private static String nextChar() {
		lookahead++;
		if (input.length() == lookahead) { // end of the input
			isInputEnd = true;
			return null;
		} else
			return Character.toString(input.charAt(lookahead));
	}

	/**
	 * Returns whether the parameter character <code>c</code> macthes the
	 * provided <code>pattern</code>.
	 * 
	 * @param c
	 *            - the character to be checked
	 * @param pattern
	 *            - the patter to be compared with
	 * @return <code>true</code>, if <code>c</code> matches the
	 *         <code>pattern</code> and <code>false</code> otherwise.
	 */
	private static boolean compare(String c, String padrao) {
		Pattern p = Pattern.compile(padrao);
		Matcher m = p.matcher(c);
		if (m.matches()) {
			return true;
		}
		return false;
	}

	public enum FileType{CPP, JAVA, CLAFER, CONSTRAINTS}
}
