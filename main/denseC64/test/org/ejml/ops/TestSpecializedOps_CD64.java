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

package org.ejml.ops;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.mult.VectorVectorMult_CD64;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestSpecializedOps_CD64 {

    private Random rand = new Random(234);


    @Test
    public void createReflector() {
        CDenseMatrix64F u = RandomMatrices_CD64.createRandom(4,1,rand);

        CDenseMatrix64F Q = SpecializedOps_CD64.createReflector(u);

        assertTrue(MatrixFeatures_CD64.isHermitian(Q, UtilEjml.TEST_64F));

        CDenseMatrix64F w = new CDenseMatrix64F(4,1);

        CommonOps_CD64.mult(Q,u,w);

        assertTrue(MatrixFeatures_CD64.isNegative(u,w,UtilEjml.TEST_64F));
    }

    @Test
    public void createReflector_gamma() {
        CDenseMatrix64F u = RandomMatrices_CD64.createRandom(4,1,rand);
        double gamma = 2.0/(double)Math.pow(NormOps_CD64.normF(u),2.0);
        CDenseMatrix64F Q = SpecializedOps_CD64.createReflector(u,gamma);

        CDenseMatrix64F w = new CDenseMatrix64F(4,1);
        CommonOps_CD64.mult(Q,u,w);

        assertTrue(MatrixFeatures_CD64.isNegative(u,w,UtilEjml.TEST_64F));
    }

    @Test
    public void pivotMatrix() {
        int pivots[] = new int[]{1,0,3,2};

        CDenseMatrix64F A = RandomMatrices_CD64.createRandom(4,4,-1,-1,rand);
        CDenseMatrix64F P = SpecializedOps_CD64.pivotMatrix(null,pivots,4,false);
        CDenseMatrix64F Pt = SpecializedOps_CD64.pivotMatrix(null,pivots,4,true);

        CDenseMatrix64F B = new CDenseMatrix64F(4,4);

        // see if it swapped the rows
        CommonOps_CD64.mult(P, A, B);

        for( int i = 0; i < 4; i++ ) {
            int index = pivots[i];
            for( int j = 0; j < 4; j++ ) {
                double real = A.getReal(index,j);
                double imag = A.getImag(index, j);

                assertEquals(real,B.getReal(i, j),UtilEjml.TEST_64F);
                assertEquals(imag,B.getImag(i, j),UtilEjml.TEST_64F);
            }
        }

        // see if it transposed
        CommonOps_CD64.transpose(P,B);

        assertTrue(MatrixFeatures_CD64.isIdentical(B, Pt, UtilEjml.TEST_64F));
    }


    @Test
    public void elementDiagMaxMagnitude2() {
        CDenseMatrix64F A = RandomMatrices_CD64.createRandom(4,5,-1,1,rand);

        Complex64F a = new Complex64F();

        double expected = 0;
        for (int i = 0; i < 4; i++) {
            A.get(i,i,a);
            if( a.getMagnitude2() > expected )
                expected = a.getMagnitude2();
        }

        double found = SpecializedOps_CD64.elementDiagMaxMagnitude2(A);
        assertEquals(expected, found, UtilEjml.TEST_64F);
    }

    @Test
    public void qualityTriangular() {
        CDenseMatrix64F A = RandomMatrices_CD64.createRandom(4,4,-1,-1,rand);

        double max = Math.sqrt(SpecializedOps_CD64.elementDiagMaxMagnitude2(A));

        Complex64F a = new Complex64F();
        Complex64F tmp = new Complex64F();
        Complex64F total = new Complex64F(1,0);
        for (int i = 0; i < 4; i++) {
            A.get(i,i,a);
            a.real /= max;
            a.imaginary /= max;

            ComplexMath64F.multiply(total,a,tmp);
            total.set(tmp);
        }
        double expected = total.getMagnitude();

        double found = SpecializedOps_CD64.qualityTriangular(A);
        assertEquals(expected,found,UtilEjml.TEST_64F);
    }

    @Test
    public void householder() {
        CDenseMatrix64F U = RandomMatrices_CD64.createRandom(6,1,rand);
        double gamma = 1.6;

        // Q = I - gamma*U*U^H
        CDenseMatrix64F I = CommonOps_CD64.identity(6);
        CDenseMatrix64F UUt = new CDenseMatrix64F(6,6);
        CDenseMatrix64F expected = new CDenseMatrix64F(6,6);

        VectorVectorMult_CD64.outerProdH(U, U, UUt);
        CommonOps_CD64.elementMultiply(UUt,gamma,0,UUt);
        CommonOps_CD64.subtract(I,UUt,expected);

        CDenseMatrix64F found = SpecializedOps_CD64.householder(U,gamma);

        assertTrue(MatrixFeatures_CD64.isIdentical(expected,found,UtilEjml.TEST_64F));
    }

    @Test
    public void householderVector() {
        CDenseMatrix64F x = RandomMatrices_CD64.createRandom(6, 1, rand);

//        x.set(0,0,0,0);

        CDenseMatrix64F u = SpecializedOps_CD64.householderVector(x);
        double gamma = 2.0/(double)Math.pow(NormOps_CD64.normF(u), 2.0);

        // Q = I - gamma*U*U^H
        CDenseMatrix64F Q = CommonOps_CD64.identity(6);

        CommonOps_CD64.multAddTransB(-gamma,0,u,u,Q);

        CDenseMatrix64F found = new CDenseMatrix64F(x.numRows,x.numCols);
        CommonOps_CD64.mult(Q,x,found);

        Complex64F c = new Complex64F();
        found.get(0,0,c);
        assertTrue(c.real != 0);
        assertTrue(c.imaginary != 0 );

        for (int i = 1; i < found.numRows; i++) {
            found.get(i,0,c);
            assertEquals(0,c.real, UtilEjml.TEST_64F);
            assertEquals(0,c.imaginary,UtilEjml.TEST_64F);
        }
    }
}