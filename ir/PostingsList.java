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
import java.lang.Math;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
    
    /** The postings list as a linked list. */
    public LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();

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
    public void add2(PostingsEntry entryIn){
        for(int i=0;i<list.size();i++){                                 //FOR ALL POSTS (DOCUMENTS)
            int tempDocID = (list.get(i)).docID;                        //PARTICULAR DocID
            if(entryIn.docID == tempDocID){                             //IF DOCUMENT ALREADY EXISTS
                (list.get(i)).addPosition((entryIn.positions).get(0));  //ADD WORDPOSITION TO DOCUMENT
                break;
                //INCREASE FREQUENCY?   NOT NEEDED FOR ASSIGNMENT
            }
            if(entryIn.docID < tempDocID){                              //IF SMALLER THAN -> INSERT
                list.add(i,entryIn);
                break;
            }
            else{
                if(i==list.size()-1){                                   //IF AT LAST POS -> INSERT
                    list.add(entryIn);
                    break;
                }
                if(entryIn.docID < (list.get(i+1)).docID){  //IF SMALLER THAN -> AND LARGER THAN <-
                    list.add(i+1,entryIn);                  //INSERT
                    break;
                }
            }
        }
        if(list.size() == 0){                                           //LIST EMPTY -> INSERT
            list.add(entryIn);
        }
    }

    public void add(PostingsEntry entryIn){
        if(list.size() == 0){ list.add(entryIn); return; }
        if(list.size() == 1){
            if(entryIn.docID == list.get(0).docID){           //IF POINTING AT SAME DOCUMENTA
                (list.get(0)).addPosition((entryIn.positions).get(0));
                //(list.get(0)).score++;                          //NEW
                (list.get(0)).tf++;                                 //NEWER
                return;
            }
            if(entryIn.docID > list.get(0).docID){
                list.add(entryIn);
                return;
            }
            else{
                list.add(0,entryIn);
                return;
            }
        }
        Pointer pointer = new Pointer(list.size());

        while(true){
            if(entryIn.docID == list.get(pointer.value-1).docID){           //IF POINTING AT SAME DOCUMENT
                (list.get(pointer.value-1)).addPosition((entryIn.positions).get(0));
                //(list.get(pointer.value-1)).score++;                        //NEW
                (list.get(pointer.value-1)).tf++;                             //NEWER
                break;
            }
            if(pointer.value-1 == 0){                                       //IF POINTER == FIRST
                if(entryIn.docID < list.get(0).docID){                      //IF SMALLER THAN FIRST
                    list.add(0,entryIn);                                    //ADD AT FIRST POSITION
                    break;
                }
                else{                                                       //IF LARGER THAN FIRST
                 //   list.add(1,entryIn);
                    if(entryIn.docID < list.get(1).docID){
                        list.add(1,entryIn);
                        break;
                    }
                    pointer.right();                                        //GO RIGHT
                 //   break;
                }
            }
            else if(pointer.value-1 == list.size()-1){                      //IF POINTER == LAST
                if(entryIn.docID > list.get(list.size()-1).docID){          //IF LARGER THAN LAST
                    list.add(entryIn);                                      //ADD AT LAST POSITION
                    break;
                }
                else{                                                       //IF SMALLER THAN LAST
                   // list.add(list.size()-1,entryIn);
                   // break;
                   if(entryIn.docID > list.get(list.size()-2).docID){
                        list.add(list.size()-2,entryIn);
                        break;
                   }
                   pointer.left();                                          //GO LEFT
                }
            }
            else{// if(pointer.value-1 != 0 && pointer.value-1 != list.size()-1){   //IF POINTER != FIRST/LAST
                if(entryIn.docID < list.get(pointer.value-1).docID){        //IF SMALLER THAN POINTED
                    if(entryIn.docID > list.get(pointer.value-2).docID){
                        list.add(pointer.value-1,entryIn);
                        break;
                    }
                    pointer.left();                                         //LEFT
                }
                else{                                                       //IF LARGER THAN POINTED
                    if(entryIn.docID < list.get(pointer.value).docID){
                        list.add(pointer.value,entryIn);
                        break;
                    }
                    pointer.right();                                        //RIGHT
                }
            }
        }
    }

    class Pointer{
        public int value;
        int startValue;
        int nrOfJumps;
        int jump;
        int listSize;

        public Pointer(int listSizeIn){
            value = listSizeIn/2; 
            if(value<1){ value = 1; } 
            startValue = listSizeIn/2;
            nrOfJumps = 1;
            listSize = listSizeIn;
            jump = value/2;
        }

        public void right(){
            setNextJump();
            value = value+jump;
            nrOfJumps++;
        }
        public void left(){
            setNextJump();
            value = value-jump;
            nrOfJumps++;
        }
        public void setNextJump(){
            if(jump>1){ jump = startValue/(FUCKYOUJAVA(2,nrOfJumps)); }
            if(jump<1){ jump = 1; }
        }
    }
    //
    //  YOUR CODE THERE
    //
    public Integer FUCKYOUJAVA(int a,int b){
        int output = 1;
        if(b==0){ return 1; }

        for(int i=0;i<b;i++){
            output = output*a;
        }
        return output;
    }
}

