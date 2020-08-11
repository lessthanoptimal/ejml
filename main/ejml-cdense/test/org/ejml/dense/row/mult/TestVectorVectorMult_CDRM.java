/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.Complex_F32;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class TestVectorVectorMult_CDRM {

    Random rand = new Random(234);

    @Test
    public void innerProd() {

        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(1,6,rand);
        CMatrixRMaj b = RandomMatrices_CDRM.rectangle(6,1,rand);

        CMatrixRMaj c = new CMatrixRMaj(1,1);

        CommonOps_CDRM.mult(a,b,c);

        Complex_F32 expected = new Complex_F32();
        c.get(0,0,expected);
        Complex_F32 found = VectorVectorMult_CDRM.innerProd(a,b,null);

        EjmlUnitTests.assertEquals(expected,found, UtilEjml.TEST_F32);
    }

    @Test
    public void innerProdH() {

        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(1,6,rand);
        CMatrixRMaj b = RandomMatrices_CDRM.rectangle(6,1,rand);

        Complex_F32 found = VectorVectorMult_CDRM.innerProdH(a, b, null);

        CMatrixRMaj c = new CMatrixRMaj(1,1);

        CommonOps_CDRM.conjugate(b,b);
        CommonOps_CDRM.mult(a,b,c);

        Complex_F32 expected = new Complex_F32();
        c.get(0,0,expected);

        EjmlUnitTests.assertEquals(expected,found,UtilEjml.TEST_F32);
    }

    @Test
    public void outerProd() {
        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(6,1,rand);
        CMatrixRMaj b = RandomMatrices_CDRM.rectangle(1,6,rand);

        CMatrixRMaj expected = new CMatrixRMaj(6,6);
        CMatrixRMaj found = new CMatrixRMaj(6,6);

        CommonOps_CDRM.mult(a,b,expected);
        VectorVectorMult_CDRM.outerProd(a,b,found);

        EjmlUnitTests.assertEquals(expected,found,UtilEjml.TEST_F32);
    }

    @Test
    public void outerProdH() {
        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(6,1,rand);
        CMatrixRMaj b = RandomMatrices_CDRM.rectangle(1,6,rand);

        CMatrixRMaj expected = new CMatrixRMaj(6,6);
        CMatrixRMaj found = new CMatrixRMaj(6,6);

        VectorVectorMult_CDRM.outerProdH(a, b, found);
        CommonOps_CDRM.conjugate(b,b);
        CommonOps_CDRM.mult(a, b, expected);

        EjmlUnitTests.assertEquals(expected,found,UtilEjml.TEST_F32);
    }
}