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

package org.ejml.alg.dense.decomposition.chol;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.junit.Test;

import java.util.Random;

import static org.ejml.alg.dense.decomposition.CheckDecompositionInterface_R64.checkModifiedInput;
import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestCholeskyDecompositionLDL_R64 {

    Random rand = new Random(0x45478);

        @Test
    public void checkModifyInput() {
        checkModifiedInput(new CholeskyDecompositionLDL_R64());
    }

    @Test
    public void testDecompose() {
        DMatrixRow_F64 A = new DMatrixRow_F64(3,3, true, 1, 2, 4, 2, 7, 23, 4, 23, 98);


        DMatrixRow_F64 L = new DMatrixRow_F64(3,3, true, 1, 0, 0, 2, 1, 0, 4, 5, 1);

        double D[] = new double[]{1,3,7};

        CholeskyDecompositionLDL_R64 cholesky = new CholeskyDecompositionLDL_R64();
        assertTrue(cholesky.decompose(A));

        DMatrixRow_F64 foundL = cholesky.getL();

        EjmlUnitTests.assertEquals(L,foundL,UtilEjml.TEST_F64);
        for( int i = 0; i < D.length; i++ ) {
            assertEquals(D[i],cholesky.getDiagonal()[i], UtilEjml.TEST_F64);
        }
    }

    /**
     * If it is not positive definate it should fail
     */
    @Test
    public void testNotPositiveDefinate() {
        DMatrixRow_F64 A = new DMatrixRow_F64(2,2, true, 1, -1, -1, -2);

        CholeskyDecompositionLDL_R64 alg = new CholeskyDecompositionLDL_R64();
        assertFalse(alg.decompose(A));
    }
}