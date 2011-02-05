/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.dense.mult;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.*;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestVectorVectorMult {

    Random rand = new Random(45837);

    @Test
    public void innerProduct() {
        DenseMatrix64F A = new DenseMatrix64F(4,1, true, 1, 2, 3, 4);
        DenseMatrix64F B = new DenseMatrix64F(4,1, true, -1, -2, -3, -4);

        double val = VectorVectorMult.innerProd(A,B);

        assertEquals(-30,val,1e-8);
    }

    @Test
    public void innerProdA() {
        DenseMatrix64F A = RandomMatrices.createRandom(3,3,rand);
        DenseMatrix64F x = RandomMatrices.createRandom(3,1,rand);
        DenseMatrix64F y = RandomMatrices.createRandom(3,1,rand);

        DenseMatrix64F temp = new DenseMatrix64F(1,3);

        // compute the expected result first
        CommonOps.multTransA(x,A,temp);
        double expected = VectorVectorMult.innerProd(temp,y);

        double found = VectorVectorMult.innerProdA(x,A,y);

        assertEquals(expected,found,1e-8);

    }

    @Test
    public void innerProdTranA() {
        DenseMatrix64F A = RandomMatrices.createRandom(3,3,rand);
        DenseMatrix64F x = RandomMatrices.createRandom(3,1,rand);
        DenseMatrix64F y = RandomMatrices.createRandom(3,1,rand);

        DenseMatrix64F Atran = new DenseMatrix64F(3,3);
        CommonOps.transpose(A,Atran);

        DenseMatrix64F temp = new DenseMatrix64F(1,3);

        // compute the expected result first
        CommonOps.multTransA(x,Atran,temp);
        double expected = VectorVectorMult.innerProd(temp,y);

        double found = VectorVectorMult.innerProdTranA(x,A,y);

        assertEquals(expected,found,1e-8);
    }

    @Test
    public void outerProd() {
        DenseMatrix64F A = new DenseMatrix64F(4,1, true, 1, 2, 3, 4);
        DenseMatrix64F B = new DenseMatrix64F(4,1, true, -1, -2, -3, -4);

        DenseMatrix64F C = RandomMatrices.createRandom(4,4,rand);
        VectorVectorMult.outerProd(A,B,C);

        // compare it against the equivalent matrix matrix multiply
        DenseMatrix64F D =  RandomMatrices.createRandom(4,4,rand);
        MatrixMatrixMult.multTransB(A,B,D);

        EjmlUnitTests.assertEquals(D,C,0);
    }

    @Test
    public void addOuterProd() {
        DenseMatrix64F A = new DenseMatrix64F(4,1, true, 1, 2, 3, 4);
        DenseMatrix64F B = new DenseMatrix64F(4,1, true, -1, -2, -3, -4);

        DenseMatrix64F C = RandomMatrices.createRandom(4,4,rand);
        DenseMatrix64F D =  C.copy();

        VectorVectorMult.addOuterProd(1.0,A,B,C);

        // compare it against the equivalent matrix matrix multiply
        DenseMatrix64F E = RandomMatrices.createRandom(4,4,rand);
        MatrixMatrixMult.multTransB(A,B,E);
        CommonOps.add(D,E,D);

        assertTrue(MatrixFeatures.isEquals(D,C));

        // now try it with another gamma
        C = RandomMatrices.createRandom(4,4,rand);
        D = C.copy();

        VectorVectorMult.addOuterProd(2.5,A,B,C);

        MatrixMatrixMult.multTransB(2.5,A,B,E);
        CommonOps.add(D,E,D);

        EjmlUnitTests.assertEquals(D,C,0);
    }

    @Test
    public void householder() {
        DenseMatrix64F u = RandomMatrices.createRandom(4,1,rand);
        DenseMatrix64F x = RandomMatrices.createRandom(4,1,rand);
        DenseMatrix64F y = RandomMatrices.createRandom(4,1,rand);


        double gamma = 4.5;

        VectorVectorMult.householder(gamma,u,x,y);

        DenseMatrix64F L = CommonOps.identity(4,4);
        DenseMatrix64F y_exp = RandomMatrices.createRandom(4,1,rand);

        VectorVectorMult.addOuterProd(gamma,u,u,L);
        CommonOps.mult(L,x,y_exp);

        EjmlUnitTests.assertEquals(y,y_exp,1e-8);
    }

    @Test
    public void rank1UpdateIdenity() {
        DenseMatrix64F u = RandomMatrices.createRandom(6,1,rand);
        DenseMatrix64F x = RandomMatrices.createRandom(6,1,rand);

        NormOps.normalizeF(u);

        DenseMatrix64F Q = SpecializedOps.createReflector(u);

        DenseMatrix64F expected = new DenseMatrix64F(6,1);
        DenseMatrix64F found = new DenseMatrix64F(6,1);

        CommonOps.mult(Q,x,expected);
        VectorVectorMult.householder(-2.0,u,x,found);

        EjmlUnitTests.assertEquals(expected,found,1e-8);
    }
}
