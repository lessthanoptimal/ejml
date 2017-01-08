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
import org.ejml.alg.dense.mult.VectorVectorMult_CR64;
import org.ejml.data.Complex_F64;
import org.ejml.data.RowMatrix_C64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestSpecializedOps_CR64 {

    private Random rand = new Random(234);


    @Test
    public void createReflector() {
        RowMatrix_C64 u = RandomMatrices_CR64.createRandom(4,1,rand);

        RowMatrix_C64 Q = SpecializedOps_CR64.createReflector(u);

        assertTrue(MatrixFeatures_CR64.isHermitian(Q, UtilEjml.TEST_F64));

        RowMatrix_C64 w = new RowMatrix_C64(4,1);

        CommonOps_CR64.mult(Q,u,w);

        assertTrue(MatrixFeatures_CR64.isNegative(u,w,UtilEjml.TEST_F64));
    }

    @Test
    public void createReflector_gamma() {
        RowMatrix_C64 u = RandomMatrices_CR64.createRandom(4,1,rand);
        double gamma = 2.0/(double)Math.pow(NormOps_CR64.normF(u),2.0);
        RowMatrix_C64 Q = SpecializedOps_CR64.createReflector(u,gamma);

        RowMatrix_C64 w = new RowMatrix_C64(4,1);
        CommonOps_CR64.mult(Q,u,w);

        assertTrue(MatrixFeatures_CR64.isNegative(u,w,UtilEjml.TEST_F64));
    }

    @Test
    public void pivotMatrix() {
        int pivots[] = new int[]{1,0,3,2};

        RowMatrix_C64 A = RandomMatrices_CR64.createRandom(4,4,-1,-1,rand);
        RowMatrix_C64 P = SpecializedOps_CR64.pivotMatrix(null,pivots,4,false);
        RowMatrix_C64 Pt = SpecializedOps_CR64.pivotMatrix(null,pivots,4,true);

        RowMatrix_C64 B = new RowMatrix_C64(4,4);

        // see if it swapped the rows
        CommonOps_CR64.mult(P, A, B);

        for( int i = 0; i < 4; i++ ) {
            int index = pivots[i];
            for( int j = 0; j < 4; j++ ) {
                double real = A.getReal(index,j);
                double imag = A.getImag(index, j);

                assertEquals(real,B.getReal(i, j),UtilEjml.TEST_F64);
                assertEquals(imag,B.getImag(i, j),UtilEjml.TEST_F64);
            }
        }

        // see if it transposed
        CommonOps_CR64.transpose(P,B);

        assertTrue(MatrixFeatures_CR64.isIdentical(B, Pt, UtilEjml.TEST_F64));
    }


    @Test
    public void elementDiagMaxMagnitude2() {
        RowMatrix_C64 A = RandomMatrices_CR64.createRandom(4,5,-1,1,rand);

        Complex_F64 a = new Complex_F64();

        double expected = 0;
        for (int i = 0; i < 4; i++) {
            A.get(i,i,a);
            if( a.getMagnitude2() > expected )
                expected = a.getMagnitude2();
        }

        double found = SpecializedOps_CR64.elementDiagMaxMagnitude2(A);
        assertEquals(expected, found, UtilEjml.TEST_F64);
    }

    @Test
    public void qualityTriangular() {
        RowMatrix_C64 A = RandomMatrices_CR64.createRandom(4,4,-1,-1,rand);

        double max = Math.sqrt(SpecializedOps_CR64.elementDiagMaxMagnitude2(A));

        Complex_F64 a = new Complex_F64();
        Complex_F64 tmp = new Complex_F64();
        Complex_F64 total = new Complex_F64(1,0);
        for (int i = 0; i < 4; i++) {
            A.get(i,i,a);
            a.real /= max;
            a.imaginary /= max;

            ComplexMath_F64.multiply(total,a,tmp);
            total.set(tmp);
        }
        double expected = total.getMagnitude();

        double found = SpecializedOps_CR64.qualityTriangular(A);
        assertEquals(expected,found,UtilEjml.TEST_F64);
    }

    @Test
    public void householder() {
        RowMatrix_C64 U = RandomMatrices_CR64.createRandom(6,1,rand);
        double gamma = 1.6;

        // Q = I - gamma*U*U^H
        RowMatrix_C64 I = CommonOps_CR64.identity(6);
        RowMatrix_C64 UUt = new RowMatrix_C64(6,6);
        RowMatrix_C64 expected = new RowMatrix_C64(6,6);

        VectorVectorMult_CR64.outerProdH(U, U, UUt);
        CommonOps_CR64.elementMultiply(UUt,gamma,0,UUt);
        CommonOps_CR64.subtract(I,UUt,expected);

        RowMatrix_C64 found = SpecializedOps_CR64.householder(U,gamma);

        assertTrue(MatrixFeatures_CR64.isIdentical(expected,found,UtilEjml.TEST_F64));
    }

    @Test
    public void householderVector() {
        RowMatrix_C64 x = RandomMatrices_CR64.createRandom(6, 1, rand);

//        x.set(0,0,0,0);

        RowMatrix_C64 u = SpecializedOps_CR64.householderVector(x);
        double gamma = 2.0/(double)Math.pow(NormOps_CR64.normF(u), 2.0);

        // Q = I - gamma*U*U^H
        RowMatrix_C64 Q = CommonOps_CR64.identity(6);

        CommonOps_CR64.multAddTransB(-gamma,0,u,u,Q);

        RowMatrix_C64 found = new RowMatrix_C64(x.numRows,x.numCols);
        CommonOps_CR64.mult(Q,x,found);

        Complex_F64 c = new Complex_F64();
        found.get(0,0,c);
        assertTrue(c.real != 0);
        assertTrue(c.imaginary != 0 );

        for (int i = 1; i < found.numRows; i++) {
            found.get(i,0,c);
            assertEquals(0,c.real, UtilEjml.TEST_F64);
            assertEquals(0,c.imaginary,UtilEjml.TEST_F64);
        }
    }
}