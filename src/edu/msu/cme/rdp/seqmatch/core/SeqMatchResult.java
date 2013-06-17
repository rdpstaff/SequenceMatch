/*
 * SeqMatchResult.java
 *
 * Created on February 18, 2004, 4:41 PM
 */
package edu.msu.cme.rdp.seqmatch.core;

import java.io.Serializable;

/**
 *
 * @author  wangqion
 */
public class SeqMatchResult implements Serializable {

    String seqName = "";   // the name of the match sequence
    short internalID;  // the integer ID of the match sequence
    float score;    // the score of the match
    boolean reverse = false;  // true means forward direction
    int wordCount;  // the number of valid unique words
    double sim = Double.NaN;

    /** Creates a new instance of SeqMatchResult */
    SeqMatchResult(String seqName, short matchID, float score, int wordCount) {
        this.seqName = seqName;
        this.internalID = matchID;
        this.score = score;
        this.wordCount = wordCount;
    }

    SeqMatchResult(short matchID, float score, int wordCount) {
        this.internalID = matchID;
        this.score = score;
        this.wordCount = wordCount;
    }

    /** Returns the integer ID of the match sequence */
    public short getInternalID() {
        return internalID;
    }

    /** Returns the name of the match sequence */
    public String getSeqName() {
        return seqName;
    }

    /** Returns the score of the match */
    public float getScore() {
        return score;
    }

    /** Returns the number of valid unique words in the sequence */
    public int getWordCount() {
        return wordCount;
    }

    /**
     * Checks the direction of the query sequence string used in the match.
     * @return <code>true</code> if the sequence string used in the match is reversed,
     *         <code>false</code> otherwise.
     */
    public boolean isReverse() {
        return reverse;
    }

    public void setSimilarity(double d) {
        this.sim = d;
    }

    public double getSimilarity() {
        return sim;
    }
}
