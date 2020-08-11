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
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F32;
import org.ejml.interfaces.decomposition.LUDecomposition_F32;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public abstract class GenericCholeskyTests_FDRM {
    Random rand = new Random(0x45478);

    boolean canL = true;
    boolean canR = true;

    public abstract CholeskyDecomposition_F32<FMatrixRMaj> create(boolean lower );

    @Test
    public void testDecomposeL() {
        if( !canL ) return;

        FMatrixRMaj A = new FMatrixRMaj(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);

        FMatrixRMaj L = new FMatrixRMaj(3,3, true, 1, 0, 0, 2, 3, 0, 4, 5, 7);

        CholeskyDecomposition_F32<FMatrixRMaj> cholesky = create(true);
        assertTrue(cholesky.decompose(A));

        FMatrixRMaj foundL = cholesky.getT(null);

        EjmlUnitTests.assertEquals(L,foundL,UtilEjml.TEST_F32);
    }

    @Test
    public void testDecomposeR() {
        if( !canR ) return;

        FMatrixRMaj A = new FMatrixRMaj(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);

        FMatrixRMaj R = new FMatrixRMaj(3,3, true, 1, 2, 4, 0, 3, 5, 0, 0, 7);

        CholeskyDecomposition_F32<FMatrixRMaj> cholesky = create(false);
        assertTrue(cholesky.decompose(A));

        FMatrixRMaj foundR = cholesky.getT(null);

        EjmlUnitTests.assertEquals(R,foundR,UtilEjml.TEST_F32);
    }

    /**
     * If it is not positive definate it should fail
     */
    @Test
    public void testNotPositiveDefinite() {
        FMatrixRMaj A = new FMatrixRMaj(2,2, true, 1, -1, -1, -2);

        CholeskyDecomposition_F32<FMatrixRMaj> alg = create(true);
        assertFalse(alg.decompose(A));
    }

    /**
     * The correctness of getT(null) has been tested else where effectively.  This
     * checks to see if it handles the case where an input is provided correctly.
     */
    @Test
    public void getT() {
        FMatrixRMaj A = new FMatrixRMaj(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);

        CholeskyDecomposition_F32<FMatrixRMaj> cholesky = create(true);

        assertTrue(cholesky.decompose(A));

        FMatrixRMaj L_null = cholesky.getT(null);
        FMatrixRMaj L_provided = RandomMatrices_FDRM.rectangle(3,3,rand);
        assertTrue( L_provided == cholesky.getT(L_provided));

        assertTrue(MatrixFeatures_FDRM.isEquals(L_null,L_provided));
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
        SimpleMatrix A = SimpleMatrix.wrap( RandomMatrices_FDRM.symmetricPosDef(size,rand));

        CholeskyDecomposition_F32<FMatrixRMaj> cholesky = create(lower);
        assertTrue(DecompositionFactory_FDRM.decomposeSafe(cholesky,(FMatrixRMaj)A.getMatrix()));

        SimpleMatrix T = SimpleMatrix.wrap(cholesky.getT(null));
        SimpleMatrix found;

        if( lower ) {
            found = T.mult(T.transpose());
        } else {
            found = T.transpose().mult(T);
        }

        assertTrue(A.isIdentical(found, UtilEjml.TEST_F32));
    }

    @Test
    public void checkDeterminant() {
        for( int i = 0; i < 2; i++ ) {
            boolean lower = i == 0;
            if( lower && !canL )
                continue;
            if( !lower && !canR )
                continue;

            for( int size = 1; size < 20; size += 2 ) {
                checkDeterminant(lower, size);
            }
        }
    }

    public void checkDeterminant( boolean lower , int size ) {

        LUDecomposition_F32<FMatrixRMaj> lu = DecompositionFactory_FDRM.lu(size,size);
        CholeskyDecomposition_F32<FMatrixRMaj> cholesky = create(lower);

        FMatrixRMaj A = RandomMatrices_FDRM.symmetricPosDef(size,rand);

        assertTrue(DecompositionFactory_FDRM.decomposeSafe(lu,A));
        assertTrue(DecompositionFactory_FDRM.decomposeSafe(cholesky,A));

        float expected = lu.computeDeterminant().real;
        float found = cholesky.computeDeterminant().real;

        assertEquals(expected,found,UtilEjml.TEST_F32);
    }
}
