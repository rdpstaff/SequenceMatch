/*
 * SeqMatch.java
 *
 * Created on February 18, 2004, 2:41 PM
 */
package edu.msu.cme.rdp.seqmatch.core;

import edu.msu.cme.rdp.readseq.readers.Sequence;

/**
 *
 * @author  wangqion
 */
public interface SeqMatch {

    public SeqMatchResultSet match(Sequence seq, int numOfResults);

    public SeqMatch duplicate();
}
