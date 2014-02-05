/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.CholeskyDecomposition;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestCholeskyOuterForm_B64 {

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

            CholeskyOuterForm_B64 blockChol = new CholeskyOuterForm_B64(false);

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

            CholeskyOuterForm_B64 blockChol = new CholeskyOuterForm_B64(true);

            assertTrue(DecompositionFactory.decomposeSafe(blockChol,blockA));

            assertTrue(GenericMatrixOps.isEquivalent(L,blockA,1e-8));
        }
    }
}
