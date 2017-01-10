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

package org.ejml.sparse;

import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.Matrix_64;
import org.ejml.data.SMatrixCC_64;
import org.ejml.data.SMatrixTriplet_64;

import java.util.Arrays;

/**
 * @author Peter Abeles
 */
public class ConvertSparseMatrix_F64 {
    public static SMatrixTriplet_64 convert(Matrix_64 src , SMatrixTriplet_64 dst ) {
        if( dst == null )
            dst = new SMatrixTriplet_64(src.getNumRows(), src.getNumCols(), 1);
        else
            dst.reshape(src.getNumRows(), src.getNumCols());

        for (int row = 0; row < src.getNumRows(); row++) {
            for (int col = 0; col < src.getNumCols(); col++) {
                double value = src.unsafe_get(row,col);
                if( value != 0.0 )
                    dst.add(row,col,value);
            }
        }

        return dst;
    }

    public static SMatrixTriplet_64 convert( DMatrixRow_F64 src , SMatrixTriplet_64 dst ) {
        if( dst == null )
            dst = new SMatrixTriplet_64(src.numRows, src.numCols,src.numRows*src.numCols);
        else
            dst.reshape(src.numRows, src.numCols);

        int index = 0;
        for (int row = 0; row < src.numRows; row++) {
            for (int col = 0; col < src.numCols; col++) {
                double value = src.data[index++];
                if( value != 0.0 )
                    dst.add(row,col,value);
            }
        }

        return dst;
    }

    public static DMatrixRow_F64 convert( SMatrixTriplet_64 src , DMatrixRow_F64 dst ) {
        if( dst == null )
            dst = new DMatrixRow_F64(src.numRows, src.numCols);
        else {
            dst.reshape(src.numRows, src.numCols);
            dst.zero();
        }

        for (int i = 0; i < src.length; i++) {
            SMatrixTriplet_64.Element e = src.data[i];

            dst.unsafe_set(e.row, e.col, e.value);
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
    public static SMatrixCC_64 convert( SMatrixTriplet_64 src , SMatrixCC_64 dst , int hist[] ) {
        if( dst == null )
            dst = new SMatrixCC_64(src.numRows, src.numCols , src.length);
        else
            dst.reshape(src.numRows, src.numCols, src.length);

        // compute the number of elements in each columns
        if( hist == null )
            hist = new int[ src.numCols ];
        else if( hist.length >= src.numCols )
            Arrays.fill(hist,0, 0, src.numCols);
        else
            throw new IllegalArgumentException("Length of hist must be at least numCols");

        for (int i = 0; i < src.length; i++) {
            SMatrixTriplet_64.Element e = src.data[i];

            hist[e.col]++;
        }

        // define col_idx
        dst.col_idx[0] = 0;
        int index = 0;
        for (int i = 1; i < src.numCols; i++) {
            dst.col_idx[i] = index += hist[i-1];
        }

        // hist will now be used to store the number of times a row has been added to a column
        Arrays.fill(hist,0,0, src.numCols);

        // now write the row indexes and the values
        for (int i = 0; i < src.length; i++) {
            SMatrixTriplet_64.Element e = src.data[i];

            int offset = hist[e.col]++;
            index = dst.col_idx[e.col] + offset;
            dst.row_idx[index] = e.row;
            dst.data[index] = e.value;
        }

        return dst;
    }

    public static SMatrixTriplet_64 convert( SMatrixCC_64 src , SMatrixTriplet_64 dst ) {
        if( dst == null )
            dst = new SMatrixTriplet_64(src.numRows, src.numCols, src.numElements );
        else
            dst.reshape( src.numRows , src.numCols );

        int i0 = src.col_idx[0];
        for (int col = 0; col < src.numCols; col++) {
            int i1 = src.col_idx[col+1];

            for (int i = i0; i < i1; i++) {
                int row = src.row_idx[i];
                dst.add(row,col, src.data[i]);
            }
            i0 = i1;
        }

        return dst;
    }

}
