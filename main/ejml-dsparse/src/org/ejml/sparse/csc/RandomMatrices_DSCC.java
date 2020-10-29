/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.ops.DConvertMatrixStruct;

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
    public static DMatrixSparseCSC rectangle( int numRows, int numCols, int nz_total,
                                              double min, double max, Random rand ) {

        if (UtilEjml.exceedsMaxMatrixSize(numRows, numCols))
            throw new IllegalArgumentException("Due to how a random matrix is created, rows*cols < Integer.MAX_VALUE");

        nz_total = Math.min(numRows*numCols, nz_total);
        int[] selected = UtilEjml.shuffled(numRows*numCols, nz_total, rand);
        Arrays.sort(selected, 0, nz_total);

        DMatrixSparseCSC ret = new DMatrixSparseCSC(numRows, numCols, nz_total);
        ret.indicesSorted = true;

        // compute the number of elements in each column
        int[] hist = new int[numCols];
        for (int i = 0; i < nz_total; i++) {
            hist[selected[i]/numRows]++;
        }

        // define col_idx
        ret.histogramToStructure(hist);

        for (int i = 0; i < nz_total; i++) {
            int row = selected[i]%numRows;

            ret.nz_rows[i] = row;
            ret.nz_values[i] = rand.nextDouble()*(max - min) + min;
        }

        return ret;
    }

    public static DMatrixSparseCSC rectangle( int numRows, int numCols, int nz_total, Random rand ) {
        return rectangle(numRows, numCols, nz_total, -1, 1, rand);
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
    public static DMatrixSparseCSC symmetric( int N, int nz_total,
                                              double min, double max, Random rand ) {
        if (UtilEjml.exceedsMaxMatrixSize(N, N))
            throw new IllegalArgumentException("Due to how a random matrix is created, N*N < Integer.MAX_VALUE");
        if (N < 0)
            throw new IllegalArgumentException("Matrix must have a positive size. N=" + N);

        // compute the number of elements in the triangle, including diagonal
        int Ntriagle = (N*N + N)/2;
        // create a list of open elements
        int[] open = new int[Ntriagle];
        for (int row = 0, index = 0; row < N; row++) {
            for (int col = row; col < N; col++, index++) {
                open[index] = row*N + col;
            }
        }

        // perform a random draw
        UtilEjml.shuffle(open, open.length, 0, nz_total, rand);
        Arrays.sort(open, 0, nz_total);

        // construct the matrix
        DMatrixSparseTriplet A = new DMatrixSparseTriplet(N, N, nz_total*2);
        for (int i = 0; i < nz_total; i++) {
            int index = open[i];
            int row = index/N;
            int col = index%N;

            double value = rand.nextDouble()*(max - min) + min;

            if (row == col) {
                A.addItem(row, col, value);
            } else {
                A.addItem(row, col, value);
                A.addItem(col, row, value);
            }
        }

        DMatrixSparseCSC B = new DMatrixSparseCSC(N, N, A.nz_length);
        DConvertMatrixStruct.convert(A, B);

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
    public static DMatrixSparseCSC triangleLower( int dimen, int hessenberg, int nz_total,
                                                  double min, double max, Random rand ) {

        // number of elements which are along the diagonal
        int diag_total = dimen - hessenberg;

        // pre compute element count in each row
        int[] rowStart = new int[dimen];
        int[] rowEnd = new int[dimen];
        // diagonal is mandatory and these indexes refer to a triangle -1 dimension
        int N = 0;
        for (int i = 0; i < dimen; i++) {
            if (i < dimen - 1 + hessenberg) rowStart[i] = N;
            N += i < hessenberg ? dimen : dimen - 1 - i + hessenberg;
            if (i < dimen - 1 + hessenberg) rowEnd[i] = N;
        }
        N += dimen - hessenberg;

        // constrain the total number of non-zero elements
        nz_total = Math.min(N, nz_total);
        nz_total = Math.max(diag_total, nz_total);

        // number of elements which are not the diagonal elements
        int off_total = nz_total - diag_total;

        int[] selected = UtilEjml.shuffled(N - diag_total, off_total, rand);
        Arrays.sort(selected, 0, off_total);

        DMatrixSparseCSC L = new DMatrixSparseCSC(dimen, dimen, nz_total);

        // compute the number of elements in each column
        int[] hist = new int[dimen];
        int s_index = 0;
        for (int col = 0; col < dimen; col++) {
            if (col >= hessenberg)
                hist[col]++;
            while (s_index < off_total && selected[s_index] < rowEnd[col]) {
                hist[col]++;
                s_index++;
            }
        }

        // define col_idx
        L.histogramToStructure(hist);

        int nz_index = 0;
        s_index = 0;
        for (int col = 0; col < dimen; col++) {
            int offset = col >= hessenberg ? col - hessenberg + 1 : 0;

            // assign the diagonal element a value
            if (col >= hessenberg) {
                L.nz_rows[nz_index] = col - hessenberg;
                L.nz_values[nz_index++] = rand.nextDouble()*(max - min) + min;
            }

            // assign the other elements values
            while (s_index < off_total && selected[s_index] < rowEnd[col]) {
                // the extra + 1 is because random elements were not allowed along the diagonal
                int row = selected[s_index++] - rowStart[col] + offset;

                L.nz_rows[nz_index] = row;
                L.nz_values[nz_index++] = rand.nextDouble()*(max - min) + min;
            }
        }

        return L;
    }

    public static DMatrixSparseCSC triangleUpper( int dimen, int hessenberg, int nz_total,
                                                  double min, double max, Random rand ) {
        DMatrixSparseCSC L = triangleLower(dimen, hessenberg, nz_total, min, max, rand);
        DMatrixSparseCSC U = L.createLike();

        CommonOps_DSCC.transpose(L, U, null);
        return U;
    }

    public static int nonzero( int numRows, int numCols, double minFill, double maxFill, Random rand ) {
        int N = numRows*numCols;
        return (int)(N*(rand.nextDouble()*(maxFill - minFill) + minFill) + 0.5);
    }

    /**
     * Creates a triangular matrix where the amount of fill is randomly selected too.
     *
     * @param upper true for upper triangular and false for lower
     * @param N number of rows and columns
     * @param minFill minimum fill fraction
     * @param maxFill maximum fill fraction
     * @param rand random number generator
     * @return Random matrix
     */
    public static DMatrixSparseCSC triangle( boolean upper, int N, double minFill, double maxFill, Random rand ) {
        int nz = (int)(((N - 1)*(N - 1)/2)*(rand.nextDouble()*(maxFill - minFill) + minFill)) + N;

        if (upper) {
            return triangleUpper(N, 0, nz, -1, 1, rand);
        } else {
            return triangleLower(N, 0, nz, -1, 1, rand);
        }
    }

    /**
     * Creates a random symmetric positive definite matrix with zero values.
     *
     * @param width number of columns and rows
     * @param probabilityZero How likely a value is of being zero. 0 = no zeros. 1.0 = all zeros
     * @param rand random number generator
     * @return Random matrix
     */
    public static DMatrixSparseCSC symmetricPosDef( int width, double probabilityZero, Random rand ) {
        if (UtilEjml.exceedsMaxMatrixSize(width, width))
            throw new IllegalArgumentException("Due to how a random matrix is created, width*width < Integer.MAX_VALUE");

        if (probabilityZero < 0 || probabilityZero > 1.0)
            throw new IllegalArgumentException("Invalid value for probabilityZero");

        // This is not formally proven to work.  It just seems to work.
        DMatrixRMaj a = new DMatrixRMaj(width, 1);
        DMatrixRMaj b = new DMatrixRMaj(width, width);

        for (int i = 1; i < width; i++) {
            if (rand.nextDouble() >= probabilityZero)
                a.set(i, 0, rand.nextDouble()*2 - 1.0);
        }

        CommonOps_DDRM.multTransB(a, a, b);

        for (int i = 0; i < width; i++) {
            b.add(i, i, 1.0 + (double)(rand.nextDouble()*0.1));
        }

        DMatrixSparseCSC out = new DMatrixSparseCSC(width, width, width);
        DConvertMatrixStruct.convert(b, out, UtilEjml.TEST_F64);

        return out;
    }

    /**
     * Creates a random matrix where each column has exactly `nzEntriesPerColumn` non-zero entries.
     * Compared to {@link #rectangle} this method can generate larger sparse matrices.
     *
     * @param numRows Number of rows
     * @param numCols Number of columns
     * @param nzEntriesPerColumn Amount of nz-entries per column
     * @param min Minimum element value, inclusive
     * @param max Maximum element value, inclusive
     * @param rand Random number generator
     * @return Randomly generated matrix
     */
    public static DMatrixSparseCSC generateUniform( int numRows, int numCols, int nzEntriesPerColumn,
                                                    double min, double max, Random rand ) {
        if (nzEntriesPerColumn > numRows) {
            throw new IllegalArgumentException("numRows must be greater than nzEntriesPerColumn");
        }

        int nz_total = Math.toIntExact(nzEntriesPerColumn*numCols);

        DMatrixSparseCSC matrix = new DMatrixSparseCSC(numRows, numCols, nz_total);
        matrix.indicesSorted = true;

        if (nzEntriesPerColumn == 0) {
            return matrix;
        }

        int[] nz_hist = new int[numCols];
        Arrays.fill(nz_hist, nzEntriesPerColumn);
        matrix.histogramToStructure(nz_hist);

        boolean[] selectedRows = new boolean[numRows];

        // if the density is high enough, picking random rows will be very slow
        // in this case we unselect (numRows-nzEntriesPerColumn) rows
        boolean dropRows = ((float)nzEntriesPerColumn/numRows) > 0.5;

        for (int col = 0; col < numCols; col++) {
            if (dropRows) {
                // select all rows at first
                Arrays.fill(selectedRows, true);
            } else {
                Arrays.fill(selectedRows, false);
            }

            int nz_index = col*nzEntriesPerColumn;

            // selecting rows
            if (dropRows) {
                for (int colEntry = 0; colEntry < (numRows - nzEntriesPerColumn); colEntry++) {
                    int row = rand.nextInt(numRows);

                    while (!selectedRows[row]) {
                        row = rand.nextInt(numRows);
                    }

                    selectedRows[row] = false;
                }

                for (int row = 0; row < selectedRows.length; row++) {
                    if (selectedRows[row]) {
                        matrix.nz_rows[nz_index] = row;
                        matrix.nz_values[nz_index] = rand.nextDouble()*(max - min) + min;
                        nz_index++;
                    }
                }
            } else {
                for (int colEntry = 0; colEntry < nzEntriesPerColumn; colEntry++) {
                    int row = rand.nextInt(numRows);
                    // avoid duplicate entries

                    while (selectedRows[row]) {
                        row = rand.nextInt(numRows);
                    }

                    selectedRows[row] = true;
                    matrix.nz_rows[nz_index] = row;
                    matrix.nz_values[nz_index] = rand.nextDouble()*(max - min) + min;
                    nz_index++;
                }

                Arrays.sort(matrix.nz_rows, nz_index - nzEntriesPerColumn, nz_index);
            }
        }

        return matrix;
    }

    /**
     * Modies the matrix to make sure that at least one element in each column has a value
     */
    public static void ensureNotSingular( DMatrixSparseCSC A, Random rand ) {
//        if( A.numRows < A.numCols ) {
//            throw new IllegalArgumentException("Fewer equations than variables");
//        }

        int[] s = UtilEjml.shuffled(A.numRows, rand);
        Arrays.sort(s);

        int N = Math.min(A.numCols, A.numRows);
        for (int col = 0; col < N; col++) {
            A.set(s[col], col, rand.nextDouble() + 0.5);
        }
    }
}
