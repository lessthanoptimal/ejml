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
import org.ejml.data.BMatrixRMaj;
import org.ejml.data.FMatrixRMaj;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.ejml.UtilEjml.parse_FDRM;
import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public class TestMatrixFeatures_FDRM {

    Random rand = new Random(0xff24);

    @Test
    public void hasUncountable() {
        FMatrixRMaj a = new FMatrixRMaj(4,4);

        // check a negative case first
        assertFalse(MatrixFeatures_FDRM.hasUncountable(a));

        // check two positve cases with different types of uncountables
        a.set(2, 2, Float.NaN);
        assertTrue(MatrixFeatures_FDRM.hasUncountable(a));

        a.set(2, 2, Float.POSITIVE_INFINITY);
        assertTrue(MatrixFeatures_FDRM.hasUncountable(a));
    }

    @Test
    public void isZeros() {
        FMatrixRMaj a = new FMatrixRMaj(4,4);
        a.set(0, 0, 1);

        assertFalse(MatrixFeatures_FDRM.isZeros(a, 0.1f));
        assertTrue(MatrixFeatures_FDRM.isZeros(a, 2));
    }

    @Test
    public void isVector() {
        FMatrixRMaj a = new FMatrixRMaj(4,4);

        assertFalse(MatrixFeatures_FDRM.isVector(a));

        a.reshape(3, 1, false);
        assertTrue(MatrixFeatures_FDRM.isVector(a));

        a.reshape(1, 3, false);
        assertTrue(MatrixFeatures_FDRM.isVector(a));
    }

    /**
     * Check some trial cases.
     */
    @Test
    public void isPositiveDefinite() {
        FMatrixRMaj a = UtilEjml.parse_FDRM("2 0 0 2",2);
        FMatrixRMaj b = UtilEjml.parse_FDRM("0 1 1 0",2);
        FMatrixRMaj c = UtilEjml.parse_FDRM("0 0 0 0",2);

        assertTrue(MatrixFeatures_FDRM.isPositiveDefinite(a));
        assertFalse(MatrixFeatures_FDRM.isPositiveDefinite(b));
        assertFalse(MatrixFeatures_FDRM.isPositiveDefinite(c));

        // make sure the input isn't modified
        assertEquals(2, a.get(0, 0), UtilEjml.TEST_F32);
        assertEquals(2, a.get(1, 1), UtilEjml.TEST_F32);
    }

    @Test
    public void isPositiveSemidefinite() {
        FMatrixRMaj a = UtilEjml.parse_FDRM("2 0 0 2",2);
        FMatrixRMaj b = UtilEjml.parse_FDRM("0 1 1 0",2);
        FMatrixRMaj c = UtilEjml.parse_FDRM("0 0 0 0", 2);

        assertTrue(MatrixFeatures_FDRM.isPositiveSemidefinite(a));
        assertFalse(MatrixFeatures_FDRM.isPositiveSemidefinite(b));
        assertTrue(MatrixFeatures_FDRM.isPositiveSemidefinite(c));

        // make sure the input isn't modified
        assertEquals(2, a.get(0, 0), UtilEjml.TEST_F32);
        assertEquals(2, a.get(1, 1), UtilEjml.TEST_F32);
    }

    @Test
    public void isSquare() {
        FMatrixRMaj a = new FMatrixRMaj(5,4);

        assertFalse(MatrixFeatures_FDRM.isSquare(a));

        a.reshape(4, 4, false);
        assertTrue(MatrixFeatures_FDRM.isSquare(a));
    }

    @Test
    public void isDiagonalPositive() {
        FMatrixRMaj m = CommonOps_FDRM.identity(3);
        assertTrue(MatrixFeatures_FDRM.isDiagonalPositive(m));

        m.set(1, 1, -1);
        assertFalse(MatrixFeatures_FDRM.isDiagonalPositive(m));

        m.set(1, 1, Float.NaN);
        assertFalse(MatrixFeatures_FDRM.isDiagonalPositive(m));
    }

    @Test
    public void isSymmetric() {
        FMatrixRMaj m = CommonOps_FDRM.identity(3);
        m.set(1, 2, 5);m.set(2,1,5);
        assertTrue(MatrixFeatures_FDRM.isSymmetric(m));

        m.set(1, 2, 50);
        assertTrue(!MatrixFeatures_FDRM.isSymmetric(m));

        m.set(1, 2, Float.NaN);
        assertTrue(!MatrixFeatures_FDRM.isSymmetric(m));
    }

    @Test
    public void isSkewSymmetric() {
        FMatrixRMaj m = CommonOps_FDRM.identity(3);
        m.set(1, 2, 5);m.set(2,1,-5);
        assertTrue(MatrixFeatures_FDRM.isSkewSymmetric(m, UtilEjml.TEST_F32));

        m.set(1, 2, -5);
        assertTrue(!MatrixFeatures_FDRM.isSkewSymmetric(m, UtilEjml.TEST_F32));

        m.set(1, 2, Float.NaN);
        assertTrue(!MatrixFeatures_FDRM.isSkewSymmetric(m, UtilEjml.TEST_F32));
    }


    @Test
    public void isEquals() {
        String a = "-0.779094f   1.682750f   0.039239f\n" +
                 "   1.304014f  -1.880739f   1.438741f\n" +
                 "  -0.746918f   1.382356f  -0.520416f";

        FMatrixRMaj m = parse_FDRM(a,3);
        FMatrixRMaj n = parse_FDRM(a,3);

        assertTrue(MatrixFeatures_FDRM.isEquals(m,n));

        n.set(2,1,-0.5f);
        assertFalse(MatrixFeatures_FDRM.isEquals(m,n));

        m.set(2,1,Float.NaN);
        n.set(2, 1, Float.NaN);
        assertFalse(MatrixFeatures_FDRM.isEquals(m, n));
        m.set(2, 1, Float.POSITIVE_INFINITY);
        n.set(2,1,Float.POSITIVE_INFINITY);
        assertTrue(MatrixFeatures_FDRM.isEquals(m, n));
    }

    @Test
    public void isEquals_boolean() {
         BMatrixRMaj a = new BMatrixRMaj(3,4);
         BMatrixRMaj b = new BMatrixRMaj(3,4);

         RandomMatrices_FDRM.setRandomB(a,rand);
         b.set(a);

         assertTrue(MatrixFeatures_FDRM.isEquals(a, b));

         b.data[4] = !a.data[4];
         assertFalse(MatrixFeatures_FDRM.isEquals(a, b));
     }

    @Test
    public void isEquals_tol() {
        String a = "-0.779094f   1.682750f   0.039239f\n" +
                 "   1.304014f  -1.880739f   1.438741f\n" +
                 "  -0.746918f   1.382356f  -0.520416f";

        FMatrixRMaj m = parse_FDRM(a,3);
        FMatrixRMaj n = parse_FDRM(a,3);

        assertTrue(MatrixFeatures_FDRM.isEquals(m,n,UtilEjml.TEST_F32_SQ));

        n.set(2,1,n.get(2,1)+ UtilEjml.F_EPS);
        assertTrue(MatrixFeatures_FDRM.isEquals(m,n,UtilEjml.TEST_F32_SQ));

        n.set(2,1,n.get(2,1)+UtilEjml.TEST_F32_SQ);
        assertFalse(MatrixFeatures_FDRM.isEquals(m, n, UtilEjml.TEST_F32_SQ));

        m.set(2,1,Float.NaN);
        n.set(2, 1, Float.NaN);
        assertFalse(MatrixFeatures_FDRM.isEquals(m, n, UtilEjml.TEST_F32_SQ));
        m.set(2, 1, Float.POSITIVE_INFINITY);
        n.set(2,1,Float.POSITIVE_INFINITY);
        assertFalse(MatrixFeatures_FDRM.isEquals(m, n, UtilEjml.TEST_F32_SQ));
    }

    @Test
    public void isEqualsTriangle() {

        // see if it works with different sized matrices
        for( int m = 2; m < 10; m+=3) {
            for( int n = 2; n < 10; n += 3 ) {
                FMatrixRMaj a = RandomMatrices_FDRM.rectangle(m,n,rand);
                FMatrixRMaj b = a.copy();

                // make the bottom triangle not the same
                b.set(m-1,0,0);

                assertTrue(MatrixFeatures_FDRM.isEqualsTriangle(a,b, true, UtilEjml.TEST_F32),"m = "+m+" n = "+n);
                assertFalse(MatrixFeatures_FDRM.isEqualsTriangle(a,b, false, UtilEjml.TEST_F32));

                // make the upper triangle not the same
                b = a.copy();
                b.set(0,n-1,0);

                assertFalse(MatrixFeatures_FDRM.isEqualsTriangle(a,b, true, UtilEjml.TEST_F32));
                assertTrue(MatrixFeatures_FDRM.isEqualsTriangle(a,b, false, UtilEjml.TEST_F32));
            }
        }
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
        FMatrixRMaj A = new FMatrixRMaj(2,2);
        CommonOps_FDRM.fill(A, valA);
        FMatrixRMaj B = new FMatrixRMaj(2,2);
        CommonOps_FDRM.fill(B, valB);

        assertEquals(expected, MatrixFeatures_FDRM.isIdentical(A,B,tol));
    }

    @Test
    public void isInverse() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(3,3,-1,1,rand);
        FMatrixRMaj A_inv = A.copy();

        CommonOps_FDRM.invert(A_inv);

        assertTrue(MatrixFeatures_FDRM.isInverse(A,A_inv,UtilEjml.TEST_F32));

        A_inv.set(1,2,3);
        assertFalse(MatrixFeatures_FDRM.isInverse(A,A_inv,UtilEjml.TEST_F32));

        A_inv.set(1,2,Float.NaN);
        assertFalse(MatrixFeatures_FDRM.isInverse(A,A_inv,UtilEjml.TEST_F32));
    }

    /**
     * Makes sure it isn't modifying the input
     */
    @Test
    public void isInverse_nomodify() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(3,3,-1,1,rand);
        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(3,3,-1,1,rand);
        FMatrixRMaj A_copy = A.copy();
        FMatrixRMaj B_copy = B.copy();

        MatrixFeatures_FDRM.isInverse(A,B,UtilEjml.TEST_F32);

        assertTrue(MatrixFeatures_FDRM.isIdentical(A,A_copy,UtilEjml.TEST_F32));
        assertTrue(MatrixFeatures_FDRM.isIdentical(B,B_copy,UtilEjml.TEST_F32));
    }

    @Test
    public void hasNaN() {
        FMatrixRMaj m = new FMatrixRMaj(3,3);
        assertFalse(MatrixFeatures_FDRM.hasNaN(m));

        m.set(1,2,-Float.NaN);
        assertTrue(MatrixFeatures_FDRM.hasNaN(m));
    }

    @Test
    public void isOrthogonal() {
        // rotation matrices are orthogonal
        float c = (float)Math.cos(0.1f);
        float s = (float)Math.sin(0.1f);

        FMatrixRMaj A = new FMatrixRMaj(new float[][]{{c,s},{-s,c}});

        assertTrue(MatrixFeatures_FDRM.isOrthogonal(A,UtilEjml.TEST_F32_SQ));

        // try a negative case now
        A.set(0,1,495);

        assertFalse(MatrixFeatures_FDRM.isOrthogonal(A,UtilEjml.TEST_F32_SQ));

        A.set(0,1,Float.NaN);

        assertFalse(MatrixFeatures_FDRM.isOrthogonal(A,UtilEjml.TEST_F32_SQ));
    }

    @Test
    public void isRowsLinearIndependent() {
        // test a positive case
        FMatrixRMaj A = new FMatrixRMaj(2,3, true, 1, 2, 3, 2, 3, 4);
        assertTrue(MatrixFeatures_FDRM.isRowsLinearIndependent(A));

        // make sure the input wasn't modified
        FMatrixRMaj A_copy = new FMatrixRMaj(2,3, true, 1, 2, 3, 2, 3, 4);
        assertTrue(MatrixFeatures_FDRM.isIdentical(A,A_copy,UtilEjml.TEST_F32));

        // test negative case
        A = new FMatrixRMaj(2,3, true, 1, 2, 3, 1, 2, 3);
        assertFalse(MatrixFeatures_FDRM.isRowsLinearIndependent(A));
    }

    @Test
    public void isConstantVal() {
        FMatrixRMaj a = new FMatrixRMaj(3,4);

        CommonOps_FDRM.fill(a, 2.4f);

        assertTrue(MatrixFeatures_FDRM.isConstantVal(a,2.4f,UtilEjml.TEST_F32));
        assertFalse(MatrixFeatures_FDRM.isConstantVal(a,6,UtilEjml.TEST_F32));

        a.set(1,1,Float.NaN);
        assertFalse(MatrixFeatures_FDRM.isConstantVal(a,2.4f,UtilEjml.TEST_F32));
    }

    @Test
    public void isIdentity() {
        FMatrixRMaj I = CommonOps_FDRM.identity(4);

        assertTrue(MatrixFeatures_FDRM.isIdentity(I,UtilEjml.TEST_F32));

        I.set(3,2,0.1f);
        assertFalse(MatrixFeatures_FDRM.isIdentity(I,UtilEjml.TEST_F32));

        I.set(3,2,Float.NaN);
        assertFalse(MatrixFeatures_FDRM.isIdentity(I,UtilEjml.TEST_F32));
    }

    @Test
    public void isNegative() {
        FMatrixRMaj a = RandomMatrices_FDRM.rectangle(4,5,rand);
        FMatrixRMaj b = a.copy();
        CommonOps_FDRM.scale(-1,b);

        // test the positive case first
        assertTrue(MatrixFeatures_FDRM.isNegative(a,b,UtilEjml.TEST_F32));

        // now the negative case
        b.set(2,2,10);
        assertFalse(MatrixFeatures_FDRM.isNegative(a,b,UtilEjml.TEST_F32));

        b.set(2,2,Float.NaN);
        assertFalse(MatrixFeatures_FDRM.isNegative(a,b,UtilEjml.TEST_F32));
    }

    @Test
    public void isUpperTriangle() {
        // test matrices that are upper triangular to various degree hessenberg
        for( int hessenberg = 0; hessenberg < 2; hessenberg++ ) {
            FMatrixRMaj A = new FMatrixRMaj(6,6);
            for( int i = 0; i < A.numRows; i++ ) {
                int s = i <= hessenberg ? 0 : i-hessenberg;

                for( int j = s; j < A.numCols; j++ ) {
                   A.set(i,j,2);
                }
            }

            // test positive
            for( int i = hessenberg; i < A.numRows; i++ ) {
                assertTrue(MatrixFeatures_FDRM.isUpperTriangle(A,i,UtilEjml.TEST_F32));
            }

            // test negative
            for( int i = 0; i < hessenberg; i++ ) {
                assertFalse(MatrixFeatures_FDRM.isUpperTriangle(A,i,UtilEjml.TEST_F32));
            }

            // see if it handles NaN well
            A.set(4,0,Float.NaN);
            assertFalse(MatrixFeatures_FDRM.isUpperTriangle(A,0,UtilEjml.TEST_F32));
        }
    }

    @Test
    public void isLowerTriangle() {
        // test matrices that are upper triangular to various degree hessenberg
        for( int hessenberg = 0; hessenberg < 2; hessenberg++ ) {
            FMatrixRMaj A = new FMatrixRMaj(6,6);
            for( int i = 0; i < A.numRows; i++ ) {
                int s = i <= hessenberg ? 0 : i-hessenberg;

                for( int j = s; j < A.numCols; j++ ) {
                   A.set(i,j,2);
                }
            }
            CommonOps_FDRM.transpose(A);

            // test positive
            for( int i = hessenberg; i < A.numRows; i++ ) {
                assertTrue(MatrixFeatures_FDRM.isLowerTriangle(A,i,UtilEjml.TEST_F32));
            }

            // test negative
            for( int i = 0; i < hessenberg; i++ ) {
                assertFalse(MatrixFeatures_FDRM.isLowerTriangle(A,i,UtilEjml.TEST_F32));
            }

            // see if it handles NaN well
            A.set(0,4,Float.NaN);
            assertFalse(MatrixFeatures_FDRM.isLowerTriangle(A,0,UtilEjml.TEST_F32));
        }
    }


    @Test
    public void rank() {
        FMatrixRMaj a = UtilEjml.parse_FDRM("2 0 0 2",2);
        FMatrixRMaj a_copy = a.copy();

        assertEquals(2, MatrixFeatures_FDRM.rank(a));
        // make sure the input wasn't modified
        assertTrue(MatrixFeatures_FDRM.isIdentical(a,a_copy,UtilEjml.TEST_F32));

        a = UtilEjml.parse_FDRM("2 0 0 0",2);
        assertEquals(1, MatrixFeatures_FDRM.rank(a));
    }

    @Test
    public void rank_threshold() {
        FMatrixRMaj a = UtilEjml.parse_FDRM("2 0 0 2",2);
        FMatrixRMaj a_copy = a.copy();

        assertEquals(2, MatrixFeatures_FDRM.rank(a,10 * UtilEjml.F_EPS));
        // make sure the input wasn't modified
        assertTrue(MatrixFeatures_FDRM.isIdentical(a,a_copy,UtilEjml.TEST_F32));

        a = UtilEjml.parse_FDRM("2 0 0 1e-20",2);
        assertEquals(1, MatrixFeatures_FDRM.rank(a,10 * UtilEjml.F_EPS));

        // make sure it's using the threshold parameter
        a = UtilEjml.parse_FDRM("2 0 0 1e-20",2);
        assertEquals(2, MatrixFeatures_FDRM.rank(a, (float)Math.pow(UtilEjml.F_EPS, 10) ));
    }

    @Test
    public void nullity() {
        FMatrixRMaj a = UtilEjml.parse_FDRM("2 0 0 2",2);
        FMatrixRMaj a_copy = a.copy();

        assertEquals(0, MatrixFeatures_FDRM.nullity(a));
        // make sure the input wasn't modified
        assertTrue(MatrixFeatures_FDRM.isIdentical(a,a_copy,UtilEjml.TEST_F32));

        a = UtilEjml.parse_FDRM("2 0 0 0",2);
        assertEquals(1, MatrixFeatures_FDRM.nullity(a));
    }

    @Test
    public void nullity_threshold() {
        FMatrixRMaj a = UtilEjml.parse_FDRM("2 0 0 2",2);
        FMatrixRMaj a_copy = a.copy();

        assertEquals(0, MatrixFeatures_FDRM.nullity(a, 10 * UtilEjml.F_EPS));
        // make sure the input wasn't modified
        assertTrue(MatrixFeatures_FDRM.isIdentical(a,a_copy,UtilEjml.TEST_F32));

        a = UtilEjml.parse_FDRM("2 0 0 1e-20",2);
        assertEquals(1, MatrixFeatures_FDRM.nullity(a, 10 * UtilEjml.F_EPS));

        // make sure it's using the threshold parameter
        a = UtilEjml.parse_FDRM("2 0 0 1e-20",2);
        assertEquals(0, MatrixFeatures_FDRM.nullity(a, (float)Math.pow(UtilEjml.F_EPS, 10)));
    }

    @Test
    public void countNonZero() {
        FMatrixRMaj a = RandomMatrices_FDRM.rectangle(10,5,rand);

        a.set(0,3,0);
        a.set(1,3,0);
        a.set(2,3,0);

        int found = MatrixFeatures_FDRM.countNonZero(a);
        assertEquals(50-3, found);

        a.set(1,3, Float.NaN);
        assertEquals(50-3, found);

        a.set(1,3, Float.POSITIVE_INFINITY);
        assertEquals(50-3, found);
    }
}
