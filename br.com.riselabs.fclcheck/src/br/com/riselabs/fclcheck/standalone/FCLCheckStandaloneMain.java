/**
 * 
 */
package br.com.riselabs.fclcheck.standalone;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import br.com.riselabs.vparser.lexer.Lexer;
import br.com.riselabs.vparser.lexer.beans.Token;
import br.com.riselabs.vparser.lexer.enums.TokenType;

import br.com.riselabs.fclcheck.standalone.cfrparser.ClaferParser;
/**
 * @author Alcemir Santos
 * 
 */
public class FCLCheckStandaloneMain {
	private final static String WORKSPACE = "/Users/alcemir/Documents/workspace/";

	private final static String PROJECT_HOME = "/Users/alcemir/Documents/workspace/testeJ/";
	private static String cfrFile = "linux-2.6.32.cfr";
	private static String project = "SNadi-Files/";

	private static FileWriter writer;

	private static List<FCLWrapper> database;

	public static void main(String[] args) throws IOException {
		String[] files = { "linux-crosstree.constraints",
				"ecos-crosstree.constraints", "uclibc-crosstree.constraints",
				"busybox-crosstree.constraints" };

		for (String file : files) {
			ConstraintFileReader.load(WORKSPACE + project + file);
			List<String> constraints = ConstraintFileReader.getLines();
			List<List<Token>> cTokenized = new ArrayList<List<Token>>();
			for (String string : constraints) {
				List<Token> result = Lexer.tokenize(string, Lexer.FileType.CONSTRAINTS);
				cTokenized.add(result);
				makeFCLConstraint(result);
			}
			System.out.println("\n:: "+file+" statistics!");
			doContraintsStats(cTokenized);
		}
	}

	public static void parseClaferFile() throws IOException {
		// load the "clafer.cfr" model
		ClaferParser.load(PROJECT_HOME + cfrFile);

		// parse the model looking for constraints
		ClaferParser.parse();

		// get the set of constraints
		List<String> constraints = ClaferParser.getConstraints();

		// Writes the content to the file
		String filename = "constraints.txt";
		writer = new FileWriter(PROJECT_HOME + filename);

		// tokenizing each constraint found
		int i = 0;
		List<List<Token>> cTokenized = new ArrayList<List<Token>>();
		for (String string : constraints) {
			List<Token> result = Lexer.tokenize(string, Lexer.FileType.CLAFER);
			boolean ok = is2FeatureRealtionship(result);
			if (ok) {
				String s = "";
				for (Token token : result) {
					s += token.getValue() + " ";
				}
				writer.write(s + "\n");
				System.out.println(s);

				// makeFCLConstraint(result);
				i++;
			}
			cTokenized.add(result);
		}
		// doContraintsStats(cTokenized);
		System.out.println("\n:: " + i + " out of " + constraints.size()
				+ " constraints (" + (double) i / (double) constraints.size()
				+ ").");
		//
		// closing the writer
		writer.flush();
		writer.close();
	}

	/**
	 * @param cTokenized
	 */
	private static void doContraintsStats(List<List<Token>> cTokenized) {
		Integer numTags = 0;

		Map<Integer, List<List<Token>>> consts = new HashMap<Integer, List<List<Token>>>();

		List<List<Token>> l;
		for (List<Token> ctokens : cTokenized) {
			for (Token token : ctokens) {
				if (token.getLexeme() == TokenType.TAG) {
					numTags++;
				}
			}
			if (consts.containsKey(numTags)) {
				l = consts.get(numTags);
				l.add(ctokens);
				consts.put(numTags, l);
			} else {
				l = new ArrayList<>();
				l.add(ctokens);
				consts.put(numTags, l);
			}
			numTags = 0;
		}
//		XXX writeConstraintsFile(consts);  use only when needed
		for (Entry<Integer, List<List<Token>>> e : consts.entrySet()) {
			System.out.println(e.getKey() + ": " + e.getValue().size()
					+ " occurences.");
		}
	}

	private static void writeConstraintsFile(
			Map<Integer, List<List<Token>>> consts) {
		// Writes the content to the file
		String filename;

		for (Entry<Integer, List<List<Token>>> e : consts.entrySet()) {
			filename = "constraints" + e.getKey() + ".txt";
			try {
				writer = new FileWriter(PROJECT_HOME + filename);

				String s;
				for (List<Token> c : e.getValue()) {
					s = "";
					for (Token t : c) {
						s += t.getValue() + " ";
					}
					writer.write(s + "\n");
				}
				// closing the writer
				writer.flush();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
	}

	/**
	 * @param result
	 */
	private static void makeFCLConstraint(List<Token> result) {
		int pos = getBinOpPosition(result);
		FCLWrapper item = new FCLWrapper();
		item.setDependency(result);

		item.setConstraint(translateToFCLConstraint(result, pos));

//		System.out.println(item.getConstraint());
		database = new ArrayList<FCLWrapper>();
		database.add(item);
	}

	public static FCLConstraint translateToFCLConstraint(List<Token> tokens,
			int binOpPos) {
		FCLDependency dependency = null;
		String left = "";
		String right = "";

		switch (tokens.get(binOpPos).getValue()) {
		case "=>":
			left += getExpression(tokens, 0, binOpPos);//tokens.get(0).getValue() + tokens.get(1).getValue();
			
			if (tokens.get(binOpPos + 1).getValue().equals("!")) {
				right += getExpression(tokens, binOpPos+2, tokens.size());//tokens.get(binOpPos + 2).getValue();
				dependency = FCLDependency.EXCLUDES;
			} else {
				right += getExpression(tokens, binOpPos+1, tokens.size());//tokens.get(binOpPos + 1).getValue();
				dependency = FCLDependency.INCLUDES;
			}
			break;
		case "&&":
			if (tokens.get(0).getValue() == "!") {

				left += tokens.get(0).getValue() + tokens.get(1).getValue();

				if (tokens.get(binOpPos + 1).getValue() == "!") {
					right += tokens.get(binOpPos + 2).getValue();
					dependency = FCLDependency.EXCLUDES;
				} else {
					right += tokens.get(binOpPos + 1).getValue();
					dependency = FCLDependency.INCLUDES;
				}

			} else {
				left += tokens.get(0).getValue();

				if (tokens.get(binOpPos + 1).getValue() == "!") {
					right += tokens.get(binOpPos + 2).getValue();
					dependency = FCLDependency.EXCLUDES;
				} else {
					right += tokens.get(binOpPos + 1).getValue();
					dependency = FCLDependency.INCLUDES;
				}
			}
			break;
		case "||":
			if (tokens.get(binOpPos + 1).getValue() == "!") {
				if (tokens.get(0).getValue() == "!")
					dependency = FCLDependency.EXCLUDES;
				else
					return new FCLConstraint(tokens.get(binOpPos + 2)
							.getValue(), FCLDependency.INCLUDES, tokens.get(0)
							.getValue());
			} else if (tokens.get(binOpPos + 1).getValue() != "!") {
				if (tokens.get(0).getValue() == "!")
					dependency = FCLDependency.INCLUDES;
				else
					dependency = FCLDependency.MUTUALLY_EXCLUSIVE;
			}
			break;
		case "<=>":
			// TODO find the cases
			left += getExpression(tokens, 0, binOpPos);
			right += getExpression(tokens, binOpPos+1, tokens.size());
			dependency = FCLDependency.IFF;
			break;
		default:
			System.err.println("none of the options matched!");
		}

		return new FCLConstraint(left, dependency, right);
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
	/**
	 * @param result
	 * @return
	 */
	private static boolean is2FeatureRealtionship(List<Token> result) {
		int count = 0;
		for (Token token : result) {
			if (token.getLexeme() == TokenType.TAG)
				count++;
		}
		return (count == 2) ? true : false;
	}

	static class ConstraintFileReader {
		private static FileInputStream is;
		private static InputStreamReader isr;
		private static BufferedReader content;

		public static void load(String filepath) throws FileNotFoundException {
			// open input stream test.txt for reading purpose.
			is = new FileInputStream(filepath);

			// create new input stream reader
			isr = new InputStreamReader(is);

			// create new buffered reader
			content = new BufferedReader(isr);
		}

		public static List<String> getLines() throws IOException {

			List<String> lines = new ArrayList<String>();
			String buffer = "";
			int value = 0;
			// run until reach the end of the content file
			while ((value = content.read()) != -1) {
				// converts int to character
				char c = (char) value;

				if ((c == '\n' || c == '\r') && !buffer.isEmpty()) {
					lines.add(buffer);
					// System.out.println(buffer);
					buffer = "";
				} else {
					buffer += c;
				}
			} // end while

			return lines;
		}
	}
}
