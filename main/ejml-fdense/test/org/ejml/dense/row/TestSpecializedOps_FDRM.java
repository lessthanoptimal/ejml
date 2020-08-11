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
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.mult.VectorVectorMult_FDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public class TestSpecializedOps_FDRM {
    Random rand = new Random(7476547);

    @Test
    public void createReflector() {
        FMatrixRMaj u = RandomMatrices_FDRM.rectangle(4,1,rand);

        FMatrixRMaj Q = SpecializedOps_FDRM.createReflector(u);

        assertTrue(MatrixFeatures_FDRM.isOrthogonal(Q,UtilEjml.TEST_F32));

        FMatrixRMaj w = new FMatrixRMaj(4,1);

        CommonOps_FDRM.mult(Q,u,w);

        assertTrue(MatrixFeatures_FDRM.isNegative(u,w,UtilEjml.TEST_F32));
    }

    @Test
    public void createReflector_gamma() {
        FMatrixRMaj u = RandomMatrices_FDRM.rectangle(4,1,rand);
        float gamma = 1.5f;
        FMatrixRMaj Q = SpecializedOps_FDRM.createReflector(u,gamma);

        FMatrixRMaj w = RandomMatrices_FDRM.rectangle(4,1,rand);

        FMatrixRMaj v_found = new FMatrixRMaj(4,1);
        CommonOps_FDRM.mult(Q,w,v_found);

        FMatrixRMaj v_exp = new FMatrixRMaj(4,1);

        VectorVectorMult_FDRM.householder(-gamma,u,w,v_exp);

        assertTrue(MatrixFeatures_FDRM.isIdentical(v_found,v_exp,UtilEjml.TEST_F32));
    }

    @Test
    public void copyChangeRow() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(3,2,rand);

        FMatrixRMaj B = A.copy();

        int []order = new int[]{2,0,1};

        FMatrixRMaj C = SpecializedOps_FDRM.copyChangeRow(order,A,null);

        // make sure it didn't modify A
        assertTrue(MatrixFeatures_FDRM.isIdentical(A,B,0));

        // see if the row change was correctly performed
        for( int i = 0; i < order.length; i++ ) {
            int o = order[i];

            for( int j = 0; j < A.numCols; j++ ) {
                assertEquals(A.get(o,j),C.get(i,j),1e-16);
            }
        }
    }

    @Test
    public void copyTriangle() {

        for( int m = 2; m <= 6; m += 2 ) {
            for( int n = 2; n <= 6; n += 2 ) {
                FMatrixRMaj A = RandomMatrices_FDRM.rectangle(m,n,rand);

                FMatrixRMaj B = SpecializedOps_FDRM.copyTriangle(A,null,true);

                assertTrue(MatrixFeatures_FDRM.isEqualsTriangle(A,B, true, UtilEjml.TEST_F32));
                assertFalse(MatrixFeatures_FDRM.isEquals(A,B,UtilEjml.TEST_F32));

                CommonOps_FDRM.fill(B, 0);
                SpecializedOps_FDRM.copyTriangle(A,B,false);
                assertTrue(MatrixFeatures_FDRM.isEqualsTriangle(A,B, false, UtilEjml.TEST_F32));
                assertFalse(MatrixFeatures_FDRM.isEquals(A,B,UtilEjml.TEST_F32));
            }
        }
    }

    @Test
    public void multLowerTranB() {
        for( int m = 1; m <= 10; m++ ) {
            FMatrixRMaj L = RandomMatrices_FDRM.triangularUpper(m,0,-1,1,rand);
            CommonOps_FDRM.transpose(L);

            FMatrixRMaj expected = new FMatrixRMaj(m,m);
            CommonOps_FDRM.multTransB(L,L,expected);

            SpecializedOps_FDRM.multLowerTranB(L);

            assertTrue(MatrixFeatures_FDRM.isIdentical(expected,L,UtilEjml.TEST_F32));
        }
    }

    @Test
    public void multLowerTranA() {
        for( int m = 1; m <= 10; m++ ) {
            FMatrixRMaj L = RandomMatrices_FDRM.triangularUpper(m,0,-1,1,rand);
            CommonOps_FDRM.transpose(L);

            FMatrixRMaj expected = new FMatrixRMaj(m,m);
            CommonOps_FDRM.multTransA(L,L,expected);

            SpecializedOps_FDRM.multLowerTranA(L);

            assertTrue(MatrixFeatures_FDRM.isIdentical(expected,L,UtilEjml.TEST_F32));
        }
    }

    @Test
    public void diffNormF() {
        FMatrixRMaj a = RandomMatrices_FDRM.rectangle(3,2,rand);
        FMatrixRMaj b = RandomMatrices_FDRM.rectangle(3,2,rand);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(3,2,rand);

        CommonOps_FDRM.subtract(a, b, c);
        float expectedNorm = NormOps_FDRM.fastNormF(c);
        float foundNorm = SpecializedOps_FDRM.diffNormF(a,b);

        assertEquals(expectedNorm,foundNorm,UtilEjml.TEST_F32);
    }

    @Test
    public void diffNormP1() {
        FMatrixRMaj a = RandomMatrices_FDRM.rectangle(3,2,rand);
        FMatrixRMaj b = RandomMatrices_FDRM.rectangle(3,2,rand);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(3,2,rand);

        CommonOps_FDRM.subtract(a, b, c);
        float expectedNorm = 0;
        for( int i = 0; i < c.getNumElements(); i++ ) {
            expectedNorm += Math.abs(c.get(i));
        }
        float foundNorm = SpecializedOps_FDRM.diffNormP1(a,b);

        assertEquals(expectedNorm,foundNorm, UtilEjml.TEST_F32);
    }

    @Test
    public void addIdentity() {
        FMatrixRMaj M = RandomMatrices_FDRM.rectangle(4,4,rand);
        FMatrixRMaj A = M.copy();

        SpecializedOps_FDRM.addIdentity(A,A,2.0f);
        CommonOps_FDRM.subtractEquals(A, M);

        float total = CommonOps_FDRM.elementSum(A);

        assertEquals(4*2,total,UtilEjml.TEST_F32);
    }

    @Test
    public void subvector() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(4,5,rand);

        FMatrixRMaj v = new FMatrixRMaj(7,1);

        // first extract a row vector
        SpecializedOps_FDRM.subvector(A,2,1,2,true,1,v);

        assertEquals(0,v.get(0),UtilEjml.TEST_F32);

        for( int i = 0; i < 2; i++ ) {
            assertEquals(A.get(2,1+i),v.get(1+i),UtilEjml.TEST_F32);
        }

        // now extract a column vector
        SpecializedOps_FDRM.subvector(A,2,1,2,false,1,v);

        for( int i = 0; i < 2; i++ ) {
            assertEquals(A.get(2+i,1),v.get(1+i),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void splitIntoVectors() {

        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(3,5,rand);

        // column vectors
        FMatrixRMaj v[] = SpecializedOps_FDRM.splitIntoVectors(A,true);

        assertEquals(5,v.length);
        for( int i = 0; i < v.length; i++ ) {
            FMatrixRMaj a = v[i];

            assertEquals(3,a.getNumRows());
            assertEquals(1,a.getNumCols());

            for( int j = 0; j < A.numRows; j++ ) {
                assertEquals(A.get(j,i),a.get(j),UtilEjml.TEST_F32);
            }
        }

        // row vectors
        v = SpecializedOps_FDRM.splitIntoVectors(A,false);

        assertEquals(3,v.length);
        for( int i = 0; i < v.length; i++ ) {
            FMatrixRMaj a = v[i];

            assertEquals(1,a.getNumRows());
            assertEquals(5,a.getNumCols());

            for( int j = 0; j < A.numCols; j++ ) {
                assertEquals(A.get(i,j),a.get(j),UtilEjml.TEST_F32);
            }
        }

    }

    @Test
    public void pivotMatrix() {
        int pivots[] = new int[]{1,0,3,2};

        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(4,4,rand);
        FMatrixRMaj P = SpecializedOps_FDRM.pivotMatrix(null,pivots,4,false);
        FMatrixRMaj Pt = SpecializedOps_FDRM.pivotMatrix(null,pivots,4,true);

        FMatrixRMaj B = new FMatrixRMaj(4,4);

        // see if it swapped the rows
        CommonOps_FDRM.mult(P,A,B);

        for( int i = 0; i < 4; i++ ) {
            int index = pivots[i];
            for( int j = 0; j < 4; j++ ) {
                float val = A.get(index,j);
                assertEquals(val,B.get(i,j),UtilEjml.TEST_F32);
            }
        }

        // see if it transposed
        CommonOps_FDRM.transpose(P,B);

        assertTrue(MatrixFeatures_FDRM.isIdentical(B,Pt,UtilEjml.TEST_F32));
    }

    @Test
    public void diagProd() {
        FMatrixRMaj A = new FMatrixRMaj(3,3);

        A.set(0,0,1);
        A.set(1,1,2);
        A.set(2,2,3);

        float found = SpecializedOps_FDRM.diagProd(A);

        assertEquals(6,found,UtilEjml.TEST_F32);
    }

    @Test
    public void elementDiagonalMaxAbs() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(4,5,rand);

        float expected = 0;
        for (int i = 0; i < 4; i++) {
            float a = A.get(i,i);
            if( Math.abs(a) > expected )
                expected = Math.abs(a);
        }

        float found = SpecializedOps_FDRM.elementDiagonalMaxAbs(A);
        assertEquals(expected,found,UtilEjml.TEST_F32);
    }

    @Test
    public void qualityTriangular() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(4,4,rand);

        float max = SpecializedOps_FDRM.elementDiagonalMaxAbs(A);
        float expected = 1.0f;
        for (int i = 0; i < 4; i++) {
            expected *= A.get(i,i)/max;
        }

        float found = SpecializedOps_FDRM.qualityTriangular(A);
        assertEquals(expected,found,UtilEjml.TEST_F32);
    }
    
    @Test
    public void elementSumSq() {
        FMatrixRMaj A = new FMatrixRMaj(2,3,true,1,2,3,4,5,6);
        
        float expected = 1+4+9+16+25+36;
        float found = SpecializedOps_FDRM.elementSumSq(A);
        
        assertEquals(expected,found,UtilEjml.TEST_F32);

        A.zero();
        found = SpecializedOps_FDRM.elementSumSq(A);
        assertEquals(0.0f,found,UtilEjml.TEST_F32);
    }
}
