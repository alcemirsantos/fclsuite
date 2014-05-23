package br.com.riselabs.vparser;

import java.util.LinkedList;
import java.util.List;

public class AbstractParser {

	LinkedList<Token> tokens;
	Token lookahead;
	  
	public static void main(String[] args ){
		LinkedList<Token> mTokens = new LinkedList<Token>();
		
//		mTokens.add(new Token(Token.NUMBER, sequence));
		
		new AbstractParser().parse(mTokens);
	}
	  public void parse(List<Token> tokens)
	  {
	    this.tokens = new LinkedList<Token>(tokens);
	    lookahead = this.tokens.getFirst();
	    expression();
	  }
	  private void nextToken()
	  {
	    tokens.pop();
	    // at the end of input we return an epsilon token
	    if (tokens.isEmpty())
	      lookahead = new Token(Token.EPSILON, "");
	    else
	      lookahead = tokens.getFirst();
	  }
	  
	  private void expression()
	  {
	    // expression -> signed_term sum_op
	    signedTerm();
	    sumOp();
	  }
	  
	  private void sumOp()
	  {
	    if (lookahead.token == Token.PLUSMINUS)
	    {
	      // sum_op -> PLUSMINUS term sum_op
	      nextToken();
	      term();
	      sumOp();
	    }
	    else
	    {
	      // sum_op -> EPSILON
	    }
	  }
	  
	  private void signedTerm()
	  {
	    if (lookahead.token == Token.PLUSMINUS)
	    {
	      // signed_term -> PLUSMINUS term
	      nextToken();
	      term();
	    }
	    else
	    {
	      // signed_term -> term
	      term();
	    }
	  }
	  
	  private void term()
	  {
	    // term -> factor term_op
	    factor();
	    termOp();
	  }

	  private void termOp()
	  {
	    if (lookahead.token == Token.MULTDIV)
	    {
	      // term_op -> MULTDIV factor term_op
	      nextToken();
	      factor();
	      termOp();
	    }
	    else
	    {
	      // term_op -> EPSILON
	    }
	  }
	  
	  private void factor()
	  {
	    argument();
	    factorOp();
	  }

	  private void factorOp()
	  {
	    if (lookahead.token == Token.RAISED)
	    {
	      // factor_op -> RAISED expression
	      nextToken();
	      expression();
	    }
	    else
	    {
	      // factor_op -> EPSILON
	    }
	  }

	  
	  private void argument()
	  {
	    if (lookahead.token == Token.FUNCTION)
	    {
	      // argument -> FUNCTION argument
	      nextToken();
	      argument();
	    }
	    else if (lookahead.token == Token.OPEN_BRACKET)
	    {
	      // argument -> OPEN_BRACKET sum CLOSE_BRACKET
	      nextToken();
	      expression();

	      if (lookahead.token != Token.CLOSE_BRACKET)
			try {
				throw new ParserException("Closing brackets expected and "
				  + lookahead.sequence + " found instead");
			} catch (ParserException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}

	      nextToken();
	    }
	    else
	    {
	      // argument -> value
	      value();
	    }
	  }

	  
	  private void value()
	  {
	    if (lookahead.token == Token.NUMBER)
	    {
	      // argument -> NUMBER
	      nextToken();
	    }
	    else if (lookahead.token == Token.VARIABLE)
	    {
	      // argument -> VARIABLE
	      nextToken();
	    }
	    else
	    {
	      try {
			throw new ParserException(
			    "Unexpected symbol "+lookahead.sequence+" found");
		} catch (ParserException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	    }
	  }
	  
	   class Token
	  {
	    public static final int EPSILON = 0;
	    public static final int PLUSMINUS = 1;
	    public static final int MULTDIV = 2;
	    public static final int RAISED = 3;
	    public static final int FUNCTION = 4;
	    public static final int OPEN_BRACKET = 5;
	    public static final int CLOSE_BRACKET = 6;
	    public static final int NUMBER = 7;
	    public static final int VARIABLE = 8;

	    public final int token;
	    public final String sequence;

	    public Token(int token, String sequence)
	    {
	      super();
	      this.token = token;
	      this.sequence = sequence;
	    }
	  }
	   
	   class ParserException extends Exception{
		   public ParserException(String message) {
			   super(message);
		}
	   }
	   
}
