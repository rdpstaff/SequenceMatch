/*
 * Trainee.java
 *
 * Created on February 18, 2004, 4:50 PM
 */
package edu.msu.cme.rdp.seqmatch.train;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.*;
import java.nio.channels.*;
import java.nio.ByteBuffer;

/**
 *
 * @author  wangqion
 */
public class ConcreteTrainee implements Trainee {

    private static final int INITIAL_CAPACITY = 20;
    int numOfSeqs = 0;  //number of training sequences
    ByteBuffer rawSeqIDs;    // sequence IDs
    CharBuffer seqIDs;
    ByteBuffer rawWordCounts;  // word count for each sequence
    ShortBuffer wordCounts;
    ByteBuffer[] wordIndexList = new ByteBuffer[MAX_NUM_WORD];
    // an array of indices of sequence IDs that contains that word

    /** Creates a new instance of Trainee */
    public ConcreteTrainee() {
        rawSeqIDs = ByteBuffer.allocate(2 * NAME_SIZE * MAX_NUM_SEQ);   // sequence IDs
        seqIDs = rawSeqIDs.asCharBuffer();
        rawWordCounts = ByteBuffer.allocate(2 * MAX_NUM_SEQ);  // word count for each sequence
        wordCounts = rawWordCounts.asShortBuffer();
        for (int i = 0; i < MAX_NUM_WORD; i++) {
            wordIndexList[i] = ByteBuffer.allocate(INITIAL_CAPACITY);
        }
    }

    private ConcreteTrainee(int numS) {
        this.numOfSeqs = numS;
    }

    public Trainee duplicate() {
        ConcreteTrainee retval = new ConcreteTrainee(this.numOfSeqs);
        retval.rawSeqIDs = this.rawSeqIDs.duplicate();
        retval.seqIDs = this.seqIDs.duplicate();
        retval.rawWordCounts = this.rawWordCounts.duplicate();
        retval.wordCounts = this.wordCounts.duplicate();
        for (int i = 0; i < Trainee.MAX_NUM_WORD; ++i) {
            retval.wordIndexList[i] = this.wordIndexList[i].duplicate();
        }
        return retval;
    }

    public ShortBuffer slice(int word) {
        ShortBuffer lhs = (wordIndexList[ word].asShortBuffer());
        lhs.position(0);
        return lhs;
    }

    public String getSeqName(int id) {

        seqIDs.limit((id + 1) * Trainee.NAME_SIZE);
        seqIDs.position(id * Trainee.NAME_SIZE);

        CharBuffer aNameBuf = seqIDs.slice();
        return aNameBuf.toString().trim();
    }

    public int getWordCount(int id) {
        return wordCounts.get(id);
    }

    public int getNumOfSeqs() {
        return numOfSeqs;
    }

    public void save(File file) throws IOException {

        ByteBuffer offsets = ByteBuffer.allocate(4 * (MAX_NUM_WORD + 1));
        int offset = 0;
        offsets.putInt(offset);
        for (int i = 0; i < MAX_NUM_WORD; ++i) {
            offset += wordIndexList[i].limit() / 2;
            offsets.putInt(offset);
            wordIndexList[ i].position(0);
        }
        offsets.position(0);
        rawSeqIDs.position(0);

        FileOutputStream os = new FileOutputStream(file);
        FileChannel channel = os.getChannel();

        // this is the number of sequences in the training set
        ByteBuffer numBuf = ByteBuffer.allocate(4);
        numBuf.putInt(numOfSeqs);
        numBuf.position(0);
        if (numBuf.limit() != channel.write(numBuf)) {
            throw new IOException("fail to write the number of training sequences");
        }

        rawSeqIDs.limit(2 * numOfSeqs * NAME_SIZE);

        if (rawSeqIDs.limit() != channel.write(rawSeqIDs)) {
            throw new IOException("fail to write rawSeqIDs");
        }
        rawSeqIDs.position(0);

        rawWordCounts.limit(2 * numOfSeqs);
        if (rawWordCounts.limit() != channel.write(rawWordCounts)) {
            throw new IOException("fail to write rawWordCounts");
        }
        rawWordCounts.position(0);

        if (offsets.limit() != channel.write(offsets)) {
            throw new IOException("fail to write offsets");
        }

        for (int i = 0; i < Trainee.MAX_NUM_WORD; ++i) {
            ByteBuffer buf = wordIndexList[i];
            if (buf.limit() > 0) {
                if (buf.limit() != channel.write(buf)) {
                    throw new IOException("fail to write wordIndexList");
                }
                buf.position(0);
            }
        }
        channel.close();
        os.close();
    }
}
