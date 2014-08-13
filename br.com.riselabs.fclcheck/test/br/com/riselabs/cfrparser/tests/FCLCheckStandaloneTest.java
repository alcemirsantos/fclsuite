/**
 * 
 */
package br.com.riselabs.cfrparser.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.riselabs.fclcheck.standalone.FCLCheckStandaloneMain;
import br.com.riselabs.fclcheck.standalone.FCLConstraint;
import br.com.riselabs.vparser.lexer.beans.Token;
import br.com.riselabs.vparser.lexer.enums.TokenType;

/**
 * @author Alcemir Santos
 *
 */
public class FCLCheckStandaloneTest {

	FCLCheckStandaloneMain main;
	List<Token> tokens;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		tokens = new ArrayList<Token>();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * !P => !Q
	 */
	@Test
	public void testTranslateToFCLConstraint00() {
		tokens.add(Factory.getTokenNot());
		tokens.add(Factory.getTokenTag("P"));
		tokens.add(Factory.getTokenImplies());
		tokens.add(Factory.getTokenNot());
		tokens.add(Factory.getTokenTag("Q"));
		
		FCLConstraint constraint = FCLCheckStandaloneMain.translateToFCLConstraint(tokens, 2);
		
		assertEquals("",  "!P excludes Q;", constraint.toString());
	}
	
	/**
	 * !P => Q
	 */
	@Test
	public void testTranslateToFCLConstraint01() {
		tokens.add(Factory.getTokenNot());
		tokens.add(Factory.getTokenTag("P"));
		tokens.add(Factory.getTokenImplies());
		tokens.add(Factory.getTokenTag("Q"));
		
		FCLConstraint constraint = FCLCheckStandaloneMain.translateToFCLConstraint(tokens, 2);
		
		assertEquals("",  "!P includes Q;", constraint.toString());
	}
	
	/**
	 * !(P) => Q
	 */
	@Test
	public void testTranslateToFCLConstraint01a() {
		tokens.add(Factory.getTokenNot());
		tokens.add(Factory.getTokenOpenPar());
		tokens.add(Factory.getTokenTag("P"));
		tokens.add(Factory.getTokenClosePar());
		tokens.add(Factory.getTokenImplies());
		tokens.add(Factory.getTokenTag("Q"));
		
		FCLConstraint constraint = FCLCheckStandaloneMain.translateToFCLConstraint(tokens, 4);
		
		assertEquals("",  "!(P) includes Q;", constraint.toString());
	}
	
	/**
	 * P => !Q
	 */
	@Test
	public void testTranslateToFCLConstraint02() {
		tokens.add(Factory.getTokenTag("P"));
		tokens.add(Factory.getTokenImplies());
		tokens.add(Factory.getTokenNot());
		tokens.add(Factory.getTokenTag("Q"));
		
		FCLConstraint constraint = FCLCheckStandaloneMain.translateToFCLConstraint(tokens, 1);
		
		assertEquals("",  "P excludes Q;", constraint.toString());
	}
	
	/**
	 * P => !Q
	 */
	@Test
	public void testTranslateToFCLConstraint02a() {
		tokens.add(Factory.getTokenTag("P"));
		tokens.add(Factory.getTokenImplies());
		tokens.add(Factory.getTokenNot());
		tokens.add(Factory.getTokenOpenPar());
		tokens.add(Factory.getTokenTag("Q"));
		tokens.add(Factory.getTokenClosePar());
		FCLConstraint constraint = FCLCheckStandaloneMain.translateToFCLConstraint(tokens, 1);
		
		assertEquals("",  "P excludes (Q);", constraint.toString());
	}
	
	/**
	 * P => Q
	 */
	@Test
	public void testTranslateToFCLConstraint03() {
		tokens.add(Factory.getTokenTag("P"));
		tokens.add(Factory.getTokenImplies());
		tokens.add(Factory.getTokenTag("Q"));
		
		FCLConstraint constraint = FCLCheckStandaloneMain.translateToFCLConstraint(tokens, 1);
		
		assertEquals("",  "P includes Q;", constraint.toString());
	}
	
	
	/**
	 * !P && !Q
	 */
	@Test
	public void testTranslateToFCLConstraint04() {
		tokens.add(Factory.getTokenNot());
		tokens.add(Factory.getTokenTag("P"));
		tokens.add(Factory.getTokenAnd());
		tokens.add(Factory.getTokenNot());
		tokens.add(Factory.getTokenTag("Q"));
		
		FCLConstraint constraint = FCLCheckStandaloneMain.translateToFCLConstraint(tokens, 2);
		
		assertEquals("",  "!P excludes Q;", constraint.toString());
	}
	
	static class Factory{
		static Token getTokenTag(String value){
			return new Token(TokenType.TAG, value, 0); 
		}
		
		/**
		 * @return
		 */
		 static Token getTokenOpenPar() {
			return new Token(TokenType.LEFT_PAR, "(", 0);
		}

		/**
		 * @return
		 */
		static Token getTokenClosePar() {
			return new Token(TokenType.RIGHT_PAR, ")", 0);
		}

		static Token getTokenImplies(){
			return new Token(TokenType.BINOP, "=>", 0); 
		}
		
		static Token getTokenNot(){
			return new Token(TokenType.UNOP, "!", 0); 
		}
		
		static Token getTokenAnd(){
			return new Token(TokenType.BINOP, "&&", 0); 
		}
		
		static Token getTokenOr(){
			return new Token(TokenType.BINOP, "||", 0); 
		}
	}
	
}
