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
public class BlockInnerTriangularSolver {

    /**
     * <p>
     * Inverts a square lower triangular matrix:  L = L<sup>-1</sup>
     * </p>
     *
     * @param L Lower triangular matrix.  Overwritten with the inverse. Modified.
     * @param m The number of rows and columns.
     * @param offsetL which index does the matrix start at.
     */
    public static void invertLower( double L[] ,
                                    int m ,
                                    int offsetL )
    {
        for( int i = 0; i < m; i++ ) {
            double L_ii = L[ offsetL + i*m + i ];
            for( int j = 0; j < i; j++ ) {
                double val = 0;
                for( int k = 0; k < i; k++ ) {
                    val += L[offsetL + i*m + k] * L[ offsetL + k*m + j ];
                }
                L[ offsetL + i*m + j ] = -val / L_ii;
            }
            L[ offsetL + i*m + i ] =  1.0 / L_ii;
        }
    }

    /**
     * <p>
     * Solves for non-singular lower triangular matrices using forward substitution.
     * <br>
     * B = L<sup>-1</sup>B<br>
     * <br>
     * where B is a (m by n) matrix, L is a triangular (m by m) matrix.
     * </p>
     *
     * @param L An m by m non-singular lower triangular matrix. Not modified.
     * @param b An m by n matrix. Modified.
     * @param m size of the L matrix
     * @param n number of columns in the B matrix.
     * @param offsetL initial index in L where the matrix starts
     * @param offsetB initial index in B where the matrix starts
     */
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

    /**
     * <p>
     * Solves for non-singular transposed lower triangular matrices using backwards substitution:
     * <br>
     * B = L<sup>-T</sup>B<br>
     * <br>
     * where B is a (m by n) matrix, L is a triangular (m by m) matrix.
     * </p>
     *
     * @param L An m by m non-singular lower triangular matrix. Not modified.
     * @param b An m by n matrix. Modified.
     * @param m size of the L matrix
     * @param n number of columns in the B matrix.
     * @param offsetL initial index in L where the matrix starts
     * @param offsetB initial index in B where the matrix starts
     */
    public static void solveTransL( double L[] , double []b ,
                                    int m , int n ,
                                    int offsetL , int offsetB )
    {
        for( int j = 0; j < n; j++ ) {
            for( int i = m-1; i >= 0; i-- ) {
                double sum = b[offsetB + i*n+j];
                for( int k=i+1; k<m; k++ ) {
                    sum -= L[offsetL + k*m+i]* b[offsetB + k*n+j];
                }
                b[offsetB + i*n+j] = sum / L[offsetL + i*m+i];
            }
        }
    }

     /**
     * <p>
     * Solves for non-singular lower triangular matrices using forward substitution.
     * <br>
     * B<sup>T</sup> = L<sup>-1</sup>B<sup>T</sup><br>
     * <br>
     * where B is a (n by m) matrix, L is a triangular (m by m) matrix.
     * </p>
     *
     * @param L An m by m non-singular lower triangular matrix. Not modified.
     * @param b An n by m matrix. Modified.
     * @param m size of the L matrix
     * @param n number of columns in the B matrix.
     * @param offsetL initial index in L where the matrix starts
     * @param offsetB initial index in B where the matrix starts
     */
    public static void solveLTransB( double L[] , double []b ,
                                     int m , int n ,
                                     int offsetL , int offsetB )
    {
//        for( int j = 0; j < n; j++ ) {
//            for( int i = 0; i < m; i++ ) {
//                double sum = b[offsetB + j*m+i];
//                for( int k=0; k<i; k++ ) {
//                    sum -= L[offsetL + i*m+k]* b[offsetB + j*m+k];
//                }
//                b[offsetB + j*m+i] = sum / L[offsetL + i*m+i];
//            }
//        }
        for( int j = 0; j < n; j++ ) {
            for( int i = 0; i < m; i++ ) {
                double sum = b[offsetB + j*m+i];
                int l = offsetL+i*m;
                int bb = offsetB +j*m;
                int endL = l+i;
                while( l != endL ) {
//                for( int k=0; k<i; k++ ) {
                    sum -= L[l++]* b[bb++];
                }
                b[offsetB + j*m+i] = sum / L[offsetL + i*m+i];
            }
        }
    }

    /**
     * <p>
     * Solves for non-singular upper triangular matrices using backwards substitution.
     * <br>
     * B = U<sup>-1</sup>B<br>
     * <br>
     * where B (m by n) is a matrix, U is a (m by m ) triangular matrix.<br>
     * </p>
     *
     * @param U An m by m non-singular upper triangular matrix. Not modified.
     * @param b An m by n matrix. Modified.
     * @param m size of the L matrix
     * @paramUn number of columns in the B matrix.
     * @param offsetU initial index in L where the matrix starts
     * @param offsetB initial index in B where the matrix starts
     */
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

    /**
     * <p>
     * Solves for non-singular upper triangular matrices using forward substitution.
     * <br>
     * B = U<sup>-T</sup>B<br>
     * <br>
     * where B (m by n) is a matrix, U is a (m by m ) triangular matrix.<br>
     * </p>
     *
     * @param U An m by m non-singular upper triangular matrix. Not modified.
     * @param b An m by n matrix. Modified.
     * @param m size of the L matrix
     * @paramUn number of columns in the B matrix.
     * @param offsetU initial index in L where the matrix starts
     * @param offsetB initial index in B where the matrix starts
     */
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
