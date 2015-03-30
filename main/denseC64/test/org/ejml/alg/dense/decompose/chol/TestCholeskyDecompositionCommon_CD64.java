/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decompose.chol;

import org.ejml.data.CDenseMatrix64F;
import org.ejml.interfaces.decomposition.CholeskyDecomposition;
import org.ejml.ops.CMatrixFeatures;
import org.ejml.ops.CRandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestCholeskyDecompositionCommon_CD64 {
    Random rand = new Random(234);

    int N = 6;

    /**
     * The correctness of getT(null) has been tested else where effectively.  This
     * checks to see if it handles the case where an input is provided correctly.
     */
    @Test
    public void getT() {
        CDenseMatrix64F A = CRandomMatrices.createHermPosDef(N, rand);

        CholeskyDecomposition<CDenseMatrix64F> cholesky = new Dummy(true);

        CDenseMatrix64F L_null = cholesky.getT(null);
        CDenseMatrix64F L_provided = CRandomMatrices.createRandom(N, N, rand);
        assertTrue( L_provided == cholesky.getT(L_provided));

        assertTrue(CMatrixFeatures.isEquals(L_null, L_provided));
    }

    private class Dummy extends CholeskyDecompositionCommon_CD64 {

        public Dummy(boolean lower) {
            super(lower);
            T = CRandomMatrices.createRandom(N,N,rand);
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