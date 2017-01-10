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

package org.ejml.dense.row.decomposition.svd;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.UtilTestMatrix;
import org.ejml.dense.row.MatrixFeatures_R64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestSvdImplicitQrDecompose_R64 extends StandardSvdChecks_R64 {

    boolean compact;
    boolean needU;
    boolean needV;

    @Override
    public SingularValueDecomposition_F64 createSvd() {
        return new SvdImplicitQrDecompose_R64(compact,needU,needV,false);
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

                SingularValueDecomposition_F64<DMatrixRow_F64> alg = new SvdImplicitQrDecompose_R64(compact,true,true,false);

                DMatrixRow_F64 A;

                if( singular ) {
                    double sv[] = new double[ Math.min(numRows,numCols)];
//                    for( int i = 0; i < sv.length; i++ )
//                        sv[i] = rand.nextDouble()*2;
//                    sv[0] = 0;

                    A = RandomMatrices_R64.createSingularValues(numRows,numCols,rand,sv);
//                    A = new DMatrixRow_F64(numRows,numCols);
                } else {
                    A = RandomMatrices_R64.createRandom(numRows,numCols,-1,1,rand);
                }

                assertTrue(alg.decompose(A.copy()));

                DMatrixRow_F64 origU = alg.getU(null,false);
                double sv[] = alg.getSingularValues();
                DMatrixRow_F64 origV = alg.getV(null,false);

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

    public void testPartial( DMatrixRow_F64 A ,
                             DMatrixRow_F64 U ,
                             double sv[] ,
                             DMatrixRow_F64 V ,
                             boolean checkU , boolean checkV )
    {
        SingularValueDecomposition_F64<DMatrixRow_F64> alg = new SvdImplicitQrDecompose_R64(compact,checkU,checkV,false);

        assertTrue(alg.decompose(A.copy()));

        UtilTestMatrix.checkSameElements(UtilEjml.TEST_F64,sv.length,sv,alg.getSingularValues());

        if( checkU ) {
            assertTrue(MatrixFeatures_R64.isIdentical(U,alg.getU(null,false), UtilEjml.TEST_F64));
        }
        if( checkV )
            assertTrue(MatrixFeatures_R64.isIdentical(V,alg.getV(null,false), UtilEjml.TEST_F64));
    }
}
