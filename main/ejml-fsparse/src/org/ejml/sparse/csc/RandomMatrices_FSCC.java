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

package org.ejml.sparse.csc;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.data.FMatrixSparseTriplet;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.ops.ConvertFMatrixStruct;

import java.util.Arrays;
import java.util.Random;


/**
 * @author Peter Abeles
 */
public class RandomMatrices_FSCC {

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
    public static FMatrixSparseCSC rectangle(int numRows , int numCols , int nz_total ,
                                             float min , float max , Random rand ) {

        nz_total = Math.min(numCols*numRows,nz_total);
        int[] selected = UtilEjml.shuffled(numRows*numCols, nz_total, rand);
        Arrays.sort(selected,0,nz_total);

        FMatrixSparseCSC ret = new FMatrixSparseCSC(numRows,numCols,nz_total);
        ret.indicesSorted = true;

        // compute the number of elements in each column
        int hist[] = new int[ numCols ];
        for (int i = 0; i < nz_total; i++) {
            hist[selected[i]/numRows]++;
        }

        // define col_idx
        ret.histogramToStructure(hist);

        for (int i = 0; i < nz_total; i++) {
            int row = selected[i]%numRows;

            ret.nz_rows[i] = row;
            ret.nz_values[i] = rand.nextFloat()*(max-min)+min;
        }

        return ret;
    }


    public static FMatrixSparseCSC rectangle(int numRows , int numCols , int nz_total , Random rand ) {
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
    public static FMatrixSparseCSC symmetric( int N , int nz_total ,
                                              float min , float max , Random rand) {

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
        UtilEjml.shuffle(open,open.length,0,nz_total,rand);
        Arrays.sort(open,0,nz_total);

        // construct the matrix
        FMatrixSparseTriplet A = new FMatrixSparseTriplet(N,N,nz_total*2);
        for (int i = 0; i < nz_total; i++) {
            int index = open[i];
            int row = index/N;
            int col = index%N;

            float value = rand.nextFloat()*(max-min)+min;

            if( row == col ) {
                A.addItem(row,col,value);
            } else {
                A.addItem(row,col,value);
                A.addItem(col,row,value);
            }
        }

        FMatrixSparseCSC B = new FMatrixSparseCSC(N,N,A.nz_length);
        ConvertFMatrixStruct.convert(A,B);

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
    public static FMatrixSparseCSC triangleLower(int dimen , int hessenberg, int nz_total,
                                                 float min , float max , Random rand ) {

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

        int[] selected = UtilEjml.shuffled(N-diag_total, off_total, rand);
        Arrays.sort(selected,0,off_total);

        FMatrixSparseCSC L = new FMatrixSparseCSC(dimen,dimen,nz_total);

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
        L.histogramToStructure(hist);

        int nz_index = 0;
        s_index = 0;
        for (int col = 0; col < dimen; col++) {
            int offset = col >= hessenberg ? col - hessenberg+1 : 0;

            // assign the diagonal element a value
            if( col >= hessenberg ) {
                L.nz_rows[nz_index] = col-hessenberg;
                L.nz_values[nz_index++] = rand.nextFloat() * (max - min) + min;
            }

            // assign the other elements values
            while( s_index < off_total && selected[s_index] < rowEnd[col]  ) {
                // the extra + 1 is because random elements were not allowed along the diagonal
                int row = selected[s_index++] - rowStart[col] + offset;

                L.nz_rows[nz_index] = row;
                L.nz_values[nz_index++] = rand.nextFloat()*(max-min)+min;
            }
        }

        return L;
    }

    public static FMatrixSparseCSC triangleUpper(int dimen , int hessenberg, int nz_total,
                                                 float min , float max , Random rand ) {
        FMatrixSparseCSC L = triangleLower(dimen, hessenberg, nz_total, min, max, rand);
        FMatrixSparseCSC U = L.createLike();

        CommonOps_FSCC.transpose(L,U,null);
        return U;
    }

    public static int nonzero( int numRows , int numCols , float minFill , float maxFill , Random rand  ) {
        int N = numRows*numCols;
        return (int)(N*(rand.nextFloat()*(maxFill-minFill)+minFill)+0.5f);
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
    public static FMatrixSparseCSC triangle( boolean upper , int N , float minFill , float maxFill , Random rand ) {
        int nz = (int)(((N-1)*(N-1)/2)*(rand.nextFloat()*(maxFill-minFill)+minFill))+N;

        if( upper ) {
            return triangleUpper(N,0,nz,-1,1,rand);
        } else {
            return triangleLower(N,0,nz,-1,1,rand);
        }
    }

    /**
     * Creates a random symmetric positive definite matrix with zero values.
     *
     * @param width number of columns and rows
     * @param probabilityZero How likely a value is of being zero. 0 = no zeros. 1.0f = all zeros
     * @param rand random number generator
     * @return Random matrix
     */
    public static FMatrixSparseCSC symmetricPosDef( int width , float probabilityZero , Random rand ) {
        if( probabilityZero < 0 || probabilityZero > 1.0f )
            throw new IllegalArgumentException("Invalid value for probabilityZero");

        // This is not formally proven to work.  It just seems to work.
        FMatrixRMaj a = new FMatrixRMaj(width,1);
        FMatrixRMaj b = new FMatrixRMaj(width,width);

        for( int i = 1; i < width; i++ ) {
            if( rand.nextFloat() >= probabilityZero)
                a.set(i,0,rand.nextFloat()*2-1.0f);
        }

        CommonOps_FDRM.multTransB(a,a,b);

        for( int i = 0; i < width; i++ ) {
            b.add(i,i,1.0f + (float)(rand.nextFloat()*0.1f) );
        }

        FMatrixSparseCSC out = new FMatrixSparseCSC(width, width,width);
        ConvertFMatrixStruct.convert(b,out, UtilEjml.TEST_F32);

        return out;
    }

    /**
     * Modies the matrix to make sure that at least one element in each column has a value
     */
    public static void ensureNotSingular( FMatrixSparseCSC A , Random rand ) {
//        if( A.numRows < A.numCols ) {
//            throw new IllegalArgumentException("Fewer equations than variables");
//        }

        int []s = UtilEjml.shuffled(A.numRows,rand);
        Arrays.sort(s);

        int N = Math.min(A.numCols,A.numRows);
        for (int col = 0; col < N; col++) {
            A.set(s[col],col,rand.nextFloat()+0.5f);
        }
    }
}
