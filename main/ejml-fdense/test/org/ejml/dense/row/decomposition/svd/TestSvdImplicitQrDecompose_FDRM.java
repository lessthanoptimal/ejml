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

package org.ejml.dense.row.decomposition.svd;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.UtilTestMatrix;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F32;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestSvdImplicitQrDecompose_FDRM extends StandardSvdChecks_FDRM {

    boolean compact;
    boolean needU;
    boolean needV;

    @Override
    public SingularValueDecomposition_F32 createSvd() {
        return new SvdImplicitQrDecompose_FDRM(compact,needU,needV,false);
    }

    @Test
    public void checkCompact() {
        compact = true;
        needU = true;
        needV = true;
        allTests();
    }

    @Test
    public void checkNotCompact() {
        compact = false;
        needU = true;
        needV = true;
        allTests();
    }

    /**
     * This SVD can be configured to compute or not compute different components
     * Checks to see if it has the expected behavior no matter how it is configured
     */
    @Test
    public void checkAllPermutations() {
        // test matrices with different shapes.
        // this ensure that transposed and non-transposed are handled correctly
        checkAllPermutations(5, 5);
        checkAllPermutations(7, 5);
        checkAllPermutations(5, 7);
//        // for much taller or wider matrices different algs might be used
        checkAllPermutations(30, 5);
        checkAllPermutations(5, 30);
    }

    private void checkAllPermutations(int numRows, int numCols) {

        for( int a = 0; a < 2; a++ ) {
            boolean singular = a == 0;

            for( int k = 0; k < 2; k++ ) {
                compact = k == 0;

                SingularValueDecomposition_F32<FMatrixRMaj> alg = new SvdImplicitQrDecompose_FDRM(compact,true,true,false);

                FMatrixRMaj A;

                if( singular ) {
                    float sv[] = new float[ Math.min(numRows,numCols)];
//                    for( int i = 0; i < sv.length; i++ )
//                        sv[i] = rand.nextFloat()*2;
//                    sv[0] = 0;

                    A = RandomMatrices_FDRM.singular(numRows,numCols,rand,sv);
//                    A = new FMatrixRMaj(numRows,numCols);
                } else {
                    A = RandomMatrices_FDRM.rectangle(numRows,numCols,-1,1,rand);
                }

                assertTrue(alg.decompose(A.copy()));

                FMatrixRMaj origU = alg.getU(null,false);
                float sv[] = alg.getSingularValues();
                FMatrixRMaj origV = alg.getV(null,false);

                for( int i = 0; i < 2; i++ ) {
                    needU = i == 0;
                    for( int j = 0; j < 2; j++ ) {
                        needV = j==0;

                        testPartial(A,origU,sv,origV,needU,needV);
                    }
                }
            }
        }
    }

    public void testPartial( FMatrixRMaj A ,
                             FMatrixRMaj U ,
                             float sv[] ,
                             FMatrixRMaj V ,
                             boolean checkU , boolean checkV )
    {
        SingularValueDecomposition_F32<FMatrixRMaj> alg = new SvdImplicitQrDecompose_FDRM(compact,checkU,checkV,false);

        assertTrue(alg.decompose(A.copy()));

        UtilTestMatrix.checkSameElements(UtilEjml.TEST_F32,sv.length,sv,alg.getSingularValues());

        if( checkU ) {
            assertTrue(MatrixFeatures_FDRM.isIdentical(U,alg.getU(null,false), UtilEjml.TEST_F32));
        }
        if( checkV )
            assertTrue(MatrixFeatures_FDRM.isIdentical(V,alg.getV(null,false), UtilEjml.TEST_F32));
    }
}
