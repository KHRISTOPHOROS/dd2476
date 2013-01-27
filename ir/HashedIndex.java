/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012
 */  


package ir;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.ArrayList;

/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
	//
	//  YOUR CODE HERE
	//
        if(index.get(token) == null){
            index.put(token,new PostingsList(new PostingsEntry(docID,offset)));
        }
        else{
            (index.get(token)).add(new PostingsEntry(docID,offset));
        }
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
	// 
	//  REPLACE THE STATEMENT BELOW WITH YOUR CODE
	//
        return index.get(token);
    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType ) {
	// 
	//  REPLACE THE STATEMENT BELOW WITH YOUR CODE
	//
        int nrOfTerms = query.terms.size();
        ArrayList<PostingsList> postLists = new ArrayList<PostingsList>();

        //GET PostingsLists for each term
        for(int i=0;i<nrOfTerms;i++){
            String tempToken = query.terms.get(i);
            if(index.get(tempToken) != null){
                postLists.add(index.get(tempToken));
            }
        }

        if(queryType == Index.PHRASE_QUERY){                       //PHRASE QUERY
            if(nrOfTerms == 1){
                String token1 = query.terms.get(0);
                PostingsList hitList = index.get(token1);
                return hitList;
            }

            return intersectPhrase(intersect(postLists));
        }
        else if(queryType == Index.INTERSECTION_QUERY){                 //INTERSECTION QUERY
            if(nrOfTerms == 1){
                String token1 = query.terms.get(0);
                PostingsList hitList = index.get(token1);
                return hitList;
            }

            return intersect(postLists).get(0);
        }

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
            System.out.println("STARTING LOOP");
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

    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
