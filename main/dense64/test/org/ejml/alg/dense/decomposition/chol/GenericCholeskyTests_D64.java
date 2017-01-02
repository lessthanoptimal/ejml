/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory_D64;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.ejml.interfaces.decomposition.LUDecomposition_F64;
import org.ejml.ops.MatrixFeatures_D64;
import org.ejml.ops.RandomMatrices_D64;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public abstract class GenericCholeskyTests_D64 {
    Random rand = new Random(0x45478);

    boolean canL = true;
    boolean canR = true;

    public abstract CholeskyDecomposition_F64<DenseMatrix64F> create(boolean lower );

    @Test
    public void testDecomposeL() {
        if( !canL ) return;

        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);

        DenseMatrix64F L = new DenseMatrix64F(3,3, true, 1, 0, 0, 2, 3, 0, 4, 5, 7);

        CholeskyDecomposition_F64<DenseMatrix64F> cholesky = create(true);
        assertTrue(cholesky.decompose(A));

        DenseMatrix64F foundL = cholesky.getT(null);

        EjmlUnitTests.assertEquals(L,foundL,UtilEjml.TEST_64F);
    }

    @Test
    public void testDecomposeR() {
        if( !canR ) return;

        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);

        DenseMatrix64F R = new DenseMatrix64F(3,3, true, 1, 2, 4, 0, 3, 5, 0, 0, 7);

        CholeskyDecomposition_F64<DenseMatrix64F> cholesky = create(false);
        assertTrue(cholesky.decompose(A));

        DenseMatrix64F foundR = cholesky.getT(null);

        EjmlUnitTests.assertEquals(R,foundR,UtilEjml.TEST_64F);
    }

    /**
     * If it is not positive definate it should fail
     */
    @Test
    public void testNotPositiveDefinite() {
        DenseMatrix64F A = new DenseMatrix64F(2,2, true, 1, -1, -1, -2);

        CholeskyDecomposition_F64<DenseMatrix64F> alg = create(true);
        assertFalse(alg.decompose(A));
    }

    /**
     * The correctness of getT(null) has been tested else where effectively.  This
     * checks to see if it handles the case where an input is provided correctly.
     */
    @Test
    public void getT() {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);

        CholeskyDecomposition_F64<DenseMatrix64F> cholesky = create(true);

        assertTrue(cholesky.decompose(A));

        DenseMatrix64F L_null = cholesky.getT(null);
        DenseMatrix64F L_provided = RandomMatrices_D64.createRandom(3,3,rand);
        assertTrue( L_provided == cholesky.getT(L_provided));

        assertTrue(MatrixFeatures_D64.isEquals(L_null,L_provided));
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
        SimpleMatrix A = SimpleMatrix.wrap( RandomMatrices_D64.createSymmPosDef(size,rand));

        CholeskyDecomposition_F64<DenseMatrix64F> cholesky = create(lower);
        assertTrue(DecompositionFactory_D64.decomposeSafe(cholesky,A.getMatrix()));

        SimpleMatrix T = SimpleMatrix.wrap(cholesky.getT(null));
        SimpleMatrix found;

        if( lower ) {
            found = T.mult(T.transpose());
        } else {
            found = T.transpose().mult(T);
        }

        assertTrue(A.isIdentical(found, UtilEjml.TEST_64F));
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

        LUDecomposition_F64<DenseMatrix64F> lu = DecompositionFactory_D64.lu(size,size);
        CholeskyDecomposition_F64<DenseMatrix64F> cholesky = create(lower);

        DenseMatrix64F A = RandomMatrices_D64.createSymmPosDef(size,rand);

        assertTrue(DecompositionFactory_D64.decomposeSafe(lu,A));
        assertTrue(DecompositionFactory_D64.decomposeSafe(cholesky,A));

        double expected = lu.computeDeterminant().real;
        double found = cholesky.computeDeterminant().real;

        assertEquals(expected,found,UtilEjml.TEST_64F);
    }
}
