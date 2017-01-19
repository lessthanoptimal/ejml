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

package org.ejml.ops;

import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;

import java.util.Arrays;

/**
 * Contains functions for converting between row and sparse matrix formats as well as sparse to sparse.
 *
 * @author Peter Abeles
 */
public class ConvertDMatrixSparse {
    public static DMatrixSparseTriplet convert(DMatrix src , DMatrixSparseTriplet dst ) {
        if( dst == null )
            dst = new DMatrixSparseTriplet(src.getNumRows(), src.getNumCols(), 1);
        else
            dst.reshape(src.getNumRows(), src.getNumCols());

        for (int row = 0; row < src.getNumRows(); row++) {
            for (int col = 0; col < src.getNumCols(); col++) {
                double value = src.unsafe_get(row,col);
                if( value != 0.0 )
                    dst.addItem(row,col,value);
            }
        }

        return dst;
    }

    public static DMatrixSparseTriplet convert(DMatrixRMaj src , DMatrixSparseTriplet dst ) {
        if( dst == null )
            dst = new DMatrixSparseTriplet(src.numRows, src.numCols,src.numRows*src.numCols);
        else
            dst.reshape(src.numRows, src.numCols);

        int index = 0;
        for (int row = 0; row < src.numRows; row++) {
            for (int col = 0; col < src.numCols; col++) {
                double value = src.data[index++];
                if( value != 0.0 )
                    dst.addItem(row,col,value);
            }
        }

        return dst;
    }

    public static DMatrixRMaj convert(DMatrixSparseTriplet src , DMatrixRMaj dst ) {
        if( dst == null )
            dst = new DMatrixRMaj(src.numRows, src.numCols);
        else {
            dst.reshape(src.numRows, src.numCols);
            dst.zero();
        }

        for (int i = 0; i < src.nz_length; i++) {
            DMatrixSparseTriplet.Element e = src.nz_data[i];

            dst.unsafe_set(e.row, e.col, e.value);
        }

        return dst;
    }

    public static DMatrixRMaj convert(DMatrixSparseCSC src , DMatrixRMaj dst ) {
        if( dst == null )
            dst = new DMatrixRMaj(src.numRows, src.numCols);
        else {
            dst.reshape(src.numRows, src.numCols);
            dst.zero();
        }

        int idx0 = src.col_idx[0];
        for (int j = 1; j <= src.numCols; j++) {
            int idx1 = src.col_idx[j];

            for (int i = idx0; i < idx1; i++) {
                int row = src.nz_rows[i];
                double val = src.nz_values[i];

                dst.unsafe_set(row,j-1, val);
            }
            idx0 = idx1;
        }

        return dst;
    }

    /**
     *
     *
     * @param src Original matrix that is to be converted.
     * @param dst Storage for the converted matrix.  If null a new instance will be returned.
     * @return The converted matrix
     */
    public static DMatrixSparseCSC convert(DMatrixRMaj src , DMatrixSparseCSC dst ) {
        int nonzero = 0;
        int N = src.numRows*src.numCols;
        for (int i = 0; i < N; i++) {
            if( src.data[i] != 0 )
                nonzero++;
        }

        if( dst == null )
            dst = new DMatrixSparseCSC(src.numRows, src.numCols, nonzero);
        else
            dst.reshape(src.numRows, src.numCols, nonzero);
        dst.nz_length = 0;

        dst.col_idx[0] = 0;
        for (int col = 0; col < src.numCols; col++) {
            for (int row = 0; row < src.numRows; row++) {
                double value = src.data[row*src.numCols+col];
                if( value == 0 )
                    continue;

                dst.nz_rows[dst.nz_length] = row;
                dst.nz_values[dst.nz_length] = value;
                dst.nz_length += 1;
            }
            dst.col_idx[col+1] = dst.nz_length;
        }

        return dst;
    }

    /**
     * Converts SMatrixTriplet_64 into a SMatrixCC_64.
     *
     * @param src Original matrix which is to be copied.  Not modified.
     * @param dst Destination. Will be a copy.  Modified.
     * @param hist Workspace.  Should be at least as long as the number of columns.  Can be null.
     */
    public static DMatrixSparseCSC convert(DMatrixSparseTriplet src , DMatrixSparseCSC dst , int hist[] ) {
        if( dst == null )
            dst = new DMatrixSparseCSC(src.numRows, src.numCols , src.nz_length);
        else
            dst.reshape(src.numRows, src.numCols, src.nz_length);

        if( hist == null )
            hist = new int[ src.numCols ];
        else if( hist.length >= src.numCols )
            Arrays.fill(hist,0,src.numCols, 0);
        else
            throw new IllegalArgumentException("Length of hist must be at least numCols");

        // compute the number of elements in each columns
        for (int i = 0; i < src.nz_length; i++) {
            hist[src.nz_data[i].col]++;
        }

        // define col_idx
        dst.colsum(hist);

        // now write the row indexes and the values
        for (int i = 0; i < src.nz_length; i++) {
            DMatrixSparseTriplet.Element e = src.nz_data[i];

            int index = hist[e.col]++;
            dst.nz_rows[index] = e.row;
            dst.nz_values[index] = e.value;
        }
        dst.nz_length = src.nz_length;
        dst.indicesSorted = false;

        return dst;
    }

    public static DMatrixSparseCSC convert(DMatrixSparseTriplet src , DMatrixSparseCSC dst ) {
        return convert(src,dst,null);
    }

    public static DMatrixSparseTriplet convert(DMatrixSparseCSC src , DMatrixSparseTriplet dst ) {
        if( dst == null )
            dst = new DMatrixSparseTriplet(src.numRows, src.numCols, src.nz_length);
        else
            dst.reshape( src.numRows , src.numCols );

        int i0 = src.col_idx[0];
        for (int col = 0; col < src.numCols; col++) {
            int i1 = src.col_idx[col+1];

            for (int i = i0; i < i1; i++) {
                int row = src.nz_rows[i];
                dst.addItem(row,col, src.nz_values[i]);
            }
            i0 = i1;
        }

        return dst;
    }

}
