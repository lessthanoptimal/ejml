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

package org.ejml.dense.row.mult;

import org.ejml.interfaces.mult.MatrixMultUnrolled;

/**
 * @author Peter Abeles
 */
public class MatrixUnroll_MultTranA_DDRM {
    public static final int MAX = 40;
    public static final MatrixMultUnrolled[] ops = new MatrixMultUnrolled[]{new M6()};

    public static boolean mult( double[] A, int offsetA,  double[] B, int offsetB,  double[] C, int offsetC,
                                int rowA, int colA, int colB)
    {
        if( colA < MAX ) {
            ops[colA-1].mult(A,offsetA,B,offsetB,C,offsetC,rowA,colB);
            return true;
        }
        return false;
    }

    public static class M6 implements MatrixMultUnrolled {
        @Override
        public void mult(double[] A, int offsetA,  double[] B, int offsetB,  double[] C, int offsetC,
                         int rowA, int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1 = A[indexA++],a2 = A[indexA++],a3 = A[indexA++],a4 = A[indexA++],a5 = A[indexA++];
                double a6 = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1 * B[indexB];indexB += colB;total += a2 * B[indexB];indexB += colB;
                    total += a3 * B[indexB];indexB += colB;total += a4 * B[indexB];indexB += colB;
                    total += a5 * B[indexB];indexB += colB;total += a6 * B[indexB];

                    C[ cIndex++ ] = total;
                }
            }
        }
    }
}
