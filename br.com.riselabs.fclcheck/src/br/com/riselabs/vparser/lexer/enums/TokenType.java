package br.com.riselabs.vparser.lexer.enums;

public enum TokenType {
	TAG("<Feature identifier>"), 
	EXP_MACRO("<Expression Macro>"), 
	TAG_MACRO("<Tag Macro>"), 
	DOUBLE_SLASH("<Comment begining...>"), 
	LEFT_PAR("<Left Paragraph>"), 
	RIGHT_PAR("<Right Paragraph>"), 
	BINOP("<Binary Operator"),
	UNOP("<Unary Operator");
	
	private String description;
	
	TokenType(String description){
		this.description = description;
	}
	
	public String getDescription(){return this.description;}

}
