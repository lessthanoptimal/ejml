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

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.lu.LUDecompositionAlt_DDRM;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestUnrolledDeterminantFromMinor_DDRM {

    Random rand = new Random(234234);

    @Test
    public void testAll() {
        for(int N = 2; N <= UnrolledDeterminantFromMinor_DDRM.MAX; N++ ) {
            DMatrixRMaj A = RandomMatrices_DDRM.rectangle(N,N,rand);

            double unrolled = UnrolledDeterminantFromMinor_DDRM.det(A);
            LUDecompositionAlt_DDRM alg = new LUDecompositionAlt_DDRM();
            assertTrue( alg.decompose(A) );
            double expected = alg.computeDeterminant().real;

            assertEquals(expected,unrolled, UtilEjml.TEST_F64);
        }
    }
}