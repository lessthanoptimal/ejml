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

package org.ejml.dense.row.misc;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F32;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestUnrolledCholesky_FDRM {
    Random rand = new Random(2345);

    @Test
    public void lower_CompareToClass() {
        CholeskyDecomposition_F32<FMatrixRMaj> chol = DecompositionFactory_FDRM.chol(true);

        for (int size = 1; size <= UnrolledCholesky_FDRM.MAX; size++) {
            FMatrixRMaj A = RandomMatrices_FDRM.symmetricPosDef(size,rand);

            chol.decompose(A.copy());

            FMatrixRMaj found = new FMatrixRMaj(size,size);
            FMatrixRMaj expected = chol.getT(null);

            assertTrue(UnrolledCholesky_FDRM.lower(A,found));

            assertTrue(MatrixFeatures_FDRM.isIdentical(found,expected, UtilEjml.TEST_F32));
        }
    }

    /**
     * Give it matrices which are not SPD and see if it fails
     */
    @Test
    public void lower_CompareToClass_NonSPD() {
        for (int size = 2; size <= UnrolledCholesky_FDRM.MAX; size++) {
            FMatrixRMaj A = RandomMatrices_FDRM.rectangle(size,size,rand);

            int row = rand.nextInt(size);
            A.set(row,rand.nextInt(row+1), rand.nextFloat()*-1);

            FMatrixRMaj found = new FMatrixRMaj(size,size);

            assertFalse(UnrolledCholesky_FDRM.lower(A,found));
        }
    }

    @Test
    public void upper_CompareToClass() {
        CholeskyDecomposition_F32<FMatrixRMaj> chol = DecompositionFactory_FDRM.chol(false);

        for (int size = 1; size <= UnrolledCholesky_FDRM.MAX; size++) {
            FMatrixRMaj A = RandomMatrices_FDRM.symmetricPosDef(size,rand);

            chol.decompose(A.copy());

            FMatrixRMaj found = new FMatrixRMaj(size,size);
            FMatrixRMaj expected = chol.getT(null);

            assertTrue(UnrolledCholesky_FDRM.upper(A,found));

            assertTrue(MatrixFeatures_FDRM.isIdentical(found,expected, UtilEjml.TEST_F32));
        }
    }

    /**
     * Give it matrices which are not SPD and see if it fails
     */
    @Test
    public void upper_CompareToClass_NonSPD() {
        for (int size = 2; size <= UnrolledCholesky_FDRM.MAX; size++) {
            FMatrixRMaj A = RandomMatrices_FDRM.rectangle(size,size,rand);

            int col = rand.nextInt(size);
            A.set(rand.nextInt(col+1),col, rand.nextFloat()*-1);

            FMatrixRMaj found = new FMatrixRMaj(size,size);

            assertFalse(UnrolledCholesky_FDRM.upper(A,found));
        }
    }

}