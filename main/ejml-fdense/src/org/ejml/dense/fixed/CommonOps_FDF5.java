/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.fixed;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrix5;
import org.ejml.data.FMatrix5x5;

/**
 * <p>Common matrix operations for fixed sized matrices which are 5 x 5 or 5 element vectors.</p>
 * <p>DO NOT MODIFY.  Automatically generated code created by GenerateCommonOps_DDF</p>
 *
 * @author Peter Abeles
 */
public class CommonOps_FDF5 {
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
    public static void add( FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c ) {
        c.a11 = a.a11 + b.a11;
        c.a12 = a.a12 + b.a12;
        c.a13 = a.a13 + b.a13;
        c.a14 = a.a14 + b.a14;
        c.a15 = a.a15 + b.a15;
        c.a21 = a.a21 + b.a21;
        c.a22 = a.a22 + b.a22;
        c.a23 = a.a23 + b.a23;
        c.a24 = a.a24 + b.a24;
        c.a25 = a.a25 + b.a25;
        c.a31 = a.a31 + b.a31;
        c.a32 = a.a32 + b.a32;
        c.a33 = a.a33 + b.a33;
        c.a34 = a.a34 + b.a34;
        c.a35 = a.a35 + b.a35;
        c.a41 = a.a41 + b.a41;
        c.a42 = a.a42 + b.a42;
        c.a43 = a.a43 + b.a43;
        c.a44 = a.a44 + b.a44;
        c.a45 = a.a45 + b.a45;
        c.a51 = a.a51 + b.a51;
        c.a52 = a.a52 + b.a52;
        c.a53 = a.a53 + b.a53;
        c.a54 = a.a54 + b.a54;
        c.a55 = a.a55 + b.a55;
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = a + b <br>
     * c<sub>i</sub> = a<sub>i</sub> + b<sub>i</sub> <br>
     * </p>
     *
     * <p>
     * Vector C can be the same instance as Vector A and/or B.
     * </p>
     *
     * @param a A Vector. Not modified.
     * @param b A Vector. Not modified.
     * @param c A Vector where the results are stored. Modified.
     */
    public static void add( FMatrix5 a , FMatrix5 b , FMatrix5 c ) {
        c.a1 = a.a1 + b.a1;
        c.a2 = a.a2 + b.a2;
        c.a3 = a.a3 + b.a3;
        c.a4 = a.a4 + b.a4;
        c.a5 = a.a5 + b.a5;
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
    public static void addEquals( FMatrix5x5 a , FMatrix5x5 b ) {
        a.a11 += b.a11;
        a.a12 += b.a12;
        a.a13 += b.a13;
        a.a14 += b.a14;
        a.a15 += b.a15;
        a.a21 += b.a21;
        a.a22 += b.a22;
        a.a23 += b.a23;
        a.a24 += b.a24;
        a.a25 += b.a25;
        a.a31 += b.a31;
        a.a32 += b.a32;
        a.a33 += b.a33;
        a.a34 += b.a34;
        a.a35 += b.a35;
        a.a41 += b.a41;
        a.a42 += b.a42;
        a.a43 += b.a43;
        a.a44 += b.a44;
        a.a45 += b.a45;
        a.a51 += b.a51;
        a.a52 += b.a52;
        a.a53 += b.a53;
        a.a54 += b.a54;
        a.a55 += b.a55;
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * a = a + b <br>
     * a<sub>i</sub> = a<sub>i</sub> + b<sub>i</sub> <br>
     * </p>
     *
     * @param a A Vector. Modified.
     * @param b A Vector. Not modified.
     */
    public static void addEquals( FMatrix5 a , FMatrix5 b ) {
        a.a1 += b.a1;
        a.a2 += b.a2;
        a.a3 += b.a3;
        a.a4 += b.a4;
        a.a5 += b.a5;
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = a - b <br>
     * c<sub>ij</sub> = a<sub>ij</sub> - b<sub>ij</sub> <br>
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
    public static void subtract( FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c ) {
        c.a11 = a.a11 - b.a11;
        c.a12 = a.a12 - b.a12;
        c.a13 = a.a13 - b.a13;
        c.a14 = a.a14 - b.a14;
        c.a15 = a.a15 - b.a15;
        c.a21 = a.a21 - b.a21;
        c.a22 = a.a22 - b.a22;
        c.a23 = a.a23 - b.a23;
        c.a24 = a.a24 - b.a24;
        c.a25 = a.a25 - b.a25;
        c.a31 = a.a31 - b.a31;
        c.a32 = a.a32 - b.a32;
        c.a33 = a.a33 - b.a33;
        c.a34 = a.a34 - b.a34;
        c.a35 = a.a35 - b.a35;
        c.a41 = a.a41 - b.a41;
        c.a42 = a.a42 - b.a42;
        c.a43 = a.a43 - b.a43;
        c.a44 = a.a44 - b.a44;
        c.a45 = a.a45 - b.a45;
        c.a51 = a.a51 - b.a51;
        c.a52 = a.a52 - b.a52;
        c.a53 = a.a53 - b.a53;
        c.a54 = a.a54 - b.a54;
        c.a55 = a.a55 - b.a55;
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = a - b <br>
     * c<sub>i</sub> = a<sub>i</sub> - b<sub>i</sub> <br>
     * </p>
     *
     * <p>
     * Vector C can be the same instance as Vector A and/or B.
     * </p>
     *
     * @param a A Vector. Not modified.
     * @param b A Vector. Not modified.
     * @param c A Vector where the results are stored. Modified.
     */
    public static void subtract( FMatrix5 a , FMatrix5 b , FMatrix5 c ) {
        c.a1 = a.a1 - b.a1;
        c.a2 = a.a2 - b.a2;
        c.a3 = a.a3 - b.a3;
        c.a4 = a.a4 - b.a4;
        c.a5 = a.a5 - b.a5;
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * a = a - b <br>
     * a<sub>ij</sub> = a<sub>ij</sub> - b<sub>ij</sub> <br>
     * </p>
     *
     * @param a A Matrix. Modified.
     * @param b A Matrix. Not modified.
     */
    public static void subtractEquals( FMatrix5x5 a , FMatrix5x5 b ) {
        a.a11 -= b.a11;
        a.a12 -= b.a12;
        a.a13 -= b.a13;
        a.a14 -= b.a14;
        a.a15 -= b.a15;
        a.a21 -= b.a21;
        a.a22 -= b.a22;
        a.a23 -= b.a23;
        a.a24 -= b.a24;
        a.a25 -= b.a25;
        a.a31 -= b.a31;
        a.a32 -= b.a32;
        a.a33 -= b.a33;
        a.a34 -= b.a34;
        a.a35 -= b.a35;
        a.a41 -= b.a41;
        a.a42 -= b.a42;
        a.a43 -= b.a43;
        a.a44 -= b.a44;
        a.a45 -= b.a45;
        a.a51 -= b.a51;
        a.a52 -= b.a52;
        a.a53 -= b.a53;
        a.a54 -= b.a54;
        a.a55 -= b.a55;
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * a = a - b <br>
     * a<sub>i</sub> = a<sub>i</sub> - b<sub>i</sub> <br>
     * </p>
     *
     * @param a A Vector. Modified.
     * @param b A Vector. Not modified.
     */
    public static void subtractEquals( FMatrix5 a , FMatrix5 b ) {
        a.a1 -= b.a1;
        a.a2 -= b.a2;
        a.a3 -= b.a3;
        a.a4 -= b.a4;
        a.a5 -= b.a5;
    }

    /**
     * Performs an in-place transpose.  This algorithm is only efficient for square
     * matrices.
     *
     * @param m The matrix that is to be transposed. Modified.
     */
    public static void transpose( FMatrix5x5 m ) {
        float tmp;
        tmp = m.a12; m.a12 = m.a21; m.a21 = tmp;
        tmp = m.a13; m.a13 = m.a31; m.a31 = tmp;
        tmp = m.a14; m.a14 = m.a41; m.a41 = tmp;
        tmp = m.a15; m.a15 = m.a51; m.a51 = tmp;
        tmp = m.a23; m.a23 = m.a32; m.a32 = tmp;
        tmp = m.a24; m.a24 = m.a42; m.a42 = tmp;
        tmp = m.a25; m.a25 = m.a52; m.a52 = tmp;
        tmp = m.a34; m.a34 = m.a43; m.a43 = tmp;
        tmp = m.a35; m.a35 = m.a53; m.a53 = tmp;
        tmp = m.a45; m.a45 = m.a54; m.a54 = tmp;
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
    public static FMatrix5x5 transpose( FMatrix5x5 input , FMatrix5x5 output ) {
        if( input == null )
            input = new FMatrix5x5();

        UtilEjml.checkSameInstance(input,output);
        output.a11 = input.a11;
        output.a12 = input.a21;
        output.a13 = input.a31;
        output.a14 = input.a41;
        output.a15 = input.a51;
        output.a21 = input.a12;
        output.a22 = input.a22;
        output.a23 = input.a32;
        output.a24 = input.a42;
        output.a25 = input.a52;
        output.a31 = input.a13;
        output.a32 = input.a23;
        output.a33 = input.a33;
        output.a34 = input.a43;
        output.a35 = input.a53;
        output.a41 = input.a14;
        output.a42 = input.a24;
        output.a43 = input.a34;
        output.a44 = input.a44;
        output.a45 = input.a54;
        output.a51 = input.a15;
        output.a52 = input.a25;
        output.a53 = input.a35;
        output.a54 = input.a45;
        output.a55 = input.a55;

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
     * @param c (Output) Where the results of the operation are stored. Modified.
     */
    public static void mult( FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 = a.a11*b.a11 + a.a12*b.a21 + a.a13*b.a31 + a.a14*b.a41 + a.a15*b.a51;
        c.a12 = a.a11*b.a12 + a.a12*b.a22 + a.a13*b.a32 + a.a14*b.a42 + a.a15*b.a52;
        c.a13 = a.a11*b.a13 + a.a12*b.a23 + a.a13*b.a33 + a.a14*b.a43 + a.a15*b.a53;
        c.a14 = a.a11*b.a14 + a.a12*b.a24 + a.a13*b.a34 + a.a14*b.a44 + a.a15*b.a54;
        c.a15 = a.a11*b.a15 + a.a12*b.a25 + a.a13*b.a35 + a.a14*b.a45 + a.a15*b.a55;
        c.a21 = a.a21*b.a11 + a.a22*b.a21 + a.a23*b.a31 + a.a24*b.a41 + a.a25*b.a51;
        c.a22 = a.a21*b.a12 + a.a22*b.a22 + a.a23*b.a32 + a.a24*b.a42 + a.a25*b.a52;
        c.a23 = a.a21*b.a13 + a.a22*b.a23 + a.a23*b.a33 + a.a24*b.a43 + a.a25*b.a53;
        c.a24 = a.a21*b.a14 + a.a22*b.a24 + a.a23*b.a34 + a.a24*b.a44 + a.a25*b.a54;
        c.a25 = a.a21*b.a15 + a.a22*b.a25 + a.a23*b.a35 + a.a24*b.a45 + a.a25*b.a55;
        c.a31 = a.a31*b.a11 + a.a32*b.a21 + a.a33*b.a31 + a.a34*b.a41 + a.a35*b.a51;
        c.a32 = a.a31*b.a12 + a.a32*b.a22 + a.a33*b.a32 + a.a34*b.a42 + a.a35*b.a52;
        c.a33 = a.a31*b.a13 + a.a32*b.a23 + a.a33*b.a33 + a.a34*b.a43 + a.a35*b.a53;
        c.a34 = a.a31*b.a14 + a.a32*b.a24 + a.a33*b.a34 + a.a34*b.a44 + a.a35*b.a54;
        c.a35 = a.a31*b.a15 + a.a32*b.a25 + a.a33*b.a35 + a.a34*b.a45 + a.a35*b.a55;
        c.a41 = a.a41*b.a11 + a.a42*b.a21 + a.a43*b.a31 + a.a44*b.a41 + a.a45*b.a51;
        c.a42 = a.a41*b.a12 + a.a42*b.a22 + a.a43*b.a32 + a.a44*b.a42 + a.a45*b.a52;
        c.a43 = a.a41*b.a13 + a.a42*b.a23 + a.a43*b.a33 + a.a44*b.a43 + a.a45*b.a53;
        c.a44 = a.a41*b.a14 + a.a42*b.a24 + a.a43*b.a34 + a.a44*b.a44 + a.a45*b.a54;
        c.a45 = a.a41*b.a15 + a.a42*b.a25 + a.a43*b.a35 + a.a44*b.a45 + a.a45*b.a55;
        c.a51 = a.a51*b.a11 + a.a52*b.a21 + a.a53*b.a31 + a.a54*b.a41 + a.a55*b.a51;
        c.a52 = a.a51*b.a12 + a.a52*b.a22 + a.a53*b.a32 + a.a54*b.a42 + a.a55*b.a52;
        c.a53 = a.a51*b.a13 + a.a52*b.a23 + a.a53*b.a33 + a.a54*b.a43 + a.a55*b.a53;
        c.a54 = a.a51*b.a14 + a.a52*b.a24 + a.a53*b.a34 + a.a54*b.a44 + a.a55*b.a54;
        c.a55 = a.a51*b.a15 + a.a52*b.a25 + a.a53*b.a35 + a.a54*b.a45 + a.a55*b.a55;
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = &alpha; * a * b <br>
     * <br>
     * c<sub>ij</sub> = &alpha; &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param alpha Scaling factor.
     * @param a (Input) The left matrix in the multiplication operation. Not modified.
     * @param b (Input) The right matrix in the multiplication operation. Not modified.
     * @param c (Output) Where the results of the operation are stored. Modified.
     */
    public static void mult( float alpha , FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 = alpha*(a.a11*b.a11 + a.a12*b.a21 + a.a13*b.a31 + a.a14*b.a41 + a.a15*b.a51);
        c.a12 = alpha*(a.a11*b.a12 + a.a12*b.a22 + a.a13*b.a32 + a.a14*b.a42 + a.a15*b.a52);
        c.a13 = alpha*(a.a11*b.a13 + a.a12*b.a23 + a.a13*b.a33 + a.a14*b.a43 + a.a15*b.a53);
        c.a14 = alpha*(a.a11*b.a14 + a.a12*b.a24 + a.a13*b.a34 + a.a14*b.a44 + a.a15*b.a54);
        c.a15 = alpha*(a.a11*b.a15 + a.a12*b.a25 + a.a13*b.a35 + a.a14*b.a45 + a.a15*b.a55);
        c.a21 = alpha*(a.a21*b.a11 + a.a22*b.a21 + a.a23*b.a31 + a.a24*b.a41 + a.a25*b.a51);
        c.a22 = alpha*(a.a21*b.a12 + a.a22*b.a22 + a.a23*b.a32 + a.a24*b.a42 + a.a25*b.a52);
        c.a23 = alpha*(a.a21*b.a13 + a.a22*b.a23 + a.a23*b.a33 + a.a24*b.a43 + a.a25*b.a53);
        c.a24 = alpha*(a.a21*b.a14 + a.a22*b.a24 + a.a23*b.a34 + a.a24*b.a44 + a.a25*b.a54);
        c.a25 = alpha*(a.a21*b.a15 + a.a22*b.a25 + a.a23*b.a35 + a.a24*b.a45 + a.a25*b.a55);
        c.a31 = alpha*(a.a31*b.a11 + a.a32*b.a21 + a.a33*b.a31 + a.a34*b.a41 + a.a35*b.a51);
        c.a32 = alpha*(a.a31*b.a12 + a.a32*b.a22 + a.a33*b.a32 + a.a34*b.a42 + a.a35*b.a52);
        c.a33 = alpha*(a.a31*b.a13 + a.a32*b.a23 + a.a33*b.a33 + a.a34*b.a43 + a.a35*b.a53);
        c.a34 = alpha*(a.a31*b.a14 + a.a32*b.a24 + a.a33*b.a34 + a.a34*b.a44 + a.a35*b.a54);
        c.a35 = alpha*(a.a31*b.a15 + a.a32*b.a25 + a.a33*b.a35 + a.a34*b.a45 + a.a35*b.a55);
        c.a41 = alpha*(a.a41*b.a11 + a.a42*b.a21 + a.a43*b.a31 + a.a44*b.a41 + a.a45*b.a51);
        c.a42 = alpha*(a.a41*b.a12 + a.a42*b.a22 + a.a43*b.a32 + a.a44*b.a42 + a.a45*b.a52);
        c.a43 = alpha*(a.a41*b.a13 + a.a42*b.a23 + a.a43*b.a33 + a.a44*b.a43 + a.a45*b.a53);
        c.a44 = alpha*(a.a41*b.a14 + a.a42*b.a24 + a.a43*b.a34 + a.a44*b.a44 + a.a45*b.a54);
        c.a45 = alpha*(a.a41*b.a15 + a.a42*b.a25 + a.a43*b.a35 + a.a44*b.a45 + a.a45*b.a55);
        c.a51 = alpha*(a.a51*b.a11 + a.a52*b.a21 + a.a53*b.a31 + a.a54*b.a41 + a.a55*b.a51);
        c.a52 = alpha*(a.a51*b.a12 + a.a52*b.a22 + a.a53*b.a32 + a.a54*b.a42 + a.a55*b.a52);
        c.a53 = alpha*(a.a51*b.a13 + a.a52*b.a23 + a.a53*b.a33 + a.a54*b.a43 + a.a55*b.a53);
        c.a54 = alpha*(a.a51*b.a14 + a.a52*b.a24 + a.a53*b.a34 + a.a54*b.a44 + a.a55*b.a54);
        c.a55 = alpha*(a.a51*b.a15 + a.a52*b.a25 + a.a53*b.a35 + a.a54*b.a45 + a.a55*b.a55);
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = a<sup>T</sup> * b <br>
     * <br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param a (Input) The left matrix in the multiplication operation. Not modified.
     * @param b (Input) The right matrix in the multiplication operation. Not modified.
     * @param c (Output) Where the results of the operation are stored. Modified.
     */
    public static void multTransA( FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 = a.a11*b.a11 + a.a21*b.a21 + a.a31*b.a31 + a.a41*b.a41 + a.a51*b.a51;
        c.a12 = a.a11*b.a12 + a.a21*b.a22 + a.a31*b.a32 + a.a41*b.a42 + a.a51*b.a52;
        c.a13 = a.a11*b.a13 + a.a21*b.a23 + a.a31*b.a33 + a.a41*b.a43 + a.a51*b.a53;
        c.a14 = a.a11*b.a14 + a.a21*b.a24 + a.a31*b.a34 + a.a41*b.a44 + a.a51*b.a54;
        c.a15 = a.a11*b.a15 + a.a21*b.a25 + a.a31*b.a35 + a.a41*b.a45 + a.a51*b.a55;
        c.a21 = a.a12*b.a11 + a.a22*b.a21 + a.a32*b.a31 + a.a42*b.a41 + a.a52*b.a51;
        c.a22 = a.a12*b.a12 + a.a22*b.a22 + a.a32*b.a32 + a.a42*b.a42 + a.a52*b.a52;
        c.a23 = a.a12*b.a13 + a.a22*b.a23 + a.a32*b.a33 + a.a42*b.a43 + a.a52*b.a53;
        c.a24 = a.a12*b.a14 + a.a22*b.a24 + a.a32*b.a34 + a.a42*b.a44 + a.a52*b.a54;
        c.a25 = a.a12*b.a15 + a.a22*b.a25 + a.a32*b.a35 + a.a42*b.a45 + a.a52*b.a55;
        c.a31 = a.a13*b.a11 + a.a23*b.a21 + a.a33*b.a31 + a.a43*b.a41 + a.a53*b.a51;
        c.a32 = a.a13*b.a12 + a.a23*b.a22 + a.a33*b.a32 + a.a43*b.a42 + a.a53*b.a52;
        c.a33 = a.a13*b.a13 + a.a23*b.a23 + a.a33*b.a33 + a.a43*b.a43 + a.a53*b.a53;
        c.a34 = a.a13*b.a14 + a.a23*b.a24 + a.a33*b.a34 + a.a43*b.a44 + a.a53*b.a54;
        c.a35 = a.a13*b.a15 + a.a23*b.a25 + a.a33*b.a35 + a.a43*b.a45 + a.a53*b.a55;
        c.a41 = a.a14*b.a11 + a.a24*b.a21 + a.a34*b.a31 + a.a44*b.a41 + a.a54*b.a51;
        c.a42 = a.a14*b.a12 + a.a24*b.a22 + a.a34*b.a32 + a.a44*b.a42 + a.a54*b.a52;
        c.a43 = a.a14*b.a13 + a.a24*b.a23 + a.a34*b.a33 + a.a44*b.a43 + a.a54*b.a53;
        c.a44 = a.a14*b.a14 + a.a24*b.a24 + a.a34*b.a34 + a.a44*b.a44 + a.a54*b.a54;
        c.a45 = a.a14*b.a15 + a.a24*b.a25 + a.a34*b.a35 + a.a44*b.a45 + a.a54*b.a55;
        c.a51 = a.a15*b.a11 + a.a25*b.a21 + a.a35*b.a31 + a.a45*b.a41 + a.a55*b.a51;
        c.a52 = a.a15*b.a12 + a.a25*b.a22 + a.a35*b.a32 + a.a45*b.a42 + a.a55*b.a52;
        c.a53 = a.a15*b.a13 + a.a25*b.a23 + a.a35*b.a33 + a.a45*b.a43 + a.a55*b.a53;
        c.a54 = a.a15*b.a14 + a.a25*b.a24 + a.a35*b.a34 + a.a45*b.a44 + a.a55*b.a54;
        c.a55 = a.a15*b.a15 + a.a25*b.a25 + a.a35*b.a35 + a.a45*b.a45 + a.a55*b.a55;
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = &alpha; * a<sup>T</sup> * b <br>
     * <br>
     * c<sub>ij</sub> = &alpha; * &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param alpha Scaling factor.
     * @param a (Input) The left matrix in the multiplication operation. Not modified.
     * @param b (Input) The right matrix in the multiplication operation. Not modified.
     * @param c (Output) Where the results of the operation are stored. Modified.
     */
    public static void multTransA( float alpha , FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 = alpha*(a.a11*b.a11 + a.a21*b.a21 + a.a31*b.a31 + a.a41*b.a41 + a.a51*b.a51);
        c.a12 = alpha*(a.a11*b.a12 + a.a21*b.a22 + a.a31*b.a32 + a.a41*b.a42 + a.a51*b.a52);
        c.a13 = alpha*(a.a11*b.a13 + a.a21*b.a23 + a.a31*b.a33 + a.a41*b.a43 + a.a51*b.a53);
        c.a14 = alpha*(a.a11*b.a14 + a.a21*b.a24 + a.a31*b.a34 + a.a41*b.a44 + a.a51*b.a54);
        c.a15 = alpha*(a.a11*b.a15 + a.a21*b.a25 + a.a31*b.a35 + a.a41*b.a45 + a.a51*b.a55);
        c.a21 = alpha*(a.a12*b.a11 + a.a22*b.a21 + a.a32*b.a31 + a.a42*b.a41 + a.a52*b.a51);
        c.a22 = alpha*(a.a12*b.a12 + a.a22*b.a22 + a.a32*b.a32 + a.a42*b.a42 + a.a52*b.a52);
        c.a23 = alpha*(a.a12*b.a13 + a.a22*b.a23 + a.a32*b.a33 + a.a42*b.a43 + a.a52*b.a53);
        c.a24 = alpha*(a.a12*b.a14 + a.a22*b.a24 + a.a32*b.a34 + a.a42*b.a44 + a.a52*b.a54);
        c.a25 = alpha*(a.a12*b.a15 + a.a22*b.a25 + a.a32*b.a35 + a.a42*b.a45 + a.a52*b.a55);
        c.a31 = alpha*(a.a13*b.a11 + a.a23*b.a21 + a.a33*b.a31 + a.a43*b.a41 + a.a53*b.a51);
        c.a32 = alpha*(a.a13*b.a12 + a.a23*b.a22 + a.a33*b.a32 + a.a43*b.a42 + a.a53*b.a52);
        c.a33 = alpha*(a.a13*b.a13 + a.a23*b.a23 + a.a33*b.a33 + a.a43*b.a43 + a.a53*b.a53);
        c.a34 = alpha*(a.a13*b.a14 + a.a23*b.a24 + a.a33*b.a34 + a.a43*b.a44 + a.a53*b.a54);
        c.a35 = alpha*(a.a13*b.a15 + a.a23*b.a25 + a.a33*b.a35 + a.a43*b.a45 + a.a53*b.a55);
        c.a41 = alpha*(a.a14*b.a11 + a.a24*b.a21 + a.a34*b.a31 + a.a44*b.a41 + a.a54*b.a51);
        c.a42 = alpha*(a.a14*b.a12 + a.a24*b.a22 + a.a34*b.a32 + a.a44*b.a42 + a.a54*b.a52);
        c.a43 = alpha*(a.a14*b.a13 + a.a24*b.a23 + a.a34*b.a33 + a.a44*b.a43 + a.a54*b.a53);
        c.a44 = alpha*(a.a14*b.a14 + a.a24*b.a24 + a.a34*b.a34 + a.a44*b.a44 + a.a54*b.a54);
        c.a45 = alpha*(a.a14*b.a15 + a.a24*b.a25 + a.a34*b.a35 + a.a44*b.a45 + a.a54*b.a55);
        c.a51 = alpha*(a.a15*b.a11 + a.a25*b.a21 + a.a35*b.a31 + a.a45*b.a41 + a.a55*b.a51);
        c.a52 = alpha*(a.a15*b.a12 + a.a25*b.a22 + a.a35*b.a32 + a.a45*b.a42 + a.a55*b.a52);
        c.a53 = alpha*(a.a15*b.a13 + a.a25*b.a23 + a.a35*b.a33 + a.a45*b.a43 + a.a55*b.a53);
        c.a54 = alpha*(a.a15*b.a14 + a.a25*b.a24 + a.a35*b.a34 + a.a45*b.a44 + a.a55*b.a54);
        c.a55 = alpha*(a.a15*b.a15 + a.a25*b.a25 + a.a35*b.a35 + a.a45*b.a45 + a.a55*b.a55);
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = a<sup>T</sup> * b<sup>T</sup><br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a (Input) The left matrix in the multiplication operation. Not modified.
     * @param b (Input) The right matrix in the multiplication operation. Not modified.
     * @param c (Output) Where the results of the operation are stored. Modified.
     */
    public static void multTransAB( FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 = a.a11*b.a11 + a.a21*b.a12 + a.a31*b.a13 + a.a41*b.a14 + a.a51*b.a15;
        c.a12 = a.a11*b.a21 + a.a21*b.a22 + a.a31*b.a23 + a.a41*b.a24 + a.a51*b.a25;
        c.a13 = a.a11*b.a31 + a.a21*b.a32 + a.a31*b.a33 + a.a41*b.a34 + a.a51*b.a35;
        c.a14 = a.a11*b.a41 + a.a21*b.a42 + a.a31*b.a43 + a.a41*b.a44 + a.a51*b.a45;
        c.a15 = a.a11*b.a51 + a.a21*b.a52 + a.a31*b.a53 + a.a41*b.a54 + a.a51*b.a55;
        c.a21 = a.a12*b.a11 + a.a22*b.a12 + a.a32*b.a13 + a.a42*b.a14 + a.a52*b.a15;
        c.a22 = a.a12*b.a21 + a.a22*b.a22 + a.a32*b.a23 + a.a42*b.a24 + a.a52*b.a25;
        c.a23 = a.a12*b.a31 + a.a22*b.a32 + a.a32*b.a33 + a.a42*b.a34 + a.a52*b.a35;
        c.a24 = a.a12*b.a41 + a.a22*b.a42 + a.a32*b.a43 + a.a42*b.a44 + a.a52*b.a45;
        c.a25 = a.a12*b.a51 + a.a22*b.a52 + a.a32*b.a53 + a.a42*b.a54 + a.a52*b.a55;
        c.a31 = a.a13*b.a11 + a.a23*b.a12 + a.a33*b.a13 + a.a43*b.a14 + a.a53*b.a15;
        c.a32 = a.a13*b.a21 + a.a23*b.a22 + a.a33*b.a23 + a.a43*b.a24 + a.a53*b.a25;
        c.a33 = a.a13*b.a31 + a.a23*b.a32 + a.a33*b.a33 + a.a43*b.a34 + a.a53*b.a35;
        c.a34 = a.a13*b.a41 + a.a23*b.a42 + a.a33*b.a43 + a.a43*b.a44 + a.a53*b.a45;
        c.a35 = a.a13*b.a51 + a.a23*b.a52 + a.a33*b.a53 + a.a43*b.a54 + a.a53*b.a55;
        c.a41 = a.a14*b.a11 + a.a24*b.a12 + a.a34*b.a13 + a.a44*b.a14 + a.a54*b.a15;
        c.a42 = a.a14*b.a21 + a.a24*b.a22 + a.a34*b.a23 + a.a44*b.a24 + a.a54*b.a25;
        c.a43 = a.a14*b.a31 + a.a24*b.a32 + a.a34*b.a33 + a.a44*b.a34 + a.a54*b.a35;
        c.a44 = a.a14*b.a41 + a.a24*b.a42 + a.a34*b.a43 + a.a44*b.a44 + a.a54*b.a45;
        c.a45 = a.a14*b.a51 + a.a24*b.a52 + a.a34*b.a53 + a.a44*b.a54 + a.a54*b.a55;
        c.a51 = a.a15*b.a11 + a.a25*b.a12 + a.a35*b.a13 + a.a45*b.a14 + a.a55*b.a15;
        c.a52 = a.a15*b.a21 + a.a25*b.a22 + a.a35*b.a23 + a.a45*b.a24 + a.a55*b.a25;
        c.a53 = a.a15*b.a31 + a.a25*b.a32 + a.a35*b.a33 + a.a45*b.a34 + a.a55*b.a35;
        c.a54 = a.a15*b.a41 + a.a25*b.a42 + a.a35*b.a43 + a.a45*b.a44 + a.a55*b.a45;
        c.a55 = a.a15*b.a51 + a.a25*b.a52 + a.a35*b.a53 + a.a45*b.a54 + a.a55*b.a55;
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = &alpha;*a<sup>T</sup> * b<sup>T</sup><br>
     * c<sub>ij</sub> = &alpha;*&sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param alpha Scaling factor.
     * @param a (Input) The left matrix in the multiplication operation. Not modified.
     * @param b (Input) The right matrix in the multiplication operation. Not modified.
     * @param c (Output) Where the results of the operation are stored. Modified.
     */
    public static void multTransAB( float alpha , FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 = alpha*(a.a11*b.a11 + a.a21*b.a12 + a.a31*b.a13 + a.a41*b.a14 + a.a51*b.a15);
        c.a12 = alpha*(a.a11*b.a21 + a.a21*b.a22 + a.a31*b.a23 + a.a41*b.a24 + a.a51*b.a25);
        c.a13 = alpha*(a.a11*b.a31 + a.a21*b.a32 + a.a31*b.a33 + a.a41*b.a34 + a.a51*b.a35);
        c.a14 = alpha*(a.a11*b.a41 + a.a21*b.a42 + a.a31*b.a43 + a.a41*b.a44 + a.a51*b.a45);
        c.a15 = alpha*(a.a11*b.a51 + a.a21*b.a52 + a.a31*b.a53 + a.a41*b.a54 + a.a51*b.a55);
        c.a21 = alpha*(a.a12*b.a11 + a.a22*b.a12 + a.a32*b.a13 + a.a42*b.a14 + a.a52*b.a15);
        c.a22 = alpha*(a.a12*b.a21 + a.a22*b.a22 + a.a32*b.a23 + a.a42*b.a24 + a.a52*b.a25);
        c.a23 = alpha*(a.a12*b.a31 + a.a22*b.a32 + a.a32*b.a33 + a.a42*b.a34 + a.a52*b.a35);
        c.a24 = alpha*(a.a12*b.a41 + a.a22*b.a42 + a.a32*b.a43 + a.a42*b.a44 + a.a52*b.a45);
        c.a25 = alpha*(a.a12*b.a51 + a.a22*b.a52 + a.a32*b.a53 + a.a42*b.a54 + a.a52*b.a55);
        c.a31 = alpha*(a.a13*b.a11 + a.a23*b.a12 + a.a33*b.a13 + a.a43*b.a14 + a.a53*b.a15);
        c.a32 = alpha*(a.a13*b.a21 + a.a23*b.a22 + a.a33*b.a23 + a.a43*b.a24 + a.a53*b.a25);
        c.a33 = alpha*(a.a13*b.a31 + a.a23*b.a32 + a.a33*b.a33 + a.a43*b.a34 + a.a53*b.a35);
        c.a34 = alpha*(a.a13*b.a41 + a.a23*b.a42 + a.a33*b.a43 + a.a43*b.a44 + a.a53*b.a45);
        c.a35 = alpha*(a.a13*b.a51 + a.a23*b.a52 + a.a33*b.a53 + a.a43*b.a54 + a.a53*b.a55);
        c.a41 = alpha*(a.a14*b.a11 + a.a24*b.a12 + a.a34*b.a13 + a.a44*b.a14 + a.a54*b.a15);
        c.a42 = alpha*(a.a14*b.a21 + a.a24*b.a22 + a.a34*b.a23 + a.a44*b.a24 + a.a54*b.a25);
        c.a43 = alpha*(a.a14*b.a31 + a.a24*b.a32 + a.a34*b.a33 + a.a44*b.a34 + a.a54*b.a35);
        c.a44 = alpha*(a.a14*b.a41 + a.a24*b.a42 + a.a34*b.a43 + a.a44*b.a44 + a.a54*b.a45);
        c.a45 = alpha*(a.a14*b.a51 + a.a24*b.a52 + a.a34*b.a53 + a.a44*b.a54 + a.a54*b.a55);
        c.a51 = alpha*(a.a15*b.a11 + a.a25*b.a12 + a.a35*b.a13 + a.a45*b.a14 + a.a55*b.a15);
        c.a52 = alpha*(a.a15*b.a21 + a.a25*b.a22 + a.a35*b.a23 + a.a45*b.a24 + a.a55*b.a25);
        c.a53 = alpha*(a.a15*b.a31 + a.a25*b.a32 + a.a35*b.a33 + a.a45*b.a34 + a.a55*b.a35);
        c.a54 = alpha*(a.a15*b.a41 + a.a25*b.a42 + a.a35*b.a43 + a.a45*b.a44 + a.a55*b.a45);
        c.a55 = alpha*(a.a15*b.a51 + a.a25*b.a52 + a.a35*b.a53 + a.a45*b.a54 + a.a55*b.a55);
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = a * b<sup>T</sup> <br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a (Input) The left matrix in the multiplication operation. Not modified.
     * @param b (Input) The right matrix in the multiplication operation. Not modified.
     * @param c (Output) Where the results of the operation are stored. Modified.
     */
    public static void multTransB( FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 = a.a11*b.a11 + a.a12*b.a12 + a.a13*b.a13 + a.a14*b.a14 + a.a15*b.a15;
        c.a12 = a.a11*b.a21 + a.a12*b.a22 + a.a13*b.a23 + a.a14*b.a24 + a.a15*b.a25;
        c.a13 = a.a11*b.a31 + a.a12*b.a32 + a.a13*b.a33 + a.a14*b.a34 + a.a15*b.a35;
        c.a14 = a.a11*b.a41 + a.a12*b.a42 + a.a13*b.a43 + a.a14*b.a44 + a.a15*b.a45;
        c.a15 = a.a11*b.a51 + a.a12*b.a52 + a.a13*b.a53 + a.a14*b.a54 + a.a15*b.a55;
        c.a21 = a.a21*b.a11 + a.a22*b.a12 + a.a23*b.a13 + a.a24*b.a14 + a.a25*b.a15;
        c.a22 = a.a21*b.a21 + a.a22*b.a22 + a.a23*b.a23 + a.a24*b.a24 + a.a25*b.a25;
        c.a23 = a.a21*b.a31 + a.a22*b.a32 + a.a23*b.a33 + a.a24*b.a34 + a.a25*b.a35;
        c.a24 = a.a21*b.a41 + a.a22*b.a42 + a.a23*b.a43 + a.a24*b.a44 + a.a25*b.a45;
        c.a25 = a.a21*b.a51 + a.a22*b.a52 + a.a23*b.a53 + a.a24*b.a54 + a.a25*b.a55;
        c.a31 = a.a31*b.a11 + a.a32*b.a12 + a.a33*b.a13 + a.a34*b.a14 + a.a35*b.a15;
        c.a32 = a.a31*b.a21 + a.a32*b.a22 + a.a33*b.a23 + a.a34*b.a24 + a.a35*b.a25;
        c.a33 = a.a31*b.a31 + a.a32*b.a32 + a.a33*b.a33 + a.a34*b.a34 + a.a35*b.a35;
        c.a34 = a.a31*b.a41 + a.a32*b.a42 + a.a33*b.a43 + a.a34*b.a44 + a.a35*b.a45;
        c.a35 = a.a31*b.a51 + a.a32*b.a52 + a.a33*b.a53 + a.a34*b.a54 + a.a35*b.a55;
        c.a41 = a.a41*b.a11 + a.a42*b.a12 + a.a43*b.a13 + a.a44*b.a14 + a.a45*b.a15;
        c.a42 = a.a41*b.a21 + a.a42*b.a22 + a.a43*b.a23 + a.a44*b.a24 + a.a45*b.a25;
        c.a43 = a.a41*b.a31 + a.a42*b.a32 + a.a43*b.a33 + a.a44*b.a34 + a.a45*b.a35;
        c.a44 = a.a41*b.a41 + a.a42*b.a42 + a.a43*b.a43 + a.a44*b.a44 + a.a45*b.a45;
        c.a45 = a.a41*b.a51 + a.a42*b.a52 + a.a43*b.a53 + a.a44*b.a54 + a.a45*b.a55;
        c.a51 = a.a51*b.a11 + a.a52*b.a12 + a.a53*b.a13 + a.a54*b.a14 + a.a55*b.a15;
        c.a52 = a.a51*b.a21 + a.a52*b.a22 + a.a53*b.a23 + a.a54*b.a24 + a.a55*b.a25;
        c.a53 = a.a51*b.a31 + a.a52*b.a32 + a.a53*b.a33 + a.a54*b.a34 + a.a55*b.a35;
        c.a54 = a.a51*b.a41 + a.a52*b.a42 + a.a53*b.a43 + a.a54*b.a44 + a.a55*b.a45;
        c.a55 = a.a51*b.a51 + a.a52*b.a52 + a.a53*b.a53 + a.a54*b.a54 + a.a55*b.a55;
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = &alpha; * a * b<sup>T</sup> <br>
     * c<sub>ij</sub> = &alpha;*&sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param alpha Scaling factor.
     * @param a (Input) The left matrix in the multiplication operation. Not modified.
     * @param b (Input) The right matrix in the multiplication operation. Not modified.
     * @param c (Output) Where the results of the operation are stored. Modified.
     */
    public static void multTransB( float alpha , FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 = alpha*(a.a11*b.a11 + a.a12*b.a12 + a.a13*b.a13 + a.a14*b.a14 + a.a15*b.a15);
        c.a12 = alpha*(a.a11*b.a21 + a.a12*b.a22 + a.a13*b.a23 + a.a14*b.a24 + a.a15*b.a25);
        c.a13 = alpha*(a.a11*b.a31 + a.a12*b.a32 + a.a13*b.a33 + a.a14*b.a34 + a.a15*b.a35);
        c.a14 = alpha*(a.a11*b.a41 + a.a12*b.a42 + a.a13*b.a43 + a.a14*b.a44 + a.a15*b.a45);
        c.a15 = alpha*(a.a11*b.a51 + a.a12*b.a52 + a.a13*b.a53 + a.a14*b.a54 + a.a15*b.a55);
        c.a21 = alpha*(a.a21*b.a11 + a.a22*b.a12 + a.a23*b.a13 + a.a24*b.a14 + a.a25*b.a15);
        c.a22 = alpha*(a.a21*b.a21 + a.a22*b.a22 + a.a23*b.a23 + a.a24*b.a24 + a.a25*b.a25);
        c.a23 = alpha*(a.a21*b.a31 + a.a22*b.a32 + a.a23*b.a33 + a.a24*b.a34 + a.a25*b.a35);
        c.a24 = alpha*(a.a21*b.a41 + a.a22*b.a42 + a.a23*b.a43 + a.a24*b.a44 + a.a25*b.a45);
        c.a25 = alpha*(a.a21*b.a51 + a.a22*b.a52 + a.a23*b.a53 + a.a24*b.a54 + a.a25*b.a55);
        c.a31 = alpha*(a.a31*b.a11 + a.a32*b.a12 + a.a33*b.a13 + a.a34*b.a14 + a.a35*b.a15);
        c.a32 = alpha*(a.a31*b.a21 + a.a32*b.a22 + a.a33*b.a23 + a.a34*b.a24 + a.a35*b.a25);
        c.a33 = alpha*(a.a31*b.a31 + a.a32*b.a32 + a.a33*b.a33 + a.a34*b.a34 + a.a35*b.a35);
        c.a34 = alpha*(a.a31*b.a41 + a.a32*b.a42 + a.a33*b.a43 + a.a34*b.a44 + a.a35*b.a45);
        c.a35 = alpha*(a.a31*b.a51 + a.a32*b.a52 + a.a33*b.a53 + a.a34*b.a54 + a.a35*b.a55);
        c.a41 = alpha*(a.a41*b.a11 + a.a42*b.a12 + a.a43*b.a13 + a.a44*b.a14 + a.a45*b.a15);
        c.a42 = alpha*(a.a41*b.a21 + a.a42*b.a22 + a.a43*b.a23 + a.a44*b.a24 + a.a45*b.a25);
        c.a43 = alpha*(a.a41*b.a31 + a.a42*b.a32 + a.a43*b.a33 + a.a44*b.a34 + a.a45*b.a35);
        c.a44 = alpha*(a.a41*b.a41 + a.a42*b.a42 + a.a43*b.a43 + a.a44*b.a44 + a.a45*b.a45);
        c.a45 = alpha*(a.a41*b.a51 + a.a42*b.a52 + a.a43*b.a53 + a.a44*b.a54 + a.a45*b.a55);
        c.a51 = alpha*(a.a51*b.a11 + a.a52*b.a12 + a.a53*b.a13 + a.a54*b.a14 + a.a55*b.a15);
        c.a52 = alpha*(a.a51*b.a21 + a.a52*b.a22 + a.a53*b.a23 + a.a54*b.a24 + a.a55*b.a25);
        c.a53 = alpha*(a.a51*b.a31 + a.a52*b.a32 + a.a53*b.a33 + a.a54*b.a34 + a.a55*b.a35);
        c.a54 = alpha*(a.a51*b.a41 + a.a52*b.a42 + a.a53*b.a43 + a.a54*b.a44 + a.a55*b.a45);
        c.a55 = alpha*(a.a51*b.a51 + a.a52*b.a52 + a.a53*b.a53 + a.a54*b.a54 + a.a55*b.a55);
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c += a * b <br>
     * <br>
     * c<sub>ij</sub> += &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c (Output) Where the results of the operation are stored. Modified.
     */
    public static void multAdd( FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 += a.a11*b.a11 + a.a12*b.a21 + a.a13*b.a31 + a.a14*b.a41 + a.a15*b.a51;
        c.a12 += a.a11*b.a12 + a.a12*b.a22 + a.a13*b.a32 + a.a14*b.a42 + a.a15*b.a52;
        c.a13 += a.a11*b.a13 + a.a12*b.a23 + a.a13*b.a33 + a.a14*b.a43 + a.a15*b.a53;
        c.a14 += a.a11*b.a14 + a.a12*b.a24 + a.a13*b.a34 + a.a14*b.a44 + a.a15*b.a54;
        c.a15 += a.a11*b.a15 + a.a12*b.a25 + a.a13*b.a35 + a.a14*b.a45 + a.a15*b.a55;
        c.a21 += a.a21*b.a11 + a.a22*b.a21 + a.a23*b.a31 + a.a24*b.a41 + a.a25*b.a51;
        c.a22 += a.a21*b.a12 + a.a22*b.a22 + a.a23*b.a32 + a.a24*b.a42 + a.a25*b.a52;
        c.a23 += a.a21*b.a13 + a.a22*b.a23 + a.a23*b.a33 + a.a24*b.a43 + a.a25*b.a53;
        c.a24 += a.a21*b.a14 + a.a22*b.a24 + a.a23*b.a34 + a.a24*b.a44 + a.a25*b.a54;
        c.a25 += a.a21*b.a15 + a.a22*b.a25 + a.a23*b.a35 + a.a24*b.a45 + a.a25*b.a55;
        c.a31 += a.a31*b.a11 + a.a32*b.a21 + a.a33*b.a31 + a.a34*b.a41 + a.a35*b.a51;
        c.a32 += a.a31*b.a12 + a.a32*b.a22 + a.a33*b.a32 + a.a34*b.a42 + a.a35*b.a52;
        c.a33 += a.a31*b.a13 + a.a32*b.a23 + a.a33*b.a33 + a.a34*b.a43 + a.a35*b.a53;
        c.a34 += a.a31*b.a14 + a.a32*b.a24 + a.a33*b.a34 + a.a34*b.a44 + a.a35*b.a54;
        c.a35 += a.a31*b.a15 + a.a32*b.a25 + a.a33*b.a35 + a.a34*b.a45 + a.a35*b.a55;
        c.a41 += a.a41*b.a11 + a.a42*b.a21 + a.a43*b.a31 + a.a44*b.a41 + a.a45*b.a51;
        c.a42 += a.a41*b.a12 + a.a42*b.a22 + a.a43*b.a32 + a.a44*b.a42 + a.a45*b.a52;
        c.a43 += a.a41*b.a13 + a.a42*b.a23 + a.a43*b.a33 + a.a44*b.a43 + a.a45*b.a53;
        c.a44 += a.a41*b.a14 + a.a42*b.a24 + a.a43*b.a34 + a.a44*b.a44 + a.a45*b.a54;
        c.a45 += a.a41*b.a15 + a.a42*b.a25 + a.a43*b.a35 + a.a44*b.a45 + a.a45*b.a55;
        c.a51 += a.a51*b.a11 + a.a52*b.a21 + a.a53*b.a31 + a.a54*b.a41 + a.a55*b.a51;
        c.a52 += a.a51*b.a12 + a.a52*b.a22 + a.a53*b.a32 + a.a54*b.a42 + a.a55*b.a52;
        c.a53 += a.a51*b.a13 + a.a52*b.a23 + a.a53*b.a33 + a.a54*b.a43 + a.a55*b.a53;
        c.a54 += a.a51*b.a14 + a.a52*b.a24 + a.a53*b.a34 + a.a54*b.a44 + a.a55*b.a54;
        c.a55 += a.a51*b.a15 + a.a52*b.a25 + a.a53*b.a35 + a.a54*b.a45 + a.a55*b.a55;
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c += &alpha; * a * b <br>
     * <br>
     * c<sub>ij</sub> += &alpha; &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param alpha Scaling factor.
     * @param a (Input) The left matrix in the multiplication operation. Not modified.
     * @param b (Input) The right matrix in the multiplication operation. Not modified.
     * @param c (Output) Where the results of the operation are stored. Modified.
     */
    public static void multAdd( float alpha , FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 += alpha*(a.a11*b.a11 + a.a12*b.a21 + a.a13*b.a31 + a.a14*b.a41 + a.a15*b.a51);
        c.a12 += alpha*(a.a11*b.a12 + a.a12*b.a22 + a.a13*b.a32 + a.a14*b.a42 + a.a15*b.a52);
        c.a13 += alpha*(a.a11*b.a13 + a.a12*b.a23 + a.a13*b.a33 + a.a14*b.a43 + a.a15*b.a53);
        c.a14 += alpha*(a.a11*b.a14 + a.a12*b.a24 + a.a13*b.a34 + a.a14*b.a44 + a.a15*b.a54);
        c.a15 += alpha*(a.a11*b.a15 + a.a12*b.a25 + a.a13*b.a35 + a.a14*b.a45 + a.a15*b.a55);
        c.a21 += alpha*(a.a21*b.a11 + a.a22*b.a21 + a.a23*b.a31 + a.a24*b.a41 + a.a25*b.a51);
        c.a22 += alpha*(a.a21*b.a12 + a.a22*b.a22 + a.a23*b.a32 + a.a24*b.a42 + a.a25*b.a52);
        c.a23 += alpha*(a.a21*b.a13 + a.a22*b.a23 + a.a23*b.a33 + a.a24*b.a43 + a.a25*b.a53);
        c.a24 += alpha*(a.a21*b.a14 + a.a22*b.a24 + a.a23*b.a34 + a.a24*b.a44 + a.a25*b.a54);
        c.a25 += alpha*(a.a21*b.a15 + a.a22*b.a25 + a.a23*b.a35 + a.a24*b.a45 + a.a25*b.a55);
        c.a31 += alpha*(a.a31*b.a11 + a.a32*b.a21 + a.a33*b.a31 + a.a34*b.a41 + a.a35*b.a51);
        c.a32 += alpha*(a.a31*b.a12 + a.a32*b.a22 + a.a33*b.a32 + a.a34*b.a42 + a.a35*b.a52);
        c.a33 += alpha*(a.a31*b.a13 + a.a32*b.a23 + a.a33*b.a33 + a.a34*b.a43 + a.a35*b.a53);
        c.a34 += alpha*(a.a31*b.a14 + a.a32*b.a24 + a.a33*b.a34 + a.a34*b.a44 + a.a35*b.a54);
        c.a35 += alpha*(a.a31*b.a15 + a.a32*b.a25 + a.a33*b.a35 + a.a34*b.a45 + a.a35*b.a55);
        c.a41 += alpha*(a.a41*b.a11 + a.a42*b.a21 + a.a43*b.a31 + a.a44*b.a41 + a.a45*b.a51);
        c.a42 += alpha*(a.a41*b.a12 + a.a42*b.a22 + a.a43*b.a32 + a.a44*b.a42 + a.a45*b.a52);
        c.a43 += alpha*(a.a41*b.a13 + a.a42*b.a23 + a.a43*b.a33 + a.a44*b.a43 + a.a45*b.a53);
        c.a44 += alpha*(a.a41*b.a14 + a.a42*b.a24 + a.a43*b.a34 + a.a44*b.a44 + a.a45*b.a54);
        c.a45 += alpha*(a.a41*b.a15 + a.a42*b.a25 + a.a43*b.a35 + a.a44*b.a45 + a.a45*b.a55);
        c.a51 += alpha*(a.a51*b.a11 + a.a52*b.a21 + a.a53*b.a31 + a.a54*b.a41 + a.a55*b.a51);
        c.a52 += alpha*(a.a51*b.a12 + a.a52*b.a22 + a.a53*b.a32 + a.a54*b.a42 + a.a55*b.a52);
        c.a53 += alpha*(a.a51*b.a13 + a.a52*b.a23 + a.a53*b.a33 + a.a54*b.a43 + a.a55*b.a53);
        c.a54 += alpha*(a.a51*b.a14 + a.a52*b.a24 + a.a53*b.a34 + a.a54*b.a44 + a.a55*b.a54);
        c.a55 += alpha*(a.a51*b.a15 + a.a52*b.a25 + a.a53*b.a35 + a.a54*b.a45 + a.a55*b.a55);
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c += a<sup>T</sup> * b <br>
     * <br>
     * c<sub>ij</sub> += &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param a (Input) The left matrix in the multiplication operation. Not modified.
     * @param b (Input) The right matrix in the multiplication operation. Not modified.
     * @param c (Output) Where the results of the operation are stored. Modified.
     */
    public static void multAddTransA( FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 += a.a11*b.a11 + a.a21*b.a21 + a.a31*b.a31 + a.a41*b.a41 + a.a51*b.a51;
        c.a12 += a.a11*b.a12 + a.a21*b.a22 + a.a31*b.a32 + a.a41*b.a42 + a.a51*b.a52;
        c.a13 += a.a11*b.a13 + a.a21*b.a23 + a.a31*b.a33 + a.a41*b.a43 + a.a51*b.a53;
        c.a14 += a.a11*b.a14 + a.a21*b.a24 + a.a31*b.a34 + a.a41*b.a44 + a.a51*b.a54;
        c.a15 += a.a11*b.a15 + a.a21*b.a25 + a.a31*b.a35 + a.a41*b.a45 + a.a51*b.a55;
        c.a21 += a.a12*b.a11 + a.a22*b.a21 + a.a32*b.a31 + a.a42*b.a41 + a.a52*b.a51;
        c.a22 += a.a12*b.a12 + a.a22*b.a22 + a.a32*b.a32 + a.a42*b.a42 + a.a52*b.a52;
        c.a23 += a.a12*b.a13 + a.a22*b.a23 + a.a32*b.a33 + a.a42*b.a43 + a.a52*b.a53;
        c.a24 += a.a12*b.a14 + a.a22*b.a24 + a.a32*b.a34 + a.a42*b.a44 + a.a52*b.a54;
        c.a25 += a.a12*b.a15 + a.a22*b.a25 + a.a32*b.a35 + a.a42*b.a45 + a.a52*b.a55;
        c.a31 += a.a13*b.a11 + a.a23*b.a21 + a.a33*b.a31 + a.a43*b.a41 + a.a53*b.a51;
        c.a32 += a.a13*b.a12 + a.a23*b.a22 + a.a33*b.a32 + a.a43*b.a42 + a.a53*b.a52;
        c.a33 += a.a13*b.a13 + a.a23*b.a23 + a.a33*b.a33 + a.a43*b.a43 + a.a53*b.a53;
        c.a34 += a.a13*b.a14 + a.a23*b.a24 + a.a33*b.a34 + a.a43*b.a44 + a.a53*b.a54;
        c.a35 += a.a13*b.a15 + a.a23*b.a25 + a.a33*b.a35 + a.a43*b.a45 + a.a53*b.a55;
        c.a41 += a.a14*b.a11 + a.a24*b.a21 + a.a34*b.a31 + a.a44*b.a41 + a.a54*b.a51;
        c.a42 += a.a14*b.a12 + a.a24*b.a22 + a.a34*b.a32 + a.a44*b.a42 + a.a54*b.a52;
        c.a43 += a.a14*b.a13 + a.a24*b.a23 + a.a34*b.a33 + a.a44*b.a43 + a.a54*b.a53;
        c.a44 += a.a14*b.a14 + a.a24*b.a24 + a.a34*b.a34 + a.a44*b.a44 + a.a54*b.a54;
        c.a45 += a.a14*b.a15 + a.a24*b.a25 + a.a34*b.a35 + a.a44*b.a45 + a.a54*b.a55;
        c.a51 += a.a15*b.a11 + a.a25*b.a21 + a.a35*b.a31 + a.a45*b.a41 + a.a55*b.a51;
        c.a52 += a.a15*b.a12 + a.a25*b.a22 + a.a35*b.a32 + a.a45*b.a42 + a.a55*b.a52;
        c.a53 += a.a15*b.a13 + a.a25*b.a23 + a.a35*b.a33 + a.a45*b.a43 + a.a55*b.a53;
        c.a54 += a.a15*b.a14 + a.a25*b.a24 + a.a35*b.a34 + a.a45*b.a44 + a.a55*b.a54;
        c.a55 += a.a15*b.a15 + a.a25*b.a25 + a.a35*b.a35 + a.a45*b.a45 + a.a55*b.a55;
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c += &alpha; * a<sup>T</sup> * b <br>
     * <br>
     * c<sub>ij</sub> += &alpha; * &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param alpha Scaling factor.
     * @param a (Input) The left matrix in the multiplication operation. Not modified.
     * @param b (Input) The right matrix in the multiplication operation. Not modified.
     * @param c (Output) Where the results of the operation are stored. Modified.
     */
    public static void multAddTransA( float alpha , FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 += alpha*(a.a11*b.a11 + a.a21*b.a21 + a.a31*b.a31 + a.a41*b.a41 + a.a51*b.a51);
        c.a12 += alpha*(a.a11*b.a12 + a.a21*b.a22 + a.a31*b.a32 + a.a41*b.a42 + a.a51*b.a52);
        c.a13 += alpha*(a.a11*b.a13 + a.a21*b.a23 + a.a31*b.a33 + a.a41*b.a43 + a.a51*b.a53);
        c.a14 += alpha*(a.a11*b.a14 + a.a21*b.a24 + a.a31*b.a34 + a.a41*b.a44 + a.a51*b.a54);
        c.a15 += alpha*(a.a11*b.a15 + a.a21*b.a25 + a.a31*b.a35 + a.a41*b.a45 + a.a51*b.a55);
        c.a21 += alpha*(a.a12*b.a11 + a.a22*b.a21 + a.a32*b.a31 + a.a42*b.a41 + a.a52*b.a51);
        c.a22 += alpha*(a.a12*b.a12 + a.a22*b.a22 + a.a32*b.a32 + a.a42*b.a42 + a.a52*b.a52);
        c.a23 += alpha*(a.a12*b.a13 + a.a22*b.a23 + a.a32*b.a33 + a.a42*b.a43 + a.a52*b.a53);
        c.a24 += alpha*(a.a12*b.a14 + a.a22*b.a24 + a.a32*b.a34 + a.a42*b.a44 + a.a52*b.a54);
        c.a25 += alpha*(a.a12*b.a15 + a.a22*b.a25 + a.a32*b.a35 + a.a42*b.a45 + a.a52*b.a55);
        c.a31 += alpha*(a.a13*b.a11 + a.a23*b.a21 + a.a33*b.a31 + a.a43*b.a41 + a.a53*b.a51);
        c.a32 += alpha*(a.a13*b.a12 + a.a23*b.a22 + a.a33*b.a32 + a.a43*b.a42 + a.a53*b.a52);
        c.a33 += alpha*(a.a13*b.a13 + a.a23*b.a23 + a.a33*b.a33 + a.a43*b.a43 + a.a53*b.a53);
        c.a34 += alpha*(a.a13*b.a14 + a.a23*b.a24 + a.a33*b.a34 + a.a43*b.a44 + a.a53*b.a54);
        c.a35 += alpha*(a.a13*b.a15 + a.a23*b.a25 + a.a33*b.a35 + a.a43*b.a45 + a.a53*b.a55);
        c.a41 += alpha*(a.a14*b.a11 + a.a24*b.a21 + a.a34*b.a31 + a.a44*b.a41 + a.a54*b.a51);
        c.a42 += alpha*(a.a14*b.a12 + a.a24*b.a22 + a.a34*b.a32 + a.a44*b.a42 + a.a54*b.a52);
        c.a43 += alpha*(a.a14*b.a13 + a.a24*b.a23 + a.a34*b.a33 + a.a44*b.a43 + a.a54*b.a53);
        c.a44 += alpha*(a.a14*b.a14 + a.a24*b.a24 + a.a34*b.a34 + a.a44*b.a44 + a.a54*b.a54);
        c.a45 += alpha*(a.a14*b.a15 + a.a24*b.a25 + a.a34*b.a35 + a.a44*b.a45 + a.a54*b.a55);
        c.a51 += alpha*(a.a15*b.a11 + a.a25*b.a21 + a.a35*b.a31 + a.a45*b.a41 + a.a55*b.a51);
        c.a52 += alpha*(a.a15*b.a12 + a.a25*b.a22 + a.a35*b.a32 + a.a45*b.a42 + a.a55*b.a52);
        c.a53 += alpha*(a.a15*b.a13 + a.a25*b.a23 + a.a35*b.a33 + a.a45*b.a43 + a.a55*b.a53);
        c.a54 += alpha*(a.a15*b.a14 + a.a25*b.a24 + a.a35*b.a34 + a.a45*b.a44 + a.a55*b.a54);
        c.a55 += alpha*(a.a15*b.a15 + a.a25*b.a25 + a.a35*b.a35 + a.a45*b.a45 + a.a55*b.a55);
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c += a<sup>T</sup> * b<sup>T</sup><br>
     * c<sub>ij</sub> += &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a (Input) The left matrix in the multiplication operation. Not modified.
     * @param b (Input) The right matrix in the multiplication operation. Not modified.
     * @param c (Output) Where the results of the operation are stored. Modified.
     */
    public static void multAddTransAB( FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 += a.a11*b.a11 + a.a21*b.a12 + a.a31*b.a13 + a.a41*b.a14 + a.a51*b.a15;
        c.a12 += a.a11*b.a21 + a.a21*b.a22 + a.a31*b.a23 + a.a41*b.a24 + a.a51*b.a25;
        c.a13 += a.a11*b.a31 + a.a21*b.a32 + a.a31*b.a33 + a.a41*b.a34 + a.a51*b.a35;
        c.a14 += a.a11*b.a41 + a.a21*b.a42 + a.a31*b.a43 + a.a41*b.a44 + a.a51*b.a45;
        c.a15 += a.a11*b.a51 + a.a21*b.a52 + a.a31*b.a53 + a.a41*b.a54 + a.a51*b.a55;
        c.a21 += a.a12*b.a11 + a.a22*b.a12 + a.a32*b.a13 + a.a42*b.a14 + a.a52*b.a15;
        c.a22 += a.a12*b.a21 + a.a22*b.a22 + a.a32*b.a23 + a.a42*b.a24 + a.a52*b.a25;
        c.a23 += a.a12*b.a31 + a.a22*b.a32 + a.a32*b.a33 + a.a42*b.a34 + a.a52*b.a35;
        c.a24 += a.a12*b.a41 + a.a22*b.a42 + a.a32*b.a43 + a.a42*b.a44 + a.a52*b.a45;
        c.a25 += a.a12*b.a51 + a.a22*b.a52 + a.a32*b.a53 + a.a42*b.a54 + a.a52*b.a55;
        c.a31 += a.a13*b.a11 + a.a23*b.a12 + a.a33*b.a13 + a.a43*b.a14 + a.a53*b.a15;
        c.a32 += a.a13*b.a21 + a.a23*b.a22 + a.a33*b.a23 + a.a43*b.a24 + a.a53*b.a25;
        c.a33 += a.a13*b.a31 + a.a23*b.a32 + a.a33*b.a33 + a.a43*b.a34 + a.a53*b.a35;
        c.a34 += a.a13*b.a41 + a.a23*b.a42 + a.a33*b.a43 + a.a43*b.a44 + a.a53*b.a45;
        c.a35 += a.a13*b.a51 + a.a23*b.a52 + a.a33*b.a53 + a.a43*b.a54 + a.a53*b.a55;
        c.a41 += a.a14*b.a11 + a.a24*b.a12 + a.a34*b.a13 + a.a44*b.a14 + a.a54*b.a15;
        c.a42 += a.a14*b.a21 + a.a24*b.a22 + a.a34*b.a23 + a.a44*b.a24 + a.a54*b.a25;
        c.a43 += a.a14*b.a31 + a.a24*b.a32 + a.a34*b.a33 + a.a44*b.a34 + a.a54*b.a35;
        c.a44 += a.a14*b.a41 + a.a24*b.a42 + a.a34*b.a43 + a.a44*b.a44 + a.a54*b.a45;
        c.a45 += a.a14*b.a51 + a.a24*b.a52 + a.a34*b.a53 + a.a44*b.a54 + a.a54*b.a55;
        c.a51 += a.a15*b.a11 + a.a25*b.a12 + a.a35*b.a13 + a.a45*b.a14 + a.a55*b.a15;
        c.a52 += a.a15*b.a21 + a.a25*b.a22 + a.a35*b.a23 + a.a45*b.a24 + a.a55*b.a25;
        c.a53 += a.a15*b.a31 + a.a25*b.a32 + a.a35*b.a33 + a.a45*b.a34 + a.a55*b.a35;
        c.a54 += a.a15*b.a41 + a.a25*b.a42 + a.a35*b.a43 + a.a45*b.a44 + a.a55*b.a45;
        c.a55 += a.a15*b.a51 + a.a25*b.a52 + a.a35*b.a53 + a.a45*b.a54 + a.a55*b.a55;
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c += &alpha;*a<sup>T</sup> * b<sup>T</sup><br>
     * c<sub>ij</sub> += &alpha;*&sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param alpha Scaling factor.
     * @param a (Input) The left matrix in the multiplication operation. Not modified.
     * @param b (Input) The right matrix in the multiplication operation. Not modified.
     * @param c (Output) Where the results of the operation are stored. Modified.
     */
    public static void multAddTransAB( float alpha , FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 += alpha*(a.a11*b.a11 + a.a21*b.a12 + a.a31*b.a13 + a.a41*b.a14 + a.a51*b.a15);
        c.a12 += alpha*(a.a11*b.a21 + a.a21*b.a22 + a.a31*b.a23 + a.a41*b.a24 + a.a51*b.a25);
        c.a13 += alpha*(a.a11*b.a31 + a.a21*b.a32 + a.a31*b.a33 + a.a41*b.a34 + a.a51*b.a35);
        c.a14 += alpha*(a.a11*b.a41 + a.a21*b.a42 + a.a31*b.a43 + a.a41*b.a44 + a.a51*b.a45);
        c.a15 += alpha*(a.a11*b.a51 + a.a21*b.a52 + a.a31*b.a53 + a.a41*b.a54 + a.a51*b.a55);
        c.a21 += alpha*(a.a12*b.a11 + a.a22*b.a12 + a.a32*b.a13 + a.a42*b.a14 + a.a52*b.a15);
        c.a22 += alpha*(a.a12*b.a21 + a.a22*b.a22 + a.a32*b.a23 + a.a42*b.a24 + a.a52*b.a25);
        c.a23 += alpha*(a.a12*b.a31 + a.a22*b.a32 + a.a32*b.a33 + a.a42*b.a34 + a.a52*b.a35);
        c.a24 += alpha*(a.a12*b.a41 + a.a22*b.a42 + a.a32*b.a43 + a.a42*b.a44 + a.a52*b.a45);
        c.a25 += alpha*(a.a12*b.a51 + a.a22*b.a52 + a.a32*b.a53 + a.a42*b.a54 + a.a52*b.a55);
        c.a31 += alpha*(a.a13*b.a11 + a.a23*b.a12 + a.a33*b.a13 + a.a43*b.a14 + a.a53*b.a15);
        c.a32 += alpha*(a.a13*b.a21 + a.a23*b.a22 + a.a33*b.a23 + a.a43*b.a24 + a.a53*b.a25);
        c.a33 += alpha*(a.a13*b.a31 + a.a23*b.a32 + a.a33*b.a33 + a.a43*b.a34 + a.a53*b.a35);
        c.a34 += alpha*(a.a13*b.a41 + a.a23*b.a42 + a.a33*b.a43 + a.a43*b.a44 + a.a53*b.a45);
        c.a35 += alpha*(a.a13*b.a51 + a.a23*b.a52 + a.a33*b.a53 + a.a43*b.a54 + a.a53*b.a55);
        c.a41 += alpha*(a.a14*b.a11 + a.a24*b.a12 + a.a34*b.a13 + a.a44*b.a14 + a.a54*b.a15);
        c.a42 += alpha*(a.a14*b.a21 + a.a24*b.a22 + a.a34*b.a23 + a.a44*b.a24 + a.a54*b.a25);
        c.a43 += alpha*(a.a14*b.a31 + a.a24*b.a32 + a.a34*b.a33 + a.a44*b.a34 + a.a54*b.a35);
        c.a44 += alpha*(a.a14*b.a41 + a.a24*b.a42 + a.a34*b.a43 + a.a44*b.a44 + a.a54*b.a45);
        c.a45 += alpha*(a.a14*b.a51 + a.a24*b.a52 + a.a34*b.a53 + a.a44*b.a54 + a.a54*b.a55);
        c.a51 += alpha*(a.a15*b.a11 + a.a25*b.a12 + a.a35*b.a13 + a.a45*b.a14 + a.a55*b.a15);
        c.a52 += alpha*(a.a15*b.a21 + a.a25*b.a22 + a.a35*b.a23 + a.a45*b.a24 + a.a55*b.a25);
        c.a53 += alpha*(a.a15*b.a31 + a.a25*b.a32 + a.a35*b.a33 + a.a45*b.a34 + a.a55*b.a35);
        c.a54 += alpha*(a.a15*b.a41 + a.a25*b.a42 + a.a35*b.a43 + a.a45*b.a44 + a.a55*b.a45);
        c.a55 += alpha*(a.a15*b.a51 + a.a25*b.a52 + a.a35*b.a53 + a.a45*b.a54 + a.a55*b.a55);
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c += a * b<sup>T</sup> <br>
     * c<sub>ij</sub> += &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a (Input) The left matrix in the multiplication operation. Not modified.
     * @param b (Input) The right matrix in the multiplication operation. Not modified.
     * @param c (Output) Where the results of the operation are stored. Modified.
     */
    public static void multAddTransB( FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 += a.a11*b.a11 + a.a12*b.a12 + a.a13*b.a13 + a.a14*b.a14 + a.a15*b.a15;
        c.a12 += a.a11*b.a21 + a.a12*b.a22 + a.a13*b.a23 + a.a14*b.a24 + a.a15*b.a25;
        c.a13 += a.a11*b.a31 + a.a12*b.a32 + a.a13*b.a33 + a.a14*b.a34 + a.a15*b.a35;
        c.a14 += a.a11*b.a41 + a.a12*b.a42 + a.a13*b.a43 + a.a14*b.a44 + a.a15*b.a45;
        c.a15 += a.a11*b.a51 + a.a12*b.a52 + a.a13*b.a53 + a.a14*b.a54 + a.a15*b.a55;
        c.a21 += a.a21*b.a11 + a.a22*b.a12 + a.a23*b.a13 + a.a24*b.a14 + a.a25*b.a15;
        c.a22 += a.a21*b.a21 + a.a22*b.a22 + a.a23*b.a23 + a.a24*b.a24 + a.a25*b.a25;
        c.a23 += a.a21*b.a31 + a.a22*b.a32 + a.a23*b.a33 + a.a24*b.a34 + a.a25*b.a35;
        c.a24 += a.a21*b.a41 + a.a22*b.a42 + a.a23*b.a43 + a.a24*b.a44 + a.a25*b.a45;
        c.a25 += a.a21*b.a51 + a.a22*b.a52 + a.a23*b.a53 + a.a24*b.a54 + a.a25*b.a55;
        c.a31 += a.a31*b.a11 + a.a32*b.a12 + a.a33*b.a13 + a.a34*b.a14 + a.a35*b.a15;
        c.a32 += a.a31*b.a21 + a.a32*b.a22 + a.a33*b.a23 + a.a34*b.a24 + a.a35*b.a25;
        c.a33 += a.a31*b.a31 + a.a32*b.a32 + a.a33*b.a33 + a.a34*b.a34 + a.a35*b.a35;
        c.a34 += a.a31*b.a41 + a.a32*b.a42 + a.a33*b.a43 + a.a34*b.a44 + a.a35*b.a45;
        c.a35 += a.a31*b.a51 + a.a32*b.a52 + a.a33*b.a53 + a.a34*b.a54 + a.a35*b.a55;
        c.a41 += a.a41*b.a11 + a.a42*b.a12 + a.a43*b.a13 + a.a44*b.a14 + a.a45*b.a15;
        c.a42 += a.a41*b.a21 + a.a42*b.a22 + a.a43*b.a23 + a.a44*b.a24 + a.a45*b.a25;
        c.a43 += a.a41*b.a31 + a.a42*b.a32 + a.a43*b.a33 + a.a44*b.a34 + a.a45*b.a35;
        c.a44 += a.a41*b.a41 + a.a42*b.a42 + a.a43*b.a43 + a.a44*b.a44 + a.a45*b.a45;
        c.a45 += a.a41*b.a51 + a.a42*b.a52 + a.a43*b.a53 + a.a44*b.a54 + a.a45*b.a55;
        c.a51 += a.a51*b.a11 + a.a52*b.a12 + a.a53*b.a13 + a.a54*b.a14 + a.a55*b.a15;
        c.a52 += a.a51*b.a21 + a.a52*b.a22 + a.a53*b.a23 + a.a54*b.a24 + a.a55*b.a25;
        c.a53 += a.a51*b.a31 + a.a52*b.a32 + a.a53*b.a33 + a.a54*b.a34 + a.a55*b.a35;
        c.a54 += a.a51*b.a41 + a.a52*b.a42 + a.a53*b.a43 + a.a54*b.a44 + a.a55*b.a45;
        c.a55 += a.a51*b.a51 + a.a52*b.a52 + a.a53*b.a53 + a.a54*b.a54 + a.a55*b.a55;
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c += &alpha; * a * b<sup>T</sup> <br>
     * c<sub>ij</sub> += &alpha;*&sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param alpha Scaling factor.
     * @param a (Input) The left matrix in the multiplication operation. Not modified.
     * @param b (Input) The right matrix in the multiplication operation. Not modified.
     * @param c (Output) Where the results of the operation are stored. Modified.
     */
    public static void multAddTransB( float alpha , FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 += alpha*(a.a11*b.a11 + a.a12*b.a12 + a.a13*b.a13 + a.a14*b.a14 + a.a15*b.a15);
        c.a12 += alpha*(a.a11*b.a21 + a.a12*b.a22 + a.a13*b.a23 + a.a14*b.a24 + a.a15*b.a25);
        c.a13 += alpha*(a.a11*b.a31 + a.a12*b.a32 + a.a13*b.a33 + a.a14*b.a34 + a.a15*b.a35);
        c.a14 += alpha*(a.a11*b.a41 + a.a12*b.a42 + a.a13*b.a43 + a.a14*b.a44 + a.a15*b.a45);
        c.a15 += alpha*(a.a11*b.a51 + a.a12*b.a52 + a.a13*b.a53 + a.a14*b.a54 + a.a15*b.a55);
        c.a21 += alpha*(a.a21*b.a11 + a.a22*b.a12 + a.a23*b.a13 + a.a24*b.a14 + a.a25*b.a15);
        c.a22 += alpha*(a.a21*b.a21 + a.a22*b.a22 + a.a23*b.a23 + a.a24*b.a24 + a.a25*b.a25);
        c.a23 += alpha*(a.a21*b.a31 + a.a22*b.a32 + a.a23*b.a33 + a.a24*b.a34 + a.a25*b.a35);
        c.a24 += alpha*(a.a21*b.a41 + a.a22*b.a42 + a.a23*b.a43 + a.a24*b.a44 + a.a25*b.a45);
        c.a25 += alpha*(a.a21*b.a51 + a.a22*b.a52 + a.a23*b.a53 + a.a24*b.a54 + a.a25*b.a55);
        c.a31 += alpha*(a.a31*b.a11 + a.a32*b.a12 + a.a33*b.a13 + a.a34*b.a14 + a.a35*b.a15);
        c.a32 += alpha*(a.a31*b.a21 + a.a32*b.a22 + a.a33*b.a23 + a.a34*b.a24 + a.a35*b.a25);
        c.a33 += alpha*(a.a31*b.a31 + a.a32*b.a32 + a.a33*b.a33 + a.a34*b.a34 + a.a35*b.a35);
        c.a34 += alpha*(a.a31*b.a41 + a.a32*b.a42 + a.a33*b.a43 + a.a34*b.a44 + a.a35*b.a45);
        c.a35 += alpha*(a.a31*b.a51 + a.a32*b.a52 + a.a33*b.a53 + a.a34*b.a54 + a.a35*b.a55);
        c.a41 += alpha*(a.a41*b.a11 + a.a42*b.a12 + a.a43*b.a13 + a.a44*b.a14 + a.a45*b.a15);
        c.a42 += alpha*(a.a41*b.a21 + a.a42*b.a22 + a.a43*b.a23 + a.a44*b.a24 + a.a45*b.a25);
        c.a43 += alpha*(a.a41*b.a31 + a.a42*b.a32 + a.a43*b.a33 + a.a44*b.a34 + a.a45*b.a35);
        c.a44 += alpha*(a.a41*b.a41 + a.a42*b.a42 + a.a43*b.a43 + a.a44*b.a44 + a.a45*b.a45);
        c.a45 += alpha*(a.a41*b.a51 + a.a42*b.a52 + a.a43*b.a53 + a.a44*b.a54 + a.a45*b.a55);
        c.a51 += alpha*(a.a51*b.a11 + a.a52*b.a12 + a.a53*b.a13 + a.a54*b.a14 + a.a55*b.a15);
        c.a52 += alpha*(a.a51*b.a21 + a.a52*b.a22 + a.a53*b.a23 + a.a54*b.a24 + a.a55*b.a25);
        c.a53 += alpha*(a.a51*b.a31 + a.a52*b.a32 + a.a53*b.a33 + a.a54*b.a34 + a.a55*b.a35);
        c.a54 += alpha*(a.a51*b.a41 + a.a52*b.a42 + a.a53*b.a43 + a.a54*b.a44 + a.a55*b.a45);
        c.a55 += alpha*(a.a51*b.a51 + a.a52*b.a52 + a.a53*b.a53 + a.a54*b.a54 + a.a55*b.a55);
    }

    /**
     * C = &alpha;A + &beta;u*v<sup>T</sup>
     * 
     * @param alpha scale factor applied to A
     * @param A matrix
     * @param beta scale factor applies to outer product
     * @param u vector
     * @param v vector
     * @param C Storage for solution. Can be same instance as A.
     */
    public static void multAddOuter( float alpha , FMatrix5x5 A , float beta , FMatrix5 u , FMatrix5 v , FMatrix5x5 C ) {
        C.a11 = alpha*A.a11 + beta*u.a1*v.a1;
        C.a12 = alpha*A.a12 + beta*u.a1*v.a2;
        C.a13 = alpha*A.a13 + beta*u.a1*v.a3;
        C.a14 = alpha*A.a14 + beta*u.a1*v.a4;
        C.a15 = alpha*A.a15 + beta*u.a1*v.a5;
        C.a21 = alpha*A.a21 + beta*u.a2*v.a1;
        C.a22 = alpha*A.a22 + beta*u.a2*v.a2;
        C.a23 = alpha*A.a23 + beta*u.a2*v.a3;
        C.a24 = alpha*A.a24 + beta*u.a2*v.a4;
        C.a25 = alpha*A.a25 + beta*u.a2*v.a5;
        C.a31 = alpha*A.a31 + beta*u.a3*v.a1;
        C.a32 = alpha*A.a32 + beta*u.a3*v.a2;
        C.a33 = alpha*A.a33 + beta*u.a3*v.a3;
        C.a34 = alpha*A.a34 + beta*u.a3*v.a4;
        C.a35 = alpha*A.a35 + beta*u.a3*v.a5;
        C.a41 = alpha*A.a41 + beta*u.a4*v.a1;
        C.a42 = alpha*A.a42 + beta*u.a4*v.a2;
        C.a43 = alpha*A.a43 + beta*u.a4*v.a3;
        C.a44 = alpha*A.a44 + beta*u.a4*v.a4;
        C.a45 = alpha*A.a45 + beta*u.a4*v.a5;
        C.a51 = alpha*A.a51 + beta*u.a5*v.a1;
        C.a52 = alpha*A.a52 + beta*u.a5*v.a2;
        C.a53 = alpha*A.a53 + beta*u.a5*v.a3;
        C.a54 = alpha*A.a54 + beta*u.a5*v.a4;
        C.a55 = alpha*A.a55 + beta*u.a5*v.a5;
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
    public static void mult( FMatrix5x5 a , FMatrix5 b , FMatrix5 c) {
        c.a1 = a.a11*b.a1 + a.a12*b.a2 + a.a13*b.a3 + a.a14*b.a4 + a.a15*b.a5;
        c.a2 = a.a21*b.a1 + a.a22*b.a2 + a.a23*b.a3 + a.a24*b.a4 + a.a25*b.a5;
        c.a3 = a.a31*b.a1 + a.a32*b.a2 + a.a33*b.a3 + a.a34*b.a4 + a.a35*b.a5;
        c.a4 = a.a41*b.a1 + a.a42*b.a2 + a.a43*b.a3 + a.a44*b.a4 + a.a45*b.a5;
        c.a5 = a.a51*b.a1 + a.a52*b.a2 + a.a53*b.a3 + a.a54*b.a4 + a.a55*b.a5;
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
    public static void mult( FMatrix5 a , FMatrix5x5 b , FMatrix5 c) {
        c.a1 = a.a1*b.a11 + a.a2*b.a21 + a.a3*b.a31 + a.a4*b.a41 + a.a5*b.a51;
        c.a2 = a.a1*b.a12 + a.a2*b.a22 + a.a3*b.a32 + a.a4*b.a42 + a.a5*b.a52;
        c.a3 = a.a1*b.a13 + a.a2*b.a23 + a.a3*b.a33 + a.a4*b.a43 + a.a5*b.a53;
        c.a4 = a.a1*b.a14 + a.a2*b.a24 + a.a3*b.a34 + a.a4*b.a44 + a.a5*b.a54;
        c.a5 = a.a1*b.a15 + a.a2*b.a25 + a.a3*b.a35 + a.a4*b.a45 + a.a5*b.a55;
    }

    /**
     * <p>Performs the vector dot product:<br>
     * <br>
     * c = a * b <br>
     * <br>
     * c &ge; &sum;<sub>k=1:n</sub> { b<sub>k</sub> * a<sub>k</sub> }
     * </p>
     *
     * @param a The left vector in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @return The dot product
     */
    public static float dot( FMatrix5 a , FMatrix5 b ) {
        return a.a1*b.a1 + a.a2*b.a2 + a.a3*b.a3 + a.a4*b.a4 + a.a5*b.a5;
    }

    /**
     * Sets all the diagonal elements equal to one and everything else equal to zero.
     * If this is a square matrix then it will be an identity matrix.
     *
     * @param a A matrix.
     */
    public static void setIdentity( FMatrix5x5 a ) {
        a.a11 = 1; a.a21 = 0; a.a31 = 0; a.a41 = 0; a.a51 = 0;
        a.a12 = 0; a.a22 = 1; a.a32 = 0; a.a42 = 0; a.a52 = 0;
        a.a13 = 0; a.a23 = 0; a.a33 = 1; a.a43 = 0; a.a53 = 0;
        a.a14 = 0; a.a24 = 0; a.a34 = 0; a.a44 = 1; a.a54 = 0;
        a.a15 = 0; a.a25 = 0; a.a35 = 0; a.a45 = 0; a.a55 = 1;
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
    public static boolean invert( FMatrix5x5 a , FMatrix5x5 inv ) {

        float scale = 1.0f/elementMaxAbs(a);

        float a11 = a.a11*scale;
        float a12 = a.a12*scale;
        float a13 = a.a13*scale;
        float a14 = a.a14*scale;
        float a15 = a.a15*scale;
        float a21 = a.a21*scale;
        float a22 = a.a22*scale;
        float a23 = a.a23*scale;
        float a24 = a.a24*scale;
        float a25 = a.a25*scale;
        float a31 = a.a31*scale;
        float a32 = a.a32*scale;
        float a33 = a.a33*scale;
        float a34 = a.a34*scale;
        float a35 = a.a35*scale;
        float a41 = a.a41*scale;
        float a42 = a.a42*scale;
        float a43 = a.a43*scale;
        float a44 = a.a44*scale;
        float a45 = a.a45*scale;
        float a51 = a.a51*scale;
        float a52 = a.a52*scale;
        float a53 = a.a53*scale;
        float a54 = a.a54*scale;
        float a55 = a.a55*scale;

        float m11 =  + a22*( + a33*(a44*a55 - a45*a54) - a34*(a43*a55 - a45*a53) + a35*(a43*a54 - a44*a53)) - a23*( + a32*(a44*a55 - a45*a54) - a34*(a42*a55 - a45*a52) + a35*(a42*a54 - a44*a52)) + a24*( + a32*(a43*a55 - a45*a53) - a33*(a42*a55 - a45*a52) + a35*(a42*a53 - a43*a52)) - a25*( + a32*(a43*a54 - a44*a53) - a33*(a42*a54 - a44*a52) + a34*(a42*a53 - a43*a52));
        float m12 = -(  + a21*( + a33*(a44*a55 - a45*a54) - a34*(a43*a55 - a45*a53) + a35*(a43*a54 - a44*a53)) - a23*( + a31*(a44*a55 - a45*a54) - a34*(a41*a55 - a45*a51) + a35*(a41*a54 - a44*a51)) + a24*( + a31*(a43*a55 - a45*a53) - a33*(a41*a55 - a45*a51) + a35*(a41*a53 - a43*a51)) - a25*( + a31*(a43*a54 - a44*a53) - a33*(a41*a54 - a44*a51) + a34*(a41*a53 - a43*a51)));
        float m13 =  + a21*( + a32*(a44*a55 - a45*a54) - a34*(a42*a55 - a45*a52) + a35*(a42*a54 - a44*a52)) - a22*( + a31*(a44*a55 - a45*a54) - a34*(a41*a55 - a45*a51) + a35*(a41*a54 - a44*a51)) + a24*( + a31*(a42*a55 - a45*a52) - a32*(a41*a55 - a45*a51) + a35*(a41*a52 - a42*a51)) - a25*( + a31*(a42*a54 - a44*a52) - a32*(a41*a54 - a44*a51) + a34*(a41*a52 - a42*a51));
        float m14 = -(  + a21*( + a32*(a43*a55 - a45*a53) - a33*(a42*a55 - a45*a52) + a35*(a42*a53 - a43*a52)) - a22*( + a31*(a43*a55 - a45*a53) - a33*(a41*a55 - a45*a51) + a35*(a41*a53 - a43*a51)) + a23*( + a31*(a42*a55 - a45*a52) - a32*(a41*a55 - a45*a51) + a35*(a41*a52 - a42*a51)) - a25*( + a31*(a42*a53 - a43*a52) - a32*(a41*a53 - a43*a51) + a33*(a41*a52 - a42*a51)));
        float m15 =  + a21*( + a32*(a43*a54 - a44*a53) - a33*(a42*a54 - a44*a52) + a34*(a42*a53 - a43*a52)) - a22*( + a31*(a43*a54 - a44*a53) - a33*(a41*a54 - a44*a51) + a34*(a41*a53 - a43*a51)) + a23*( + a31*(a42*a54 - a44*a52) - a32*(a41*a54 - a44*a51) + a34*(a41*a52 - a42*a51)) - a24*( + a31*(a42*a53 - a43*a52) - a32*(a41*a53 - a43*a51) + a33*(a41*a52 - a42*a51));
        float m21 = -(  + a12*( + a33*(a44*a55 - a45*a54) - a34*(a43*a55 - a45*a53) + a35*(a43*a54 - a44*a53)) - a13*( + a32*(a44*a55 - a45*a54) - a34*(a42*a55 - a45*a52) + a35*(a42*a54 - a44*a52)) + a14*( + a32*(a43*a55 - a45*a53) - a33*(a42*a55 - a45*a52) + a35*(a42*a53 - a43*a52)) - a15*( + a32*(a43*a54 - a44*a53) - a33*(a42*a54 - a44*a52) + a34*(a42*a53 - a43*a52)));
        float m22 =  + a11*( + a33*(a44*a55 - a45*a54) - a34*(a43*a55 - a45*a53) + a35*(a43*a54 - a44*a53)) - a13*( + a31*(a44*a55 - a45*a54) - a34*(a41*a55 - a45*a51) + a35*(a41*a54 - a44*a51)) + a14*( + a31*(a43*a55 - a45*a53) - a33*(a41*a55 - a45*a51) + a35*(a41*a53 - a43*a51)) - a15*( + a31*(a43*a54 - a44*a53) - a33*(a41*a54 - a44*a51) + a34*(a41*a53 - a43*a51));
        float m23 = -(  + a11*( + a32*(a44*a55 - a45*a54) - a34*(a42*a55 - a45*a52) + a35*(a42*a54 - a44*a52)) - a12*( + a31*(a44*a55 - a45*a54) - a34*(a41*a55 - a45*a51) + a35*(a41*a54 - a44*a51)) + a14*( + a31*(a42*a55 - a45*a52) - a32*(a41*a55 - a45*a51) + a35*(a41*a52 - a42*a51)) - a15*( + a31*(a42*a54 - a44*a52) - a32*(a41*a54 - a44*a51) + a34*(a41*a52 - a42*a51)));
        float m24 =  + a11*( + a32*(a43*a55 - a45*a53) - a33*(a42*a55 - a45*a52) + a35*(a42*a53 - a43*a52)) - a12*( + a31*(a43*a55 - a45*a53) - a33*(a41*a55 - a45*a51) + a35*(a41*a53 - a43*a51)) + a13*( + a31*(a42*a55 - a45*a52) - a32*(a41*a55 - a45*a51) + a35*(a41*a52 - a42*a51)) - a15*( + a31*(a42*a53 - a43*a52) - a32*(a41*a53 - a43*a51) + a33*(a41*a52 - a42*a51));
        float m25 = -(  + a11*( + a32*(a43*a54 - a44*a53) - a33*(a42*a54 - a44*a52) + a34*(a42*a53 - a43*a52)) - a12*( + a31*(a43*a54 - a44*a53) - a33*(a41*a54 - a44*a51) + a34*(a41*a53 - a43*a51)) + a13*( + a31*(a42*a54 - a44*a52) - a32*(a41*a54 - a44*a51) + a34*(a41*a52 - a42*a51)) - a14*( + a31*(a42*a53 - a43*a52) - a32*(a41*a53 - a43*a51) + a33*(a41*a52 - a42*a51)));
        float m31 =  + a12*( + a23*(a44*a55 - a45*a54) - a24*(a43*a55 - a45*a53) + a25*(a43*a54 - a44*a53)) - a13*( + a22*(a44*a55 - a45*a54) - a24*(a42*a55 - a45*a52) + a25*(a42*a54 - a44*a52)) + a14*( + a22*(a43*a55 - a45*a53) - a23*(a42*a55 - a45*a52) + a25*(a42*a53 - a43*a52)) - a15*( + a22*(a43*a54 - a44*a53) - a23*(a42*a54 - a44*a52) + a24*(a42*a53 - a43*a52));
        float m32 = -(  + a11*( + a23*(a44*a55 - a45*a54) - a24*(a43*a55 - a45*a53) + a25*(a43*a54 - a44*a53)) - a13*( + a21*(a44*a55 - a45*a54) - a24*(a41*a55 - a45*a51) + a25*(a41*a54 - a44*a51)) + a14*( + a21*(a43*a55 - a45*a53) - a23*(a41*a55 - a45*a51) + a25*(a41*a53 - a43*a51)) - a15*( + a21*(a43*a54 - a44*a53) - a23*(a41*a54 - a44*a51) + a24*(a41*a53 - a43*a51)));
        float m33 =  + a11*( + a22*(a44*a55 - a45*a54) - a24*(a42*a55 - a45*a52) + a25*(a42*a54 - a44*a52)) - a12*( + a21*(a44*a55 - a45*a54) - a24*(a41*a55 - a45*a51) + a25*(a41*a54 - a44*a51)) + a14*( + a21*(a42*a55 - a45*a52) - a22*(a41*a55 - a45*a51) + a25*(a41*a52 - a42*a51)) - a15*( + a21*(a42*a54 - a44*a52) - a22*(a41*a54 - a44*a51) + a24*(a41*a52 - a42*a51));
        float m34 = -(  + a11*( + a22*(a43*a55 - a45*a53) - a23*(a42*a55 - a45*a52) + a25*(a42*a53 - a43*a52)) - a12*( + a21*(a43*a55 - a45*a53) - a23*(a41*a55 - a45*a51) + a25*(a41*a53 - a43*a51)) + a13*( + a21*(a42*a55 - a45*a52) - a22*(a41*a55 - a45*a51) + a25*(a41*a52 - a42*a51)) - a15*( + a21*(a42*a53 - a43*a52) - a22*(a41*a53 - a43*a51) + a23*(a41*a52 - a42*a51)));
        float m35 =  + a11*( + a22*(a43*a54 - a44*a53) - a23*(a42*a54 - a44*a52) + a24*(a42*a53 - a43*a52)) - a12*( + a21*(a43*a54 - a44*a53) - a23*(a41*a54 - a44*a51) + a24*(a41*a53 - a43*a51)) + a13*( + a21*(a42*a54 - a44*a52) - a22*(a41*a54 - a44*a51) + a24*(a41*a52 - a42*a51)) - a14*( + a21*(a42*a53 - a43*a52) - a22*(a41*a53 - a43*a51) + a23*(a41*a52 - a42*a51));
        float m41 = -(  + a12*( + a23*(a34*a55 - a35*a54) - a24*(a33*a55 - a35*a53) + a25*(a33*a54 - a34*a53)) - a13*( + a22*(a34*a55 - a35*a54) - a24*(a32*a55 - a35*a52) + a25*(a32*a54 - a34*a52)) + a14*( + a22*(a33*a55 - a35*a53) - a23*(a32*a55 - a35*a52) + a25*(a32*a53 - a33*a52)) - a15*( + a22*(a33*a54 - a34*a53) - a23*(a32*a54 - a34*a52) + a24*(a32*a53 - a33*a52)));
        float m42 =  + a11*( + a23*(a34*a55 - a35*a54) - a24*(a33*a55 - a35*a53) + a25*(a33*a54 - a34*a53)) - a13*( + a21*(a34*a55 - a35*a54) - a24*(a31*a55 - a35*a51) + a25*(a31*a54 - a34*a51)) + a14*( + a21*(a33*a55 - a35*a53) - a23*(a31*a55 - a35*a51) + a25*(a31*a53 - a33*a51)) - a15*( + a21*(a33*a54 - a34*a53) - a23*(a31*a54 - a34*a51) + a24*(a31*a53 - a33*a51));
        float m43 = -(  + a11*( + a22*(a34*a55 - a35*a54) - a24*(a32*a55 - a35*a52) + a25*(a32*a54 - a34*a52)) - a12*( + a21*(a34*a55 - a35*a54) - a24*(a31*a55 - a35*a51) + a25*(a31*a54 - a34*a51)) + a14*( + a21*(a32*a55 - a35*a52) - a22*(a31*a55 - a35*a51) + a25*(a31*a52 - a32*a51)) - a15*( + a21*(a32*a54 - a34*a52) - a22*(a31*a54 - a34*a51) + a24*(a31*a52 - a32*a51)));
        float m44 =  + a11*( + a22*(a33*a55 - a35*a53) - a23*(a32*a55 - a35*a52) + a25*(a32*a53 - a33*a52)) - a12*( + a21*(a33*a55 - a35*a53) - a23*(a31*a55 - a35*a51) + a25*(a31*a53 - a33*a51)) + a13*( + a21*(a32*a55 - a35*a52) - a22*(a31*a55 - a35*a51) + a25*(a31*a52 - a32*a51)) - a15*( + a21*(a32*a53 - a33*a52) - a22*(a31*a53 - a33*a51) + a23*(a31*a52 - a32*a51));
        float m45 = -(  + a11*( + a22*(a33*a54 - a34*a53) - a23*(a32*a54 - a34*a52) + a24*(a32*a53 - a33*a52)) - a12*( + a21*(a33*a54 - a34*a53) - a23*(a31*a54 - a34*a51) + a24*(a31*a53 - a33*a51)) + a13*( + a21*(a32*a54 - a34*a52) - a22*(a31*a54 - a34*a51) + a24*(a31*a52 - a32*a51)) - a14*( + a21*(a32*a53 - a33*a52) - a22*(a31*a53 - a33*a51) + a23*(a31*a52 - a32*a51)));
        float m51 =  + a12*( + a23*(a34*a45 - a35*a44) - a24*(a33*a45 - a35*a43) + a25*(a33*a44 - a34*a43)) - a13*( + a22*(a34*a45 - a35*a44) - a24*(a32*a45 - a35*a42) + a25*(a32*a44 - a34*a42)) + a14*( + a22*(a33*a45 - a35*a43) - a23*(a32*a45 - a35*a42) + a25*(a32*a43 - a33*a42)) - a15*( + a22*(a33*a44 - a34*a43) - a23*(a32*a44 - a34*a42) + a24*(a32*a43 - a33*a42));
        float m52 = -(  + a11*( + a23*(a34*a45 - a35*a44) - a24*(a33*a45 - a35*a43) + a25*(a33*a44 - a34*a43)) - a13*( + a21*(a34*a45 - a35*a44) - a24*(a31*a45 - a35*a41) + a25*(a31*a44 - a34*a41)) + a14*( + a21*(a33*a45 - a35*a43) - a23*(a31*a45 - a35*a41) + a25*(a31*a43 - a33*a41)) - a15*( + a21*(a33*a44 - a34*a43) - a23*(a31*a44 - a34*a41) + a24*(a31*a43 - a33*a41)));
        float m53 =  + a11*( + a22*(a34*a45 - a35*a44) - a24*(a32*a45 - a35*a42) + a25*(a32*a44 - a34*a42)) - a12*( + a21*(a34*a45 - a35*a44) - a24*(a31*a45 - a35*a41) + a25*(a31*a44 - a34*a41)) + a14*( + a21*(a32*a45 - a35*a42) - a22*(a31*a45 - a35*a41) + a25*(a31*a42 - a32*a41)) - a15*( + a21*(a32*a44 - a34*a42) - a22*(a31*a44 - a34*a41) + a24*(a31*a42 - a32*a41));
        float m54 = -(  + a11*( + a22*(a33*a45 - a35*a43) - a23*(a32*a45 - a35*a42) + a25*(a32*a43 - a33*a42)) - a12*( + a21*(a33*a45 - a35*a43) - a23*(a31*a45 - a35*a41) + a25*(a31*a43 - a33*a41)) + a13*( + a21*(a32*a45 - a35*a42) - a22*(a31*a45 - a35*a41) + a25*(a31*a42 - a32*a41)) - a15*( + a21*(a32*a43 - a33*a42) - a22*(a31*a43 - a33*a41) + a23*(a31*a42 - a32*a41)));
        float m55 =  + a11*( + a22*(a33*a44 - a34*a43) - a23*(a32*a44 - a34*a42) + a24*(a32*a43 - a33*a42)) - a12*( + a21*(a33*a44 - a34*a43) - a23*(a31*a44 - a34*a41) + a24*(a31*a43 - a33*a41)) + a13*( + a21*(a32*a44 - a34*a42) - a22*(a31*a44 - a34*a41) + a24*(a31*a42 - a32*a41)) - a14*( + a21*(a32*a43 - a33*a42) - a22*(a31*a43 - a33*a41) + a23*(a31*a42 - a32*a41));

        float det = (a11*m11 + a12*m12 + a13*m13 + a14*m14 + a15*m15)/scale;

        inv.a11 = m11/det;
        inv.a12 = m21/det;
        inv.a13 = m31/det;
        inv.a14 = m41/det;
        inv.a15 = m51/det;
        inv.a21 = m12/det;
        inv.a22 = m22/det;
        inv.a23 = m32/det;
        inv.a24 = m42/det;
        inv.a25 = m52/det;
        inv.a31 = m13/det;
        inv.a32 = m23/det;
        inv.a33 = m33/det;
        inv.a34 = m43/det;
        inv.a35 = m53/det;
        inv.a41 = m14/det;
        inv.a42 = m24/det;
        inv.a43 = m34/det;
        inv.a44 = m44/det;
        inv.a45 = m54/det;
        inv.a51 = m15/det;
        inv.a52 = m25/det;
        inv.a53 = m35/det;
        inv.a54 = m45/det;
        inv.a55 = m55/det;

        return !Float.isNaN(det) && !Float.isInfinite(det);
    }

    /**
     * Computes the determinant using minor matrices.<br>
     * WARNING: Potentially less stable than using LU decomposition.
     *
     * @param mat Input matrix.  Not modified.
     * @return The determinant.
     */
    public static float det( FMatrix5x5 mat ) {

        float  a11 = mat.a22;
        float  a12 = mat.a23;
        float  a13 = mat.a24;
        float  a14 = mat.a25;
        float  a21 = mat.a32;
        float  a22 = mat.a33;
        float  a23 = mat.a34;
        float  a24 = mat.a35;
        float  a31 = mat.a42;
        float  a32 = mat.a43;
        float  a33 = mat.a44;
        float  a34 = mat.a45;
        float  a41 = mat.a52;
        float  a42 = mat.a53;
        float  a43 = mat.a54;
        float  a44 = mat.a55;

        float ret = 0;
        ret += mat.a11 * ( + a11*( + a22*(a33*a44 - a34*a43) - a23*(a32*a44 - a34*a42) + a24*(a32*a43 - a33*a42)) - a12*( + a21*(a33*a44 - a34*a43) - a23*(a31*a44 - a34*a41) + a24*(a31*a43 - a33*a41)) + a13*( + a21*(a32*a44 - a34*a42) - a22*(a31*a44 - a34*a41) + a24*(a31*a42 - a32*a41)) - a14*( + a21*(a32*a43 - a33*a42) - a22*(a31*a43 - a33*a41) + a23*(a31*a42 - a32*a41)));
        a11 = mat.a21;
        a21 = mat.a31;
        a31 = mat.a41;
        a41 = mat.a51;
        ret -= mat.a12 * ( + a11*( + a22*(a33*a44 - a34*a43) - a23*(a32*a44 - a34*a42) + a24*(a32*a43 - a33*a42)) - a12*( + a21*(a33*a44 - a34*a43) - a23*(a31*a44 - a34*a41) + a24*(a31*a43 - a33*a41)) + a13*( + a21*(a32*a44 - a34*a42) - a22*(a31*a44 - a34*a41) + a24*(a31*a42 - a32*a41)) - a14*( + a21*(a32*a43 - a33*a42) - a22*(a31*a43 - a33*a41) + a23*(a31*a42 - a32*a41)));
        a12 = mat.a22;
        a22 = mat.a32;
        a32 = mat.a42;
        a42 = mat.a52;
        ret += mat.a13 * ( + a11*( + a22*(a33*a44 - a34*a43) - a23*(a32*a44 - a34*a42) + a24*(a32*a43 - a33*a42)) - a12*( + a21*(a33*a44 - a34*a43) - a23*(a31*a44 - a34*a41) + a24*(a31*a43 - a33*a41)) + a13*( + a21*(a32*a44 - a34*a42) - a22*(a31*a44 - a34*a41) + a24*(a31*a42 - a32*a41)) - a14*( + a21*(a32*a43 - a33*a42) - a22*(a31*a43 - a33*a41) + a23*(a31*a42 - a32*a41)));
        a13 = mat.a23;
        a23 = mat.a33;
        a33 = mat.a43;
        a43 = mat.a53;
        ret -= mat.a14 * ( + a11*( + a22*(a33*a44 - a34*a43) - a23*(a32*a44 - a34*a42) + a24*(a32*a43 - a33*a42)) - a12*( + a21*(a33*a44 - a34*a43) - a23*(a31*a44 - a34*a41) + a24*(a31*a43 - a33*a41)) + a13*( + a21*(a32*a44 - a34*a42) - a22*(a31*a44 - a34*a41) + a24*(a31*a42 - a32*a41)) - a14*( + a21*(a32*a43 - a33*a42) - a22*(a31*a43 - a33*a41) + a23*(a31*a42 - a32*a41)));
        a14 = mat.a24;
        a24 = mat.a34;
        a34 = mat.a44;
        a44 = mat.a54;
        ret += mat.a15 * ( + a11*( + a22*(a33*a44 - a34*a43) - a23*(a32*a44 - a34*a42) + a24*(a32*a43 - a33*a42)) - a12*( + a21*(a33*a44 - a34*a43) - a23*(a31*a44 - a34*a41) + a24*(a31*a43 - a33*a41)) + a13*( + a21*(a32*a44 - a34*a42) - a22*(a31*a44 - a34*a41) + a24*(a31*a42 - a32*a41)) - a14*( + a21*(a32*a43 - a33*a42) - a22*(a31*a43 - a33*a41) + a23*(a31*a42 - a32*a41)));

        return ret;
    }

    /**
     * Performs a lower Cholesky decomposition of matrix 'A' and stores result in A.
     *
     * @param A (Input) SPD Matrix. (Output) lower cholesky.
     * @return true if it was successful or false if it failed.  Not always reliable.
     */
    public static boolean cholL( FMatrix5x5 A ) {

        A.a11 = (float)Math.sqrt(A.a11);
        A.a12 = 0;
        A.a13 = 0;
        A.a14 = 0;
        A.a15 = 0;
        A.a21 = (A.a21)/A.a11;
        A.a22 = (float)Math.sqrt(A.a22-A.a21*A.a21);
        A.a23 = 0;
        A.a24 = 0;
        A.a25 = 0;
        A.a31 = (A.a31)/A.a11;
        A.a32 = (A.a32-A.a31*A.a21)/A.a22;
        A.a33 = (float)Math.sqrt(A.a33-A.a31*A.a31-A.a32*A.a32);
        A.a34 = 0;
        A.a35 = 0;
        A.a41 = (A.a41)/A.a11;
        A.a42 = (A.a42-A.a41*A.a21)/A.a22;
        A.a43 = (A.a43-A.a41*A.a31-A.a42*A.a32)/A.a33;
        A.a44 = (float)Math.sqrt(A.a44-A.a41*A.a41-A.a42*A.a42-A.a43*A.a43);
        A.a45 = 0;
        A.a51 = (A.a51)/A.a11;
        A.a52 = (A.a52-A.a51*A.a21)/A.a22;
        A.a53 = (A.a53-A.a51*A.a31-A.a52*A.a32)/A.a33;
        A.a54 = (A.a54-A.a51*A.a41-A.a52*A.a42-A.a53*A.a43)/A.a44;
        A.a55 = (float)Math.sqrt(A.a55-A.a51*A.a51-A.a52*A.a52-A.a53*A.a53-A.a54*A.a54);
        return !UtilEjml.isUncountable(A.a55);
    }

    /**
     * Performs an upper Cholesky decomposition of matrix 'A' and stores result in A.
     *
     * @param A (Input) SPD Matrix. (Output) upper cholesky.
     * @return true if it was successful or false if it failed.  Not always reliable.
     */
    public static boolean cholU( FMatrix5x5 A ) {

        A.a11 = (float)Math.sqrt(A.a11);
        A.a21 = 0;
        A.a31 = 0;
        A.a41 = 0;
        A.a51 = 0;
        A.a12 = (A.a12)/A.a11;
        A.a22 = (float)Math.sqrt(A.a22-A.a12*A.a12);
        A.a32 = 0;
        A.a42 = 0;
        A.a52 = 0;
        A.a13 = (A.a13)/A.a11;
        A.a23 = (A.a23-A.a12*A.a13)/A.a22;
        A.a33 = (float)Math.sqrt(A.a33-A.a13*A.a13-A.a23*A.a23);
        A.a43 = 0;
        A.a53 = 0;
        A.a14 = (A.a14)/A.a11;
        A.a24 = (A.a24-A.a12*A.a14)/A.a22;
        A.a34 = (A.a34-A.a13*A.a14-A.a23*A.a24)/A.a33;
        A.a44 = (float)Math.sqrt(A.a44-A.a14*A.a14-A.a24*A.a24-A.a34*A.a34);
        A.a54 = 0;
        A.a15 = (A.a15)/A.a11;
        A.a25 = (A.a25-A.a12*A.a15)/A.a22;
        A.a35 = (A.a35-A.a13*A.a15-A.a23*A.a25)/A.a33;
        A.a45 = (A.a45-A.a14*A.a15-A.a24*A.a25-A.a34*A.a35)/A.a44;
        A.a55 = (float)Math.sqrt(A.a55-A.a15*A.a15-A.a25*A.a25-A.a35*A.a35-A.a45*A.a45);
        return !UtilEjml.isUncountable(A.a55);
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
    public static float trace( FMatrix5x5 a ) {
        return a.a11 + a.a22 + a.a33 + a.a44 + a.a55;
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
    public static void diag( FMatrix5x5 input , FMatrix5 out ) {
        out.a1 = input.a11;
        out.a2 = input.a22;
        out.a3 = input.a33;
        out.a4 = input.a44;
        out.a5 = input.a55;
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
    public static float elementMax( FMatrix5x5 a ) {
        float max = a.a11;
        if( a.a12 > max ) max = a.a12;
        if( a.a13 > max ) max = a.a13;
        if( a.a14 > max ) max = a.a14;
        if( a.a15 > max ) max = a.a15;
        if( a.a21 > max ) max = a.a21;
        if( a.a22 > max ) max = a.a22;
        if( a.a23 > max ) max = a.a23;
        if( a.a24 > max ) max = a.a24;
        if( a.a25 > max ) max = a.a25;
        if( a.a31 > max ) max = a.a31;
        if( a.a32 > max ) max = a.a32;
        if( a.a33 > max ) max = a.a33;
        if( a.a34 > max ) max = a.a34;
        if( a.a35 > max ) max = a.a35;
        if( a.a41 > max ) max = a.a41;
        if( a.a42 > max ) max = a.a42;
        if( a.a43 > max ) max = a.a43;
        if( a.a44 > max ) max = a.a44;
        if( a.a45 > max ) max = a.a45;
        if( a.a51 > max ) max = a.a51;
        if( a.a52 > max ) max = a.a52;
        if( a.a53 > max ) max = a.a53;
        if( a.a54 > max ) max = a.a54;
        if( a.a55 > max ) max = a.a55;

        return max;
    }

    /**
     * <p>
     * Returns the value of the element in the vector that has the largest value.<br>
     * <br>
     * Max{ a<sub>i</sub> } for all i<br>
     * </p>
     *
     * @param a A vector. Not modified.
     * @return The max element value of the matrix.
     */
    public static float elementMax( FMatrix5 a ) {
        float max = a.a1;
        if( a.a2 > max ) max = a.a2;
        if( a.a3 > max ) max = a.a3;
        if( a.a4 > max ) max = a.a4;
        if( a.a5 > max ) max = a.a5;

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
    public static float elementMaxAbs( FMatrix5x5 a ) {
        float max = Math.abs(a.a11);
        float tmp = Math.abs(a.a12); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a13); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a14); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a15); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a21); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a22); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a23); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a24); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a25); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a31); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a32); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a33); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a34); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a35); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a41); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a42); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a43); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a44); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a45); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a51); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a52); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a53); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a54); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a55); if( tmp > max ) max = tmp;

        return max;
    }

    /**
     * <p>
     * Returns the absolute value of the element in the vector that has the largest absolute value.<br>
     * <br>
     * Max{ |a<sub>i</sub>| } for all i<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The max abs element value of the vector.
     */
    public static float elementMaxAbs( FMatrix5 a ) {
        float max = Math.abs(a.a1);
        float tmp = Math.abs(a.a2); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a2); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a3); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a4); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a5); if( tmp > max ) max = tmp;

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
    public static float elementMin( FMatrix5x5 a ) {
        float min = a.a11;
        if( a.a12 < min ) min = a.a12;
        if( a.a13 < min ) min = a.a13;
        if( a.a14 < min ) min = a.a14;
        if( a.a15 < min ) min = a.a15;
        if( a.a21 < min ) min = a.a21;
        if( a.a22 < min ) min = a.a22;
        if( a.a23 < min ) min = a.a23;
        if( a.a24 < min ) min = a.a24;
        if( a.a25 < min ) min = a.a25;
        if( a.a31 < min ) min = a.a31;
        if( a.a32 < min ) min = a.a32;
        if( a.a33 < min ) min = a.a33;
        if( a.a34 < min ) min = a.a34;
        if( a.a35 < min ) min = a.a35;
        if( a.a41 < min ) min = a.a41;
        if( a.a42 < min ) min = a.a42;
        if( a.a43 < min ) min = a.a43;
        if( a.a44 < min ) min = a.a44;
        if( a.a45 < min ) min = a.a45;
        if( a.a51 < min ) min = a.a51;
        if( a.a52 < min ) min = a.a52;
        if( a.a53 < min ) min = a.a53;
        if( a.a54 < min ) min = a.a54;
        if( a.a55 < min ) min = a.a55;

        return min;
    }

    /**
     * <p>
     * Returns the value of the element in the vector that has the minimum value.<br>
     * <br>
     * Min{ a<sub>i</sub> } for all<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The value of element in the vector with the minimum value.
     */
    public static float elementMin( FMatrix5 a ) {
        float min = a.a1;
        if( a.a2 < min ) min = a.a2;
        if( a.a3 < min ) min = a.a3;
        if( a.a4 < min ) min = a.a4;
        if( a.a5 < min ) min = a.a5;

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
    public static float elementMinAbs( FMatrix5x5 a ) {
        float min = Math.abs(a.a11);
        float tmp = Math.abs(a.a12); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a13); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a14); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a15); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a21); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a22); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a23); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a24); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a25); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a31); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a32); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a33); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a34); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a35); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a41); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a42); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a43); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a44); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a45); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a51); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a52); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a53); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a54); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a55); if( tmp < min ) min = tmp;

        return min;
    }

    /**
     * <p>
     * Returns the absolute value of the element in the vector that has the smallest absolute value.<br>
     * <br>
     * Min{ |a<sub>i</sub>| } for all i<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The max element value of the vector.
     */
    public static float elementMinAbs( FMatrix5 a ) {
        float min = Math.abs(a.a1);
        float tmp = Math.abs(a.a1); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a2); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a3); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a4); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a5); if( tmp < min ) min = tmp;

        return min;
    }

    /**
     * <p>Performs an element by element multiplication operation:<br>
     * <br>
     * a<sub>ij</sub> = a<sub>ij</sub> * b<sub>ij</sub> <br>
     * </p>
     * @param a The left matrix in the multiplication operation. Modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     */
    public static void elementMult( FMatrix5x5 a , FMatrix5x5 b) {
        a.a11 *= b.a11; a.a12 *= b.a12; a.a13 *= b.a13; a.a14 *= b.a14; a.a15 *= b.a15;
        a.a21 *= b.a21; a.a22 *= b.a22; a.a23 *= b.a23; a.a24 *= b.a24; a.a25 *= b.a25;
        a.a31 *= b.a31; a.a32 *= b.a32; a.a33 *= b.a33; a.a34 *= b.a34; a.a35 *= b.a35;
        a.a41 *= b.a41; a.a42 *= b.a42; a.a43 *= b.a43; a.a44 *= b.a44; a.a45 *= b.a45;
        a.a51 *= b.a51; a.a52 *= b.a52; a.a53 *= b.a53; a.a54 *= b.a54; a.a55 *= b.a55;
    }

    /**
     * <p>Performs an element by element multiplication operation:<br>
     * <br>
     * a<sub>i</sub> = a<sub>i</sub> * b<sub>i</sub> <br>
     * </p>
     * @param a The left vector in the multiplication operation. Modified.
     * @param b The right vector in the multiplication operation. Not modified.
     */
    public static void elementMult( FMatrix5 a , FMatrix5 b) {
        a.a1 *= b.a1;
        a.a2 *= b.a2;
        a.a3 *= b.a3;
        a.a4 *= b.a4;
        a.a5 *= b.a5;
    }

    /**
     * <p>Performs an element by element multiplication operation:<br>
     * <br>
     * c<sub>ij</sub> = a<sub>ij</sub> * b<sub>ij</sub> <br>
     * </p>
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void elementMult( FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c ) {
        c.a11 = a.a11*b.a11; c.a12 = a.a12*b.a12; c.a13 = a.a13*b.a13; c.a14 = a.a14*b.a14; c.a15 = a.a15*b.a15;
        c.a21 = a.a21*b.a21; c.a22 = a.a22*b.a22; c.a23 = a.a23*b.a23; c.a24 = a.a24*b.a24; c.a25 = a.a25*b.a25;
        c.a31 = a.a31*b.a31; c.a32 = a.a32*b.a32; c.a33 = a.a33*b.a33; c.a34 = a.a34*b.a34; c.a35 = a.a35*b.a35;
        c.a41 = a.a41*b.a41; c.a42 = a.a42*b.a42; c.a43 = a.a43*b.a43; c.a44 = a.a44*b.a44; c.a45 = a.a45*b.a45;
        c.a51 = a.a51*b.a51; c.a52 = a.a52*b.a52; c.a53 = a.a53*b.a53; c.a54 = a.a54*b.a54; c.a55 = a.a55*b.a55;
    }

    /**
     * <p>Performs an element by element multiplication operation:<br>
     * <br>
     * c<sub>i</sub> = a<sub>i</sub> * b<sub>j</sub> <br>
     * </p>
     * @param a The left vector in the multiplication operation. Not modified.
     * @param b The right vector in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void elementMult( FMatrix5 a , FMatrix5 b , FMatrix5 c ) {
        c.a1 = a.a1*b.a1;
        c.a2 = a.a2*b.a2;
        c.a3 = a.a3*b.a3;
        c.a4 = a.a4*b.a4;
        c.a5 = a.a5*b.a5;
    }

    /**
     * <p>Performs an element by element division operation:<br>
     * <br>
     * a<sub>ij</sub> = a<sub>ij</sub> / b<sub>ij</sub> <br>
     * </p>
     * @param a The left matrix in the division operation. Modified.
     * @param b The right matrix in the division operation. Not modified.
     */
    public static void elementDiv( FMatrix5x5 a , FMatrix5x5 b) {
        a.a11 /= b.a11; a.a12 /= b.a12; a.a13 /= b.a13; a.a14 /= b.a14; a.a15 /= b.a15;
        a.a21 /= b.a21; a.a22 /= b.a22; a.a23 /= b.a23; a.a24 /= b.a24; a.a25 /= b.a25;
        a.a31 /= b.a31; a.a32 /= b.a32; a.a33 /= b.a33; a.a34 /= b.a34; a.a35 /= b.a35;
        a.a41 /= b.a41; a.a42 /= b.a42; a.a43 /= b.a43; a.a44 /= b.a44; a.a45 /= b.a45;
        a.a51 /= b.a51; a.a52 /= b.a52; a.a53 /= b.a53; a.a54 /= b.a54; a.a55 /= b.a55;
    }

    /**
     * <p>Performs an element by element division operation:<br>
     * <br>
     * a<sub>i</sub> = a<sub>i</sub> / b<sub>i</sub> <br>
     * </p>
     * @param a The left vector in the division operation. Modified.
     * @param b The right vector in the division operation. Not modified.
     */
    public static void elementDiv( FMatrix5 a , FMatrix5 b) {
        a.a1 /= b.a1;
        a.a2 /= b.a2;
        a.a3 /= b.a3;
        a.a4 /= b.a4;
        a.a5 /= b.a5;
    }

    /**
     * <p>Performs an element by element division operation:<br>
     * <br>
     * c<sub>ij</sub> = a<sub>ij</sub> / b<sub>ij</sub> <br>
     * </p>
     * @param a The left matrix in the division operation. Not modified.
     * @param b The right matrix in the division operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void elementDiv( FMatrix5x5 a , FMatrix5x5 b , FMatrix5x5 c ) {
        c.a11 = a.a11/b.a11; c.a12 = a.a12/b.a12; c.a13 = a.a13/b.a13; c.a14 = a.a14/b.a14; c.a15 = a.a15/b.a15;
        c.a21 = a.a21/b.a21; c.a22 = a.a22/b.a22; c.a23 = a.a23/b.a23; c.a24 = a.a24/b.a24; c.a25 = a.a25/b.a25;
        c.a31 = a.a31/b.a31; c.a32 = a.a32/b.a32; c.a33 = a.a33/b.a33; c.a34 = a.a34/b.a34; c.a35 = a.a35/b.a35;
        c.a41 = a.a41/b.a41; c.a42 = a.a42/b.a42; c.a43 = a.a43/b.a43; c.a44 = a.a44/b.a44; c.a45 = a.a45/b.a45;
        c.a51 = a.a51/b.a51; c.a52 = a.a52/b.a52; c.a53 = a.a53/b.a53; c.a54 = a.a54/b.a54; c.a55 = a.a55/b.a55;
    }

    /**
     * <p>Performs an element by element division operation:<br>
     * <br>
     * c<sub>i</sub> = a<sub>i</sub> / b<sub>i</sub> <br>
     * </p>
     * @param a The left vector in the division operation. Not modified.
     * @param b The right vector in the division operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void elementDiv( FMatrix5 a , FMatrix5 b , FMatrix5 c ) {
        c.a1 = a.a1/b.a1;
        c.a2 = a.a2/b.a2;
        c.a3 = a.a3/b.a3;
        c.a4 = a.a4/b.a4;
        c.a5 = a.a5/b.a5;
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
    public static void scale( float alpha , FMatrix5x5 a ) {
        a.a11 *= alpha; a.a12 *= alpha; a.a13 *= alpha; a.a14 *= alpha; a.a15 *= alpha;
        a.a21 *= alpha; a.a22 *= alpha; a.a23 *= alpha; a.a24 *= alpha; a.a25 *= alpha;
        a.a31 *= alpha; a.a32 *= alpha; a.a33 *= alpha; a.a34 *= alpha; a.a35 *= alpha;
        a.a41 *= alpha; a.a42 *= alpha; a.a43 *= alpha; a.a44 *= alpha; a.a45 *= alpha;
        a.a51 *= alpha; a.a52 *= alpha; a.a53 *= alpha; a.a54 *= alpha; a.a55 *= alpha;
    }

    /**
     * <p>
     * Performs an in-place element by element scalar multiplication.<br>
     * <br>
     * a<sub>ij</sub> = &alpha;*a<sub>ij</sub>
     * </p>
     *
     * @param a The vector that is to be scaled.  Modified.
     * @param alpha the amount each element is multiplied by.
     */
    public static void scale( float alpha , FMatrix5 a ) {
        a.a1 *= alpha;
        a.a2 *= alpha;
        a.a3 *= alpha;
        a.a4 *= alpha;
        a.a5 *= alpha;
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
    public static void scale( float alpha , FMatrix5x5 a , FMatrix5x5 b ) {
        b.a11 = a.a11*alpha; b.a12 = a.a12*alpha; b.a13 = a.a13*alpha; b.a14 = a.a14*alpha; b.a15 = a.a15*alpha;
        b.a21 = a.a21*alpha; b.a22 = a.a22*alpha; b.a23 = a.a23*alpha; b.a24 = a.a24*alpha; b.a25 = a.a25*alpha;
        b.a31 = a.a31*alpha; b.a32 = a.a32*alpha; b.a33 = a.a33*alpha; b.a34 = a.a34*alpha; b.a35 = a.a35*alpha;
        b.a41 = a.a41*alpha; b.a42 = a.a42*alpha; b.a43 = a.a43*alpha; b.a44 = a.a44*alpha; b.a45 = a.a45*alpha;
        b.a51 = a.a51*alpha; b.a52 = a.a52*alpha; b.a53 = a.a53*alpha; b.a54 = a.a54*alpha; b.a55 = a.a55*alpha;
    }

    /**
     * <p>
     * Performs an element by element scalar multiplication.<br>
     * <br>
     * b<sub>i</sub> = &alpha;*a<sub>i</sub>
     * </p>
     *
     * @param alpha the amount each element is multiplied by.
     * @param a The vector that is to be scaled.  Not modified.
     * @param b Where the scaled matrix is stored. Modified.
     */
    public static void scale( float alpha , FMatrix5 a , FMatrix5 b ) {
        b.a1 = a.a1*alpha;
        b.a2 = a.a2*alpha;
        b.a3 = a.a3*alpha;
        b.a4 = a.a4*alpha;
        b.a5 = a.a5*alpha;
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
    public static void divide( FMatrix5x5 a , float alpha ) {
        a.a11 /= alpha; a.a12 /= alpha; a.a13 /= alpha; a.a14 /= alpha; a.a15 /= alpha;
        a.a21 /= alpha; a.a22 /= alpha; a.a23 /= alpha; a.a24 /= alpha; a.a25 /= alpha;
        a.a31 /= alpha; a.a32 /= alpha; a.a33 /= alpha; a.a34 /= alpha; a.a35 /= alpha;
        a.a41 /= alpha; a.a42 /= alpha; a.a43 /= alpha; a.a44 /= alpha; a.a45 /= alpha;
        a.a51 /= alpha; a.a52 /= alpha; a.a53 /= alpha; a.a54 /= alpha; a.a55 /= alpha;
    }

    /**
     * <p>
     * Performs an in-place element by element scalar division. Scalar denominator.<br>
     * <br>
     * a<sub>i</sub> = a<sub>i</sub>/&alpha;
     * </p>
     *
     * @param a The vector whose elements are to be divided.  Modified.
     * @param alpha the amount each element is divided by.
     */
    public static void divide( FMatrix5 a , float alpha ) {
        a.a1 /= alpha;
        a.a2 /= alpha;
        a.a3 /= alpha;
        a.a4 /= alpha;
        a.a5 /= alpha;
    }

    /**
     * <p>
     * Performs an element by element scalar division.  Scalar denominator.<br>
     * <br>
     * b<sub>ij</sub> = a<sub>ij</sub> /&alpha;
     * </p>
     *
     * @param alpha the amount each element is divided by.
     * @param a The matrix whose elements are to be divided.  Not modified.
     * @param b Where the results are stored. Modified.
     */
    public static void divide( FMatrix5x5 a , float alpha , FMatrix5x5 b ) {
        b.a11 = a.a11/alpha; b.a12 = a.a12/alpha; b.a13 = a.a13/alpha; b.a14 = a.a14/alpha; b.a15 = a.a15/alpha;
        b.a21 = a.a21/alpha; b.a22 = a.a22/alpha; b.a23 = a.a23/alpha; b.a24 = a.a24/alpha; b.a25 = a.a25/alpha;
        b.a31 = a.a31/alpha; b.a32 = a.a32/alpha; b.a33 = a.a33/alpha; b.a34 = a.a34/alpha; b.a35 = a.a35/alpha;
        b.a41 = a.a41/alpha; b.a42 = a.a42/alpha; b.a43 = a.a43/alpha; b.a44 = a.a44/alpha; b.a45 = a.a45/alpha;
        b.a51 = a.a51/alpha; b.a52 = a.a52/alpha; b.a53 = a.a53/alpha; b.a54 = a.a54/alpha; b.a55 = a.a55/alpha;
    }

    /**
     * <p>
     * Performs an element by element scalar division.  Scalar denominator.<br>
     * <br>
     * b<sub>i</sub> = a<sub>i</sub> /&alpha;
     * </p>
     *
     * @param alpha the amount each element is divided by.
     * @param a The vector whose elements are to be divided.  Not modified.
     * @param b Where the results are stored. Modified.
     */
    public static void divide( FMatrix5 a , float alpha , FMatrix5 b ) {
        b.a1 = a.a1/alpha;
        b.a2 = a.a2/alpha;
        b.a3 = a.a3/alpha;
        b.a4 = a.a4/alpha;
        b.a5 = a.a5/alpha;
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
    public static void changeSign( FMatrix5x5 a )
    {
        a.a11 = -a.a11; a.a12 = -a.a12; a.a13 = -a.a13; a.a14 = -a.a14; a.a15 = -a.a15;
        a.a21 = -a.a21; a.a22 = -a.a22; a.a23 = -a.a23; a.a24 = -a.a24; a.a25 = -a.a25;
        a.a31 = -a.a31; a.a32 = -a.a32; a.a33 = -a.a33; a.a34 = -a.a34; a.a35 = -a.a35;
        a.a41 = -a.a41; a.a42 = -a.a42; a.a43 = -a.a43; a.a44 = -a.a44; a.a45 = -a.a45;
        a.a51 = -a.a51; a.a52 = -a.a52; a.a53 = -a.a53; a.a54 = -a.a54; a.a55 = -a.a55;
    }

    /**
     * <p>
     * Changes the sign of every element in the vector.<br>
     * <br>
     * a<sub>i</sub> = -a<sub>i</sub>
     * </p>
     *
     * @param a A vector. Modified.
     */
    public static void changeSign( FMatrix5 a )
    {
        a.a1 = -a.a1;
        a.a2 = -a.a2;
        a.a3 = -a.a3;
        a.a4 = -a.a4;
        a.a5 = -a.a5;
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
    public static void fill( FMatrix5x5 a , float v  ) {
        a.a11 = v; a.a12 = v; a.a13 = v; a.a14 = v; a.a15 = v;
        a.a21 = v; a.a22 = v; a.a23 = v; a.a24 = v; a.a25 = v;
        a.a31 = v; a.a32 = v; a.a33 = v; a.a34 = v; a.a35 = v;
        a.a41 = v; a.a42 = v; a.a43 = v; a.a44 = v; a.a45 = v;
        a.a51 = v; a.a52 = v; a.a53 = v; a.a54 = v; a.a55 = v;
    }

    /**
     * <p>
     * Sets every element in the vector to the specified value.<br>
     * <br>
     * a<sub>i</sub> = value
     * <p>
     *
     * @param a A vector whose elements are about to be set. Modified.
     * @param v The value each element will have.
     */
    public static void fill( FMatrix5 a , float v  ) {
        a.a1 = v;
        a.a2 = v;
        a.a3 = v;
        a.a4 = v;
        a.a5 = v;
    }

    /**
     * Extracts the row from the matrix a.
     * @param a Input matrix
     * @param row Which row is to be extracted
     * @param out output. Storage for the extracted row. If null then a new vector will be returned.
     * @return The extracted row.
     */
    public static FMatrix5 extractRow( FMatrix5x5 a , int row , FMatrix5 out ) {
        if( out == null) out = new FMatrix5();
        switch( row ) {
            case 0:
                out.a1 = a.a11;
                out.a2 = a.a12;
                out.a3 = a.a13;
                out.a4 = a.a14;
                out.a5 = a.a15;
            break;
            case 1:
                out.a1 = a.a21;
                out.a2 = a.a22;
                out.a3 = a.a23;
                out.a4 = a.a24;
                out.a5 = a.a25;
            break;
            case 2:
                out.a1 = a.a31;
                out.a2 = a.a32;
                out.a3 = a.a33;
                out.a4 = a.a34;
                out.a5 = a.a35;
            break;
            case 3:
                out.a1 = a.a41;
                out.a2 = a.a42;
                out.a3 = a.a43;
                out.a4 = a.a44;
                out.a5 = a.a45;
            break;
            case 4:
                out.a1 = a.a51;
                out.a2 = a.a52;
                out.a3 = a.a53;
                out.a4 = a.a54;
                out.a5 = a.a55;
            break;
            default:
                throw new IllegalArgumentException("Out of bounds row.  row = "+row);
        }
        return out;
    }

    /**
     * Extracts the column from the matrix a.
     * @param a Input matrix
     * @param column Which column is to be extracted
     * @param out output. Storage for the extracted column. If null then a new vector will be returned.
     * @return The extracted column.
     */
    public static FMatrix5 extractColumn( FMatrix5x5 a , int column , FMatrix5 out ) {
        if( out == null) out = new FMatrix5();
        switch( column ) {
            case 0:
                out.a1 = a.a11;
                out.a2 = a.a21;
                out.a3 = a.a31;
                out.a4 = a.a41;
                out.a5 = a.a51;
            break;
            case 1:
                out.a1 = a.a12;
                out.a2 = a.a22;
                out.a3 = a.a32;
                out.a4 = a.a42;
                out.a5 = a.a52;
            break;
            case 2:
                out.a1 = a.a13;
                out.a2 = a.a23;
                out.a3 = a.a33;
                out.a4 = a.a43;
                out.a5 = a.a53;
            break;
            case 3:
                out.a1 = a.a14;
                out.a2 = a.a24;
                out.a3 = a.a34;
                out.a4 = a.a44;
                out.a5 = a.a54;
            break;
            case 4:
                out.a1 = a.a15;
                out.a2 = a.a25;
                out.a3 = a.a35;
                out.a4 = a.a45;
                out.a5 = a.a55;
            break;
            default:
                throw new IllegalArgumentException("Out of bounds column.  column = "+column);
        }
        return out;
    }

}

