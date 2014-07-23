/**
 * 
 */
package br.com.riselabs.vparser.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

/**
 * @author Alcemir Santos
 * 
 */
public class CSVUtil {
	private static String filename = "macros.csv";
	private static String workspacePath;
	private static String filepath;
	private static String[] csvHeaders = { "id", "macroExpression", "file" };
	private static List<String[]> records;

	public static void readCSV() {
		try {

			CsvReader macros = new CsvReader(filepath);

			macros.readHeaders();

			while (macros.readRecord()) {
				String macroID = macros.get(csvHeaders[0]);
				String macroExpression = macros.get(csvHeaders[1]);
				String containingFile = macros.get(csvHeaders[2]);

				// perform program logic here
				System.out.println(macroID + ":" + macroExpression + "@"
						+ containingFile);
			}

			macros.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void addCSVRecord(String[] entry){
		if(records == null) records = new ArrayList<String[]>();
		records.add(entry);
	}
	
	public static void writeCSV() {

		try {
			CsvWriter csvOutput = new CsvWriter(new FileWriter(filepath, true), ',');
			csvOutput.write(csvHeaders[0]);
			csvOutput.write(csvHeaders[1]);
			csvOutput.write(csvHeaders[2]);
			csvOutput.endRecord();
			// write out a few records
			for (String[] entry : records) {
				csvOutput.writeRecord(entry);				
			}

			csvOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("\n::->CSV file sucessfully written!");;
	}

	public static void deleteCSV() {
		// before we open the file check to see if it already exists
		boolean alreadyExists = new File(filepath).exists();

		if (alreadyExists) {
			new File(filepath).delete();
		}
	}

	public static int csvRecordCount() {
		if (filepath==null) {
			init();
			return 0;
		}else{
			return records.size();
		}
		
	}

	/**
	 * 
	 */
	public static void init() {
		workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		filepath = workspacePath+File.separator+filename;
		if (records!=null) {
			records.clear();
		}else{
			records = new ArrayList<String[]>();
		}
	}

	public static void main(String[] args) {

		System.out.println("writing beginning...");
		addCSVRecord(new String[] { "a", "v", "f" });
		writeCSV();
		System.out.println("reading beginning...");
		readCSV();
		System.out.println("cleaning beginning...");
		deleteCSV();
	}
}
