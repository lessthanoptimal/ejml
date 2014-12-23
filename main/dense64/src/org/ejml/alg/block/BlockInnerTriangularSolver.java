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

package org.ejml.alg.block;

/**
 * <p>
 * Contains triangular solvers for inner blocks of a {@link org.ejml.data.BlockMatrix64F}.
 * </p>
 *
 * <p>
 * Algorithm for lower triangular inverse:<br>
 *
 * <pre>
 * for i=1:m
 *     for j=1:i-1
 *         val = 0
 *         for k=j:i-1
 *             val = val - L(i,k) * X(k,j)
 *         end
 *         x(i,j) = val / L(i,i)
 *     end
 *     x(i,i) = 1 / L(i,i)
 * end
 * </pre> 
 * </p>
 *
 * @author Peter Abeles
 */
public class BlockInnerTriangularSolver {

    /**
     * <p>
     * Inverts a square lower triangular matrix:  L = L<sup>-1</sup>
     * </p>
     *
     * @param L Lower triangular matrix being inverted. Not modified.
     * @oaran K_inv Where the inverse is stored.  Can be the same as L.  Modified.
     * @param m The number of rows and columns.
     * @param offsetL which index does the L matrix start at.
     * @param offsetL_inv which index does the L_inv matrix start at.
     *
     */
    public static void invertLower( double L[] ,
                                    double L_inv[] ,
                                    int m ,
                                    int offsetL ,
                                    int offsetL_inv )
    {
        for( int i = 0; i < m; i++ ) {
            double L_ii = L[ offsetL + i*m + i ];
            for( int j = 0; j < i; j++ ) {
                double val = 0;
                for( int k = j; k < i; k++ ) {
                    val += L[offsetL + i*m + k] * L_inv[ offsetL_inv + k*m + j ];
                }
                L_inv[ offsetL_inv + i*m + j ] = -val / L_ii;
            }
            L_inv[ offsetL_inv + i*m + i ] =  1.0 / L_ii;
        }
    }

    /**
     * <p>
     * Inverts a square lower triangular matrix:  L = L<sup>-1</sup>
     * </p>
     *
     * @param L Lower triangular matrix being inverted. Over written with inverted matrix.  Modified.
     * @param m The number of rows and columns.
     * @param offsetL which index does the L matrix start at.
     *
     */
    public static void invertLower( double L[] ,
                                    int m ,
                                    int offsetL )
    {
        for( int i = 0; i < m; i++ ) {
            double L_ii = L[ offsetL + i*m + i ];
            for( int j = 0; j < i; j++ ) {
                double val = 0;
                for( int k = j; k < i; k++ ) {
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
     * where B is a (m by n) matrix, L is a lower triangular (m by m) matrix.
     * </p>
     *
     * @param L An m by m non-singular lower triangular matrix. Not modified.
     * @param b An m by n matrix. Modified.
     * @param m size of the L matrix
     * @param n number of columns in the B matrix.
     * @param strideL number of elements that need to be added to go to the next row in L
     * @param offsetL initial index in L where the matrix starts
     * @param offsetB initial index in B where the matrix starts
     */
    public static void solveL( double L[] , double []b ,
                               int m , int n ,
                               int strideL , int offsetL , int offsetB )
    {
        for( int j = 0; j < n; j++ ) {
            for( int i = 0; i < m; i++ ) {
                double sum = b[offsetB + i*n+j];
                for( int k=0; k<i; k++ ) {
                    sum -= L[offsetL + i*strideL+k]* b[offsetB + k*n+j];
                }
                b[offsetB + i*n+j] = sum / L[offsetL + i*strideL+i];
            }
        }
    }

    /**
     * <p>
     * Solves for non-singular transposed lower triangular matrices using backwards substitution:
     * <br>
     * B = L<sup>-T</sup>B<br>
     * <br>
     * where B is a (m by n) matrix, L is a lower triangular (m by m) matrix.
     * </p>
     *
     * @param L An m by m non-singular lower triangular matrix. Not modified.
     * @param b An m by n matrix. Modified.
     * @param m size of the L matrix
     * @param n number of columns in the B matrix.
     * @param strideL number of elements that need to be added to go to the next row in L
     * @param offsetL initial index in L where the matrix starts
     * @param offsetB initial index in B where the matrix starts
     */
    public static void solveTransL( double L[] , double []b ,
                                    int m , int n ,
                                    int strideL , int offsetL , int offsetB )
    {
        for( int j = 0; j < n; j++ ) {
            for( int i = m-1; i >= 0; i-- ) {
                double sum = b[offsetB + i*n+j];
                for( int k=i+1; k<m; k++ ) {
                    sum -= L[offsetL + k*strideL+i]* b[offsetB + k*n+j];
                }
                b[offsetB + i*n+j] = sum / L[offsetL + i*strideL+i];
            }
        }
    }

     /**
     * <p>
     * Solves for non-singular lower triangular matrices using forward substitution.
     * <br>
     * B<sup>T</sup> = L<sup>-1</sup>B<sup>T</sup><br>
     * <br>
     * where B is a (n by m) matrix, L is a lower triangular (m by m) matrix.
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
                                     int strideL , int offsetL , int offsetB )
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
                int l = offsetL+i*strideL;
                int bb = offsetB +j*m;
                int endL = l+i;
                while( l != endL ) {
//                for( int k=0; k<i; k++ ) {
                    sum -= L[l++]* b[bb++];
                }
                b[offsetB + j*m+i] = sum / L[offsetL + i*strideL+i];
            }
        }
    }

    /**
     * <p>
     * Solves for non-singular upper triangular matrices using backwards substitution.
     * <br>
     * B = U<sup>-1</sup>B<br>
     * <br>
     * where B (m by n) is a matrix, U is a (m by m ) upper triangular matrix.<br>
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
                               int strideU , int offsetU , int offsetB )
    {
        for( int j = 0; j < n; j++ ) {
            for( int i = m-1; i >= 0; i-- ) {
                double sum = b[offsetB + i*n+j];
                for( int k=i+1; k<m; k++ ) {
                    sum -= U[offsetU + i*strideU+k]* b[offsetB + k*n+j];
                }
                b[offsetB + i*n+j] = sum / U[offsetU + i*strideU+i];
            }
        }
    }

    /**
     * <p>
     * Solves for non-singular upper triangular matrices using forward substitution.
     * <br>
     * B = U<sup>-T</sup>B<br>
     * <br>
     * where B (m by n) is a matrix, U is a (m by m ) upper triangular matrix.<br>
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
                                    int strideU , int offsetU , int offsetB )
    {
        for( int j = 0; j < n; j++ ) {
            for( int i = 0; i < m; i++ ) {
                double sum = b[offsetB + i*n+j];
                for( int k=0; k<i; k++ ) {
                    sum -= U[offsetU + k*strideU+i]* b[offsetB + k*n+j];
                }
                b[offsetB + i*n+j] = sum / U[offsetU + i*strideU+i];
            }
        }
    }
}
