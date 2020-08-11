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

package org.ejml.dense.row.factory;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F32;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F32;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestDecompositionFactory_FDRM {

    Random rand = new Random(234234);

    @Test
    public void quality_eig() {
        // I'm assuming it can process this matrix with no problems
        FMatrixRMaj A = RandomMatrices_FDRM.symmetric(5,-1,1,rand);

        EigenDecomposition_F32<FMatrixRMaj> eig = DecompositionFactory_FDRM.eig(A.numRows,true);

        assertTrue(eig.decompose(A));

        float origQuality = DecompositionFactory_FDRM.quality(A,eig);

        // Mess up the EVD so that it will be of poor quality
        eig.getEigenVector(2).set(2,0,5);

        float modQuality = DecompositionFactory_FDRM.quality(A,eig);

        assertTrue(origQuality < modQuality);
        assertTrue(origQuality < UtilEjml.TEST_F32);
    }

    @Test
    public void quality_svd() {
        // I'm assuming it can process this matrix with no problems
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(4,5,rand);

        SingularValueDecomposition_F32<FMatrixRMaj> svd = DecompositionFactory_FDRM.svd(A.numRows,A.numCols,true,true,false);

        assertTrue(svd.decompose(A));

        float origQuality = DecompositionFactory_FDRM.quality(A,svd);

        // Mess up the SVD so that it will be of poor quality
        svd.getSingularValues()[2] = 5;

        float modQuality = DecompositionFactory_FDRM.quality(A,svd);

        assertTrue(origQuality < modQuality);
        assertTrue(origQuality < UtilEjml.TEST_F32);
    }
}
