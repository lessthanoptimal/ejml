/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decomposition.svd;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.UtilTestMatrix;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSvdImplicitQrDecompose_DDRM extends StandardSvdChecks_DDRM {
    boolean compact;
    boolean needU;
    boolean needV;

    @Override public SingularValueDecomposition_F64 createSvd() {
        return new SvdImplicitQrDecompose_DDRM(compact, needU, needV, false);
    }

    @Test void checkCompact() {
        compact = true;
        needU = true;
        needV = true;
        allTests();
    }

    @Test void checkNotCompact() {
        compact = false;
        needU = true;
        needV = true;
        allTests();
    }

    /**
     * This SVD can be configured to compute or not compute different components
     * Checks to see if it has the expected behavior no matter how it is configured
     */
    @Test void checkAllPermutations() {
        // test matrices with different shapes.
        // this ensure that transposed and non-transposed are handled correctly
        checkAllPermutations(5, 5);
        checkAllPermutations(7, 5);
        checkAllPermutations(5, 7);
//        // for much taller or wider matrices different algs might be used
        checkAllPermutations(30, 5);
        checkAllPermutations(5, 30);
    }

    private void checkAllPermutations( int numRows, int numCols ) {
        for (int a = 0; a < 2; a++) {
            boolean singular = a == 0;

            for (int k = 0; k < 2; k++) {
                compact = k == 0;

                SingularValueDecomposition_F64<DMatrixRMaj> alg = new SvdImplicitQrDecompose_DDRM(compact, true, true, false);

                DMatrixRMaj A;

                if (singular) {
                    double sv[] = new double[Math.min(numRows, numCols)];
//                    for( int i = 0; i < sv.length; i++ )
//                        sv[i] = rand.nextDouble()*2;
//                    sv[0] = 0;

                    A = RandomMatrices_DDRM.singular(numRows, numCols, rand, sv);
//                    A = new DMatrixRMaj(numRows,numCols);
                } else {
                    A = RandomMatrices_DDRM.rectangle(numRows, numCols, -1, 1, rand);
                }

                assertTrue(alg.decompose(A.copy()));

                DMatrixRMaj origU = alg.getU(null, false);
                double sv[] = alg.getSingularValues();
                DMatrixRMaj origV = alg.getV(null, false);

                for (int i = 0; i < 2; i++) {
                    needU = i == 0;
                    for (int j = 0; j < 2; j++) {
                        needV = j == 0;

                        testPartial(A, origU, sv, origV, needU, needV);
                    }
                }
            }
        }
    }

    public void testPartial( DMatrixRMaj A,
                             DMatrixRMaj U,
                             double sv[],
                             DMatrixRMaj V,
                             boolean checkU, boolean checkV ) {
        SingularValueDecomposition_F64<DMatrixRMaj> alg = new SvdImplicitQrDecompose_DDRM(compact, checkU, checkV, false);

        assertTrue(alg.decompose(A.copy()));

        UtilTestMatrix.checkSameElements(UtilEjml.TEST_F64, sv.length, sv, alg.getSingularValues());

        if (checkU) {
            assertTrue(MatrixFeatures_DDRM.isIdentical(U, alg.getU(null, false), UtilEjml.TEST_F64));
        }
        if (checkV)
            assertTrue(MatrixFeatures_DDRM.isIdentical(V, alg.getV(null, false), UtilEjml.TEST_F64));
    }
}
