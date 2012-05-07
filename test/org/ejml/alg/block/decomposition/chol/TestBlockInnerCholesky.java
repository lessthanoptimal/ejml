/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.block.decomposition.chol;

import org.ejml.alg.dense.decomposition.CholeskyDecomposition;
import org.ejml.alg.dense.decomposition.DecompositionFactory;
import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestBlockInnerCholesky {
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
        DenseMatrix64F A = RandomMatrices.createSymmPosDef(n,rand);

        // decompose a DenseMatrix64F to find expected solution
        CholeskyDecomposition<DenseMatrix64F> chol = DecompositionFactory.chol(n,lower);

        assertTrue(DecompositionFactory.decomposeSafe(chol,A));

        DenseMatrix64F expected = chol.getT(null);

        // copy the original data by an offset
        double data[] = new double[ A.getNumElements() + 2 ];
        System.arraycopy(A.data,0,data,2,A.getNumElements());

        // decompose using the algorithm
        if( lower )
            assertTrue(BlockInnerCholesky.lower(data,2, n));
        else
            assertTrue(BlockInnerCholesky.upper(data,2, n));

        DenseMatrix64F found = new DenseMatrix64F(n, n);
        System.arraycopy(data,2,found.data,0,found.data.length);

        // set lower triangular potion to be zero so that it is exactly the same
        assertTrue(GenericMatrixOps.isEquivalentTriangle(!lower,expected,found,1e-10));
    }

    /**
     * See if it fails when the matrix is not positive definite.
     */
    private void checkNotPositiveDefinite(int n, boolean lower) {
        DenseMatrix64F A = new DenseMatrix64F(n,n);
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
            assertFalse(BlockInnerCholesky.lower(data,2, n));
        else
            assertFalse(BlockInnerCholesky.upper(data,2, n));
    }
}
