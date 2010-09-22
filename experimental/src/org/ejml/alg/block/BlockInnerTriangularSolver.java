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
 * Contains triangular solvers for inner blocks of a {@link org.ejml.data.BlockMatrix64F}.
 *
 * @author Peter Abeles
 */
// todo add solveL_transB()
public class BlockInnerTriangularSolver {

    /**
     * <p>
     * Performs an in-place solve operation on the provided submatrix.<br>
     * <br>
     * B = T<sup>-1</sup> B<br>
     * <br>
     * where T is a triangular matrix. T or B can be optionally transposed.
     * </p>
     *
     * The triangle must be a full inner block inside a {@link org.ejml.data.BlockMatrix64F}.
     *
     *
     * @param blockLength Size of the inner blocks in the block matrix.
     * @param upper If T is upper or lower triangular.
     * @param T An upper or lower triangular matrix that is contained in an inner block.
     * @param B A submatrix. Modified.
     * @param transT If T is transposed or not.
     * @param transB If B is transposed or not.
     */
    public static void solve( int blockLength ,
                              boolean upper , D1Submatrix64F T ,
                              D1Submatrix64F B ,
                              boolean transT , boolean transB )
    {
        int M = T.row1-T.row0;
        if( M > blockLength )
            throw new IllegalArgumentException("L can be at most the size of a block");

        int offsetT = T.row0*T.original.numCols+M*T.col0;

        double dataT[] = T.original.data;
        double dataB[] = B.original.data;

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
                    throw new IllegalArgumentException("Operation not yet supported");
                }
            }
        } else {
            if( M != B.row1-B.row0 )
                throw new IllegalArgumentException("L and B must have the same number of rows.");

            if( upper ) {
                if ( transT ) {
                    for( int i = B.col0; i < B.col1; i += blockLength ) {
                        int offsetB = B.row0*B.original.numCols + M*i;

                        int N = Math.min(B.col1 , i + blockLength ) - i;
                        solveTransU(dataT,dataB,M,N,offsetT,offsetB);
                    }
                } else {
                    for( int i = B.col0; i < B.col1; i += blockLength ) {
                        int offsetB = B.row0*B.original.numCols + M*i;

                        int N = Math.min(B.col1 , i + blockLength ) - i;
                        solveU(dataT,dataB,M,N,offsetT,offsetB);
                    }
                }
            } else {
                if ( transT ) {
                   throw new IllegalArgumentException("Operation not yet supported");
                } else {
                    for( int i = B.col0; i < B.col1; i += blockLength ) {
                        int offsetB = B.row0*B.original.numCols + M*i;

                        int N = Math.min(B.col1 , i + blockLength ) - i;
                        solveL(dataT,dataB,M,N,offsetT,offsetB);
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Solves for non-singular lower triangular matrices using forward substitution.
     * <br>
     * b = L<sup>-1</sup>b<br>
     * <br>
     * where b is a vector, L is an n by n matrix.<br>
     * </p>
     *
     * L is a m by m matrix
     * B is a m by n matrix
     *
     * @param L An n by n non-singular lower triangular matrix. Not modified.
     * @param b A vector of length n. Modified.
     * @param n The size of the matrices.
     * @param offsetL initial index in L where the matrix starts
     * @param offsetB initial index in B where the matrix starts
     */
    // TODO optimize
    public static void solveL( double L[] , double []b ,
                               int m , int n ,
                               int offsetL , int offsetB )
    {
        for( int j = 0; j < n; j++ ) {
            for( int i = 0; i < m; i++ ) {
                double sum = b[offsetB + i*n+j];
                for( int k=0; k<i; k++ ) {
                    sum -= L[offsetL + i*m+k]* b[offsetB + k*n+j];
                }
                b[offsetB + i*n+j] = sum / L[offsetL + i*m+i];
            }
        }
    }

    public static void solveU( double U[] , double []b ,
                               int m , int n ,
                               int offsetU , int offsetB )
    {
        for( int j = 0; j < n; j++ ) {
            for( int i = m-1; i >= 0; i-- ) {
                double sum = b[offsetB + i*n+j];
                for( int k=i+1; k<m; k++ ) {
                    sum -= U[offsetU + i*m+k]* b[offsetB + k*n+j];
                }
                b[offsetB + i*n+j] = sum / U[offsetU + i*m+i];
            }
        }
    }

    public static void solveTransU( double U[] , double []b ,
                                    int m , int n ,
                                    int offsetU , int offsetB )
    {
        for( int j = 0; j < n; j++ ) {
            for( int i = 0; i < m; i++ ) {
                double sum = b[offsetB + i*n+j];
                for( int k=0; k<i; k++ ) {
                    sum -= U[offsetU + k*m+i]* b[offsetB + k*n+j];
                }
                b[offsetB + i*n+j] = sum / U[offsetU + i*m+i];
            }
        }
    }
}
