/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decompose;

/**
 * <p>
 * This contains algorithms for solving systems of equations where T is a
 * non-singular triangular complex matrix:<br>
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
public class TriangularSolver_CDRM {
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
    public static void solveU( float U[] , float []b , int n )
    {
//        for( int i =n-1; i>=0; i-- ) {
//            float sum = b[i];
//            for( int j = i+1; j <n; j++ ) {
//                sum -= U[i*n+j]* b[j];
//            }
//            b[i] = sum/U[i*n+i];
//        }
        int stride = n*2;
        for( int i =n-1; i>=0; i-- ) {
            float sumReal = b[i*2];
            float sumImg = b[i*2+1];
            int indexU = i*stride+i*2+2;
            for( int j = i+1; j <n; j++ ) {
                float realB = b[j*2];
                float imgB = b[j*2+1];

                float realU = U[indexU++];
                float imgU = U[indexU++];

                sumReal -= realB*realU - imgB*imgU;
                sumImg -= realB*imgU + imgB*realU;
            }

            // b = sum/U
            float realU = U[i*stride+i*2];
            float imgU = U[i*stride+i*2+1];

            float normU = realU*realU + imgU*imgU;
            b[i*2] = (sumReal*realU + sumImg*imgU)/normU;
            b[i*2+1] = (sumImg*realU - sumReal*imgU)/normU;
        }
    }

    /**
     * <p>
     * Solves for non-singular lower triangular matrices with real valued diagonal elements
     * using forward substitution.
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
    public static void solveL_diagReal(float L[], float[] b, int n)
    {
//        for( int i = 0; i < n; i++ ) {
//            float sum = b[i];
//            for( int k=0; k<i; k++ ) {
//                sum -= L[i*n+k]* b[k];
//            }
//            b[i] = sum / L[i*n+i];
//        }
        int stride = n*2;

        for( int i = 0; i < n; i++ ) {
            float realSum = b[i*2];
            float imagSum = b[i*2+1];

            int indexL = i*stride;
            int indexB = 0;
            for( int k=0; k<i; k++ ) {
                float realL = L[indexL++];
                float imagL = L[indexL++];

                float realB = b[indexB++];
                float imagB = b[indexB++];

                realSum -= realL*realB - imagL*imagB;
                imagSum -= realL*imagB + imagL*realB;
            }

            float realL = L[indexL];

            b[i*2    ] = realSum/realL;
            b[i*2 + 1] = imagSum/realL;
        }
    }

    /**
     * <p>
     * This is a forward substitution solver for non-singular lower triangular matrices with
     * real valued diagonal elements.
     * <br>
     * b = (L<sup>CT</sup>)<sup>-1</sup>b<br>
     * <br>
     * where b is a vector, L is an n by n matrix.<br>
     * </p>
     * <p>
     * L is a lower triangular matrix, but it comes up with a solution as if it was
     * an upper triangular matrix that was computed by conjugate transposing L.
     * </p>
     *
     * @param L An n by n non-singular lower triangular matrix. Not modified.
     * @param b A vector of length n. Modified.
     * @param n The size of the matrices.
     */
    public static void solveConjTranL_diagReal(float L[], float[] b, int n)
    {
//        for( int i =n-1; i>=0; i-- ) {
//            float sum = b[i];
//            for( int k = i+1; k <n; k++ ) {
//                sum -= L[k*n+i]* b[k];
//            }
//            b[i] = sum/L[i*n+i];
//        }

        for( int i = n-1; i>=0; i-- ) {
            float realSum = b[i*2];
            float imagSum = b[i*2+1];

            int indexB = (i+1)*2;
            for( int k = i+1; k <n; k++ ) {
                int indexL = (k*n+i)*2;

                float realL = L[indexL];
                float imagL = L[indexL+1];

                float realB = b[indexB++];
                float imagB = b[indexB++];

                realSum -= realL*realB + imagL*imagB;
                imagSum -= realL*imagB - imagL*realB;
            }

            float realL =  L[(i*n+i)*2];

            b[i*2    ] = realSum/realL;
            b[i*2 + 1] = imagSum/realL;
        }
    }
}
