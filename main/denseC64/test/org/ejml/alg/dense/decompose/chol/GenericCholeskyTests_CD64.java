/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decompose.chol;

import org.ejml.alg.dense.decompose.CheckDecompositionInterface_CD64;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.ejml.factory.CDecompositionFactory;
import org.ejml.interfaces.decomposition.CholeskyDecomposition;
import org.ejml.interfaces.decomposition.LUDecomposition;
import org.ejml.ops.CCommonOps;
import org.ejml.ops.CMatrixFeatures;
import org.ejml.ops.CRandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
* @author Peter Abeles
*/
// TODO Handle special case of 1x1 matrix
public abstract class GenericCholeskyTests_CD64 {
    Random rand = new Random(0x45478);

    boolean canL = true;
    boolean canR = true;

    public abstract CholeskyDecomposition<CDenseMatrix64F> create( boolean lower );

    @Test
    public void checkModifyInput() {
        CheckDecompositionInterface_CD64.checkModifiedInput(create(true));
        CheckDecompositionInterface_CD64.checkModifiedInput(create(false));
    }

    /**
     * If it is not positive definate it should fail
     */
    @Test
    public void testNotPositiveDefinite() {
        CDenseMatrix64F A = new CDenseMatrix64F(2, 2, true, 1, 0, -1, 0, -1, 0, -2, 0);

        CholeskyDecomposition<CDenseMatrix64F> alg = create(true);
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
        CDenseMatrix64F A = CRandomMatrices.createHermPosDef(size, rand);

        CholeskyDecomposition<CDenseMatrix64F> cholesky = create(lower);
        assertTrue(CDecompositionFactory.decomposeSafe(cholesky, A));

        CDenseMatrix64F T = cholesky.getT(null);
        CDenseMatrix64F T_trans = new CDenseMatrix64F(size,size);
        CCommonOps.transposeConjugate(T, T_trans);
        CDenseMatrix64F found = new CDenseMatrix64F(size,size);

        if( lower ) {
            CCommonOps.mult(T,T_trans,found);
        } else {
            CCommonOps.mult(T_trans,T,found);
        }

        assertTrue(CMatrixFeatures.isIdentical(A, found, 1e-8));
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

        LUDecomposition<CDenseMatrix64F> lu = CDecompositionFactory.lu(size,size);
        CholeskyDecomposition<CDenseMatrix64F> cholesky = create(lower);

        CDenseMatrix64F A = CRandomMatrices.createHermPosDef(size, rand);

        assertTrue(CDecompositionFactory.decomposeSafe(lu,A));
        assertTrue(CDecompositionFactory.decomposeSafe(cholesky,A));

        Complex64F expected = lu.computeDeterminant();
        Complex64F found = cholesky.computeDeterminant();

        assertEquals(expected.real,found.real,1e-8);
        assertEquals(expected.imaginary,found.imaginary,1e-8);
    }

    @Test
    public void failZeros() {
        CDenseMatrix64F A = new CDenseMatrix64F(3,3);

        assertFalse(create(true).decompose(A));
        assertFalse(create(false).decompose(A));
    }
}
