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

package org.ejml.dense.row.decompose.chol;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F32;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.MatrixFeatures_CDRM;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.ejml.dense.row.decompose.CheckDecompositionInterface_CDRM;
import org.ejml.dense.row.factory.DecompositionFactory_CDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F32;
import org.ejml.interfaces.decomposition.LUDecomposition_F32;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
* @author Peter Abeles
*/
// TODO Handle special case of 1x1 matrix
public abstract class GenericCholeskyTests_CDRM {
    Random rand = new Random(0x45478);

    boolean canL = true;
    boolean canR = true;

    public abstract CholeskyDecomposition_F32<CMatrixRMaj> create(boolean lower );

    @Test
    public void checkModifyInput() {
        CheckDecompositionInterface_CDRM.checkModifiedInput(create(true));
        CheckDecompositionInterface_CDRM.checkModifiedInput(create(false));
    }

    /**
     * If it is not positive definate it should fail
     */
    @Test
    public void testNotPositiveDefinite() {
        CMatrixRMaj A = new CMatrixRMaj(2, 2, true, 1, 0, -1, 0, -1, 0, -2, 0);

        CholeskyDecomposition_F32<CMatrixRMaj> alg = create(true);
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
        CMatrixRMaj A = RandomMatrices_CDRM.hermitianPosDef(size, rand);

        CholeskyDecomposition_F32<CMatrixRMaj> cholesky = create(lower);
        assertTrue(DecompositionFactory_CDRM.decomposeSafe(cholesky, A));

        CMatrixRMaj T = cholesky.getT(null);
        CMatrixRMaj T_trans = new CMatrixRMaj(size,size);
        CommonOps_CDRM.transposeConjugate(T, T_trans);
        CMatrixRMaj found = new CMatrixRMaj(size,size);

        if( lower ) {
            CommonOps_CDRM.mult(T,T_trans,found);
        } else {
            CommonOps_CDRM.mult(T_trans,T,found);
        }

        assertTrue(MatrixFeatures_CDRM.isIdentical(A, found, UtilEjml.TEST_F32));
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

        LUDecomposition_F32<CMatrixRMaj> lu = DecompositionFactory_CDRM.lu(size,size);
        CholeskyDecomposition_F32<CMatrixRMaj> cholesky = create(lower);

        CMatrixRMaj A = RandomMatrices_CDRM.hermitianPosDef(size, rand);

        assertTrue(DecompositionFactory_CDRM.decomposeSafe(lu,A));
        assertTrue(DecompositionFactory_CDRM.decomposeSafe(cholesky,A));

        Complex_F32 expected = lu.computeDeterminant();
        Complex_F32 found = cholesky.computeDeterminant();

        assertEquals(expected.real,found.real,UtilEjml.TEST_F32);
        assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F32);
    }

    @Test
    public void failZeros() {
        CMatrixRMaj A = new CMatrixRMaj(3,3);

        assertFalse(create(true).decompose(A));
        assertFalse(create(false).decompose(A));
    }
}
