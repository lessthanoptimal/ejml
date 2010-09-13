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

// todo move more complex testing from ops to this class's test
// todo make the checks in ops test its bounds checking and basic functionality

/**
 * Contains matrix multiplication operations on  {@link org.ejml.data.BlockMatrix64F}. To
 * reduce code complexity all operations take a submatrix as input.
 *
 * @author Peter Abeles
 */
public class BlockMatrixMultiplication {

    /**
     * <p>
     * Performs a matrix multiplication on {@link org.ejml.data.BlockMatrix64F} submatrices.<br>
     * <br>
     * c = a * b <br>
     * <br>
     * </p>
     *
     * <p>
     * It is assumed that all submatrices start at the beginning of a block.
     * </p>
     *
     * @param blockLength Size of the blocks in the submatrix.
     * @param A A submatrix.  Not modified.
     * @param B A submatrix.  Not modified.
     * @param C Result of the operation.  Modified,
     */
    public static void mult( int blockLength ,
                             D1Submatrix64F A , D1Submatrix64F B ,
                             D1Submatrix64F C )
    {
        for( int i = A.row0; i < A.row1; i += blockLength ) {
            int heightA = Math.min( blockLength , A.row1 - i );

            for( int j = B.col0; j < B.col1; j += blockLength ) {
                int widthB = Math.min( blockLength , B.col1 - j );

                int indexC = i*C.original.numCols + j*heightA;

                for( int k = A.col0; k < A.col1; k += blockLength ) {
                    int widthA = Math.min( blockLength , A.col1 - k );

                    int indexA = i*A.original.numCols + k*heightA;
                    int indexB = k*B.original.numCols + j*widthA;

                    if( k == 0 )
                        multBlockSet(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthB);
                    else
                        multBlockAdd(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthB);
                }
            }
        }
    }

    /**
     * Performs a matrix multiplication between inner block matrices.
     *
     * (m , o) = (m , n) * (n , o)
     */
    protected static void multBlockSet( double[] dataA, double []dataB, double []dataC,
                                        int indexA, int indexB, int indexC,
                                        final int m, final int n, final int o) {
        for( int i = 0; i < m; i++ ) {
            for( int j = 0; j < o; j++ , indexC++ ) {
                int indexBB = indexB + j;
                int indexAA = indexA;

                double val = 0;

                int end = indexA + n;

                for( ; indexAA != end; indexAA++) {
                    val += dataA[ indexAA ] * dataB[indexBB];
                    indexBB += o;
                }

                dataC[ indexC ] = val;
            }

            indexA += n;
        }
    }

    /**
     * Performs a matrix multiplication between inner block matrices.
     *
     * (m , o) += (m , n) * (n , o)
     */
    protected static void multBlockAdd( double[] dataA, double []dataB, double []dataC,
                                      int indexA, int indexB, int indexC,
                                      final int m, final int n, final int o) {
//        for( int i = 0; i < m; i++ ) {
//            for( int j = 0; j < o; j++ ) {
//                double val = 0;
//
//                for( int k = 0; k < n; k++ ) {
//                    val += A.data[i*n + k + indexA] * B.data[k*o + j + indexB];
//                }
//
//                C.data[ i*o + j + indexC ] += val;
//            }
//        }

        for( int i = 0; i < m; i++ ) {
            for( int j = 0; j < o; j++ , indexC++ ) {
                int indexBB = indexB + j;
                int indexAA = indexA;

                double val = 0;

                int end = indexA + n;

                for( ; indexAA != end; indexAA++) {
                    val += dataA[ indexAA ] * dataB[indexBB];
                    indexBB += o;
                }

                dataC[ indexC ] += val;
            }

            indexA += n;
        }
    }

    /**
     * <p>
     * Performs a matrix multiplication with a transpose on {@link org.ejml.data.BlockMatrix64F} submatrices.<br>
     * <br>
     * c = a<sup>T</sup> * b <br>
     * <br>
     * </p>
     *
     * <p>
     * It is assumed that all submatrices start at the beginning of a block.
     * </p>
     *
     * @param blockLength Size of the blocks in the submatrix.
     * @param A A submatrix.  Not modified.
     * @param B A submatrix.  Not modified.
     * @param C Result of the operation.  Modified,
     */
    public static void multTransA( int blockLength ,
                                   D1Submatrix64F A , D1Submatrix64F B ,
                                   D1Submatrix64F C )
    {

    }

    /**
     * <p>
     * Performs a matrix multiplication with a transpose on {@link org.ejml.data.BlockMatrix64F} submatrices.<br>
     * <br>
     * c = a * b <sup>T</sup> <br>
     * <br>
     * </p>
     *
     * <p>
     * It is assumed that all submatrices start at the beginning of a block.
     * </p>
     *
     * @param blockSize Size of the blocks in the submatrix.
     * @param A A submatrix.  Not modified.
     * @param B A submatrix.  Not modified.
     * @param C Result of the operation.  Modified,
     */
    public static void multTransB( int blockSize ,
                                   D1Submatrix64F A , D1Submatrix64F B ,
                                   D1Submatrix64F C )
    {

    }
}
