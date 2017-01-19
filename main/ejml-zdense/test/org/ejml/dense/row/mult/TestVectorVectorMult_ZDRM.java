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

package org.ejml.dense.row.mult;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.junit.Test;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class TestVectorVectorMult_ZDRM {

    Random rand = new Random(234);

    @Test
    public void innerProd() {

        ZMatrixRMaj a = RandomMatrices_ZDRM.createRandom(1,6,rand);
        ZMatrixRMaj b = RandomMatrices_ZDRM.createRandom(6,1,rand);

        ZMatrixRMaj c = new ZMatrixRMaj(1,1);

        CommonOps_ZDRM.mult(a,b,c);

        Complex_F64 expected = new Complex_F64();
        c.get(0,0,expected);
        Complex_F64 found = VectorVectorMult_ZDRM.innerProd(a,b,null);

        EjmlUnitTests.assertEquals(expected,found, UtilEjml.TEST_F64);
    }

    @Test
    public void innerProdH() {

        ZMatrixRMaj a = RandomMatrices_ZDRM.createRandom(1,6,rand);
        ZMatrixRMaj b = RandomMatrices_ZDRM.createRandom(6,1,rand);

        Complex_F64 found = VectorVectorMult_ZDRM.innerProdH(a, b, null);

        ZMatrixRMaj c = new ZMatrixRMaj(1,1);

        CommonOps_ZDRM.conjugate(b,b);
        CommonOps_ZDRM.mult(a,b,c);

        Complex_F64 expected = new Complex_F64();
        c.get(0,0,expected);

        EjmlUnitTests.assertEquals(expected,found,UtilEjml.TEST_F64);
    }

    @Test
    public void outerProd() {
        ZMatrixRMaj a = RandomMatrices_ZDRM.createRandom(6,1,rand);
        ZMatrixRMaj b = RandomMatrices_ZDRM.createRandom(1,6,rand);

        ZMatrixRMaj expected = new ZMatrixRMaj(6,6);
        ZMatrixRMaj found = new ZMatrixRMaj(6,6);

        CommonOps_ZDRM.mult(a,b,expected);
        VectorVectorMult_ZDRM.outerProd(a,b,found);

        EjmlUnitTests.assertEquals(expected,found,UtilEjml.TEST_F64);
    }

    @Test
    public void outerProdH() {
        ZMatrixRMaj a = RandomMatrices_ZDRM.createRandom(6,1,rand);
        ZMatrixRMaj b = RandomMatrices_ZDRM.createRandom(1,6,rand);

        ZMatrixRMaj expected = new ZMatrixRMaj(6,6);
        ZMatrixRMaj found = new ZMatrixRMaj(6,6);

        VectorVectorMult_ZDRM.outerProdH(a, b, found);
        CommonOps_ZDRM.conjugate(b,b);
        CommonOps_ZDRM.mult(a, b, expected);

        EjmlUnitTests.assertEquals(expected,found,UtilEjml.TEST_F64);
    }
}