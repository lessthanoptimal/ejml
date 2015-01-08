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

import org.ejml.data.FixedMatrix6_64F;
import org.ejml.data.FixedMatrix6x6_64F;

/**
 * <p>Common matrix operations for fixed sized matrices which are 6 x 6 or 6 element vectors.</p>
 * <p>DO NOT MODIFY.  Automatically generated code created by GenerateFixedOps</p>
 *
 * @author Peter Abeles
 */
public class FixedOps6 {
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
    public static void add( FixedMatrix6x6_64F a , FixedMatrix6x6_64F b , FixedMatrix6x6_64F c ) {
        c.a11 = a.a11 + b.a11;
        c.a12 = a.a12 + b.a12;
        c.a13 = a.a13 + b.a13;
        c.a14 = a.a14 + b.a14;
        c.a15 = a.a15 + b.a15;
        c.a16 = a.a16 + b.a16;
        c.a21 = a.a21 + b.a21;
        c.a22 = a.a22 + b.a22;
        c.a23 = a.a23 + b.a23;
        c.a24 = a.a24 + b.a24;
        c.a25 = a.a25 + b.a25;
        c.a26 = a.a26 + b.a26;
        c.a31 = a.a31 + b.a31;
        c.a32 = a.a32 + b.a32;
        c.a33 = a.a33 + b.a33;
        c.a34 = a.a34 + b.a34;
        c.a35 = a.a35 + b.a35;
        c.a36 = a.a36 + b.a36;
        c.a41 = a.a41 + b.a41;
        c.a42 = a.a42 + b.a42;
        c.a43 = a.a43 + b.a43;
        c.a44 = a.a44 + b.a44;
        c.a45 = a.a45 + b.a45;
        c.a46 = a.a46 + b.a46;
        c.a51 = a.a51 + b.a51;
        c.a52 = a.a52 + b.a52;
        c.a53 = a.a53 + b.a53;
        c.a54 = a.a54 + b.a54;
        c.a55 = a.a55 + b.a55;
        c.a56 = a.a56 + b.a56;
        c.a61 = a.a61 + b.a61;
        c.a62 = a.a62 + b.a62;
        c.a63 = a.a63 + b.a63;
        c.a64 = a.a64 + b.a64;
        c.a65 = a.a65 + b.a65;
        c.a66 = a.a66 + b.a66;
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
    public static void addEquals( FixedMatrix6x6_64F a , FixedMatrix6x6_64F b ) {
        a.a11 += b.a11;
        a.a12 += b.a12;
        a.a13 += b.a13;
        a.a14 += b.a14;
        a.a15 += b.a15;
        a.a16 += b.a16;
        a.a21 += b.a21;
        a.a22 += b.a22;
        a.a23 += b.a23;
        a.a24 += b.a24;
        a.a25 += b.a25;
        a.a26 += b.a26;
        a.a31 += b.a31;
        a.a32 += b.a32;
        a.a33 += b.a33;
        a.a34 += b.a34;
        a.a35 += b.a35;
        a.a36 += b.a36;
        a.a41 += b.a41;
        a.a42 += b.a42;
        a.a43 += b.a43;
        a.a44 += b.a44;
        a.a45 += b.a45;
        a.a46 += b.a46;
        a.a51 += b.a51;
        a.a52 += b.a52;
        a.a53 += b.a53;
        a.a54 += b.a54;
        a.a55 += b.a55;
        a.a56 += b.a56;
        a.a61 += b.a61;
        a.a62 += b.a62;
        a.a63 += b.a63;
        a.a64 += b.a64;
        a.a65 += b.a65;
        a.a66 += b.a66;
    }

    /**
     * Performs an in-place transpose.  This algorithm is only efficient for square
     * matrices.
     *
     * @param m The matrix that is to be transposed. Modified.
     */
    public static void transpose( FixedMatrix6x6_64F m ) {
        double tmp;
        tmp = m.a12; m.a12 = m.a21; m.a21 = tmp;
        tmp = m.a13; m.a13 = m.a31; m.a31 = tmp;
        tmp = m.a14; m.a14 = m.a41; m.a41 = tmp;
        tmp = m.a15; m.a15 = m.a51; m.a51 = tmp;
        tmp = m.a16; m.a16 = m.a61; m.a61 = tmp;
        tmp = m.a23; m.a23 = m.a32; m.a32 = tmp;
        tmp = m.a24; m.a24 = m.a42; m.a42 = tmp;
        tmp = m.a25; m.a25 = m.a52; m.a52 = tmp;
        tmp = m.a26; m.a26 = m.a62; m.a62 = tmp;
        tmp = m.a34; m.a34 = m.a43; m.a43 = tmp;
        tmp = m.a35; m.a35 = m.a53; m.a53 = tmp;
        tmp = m.a36; m.a36 = m.a63; m.a63 = tmp;
        tmp = m.a45; m.a45 = m.a54; m.a54 = tmp;
        tmp = m.a46; m.a46 = m.a64; m.a64 = tmp;
        tmp = m.a56; m.a56 = m.a65; m.a65 = tmp;
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
    public static FixedMatrix6x6_64F transpose( FixedMatrix6x6_64F input , FixedMatrix6x6_64F output ) {
        if( input == null )
            input = new FixedMatrix6x6_64F();

        output.a11 = input.a11;
        output.a12 = input.a21;
        output.a13 = input.a31;
        output.a14 = input.a41;
        output.a15 = input.a51;
        output.a16 = input.a61;
        output.a21 = input.a12;
        output.a22 = input.a22;
        output.a23 = input.a32;
        output.a24 = input.a42;
        output.a25 = input.a52;
        output.a26 = input.a62;
        output.a31 = input.a13;
        output.a32 = input.a23;
        output.a33 = input.a33;
        output.a34 = input.a43;
        output.a35 = input.a53;
        output.a36 = input.a63;
        output.a41 = input.a14;
        output.a42 = input.a24;
        output.a43 = input.a34;
        output.a44 = input.a44;
        output.a45 = input.a54;
        output.a46 = input.a64;
        output.a51 = input.a15;
        output.a52 = input.a25;
        output.a53 = input.a35;
        output.a54 = input.a45;
        output.a55 = input.a55;
        output.a56 = input.a65;
        output.a61 = input.a16;
        output.a62 = input.a26;
        output.a63 = input.a36;
        output.a64 = input.a46;
        output.a65 = input.a56;
        output.a66 = input.a66;

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
    public static void mult( FixedMatrix6x6_64F a , FixedMatrix6x6_64F b , FixedMatrix6x6_64F c) {
        c.a11 = a.a11*b.a11 + a.a12*b.a21 + a.a13*b.a31 + a.a14*b.a41 + a.a15*b.a51 + a.a16*b.a61;
        c.a12 = a.a11*b.a12 + a.a12*b.a22 + a.a13*b.a32 + a.a14*b.a42 + a.a15*b.a52 + a.a16*b.a62;
        c.a13 = a.a11*b.a13 + a.a12*b.a23 + a.a13*b.a33 + a.a14*b.a43 + a.a15*b.a53 + a.a16*b.a63;
        c.a14 = a.a11*b.a14 + a.a12*b.a24 + a.a13*b.a34 + a.a14*b.a44 + a.a15*b.a54 + a.a16*b.a64;
        c.a15 = a.a11*b.a15 + a.a12*b.a25 + a.a13*b.a35 + a.a14*b.a45 + a.a15*b.a55 + a.a16*b.a65;
        c.a16 = a.a11*b.a16 + a.a12*b.a26 + a.a13*b.a36 + a.a14*b.a46 + a.a15*b.a56 + a.a16*b.a66;
        c.a21 = a.a21*b.a11 + a.a22*b.a21 + a.a23*b.a31 + a.a24*b.a41 + a.a25*b.a51 + a.a26*b.a61;
        c.a22 = a.a21*b.a12 + a.a22*b.a22 + a.a23*b.a32 + a.a24*b.a42 + a.a25*b.a52 + a.a26*b.a62;
        c.a23 = a.a21*b.a13 + a.a22*b.a23 + a.a23*b.a33 + a.a24*b.a43 + a.a25*b.a53 + a.a26*b.a63;
        c.a24 = a.a21*b.a14 + a.a22*b.a24 + a.a23*b.a34 + a.a24*b.a44 + a.a25*b.a54 + a.a26*b.a64;
        c.a25 = a.a21*b.a15 + a.a22*b.a25 + a.a23*b.a35 + a.a24*b.a45 + a.a25*b.a55 + a.a26*b.a65;
        c.a26 = a.a21*b.a16 + a.a22*b.a26 + a.a23*b.a36 + a.a24*b.a46 + a.a25*b.a56 + a.a26*b.a66;
        c.a31 = a.a31*b.a11 + a.a32*b.a21 + a.a33*b.a31 + a.a34*b.a41 + a.a35*b.a51 + a.a36*b.a61;
        c.a32 = a.a31*b.a12 + a.a32*b.a22 + a.a33*b.a32 + a.a34*b.a42 + a.a35*b.a52 + a.a36*b.a62;
        c.a33 = a.a31*b.a13 + a.a32*b.a23 + a.a33*b.a33 + a.a34*b.a43 + a.a35*b.a53 + a.a36*b.a63;
        c.a34 = a.a31*b.a14 + a.a32*b.a24 + a.a33*b.a34 + a.a34*b.a44 + a.a35*b.a54 + a.a36*b.a64;
        c.a35 = a.a31*b.a15 + a.a32*b.a25 + a.a33*b.a35 + a.a34*b.a45 + a.a35*b.a55 + a.a36*b.a65;
        c.a36 = a.a31*b.a16 + a.a32*b.a26 + a.a33*b.a36 + a.a34*b.a46 + a.a35*b.a56 + a.a36*b.a66;
        c.a41 = a.a41*b.a11 + a.a42*b.a21 + a.a43*b.a31 + a.a44*b.a41 + a.a45*b.a51 + a.a46*b.a61;
        c.a42 = a.a41*b.a12 + a.a42*b.a22 + a.a43*b.a32 + a.a44*b.a42 + a.a45*b.a52 + a.a46*b.a62;
        c.a43 = a.a41*b.a13 + a.a42*b.a23 + a.a43*b.a33 + a.a44*b.a43 + a.a45*b.a53 + a.a46*b.a63;
        c.a44 = a.a41*b.a14 + a.a42*b.a24 + a.a43*b.a34 + a.a44*b.a44 + a.a45*b.a54 + a.a46*b.a64;
        c.a45 = a.a41*b.a15 + a.a42*b.a25 + a.a43*b.a35 + a.a44*b.a45 + a.a45*b.a55 + a.a46*b.a65;
        c.a46 = a.a41*b.a16 + a.a42*b.a26 + a.a43*b.a36 + a.a44*b.a46 + a.a45*b.a56 + a.a46*b.a66;
        c.a51 = a.a51*b.a11 + a.a52*b.a21 + a.a53*b.a31 + a.a54*b.a41 + a.a55*b.a51 + a.a56*b.a61;
        c.a52 = a.a51*b.a12 + a.a52*b.a22 + a.a53*b.a32 + a.a54*b.a42 + a.a55*b.a52 + a.a56*b.a62;
        c.a53 = a.a51*b.a13 + a.a52*b.a23 + a.a53*b.a33 + a.a54*b.a43 + a.a55*b.a53 + a.a56*b.a63;
        c.a54 = a.a51*b.a14 + a.a52*b.a24 + a.a53*b.a34 + a.a54*b.a44 + a.a55*b.a54 + a.a56*b.a64;
        c.a55 = a.a51*b.a15 + a.a52*b.a25 + a.a53*b.a35 + a.a54*b.a45 + a.a55*b.a55 + a.a56*b.a65;
        c.a56 = a.a51*b.a16 + a.a52*b.a26 + a.a53*b.a36 + a.a54*b.a46 + a.a55*b.a56 + a.a56*b.a66;
        c.a61 = a.a61*b.a11 + a.a62*b.a21 + a.a63*b.a31 + a.a64*b.a41 + a.a65*b.a51 + a.a66*b.a61;
        c.a62 = a.a61*b.a12 + a.a62*b.a22 + a.a63*b.a32 + a.a64*b.a42 + a.a65*b.a52 + a.a66*b.a62;
        c.a63 = a.a61*b.a13 + a.a62*b.a23 + a.a63*b.a33 + a.a64*b.a43 + a.a65*b.a53 + a.a66*b.a63;
        c.a64 = a.a61*b.a14 + a.a62*b.a24 + a.a63*b.a34 + a.a64*b.a44 + a.a65*b.a54 + a.a66*b.a64;
        c.a65 = a.a61*b.a15 + a.a62*b.a25 + a.a63*b.a35 + a.a64*b.a45 + a.a65*b.a55 + a.a66*b.a65;
        c.a66 = a.a61*b.a16 + a.a62*b.a26 + a.a63*b.a36 + a.a64*b.a46 + a.a65*b.a56 + a.a66*b.a66;
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
    public static void multTransA( FixedMatrix6x6_64F a , FixedMatrix6x6_64F b , FixedMatrix6x6_64F c) {
        c.a11 = a.a11*b.a11 + a.a21*b.a21 + a.a31*b.a31 + a.a41*b.a41 + a.a51*b.a51 + a.a61*b.a61;
        c.a12 = a.a11*b.a12 + a.a21*b.a22 + a.a31*b.a32 + a.a41*b.a42 + a.a51*b.a52 + a.a61*b.a62;
        c.a13 = a.a11*b.a13 + a.a21*b.a23 + a.a31*b.a33 + a.a41*b.a43 + a.a51*b.a53 + a.a61*b.a63;
        c.a14 = a.a11*b.a14 + a.a21*b.a24 + a.a31*b.a34 + a.a41*b.a44 + a.a51*b.a54 + a.a61*b.a64;
        c.a15 = a.a11*b.a15 + a.a21*b.a25 + a.a31*b.a35 + a.a41*b.a45 + a.a51*b.a55 + a.a61*b.a65;
        c.a16 = a.a11*b.a16 + a.a21*b.a26 + a.a31*b.a36 + a.a41*b.a46 + a.a51*b.a56 + a.a61*b.a66;
        c.a21 = a.a12*b.a11 + a.a22*b.a21 + a.a32*b.a31 + a.a42*b.a41 + a.a52*b.a51 + a.a62*b.a61;
        c.a22 = a.a12*b.a12 + a.a22*b.a22 + a.a32*b.a32 + a.a42*b.a42 + a.a52*b.a52 + a.a62*b.a62;
        c.a23 = a.a12*b.a13 + a.a22*b.a23 + a.a32*b.a33 + a.a42*b.a43 + a.a52*b.a53 + a.a62*b.a63;
        c.a24 = a.a12*b.a14 + a.a22*b.a24 + a.a32*b.a34 + a.a42*b.a44 + a.a52*b.a54 + a.a62*b.a64;
        c.a25 = a.a12*b.a15 + a.a22*b.a25 + a.a32*b.a35 + a.a42*b.a45 + a.a52*b.a55 + a.a62*b.a65;
        c.a26 = a.a12*b.a16 + a.a22*b.a26 + a.a32*b.a36 + a.a42*b.a46 + a.a52*b.a56 + a.a62*b.a66;
        c.a31 = a.a13*b.a11 + a.a23*b.a21 + a.a33*b.a31 + a.a43*b.a41 + a.a53*b.a51 + a.a63*b.a61;
        c.a32 = a.a13*b.a12 + a.a23*b.a22 + a.a33*b.a32 + a.a43*b.a42 + a.a53*b.a52 + a.a63*b.a62;
        c.a33 = a.a13*b.a13 + a.a23*b.a23 + a.a33*b.a33 + a.a43*b.a43 + a.a53*b.a53 + a.a63*b.a63;
        c.a34 = a.a13*b.a14 + a.a23*b.a24 + a.a33*b.a34 + a.a43*b.a44 + a.a53*b.a54 + a.a63*b.a64;
        c.a35 = a.a13*b.a15 + a.a23*b.a25 + a.a33*b.a35 + a.a43*b.a45 + a.a53*b.a55 + a.a63*b.a65;
        c.a36 = a.a13*b.a16 + a.a23*b.a26 + a.a33*b.a36 + a.a43*b.a46 + a.a53*b.a56 + a.a63*b.a66;
        c.a41 = a.a14*b.a11 + a.a24*b.a21 + a.a34*b.a31 + a.a44*b.a41 + a.a54*b.a51 + a.a64*b.a61;
        c.a42 = a.a14*b.a12 + a.a24*b.a22 + a.a34*b.a32 + a.a44*b.a42 + a.a54*b.a52 + a.a64*b.a62;
        c.a43 = a.a14*b.a13 + a.a24*b.a23 + a.a34*b.a33 + a.a44*b.a43 + a.a54*b.a53 + a.a64*b.a63;
        c.a44 = a.a14*b.a14 + a.a24*b.a24 + a.a34*b.a34 + a.a44*b.a44 + a.a54*b.a54 + a.a64*b.a64;
        c.a45 = a.a14*b.a15 + a.a24*b.a25 + a.a34*b.a35 + a.a44*b.a45 + a.a54*b.a55 + a.a64*b.a65;
        c.a46 = a.a14*b.a16 + a.a24*b.a26 + a.a34*b.a36 + a.a44*b.a46 + a.a54*b.a56 + a.a64*b.a66;
        c.a51 = a.a15*b.a11 + a.a25*b.a21 + a.a35*b.a31 + a.a45*b.a41 + a.a55*b.a51 + a.a65*b.a61;
        c.a52 = a.a15*b.a12 + a.a25*b.a22 + a.a35*b.a32 + a.a45*b.a42 + a.a55*b.a52 + a.a65*b.a62;
        c.a53 = a.a15*b.a13 + a.a25*b.a23 + a.a35*b.a33 + a.a45*b.a43 + a.a55*b.a53 + a.a65*b.a63;
        c.a54 = a.a15*b.a14 + a.a25*b.a24 + a.a35*b.a34 + a.a45*b.a44 + a.a55*b.a54 + a.a65*b.a64;
        c.a55 = a.a15*b.a15 + a.a25*b.a25 + a.a35*b.a35 + a.a45*b.a45 + a.a55*b.a55 + a.a65*b.a65;
        c.a56 = a.a15*b.a16 + a.a25*b.a26 + a.a35*b.a36 + a.a45*b.a46 + a.a55*b.a56 + a.a65*b.a66;
        c.a61 = a.a16*b.a11 + a.a26*b.a21 + a.a36*b.a31 + a.a46*b.a41 + a.a56*b.a51 + a.a66*b.a61;
        c.a62 = a.a16*b.a12 + a.a26*b.a22 + a.a36*b.a32 + a.a46*b.a42 + a.a56*b.a52 + a.a66*b.a62;
        c.a63 = a.a16*b.a13 + a.a26*b.a23 + a.a36*b.a33 + a.a46*b.a43 + a.a56*b.a53 + a.a66*b.a63;
        c.a64 = a.a16*b.a14 + a.a26*b.a24 + a.a36*b.a34 + a.a46*b.a44 + a.a56*b.a54 + a.a66*b.a64;
        c.a65 = a.a16*b.a15 + a.a26*b.a25 + a.a36*b.a35 + a.a46*b.a45 + a.a56*b.a55 + a.a66*b.a65;
        c.a66 = a.a16*b.a16 + a.a26*b.a26 + a.a36*b.a36 + a.a46*b.a46 + a.a56*b.a56 + a.a66*b.a66;
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
    public static void multTransAB( FixedMatrix6x6_64F a , FixedMatrix6x6_64F b , FixedMatrix6x6_64F c) {
        c.a11 = a.a11*b.a11 + a.a21*b.a12 + a.a31*b.a13 + a.a41*b.a14 + a.a51*b.a15 + a.a61*b.a16;
        c.a12 = a.a11*b.a21 + a.a21*b.a22 + a.a31*b.a23 + a.a41*b.a24 + a.a51*b.a25 + a.a61*b.a26;
        c.a13 = a.a11*b.a31 + a.a21*b.a32 + a.a31*b.a33 + a.a41*b.a34 + a.a51*b.a35 + a.a61*b.a36;
        c.a14 = a.a11*b.a41 + a.a21*b.a42 + a.a31*b.a43 + a.a41*b.a44 + a.a51*b.a45 + a.a61*b.a46;
        c.a15 = a.a11*b.a51 + a.a21*b.a52 + a.a31*b.a53 + a.a41*b.a54 + a.a51*b.a55 + a.a61*b.a56;
        c.a16 = a.a11*b.a61 + a.a21*b.a62 + a.a31*b.a63 + a.a41*b.a64 + a.a51*b.a65 + a.a61*b.a66;
        c.a21 = a.a12*b.a11 + a.a22*b.a12 + a.a32*b.a13 + a.a42*b.a14 + a.a52*b.a15 + a.a62*b.a16;
        c.a22 = a.a12*b.a21 + a.a22*b.a22 + a.a32*b.a23 + a.a42*b.a24 + a.a52*b.a25 + a.a62*b.a26;
        c.a23 = a.a12*b.a31 + a.a22*b.a32 + a.a32*b.a33 + a.a42*b.a34 + a.a52*b.a35 + a.a62*b.a36;
        c.a24 = a.a12*b.a41 + a.a22*b.a42 + a.a32*b.a43 + a.a42*b.a44 + a.a52*b.a45 + a.a62*b.a46;
        c.a25 = a.a12*b.a51 + a.a22*b.a52 + a.a32*b.a53 + a.a42*b.a54 + a.a52*b.a55 + a.a62*b.a56;
        c.a26 = a.a12*b.a61 + a.a22*b.a62 + a.a32*b.a63 + a.a42*b.a64 + a.a52*b.a65 + a.a62*b.a66;
        c.a31 = a.a13*b.a11 + a.a23*b.a12 + a.a33*b.a13 + a.a43*b.a14 + a.a53*b.a15 + a.a63*b.a16;
        c.a32 = a.a13*b.a21 + a.a23*b.a22 + a.a33*b.a23 + a.a43*b.a24 + a.a53*b.a25 + a.a63*b.a26;
        c.a33 = a.a13*b.a31 + a.a23*b.a32 + a.a33*b.a33 + a.a43*b.a34 + a.a53*b.a35 + a.a63*b.a36;
        c.a34 = a.a13*b.a41 + a.a23*b.a42 + a.a33*b.a43 + a.a43*b.a44 + a.a53*b.a45 + a.a63*b.a46;
        c.a35 = a.a13*b.a51 + a.a23*b.a52 + a.a33*b.a53 + a.a43*b.a54 + a.a53*b.a55 + a.a63*b.a56;
        c.a36 = a.a13*b.a61 + a.a23*b.a62 + a.a33*b.a63 + a.a43*b.a64 + a.a53*b.a65 + a.a63*b.a66;
        c.a41 = a.a14*b.a11 + a.a24*b.a12 + a.a34*b.a13 + a.a44*b.a14 + a.a54*b.a15 + a.a64*b.a16;
        c.a42 = a.a14*b.a21 + a.a24*b.a22 + a.a34*b.a23 + a.a44*b.a24 + a.a54*b.a25 + a.a64*b.a26;
        c.a43 = a.a14*b.a31 + a.a24*b.a32 + a.a34*b.a33 + a.a44*b.a34 + a.a54*b.a35 + a.a64*b.a36;
        c.a44 = a.a14*b.a41 + a.a24*b.a42 + a.a34*b.a43 + a.a44*b.a44 + a.a54*b.a45 + a.a64*b.a46;
        c.a45 = a.a14*b.a51 + a.a24*b.a52 + a.a34*b.a53 + a.a44*b.a54 + a.a54*b.a55 + a.a64*b.a56;
        c.a46 = a.a14*b.a61 + a.a24*b.a62 + a.a34*b.a63 + a.a44*b.a64 + a.a54*b.a65 + a.a64*b.a66;
        c.a51 = a.a15*b.a11 + a.a25*b.a12 + a.a35*b.a13 + a.a45*b.a14 + a.a55*b.a15 + a.a65*b.a16;
        c.a52 = a.a15*b.a21 + a.a25*b.a22 + a.a35*b.a23 + a.a45*b.a24 + a.a55*b.a25 + a.a65*b.a26;
        c.a53 = a.a15*b.a31 + a.a25*b.a32 + a.a35*b.a33 + a.a45*b.a34 + a.a55*b.a35 + a.a65*b.a36;
        c.a54 = a.a15*b.a41 + a.a25*b.a42 + a.a35*b.a43 + a.a45*b.a44 + a.a55*b.a45 + a.a65*b.a46;
        c.a55 = a.a15*b.a51 + a.a25*b.a52 + a.a35*b.a53 + a.a45*b.a54 + a.a55*b.a55 + a.a65*b.a56;
        c.a56 = a.a15*b.a61 + a.a25*b.a62 + a.a35*b.a63 + a.a45*b.a64 + a.a55*b.a65 + a.a65*b.a66;
        c.a61 = a.a16*b.a11 + a.a26*b.a12 + a.a36*b.a13 + a.a46*b.a14 + a.a56*b.a15 + a.a66*b.a16;
        c.a62 = a.a16*b.a21 + a.a26*b.a22 + a.a36*b.a23 + a.a46*b.a24 + a.a56*b.a25 + a.a66*b.a26;
        c.a63 = a.a16*b.a31 + a.a26*b.a32 + a.a36*b.a33 + a.a46*b.a34 + a.a56*b.a35 + a.a66*b.a36;
        c.a64 = a.a16*b.a41 + a.a26*b.a42 + a.a36*b.a43 + a.a46*b.a44 + a.a56*b.a45 + a.a66*b.a46;
        c.a65 = a.a16*b.a51 + a.a26*b.a52 + a.a36*b.a53 + a.a46*b.a54 + a.a56*b.a55 + a.a66*b.a56;
        c.a66 = a.a16*b.a61 + a.a26*b.a62 + a.a36*b.a63 + a.a46*b.a64 + a.a56*b.a65 + a.a66*b.a66;
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
    public static void multTransB( FixedMatrix6x6_64F a , FixedMatrix6x6_64F b , FixedMatrix6x6_64F c) {
        c.a11 = a.a11*b.a11 + a.a12*b.a12 + a.a13*b.a13 + a.a14*b.a14 + a.a15*b.a15 + a.a16*b.a16;
        c.a12 = a.a11*b.a21 + a.a12*b.a22 + a.a13*b.a23 + a.a14*b.a24 + a.a15*b.a25 + a.a16*b.a26;
        c.a13 = a.a11*b.a31 + a.a12*b.a32 + a.a13*b.a33 + a.a14*b.a34 + a.a15*b.a35 + a.a16*b.a36;
        c.a14 = a.a11*b.a41 + a.a12*b.a42 + a.a13*b.a43 + a.a14*b.a44 + a.a15*b.a45 + a.a16*b.a46;
        c.a15 = a.a11*b.a51 + a.a12*b.a52 + a.a13*b.a53 + a.a14*b.a54 + a.a15*b.a55 + a.a16*b.a56;
        c.a16 = a.a11*b.a61 + a.a12*b.a62 + a.a13*b.a63 + a.a14*b.a64 + a.a15*b.a65 + a.a16*b.a66;
        c.a21 = a.a21*b.a11 + a.a22*b.a12 + a.a23*b.a13 + a.a24*b.a14 + a.a25*b.a15 + a.a26*b.a16;
        c.a22 = a.a21*b.a21 + a.a22*b.a22 + a.a23*b.a23 + a.a24*b.a24 + a.a25*b.a25 + a.a26*b.a26;
        c.a23 = a.a21*b.a31 + a.a22*b.a32 + a.a23*b.a33 + a.a24*b.a34 + a.a25*b.a35 + a.a26*b.a36;
        c.a24 = a.a21*b.a41 + a.a22*b.a42 + a.a23*b.a43 + a.a24*b.a44 + a.a25*b.a45 + a.a26*b.a46;
        c.a25 = a.a21*b.a51 + a.a22*b.a52 + a.a23*b.a53 + a.a24*b.a54 + a.a25*b.a55 + a.a26*b.a56;
        c.a26 = a.a21*b.a61 + a.a22*b.a62 + a.a23*b.a63 + a.a24*b.a64 + a.a25*b.a65 + a.a26*b.a66;
        c.a31 = a.a31*b.a11 + a.a32*b.a12 + a.a33*b.a13 + a.a34*b.a14 + a.a35*b.a15 + a.a36*b.a16;
        c.a32 = a.a31*b.a21 + a.a32*b.a22 + a.a33*b.a23 + a.a34*b.a24 + a.a35*b.a25 + a.a36*b.a26;
        c.a33 = a.a31*b.a31 + a.a32*b.a32 + a.a33*b.a33 + a.a34*b.a34 + a.a35*b.a35 + a.a36*b.a36;
        c.a34 = a.a31*b.a41 + a.a32*b.a42 + a.a33*b.a43 + a.a34*b.a44 + a.a35*b.a45 + a.a36*b.a46;
        c.a35 = a.a31*b.a51 + a.a32*b.a52 + a.a33*b.a53 + a.a34*b.a54 + a.a35*b.a55 + a.a36*b.a56;
        c.a36 = a.a31*b.a61 + a.a32*b.a62 + a.a33*b.a63 + a.a34*b.a64 + a.a35*b.a65 + a.a36*b.a66;
        c.a41 = a.a41*b.a11 + a.a42*b.a12 + a.a43*b.a13 + a.a44*b.a14 + a.a45*b.a15 + a.a46*b.a16;
        c.a42 = a.a41*b.a21 + a.a42*b.a22 + a.a43*b.a23 + a.a44*b.a24 + a.a45*b.a25 + a.a46*b.a26;
        c.a43 = a.a41*b.a31 + a.a42*b.a32 + a.a43*b.a33 + a.a44*b.a34 + a.a45*b.a35 + a.a46*b.a36;
        c.a44 = a.a41*b.a41 + a.a42*b.a42 + a.a43*b.a43 + a.a44*b.a44 + a.a45*b.a45 + a.a46*b.a46;
        c.a45 = a.a41*b.a51 + a.a42*b.a52 + a.a43*b.a53 + a.a44*b.a54 + a.a45*b.a55 + a.a46*b.a56;
        c.a46 = a.a41*b.a61 + a.a42*b.a62 + a.a43*b.a63 + a.a44*b.a64 + a.a45*b.a65 + a.a46*b.a66;
        c.a51 = a.a51*b.a11 + a.a52*b.a12 + a.a53*b.a13 + a.a54*b.a14 + a.a55*b.a15 + a.a56*b.a16;
        c.a52 = a.a51*b.a21 + a.a52*b.a22 + a.a53*b.a23 + a.a54*b.a24 + a.a55*b.a25 + a.a56*b.a26;
        c.a53 = a.a51*b.a31 + a.a52*b.a32 + a.a53*b.a33 + a.a54*b.a34 + a.a55*b.a35 + a.a56*b.a36;
        c.a54 = a.a51*b.a41 + a.a52*b.a42 + a.a53*b.a43 + a.a54*b.a44 + a.a55*b.a45 + a.a56*b.a46;
        c.a55 = a.a51*b.a51 + a.a52*b.a52 + a.a53*b.a53 + a.a54*b.a54 + a.a55*b.a55 + a.a56*b.a56;
        c.a56 = a.a51*b.a61 + a.a52*b.a62 + a.a53*b.a63 + a.a54*b.a64 + a.a55*b.a65 + a.a56*b.a66;
        c.a61 = a.a61*b.a11 + a.a62*b.a12 + a.a63*b.a13 + a.a64*b.a14 + a.a65*b.a15 + a.a66*b.a16;
        c.a62 = a.a61*b.a21 + a.a62*b.a22 + a.a63*b.a23 + a.a64*b.a24 + a.a65*b.a25 + a.a66*b.a26;
        c.a63 = a.a61*b.a31 + a.a62*b.a32 + a.a63*b.a33 + a.a64*b.a34 + a.a65*b.a35 + a.a66*b.a36;
        c.a64 = a.a61*b.a41 + a.a62*b.a42 + a.a63*b.a43 + a.a64*b.a44 + a.a65*b.a45 + a.a66*b.a46;
        c.a65 = a.a61*b.a51 + a.a62*b.a52 + a.a63*b.a53 + a.a64*b.a54 + a.a65*b.a55 + a.a66*b.a56;
        c.a66 = a.a61*b.a61 + a.a62*b.a62 + a.a63*b.a63 + a.a64*b.a64 + a.a65*b.a65 + a.a66*b.a66;
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
    public static void mult( FixedMatrix6x6_64F a , FixedMatrix6_64F b , FixedMatrix6_64F c) {
        c.a1 = a.a11*b.a1 + a.a12*b.a2 + a.a13*b.a3 + a.a14*b.a4 + a.a15*b.a5 + a.a16*b.a6;
        c.a2 = a.a21*b.a1 + a.a22*b.a2 + a.a23*b.a3 + a.a24*b.a4 + a.a25*b.a5 + a.a26*b.a6;
        c.a3 = a.a31*b.a1 + a.a32*b.a2 + a.a33*b.a3 + a.a34*b.a4 + a.a35*b.a5 + a.a36*b.a6;
        c.a4 = a.a41*b.a1 + a.a42*b.a2 + a.a43*b.a3 + a.a44*b.a4 + a.a45*b.a5 + a.a46*b.a6;
        c.a5 = a.a51*b.a1 + a.a52*b.a2 + a.a53*b.a3 + a.a54*b.a4 + a.a55*b.a5 + a.a56*b.a6;
        c.a6 = a.a61*b.a1 + a.a62*b.a2 + a.a63*b.a3 + a.a64*b.a4 + a.a65*b.a5 + a.a66*b.a6;
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
    public static void mult( FixedMatrix6_64F a , FixedMatrix6x6_64F b , FixedMatrix6_64F c) {
        c.a1 = a.a1*b.a11 + a.a2*b.a21 + a.a3*b.a31 + a.a4*b.a41 + a.a5*b.a51 + a.a6*b.a61;
        c.a2 = a.a1*b.a12 + a.a2*b.a22 + a.a3*b.a32 + a.a4*b.a42 + a.a5*b.a52 + a.a6*b.a62;
        c.a3 = a.a1*b.a13 + a.a2*b.a23 + a.a3*b.a33 + a.a4*b.a43 + a.a5*b.a53 + a.a6*b.a63;
        c.a4 = a.a1*b.a14 + a.a2*b.a24 + a.a3*b.a34 + a.a4*b.a44 + a.a5*b.a54 + a.a6*b.a64;
        c.a5 = a.a1*b.a15 + a.a2*b.a25 + a.a3*b.a35 + a.a4*b.a45 + a.a5*b.a55 + a.a6*b.a65;
        c.a6 = a.a1*b.a16 + a.a2*b.a26 + a.a3*b.a36 + a.a4*b.a46 + a.a5*b.a56 + a.a6*b.a66;
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
    public static double dot( FixedMatrix6_64F a , FixedMatrix6_64F b ) {
        return a.a1*b.a1 + a.a2*b.a2 + a.a3*b.a3 + a.a4*b.a4 + a.a5*b.a5 + a.a6*b.a6;
    }

    /**
     * Sets all the diagonal elements equal to one and everything else equal to zero.
     * If this is a square matrix then it will be an identity matrix.
     *
     * @param a A matrix.
     */
    public static void setIdentity( FixedMatrix6x6_64F a ) {
        a.a11 = 1; a.a21 = 0; a.a31 = 0; a.a41 = 0; a.a51 = 0; a.a61 = 0;
        a.a12 = 0; a.a22 = 1; a.a32 = 0; a.a42 = 0; a.a52 = 0; a.a62 = 0;
        a.a13 = 0; a.a23 = 0; a.a33 = 1; a.a43 = 0; a.a53 = 0; a.a63 = 0;
        a.a14 = 0; a.a24 = 0; a.a34 = 0; a.a44 = 1; a.a54 = 0; a.a64 = 0;
        a.a15 = 0; a.a25 = 0; a.a35 = 0; a.a45 = 0; a.a55 = 1; a.a65 = 0;
        a.a16 = 0; a.a26 = 0; a.a36 = 0; a.a46 = 0; a.a56 = 0; a.a66 = 1;
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
    public static double trace( FixedMatrix6x6_64F a ) {
        return a.a11 + a.a21 + a.a31 + a.a41 + a.a51 + a.a61;
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
    public static void diag( FixedMatrix6x6_64F input , FixedMatrix6_64F out ) {
        out.a1 = input.a11;
        out.a2 = input.a22;
        out.a3 = input.a33;
        out.a4 = input.a44;
        out.a5 = input.a55;
        out.a6 = input.a66;
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
    public static double elementMax( FixedMatrix6x6_64F a ) {
        double max = a.a11;
        max = Math.max(max,a.a12);
        max = Math.max(max,a.a13);
        max = Math.max(max,a.a14);
        max = Math.max(max,a.a15);
        max = Math.max(max,a.a16);
        max = Math.max(max,a.a21);
        max = Math.max(max,a.a22);
        max = Math.max(max,a.a23);
        max = Math.max(max,a.a24);
        max = Math.max(max,a.a25);
        max = Math.max(max,a.a26);
        max = Math.max(max,a.a31);
        max = Math.max(max,a.a32);
        max = Math.max(max,a.a33);
        max = Math.max(max,a.a34);
        max = Math.max(max,a.a35);
        max = Math.max(max,a.a36);
        max = Math.max(max,a.a41);
        max = Math.max(max,a.a42);
        max = Math.max(max,a.a43);
        max = Math.max(max,a.a44);
        max = Math.max(max,a.a45);
        max = Math.max(max,a.a46);
        max = Math.max(max,a.a51);
        max = Math.max(max,a.a52);
        max = Math.max(max,a.a53);
        max = Math.max(max,a.a54);
        max = Math.max(max,a.a55);
        max = Math.max(max,a.a56);
        max = Math.max(max,a.a61);
        max = Math.max(max,a.a62);
        max = Math.max(max,a.a63);
        max = Math.max(max,a.a64);
        max = Math.max(max,a.a65);
        max = Math.max(max,a.a66);

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
    public static double elementMaxAbs( FixedMatrix6x6_64F a ) {
        double max = a.a11;
        max = Math.max(max,Math.abs(a.a12));
        max = Math.max(max,Math.abs(a.a13));
        max = Math.max(max,Math.abs(a.a14));
        max = Math.max(max,Math.abs(a.a15));
        max = Math.max(max,Math.abs(a.a16));
        max = Math.max(max,Math.abs(a.a21));
        max = Math.max(max,Math.abs(a.a22));
        max = Math.max(max,Math.abs(a.a23));
        max = Math.max(max,Math.abs(a.a24));
        max = Math.max(max,Math.abs(a.a25));
        max = Math.max(max,Math.abs(a.a26));
        max = Math.max(max,Math.abs(a.a31));
        max = Math.max(max,Math.abs(a.a32));
        max = Math.max(max,Math.abs(a.a33));
        max = Math.max(max,Math.abs(a.a34));
        max = Math.max(max,Math.abs(a.a35));
        max = Math.max(max,Math.abs(a.a36));
        max = Math.max(max,Math.abs(a.a41));
        max = Math.max(max,Math.abs(a.a42));
        max = Math.max(max,Math.abs(a.a43));
        max = Math.max(max,Math.abs(a.a44));
        max = Math.max(max,Math.abs(a.a45));
        max = Math.max(max,Math.abs(a.a46));
        max = Math.max(max,Math.abs(a.a51));
        max = Math.max(max,Math.abs(a.a52));
        max = Math.max(max,Math.abs(a.a53));
        max = Math.max(max,Math.abs(a.a54));
        max = Math.max(max,Math.abs(a.a55));
        max = Math.max(max,Math.abs(a.a56));
        max = Math.max(max,Math.abs(a.a61));
        max = Math.max(max,Math.abs(a.a62));
        max = Math.max(max,Math.abs(a.a63));
        max = Math.max(max,Math.abs(a.a64));
        max = Math.max(max,Math.abs(a.a65));
        max = Math.max(max,Math.abs(a.a66));

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
    public static double elementMin( FixedMatrix6x6_64F a ) {
        double min = a.a11;
        min = Math.min(min,a.a12);
        min = Math.min(min,a.a13);
        min = Math.min(min,a.a14);
        min = Math.min(min,a.a15);
        min = Math.min(min,a.a16);
        min = Math.min(min,a.a21);
        min = Math.min(min,a.a22);
        min = Math.min(min,a.a23);
        min = Math.min(min,a.a24);
        min = Math.min(min,a.a25);
        min = Math.min(min,a.a26);
        min = Math.min(min,a.a31);
        min = Math.min(min,a.a32);
        min = Math.min(min,a.a33);
        min = Math.min(min,a.a34);
        min = Math.min(min,a.a35);
        min = Math.min(min,a.a36);
        min = Math.min(min,a.a41);
        min = Math.min(min,a.a42);
        min = Math.min(min,a.a43);
        min = Math.min(min,a.a44);
        min = Math.min(min,a.a45);
        min = Math.min(min,a.a46);
        min = Math.min(min,a.a51);
        min = Math.min(min,a.a52);
        min = Math.min(min,a.a53);
        min = Math.min(min,a.a54);
        min = Math.min(min,a.a55);
        min = Math.min(min,a.a56);
        min = Math.min(min,a.a61);
        min = Math.min(min,a.a62);
        min = Math.min(min,a.a63);
        min = Math.min(min,a.a64);
        min = Math.min(min,a.a65);
        min = Math.min(min,a.a66);

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
    public static double elementMinAbs( FixedMatrix6x6_64F a ) {
        double min = a.a11;
        min = Math.min(min,Math.abs(a.a12));
        min = Math.min(min,Math.abs(a.a13));
        min = Math.min(min,Math.abs(a.a14));
        min = Math.min(min,Math.abs(a.a15));
        min = Math.min(min,Math.abs(a.a16));
        min = Math.min(min,Math.abs(a.a21));
        min = Math.min(min,Math.abs(a.a22));
        min = Math.min(min,Math.abs(a.a23));
        min = Math.min(min,Math.abs(a.a24));
        min = Math.min(min,Math.abs(a.a25));
        min = Math.min(min,Math.abs(a.a26));
        min = Math.min(min,Math.abs(a.a31));
        min = Math.min(min,Math.abs(a.a32));
        min = Math.min(min,Math.abs(a.a33));
        min = Math.min(min,Math.abs(a.a34));
        min = Math.min(min,Math.abs(a.a35));
        min = Math.min(min,Math.abs(a.a36));
        min = Math.min(min,Math.abs(a.a41));
        min = Math.min(min,Math.abs(a.a42));
        min = Math.min(min,Math.abs(a.a43));
        min = Math.min(min,Math.abs(a.a44));
        min = Math.min(min,Math.abs(a.a45));
        min = Math.min(min,Math.abs(a.a46));
        min = Math.min(min,Math.abs(a.a51));
        min = Math.min(min,Math.abs(a.a52));
        min = Math.min(min,Math.abs(a.a53));
        min = Math.min(min,Math.abs(a.a54));
        min = Math.min(min,Math.abs(a.a55));
        min = Math.min(min,Math.abs(a.a56));
        min = Math.min(min,Math.abs(a.a61));
        min = Math.min(min,Math.abs(a.a62));
        min = Math.min(min,Math.abs(a.a63));
        min = Math.min(min,Math.abs(a.a64));
        min = Math.min(min,Math.abs(a.a65));
        min = Math.min(min,Math.abs(a.a66));

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
    public static void elementMult( FixedMatrix6x6_64F a , FixedMatrix6x6_64F b) {
        a.a11 *= b.a11; a.a12 *= b.a12; a.a13 *= b.a13; a.a14 *= b.a14; a.a15 *= b.a15; a.a16 *= b.a16;
        a.a21 *= b.a21; a.a22 *= b.a22; a.a23 *= b.a23; a.a24 *= b.a24; a.a25 *= b.a25; a.a26 *= b.a26;
        a.a31 *= b.a31; a.a32 *= b.a32; a.a33 *= b.a33; a.a34 *= b.a34; a.a35 *= b.a35; a.a36 *= b.a36;
        a.a41 *= b.a41; a.a42 *= b.a42; a.a43 *= b.a43; a.a44 *= b.a44; a.a45 *= b.a45; a.a46 *= b.a46;
        a.a51 *= b.a51; a.a52 *= b.a52; a.a53 *= b.a53; a.a54 *= b.a54; a.a55 *= b.a55; a.a56 *= b.a56;
        a.a61 *= b.a61; a.a62 *= b.a62; a.a63 *= b.a63; a.a64 *= b.a64; a.a65 *= b.a65; a.a66 *= b.a66;
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
    public static void elementMult( FixedMatrix6x6_64F a , FixedMatrix6x6_64F b , FixedMatrix6x6_64F c ) {
        c.a11 = a.a11*b.a11; c.a12 = a.a12*b.a12; c.a13 = a.a13*b.a13; c.a14 = a.a14*b.a14; c.a15 = a.a15*b.a15; c.a16 = a.a16*b.a16;
        c.a21 = a.a21*b.a21; c.a22 = a.a22*b.a22; c.a23 = a.a23*b.a23; c.a24 = a.a24*b.a24; c.a25 = a.a25*b.a25; c.a26 = a.a26*b.a26;
        c.a31 = a.a31*b.a31; c.a32 = a.a32*b.a32; c.a33 = a.a33*b.a33; c.a34 = a.a34*b.a34; c.a35 = a.a35*b.a35; c.a36 = a.a36*b.a36;
        c.a41 = a.a41*b.a41; c.a42 = a.a42*b.a42; c.a43 = a.a43*b.a43; c.a44 = a.a44*b.a44; c.a45 = a.a45*b.a45; c.a46 = a.a46*b.a46;
        c.a51 = a.a51*b.a51; c.a52 = a.a52*b.a52; c.a53 = a.a53*b.a53; c.a54 = a.a54*b.a54; c.a55 = a.a55*b.a55; c.a56 = a.a56*b.a56;
        c.a61 = a.a61*b.a61; c.a62 = a.a62*b.a62; c.a63 = a.a63*b.a63; c.a64 = a.a64*b.a64; c.a65 = a.a65*b.a65; c.a66 = a.a66*b.a66;
    }

    /**
     * <p>Performs the an element by element division operation:<br>
     * <br>
     * a<sub>ij</sub> = a<sub>ij</sub> / b<sub>ij</sub> <br>
     * </p>
     * @param a The left matrix in the division operation. Modified.
     * @param b The right matrix in the division operation. Not modified.
     */
    public static void elementDiv( FixedMatrix6x6_64F a , FixedMatrix6x6_64F b) {
        a.a11 /= b.a11; a.a12 /= b.a12; a.a13 /= b.a13; a.a14 /= b.a14; a.a15 /= b.a15; a.a16 /= b.a16;
        a.a21 /= b.a21; a.a22 /= b.a22; a.a23 /= b.a23; a.a24 /= b.a24; a.a25 /= b.a25; a.a26 /= b.a26;
        a.a31 /= b.a31; a.a32 /= b.a32; a.a33 /= b.a33; a.a34 /= b.a34; a.a35 /= b.a35; a.a36 /= b.a36;
        a.a41 /= b.a41; a.a42 /= b.a42; a.a43 /= b.a43; a.a44 /= b.a44; a.a45 /= b.a45; a.a46 /= b.a46;
        a.a51 /= b.a51; a.a52 /= b.a52; a.a53 /= b.a53; a.a54 /= b.a54; a.a55 /= b.a55; a.a56 /= b.a56;
        a.a61 /= b.a61; a.a62 /= b.a62; a.a63 /= b.a63; a.a64 /= b.a64; a.a65 /= b.a65; a.a66 /= b.a66;
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
    public static void elementDiv( FixedMatrix6x6_64F a , FixedMatrix6x6_64F b , FixedMatrix6x6_64F c ) {
        c.a11 = a.a11/b.a11; c.a12 = a.a12/b.a12; c.a13 = a.a13/b.a13; c.a14 = a.a14/b.a14; c.a15 = a.a15/b.a15; c.a16 = a.a16/b.a16;
        c.a21 = a.a21/b.a21; c.a22 = a.a22/b.a22; c.a23 = a.a23/b.a23; c.a24 = a.a24/b.a24; c.a25 = a.a25/b.a25; c.a26 = a.a26/b.a26;
        c.a31 = a.a31/b.a31; c.a32 = a.a32/b.a32; c.a33 = a.a33/b.a33; c.a34 = a.a34/b.a34; c.a35 = a.a35/b.a35; c.a36 = a.a36/b.a36;
        c.a41 = a.a41/b.a41; c.a42 = a.a42/b.a42; c.a43 = a.a43/b.a43; c.a44 = a.a44/b.a44; c.a45 = a.a45/b.a45; c.a46 = a.a46/b.a46;
        c.a51 = a.a51/b.a51; c.a52 = a.a52/b.a52; c.a53 = a.a53/b.a53; c.a54 = a.a54/b.a54; c.a55 = a.a55/b.a55; c.a56 = a.a56/b.a56;
        c.a61 = a.a61/b.a61; c.a62 = a.a62/b.a62; c.a63 = a.a63/b.a63; c.a64 = a.a64/b.a64; c.a65 = a.a65/b.a65; c.a66 = a.a66/b.a66;
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
    public static void scale( double alpha , FixedMatrix6x6_64F a ) {
        a.a11 *= alpha; a.a12 *= alpha; a.a13 *= alpha; a.a14 *= alpha; a.a15 *= alpha; a.a16 *= alpha;
        a.a21 *= alpha; a.a22 *= alpha; a.a23 *= alpha; a.a24 *= alpha; a.a25 *= alpha; a.a26 *= alpha;
        a.a31 *= alpha; a.a32 *= alpha; a.a33 *= alpha; a.a34 *= alpha; a.a35 *= alpha; a.a36 *= alpha;
        a.a41 *= alpha; a.a42 *= alpha; a.a43 *= alpha; a.a44 *= alpha; a.a45 *= alpha; a.a46 *= alpha;
        a.a51 *= alpha; a.a52 *= alpha; a.a53 *= alpha; a.a54 *= alpha; a.a55 *= alpha; a.a56 *= alpha;
        a.a61 *= alpha; a.a62 *= alpha; a.a63 *= alpha; a.a64 *= alpha; a.a65 *= alpha; a.a66 *= alpha;
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
    public static void scale( double alpha , FixedMatrix6x6_64F a , FixedMatrix6x6_64F b ) {
        b.a11 = a.a11*alpha; b.a12 = a.a12*alpha; b.a13 = a.a13*alpha; b.a14 = a.a14*alpha; b.a15 = a.a15*alpha; b.a16 = a.a16*alpha;
        b.a21 = a.a21*alpha; b.a22 = a.a22*alpha; b.a23 = a.a23*alpha; b.a24 = a.a24*alpha; b.a25 = a.a25*alpha; b.a26 = a.a26*alpha;
        b.a31 = a.a31*alpha; b.a32 = a.a32*alpha; b.a33 = a.a33*alpha; b.a34 = a.a34*alpha; b.a35 = a.a35*alpha; b.a36 = a.a36*alpha;
        b.a41 = a.a41*alpha; b.a42 = a.a42*alpha; b.a43 = a.a43*alpha; b.a44 = a.a44*alpha; b.a45 = a.a45*alpha; b.a46 = a.a46*alpha;
        b.a51 = a.a51*alpha; b.a52 = a.a52*alpha; b.a53 = a.a53*alpha; b.a54 = a.a54*alpha; b.a55 = a.a55*alpha; b.a56 = a.a56*alpha;
        b.a61 = a.a61*alpha; b.a62 = a.a62*alpha; b.a63 = a.a63*alpha; b.a64 = a.a64*alpha; b.a65 = a.a65*alpha; b.a66 = a.a66*alpha;
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
    public static void divide( FixedMatrix6x6_64F a , double alpha ) {
        a.a11 /= alpha; a.a12 /= alpha; a.a13 /= alpha; a.a14 /= alpha; a.a15 /= alpha; a.a16 /= alpha;
        a.a21 /= alpha; a.a22 /= alpha; a.a23 /= alpha; a.a24 /= alpha; a.a25 /= alpha; a.a26 /= alpha;
        a.a31 /= alpha; a.a32 /= alpha; a.a33 /= alpha; a.a34 /= alpha; a.a35 /= alpha; a.a36 /= alpha;
        a.a41 /= alpha; a.a42 /= alpha; a.a43 /= alpha; a.a44 /= alpha; a.a45 /= alpha; a.a46 /= alpha;
        a.a51 /= alpha; a.a52 /= alpha; a.a53 /= alpha; a.a54 /= alpha; a.a55 /= alpha; a.a56 /= alpha;
        a.a61 /= alpha; a.a62 /= alpha; a.a63 /= alpha; a.a64 /= alpha; a.a65 /= alpha; a.a66 /= alpha;
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
    public static void divide( FixedMatrix6x6_64F a , double alpha , FixedMatrix6x6_64F b ) {
        b.a11 = a.a11/alpha; b.a12 = a.a12/alpha; b.a13 = a.a13/alpha; b.a14 = a.a14/alpha; b.a15 = a.a15/alpha; b.a16 = a.a16/alpha;
        b.a21 = a.a21/alpha; b.a22 = a.a22/alpha; b.a23 = a.a23/alpha; b.a24 = a.a24/alpha; b.a25 = a.a25/alpha; b.a26 = a.a26/alpha;
        b.a31 = a.a31/alpha; b.a32 = a.a32/alpha; b.a33 = a.a33/alpha; b.a34 = a.a34/alpha; b.a35 = a.a35/alpha; b.a36 = a.a36/alpha;
        b.a41 = a.a41/alpha; b.a42 = a.a42/alpha; b.a43 = a.a43/alpha; b.a44 = a.a44/alpha; b.a45 = a.a45/alpha; b.a46 = a.a46/alpha;
        b.a51 = a.a51/alpha; b.a52 = a.a52/alpha; b.a53 = a.a53/alpha; b.a54 = a.a54/alpha; b.a55 = a.a55/alpha; b.a56 = a.a56/alpha;
        b.a61 = a.a61/alpha; b.a62 = a.a62/alpha; b.a63 = a.a63/alpha; b.a64 = a.a64/alpha; b.a65 = a.a65/alpha; b.a66 = a.a66/alpha;
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
    public static void changeSign( FixedMatrix6x6_64F a )
    {
        a.a11 = -a.a11; a.a12 = -a.a12; a.a13 = -a.a13; a.a14 = -a.a14; a.a15 = -a.a15; a.a16 = -a.a16;
        a.a21 = -a.a21; a.a22 = -a.a22; a.a23 = -a.a23; a.a24 = -a.a24; a.a25 = -a.a25; a.a26 = -a.a26;
        a.a31 = -a.a31; a.a32 = -a.a32; a.a33 = -a.a33; a.a34 = -a.a34; a.a35 = -a.a35; a.a36 = -a.a36;
        a.a41 = -a.a41; a.a42 = -a.a42; a.a43 = -a.a43; a.a44 = -a.a44; a.a45 = -a.a45; a.a46 = -a.a46;
        a.a51 = -a.a51; a.a52 = -a.a52; a.a53 = -a.a53; a.a54 = -a.a54; a.a55 = -a.a55; a.a56 = -a.a56;
        a.a61 = -a.a61; a.a62 = -a.a62; a.a63 = -a.a63; a.a64 = -a.a64; a.a65 = -a.a65; a.a66 = -a.a66;
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
    public static void fill( FixedMatrix6x6_64F a , double v  ) {
        a.a11 = v; a.a12 = v; a.a13 = v; a.a14 = v; a.a15 = v; a.a16 = v;
        a.a21 = v; a.a22 = v; a.a23 = v; a.a24 = v; a.a25 = v; a.a26 = v;
        a.a31 = v; a.a32 = v; a.a33 = v; a.a34 = v; a.a35 = v; a.a36 = v;
        a.a41 = v; a.a42 = v; a.a43 = v; a.a44 = v; a.a45 = v; a.a46 = v;
        a.a51 = v; a.a52 = v; a.a53 = v; a.a54 = v; a.a55 = v; a.a56 = v;
        a.a61 = v; a.a62 = v; a.a63 = v; a.a64 = v; a.a65 = v; a.a66 = v;
    }

}

