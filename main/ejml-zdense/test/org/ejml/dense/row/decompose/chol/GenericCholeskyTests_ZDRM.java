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

package org.ejml.dense.row.decompose.chol;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.MatrixFeatures_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.ejml.dense.row.decompose.CheckDecompositionInterface_ZDRM;
import org.ejml.dense.row.factory.DecompositionFactory_ZDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.ejml.interfaces.decomposition.LUDecomposition_F64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
* @author Peter Abeles
*/
// TODO Handle special case of 1x1 matrix
public abstract class GenericCholeskyTests_ZDRM {
    Random rand = new Random(0x45478);

    boolean canL = true;
    boolean canR = true;

    public abstract CholeskyDecomposition_F64<ZMatrixRMaj> create(boolean lower );

    @Test
    public void checkModifyInput() {
        CheckDecompositionInterface_ZDRM.checkModifiedInput(create(true));
        CheckDecompositionInterface_ZDRM.checkModifiedInput(create(false));
    }

    /**
     * If it is not positive definate it should fail
     */
    @Test
    public void testNotPositiveDefinite() {
        ZMatrixRMaj A = new ZMatrixRMaj(2, 2, true, 1, 0, -1, 0, -1, 0, -2, 0);

        CholeskyDecomposition_F64<ZMatrixRMaj> alg = create(true);
        assertFalse(alg.decompose(A));
    }

    /**
     * Test across several different matrix sizes and upper/lower decompositions using
     * the definition of cholesky.
     */
    @Test
    public void checkWithDefinition() {
        for( int i = 0; i < 2; i++ ) {
            boolean lower = i == 0;
            if( lower && !canL )
                continue;
            if( !lower && !canR )
                continue;

            for( int size = 1; size < 10; size++ ) {
                checkWithDefinition(lower, size);
            }
        }
    }

    private void checkWithDefinition(boolean lower, int size) {
        ZMatrixRMaj A = RandomMatrices_ZDRM.createHermPosDef(size, rand);

        CholeskyDecomposition_F64<ZMatrixRMaj> cholesky = create(lower);
        assertTrue(DecompositionFactory_ZDRM.decomposeSafe(cholesky, A));

        ZMatrixRMaj T = cholesky.getT(null);
        ZMatrixRMaj T_trans = new ZMatrixRMaj(size,size);
        CommonOps_ZDRM.transposeConjugate(T, T_trans);
        ZMatrixRMaj found = new ZMatrixRMaj(size,size);

        if( lower ) {
            CommonOps_ZDRM.mult(T,T_trans,found);
        } else {
            CommonOps_ZDRM.mult(T_trans,T,found);
        }

        assertTrue(MatrixFeatures_ZDRM.isIdentical(A, found, UtilEjml.TEST_F64));
    }

    @Test
    public void checkDeterminant() {
        for( int i = 0; i < 2; i++ ) {
            boolean lower = i == 0;
            if( lower && !canL )
                continue;
            if( !lower && !canR )
                continue;

            for( int size = 2; size < 20; size += 2 ) {
                checkDeterminant(lower, size);
            }
        }
    }

    public void checkDeterminant( boolean lower , int size ) {

        LUDecomposition_F64<ZMatrixRMaj> lu = DecompositionFactory_ZDRM.lu(size,size);
        CholeskyDecomposition_F64<ZMatrixRMaj> cholesky = create(lower);

        ZMatrixRMaj A = RandomMatrices_ZDRM.createHermPosDef(size, rand);

        assertTrue(DecompositionFactory_ZDRM.decomposeSafe(lu,A));
        assertTrue(DecompositionFactory_ZDRM.decomposeSafe(cholesky,A));

        Complex_F64 expected = lu.computeDeterminant();
        Complex_F64 found = cholesky.computeDeterminant();

        assertEquals(expected.real,found.real,UtilEjml.TEST_F64);
        assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F64);
    }

    @Test
    public void failZeros() {
        ZMatrixRMaj A = new ZMatrixRMaj(3,3);

        assertFalse(create(true).decompose(A));
        assertFalse(create(false).decompose(A));
    }
}
