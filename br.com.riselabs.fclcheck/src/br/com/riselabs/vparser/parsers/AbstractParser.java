package br.com.riselabs.vparser.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractParser {

	
	/**
	 * Reads the <code>InputSteam</code> provided and returns a map between line
	 * number and its content.
	 * 
	 * @param input
	 *            - the <code>InputStream</code> to be read.
	 * @return the map between line and its content.
	 */
	protected Map<Integer, String> getLinesMappingFrom(InputStream input) {
		Map<Integer, String> map = new HashMap<>();
		int i, lineCounter = 0;
		char c;
		String buffer = "";
		try {
			// reads till the end of the stream
			while ((i = input.read()) != -1) {
				// converts integer to character
				c = (char) i;

				if (c == '\n') {
					lineCounter++;
					buffer += c;
					// maps each line
					map.put(lineCounter, buffer);
					buffer = "";
				} else {
					buffer += c;
				}
			}
		} catch (Exception e) {
			// if any I/O error occurs
			e.printStackTrace();
		} finally {

			// releases system resources associated with this stream
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return map;
	}
}
