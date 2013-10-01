/*
 * Copyright (c) 2009-2013, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.fixed;

import org.ejml.data.FixedMatrix4x4_64F;

/**
 * Common matrix operations for fixed sized matrices which are 4 x 4 or 4 element vectors.
 *
 * @author Peter Abeles
 */
public class FixedOps4 {
    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = a + b <br>
     * c<sub>ij</sub> = a<sub>ij</sub> + b<sub>ij</sub> <br>
     * </p>
     *
     * <p>
     * Matrix C can be the same instance as Matrix A and/or B.
     * </p>
     *
     * @param a A Matrix. Not modified.
     * @param b A Matrix. Not modified.
     * @param c A Matrix where the results are stored. Modified.
     */
    public static void add( FixedMatrix4x4_64F a , FixedMatrix4x4_64F b , FixedMatrix4x4_64F c ) {
        c.a11 = a.a11 + b.a11;
        c.a12 = a.a12 + b.a12;
        c.a13 = a.a13 + b.a13;
        c.a14 = a.a14 + b.a14;
        c.a21 = a.a21 + b.a21;
        c.a22 = a.a22 + b.a22;
        c.a23 = a.a23 + b.a23;
        c.a24 = a.a24 + b.a24;
        c.a31 = a.a31 + b.a31;
        c.a32 = a.a32 + b.a32;
        c.a33 = a.a33 + b.a33;
        c.a34 = a.a34 + b.a34;
        c.a41 = a.a41 + b.a41;
        c.a42 = a.a42 + b.a42;
        c.a43 = a.a43 + b.a43;
        c.a44 = a.a44 + b.a44;
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * a = a + b <br>
     * a<sub>ij</sub> = a<sub>ij</sub> + b<sub>ij</sub> <br>
     * </p>
     *
     * @param a A Matrix. Modified.
     * @param b A Matrix. Not modified.
     */
    public static void addEquals( FixedMatrix4x4_64F a , FixedMatrix4x4_64F b ) {
        a.a11 += b.a11;
        a.a12 += b.a12;
        a.a13 += b.a13;
        a.a14 += b.a14;
        a.a21 += b.a21;
        a.a22 += b.a22;
        a.a23 += b.a23;
        a.a24 += b.a24;
        a.a31 += b.a31;
        a.a32 += b.a32;
        a.a33 += b.a33;
        a.a34 += b.a34;
        a.a41 += b.a41;
        a.a42 += b.a42;
        a.a43 += b.a43;
        a.a44 += b.a44;
    }

    /**
     * Performs an in-place transpose.  This algorithm is only efficient for square
     * matrices.
     *
     * @param m The matrix that is to be transposed. Modified.
     */
    public static void transpose( FixedMatrix4x4_64F m ) {
        double tmp;
        tmp = m.a12; m.a12 = m.a21; m.a21 = tmp;
        tmp = m.a13; m.a13 = m.a31; m.a31 = tmp;
        tmp = m.a14; m.a14 = m.a41; m.a41 = tmp;
        tmp = m.a23; m.a23 = m.a32; m.a32 = tmp;
        tmp = m.a24; m.a24 = m.a42; m.a42 = tmp;
        tmp = m.a34; m.a34 = m.a43; m.a43 = tmp;
    }

    /**
     * <p>
     * Transposes matrix 'a' and stores the results in 'b':<br>
     * <br>
     * b<sub>ij</sub> = a<sub>ji</sub><br>
     * where 'b' is the transpose of 'a'.
     * </p>
     *
     * @param input The original matrix.  Not modified.
     * @param output Where the transpose is stored. If null a new matrix is created. Modified.
     * @return The transposed matrix.
     */
    public static FixedMatrix4x4_64F transpose( FixedMatrix4x4_64F input , FixedMatrix4x4_64F output ) {
        if( input == null )
            input = new FixedMatrix4x4_64F();

        output.a11 = input.a11;
        output.a12 = input.a21;
        output.a13 = input.a31;
        output.a14 = input.a41;
        output.a21 = input.a12;
        output.a22 = input.a22;
        output.a23 = input.a32;
        output.a24 = input.a42;
        output.a31 = input.a13;
        output.a32 = input.a23;
        output.a33 = input.a33;
        output.a34 = input.a43;
        output.a41 = input.a14;
        output.a42 = input.a24;
        output.a43 = input.a34;
        output.a44 = input.a44;

        return output;
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = a * b <br>
     * <br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void mult( FixedMatrix4x4_64F a , FixedMatrix4x4_64F b , FixedMatrix4x4_64F c) {
        c.a11 = a.a11*b.a11 + a.a12*b.a21 + a.a13*b.a31 + a.a14*b.a41;
        c.a12 = a.a11*b.a12 + a.a12*b.a22 + a.a13*b.a32 + a.a14*b.a42;
        c.a13 = a.a11*b.a13 + a.a12*b.a23 + a.a13*b.a33 + a.a14*b.a43;
        c.a14 = a.a11*b.a14 + a.a12*b.a24 + a.a13*b.a34 + a.a14*b.a44;
        c.a21 = a.a21*b.a11 + a.a22*b.a21 + a.a23*b.a31 + a.a24*b.a41;
        c.a22 = a.a21*b.a12 + a.a22*b.a22 + a.a23*b.a32 + a.a24*b.a42;
        c.a23 = a.a21*b.a13 + a.a22*b.a23 + a.a23*b.a33 + a.a24*b.a43;
        c.a24 = a.a21*b.a14 + a.a22*b.a24 + a.a23*b.a34 + a.a24*b.a44;
        c.a31 = a.a31*b.a11 + a.a32*b.a21 + a.a33*b.a31 + a.a34*b.a41;
        c.a32 = a.a31*b.a12 + a.a32*b.a22 + a.a33*b.a32 + a.a34*b.a42;
        c.a33 = a.a31*b.a13 + a.a32*b.a23 + a.a33*b.a33 + a.a34*b.a43;
        c.a34 = a.a31*b.a14 + a.a32*b.a24 + a.a33*b.a34 + a.a34*b.a44;
        c.a41 = a.a41*b.a11 + a.a42*b.a21 + a.a43*b.a31 + a.a44*b.a41;
        c.a42 = a.a41*b.a12 + a.a42*b.a22 + a.a43*b.a32 + a.a44*b.a42;
        c.a43 = a.a41*b.a13 + a.a42*b.a23 + a.a43*b.a33 + a.a44*b.a43;
        c.a44 = a.a41*b.a14 + a.a42*b.a24 + a.a43*b.a34 + a.a44*b.a44;
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = a<sup>T</sup> * b <br>
     * <br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransA( FixedMatrix4x4_64F a , FixedMatrix4x4_64F b , FixedMatrix4x4_64F c) {
        c.a11 = a.a11*b.a11 + a.a21*b.a21 + a.a31*b.a31 + a.a41*b.a41;
        c.a12 = a.a11*b.a12 + a.a21*b.a22 + a.a31*b.a32 + a.a41*b.a42;
        c.a13 = a.a11*b.a13 + a.a21*b.a23 + a.a31*b.a33 + a.a41*b.a43;
        c.a14 = a.a11*b.a14 + a.a21*b.a24 + a.a31*b.a34 + a.a41*b.a44;
        c.a21 = a.a12*b.a11 + a.a22*b.a21 + a.a32*b.a31 + a.a42*b.a41;
        c.a22 = a.a12*b.a12 + a.a22*b.a22 + a.a32*b.a32 + a.a42*b.a42;
        c.a23 = a.a12*b.a13 + a.a22*b.a23 + a.a32*b.a33 + a.a42*b.a43;
        c.a24 = a.a12*b.a14 + a.a22*b.a24 + a.a32*b.a34 + a.a42*b.a44;
        c.a31 = a.a13*b.a11 + a.a23*b.a21 + a.a33*b.a31 + a.a43*b.a41;
        c.a32 = a.a13*b.a12 + a.a23*b.a22 + a.a33*b.a32 + a.a43*b.a42;
        c.a33 = a.a13*b.a13 + a.a23*b.a23 + a.a33*b.a33 + a.a43*b.a43;
        c.a34 = a.a13*b.a14 + a.a23*b.a24 + a.a33*b.a34 + a.a43*b.a44;
        c.a41 = a.a14*b.a11 + a.a24*b.a21 + a.a34*b.a31 + a.a44*b.a41;
        c.a42 = a.a14*b.a12 + a.a24*b.a22 + a.a34*b.a32 + a.a44*b.a42;
        c.a43 = a.a14*b.a13 + a.a24*b.a23 + a.a34*b.a33 + a.a44*b.a43;
        c.a44 = a.a14*b.a14 + a.a24*b.a24 + a.a34*b.a34 + a.a44*b.a44;
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = a<sup>T</sup> * b<sup>T</sup><br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransAB( FixedMatrix4x4_64F a , FixedMatrix4x4_64F b , FixedMatrix4x4_64F c) {
        c.a11 = a.a11*b.a11 + a.a21*b.a12 + a.a31*b.a13 + a.a41*b.a14;
        c.a12 = a.a11*b.a21 + a.a21*b.a22 + a.a31*b.a23 + a.a41*b.a24;
        c.a13 = a.a11*b.a31 + a.a21*b.a32 + a.a31*b.a33 + a.a41*b.a34;
        c.a14 = a.a11*b.a41 + a.a21*b.a42 + a.a31*b.a43 + a.a41*b.a44;
        c.a21 = a.a12*b.a11 + a.a22*b.a12 + a.a32*b.a13 + a.a42*b.a14;
        c.a22 = a.a12*b.a21 + a.a22*b.a22 + a.a32*b.a23 + a.a42*b.a24;
        c.a23 = a.a12*b.a31 + a.a22*b.a32 + a.a32*b.a33 + a.a42*b.a34;
        c.a24 = a.a12*b.a41 + a.a22*b.a42 + a.a32*b.a43 + a.a42*b.a44;
        c.a31 = a.a13*b.a11 + a.a23*b.a12 + a.a33*b.a13 + a.a43*b.a14;
        c.a32 = a.a13*b.a21 + a.a23*b.a22 + a.a33*b.a23 + a.a43*b.a24;
        c.a33 = a.a13*b.a31 + a.a23*b.a32 + a.a33*b.a33 + a.a43*b.a34;
        c.a34 = a.a13*b.a41 + a.a23*b.a42 + a.a33*b.a43 + a.a43*b.a44;
        c.a41 = a.a14*b.a11 + a.a24*b.a12 + a.a34*b.a13 + a.a44*b.a14;
        c.a42 = a.a14*b.a21 + a.a24*b.a22 + a.a34*b.a23 + a.a44*b.a24;
        c.a43 = a.a14*b.a31 + a.a24*b.a32 + a.a34*b.a33 + a.a44*b.a34;
        c.a44 = a.a14*b.a41 + a.a24*b.a42 + a.a34*b.a43 + a.a44*b.a44;
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = a * b<sup>T</sup> <br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransB( FixedMatrix4x4_64F a , FixedMatrix4x4_64F b , FixedMatrix4x4_64F c) {
        c.a11 = a.a11*b.a11 + a.a12*b.a12 + a.a13*b.a13 + a.a14*b.a14;
        c.a12 = a.a11*b.a21 + a.a12*b.a22 + a.a13*b.a23 + a.a14*b.a24;
        c.a13 = a.a11*b.a31 + a.a12*b.a32 + a.a13*b.a33 + a.a14*b.a34;
        c.a14 = a.a11*b.a41 + a.a12*b.a42 + a.a13*b.a43 + a.a14*b.a44;
        c.a21 = a.a21*b.a11 + a.a22*b.a12 + a.a23*b.a13 + a.a24*b.a14;
        c.a22 = a.a21*b.a21 + a.a22*b.a22 + a.a23*b.a23 + a.a24*b.a24;
        c.a23 = a.a21*b.a31 + a.a22*b.a32 + a.a23*b.a33 + a.a24*b.a34;
        c.a24 = a.a21*b.a41 + a.a22*b.a42 + a.a23*b.a43 + a.a24*b.a44;
        c.a31 = a.a31*b.a11 + a.a32*b.a12 + a.a33*b.a13 + a.a34*b.a14;
        c.a32 = a.a31*b.a21 + a.a32*b.a22 + a.a33*b.a23 + a.a34*b.a24;
        c.a33 = a.a31*b.a31 + a.a32*b.a32 + a.a33*b.a33 + a.a34*b.a34;
        c.a34 = a.a31*b.a41 + a.a32*b.a42 + a.a33*b.a43 + a.a34*b.a44;
        c.a41 = a.a41*b.a11 + a.a42*b.a12 + a.a43*b.a13 + a.a44*b.a14;
        c.a42 = a.a41*b.a21 + a.a42*b.a22 + a.a43*b.a23 + a.a44*b.a24;
        c.a43 = a.a41*b.a31 + a.a42*b.a32 + a.a43*b.a33 + a.a44*b.a34;
        c.a44 = a.a41*b.a41 + a.a42*b.a42 + a.a43*b.a43 + a.a44*b.a44;
    }

}

