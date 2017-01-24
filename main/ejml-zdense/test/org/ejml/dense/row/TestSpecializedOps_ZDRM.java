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

package org.ejml.dense.row;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.mult.VectorVectorMult_ZDRM;
import org.ejml.ops.ComplexMath_F64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestSpecializedOps_ZDRM {

    private Random rand = new Random(234);


    @Test
    public void createReflector() {
        ZMatrixRMaj u = RandomMatrices_ZDRM.rectangle(4,1,rand);

        ZMatrixRMaj Q = SpecializedOps_ZDRM.createReflector(u);

        assertTrue(MatrixFeatures_ZDRM.isHermitian(Q, UtilEjml.TEST_F64));

        ZMatrixRMaj w = new ZMatrixRMaj(4,1);

        CommonOps_ZDRM.mult(Q,u,w);

        assertTrue(MatrixFeatures_ZDRM.isNegative(u,w,UtilEjml.TEST_F64));
    }

    @Test
    public void createReflector_gamma() {
        ZMatrixRMaj u = RandomMatrices_ZDRM.rectangle(4,1,rand);
        double gamma = 2.0/(double)Math.pow(NormOps_ZDRM.normF(u),2.0);
        ZMatrixRMaj Q = SpecializedOps_ZDRM.createReflector(u,gamma);

        ZMatrixRMaj w = new ZMatrixRMaj(4,1);
        CommonOps_ZDRM.mult(Q,u,w);

        assertTrue(MatrixFeatures_ZDRM.isNegative(u,w,UtilEjml.TEST_F64));
    }

    @Test
    public void pivotMatrix() {
        int pivots[] = new int[]{1,0,3,2};

        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(4,4,-1,-1,rand);
        ZMatrixRMaj P = SpecializedOps_ZDRM.pivotMatrix(null,pivots,4,false);
        ZMatrixRMaj Pt = SpecializedOps_ZDRM.pivotMatrix(null,pivots,4,true);

        ZMatrixRMaj B = new ZMatrixRMaj(4,4);

        // see if it swapped the rows
        CommonOps_ZDRM.mult(P, A, B);

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
        CommonOps_ZDRM.transpose(P,B);

        assertTrue(MatrixFeatures_ZDRM.isIdentical(B, Pt, UtilEjml.TEST_F64));
    }


    @Test
    public void elementDiagMaxMagnitude2() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(4,5,-1,1,rand);

        Complex_F64 a = new Complex_F64();

        double expected = 0;
        for (int i = 0; i < 4; i++) {
            A.get(i,i,a);
            if( a.getMagnitude2() > expected )
                expected = a.getMagnitude2();
        }

        double found = SpecializedOps_ZDRM.elementDiagMaxMagnitude2(A);
        assertEquals(expected, found, UtilEjml.TEST_F64);
    }

    @Test
    public void qualityTriangular() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(4,4,-1,-1,rand);

        double max = Math.sqrt(SpecializedOps_ZDRM.elementDiagMaxMagnitude2(A));

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

        double found = SpecializedOps_ZDRM.qualityTriangular(A);
        assertEquals(expected,found,UtilEjml.TEST_F64);
    }

    @Test
    public void householder() {
        ZMatrixRMaj U = RandomMatrices_ZDRM.rectangle(6,1,rand);
        double gamma = 1.6;

        // Q = I - gamma*U*U^H
        ZMatrixRMaj I = CommonOps_ZDRM.identity(6);
        ZMatrixRMaj UUt = new ZMatrixRMaj(6,6);
        ZMatrixRMaj expected = new ZMatrixRMaj(6,6);

        VectorVectorMult_ZDRM.outerProdH(U, U, UUt);
        CommonOps_ZDRM.elementMultiply(UUt,gamma,0,UUt);
        CommonOps_ZDRM.subtract(I,UUt,expected);

        ZMatrixRMaj found = SpecializedOps_ZDRM.householder(U,gamma);

        assertTrue(MatrixFeatures_ZDRM.isIdentical(expected,found,UtilEjml.TEST_F64));
    }

    @Test
    public void householderVector() {
        ZMatrixRMaj x = RandomMatrices_ZDRM.rectangle(6, 1, rand);

//        x.set(0,0,0,0);

        ZMatrixRMaj u = SpecializedOps_ZDRM.householderVector(x);
        double gamma = 2.0/(double)Math.pow(NormOps_ZDRM.normF(u), 2.0);

        // Q = I - gamma*U*U^H
        ZMatrixRMaj Q = CommonOps_ZDRM.identity(6);

        CommonOps_ZDRM.multAddTransB(-gamma,0,u,u,Q);

        ZMatrixRMaj found = new ZMatrixRMaj(x.numRows,x.numCols);
        CommonOps_ZDRM.mult(Q,x,found);

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