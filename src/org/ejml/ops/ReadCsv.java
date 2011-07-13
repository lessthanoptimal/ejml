/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.ops;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Base class for reading CSV formatted files.  CSV stands for column-space-value where text strings are separated
 * by a space character.  The values are typically stored in a human readable format.  The encoded text for a single
 * variable is referred to as a word.
 * </p>
 *
 * <p>
 * Comments are allowed and identified by starting a line with the comment character.  The comment character is user
 * configurable.  By default there is no comment character.
 * </p>
 *
 * @author Peter Abeles
 */
public class ReadCsv {
    // if there is a comment character
    private boolean hasComment = false;
    // what the comment character is
    private char comment;

    // reader for the input stream
    private BufferedReader in;

    // number of lines that have been read
    private int lineNumber = 0;

    /**
     * Constructor for ReadCsv
     *
     * @param in Where the input comes from.
     */
    public ReadCsv(InputStream in) {
        this.in = new BufferedReader(new InputStreamReader(in));
    }

    /**
     * Sets the comment character.  All lines that start with this character will be ignored.
     *
     * @param comment The new comment character.
     */
    public void setComment(char comment) {
        hasComment = true;
        this.comment = comment;
    }

    /**
     * Returns how many lines have been read.
     *
     * @return Line number
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Returns the reader that it is using internally.
     * @return The reader.
     */
    public BufferedReader getReader() {
        return in;
    }

    /**
     * Finds the next valid line of words in the stream and extracts them.
     *
     * @return List of valid words on the line.  null if the end of the file has been reached.
     * @throws java.io.IOException
     */
    protected List<String> extractWords() throws IOException
    {
        while( true ) {
            lineNumber++;
            String line = in.readLine();
            if( line == null ) {
                return null;
            }

            // skip comment lines
            if( hasComment ) {
                if( line.charAt(0) == comment )
                    continue;
            }

            // extract the words, which are the variables encoded
            return parseWords(line);
        }
    }

    /**
     * Extracts the words from a string.  Words are seperated by a space character.
     *
     * @param line The line that is being parsed.
     * @return A list of words contained on the line.
     */
    protected List<String> parseWords(String line) {
        List<String> words = new ArrayList<String>();
        boolean insideWord = !isSpace(line.charAt(0));
        int last = 0;
        for( int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if( insideWord ) {
                // see if its at the end of a word
                if( isSpace(c)) {
                    words.add( line.substring(last,i) );
                    insideWord = false;
                }
            } else {
                if( !isSpace(c)) {
                    last = i;
                    insideWord = true;
                }
            }
        }

        // if the line ended add the final word
        if( insideWord ) {
            words.add( line.substring(last));
        }
        return words;
    }

    /**
     * Checks to see if 'c' is a space character or not.
     *
     * @param c The character being tested.
     * @return if it is a space character or not.
     */
    private boolean isSpace(char c) {
        return c == ' ' || c == '\t';
    }
}
