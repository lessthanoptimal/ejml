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

package org.ejml.alg.dense.decomposition;

/**
 * <p>
 * This contains algorithms for solving systems of equations where T is a
 * non-singular triangular matrix:<br>
 * <br>
 * T*x = b<br>
 * <br>
 * where x and b are vectors, and T is an n by n matrix. T can either be a lower or upper triangular matrix.<br>
 * </p>
 * <p>
 * These functions are designed for use inside of other algorithms.  To use them directly
 * is dangerous since no sanity checks are performed.
 * </p>
 *
 * @author Peter Abeles
 */
public class TriangularSolver {

    /**
     * <p>
     * Inverts a square lower triangular matrix:  L = L<sup>-1</sup>
     * </p>
     *
     *
     * @param L
     * @param m
     */
    public static void invertLower( double L[] , int m ) {
        for( int i = 0; i < m; i++ ) {
            double L_ii = L[ i*m + i ];
            for( int j = 0; j < i; j++ ) {
                double val = 0;
                for( int k = j; k < i; k++ ) {
                    val += L[ i*m + k] * L[ k*m + j ];
                }
                L[ i*m + j ] = -val / L_ii;
            }
            L[ i*m + i ] =  1.0 / L_ii;
        }
    }

    public static void invertLower( double L[] , double L_inv[] , int m ) {
        for( int i = 0; i < m; i++ ) {
            double L_ii = L[ i*m + i ];
            for( int j = 0; j < i; j++ ) {
                double val = 0;
                for( int k = j; k < i; k++ ) {
                    val -= L[ i*m + k] * L_inv[ k*m + j ];
                }
                L_inv[ i*m + j ] = val / L_ii;
            }
            L_inv[ i*m + i ] =  1.0 / L_ii;
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
     * @param L An n by n non-singular lower triangular matrix. Not modified.
     * @param b A vector of length n. Modified.
     * @param n The size of the matrices.
     */
    public static void solveL( double L[] , double []b , int n )
    {
//        for( int i = 0; i < n; i++ ) {
//            double sum = b[i];
//            for( int k=0; k<i; k++ ) {
//                sum -= L[i*n+k]* b[k];
//            }
//            b[i] = sum / L[i*n+i];
//        }
        for( int i = 0; i < n; i++ ) {
            double sum = b[i];
            int indexL = i*n;
            for( int k=0; k<i; k++ ) {
                sum -= L[indexL++]* b[k];
            }
            b[i] = sum / L[indexL];
        }
    }

    /**
     *
     * L is a m by m matrix
     * B is a m by n matrix
     *
     * @param L
     * @param b
     * @param m
     * @param n
     */
    public static void solveL( double L[] , double []b , int m , int n )
    {
        for( int j = 0; j < n; j++ ) {
            for( int i = 0; i < m; i++ ) {
                double sum = b[i*n+j];
                for( int k=0; k<i; k++ ) {
                    sum -= L[i*m+k]* b[k*n+j];
                }
                b[i*n+j] = sum / L[i*m+i];
            }
        }
    }

    /**
     * <p>
     * This is a forward substitution solver for non-singular lower triangular matrices.
     * <br>
     * b = (L<sup>T</sup>)<sup>-1</sup>b<br>
     * <br>
     * where b is a vector, L is an n by n matrix.<br>
     * </p>
     * <p>
     * L is a lower triangular matrix, but it comes up with a solution as if it was
     * an upper triangular matrix that was computed by transposing L.
     * </p>
     *
     * @param L An n by n non-singular lower triangular matrix. Not modified.
     * @param b A vector of length n. Modified.
     * @param n The size of the matrices.
     */
    public static void solveTranL( double L[] , double []b , int n )
    {
        for( int i =n-1; i>=0; i-- ) {
            double sum = b[i];
            for( int k = i+1; k <n; k++ ) {
                sum -= L[k*n+i]* b[k];
            }
            b[i] = sum/L[i*n+i];
        }
    }

    /**
     * <p>
     * This is a forward substitution solver for non-singular upper triangular matrices.
     * <br>
     * b = U<sup>-1</sup>b<br>
     * <br>
     * where b is a vector, U is an n by n matrix.<br>
     * </p>
     *
     * @param U An n by n non-singular upper triangular matrix. Not modified.
     * @param b A vector of length n. Modified.
     * @param n The size of the matrices.
     */
    public static void solveU( double U[] , double []b , int n )
    {
//        for( int i =n-1; i>=0; i-- ) {
//            double sum = b[i];
//            for( int j = i+1; j <n; j++ ) {
//                sum -= U[i*n+j]* b[j];
//            }
//            b[i] = sum/U[i*n+i];
//        }
        for( int i =n-1; i>=0; i-- ) {
            double sum = b[i];
            int indexU = i*n+i+1;
            for( int j = i+1; j <n; j++ ) {
                sum -= U[indexU++]* b[j];
            }
            b[i] = sum/U[i*n+i];
        }
    }

    public static void solveU( double U[] , double []b , int sideLength , int minRow , int maxRow )
    {
//        for( int i =maxRow-1; i>=minRow; i-- ) {
//            double sum = b[i];
//            for( int j = i+1; j <maxRow; j++ ) {
//                sum -= U[i*sideLength+j]* b[j];
//            }
//            b[i] = sum/U[i*sideLength+i];
//        }
        for( int i =maxRow-1; i>=minRow; i-- ) {
            double sum = b[i];
            int indexU = i*sideLength+i+1;
            for( int j = i+1; j <maxRow; j++ ) {
                sum -= U[indexU++]* b[j];
            }
            b[i] = sum/U[i*sideLength+i];
        }
    }

    /**
     * <p>
     * This is a forward substitution solver for non-singular upper triangular matrices which are
     * a sub-matrix inside a larger.  The columns of 'b' are solved for individually
     * <br>
     * b = U<sup>-1</sup>b<br>
     * <br>
     * where b is a matrix, U is an n by n matrix.<br>
     * </p>
     *
     * @param U Matrix containing the upper triangle system
     * @param startU Index of the first element in U
     * @param strideU stride between rows
     * @param widthU How wide the square matrix is
     * @param b Matrix containing the solution to the system.  Overwritten with the solution.
     * @param startB Index of the first element in B
     * @param strideB stride between rows
     * @param widthB How wide the matrix is.  Length is the same as U's width
     */
    public static void solveU( double []U , int startU , int strideU , int widthU ,
                               double []b , int startB , int strideB , int widthB )
    {
        for( int colB = 0; colB < widthB; colB++ ) {
            for( int i =widthU-1; i>=0; i-- ) {
                double sum = b[startB + i*strideB + colB];
                for( int j = i+1; j <widthU; j++ ) {
                    sum -= U[startU + i*strideU+j]* b[startB + j*strideB + colB ];
                }
                b[startB + i*strideB + colB] = sum/U[ startU + i*strideU + i ];
            }
        }

        // todo comment out the above and optimize it
    }
}
