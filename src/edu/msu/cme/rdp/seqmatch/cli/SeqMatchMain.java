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

import edu.msu.cme.rdp.readseq.SequenceFormat;
import edu.msu.cme.rdp.readseq.readers.SequenceReader;
import edu.msu.cme.rdp.readseq.readers.SeqReader;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import edu.msu.cme.rdp.readseq.utils.SeqUtils;
import edu.msu.cme.rdp.seqmatch.core.SeqMatch;
import edu.msu.cme.rdp.seqmatch.core.SeqMatchResult;
import edu.msu.cme.rdp.seqmatch.core.SeqMatchResultSet;
import edu.msu.cme.rdp.seqmatch.train.ConcreteTrainee;
import edu.msu.cme.rdp.seqmatch.train.StorageTrainee;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 *
 * @author fishjord
 */
public class SeqMatchMain {

    private static final Options options = new Options();

    static {
        options.addOption("k", "knn", true, "Find k nearest neighbors [default = 20]");
        options.addOption("s", "sab", true, "Minimum sab score [default = .5]");
    }

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.err.println("USAGE: SeqMatchMain [train|seqmatch] <args>");
            return;
        }

        String cmd = args[0];
        args = Arrays.copyOfRange(args, 1, args.length);

        if (cmd.equals("train")) {
            if (args.length != 2) {
                System.err.println("USAGE: train <reference sequences> <training_out_file>");
                return;
            }

            File refSeqs = new File(args[0]);
            File traineeFileOut = new File(args[1]);

            ConcreteTrainee trainee = CLISeqMatchFactory.getTraineeFactory().train(refSeqs);
            trainee.save(traineeFileOut);
        } else if (cmd.equals("seqmatch")) {
            File refFile = null;
            File queryFile = null;

            int knn = 20;
            float minSab = .5f;

            try {
                CommandLine line = new PosixParser().parse(options, args);

                if (line.hasOption("knn")) {
                    knn = Integer.parseInt(line.getOptionValue("knn"));
                }

                if (line.hasOption("sab")) {
                    minSab = Float.parseFloat(line.getOptionValue("sab"));
                }

                args = line.getArgs();

                if (args.length != 2) {
                    throw new Exception("Unexpected number of command line arguments");
                }

                refFile = new File(args[0]);
                queryFile = new File(args[1]);

            } catch (Exception e) {
                new HelpFormatter().printHelp("seqmatch <refseqs | trainee_file> <query_file>", options);
                System.err.println("Error: " + e.getMessage());
                return;
            }
            PrintStream out = new PrintStream(System.out);

            SeqMatch seqmatch = null;
            if (SeqUtils.guessFileFormat(refFile) == SequenceFormat.UNKNOWN) {
                seqmatch = CLISeqMatchFactory.trainTwowaySeqMatch(new StorageTrainee(refFile));
            } else {
                seqmatch =  CreateMultiMatchFromFile.getMultiMatch(refFile);
            }

            out.println("query name\ttarget seq\torientation\tS_ab score\tunique common oligomers");

            SeqReader reader = new SequenceReader(queryFile);
            Sequence seq;

            while ((seq = reader.readNextSequence()) != null) {
                SeqMatchResultSet resultSet = seqmatch.match(seq, knn);
                for (SeqMatchResult result : resultSet) {
                    char r = '+';
                    if (result.isReverse()) {
                        r = '-';
                    }

                        out.println(seq.getSeqName() + "\t" + result.getSeqName() + "\t" + r + "\t" + result.getScore() + "\t" + result.getWordCount());
                    if (result.getScore() > minSab) {
                        out.println(seq.getSeqName() + "\t" + result.getSeqName() + "\t" + r + "\t" + result.getScore() + "\t" + result.getWordCount());
                    }
                }
            }

            out.close();
        }
    }
}
