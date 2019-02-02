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

package org.ejml.sparse.triplet;

import org.ejml.data.DMatrixSparseTriplet;

/**
 * @author Peter Abeles
 */
public class MatrixFeatures_DSTL {

    public static boolean isEquals(DMatrixSparseTriplet a , DMatrixSparseTriplet b ) {
        if( !isSameShape(a,b) )
            return false;

        for (int blockIdx = 0; blockIdx < a.blockSize; blockIdx++) {
            int[] blockRC = a.nz_rowcol.getBlock(blockIdx);
            double[] blockV = a.nz_value.getBlock(blockIdx);

            final int N =a.nz_rowcol.getBlockLength(blockIdx);
            for (int i = 0; i < N; ) {
                double avalue = blockV[i/2];
                int arow = blockRC[i++];
                int acol = blockRC[i++];

                int bindex = b.nz_index(arow, acol);
                if( bindex < 0 )
                    return false;

                double bvalue = b.nz_value.get(bindex);

                if( avalue != bvalue )
                    return false;
            }
        }

        return true;
    }

    public static boolean isEquals(DMatrixSparseTriplet a , DMatrixSparseTriplet b , double tol ) {
        if( !isSameShape(a,b) )
            return false;

        for (int blockIdx = 0; blockIdx < a.blockSize; blockIdx++) {
            int[] blockRC = a.nz_rowcol.getBlock(blockIdx);
            double[] blockV = a.nz_value.getBlock(blockIdx);

            final int N =a.nz_rowcol.getBlockLength(blockIdx);
            for (int i = 0; i < N; ) {
                double avalue = blockV[i/2];
                int arow = blockRC[i++];
                int acol = blockRC[i++];

                int bindex = b.nz_index(arow, acol);
                if( bindex < 0 )
                    return false;

                double bvalue = b.nz_value.get(bindex);

                if( Math.abs(avalue-bvalue) > tol )
                    return false;
            }
        }

        return true;
    }

    public static boolean isSameShape(DMatrixSparseTriplet a , DMatrixSparseTriplet b) {
        return a.numRows == b.numRows && a.numCols == b.numCols && a.nz_length == b.nz_length;
    }
}
