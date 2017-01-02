/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps_D64;
import org.ejml.ops.MatrixFeatures_D64;
import org.ejml.ops.RandomMatrices_D64;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestVectorVectorMult_D64 {

    Random rand = new Random(45837);

    @Test
    public void innerProduct() {
        DenseMatrix64F A = new DenseMatrix64F(4,1, true, 1, 2, 3, 4);
        DenseMatrix64F B = new DenseMatrix64F(4,1, true, -1, -2, -3, -4);

        double val = VectorVectorMult_D64.innerProd(A,B);

        assertEquals(-30,val, UtilEjml.TEST_64F);
    }

    @Test
    public void innerProdA() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(3,4,rand);
        DenseMatrix64F x = RandomMatrices_D64.createRandom(3,1,rand);
        DenseMatrix64F y = RandomMatrices_D64.createRandom(4,1,rand);

        DenseMatrix64F temp = new DenseMatrix64F(1,4);

        // compute the expected result first
        CommonOps_D64.multTransA(x,A,temp);
        double expected = VectorVectorMult_D64.innerProd(temp,y);

        double found = VectorVectorMult_D64.innerProdA(x,A,y);

        assertEquals(expected,found,UtilEjml.TEST_64F);
    }

    @Test
    public void innerProdTranA() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(3,3,rand);
        DenseMatrix64F x = RandomMatrices_D64.createRandom(3,1,rand);
        DenseMatrix64F y = RandomMatrices_D64.createRandom(3,1,rand);

        DenseMatrix64F Atran = new DenseMatrix64F(3,3);
        CommonOps_D64.transpose(A,Atran);

        DenseMatrix64F temp = new DenseMatrix64F(1,3);

        // compute the expected result first
        CommonOps_D64.multTransA(x,Atran,temp);
        double expected = VectorVectorMult_D64.innerProd(temp,y);

        double found = VectorVectorMult_D64.innerProdTranA(x,A,y);

        assertEquals(expected,found,UtilEjml.TEST_64F);
    }

    @Test
    public void outerProd() {
        DenseMatrix64F A = new DenseMatrix64F(4,1, true, 1, 2, 3, 4);
        DenseMatrix64F B = new DenseMatrix64F(4,1, true, -1, -2, -3, -4);

        DenseMatrix64F C = RandomMatrices_D64.createRandom(4,4,rand);
        VectorVectorMult_D64.outerProd(A,B,C);

        // compare it against the equivalent matrix matrix multiply
        DenseMatrix64F D =  RandomMatrices_D64.createRandom(4,4,rand);
        MatrixMatrixMult_D64.multTransB(A,B,D);

        EjmlUnitTests.assertEquals(D,C,0);
    }

    @Test
    public void addOuterProd() {
        DenseMatrix64F A = new DenseMatrix64F(4,1, true, 1, 2, 3, 4);
        DenseMatrix64F B = new DenseMatrix64F(4,1, true, -1, -2, -3, -4);

        DenseMatrix64F C = RandomMatrices_D64.createRandom(4,4,rand);
        DenseMatrix64F D =  C.copy();

        VectorVectorMult_D64.addOuterProd(1.0,A,B,C);

        // compare it against the equivalent matrix matrix multiply
        DenseMatrix64F E = RandomMatrices_D64.createRandom(4,4,rand);
        MatrixMatrixMult_D64.multTransB(A,B,E);
        CommonOps_D64.add(D,E,D);

        assertTrue(MatrixFeatures_D64.isEquals(D,C));

        // now try it with another gamma
        C = RandomMatrices_D64.createRandom(4,4,rand);
        D = C.copy();

        VectorVectorMult_D64.addOuterProd(2.5,A,B,C);

        MatrixMatrixMult_D64.multTransB(2.5,A,B,E);
        CommonOps_D64.add(D,E,D);

        EjmlUnitTests.assertEquals(D,C,0);
    }

    @Test
    public void householder() {
        DenseMatrix64F u = RandomMatrices_D64.createRandom(4,1,rand);
        DenseMatrix64F x = RandomMatrices_D64.createRandom(4,1,rand);
        DenseMatrix64F y = RandomMatrices_D64.createRandom(4,1,rand);


        double gamma = 4.5;

        VectorVectorMult_D64.householder(gamma,u,x,y);

        DenseMatrix64F L = CommonOps_D64.identity(4,4);
        DenseMatrix64F y_exp = RandomMatrices_D64.createRandom(4,1,rand);

        VectorVectorMult_D64.addOuterProd(gamma,u,u,L);
        CommonOps_D64.mult(L,x,y_exp);

        EjmlUnitTests.assertEquals(y,y_exp,UtilEjml.TEST_64F);
    }

    @Test
    public void rank1Update_two_square() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(6,6,rand);
        DenseMatrix64F u = RandomMatrices_D64.createRandom(6,1,rand);
        DenseMatrix64F w = RandomMatrices_D64.createRandom(6,1,rand);
        double gamma = -45;

        SimpleMatrix _A = SimpleMatrix.wrap(A);
        SimpleMatrix _u = SimpleMatrix.wrap(u);
        SimpleMatrix _w = SimpleMatrix.wrap(w);
        
        SimpleMatrix expected = _A.plus(_u.mult(_w.transpose()).scale(gamma));
        DenseMatrix64F found = new DenseMatrix64F(6,6);

        VectorVectorMult_D64.rank1Update(gamma,A,u,w,found);

        EjmlUnitTests.assertEquals(expected.getMatrix(),found,UtilEjml.TEST_64F);
    }

    @Test
    public void rank1Update_one_square() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(6,6,rand);
        DenseMatrix64F u = RandomMatrices_D64.createRandom(6,1,rand);
        DenseMatrix64F w = RandomMatrices_D64.createRandom(6,1,rand);
        double gamma = -45;

        SimpleMatrix _A = SimpleMatrix.wrap(A);
        SimpleMatrix _u = SimpleMatrix.wrap(u);
        SimpleMatrix _w = SimpleMatrix.wrap(w);

        SimpleMatrix expected = _A.plus(_u.mult(_w.transpose()).scale(gamma));
        DenseMatrix64F found = A.copy();

        VectorVectorMult_D64.rank1Update(gamma,found,u,w);

        EjmlUnitTests.assertEquals(expected.getMatrix(),found,UtilEjml.TEST_64F);
    }
}
