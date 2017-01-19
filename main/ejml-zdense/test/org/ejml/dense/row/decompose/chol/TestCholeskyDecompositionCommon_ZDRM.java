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

package org.ejml.dense.row.decompose.chol;

import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestCholeskyDecompositionCommon_ZDRM {
    Random rand = new Random(234);

    int N = 6;

    /**
     * The correctness of getT(null) has been tested else where effectively.  This
     * checks to see if it handles the case where an input is provided correctly.
     */
    @Test
    public void getT() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.createHermPosDef(N, rand);

        CholeskyDecomposition_F64<ZMatrixRMaj> cholesky = new Dummy(true);

        ZMatrixRMaj L_null = cholesky.getT(null);
        ZMatrixRMaj L_provided = RandomMatrices_ZDRM.createRandom(N, N, rand);
        assertTrue( L_provided == cholesky.getT(L_provided));

        assertTrue(MatrixFeatures_ZDRM.isEquals(L_null, L_provided));
    }

    private class Dummy extends CholeskyDecompositionCommon_ZDRM {

        public Dummy(boolean lower) {
            super(lower);
            T = RandomMatrices_ZDRM.createRandom(N,N,rand);
            n = N;
        }

        @Override
        protected boolean decomposeLower() {
            return true;
        }

        @Override
        protected boolean decomposeUpper() {
            return true;
        }
    }
}