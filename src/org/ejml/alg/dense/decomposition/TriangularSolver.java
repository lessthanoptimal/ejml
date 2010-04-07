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
        for( int i = 0; i < n; i++ ) {
            double sum = b[i];
            for( int k=0; k<i; k++ ) {
                sum -= L[i*n+k]* b[k];
            }
            b[i] = sum / L[i*n+i];
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
        for( int i =n-1; i>=0; i-- ) {
            double sum = b[i];
            for( int j = i+1; j <n; j++ ) {
                sum -= U[i*n+j]* b[j];
            }
            b[i] = sum/U[i*n+i];
        }
    }

    public static void solveU( double U[] , double []b , int sideLength , int minRow , int maxRow )
    {
        for( int i =maxRow-1; i>=minRow; i-- ) {
            double sum = b[i];
            for( int j = i+1; j <maxRow; j++ ) {
                sum -= U[i*sideLength+j]* b[j];
            }
            b[i] = sum/U[i*sideLength+i];
        }
    }
}
