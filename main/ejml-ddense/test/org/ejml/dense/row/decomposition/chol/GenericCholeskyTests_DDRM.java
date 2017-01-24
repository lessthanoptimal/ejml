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

package org.ejml.dense.row.decomposition.chol;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.ejml.interfaces.decomposition.LUDecomposition_F64;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public abstract class GenericCholeskyTests_DDRM {
    Random rand = new Random(0x45478);

    boolean canL = true;
    boolean canR = true;

    public abstract CholeskyDecomposition_F64<DMatrixRMaj> create(boolean lower );

    @Test
    public void testDecomposeL() {
        if( !canL ) return;

        DMatrixRMaj A = new DMatrixRMaj(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);

        DMatrixRMaj L = new DMatrixRMaj(3,3, true, 1, 0, 0, 2, 3, 0, 4, 5, 7);

        CholeskyDecomposition_F64<DMatrixRMaj> cholesky = create(true);
        assertTrue(cholesky.decompose(A));

        DMatrixRMaj foundL = cholesky.getT(null);

        EjmlUnitTests.assertEquals(L,foundL,UtilEjml.TEST_F64);
    }

    @Test
    public void testDecomposeR() {
        if( !canR ) return;

        DMatrixRMaj A = new DMatrixRMaj(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);

        DMatrixRMaj R = new DMatrixRMaj(3,3, true, 1, 2, 4, 0, 3, 5, 0, 0, 7);

        CholeskyDecomposition_F64<DMatrixRMaj> cholesky = create(false);
        assertTrue(cholesky.decompose(A));

        DMatrixRMaj foundR = cholesky.getT(null);

        EjmlUnitTests.assertEquals(R,foundR,UtilEjml.TEST_F64);
    }

    /**
     * If it is not positive definate it should fail
     */
    @Test
    public void testNotPositiveDefinite() {
        DMatrixRMaj A = new DMatrixRMaj(2,2, true, 1, -1, -1, -2);

        CholeskyDecomposition_F64<DMatrixRMaj> alg = create(true);
        assertFalse(alg.decompose(A));
    }

    /**
     * The correctness of getT(null) has been tested else where effectively.  This
     * checks to see if it handles the case where an input is provided correctly.
     */
    @Test
    public void getT() {
        DMatrixRMaj A = new DMatrixRMaj(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);

        CholeskyDecomposition_F64<DMatrixRMaj> cholesky = create(true);

        assertTrue(cholesky.decompose(A));

        DMatrixRMaj L_null = cholesky.getT(null);
        DMatrixRMaj L_provided = RandomMatrices_DDRM.rectangle(3,3,rand);
        assertTrue( L_provided == cholesky.getT(L_provided));

        assertTrue(MatrixFeatures_DDRM.isEquals(L_null,L_provided));
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
        SimpleMatrix A = SimpleMatrix.wrap( RandomMatrices_DDRM.symmetricPosDef(size,rand));

        CholeskyDecomposition_F64<DMatrixRMaj> cholesky = create(lower);
        assertTrue(DecompositionFactory_DDRM.decomposeSafe(cholesky,(DMatrixRMaj)A.getMatrix()));

        SimpleMatrix T = SimpleMatrix.wrap(cholesky.getT(null));
        SimpleMatrix found;

        if( lower ) {
            found = T.mult(T.transpose());
        } else {
            found = T.transpose().mult(T);
        }

        assertTrue(A.isIdentical(found, UtilEjml.TEST_F64));
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

        LUDecomposition_F64<DMatrixRMaj> lu = DecompositionFactory_DDRM.lu(size,size);
        CholeskyDecomposition_F64<DMatrixRMaj> cholesky = create(lower);

        DMatrixRMaj A = RandomMatrices_DDRM.symmetricPosDef(size,rand);

        assertTrue(DecompositionFactory_DDRM.decomposeSafe(lu,A));
        assertTrue(DecompositionFactory_DDRM.decomposeSafe(cholesky,A));

        double expected = lu.computeDeterminant().real;
        double found = cholesky.computeDeterminant().real;

        assertEquals(expected,found,UtilEjml.TEST_F64);
    }
}
