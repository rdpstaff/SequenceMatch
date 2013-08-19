/*
 * TraineeFactory.java
 *
 * Created on February 18, 2004, 4:51 PM
 */
package edu.msu.cme.rdp.seqmatch.train;

import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import edu.msu.cme.rdp.readseq.readers.SeqReader;
import edu.msu.cme.rdp.readseq.utils.SeqUtils;
import edu.msu.cme.rdp.seqmatch.seq.WordGenerator;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.io.File;

/**
 *
 * @author  wangqion
 */
public class TraineeFactory {

    /** Creates a new instance of TraineeFactory */
    public TraineeFactory() {
    }

    public ConcreteTrainee train(File file) throws IOException {
        SequenceReader parser = new SequenceReader(file);
        return this.train(parser);
    }

    public ConcreteTrainee train(SeqReader parser) throws IOException {
        ConcreteTrainee trainee = new ConcreteTrainee();
        Sequence pSeq;
        short seqCount = 0;
        int numOfSeqs = 0;  // the short seqCount will overflow if there are maximum number sequences parsed.
        StringBuilder origNameBuf = new StringBuilder(Trainee.NAME_SIZE);
        for (int i = 0; i < Trainee.NAME_SIZE; i++) {
            origNameBuf.append(" ");
        }

        try {
            while ((pSeq = parser.readNextSequence()) != null) {
                if (numOfSeqs == Trainee.MAX_NUM_SEQ) {  // use numOfSeqs instead of seqCount because seqCount will overflow
                    throw new IOException("Training data cannot have more than " + Trainee.MAX_NUM_SEQ + " sequences");
                }

                String name = pSeq.getSeqName();
                String seqString = SeqUtils.getUnalignedSeqString(pSeq.getSeqString());
                short wordCount = 0;
                WordGenerator generator = new WordGenerator(seqString);
                while (generator.hasNext()) {

                    ++wordCount;
                    int word = generator.next();
                    ByteBuffer buf = trainee.wordIndexList[word];

                    if (buf.position() == buf.limit()) {

                        ByteBuffer newBuf = ByteBuffer.allocate(buf.capacity() * 2);
                        buf.flip();
                        newBuf.put(buf);
                        buf = newBuf;
                        trainee.wordIndexList[word] = buf;
                    }
                    buf.putShort(seqCount);
                }
                // only use the first few chars of the name, check the length
                StringBuffer nameBuf = new StringBuffer(origNameBuf.toString());

                name = name.substring(0, Math.min(name.length(), Trainee.NAME_SIZE));
                if (pSeq.getSeqName().length() > Trainee.NAME_SIZE) {
                    System.err.println("Warning: the length of seqID: " + pSeq.getSeqName() + " exceeds the limit: " + Trainee.NAME_SIZE + " truncating to " + name);
                }
                nameBuf = nameBuf.replace(0, name.length(), name);

                trainee.seqIDs.put(nameBuf.toString());
                trainee.wordCounts.put(wordCount);
                seqCount++;
                numOfSeqs++;
            }

            trainee.numOfSeqs = numOfSeqs;

        } finally {
            parser.close();
        }

        // do compression for wordIndeices after get all the data
        for (int i = 0; i < Trainee.MAX_NUM_WORD; ++i) {
            short lastSeqID = Short.MIN_VALUE;
            short runCount = 0;
            ByteBuffer oldBuffer = trainee.wordIndexList[i];
            ByteBuffer newBuffer = ByteBuffer.allocate(oldBuffer.limit());
            oldBuffer.flip();
            while (oldBuffer.position() < oldBuffer.limit()) {
                short nextSeqID = oldBuffer.getShort();

                if (nextSeqID == lastSeqID + 1) {
                    --runCount;
                } else { // not a continuous seqID
                    if (runCount == -1) {
                        newBuffer.putShort(lastSeqID);
                    } else if (runCount < -1) {
                        newBuffer.putShort(runCount);
                    }
                    newBuffer.putShort(nextSeqID);
                    runCount = 0;
                }
                lastSeqID = nextSeqID;
            }
            if (runCount == -1) {
                newBuffer.putShort(lastSeqID);
            } else if (runCount < -1) {
                newBuffer.putShort(runCount);
            }

            newBuffer.flip();
            trainee.wordIndexList[i] = newBuffer;
        }
        return trainee;

    }
}
