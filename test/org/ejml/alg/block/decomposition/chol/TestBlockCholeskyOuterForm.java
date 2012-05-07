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

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.dense.decomposition.CholeskyDecomposition;
import org.ejml.alg.dense.decomposition.DecompositionFactory;
import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestBlockCholeskyOuterForm {

    Random rand = new Random(1231);

    // size of a block
    int bl = 5;

    /**
     * Test upper cholesky decomposition for upper triangular.
     */
    @Test
    public void testUpper() {
        // test against various different sizes
        for( int N = bl-2; N <= 13; N += 2 ) {
            DenseMatrix64F A = RandomMatrices.createSymmPosDef(N,rand);

            CholeskyDecomposition<DenseMatrix64F> chol = DecompositionFactory.chol(1,false);
            assertTrue(chol.decompose(A));

            DenseMatrix64F L = chol.getT(null);

            BlockMatrix64F blockA = BlockMatrixOps.convert(A,bl);

            BlockCholeskyOuterForm blockChol = new BlockCholeskyOuterForm(false);

            assertTrue(DecompositionFactory.decomposeSafe(blockChol,blockA));

            assertTrue(GenericMatrixOps.isEquivalent(L,blockA,1e-8));
        }
    }

    /**
     * Test upper cholesky decomposition for upper triangular.
     */
    @Test
    public void testLower() {
        // test against various different sizes
        for( int N = bl-2; N <= 13; N += 2 ) {

            DenseMatrix64F A = RandomMatrices.createSymmPosDef(N,rand);

            CholeskyDecomposition<DenseMatrix64F> chol = DecompositionFactory.chol(1,true);
            assertTrue(chol.decompose(A));

            DenseMatrix64F L = chol.getT(null);

            BlockMatrix64F blockA = BlockMatrixOps.convert(A,bl);

            BlockCholeskyOuterForm blockChol = new BlockCholeskyOuterForm(true);

            assertTrue(DecompositionFactory.decomposeSafe(blockChol,blockA));

            assertTrue(GenericMatrixOps.isEquivalent(L,blockA,1e-8));
        }
    }
}
