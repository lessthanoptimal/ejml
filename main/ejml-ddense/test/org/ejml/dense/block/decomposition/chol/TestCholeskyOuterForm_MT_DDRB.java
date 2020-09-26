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

package org.ejml.dense.block.decomposition.chol;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRBlock;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.generic.GenericMatrixOps_F64;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestCholeskyOuterForm_MT_DDRB {
	Random rand = new Random(1231);

	// size of a block
	int bl = 5;

	/**
	 * Test upper cholesky decomposition for upper triangular.
	 */
	@Test
	void compareToSingle() {
		compareToSingle(true);
		compareToSingle(false);
	}

	void compareToSingle(boolean lower) {
		// test against various different sizes
		for( int N = bl-2; N <= 41; N += 6 ) {
			DMatrixRBlock A = MatrixOps_DDRB.convert(RandomMatrices_DDRM.symmetricPosDef(N, rand),bl);
			DMatrixRBlock B = A.copy();

			var single = new CholeskyOuterForm_DDRB(lower);
			var concurrent = new CholeskyOuterForm_MT_DDRB(lower);

			assertTrue(DecompositionFactory_DDRM.decomposeSafe(single,A));
			assertTrue(DecompositionFactory_DDRM.decomposeSafe(concurrent,B));

			assertTrue(GenericMatrixOps_F64.isEquivalent(single.getT(null),concurrent.getT(null), UtilEjml.TEST_F64));

			double expectedDet = single.computeDeterminant().real;
			double foundDet = concurrent.computeDeterminant().real;

			assertEquals(expectedDet,foundDet,UtilEjml.TEST_F64);
		}
	}
}

