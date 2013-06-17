/*
 * TwowaySeqMatch.java
 *
 * Created on February 18, 2004, 4:58 PM
 */
package edu.msu.cme.rdp.seqmatch.core;

import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.utils.IUBUtilities;

/**
 *
 * @author  wangqion
 */
public class TwowaySeqMatch implements SeqMatch {

    SeqMatch match;

    /** Creates a new instance of TwowaySeqMatch */
    public TwowaySeqMatch(SeqMatch match) {
        this.match = match;
    }

    private TwowaySeqMatch() {
        //empty constructor
    }

    public SeqMatch duplicate() {
        TwowaySeqMatch retval = new TwowaySeqMatch();
        retval.match = this.match.duplicate();
        return retval;
    }

    /**
     * Given a query sequence, do sequence match using both forward
     * and reverse sequence strings,
     * returns a set containing the top certain number of
     * closest matches in the descending order of scores.
     * @param seq   a query sequence
     * @param numOfResults  the number of results requested
     * @return a ordered Set of results
     */
    public SeqMatchResultSet match(Sequence seq, int numOfResults) {
        Sequence reverseSeq = new Sequence(seq.getSeqName(), seq.getDesc(), IUBUtilities.reverseComplement(seq.getSeqString()));
        SeqMatchResultSet forwardResults = match.match(seq, numOfResults);
        SeqMatchResultSet reverseResults = match.match(reverseSeq, numOfResults);
        // if the training set contains less sequences than the number of results requested
        int numOfReturns = Math.min(forwardResults.size(), numOfResults);

        for(SeqMatchResult reverseResult : reverseResults) {
            reverseResult.reverse = true;
        }

        forwardResults.addAll(reverseResults);

        SeqMatchResultSet combinedResults = new SeqMatchResultSet(forwardResults.getQueryWordCount(), forwardResults.getQuerySeq());

        for(SeqMatchResult result : forwardResults) {
            combinedResults.add(result);

            if(combinedResults.size() >= numOfReturns) {
                break;
            }
        }

        return combinedResults;
    }
}
