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

package org.ejml.alg.block.decomposition.chol;

import org.ejml.UtilEjml;
import org.ejml.alg.block.MatrixOps_B64;
import org.ejml.alg.generic.GenericMatrixOps_F64;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory_D64;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.ejml.ops.RandomMatrices_D64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
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
            DenseMatrix64F A = RandomMatrices_D64.createSymmPosDef(N,rand);

            CholeskyDecomposition_F64<DenseMatrix64F> chol = DecompositionFactory_D64.chol(1,false);
            assertTrue(DecompositionFactory_D64.decomposeSafe(chol,A));

            DenseMatrix64F expectedT = chol.getT(null);

            BlockMatrix64F blockA = MatrixOps_B64.convert(A,bl);

            CholeskyOuterForm_B64 blockChol = new CholeskyOuterForm_B64(false);

            assertTrue(DecompositionFactory_D64.decomposeSafe(blockChol,blockA));

            assertTrue(GenericMatrixOps_F64.isEquivalent(expectedT,blockChol.getT(null), UtilEjml.TEST_64F));

            double blockDet = blockChol.computeDeterminant().real;
            double expectedDet = chol.computeDeterminant().real;

            assertEquals(expectedDet,blockDet,UtilEjml.TEST_64F);
        }
    }

    /**
     * Test upper cholesky decomposition for upper triangular.
     */
    @Test
    public void testLower() {
        // test against various different sizes
        for( int N = bl-2; N <= 13; N += 2 ) {

            DenseMatrix64F A = RandomMatrices_D64.createSymmPosDef(N,rand);

            CholeskyDecomposition_F64<DenseMatrix64F> chol = DecompositionFactory_D64.chol(1,true);
            assertTrue(DecompositionFactory_D64.decomposeSafe(chol, A));

            DenseMatrix64F expectedT = chol.getT(null);

            BlockMatrix64F blockA = MatrixOps_B64.convert(A,bl);

            CholeskyOuterForm_B64 blockChol = new CholeskyOuterForm_B64(true);

            assertTrue(DecompositionFactory_D64.decomposeSafe(blockChol,blockA));

            assertTrue(GenericMatrixOps_F64.isEquivalent(expectedT,blockChol.getT(null),UtilEjml.TEST_64F));

            double blockDet = blockChol.computeDeterminant().real;
            double expectedDet = chol.computeDeterminant().real;

            assertEquals(expectedDet,blockDet,UtilEjml.TEST_64F);
        }
    }
}
