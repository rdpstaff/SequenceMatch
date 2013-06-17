/*
 * WordGenerator can be used to fetch unique words from a sequence string.
 * A word is 7-base subsequences represented by an integer.
 * Created on February 18, 2004, 3:29 PM
 *
 *
 * @author  wangqion
 */
package edu.msu.cme.rdp.seqmatch.seq;

import java.util.BitSet;

public class WordGenerator {

    public static final int WORD_SIZE = 7;
    public static final int MAX_NUM_WORD = 1 << (WORD_SIZE * 2);
    private static final int MASK = MAX_NUM_WORD - 1;
    private static final int MAX_ASCII = 128;
    private String seqString;
    private int nextWord = 0;
    private int baseCount = 1; // top of loop will subtract
    private int position = 0;
    private BitSet bitSet = new BitSet(MAX_NUM_WORD);
    private boolean invalid = false;
    private static int[] charLookup = new int[MAX_ASCII];

    static {
        for (int i = 0; i < MAX_ASCII; i++) {
            charLookup[i] = -1;
        }
        charLookup['A'] = 0;  // A
        charLookup['T'] = 1;  // T
        charLookup['U'] = 1;  // U
        charLookup['G'] = 2;  // G
        charLookup['C'] = 3;  // C

        charLookup['a'] = 0;  // a
        charLookup['t'] = 1;  // t
        charLookup['u'] = 1;  // u
        charLookup['g'] = 2;  // g
        charLookup['c'] = 3;  // c
    }

    /**
     * Creates a new instance of WordGenerator
     * @param seq   the sequence string of a sequence
     */
    public WordGenerator(String seq) {
        seqString = seq;
    }

    /**
     * Checks to see if any more word exists. Only count unique words.
     * @return true if there is a word exists
     *          false if there is no word exists
     */
    public boolean hasNext() {
        int charIndex = 0;

        do {
            --baseCount;

            while (baseCount < WORD_SIZE && position < seqString.length()) {
                char nextBase = seqString.charAt(position++);

                charIndex = charLookup[nextBase];
                if (charIndex == -1) {
                    baseCount = -1;
                    charIndex = 0;
                }

                ++baseCount;
                nextWord <<= 2;
                nextWord &= MASK;
                nextWord |= charIndex;
            }

        } while (bitSet.get(nextWord) == true && position < seqString.length());

        if (baseCount < WORD_SIZE) {
            invalid = true;
            return false;
        } else if (bitSet.get(nextWord) == false) {
            bitSet.set(nextWord);
            return true;
        }
        return false;
    }

    /**
     * Returns the next word. hasNext() should be called before this method.
     */

    public int next() {
        if (invalid) {
            throw new IllegalStateException("Attempt to call WordGenerator.next() when no more words.");
        }
        return nextWord;
    }
}
