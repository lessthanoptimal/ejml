/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

import org.ejml.alg.dense.mult.VectorVectorMult;
import org.ejml.data.DenseMatrix64F;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestSpecializedOps {
    Random rand = new Random(7476547);

    @Test
    public void createReflector() {
        DenseMatrix64F u = RandomMatrices.createRandom(4,1,rand);

        DenseMatrix64F Q = SpecializedOps.createReflector(u);

        assertTrue(MatrixFeatures.isOrthogonal(Q,1e-8));

        DenseMatrix64F w = new DenseMatrix64F(4,1);

        CommonOps.mult(Q,u,w);

        assertTrue(MatrixFeatures.isNegative(u,w,1e-8));
    }

    @Test
    public void createReflector_gamma() {
        DenseMatrix64F u = RandomMatrices.createRandom(4,1,rand);
        double gamma = 1.5;
        DenseMatrix64F Q = SpecializedOps.createReflector(u,gamma);

        DenseMatrix64F w = RandomMatrices.createRandom(4,1,rand);

        DenseMatrix64F v_found = new DenseMatrix64F(4,1);
        CommonOps.mult(Q,w,v_found);

        DenseMatrix64F v_exp = new DenseMatrix64F(4,1);

        VectorVectorMult.householder(-gamma,u,w,v_exp);

        assertTrue(MatrixFeatures.isIdentical(v_found,v_exp,1e-8));
    }

    @Test
    public void copyChangeRow() {
        DenseMatrix64F A = RandomMatrices.createRandom(3,2,rand);

        DenseMatrix64F B = A.copy();

        int []order = new int[]{2,0,1};

        DenseMatrix64F C = SpecializedOps.copyChangeRow(order,A,null);

        // make sure it didn't modify A
        assertTrue(MatrixFeatures.isIdentical(A,B,0));

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
                DenseMatrix64F A = RandomMatrices.createRandom(m,n,rand);

                DenseMatrix64F B = SpecializedOps.copyTriangle(A,null,true);

                assertTrue(MatrixFeatures.isEqualsTriangle(A,B, true, 1e-8));
                assertFalse(MatrixFeatures.isEquals(A,B,1e-8));

                CommonOps.fill(B, 0);
                SpecializedOps.copyTriangle(A,B,false);
                assertTrue(MatrixFeatures.isEqualsTriangle(A,B, false, 1e-8));
                assertFalse(MatrixFeatures.isEquals(A,B,1e-8));
            }
        }
    }

    @Test
    public void diffNormF() {
        DenseMatrix64F a = RandomMatrices.createRandom(3,2,rand);
        DenseMatrix64F b = RandomMatrices.createRandom(3,2,rand);
        DenseMatrix64F c = RandomMatrices.createRandom(3,2,rand);

        CommonOps.subtract(a, b, c);
        double expectedNorm = NormOps.fastNormF(c);
        double foundNorm = SpecializedOps.diffNormF(a,b);

        assertEquals(expectedNorm,foundNorm,1e-8);
    }

    @Test
    public void diffNormP1() {
        DenseMatrix64F a = RandomMatrices.createRandom(3,2,rand);
        DenseMatrix64F b = RandomMatrices.createRandom(3,2,rand);
        DenseMatrix64F c = RandomMatrices.createRandom(3,2,rand);

        CommonOps.subtract(a, b, c);
        double expectedNorm = 0;
        for( int i = 0; i < c.getNumElements(); i++ ) {
            expectedNorm += Math.abs(c.get(i));
        }
        double foundNorm = SpecializedOps.diffNormP1(a,b);

        assertEquals(expectedNorm,foundNorm,1e-8);
    }

    @Test
    public void addIdentity() {
        DenseMatrix64F M = RandomMatrices.createRandom(4,4,rand);
        DenseMatrix64F A = M.copy();

        SpecializedOps.addIdentity(A,A,2.0);
        CommonOps.subtractEquals(A, M);

        double total = CommonOps.elementSum(A);

        assertEquals(4*2,total,1e-8);
    }

    @Test
    public void subvector() {
        DenseMatrix64F A = RandomMatrices.createRandom(4,5,rand);

        DenseMatrix64F v = new DenseMatrix64F(7,1);

        // first extract a row vector
        SpecializedOps.subvector(A,2,1,2,true,1,v);

        assertEquals(0,v.get(0),1e-8);

        for( int i = 0; i < 2; i++ ) {
            assertEquals(A.get(2,1+i),v.get(1+i),1e-8);
        }

        // now extract a column vector
        SpecializedOps.subvector(A,2,1,2,false,1,v);

        for( int i = 0; i < 2; i++ ) {
            assertEquals(A.get(2+i,1),v.get(1+i),1e-8);
        }
    }

    @Test
    public void splitIntoVectors() {

        DenseMatrix64F A = RandomMatrices.createRandom(3,5,rand);

        // column vectors
        DenseMatrix64F v[] = SpecializedOps.splitIntoVectors(A,true);

        assertEquals(5,v.length);
        for( int i = 0; i < v.length; i++ ) {
            DenseMatrix64F a = v[i];

            assertEquals(3,a.getNumRows());
            assertEquals(1,a.getNumCols());

            for( int j = 0; j < A.numRows; j++ ) {
                assertEquals(A.get(j,i),a.get(j),1e-8);
            }
        }

        // row vectors
        v = SpecializedOps.splitIntoVectors(A,false);

        assertEquals(3,v.length);
        for( int i = 0; i < v.length; i++ ) {
            DenseMatrix64F a = v[i];

            assertEquals(1,a.getNumRows());
            assertEquals(5,a.getNumCols());

            for( int j = 0; j < A.numCols; j++ ) {
                assertEquals(A.get(i,j),a.get(j),1e-8);
            }
        }

    }

    @Test
    public void pivotMatrix() {
        int pivots[] = new int[]{1,0,3,2};

        DenseMatrix64F A = RandomMatrices.createRandom(4,4,rand);
        DenseMatrix64F P = SpecializedOps.pivotMatrix(null,pivots,4,false);
        DenseMatrix64F Pt = SpecializedOps.pivotMatrix(null,pivots,4,true);

        DenseMatrix64F B = new DenseMatrix64F(4,4);

        // see if it swapped the rows
        CommonOps.mult(P,A,B);

        for( int i = 0; i < 4; i++ ) {
            int index = pivots[i];
            for( int j = 0; j < 4; j++ ) {
                double val = A.get(index,j);
                assertEquals(val,B.get(i,j),1e-8);
            }
        }

        // see if it transposed
        CommonOps.transpose(P,B);

        assertTrue(MatrixFeatures.isIdentical(B,Pt,1e-8));
    }

    @Test
    public void diagProd() {
        DenseMatrix64F A = new DenseMatrix64F(3,3);

        A.set(0,0,1);
        A.set(1,1,2);
        A.set(2,2,3);

        double found = SpecializedOps.diagProd(A);

        assertEquals(6,found,1e-8);
    }

    @Test
    public void elementDiagonalMaxAbs() {
        DenseMatrix64F A = RandomMatrices.createRandom(4,5,rand);

        double expected = 0;
        for (int i = 0; i < 4; i++) {
            double a = A.get(i,i);
            if( Math.abs(a) > expected )
                expected = Math.abs(a);
        }

        double found = SpecializedOps.elementDiagonalMaxAbs(A);
        assertEquals(expected,found,1e-8);
    }

    @Test
    public void qualityTriangular() {
        DenseMatrix64F A = RandomMatrices.createRandom(4,4,rand);

        double max = SpecializedOps.elementDiagonalMaxAbs(A);
        double expected = 1.0;
        for (int i = 0; i < 4; i++) {
            expected *= A.get(i,i)/max;
        }

        double found = SpecializedOps.qualityTriangular(A);
        assertEquals(expected,found,1e-8);
    }
    
    @Test
    public void elementSumSq() {
        DenseMatrix64F A = new DenseMatrix64F(2,3,true,1,2,3,4,5,6);
        
        double expected = 1+4+9+16+25+36;
        double found = SpecializedOps.elementSumSq(A);
        
        assertEquals(expected,found,1e-8);
    }
}
