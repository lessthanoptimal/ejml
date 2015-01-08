/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_D64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestUnrolledDeterminantFromMinor {

    Random rand = new Random(234234);

    @Test
    public void testAll() {
        for( int N = 2; N <= UnrolledDeterminantFromMinor.MAX; N++ ) {
            DenseMatrix64F A = RandomMatrices.createRandom(N,N,rand);

            double unrolled = UnrolledDeterminantFromMinor.det(A);
            LUDecompositionAlt_D64 alg = new LUDecompositionAlt_D64();
            assertTrue( alg.decompose(A) );
            double expected = alg.computeDeterminant().real;

            assertEquals(expected,unrolled,1e-8);
        }
    }
}