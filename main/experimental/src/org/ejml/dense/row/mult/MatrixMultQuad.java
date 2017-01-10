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

package org.ejml.dense.row.mult;

import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.row.CommonOps_R64;

/**
 * @author Peter Abeles
 */
public class MatrixMultQuad {
    /**
     * <p>
     * Performs matrix multiplication on an equation in quadratic form with a transpose on the second A:<br>
     * <br>
     * out = A*B*A<sup>T</sup>
     * </p>
     * @param A Left and right matrix.
     * @param B Middle square matrix.  Size = (A.numCols,A.numCols)
     * @param out Output matrix.  Size = (A.numRows,A.numRows);
     */
    public static void multQuad1(DMatrixRow_F64 A , DMatrixRow_F64 B , DMatrixRow_F64 out ) {

        if( A.numCols != B.numCols || A.numCols != B.numRows
                || A.numRows != out.numRows || A.numRows != out.numCols )
            throw new IllegalArgumentException("Incompatible matrix shapes");

        CommonOps_R64.fill(out, 0);

        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
                double total = 0;

                int indexA = i*A.numCols;
                int indexB = j;
                int end = indexA + A.numCols;
                for (; indexA < end; indexA++ , indexB += B.numCols ) {
//                for (int k = 0; k < A.numCols; k++) {
//                    total += A.get(i,k)*B.get(k,j);
                    total += A.data[indexA] * B.data[indexB];
                }

                int indexOut = i*out.numCols;
                indexA = j;
                end = indexOut + A.numRows;
                for (; indexOut < end; indexOut++, indexA += A.numCols) {
//                for (int l = 0; l < A.numRows; l++) {
//                    out.data[ out.getIndex(i,l) ] += total*A.get(l,j);
                    out.data[ indexOut] += total*A.data[indexA];
                }
            }
        }
    }
}
