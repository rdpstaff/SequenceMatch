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
import edu.msu.cme.rdp.readseq.utils.IUBUtilities;
import edu.msu.cme.rdp.seqmatch.core.SeqMatch;
import edu.msu.cme.rdp.seqmatch.core.SeqMatchResult;
import edu.msu.cme.rdp.seqmatch.core.SeqMatchResultSet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author wangqion
 */
public class CheckReverseSeq {
    private SeqMatch match = null;
    private String traineeDesc = null;
    public static float DIFF_SCORE_CUTOFF = 0.3f;   // default different between the sab scores from original seqstring and reversed seqstring
    private float diffScoreCutoff = DIFF_SCORE_CUTOFF;
    
    public CheckReverseSeq(String traineeFile, String traineeDesc) throws IOException{
        match = CreateMultiMatchFromFile.getMultiMatch(new File(traineeFile));
        this.traineeDesc = traineeDesc;
        if ( this.traineeDesc == null){
            this.traineeDesc = traineeFile;
        }
    }
    
    public CheckReverseSeq(String traineeFile, String traineeDesc, float diffScoreCutoff) throws IOException{
        this(traineeFile, traineeDesc);
        this.diffScoreCutoff = diffScoreCutoff;
    }
    
    public CheckReverseSeq(SeqMatch match, String traineeDesc){
        this.match = match;
        this.traineeDesc = traineeDesc;
    }
    
    /**
     * For each query, do seqmatch with the original sequence, and do seqmatch with the reversed but not complemented sequence 
     * @param seq
     * @return 
     */
    public ArrayList<SeqMatchResultSet> check(Sequence seq){
        ArrayList<SeqMatchResultSet> result = new ArrayList<SeqMatchResultSet>();
        result.add(match.match(seq, 1));

        // check the reverse without complement
        Sequence revSeq = new Sequence(seq.getSeqName(), seq.getDesc(), IUBUtilities.reverse(seq.getSeqString()));
        result.add(match.match(revSeq, 1));
        return result;
        
    }
    
    /**
     * return a SeqMatchResult if sequence is good, return null if sequence is reversed but not complemented
     * @param seq
     * @return 
     */
    public SeqMatchResult getGoodSeqMatchResult(Sequence seq){
        ArrayList<SeqMatchResultSet> result = check(seq);
        SeqMatchResult origResult = result.get(0).iterator().next();

        // check the reverse without complement
        SeqMatchResult revResult = result.get(1).iterator().next();
        
        if ( (revResult.getScore() - origResult.getScore()) < diffScoreCutoff ){
            return origResult;
        }
        return null;
    }
    
    /**
     * return true if sequence is reversed but not complemented
     * @param seq
     * @return 
     */
    public boolean isRevNotComplement(Sequence seq){
        ArrayList<SeqMatchResultSet> result = check(seq);
        SeqMatchResult origResult = result.get(0).iterator().next();

        // check the reverse without complement
        SeqMatchResult revResult = result.get(1).iterator().next();
        
        if ( (revResult.getScore() - origResult.getScore()) < diffScoreCutoff ){
            return false;
        }
        return true;

    }
   
    
    public String getTraineeDesc(){
        return traineeDesc;
    }
}
