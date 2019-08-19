/*
 * Copyright (c) 2009-2019, Peter Abeles. All Rights Reserved.
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

import org.ejml.UtilEjml;
import org.ejml.data.DMatrix4;
import org.ejml.data.DMatrixRMaj;

import javax.annotation.Nullable;

/**
 * Converts 1D and 2D arrays to and from EJML data types
 *
 * @author Peter Abeles
 */
public class ConvertDArrays {
    public static DMatrixRMaj convert(double[][]src , @Nullable DMatrixRMaj dst ) {
        int rows = src.length;
        if( rows == 0 )
            throw new IllegalArgumentException("Rows of src can't be zero");
        int cols = src[0].length;

        UtilEjml.checkTooLarge(rows,cols);

        if( dst == null ) {
            dst = new DMatrixRMaj(rows,cols);
        } else {
            dst.reshape(rows,cols);
        }
        int pos = 0;
        for( int i = 0; i < rows; i++ ) {
            double []row = src[i];

            if( row.length != cols ) {
                throw new IllegalArgumentException("All rows must have the same length");
            }

            System.arraycopy(row,0,dst.data,pos,cols);

            pos += cols;
        }

        return dst;
    }

//    public static DMatrixSparseCSC convert(double[][]src , @Nullable DMatrixSparseCSC dst ) {
//        int rows = src.length;
//        if( rows == 0 )
//            throw new IllegalArgumentException("Rows of src can't be zero");
//        int cols = src[0].length;
//
//        if( dst == null ) {
//            dst = new DMatrixSparseCSC(rows,cols);
//        } else {
//            dst.reshape(rows,cols);
//        }
//
//        for (int col = 0; col < cols; col++) {
//            for (int row = 0; row < rows; row++) {
//                double v = src[row][col];
//                if( v == 0 )
//                    continue;
//                // make sure there's enoguh data to store the new element and a bit extra
//                if( dst.nz_values.length <= dst.nz_length ) {
//                    dst.growMaxLength(dst.nz_values.length*2+2,true);
//                }
//                dst.nz_values[dst.nz_length] = v;
//                dst.nz_rows[dst.nz_length++] = row;
//            }
//            dst.col_idx[col+1] = dst.nz_length;
//        }
//        dst.indicesSorted = true;
//
//
//        return dst;
//    }

    public static DMatrix4 convert(double[][] src, DMatrix4 dst) {
        if( dst == null )
            dst = new DMatrix4();

        if( src.length == 4 ) {
            if( src[0].length == 1 )
                throw new IllegalArgumentException(("Expected a vector"));
            dst.a1 = src[0][0];
            dst.a2 = src[1][0];
            dst.a3 = src[2][0];
            dst.a4 = src[3][0];
        } else if( src.length == 1 && src[0].length == 4 ) {
            dst.a1 = src[0][0];
            dst.a2 = src[0][1];
            dst.a3 = src[0][2];
            dst.a4 = src[0][3];
        } else {
            throw new IllegalArgumentException("Expected a 4x1 or 1x4 vector");
        }

        return dst;
    }
}
