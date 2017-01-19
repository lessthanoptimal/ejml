/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row;

import org.ejml.data.ZMatrixD1;
import org.ejml.data.ZMatrixRMaj;

import java.util.Random;

/**
 * Contains a list of functions for creating random row complex matrices and vectors with different structures.
 *
 * @author Peter Abeles
 */
public class RandomMatrices_ZDRM {
    /**
     * <p>
     * Returns a matrix where all the elements are selected independently from
     * a uniform distribution between -1 and 1 inclusive.
     * </p>
     *
     * @param numRow Number of rows in the new matrix.
     * @param numCol Number of columns in the new matrix.
     * @param rand Random number generator used to fill the matrix.
     * @return The randomly generated matrix.
     */
    public static ZMatrixRMaj rectangle(int numRow , int numCol , Random rand ) {
        return rectangle(numRow,numCol,-1,1,rand);
    }

    /**
     * <p>
     * Returns a matrix where all the elements are selected independently from
     * a uniform distribution between 'min' and 'max' inclusive.
     * </p>
     *
     * @param numRow Number of rows in the new matrix.
     * @param numCol Number of columns in the new matrix.
     * @param min The minimum value each element can be.
     * @param max The maximum value each element can be.
     * @param rand Random number generator used to fill the matrix.
     * @return The randomly generated matrix.
     */
    public static ZMatrixRMaj rectangle(int numRow , int numCol , double min , double max , Random rand ) {
        ZMatrixRMaj mat = new ZMatrixRMaj(numRow,numCol);

        fillUniform(mat,min,max,rand);

        return mat;
    }

    /**
     * <p>
     * Sets each element in the matrix to a value drawn from an uniform distribution from 0 to 1 inclusive.
     * </p>
     *
     * @param mat The matrix who is to be randomized. Modified.
     * @param rand Random number generator used to fill the matrix.
     */
    public static void fillUniform(ZMatrixRMaj mat , Random rand )
    {
        fillUniform(mat,0,1,rand);
    }

    /**
     * <p>
     * Sets each element in the matrix to a value drawn from an uniform distribution from 'min' to 'max' inclusive.
     * </p>
     *
     * @param min The minimum value each element can be.
     * @param max The maximum value each element can be.
     * @param mat The matrix who is to be randomized. Modified.
     * @param rand Random number generator used to fill the matrix.
     */
    public static void fillUniform(ZMatrixD1 mat , double min , double max , Random rand )
    {
        double d[] = mat.getData();
        int size = mat.getDataLength();

        double r = max-min;

        for( int i = 0; i < size; i++ ) {
            d[i] = r*rand.nextDouble()+min;
        }
    }

    /**
     * Creates a random symmetric positive definite matrix.
     *
     * @param width The width of the square matrix it returns.
     * @param rand Random number generator used to make the matrix.
     * @return The random symmetric  positive definite matrix.
     */
    public static ZMatrixRMaj hermitianPosDef(int width, Random rand) {
        // This is not formally proven to work.  It just seems to work.
        ZMatrixRMaj a = RandomMatrices_ZDRM.rectangle(width,1,rand);
        ZMatrixRMaj b = new ZMatrixRMaj(1,width);
        ZMatrixRMaj c = new ZMatrixRMaj(width,width);

        CommonOps_ZDRM.transposeConjugate(a,b);
        CommonOps_ZDRM.mult(a, b, c);

        for( int i = 0; i < width; i++ ) {
            c.data[2*(i*width+i)] += 1;
        }

        return c;
    }

    /**
     * Creates a random Hermitian matrix with elements from min to max value.
     *
     * @param length Width and height of the matrix.
     * @param min Minimum value an element can have.
     * @param max Maximum value an element can have.
     * @param rand Random number generator.
     * @return A symmetric matrix.
     */
    public static ZMatrixRMaj hermitian(int length, double min, double max, Random rand) {
        ZMatrixRMaj A = new ZMatrixRMaj(length,length);

        fillHermitian(A, min, max, rand);

        return A;
    }

    /**
     * Assigns the provided square matrix to be a random Hermitian matrix with elements from min to max value.
     *
     * @param A The matrix that is to be modified.  Must be square.  Modified.
     * @param min Minimum value an element can have.
     * @param max Maximum value an element can have.
     * @param rand Random number generator.
     */
    public static void fillHermitian(ZMatrixRMaj A, double min, double max, Random rand) {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("A must be a square matrix");

        double range = max-min;

        int length = A.numRows;

        for( int i = 0; i < length; i++ ) {
            A.set(i,i,rand.nextDouble()*range + min,0);

            for( int j = i+1; j < length; j++ ) {
                double real = rand.nextDouble()*range + min;
                double imaginary = rand.nextDouble()*range + min;
                A.set(i,j,real,imaginary);
                A.set(j,i,real,-imaginary);
            }
        }
    }

//    /**
//     * Creates an upper triangular matrix whose values are selected from a uniform distribution.  If hessenberg
//     * is greater than zero then a hessenberg matrix of the specified degree is created instead.
//     *
//     * @param dimen Number of rows and columns in the matrix..
//     * @param hessenberg 0 for triangular matrix and > 0 for hessenberg matrix.
//     * @param min minimum value an element can be.
//     * @param max maximum value an element can be.
//     * @param rand random number generator used.
//     * @return The randomly generated matrix.
//     */
//    public static ZMatrixRMaj createUpperTriangle( int dimen , int hessenberg , double min , double max , Random rand )
//    {
//        if( hessenberg < 0 )
//            throw new RuntimeException("hessenberg must be more than or equal to 0");
//
//        double range = max-min;
//
//        ZMatrixRMaj A = new ZMatrixRMaj(dimen,dimen);
//
//        for( int i = 0; i < dimen; i++ ) {
//            int start = i <= hessenberg ? 0 : i-hessenberg;
//
//            for( int j = start; j < dimen; j++ ) {
//                double real = rand.nextDouble()*range + min;
//                double imaginary = rand.nextDouble()*range + min;
//
//                A.set(i,j, real, imaginary);
//            }
//
//        }
//
//        return A;
//    }
}
