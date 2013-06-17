/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.msu.cme.rdp.seqmatch.cli;


import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.seqmatch.core.SeqMatchResult;
import edu.msu.cme.rdp.seqmatch.core.SeqMatchResultSet;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author wangqion
 */
public class CheckReverseSeqTest {
    CheckReverseSeq checker = null;
    Sequence goodSeq  = new Sequence("good", "", "cccgtcgagtgaacgctggcggtaggcctaacacatgcaagtcgaacggcagcacaggagagcttgctctctgggtggcgagtggcggacgggtgaggaatacatcggaatctactctgtcgtgggggataacgtagggaaacttacgctaataccgca");
    Sequence badSeq  = new Sequence("badseq", "", "cgaagaccacgttgtttgagggtaccacactgcccgccacacatgttccgggcccttgcataagtggcgtcgttacgactagacgctaatgatcgctaaggctgaagtgcctcagctcaacgtctgaggctaggcctgactctacccc");

    public CheckReverseSeqTest() throws IOException {
        checker = new CheckReverseSeq(this.getClass().getResource("/test/seqmatch/test_seqmatch_train.fa").getPath(), "4");
    }


    /**
     * Test of check method, of class CheckReverseSeq.
     */
    @Test
    public void testCheck() {
       ArrayList<SeqMatchResultSet> result = checker.check(goodSeq);
       SeqMatchResult origResult = result.get(0).iterator().next();
       assertEquals(origResult.getSeqName(), "s1");
       assertEquals(origResult.getScore(), 1.0f, 0.01f);
       
        // check the reverse without complement
       SeqMatchResult revResult = result.get(1).iterator().next();
       assertEquals(revResult.getSeqName(), "s1");
       assertEquals(revResult.getScore(), 0.13f, 0.01f);
    }

    /**
     * Test of getGoodSeqMatchResult method, of class CheckReverseSeq.
     */
    @Test
    public void testGetGoodSeqMatchResult() {
        SeqMatchResult result = checker.getGoodSeqMatchResult(goodSeq);
        assertNotNull(result);
        assertEquals(result.getSeqName(), "s1");
        result = checker.getGoodSeqMatchResult(badSeq);
        assertNull(result);
    }

    /**
     * Test of isRevNotComplement method, of class CheckReverseSeq.
     */
    @Test
    public void testIsRevNotComplement() {
        boolean isRev = checker.isRevNotComplement(goodSeq);
        assertFalse(isRev);
        
        isRev = checker.isRevNotComplement(badSeq);
        assertTrue(isRev);
    }

    /**
     * Test of getTraineeDesc method, of class CheckReverseSeq.
     */
    @Test
    public void testGetTraineeDesc() {
        assertEquals(checker.getTraineeDesc(), "4");
    }
}
