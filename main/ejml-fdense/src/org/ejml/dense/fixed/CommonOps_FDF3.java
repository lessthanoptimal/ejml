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
import org.ejml.data.FMatrix3;
import org.ejml.data.FMatrix3x3;

/**
 * <p>Common matrix operations for fixed sized matrices which are 3 x 3 or 3 element vectors.</p>
 * <p>DO NOT MODIFY.  Automatically generated code created by GenerateCommonOps_DDF</p>
 *
 * @author Peter Abeles
 */
public class CommonOps_FDF3 {
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
    public static void add( FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c ) {
        c.a11 = a.a11 + b.a11;
        c.a12 = a.a12 + b.a12;
        c.a13 = a.a13 + b.a13;
        c.a21 = a.a21 + b.a21;
        c.a22 = a.a22 + b.a22;
        c.a23 = a.a23 + b.a23;
        c.a31 = a.a31 + b.a31;
        c.a32 = a.a32 + b.a32;
        c.a33 = a.a33 + b.a33;
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
    public static void add( FMatrix3 a , FMatrix3 b , FMatrix3 c ) {
        c.a1 = a.a1 + b.a1;
        c.a2 = a.a2 + b.a2;
        c.a3 = a.a3 + b.a3;
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
    public static void addEquals( FMatrix3x3 a , FMatrix3x3 b ) {
        a.a11 += b.a11;
        a.a12 += b.a12;
        a.a13 += b.a13;
        a.a21 += b.a21;
        a.a22 += b.a22;
        a.a23 += b.a23;
        a.a31 += b.a31;
        a.a32 += b.a32;
        a.a33 += b.a33;
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
    public static void addEquals( FMatrix3 a , FMatrix3 b ) {
        a.a1 += b.a1;
        a.a2 += b.a2;
        a.a3 += b.a3;
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
    public static void subtract( FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c ) {
        c.a11 = a.a11 - b.a11;
        c.a12 = a.a12 - b.a12;
        c.a13 = a.a13 - b.a13;
        c.a21 = a.a21 - b.a21;
        c.a22 = a.a22 - b.a22;
        c.a23 = a.a23 - b.a23;
        c.a31 = a.a31 - b.a31;
        c.a32 = a.a32 - b.a32;
        c.a33 = a.a33 - b.a33;
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
    public static void subtract( FMatrix3 a , FMatrix3 b , FMatrix3 c ) {
        c.a1 = a.a1 - b.a1;
        c.a2 = a.a2 - b.a2;
        c.a3 = a.a3 - b.a3;
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
    public static void subtractEquals( FMatrix3x3 a , FMatrix3x3 b ) {
        a.a11 -= b.a11;
        a.a12 -= b.a12;
        a.a13 -= b.a13;
        a.a21 -= b.a21;
        a.a22 -= b.a22;
        a.a23 -= b.a23;
        a.a31 -= b.a31;
        a.a32 -= b.a32;
        a.a33 -= b.a33;
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
    public static void subtractEquals( FMatrix3 a , FMatrix3 b ) {
        a.a1 -= b.a1;
        a.a2 -= b.a2;
        a.a3 -= b.a3;
    }

    /**
     * Performs an in-place transpose.  This algorithm is only efficient for square
     * matrices.
     *
     * @param m The matrix that is to be transposed. Modified.
     */
    public static void transpose( FMatrix3x3 m ) {
        float tmp;
        tmp = m.a12; m.a12 = m.a21; m.a21 = tmp;
        tmp = m.a13; m.a13 = m.a31; m.a31 = tmp;
        tmp = m.a23; m.a23 = m.a32; m.a32 = tmp;
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
    public static FMatrix3x3 transpose( FMatrix3x3 input , FMatrix3x3 output ) {
        if( input == null )
            input = new FMatrix3x3();

        UtilEjml.checkSameInstance(input,output);
        output.a11 = input.a11;
        output.a12 = input.a21;
        output.a13 = input.a31;
        output.a21 = input.a12;
        output.a22 = input.a22;
        output.a23 = input.a32;
        output.a31 = input.a13;
        output.a32 = input.a23;
        output.a33 = input.a33;

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
    public static void mult( FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 = a.a11*b.a11 + a.a12*b.a21 + a.a13*b.a31;
        c.a12 = a.a11*b.a12 + a.a12*b.a22 + a.a13*b.a32;
        c.a13 = a.a11*b.a13 + a.a12*b.a23 + a.a13*b.a33;
        c.a21 = a.a21*b.a11 + a.a22*b.a21 + a.a23*b.a31;
        c.a22 = a.a21*b.a12 + a.a22*b.a22 + a.a23*b.a32;
        c.a23 = a.a21*b.a13 + a.a22*b.a23 + a.a23*b.a33;
        c.a31 = a.a31*b.a11 + a.a32*b.a21 + a.a33*b.a31;
        c.a32 = a.a31*b.a12 + a.a32*b.a22 + a.a33*b.a32;
        c.a33 = a.a31*b.a13 + a.a32*b.a23 + a.a33*b.a33;
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
    public static void mult( float alpha , FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 = alpha*(a.a11*b.a11 + a.a12*b.a21 + a.a13*b.a31);
        c.a12 = alpha*(a.a11*b.a12 + a.a12*b.a22 + a.a13*b.a32);
        c.a13 = alpha*(a.a11*b.a13 + a.a12*b.a23 + a.a13*b.a33);
        c.a21 = alpha*(a.a21*b.a11 + a.a22*b.a21 + a.a23*b.a31);
        c.a22 = alpha*(a.a21*b.a12 + a.a22*b.a22 + a.a23*b.a32);
        c.a23 = alpha*(a.a21*b.a13 + a.a22*b.a23 + a.a23*b.a33);
        c.a31 = alpha*(a.a31*b.a11 + a.a32*b.a21 + a.a33*b.a31);
        c.a32 = alpha*(a.a31*b.a12 + a.a32*b.a22 + a.a33*b.a32);
        c.a33 = alpha*(a.a31*b.a13 + a.a32*b.a23 + a.a33*b.a33);
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
    public static void multTransA( FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 = a.a11*b.a11 + a.a21*b.a21 + a.a31*b.a31;
        c.a12 = a.a11*b.a12 + a.a21*b.a22 + a.a31*b.a32;
        c.a13 = a.a11*b.a13 + a.a21*b.a23 + a.a31*b.a33;
        c.a21 = a.a12*b.a11 + a.a22*b.a21 + a.a32*b.a31;
        c.a22 = a.a12*b.a12 + a.a22*b.a22 + a.a32*b.a32;
        c.a23 = a.a12*b.a13 + a.a22*b.a23 + a.a32*b.a33;
        c.a31 = a.a13*b.a11 + a.a23*b.a21 + a.a33*b.a31;
        c.a32 = a.a13*b.a12 + a.a23*b.a22 + a.a33*b.a32;
        c.a33 = a.a13*b.a13 + a.a23*b.a23 + a.a33*b.a33;
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
    public static void multTransA( float alpha , FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 = alpha*(a.a11*b.a11 + a.a21*b.a21 + a.a31*b.a31);
        c.a12 = alpha*(a.a11*b.a12 + a.a21*b.a22 + a.a31*b.a32);
        c.a13 = alpha*(a.a11*b.a13 + a.a21*b.a23 + a.a31*b.a33);
        c.a21 = alpha*(a.a12*b.a11 + a.a22*b.a21 + a.a32*b.a31);
        c.a22 = alpha*(a.a12*b.a12 + a.a22*b.a22 + a.a32*b.a32);
        c.a23 = alpha*(a.a12*b.a13 + a.a22*b.a23 + a.a32*b.a33);
        c.a31 = alpha*(a.a13*b.a11 + a.a23*b.a21 + a.a33*b.a31);
        c.a32 = alpha*(a.a13*b.a12 + a.a23*b.a22 + a.a33*b.a32);
        c.a33 = alpha*(a.a13*b.a13 + a.a23*b.a23 + a.a33*b.a33);
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
    public static void multTransAB( FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 = a.a11*b.a11 + a.a21*b.a12 + a.a31*b.a13;
        c.a12 = a.a11*b.a21 + a.a21*b.a22 + a.a31*b.a23;
        c.a13 = a.a11*b.a31 + a.a21*b.a32 + a.a31*b.a33;
        c.a21 = a.a12*b.a11 + a.a22*b.a12 + a.a32*b.a13;
        c.a22 = a.a12*b.a21 + a.a22*b.a22 + a.a32*b.a23;
        c.a23 = a.a12*b.a31 + a.a22*b.a32 + a.a32*b.a33;
        c.a31 = a.a13*b.a11 + a.a23*b.a12 + a.a33*b.a13;
        c.a32 = a.a13*b.a21 + a.a23*b.a22 + a.a33*b.a23;
        c.a33 = a.a13*b.a31 + a.a23*b.a32 + a.a33*b.a33;
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
    public static void multTransAB( float alpha , FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 = alpha*(a.a11*b.a11 + a.a21*b.a12 + a.a31*b.a13);
        c.a12 = alpha*(a.a11*b.a21 + a.a21*b.a22 + a.a31*b.a23);
        c.a13 = alpha*(a.a11*b.a31 + a.a21*b.a32 + a.a31*b.a33);
        c.a21 = alpha*(a.a12*b.a11 + a.a22*b.a12 + a.a32*b.a13);
        c.a22 = alpha*(a.a12*b.a21 + a.a22*b.a22 + a.a32*b.a23);
        c.a23 = alpha*(a.a12*b.a31 + a.a22*b.a32 + a.a32*b.a33);
        c.a31 = alpha*(a.a13*b.a11 + a.a23*b.a12 + a.a33*b.a13);
        c.a32 = alpha*(a.a13*b.a21 + a.a23*b.a22 + a.a33*b.a23);
        c.a33 = alpha*(a.a13*b.a31 + a.a23*b.a32 + a.a33*b.a33);
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
    public static void multTransB( FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 = a.a11*b.a11 + a.a12*b.a12 + a.a13*b.a13;
        c.a12 = a.a11*b.a21 + a.a12*b.a22 + a.a13*b.a23;
        c.a13 = a.a11*b.a31 + a.a12*b.a32 + a.a13*b.a33;
        c.a21 = a.a21*b.a11 + a.a22*b.a12 + a.a23*b.a13;
        c.a22 = a.a21*b.a21 + a.a22*b.a22 + a.a23*b.a23;
        c.a23 = a.a21*b.a31 + a.a22*b.a32 + a.a23*b.a33;
        c.a31 = a.a31*b.a11 + a.a32*b.a12 + a.a33*b.a13;
        c.a32 = a.a31*b.a21 + a.a32*b.a22 + a.a33*b.a23;
        c.a33 = a.a31*b.a31 + a.a32*b.a32 + a.a33*b.a33;
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
    public static void multTransB( float alpha , FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 = alpha*(a.a11*b.a11 + a.a12*b.a12 + a.a13*b.a13);
        c.a12 = alpha*(a.a11*b.a21 + a.a12*b.a22 + a.a13*b.a23);
        c.a13 = alpha*(a.a11*b.a31 + a.a12*b.a32 + a.a13*b.a33);
        c.a21 = alpha*(a.a21*b.a11 + a.a22*b.a12 + a.a23*b.a13);
        c.a22 = alpha*(a.a21*b.a21 + a.a22*b.a22 + a.a23*b.a23);
        c.a23 = alpha*(a.a21*b.a31 + a.a22*b.a32 + a.a23*b.a33);
        c.a31 = alpha*(a.a31*b.a11 + a.a32*b.a12 + a.a33*b.a13);
        c.a32 = alpha*(a.a31*b.a21 + a.a32*b.a22 + a.a33*b.a23);
        c.a33 = alpha*(a.a31*b.a31 + a.a32*b.a32 + a.a33*b.a33);
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
    public static void multAdd( FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 += a.a11*b.a11 + a.a12*b.a21 + a.a13*b.a31;
        c.a12 += a.a11*b.a12 + a.a12*b.a22 + a.a13*b.a32;
        c.a13 += a.a11*b.a13 + a.a12*b.a23 + a.a13*b.a33;
        c.a21 += a.a21*b.a11 + a.a22*b.a21 + a.a23*b.a31;
        c.a22 += a.a21*b.a12 + a.a22*b.a22 + a.a23*b.a32;
        c.a23 += a.a21*b.a13 + a.a22*b.a23 + a.a23*b.a33;
        c.a31 += a.a31*b.a11 + a.a32*b.a21 + a.a33*b.a31;
        c.a32 += a.a31*b.a12 + a.a32*b.a22 + a.a33*b.a32;
        c.a33 += a.a31*b.a13 + a.a32*b.a23 + a.a33*b.a33;
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
    public static void multAdd( float alpha , FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 += alpha*(a.a11*b.a11 + a.a12*b.a21 + a.a13*b.a31);
        c.a12 += alpha*(a.a11*b.a12 + a.a12*b.a22 + a.a13*b.a32);
        c.a13 += alpha*(a.a11*b.a13 + a.a12*b.a23 + a.a13*b.a33);
        c.a21 += alpha*(a.a21*b.a11 + a.a22*b.a21 + a.a23*b.a31);
        c.a22 += alpha*(a.a21*b.a12 + a.a22*b.a22 + a.a23*b.a32);
        c.a23 += alpha*(a.a21*b.a13 + a.a22*b.a23 + a.a23*b.a33);
        c.a31 += alpha*(a.a31*b.a11 + a.a32*b.a21 + a.a33*b.a31);
        c.a32 += alpha*(a.a31*b.a12 + a.a32*b.a22 + a.a33*b.a32);
        c.a33 += alpha*(a.a31*b.a13 + a.a32*b.a23 + a.a33*b.a33);
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
    public static void multAddTransA( FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 += a.a11*b.a11 + a.a21*b.a21 + a.a31*b.a31;
        c.a12 += a.a11*b.a12 + a.a21*b.a22 + a.a31*b.a32;
        c.a13 += a.a11*b.a13 + a.a21*b.a23 + a.a31*b.a33;
        c.a21 += a.a12*b.a11 + a.a22*b.a21 + a.a32*b.a31;
        c.a22 += a.a12*b.a12 + a.a22*b.a22 + a.a32*b.a32;
        c.a23 += a.a12*b.a13 + a.a22*b.a23 + a.a32*b.a33;
        c.a31 += a.a13*b.a11 + a.a23*b.a21 + a.a33*b.a31;
        c.a32 += a.a13*b.a12 + a.a23*b.a22 + a.a33*b.a32;
        c.a33 += a.a13*b.a13 + a.a23*b.a23 + a.a33*b.a33;
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
    public static void multAddTransA( float alpha , FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 += alpha*(a.a11*b.a11 + a.a21*b.a21 + a.a31*b.a31);
        c.a12 += alpha*(a.a11*b.a12 + a.a21*b.a22 + a.a31*b.a32);
        c.a13 += alpha*(a.a11*b.a13 + a.a21*b.a23 + a.a31*b.a33);
        c.a21 += alpha*(a.a12*b.a11 + a.a22*b.a21 + a.a32*b.a31);
        c.a22 += alpha*(a.a12*b.a12 + a.a22*b.a22 + a.a32*b.a32);
        c.a23 += alpha*(a.a12*b.a13 + a.a22*b.a23 + a.a32*b.a33);
        c.a31 += alpha*(a.a13*b.a11 + a.a23*b.a21 + a.a33*b.a31);
        c.a32 += alpha*(a.a13*b.a12 + a.a23*b.a22 + a.a33*b.a32);
        c.a33 += alpha*(a.a13*b.a13 + a.a23*b.a23 + a.a33*b.a33);
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
    public static void multAddTransAB( FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 += a.a11*b.a11 + a.a21*b.a12 + a.a31*b.a13;
        c.a12 += a.a11*b.a21 + a.a21*b.a22 + a.a31*b.a23;
        c.a13 += a.a11*b.a31 + a.a21*b.a32 + a.a31*b.a33;
        c.a21 += a.a12*b.a11 + a.a22*b.a12 + a.a32*b.a13;
        c.a22 += a.a12*b.a21 + a.a22*b.a22 + a.a32*b.a23;
        c.a23 += a.a12*b.a31 + a.a22*b.a32 + a.a32*b.a33;
        c.a31 += a.a13*b.a11 + a.a23*b.a12 + a.a33*b.a13;
        c.a32 += a.a13*b.a21 + a.a23*b.a22 + a.a33*b.a23;
        c.a33 += a.a13*b.a31 + a.a23*b.a32 + a.a33*b.a33;
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
    public static void multAddTransAB( float alpha , FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 += alpha*(a.a11*b.a11 + a.a21*b.a12 + a.a31*b.a13);
        c.a12 += alpha*(a.a11*b.a21 + a.a21*b.a22 + a.a31*b.a23);
        c.a13 += alpha*(a.a11*b.a31 + a.a21*b.a32 + a.a31*b.a33);
        c.a21 += alpha*(a.a12*b.a11 + a.a22*b.a12 + a.a32*b.a13);
        c.a22 += alpha*(a.a12*b.a21 + a.a22*b.a22 + a.a32*b.a23);
        c.a23 += alpha*(a.a12*b.a31 + a.a22*b.a32 + a.a32*b.a33);
        c.a31 += alpha*(a.a13*b.a11 + a.a23*b.a12 + a.a33*b.a13);
        c.a32 += alpha*(a.a13*b.a21 + a.a23*b.a22 + a.a33*b.a23);
        c.a33 += alpha*(a.a13*b.a31 + a.a23*b.a32 + a.a33*b.a33);
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
    public static void multAddTransB( FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 += a.a11*b.a11 + a.a12*b.a12 + a.a13*b.a13;
        c.a12 += a.a11*b.a21 + a.a12*b.a22 + a.a13*b.a23;
        c.a13 += a.a11*b.a31 + a.a12*b.a32 + a.a13*b.a33;
        c.a21 += a.a21*b.a11 + a.a22*b.a12 + a.a23*b.a13;
        c.a22 += a.a21*b.a21 + a.a22*b.a22 + a.a23*b.a23;
        c.a23 += a.a21*b.a31 + a.a22*b.a32 + a.a23*b.a33;
        c.a31 += a.a31*b.a11 + a.a32*b.a12 + a.a33*b.a13;
        c.a32 += a.a31*b.a21 + a.a32*b.a22 + a.a33*b.a23;
        c.a33 += a.a31*b.a31 + a.a32*b.a32 + a.a33*b.a33;
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
    public static void multAddTransB( float alpha , FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c) {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);
        c.a11 += alpha*(a.a11*b.a11 + a.a12*b.a12 + a.a13*b.a13);
        c.a12 += alpha*(a.a11*b.a21 + a.a12*b.a22 + a.a13*b.a23);
        c.a13 += alpha*(a.a11*b.a31 + a.a12*b.a32 + a.a13*b.a33);
        c.a21 += alpha*(a.a21*b.a11 + a.a22*b.a12 + a.a23*b.a13);
        c.a22 += alpha*(a.a21*b.a21 + a.a22*b.a22 + a.a23*b.a23);
        c.a23 += alpha*(a.a21*b.a31 + a.a22*b.a32 + a.a23*b.a33);
        c.a31 += alpha*(a.a31*b.a11 + a.a32*b.a12 + a.a33*b.a13);
        c.a32 += alpha*(a.a31*b.a21 + a.a32*b.a22 + a.a33*b.a23);
        c.a33 += alpha*(a.a31*b.a31 + a.a32*b.a32 + a.a33*b.a33);
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
    public static void multAddOuter( float alpha , FMatrix3x3 A , float beta , FMatrix3 u , FMatrix3 v , FMatrix3x3 C ) {
        C.a11 = alpha*A.a11 + beta*u.a1*v.a1;
        C.a12 = alpha*A.a12 + beta*u.a1*v.a2;
        C.a13 = alpha*A.a13 + beta*u.a1*v.a3;
        C.a21 = alpha*A.a21 + beta*u.a2*v.a1;
        C.a22 = alpha*A.a22 + beta*u.a2*v.a2;
        C.a23 = alpha*A.a23 + beta*u.a2*v.a3;
        C.a31 = alpha*A.a31 + beta*u.a3*v.a1;
        C.a32 = alpha*A.a32 + beta*u.a3*v.a2;
        C.a33 = alpha*A.a33 + beta*u.a3*v.a3;
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
    public static void mult( FMatrix3x3 a , FMatrix3 b , FMatrix3 c) {
        c.a1 = a.a11*b.a1 + a.a12*b.a2 + a.a13*b.a3;
        c.a2 = a.a21*b.a1 + a.a22*b.a2 + a.a23*b.a3;
        c.a3 = a.a31*b.a1 + a.a32*b.a2 + a.a33*b.a3;
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
    public static void mult( FMatrix3 a , FMatrix3x3 b , FMatrix3 c) {
        c.a1 = a.a1*b.a11 + a.a2*b.a21 + a.a3*b.a31;
        c.a2 = a.a1*b.a12 + a.a2*b.a22 + a.a3*b.a32;
        c.a3 = a.a1*b.a13 + a.a2*b.a23 + a.a3*b.a33;
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
    public static float dot( FMatrix3 a , FMatrix3 b ) {
        return a.a1*b.a1 + a.a2*b.a2 + a.a3*b.a3;
    }

    /**
     * Sets all the diagonal elements equal to one and everything else equal to zero.
     * If this is a square matrix then it will be an identity matrix.
     *
     * @param a A matrix.
     */
    public static void setIdentity( FMatrix3x3 a ) {
        a.a11 = 1; a.a21 = 0; a.a31 = 0;
        a.a12 = 0; a.a22 = 1; a.a32 = 0;
        a.a13 = 0; a.a23 = 0; a.a33 = 1;
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
    public static boolean invert( FMatrix3x3 a , FMatrix3x3 inv ) {

        float scale = 1.0f/elementMaxAbs(a);

        float a11 = a.a11*scale;
        float a12 = a.a12*scale;
        float a13 = a.a13*scale;
        float a21 = a.a21*scale;
        float a22 = a.a22*scale;
        float a23 = a.a23*scale;
        float a31 = a.a31*scale;
        float a32 = a.a32*scale;
        float a33 = a.a33*scale;

        float m11 = a22*a33 - a23*a32;
        float m12 = -( a21*a33 - a23*a31);
        float m13 = a21*a32 - a22*a31;
        float m21 = -( a12*a33 - a13*a32);
        float m22 = a11*a33 - a13*a31;
        float m23 = -( a11*a32 - a12*a31);
        float m31 = a12*a23 - a13*a22;
        float m32 = -( a11*a23 - a13*a21);
        float m33 = a11*a22 - a12*a21;

        float det = (a11*m11 + a12*m12 + a13*m13)/scale;

        inv.a11 = m11/det;
        inv.a12 = m21/det;
        inv.a13 = m31/det;
        inv.a21 = m12/det;
        inv.a22 = m22/det;
        inv.a23 = m32/det;
        inv.a31 = m13/det;
        inv.a32 = m23/det;
        inv.a33 = m33/det;

        return !Float.isNaN(det) && !Float.isInfinite(det);
    }

    /**
     * Computes the determinant using minor matrices.<br>
     * WARNING: Potentially less stable than using LU decomposition.
     *
     * @param mat Input matrix.  Not modified.
     * @return The determinant.
     */
    public static float det( FMatrix3x3 mat ) {

        float a = mat.a11*(mat.a22*mat.a33 - mat.a23*mat.a32);
        float b = mat.a12*(mat.a21*mat.a33 - mat.a23*mat.a31);
        float c = mat.a13*(mat.a21*mat.a32 - mat.a31*mat.a22);

        return a-b+c;
    }

    /**
     * Performs a lower Cholesky decomposition of matrix 'A' and stores result in A.
     *
     * @param A (Input) SPD Matrix. (Output) lower cholesky.
     * @return true if it was successful or false if it failed.  Not always reliable.
     */
    public static boolean cholL( FMatrix3x3 A ) {

        A.a11 = (float)Math.sqrt(A.a11);
        A.a12 = 0;
        A.a13 = 0;
        A.a21 = (A.a21)/A.a11;
        A.a22 = (float)Math.sqrt(A.a22-A.a21*A.a21);
        A.a23 = 0;
        A.a31 = (A.a31)/A.a11;
        A.a32 = (A.a32-A.a31*A.a21)/A.a22;
        A.a33 = (float)Math.sqrt(A.a33-A.a31*A.a31-A.a32*A.a32);
        return !UtilEjml.isUncountable(A.a33);
    }

    /**
     * Performs an upper Cholesky decomposition of matrix 'A' and stores result in A.
     *
     * @param A (Input) SPD Matrix. (Output) upper cholesky.
     * @return true if it was successful or false if it failed.  Not always reliable.
     */
    public static boolean cholU( FMatrix3x3 A ) {

        A.a11 = (float)Math.sqrt(A.a11);
        A.a21 = 0;
        A.a31 = 0;
        A.a12 = (A.a12)/A.a11;
        A.a22 = (float)Math.sqrt(A.a22-A.a12*A.a12);
        A.a32 = 0;
        A.a13 = (A.a13)/A.a11;
        A.a23 = (A.a23-A.a12*A.a13)/A.a22;
        A.a33 = (float)Math.sqrt(A.a33-A.a13*A.a13-A.a23*A.a23);
        return !UtilEjml.isUncountable(A.a33);
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
    public static float trace( FMatrix3x3 a ) {
        return a.a11 + a.a22 + a.a33;
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
    public static void diag( FMatrix3x3 input , FMatrix3 out ) {
        out.a1 = input.a11;
        out.a2 = input.a22;
        out.a3 = input.a33;
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
    public static float elementMax( FMatrix3x3 a ) {
        float max = a.a11;
        if( a.a12 > max ) max = a.a12;
        if( a.a13 > max ) max = a.a13;
        if( a.a21 > max ) max = a.a21;
        if( a.a22 > max ) max = a.a22;
        if( a.a23 > max ) max = a.a23;
        if( a.a31 > max ) max = a.a31;
        if( a.a32 > max ) max = a.a32;
        if( a.a33 > max ) max = a.a33;

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
    public static float elementMax( FMatrix3 a ) {
        float max = a.a1;
        if( a.a2 > max ) max = a.a2;
        if( a.a3 > max ) max = a.a3;

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
    public static float elementMaxAbs( FMatrix3x3 a ) {
        float max = Math.abs(a.a11);
        float tmp = Math.abs(a.a12); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a13); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a21); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a22); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a23); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a31); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a32); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a33); if( tmp > max ) max = tmp;

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
    public static float elementMaxAbs( FMatrix3 a ) {
        float max = Math.abs(a.a1);
        float tmp = Math.abs(a.a2); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a2); if( tmp > max ) max = tmp;
        tmp = Math.abs(a.a3); if( tmp > max ) max = tmp;

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
    public static float elementMin( FMatrix3x3 a ) {
        float min = a.a11;
        if( a.a12 < min ) min = a.a12;
        if( a.a13 < min ) min = a.a13;
        if( a.a21 < min ) min = a.a21;
        if( a.a22 < min ) min = a.a22;
        if( a.a23 < min ) min = a.a23;
        if( a.a31 < min ) min = a.a31;
        if( a.a32 < min ) min = a.a32;
        if( a.a33 < min ) min = a.a33;

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
    public static float elementMin( FMatrix3 a ) {
        float min = a.a1;
        if( a.a2 < min ) min = a.a2;
        if( a.a3 < min ) min = a.a3;

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
    public static float elementMinAbs( FMatrix3x3 a ) {
        float min = Math.abs(a.a11);
        float tmp = Math.abs(a.a12); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a13); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a21); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a22); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a23); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a31); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a32); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a33); if( tmp < min ) min = tmp;

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
    public static float elementMinAbs( FMatrix3 a ) {
        float min = Math.abs(a.a1);
        float tmp = Math.abs(a.a1); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a2); if( tmp < min ) min = tmp;
        tmp = Math.abs(a.a3); if( tmp < min ) min = tmp;

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
    public static void elementMult( FMatrix3x3 a , FMatrix3x3 b) {
        a.a11 *= b.a11; a.a12 *= b.a12; a.a13 *= b.a13;
        a.a21 *= b.a21; a.a22 *= b.a22; a.a23 *= b.a23;
        a.a31 *= b.a31; a.a32 *= b.a32; a.a33 *= b.a33;
    }

    /**
     * <p>Performs an element by element multiplication operation:<br>
     * <br>
     * a<sub>i</sub> = a<sub>i</sub> * b<sub>i</sub> <br>
     * </p>
     * @param a The left vector in the multiplication operation. Modified.
     * @param b The right vector in the multiplication operation. Not modified.
     */
    public static void elementMult( FMatrix3 a , FMatrix3 b) {
        a.a1 *= b.a1;
        a.a2 *= b.a2;
        a.a3 *= b.a3;
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
    public static void elementMult( FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c ) {
        c.a11 = a.a11*b.a11; c.a12 = a.a12*b.a12; c.a13 = a.a13*b.a13;
        c.a21 = a.a21*b.a21; c.a22 = a.a22*b.a22; c.a23 = a.a23*b.a23;
        c.a31 = a.a31*b.a31; c.a32 = a.a32*b.a32; c.a33 = a.a33*b.a33;
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
    public static void elementMult( FMatrix3 a , FMatrix3 b , FMatrix3 c ) {
        c.a1 = a.a1*b.a1;
        c.a2 = a.a2*b.a2;
        c.a3 = a.a3*b.a3;
    }

    /**
     * <p>Performs an element by element division operation:<br>
     * <br>
     * a<sub>ij</sub> = a<sub>ij</sub> / b<sub>ij</sub> <br>
     * </p>
     * @param a The left matrix in the division operation. Modified.
     * @param b The right matrix in the division operation. Not modified.
     */
    public static void elementDiv( FMatrix3x3 a , FMatrix3x3 b) {
        a.a11 /= b.a11; a.a12 /= b.a12; a.a13 /= b.a13;
        a.a21 /= b.a21; a.a22 /= b.a22; a.a23 /= b.a23;
        a.a31 /= b.a31; a.a32 /= b.a32; a.a33 /= b.a33;
    }

    /**
     * <p>Performs an element by element division operation:<br>
     * <br>
     * a<sub>i</sub> = a<sub>i</sub> / b<sub>i</sub> <br>
     * </p>
     * @param a The left vector in the division operation. Modified.
     * @param b The right vector in the division operation. Not modified.
     */
    public static void elementDiv( FMatrix3 a , FMatrix3 b) {
        a.a1 /= b.a1;
        a.a2 /= b.a2;
        a.a3 /= b.a3;
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
    public static void elementDiv( FMatrix3x3 a , FMatrix3x3 b , FMatrix3x3 c ) {
        c.a11 = a.a11/b.a11; c.a12 = a.a12/b.a12; c.a13 = a.a13/b.a13;
        c.a21 = a.a21/b.a21; c.a22 = a.a22/b.a22; c.a23 = a.a23/b.a23;
        c.a31 = a.a31/b.a31; c.a32 = a.a32/b.a32; c.a33 = a.a33/b.a33;
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
    public static void elementDiv( FMatrix3 a , FMatrix3 b , FMatrix3 c ) {
        c.a1 = a.a1/b.a1;
        c.a2 = a.a2/b.a2;
        c.a3 = a.a3/b.a3;
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
    public static void scale( float alpha , FMatrix3x3 a ) {
        a.a11 *= alpha; a.a12 *= alpha; a.a13 *= alpha;
        a.a21 *= alpha; a.a22 *= alpha; a.a23 *= alpha;
        a.a31 *= alpha; a.a32 *= alpha; a.a33 *= alpha;
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
    public static void scale( float alpha , FMatrix3 a ) {
        a.a1 *= alpha;
        a.a2 *= alpha;
        a.a3 *= alpha;
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
    public static void scale( float alpha , FMatrix3x3 a , FMatrix3x3 b ) {
        b.a11 = a.a11*alpha; b.a12 = a.a12*alpha; b.a13 = a.a13*alpha;
        b.a21 = a.a21*alpha; b.a22 = a.a22*alpha; b.a23 = a.a23*alpha;
        b.a31 = a.a31*alpha; b.a32 = a.a32*alpha; b.a33 = a.a33*alpha;
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
    public static void scale( float alpha , FMatrix3 a , FMatrix3 b ) {
        b.a1 = a.a1*alpha;
        b.a2 = a.a2*alpha;
        b.a3 = a.a3*alpha;
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
    public static void divide( FMatrix3x3 a , float alpha ) {
        a.a11 /= alpha; a.a12 /= alpha; a.a13 /= alpha;
        a.a21 /= alpha; a.a22 /= alpha; a.a23 /= alpha;
        a.a31 /= alpha; a.a32 /= alpha; a.a33 /= alpha;
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
    public static void divide( FMatrix3 a , float alpha ) {
        a.a1 /= alpha;
        a.a2 /= alpha;
        a.a3 /= alpha;
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
    public static void divide( FMatrix3x3 a , float alpha , FMatrix3x3 b ) {
        b.a11 = a.a11/alpha; b.a12 = a.a12/alpha; b.a13 = a.a13/alpha;
        b.a21 = a.a21/alpha; b.a22 = a.a22/alpha; b.a23 = a.a23/alpha;
        b.a31 = a.a31/alpha; b.a32 = a.a32/alpha; b.a33 = a.a33/alpha;
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
    public static void divide( FMatrix3 a , float alpha , FMatrix3 b ) {
        b.a1 = a.a1/alpha;
        b.a2 = a.a2/alpha;
        b.a3 = a.a3/alpha;
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
    public static void changeSign( FMatrix3x3 a )
    {
        a.a11 = -a.a11; a.a12 = -a.a12; a.a13 = -a.a13;
        a.a21 = -a.a21; a.a22 = -a.a22; a.a23 = -a.a23;
        a.a31 = -a.a31; a.a32 = -a.a32; a.a33 = -a.a33;
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
    public static void changeSign( FMatrix3 a )
    {
        a.a1 = -a.a1;
        a.a2 = -a.a2;
        a.a3 = -a.a3;
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
    public static void fill( FMatrix3x3 a , float v  ) {
        a.a11 = v; a.a12 = v; a.a13 = v;
        a.a21 = v; a.a22 = v; a.a23 = v;
        a.a31 = v; a.a32 = v; a.a33 = v;
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
    public static void fill( FMatrix3 a , float v  ) {
        a.a1 = v;
        a.a2 = v;
        a.a3 = v;
    }

    /**
     * Extracts the row from the matrix a.
     * @param a Input matrix
     * @param row Which row is to be extracted
     * @param out output. Storage for the extracted row. If null then a new vector will be returned.
     * @return The extracted row.
     */
    public static FMatrix3 extractRow( FMatrix3x3 a , int row , FMatrix3 out ) {
        if( out == null) out = new FMatrix3();
        switch( row ) {
            case 0:
                out.a1 = a.a11;
                out.a2 = a.a12;
                out.a3 = a.a13;
            break;
            case 1:
                out.a1 = a.a21;
                out.a2 = a.a22;
                out.a3 = a.a23;
            break;
            case 2:
                out.a1 = a.a31;
                out.a2 = a.a32;
                out.a3 = a.a33;
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
    public static FMatrix3 extractColumn( FMatrix3x3 a , int column , FMatrix3 out ) {
        if( out == null) out = new FMatrix3();
        switch( column ) {
            case 0:
                out.a1 = a.a11;
                out.a2 = a.a21;
                out.a3 = a.a31;
            break;
            case 1:
                out.a1 = a.a12;
                out.a2 = a.a22;
                out.a3 = a.a32;
            break;
            case 2:
                out.a1 = a.a13;
                out.a2 = a.a23;
                out.a3 = a.a33;
            break;
            default:
                throw new IllegalArgumentException("Out of bounds column.  column = "+column);
        }
        return out;
    }

}

