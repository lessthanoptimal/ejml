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

import org.ejml.data.D1Submatrix64F;


/**
 * Performs rank-n update operations on the inner blocks of a {@link org.ejml.data.BlockMatrix64F}
 *
 * @author Peter Abeles
 */
public class BlockInnerRankUpdate {

    /**
     *
     * A = A + &alpha; B <sup>T</sup>B
     *
     * @param blockLength
     * @param alpha
     * @param A
     * @param B
     */
    public static void rankNUpdate( int blockLength , double alpha ,
                                    D1Submatrix64F A , D1Submatrix64F B )
    {

        int heightB = B.row1-B.row0;
        if( heightB > blockLength )
            throw new IllegalArgumentException("Height of B cannot be greater than the block length");

        int N = B.col1-B.col0;

        if( A.col1-A.col0 != N )
            throw new IllegalArgumentException("A does not have the expected number of columns based on B's width");
        if( A.row1-A.row0 != N )
            throw new IllegalArgumentException("A does not have the expected number of rows based on B's width");


        for( int i = B.col0; i < B.col1; i += blockLength ) {

            int indexB_i = B.row0*B.original.numCols + i*heightB;
            int widthB_i = Math.min(blockLength,B.col1-i);

            int rowA = i-B.col0+A.row0;
            int heightA = Math.min( blockLength , A.row1 - rowA);

            for( int j = B.col0; j < B.col1; j += blockLength ) {

                int widthB_j = Math.min(blockLength,B.col1-j);

                int indexA = rowA * A.original.numCols + (j-B.col0+A.col0)*heightA;
                int indexB_j = B.row0*B.original.numCols + j*heightB;


                BlockInnerMultiplication.multTransABlockAdd(alpha,
                        B.original.data,B.original.data,A.original.data,
                        indexB_i,indexB_j,indexA,heightB,widthB_i,widthB_j);
            }
        }
    }

    /**
     * Rank N update function for a symmetric inner submatrix and only operates on the upper
     * triangular portion of the submatrix.
     */
    public static void symmRankNUpdate_U( int blockLength ,
                                          D1Submatrix64F A , D1Submatrix64F B )
    {

        int heightB = B.row1-B.row0;
        if( heightB > blockLength )
            throw new IllegalArgumentException("Height of B cannot be greater than the block length");

        int N = B.col1-B.col0;

        if( A.col1-A.col0 != N )
            throw new IllegalArgumentException("A does not have the expected number of columns based on B's width");
        if( A.row1-A.row0 != N )
            throw new IllegalArgumentException("A does not have the expected number of rows based on B's width");


        for( int i = B.col0; i < B.col1; i += blockLength ) {

            int indexB_i = B.row0*B.original.numCols + i*heightB;
            int widthB_i = Math.min(blockLength,B.col1-i);

            int rowA = i-B.col0+A.row0;
            int heightA = Math.min( blockLength , A.row1 - rowA);

            for( int j = i; j < B.col1; j += blockLength ) {
                // todo j == i do only a triangle block

                int widthB_j = Math.min(blockLength,B.col1-j);

                int indexA = rowA * A.original.numCols + (j-B.col0+A.col0)*heightA;
                int indexB_j = B.row0*B.original.numCols + j*heightB;

                multTransABlockMinus( B.original.data,A.original.data,
                        indexB_i,indexB_j,indexA,heightB,widthB_i,widthB_j);
            }
        }
    }

    /**
     * <p>
     * Performs the following operation on a block:<br>
     * <br>
     * c = c - a<sup>T</sup>a<br>
     * </p>
     */
    protected static void multTransABlockMinus( double[] dataA, double []dataC,
                                                int indexA, int indexB, int indexC,
                                                final int heightA, final int widthA, final int widthC ) {
//        for( int i = 0; i < widthA; i++ ) {
//            for( int k = 0; k < heightA; k++ ) {
//
//                double valA = dataA[k*widthA + i + indexA];
//                for( int j = 0; j < widthC; j++ ) {
//                    dataC[ i*widthC + j + indexC ] -= valA * dataA[k*widthC + j + indexB];
//                }
//            }
//        }

        for( int k = 0; k < heightA; k++ ) {
            int a = k*widthA + indexA;
            int c = indexC;
            int endA = a + widthA;

            int rowB = k*widthC + indexB;
            int endB = rowB + widthC;
            while( a != endA ) {
                double valA = dataA[a++];

                int b = rowB;
                while( b != endB ) {
                    dataC[ c++ ] -= valA * dataA[b++];
                }
            }
        }
    }
}
