/*
 * WordGeneratorTest.java
 * JUnit based test
 *
 * Created on February 19, 2004, 2:51 PM
 */
package edu.msu.cme.rdp.seqmatch.seq;

import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author wangqion
 */
public class WordGeneratorTest extends TestCase {

    public WordGeneratorTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(WordGeneratorTest.class);
        return suite;
    }

    /** Test of hasNext method, of class edu.msu.cme.rdp.seqmatch.seq.WordGenerator. */
    public void testHasNext() throws IOException {
        System.out.println("testHasNext");

        String seq = "AAAAAAAAAGG-CCCCCCCCUn";
        WordGenerator generator = new WordGenerator(seq);

        assertTrue(generator.hasNext());
        assertEquals(generator.next(), 0);

        generator.hasNext();
        assertEquals(generator.next(), 2);
        generator.hasNext();
        assertEquals(generator.next(), 10);
        generator.hasNext();

        assertEquals(generator.next(), 16383);
        generator.hasNext();
        assertEquals(generator.next(), 16381);
        assertFalse(generator.hasNext());
        assertFalse(generator.hasNext());

        try {
            generator.next();
            fail("should throw IllegalStateException");
        } catch (IllegalStateException ex) {
        }

    }
}
