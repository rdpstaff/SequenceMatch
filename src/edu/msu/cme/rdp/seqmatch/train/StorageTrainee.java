/*
 * StorageTrainee.java
 *
 * Created on February 23, 2004, 3:02 PM
 */
package edu.msu.cme.rdp.seqmatch.train;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author  wangqion
 */
//# seqs    4 bytes
//
public class StorageTrainee implements Trainee {

    private CharBuffer seqIDs;
    private ShortBuffer wordCounts;
    private IntBuffer wordIndicies;
    private ShortBuffer wordSeqs;
    private int numOfSeqs;
    private int nameSize = Trainee.NAME_SIZE;

    public StorageTrainee(URL url) throws IOException {
        File buildFrom;
        if (url.getProtocol().equals("file")) {
            buildFrom = new File(url.getFile());
        } else {
            if (System.getProperty("java.io.tmpdir") == null) {
                throw new IOException("System temporary directory (java.io.tmpdir) is not set, cannot cache trainee files on the local machine");
            }

            buildFrom = new File(System.getProperty("java.io.tmpdir"), url.getFile().replace("/", "_"));

            if (!buildFrom.exists()) {
                buildFrom.deleteOnExit();
                FileOutputStream outStream = new FileOutputStream(buildFrom);
                InputStream in = url.openStream();
                byte[] buf = new byte[4096];
                int read;

                while ((read = in.read(buf)) != -1) {
                    outStream.write(buf, 0, read);
                }

                in.close();
                outStream.close();
            }
        }

        buildFromFile(buildFrom);
    }

    /** Creates a new instance of StorageTrainee */
    public StorageTrainee(File file) throws IOException {
        buildFromFile(file);
    }

    private StorageTrainee() {
        ;// empty constructor
    }

    private void buildFromFile(File file) throws IOException {
        int currentPosition = 0;
        FileInputStream fs = new FileInputStream(file);
        FileChannel fc = fs.getChannel();
        int sz = (int) fc.size();

        MappedByteBuffer rawBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
        //
        numOfSeqs = rawBuffer.getInt();
        currentPosition = rawBuffer.position();

        rawBuffer.limit(currentPosition + numOfSeqs * 2 * Trainee.NAME_SIZE);
        rawBuffer.position(currentPosition);
        ByteBuffer rawSeqIDs = rawBuffer.slice();
        seqIDs = rawSeqIDs.asCharBuffer();

        /* This is not neccessary if the files were built using the same Trainee.NAME_SIZE.
         * It failed to find the correct nameSize if the first seqname size is 20 because it searches for a first space in seqnames
         */
        boolean firstId = true;
        int nextIdStart = -1;

        for (int index = 0; index < seqIDs.length(); index++) {
            if (firstId && seqIDs.charAt(index) == ' ') {
                firstId = false;
            } else if (!firstId && seqIDs.charAt(index) != ' ') {
                nextIdStart = index;
                break;
            }
        }

        if (nextIdStart == -1) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Only one sequence id in trainee file {0} unable to estimate trainee file version", file);
        } else {
            if (nextIdStart != Trainee.NAME_SIZE) {
                Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Detected different seqid length [{0}] in file {1} than expected[{2}]...trying to recover", new Object[] { nextIdStart, file, Trainee.NAME_SIZE});

                rawBuffer.limit(currentPosition + numOfSeqs * 2 * nextIdStart);
                rawBuffer.position(currentPosition);
                rawSeqIDs = rawBuffer.slice();
                seqIDs = rawSeqIDs.asCharBuffer();
                nameSize = nextIdStart;
            }
        }

        currentPosition = rawBuffer.limit();

        rawBuffer.limit(currentPosition + numOfSeqs * 2);
        rawBuffer.position(currentPosition);
        ByteBuffer rawWordCounts = rawBuffer.slice();
        wordCounts = rawWordCounts.asShortBuffer();
        currentPosition = rawBuffer.limit();

        rawBuffer.limit(currentPosition + (Trainee.MAX_NUM_WORD + 1) * 4);
        rawBuffer.position(currentPosition);
        ByteBuffer rawWordIndicies = rawBuffer.slice();
        wordIndicies = rawWordIndicies.asIntBuffer();
        currentPosition = rawBuffer.limit();

        rawBuffer.limit(sz);
        rawBuffer.position(currentPosition);
        ByteBuffer rawWordSeqs = rawBuffer.slice();
        wordSeqs = rawWordSeqs.asShortBuffer();

        fc.close();
        fs.close();
    }

    public Trainee duplicate() {
        StorageTrainee retval = new StorageTrainee();
        retval.seqIDs = this.seqIDs.duplicate();
        retval.wordIndicies = this.wordIndicies.duplicate();
        retval.wordCounts = this.wordCounts.duplicate();
        retval.wordSeqs = this.wordSeqs.duplicate();
        retval.numOfSeqs = this.numOfSeqs;
        return retval;
    }

    public String getSeqName(int id) {
        int startPos = id * nameSize;
        int endPos = startPos + nameSize;
        seqIDs.limit(endPos).position(startPos);
        CharBuffer idBuf = seqIDs.slice();
        return idBuf.toString().trim();
    }

    public int getWordCount(int id) {
        return wordCounts.get(id);
    }

    public int getNumOfSeqs() {
        return numOfSeqs;
    }

    public ShortBuffer slice(int word) {
        int startPos = wordIndicies.get(word);
        int endPos = wordIndicies.get(word + 1);
        wordSeqs.limit(endPos);
        wordSeqs.position(startPos);

        return wordSeqs;
    }
}
