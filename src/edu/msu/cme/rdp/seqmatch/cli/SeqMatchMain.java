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
import edu.msu.cme.rdp.seqmatch.core.*;
import edu.msu.cme.rdp.seqmatch.train.StorageTrainee;
import edu.msu.cme.rdp.seqmatch.train.Trainee;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
        options.addOption("d", "desc", true, "A tab-delimited description file containing seqID and description");
        options.addOption("o", "outFile", true, "Write output to a file");
    }

    public static HashMap<String, String> readDesc(File infile) throws IOException {
        HashMap<String, String> descMap = new HashMap<String, String>();
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String line;
        while( (line=reader.readLine()) != null){
            String[] val = line.split("\\s+");
            StringBuilder desc = new StringBuilder();
            if ( val.length > 1) {
                desc.append(val[1]);
            }
            for ( int i = 2; i < val.length; i++){
                desc.append(" ").append(val[i]);
            }
            int length = val[0].length() > Trainee.NAME_SIZE ? Trainee.NAME_SIZE: val[0].length();
            descMap.put(val[0].substring(0, length), desc.toString());
        }
        reader.close();
        return descMap;
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
                System.err.println("USAGE: train <reference sequences> <trainee_out_file_prefix>" +
                        "\nMultiple trainee output files might be created, each containing maximum " + Trainee.MAX_NUM_SEQ + " sequences");
                return;
            }

            File refSeqs = new File(args[0]);
            File traineeFileOut = new File(args[1]);

            //maybe more than 1 trainee files need to be created, depending on the number of seqs
            CreateMultiMatchFromFile.getMultiTrainee(refSeqs, traineeFileOut);
        } else if (cmd.equals("seqmatch")) {
            File refFile = null;
            File queryFile = null;
            HashMap<String,String> descMap = new HashMap<String, String>();
            PrintStream out = new PrintStream(System.out);
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
                if (line.hasOption("desc")) {
                    descMap = readDesc(new File(line.getOptionValue("desc")));
                }
                if (line.hasOption("outFile")) {
                    out = new PrintStream(new File(line.getOptionValue("outFile")));
                }
                
                args = line.getArgs();

                if (args.length != 2) {
                    throw new Exception("Unexpected number of command line arguments");
                }

                refFile = new File(args[0]);
                queryFile = new File(args[1]);

            } catch (Exception e) {
                new HelpFormatter().printHelp("seqmatch <refseqs | trainee_file_or_dir> <query_file>\n" +
                        " trainee_file_or_dir is a single trainee file or a directory containing multiple trainee files", options);
                System.err.println("Error: " + e.getMessage());
                return;
            }
            

            SeqMatch seqmatch = null;
            if ( refFile.isDirectory()){  // a directory of trainee files
                List<SeqMatch> engineList = new ArrayList<SeqMatch>();
                for ( File f: refFile.listFiles()){
                    if ( !f.isHidden()){
                        TwowaySeqMatch match = new TwowaySeqMatch(new SeqMatchEngine(new StorageTrainee(f)));
                        engineList.add(match);
                    }
                }
                 seqmatch = new MultiTraineeSeqMatch(engineList);
            }else {  // a single fasta file or trainee file
                if (SeqUtils.guessFileFormat(refFile) == SequenceFormat.UNKNOWN) {
                    seqmatch = CLISeqMatchFactory.trainTwowaySeqMatch(new StorageTrainee(refFile));
                } else {
                    seqmatch =  CreateMultiMatchFromFile.getMultiMatch(refFile);
                }
            }

            out.println("query name\tmatch seq\torientation\tS_ab score\tunique oligomers\tdescription");

            SeqReader reader = new SequenceReader(queryFile);
            Sequence seq;

            while ((seq = reader.readNextSequence()) != null) {
                SeqMatchResultSet resultSet = seqmatch.match(seq, knn);
                for (SeqMatchResult result : resultSet) {
                    char r = '+';
                    if (result.isReverse()) {
                        r = '-';
                    }

                    if (result.getScore() > minSab) {
                        out.println(seq.getSeqName() + "\t" + result.getSeqName() + "\t" + r + "\t" 
                                + result.getScore() + "\t" + resultSet.getQueryWordCount() + "\t" + descMap.get(result.getSeqName()));
                    }
                }
            }

            out.close();
        }else {
            throw new IllegalArgumentException("USAGE: SeqMatchMain [train|seqmatch] <args>");
        }
    }
}
