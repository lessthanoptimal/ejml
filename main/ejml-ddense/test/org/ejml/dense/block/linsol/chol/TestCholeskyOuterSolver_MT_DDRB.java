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

package org.ejml.dense.block.linsol.chol;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRBlock;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestCholeskyOuterSolver_MT_DDRB {
	protected Random rand = new Random(234234);
	protected int r = 3;

	@Test
	void compareToSingle() {
		var single = new CholeskyOuterSolver_DDRB();
		var concurrent = new CholeskyOuterSolver_MT_DDRB();

		for (int i = 1; i <= r*3; i++) {
			for (int j = 1; j <= r*3; j++) {
				DMatrixRBlock A = MatrixOps_DDRB.convert(RandomMatrices_DDRM.symmetricPosDef(i, rand),r);
				DMatrixRBlock B = A.copy();
				DMatrixRBlock Y = A.create(i, j);
				DMatrixRBlock X_expected = A.create(i, j);
				DMatrixRBlock X_found = A.create(i, j);

				assertTrue(single.setA(A));
				assertTrue(concurrent.setA(B));
				assertTrue(MatrixOps_DDRB.isEquals(A, B, UtilEjml.TEST_F64));

				single.solve(Y, X_expected);
				concurrent.solve(Y, X_found);

				assertTrue(MatrixOps_DDRB.isEquals(X_expected, X_found, UtilEjml.TEST_F64));
			}
		}
	}
}

