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
import org.ejml.data.Complex_F64;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.mult.VectorVectorMult_ZDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestMatrixFeatures_ZDRM {

    private Random rand = new Random(234);

    @Test
    public void isVector() {
        ZMatrixRMaj a = new ZMatrixRMaj(4,4);

        assertFalse(MatrixFeatures_ZDRM.isVector(a));

        a.reshape(3, 1);
        assertTrue(MatrixFeatures_ZDRM.isVector(a));

        a.reshape(1, 3);
        assertTrue(MatrixFeatures_ZDRM.isVector(a));
    }

    @Test
    public void isNegative() {
        ZMatrixRMaj a = RandomMatrices_ZDRM.rectangle(4,5,rand);
        ZMatrixRMaj b = a.copy();
        CommonOps_ZDRM.scale(-1,0,b);

        // test the positive case first
        assertTrue(MatrixFeatures_ZDRM.isNegative(a,b,UtilEjml.TEST_F64));

        // now the negative case
        b.set(2,2,10,0);
        assertFalse(MatrixFeatures_ZDRM.isNegative(a,b,UtilEjml.TEST_F64));

        b.set(2,2,Double.NaN,0);
        assertFalse(MatrixFeatures_ZDRM.isNegative(a,b,UtilEjml.TEST_F64));
    }

    @Test
    public void hasUncountable() {
        ZMatrixRMaj a = new ZMatrixRMaj(4,4);

        // check a negative case first
        assertFalse(MatrixFeatures_ZDRM.hasUncountable(a));

        // check two positve cases with different types of uncountables
        a.set(2,2,Double.NaN,0);
        assertTrue(MatrixFeatures_ZDRM.hasUncountable(a));

        a.set(2,2,Double.POSITIVE_INFINITY,0);
        assertTrue(MatrixFeatures_ZDRM.hasUncountable(a));
    }

    @Test
    public void hasNaN() {
        ZMatrixRMaj m = new ZMatrixRMaj(3,3);
        assertFalse(MatrixFeatures_ZDRM.hasNaN(m));

        m.set(1,2,-Double.NaN,0);
        assertTrue(MatrixFeatures_ZDRM.hasNaN(m));
    }

    @Test
    public void isEquals() {
        ZMatrixRMaj m = RandomMatrices_ZDRM.rectangle(3,4,-1,1,rand);
        ZMatrixRMaj n = m.copy();

        assertTrue(MatrixFeatures_ZDRM.isEquals(m,n));

        n.set(2,1,-0.5,-0.6);
        assertFalse(MatrixFeatures_ZDRM.isEquals(m,n));

        m.set(2,1,Double.NaN,1);
        n.set(2,1,Double.NaN,1);
        assertFalse(MatrixFeatures_ZDRM.isEquals(m,n));
        m.set(2,1,Double.POSITIVE_INFINITY,1);
        n.set(2,1,Double.POSITIVE_INFINITY,1);
        assertTrue(MatrixFeatures_ZDRM.isEquals(m,n));
    }

    @Test
    public void isEquals_tol() {
        ZMatrixRMaj m = RandomMatrices_ZDRM.rectangle(3,4,-1,1,rand);
        ZMatrixRMaj n = m.copy();

        assertTrue(MatrixFeatures_ZDRM.isEquals(m,n,UtilEjml.TEST_F64));

        n.data[4] += UtilEjml.EPS;
        assertTrue(MatrixFeatures_ZDRM.isEquals(m,n,UtilEjml.TEST_F64));

        n.data[4] += Math.sqrt(UtilEjml.TEST_F64_SQ);
        assertFalse(MatrixFeatures_ZDRM.isEquals(m,n,UtilEjml.TEST_F64));

        m.set(2,1,Double.NaN,1);
        n.set(2,1,Double.NaN,1);
        assertFalse(MatrixFeatures_ZDRM.isEquals(m,n,UtilEjml.TEST_F64));
        m.set(2,1,Double.POSITIVE_INFINITY,1);
        n.set(2,1,Double.POSITIVE_INFINITY,1);
        assertFalse(MatrixFeatures_ZDRM.isEquals(m,n,UtilEjml.TEST_F64));
    }

    @Test
    public void isIdentical() {

        double values[] = new double[]{1.0,Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY};

        for( int i = 0; i < values.length; i++ ) {
            for( int j = 0; j < values.length; j++ ) {
                checkIdentical(values[i],values[j],UtilEjml.TEST_F64,i==j);
            }
        }

        checkIdentical(1.0,1.5,UtilEjml.TEST_F64,false);
        checkIdentical(1.5,1.0,UtilEjml.TEST_F64,false);
        checkIdentical(1.0,1.0000000001,UtilEjml.TEST_F64,true);
        checkIdentical(1.0,Double.NaN,UtilEjml.TEST_F64,false);
        checkIdentical(Double.NaN,1.0,UtilEjml.TEST_F64,false);
    }

    private void checkIdentical( double valA , double valB , double tol , boolean expected ) {
        ZMatrixRMaj A = new ZMatrixRMaj(2,2);
        ZMatrixRMaj B = new ZMatrixRMaj(2,2);
        CommonOps_ZDRM.fill(A, valA,0);
        CommonOps_ZDRM.fill(B, valB,0);

        assertEquals(expected, MatrixFeatures_ZDRM.isIdentical(A,B,tol));

        CommonOps_ZDRM.fill(A, 0,valA);
        CommonOps_ZDRM.fill(B, 0,valB);

        assertEquals(expected, MatrixFeatures_ZDRM.isIdentical(A,B,tol));
    }

    @Test
    public void isIdentity() {
        ZMatrixRMaj m = CommonOps_ZDRM.diag(1,0,1,0,1,0);

        assertTrue(MatrixFeatures_ZDRM.isIdentity(m,UtilEjml.TEST_F64));

        m.setImag(0,0,10*UtilEjml.EPS);
        assertTrue(MatrixFeatures_ZDRM.isIdentity(m, UtilEjml.TEST_F64));
        m.setReal(0, 0, 1 + 10*UtilEjml.EPS);
        assertTrue(MatrixFeatures_ZDRM.isIdentity(m,UtilEjml.TEST_F64));

        assertFalse(MatrixFeatures_ZDRM.isIdentity(m, UtilEjml.EPS));
        assertFalse(MatrixFeatures_ZDRM.isIdentity(m, UtilEjml.EPS));

        m.setImag(1,0,10*UtilEjml.EPS);
        assertTrue(MatrixFeatures_ZDRM.isIdentity(m,UtilEjml.TEST_F64));
        m.setReal(1,0,10*UtilEjml.EPS);
        assertTrue(MatrixFeatures_ZDRM.isIdentity(m,UtilEjml.TEST_F64));

        assertFalse(MatrixFeatures_ZDRM.isIdentity(m,UtilEjml.EPS));
        assertFalse(MatrixFeatures_ZDRM.isIdentity(m,UtilEjml.EPS));
    }

    @Test
    public void isHermitian() {
        ZMatrixRMaj A = new ZMatrixRMaj(new double[][]{{1,0, 2,2.1},{2,-2.1 ,3,0}});

        assertTrue(MatrixFeatures_ZDRM.isHermitian(A, UtilEjml.TEST_F64));

        A.set(0,1,5,6);

        assertFalse(MatrixFeatures_ZDRM.isHermitian(A, UtilEjml.TEST_F64));
    }

    @Test
    public void isUnitary() {
        // create a reflector since it's unitary
        ZMatrixRMaj u = RandomMatrices_ZDRM.rectangle(5,1,rand);
        Complex_F64 dot = new Complex_F64();
        VectorVectorMult_ZDRM.innerProdH(u, u,dot);
        double gamma = 2.0/dot.real;
        ZMatrixRMaj A = SpecializedOps_ZDRM.householder(u,gamma);

        assertTrue(MatrixFeatures_ZDRM.isUnitary(A, UtilEjml.TEST_F64));

        // try a negative case now
        A.set(0,1,495,400);

        assertFalse(MatrixFeatures_ZDRM.isUnitary(A, UtilEjml.TEST_F64));

        A.set(0,1,Double.NaN,Double.NaN);

        assertFalse(MatrixFeatures_ZDRM.isUnitary(A, UtilEjml.TEST_F64));
    }

    /**
     * Check some trial cases.
     */
    @Test
    public void isPositiveDefinite() {
        ZMatrixRMaj a = new ZMatrixRMaj(2,2,true,2,0,0,0,0,0,2,0);
        ZMatrixRMaj b = new ZMatrixRMaj(2,2,true,0,0,1,0,1,0,0,0);
        ZMatrixRMaj c = new ZMatrixRMaj(2,2);

        assertTrue(MatrixFeatures_ZDRM.isPositiveDefinite(a));
        assertFalse(MatrixFeatures_ZDRM.isPositiveDefinite(b));
        assertFalse(MatrixFeatures_ZDRM.isPositiveDefinite(c));

        // make sure the input isn't modified
        assertEquals(2,a.getReal(0, 0),UtilEjml.TEST_F64);
        assertEquals(2,a.getReal(1,1),UtilEjml.TEST_F64);
    }

    @Test
    public void isUpperTriangle() {
        // test matrices that are upper triangular to various degree hessenberg
        for( int hessenberg = 0; hessenberg < 2; hessenberg++ ) {
            ZMatrixRMaj A = new ZMatrixRMaj(6,6);
            for( int i = 0; i < A.numRows; i++ ) {
                int s = i <= hessenberg ? 0 : i-hessenberg;

                for( int j = s; j < A.numCols; j++ ) {
                   A.set(i,j,2,2);
                }
            }

            // test positive
            for( int i = hessenberg; i < A.numRows; i++ ) {
                assertTrue(MatrixFeatures_ZDRM.isUpperTriangle(A,i,UtilEjml.TEST_F64));
            }

            // test negative
            for( int i = 0; i < hessenberg; i++ ) {
                assertFalse(MatrixFeatures_ZDRM.isUpperTriangle(A,i, UtilEjml.TEST_F64));
            }

            // see if it handles NaN well
            A.set(4,0,Double.NaN,Double.NaN);
            assertFalse(MatrixFeatures_ZDRM.isUpperTriangle(A,0,UtilEjml.TEST_F64));
        }
    }

    @Test
    public void isLowerTriangle() {
        // test matrices that are upper triangular to various degree hessenberg
        for( int hessenberg = 0; hessenberg < 2; hessenberg++ ) {
            ZMatrixRMaj A = new ZMatrixRMaj(6,6);
            for( int i = 0; i < A.numRows; i++ ) {
                int s = i <= hessenberg ? 0 : i-hessenberg;

                for( int j = s; j < A.numCols; j++ ) {
                   A.set(i,j,2,2);
                }
            }
            CommonOps_ZDRM.transpose(A);

            // test positive
            for( int i = hessenberg; i < A.numRows; i++ ) {
                assertTrue(MatrixFeatures_ZDRM.isLowerTriangle(A,i,UtilEjml.TEST_F64));
            }

            // test negative
            for( int i = 0; i < hessenberg; i++ ) {
                assertFalse(MatrixFeatures_ZDRM.isLowerTriangle(A,i,UtilEjml.TEST_F64));
            }

            // see if it handles NaN well
            A.set(0,4,Double.NaN,Double.NaN);
            assertFalse(MatrixFeatures_ZDRM.isLowerTriangle(A,0,UtilEjml.TEST_F64));
        }
    }


    @Test
    public void isZeros() {
        ZMatrixRMaj a = new ZMatrixRMaj(4,4);
        a.set(0, 0, 1, -1);

        assertFalse(MatrixFeatures_ZDRM.isZeros(a, 0.1));
        assertTrue(MatrixFeatures_ZDRM.isZeros(a, 2));
    }
}
