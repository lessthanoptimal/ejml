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

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestMatrixFeatures_CD64 {

    private Random rand = new Random(234);

    @Test
    public void isVector() {
        CDenseMatrix64F a = new CDenseMatrix64F(4,4);

        assertFalse(MatrixFeatures_CD64.isVector(a));

        a.reshape(3, 1);
        assertTrue(MatrixFeatures_CD64.isVector(a));

        a.reshape(1, 3);
        assertTrue(MatrixFeatures_CD64.isVector(a));
    }

    @Test
    public void isNegative() {
        CDenseMatrix64F a = RandomMatrices_CD64.createRandom(4,5,rand);
        CDenseMatrix64F b = a.copy();
        CommonOps_CD64.scale(-1,0,b);

        // test the positive case first
        assertTrue(MatrixFeatures_CD64.isNegative(a,b,UtilEjml.TEST_64F));

        // now the negative case
        b.set(2,2,10,0);
        assertFalse(MatrixFeatures_CD64.isNegative(a,b,UtilEjml.TEST_64F));

        b.set(2,2,Double.NaN,0);
        assertFalse(MatrixFeatures_CD64.isNegative(a,b,UtilEjml.TEST_64F));
    }

    @Test
    public void hasUncountable() {
        CDenseMatrix64F a = new CDenseMatrix64F(4,4);

        // check a negative case first
        assertFalse(MatrixFeatures_CD64.hasUncountable(a));

        // check two positve cases with different types of uncountables
        a.set(2,2,Double.NaN,0);
        assertTrue(MatrixFeatures_CD64.hasUncountable(a));

        a.set(2,2,Double.POSITIVE_INFINITY,0);
        assertTrue(MatrixFeatures_CD64.hasUncountable(a));
    }

    @Test
    public void hasNaN() {
        CDenseMatrix64F m = new CDenseMatrix64F(3,3);
        assertFalse(MatrixFeatures_CD64.hasNaN(m));

        m.set(1,2,-Double.NaN,0);
        assertTrue(MatrixFeatures_CD64.hasNaN(m));
    }

    @Test
    public void isEquals() {
        CDenseMatrix64F m = RandomMatrices_CD64.createRandom(3,4,-1,1,rand);
        CDenseMatrix64F n = m.copy();

        assertTrue(MatrixFeatures_CD64.isEquals(m,n));

        n.set(2,1,-0.5,-0.6);
        assertFalse(MatrixFeatures_CD64.isEquals(m,n));

        m.set(2,1,Double.NaN,1);
        n.set(2,1,Double.NaN,1);
        assertFalse(MatrixFeatures_CD64.isEquals(m,n));
        m.set(2,1,Double.POSITIVE_INFINITY,1);
        n.set(2,1,Double.POSITIVE_INFINITY,1);
        assertTrue(MatrixFeatures_CD64.isEquals(m,n));
    }

    @Test
    public void isEquals_tol() {
        CDenseMatrix64F m = RandomMatrices_CD64.createRandom(3,4,-1,1,rand);
        CDenseMatrix64F n = m.copy();

        assertTrue(MatrixFeatures_CD64.isEquals(m,n,UtilEjml.TEST_64F));

        n.data[4] += UtilEjml.EPS;
        assertTrue(MatrixFeatures_CD64.isEquals(m,n,UtilEjml.TEST_64F));

        n.data[4] += Math.sqrt(UtilEjml.TEST_64F_SQ);
        assertFalse(MatrixFeatures_CD64.isEquals(m,n,UtilEjml.TEST_64F));

        m.set(2,1,Double.NaN,1);
        n.set(2,1,Double.NaN,1);
        assertFalse(MatrixFeatures_CD64.isEquals(m,n,UtilEjml.TEST_64F));
        m.set(2,1,Double.POSITIVE_INFINITY,1);
        n.set(2,1,Double.POSITIVE_INFINITY,1);
        assertFalse(MatrixFeatures_CD64.isEquals(m,n,UtilEjml.TEST_64F));
    }

    @Test
    public void isIdentical() {

        double values[] = new double[]{1.0,Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY};

        for( int i = 0; i < values.length; i++ ) {
            for( int j = 0; j < values.length; j++ ) {
                checkIdentical(values[i],values[j],UtilEjml.TEST_64F,i==j);
            }
        }

        checkIdentical(1.0,1.5,UtilEjml.TEST_64F,false);
        checkIdentical(1.5,1.0,UtilEjml.TEST_64F,false);
        checkIdentical(1.0,1.0000000001,UtilEjml.TEST_64F,true);
        checkIdentical(1.0,Double.NaN,UtilEjml.TEST_64F,false);
        checkIdentical(Double.NaN,1.0,UtilEjml.TEST_64F,false);
    }

    private void checkIdentical( double valA , double valB , double tol , boolean expected ) {
        CDenseMatrix64F A = new CDenseMatrix64F(2,2);
        CDenseMatrix64F B = new CDenseMatrix64F(2,2);
        CommonOps_CD64.fill(A, valA,0);
        CommonOps_CD64.fill(B, valB,0);

        assertEquals(expected, MatrixFeatures_CD64.isIdentical(A,B,tol));

        CommonOps_CD64.fill(A, 0,valA);
        CommonOps_CD64.fill(B, 0,valB);

        assertEquals(expected, MatrixFeatures_CD64.isIdentical(A,B,tol));
    }

    @Test
    public void isIdentity() {
        CDenseMatrix64F m = CommonOps_CD64.diag(1,0,1,0,1,0);

        assertTrue(MatrixFeatures_CD64.isIdentity(m,UtilEjml.TEST_64F));

        m.setImag(0,0,10*UtilEjml.EPS);
        assertTrue(MatrixFeatures_CD64.isIdentity(m, UtilEjml.TEST_64F));
        m.setReal(0, 0, 1 + 10*UtilEjml.EPS);
        assertTrue(MatrixFeatures_CD64.isIdentity(m,UtilEjml.TEST_64F));

        assertFalse(MatrixFeatures_CD64.isIdentity(m, UtilEjml.EPS));
        assertFalse(MatrixFeatures_CD64.isIdentity(m, UtilEjml.EPS));

        m.setImag(1,0,10*UtilEjml.EPS);
        assertTrue(MatrixFeatures_CD64.isIdentity(m,UtilEjml.TEST_64F));
        m.setReal(1,0,10*UtilEjml.EPS);
        assertTrue(MatrixFeatures_CD64.isIdentity(m,UtilEjml.TEST_64F));

        assertFalse(MatrixFeatures_CD64.isIdentity(m,UtilEjml.EPS));
        assertFalse(MatrixFeatures_CD64.isIdentity(m,UtilEjml.EPS));
    }

    @Test
    public void isHermitian() {
        CDenseMatrix64F A = new CDenseMatrix64F(new double[][]{{1,0, 2,2.1},{2,-2.1 ,3,0}});

        assertTrue(MatrixFeatures_CD64.isHermitian(A, UtilEjml.TEST_64F));

        A.set(0,1,5,6);

        assertFalse(MatrixFeatures_CD64.isHermitian(A, UtilEjml.TEST_64F));
    }

    @Test
    public void isUnitary() {
        // create a reflector since it's unitary
        CDenseMatrix64F u = RandomMatrices_CD64.createRandom(5,1,rand);
        Complex64F dot = new Complex64F();
        VectorVectorMult_CD64.innerProdH(u, u,dot);
        double gamma = 2.0/dot.real;
        CDenseMatrix64F A = SpecializedOps_CD64.householder(u,gamma);

        assertTrue(MatrixFeatures_CD64.isUnitary(A, UtilEjml.TEST_64F));

        // try a negative case now
        A.set(0,1,495,400);

        assertFalse(MatrixFeatures_CD64.isUnitary(A, UtilEjml.TEST_64F));

        A.set(0,1,Double.NaN,Double.NaN);

        assertFalse(MatrixFeatures_CD64.isUnitary(A, UtilEjml.TEST_64F));
    }

    /**
     * Check some trial cases.
     */
    @Test
    public void isPositiveDefinite() {
        CDenseMatrix64F a = new CDenseMatrix64F(2,2,true,2,0,0,0,0,0,2,0);
        CDenseMatrix64F b = new CDenseMatrix64F(2,2,true,0,0,1,0,1,0,0,0);
        CDenseMatrix64F c = new CDenseMatrix64F(2,2);

        assertTrue(MatrixFeatures_CD64.isPositiveDefinite(a));
        assertFalse(MatrixFeatures_CD64.isPositiveDefinite(b));
        assertFalse(MatrixFeatures_CD64.isPositiveDefinite(c));

        // make sure the input isn't modified
        assertEquals(2,a.getReal(0, 0),UtilEjml.TEST_64F);
        assertEquals(2,a.getReal(1,1),UtilEjml.TEST_64F);
    }

    @Test
    public void isUpperTriangle() {
        // test matrices that are upper triangular to various degree hessenberg
        for( int hessenberg = 0; hessenberg < 2; hessenberg++ ) {
            CDenseMatrix64F A = new CDenseMatrix64F(6,6);
            for( int i = 0; i < A.numRows; i++ ) {
                int s = i <= hessenberg ? 0 : i-hessenberg;

                for( int j = s; j < A.numCols; j++ ) {
                   A.set(i,j,2,2);
                }
            }

            // test positive
            for( int i = hessenberg; i < A.numRows; i++ ) {
                assertTrue(MatrixFeatures_CD64.isUpperTriangle(A,i,UtilEjml.TEST_64F));
            }

            // test negative
            for( int i = 0; i < hessenberg; i++ ) {
                assertFalse(MatrixFeatures_CD64.isUpperTriangle(A,i, UtilEjml.TEST_64F));
            }

            // see if it handles NaN well
            A.set(4,0,Double.NaN,Double.NaN);
            assertFalse(MatrixFeatures_CD64.isUpperTriangle(A,0,UtilEjml.TEST_64F));
        }
    }

    @Test
    public void isLowerTriangle() {
        // test matrices that are upper triangular to various degree hessenberg
        for( int hessenberg = 0; hessenberg < 2; hessenberg++ ) {
            CDenseMatrix64F A = new CDenseMatrix64F(6,6);
            for( int i = 0; i < A.numRows; i++ ) {
                int s = i <= hessenberg ? 0 : i-hessenberg;

                for( int j = s; j < A.numCols; j++ ) {
                   A.set(i,j,2,2);
                }
            }
            CommonOps_CD64.transpose(A);

            // test positive
            for( int i = hessenberg; i < A.numRows; i++ ) {
                assertTrue(MatrixFeatures_CD64.isLowerTriangle(A,i,UtilEjml.TEST_64F));
            }

            // test negative
            for( int i = 0; i < hessenberg; i++ ) {
                assertFalse(MatrixFeatures_CD64.isLowerTriangle(A,i,UtilEjml.TEST_64F));
            }

            // see if it handles NaN well
            A.set(0,4,Double.NaN,Double.NaN);
            assertFalse(MatrixFeatures_CD64.isLowerTriangle(A,0,UtilEjml.TEST_64F));
        }
    }


    @Test
    public void isZeros() {
        CDenseMatrix64F a = new CDenseMatrix64F(4,4);
        a.set(0, 0, 1, -1);

        assertFalse(MatrixFeatures_CD64.isZeros(a, 0.1));
        assertTrue(MatrixFeatures_CD64.isZeros(a, 2));
    }
}
