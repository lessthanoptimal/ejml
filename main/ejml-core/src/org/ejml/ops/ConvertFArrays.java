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
import org.ejml.data.FMatrix4;
import org.ejml.data.FMatrixRMaj;


/**
 * Converts 1D and 2D arrays to and from EJML data types
 *
 * @author Peter Abeles
 */
public class ConvertFArrays {
    public static FMatrixRMaj convert(float[][]src , FMatrixRMaj dst ) {
        int rows = src.length;
        if( rows == 0 )
            throw new IllegalArgumentException("Rows of src can't be zero");
        int cols = src[0].length;

        UtilEjml.checkTooLarge(rows,cols);

        if( dst == null ) {
            dst = new FMatrixRMaj(rows,cols);
        } else {
            dst.reshape(rows,cols);
        }
        int pos = 0;
        for( int i = 0; i < rows; i++ ) {
            float []row = src[i];

            if( row.length != cols ) {
                throw new IllegalArgumentException("All rows must have the same length");
            }

            System.arraycopy(row,0,dst.data,pos,cols);

            pos += cols;
        }

        return dst;
    }

//    public static FMatrixSparseCSC convert(float[][]src , FMatrixSparseCSC dst ) {
//        int rows = src.length;
//        if( rows == 0 )
//            throw new IllegalArgumentException("Rows of src can't be zero");
//        int cols = src[0].length;
//
//        if( dst == null ) {
//            dst = new FMatrixSparseCSC(rows,cols);
//        } else {
//            dst.reshape(rows,cols);
//        }
//
//        for (int col = 0; col < cols; col++) {
//            for (int row = 0; row < rows; row++) {
//                float v = src[row][col];
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

    public static FMatrix4 convert(float[][] src, FMatrix4 dst) {
        if( dst == null )
            dst = new FMatrix4();

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
