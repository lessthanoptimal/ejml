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
import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.MatrixFeatures_R64;
import org.ejml.ops.RandomMatrices_R64;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestVectorVectorMult_R64 {

    Random rand = new Random(45837);

    @Test
    public void innerProduct() {
        RowMatrix_F64 A = new RowMatrix_F64(4,1, true, 1, 2, 3, 4);
        RowMatrix_F64 B = new RowMatrix_F64(4,1, true, -1, -2, -3, -4);

        double val = VectorVectorMult_R64.innerProd(A,B);

        assertEquals(-30,val, UtilEjml.TEST_F64);
    }

    @Test
    public void innerProdA() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(3,4,rand);
        RowMatrix_F64 x = RandomMatrices_R64.createRandom(3,1,rand);
        RowMatrix_F64 y = RandomMatrices_R64.createRandom(4,1,rand);

        RowMatrix_F64 temp = new RowMatrix_F64(1,4);

        // compute the expected result first
        CommonOps_R64.multTransA(x,A,temp);
        double expected = VectorVectorMult_R64.innerProd(temp,y);

        double found = VectorVectorMult_R64.innerProdA(x,A,y);

        assertEquals(expected,found,UtilEjml.TEST_F64);
    }

    @Test
    public void innerProdTranA() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(3,3,rand);
        RowMatrix_F64 x = RandomMatrices_R64.createRandom(3,1,rand);
        RowMatrix_F64 y = RandomMatrices_R64.createRandom(3,1,rand);

        RowMatrix_F64 Atran = new RowMatrix_F64(3,3);
        CommonOps_R64.transpose(A,Atran);

        RowMatrix_F64 temp = new RowMatrix_F64(1,3);

        // compute the expected result first
        CommonOps_R64.multTransA(x,Atran,temp);
        double expected = VectorVectorMult_R64.innerProd(temp,y);

        double found = VectorVectorMult_R64.innerProdTranA(x,A,y);

        assertEquals(expected,found,UtilEjml.TEST_F64);
    }

    @Test
    public void outerProd() {
        RowMatrix_F64 A = new RowMatrix_F64(4,1, true, 1, 2, 3, 4);
        RowMatrix_F64 B = new RowMatrix_F64(4,1, true, -1, -2, -3, -4);

        RowMatrix_F64 C = RandomMatrices_R64.createRandom(4,4,rand);
        VectorVectorMult_R64.outerProd(A,B,C);

        // compare it against the equivalent matrix matrix multiply
        RowMatrix_F64 D =  RandomMatrices_R64.createRandom(4,4,rand);
        MatrixMatrixMult_R64.multTransB(A,B,D);

        EjmlUnitTests.assertEquals(D,C,0);
    }

    @Test
    public void addOuterProd() {
        RowMatrix_F64 A = new RowMatrix_F64(4,1, true, 1, 2, 3, 4);
        RowMatrix_F64 B = new RowMatrix_F64(4,1, true, -1, -2, -3, -4);

        RowMatrix_F64 C = RandomMatrices_R64.createRandom(4,4,rand);
        RowMatrix_F64 D =  C.copy();

        VectorVectorMult_R64.addOuterProd(1.0,A,B,C);

        // compare it against the equivalent matrix matrix multiply
        RowMatrix_F64 E = RandomMatrices_R64.createRandom(4,4,rand);
        MatrixMatrixMult_R64.multTransB(A,B,E);
        CommonOps_R64.add(D,E,D);

        assertTrue(MatrixFeatures_R64.isEquals(D,C));

        // now try it with another gamma
        C = RandomMatrices_R64.createRandom(4,4,rand);
        D = C.copy();

        VectorVectorMult_R64.addOuterProd(2.5,A,B,C);

        MatrixMatrixMult_R64.multTransB(2.5,A,B,E);
        CommonOps_R64.add(D,E,D);

        EjmlUnitTests.assertEquals(D,C,0);
    }

    @Test
    public void householder() {
        RowMatrix_F64 u = RandomMatrices_R64.createRandom(4,1,rand);
        RowMatrix_F64 x = RandomMatrices_R64.createRandom(4,1,rand);
        RowMatrix_F64 y = RandomMatrices_R64.createRandom(4,1,rand);


        double gamma = 4.5;

        VectorVectorMult_R64.householder(gamma,u,x,y);

        RowMatrix_F64 L = CommonOps_R64.identity(4,4);
        RowMatrix_F64 y_exp = RandomMatrices_R64.createRandom(4,1,rand);

        VectorVectorMult_R64.addOuterProd(gamma,u,u,L);
        CommonOps_R64.mult(L,x,y_exp);

        EjmlUnitTests.assertEquals(y,y_exp,UtilEjml.TEST_F64);
    }

    @Test
    public void rank1Update_two_square() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(6,6,rand);
        RowMatrix_F64 u = RandomMatrices_R64.createRandom(6,1,rand);
        RowMatrix_F64 w = RandomMatrices_R64.createRandom(6,1,rand);
        double gamma = -45;

        SimpleMatrix _A = SimpleMatrix.wrap(A);
        SimpleMatrix _u = SimpleMatrix.wrap(u);
        SimpleMatrix _w = SimpleMatrix.wrap(w);
        
        SimpleMatrix expected = _A.plus(_u.mult(_w.transpose()).scale(gamma));
        RowMatrix_F64 found = new RowMatrix_F64(6,6);

        VectorVectorMult_R64.rank1Update(gamma,A,u,w,found);

        EjmlUnitTests.assertEquals(expected.matrix_F64(),found,UtilEjml.TEST_F64);
    }

    @Test
    public void rank1Update_one_square() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(6,6,rand);
        RowMatrix_F64 u = RandomMatrices_R64.createRandom(6,1,rand);
        RowMatrix_F64 w = RandomMatrices_R64.createRandom(6,1,rand);
        double gamma = -45;

        SimpleMatrix _A = SimpleMatrix.wrap(A);
        SimpleMatrix _u = SimpleMatrix.wrap(u);
        SimpleMatrix _w = SimpleMatrix.wrap(w);

        SimpleMatrix expected = _A.plus(_u.mult(_w.transpose()).scale(gamma));
        RowMatrix_F64 found = A.copy();

        VectorVectorMult_R64.rank1Update(gamma,found,u,w);

        EjmlUnitTests.assertEquals(expected.matrix_F64(),found,UtilEjml.TEST_F64);
    }
}
