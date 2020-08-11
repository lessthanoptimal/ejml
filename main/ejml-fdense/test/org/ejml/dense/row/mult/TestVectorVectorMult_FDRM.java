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
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestVectorVectorMult_FDRM {

    Random rand = new Random(45837);

    @Test
    public void innerProduct() {
        FMatrixRMaj A = new FMatrixRMaj(4,1, true, 1, 2, 3, 4);
        FMatrixRMaj B = new FMatrixRMaj(4,1, true, -1, -2, -3, -4);

        float val = VectorVectorMult_FDRM.innerProd(A,B);

        assertEquals(-30,val, UtilEjml.TEST_F32);
    }

    @Test
    public void innerProdA() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(3,4,rand);
        FMatrixRMaj x = RandomMatrices_FDRM.rectangle(3,1,rand);
        FMatrixRMaj y = RandomMatrices_FDRM.rectangle(4,1,rand);

        FMatrixRMaj temp = new FMatrixRMaj(1,4);

        // compute the expected result first
        CommonOps_FDRM.multTransA(x,A,temp);
        float expected = VectorVectorMult_FDRM.innerProd(temp,y);

        float found = VectorVectorMult_FDRM.innerProdA(x,A,y);

        assertEquals(expected,found,UtilEjml.TEST_F32);
    }

    @Test
    public void innerProdTranA() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(3,3,rand);
        FMatrixRMaj x = RandomMatrices_FDRM.rectangle(3,1,rand);
        FMatrixRMaj y = RandomMatrices_FDRM.rectangle(3,1,rand);

        FMatrixRMaj Atran = new FMatrixRMaj(3,3);
        CommonOps_FDRM.transpose(A,Atran);

        FMatrixRMaj temp = new FMatrixRMaj(1,3);

        // compute the expected result first
        CommonOps_FDRM.multTransA(x,Atran,temp);
        float expected = VectorVectorMult_FDRM.innerProd(temp,y);

        float found = VectorVectorMult_FDRM.innerProdTranA(x,A,y);

        assertEquals(expected,found,UtilEjml.TEST_F32);
    }

    @Test
    public void outerProd() {
        FMatrixRMaj A = new FMatrixRMaj(4,1, true, 1, 2, 3, 4);
        FMatrixRMaj B = new FMatrixRMaj(4,1, true, -1, -2, -3, -4);

        FMatrixRMaj C = RandomMatrices_FDRM.rectangle(4,4,rand);
        VectorVectorMult_FDRM.outerProd(A,B,C);

        // compare it against the equivalent matrix matrix multiply
        FMatrixRMaj D =  RandomMatrices_FDRM.rectangle(4,4,rand);
        MatrixMatrixMult_FDRM.multTransB(A,B,D);

        EjmlUnitTests.assertEquals(D,C,0);
    }

    @Test
    public void addOuterProd() {
        FMatrixRMaj A = new FMatrixRMaj(4,1, true, 1, 2, 3, 4);
        FMatrixRMaj B = new FMatrixRMaj(4,1, true, -1, -2, -3, -4);

        FMatrixRMaj C = RandomMatrices_FDRM.rectangle(4,4,rand);
        FMatrixRMaj D =  C.copy();

        VectorVectorMult_FDRM.addOuterProd(1.0f,A,B,C);

        // compare it against the equivalent matrix matrix multiply
        FMatrixRMaj E = RandomMatrices_FDRM.rectangle(4,4,rand);
        MatrixMatrixMult_FDRM.multTransB(A,B,E);
        CommonOps_FDRM.add(D,E,D);

        assertTrue(MatrixFeatures_FDRM.isEquals(D,C));

        // now try it with another gamma
        C = RandomMatrices_FDRM.rectangle(4,4,rand);
        D = C.copy();

        VectorVectorMult_FDRM.addOuterProd(2.5f,A,B,C);

        MatrixMatrixMult_FDRM.multTransB(2.5f,A,B,E);
        CommonOps_FDRM.add(D,E,D);

        EjmlUnitTests.assertEquals(D,C,0);
    }

    @Test
    public void householder() {
        FMatrixRMaj u = RandomMatrices_FDRM.rectangle(4,1,rand);
        FMatrixRMaj x = RandomMatrices_FDRM.rectangle(4,1,rand);
        FMatrixRMaj y = RandomMatrices_FDRM.rectangle(4,1,rand);


        float gamma = 4.5f;

        VectorVectorMult_FDRM.householder(gamma,u,x,y);

        FMatrixRMaj L = CommonOps_FDRM.identity(4,4);
        FMatrixRMaj y_exp = RandomMatrices_FDRM.rectangle(4,1,rand);

        VectorVectorMult_FDRM.addOuterProd(gamma,u,u,L);
        CommonOps_FDRM.mult(L,x,y_exp);

        EjmlUnitTests.assertEquals(y,y_exp,UtilEjml.TEST_F32);
    }

    @Test
    public void rank1Update_two_square() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(6,6,rand);
        FMatrixRMaj u = RandomMatrices_FDRM.rectangle(6,1,rand);
        FMatrixRMaj w = RandomMatrices_FDRM.rectangle(6,1,rand);
        float gamma = -45;

        SimpleMatrix _A = SimpleMatrix.wrap(A);
        SimpleMatrix _u = SimpleMatrix.wrap(u);
        SimpleMatrix _w = SimpleMatrix.wrap(w);
        
        SimpleMatrix expected = _A.plus(_u.mult(_w.transpose()).scale(gamma));
        FMatrixRMaj found = new FMatrixRMaj(6,6);

        VectorVectorMult_FDRM.rank1Update(gamma,A,u,w,found);

        EjmlUnitTests.assertEquals(expected.getFDRM(),found,UtilEjml.TEST_F32);
    }

    @Test
    public void rank1Update_one_square() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(6,6,rand);
        FMatrixRMaj u = RandomMatrices_FDRM.rectangle(6,1,rand);
        FMatrixRMaj w = RandomMatrices_FDRM.rectangle(6,1,rand);
        float gamma = -45;

        SimpleMatrix _A = SimpleMatrix.wrap(A);
        SimpleMatrix _u = SimpleMatrix.wrap(u);
        SimpleMatrix _w = SimpleMatrix.wrap(w);

        SimpleMatrix expected = _A.plus(_u.mult(_w.transpose()).scale(gamma));
        FMatrixRMaj found = A.copy();

        VectorVectorMult_FDRM.rank1Update(gamma,found,u,w);

        EjmlUnitTests.assertEquals(expected.getFDRM(),found,UtilEjml.TEST_F32);
    }
}
