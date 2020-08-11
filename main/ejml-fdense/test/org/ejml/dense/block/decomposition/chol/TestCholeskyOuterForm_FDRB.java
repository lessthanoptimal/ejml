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
import org.ejml.data.FMatrixRBlock;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.block.MatrixOps_FDRB;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.generic.GenericMatrixOps_F32;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F32;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestCholeskyOuterForm_FDRB {

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
            FMatrixRMaj A = RandomMatrices_FDRM.symmetricPosDef(N,rand);

            CholeskyDecomposition_F32<FMatrixRMaj> chol = DecompositionFactory_FDRM.chol(1,false);
            assertTrue(DecompositionFactory_FDRM.decomposeSafe(chol,A));

            FMatrixRMaj expectedT = chol.getT(null);

            FMatrixRBlock blockA = MatrixOps_FDRB.convert(A,bl);

            CholeskyOuterForm_FDRB blockChol = new CholeskyOuterForm_FDRB(false);

            assertTrue(DecompositionFactory_FDRM.decomposeSafe(blockChol,blockA));

            assertTrue(GenericMatrixOps_F32.isEquivalent(expectedT,blockChol.getT(null), UtilEjml.TEST_F32));

            float blockDet = blockChol.computeDeterminant().real;
            float expectedDet = chol.computeDeterminant().real;

            assertEquals(expectedDet,blockDet,UtilEjml.TEST_F32);
        }
    }

    /**
     * Test upper cholesky decomposition for upper triangular.
     */
    @Test
    public void testLower() {
        // test against various different sizes
        for( int N = bl-2; N <= 13; N += 2 ) {

            FMatrixRMaj A = RandomMatrices_FDRM.symmetricPosDef(N,rand);

            CholeskyDecomposition_F32<FMatrixRMaj> chol = DecompositionFactory_FDRM.chol(1,true);
            assertTrue(DecompositionFactory_FDRM.decomposeSafe(chol, A));

            FMatrixRMaj expectedT = chol.getT(null);

            FMatrixRBlock blockA = MatrixOps_FDRB.convert(A,bl);

            CholeskyOuterForm_FDRB blockChol = new CholeskyOuterForm_FDRB(true);

            assertTrue(DecompositionFactory_FDRM.decomposeSafe(blockChol,blockA));

            assertTrue(GenericMatrixOps_F32.isEquivalent(expectedT,blockChol.getT(null),UtilEjml.TEST_F32));

            float blockDet = blockChol.computeDeterminant().real;
            float expectedDet = chol.computeDeterminant().real;

            assertEquals(expectedDet,blockDet,UtilEjml.TEST_F32);
        }
    }
}
