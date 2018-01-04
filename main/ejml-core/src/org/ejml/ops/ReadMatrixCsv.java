/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.ops;

import org.ejml.data.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * Reads in a matrix that is in a column-space-value (CSV) format.
 *
 * @author Peter Abeles
 */
public class ReadMatrixCsv extends ReadCsv {

    /**
     * Specifies where input comes from.
     *
     * @param in Where the input comes from.
     */
    public ReadMatrixCsv(InputStream in) {
        super(in);
    }

    /**
     * Reads in a {@link Matrix} from the IO stream.
     * @return Matrix
     * @throws IOException If anything goes wrong.
     */
    public <M extends FMatrix>M read32() throws IOException {
        List<String> words = extractWords();

        if( words.size() == 3 ) {
            int numRows = Integer.parseInt(words.get(0));
            int numCols = Integer.parseInt(words.get(1));
            boolean real = words.get(2).compareToIgnoreCase("real") == 0;

            if (numRows < 0 || numCols < 0)
                throw new IOException("Invalid number of rows and/or columns: " + numRows + " " + numCols);

            if (real)
                return (M) readFDRM(numRows, numCols);
            else
                return (M) readCDRM(numRows, numCols);
        } else if ( words.size() == 4 ) {
            int numRows = Integer.parseInt(words.get(0));
            int numCols = Integer.parseInt(words.get(1));
            int length = Integer.parseInt(words.get(2));
            boolean real = words.get(3).compareToIgnoreCase("real") == 0;

            if (numRows < 0 || numCols < 0)
                throw new IOException("Invalid number of rows and/or columns: " + numRows + " " + numCols);

            if (real)
                return (M) readFSTR(numRows, numCols, length);
            else
                throw new IllegalArgumentException("Sparse complex not yet supported");
        } else {
            throw new IOException("Unexpected number of words on the first line. Found "+words.size());
        }
    }

    /**
     * Reads in a {@link Matrix} from the IO stream.
     * @return Matrix
     * @throws IOException If anything goes wrong.
     */
    public <M extends DMatrix>M read64() throws IOException {
        List<String> words = extractWords();

        if( words.size() == 3 ) {
            int numRows = Integer.parseInt(words.get(0));
            int numCols = Integer.parseInt(words.get(1));
            boolean real = words.get(2).compareToIgnoreCase("real") == 0;

            if (numRows < 0 || numCols < 0)
                throw new IOException("Invalid number of rows and/or columns: " + numRows + " " + numCols);

            if (real)
                return (M) readDDRM(numRows, numCols);
            else
                return (M) readZDRM(numRows, numCols);
        } else if ( words.size() == 4 ) {
            int numRows = Integer.parseInt(words.get(0));
            int numCols = Integer.parseInt(words.get(1));
            int length = Integer.parseInt(words.get(2));
            boolean real = words.get(3).compareToIgnoreCase("real") == 0;

            if (numRows < 0 || numCols < 0)
                throw new IOException("Invalid number of rows and/or columns: " + numRows + " " + numCols);

            if (real)
                return (M) readDSTR(numRows, numCols, length);
            else
                throw new IllegalArgumentException("Sparse complex not yet supported");
        } else {
            throw new IOException("Unexpected number of words on the first line. Found "+words.size());
        }
    }

    /**
     * Reads in a {@link DMatrixRMaj} from the IO stream where the user specifies the matrix dimensions.
     *
     * @param numRows Number of rows in the matrix
     * @param numCols Number of columns in the matrix
     * @return DMatrixRMaj
     * @throws IOException
     */
    public DMatrixRMaj readDDRM(int numRows, int numCols) throws IOException {

        DMatrixRMaj A = new DMatrixRMaj(numRows,numCols);

        for( int i = 0; i < numRows; i++ ) {
            List<String> words = extractWords();
            if( words == null )
                throw new IOException("Too few rows found. expected "+numRows+" actual "+i);

            if( words.size() != numCols )
                throw new IOException("Unexpected number of words in column. Found "+words.size()+" expected "+numCols);
            for( int j = 0; j < numCols; j++ ) {
                A.set(i,j,Double.parseDouble(words.get(j)));
            }
        }

        return A;
    }

    /**
     * Reads in a {@link FMatrixRMaj} from the IO stream where the user specifies the matrix dimensions.
     *
     * @param numRows Number of rows in the matrix
     * @param numCols Number of columns in the matrix
     * @return FMatrixRMaj
     * @throws IOException
     */
    public FMatrixRMaj readFDRM(int numRows, int numCols) throws IOException {

        FMatrixRMaj A = new FMatrixRMaj(numRows,numCols);

        for( int i = 0; i < numRows; i++ ) {
            List<String> words = extractWords();
            if( words == null )
                throw new IOException("Too few rows found. expected "+numRows+" actual "+i);

            if( words.size() != numCols )
                throw new IOException("Unexpected number of words in column. Found "+words.size()+" expected "+numCols);
            for( int j = 0; j < numCols; j++ ) {
                A.set(i,j,Float.parseFloat(words.get(j)));
            }
        }

        return A;
    }


    /**
     * Reads in a {@link ZMatrixRMaj} from the IO stream where the user specifies the matrix dimensions.
     *
     * @param numRows Number of rows in the matrix
     * @param numCols Number of columns in the matrix
     * @return ZMatrixRMaj
     * @throws IOException
     */
    public ZMatrixRMaj readZDRM(int numRows, int numCols) throws IOException {

        ZMatrixRMaj A = new ZMatrixRMaj(numRows,numCols);

        int wordsCol = numCols*2;

        for( int i = 0; i < numRows; i++ ) {
            List<String> words = extractWords();
            if( words == null )
                throw new IOException("Too few rows found. expected "+numRows+" actual "+i);

            if( words.size() != wordsCol )
                throw new IOException("Unexpected number of words in column. Found "+words.size()+" expected "+wordsCol);
            for( int j = 0; j < wordsCol; j += 2 ) {

                double real = Double.parseDouble(words.get(j));
                double imaginary = Double.parseDouble(words.get(j+1));

                A.set(i, j, real, imaginary);
            }
        }

        return A;
    }

    /**
     * Reads in a {@link CMatrixRMaj} from the IO stream where the user specifies the matrix dimensions.
     *
     * @param numRows Number of rows in the matrix
     * @param numCols Number of columns in the matrix
     * @return CMatrixRMaj
     * @throws IOException
     */
    public CMatrixRMaj readCDRM(int numRows, int numCols) throws IOException {

        CMatrixRMaj A = new CMatrixRMaj(numRows,numCols);

        int wordsCol = numCols*2;

        for( int i = 0; i < numRows; i++ ) {
            List<String> words = extractWords();
            if( words == null )
                throw new IOException("Too few rows found. expected "+numRows+" actual "+i);

            if( words.size() != wordsCol )
                throw new IOException("Unexpected number of words in column. Found "+words.size()+" expected "+wordsCol);
            for( int j = 0; j < wordsCol; j += 2 ) {

                float real = Float.parseFloat(words.get(j));
                float imaginary = Float.parseFloat(words.get(j+1));

                A.set(i, j, real, imaginary);
            }
        }

        return A;
    }

    private FMatrixSparseTriplet readFSTR(int numRows, int numCols, int length) throws IOException {
        List<String> words;
        FMatrixSparseTriplet m = new FMatrixSparseTriplet(numRows,numCols,length);

        for (int i = 0; i < length; i++) {
            words = extractWords();

            if( words.size() != 3 )
                throw new IllegalArgumentException("Unexpected number of words on line "+getLineNumber());

            int row = Integer.parseInt(words.get(0));
            int col = Integer.parseInt(words.get(1));
            float value = Float.parseFloat(words.get(2));

            m.addItem(row,col,value);
        }

        return m;
    }

    private DMatrixSparseTriplet readDSTR(int numRows, int numCols, int length) throws IOException {
        List<String> words;
        DMatrixSparseTriplet m = new DMatrixSparseTriplet(numRows,numCols,length);

        for (int i = 0; i < length; i++) {
            words = extractWords();

            if( words.size() != 3 )
                throw new IllegalArgumentException("Unexpected number of words on line "+getLineNumber());

            int row = Integer.parseInt(words.get(0));
            int col = Integer.parseInt(words.get(1));
            double value = Double.parseDouble(words.get(2));

            m.addItem(row,col,value);
        }

        return m;
    }
}
