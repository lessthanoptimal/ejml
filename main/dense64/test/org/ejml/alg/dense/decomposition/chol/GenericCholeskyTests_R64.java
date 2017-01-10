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
import org.ejml.factory.DecompositionFactory_R64;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.ejml.interfaces.decomposition.LUDecomposition_F64;
import org.ejml.ops.MatrixFeatures_R64;
import org.ejml.ops.RandomMatrices_R64;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public abstract class GenericCholeskyTests_R64 {
    Random rand = new Random(0x45478);

    boolean canL = true;
    boolean canR = true;

    public abstract CholeskyDecomposition_F64<DMatrixRow_F64> create(boolean lower );

    @Test
    public void testDecomposeL() {
        if( !canL ) return;

        DMatrixRow_F64 A = new DMatrixRow_F64(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);

        DMatrixRow_F64 L = new DMatrixRow_F64(3,3, true, 1, 0, 0, 2, 3, 0, 4, 5, 7);

        CholeskyDecomposition_F64<DMatrixRow_F64> cholesky = create(true);
        assertTrue(cholesky.decompose(A));

        DMatrixRow_F64 foundL = cholesky.getT(null);

        EjmlUnitTests.assertEquals(L,foundL,UtilEjml.TEST_F64);
    }

    @Test
    public void testDecomposeR() {
        if( !canR ) return;

        DMatrixRow_F64 A = new DMatrixRow_F64(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);

        DMatrixRow_F64 R = new DMatrixRow_F64(3,3, true, 1, 2, 4, 0, 3, 5, 0, 0, 7);

        CholeskyDecomposition_F64<DMatrixRow_F64> cholesky = create(false);
        assertTrue(cholesky.decompose(A));

        DMatrixRow_F64 foundR = cholesky.getT(null);

        EjmlUnitTests.assertEquals(R,foundR,UtilEjml.TEST_F64);
    }

    /**
     * If it is not positive definate it should fail
     */
    @Test
    public void testNotPositiveDefinite() {
        DMatrixRow_F64 A = new DMatrixRow_F64(2,2, true, 1, -1, -1, -2);

        CholeskyDecomposition_F64<DMatrixRow_F64> alg = create(true);
        assertFalse(alg.decompose(A));
    }

    /**
     * The correctness of getT(null) has been tested else where effectively.  This
     * checks to see if it handles the case where an input is provided correctly.
     */
    @Test
    public void getT() {
        DMatrixRow_F64 A = new DMatrixRow_F64(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);

        CholeskyDecomposition_F64<DMatrixRow_F64> cholesky = create(true);

        assertTrue(cholesky.decompose(A));

        DMatrixRow_F64 L_null = cholesky.getT(null);
        DMatrixRow_F64 L_provided = RandomMatrices_R64.createRandom(3,3,rand);
        assertTrue( L_provided == cholesky.getT(L_provided));

        assertTrue(MatrixFeatures_R64.isEquals(L_null,L_provided));
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
        SimpleMatrix A = SimpleMatrix.wrap( RandomMatrices_R64.createSymmPosDef(size,rand));

        CholeskyDecomposition_F64<DMatrixRow_F64> cholesky = create(lower);
        assertTrue(DecompositionFactory_R64.decomposeSafe(cholesky,(DMatrixRow_F64)A.getMatrix()));

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

        LUDecomposition_F64<DMatrixRow_F64> lu = DecompositionFactory_R64.lu(size,size);
        CholeskyDecomposition_F64<DMatrixRow_F64> cholesky = create(lower);

        DMatrixRow_F64 A = RandomMatrices_R64.createSymmPosDef(size,rand);

        assertTrue(DecompositionFactory_R64.decomposeSafe(lu,A));
        assertTrue(DecompositionFactory_R64.decomposeSafe(cholesky,A));

        double expected = lu.computeDeterminant().real;
        double found = cholesky.computeDeterminant().real;

        assertEquals(expected,found,UtilEjml.TEST_F64);
    }
}
