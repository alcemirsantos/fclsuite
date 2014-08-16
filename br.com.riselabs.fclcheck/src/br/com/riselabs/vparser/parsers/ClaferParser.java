/**
 * 
 */
package br.com.riselabs.vparser.parsers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alcemir Santos
 * 
 */
public class ClaferParser {

	private static boolean hasConstraints;

	private static boolean allowedToParse = false;
	private static FileInputStream is;
	private static InputStreamReader isr;
	private static BufferedReader content;

	private static List<String> constraints;

	/**
	 * @param filepath
	 * @throws FileNotFoundException
	 */
	public static void load(String filepath) throws FileNotFoundException {
		// open input stream test.txt for reading purpose.
		is = new FileInputStream(filepath);

		// create new input stream reader
		isr = new InputStreamReader(is);

		// create new buffered reader
		content = new BufferedReader(isr);
		allowedToParse = true;
	}

	/**
	 * @return
	 */
	public static boolean canParse() {
		boolean contentIsReady = false;
		if (content != null) {
			try {
				contentIsReady = content.ready() ? true : false;
			} catch (IOException e) {
				contentIsReady = false;
				e.printStackTrace();
			}
		}

		return (allowedToParse && contentIsReady);
	}

	/**
	 * @return
	 */
	public static boolean hasConstraints() {
		return hasConstraints;
	}

	/**
	 * parse the file loaded previously.
	 * 
	 * @throws IOException
	 * 
	 */
	public static void parse() throws IOException {

		constraints = new ArrayList<String>();
		String buffer = "";
		int value = 0;
		// run until reach the end of the content file
		boolean isConstraintBlock = false;
		while ((value = content.read()) != -1) {
			// converts int to character
			char c = (char) value;

			if(c=='\n' ){
				if( isConstraintBlock ) {
					if(isConstraint(buffer))constraints.add(buffer);
					buffer = "";
					hasConstraints = true;
				}
				continue;
			}else if(c=='['){
				isConstraintBlock = true;
				buffer = String.valueOf(c);
			} else if (c==']') {
				buffer += String.valueOf(c);
//				constraints.add(buffer);
				buffer = "";
				isConstraintBlock = false;
				hasConstraints = true;
			}else {
				buffer += String.valueOf(c);
			}
				
			// prints character
//			System.out.print(c);

		}

	}
	
	public static boolean isConstraint(String string){
		boolean result = true;
		if (string.startsWith("[") || string.charAt(string.length() - 1) == ']') {
				result = false;
		}
		return result;
	}

	/**
	 * releases resources associated with the streams
	 */
	public static void release() throws IOException {
		allowedToParse = false;
		if (is != null) {
			is.close();
			is = null;
		}
		if (isr != null) {
			isr.close();
			isr = null;
		}
		if (content != null) {
			content.close();
			content = null;
		}
	}

	/**
	 * @return
	 */
	public static List<String> getConstraints() {
		return constraints;
	}
}
