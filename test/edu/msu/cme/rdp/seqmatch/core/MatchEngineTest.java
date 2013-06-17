/*
 * MatchEngineTest.java
 * JUnit based test
 *
 * Created on February 20, 2004, 2:44 PM
 */
package edu.msu.cme.rdp.seqmatch.core;

import edu.msu.cme.rdp.readseq.readers.MemSeqReader;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.seqmatch.train.*;

import java.util.Set;
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
public class MatchEngineTest extends TestCase {

    public MatchEngineTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(MatchEngineTest.class);
        return suite;
    }

    /** Test of match method, of class edu.msu.cme.rdp.seqmatch.core.MatchEngine. */
    public void testMatch() throws IOException {
        System.out.println("testMatch");

        List<Sequence> seqs = Arrays.asList(new Sequence("seq1", "", "AAAAAAAAAGGACCCCCCCCUn"), new Sequence("seq3", "", "GAAAAAAAGGCCCCCCCCU"), new Sequence("seq2", "", "AAAAAAAAGACCCCCCCCUn"));

        TraineeFactory factory = new TraineeFactory();
        ConcreteTrainee trainee = factory.train(new MemSeqReader(seqs));
        
        File file = File.createTempFile("testMatchEngine", "tmp");
        file.deleteOnExit();

        trainee.save(file);
        SeqMatchEngine engine = new SeqMatchEngine(trainee);

        Sequence seq = new Sequence("seq4", "", "GAAAAAAAGGCCCCCCCCU");
        SeqMatchResultSet scores = engine.match(seq, 3);
        assertEquals(3, scores.size());
        Iterator<SeqMatchResult> it = scores.iterator();

        SeqMatchResult r1 = it.next();
        assertEquals(r1.getSeqName(), "seq3");

        it.next();
        SeqMatchResult r3 = (SeqMatchResult) it.next();
        

        Trainee trainee2 = new StorageTrainee(file);
        
        engine = new SeqMatchEngine(trainee2);
        Set store_scores = engine.match(seq, 3);
        assertEquals(3, scores.size());

        Iterator<SeqMatchResult> store_it = store_scores.iterator();

        SeqMatchResult r2;
        r2 = store_it.next();
        assertEquals(r1.getSeqName(), r2.getSeqName());
        store_it.next();

        SeqMatchResult r4 = store_it.next();
        assertEquals(r3.getSeqName(), r4.getSeqName());

        System.out.flush();
    }
}
