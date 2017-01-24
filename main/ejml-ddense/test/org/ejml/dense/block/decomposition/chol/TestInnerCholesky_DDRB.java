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

package org.ejml.dense.block.decomposition.chol;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.generic.GenericMatrixOps_F64;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestInnerCholesky_DDRB {
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
        DMatrixRMaj A = RandomMatrices_DDRM.symmetricPosDef(n,rand);

        // decompose a DMatrixRMaj to find expected solution
        CholeskyDecomposition_F64<DMatrixRMaj> chol = DecompositionFactory_DDRM.chol(n,lower);

        assertTrue(DecompositionFactory_DDRM.decomposeSafe(chol,A));

        DMatrixRMaj expected = chol.getT(null);

        // copy the original data by an offset
        double data[] = new double[ A.getNumElements() + 2 ];
        System.arraycopy(A.data,0,data,2,A.getNumElements());

        // decompose using the algorithm
        if( lower )
            assertTrue(InnerCholesky_DDRB.lower(data, 2, n));
        else
            assertTrue(InnerCholesky_DDRB.upper(data, 2, n));

        DMatrixRMaj found = new DMatrixRMaj(n, n);
        System.arraycopy(data,2,found.data,0,found.data.length);

        // set lower triangular potion to be zero so that it is exactly the same
        assertTrue(GenericMatrixOps_F64.isEquivalentTriangle(!lower,expected,found, UtilEjml.TEST_F64));
    }

    /**
     * See if it fails when the matrix is not positive definite.
     */
    private void checkNotPositiveDefinite(int n, boolean lower) {
        DMatrixRMaj A = new DMatrixRMaj(n,n);
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
            assertFalse(InnerCholesky_DDRB.lower(data, 2, n));
        else
            assertFalse(InnerCholesky_DDRB.upper(data, 2, n));
    }
}
