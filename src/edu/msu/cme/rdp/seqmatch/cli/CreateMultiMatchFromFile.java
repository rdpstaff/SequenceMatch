/*
 * Copyright (C) 2012 Michigan State University <rdpstaff at msu.edu>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.msu.cme.rdp.seqmatch.cli;

import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import edu.msu.cme.rdp.seqmatch.core.MultiTraineeSeqMatch;
import edu.msu.cme.rdp.seqmatch.core.SeqMatch;
import edu.msu.cme.rdp.seqmatch.core.SeqMatchEngine;
import edu.msu.cme.rdp.seqmatch.core.TwowaySeqMatch;
import edu.msu.cme.rdp.seqmatch.train.ConcreteTrainee;
import edu.msu.cme.rdp.seqmatch.train.Trainee;
import edu.msu.cme.rdp.seqmatch.train.TraineeFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wangqion
 */
public class CreateMultiMatchFromFile {
    
    public static SeqMatch getMultiMatch(File traineeFile) throws IOException {
        TraineeFactory factory = new TraineeFactory();
        List<SeqMatch> engineList = new ArrayList<SeqMatch>();
        SequenceReader parser = null;
        int seqCount = 0;
        int fileCount = 1;

        parser = new SequenceReader(traineeFile);
        Sequence seq;
        File tmpFile = File.createTempFile("tempTrainee_" + fileCount, ".fa");
        tmpFile.deleteOnExit();
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));
        // this may create multiple matches, for the limit of sequences for each trainee is SHORT.MAX_VALUE
        while ((seq = parser.readNextSequence()) != null) {
            if (seqCount == Trainee.MAX_NUM_SEQ) {
                // make one trainee
                writer.close();
                ConcreteTrainee trainee = (ConcreteTrainee) factory.train(tmpFile);
                TwowaySeqMatch match = new TwowaySeqMatch(new SeqMatchEngine(trainee));
                engineList.add(match);
                tmpFile.delete();

                // create the next one                		
                seqCount = 0;
                tmpFile = File.createTempFile("tempTrainee_" + (++fileCount), ".fa");
                writer = new BufferedWriter(new FileWriter(tmpFile));

            }
            writer.write(">" + seq.getSeqName() + "\n" + seq.getSeqString() + "\n");
            seqCount++;
        }

        //make the last one
        writer.close();

        ConcreteTrainee trainee = (ConcreteTrainee) factory.train(tmpFile);
        TwowaySeqMatch match = new TwowaySeqMatch(new SeqMatchEngine(trainee));
        engineList.add(match);

        SeqMatch retval = new MultiTraineeSeqMatch(engineList);

        return retval;

    }
    
    public static void getMultiTrainee(File traineeFile, File outprefix) throws IOException {
        TraineeFactory factory = new TraineeFactory();
        List<SeqMatch> engineList = new ArrayList<SeqMatch>();
        SequenceReader parser = null;
        int seqCount = 0;
        int fileCount = 1;

        parser = new SequenceReader(traineeFile);
        Sequence seq;
        File tmpFile = File.createTempFile("tempTrainee_" + fileCount, ".fa");
        
        tmpFile.deleteOnExit();
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));
        // this may create multiple matches, for the limit of sequences for each trainee is SHORT.MAX_VALUE
        while ((seq = parser.readNextSequence()) != null) {
            if (seqCount == Trainee.MAX_NUM_SEQ) {
                // make one trainee
                writer.close();
                ConcreteTrainee trainee = (ConcreteTrainee) factory.train(tmpFile);
                trainee.save( new File(outprefix + "_" + String.valueOf(fileCount) + ".trainee"));                
                tmpFile.delete();

                // create the next one                		
                seqCount = 0;
                tmpFile = File.createTempFile("tempTrainee_" + (++fileCount), ".fa");
                writer = new BufferedWriter(new FileWriter(tmpFile));

            }
            writer.write(">" + seq.getSeqName() + "\n" + seq.getSeqString() + "\n");
            seqCount++;
        }

        //make the last one
        writer.close();

        ConcreteTrainee trainee = (ConcreteTrainee) factory.train(tmpFile);
        if (fileCount == 1){ // there is only one file
            trainee.save( new File(outprefix + ".trainee"));
        }else { 
            trainee.save( new File(outprefix + "_" + String.valueOf(fileCount) + ".trainee"));
        }

    }
   
}
