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
import java.util.Arrays;
import java.util.Collections;

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

        if(queryType == Index.PHRASE_QUERY){                            //PHRASE QUERY
            if(nrOfTerms == 1){                                         //IF ONE TERM
                String token1 = query.terms.get(0);
                PostingsList hitList = index.get(token1);               //GET FROM HASHTABLE
                return hitList;
            }

            return intersectPhrase(intersect(postLists));
        }
        else if(queryType == Index.INTERSECTION_QUERY){                 //INTERSECTION QUERY
            if(nrOfTerms == 1){                                         //IF ONE TERM
                String token1 = query.terms.get(0);
                PostingsList hitList = index.get(token1);               //GET FROM HASHTABLE
                return hitList;
            }

            return intersect(postLists).get(0);
        }
        else if(queryType == Index.RANKED_QUERY){
            if(nrOfTerms == 1){
                String token1 = query.terms.get(0);
                PostingsList hitList = index.get(token1);
                return hitList;
            }
            return rankedRetrieval(postLists);
        }

        System.out.println("RETURN OF NULL WAS REACHED!");
        return null;
    }

    //RETURNS ONE PostingsList FOR EACH WORD, CONTAINING ALL PostingsEntrys (DocIDs) THAT THAT WORD (AND THE OTHER WANTED WORDS) OCCURED IN
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
        int[] pointers = new int[nrOfTerms];
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

                    if(!hasInteger(tempPos0+k,tempDoc.positions,pointers,k)){  //DOCUMENT DOES NOT HAVE PHRASE
                        break;
                    }
                    if(k==nrOfTerms-1){                             //ALL TERMS WERE CORRECTLY PLACED
                        foundPhrase = true;                         //PHRASE IS FOUND
                        output.add(tempDoc);                        //ADD DOCUMENT TO ANSWER
                    }
                }
            }
            pointers = resetPointers(pointers);                     //RESET POINTERS FOR NEXT DOCUMENTCHECK
        }
    return output;
    }

    //WHAT IF QUERY CONTAINS SAME TERM MORE THAN ONCE????????????????????????????????????? I HAVE NO IDEA?!!!!! BATMAN!
                    //df_t = DOCUMENT FREQUENCY = NUMBER OF DOCUMENTS IN COLLECTION THAT CONTAINS TERM t
    public PostingsList rankedRetrieval(ArrayList<PostingsList> postListsIn){
        HashMap<Integer,Integer> docIDs = new HashMap<Integer,Integer>();   //FOR COUNTING docIDs
        HashMap<Integer,PostingsEntry> docs = new HashMap<Integer,PostingsEntry>();     //FOR KEEPING DOCUMENTS AVAILABLE FOR OUTPUT
        HashMap<Integer,Integer> getIndex = new HashMap<Integer,Integer>(); //FOR GETTING INDEX IN MATRIX

        int index = -1;         //EACH docID NEEDS A KEY. index WILL BE THE NUMBER OF docIDs EVENTUALLY
        for(int i=0;i<postListsIn.size();i++){                              //FOR EACH PostingsList
            for(int j=0;j<postListsIn.get(i).size();j++){                   //FOR EACH PostingsEntry
                if(!docIDs.containsValue(postListsIn.get(i).get(j).docID)){;//IF NOT ALREADY ADDED
                    index++;
                System.out.println("docID: "+postListsIn.get(i).get(j).docID);
                System.out.println("index: "+index);
                    docIDs.put(index,postListsIn.get(i).get(j).docID);      //ADD docID
                    getIndex.put(postListsIn.get(i).get(j).docID,index);
                    docs.put(index,postListsIn.get(i).get(j));
                }
            }
        }
        int rows = postListsIn.size();
        int cols = index+1;                                      //+1 SINCE index=n MEANS n+1 DOCUMENTS

        double[][] matrix = new double[rows][cols];                 //THE MATRIX!!11!!
        double[] idf = new double[rows];                            //INVERSE DOCUMENT FREQUENCIES
        
        for(int i=0;i<postListsIn.size();i++){                      //FOR EACH PostingsList (TERM)
            idf[i] = Math.log((double)cols/postListsIn.get(i).size())/Math.log(10);      //(ADD NR OF DOCUMENTS TO docfreqs)
            for(int j=0;j<postListsIn.get(i).size();j++){           //FOR EACH PostingsEntry (DOCUMENT)
                int tempID = postListsIn.get(i).get(j).docID;
                int tempScore = postListsIn.get(i).get(j).tf;
                matrix[i][getIndex.get(tempID)] = tempScore;        //ADD DOCUMENT SCORE TO MATRIX
            }
        }

        double[] sumOfSquares = new double[cols];                   //FOR NORMALIZING LATER
        for(int col=0;col<cols;col++){
            for(int row=0;row<rows;row++){          //TERM-FREQUENCY -> LOG FREQUENCY
                if(matrix[row][col] > 0){
                    //matrix[row][col] = 1 + Math.log(matrix[row][col]) / Math.log(10);   //GIVEN FORMULA
                    //matrix[row][col] = matrix[row][col] * idf[row];                      //tf --> tf-idf
                    sumOfSquares[col] += Math.pow(matrix[row][col],2);                  //SUMMING SQUARES OF ALL ELEMENTS
                }
            }
            sumOfSquares[col] = Math.sqrt(sumOfSquares[col]);                                //SQURTING SQUARES OF ALL EMELEMNTS
        }                   //MATRIX SHOULD NOW BE FILLED WITH tf-idf VALUES... YAY!!
        
        //LETS NORMALIZE THE WHOLE MATRIX!!!
        for(int col=0;col<cols;col++){
            for(int row=0;row<rows;row++){
                //matrix[row][col] = matrix[row][col] / sumOfSquares[col];
            }
        }                   //MATRIX SHOULD NOW BE NORMALIZED.. YEY!!!

        //CREATE tf-idf VECOR FOR QUERY
        double[] query = new double[rows];
        Arrays.fill(query,1);                                   //FILL QUERY WITH ONES; IT COINTAINS EACH TERM ONCE.. (RIGHT?)              BATMAN

        for(int i=0;i<rows;i++){
            query[i] = query[i] * Math.log(cols/postListsIn.get(i).size()) / Math.log(10);      //tf -> tf*idf
        }

        double[] scores = new double[cols];
        for(int col=0;col<cols;col++){
            for(int row=0;row<rows;row++){
                scores[col] += query[row] * matrix[row][col];
            }
        }

        //CREATE PostingsList CONTAINING PostingsEntrys SORTED ACCORDING TO scores
        PostingsList output = new PostingsList();

        for(int col=0;col<cols;col++){
            docs.get(col).score = scores[col];                                      //GIVE EACH DOCUMENT ITS SCORE SO IT CAN BE SORTED
            output.add(docs.get(col));                                              //ADD ALL DOCUMENTS TO THE OUTPUT
        }


//        Collections.sort(output.list);
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
 //for(int i=0;i<cols;i++){ System.out.print(sumOfSquares[i]+" "); }
 System.out.println();

        //System.out.println("idf.size(): "+idf.length);
        //for(int i=0;i<rows;i++){ System.out.println(idf[i]); }
        //FOR PRINTING                                                             //UNNECESSARY CRAP!!!!!!!!!!!!!
        for(int row=0;row<rows;row++){                                              //ALL OF IT!
            for(int col=0;col<cols;col++){
                System.out.print(matrix[row][col]+" ");
            }
            System.out.println("");
        }


        //create matrix: col = length(docIDs), row = nrOfTerms+2?   docID   7  132  84  95  8  74  35
        //                                                          wizard  3   2   1   8   0   0   1
        //                                                          warlock 0   0   13  7   3   0   0
        //                                                          mageiy  12  11  15  9   0   3   0  
        //                                                          WEIGHTS 15  13  29  24  3   3   1
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return output;
    }

    public boolean hasInteger(int targetIn, ArrayList<Integer> listIn, int[] pointers, int pointer){
        int size = listIn.size();
        for(int i=pointers[pointer];i<size;i++){                                                            //BORJA FROM NOLL = NO PROBLEM
            if(listIn.get(i) == targetIn){
                pointers[pointer] = i;          //NEW
                return true;
            }
            if(listIn.get(i) > targetIn){       //NEW
                pointers[pointer] = i;          //NEW
                return false;                   //NEW
            }                                   //NEW
        }
        return false;
    }
    public int[] resetPointers(int[] pointersIn){
        int[] pointersOut = pointersIn;
        for(int i=0;i<pointersIn.length;i++){
            pointersOut[i] = 0;
        }
        return pointersOut;
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
