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

package org.ejml.sparse.csc;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.ops.ConvertDMatrixSparse;

import java.util.Arrays;
import java.util.Random;


/**
 * @author Peter Abeles
 */
public class RandomMatrices_DSCC {

    /**
     * Randomly generates matrix with the specified number of non-zero elements filled with values from min to max.
     *
     * @param numRows Number of rows
     * @param numCols Number of columns
     * @param nz_total Total number of non-zero elements in the matrix
     * @param min Minimum element value, inclusive
     * @param max Maximum element value, inclusive
     * @param rand Random number generator
     * @return Randomly generated matrix
     */
    public static DMatrixSparseCSC rectangle(int numRows , int numCols , int nz_total ,
                                             double min , double max , Random rand ) {

        nz_total = Math.min(numCols*numRows,nz_total);
        int[] selected = shuffle(numRows*numCols, nz_total, rand);

        DMatrixSparseCSC ret = new DMatrixSparseCSC(numRows,numCols,nz_total);
        ret.indicesSorted = true;

        // compute the number of elements in each column
        int hist[] = new int[ numCols ];
        for (int i = 0; i < nz_total; i++) {
            hist[selected[i]/numRows]++;
        }

        // define col_idx
        ret.colsum(hist);

        for (int i = 0; i < nz_total; i++) {
            int row = selected[i]%numRows;

            ret.nz_rows[i] = row;
            ret.nz_values[i] = rand.nextDouble()*(max-min)+min;
        }

        return ret;
    }

    private static int[] shuffle(int N, int nz_total, Random rand) {
        // Create a list of all the possible element values
        if( N < 0 )
            throw new IllegalArgumentException("matrix size is too large");

        int selected[] = new int[N];
        for (int i = 0; i < N; i++) {
            selected[i] = i;
        }
        shuffle(selected,N,nz_total,rand);

        return selected;
    }

    static void shuffle(int []array, int N , int nz_total, Random rand) {

        for (int i = 0; i < nz_total; i++) {
            int idx = rand.nextInt(N-i)+i;
            int selected = array[idx];
            array[idx] = array[i];
            array[i] = selected;
        }

        // put the indexes in order
        Arrays.sort(array,0,nz_total);
    }

    public static DMatrixSparseCSC rectangle(int numRows , int numCols , int nz_total , Random rand ) {
        return rectangle(numRows, numCols, nz_total, -1,1,rand);
    }

    /**
     * Creates a random symmetric matrix. The entire matrix will be filled in, not just a triangular
     * portion.
     *
     * @param N Number of rows and columns
     * @param nz_total Number of nonzero elements in the triangular portion of the matrix
     * @param min Minimum element value, inclusive
     * @param max Maximum element value, inclusive
     * @param rand Random number generator
     * @return Randomly generated matrix
     */
    public static DMatrixSparseCSC symmetric( int N , int nz_total ,
                                              double min , double max , Random rand) {

        // compute the number of elements in the triangle, including diagonal
        int Ntriagle = (N*N+N)/2;
        // create a list of open elements
        int open[] = new int[Ntriagle];
        for (int row = 0, index = 0; row < N; row++) {
            for (int col = row; col < N; col++, index++) {
                open[index] = row*N+col;
            }
        }

        // perform a random draw
        shuffle(open,open.length,nz_total,rand);

        // construct the matrix
        DMatrixSparseTriplet A = new DMatrixSparseTriplet(N,N,nz_total*2);
        for (int i = 0; i < nz_total; i++) {
            int index = open[i];
            int row = index/N;
            int col = index%N;

            double value = rand.nextDouble()*(max-min)+min;

            if( row == col ) {
                A.addItem(row,col,value);
            } else {
                A.addItem(row,col,value);
                A.addItem(col,row,value);
            }
        }

        DMatrixSparseCSC B = new DMatrixSparseCSC(N,N,A.nz_length);
        ConvertDMatrixSparse.convert(A,B);

        return B;
    }

    /**
     * Randomly generates lower triangular (or hessenberg) matrix with the specified number of of non-zero
     * elements.  The diagonal elements must be non-zero.
     *
     * @param dimen Number of rows and columns
     * @param hessenberg Hessenberg degree. 0 is triangular and 1 or more is Hessenberg.
     * @param nz_total Total number of non-zero elements in the matrix.  Adjust to meet matrix size constraints.
     * @param min Minimum element value, inclusive
     * @param max Maximum element value, inclusive
     * @param rand Random number generator
     * @return Randomly generated matrix
     */
    public static DMatrixSparseCSC triangleLower(int dimen , int hessenberg, int nz_total,
                                                 double min , double max , Random rand ) {

        // number of elements which are along the diagonal
        int diag_total = dimen-hessenberg;

        // pre compute element count in each row
        int rowStart[] = new int[dimen];
        int rowEnd[] = new int[dimen];
        // diagonal is manditory and these indexes refer to a triangle -1 dimension
        int N = 0;
        for (int i = 0; i < dimen; i++) {
            if( i < dimen-1+hessenberg) rowStart[i] = N;
            N += i < hessenberg ? dimen : dimen-1-i+hessenberg;;
            if( i < dimen-1+hessenberg) rowEnd[i] = N;
        }
        N += dimen-hessenberg;

        // contrain the total number of non-zero elements
        nz_total = Math.min(N,nz_total);
        nz_total = Math.max(diag_total,nz_total);

        // number of elements which are not the diagonal elements
        int off_total = nz_total-diag_total;

        int[] selected = shuffle(N-diag_total, off_total, rand);

        DMatrixSparseCSC L = new DMatrixSparseCSC(dimen,dimen,nz_total);

        // compute the number of elements in each column
        int hist[] = new int[ dimen ];
        int s_index = 0;
        for (int col = 0; col < dimen; col++) {
            if( col >= hessenberg )
                hist[col]++;
            while( s_index < off_total && selected[s_index] < rowEnd[col]  ) {
                hist[col]++;
                s_index++;
            }
        }

        // define col_idx
        L.colsum(hist);

        int nz_index = 0;
        s_index = 0;
        for (int col = 0; col < dimen; col++) {
            int offset = col >= hessenberg ? col - hessenberg+1 : 0;

            // assign the diagonal element a value
            if( col >= hessenberg ) {
                L.nz_rows[nz_index] = col-hessenberg;
                L.nz_values[nz_index++] = rand.nextDouble() * (max - min) + min;
            }

            // assign the other elements values
            while( s_index < off_total && selected[s_index] < rowEnd[col]  ) {
                // the extra + 1 is because random elements were not allowed along the diagonal
                int row = selected[s_index++] - rowStart[col] + offset;

                L.nz_rows[nz_index] = row;
                L.nz_values[nz_index++] = rand.nextDouble()*(max-min)+min;
            }
        }

        return L;
    }

    public static DMatrixSparseCSC triangleUpper(int dimen , int hessenberg, int nz_total,
                                                 double min , double max , Random rand ) {
        DMatrixSparseCSC L = triangleLower(dimen, hessenberg, nz_total, min, max, rand);
        DMatrixSparseCSC U = L.createLike();

        CommonOps_DSCC.transpose(L,U,null);
        return U;
    }

    /**
     * Creates a triangular matrix where the amount of fill is randomly selected too.
     *
     * @param upper true for upper triangular and false for lower
     * @param N number of rows and columns
er      * @param minFill minimum fill fraction
     * @param maxFill maximum fill fraction
     * @param rand random number generator
     * @return Random matrix
     */
    public static DMatrixSparseCSC triangle( boolean upper , int N , double minFill , double maxFill , Random rand ) {
        int nz = (int)(((N-1)*(N-1)/2)*(rand.nextDouble()*(maxFill-minFill)+minFill))+N;

        if( upper ) {
            return triangleUpper(N,0,nz,-1,1,rand);
        } else {
            return triangleLower(N,0,nz,-1,1,rand);
        }
    }

    /**
     * Creates a random symmetric positive definite matrix.
     * @param width number of columns and rows
     * @param nz_total Used to adjust number of non-zero values. Exact amount in final matrix will be more than this.
     * @param rand random number generator
     * @return Random matrix
     */
    public static DMatrixSparseCSC symmetricPosDef( int width , int nz_total , Random rand ) {
        DMatrixSparseCSC A = rectangle(width,width,nz_total,rand);

        // to ensure it's SPD assign non-zero values to all the diagonal elements
        for (int i = 0; i < width; i++) {
            A.set(i,i,Math.max(0.01,rand.nextDouble()));
        }

        DMatrixSparseCSC spd = new DMatrixSparseCSC(width,width,0);
        CommonOps_DSCC.multTransB(A,A,spd,null,null);

        return spd;
    }
}
