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

    public static double findMax( double[] u, int startU , int length ) {
        double max = -1;

        int index = startU;
        int stopIndex = startU + length;
        for( ; index < stopIndex; index++ ) {
            double val = u[index];
            val = (val <= 0.0D) ? 0.0D - val : val;
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

    public static void divideElements(int j, int numRows , double[] u, int startU , double u_0 ) {
        double div_u = 1.0/u_0;

        if( Double.isInfinite(div_u)) {
            for( int i = j; i < numRows; i++ ) {
                u[i+startU] /= u_0;
            }
        } else {
            for( int i = j; i < numRows; i++ ) {
                u[i+startU] *= div_u;
            }
        }
    }

    public static void divideElements_Brow(int j, int numRows , double[] u,
                                             double b[] , int startB ,
                                             double u_0 ) {
        double div_u = 1.0/u_0;

        if( Double.isInfinite(div_u)) {
            for( int i = j; i < numRows; i++ ) {
                u[i] = b[i+startB] /= u_0;
            }
        } else {
            for( int i = j; i < numRows; i++ ) {
                u[i] = b[i+startB] *= div_u;
            }
        }
    }

    public static void divideElements_Bcol(int j, int numRows , int numCols ,
                                             double[] u,
                                             double b[] , int startB ,
                                             double u_0 ) {
        double div_u = 1.0/u_0;

        if( Double.isInfinite(div_u)) {
            int indexB = j*numCols+startB;
            for( int i = j; i < numRows; i++ , indexB += numCols ) {
                b[indexB] = u[i] /= u_0;
            }
        } else {
            int indexB = j*numCols+startB;
            for( int i = j; i < numRows; i++ , indexB += numCols ) {
                b[indexB] = u[i] *= div_u;
            }
        }
    }

    public static double computeTau(int j, int numRows , double[] u, int startU , double max) {
        // compute the norm2 of the matrix, with each element
        // normalized by the max value to avoid overflow problems
        double tau = 0;
        double div_max = 1.0/max;
        if( Double.isInfinite(div_max)) {
            // more accurate
            for( int i = j; i < numRows; i++ ) {
                double d = u[startU+i] /= max;
                tau += d*d;
            }
        } else {
            // faster
            for( int i = j; i < numRows; i++ ) {
                double d = u[startU+i] *= div_max;
                tau += d*d;
            }
        }
        tau = Math.sqrt(tau);

        if( u[startU+j] < 0 )
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
}
