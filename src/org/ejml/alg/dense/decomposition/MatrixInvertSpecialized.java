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
 * This class is for hardcoded analytical matrix inversion algorithms.  This runsg much
 * faster than generalized algorithms since they don't need to iterate over loops and have
 * fewer array reads/writes.
 *
 * @author Peter Abeles
 */
public class MatrixInvertSpecialized {

    /**
     * Computes the inverse of a 2 by 2 matrix, fast
     *
     * @param m The matrix being inverted and where the results are stored.
     */
    public static void invert2x2( double m[] ) {
        double a = m[0];
        double b = m[1];
        double c = m[2];
        double d = m[3];

        double div = a*d - b*c;

        m[0] = d/div;
        m[1] = -b/div;
        m[2] = -c/div;
        m[3] = a/div;
    }

    /**
     * Computes the inverse matrix of 'A', which is assumed to be a 3 by 3 matrix.  Row-major
     * encoding is assumed
     *
     * @param A The row-major 3 by 3 matrix.
     */
    public static void invert3x3( double A[] ) {
        double a11 = A[0]; double a12 = A[1]; double a13 = A[2];
        double a21 = A[3]; double a22 = A[4]; double a23 = A[5];
        double a31 = A[6]; double a32 = A[7]; double a33 = A[8];

        //Compute the minor of matrix 'A' and stores the results in 'M'
        double M0 = a22*a33 - a32*a23;
        double M1 = -a31*a23 + a21*a33;
        double M2 = a21*a32 - a31*a22;
        double M3 = -a32*a13 + a12*a33;
        double M4 = a11*a33 - a31*a13;
        double M5 = -a31*a12 + a11*a32;
        double M6 = a12*a23 - a22*a13;
        double M7 = -a13*a21 + a11*a23;
        double M8 = a11*a22 - a21*a12;

        // compute matrix of cofactors
        M1 = -M1;
        M3 = -M3;
        M5 = -M5;
        M7 = -M7;

        // find the inverse of the determinate
        double detInv = 1.0/(a11*M0 + a12*M1 + a13*M2);

        // compute the inverse
        A[0] = M0 * detInv;
        A[1] = M3 * detInv;
        A[2] = M6 * detInv;
        A[3] = M1 * detInv;
        A[4] = M4 * detInv;
        A[5] = M7 * detInv;
        A[6] = M2 * detInv;
        A[7] = M5 * detInv;
        A[8] = M8 * detInv;
    }
}
