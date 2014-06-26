/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.svd;

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.UtilTestMatrix;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestSvdImplicitQrDecompose_D64 extends StandardSvdChecks {

    boolean compact;
    boolean needU;
    boolean needV;

    @Override
    public SingularValueDecomposition createSvd() {
        return new SvdImplicitQrDecompose_D64(compact,needU,needV,false);
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

                SingularValueDecomposition<DenseMatrix64F> alg = new SvdImplicitQrDecompose_D64(compact,true,true,false);

                DenseMatrix64F A;

                if( singular ) {
                    double sv[] = new double[ Math.min(numRows,numCols)];
//                    for( int i = 0; i < sv.length; i++ )
//                        sv[i] = rand.nextDouble()*2;
//                    sv[0] = 0;

                    A = RandomMatrices.createSingularValues(numRows,numCols,rand,sv);
//                    A = new DenseMatrix64F(numRows,numCols);
                } else {
                    A = RandomMatrices.createRandom(numRows,numCols,-1,1,rand);
                }

                assertTrue(alg.decompose(A.copy()));

                DenseMatrix64F origU = alg.getU(null,false);
                double sv[] = alg.getSingularValues();
                DenseMatrix64F origV = alg.getV(null,false);

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

    public void testPartial( DenseMatrix64F A ,
                             DenseMatrix64F U ,
                             double sv[] ,
                             DenseMatrix64F V ,
                             boolean checkU , boolean checkV )
    {
        SingularValueDecomposition<DenseMatrix64F> alg = new SvdImplicitQrDecompose_D64(compact,checkU,checkV,false);

        assertTrue(alg.decompose(A.copy()));

        UtilTestMatrix.checkSameElements(1e-10,sv.length,sv,alg.getSingularValues());

        if( checkU ) {
            assertTrue(MatrixFeatures.isIdentical(U,alg.getU(null,false),1e-10));
        }
        if( checkV )
            assertTrue(MatrixFeatures.isIdentical(V,alg.getV(null,false),1e-10));
    }
}
