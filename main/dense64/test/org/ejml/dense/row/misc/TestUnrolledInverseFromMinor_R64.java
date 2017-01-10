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

package org.ejml.dense.row.misc;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.ejml.dense.row.decomposition.lu.LUDecompositionAlt_R64;
import org.ejml.dense.row.linsol.lu.LinearSolverLu_R64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestUnrolledInverseFromMinor_R64 {

    Random rand = new Random(234234);

    /**
     * Compare it against LU decomposition
     */
    @Test
    public void compareToLU() {

        for(int N = 2; N <= UnrolledInverseFromMinor_R64.MAX; N++ ) {
            DMatrixRow_F64 A = RandomMatrices_R64.createRandom(N,N,rand);

            DMatrixRow_F64 expected = new DMatrixRow_F64(N,N);
            DMatrixRow_F64 found = new DMatrixRow_F64(N,N);

            // first compute inverse by LU
            LUDecompositionAlt_R64 alg = new LUDecompositionAlt_R64();
            LinearSolverLu_R64 solver = new LinearSolverLu_R64(alg);

            assertTrue( solver.setA(A));
            solver.invert(expected);

            // compute the result from the algorithm being tested
            UnrolledInverseFromMinor_R64.inv(A,found);

            EjmlUnitTests.assertEquals(expected,found, UtilEjml.TEST_F64);
        }

    }
}
