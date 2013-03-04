/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

import java.util.*;
import java.io.*;

public class PageRank{

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;
    public int nrOfDocs = 0;
    public double c = 0.85;
    public double J = 0;

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a Hashtable, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a Hashtable whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    
    /* --------------------------------------------- */


    public PageRank( String filename ) {
        int noOfDocs = readDocs( filename );
        computePagerank( noOfDocs );
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and creates the docs table. When this method 
     *   finishes executing then the @code{out} vector of outlinks is 
     *   initialised for each doc, and the @code{p} matrix is filled with
     *   zeroes (that indicate direct links) and NO_LINK (if there is no
     *   direct link. <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
            int index = line.indexOf( ";" );
            String title = line.substring( 0, index );
            Integer fromdoc = docNumber.get( title );
            //  Have we seen this document before?
            if ( fromdoc == null ) {	
                // This is a previously unseen doc, so add it to the table.
                fromdoc = fileIndex++;
                docNumber.put( title, fromdoc );
                docName[fromdoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
                // This is a previousy unseen doc, so add it to the table.
                otherDoc = fileIndex++;
                docNumber.put( otherTitle, otherDoc );
                docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
		    if ( link.get(fromdoc) == null ) {
                link.put(fromdoc, new Hashtable<Integer,Boolean>());
		    }
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
                link.get(fromdoc).put( otherDoc, true );
                out[fromdoc]++;
		    }
		}
	    }
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
            System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
            System.err.print( "done. " );
	    }
	    // Compute the number of sinks.
	    for ( int i=0; i<fileIndex; i++ ) {
            if ( out[i] == 0 )
                numberOfSinks++;
            }
    }
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex;
    }


    /* --------------------------------------------- */


    /*
     *   Computes the pagerank of each document.
     */
    void computePagerank( int numberOfDocs ) {
        computePagerank1( numberOfDocs );
    }

    void computePagerank0( int numberOfDocs ) {
        nrOfDocs = numberOfDocs;                                                //GLOBAL VARIABLES; MY UGLY SOLUTION TO AVOID SENDING numberOfDocs to P()
        c = 0.85;
        J = ((double)1/(double)numberOfDocs);

	    double[] x0= new double[numberOfDocs];                                  //INITIALIZE x0
        double[] x1 = new double[numberOfDocs];
        Arrays.fill(x1,(1/(double)numberOfDocs));                               //INITIALIZE x1

        while(diff(x0,x1) > EPSILON){
            x0 = x1;
            x1 = xG(x0);
            System.out.println(diff(x0,x1));
        }

    //    for(int B=0;B<numberOfDocs;B++){
    //        x1[B] = -1*x1[B];
    //    }

//////////////////////////////////////
        Doc_n_Rank[] output = new Doc_n_Rank[numberOfDocs];
        for(int i=0;i<numberOfDocs;i++){
            output[i] = new Doc_n_Rank(i,x1[i]);
        }

        Arrays.sort(output);

        for(int B=0;B<50;B++){ System.out.println(docName[output[B].docID]+": "+output[B].rank); }
/////////////////////////////////////
        //HashMap<Double,Integer> rank2Doc = new HashMap<Double,Integer>();       //HASHMAP FOR GETTING DocIDs AFTER THE ARRAY OF RANKS IS SORTED
        //int DOPPELGANGERS = 0;
        //for(int k=0;k<numberOfDocs;k++){
        //    if(!rank2Doc.containsKey(x1[k])){
        //        rank2Doc.put(x1[k],k);                                            //+1 OR NOT?????? BATMAN BATMAN
        //    }
        //    else{                                                                 //
        //        DOPPELGANGERS++;                                                  //
        //        x1[k] += DOPPELGANGERS*0.00000001;                                   //    I HAVE NO IDEA WHAT IM DOING
        //        rank2Doc.put(x1[k],k);                                            //
        //    }                                                                     //

        //}

        //Arrays.sort(x1);

        //int[] output = new int[numberOfDocs];                                   //VECTOR FOR PLACING DocIDs IN ORDER ACCORDING TO SORTED RANKS
        //for(int k=0;k<numberOfDocs;k++){
        //    output[k] = rank2Doc.get(x1[k]);    //+1 ??
        //}
        
        //PRINTING RESULTS
        //for(int Q=0;Q<numberOfDocs;Q++){
        //    System.out.println(docName[output[Q]]);
        //    if(Q==50){break;}
        //}
    }

    void computePagerank1( int numberOfDocs ){              //IF THERE ARE NO OUTGOING LINKS MAKE A RANDOM JUMP
        double[] visits = new double[numberOfDocs];
        long D1 = Math.round(Math.random()*numberOfDocs);                       //DocID FOR THE FATHER DOCUMENT
        int N = numberOfDocs*numberOfDocs;                                      //NUMBER OF TIMES TO RUN THE ALGORITHM

        for(int iteration=0;iteration<N;iteration++){                           //FOR N ITERATIONS:
            if(link.get((int)D1) == null){                                      //IF THERE ARE NO OUTGOING LINKS; RANDOM JUMP
                long D2 = Math.round(Math.random()*(numberOfDocs-1));
                visits[(int)D2]++;
                D1 = D2;
            }
            else if(Math.random() < c){                                         //WITH PROBABILITY c; WALK ALONG ONE OF D1s LINKS
                Set<Integer> outDocsSet = link.get((int)D1).keySet();
                Integer[] outDocsArray = outDocsSet.toArray(new Integer[outDocsSet.size()]);
                int index = (int)Math.round(Math.random()*(outDocsArray.length-1));
                long D2 = outDocsArray[index];
                visits[(int)D2]++;                                                   //D2 IS NOW VISITED
                D1 = D2;
            }
            else{                                                               //WITH PROBABILITY (c-1); MAKE RANDOM JUMP
                long D2 = Math.round(Math.random()*(numberOfDocs-1));
                visits[(int)D2]++;
                D1 = D2;
            }
        }

        Doc_n_Rank[] output = new Doc_n_Rank[numberOfDocs];
        for(int i=0;i<numberOfDocs;i++){
            output[i] = new Doc_n_Rank(i,visits[i]);
        }

        Arrays.sort(output);

        for(int B=0;B<50;B++){ System.out.println(docName[output[B].docID]+": "+output[B].rank); }
    }

    double P(int row, int col){
        if(link.get(row) != null){                                              //IF THERE ARE OUTGOING LINKS FROM DOCUMENT "row"
            int nrOfLinks = link.get(row).size();
            if(link.get(row).containsKey(col)){                                     //IF THERE IS AN OUTGOING LINK TO DOCUMENT "col"
                return (1/(double)nrOfLinks);                                           //RETURN THE PROBABILITY
            }
            return 0;                                                               //NO OUTGOING LINK TO DOCUMENT "col": PROBABILITY 0
        }
        if(row != col){
            return (1/((double)nrOfDocs-1));                                            //IF THERE ARE NO OUTGOING LINKS FROM DOCUMENT "row": PROB IS EQUAL TO ALL DOCS
        }
        return 0;                                                              //RETURN 0 IF ELEMENT IS ALONG THE DIAGONAL
    }

    double G(int row, int col){
        return c*P(row,col) + (1-c)*J;
    }

    double[] xG(double[] x){
        double sum = 0;
        double[] xOut = new double[nrOfDocs];
        for(int col=0;col<nrOfDocs;col++){
            sum = 0;
            for(int row=0;row<nrOfDocs;row++){
                sum += x[row] * G(row,col);
            }
            xOut[col] = sum;
        }
        return xOut;
    }

    double diff(double[] v1, double[] v2){
        int length = v1.length;
        double sum = 0;

        for(int i=0;i<length;i++){
            sum += Math.pow(v1[i]-v2[i],2);
        }
        sum = Math.sqrt(sum); 
        
        return sum;
    }

    /* --------------------------------------------- */

    class Doc_n_Rank implements Comparable<Doc_n_Rank>{                                 //CLASS FOR KEEPING COCUMENT IDs AND CORRESPONDING RANKS
        public int docID;
        public double rank;

        public Doc_n_Rank(int docIDIn, double rankIn){
            docID = docIDIn;
            rank = rankIn;
        }

        public int compareTo(Doc_n_Rank other){
            if(this.rank > other.rank){return -1;}
            if(this.rank < other.rank){return 1;}
            return 0;
        }
    }

    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
	    new PageRank( args[0] );
	}
    }
}
