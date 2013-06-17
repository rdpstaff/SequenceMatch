/*
 * SeqMatchEngine.java
 *
 * Created on February 18, 2004, 4:47 PM
 */
package edu.msu.cme.rdp.seqmatch.core;

import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.sequences.RDPSequence;
import edu.msu.cme.rdp.readseq.utils.SeqUtils;
import edu.msu.cme.rdp.seqmatch.train.Trainee;
import edu.msu.cme.rdp.seqmatch.seq.WordGenerator;
import java.nio.ShortBuffer;

/**
 *
 * @author  wangqion
 */
public class SeqMatchEngine implements SeqMatch {

    private Trainee trainee;
    private short[] seqIdArray = new short[Trainee.MAX_NUM_SEQ];
    private short[] scores = new short[Trainee.MAX_NUM_SEQ];

    /** Creates a new instance of SeqMatchEngine */
    public SeqMatchEngine(Trainee trainee) {
        this.trainee = trainee;
    }

    private SeqMatchEngine() {
        ;//empty constructor
    }

    public SeqMatch duplicate() {
        SeqMatchEngine retval = new SeqMatchEngine();
        retval.trainee = this.trainee.duplicate();
        return retval;
    }

    /**
     * Given a query sequence, using the sequence string as it is,
     * returns a set containing the top certain number of
     * closest matches in the descending order of scores.
     * @param seq   a query sequence
     * @param numOfResults  the number of results requested
     * @return a ordered Set of results
     */
    public SeqMatchResultSet match(Sequence seq, int numOfResults) {
        for (int i = 0; i < Trainee.MAX_NUM_SEQ; ++i) {
            scores[i] = 0;
        }
        WordGenerator generator = new WordGenerator(SeqUtils.getUnalignedSeqString(seq.getSeqString()));
        int wordCount = 0;
        while (generator.hasNext()) {
            ++wordCount;
            int nextWord = generator.next();

            ShortBuffer buf = trainee.slice(nextWord);
            int idCount = buf.limit() - buf.position();

            buf.get(seqIdArray, 0, idCount);
            short lastID = Short.MIN_VALUE;

            for (int i = 0; i < idCount; ++i) {
                short seqID = seqIdArray[i];

                if (seqID < 0) {
                    for (; seqID < 0; ++seqID) {
                        ++scores[++lastID];
                    }
                } else {
                    ++scores[seqID];
                    lastID = seqID;
                }
            }
        }

        SeqMatchResultSet resultSet = new SeqMatchResultSet(wordCount, seq);

        int currentSeq;
        int numOfTraineeSeqs = trainee.getNumOfSeqs();

        for (currentSeq = 0; currentSeq < numOfResults && currentSeq < numOfTraineeSeqs; ++currentSeq) {
            float matches = scores[currentSeq];
            int matchWordCount = trainee.getWordCount(currentSeq);
            float totalWords = Math.min(wordCount, matchWordCount);
            float currentScore = matches / totalWords;
            String seqName = trainee.getSeqName((short) currentSeq);

            resultSet.add(new SeqMatchResult(seqName, (short) currentSeq, currentScore, matchWordCount));
        }

        SeqMatchResult minGoodResult = (SeqMatchResult) resultSet.last();
        float minGoodScore = minGoodResult.getScore();

        for (; currentSeq < numOfTraineeSeqs; ++currentSeq) {

            float matches = scores[currentSeq];
            int matchWordCount = trainee.getWordCount(currentSeq);
            float totalWords = Math.min(wordCount, matchWordCount);
            float currentScore = matches / totalWords;
            if (currentScore > minGoodScore) {
                resultSet.remove(minGoodResult);
                minGoodResult.internalID = (short) currentSeq;
                minGoodResult.seqName = trainee.getSeqName((short) currentSeq);
                minGoodResult.score = currentScore;
                minGoodResult.wordCount = matchWordCount;
                resultSet.add(minGoodResult);
                minGoodResult = (SeqMatchResult) resultSet.last();
                minGoodScore = minGoodResult.getScore();
            }
        }

        return resultSet;
    }
}
