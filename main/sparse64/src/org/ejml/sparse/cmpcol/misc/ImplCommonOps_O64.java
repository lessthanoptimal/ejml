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

package org.ejml.sparse.cmpcol.misc;

import org.ejml.data.SMatrixCC_F64;
import org.ejml.sparse.cmpcol.CommonOps_O64;

import java.util.Arrays;

/**
 * Implementation class.  Not recommended for direct use.  Instead use {@link CommonOps_O64}
 * instead.
 *
 * @author Peter Abeles
 */
public class ImplCommonOps_O64 {

    /**
     * Performs a matrix transpose.
     *
     * @param a Original matrix.  Not modified.
     * @param b Storage for transposed 'a'.  Assumed to be of the correct shape and length.
     * @param work Work space.  null or an array the size of the rows in 'a'
     */
    public static void transpose(SMatrixCC_F64 a , SMatrixCC_F64 b , int work[] ) {
        // make sure enough memory has been declared
        if( work == null )
            work = new int[ a.numRows ];
        else
            Arrays.fill(work,0,a.numRows,0);

        b.length = a.length;

        // compute the histogram for each row in 'a'
        int idx0 = a.col_idx[0];
        for (int j = 1; j <= a.numCols; j++) {
            int idx1 = a.col_idx[j];
            for (int i = idx0; i < idx1; i++) {
                work[a.row_idx[i]]++;
            }
            idx0 = idx1;
        }

        // construct col_idx in the transposed matrix
        colsum(b,work);

        // fill in the row indexes
        idx0 = a.col_idx[0];
        for (int j = 1; j <= a.numCols; j++) {
            int col = j-1;
            int idx1 = a.col_idx[j];
            for (int i = idx0; i < idx1; i++) {
                int row = a.row_idx[i];
                int index = work[row]++;
                b.row_idx[index] = col;
                b.data[index] = a.data[i];
            }
            idx0 = idx1;
        }
    }

    /**
     * Given the histogram of columns compute the col_idx for the matrix.  Then overwrite histogram with
     * those values.
     * @param a A matrix
     * @param histogram histogram of column values in the sparse matrix
     */
    public static void colsum( SMatrixCC_F64 a, int histogram[] ) {
        a.col_idx[0] = 0;
        int index = 0;
        for (int i = 1; i <= a.numCols; i++) {
            a.col_idx[i] = index += histogram[i-1];
        }
        System.arraycopy(a.col_idx,0,histogram,0,a.numCols);
    }
}
