/*
 * SeqmatchWebWrapper.java
 *
 * Created on March 3, 2004, 2:16 PM
 */

/*
 * SeqMatchFactory.java
 *
 * Created on February 25, 2004, 5:26 PM
 */
package edu.msu.cme.rdp.seqmatch.cli;

import edu.msu.cme.rdp.seqmatch.core.SeqMatch;
import edu.msu.cme.rdp.seqmatch.core.SeqMatchEngine;
import edu.msu.cme.rdp.seqmatch.core.TwowaySeqMatch;
import edu.msu.cme.rdp.seqmatch.train.Trainee;
import edu.msu.cme.rdp.seqmatch.train.TraineeFactory;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author  wangqion
 */
public class CLISeqMatchFactory {

    private static final TraineeFactory traineeFactory = new TraineeFactory();

    public static SeqMatch trainTwowaySeqMatch(Trainee trainee) {
        return new TwowaySeqMatch(trainSeqMatch(trainee));
    }

    public static SeqMatch trainSeqMatch(Trainee trainee) {
        return new SeqMatchEngine(trainee);
    }

    public static SeqMatch trainTwowaySeqMatch(File traineeFile) throws IOException {
        return new TwowaySeqMatch(trainSeqMatch(traineeFile));
    }

    public static SeqMatch trainSeqMatch(File traineeFile) throws IOException {
        return new SeqMatchEngine(traineeFactory.train(traineeFile));
    }

    public static TraineeFactory getTraineeFactory() {
        return traineeFactory;
    }
}
