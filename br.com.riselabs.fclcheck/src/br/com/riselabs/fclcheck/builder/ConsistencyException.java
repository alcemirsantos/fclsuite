package br.com.riselabs.fclcheck.builder;


public class ConsistencyException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int lineNumber;
	private String message;
	
	public ConsistencyException(String message, int lineNumber) {
		this.message = message;
		this.lineNumber = lineNumber;
	}

	public int getLineNumber() {
		return this.lineNumber;
	}
	
	public String getMessage(){
		return this.message;
	}
	
	@Override
	public String toString(){
		return "Inconsistency found at the line "+this.lineNumber+": "+message;
	}

}
