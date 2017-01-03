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
import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.ejml.ops.CommonOps_CD64;
import org.ejml.ops.RandomMatrices_CD64;
import org.junit.Test;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class TestVectorVectorMult_CD64 {

    Random rand = new Random(234);

    @Test
    public void innerProd() {

        CDenseMatrix64F a = RandomMatrices_CD64.createRandom(1,6,rand);
        CDenseMatrix64F b = RandomMatrices_CD64.createRandom(6,1,rand);

        CDenseMatrix64F c = new CDenseMatrix64F(1,1);

        CommonOps_CD64.mult(a,b,c);

        Complex64F expected = new Complex64F();
        c.get(0,0,expected);
        Complex64F found = VectorVectorMult_CD64.innerProd(a,b,null);

        EjmlUnitTests.assertEquals(expected,found, UtilEjml.TEST_64F);
    }

    @Test
    public void innerProdH() {

        CDenseMatrix64F a = RandomMatrices_CD64.createRandom(1,6,rand);
        CDenseMatrix64F b = RandomMatrices_CD64.createRandom(6,1,rand);

        Complex64F found = VectorVectorMult_CD64.innerProdH(a, b, null);

        CDenseMatrix64F c = new CDenseMatrix64F(1,1);

        CommonOps_CD64.conjugate(b,b);
        CommonOps_CD64.mult(a,b,c);

        Complex64F expected = new Complex64F();
        c.get(0,0,expected);

        EjmlUnitTests.assertEquals(expected,found,UtilEjml.TEST_64F);
    }

    @Test
    public void outerProd() {
        CDenseMatrix64F a = RandomMatrices_CD64.createRandom(6,1,rand);
        CDenseMatrix64F b = RandomMatrices_CD64.createRandom(1,6,rand);

        CDenseMatrix64F expected = new CDenseMatrix64F(6,6);
        CDenseMatrix64F found = new CDenseMatrix64F(6,6);

        CommonOps_CD64.mult(a,b,expected);
        VectorVectorMult_CD64.outerProd(a,b,found);

        EjmlUnitTests.assertEquals(expected,found,UtilEjml.TEST_64F);
    }

    @Test
    public void outerProdH() {
        CDenseMatrix64F a = RandomMatrices_CD64.createRandom(6,1,rand);
        CDenseMatrix64F b = RandomMatrices_CD64.createRandom(1,6,rand);

        CDenseMatrix64F expected = new CDenseMatrix64F(6,6);
        CDenseMatrix64F found = new CDenseMatrix64F(6,6);

        VectorVectorMult_CD64.outerProdH(a, b, found);
        CommonOps_CD64.conjugate(b,b);
        CommonOps_CD64.mult(a, b, expected);

        EjmlUnitTests.assertEquals(expected,found,UtilEjml.TEST_64F);
    }
}