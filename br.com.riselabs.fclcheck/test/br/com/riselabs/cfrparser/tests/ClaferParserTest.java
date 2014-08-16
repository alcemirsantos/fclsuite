/**
 * 
 */
package br.com.riselabs.cfrparser.tests;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import br.com.riselabs.vparser.lexer.Lexer;
import br.com.riselabs.vparser.lexer.beans.Token;
import br.com.riselabs.vparser.parsers.ClaferParser;

/**
 * @author Alcemir Santos
 * 
 */
public class ClaferParserTest {

	private final static String PROJECT_HOME = "/Users/alcemir/Documents/workspace/testeJ/";
	private static String cfrFile;

	@Before
	public void setup() {
		cfrFile = "linux.cfr";
		// cfrFile = "aircraft_fm.cfr";
		// cfrFile = "arcade_game_pl_fm.cfr";
	}

	/**
	 * Test method for
	 * 
	 * @throws FileNotFoundException
	 */
	@Test
	public void testLoadClaferFileOK() throws FileNotFoundException {
		ClaferParser.load(PROJECT_HOME + cfrFile);

		assertTrue("parser should have loaded the file content",
				ClaferParser.canParse());
	}

	/**
	 * Test method for
	 * 
	 * @throws FileNotFoundException
	 */
	@Test(expected = FileNotFoundException.class)
	public void testLoadClaferFileFail() throws FileNotFoundException {
		ClaferParser.load(PROJECT_HOME + "inexistent.cfr");

		assertFalse("parser should have loaded the file content",
				ClaferParser.canParse());
	}

	@Test
	public void testReleaseResourcesOK() throws IOException {
		ClaferParser.load(PROJECT_HOME + cfrFile);
		ClaferParser.release();
		assertFalse("the parser should not allow parse.",
				ClaferParser.canParse());
	}

	@Test
	public void testParserFindConstraintsOK() throws IOException {
		ClaferParser.load(PROJECT_HOME + cfrFile);
		ClaferParser.parse();
		boolean result = ClaferParser.hasConstraints();
		assertTrue("in fact, the model has constraints.", result);
	}

	public static void main(String[] args) throws IOException {
		ClaferParser.load(PROJECT_HOME + "linux.cfr");
		ClaferParser.parse();
		List<String> constraints = ClaferParser.getConstraints();

		// creates a FileWriter Object
		FileWriter writer = new FileWriter(PROJECT_HOME + "constraints.txt");
		// Writes the content to the file
		System.out.println("\n::>> Starting Constraints (" + constraints.size()
				+ " found) <<::");
		int count = 0;
		for (String string : constraints) {
			List<Token> result = Lexer.tokenize(string, Lexer.FileType.CLAFER);
			String st = " [";
			for (Token t : result) {
				st += t.getValue() + ",";
			}
			st += "]";
			writer.write(string + st + "\n");
			count++;
		}
		System.out.println("::>> Ending Constraints (" + count + "/"
				+ constraints.size() + " found) <<::");
		writer.flush();
		writer.close();
	}
}
