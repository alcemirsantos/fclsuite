package br.com.riselabs.vparser.lexer;

public enum MacrosGrammar {
	
	/*
	Variability Point Sentence = 
	 	-> COMMENT? TAG_MACRO tag 
		 | COMMENT? EXP_MACRO expression
	
	expression -> UNOP? sentence ( BINOP expression )?
	
	sentence -> tag   | LEFTPAR expression RIGHTPAR
	
	tag -> lexrule+
	
	lexrule -> (a..z) | (A..Z)
	
	UNOP -> "!"
	BINOP -> "&&" | "||"
	LEFTPAR -> "("
	RIGHTPAR -> ")"
	COMMENT -> "//"
	
	TAG_MACRO -> "#ifdef" | "#ifndef" | "#elifdef" | "#elifndef" 
	EXP_MACRO -> "#if" | "#elif"
	
	 */

	LEFT_PAR("(", "<Left Parenthesis>"), 
	RIGHT_PAR(")", "<Right Parenthesis>"),
	
	OR("||","<Binary Operator OR>"),
	AND("&&","<Binary Operator AND>"),
	
	NOT("!","<Unary Operator NOT"),
	
	LINE_OPENNER("//", "<Line Openner>"),
	
	IF_EXP_MACRO("#ifdef","<Expression Macro IF>"),
	ELIF_EXP_MACRO("#elif","Expression Macro ELIF"),
	
	
	IFDEF_TAG_MACRO("#ifdef","Tag Macro IFDEF"),
	IFNDEF_TAG_MACRO("#ifndef","Tag Macro IFNDEF"),
	ELIFDEF_TAG_MACRO("#elifdef","Tag Macro ELIFDEF"),
	ELIFNDEF_TAG_MACRO("#elifndef","Tag Macro ELIFNDEF");
	
	private String value;
	private String description;

	MacrosGrammar(String value, String description) {
		this.value = value;
		this.description = description;
	}

	private String getValue() {
		return this.value;
	}

	private String getDescription() {
		return this.description;
	}
}
