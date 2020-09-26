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

package org.ejml.dense.row.decomposition.chol;

import org.ejml.concurrency.EjmlConcurrency;
import org.ejml.data.DMatrixRMaj;

/**
 * @author Peter Abeles
 */
public class CholeskyDecompositionBlock_MT_DDRM extends CholeskyDecompositionBlock_DDRM {
    /**
     * Creates a CholeksyDecomposition capable of decomposing a matrix that is
     * n by n, where n is the width.
     *
     * @param blockWidth The width of a block.
     */
    public CholeskyDecompositionBlock_MT_DDRM( int blockWidth ) {
        super(blockWidth);
    }

    @Override
    public void solveL_special( double[] L, DMatrixRMaj b_src, int indexSrc, int indexDst, DMatrixRMaj B ) {
        final double[] dataSrc = b_src.data;

        final double[] b = B.data;
        final int m = B.numRows;
        final int n = B.numCols;
        final int widthL = m;

        EjmlConcurrency.loopFor(0, n, j ->{
            int indexb = j;
            int rowL = 0;

            //for( int i = 0; i < widthL; i++
            for (int i = 0; i < widthL; i++, indexb += n, rowL += widthL) {
                double sum = dataSrc[indexSrc + i*b_src.numCols + j];

                int indexL = rowL;
                int endL = indexL + i;
                int indexB = j;
                //for( int k=0; k<i; k++ ) {
                for (; indexL != endL; indexB += n) {
                    sum -= L[indexL++]*b[indexB];
                }
                double val = sum/L[i*widthL + i];
                dataSrc[indexDst + j*b_src.numCols + i] = val;
                b[indexb] = val;
            }
        });
    }

    @Override
    public void symmRankTranA_sub( DMatrixRMaj a, DMatrixRMaj c, int startIndexC ) {
        // TODO update so that it doesn't modify/read parts that it shouldn't
        final double[] dataA = a.data;
        final double[] dataC = c.data;

//        for( int i = 0; i < a.numCols; i++ ) {
//            for( int k = 0; k < a.numRows; k++ ) {
//                double valA = dataA[k*a.numCols+i];
//
//                for( int j = i; j < a.numCols; j++ ) {
//                    dataC[startIndexC+i*c.numCols+j] -= valA * dataA[k*a.numCols+j];
//                }
//            }
//        }

        final int strideC = c.numCols + 1;
        EjmlConcurrency.loopFor(0, a.numCols, i ->{
            int indexA = i;
            int endR = a.numCols;

            for (int k = 0; k < a.numRows; k++, indexA += a.numCols, endR += a.numCols) {
                int indexC = startIndexC + i*strideC;
                final double valA = dataA[indexA];
                int indexR = indexA;

                while (indexR < endR) {
                    dataC[indexC++] -= valA*dataA[indexR++];
                }
            }
        });
    }
}
