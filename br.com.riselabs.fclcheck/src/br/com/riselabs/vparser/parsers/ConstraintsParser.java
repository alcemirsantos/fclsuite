package br.com.riselabs.vparser.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import br.com.riselabs.fclcheck.exceptions.PluginException;
import br.com.riselabs.fclcheck.standalone.FCLConstraint;
import br.com.riselabs.fclcheck.standalone.FCLDependencyType;
import br.com.riselabs.vparser.lexer.Lexer;
import br.com.riselabs.vparser.lexer.beans.Token;
import br.com.riselabs.vparser.lexer.enums.TokenType;

public class ConstraintsParser extends AbstractParser implements
		IConstraintsSourceParser {

	@Override
	public List<FCLConstraint> parse(IFile file) throws PluginException {
		Map<Integer, String> fmap;
		try {
			fmap = getLinesMappingFrom(file.getContents());
		} catch (CoreException e) {
			throw new PluginException("This method fails. Reasons include: \n"
					+ "- This resource does not exist.\n"
					+ "- This resource is not local. \n"
					+ "- The file-system resource is not a file. \n"
					+ "- The workspace is not in sync with the corresponding "
					+ "location in the local file system.");
		}

		List<FCLConstraint> list = new ArrayList<>();
		for (Entry<Integer, String> e: fmap.entrySet()){
			
			List<Token> result = Lexer.tokenize(e.getValue(), Lexer.FileType.CONSTRAINTS);
			
			list.add(translate(result, getBinOpPosition(result)));
		}
		System.out.println("\n:: " + file + " statistics!");
		return list;
	}


	private static int getBinOpPosition(List<Token> tokens){
		for (Token t : tokens) {
			if(t.getLexeme()==TokenType.BINOP) return tokens.indexOf(t);
		}
		return -1;
	}
	private static String getExpression(List<Token> l, int begin, int end){
		String expr="";
	
		for(int i=begin; i<end; i++){
			expr+= l.get(i).getValue();
		}
		
		return expr;
	}
	
	public static FCLConstraint translate(List<Token> tokens,
			int binOpPos) {
		FCLDependencyType dependency = null;
		String left = "";
		String right = "";

		switch (tokens.get(binOpPos).getValue()) {
		case "=>":
			left += getExpression(tokens, 0, binOpPos);//tokens.get(0).getValue() + tokens.get(1).getValue();
			
			if (tokens.get(binOpPos + 1).getValue().equals("!")) {
				right += getExpression(tokens, binOpPos+2, tokens.size());//tokens.get(binOpPos + 2).getValue();
				dependency = FCLDependencyType.EXCLUDES;
			} else {
				right += getExpression(tokens, binOpPos+1, tokens.size());//tokens.get(binOpPos + 1).getValue();
				dependency = FCLDependencyType.INCLUDES;
			}
			break;
		case "&&":
			if (tokens.get(0).getValue() == "!") {

				left += tokens.get(0).getValue() + tokens.get(1).getValue();

				if (tokens.get(binOpPos + 1).getValue() == "!") {
					right += tokens.get(binOpPos + 2).getValue();
					dependency = FCLDependencyType.EXCLUDES;
				} else {
					right += tokens.get(binOpPos + 1).getValue();
					dependency = FCLDependencyType.INCLUDES;
				}

			} else {
				left += tokens.get(0).getValue();

				if (tokens.get(binOpPos + 1).getValue() == "!") {
					right += tokens.get(binOpPos + 2).getValue();
					dependency = FCLDependencyType.EXCLUDES;
				} else {
					right += tokens.get(binOpPos + 1).getValue();
					dependency = FCLDependencyType.INCLUDES;
				}
			}
			break;
		case "||":
			if (tokens.get(binOpPos + 1).getValue() == "!") {
				if (tokens.get(0).getValue() == "!")
					dependency = FCLDependencyType.EXCLUDES;
				else
					return new FCLConstraint(tokens.get(binOpPos + 2)
							.getValue(), FCLDependencyType.INCLUDES, tokens.get(0)
							.getValue());
			} else if (tokens.get(binOpPos + 1).getValue() != "!") {
				if (tokens.get(0).getValue() == "!")
					dependency = FCLDependencyType.INCLUDES;
				else
					dependency = FCLDependencyType.MUTUALLY_EXCLUSIVE;
			}
			break;
		case "<=>":
			// TODO find the cases
			left += getExpression(tokens, 0, binOpPos);
			right += getExpression(tokens, binOpPos+1, tokens.size());
			dependency = FCLDependencyType.IFF;
			break;
		default:
			System.err.println("none of the options matched!");
		}

		return new FCLConstraint(left, dependency, right);
	}
}
