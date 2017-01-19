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
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestVectorVectorMult_DDRM {

    Random rand = new Random(45837);

    @Test
    public void innerProduct() {
        DMatrixRMaj A = new DMatrixRMaj(4,1, true, 1, 2, 3, 4);
        DMatrixRMaj B = new DMatrixRMaj(4,1, true, -1, -2, -3, -4);

        double val = VectorVectorMult_DDRM.innerProd(A,B);

        assertEquals(-30,val, UtilEjml.TEST_F64);
    }

    @Test
    public void innerProdA() {
        DMatrixRMaj A = RandomMatrices_DDRM.createRandom(3,4,rand);
        DMatrixRMaj x = RandomMatrices_DDRM.createRandom(3,1,rand);
        DMatrixRMaj y = RandomMatrices_DDRM.createRandom(4,1,rand);

        DMatrixRMaj temp = new DMatrixRMaj(1,4);

        // compute the expected result first
        CommonOps_DDRM.multTransA(x,A,temp);
        double expected = VectorVectorMult_DDRM.innerProd(temp,y);

        double found = VectorVectorMult_DDRM.innerProdA(x,A,y);

        assertEquals(expected,found,UtilEjml.TEST_F64);
    }

    @Test
    public void innerProdTranA() {
        DMatrixRMaj A = RandomMatrices_DDRM.createRandom(3,3,rand);
        DMatrixRMaj x = RandomMatrices_DDRM.createRandom(3,1,rand);
        DMatrixRMaj y = RandomMatrices_DDRM.createRandom(3,1,rand);

        DMatrixRMaj Atran = new DMatrixRMaj(3,3);
        CommonOps_DDRM.transpose(A,Atran);

        DMatrixRMaj temp = new DMatrixRMaj(1,3);

        // compute the expected result first
        CommonOps_DDRM.multTransA(x,Atran,temp);
        double expected = VectorVectorMult_DDRM.innerProd(temp,y);

        double found = VectorVectorMult_DDRM.innerProdTranA(x,A,y);

        assertEquals(expected,found,UtilEjml.TEST_F64);
    }

    @Test
    public void outerProd() {
        DMatrixRMaj A = new DMatrixRMaj(4,1, true, 1, 2, 3, 4);
        DMatrixRMaj B = new DMatrixRMaj(4,1, true, -1, -2, -3, -4);

        DMatrixRMaj C = RandomMatrices_DDRM.createRandom(4,4,rand);
        VectorVectorMult_DDRM.outerProd(A,B,C);

        // compare it against the equivalent matrix matrix multiply
        DMatrixRMaj D =  RandomMatrices_DDRM.createRandom(4,4,rand);
        MatrixMatrixMult_DDRM.multTransB(A,B,D);

        EjmlUnitTests.assertEquals(D,C,0);
    }

    @Test
    public void addOuterProd() {
        DMatrixRMaj A = new DMatrixRMaj(4,1, true, 1, 2, 3, 4);
        DMatrixRMaj B = new DMatrixRMaj(4,1, true, -1, -2, -3, -4);

        DMatrixRMaj C = RandomMatrices_DDRM.createRandom(4,4,rand);
        DMatrixRMaj D =  C.copy();

        VectorVectorMult_DDRM.addOuterProd(1.0,A,B,C);

        // compare it against the equivalent matrix matrix multiply
        DMatrixRMaj E = RandomMatrices_DDRM.createRandom(4,4,rand);
        MatrixMatrixMult_DDRM.multTransB(A,B,E);
        CommonOps_DDRM.add(D,E,D);

        assertTrue(MatrixFeatures_DDRM.isEquals(D,C));

        // now try it with another gamma
        C = RandomMatrices_DDRM.createRandom(4,4,rand);
        D = C.copy();

        VectorVectorMult_DDRM.addOuterProd(2.5,A,B,C);

        MatrixMatrixMult_DDRM.multTransB(2.5,A,B,E);
        CommonOps_DDRM.add(D,E,D);

        EjmlUnitTests.assertEquals(D,C,0);
    }

    @Test
    public void householder() {
        DMatrixRMaj u = RandomMatrices_DDRM.createRandom(4,1,rand);
        DMatrixRMaj x = RandomMatrices_DDRM.createRandom(4,1,rand);
        DMatrixRMaj y = RandomMatrices_DDRM.createRandom(4,1,rand);


        double gamma = 4.5;

        VectorVectorMult_DDRM.householder(gamma,u,x,y);

        DMatrixRMaj L = CommonOps_DDRM.identity(4,4);
        DMatrixRMaj y_exp = RandomMatrices_DDRM.createRandom(4,1,rand);

        VectorVectorMult_DDRM.addOuterProd(gamma,u,u,L);
        CommonOps_DDRM.mult(L,x,y_exp);

        EjmlUnitTests.assertEquals(y,y_exp,UtilEjml.TEST_F64);
    }

    @Test
    public void rank1Update_two_square() {
        DMatrixRMaj A = RandomMatrices_DDRM.createRandom(6,6,rand);
        DMatrixRMaj u = RandomMatrices_DDRM.createRandom(6,1,rand);
        DMatrixRMaj w = RandomMatrices_DDRM.createRandom(6,1,rand);
        double gamma = -45;

        SimpleMatrix _A = SimpleMatrix.wrap(A);
        SimpleMatrix _u = SimpleMatrix.wrap(u);
        SimpleMatrix _w = SimpleMatrix.wrap(w);
        
        SimpleMatrix expected = _A.plus(_u.mult(_w.transpose()).scale(gamma));
        DMatrixRMaj found = new DMatrixRMaj(6,6);

        VectorVectorMult_DDRM.rank1Update(gamma,A,u,w,found);

        EjmlUnitTests.assertEquals(expected.matrix_F64(),found,UtilEjml.TEST_F64);
    }

    @Test
    public void rank1Update_one_square() {
        DMatrixRMaj A = RandomMatrices_DDRM.createRandom(6,6,rand);
        DMatrixRMaj u = RandomMatrices_DDRM.createRandom(6,1,rand);
        DMatrixRMaj w = RandomMatrices_DDRM.createRandom(6,1,rand);
        double gamma = -45;

        SimpleMatrix _A = SimpleMatrix.wrap(A);
        SimpleMatrix _u = SimpleMatrix.wrap(u);
        SimpleMatrix _w = SimpleMatrix.wrap(w);

        SimpleMatrix expected = _A.plus(_u.mult(_w.transpose()).scale(gamma));
        DMatrixRMaj found = A.copy();

        VectorVectorMult_DDRM.rank1Update(gamma,found,u,w);

        EjmlUnitTests.assertEquals(expected.matrix_F64(),found,UtilEjml.TEST_F64);
    }
}
