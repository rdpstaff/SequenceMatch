/*
 * Trainee.java
 *
 * Created on February 19, 2004, 3:41 PM
 */

package edu.msu.cme.rdp.seqmatch.train;
import  edu.msu.cme.rdp.seqmatch.seq.WordGenerator;
import java.nio.ShortBuffer;


/**
 *
 * @author  wangqion
 */
public interface Trainee {
  
  static final int WORD_SIZE = WordGenerator.WORD_SIZE;
  static final int MAX_NUM_WORD =  WordGenerator.MAX_NUM_WORD;
  static final int NAME_SIZE = 20;
  static final int MAX_NUM_SEQ = Short.MAX_VALUE + 1;
  
  ShortBuffer slice(int word);
  
  String getSeqName(int id);
  
  int getWordCount( int id );
  int getNumOfSeqs();
  Trainee duplicate();

}
