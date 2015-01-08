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

package org.ejml.alg.fixed;

import org.ejml.data.FixedMatrix4_64F;
import org.ejml.data.FixedMatrix4x4_64F;

/**
 * <p>Common matrix operations for fixed sized matrices which are 4 x 4 or 4 element vectors.</p>
 * <p>DO NOT MODIFY.  Automatically generated code created by GenerateFixedOps</p>
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

    /**
     * <p>Performs matrix to vector multiplication:<br>
     * <br>
     * c = a * b <br>
     * <br>
     * c<sub>i</sub> = &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>k</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right vector in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void mult( FixedMatrix4x4_64F a , FixedMatrix4_64F b , FixedMatrix4_64F c) {
        c.a1 = a.a11*b.a1 + a.a12*b.a2 + a.a13*b.a3 + a.a14*b.a4;
        c.a2 = a.a21*b.a1 + a.a22*b.a2 + a.a23*b.a3 + a.a24*b.a4;
        c.a3 = a.a31*b.a1 + a.a32*b.a2 + a.a33*b.a3 + a.a34*b.a4;
        c.a4 = a.a41*b.a1 + a.a42*b.a2 + a.a43*b.a3 + a.a44*b.a4;
    }

    /**
     * <p>Performs vector to matrix multiplication:<br>
     * <br>
     * c = a * b <br>
     * <br>
     * c<sub>j</sub> = &sum;<sub>k=1:n</sub> { b<sub>k</sub> * a<sub>kj</sub> }
     * </p>
     *
     * @param a The left vector in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void mult( FixedMatrix4_64F a , FixedMatrix4x4_64F b , FixedMatrix4_64F c) {
        c.a1 = a.a1*b.a11 + a.a2*b.a21 + a.a3*b.a31 + a.a4*b.a41;
        c.a2 = a.a1*b.a12 + a.a2*b.a22 + a.a3*b.a32 + a.a4*b.a42;
        c.a3 = a.a1*b.a13 + a.a2*b.a23 + a.a3*b.a33 + a.a4*b.a43;
        c.a4 = a.a1*b.a14 + a.a2*b.a24 + a.a3*b.a34 + a.a4*b.a44;
    }

    /**
     * <p>Performs the vector dot product:<br>
     * <br>
     * c = a * b <br>
     * <br>
     * c> = &sum;<sub>k=1:n</sub> { b<sub>k</sub> * a<sub>k</sub> }
     * </p>
     *
     * @param a The left vector in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @return The dot product
     */
    public static double dot( FixedMatrix4_64F a , FixedMatrix4_64F b ) {
        return a.a1*b.a1 + a.a2*b.a2 + a.a3*b.a3 + a.a4*b.a4;
    }

    /**
     * Sets all the diagonal elements equal to one and everything else equal to zero.
     * If this is a square matrix then it will be an identity matrix.
     *
     * @param a A matrix.
     */
    public static void setIdentity( FixedMatrix4x4_64F a ) {
        a.a11 = 1; a.a21 = 0; a.a31 = 0; a.a41 = 0;
        a.a12 = 0; a.a22 = 1; a.a32 = 0; a.a42 = 0;
        a.a13 = 0; a.a23 = 0; a.a33 = 1; a.a43 = 0;
        a.a14 = 0; a.a24 = 0; a.a34 = 0; a.a44 = 1;
    }

    /**
     * Inverts matrix 'a' using minor matrices and stores the results in 'inv'.  Scaling is applied to improve
     * stability against overflow and underflow.
     *
     * WARNING: Potentially less stable than using LU decomposition.
     *
     * @param a Input matrix. Not modified.
     * @param inv Inverted output matrix.  Modified.
     * @return true if it was successful or false if it failed.  Not always reliable.
     */
    public static boolean invert( FixedMatrix4x4_64F a , FixedMatrix4x4_64F inv ) {

        double scale = 1.0/elementMaxAbs(a);

        double a11 = a.a11*scale;
        double a12 = a.a12*scale;
        double a13 = a.a13*scale;
        double a14 = a.a14*scale;
        double a21 = a.a21*scale;
        double a22 = a.a22*scale;
        double a23 = a.a23*scale;
        double a24 = a.a24*scale;
        double a31 = a.a31*scale;
        double a32 = a.a32*scale;
        double a33 = a.a33*scale;
        double a34 = a.a34*scale;
        double a41 = a.a41*scale;
        double a42 = a.a42*scale;
        double a43 = a.a43*scale;
        double a44 = a.a44*scale;

        double m11 =  + a22*(a33*a44 - a34*a43) - a23*(a32*a44 - a34*a42) + a24*(a32*a43 - a33*a42);
        double m12 = -(  + a21*(a33*a44 - a34*a43) - a23*(a31*a44 - a34*a41) + a24*(a31*a43 - a33*a41));
        double m13 =  + a21*(a32*a44 - a34*a42) - a22*(a31*a44 - a34*a41) + a24*(a31*a42 - a32*a41);
        double m14 = -(  + a21*(a32*a43 - a33*a42) - a22*(a31*a43 - a33*a41) + a23*(a31*a42 - a32*a41));
        double m21 = -(  + a12*(a33*a44 - a34*a43) - a13*(a32*a44 - a34*a42) + a14*(a32*a43 - a33*a42));
        double m22 =  + a11*(a33*a44 - a34*a43) - a13*(a31*a44 - a34*a41) + a14*(a31*a43 - a33*a41);
        double m23 = -(  + a11*(a32*a44 - a34*a42) - a12*(a31*a44 - a34*a41) + a14*(a31*a42 - a32*a41));
        double m24 =  + a11*(a32*a43 - a33*a42) - a12*(a31*a43 - a33*a41) + a13*(a31*a42 - a32*a41);
        double m31 =  + a12*(a23*a44 - a24*a43) - a13*(a22*a44 - a24*a42) + a14*(a22*a43 - a23*a42);
        double m32 = -(  + a11*(a23*a44 - a24*a43) - a13*(a21*a44 - a24*a41) + a14*(a21*a43 - a23*a41));
        double m33 =  + a11*(a22*a44 - a24*a42) - a12*(a21*a44 - a24*a41) + a14*(a21*a42 - a22*a41);
        double m34 = -(  + a11*(a22*a43 - a23*a42) - a12*(a21*a43 - a23*a41) + a13*(a21*a42 - a22*a41));
        double m41 = -(  + a12*(a23*a34 - a24*a33) - a13*(a22*a34 - a24*a32) + a14*(a22*a33 - a23*a32));
        double m42 =  + a11*(a23*a34 - a24*a33) - a13*(a21*a34 - a24*a31) + a14*(a21*a33 - a23*a31);
        double m43 = -(  + a11*(a22*a34 - a24*a32) - a12*(a21*a34 - a24*a31) + a14*(a21*a32 - a22*a31));
        double m44 =  + a11*(a22*a33 - a23*a32) - a12*(a21*a33 - a23*a31) + a13*(a21*a32 - a22*a31);

        double det = (a11*m11 + a12*m12 + a13*m13 + a14*m14)/scale;

        inv.a11 = m11/det;
        inv.a12 = m21/det;
        inv.a13 = m31/det;
        inv.a14 = m41/det;
        inv.a21 = m12/det;
        inv.a22 = m22/det;
        inv.a23 = m32/det;
        inv.a24 = m42/det;
        inv.a31 = m13/det;
        inv.a32 = m23/det;
        inv.a33 = m33/det;
        inv.a34 = m43/det;
        inv.a41 = m14/det;
        inv.a42 = m24/det;
        inv.a43 = m34/det;
        inv.a44 = m44/det;

        return !Double.isNaN(det) && !Double.isInfinite(det);
    }

    /**
     * Computes the determinant using minor matrices.
     * <p></p>
     * WARNING: Potentially less stable than using LU decomposition.
     *
     * @param mat Input matrix.  Not modified.
     * @return The determinant.
     */
    public static double det( FixedMatrix4x4_64F mat ) {

        double  a11 = mat.a22;
        double  a12 = mat.a23;
        double  a13 = mat.a24;
        double  a21 = mat.a32;
        double  a22 = mat.a33;
        double  a23 = mat.a34;
        double  a31 = mat.a42;
        double  a32 = mat.a43;
        double  a33 = mat.a44;

        double ret = 0;
        ret += mat.a11 * ( + a11*(a22*a33 - a23*a32) - a12*(a21*a33 - a23*a31) + a13*(a21*a32 - a22*a31));
        a11 = mat.a21;
        a21 = mat.a31;
        a31 = mat.a41;
        ret -= mat.a12 * ( + a11*(a22*a33 - a23*a32) - a12*(a21*a33 - a23*a31) + a13*(a21*a32 - a22*a31));
        a12 = mat.a22;
        a22 = mat.a32;
        a32 = mat.a42;
        ret += mat.a13 * ( + a11*(a22*a33 - a23*a32) - a12*(a21*a33 - a23*a31) + a13*(a21*a32 - a22*a31));
        a13 = mat.a23;
        a23 = mat.a33;
        a33 = mat.a43;
        ret -= mat.a14 * ( + a11*(a22*a33 - a23*a32) - a12*(a21*a33 - a23*a31) + a13*(a21*a32 - a22*a31));

        return ret;
    }

    /**
     * <p>
     * This computes the trace of the matrix:<br>
     * <br>
     * trace = &sum;<sub>i=1:n</sub> { a<sub>ii</sub> }
     * </p>
     * <p>
     * The trace is only defined for square matrices.
     * </p>
     *
     * @param a A square matrix.  Not modified.
     */
    public static double trace( FixedMatrix4x4_64F a ) {
        return a.a11 + a.a21 + a.a31 + a.a41;
    }

    /**
     * <p>
     * Extracts all diagonal elements from 'input' and places them inside the 'out' vector. Elements
     * are in sequential order.
     * </p>
     *
     *
     * @param input Matrix.  Not modified.
     * @param out Vector containing diagonal elements.  Modified.
     */
    public static void diag( FixedMatrix4x4_64F input , FixedMatrix4_64F out ) {
        out.a1 = input.a11;
        out.a2 = input.a22;
        out.a3 = input.a33;
        out.a4 = input.a44;
    }

    /**
     * <p>
     * Returns the value of the element in the matrix that has the largest value.<br>
     * <br>
     * Max{ a<sub>ij</sub> } for all i and j<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The max element value of the matrix.
     */
    public static double elementMax( FixedMatrix4x4_64F a ) {
        double max = a.a11;
        max = Math.max(max,a.a12);
        max = Math.max(max,a.a13);
        max = Math.max(max,a.a14);
        max = Math.max(max,a.a21);
        max = Math.max(max,a.a22);
        max = Math.max(max,a.a23);
        max = Math.max(max,a.a24);
        max = Math.max(max,a.a31);
        max = Math.max(max,a.a32);
        max = Math.max(max,a.a33);
        max = Math.max(max,a.a34);
        max = Math.max(max,a.a41);
        max = Math.max(max,a.a42);
        max = Math.max(max,a.a43);
        max = Math.max(max,a.a44);

        return max;
    }

    /**
     * <p>
     * Returns the absolute value of the element in the matrix that has the largest absolute value.<br>
     * <br>
     * Max{ |a<sub>ij</sub>| } for all i and j<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The max abs element value of the matrix.
     */
    public static double elementMaxAbs( FixedMatrix4x4_64F a ) {
        double max = a.a11;
        max = Math.max(max,Math.abs(a.a12));
        max = Math.max(max,Math.abs(a.a13));
        max = Math.max(max,Math.abs(a.a14));
        max = Math.max(max,Math.abs(a.a21));
        max = Math.max(max,Math.abs(a.a22));
        max = Math.max(max,Math.abs(a.a23));
        max = Math.max(max,Math.abs(a.a24));
        max = Math.max(max,Math.abs(a.a31));
        max = Math.max(max,Math.abs(a.a32));
        max = Math.max(max,Math.abs(a.a33));
        max = Math.max(max,Math.abs(a.a34));
        max = Math.max(max,Math.abs(a.a41));
        max = Math.max(max,Math.abs(a.a42));
        max = Math.max(max,Math.abs(a.a43));
        max = Math.max(max,Math.abs(a.a44));

        return max;
    }

    /**
     * <p>
     * Returns the value of the element in the matrix that has the minimum value.<br>
     * <br>
     * Min{ a<sub>ij</sub> } for all i and j<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The value of element in the matrix with the minimum value.
     */
    public static double elementMin( FixedMatrix4x4_64F a ) {
        double min = a.a11;
        min = Math.min(min,a.a12);
        min = Math.min(min,a.a13);
        min = Math.min(min,a.a14);
        min = Math.min(min,a.a21);
        min = Math.min(min,a.a22);
        min = Math.min(min,a.a23);
        min = Math.min(min,a.a24);
        min = Math.min(min,a.a31);
        min = Math.min(min,a.a32);
        min = Math.min(min,a.a33);
        min = Math.min(min,a.a34);
        min = Math.min(min,a.a41);
        min = Math.min(min,a.a42);
        min = Math.min(min,a.a43);
        min = Math.min(min,a.a44);

        return min;
    }

    /**
     * <p>
     * Returns the absolute value of the element in the matrix that has the smallest absolute value.<br>
     * <br>
     * Min{ |a<sub>ij</sub>| } for all i and j<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The max element value of the matrix.
     */
    public static double elementMinAbs( FixedMatrix4x4_64F a ) {
        double min = a.a11;
        min = Math.min(min,Math.abs(a.a12));
        min = Math.min(min,Math.abs(a.a13));
        min = Math.min(min,Math.abs(a.a14));
        min = Math.min(min,Math.abs(a.a21));
        min = Math.min(min,Math.abs(a.a22));
        min = Math.min(min,Math.abs(a.a23));
        min = Math.min(min,Math.abs(a.a24));
        min = Math.min(min,Math.abs(a.a31));
        min = Math.min(min,Math.abs(a.a32));
        min = Math.min(min,Math.abs(a.a33));
        min = Math.min(min,Math.abs(a.a34));
        min = Math.min(min,Math.abs(a.a41));
        min = Math.min(min,Math.abs(a.a42));
        min = Math.min(min,Math.abs(a.a43));
        min = Math.min(min,Math.abs(a.a44));

        return min;
    }

    /**
     * <p>Performs the an element by element multiplication operation:<br>
     * <br>
     * a<sub>ij</sub> = a<sub>ij</sub> * b<sub>ij</sub> <br>
     * </p>
     * @param a The left matrix in the multiplication operation. Modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     */
    public static void elementMult( FixedMatrix4x4_64F a , FixedMatrix4x4_64F b) {
        a.a11 *= b.a11; a.a12 *= b.a12; a.a13 *= b.a13; a.a14 *= b.a14;
        a.a21 *= b.a21; a.a22 *= b.a22; a.a23 *= b.a23; a.a24 *= b.a24;
        a.a31 *= b.a31; a.a32 *= b.a32; a.a33 *= b.a33; a.a34 *= b.a34;
        a.a41 *= b.a41; a.a42 *= b.a42; a.a43 *= b.a43; a.a44 *= b.a44;
    }

    /**
     * <p>Performs the an element by element multiplication operation:<br>
     * <br>
     * c<sub>ij</sub> = a<sub>ij</sub> * b<sub>ij</sub> <br>
     * </p>
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void elementMult( FixedMatrix4x4_64F a , FixedMatrix4x4_64F b , FixedMatrix4x4_64F c ) {
        c.a11 = a.a11*b.a11; c.a12 = a.a12*b.a12; c.a13 = a.a13*b.a13; c.a14 = a.a14*b.a14;
        c.a21 = a.a21*b.a21; c.a22 = a.a22*b.a22; c.a23 = a.a23*b.a23; c.a24 = a.a24*b.a24;
        c.a31 = a.a31*b.a31; c.a32 = a.a32*b.a32; c.a33 = a.a33*b.a33; c.a34 = a.a34*b.a34;
        c.a41 = a.a41*b.a41; c.a42 = a.a42*b.a42; c.a43 = a.a43*b.a43; c.a44 = a.a44*b.a44;
    }

    /**
     * <p>Performs the an element by element division operation:<br>
     * <br>
     * a<sub>ij</sub> = a<sub>ij</sub> / b<sub>ij</sub> <br>
     * </p>
     * @param a The left matrix in the division operation. Modified.
     * @param b The right matrix in the division operation. Not modified.
     */
    public static void elementDiv( FixedMatrix4x4_64F a , FixedMatrix4x4_64F b) {
        a.a11 /= b.a11; a.a12 /= b.a12; a.a13 /= b.a13; a.a14 /= b.a14;
        a.a21 /= b.a21; a.a22 /= b.a22; a.a23 /= b.a23; a.a24 /= b.a24;
        a.a31 /= b.a31; a.a32 /= b.a32; a.a33 /= b.a33; a.a34 /= b.a34;
        a.a41 /= b.a41; a.a42 /= b.a42; a.a43 /= b.a43; a.a44 /= b.a44;
    }

    /**
     * <p>Performs the an element by element division operation:<br>
     * <br>
     * c<sub>ij</sub> = a<sub>ij</sub> / b<sub>ij</sub> <br>
     * </p>
     * @param a The left matrix in the division operation. Not modified.
     * @param b The right matrix in the division operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void elementDiv( FixedMatrix4x4_64F a , FixedMatrix4x4_64F b , FixedMatrix4x4_64F c ) {
        c.a11 = a.a11/b.a11; c.a12 = a.a12/b.a12; c.a13 = a.a13/b.a13; c.a14 = a.a14/b.a14;
        c.a21 = a.a21/b.a21; c.a22 = a.a22/b.a22; c.a23 = a.a23/b.a23; c.a24 = a.a24/b.a24;
        c.a31 = a.a31/b.a31; c.a32 = a.a32/b.a32; c.a33 = a.a33/b.a33; c.a34 = a.a34/b.a34;
        c.a41 = a.a41/b.a41; c.a42 = a.a42/b.a42; c.a43 = a.a43/b.a43; c.a44 = a.a44/b.a44;
    }

    /**
     * <p>
     * Performs an in-place element by element scalar multiplication.<br>
     * <br>
     * a<sub>ij</sub> = &alpha;*a<sub>ij</sub>
     * </p>
     *
     * @param a The matrix that is to be scaled.  Modified.
     * @param alpha the amount each element is multiplied by.
     */
    public static void scale( double alpha , FixedMatrix4x4_64F a ) {
        a.a11 *= alpha; a.a12 *= alpha; a.a13 *= alpha; a.a14 *= alpha;
        a.a21 *= alpha; a.a22 *= alpha; a.a23 *= alpha; a.a24 *= alpha;
        a.a31 *= alpha; a.a32 *= alpha; a.a33 *= alpha; a.a34 *= alpha;
        a.a41 *= alpha; a.a42 *= alpha; a.a43 *= alpha; a.a44 *= alpha;
    }

    /**
     * <p>
     * Performs an element by element scalar multiplication.<br>
     * <br>
     * b<sub>ij</sub> = &alpha;*a<sub>ij</sub>
     * </p>
     *
     * @param alpha the amount each element is multiplied by.
     * @param a The matrix that is to be scaled.  Not modified.
     * @param b Where the scaled matrix is stored. Modified.
     */
    public static void scale( double alpha , FixedMatrix4x4_64F a , FixedMatrix4x4_64F b ) {
        b.a11 = a.a11*alpha; b.a12 = a.a12*alpha; b.a13 = a.a13*alpha; b.a14 = a.a14*alpha;
        b.a21 = a.a21*alpha; b.a22 = a.a22*alpha; b.a23 = a.a23*alpha; b.a24 = a.a24*alpha;
        b.a31 = a.a31*alpha; b.a32 = a.a32*alpha; b.a33 = a.a33*alpha; b.a34 = a.a34*alpha;
        b.a41 = a.a41*alpha; b.a42 = a.a42*alpha; b.a43 = a.a43*alpha; b.a44 = a.a44*alpha;
    }

    /**
     * <p>
     * Performs an in-place element by element scalar division. Scalar denominator.<br>
     * <br>
     * a<sub>ij</sub> = a<sub>ij</sub>/&alpha;
     * </p>
     *
     * @param a The matrix whose elements are to be divided.  Modified.
     * @param alpha the amount each element is divided by.
     */
    public static void divide( FixedMatrix4x4_64F a , double alpha ) {
        a.a11 /= alpha; a.a12 /= alpha; a.a13 /= alpha; a.a14 /= alpha;
        a.a21 /= alpha; a.a22 /= alpha; a.a23 /= alpha; a.a24 /= alpha;
        a.a31 /= alpha; a.a32 /= alpha; a.a33 /= alpha; a.a34 /= alpha;
        a.a41 /= alpha; a.a42 /= alpha; a.a43 /= alpha; a.a44 /= alpha;
    }

    /**
     * <p>
     * Performs an element by element scalar division.  Scalar denominator.<br>
     * <br>
     * b<sub>ij</sub> = *a<sub>ij</sub> /&alpha;
     * </p>
     *
     * @param alpha the amount each element is divided by.
     * @param a The matrix whose elements are to be divided.  Not modified.
     * @param b Where the results are stored. Modified.
     */
    public static void divide( FixedMatrix4x4_64F a , double alpha , FixedMatrix4x4_64F b ) {
        b.a11 = a.a11/alpha; b.a12 = a.a12/alpha; b.a13 = a.a13/alpha; b.a14 = a.a14/alpha;
        b.a21 = a.a21/alpha; b.a22 = a.a22/alpha; b.a23 = a.a23/alpha; b.a24 = a.a24/alpha;
        b.a31 = a.a31/alpha; b.a32 = a.a32/alpha; b.a33 = a.a33/alpha; b.a34 = a.a34/alpha;
        b.a41 = a.a41/alpha; b.a42 = a.a42/alpha; b.a43 = a.a43/alpha; b.a44 = a.a44/alpha;
    }

    /**
     * <p>
     * Changes the sign of every element in the matrix.<br>
     * <br>
     * a<sub>ij</sub> = -a<sub>ij</sub>
     * </p>
     *
     * @param a A matrix. Modified.
     */
    public static void changeSign( FixedMatrix4x4_64F a )
    {
        a.a11 = -a.a11; a.a12 = -a.a12; a.a13 = -a.a13; a.a14 = -a.a14;
        a.a21 = -a.a21; a.a22 = -a.a22; a.a23 = -a.a23; a.a24 = -a.a24;
        a.a31 = -a.a31; a.a32 = -a.a32; a.a33 = -a.a33; a.a34 = -a.a34;
        a.a41 = -a.a41; a.a42 = -a.a42; a.a43 = -a.a43; a.a44 = -a.a44;
    }

    /**
     * <p>
     * Sets every element in the matrix to the specified value.<br>
     * <br>
     * a<sub>ij</sub> = value
     * <p>
     *
     * @param a A matrix whose elements are about to be set. Modified.
     * @param v The value each element will have.
     */
    public static void fill( FixedMatrix4x4_64F a , double v  ) {
        a.a11 = v; a.a12 = v; a.a13 = v; a.a14 = v;
        a.a21 = v; a.a22 = v; a.a23 = v; a.a24 = v;
        a.a31 = v; a.a32 = v; a.a33 = v; a.a34 = v;
        a.a41 = v; a.a42 = v; a.a43 = v; a.a44 = v;
    }

}

