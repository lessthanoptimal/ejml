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

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.data.RowD1Matrix64F;


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
public class QrHelperFunctions {

    public static double findMax( RowD1Matrix64F u, int startU , int length ) {
        double max = -1;

        int index = startU;
        int stopIndex = startU + length;
        for( ; index < stopIndex; index++ ) {
            double val = u.get(index);
            val = (val < 0.0D) ? -val : val;
            if( val > max )
                max = val;
        }

        return max;
    }

    public static double findMax( double []u, int startU , int length ) {
        double max = -1;

        int index = startU;
        int stopIndex = startU + length;
        for( ; index < stopIndex; index++ ) {
            double val = u[ index ];
            val = (val < 0.0D) ? -val : val;
            if( val > max )
                max = val;
        }

        return max;
    }

    public static void divideElements(int j, int numRows , double[] u, double u_0 ) {
        double div_u = 1.0/u_0;

        if( Double.isInfinite(div_u)) {
            for( int i = j; i < numRows; i++ ) {
                u[i] /= u_0;
            }
        } else {
            for( int i = j; i < numRows; i++ ) {
                u[i] *= div_u;
            }
        }
    }

    public static void divideElements(int j, int numRows , RowD1Matrix64F u, int startU , double u_0 ) {
        double div_u = 1.0/u_0;

        if( Double.isInfinite(div_u)) {
            for( int i = j; i < numRows; i++ ) {
                u.div( i+startU , u_0 );
            }
        } else {
            for( int i = j; i < numRows; i++ ) {
                u.times( i+startU , div_u );
            }
        }
    }

    public static void divideElements_Brow(int j, int numRows , double[] u,
                                             RowD1Matrix64F b , int startB ,
                                             double u_0 ) {
        double div_u = 1.0/u_0;

        if( Double.isInfinite(div_u)) {
            for( int i = j; i < numRows; i++ ) {
                u[i] = b.div( i+startB , u_0 );
            }
        } else {
            for( int i = j; i < numRows; i++ ) {
                u[i] = b.times( i+startB , div_u );
            }
        }
    }

    public static void divideElements_Bcol(int j, int numRows , int numCols ,
                                             double[] u,
                                             RowD1Matrix64F b , int startB ,
                                             double u_0 ) {
        double div_u = 1.0/u_0;

        if( Double.isInfinite(div_u)) {
            int indexB = j*numCols+startB;
            for( int i = j; i < numRows; i++ , indexB += numCols ) {
                b.set(indexB , u[i] /= u_0 );
            }
        } else {
            int indexB = j*numCols+startB;
            for( int i = j; i < numRows; i++ , indexB += numCols ) {
                b.set( indexB , u[i] *= div_u );
            }
        }
    }

    public static double computeTau(int j, int numRows , RowD1Matrix64F u, int startU , double max) {
        // compute the norm2 of the matrix, with each element
        // normalized by the max value to avoid overflow problems
        double tau = 0;
        double div_max = 1.0/max;
        if( Double.isInfinite(div_max)) {
            // more accurate
            for( int i = j; i < numRows; i++ ) {
                double d = u.div( startU+i , max);
                tau += d*d;
            }
        } else {
            // faster
            for( int i = j; i < numRows; i++ ) {
                double d = u.times( startU+i , div_max );
                tau += d*d;
            }
        }
        tau = Math.sqrt(tau);

        if( u.get(startU+j) < 0 )
            tau = -tau;

        return tau;
    }

    public static double computeTau(int j, int numRows , double[] u , double max) {
        // compute the norm2 of the matrix, with each element
        // normalized by the max value to avoid overflow problems
        double tau = 0;
        double div_max = 1.0/max;
        if( Double.isInfinite(div_max)) {
            for( int i = j; i < numRows; i++ ) {
                double d = u[i] /= max;
                tau += d*d;
            }
        } else {
            for( int i = j; i < numRows; i++ ) {
                double d = u[i] *= div_max;
                tau += d*d;
            }
        }
        tau = Math.sqrt(tau);

        if( u[j] < 0 )
            tau = -tau;

        return tau;
    }

//    public static double computeTau(int j, int numRows , RowD1Matrix64F u , double max) {
//        // compute the norm2 of the matrix, with each element
//        // normalized by the max value to avoid overflow problems
//        double tau = 0;
//        double div_max = 1.0/max;
//        if( Double.isInfinite(div_max)) {
//            for( int i = j; i < numRows; i++ ) {
//                double d = u.div(i,  max);
//                tau += d*d;
//            }
//        } else {
//            for( int i = j; i < numRows; i++ ) {
//                double d = u.times(i,  div_max);
//                tau += d*d;
//            }
//        }
//        tau = Math.sqrt(tau);
//
//        if( u.get(j) < 0 )
//            tau = -tau;
//
//        return tau;
//    }

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
    public static void rank1UpdateMultR( RowD1Matrix64F A , double u[] , double gamma ,
                                         int colA0,
                                         int w0, int w1 ,
                                         double _temp[] )
    {
//        for( int i = colA0; i < A.numCols; i++ ) {
//            double val = 0;
//
//            for( int k = w0; k < w1; k++ ) {
//                val += u[k]*A.data[k*A.numCols +i];
//            }
//            _temp[i] = gamma*val;
//        }

        // reordered to reduce cpu cache issues
        for( int i = colA0; i < A.numCols; i++ ) {
            _temp[i] = u[w0]*A.get( w0 *A.numCols +i );
        }

        for( int k = w0+1; k < w1; k++ ) {
            int indexA = k*A.numCols + colA0;
            double valU = u[k];
            for( int i = colA0; i < A.numCols; i++ ) {
                _temp[i] += valU*A.get( indexA++ );
            }
        }
        for( int i = colA0; i < A.numCols; i++ ) {
            _temp[i] *= gamma;
        }

        // end of reorder

        for( int i = w0; i < w1; i++ ) {
            double valU = u[i];

            int indexA = i*A.numCols + colA0;
            for( int j = colA0; j < A.numCols; j++ ) {
                A.minus( indexA++ , valU*_temp[j] );
            }
        }
    }

    public static void rank1UpdateMultR(RowD1Matrix64F A,
                                        double u[], int offsetU,
                                        double gamma,
                                        int colA0,
                                        int w0, int w1,
                                        double _temp[])
    {
//        for( int i = colA0; i < A.numCols; i++ ) {
//            double val = 0;
//
//            for( int k = w0; k < w1; k++ ) {
//                val += u[k+offsetU]*A.data[k*A.numCols +i];
//            }
//            _temp[i] = gamma*val;
//        }

        // reordered to reduce cpu cache issues
        for( int i = colA0; i < A.numCols; i++ ) {
            _temp[i] = u[w0+offsetU]*A.get( w0 *A.numCols +i );
        }

        for( int k = w0+1; k < w1; k++ ) {
            int indexA = k*A.numCols + colA0;
            double valU = u[k+offsetU];
            for( int i = colA0; i < A.numCols; i++ ) {
                _temp[i] += valU*A.get( indexA++ );
            }
        }
        for( int i = colA0; i < A.numCols; i++ ) {
            _temp[i] *= gamma;
        }

        // end of reorder

        for( int i = w0; i < w1; i++ ) {
            double valU = u[i+offsetU];

            int indexA = i*A.numCols + colA0;
            for( int j = colA0; j < A.numCols; j++ ) {
                A.minus( indexA++ , valU*_temp[j] );
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
    public static void rank1UpdateMultL( RowD1Matrix64F A , double u[] ,
                                         double gamma ,
                                         int colA0,
                                         int w0 , int w1 ,
                                         double _temp[] )
    {
        for( int i = colA0; i < A.numRows; i++ ) {
            double sum = 0;
            int rowIndex = i*A.numCols+w0;
            for( int j = w0; j < w1; j++ ) {
                sum += A.get(rowIndex++)*u[j];
            }
            _temp[i] = -gamma*sum;
        }

        for( int i = colA0; i < A.numRows; i++ ) {
            double a = _temp[i];
            int rowIndex = i*A.numCols+w0;
            for( int j = w0; j < w1; j++ ) {
                A.plus(rowIndex++ , a*u[j] );
            }
        }
    }
}
