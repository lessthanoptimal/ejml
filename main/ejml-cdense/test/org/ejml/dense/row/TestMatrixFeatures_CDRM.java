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
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestMatrixFeatures_CDRM {

    private Random rand = new Random(234);

    @Test
    public void isVector() {
        CMatrixRMaj a = new CMatrixRMaj(4,4);

        assertFalse(MatrixFeatures_CDRM.isVector(a));

        a.reshape(3, 1);
        assertTrue(MatrixFeatures_CDRM.isVector(a));

        a.reshape(1, 3);
        assertTrue(MatrixFeatures_CDRM.isVector(a));
    }

    @Test
    public void isNegative() {
        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(4,5,rand);
        CMatrixRMaj b = a.copy();
        CommonOps_CDRM.scale(-1,0,b);

        // test the positive case first
        assertTrue(MatrixFeatures_CDRM.isNegative(a,b,UtilEjml.TEST_F32));

        // now the negative case
        b.set(2,2,10,0);
        assertFalse(MatrixFeatures_CDRM.isNegative(a,b,UtilEjml.TEST_F32));

        b.set(2,2,Float.NaN,0);
        assertFalse(MatrixFeatures_CDRM.isNegative(a,b,UtilEjml.TEST_F32));
    }

    @Test
    public void hasUncountable() {
        CMatrixRMaj a = new CMatrixRMaj(4,4);

        // check a negative case first
        assertFalse(MatrixFeatures_CDRM.hasUncountable(a));

        // check two positve cases with different types of uncountables
        a.set(2,2,Float.NaN,0);
        assertTrue(MatrixFeatures_CDRM.hasUncountable(a));

        a.set(2,2,Float.POSITIVE_INFINITY,0);
        assertTrue(MatrixFeatures_CDRM.hasUncountable(a));
    }

    @Test
    public void hasNaN() {
        CMatrixRMaj m = new CMatrixRMaj(3,3);
        assertFalse(MatrixFeatures_CDRM.hasNaN(m));

        m.set(1,2,-Float.NaN,0);
        assertTrue(MatrixFeatures_CDRM.hasNaN(m));
    }

    @Test
    public void isEquals() {
        CMatrixRMaj m = RandomMatrices_CDRM.rectangle(3,4,-1,1,rand);
        CMatrixRMaj n = m.copy();

        assertTrue(MatrixFeatures_CDRM.isEquals(m,n));

        n.set(2,1,-0.5f,-0.6f);
        assertFalse(MatrixFeatures_CDRM.isEquals(m,n));

        m.set(2,1,Float.NaN,1);
        n.set(2,1,Float.NaN,1);
        assertFalse(MatrixFeatures_CDRM.isEquals(m,n));
        m.set(2,1,Float.POSITIVE_INFINITY,1);
        n.set(2,1,Float.POSITIVE_INFINITY,1);
        assertTrue(MatrixFeatures_CDRM.isEquals(m,n));
    }

    @Test
    public void isEquals_tol() {
        CMatrixRMaj m = RandomMatrices_CDRM.rectangle(3,4,-1,1,rand);
        CMatrixRMaj n = m.copy();

        assertTrue(MatrixFeatures_CDRM.isEquals(m,n,UtilEjml.TEST_F32));

        n.data[4] += UtilEjml.F_EPS;
        assertTrue(MatrixFeatures_CDRM.isEquals(m,n,UtilEjml.TEST_F32));

        n.data[4] += (float)Math.sqrt(UtilEjml.TEST_F32_SQ);
        assertFalse(MatrixFeatures_CDRM.isEquals(m,n,UtilEjml.TEST_F32));

        m.set(2,1,Float.NaN,1);
        n.set(2,1,Float.NaN,1);
        assertFalse(MatrixFeatures_CDRM.isEquals(m,n,UtilEjml.TEST_F32));
        m.set(2,1,Float.POSITIVE_INFINITY,1);
        n.set(2,1,Float.POSITIVE_INFINITY,1);
        assertFalse(MatrixFeatures_CDRM.isEquals(m,n,UtilEjml.TEST_F32));
    }

    @Test
    public void isIdentical() {

        float values[] = new float[]{1.0f,Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY};

        for( int i = 0; i < values.length; i++ ) {
            for( int j = 0; j < values.length; j++ ) {
                checkIdentical(values[i],values[j],UtilEjml.TEST_F32,i==j);
            }
        }

        checkIdentical(1.0f,1.5f,UtilEjml.TEST_F32,false);
        checkIdentical(1.5f,1.0f,UtilEjml.TEST_F32,false);
        checkIdentical(1.0f,1.0000000001f,UtilEjml.TEST_F32,true);
        checkIdentical(1.0f,Float.NaN,UtilEjml.TEST_F32,false);
        checkIdentical(Float.NaN,1.0f,UtilEjml.TEST_F32,false);
    }

    private void checkIdentical( float valA , float valB , float tol , boolean expected ) {
        CMatrixRMaj A = new CMatrixRMaj(2,2);
        CMatrixRMaj B = new CMatrixRMaj(2,2);
        CommonOps_CDRM.fill(A, valA,0);
        CommonOps_CDRM.fill(B, valB,0);

        assertEquals(expected, MatrixFeatures_CDRM.isIdentical(A,B,tol));

        CommonOps_CDRM.fill(A, 0,valA);
        CommonOps_CDRM.fill(B, 0,valB);

        assertEquals(expected, MatrixFeatures_CDRM.isIdentical(A,B,tol));
    }

    @Test
    public void isIdentity() {
        CMatrixRMaj m = CommonOps_CDRM.diag(1,0,1,0,1,0);

        assertTrue(MatrixFeatures_CDRM.isIdentity(m,UtilEjml.TEST_F32));

        m.setImag(0,0,10*UtilEjml.F_EPS);
        assertTrue(MatrixFeatures_CDRM.isIdentity(m, UtilEjml.TEST_F32));
        m.setReal(0, 0, 1 + 10*UtilEjml.F_EPS);
        assertTrue(MatrixFeatures_CDRM.isIdentity(m,UtilEjml.TEST_F32));

        assertFalse(MatrixFeatures_CDRM.isIdentity(m, UtilEjml.F_EPS));
        assertFalse(MatrixFeatures_CDRM.isIdentity(m, UtilEjml.F_EPS));

        m.setImag(1,0,10*UtilEjml.F_EPS);
        assertTrue(MatrixFeatures_CDRM.isIdentity(m,UtilEjml.TEST_F32));
        m.setReal(1,0,10*UtilEjml.F_EPS);
        assertTrue(MatrixFeatures_CDRM.isIdentity(m,UtilEjml.TEST_F32));

        assertFalse(MatrixFeatures_CDRM.isIdentity(m,UtilEjml.F_EPS));
        assertFalse(MatrixFeatures_CDRM.isIdentity(m,UtilEjml.F_EPS));
    }

    @Test
    public void isHermitian() {
        CMatrixRMaj A = new CMatrixRMaj(new float[][]{{1,0, 2,2.1f},{2,-2.1f ,3,0}});

        assertTrue(MatrixFeatures_CDRM.isHermitian(A, UtilEjml.TEST_F32));

        A.set(0,1,5,6);

        assertFalse(MatrixFeatures_CDRM.isHermitian(A, UtilEjml.TEST_F32));
    }

    @Test
    public void isUnitary() {
        // create a reflector since it's unitary
        CMatrixRMaj u = RandomMatrices_CDRM.rectangle(5,1,rand);
        Complex_F32 dot = new Complex_F32();
        VectorVectorMult_CDRM.innerProdH(u, u,dot);
        float gamma = 2.0f/dot.real;
        CMatrixRMaj A = SpecializedOps_CDRM.householder(u,gamma);

        assertTrue(MatrixFeatures_CDRM.isUnitary(A, UtilEjml.TEST_F32));

        // try a negative case now
        A.set(0,1,495,400);

        assertFalse(MatrixFeatures_CDRM.isUnitary(A, UtilEjml.TEST_F32));

        A.set(0,1,Float.NaN,Float.NaN);

        assertFalse(MatrixFeatures_CDRM.isUnitary(A, UtilEjml.TEST_F32));
    }

    /**
     * Check some trial cases.
     */
    @Test
    public void isPositiveDefinite() {
        CMatrixRMaj a = new CMatrixRMaj(2,2,true,2,0,0,0,0,0,2,0);
        CMatrixRMaj b = new CMatrixRMaj(2,2,true,0,0,1,0,1,0,0,0);
        CMatrixRMaj c = new CMatrixRMaj(2,2);

        assertTrue(MatrixFeatures_CDRM.isPositiveDefinite(a));
        assertFalse(MatrixFeatures_CDRM.isPositiveDefinite(b));
        assertFalse(MatrixFeatures_CDRM.isPositiveDefinite(c));

        // make sure the input isn't modified
        assertEquals(2,a.getReal(0, 0),UtilEjml.TEST_F32);
        assertEquals(2,a.getReal(1,1),UtilEjml.TEST_F32);
    }

    @Test
    public void isUpperTriangle() {
        // test matrices that are upper triangular to various degree hessenberg
        for( int hessenberg = 0; hessenberg < 2; hessenberg++ ) {
            CMatrixRMaj A = new CMatrixRMaj(6,6);
            for( int i = 0; i < A.numRows; i++ ) {
                int s = i <= hessenberg ? 0 : i-hessenberg;

                for( int j = s; j < A.numCols; j++ ) {
                   A.set(i,j,2,2);
                }
            }

            // test positive
            for( int i = hessenberg; i < A.numRows; i++ ) {
                assertTrue(MatrixFeatures_CDRM.isUpperTriangle(A,i,UtilEjml.TEST_F32));
            }

            // test negative
            for( int i = 0; i < hessenberg; i++ ) {
                assertFalse(MatrixFeatures_CDRM.isUpperTriangle(A,i, UtilEjml.TEST_F32));
            }

            // see if it handles NaN well
            A.set(4,0,Float.NaN,Float.NaN);
            assertFalse(MatrixFeatures_CDRM.isUpperTriangle(A,0,UtilEjml.TEST_F32));
        }
    }

    @Test
    public void isLowerTriangle() {
        // test matrices that are upper triangular to various degree hessenberg
        for( int hessenberg = 0; hessenberg < 2; hessenberg++ ) {
            CMatrixRMaj A = new CMatrixRMaj(6,6);
            for( int i = 0; i < A.numRows; i++ ) {
                int s = i <= hessenberg ? 0 : i-hessenberg;

                for( int j = s; j < A.numCols; j++ ) {
                   A.set(i,j,2,2);
                }
            }
            CommonOps_CDRM.transpose(A);

            // test positive
            for( int i = hessenberg; i < A.numRows; i++ ) {
                assertTrue(MatrixFeatures_CDRM.isLowerTriangle(A,i,UtilEjml.TEST_F32));
            }

            // test negative
            for( int i = 0; i < hessenberg; i++ ) {
                assertFalse(MatrixFeatures_CDRM.isLowerTriangle(A,i,UtilEjml.TEST_F32));
            }

            // see if it handles NaN well
            A.set(0,4,Float.NaN,Float.NaN);
            assertFalse(MatrixFeatures_CDRM.isLowerTriangle(A,0,UtilEjml.TEST_F32));
        }
    }


    @Test
    public void isZeros() {
        CMatrixRMaj a = new CMatrixRMaj(4,4);
        a.set(0, 0, 1, -1);

        assertFalse(MatrixFeatures_CDRM.isZeros(a, 0.1f));
        assertTrue(MatrixFeatures_CDRM.isZeros(a, 2));
    }
}
