/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.misc;

import org.ejml.data.RowD1Matrix64F;


/**
 * Low level transpose algorithms.  No sanity checks are performed.    Take a look at BenchmarkTranspose to
 * see which one is faster on your computer.
 *
 * @author Peter Abeles
 */
public class TransposeAlgs {

    /**
     * In-place transpose for a square matrix.  On most architectures it is faster than the standard transpose
     * algorithm, but on most modern computers it's slower than block transpose.
     *
     * @param mat The matrix that is transposed in-place.  Modified.
     */
    public static void square( RowD1Matrix64F mat )
    {
        int index = 1;
        int indexEnd = mat.numCols;
        for( int i = 0; i < mat.numRows;
             i++ , index += i+1 , indexEnd += mat.numCols ) {
            int indexOther = (i+1)*mat.numCols + i;
            for( ; index < indexEnd; index++, indexOther += mat.numCols) {
                double val = mat.data[ index ];
                mat.data[ index ] = mat.data[ indexOther ];
                mat.data[indexOther] = val;
            }
        }
    }

    /**
     * Performs a transpose across block sub-matrices.  Reduces
     * the number of cache misses on larger matrices.
     *
     * *NOTE* If this is beneficial is highly dependent on the computer it is run on. e.g:
     * - Q6600 Almost twice as fast as standard.
     * - Pentium-M Same speed and some times a bit slower than standard.
     *
     * @param A Original matrix.  Not modified.
     * @param A_tran Transposed matrix.  Modified.
     * @param blockLength Length of a block.
     */
    public static void block( RowD1Matrix64F A , RowD1Matrix64F A_tran ,
                              final int blockLength )
    {
        for( int i = 0; i < A.numRows; i += blockLength ) {
            int blockHeight = Math.min( blockLength , A.numRows - i);

            int indexSrc = i*A.numCols;
            int indexDst = i;

            for( int j = 0; j < A.numCols; j += blockLength ) {
                int blockWidth = Math.min( blockLength , A.numCols - j);

//                int indexSrc = i*A.numCols + j;
//                int indexDst = j*A_tran.numCols + i;

                int indexSrcEnd = indexSrc + blockWidth;
//                for( int l = 0; l < blockWidth; l++ , indexSrc++ ) {
                for( ; indexSrc < indexSrcEnd;  indexSrc++ ) {
                    int rowSrc = indexSrc;
                    int rowDst = indexDst;
                    int end = rowDst + blockHeight;
//                    for( int k = 0; k < blockHeight; k++ , rowSrc += A.numCols ) {
                    for( ; rowDst < end; rowSrc += A.numCols ) {
                        // faster to write in sequence than to read in sequence
                        A_tran.data[ rowDst++ ] = A.data[ rowSrc ];
                    }
                    indexDst += A_tran.numCols;
                }
            }
        }
    }

    /**
     * A straight forward transpose.  Good for small non-square matrices.
     *
     * @param A Original matrix.  Not modified.
     * @param A_tran Transposed matrix.  Modified.
     */
    public static void standard( RowD1Matrix64F A, RowD1Matrix64F A_tran)
    {
        int index = 0;
        for( int i = 0; i < A_tran.numRows; i++ ) {
            int index2 = i;

            int end = index + A_tran.numCols;
            while( index < end ) {
                A_tran.data[index++ ] = A.data[ index2 ];
                index2 += A.numCols;
            }
        }
    }
}
