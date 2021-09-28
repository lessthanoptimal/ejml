/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decomposition.eig;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.eig.watched.WatchedDoubleStepQREigen_DDRM;
import org.ejml.dense.row.decomposition.hessenberg.HessenbergSimilarDecomposition_DDRM;
import org.ejml.dense.row.decomposition.hessenberg.HessenbergSimilarDecomposition_MT_DDRM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Technically a class by this name doesn't exist. This is just the same class with concurrent algorithms being used
 * inside
 *
 * @author Peter Abeles
 */
public class TestWatchedDoubleStepQRDecomposition_MT_DDRM extends EjmlStandardJUnit {
    int size = 100;

    @Test void compareToSingle() {
        compareToSingle(false);
        compareToSingle(true);
    }
    void compareToSingle(boolean vectors) {

        DMatrixRMaj A = RandomMatrices_DDRM.symmetric(size,-1,1,rand);
        DMatrixRMaj B = A.copy();

        var single = new WatchedDoubleStepQRDecomposition_DDRM(new HessenbergSimilarDecomposition_DDRM(),
                new WatchedDoubleStepQREigen_DDRM(),vectors);
        var concurrent = new WatchedDoubleStepQRDecomposition_DDRM(new HessenbergSimilarDecomposition_MT_DDRM(),
                new WatchedDoubleStepQREigen_DDRM(),vectors);

        assertTrue(single.decompose(A));
        assertTrue(concurrent.decompose(B));

        assertTrue(MatrixFeatures_DDRM.isEquals(A,B, UtilEjml.TEST_F64));

        assertEquals(single.getNumberOfEigenvalues(), concurrent.getNumberOfEigenvalues());
        int numEigen = single.getNumberOfEigenvalues();
        for (int i = 0; i < numEigen; i++) {
            assertEquals(single.getEigenvalue(i).real, concurrent.getEigenvalue(i).real, UtilEjml.TEST_F64);
            assertEquals(single.getEigenvalue(i).imaginary, concurrent.getEigenvalue(i).imaginary, UtilEjml.TEST_F64);

            if (!vectors)
                continue;

            DMatrixRMaj singleVec = single.getEigenVector(i);
            DMatrixRMaj concurVec = concurrent.getEigenVector(i);
            assertTrue(MatrixFeatures_DDRM.isIdentical(singleVec,concurVec,UtilEjml.TEST_F64));
        }
    }
}
