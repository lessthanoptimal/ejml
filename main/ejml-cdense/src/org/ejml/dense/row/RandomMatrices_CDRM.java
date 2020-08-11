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

import org.ejml.data.CMatrixD1;
import org.ejml.data.CMatrixRMaj;

import java.util.Random;

/**
 * Contains a list of functions for creating random row complex matrices and vectors with different structures.
 *
 * @author Peter Abeles
 */
public class RandomMatrices_CDRM {
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
    public static CMatrixRMaj rectangle(int numRow , int numCol , Random rand ) {
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
    public static CMatrixRMaj rectangle(int numRow , int numCol , float min , float max , Random rand ) {
        CMatrixRMaj mat = new CMatrixRMaj(numRow,numCol);

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
    public static void fillUniform(CMatrixRMaj mat , Random rand )
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
    public static void fillUniform(CMatrixD1 mat , float min , float max , Random rand )
    {
        float d[] = mat.getData();
        int size = mat.getDataLength();

        float r = max-min;

        for( int i = 0; i < size; i++ ) {
            d[i] = r*rand.nextFloat()+min;
        }
    }

    /**
     * Creates a random symmetric positive definite matrix.
     *
     * @param width The width of the square matrix it returns.
     * @param rand Random number generator used to make the matrix.
     * @return The random symmetric  positive definite matrix.
     */
    public static CMatrixRMaj hermitianPosDef(int width, Random rand) {
        // This is not formally proven to work.  It just seems to work.
        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(width,1,rand);
        CMatrixRMaj b = new CMatrixRMaj(1,width);
        CMatrixRMaj c = new CMatrixRMaj(width,width);

        CommonOps_CDRM.transposeConjugate(a,b);
        CommonOps_CDRM.mult(a, b, c);

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
    public static CMatrixRMaj hermitian(int length, float min, float max, Random rand) {
        CMatrixRMaj A = new CMatrixRMaj(length,length);

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
    public static void fillHermitian(CMatrixRMaj A, float min, float max, Random rand) {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("A must be a square matrix");

        float range = max-min;

        int length = A.numRows;

        for( int i = 0; i < length; i++ ) {
            A.set(i,i,rand.nextFloat()*range + min,0);

            for( int j = i+1; j < length; j++ ) {
                float real = rand.nextFloat()*range + min;
                float imaginary = rand.nextFloat()*range + min;
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
//    public static CMatrixRMaj createUpperTriangle( int dimen , int hessenberg , float min , float max , Random rand )
//    {
//        if( hessenberg < 0 )
//            throw new RuntimeException("hessenberg must be more than or equal to 0");
//
//        float range = max-min;
//
//        CMatrixRMaj A = new CMatrixRMaj(dimen,dimen);
//
//        for( int i = 0; i < dimen; i++ ) {
//            int start = i <= hessenberg ? 0 : i-hessenberg;
//
//            for( int j = start; j < dimen; j++ ) {
//                float real = rand.nextFloat()*range + min;
//                float imaginary = rand.nextFloat()*range + min;
//
//                A.set(i,j, real, imaginary);
//            }
//
//        }
//
//        return A;
//    }
}
