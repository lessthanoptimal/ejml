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

package org.ejml.alg.block.decomposition.chol;

import org.ejml.UtilEjml;
import org.ejml.alg.generic.GenericMatrixOps_F64;
import org.ejml.data.RowMatrix_F64;
import org.ejml.factory.DecompositionFactory_D64;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.ejml.ops.RandomMatrices_D64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestInnerCholesky_B64 {
    Random rand = new Random(234234);

    @Test
    public void upper() {
        checkDecompose(5, false);
        checkNotPositiveDefinite(5,true);
    }

    @Test
    public void lower() {
        checkDecompose(5, true);
        checkNotPositiveDefinite(5,true);
    }

    /**
     * Test a positive case where it should be able to decompose the matrix
     */
    private void checkDecompose(int n, boolean lower) {
        RowMatrix_F64 A = RandomMatrices_D64.createSymmPosDef(n,rand);

        // decompose a RowMatrix_F64 to find expected solution
        CholeskyDecomposition_F64<RowMatrix_F64> chol = DecompositionFactory_D64.chol(n,lower);

        assertTrue(DecompositionFactory_D64.decomposeSafe(chol,A));

        RowMatrix_F64 expected = chol.getT(null);

        // copy the original data by an offset
        double data[] = new double[ A.getNumElements() + 2 ];
        System.arraycopy(A.data,0,data,2,A.getNumElements());

        // decompose using the algorithm
        if( lower )
            assertTrue(InnerCholesky_B64.lower(data, 2, n));
        else
            assertTrue(InnerCholesky_B64.upper(data, 2, n));

        RowMatrix_F64 found = new RowMatrix_F64(n, n);
        System.arraycopy(data,2,found.data,0,found.data.length);

        // set lower triangular potion to be zero so that it is exactly the same
        assertTrue(GenericMatrixOps_F64.isEquivalentTriangle(!lower,expected,found, UtilEjml.TEST_F64));
    }

    /**
     * See if it fails when the matrix is not positive definite.
     */
    private void checkNotPositiveDefinite(int n, boolean lower) {
        RowMatrix_F64 A = new RowMatrix_F64(n,n);
        for( int i = 0; i < n; i++ ) {
            for( int j = 0; j < n; j++ ) {
                A.set(i,j,1);
            }
        }

        // copy the original data by an offset
        double data[] = new double[ A.getNumElements() + 2 ];
        System.arraycopy(A.data,0,data,2,A.getNumElements());

        // decompose using the algorithm
        if( lower )
            assertFalse(InnerCholesky_B64.lower(data, 2, n));
        else
            assertFalse(InnerCholesky_B64.upper(data, 2, n));
    }
}
