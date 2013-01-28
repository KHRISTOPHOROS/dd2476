/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012
 */  

package ir;

import com.larvalabs.megamap.MegaMapManager;
import com.larvalabs.megamap.MegaMap;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;

public class MegaIndex implements Index {

    /** 
     *  The index as a hash map that can also extend to secondary 
     *	memory if necessary. 
     */
    private MegaMap index;


    /** 
     *  The MegaMapManager is the user's entry point for creating and
     *  saving MegaMaps on disk.
     */
    private MegaMapManager manager;


    /** The directory where to place index files on disk. */
    private static final String path = "./index";


    /**
     *  Create a new index and invent a name for it.
     */
    public MegaIndex() {
	try {
	    manager = MegaMapManager.getMegaMapManager();
	    index = manager.createMegaMap( generateFilename(), path, true, false );
	}
	catch ( Exception e ) {
	    e.printStackTrace();
	}
    }


    /**
     *  Create a MegaIndex, possibly from a list of smaller
     *  indexes.
     */
    public MegaIndex( LinkedList<String> indexfiles ) {
	try {
        manager = MegaMapManager.getMegaMapManager();
        if ( indexfiles.size() == 0 ) {
            // No index file names specified. Construct a new index and
            // invent a name for it.
            index = manager.createMegaMap( generateFilename(), path, true, false );
    
        }
        else if ( indexfiles.size() == 1 ) {
            // Read the specified index from file
            index = manager.createMegaMap( indexfiles.get(0), path, true, false );
            HashMap<String,String> m = (HashMap<String,String>)index.get( "..docIDs" );
            if ( m == null ) {
                System.err.println( "Couldn't retrieve the associations between docIDs and document names" );
            }
            else {
                docIDs.putAll( m );
            }
        }
        else {
            // Merge the specified index files into a large index.
            MegaMap[] indexesToBeMerged = new MegaMap[indexfiles.size()];
            for ( int k=0; k<indexfiles.size(); k++ ) {
                System.err.println( indexfiles.get(k) );
                indexesToBeMerged[k] = manager.createMegaMap( indexfiles.get(k), path, true, false );
            }
            index = merge( indexesToBeMerged );
            for ( int k=0; k<indexfiles.size(); k++ ) {
                manager.removeMegaMap( indexfiles.get(k) );
            }
        }
	}
	catch ( Exception e ) {
	    e.printStackTrace();
	}
    }


    /**
     *  Generates unique names for index files
     */
    String generateFilename() {
	String s = "index_" + Math.abs((new java.util.Date()).hashCode());
	System.err.println( s );
	return s;
    }


    /**
     *   It is ABSOLUTELY ESSENTIAL to run this method before terminating 
     *   the JVM, otherwise the index files might become corrupted.
     */
    public void cleanup() {
	// Save the docID-filename association list in the MegaMap as well
	index.put( "..docIDs", docIDs );
	// Shutdown the MegaMap thread gracefully
	manager.shutdown();
    }



    /**
     *  Returns the dictionary (the set of terms in the index)
     *  as a HashSet.
     */
    public Set getDictionary() {
	return index.getKeys();
    }


    /**
     *  Merges several indexes into one.
     */
    MegaMap merge( MegaMap[] indexes ) {                                                                                    ///////////////HNJYA/////////////////
	try {
	    MegaMap output = manager.createMegaMap( generateFilename(), path, true, false );
	    //
	    //  YOUR CODE HERE
	    //
        for(int i=0;i<indexes.length;i++){                                  //FOR ALL MegaMaps
            Set<String> tokens = indexes[i].getKeys();                      //GET CURRENT MegaMaps KEYS
            
            int offset = -1;
            for(Iterator<String> it = tokens.iterator(); it.hasNext(); ){   //FOR ALL KEYS
                offset++;
                String token = it.next();                                   //PARTICULAR KEY
                //output.put(token,indexes[i].get(token));    //GET VALUE FROM MegaMap AND PUT IN OUTPUT
                //PostingsList stealFrom = (PostingsList)(MegaMap)(indexes[i].get(token));
                //System.out.println("BASJKASTERELOF "+indexes[i].getClass());
                //System.out.println("BAJSKASTARELOF2 "+indexes[i].get(token).getClass());
                if(indexes[i].get(token) instanceof PostingsList){

                    PostingsList stealFrom = (PostingsList)(indexes[i].get(token));
                    if(output.hasKey(token)){
                        PostingsList giveTo = (PostingsList)(output.get(token));
                        
                        for(int j=0;j<stealFrom.size();j++){
                            giveTo.add(stealFrom.get(j));
                        }
                    }
                    else{
                        output.put(token,(indexes[i].get(token)));
                    }
                }
            }
        }

	    return output;

	}catch ( Exception e ) { e.printStackTrace(); return null; }
    }

    /**
     *  Inserts this token in the hashtable.
     */
    public void insert( String token, int docID, int offset ) {
	//
	//  COPY THE CODE FROM YOUR HashedIndex CLASS HERE
	//
        try{

            if(index.hasKey(token)){
                PostingsList tempList = (PostingsList)(index.get(token));
                ((PostingsList)(index.get(token))).add(new PostingsEntry(docID,offset));
            }
            else{
                index.put(token,new PostingsList(new PostingsEntry(docID,offset)));
            }

        }catch(Exception e){ System.out.println("MEGAMAP EXCEPTION1"); }
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
	try{
	    return (PostingsList)index.get( token );
	}
	catch( Exception e ) {
	    return new PostingsList();
	}
    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType ){
	//
	//  REPLACE THE STATEMENT BELOW WITH THE CODE FROM
	//  YOUR HashedIndex CLASS
	//
        int nrOfTerms = query.terms.size();
        ArrayList<PostingsList> postLists = new ArrayList<PostingsList>();
        try{

        //GET PostingsLists for each term
        for(int i=0;i<nrOfTerms;i++){
            String tempToken = query.terms.get(i);
            if(index.get(tempToken) != null){
                postLists.add((PostingsList)index.get(tempToken));
            }
        }

        if(queryType == Index.PHRASE_QUERY){                       //PHRASE QUERY
            if(nrOfTerms == 1){
                String token1 = query.terms.get(0);
                PostingsList hitList = (PostingsList)index.get(token1);                         //BATMAN
                return hitList;
            }

            return intersectPhrase(intersect(postLists));
        }
        else if(queryType == Index.INTERSECTION_QUERY){                 //INTERSECTION QUERY
            if(nrOfTerms == 1){
                String token1 = query.terms.get(0);
                PostingsList hitList = (PostingsList)(index.get(token1));                       //BATMAN
                return hitList;
            }

            return intersect(postLists).get(0);
        }

        }catch(Exception e){ System.out.println("MEGAMAP EXCEPTION2"); }

        System.out.println("RETURN OF NULL WAS REACHED!");
        return null;
    }

    public ArrayList<PostingsList> intersect(ArrayList<PostingsList> postListsIn){
        //PostingsList postingsOut = new PostingsList();                  //THE POSTINGS TO RETURN
        int nrOfPointers = postListsIn.size();

        ArrayList<PostingsList> output = new ArrayList<PostingsList>(); //NEW
        for(int i=0;i<nrOfPointers;i++){                                //NEW
            output.add(new PostingsList());                             //NEW
        }

        ArrayList<Integer> pointers = new ArrayList<Integer>();         //LIST FOR TRACKING POINTERS
        for(int i=0;i<nrOfPointers;i++){                                //ALL OF THEM 0 INITIALLY
            pointers.add(0);
        }

        //ALGORITHM FROM BOOK
        while(true){
            int correctDocID = ((postListsIn.get(0)).get(pointers.get(0))).docID;
            int nrOfHits = 0;
            for(int i=0;i<pointers.size();i++){     //FOR ALL POSTLISTS AND THEIR POINTER, GET DocID
                if(((postListsIn.get(i)).get(pointers.get(i))).docID == correctDocID){  //IF CORRECT
                    nrOfHits++;                                         //INCREASE NR OF HITS
                }
            }
            if(nrOfHits == nrOfPointers){                               //IF DOCUMENT HAS ALL WORDS
                nrOfHits = 0;           // NO NEED
                //postingsOut.add((postListsIn.get(0)).get(pointers.get(0)));

                for(int k=0;k<nrOfPointers;k++){                                        //NEW
                    (output.get(k)).add((postListsIn.get(k)).get(pointers.get(k)));     //NEW
                }                                                                       //NEW

                if(pointersAtEnd(pointers,postListsIn,nrOfPointers)){
                    break;
                }

                for(int i=0;i<nrOfPointers;i++){                        //FOR ALL POINTERS
                    if(pointers.get(i) < (postListsIn.get(i)).size()-1){//IF NOT AT LAST POSITION
                        pointers.set(i,pointers.get(i)+1);              //ADVANCE POINTER
                    }
                }
            }
            else{
                int minPointer = 0;
                int minValue = ((postListsIn.get(0)).get(pointers.get(0))).docID;
                boolean lowFound = false;

                for(int i=0;i<nrOfPointers;i++){
                    if(pointers.get(i) < (postListsIn.get(i)).size()-1) { //NOT LAST
                        if(lowFound==false){                            //NO POINTER IS CHOSEN
                            lowFound=true;                              //PICK ONE
                            minPointer = i;
                            minValue = ((postListsIn.get(i)).get(pointers.get(i))).docID;
                        }
                        else{                                           //IF ANOTHER ALREADY CHOSEN
                            if(((postListsIn.get(i)).get(pointers.get(i))).docID < minValue){//LOWER
                                minPointer = i;                         //PICK THIS ONE
                                minValue = ((postListsIn.get(i)).get(pointers.get(i))).docID;
                            }
                        }
                    }
                }                  

                if(lowFound){           //NOW THE LOWEST POINTER THAT CAN BE INCREMENTED HAS BEEN FOUND
                    pointers.set(minPointer, pointers.get(minPointer)+1);//INCREASE LOWEST POINTER
                }
                else{                   //NO INCREMENTABLE POINTER WAS FOUND
                    break;              //NOTHING MORE TO DO HERE - BREAK
                }
            }
        }

        return output;
    }

    public PostingsList intersectPhrase(ArrayList<PostingsList> termsIn){
        PostingsList output = new PostingsList();
        int nrOfTerms = termsIn.size();
        int nrOfDocs = (termsIn.get(0)).size();
        PostingsList term0 = termsIn.get(0);                        //FOR THE FIRST TERM

        for(int i=0;i<nrOfDocs;i++){                                //FOR ALL DOCUMENTS
            PostingsEntry tempDoc0 = term0.get(i);                  //THE DOCUMENT TO ANALYZE
            boolean foundPhrase = false;

            for(int j=0;j<tempDoc0.positions.size();j++){           //FOR ALL POSITIONS FOR FIRST TERM
                int tempPos0 = (tempDoc0.positions).get(j);         //THE POSITION TO ANALYZE
                if(foundPhrase){ break; }                           //IF PHRASE IS FOUND, NEXT DOC

                for(int k=1;k<nrOfTerms;k++){                       //FOR ALL OTHER TERMS
                    PostingsList termk = termsIn.get(k);            //ANOTHER TERM TO ANALYZE

                    PostingsEntry tempDoc = termk.get(i);           //SAME DOCUMENT TO ANALYZE

                    if(!hasInteger(tempPos0+k,tempDoc.positions)){  //DOCUMENT DOES NOT HAVE PHRASE
                        break;
                    }
                    if(k==nrOfTerms-1){                             //ALL TERMS WERE CORRECTLY PLACED
                        foundPhrase = true;                         //PHRASE IS FOUND
                        output.add(tempDoc);                        //ADD DOCUMENT TO ANSWER
                    }
                }
            }
        }
    return output;
    }

    public boolean hasInteger(int targetIn, ArrayList<Integer> listIn){
        int size = listIn.size();
        for(int i=0;i<size;i++){
            if(listIn.get(i) == targetIn){
                return true;
            }
        }
        return false;
    }

    public boolean pointersAtEnd(ArrayList<Integer> pointersIn, ArrayList<PostingsList> postListsIn, int nrOfPointersIn){
        int nrOfPointersEqualsBreak = 0;
        for(int i=0;i<nrOfPointersIn;i++){                            //FOR ALL POINTERS
            if(pointersIn.get(i) == (postListsIn.get(i)).size()-1){   //IF POINTER AT LAST POSITION
                nrOfPointersEqualsBreak++;                            //INCREASE THE BREAKVALUE
            }
        }
        if(nrOfPointersEqualsBreak == pointersIn.size()){             //IF BREAKVALUE HIGH - BREAK
            return true;
        }
        else{
            return false;
        }
    }
}


