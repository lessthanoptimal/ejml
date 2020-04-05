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

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.ejml.dense.row.decomposition.CheckDecompositionInterface_DDRM.checkModifiedInput;
import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public class TestCholeskyDecompositionLDL_DDRM {

    Random rand = new Random(0x45478);

        @Test
    public void checkModifyInput() {
        checkModifiedInput(new CholeskyDecompositionLDL_DDRM());
    }

    @Test
    public void testDecompose() {
        DMatrixRMaj A = new DMatrixRMaj(3,3, true, 1, 2, 4, 2, 7, 23, 4, 23, 98);


        DMatrixRMaj L = new DMatrixRMaj(3,3, true, 1, 0, 0, 2, 1, 0, 4, 5, 1);

        double D[] = new double[]{1,3,7};

        CholeskyDecompositionLDL_DDRM cholesky = new CholeskyDecompositionLDL_DDRM();
        assertTrue(cholesky.decompose(A));

        DMatrixRMaj foundL = cholesky.getL();

        EjmlUnitTests.assertEquals(L,foundL,UtilEjml.TEST_F64);
        for( int i = 0; i < D.length; i++ ) {
            assertEquals(D[i],cholesky.getDiagonal()[i], UtilEjml.TEST_F64);
        }
    }

    /**
     * If it is not positive definite it should fail
     */
    @Test
    public void testNotPositiveDefinate() {
        DMatrixRMaj A = new DMatrixRMaj(2,2, true, 1, -1, -1, -2);

        CholeskyDecompositionLDL_DDRM alg = new CholeskyDecompositionLDL_DDRM();
        assertFalse(alg.decompose(A));
    }
}