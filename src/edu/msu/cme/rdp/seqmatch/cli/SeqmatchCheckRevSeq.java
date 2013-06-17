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
import edu.msu.cme.rdp.readseq.utils.IUBUtilities;
import edu.msu.cme.rdp.seqmatch.core.*;

import java.io.*;
import java.util.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 * This class reads a query sequence file and a fasta training file for seqmatch. 
 * if specify checkReverse, it checks if the reverse string of the sequence has higher S_ab score than the original seq
 * @author wangqion
 */
public class SeqmatchCheckRevSeq {

    private String dformat = "%1$.3f";
    private static final Options options = new Options();
    private HashMap<String, String> seqDescMap = new HashMap<String, String>();   // key= seqID, value= seq description
    

    static {
        options.addOption("c", "checkReverse", false, "check reverse but not complemented string of the input sequence, default is false");
        options.addOption("q", "queryFile", true, "query file");
        options.addOption("t", "trainingFile", true, "training file, can use /work/wangqion/rdpHarvestTools/seqmatch/trainsetno4_032012/seqmatch_train.fa");
        options.addOption("o", "outputFile", true, "good seqs seqmatch output file");
        options.addOption("n", "numOfResults", true, "number of results for each query. Default = 20");
        options.addOption("r", "rev_outputFile", true, "reversed seq seqmatch output file");
        options.addOption("f", "format", true, "format of the match result: tab, dbformat, xml, default=tab");
        options.addOption("s", "corrected_seqFile", true, "corrected sequence of the bad seqs");
        options.addOption("d", "difScoreCutoff", true, "different s_ab score cutoff, between the original seq and the reversed seq, default = 0.3, range [0 ~ 1]");
        options.addOption("h", "traineeDesc", true, "training description to be written to output");
    }
    
      

    /**
     * Do sequence match without check reverse string
     * @param inFileName
     * @param outFileName
     * @param traineeFile
     * @param numOfResults
     * @param format
     * @throws IOException 
     */
    public void doUserLibMatch(String inFileName, String traineeFile, String outFileName, int numOfResults, String format, String traineeDesc)
            throws IOException {
        SeqMatch match = CreateMultiMatchFromFile.getMultiMatch(new File(traineeFile));
        if ( traineeDesc == null){
            traineeDesc = traineeFile;
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName));

        Map<String, SeqMatchResultSet> resultMap = new HashMap();
        Sequence seq;

        SequenceReader reader = new SequenceReader(new File(inFileName));

        while ((seq = reader.readNextSequence()) != null) {
            SeqMatchResultSet resultSet = match.match(seq, numOfResults);
            resultMap.put(seq.getSeqName(), resultSet);
            printResult(resultMap, format, writer, traineeDesc);
            resultMap.clear();
        }

        reader.close();
        writer.close();
    }
    
    /**
     * For each query, compares the S_ab scores of the original
     * sequence and the reversed but not complemented sequence. If the score differs above a threshold, the sequence is then
     * marked as bad seq. There are three output files: the seqmatch results of the good sequences, the seqmatch results of the bad sequences, 
     * and a fasta file of the corrected sequence strings of the bad sequences.
     * @param inFileName
     * @param traineeFile
     * @param outFileName
     * @param revOutFileName
     * @param correctedQueryOut
     * @param diffScoreCutoff
     * @param trainset_no
     * @throws IOException 
     */
    public void checkRevSeq(String inFileName, String traineeFile, String outFileName, PrintWriter revOutputWriter, 
            PrintStream correctedQueryOut, float diffScoreCutoff, String format , String traineeDesc)
            throws IOException {
        CheckReverseSeq checker = new CheckReverseSeq(traineeFile, traineeDesc);       
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName));

        Map<String, SeqMatchResultSet> resultMap = new HashMap();
        Sequence seq;

        SequenceReader reader = new SequenceReader(new File(inFileName));

        while ((seq = reader.readNextSequence()) != null) {
            ArrayList<SeqMatchResultSet> result = checker.check(seq);
            SeqMatchResultSet resultSet = result.get(0);
            float origScore = resultSet.iterator().next().getScore();
            
            // check the reverse without complement
            SeqMatchResultSet revResultSet = result.get(1);
            float revScore = revResultSet.iterator().next().getScore();
                       
            if ( (revScore - origScore) < diffScoreCutoff ){
                resultMap.put(seq.getSeqName(), resultSet);
                printResult(resultMap, format, writer, traineeDesc);
            }else {   // this sequence is possibly a bad sequence
                resultMap.put(seq.getSeqName(), revResultSet);
                printResult(resultMap, format, revOutputWriter, traineeDesc);
                correctedQueryOut.print(">" + seq.getSeqName() + "\t" + seq.getDesc() +"\t" + revScore +"\t" + origScore + "\n" + IUBUtilities.reverse(seq.getSeqString()) +"\n");
            }
            
            resultMap.clear();
        }

        reader.close();
        writer.close();
        revOutputWriter.close();
    }

    

    /** Writes the seqmatch result to a file.
     * resultMap: key= seqName, value= match set
     */
    private void printResult(Map resultMap, String format, Writer writer, String traineeDesc) throws IOException {
        if (format.equals("xml")) {
            printXml(resultMap, writer, traineeDesc);
            return;
        }
        if (format.equals("txt")) {
            printTab(resultMap, writer, traineeDesc);
            return;
        }
        if (format.equals("dbformat")){
            printDBformat(resultMap, writer, traineeDesc);
            return;
        }

    }

    private void printTab(Map<String, SeqMatchResultSet> resultMap, Writer writer, String traineeDesc) throws IOException {

        //out.println("query name\ttarget seq\torientation\tS_ab score\tunique common oligomers");
        for (String seqName : resultMap.keySet()) {

            SeqMatchResultSet scores = resultMap.get(seqName);
            for (SeqMatchResult r : scores) {
                char orientation = '+';
                if (r.isReverse()) {
                    orientation = '-';
                }

               writer.write( seqName +"\t" + r.getSeqName() + "\t" + orientation + "\t" + String.format(dformat, r.getScore()) + "\t" + r.getWordCount() +"\n");

            }
        }

    }

    private void printXml(Map<String, SeqMatchResultSet> resultMap, Writer writer, String traineeDesc) throws IOException {

        writer.write("<SequenceMatch data=\"" + traineeDesc + "\" date=\"" + new Date() + "\">\n");
        for (String seqName : resultMap.keySet()) {

            SeqMatchResultSet scores = resultMap.get(seqName);
            StringBuilder buf = new StringBuilder();
            buf.append("<MatchList SeqID=\"").append(seqName).append("\" Olis=\"").append(scores.getQueryWordCount()).append("\">\n");

            for (SeqMatchResult r : scores) {
                String matchName = r.getSeqName();

                buf.append("<Match><ID>").append(matchName).append("</ID><S_ab>").append(String.format(dformat, r.getScore())).append("</S_ab><Similarity>").append(r.getSimilarity()).append("</Similarity><Olis>").append(r.getWordCount()).append("</Olis>");
                if (r.isReverse()) {
                    buf.append("<Reverse>" + "yes" + "</Reverse>");
                } else {
                    buf.append("<Reverse>" + "no" + "</Reverse>");
                }
                String desc = "";
                String tmpdesc = (String) this.seqDescMap.get(matchName);
                if (tmpdesc != null) {
                    desc = tmpdesc;
                }
                buf.append("<SeqDesc>").append(desc).append("</SeqDesc></Match>\n");
            }
            buf.append("</MatchList>\n");
            writer.write(buf.toString());
        }
        writer.write("</SequenceMatch>");
    }


    private void printDBformat(Map resultMap, Writer writer, String trainset_no) throws IOException {
        Iterator seqNames = resultMap.keySet().iterator();

        while (seqNames.hasNext()) {
            String seqName = (String) seqNames.next();
            Set scores = (Set) resultMap.get(seqName);
            Iterator scoreIt = scores.iterator();
            while (scoreIt.hasNext()) {
                SeqMatchResult r = (SeqMatchResult) scoreIt.next();
                String matchName = r.getSeqName();
                writer.write(seqName + "\t" + trainset_no + "\t" + String.format(dformat, r.getScore()) + "\t" + matchName +"\n");
            }
        }

    }
    
    
   

    public static void main(String[] args) throws Exception {

        String trainingFile = null;
        String queryFile = null;
        String outputFile = null;
        PrintWriter revOutputWriter = new PrintWriter(System.out);
        PrintStream correctedQueryOut = System.out;
        String traineeDesc = null;
        int numOfResults = 20;
        boolean checkReverse = false;
        
        float diffScoreCutoff = CheckReverseSeq.DIFF_SCORE_CUTOFF;
        String format = "txt";     // default
        
        try {
            CommandLine line = new PosixParser().parse(options, args);

            if (line.hasOption("c")) {
                checkReverse = true;
            }
            
            if (line.hasOption("t")) {
                trainingFile = line.getOptionValue("t");
            } else {
                throw new Exception("training file must be specified");
            }
            if (line.hasOption("q")) {
                queryFile = line.getOptionValue("q");
            } else {
                throw new Exception("query file must be specified");
            }
            if (line.hasOption("o")) {
                outputFile = line.getOptionValue("o");
            } else {
                throw new Exception("output file must be specified");
            }
            if (line.hasOption("r")) {
                revOutputWriter = new PrintWriter(line.getOptionValue("r"));
            } 
            if (line.hasOption("s")) {
                correctedQueryOut = new PrintStream(line.getOptionValue("s"));
            } 
            if (line.hasOption("d")) {
                diffScoreCutoff = Float.parseFloat(line.getOptionValue("d"));
            } 
            if (line.hasOption("h")) {
                traineeDesc = line.getOptionValue("h");
            } 
            if (line.hasOption("n")) {
                numOfResults = Integer.parseInt(line.getOptionValue("n"));
            }
            if (line.hasOption("f")) {
                format = line.getOptionValue("f");
                if ( !format.equals("tab") && !format.equals("dbformat") && !format.equals("xml")  ){
                    throw new IllegalArgumentException("Only dbformat, tab or xml format available");
                }
            }
            
        }catch (Exception e) {
            System.out.println("Command Error: " + e.getMessage());
            new HelpFormatter().printHelp(120, "SeqmatchCheckRevSeq", "", options, "", true);
            return;
        }
        
        SeqmatchCheckRevSeq theObj = new SeqmatchCheckRevSeq();
        
        if ( !checkReverse){
            theObj.doUserLibMatch(queryFile, trainingFile, outputFile, numOfResults, format, traineeDesc);
        }else {
            theObj.checkRevSeq(queryFile, trainingFile, outputFile, revOutputWriter, correctedQueryOut, diffScoreCutoff, format, traineeDesc);
        }
    }
    
}
