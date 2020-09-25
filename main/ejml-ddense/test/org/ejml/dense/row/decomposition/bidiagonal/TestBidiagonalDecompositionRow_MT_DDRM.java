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

package org.ejml.dense.row.decomposition.bidiagonal;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestBidiagonalDecompositionRow_MT_DDRM {

	Random rand = new Random(3245);
	int rows = 100;
	int cols = 80;

	@Test
	void compareToSingle() {
		DMatrixRMaj A = RandomMatrices_DDRM.rectangle(rows,cols,-1,1,rand);
		DMatrixRMaj B = A.copy();

		var algSingle = new BidiagonalDecompositionRow_DDRM();
		var algMT = new BidiagonalDecompositionRow_MT_DDRM();

		assertTrue(algSingle.decompose(A));
		assertTrue(algMT.decompose(B));

		assertTrue(MatrixFeatures_DDRM.isEquals(A,B, UtilEjml.TEST_F64));

		assertTrue(MatrixFeatures_DDRM.isEquals(algSingle.getB(null,true),
				algMT.getB(null,true), UtilEjml.TEST_F64));
		assertTrue(MatrixFeatures_DDRM.isEquals(algSingle.getB(null,false),
				algMT.getB(null,false), UtilEjml.TEST_F64));
		assertTrue(MatrixFeatures_DDRM.isEquals(algSingle.getU(null,false,true),
				algMT.getU(null,false,true), UtilEjml.TEST_F64));
		assertTrue(MatrixFeatures_DDRM.isEquals(algSingle.getU(null,false,true),
				algMT.getU(null,false,true), UtilEjml.TEST_F64));
		assertTrue(MatrixFeatures_DDRM.isEquals(algSingle.getV(null,false,true),
				algMT.getV(null,false,true), UtilEjml.TEST_F64));
		assertTrue(MatrixFeatures_DDRM.isEquals(algSingle.getV(null,false,true),
				algMT.getV(null,false,true), UtilEjml.TEST_F64));
	}
}

