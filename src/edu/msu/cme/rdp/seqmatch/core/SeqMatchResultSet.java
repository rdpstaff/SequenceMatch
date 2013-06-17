/*
 * SeqMatchResultSet.java
 *
 * Created on February 26, 2004, 2:54 PM
 */
package edu.msu.cme.rdp.seqmatch.core;

import edu.msu.cme.rdp.readseq.readers.Sequence;
import java.io.Serializable;
import java.util.TreeSet;
import java.util.Comparator;

/**
 *
 * @author  wangqion
 */
public class SeqMatchResultSet extends TreeSet<SeqMatchResult> implements Serializable {

    private int queryWordCount;
    private Sequence querySeq;

    /** Creates a new instance of SeqMatchResultSet */
    public SeqMatchResultSet(int words, Sequence querySeq) {
        super(new ResultComparator());
        this.queryWordCount = words;
        this.querySeq = querySeq;
    }

    public int getQueryWordCount() {
        return queryWordCount;
    }

    public Sequence getQuerySeq() {
        return querySeq;
    }

    public static class ResultComparator implements Comparator<SeqMatchResult>, Serializable {

        public int compare(SeqMatchResult lhs, SeqMatchResult rhs) {
            int retVal = 0;
            if (lhs.getScore() == rhs.getScore()) {
                retVal = lhs.getSeqName().compareTo(rhs.getSeqName());
            } else {
                retVal = (lhs.getScore() < rhs.getScore()) ? 1 : -1;
            }
            return retVal;
        }
    }
}
