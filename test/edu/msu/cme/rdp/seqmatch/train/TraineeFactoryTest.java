/*
 * TraineeFactoryTest.java
 * JUnit based test
 *
 * Created on February 19, 2004, 5:12 PM
 */
package edu.msu.cme.rdp.seqmatch.train;

import edu.msu.cme.rdp.readseq.writers.FastaWriter;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import java.io.File;
import java.io.IOException;
import java.nio.ShortBuffer;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wangqion
 */
public class TraineeFactoryTest {

    /** Test of train method, of class edu.msu.cme.rdp.seqmatch.train.TraineeFactory. */
    @Test
    public void testTrain() throws IOException {
        File tmpFile = File.createTempFile("seqmatch_trainee", "");
        tmpFile.deleteOnExit();

        FastaWriter out = new FastaWriter(tmpFile);
        out.writeSeq("seq1", "AAAAAAAAAGG-CCCCCCCCUnnnnnnnnnnnnnn");
        out.writeSeq("seq1", "AAAAAAAAAGGCCCCCCCCU");
        out.writeSeq("seq3", "aaaaaaaaaU");
        out.writeSeq("seq4", "aaaaaaaGaaaaaaaUC");
        out.close();        
        
        TraineeFactory factory = new TraineeFactory();
        ConcreteTrainee trainee = (ConcreteTrainee)factory.train(new SequenceReader(tmpFile));
        int largestWord = (int) Math.pow(4,Trainee.WORD_SIZE) - 1;

        //aaaaaaa
        ShortBuffer buf = trainee.wordIndexList[0].asShortBuffer();
        assertEquals(buf.get(), 0);
        assertEquals(buf.get(), -3);

        assertEquals( 11, trainee.getWordCount(0) );
        assertEquals( 11, trainee.getWordCount(1) );
        //ccccccc
        buf = trainee.slice( largestWord );
        assertEquals( 2, buf.limit() - buf.position() );
        assertEquals(buf.get(), 0);
        assertEquals(buf.get(), 1);

        buf = trainee.slice( 9 );
        assertEquals( buf.limit(), buf.position() );

        buf = trainee.slice( 0 );
        assertEquals( buf.limit(), 2 );
        assertEquals( buf.get(), 0);
        assertEquals( buf.get(), -3);

        buf = trainee.slice( 1 );
        assertEquals( buf.limit(), 2 );
        assertEquals( buf.get(), 2);
        assertEquals( buf.get(), 3);

        buf = trainee.slice( 2 );
        assertEquals( buf.limit(), 3 );
        assertEquals( buf.get(), 0);
        assertEquals( buf.get(), 1);

        buf = trainee.slice( 7 );
        assertEquals( buf.limit(), 1 );
        assertEquals( buf.get(), 3);

        buf = trainee.slice( largestWord );
        assertEquals( buf.limit(), 2 );
        assertEquals( buf.get(), 0);
        assertEquals( buf.get(), 1);
    }
}
