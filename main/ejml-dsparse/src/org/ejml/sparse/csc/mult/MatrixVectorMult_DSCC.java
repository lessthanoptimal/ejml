/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

package org.ejml.sparse.csc.mult;

import org.ejml.data.DMatrixSparseCSC;

import java.util.Arrays;

/**
 * @author Peter Abeles
 */
public class MatrixVectorMult_DSCC {
    /**
     * c = A*b
     *
     * @param A (Input) Matrix
     * @param b (Input) vector
     * @param offsetB (Input) first index in vector b
     * @param c (Output) vector
     * @param offsetC (Output) first index in vector c
     */
    public static void mult(DMatrixSparseCSC A ,
                            double b[] , int offsetB ,
                            double c[] , int offsetC )
    {
        Arrays.fill(c,offsetC,offsetC+A.numRows,0);
        multAdd(A,b,offsetB,c,offsetC);
    }

    /**
     * c = c + A*b
     *
     * @param A (Input) Matrix
     * @param b (Input) vector
     * @param offsetB (Input) first index in vector b
     * @param c (Output) vector
     * @param offsetC (Output) first index in vector c
     */
    public static void multAdd(DMatrixSparseCSC A ,
                               double b[] , int offsetB ,
                               double c[] , int offsetC )
    {
        if( b.length-offsetB < A.numCols)
            throw new IllegalArgumentException("Length of 'b' isn't long enough");
        if( c.length-offsetC < A.numRows)
            throw new IllegalArgumentException("Length of 'c' isn't long enough");

        for (int k = 0; k < A.numCols; k++) {
            int idx0 = A.col_idx[k  ];
            int idx1 = A.col_idx[k+1];

            for (int indexA = idx0; indexA < idx1; indexA++) {
                c[offsetC+A.nz_rows[indexA]] += A.nz_values[indexA]*b[offsetB+k];
            }
        }
    }

    /**
     * c = a<sup>T</sup>*B
     *
     * @param a (Input) vector
     * @param offsetA  Input) first index in vector a
     * @param B (Input) Matrix
     * @param c (Output) vector
     * @param offsetC (Output) first index in vector c
     */
    public static void mult( double a[] , int offsetA ,
                             DMatrixSparseCSC B ,
                             double c[] , int offsetC )
    {
        if( a.length-offsetA < B.numRows)
            throw new IllegalArgumentException("Length of 'a' isn't long enough");
        if( c.length-offsetC < B.numCols)
            throw new IllegalArgumentException("Length of 'c' isn't long enough");

        for (int k = 0; k < B.numCols; k++) {
            int idx0 = B.col_idx[k  ];
            int idx1 = B.col_idx[k+1];

            double sum = 0;
            for (int indexB = idx0; indexB < idx1; indexB++) {
                sum += a[offsetA+B.nz_rows[indexB]]*B.nz_values[indexB];
            }
            c[offsetC+k] = sum;
        }
    }

    /**
     * scalar = A<sup>T</sup>*B*C
     *
     * @param a (Input) vector
     * @param offsetA  Input) first index in vector a
     * @param B (Input) Matrix
     * @param c (Output) vector
     * @param offsetC (Output) first index in vector c
     */
    public static double innerProduct( double a[] , int offsetA ,
                                       DMatrixSparseCSC B ,
                                       double c[] , int offsetC )
    {
        if( a.length-offsetA < B.numRows)
            throw new IllegalArgumentException("Length of 'a' isn't long enough");
        if( c.length-offsetC < B.numCols)
            throw new IllegalArgumentException("Length of 'c' isn't long enough");

        double output = 0;

        for (int k = 0; k < B.numCols; k++) {
            int idx0 = B.col_idx[k  ];
            int idx1 = B.col_idx[k+1];

            double sum = 0;
            for (int indexB = idx0; indexB < idx1; indexB++) {
                sum += a[offsetA+B.nz_rows[indexB]]*B.nz_values[indexB];
            }
            output += sum*c[offsetC+k];
        }

        return output;
    }
}
