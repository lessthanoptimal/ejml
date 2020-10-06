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

package org.ejml.dense.block;

import org.ejml.CheckMultiThreadAgainstSingleThread;
import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DSubmatrixD1;
import org.ejml.data.Submatrix;

import java.util.Random;

class TestMatrixMult_MT_DDRB extends CheckMultiThreadAgainstSingleThread {
	int blockLength = 21;

	public TestMatrixMult_MT_DDRB() {
		super(MatrixMult_DDRB.class, MatrixMult_MT_DDRB.class, 7);
		size = 121;
	}

	@Override
	protected Submatrix createSubmatrix(long seed) {
		Random rand = new Random(seed);
		DMatrixRBlock mat = MatrixOps_DDRB.createRandom(size,size,-1,1, rand, blockLength);
		return new DSubmatrixD1(mat,0,size,0,size);
	}

	@Override
	protected void compareSubmatrices(Submatrix subA, Submatrix subB) {
		DMatrixRBlock A = (DMatrixRBlock)subA.original;
		DMatrixRBlock B = (DMatrixRBlock)subB.original;
		EjmlUnitTests.assertEquals(A,B, UtilEjml.TEST_F64);
	}

	/**
	 * Correctly configure block length for all functions
	 */
	@Override
	protected void declareParamStandard(Class[] typesThreaded, Object[] inputsThreaded, Object[] inputsSingle) {
		super.declareParamStandard(typesThreaded, inputsThreaded, inputsSingle);
		inputsThreaded[0] = blockLength;
		inputsSingle[0] = blockLength;
	}
}

