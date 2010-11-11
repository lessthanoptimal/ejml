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
 * <p>
 * Contains triangular solvers for {@link org.ejml.data.BlockMatrix64F} block aligned sub-matrices.
 * </p>
 *
 * <p>
 * For a more detailed description of a similar algorithm see:
 * Page 30 in "Fundamentals of Matrix Computations" 2nd Ed. by David S. Watkins
 * or any description of a block triangular solver in any other computational linear algebra book
 * </p>
 *
 * @author Peter Abeles
 */
public class BlockTriangularSolver {

    /**
     * <p>
     * Performs an in-place solve operation on the provided block aligned sub-matrices.<br>
     * <br>
     * B = T<sup>-1</sup> B<br>
     * <br>
     * where T is a triangular matrix. T or B can be transposed.  T is a square matrix of arbitrary
     * size and B has the same number of rows as T and an arbitrary number of columns.
     * </p>
     *
     * @param blockLength Size of the inner blocks.
     * @param upper If T is upper or lower triangular.
     * @param T An upper or lower triangular matrix. Not modified.
     * @param B A matrix whose height is the same as T's width. Solution is written here. Modified.
     */
    public static void solve( final int blockLength ,
                              final boolean upper ,
                              final D1Submatrix64F T ,
                              final D1Submatrix64F B ,
                              final boolean transT ) {

        if( upper )
            throw new IllegalArgumentException("Upper triangular matrices not supported yet");

        solveL(blockLength,T,B,transT);
    }


    /**
     * <p>
     * Performs an in-place solve operation on the provided block aligned sub-matrices.<br>
     * <br>
     * B = T<sup>-1</sup> B<br>
     * <br>
     * where T is a triangular matrix. T or B can be transposed.  T must be a single complete inner block
     * and B is either a column block vector or row block vector.
     * </p>
     *
     * @param blockLength Size of the inner blocks in the block matrix.
     * @param upper If T is upper or lower triangular.
     * @param T An upper or lower triangular matrix that is contained in an inner block. Not modified.
     * @param B A block aligned row or column submatrix. Modified.
     * @param transT If T is transposed or not.
     * @param transB If B is transposed or not.
     */
    public static void solveBlock( final int blockLength ,
                                   final boolean upper , final D1Submatrix64F T ,
                                   final D1Submatrix64F B ,
                                   final boolean transT ,final boolean transB )
    {
        final int M = T.row1-T.row0;
        if( M > blockLength )
            throw new IllegalArgumentException("T can be at most the size of a block");

        int offsetT = T.row0*T.original.numCols+M*T.col0;

        final double dataT[] = T.original.data;
        final double dataB[] = B.original.data;

        if( transB ) {
            if( upper ) {
                if ( transT ) {
                    throw new IllegalArgumentException("Operation not yet supported");
                } else {
                    throw new IllegalArgumentException("Operation not yet supported");
                }
            } else {
                if ( transT ) {
                    throw new IllegalArgumentException("Operation not yet supported");
                } else {
                    for( int i = B.row0; i < B.row1; i += blockLength ) {
                        int N = Math.min(B.row1 , i + blockLength ) - i;

                        int offsetB = i*B.original.numCols + N*B.col0;

                        BlockInnerTriangularSolver.solveLTransB(dataT,dataB,M,N,offsetT,offsetB);
                    }
                }
            }
        } else {
            if( M != B.row1-B.row0 )
                throw new IllegalArgumentException("T and B must have the same number of rows.");

            if( upper ) {
                if ( transT ) {
                    for( int i = B.col0; i < B.col1; i += blockLength ) {
                        int offsetB = B.row0*B.original.numCols + M*i;

                        int N = Math.min(B.col1 , i + blockLength ) - i;
                        BlockInnerTriangularSolver.solveTransU(dataT,dataB,M,N,offsetT,offsetB);
                    }
                } else {
                    for( int i = B.col0; i < B.col1; i += blockLength ) {
                        int offsetB = B.row0*B.original.numCols + M*i;

                        int N = Math.min(B.col1 , i + blockLength ) - i;
                        BlockInnerTriangularSolver.solveU(dataT,dataB,M,N,offsetT,offsetB);
                    }
                }
            } else {
                if ( transT ) {
                    for( int i = B.col0; i < B.col1; i += blockLength ) {
                        int offsetB = B.row0*B.original.numCols + M*i;

                        int N = Math.min(B.col1 , i + blockLength ) - i;
                        BlockInnerTriangularSolver.solveTransL(dataT,dataB,M,N,offsetT,offsetB);
                    }
                } else {
                    for( int i = B.col0; i < B.col1; i += blockLength ) {
                        int offsetB = B.row0*B.original.numCols + M*i;

                        int N = Math.min(B.col1 , i + blockLength ) - i;
                        BlockInnerTriangularSolver.solveL(dataT,dataB,M,N,offsetT,offsetB);
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Solves lower triangular systems:<br>
     * <br>
     * B = L<sup>-1</sup> B<br>
     * <br>
     * </p>
     *
     * <p> Reverse or forward substitution is used depending upon L being transposed or not. </p>
     *
     * @param blockLength
     * @param L Lower triangular with dimensions m by m.  Not modified.
     * @param B A matrix with dimensions m by n.  Solution is written into here. Modified.
     * @param transL Is the triangular matrix transposed?
     */
    public static void solveL( final int blockLength ,
                               final D1Submatrix64F L,
                               final D1Submatrix64F B ,
                               boolean transL ) {

        D1Submatrix64F Y = new D1Submatrix64F(B.original);

        D1Submatrix64F Linner = new D1Submatrix64F(L.original);
        D1Submatrix64F Binner = new D1Submatrix64F(B.original);

        int lengthL = L.col1- L.col0;

        int startI,stepI;

        if( transL ) {
            startI = lengthL - lengthL % blockLength;
            stepI = -blockLength;
        } else {
            startI = 0;
            stepI = blockLength;
        }


        for( int i = startI; ; i += stepI ) {
            if( transL ) {
                if( i < 0 ) break;
            } else {
                if( i >= lengthL ) break;
            }

            // width and height of the inner T(i,i) block
            int widthT = Math.min(blockLength, lengthL-i);

            Linner.col0 = L.col0 + i;    Linner.col1 = Linner.col0 + widthT;
            Linner.row0 = L.row0 + i;    Linner.row1 = Linner.row0 + widthT;

            Binner.col0 = B.col0;       Binner.col1 = B.col1;
            Binner.row0 = B.row0 + i;   Binner.row1 = Binner.row0 + widthT;

            // solve the top row block
            // B(i,:) = T(i,i)^-1 Y(i,:)
            solveBlock(blockLength,false, Linner,Binner,transL,false);

            boolean updateY;
            if( transL ) {
                updateY = Linner.row0 > 0;
            } else {
                updateY = Linner.row1 < L.row1;
            }
            if( updateY ) {
                // Y[i,:] = Y[i,:] - sum j=1:i-1 { T[i,j] B[j,i] }
                // where i is the next block down
                // The summation is a block inner product
                if( transL ) {
                    Linner.col1 = Linner.col0;
                    Linner.col0 = Linner.col1 - blockLength;
                    Linner.row1 = L.row1;
                    //Tinner.col1 = Tinner.col1;

//                    Binner.row0 = Binner.row0;
                    Binner.row1 = B.row0;

                    Y.row0 = Binner.row0-blockLength;
                    Y.row1 = Binner.row0;
                } else {
                    Linner.row0 = Linner.row1;
                    Linner.row1 = Math.min(Linner.row0+blockLength, L.row1);
                    Linner.col0 = L.col0;
                    //Tinner.col1 = Tinner.col1;

                    Binner.row0 = B.row0;
                    //Binner.row1 = Binner.row1;

                    Y.row0 = Binner.row1;
                    Y.row1 = Math.min(Y.row0+blockLength,B.row1);
                }



                // step through each block column
                for( int k = B.col0; k < B.col1; k += blockLength ) {

                    Binner.col0 = k;
                    Binner.col1 = Math.min(k+blockLength,B.col1);

                    Y.col0 = Binner.col0;
                    Y.col1 = Binner.col1;

                    if( transL ) {
                        // Y = Y - T * B
                        BlockMultiplication.multMinusTransA(blockLength, Linner,Binner,Y);
                    } else {

                        // Y = Y - T * B
                        BlockMultiplication.multMinus(blockLength, Linner,Binner,Y);
                    }
                }
            }
        }
    }
}
