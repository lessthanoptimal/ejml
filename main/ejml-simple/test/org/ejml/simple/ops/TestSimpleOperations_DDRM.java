/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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

package org.ejml.simple.ops;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.simple.SimpleOperations;
import org.junit.jupiter.api.Test;

class TestSimpleOperations_DDRM extends BaseSimpleOperationsChecks<DMatrixRMaj> {
    @Override public SimpleOperations<DMatrixRMaj> createOps() {
        return new SimpleOperations_DDRM();
    }

    @Override public DMatrixRMaj randomRect( int numRows, int numCols ) {
        return RandomMatrices_DDRM.rectangle(numRows, numCols, rand);
    }
    
    @Test void multTransA() {
    	SimpleOperations<DMatrixRMaj> ops = createOps();
    	for(int nRows : new int[] {1_000, 10_000, 100_000}) {
    		DMatrixRMaj randMat = randomRect(nRows, 1);
    		DMatrixRMaj ones = new DMatrixRMaj(nRows,1);
    		ops.fill(ones, (double) 1.0);

    		DMatrixRMaj expected = new DMatrixRMaj(1, 1);
    		expected.set(0, (double) ops.elementSum(randMat));

    		DMatrixRMaj result = new DMatrixRMaj(1, 1);
    		ops.multTransA(randMat, ones, result);

    		assertTrue(MatrixFeatures_DDRM.isIdentical(expected, result, (double) 1e-20));
    	}
    }
}