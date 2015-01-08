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

package org.ejml.alg.dense.mult;

import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.ejml.ops.CCommonOps;
import org.ejml.ops.CRandomMatrices;
import org.ejml.ops.EjmlUnitTests;
import org.junit.Test;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class TestCVectorVectorMult {

    Random rand = new Random(234);

    @Test
    public void innerProd() {

        CDenseMatrix64F a = CRandomMatrices.createRandom(1,6,rand);
        CDenseMatrix64F b = CRandomMatrices.createRandom(6,1,rand);

        CDenseMatrix64F c = new CDenseMatrix64F(1,1);

        CCommonOps.mult(a,b,c);

        Complex64F expected = new Complex64F();
        c.get(0,0,expected);
        Complex64F found = CVectorVectorMult.innerProd(a,b,null);

        EjmlUnitTests.assertEquals(expected,found,1e-8);
    }

    @Test
    public void innerProdH() {

        CDenseMatrix64F a = CRandomMatrices.createRandom(1,6,rand);
        CDenseMatrix64F b = CRandomMatrices.createRandom(6,1,rand);

        Complex64F found = CVectorVectorMult.innerProdH(a, b, null);

        CDenseMatrix64F c = new CDenseMatrix64F(1,1);

        CCommonOps.conjugate(b,b);
        CCommonOps.mult(a,b,c);

        Complex64F expected = new Complex64F();
        c.get(0,0,expected);

        EjmlUnitTests.assertEquals(expected,found,1e-8);
    }

    @Test
    public void outerProd() {
        CDenseMatrix64F a = CRandomMatrices.createRandom(6,1,rand);
        CDenseMatrix64F b = CRandomMatrices.createRandom(1,6,rand);

        CDenseMatrix64F expected = new CDenseMatrix64F(6,6);
        CDenseMatrix64F found = new CDenseMatrix64F(6,6);

        CCommonOps.mult(a,b,expected);
        CVectorVectorMult.outerProd(a,b,found);

        EjmlUnitTests.assertEquals(expected,found,1e-8);
    }

    @Test
    public void outerProdH() {
        CDenseMatrix64F a = CRandomMatrices.createRandom(6,1,rand);
        CDenseMatrix64F b = CRandomMatrices.createRandom(1,6,rand);

        CDenseMatrix64F expected = new CDenseMatrix64F(6,6);
        CDenseMatrix64F found = new CDenseMatrix64F(6,6);

        CVectorVectorMult.outerProdH(a, b, found);
        CCommonOps.conjugate(b,b);
        CCommonOps.mult(a, b, expected);

        EjmlUnitTests.assertEquals(expected,found,1e-8);
    }
}