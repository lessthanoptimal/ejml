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

package org.ejml.alg.block;

import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BlockMatrixOps {

    /**
     * Converts a row major matrix into a row major block matrix.
     *
     * @param src Original DenseMatrix64F.  Not modified.
     * @param dst Equivalent BlockMatrix64F. Modified.
     */
    public static void convert( DenseMatrix64F src , BlockMatrix64F dst )
    {
        if( src.numRows != dst.numRows || src.numCols != dst.numCols )
            throw new IllegalArgumentException("Must be the same size.");

        for( int i = 0; i < dst.numRows; i += dst.blockLength ) {
            int blockHeight = Math.min( dst.blockLength , dst.numRows - i);

            for( int j = 0; j < dst.numCols; j += dst.blockLength ) {
                int blockWidth = Math.min( dst.blockLength , dst.numCols - j);

                int indexDst = i*dst.numCols + blockHeight*j;
                int indexSrcRow = i*dst.numCols + j;

                for( int k = 0; k < blockHeight; k++ ) {
                    int indexSrc = indexSrcRow;
                    int end = indexSrc + blockWidth;
                    for( ;indexSrc != end; ) {
                        dst.data[indexDst++] = src.data[indexSrc++];
                    }
                    indexSrcRow += dst.numCols;
                }
            }
        }
    }

    /**
     * Converts a row major block matrix into a row major matrix.
     *
     * @param src Original BlockMatrix64F..  Not modified.
     * @param dst Equivalent DenseMatrix64F.  Modified.
     */
    public static void convert( BlockMatrix64F src , DenseMatrix64F dst )
    {
        if( dst.numRows != src.numRows || dst.numCols != src.numCols )
            throw new IllegalArgumentException("Must be the same size.");

        for( int i = 0; i < src.numRows; i += src.blockLength ) {
            int blockHeight = Math.min( src.blockLength , src.numRows - i);

            for( int j = 0; j < src.numCols; j += src.blockLength ) {
                int blockWidth = Math.min( src.blockLength , src.numCols - j);

                int indexSrc = i*src.numCols + blockHeight*j;
                int indexDstRow = i*dst.numCols + j;

                for( int k = 0; k < blockHeight; k++ ) {
                    int indexDst = indexDstRow;
                    int end = indexSrc + blockWidth;
                    for( ;indexSrc != end; ) {
                        dst.data[indexDst++] = src.data[indexSrc++];
                    }
                    indexDstRow += dst.numCols;
                }
            }
        }
    }

    /**
     * Converts the transpose of a row major matrix into a row major block matrix.
     *
     * @param src Original DenseMatrix64F.  Not modified.
     * @param dst Equivalent BlockMatrix64F. Modified.
     */
    public static void convertTranSrc( DenseMatrix64F src , BlockMatrix64F dst )
    {
        if( src.numRows != dst.numCols || src.numCols != dst.numRows )
            throw new IllegalArgumentException("Incompatible matrix shapes.");

        for( int i = 0; i < dst.numRows; i += dst.blockLength ) {
            int blockHeight = Math.min( dst.blockLength , dst.numRows - i);

            for( int j = 0; j < dst.numCols; j += dst.blockLength ) {
                int blockWidth = Math.min( dst.blockLength , dst.numCols - j);

                int indexDst = i*dst.numCols + blockHeight*j;
                int indexSrc = j*src.numCols + i;

                for( int l = 0; l < blockWidth; l++ ) {
                    int rowSrc = indexSrc + l*src.numCols;
                    int rowDst = indexDst + l;
                    for( int k = 0; k < blockHeight; k++ , rowDst += blockWidth ) {
                        dst.data[ rowDst ] = src.data[rowSrc++];
                    }
                }
            }
        }
    }

    public static void mult( BlockMatrix64F A , BlockMatrix64F B , BlockMatrix64F C )
    {

    }

    /**
     * Transposes a block matrix.
     *
     * @param A Original matrix.  Not modified.
     * @param A_tran Transposed matrix.  Modified.
     */
    public static void transpose( BlockMatrix64F A , BlockMatrix64F A_tran )
    {
        if( A.numRows != A_tran.numCols || A.numCols != A_tran.numRows )
            throw new IllegalArgumentException("Incompatible dimensions.");
        if( A.blockLength != A_tran.blockLength )
            throw new IllegalArgumentException("Incompatible block size.");

        for( int i = 0; i < A.numRows; i += A.blockLength ) {
            int blockHeight = Math.min( A.blockLength , A.numRows - i);

            for( int j = 0; j < A.numCols; j += A.blockLength ) {
                int blockWidth = Math.min( A.blockLength , A.numCols - j);

                int indexA = i*A.numCols + blockHeight*j;
                int indexC = j*A_tran.numCols + blockWidth*i;

                transposeBlock( A , A_tran , indexA , indexC , blockWidth , blockHeight );
            }
        }
    }

    /**
     * Transposes an individual block inside a block matrix.
     */
    private static void transposeBlock( BlockMatrix64F A , BlockMatrix64F A_tran,
                                        int indexA , int indexC ,
                                        int width , int height )
    {
        for( int i = 0; i < height; i++ ) {
            int rowIndexC = indexC + i;
            int rowIndexA = indexA + width*i;
            int end = rowIndexA + width;
            for( ; rowIndexA < end; rowIndexC += height, rowIndexA++ ) {
                A_tran.data[ rowIndexC ] = A.data[ rowIndexA ];
            }
        }
    }

    public static BlockMatrix64F createRandom( int numRows , int numCols ,
                                               double min , double max , Random rand )
    {
        BlockMatrix64F ret = new BlockMatrix64F(numRows,numCols);

        RandomMatrices.setRandom(ret,min,max,rand);

        return ret;
    }

}
