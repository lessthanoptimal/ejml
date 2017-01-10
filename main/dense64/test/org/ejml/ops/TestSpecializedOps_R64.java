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
import org.ejml.alg.dense.mult.VectorVectorMult_R64;
import org.ejml.data.DMatrixRow_F64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestSpecializedOps_R64 {
    Random rand = new Random(7476547);

    @Test
    public void createReflector() {
        DMatrixRow_F64 u = RandomMatrices_R64.createRandom(4,1,rand);

        DMatrixRow_F64 Q = SpecializedOps_R64.createReflector(u);

        assertTrue(MatrixFeatures_R64.isOrthogonal(Q,UtilEjml.TEST_F64));

        DMatrixRow_F64 w = new DMatrixRow_F64(4,1);

        CommonOps_R64.mult(Q,u,w);

        assertTrue(MatrixFeatures_R64.isNegative(u,w,UtilEjml.TEST_F64));
    }

    @Test
    public void createReflector_gamma() {
        DMatrixRow_F64 u = RandomMatrices_R64.createRandom(4,1,rand);
        double gamma = 1.5;
        DMatrixRow_F64 Q = SpecializedOps_R64.createReflector(u,gamma);

        DMatrixRow_F64 w = RandomMatrices_R64.createRandom(4,1,rand);

        DMatrixRow_F64 v_found = new DMatrixRow_F64(4,1);
        CommonOps_R64.mult(Q,w,v_found);

        DMatrixRow_F64 v_exp = new DMatrixRow_F64(4,1);

        VectorVectorMult_R64.householder(-gamma,u,w,v_exp);

        assertTrue(MatrixFeatures_R64.isIdentical(v_found,v_exp,UtilEjml.TEST_F64));
    }

    @Test
    public void copyChangeRow() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(3,2,rand);

        DMatrixRow_F64 B = A.copy();

        int []order = new int[]{2,0,1};

        DMatrixRow_F64 C = SpecializedOps_R64.copyChangeRow(order,A,null);

        // make sure it didn't modify A
        assertTrue(MatrixFeatures_R64.isIdentical(A,B,0));

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
                DMatrixRow_F64 A = RandomMatrices_R64.createRandom(m,n,rand);

                DMatrixRow_F64 B = SpecializedOps_R64.copyTriangle(A,null,true);

                assertTrue(MatrixFeatures_R64.isEqualsTriangle(A,B, true, UtilEjml.TEST_F64));
                assertFalse(MatrixFeatures_R64.isEquals(A,B,UtilEjml.TEST_F64));

                CommonOps_R64.fill(B, 0);
                SpecializedOps_R64.copyTriangle(A,B,false);
                assertTrue(MatrixFeatures_R64.isEqualsTriangle(A,B, false, UtilEjml.TEST_F64));
                assertFalse(MatrixFeatures_R64.isEquals(A,B,UtilEjml.TEST_F64));
            }
        }
    }

    @Test
    public void diffNormF() {
        DMatrixRow_F64 a = RandomMatrices_R64.createRandom(3,2,rand);
        DMatrixRow_F64 b = RandomMatrices_R64.createRandom(3,2,rand);
        DMatrixRow_F64 c = RandomMatrices_R64.createRandom(3,2,rand);

        CommonOps_R64.subtract(a, b, c);
        double expectedNorm = NormOps_R64.fastNormF(c);
        double foundNorm = SpecializedOps_R64.diffNormF(a,b);

        assertEquals(expectedNorm,foundNorm,UtilEjml.TEST_F64);
    }

    @Test
    public void diffNormP1() {
        DMatrixRow_F64 a = RandomMatrices_R64.createRandom(3,2,rand);
        DMatrixRow_F64 b = RandomMatrices_R64.createRandom(3,2,rand);
        DMatrixRow_F64 c = RandomMatrices_R64.createRandom(3,2,rand);

        CommonOps_R64.subtract(a, b, c);
        double expectedNorm = 0;
        for( int i = 0; i < c.getNumElements(); i++ ) {
            expectedNorm += Math.abs(c.get(i));
        }
        double foundNorm = SpecializedOps_R64.diffNormP1(a,b);

        assertEquals(expectedNorm,foundNorm, UtilEjml.TEST_F64);
    }

    @Test
    public void addIdentity() {
        DMatrixRow_F64 M = RandomMatrices_R64.createRandom(4,4,rand);
        DMatrixRow_F64 A = M.copy();

        SpecializedOps_R64.addIdentity(A,A,2.0);
        CommonOps_R64.subtractEquals(A, M);

        double total = CommonOps_R64.elementSum(A);

        assertEquals(4*2,total,UtilEjml.TEST_F64);
    }

    @Test
    public void subvector() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(4,5,rand);

        DMatrixRow_F64 v = new DMatrixRow_F64(7,1);

        // first extract a row vector
        SpecializedOps_R64.subvector(A,2,1,2,true,1,v);

        assertEquals(0,v.get(0),UtilEjml.TEST_F64);

        for( int i = 0; i < 2; i++ ) {
            assertEquals(A.get(2,1+i),v.get(1+i),UtilEjml.TEST_F64);
        }

        // now extract a column vector
        SpecializedOps_R64.subvector(A,2,1,2,false,1,v);

        for( int i = 0; i < 2; i++ ) {
            assertEquals(A.get(2+i,1),v.get(1+i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void splitIntoVectors() {

        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(3,5,rand);

        // column vectors
        DMatrixRow_F64 v[] = SpecializedOps_R64.splitIntoVectors(A,true);

        assertEquals(5,v.length);
        for( int i = 0; i < v.length; i++ ) {
            DMatrixRow_F64 a = v[i];

            assertEquals(3,a.getNumRows());
            assertEquals(1,a.getNumCols());

            for( int j = 0; j < A.numRows; j++ ) {
                assertEquals(A.get(j,i),a.get(j),UtilEjml.TEST_F64);
            }
        }

        // row vectors
        v = SpecializedOps_R64.splitIntoVectors(A,false);

        assertEquals(3,v.length);
        for( int i = 0; i < v.length; i++ ) {
            DMatrixRow_F64 a = v[i];

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

        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(4,4,rand);
        DMatrixRow_F64 P = SpecializedOps_R64.pivotMatrix(null,pivots,4,false);
        DMatrixRow_F64 Pt = SpecializedOps_R64.pivotMatrix(null,pivots,4,true);

        DMatrixRow_F64 B = new DMatrixRow_F64(4,4);

        // see if it swapped the rows
        CommonOps_R64.mult(P,A,B);

        for( int i = 0; i < 4; i++ ) {
            int index = pivots[i];
            for( int j = 0; j < 4; j++ ) {
                double val = A.get(index,j);
                assertEquals(val,B.get(i,j),UtilEjml.TEST_F64);
            }
        }

        // see if it transposed
        CommonOps_R64.transpose(P,B);

        assertTrue(MatrixFeatures_R64.isIdentical(B,Pt,UtilEjml.TEST_F64));
    }

    @Test
    public void diagProd() {
        DMatrixRow_F64 A = new DMatrixRow_F64(3,3);

        A.set(0,0,1);
        A.set(1,1,2);
        A.set(2,2,3);

        double found = SpecializedOps_R64.diagProd(A);

        assertEquals(6,found,UtilEjml.TEST_F64);
    }

    @Test
    public void elementDiagonalMaxAbs() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(4,5,rand);

        double expected = 0;
        for (int i = 0; i < 4; i++) {
            double a = A.get(i,i);
            if( Math.abs(a) > expected )
                expected = Math.abs(a);
        }

        double found = SpecializedOps_R64.elementDiagonalMaxAbs(A);
        assertEquals(expected,found,UtilEjml.TEST_F64);
    }

    @Test
    public void qualityTriangular() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(4,4,rand);

        double max = SpecializedOps_R64.elementDiagonalMaxAbs(A);
        double expected = 1.0;
        for (int i = 0; i < 4; i++) {
            expected *= A.get(i,i)/max;
        }

        double found = SpecializedOps_R64.qualityTriangular(A);
        assertEquals(expected,found,UtilEjml.TEST_F64);
    }
    
    @Test
    public void elementSumSq() {
        DMatrixRow_F64 A = new DMatrixRow_F64(2,3,true,1,2,3,4,5,6);
        
        double expected = 1+4+9+16+25+36;
        double found = SpecializedOps_R64.elementSumSq(A);
        
        assertEquals(expected,found,UtilEjml.TEST_F64);
    }
}
