/*
 * TwowayMatchTest.java
 * JUnit based test
 *
 * Created on February 24, 2004, 2:59 PM
 */
package edu.msu.cme.rdp.seqmatch.core;

import edu.msu.cme.rdp.readseq.readers.MemSeqReader;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.seqmatch.train.ConcreteTrainee;
import edu.msu.cme.rdp.seqmatch.train.TraineeFactory;

import java.io.IOException;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author wangqion
 */
public class TwowayMatchTest extends TestCase {

    public TwowayMatchTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TwowayMatchTest.class);
        return suite;
    }

    /** Test of match method, of class edu.msu.cme.rdp.seqmatch.core.MatchEngine. */
    public void testMatch() throws IOException {
        System.out.println("testMatch");

        List<Sequence> seqs = Arrays.asList(new Sequence("seq1", "", "AAAAAAAAAGGACCCCCCCCUn"), new Sequence("seq2", "", "AAAAAAAAGACCCCCCCCUn"), new Sequence("seq3", "", "GAAAAAAAGGCCCCCCCCU"));

        TraineeFactory factory = new TraineeFactory();
        ConcreteTrainee trainee = factory.train(new MemSeqReader(seqs));
        File file = File.createTempFile("testMatchEngine", "tmp");
        file.deleteOnExit();

        SeqMatchEngine onewayEngine = new SeqMatchEngine(trainee);
        TwowaySeqMatch engine = new TwowaySeqMatch(onewayEngine);

        Sequence seq = new Sequence("seq5", "", "AGGGGGGGGCCUUUUUUUC");
        SeqMatchResultSet scores = engine.match(seq, 6);

        assertEquals(3, scores.size());

        Iterator<SeqMatchResult> it = scores.iterator();

        SeqMatchResult r = it.next();

        assertEquals(r.getSeqName(), "seq3");
        assertTrue(r.isReverse());

        r = it.next();
        assertEquals(r.getSeqName(), "seq1");
        assertTrue(r.isReverse());
    }
}
