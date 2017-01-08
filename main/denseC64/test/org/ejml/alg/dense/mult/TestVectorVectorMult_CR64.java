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

package org.ejml.alg.dense.mult;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.RowMatrix_C64;
import org.ejml.ops.CommonOps_CR64;
import org.ejml.ops.RandomMatrices_CR64;
import org.junit.Test;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class TestVectorVectorMult_CR64 {

    Random rand = new Random(234);

    @Test
    public void innerProd() {

        RowMatrix_C64 a = RandomMatrices_CR64.createRandom(1,6,rand);
        RowMatrix_C64 b = RandomMatrices_CR64.createRandom(6,1,rand);

        RowMatrix_C64 c = new RowMatrix_C64(1,1);

        CommonOps_CR64.mult(a,b,c);

        Complex_F64 expected = new Complex_F64();
        c.get(0,0,expected);
        Complex_F64 found = VectorVectorMult_CR64.innerProd(a,b,null);

        EjmlUnitTests.assertEquals(expected,found, UtilEjml.TEST_F64);
    }

    @Test
    public void innerProdH() {

        RowMatrix_C64 a = RandomMatrices_CR64.createRandom(1,6,rand);
        RowMatrix_C64 b = RandomMatrices_CR64.createRandom(6,1,rand);

        Complex_F64 found = VectorVectorMult_CR64.innerProdH(a, b, null);

        RowMatrix_C64 c = new RowMatrix_C64(1,1);

        CommonOps_CR64.conjugate(b,b);
        CommonOps_CR64.mult(a,b,c);

        Complex_F64 expected = new Complex_F64();
        c.get(0,0,expected);

        EjmlUnitTests.assertEquals(expected,found,UtilEjml.TEST_F64);
    }

    @Test
    public void outerProd() {
        RowMatrix_C64 a = RandomMatrices_CR64.createRandom(6,1,rand);
        RowMatrix_C64 b = RandomMatrices_CR64.createRandom(1,6,rand);

        RowMatrix_C64 expected = new RowMatrix_C64(6,6);
        RowMatrix_C64 found = new RowMatrix_C64(6,6);

        CommonOps_CR64.mult(a,b,expected);
        VectorVectorMult_CR64.outerProd(a,b,found);

        EjmlUnitTests.assertEquals(expected,found,UtilEjml.TEST_F64);
    }

    @Test
    public void outerProdH() {
        RowMatrix_C64 a = RandomMatrices_CR64.createRandom(6,1,rand);
        RowMatrix_C64 b = RandomMatrices_CR64.createRandom(1,6,rand);

        RowMatrix_C64 expected = new RowMatrix_C64(6,6);
        RowMatrix_C64 found = new RowMatrix_C64(6,6);

        VectorVectorMult_CR64.outerProdH(a, b, found);
        CommonOps_CR64.conjugate(b,b);
        CommonOps_CR64.mult(a, b, expected);

        EjmlUnitTests.assertEquals(expected,found,UtilEjml.TEST_F64);
    }
}