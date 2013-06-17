/*
 * MultiTraineeSeqMatch.java
 *
 * Created on February 25, 2004, 2:18 PM
 */
package edu.msu.cme.rdp.seqmatch.core;

import edu.msu.cme.rdp.readseq.readers.Sequence;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author  wangqion
 */
public class MultiTraineeSeqMatch implements SeqMatch {

    private List<SeqMatch> matches = null;

    /** Creates a new instance of MultiTraineeSeqMatch */
    public MultiTraineeSeqMatch(List<SeqMatch> matches) {
        this.matches = matches;
    }

    private MultiTraineeSeqMatch() {
        // empty constructor
    }

    public SeqMatch duplicate() {
        MultiTraineeSeqMatch retval = new MultiTraineeSeqMatch();
        retval.matches = new ArrayList();
        
        for(SeqMatch origMatch : matches) {
            retval.matches.add(origMatch.duplicate());
        }
        return retval;
    }

    /**
     * Given a query sequence, do sequence match using one or more Trainees,
     * returns a set containing the top certain number of
     * closest matches in the descending order of scores.
     * @param seq   a query sequence
     * @param numOfResults  the number of results requested
     * @return a ordered Set of results
     */
    public SeqMatchResultSet match(Sequence seq, int numOfResults) {
        Iterator<SeqMatch> it = matches.iterator();
        SeqMatchResultSet allResults = null;

        int wordCount = 0;
        if (it.hasNext()) {
            SeqMatch aMatch = it.next();
            SeqMatchResultSet results = aMatch.match(seq, numOfResults);
            wordCount = results.getQueryWordCount();
            allResults = new SeqMatchResultSet(wordCount, results.getQuerySeq());
            allResults.addAll(results);
        }

        while (it.hasNext()) {
            SeqMatch aMatch = it.next();
            SeqMatchResultSet results = aMatch.match(seq, numOfResults);
            allResults.addAll(results);
        }

        SeqMatchResultSet retVal = new SeqMatchResultSet(wordCount, allResults.getQuerySeq());

        int count = 0;
        for (SeqMatchResult result : allResults) {
            retVal.add(result);
            
            if ( ++count >= numOfResults) {
                break;
            }
        }

        return retVal;
    }
}
