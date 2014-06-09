package br.com.riselabs.vparser.samples;

import java.io.*;
import java.util.*;

public class ParseE {

    static StringTokenizer st;
    static String curr;

    /** read the next token into curr */
    static void next() {
	try {
	    curr=st.nextToken().intern();
	    // use of intern() allows us to check equality with ==.
	} catch( NoSuchElementException e) {
	    curr=null;
	}
    }

    static void error(String msg) {
	System.err.println(msg);
	System.exit(-1);
    }

    static int parseE() {
	// E -> T E1
	int x=parseT();
	return parseE1(x);
    }

    static int parseE1(int x) {
	// E1 -> T E1 | epsilon
	if (curr=="+") {
	    next();
	    int y = parseT();
	    return parseE1(x+y);
	} else if(curr==")" || curr=="$" ) {
	    return x;
	} else {
	    error("Unexpected :"+curr);
	    return x; // to make compiler happy
	}
    }

    static int parseT() {
	// T -> F T1
	int x=parseF();
	return parseT1(x);
    }

    static int parseT1(int x) {
	// T1 -> * F T1 | epsilon
	if (curr=="*") {
	    next();
	    int y=parseF();
	    return parseT1(x*y);
	} else if(curr=="+" || curr==")" || curr=="$") {
	    return x;
	} else {
	    error("Unexpected :"+curr);
	    return x; // to make compiler happy
	}
    }
    
    static int parseF() {
	// F -> ( E ) | a
	if( curr=="(") {
	    next();
	    int x=parseE();
	    if(curr==")") {
		next();
		return x;
	    } else {
		error (") expected.");
		return -1; // to make compiler happy
	    }
	} else try {
	    int x=Integer.valueOf(curr).intValue();
	    next();
	    return x;
	} catch(NumberFormatException e) {
	    error("Number expected.");
	    return -1; // to make compiler happy
	}
    }

    public static void main(String args []) throws IOException {
	BufferedReader in = new BufferedReader(new InputStreamReader (System.in));
	
//	String line=in.readLine();
	String line="2 + ( 4 * 6 )";
	st = new StringTokenizer(line+" $");
	next();
	int x=parseE();
	if(curr=="$") {
	    System.out.println("OK "+x);
	} else {
	    error("End expected");
	} 
    }
}