/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.io.Serializable;
import java.util.ArrayList;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
    public double score;
    public int tf;
    public ArrayList<Integer> positions = new ArrayList<Integer>();

    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
        return Double.compare( other.score, score );
    }

    //
    //  YOUR CODE HERE
    //
    public PostingsEntry(int docIDIn, int positionIn){
        docID = docIDIn;
        positions.add(positionIn);
        tf = 1;
    }
    public PostingsEntry(int docIDIn, double scoreIn){
        docID = docIDIn;
        score = scoreIn;
    }

    public void addPosition(int positionIn){
        positions.add(positionIn);
    }
    //
    //  YOUR CODE THERE
    //

}

    
