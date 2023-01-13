/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestUnrolledCholesky_DDRM extends EjmlStandardJUnit {
    @Test void lower_CompareToClass() {
        CholeskyDecomposition_F64<DMatrixRMaj> chol = DecompositionFactory_DDRM.chol(true);

        for (int size = 1; size <= UnrolledCholesky_DDRM.MAX; size++) {
            DMatrixRMaj A = RandomMatrices_DDRM.symmetricPosDef(size,rand);

            chol.decompose(A.copy());

            DMatrixRMaj found = new DMatrixRMaj(size,size);
            DMatrixRMaj expected = chol.getT(null);

            assertTrue(UnrolledCholesky_DDRM.lower(A,found));

            assertTrue(MatrixFeatures_DDRM.isIdentical(found,expected, UtilEjml.TEST_F64));
        }
    }

    /**
     * Give it matrices which are not SPD and see if it fails
     */
    @Test void lower_CompareToClass_NonSPD() {
        for (int size = 2; size <= UnrolledCholesky_DDRM.MAX; size++) {
            DMatrixRMaj A = RandomMatrices_DDRM.rectangle(size,size,rand);

            int row = rand.nextInt(size);
            A.set(row,rand.nextInt(row+1), rand.nextDouble()*-1);

            DMatrixRMaj found = new DMatrixRMaj(size,size);

            assertFalse(UnrolledCholesky_DDRM.lower(A,found));
        }
    }

    @Test void upper_CompareToClass() {
        CholeskyDecomposition_F64<DMatrixRMaj> chol = DecompositionFactory_DDRM.chol(false);

        for (int size = 1; size <= UnrolledCholesky_DDRM.MAX; size++) {
            DMatrixRMaj A = RandomMatrices_DDRM.symmetricPosDef(size,rand);

            chol.decompose(A.copy());

            DMatrixRMaj found = new DMatrixRMaj(size,size);
            DMatrixRMaj expected = chol.getT(null);

            assertTrue(UnrolledCholesky_DDRM.upper(A,found));

            assertTrue(MatrixFeatures_DDRM.isIdentical(found,expected, UtilEjml.TEST_F64));
        }
    }

    /**
     * Give it matrices which are not SPD and see if it fails
     */
    @Test void upper_CompareToClass_NonSPD() {
        for (int size = 2; size <= UnrolledCholesky_DDRM.MAX; size++) {
            DMatrixRMaj A = RandomMatrices_DDRM.rectangle(size,size,rand);

            int col = rand.nextInt(size);
            A.set(rand.nextInt(col+1),col, rand.nextDouble()*-1);

            DMatrixRMaj found = new DMatrixRMaj(size,size);

            assertFalse(UnrolledCholesky_DDRM.upper(A,found));
        }
    }

}