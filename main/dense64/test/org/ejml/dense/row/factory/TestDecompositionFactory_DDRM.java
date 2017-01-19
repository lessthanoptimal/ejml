/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestDecompositionFactory_DDRM {

    Random rand = new Random(234234);

    @Test
    public void quality_eig() {
        // I'm assuming it can process this matrix with no problems
        DMatrixRMaj A = RandomMatrices_DDRM.createSymmetric(5,-1,1,rand);

        EigenDecomposition_F64<DMatrixRMaj> eig = DecompositionFactory_DDRM.eig(A.numRows,true);

        assertTrue(eig.decompose(A));

        double origQuality = DecompositionFactory_DDRM.quality(A,eig);

        // Mess up the EVD so that it will be of poor quality
        eig.getEigenVector(2).set(2,0,5);

        double modQuality = DecompositionFactory_DDRM.quality(A,eig);

        assertTrue(origQuality < modQuality);
        assertTrue(origQuality < UtilEjml.TEST_F64);
    }

    @Test
    public void quality_svd() {
        // I'm assuming it can process this matrix with no problems
        DMatrixRMaj A = RandomMatrices_DDRM.createRandom(4,5,rand);

        SingularValueDecomposition_F64<DMatrixRMaj> svd = DecompositionFactory_DDRM.svd(A.numRows,A.numCols,true,true,false);

        assertTrue(svd.decompose(A));

        double origQuality = DecompositionFactory_DDRM.quality(A,svd);

        // Mess up the SVD so that it will be of poor quality
        svd.getSingularValues()[2] = 5;

        double modQuality = DecompositionFactory_DDRM.quality(A,svd);

        assertTrue(origQuality < modQuality);
        assertTrue(origQuality < UtilEjml.TEST_F64);
    }
}
