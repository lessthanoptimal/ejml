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

package org.ejml.alg.dense.misc;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_R64;
import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.RandomMatrices_R64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestUnrolledDeterminantFromMinor_R64 {

    Random rand = new Random(234234);

    @Test
    public void testAll() {
        for(int N = 2; N <= UnrolledDeterminantFromMinor_R64.MAX; N++ ) {
            RowMatrix_F64 A = RandomMatrices_R64.createRandom(N,N,rand);

            double unrolled = UnrolledDeterminantFromMinor_R64.det(A);
            LUDecompositionAlt_R64 alg = new LUDecompositionAlt_R64();
            assertTrue( alg.decompose(A) );
            double expected = alg.computeDeterminant().real;

            assertEquals(expected,unrolled, UtilEjml.TEST_F64);
        }
    }
}