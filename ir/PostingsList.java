/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.LinkedList;
import java.io.Serializable;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
    
    /** The postings list as a linked list. */
    private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();

    /**  Number of postings in this list  */
    public int size() {
        return list.size();
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
        return list.get( i );
    }

    //
    //  YOUR CODE HERE
    //
    public PostingsList(){
        //System.out.println("PostingsList: Created! WITHOUT INPUT");
    }
    public PostingsList(PostingsEntry entryIn){
        //System.out.println("PostingsList: Created!");
        list.add(entryIn);
    }

    //SHOULD ADD PostingsEntry AT POSITION ACCORDING TO docID
    public void add(PostingsEntry entryIn){
       // System.out.println("PostingsList: Entry added!");
        for(int i=0;i<list.size();i++){                     // USE ARRAYLIST OF BOOLEANS INSTEAD!!
            if(entryIn.docID == (list.get(i)).docID){
                break;
                //INCREASE FREQUENCY?
            }
            if(entryIn.docID < (list.get(i)).docID){        // IF SMALLER THAN -> INSERT
                list.add(i,entryIn);
                break;
            }
            else{
            //if(entryIn.docID > (list.get(i)).docID){        // IF LARGER THAN
                if(i==list.size()-1){
                    list.add(entryIn);
                    break;
                }
                if(entryIn.docID < (list.get(i+1)).docID){  // IF SMALLER THAN -> INSERT
                    list.add(i+1,entryIn);
                    break;
                }
            }
        }
        if(list.size() == 0){
            list.add(entryIn);
        }
    }

    //
    //  YOUR CODE THERE
    //
}

