/*
 * SeqMatchResultSetTest.java
 * JUnit based test
 *
 * Created on April 14, 2004, 3:10 PM
 */
package edu.msu.cme.rdp.seqmatch.core;

import java.util.Iterator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author wangqion
 */
public class SeqmatchResultSetTest extends TestCase {

    public SeqmatchResultSetTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SeqmatchResultSetTest.class);
        return suite;
    }

    /** Test of getQueryWordCount method, of class edu.msu.cme.rdp.seqmatch.core.SeqMatchResultSet. */
    public void testSeqMatchResultSet() {
        System.out.println("testGetQueryWordCount");
        SeqMatchResult result1 = new SeqMatchResult("test1", (short) 1, (float) 1.0, 1200);

        SeqMatchResult result2 = new SeqMatchResult("test2", (short) 2, (float) 1.0, 1200);

        SeqMatchResult result3 = new SeqMatchResult("test3", (short) 3, (float) 0.9, 1200);

        SeqMatchResult result4 = new SeqMatchResult("test4", (short) 1, (float) 1.0, 1200);


        SeqMatchResultSet set1 = new SeqMatchResultSet(1300, null);
        set1.add(result1);
        set1.add(result2);

        SeqMatchResultSet set2 = new SeqMatchResultSet(1390, null);
        set2.add(result3);
        set2.add(result4);

        SeqMatchResultSet set3 = new SeqMatchResultSet(1200, null);
        set3.addAll(set1);
        set3.addAll(set2);
        assertEquals(set3.size(), 4);

        Iterator it = set1.iterator();
        while (it.hasNext()) {
            SeqMatchResult s = (SeqMatchResult) it.next();
            assertEquals(s.score, 1.0, 0.01);;
        }

        SeqMatchResultSet test = new SeqMatchResultSet(1000, null);
        it = set1.iterator();
        while (it.hasNext()) {
            SeqMatchResult s = (SeqMatchResult) it.next();
            test.add(s);
            s.score = 0.6f;
        }

        it = set1.iterator();
        while (it.hasNext()) {
            SeqMatchResult s = (SeqMatchResult) it.next();
            assertEquals(s.score, 0.6, 0.01);
        }

        it = test.iterator();
        while (it.hasNext()) {
            SeqMatchResult s = (SeqMatchResult) it.next();
            assertEquals(s.score, 0.6, 0.01);
        }
    }
}
