/*
 * StorageTraineeTest.java
 * JUnit based test
 *
 * Created on February 24, 2004, 9:13 AM
 */
package edu.msu.cme.rdp.seqmatch.train;

import java.io.IOException;
import java.io.File;
import java.nio.ShortBuffer;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wangqion
 */
public class StorageTraineeTest {

    StorageTrainee trainee;

    public StorageTraineeTest() throws IOException {
        String traineefile1 = StorageTraineeTest.class.getResource("/test/seqmatch/50TrainSeq.trainee").getFile();
        trainee = new StorageTrainee(new File(traineefile1));
    }

    /** Test of getSeqName method, of class edu.msu.cme.rdp.seqmatch.train.StorageTrainee. */
    @Test
    public void testGetSeqName() throws IOException {
        System.out.println("testGetSeqName");
        String seqName = trainee.getSeqName(1);
        assertEquals("AB0390", seqName);
        assertEquals(trainee.getNumOfSeqs(), 50);

    }

    /** Test of getWordCount method, of class edu.msu.cme.rdp.seqmatch.train.StorageTrainee. */
    @Test
    public void testGetWordCount() throws IOException {
        System.out.println("testGetWordCount");

        int wordCount = trainee.getWordCount(0);
        assertEquals(wordCount, 1311);
        wordCount = trainee.getWordCount(1);
        assertEquals(wordCount, 1397);
        wordCount = trainee.getWordCount(2);
        assertEquals(wordCount, 1420);
    }

    /** Test of slice method, of class edu.msu.cme.rdp.seqmatch.train.StorageTrainee. */
    @Test
    public void testSlice() throws IOException {
        System.out.println("testSlice");

        //word aaaaactc
        ShortBuffer wordSeqs = trainee.slice(55);

        assertEquals(wordSeqs.limit() - wordSeqs.position(), 5);
        assertEquals(wordSeqs.get(), 0);
        assertEquals(wordSeqs.get(), 1);
        wordSeqs.get();
        wordSeqs.get();
        assertEquals(wordSeqs.get(), -28);

        // aaaagtg
        wordSeqs = trainee.slice(38);

        assertEquals(wordSeqs.get(), 1);
        assertEquals(wordSeqs.get(), 6);

        wordSeqs = trainee.slice(Trainee.MAX_NUM_WORD - 1);
        assertEquals(wordSeqs.limit() - wordSeqs.position(), 0);

    }
}
