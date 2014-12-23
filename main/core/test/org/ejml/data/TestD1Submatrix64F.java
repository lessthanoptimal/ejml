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

package org.ejml.data;

import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestD1Submatrix64F {

    Random rand = new Random(234234);

    @Test
    public void get() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,10,-1,1,rand);

        D1Submatrix64F S = new D1Submatrix64F(A,2,4,1,10);

        assertEquals(A.get(3,2),S.get(1,1),1e-8);
    }

    @Test
    public void set() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,10,-1,1,rand);

        D1Submatrix64F S = new D1Submatrix64F(A,2,4,1,10);

        S.set(1,1,5);

        assertEquals(A.get(3,2),5,1e-8);
    }

    @Test
    public void extract() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,10,-1,1,rand);

        D1Submatrix64F S = new D1Submatrix64F(A,2,4,1,10);

        DenseMatrix64F M = S.extract();

        DenseMatrix64F E = CommonOps.extract(A,2,4,1,10);

        assertTrue(MatrixFeatures.isEquals(E,M));
    }
}
