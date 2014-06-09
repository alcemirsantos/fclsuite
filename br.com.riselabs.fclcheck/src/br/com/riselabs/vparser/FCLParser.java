package br.com.riselabs.vparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import br.com.riselabs.fclcheck.core.DependencyConstraint;
import br.com.riselabs.fclcheck.enums.Constraint;
import br.com.riselabs.vparser.exception.ParseException;

/**
 * 
 * @author Alcemir Santos
 * 
 */
public class FCLParser {

	private static final boolean DEBUG = false;
	final static Charset ENCODING = StandardCharsets.UTF_8;

	/**
	 * This method is responsible to parse the FCL specifications file and to
	 * return a list of modules
	 * 
	 * @param in
	 *            InputStream of the DCL specifications file
	 * @throws IOException
	 *             Error on read the file
	 * @return Map moduleName -> moduleDescription
	 */
	public static final Collection<DependencyConstraint> parseGroupsConstraints(
			final InputStream in) throws IOException {
		final Map<String, String[]> featureGroups = new HashMap<String, String[]>();

		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(in));

		final String fSeparator = ",";
		String[] split;

		while (lnr.ready()) {
			String line = lnr.readLine().trim();
			if (line.startsWith("%")) { // comments
				continue;
			} else if (line == null || line.trim().equals("")) { // empty lines
				continue;
			} else if (line.startsWith("alternative")
					|| line.startsWith("at-most-one")) {
				split = line.substring(11).replace(":", "").split(fSeparator);
				for (int i = 0; i < split.length; i++) {
					split[i] = split[i].trim();
				}
				featureGroups.put("alternative_"+lnr.getLineNumber(), split);
			} else if (line.startsWith("or") || line.startsWith("at-least-one")) {
				split = line.substring(line.startsWith("or") ? 2 : 12).split(
						fSeparator);
				for (int i = 0; i < split.length; i++) {
					split[i] = split[i].trim();
				}
				featureGroups.put("or", split);
			}
		}

		return getDependencyConstraints(featureGroups);
	}

	/**
	 * @param featureGroups
	 * @return
	 */
	private static Collection<DependencyConstraint> getDependencyConstraints(
			Map<String, String[]> featureGroups) {
		List<DependencyConstraint> dc = new LinkedList<DependencyConstraint>();

		for (Entry<String, String[]> entry : featureGroups.entrySet()) {
			if (entry.getKey().startsWith("alternative_")) {
				dc.addAll(makeCombinations(entry.getValue(),
						Constraint.CANNOT_COEXIST));
			}
		}

		return dc;
	}

	/**
	 * Return a collection of <code>{@link DependencyConstraint}</code> of the
	 * possible combinations of features provided by the options arrays.
	 * 
	 * @param options
	 * @param constraint
	 * @return
	 */
	private static Collection<DependencyConstraint> makeCombinations(
			String[] options, Constraint constraint) {
		int len = options.length;
		List<DependencyConstraint> dc = new LinkedList<DependencyConstraint>();

		for (int j = 0; j < len - 1; j++) {
			for (int k = j + 1; k < len; k++){
				DependencyConstraint d = new DependencyConstraint(options[j], options[k], constraint);
				System.out.println(d);
				dc.add(d);
			}
		}
		return dc;
	}

	/**
	 * DCL2 Method responsible to parse the DCL specifications file and to
	 * return a list of dependency constraints
	 * 
	 * @param in
	 *            InputStream of the DCL specifications file
	 * @throws IOException
	 *             Error on read the file
	 */
	public static final Collection<DependencyConstraint> parseCrossTreeConstraints(
			final InputStream in) throws IOException, ParseException {
		final List<DependencyConstraint> dependencyConstraints = new LinkedList<DependencyConstraint>();

		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(in));

		while (lnr.ready()) {
			String line = lnr.readLine().trim();
			try {
				if (line.startsWith("%")) {
					continue;
				} else if (line == null || line.trim().equals("")) {
					continue;
				} else if (line.startsWith("alternative")
						|| line.startsWith("at-most-one")
						|| line.startsWith("or")
						|| line.startsWith("at-least-one")) {
					continue;
				} else {
					if (line.startsWith("cross-tree")) {
						line = line.substring(line.indexOf(":") + 1);
					}


					String buffer="";
					List<String> list=new ArrayList<String>();
					for(int i=0;i<line.length();i++){
						char nextChar = line.charAt(i);
						if(line.charAt(i) != ' ') {
							buffer += nextChar;
							if(i==line.length()-1) list.add(buffer);
							continue;
						}else if(!buffer.isEmpty()){
							list.add(buffer);
							buffer = "";
						}else{
							buffer = "";
							continue;
						}
					}
					if(list.size()!=3) {
						throw new Exception("Constraint line ill-formed.");
					}
					
					DependencyConstraint d = new DependencyConstraint(
							list.get(0), list.get(2), Constraint
							.getConstraint(list.get(1)));
					System.out.println(d);
					dependencyConstraints.add(d);
				}
			} catch (Exception e) {
				throw new ParseException(e, line, lnr.getLineNumber());
			}
		}

		if (DEBUG) {
			for (DependencyConstraint dc : dependencyConstraints) {
				System.out.println(dc);
				System.out.println("============================");
			}
		}
		return dependencyConstraints;
	}

	public static void main(String[] args) {
		// try {
		final String filePath = System.getProperty("user.home")
				+ "/Documents/workspace/fclsuite-aux/constraints-sample.fcl";

		File f = new File(filePath);
		System.out.println(filePath + "\n" + f.exists());
		try {
			InputStream is = new FileInputStream(f);
			parseCrossTreeConstraints(is);
			parseGroupsConstraints(is);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	// For larger files

	void readLargerTextFile(String aFileName) throws IOException {
		Path path = Paths.get(aFileName);
		try (Scanner scanner = new Scanner(path, ENCODING.name())) {
			while (scanner.hasNextLine()) {
				// process each line in some way
				log(scanner.nextLine());
			}
		}
	}

	void readLargerTextFileAlternate(String aFileName) throws IOException {
		Path path = Paths.get(aFileName);
		try (BufferedReader reader = Files.newBufferedReader(path, ENCODING)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				// process each line in some way
				log(line);
			}
		}
	}

	private static void log(Object aMsg) {
		System.out.println(String.valueOf(aMsg));
	}

}
