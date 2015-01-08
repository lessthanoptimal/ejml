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

package org.ejml.alg.dense.decomposition.chol;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.EjmlUnitTests;
import org.junit.Test;

import java.util.Random;

import static org.ejml.alg.dense.decomposition.CheckDecompositionInterface.checkModifiedInput;
import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestCholeskyDecompositionLDL_D64 {

    Random rand = new Random(0x45478);

        @Test
    public void checkModifyInput() {
        checkModifiedInput(new CholeskyDecompositionLDL_D64());
    }

    @Test
    public void testDecompose() {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 1, 2, 4, 2, 7, 23, 4, 23, 98);


        DenseMatrix64F L = new DenseMatrix64F(3,3, true, 1, 0, 0, 2, 1, 0, 4, 5, 1);

        double D[] = new double[]{1,3,7};

        CholeskyDecompositionLDL_D64 cholesky = new CholeskyDecompositionLDL_D64();
        assertTrue(cholesky.decompose(A));

        DenseMatrix64F foundL = cholesky.getL();

        EjmlUnitTests.assertEquals(L,foundL,1e-8);
        for( int i = 0; i < D.length; i++ ) {
            assertEquals(D[i],cholesky.getDiagonal()[i],1e-8);
        }
    }

    /**
     * If it is not positive definate it should fail
     */
    @Test
    public void testNotPositiveDefinate() {
        DenseMatrix64F A = new DenseMatrix64F(2,2, true, 1, -1, -1, -2);

        CholeskyDecompositionLDL_D64 alg = new CholeskyDecompositionLDL_D64();
        assertFalse(alg.decompose(A));
    }
}