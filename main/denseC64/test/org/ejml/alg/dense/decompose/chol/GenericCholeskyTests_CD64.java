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

import org.ejml.data.CDenseMatrix64F;
import org.ejml.factory.CDecompositionFactory;
import org.ejml.interfaces.decomposition.CholeskyDecomposition;
import org.ejml.ops.CCommonOps;
import org.ejml.ops.CMatrixFeatures;
import org.ejml.ops.CRandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
* @author Peter Abeles
*/
// TODO Handle special case of 1x1 matrix
public abstract class GenericCholeskyTests_CD64 {
    Random rand = new Random(0x45478);

    boolean canL = true;
    boolean canR = false;

    public abstract CholeskyDecomposition<CDenseMatrix64F> create( boolean lower );

//    /**
//     * If it is not positive definate it should fail
//     */
//    @Test
//    public void testNotPositiveDefinite() {
//        DenseMatrix64F A = new DenseMatrix64F(2,2, true, 1, -1, -1, -2);
//
//        CholeskyDecomposition<DenseMatrix64F> alg = create(true);
//        assertFalse(alg.decompose(A));
//    }
//
//    /**
//     * The correctness of getT(null) has been tested else where effectively.  This
//     * checks to see if it handles the case where an input is provided correctly.
//     */
//    @Test
//    public void getT() {
//        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);
//
//        CholeskyDecomposition<CDenseMatrix64F> cholesky = create(true);
//
//        assertTrue(cholesky.decompose(A));
//
//        CDenseMatrix64F L_null = cholesky.getT(null);
//        CDenseMatrix64F L_provided = CRandomMatrices.createRandom(3, 3, rand);
//        assertTrue( L_provided == cholesky.getT(L_provided));
//
//        assertTrue(CMatrixFeatures.isEquals(L_null, L_provided));
//    }

    /**
     * A 1x1 matrix is a special case
     */
    @Test
    public void checkWithDefinition_1x1() {
        fail("Implement");
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

            // start at size = 2 since 1 is a special case
            for( int size = 4; size < 10; size++ ) {
                System.out.println("----------- Size = "+size);
                checkWithDefinition(lower, size);
            }
        }
    }

    private void checkWithDefinition(boolean lower, int size) {
        CDenseMatrix64F A = CRandomMatrices.createSymmPosDef(size, rand);

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

        A.print();
        found.print();
        T.print();

//        CCommonOps.conjugate(T,T);
//        CCommonOps.conjugate(T_trans,T_trans);
//        CCommonOps.mult(T,T_trans,found);
//        found.print();
        assertTrue(CMatrixFeatures.isIdentical(A, found, 1e-8));
    }

//    @Test
//    public void checkDeterminant() {
//        for( int i = 0; i < 2; i++ ) {
//            boolean lower = i == 0;
//            if( lower && !canL )
//                continue;
//            if( !lower && !canR )
//                continue;
//
//            for( int size = 1; size < 20; size += 2 ) {
//                checkDeterminant(lower, size);
//            }
//        }
//    }
//
//    public void checkDeterminant( boolean lower , int size ) {
//
//        LUDecomposition<DenseMatrix64F> lu = DecompositionFactory.lu(size,size);
//        CholeskyDecomposition<DenseMatrix64F> cholesky = create(lower);
//
//        DenseMatrix64F A = RandomMatrices.createSymmPosDef(size,rand);
//
//        assertTrue(DecompositionFactory.decomposeSafe(lu,A));
//        assertTrue(DecompositionFactory.decomposeSafe(cholesky,A));
//
//        double expected = lu.computeDeterminant().real;
//        double found = cholesky.computeDeterminant().real;
//
//        assertEquals(expected,found,1e-8);
//    }
}
