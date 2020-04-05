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
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public class TestSpecializedOps_DDRM {
    Random rand = new Random(7476547);

    @Test
    public void createReflector() {
        DMatrixRMaj u = RandomMatrices_DDRM.rectangle(4,1,rand);

        DMatrixRMaj Q = SpecializedOps_DDRM.createReflector(u);

        assertTrue(MatrixFeatures_DDRM.isOrthogonal(Q,UtilEjml.TEST_F64));

        DMatrixRMaj w = new DMatrixRMaj(4,1);

        CommonOps_DDRM.mult(Q,u,w);

        assertTrue(MatrixFeatures_DDRM.isNegative(u,w,UtilEjml.TEST_F64));
    }

    @Test
    public void createReflector_gamma() {
        DMatrixRMaj u = RandomMatrices_DDRM.rectangle(4,1,rand);
        double gamma = 1.5;
        DMatrixRMaj Q = SpecializedOps_DDRM.createReflector(u,gamma);

        DMatrixRMaj w = RandomMatrices_DDRM.rectangle(4,1,rand);

        DMatrixRMaj v_found = new DMatrixRMaj(4,1);
        CommonOps_DDRM.mult(Q,w,v_found);

        DMatrixRMaj v_exp = new DMatrixRMaj(4,1);

        VectorVectorMult_DDRM.householder(-gamma,u,w,v_exp);

        assertTrue(MatrixFeatures_DDRM.isIdentical(v_found,v_exp,UtilEjml.TEST_F64));
    }

    @Test
    public void copyChangeRow() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3,2,rand);

        DMatrixRMaj B = A.copy();

        int []order = new int[]{2,0,1};

        DMatrixRMaj C = SpecializedOps_DDRM.copyChangeRow(order,A,null);

        // make sure it didn't modify A
        assertTrue(MatrixFeatures_DDRM.isIdentical(A,B,0));

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
                DMatrixRMaj A = RandomMatrices_DDRM.rectangle(m,n,rand);

                DMatrixRMaj B = SpecializedOps_DDRM.copyTriangle(A,null,true);

                assertTrue(MatrixFeatures_DDRM.isEqualsTriangle(A,B, true, UtilEjml.TEST_F64));
                assertFalse(MatrixFeatures_DDRM.isEquals(A,B,UtilEjml.TEST_F64));

                CommonOps_DDRM.fill(B, 0);
                SpecializedOps_DDRM.copyTriangle(A,B,false);
                assertTrue(MatrixFeatures_DDRM.isEqualsTriangle(A,B, false, UtilEjml.TEST_F64));
                assertFalse(MatrixFeatures_DDRM.isEquals(A,B,UtilEjml.TEST_F64));
            }
        }
    }

    @Test
    public void multLowerTranB() {
        for( int m = 1; m <= 10; m++ ) {
            DMatrixRMaj L = RandomMatrices_DDRM.triangularUpper(m,0,-1,1,rand);
            CommonOps_DDRM.transpose(L);

            DMatrixRMaj expected = new DMatrixRMaj(m,m);
            CommonOps_DDRM.multTransB(L,L,expected);

            SpecializedOps_DDRM.multLowerTranB(L);

            assertTrue(MatrixFeatures_DDRM.isIdentical(expected,L,UtilEjml.TEST_F64));
        }
    }

    @Test
    public void multLowerTranA() {
        for( int m = 1; m <= 10; m++ ) {
            DMatrixRMaj L = RandomMatrices_DDRM.triangularUpper(m,0,-1,1,rand);
            CommonOps_DDRM.transpose(L);

            DMatrixRMaj expected = new DMatrixRMaj(m,m);
            CommonOps_DDRM.multTransA(L,L,expected);

            SpecializedOps_DDRM.multLowerTranA(L);

            assertTrue(MatrixFeatures_DDRM.isIdentical(expected,L,UtilEjml.TEST_F64));
        }
    }

    @Test
    public void diffNormF() {
        DMatrixRMaj a = RandomMatrices_DDRM.rectangle(3,2,rand);
        DMatrixRMaj b = RandomMatrices_DDRM.rectangle(3,2,rand);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(3,2,rand);

        CommonOps_DDRM.subtract(a, b, c);
        double expectedNorm = NormOps_DDRM.fastNormF(c);
        double foundNorm = SpecializedOps_DDRM.diffNormF(a,b);

        assertEquals(expectedNorm,foundNorm,UtilEjml.TEST_F64);
    }

    @Test
    public void diffNormP1() {
        DMatrixRMaj a = RandomMatrices_DDRM.rectangle(3,2,rand);
        DMatrixRMaj b = RandomMatrices_DDRM.rectangle(3,2,rand);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(3,2,rand);

        CommonOps_DDRM.subtract(a, b, c);
        double expectedNorm = 0;
        for( int i = 0; i < c.getNumElements(); i++ ) {
            expectedNorm += Math.abs(c.get(i));
        }
        double foundNorm = SpecializedOps_DDRM.diffNormP1(a,b);

        assertEquals(expectedNorm,foundNorm, UtilEjml.TEST_F64);
    }

    @Test
    public void addIdentity() {
        DMatrixRMaj M = RandomMatrices_DDRM.rectangle(4,4,rand);
        DMatrixRMaj A = M.copy();

        SpecializedOps_DDRM.addIdentity(A,A,2.0);
        CommonOps_DDRM.subtractEquals(A, M);

        double total = CommonOps_DDRM.elementSum(A);

        assertEquals(4*2,total,UtilEjml.TEST_F64);
    }

    @Test
    public void subvector() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4,5,rand);

        DMatrixRMaj v = new DMatrixRMaj(7,1);

        // first extract a row vector
        SpecializedOps_DDRM.subvector(A,2,1,2,true,1,v);

        assertEquals(0,v.get(0),UtilEjml.TEST_F64);

        for( int i = 0; i < 2; i++ ) {
            assertEquals(A.get(2,1+i),v.get(1+i),UtilEjml.TEST_F64);
        }

        // now extract a column vector
        SpecializedOps_DDRM.subvector(A,2,1,2,false,1,v);

        for( int i = 0; i < 2; i++ ) {
            assertEquals(A.get(2+i,1),v.get(1+i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void splitIntoVectors() {

        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3,5,rand);

        // column vectors
        DMatrixRMaj v[] = SpecializedOps_DDRM.splitIntoVectors(A,true);

        assertEquals(5,v.length);
        for( int i = 0; i < v.length; i++ ) {
            DMatrixRMaj a = v[i];

            assertEquals(3,a.getNumRows());
            assertEquals(1,a.getNumCols());

            for( int j = 0; j < A.numRows; j++ ) {
                assertEquals(A.get(j,i),a.get(j),UtilEjml.TEST_F64);
            }
        }

        // row vectors
        v = SpecializedOps_DDRM.splitIntoVectors(A,false);

        assertEquals(3,v.length);
        for( int i = 0; i < v.length; i++ ) {
            DMatrixRMaj a = v[i];

            assertEquals(1,a.getNumRows());
            assertEquals(5,a.getNumCols());

            for( int j = 0; j < A.numCols; j++ ) {
                assertEquals(A.get(i,j),a.get(j),UtilEjml.TEST_F64);
            }
        }

    }

    @Test
    public void pivotMatrix() {
        int pivots[] = new int[]{1,0,3,2};

        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4,4,rand);
        DMatrixRMaj P = SpecializedOps_DDRM.pivotMatrix(null,pivots,4,false);
        DMatrixRMaj Pt = SpecializedOps_DDRM.pivotMatrix(null,pivots,4,true);

        DMatrixRMaj B = new DMatrixRMaj(4,4);

        // see if it swapped the rows
        CommonOps_DDRM.mult(P,A,B);

        for( int i = 0; i < 4; i++ ) {
            int index = pivots[i];
            for( int j = 0; j < 4; j++ ) {
                double val = A.get(index,j);
                assertEquals(val,B.get(i,j),UtilEjml.TEST_F64);
            }
        }

        // see if it transposed
        CommonOps_DDRM.transpose(P,B);

        assertTrue(MatrixFeatures_DDRM.isIdentical(B,Pt,UtilEjml.TEST_F64));
    }

    @Test
    public void diagProd() {
        DMatrixRMaj A = new DMatrixRMaj(3,3);

        A.set(0,0,1);
        A.set(1,1,2);
        A.set(2,2,3);

        double found = SpecializedOps_DDRM.diagProd(A);

        assertEquals(6,found,UtilEjml.TEST_F64);
    }

    @Test
    public void elementDiagonalMaxAbs() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4,5,rand);

        double expected = 0;
        for (int i = 0; i < 4; i++) {
            double a = A.get(i,i);
            if( Math.abs(a) > expected )
                expected = Math.abs(a);
        }

        double found = SpecializedOps_DDRM.elementDiagonalMaxAbs(A);
        assertEquals(expected,found,UtilEjml.TEST_F64);
    }

    @Test
    public void qualityTriangular() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4,4,rand);

        double max = SpecializedOps_DDRM.elementDiagonalMaxAbs(A);
        double expected = 1.0;
        for (int i = 0; i < 4; i++) {
            expected *= A.get(i,i)/max;
        }

        double found = SpecializedOps_DDRM.qualityTriangular(A);
        assertEquals(expected,found,UtilEjml.TEST_F64);
    }
    
    @Test
    public void elementSumSq() {
        DMatrixRMaj A = new DMatrixRMaj(2,3,true,1,2,3,4,5,6);
        
        double expected = 1+4+9+16+25+36;
        double found = SpecializedOps_DDRM.elementSumSq(A);
        
        assertEquals(expected,found,UtilEjml.TEST_F64);

        A.zero();
        found = SpecializedOps_DDRM.elementSumSq(A);
        assertEquals(0.0,found,UtilEjml.TEST_F64);
    }
}
