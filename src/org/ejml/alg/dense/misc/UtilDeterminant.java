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

package org.ejml.alg.dense.misc;

import org.ejml.data.DenseMatrix64F;


/**
 * <p>
 * The determinant of a matrix is a "special" number associated with square matrices.
 * It has many uses in linear algebra. Using minor matrices it can be defined as follows:<br>
 * <br>
 * |A| = Sum{ i=1:k ; a<sub>ij</sub> C<sub>ij</sub> }<br>
 * <br>
 * C<sub>ij</sub> = (-1)<sup>i+j</sup> M<sub>ij</sub><br>
 * </p>
 *
 * <p>
 * Where M_ij is the minor of matrix A formed by eliminating row i and column j from A. This
 * process is called determinant expansion by minors (or "Laplacian expansion by minors").<br>
 * <br>
 * Definition from http://mathworld.wolfram.com/Determinant.html Aug 3, 2009.
 * </p>
 *
 * @author Peter Abeles
 */
public class UtilDeterminant {

    /**
     * Computes a matrix using an expansion of minors technique.
     *
     * @see org.ejml.alg.dense.misc.DeterminantFromMinor
     *
     * @param mat The matrix whose determant is to be computed.
     * @return The determinant.
     */
    public static double detByMinors( DenseMatrix64F mat )
    {
        DeterminantFromMinor alg = new DeterminantFromMinor(mat.numRows);

        return alg.compute(mat);
    }

    public static double det2by2( double []mat )
    {
        return mat[0]*mat[3] - mat[1]*mat[2];
    }

    public static double det3by3( double []mat )
    {
        double a11 = mat[0];
        double a12 = mat[1];
        double a13 = mat[2];
        double a21 = mat[3];
        double a22 = mat[4];
        double a23 = mat[5];
        double a31 = mat[6];
        double a32 = mat[7];
        double a33 = mat[8];

        double a = a11*(a22*a33 - a23*a32);
        double b = a12*(a21*a33 - a23*a31);
        double c = a13*(a21*a32 - a31*a22);

        return a-b+c;
    }

    /**
     * Computes the determinant of a 4 by 4 matrix using same algorithm as {@link DeterminantFromMinor} but
     * where everything has been unrolled and is much faster.
     * 
     * @param mat A row major 4 by 4 matrix.
     * @return The determinant of the matrix
     */
    public static double det4by4( double []mat )
    {
        double l0,l1,l2,l3;

        double a11 = mat[5];
        double a12 = mat[6];
        double a13 = mat[7];
        double a21 = mat[9];
        double a22 = mat[10];
        double a23 = mat[11];
        double a31 = mat[13];
        double a32 = mat[14];
        double a33 = mat[15];

        double a = a11*(a22*a33 - a23*a32);
        double b = a12*(a21*a33 - a23*a31);
        double c = a13*(a21*a32 - a31*a22);

        l0 =  mat[0]*(a-b+c);

        a11 = mat[4];
//        a12 = mat[6];
//        a13 = mat[7];
        a21 = mat[8];
//        a22 = mat[10];
//        a23 = mat[11];
        a31 = mat[12];
//        a32 = mat[14];
//        a33 = mat[15];

        a = a11*(a22*a33 - a23*a32);
        b = a12*(a21*a33 - a23*a31);
        c = a13*(a21*a32 - a31*a22);

        l1 = mat[1]*(a-b+c);

//        a11 = mat[4];
        a12 = mat[5];
//        a13 = mat[7];
//        a21 = mat[8];
        a22 = mat[9];
//        a23 = mat[11];
//        a31 = mat[12];
        a32 = mat[13];
//        a33 = mat[15];

        a = a11*(a22*a33 - a23*a32);
        b = a12*(a21*a33 - a23*a31);
        c = a13*(a21*a32 - a31*a22);

        l2 = mat[2]*(a-b+c);

        //        a11 = mat[4];
//        a12 = mat[5];
        a13 = mat[6];
//        a21 = mat[8];
//        a22 = mat[9];
        a23 = mat[10];
//        a31 = mat[12];
//        a32 = mat[13];
        a33 = mat[14];

        a = a11*(a22*a33 - a23*a32);
        b = a12*(a21*a33 - a23*a31);
        c = a13*(a21*a32 - a31*a22);

        l3 = mat[3]*(a-b+c);

        return l0 - l1 + l2 - l3;
    }

    /**
     * <p>
     * A simple and inefficient algorithm for computing the determinant. This should never be used.
     * It is at least two orders of magnitude slower than {@link DeterminantFromMinor}. This is included
     * to provide a point of comparision for other algorithms.
     * </p>
     * @param mat The matrix that the determinant is to be computed from
     * @return The determinant.
     */
    public static double detRecursive( DenseMatrix64F mat )
    {
        if(mat.numRows == 1) {
            return mat.data[0];
        } else if(mat.numRows == 2) {
            return mat.data[0] * mat.data[3] - mat.data[1] * mat.data[2];
        } else if( mat.numRows == 3 ) {
            return det3by3(mat.data);
        }

        double result = 0;

        for(int i = 0; i < mat.numRows; i++) {
            DenseMatrix64F minorMat = new DenseMatrix64F(mat.numRows-1,mat.numRows-1);

            for(int j = 1; j < mat.numRows; j++) {
                for(int k = 0; k < mat.numRows; k++) {

                    if(k < i) {
                        minorMat.set(j-1,k,mat.get(j,k));
                    } else if(k > i) {
                        minorMat.set(j-1,k-1,mat.get(j,k));
                    }
                }
            }

            if( i % 2 == 0 )
                result += mat.get(0,i) * detRecursive(minorMat);
            else
                result -= mat.get(0,i) * detRecursive(minorMat);

        }

        return result;
    }
}
