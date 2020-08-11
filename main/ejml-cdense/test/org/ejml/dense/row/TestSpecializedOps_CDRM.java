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

package org.ejml.dense.row;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F32;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.mult.VectorVectorMult_CDRM;
import org.ejml.ops.ComplexMath_F32;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestSpecializedOps_CDRM {

    private Random rand = new Random(234);


    @Test
    public void createReflector() {
        CMatrixRMaj u = RandomMatrices_CDRM.rectangle(4,1,rand);

        CMatrixRMaj Q = SpecializedOps_CDRM.createReflector(u);

        assertTrue(MatrixFeatures_CDRM.isHermitian(Q, UtilEjml.TEST_F32));

        CMatrixRMaj w = new CMatrixRMaj(4,1);

        CommonOps_CDRM.mult(Q,u,w);

        assertTrue(MatrixFeatures_CDRM.isNegative(u,w,UtilEjml.TEST_F32));
    }

    @Test
    public void createReflector_gamma() {
        CMatrixRMaj u = RandomMatrices_CDRM.rectangle(4,1,rand);
        float gamma = 2.0f/(float)Math.pow(NormOps_CDRM.normF(u),2.0f);
        CMatrixRMaj Q = SpecializedOps_CDRM.createReflector(u,gamma);

        CMatrixRMaj w = new CMatrixRMaj(4,1);
        CommonOps_CDRM.mult(Q,u,w);

        assertTrue(MatrixFeatures_CDRM.isNegative(u,w,UtilEjml.TEST_F32));
    }

    @Test
    public void pivotMatrix() {
        int pivots[] = new int[]{1,0,3,2};

        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(4,4,-1,-1,rand);
        CMatrixRMaj P = SpecializedOps_CDRM.pivotMatrix(null,pivots,4,false);
        CMatrixRMaj Pt = SpecializedOps_CDRM.pivotMatrix(null,pivots,4,true);

        CMatrixRMaj B = new CMatrixRMaj(4,4);

        // see if it swapped the rows
        CommonOps_CDRM.mult(P, A, B);

        for( int i = 0; i < 4; i++ ) {
            int index = pivots[i];
            for( int j = 0; j < 4; j++ ) {
                float real = A.getReal(index,j);
                float imag = A.getImag(index, j);

                assertEquals(real,B.getReal(i, j),UtilEjml.TEST_F32);
                assertEquals(imag,B.getImag(i, j),UtilEjml.TEST_F32);
            }
        }

        // see if it transposed
        CommonOps_CDRM.transpose(P,B);

        assertTrue(MatrixFeatures_CDRM.isIdentical(B, Pt, UtilEjml.TEST_F32));
    }


    @Test
    public void elementDiagMaxMagnitude2() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(4,5,-1,1,rand);

        Complex_F32 a = new Complex_F32();

        float expected = 0;
        for (int i = 0; i < 4; i++) {
            A.get(i,i,a);
            if( a.getMagnitude2() > expected )
                expected = a.getMagnitude2();
        }

        float found = SpecializedOps_CDRM.elementDiagMaxMagnitude2(A);
        assertEquals(expected, found, UtilEjml.TEST_F32);
    }

    @Test
    public void qualityTriangular() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(4,4,-1,-1,rand);

        float max = (float)Math.sqrt(SpecializedOps_CDRM.elementDiagMaxMagnitude2(A));

        Complex_F32 a = new Complex_F32();
        Complex_F32 tmp = new Complex_F32();
        Complex_F32 total = new Complex_F32(1,0);
        for (int i = 0; i < 4; i++) {
            A.get(i,i,a);
            a.real /= max;
            a.imaginary /= max;

            ComplexMath_F32.multiply(total,a,tmp);
            total.set(tmp);
        }
        float expected = total.getMagnitude();

        float found = SpecializedOps_CDRM.qualityTriangular(A);
        assertEquals(expected,found,UtilEjml.TEST_F32);
    }

    @Test
    public void householder() {
        CMatrixRMaj U = RandomMatrices_CDRM.rectangle(6,1,rand);
        float gamma = 1.6f;

        // Q = I - gamma*U*U^H
        CMatrixRMaj I = CommonOps_CDRM.identity(6);
        CMatrixRMaj UUt = new CMatrixRMaj(6,6);
        CMatrixRMaj expected = new CMatrixRMaj(6,6);

        VectorVectorMult_CDRM.outerProdH(U, U, UUt);
        CommonOps_CDRM.elementMultiply(UUt,gamma,0,UUt);
        CommonOps_CDRM.subtract(I,UUt,expected);

        CMatrixRMaj found = SpecializedOps_CDRM.householder(U,gamma);

        assertTrue(MatrixFeatures_CDRM.isIdentical(expected,found,UtilEjml.TEST_F32));
    }

    @Test
    public void householderVector() {
        CMatrixRMaj x = RandomMatrices_CDRM.rectangle(6, 1, rand);

//        x.set(0,0,0,0);

        CMatrixRMaj u = SpecializedOps_CDRM.householderVector(x);
        float gamma = 2.0f/(float)Math.pow(NormOps_CDRM.normF(u), 2.0f);

        // Q = I - gamma*U*U^H
        CMatrixRMaj Q = CommonOps_CDRM.identity(6);

        CommonOps_CDRM.multAddTransB(-gamma,0,u,u,Q);

        CMatrixRMaj found = new CMatrixRMaj(x.numRows,x.numCols);
        CommonOps_CDRM.mult(Q,x,found);

        Complex_F32 c = new Complex_F32();
        found.get(0,0,c);
        assertTrue(c.real != 0);
        assertTrue(c.imaginary != 0 );

        for (int i = 1; i < found.numRows; i++) {
            found.get(i,0,c);
            assertEquals(0,c.real, UtilEjml.TEST_F32);
            assertEquals(0,c.imaginary,UtilEjml.TEST_F32);
        }
    }
}