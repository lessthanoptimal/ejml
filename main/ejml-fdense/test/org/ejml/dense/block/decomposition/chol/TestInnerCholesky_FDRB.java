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

package org.ejml.dense.block.decomposition.chol;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.generic.GenericMatrixOps_F32;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F32;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestInnerCholesky_FDRB {
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
        FMatrixRMaj A = RandomMatrices_FDRM.symmetricPosDef(n,rand);

        // decompose a FMatrixRMaj to find expected solution
        CholeskyDecomposition_F32<FMatrixRMaj> chol = DecompositionFactory_FDRM.chol(n,lower);

        assertTrue(DecompositionFactory_FDRM.decomposeSafe(chol,A));

        FMatrixRMaj expected = chol.getT(null);

        // copy the original data by an offset
        float data[] = new float[ A.getNumElements() + 2 ];
        System.arraycopy(A.data,0,data,2,A.getNumElements());

        // decompose using the algorithm
        if( lower )
            assertTrue(InnerCholesky_FDRB.lower(data, 2, n));
        else
            assertTrue(InnerCholesky_FDRB.upper(data, 2, n));

        FMatrixRMaj found = new FMatrixRMaj(n, n);
        System.arraycopy(data,2,found.data,0,found.data.length);

        // set lower triangular potion to be zero so that it is exactly the same
        assertTrue(GenericMatrixOps_F32.isEquivalentTriangle(!lower,expected,found, UtilEjml.TEST_F32));
    }

    /**
     * See if it fails when the matrix is not positive definite.
     */
    private void checkNotPositiveDefinite(int n, boolean lower) {
        FMatrixRMaj A = new FMatrixRMaj(n,n);
        for( int i = 0; i < n; i++ ) {
            for( int j = 0; j < n; j++ ) {
                A.set(i,j,1);
            }
        }

        // copy the original data by an offset
        float data[] = new float[ A.getNumElements() + 2 ];
        System.arraycopy(A.data,0,data,2,A.getNumElements());

        // decompose using the algorithm
        if( lower )
            assertFalse(InnerCholesky_FDRB.lower(data, 2, n));
        else
            assertFalse(InnerCholesky_FDRB.upper(data, 2, n));
    }
}
