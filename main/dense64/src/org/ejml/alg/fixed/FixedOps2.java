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

import org.ejml.data.FixedMatrix2_64F;
import org.ejml.data.FixedMatrix2x2_64F;

/**
 * <p>Common matrix operations for fixed sized matrices which are 2 x 2 or 2 element vectors.</p>
 * <p>DO NOT MODIFY.  Automatically generated code created by GenerateFixedOps</p>
 *
 * @author Peter Abeles
 */
public class FixedOps2 {
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
    public static void add( FixedMatrix2x2_64F a , FixedMatrix2x2_64F b , FixedMatrix2x2_64F c ) {
        c.a11 = a.a11 + b.a11;
        c.a12 = a.a12 + b.a12;
        c.a21 = a.a21 + b.a21;
        c.a22 = a.a22 + b.a22;
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
    public static void addEquals( FixedMatrix2x2_64F a , FixedMatrix2x2_64F b ) {
        a.a11 += b.a11;
        a.a12 += b.a12;
        a.a21 += b.a21;
        a.a22 += b.a22;
    }

    /**
     * Performs an in-place transpose.  This algorithm is only efficient for square
     * matrices.
     *
     * @param m The matrix that is to be transposed. Modified.
     */
    public static void transpose( FixedMatrix2x2_64F m ) {
        double tmp;
        tmp = m.a12; m.a12 = m.a21; m.a21 = tmp;
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
    public static FixedMatrix2x2_64F transpose( FixedMatrix2x2_64F input , FixedMatrix2x2_64F output ) {
        if( input == null )
            input = new FixedMatrix2x2_64F();

        output.a11 = input.a11;
        output.a12 = input.a21;
        output.a21 = input.a12;
        output.a22 = input.a22;

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
    public static void mult( FixedMatrix2x2_64F a , FixedMatrix2x2_64F b , FixedMatrix2x2_64F c) {
        c.a11 = a.a11*b.a11 + a.a12*b.a21;
        c.a12 = a.a11*b.a12 + a.a12*b.a22;
        c.a21 = a.a21*b.a11 + a.a22*b.a21;
        c.a22 = a.a21*b.a12 + a.a22*b.a22;
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
    public static void multTransA( FixedMatrix2x2_64F a , FixedMatrix2x2_64F b , FixedMatrix2x2_64F c) {
        c.a11 = a.a11*b.a11 + a.a21*b.a21;
        c.a12 = a.a11*b.a12 + a.a21*b.a22;
        c.a21 = a.a12*b.a11 + a.a22*b.a21;
        c.a22 = a.a12*b.a12 + a.a22*b.a22;
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
    public static void multTransAB( FixedMatrix2x2_64F a , FixedMatrix2x2_64F b , FixedMatrix2x2_64F c) {
        c.a11 = a.a11*b.a11 + a.a21*b.a12;
        c.a12 = a.a11*b.a21 + a.a21*b.a22;
        c.a21 = a.a12*b.a11 + a.a22*b.a12;
        c.a22 = a.a12*b.a21 + a.a22*b.a22;
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
    public static void multTransB( FixedMatrix2x2_64F a , FixedMatrix2x2_64F b , FixedMatrix2x2_64F c) {
        c.a11 = a.a11*b.a11 + a.a12*b.a12;
        c.a12 = a.a11*b.a21 + a.a12*b.a22;
        c.a21 = a.a21*b.a11 + a.a22*b.a12;
        c.a22 = a.a21*b.a21 + a.a22*b.a22;
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
    public static void mult( FixedMatrix2x2_64F a , FixedMatrix2_64F b , FixedMatrix2_64F c) {
        c.a1 = a.a11*b.a1 + a.a12*b.a2;
        c.a2 = a.a21*b.a1 + a.a22*b.a2;
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
    public static void mult( FixedMatrix2_64F a , FixedMatrix2x2_64F b , FixedMatrix2_64F c) {
        c.a1 = a.a1*b.a11 + a.a2*b.a21;
        c.a2 = a.a1*b.a12 + a.a2*b.a22;
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
    public static double dot( FixedMatrix2_64F a , FixedMatrix2_64F b ) {
        return a.a1*b.a1 + a.a2*b.a2;
    }

    /**
     * Sets all the diagonal elements equal to one and everything else equal to zero.
     * If this is a square matrix then it will be an identity matrix.
     *
     * @param a A matrix.
     */
    public static void setIdentity( FixedMatrix2x2_64F a ) {
        a.a11 = 1; a.a21 = 0;
        a.a12 = 0; a.a22 = 1;
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
    public static boolean invert( FixedMatrix2x2_64F a , FixedMatrix2x2_64F inv ) {

        double scale = 1.0/elementMaxAbs(a);

        double a11 = a.a11*scale;
        double a12 = a.a12*scale;
        double a21 = a.a21*scale;
        double a22 = a.a22*scale;

        double m11 = a22;
        double m12 = -( a21);
        double m21 = -( a12);
        double m22 = a11;

        double det = (a11*m11 + a12*m12)/scale;

        inv.a11 = m11/det;
        inv.a12 = m21/det;
        inv.a21 = m12/det;
        inv.a22 = m22/det;

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
    public static double det( FixedMatrix2x2_64F mat ) {

        return mat.a11*mat.a22 - mat.a12*mat.a21;
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
    public static double trace( FixedMatrix2x2_64F a ) {
        return a.a11 + a.a21;
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
    public static void diag( FixedMatrix2x2_64F input , FixedMatrix2_64F out ) {
        out.a1 = input.a11;
        out.a2 = input.a22;
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
    public static double elementMax( FixedMatrix2x2_64F a ) {
        double max = a.a11;
        max = Math.max(max,a.a12);
        max = Math.max(max,a.a21);
        max = Math.max(max,a.a22);

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
    public static double elementMaxAbs( FixedMatrix2x2_64F a ) {
        double max = a.a11;
        max = Math.max(max,Math.abs(a.a12));
        max = Math.max(max,Math.abs(a.a21));
        max = Math.max(max,Math.abs(a.a22));

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
    public static double elementMin( FixedMatrix2x2_64F a ) {
        double min = a.a11;
        min = Math.min(min,a.a12);
        min = Math.min(min,a.a21);
        min = Math.min(min,a.a22);

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
    public static double elementMinAbs( FixedMatrix2x2_64F a ) {
        double min = a.a11;
        min = Math.min(min,Math.abs(a.a12));
        min = Math.min(min,Math.abs(a.a21));
        min = Math.min(min,Math.abs(a.a22));

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
    public static void elementMult( FixedMatrix2x2_64F a , FixedMatrix2x2_64F b) {
        a.a11 *= b.a11; a.a12 *= b.a12;
        a.a21 *= b.a21; a.a22 *= b.a22;
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
    public static void elementMult( FixedMatrix2x2_64F a , FixedMatrix2x2_64F b , FixedMatrix2x2_64F c ) {
        c.a11 = a.a11*b.a11; c.a12 = a.a12*b.a12;
        c.a21 = a.a21*b.a21; c.a22 = a.a22*b.a22;
    }

    /**
     * <p>Performs the an element by element division operation:<br>
     * <br>
     * a<sub>ij</sub> = a<sub>ij</sub> / b<sub>ij</sub> <br>
     * </p>
     * @param a The left matrix in the division operation. Modified.
     * @param b The right matrix in the division operation. Not modified.
     */
    public static void elementDiv( FixedMatrix2x2_64F a , FixedMatrix2x2_64F b) {
        a.a11 /= b.a11; a.a12 /= b.a12;
        a.a21 /= b.a21; a.a22 /= b.a22;
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
    public static void elementDiv( FixedMatrix2x2_64F a , FixedMatrix2x2_64F b , FixedMatrix2x2_64F c ) {
        c.a11 = a.a11/b.a11; c.a12 = a.a12/b.a12;
        c.a21 = a.a21/b.a21; c.a22 = a.a22/b.a22;
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
    public static void scale( double alpha , FixedMatrix2x2_64F a ) {
        a.a11 *= alpha; a.a12 *= alpha;
        a.a21 *= alpha; a.a22 *= alpha;
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
    public static void scale( double alpha , FixedMatrix2x2_64F a , FixedMatrix2x2_64F b ) {
        b.a11 = a.a11*alpha; b.a12 = a.a12*alpha;
        b.a21 = a.a21*alpha; b.a22 = a.a22*alpha;
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
    public static void divide( FixedMatrix2x2_64F a , double alpha ) {
        a.a11 /= alpha; a.a12 /= alpha;
        a.a21 /= alpha; a.a22 /= alpha;
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
    public static void divide( FixedMatrix2x2_64F a , double alpha , FixedMatrix2x2_64F b ) {
        b.a11 = a.a11/alpha; b.a12 = a.a12/alpha;
        b.a21 = a.a21/alpha; b.a22 = a.a22/alpha;
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
    public static void changeSign( FixedMatrix2x2_64F a )
    {
        a.a11 = -a.a11; a.a12 = -a.a12;
        a.a21 = -a.a21; a.a22 = -a.a22;
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
    public static void fill( FixedMatrix2x2_64F a , double v  ) {
        a.a11 = v; a.a12 = v;
        a.a21 = v; a.a22 = v;
    }

}

