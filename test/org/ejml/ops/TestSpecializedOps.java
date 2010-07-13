/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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
    public void diffNormF() {
        DenseMatrix64F a = RandomMatrices.createRandom(3,2,rand);
        DenseMatrix64F b = RandomMatrices.createRandom(3,2,rand);
        DenseMatrix64F c = RandomMatrices.createRandom(3,2,rand);

        CommonOps.sub(a,b,c);
        double expectedNorm = NormOps.fastNormF(c);
        double foundNorm = SpecializedOps.diffNormF(a,b);

        assertEquals(expectedNorm,foundNorm,1e-8);
    }

    @Test
    public void diffNormP1() {
        DenseMatrix64F a = RandomMatrices.createRandom(3,2,rand);
        DenseMatrix64F b = RandomMatrices.createRandom(3,2,rand);
        DenseMatrix64F c = RandomMatrices.createRandom(3,2,rand);

        CommonOps.sub(a,b,c);
        double expectedNorm = 0;
        for( int i = 0; i < c.data.length; i++ ) {
            expectedNorm += Math.abs(c.data[i]);
        }
        double foundNorm = SpecializedOps.diffNormP1(a,b);

        assertEquals(expectedNorm,foundNorm,1e-8);
    }

    @Test
    public void addIdentity() {
        DenseMatrix64F M = RandomMatrices.createRandom(4,4,rand);
        DenseMatrix64F A = M.copy();

        SpecializedOps.addIdentity(A,A,2.0);
        CommonOps.subEquals(A,M);

        double total = CommonOps.elementSum(A);

        assertEquals(4*2,total,1e-8);
    }

    @Test
    public void submatrix() {
        DenseMatrix64F A = RandomMatrices.createRandom(6,7,rand);

        DenseMatrix64F B = new DenseMatrix64F(2,3);

        SpecializedOps.extract(A,2,3,3,6,B);

        for( int i = 2; i <= 3; i++ ) {
            for( int j = 3; j <= 6; j++ ) {
                assertEquals(A.get(i,j),B.get(i-2,j-3),1e-8);
            }
        }
    }

    @Test
    public void subvector() {
        DenseMatrix64F A = RandomMatrices.createRandom(4,5,rand);

        DenseMatrix64F v = new DenseMatrix64F(7,1);

        // first extract a row vector
        SpecializedOps.subvector(A,2,1,2,true,1,v);

        assertEquals(0,v.data[0],1e-8);

        for( int i = 0; i < 2; i++ ) {
            assertEquals(A.get(2,1+i),v.data[1+i],1e-8);
        }

        // now extract a column vector
        SpecializedOps.subvector(A,2,1,2,false,1,v);

        for( int i = 0; i < 2; i++ ) {
            assertEquals(A.get(2+i,1),v.data[1+i],1e-8);
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
                assertEquals(A.get(j,i),a.data[j],1e-8);
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
                assertEquals(A.get(i,j),a.data[j],1e-8);
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
    public void extract() {
        DenseMatrix64F A = new DenseMatrix64F(5,5);
        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                A.set(i,j,i*A.numRows+j);
            }
        }

        DenseMatrix64F B = new DenseMatrix64F(2,3);

        SpecializedOps.extract(A,1,2,2,4,B);

        for( int i = 1; i < 3; i++ ) {
            for( int j = 2; j < 5; j++ ) {
                assertEquals(A.get(i,j),B.get(i-1,j-2),1e-8);
            }
        }
    }

    @Test
    public void extractDiag() {
        fail("Implement");
    }

    @Test
    public void insert() {
        DenseMatrix64F A = new DenseMatrix64F(5,5);
        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                A.set(i,j,i*A.numRows+j);
            }
        }

        DenseMatrix64F B = new DenseMatrix64F(8,8);

        SpecializedOps.insert(A,1,2,B);

        for( int i = 1; i < 6; i++ ) {
            for( int j = 2; j < 7; j++ ) {
                assertEquals(A.get(i-1,j-2),B.get(i,j),1e-8);
            }
        }
    }
}
