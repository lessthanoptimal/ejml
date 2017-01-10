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
import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestNaiveDeterminant {


    @Test
    public void detRecursive() {
        double[] d = new double[]{5 ,-2 ,-4 ,0.5, 0.1, 91, 8, 66, 1, -2, 10, -4, -0.2, 7, -4, 0.8};

        DMatrixRow_F64 mat = new DMatrixRow_F64(4,4, true, d);

        double val = NaiveDeterminant.recursive(mat);

        assertEquals(-27288.86,val,1e-6);
    }

    /**
     * Compares this formuation to the naive recursive formulation
     */
    @Test
    public void det() {
        Random rand = new Random(0xff);

        for( int i = 1; i <= 5; i++ ) {
            DMatrixRow_F64 A = RandomMatrices_R64.createRandom(i,i,rand);

            double expected = NaiveDeterminant.recursive(A);
            double found = NaiveDeterminant.leibniz(A);

            assertEquals(expected,found, UtilEjml.TEST_F64);
        }
    }
}
