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

package org.ejml.dense.row.decomposition.qr;

import org.ejml.data.FMatrixRMaj;


/**
 * <p>
 * Contains different functions that are useful for computing the QR decomposition of a matrix.
 * </p>
 *
 * <p>
 * Two different families of functions are provided for help in computing reflectors.  Internally
 * both of these functions switch between normalization by division or multiplication.  Multiplication
 * is most often significantly faster than division (2 or 3 times) but produces less accurate results
 * on very small numbers.  It checks to see if round off error is significant and decides which
 * one it should do.
 * </p>
 *
 * <p>
 * Tests were done using the stability benchmark in jmatbench and there doesn't seem to be
 * any advantage to always dividing by the max instead of checking and deciding.  The most
 * noticeable difference between the two methods is with very small numbers.
 * </p>
 *
 * @author Peter Abeles
 */
public class QrHelperFunctions_FDRM {

    public static float findMax( float[] u, int startU , int length ) {
        float max = -1;

        int index = startU;
        int stopIndex = startU + length;
        for( ; index < stopIndex; index++ ) {
            float val = u[index];
            val = (val < 0.0f) ? -val : val;
            if( val > max )
                max = val;
        }

        return max;
    }

    public static void divideElements(final int j, final int numRows ,
                                      final float[] u, final float u_0 ) {
//        float div_u = 1.0f/u_0;
//
//        if( Float.isInfinite(div_u)) {
            for( int i = j; i < numRows; i++ ) {
                u[i] /= u_0;
            }
//        } else {
//            for( int i = j; i < numRows; i++ ) {
//                u[i] *= div_u;
//            }
//        }
    }

    public static void divideElements(int j, int numRows , float[] u, int startU , float u_0 ) {
//        float div_u = 1.0f/u_0;
//
//        if( Float.isInfinite(div_u)) {
            for( int i = j; i < numRows; i++ ) {
                u[i+startU] /= u_0;
            }
//        } else {
//            for( int i = j; i < numRows; i++ ) {
//                u[i+startU] *= div_u;
//            }
//        }
    }

    public static void divideElements_Brow(int j, int numRows , float[] u,
                                             float b[] , int startB ,
                                             float u_0 ) {
//        float div_u = 1.0f/u_0;
//
//        if( Float.isInfinite(div_u)) {
            for( int i = j; i < numRows; i++ ) {
                u[i] = b[i+startB] /= u_0;
            }
//        } else {
//            for( int i = j; i < numRows; i++ ) {
//                u[i] = b[i+startB] *= div_u;
//            }
//        }
    }

    public static void divideElements_Bcol(int j, int numRows , int numCols ,
                                             float[] u,
                                             float b[] , int startB ,
                                             float u_0 ) {
//        float div_u = 1.0f/u_0;
//
//        if( Float.isInfinite(div_u)) {
            int indexB = j*numCols+startB;
            for( int i = j; i < numRows; i++ , indexB += numCols ) {
                b[indexB] = u[i] /= u_0;
            }
//        } else {
//            int indexB = j*numCols+startB;
//            for( int i = j; i < numRows; i++ , indexB += numCols ) {
//                b[indexB] = u[i] *= div_u;
//            }
//        }
    }

    public static float computeTauAndDivide(int j, int numRows , float[] u, int startU , float max) {
        // compute the norm2 of the matrix, with each element
        // normalized by the max value to avoid overflow problems
        float tau = 0;
//        float div_max = 1.0f/max;
//        if( Float.isInfinite(div_max)) {
            // more accurate
            for( int i = j; i < numRows; i++ ) {
                float d = u[startU+i] /= max;
                tau += d*d;
            }
//        } else {
//            // faster
//            for( int i = j; i < numRows; i++ ) {
//                float d = u[startU+i] *= div_max;
//                tau += d*d;
//            }
//        }
        tau = (float)Math.sqrt(tau);

        if( u[startU+j] < 0 )
            tau = -tau;

        return tau;
    }

    /**
     * Normalizes elements in 'u' by dividing by max and computes the norm2 of the normalized
     * array u.  Adjust the sign of the returned value depending on the size of the first
     * element in 'u'. Normalization is done to avoid overflow.
     *
     * <pre>
     * for i=j:numRows
     *   u[i] = u[i] / max
     *   tau = tau + u[i]*u[i]
     * end
     * tau = sqrt(tau)
     * if( u[j] &lt; 0 )
     *    tau = -tau;
     * </pre>
     *
     * @param j Element in 'u' that it starts at.
     * @param numRows Element in 'u' that it stops at.
     * @param u Array
     * @param max Max value in 'u' that is used to normalize it.
     * @return norm2 of 'u'
     */
    public static float computeTauAndDivide(final int j, final int numRows ,
                                             final float[] u , final float max) {
        float tau = 0;
//        float div_max = 1.0f/max;
//        if( Float.isInfinite(div_max)) {
            for( int i = j; i < numRows; i++ ) {
                float d = u[i] /= max;
                tau += d*d;
            }
//        } else {
//            for( int i = j; i < numRows; i++ ) {
//                float d = u[i] *= div_max;
//                tau += d*d;
//            }
//        }
        tau = (float)Math.sqrt(tau);

        if( u[j] < 0 )
            tau = -tau;

        return tau;
    }

    /**
     * <p>
     * Performs a rank-1 update operation on the submatrix specified by w with the multiply on the right.<br>
     * <br>
     * A = (I - &gamma;*u*u<sup>T</sup>)*A<br>
     * </p>
     * <p>
     * The order that matrix multiplies are performed has been carefully selected
     * to minimize the number of operations.
     * </p>
     *
     * <p>
     * Before this can become a truly generic operation the submatrix specification needs
     * to be made more generic.
     * </p>
     */
    public static void rank1UpdateMultR(FMatrixRMaj A , float u[] , float gamma ,
                                        int colA0,
                                        int w0, int w1 ,
                                        float _temp[] )
    {
//        for( int i = colA0; i < A.numCols; i++ ) {
//            float val = 0;
//
//            for( int k = w0; k < w1; k++ ) {
//                val += u[k]*A.data[k*A.numCols +i];
//            }
//            _temp[i] = gamma*val;
//        }

        // reordered to reduce cpu cache issues
        for( int i = colA0; i < A.numCols; i++ ) {
            _temp[i] = u[w0]*A.data[w0 *A.numCols +i];
        }

        for( int k = w0+1; k < w1; k++ ) {
            int indexA = k*A.numCols + colA0;
            float valU = u[k];
            for( int i = colA0; i < A.numCols; i++ ) {
                _temp[i] += valU*A.data[indexA++];
            }
        }
        for( int i = colA0; i < A.numCols; i++ ) {
            _temp[i] *= gamma;
        }

        // end of reorder

        for( int i = w0; i < w1; i++ ) {
            float valU = u[i];

            int indexA = i*A.numCols + colA0;
            for( int j = colA0; j < A.numCols; j++ ) {
                A.data[indexA++] -= valU*_temp[j];
            }
        }
    }

    public static void rank1UpdateMultR(FMatrixRMaj A,
                                        float u[], int offsetU,
                                        float gamma,
                                        int colA0,
                                        int w0, int w1,
                                        float _temp[])
    {
//        for( int i = colA0; i < A.numCols; i++ ) {
//            float val = 0;
//
//            for( int k = w0; k < w1; k++ ) {
//                val += u[k+offsetU]*A.data[k*A.numCols +i];
//            }
//            _temp[i] = gamma*val;
//        }

        // reordered to reduce cpu cache issues
        for( int i = colA0; i < A.numCols; i++ ) {
            _temp[i] = u[w0+offsetU]*A.data[w0 *A.numCols +i];
        }

        for( int k = w0+1; k < w1; k++ ) {
            int indexA = k*A.numCols + colA0;
            float valU = u[k+offsetU];
            for( int i = colA0; i < A.numCols; i++ ) {
                _temp[i] += valU*A.data[indexA++];
            }
        }
        for( int i = colA0; i < A.numCols; i++ ) {
            _temp[i] *= gamma;
        }

        // end of reorder

        for( int i = w0; i < w1; i++ ) {
            float valU = u[i+offsetU];

            int indexA = i*A.numCols + colA0;
            for( int j = colA0; j < A.numCols; j++ ) {
                A.data[indexA++] -= valU*_temp[j];
            }
        }
    }

    /**
     * <p>
     * Performs a rank-1 update operation on the submatrix specified by w with the multiply on the left.<br>
     * <br>
     * A = A(I - &gamma;*u*u<sup>T</sup>)<br>
     * </p>
     * <p>
     * The order that matrix multiplies are performed has been carefully selected
     * to minimize the number of operations.
     * </p>
     *
     * <p>
     * Before this can become a truly generic operation the submatrix specification needs
     * to be made more generic.
     * </p>
     */
    public static void rank1UpdateMultL(FMatrixRMaj A , float u[] ,
                                        float gamma ,
                                        int colA0,
                                        int w0 , int w1 )
    {
        for( int i = colA0; i < A.numRows; i++ ) {
            int startIndex = i*A.numCols+w0;
            float sum = 0;
            int rowIndex = startIndex;
            for( int j = w0; j < w1; j++ ) {
                sum += A.data[rowIndex++]*u[j];
            }
            sum = -gamma*sum;

            rowIndex = startIndex;
            for( int j = w0; j < w1; j++ ) {
                A.data[rowIndex++] += sum*u[j];
            }
        }
    }
}
