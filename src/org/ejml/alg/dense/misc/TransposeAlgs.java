/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.misc;

import org.ejml.data.DenseMatrix64F;


/**
 * Low level transpose algorithms.  No sanity checks are performed.
 *
 * @author Peter Abeles
 */
public class TransposeAlgs {

    /**
     * In-place transpose for a square matrix.  The most efficient algorithm but can
     * only be used on square matrices.
     *
     * @param mat The matrix that is transposed in-place.  Modified.
     */
    public static void square( DenseMatrix64F mat )
    {
        final double data[] = mat.data;

        for( int i = 0; i < mat.numRows; i++ ) {
            int index = i*mat.numCols+i+1;
            int indexOther = (i+1)*mat.numCols + i;
            for( int j = i+1; j < mat.numCols; j++ , indexOther += mat.numCols) {
                double val = data[index];
                data[index] = data[indexOther];
                data[indexOther] = val;
                index++;
            }
        }
    }

    /**
     * Performs a transpose across block sub-matrices.  Reduces
     * the number of cache misses on larger matrices.
     *
     * @param A Original matrix.  Not modified.
     * @param A_tran Transposed matrix.  Modified.
     * @param blockLength Length of a block.
     */
    public static void block( DenseMatrix64F A , DenseMatrix64F A_tran ,
                              final int blockLength )
    {
        for( int i = 0; i < A.numRows; i += blockLength ) {
            int blockHeight = Math.min( blockLength , A.numRows - i);

            for( int j = 0; j < A.numCols; j += blockLength ) {
                int blockWidth = Math.min( blockLength , A.numCols - j);

                int indexSrc = i*A.numCols + j;
                int indexDst = j*A_tran.numCols + i;

                for( int l = 0; l < blockWidth; l++ , indexSrc++ ) {
                    int rowSrc = indexSrc;
                    int rowDst = indexDst;
                    int end = rowDst + blockHeight;
//                    for( int k = 0; k < blockHeight; k++ , rowSrc += A.numCols ) {
                    for( ; rowDst < end; rowSrc += A.numCols ) {
                        // faster to write in sequence than to read in sequence
                        A_tran.data[ rowDst++ ] = A.data[rowSrc];
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
    public static void standard( DenseMatrix64F A, DenseMatrix64F A_tran)
    {
        final double rdata[] = A_tran.data;
        final double data[] = A.data;

        int index = 0;
        for( int i = 0; i < A_tran.numRows; i++ ) {
            int index2 = i;

            int end = index + A_tran.numCols;
            while( index < end ) {
                rdata[index++] = data[index2];
                index2 += A.numCols;
            }
        }
    }
}
