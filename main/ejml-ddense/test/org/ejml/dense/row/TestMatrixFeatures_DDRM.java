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

package org.ejml.dense.row;

import org.ejml.UtilEjml;
import org.ejml.data.BMatrixRMaj;
import org.ejml.data.DMatrixRMaj;
import org.junit.Test;

import java.util.Random;

import static org.ejml.UtilEjml.parse_DDRM;
import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestMatrixFeatures_DDRM {

    Random rand = new Random(0xff24);

    @Test
    public void hasUncountable() {
        DMatrixRMaj a = new DMatrixRMaj(4,4);

        // check a negative case first
        assertFalse(MatrixFeatures_DDRM.hasUncountable(a));

        // check two positve cases with different types of uncountables
        a.set(2, 2, Double.NaN);
        assertTrue(MatrixFeatures_DDRM.hasUncountable(a));

        a.set(2, 2, Double.POSITIVE_INFINITY);
        assertTrue(MatrixFeatures_DDRM.hasUncountable(a));
    }

    @Test
    public void isZeros() {
        DMatrixRMaj a = new DMatrixRMaj(4,4);
        a.set(0, 0, 1);

        assertFalse(MatrixFeatures_DDRM.isZeros(a, 0.1));
        assertTrue(MatrixFeatures_DDRM.isZeros(a, 2));
    }

    @Test
    public void isVector() {
        DMatrixRMaj a = new DMatrixRMaj(4,4);

        assertFalse(MatrixFeatures_DDRM.isVector(a));

        a.reshape(3, 1, false);
        assertTrue(MatrixFeatures_DDRM.isVector(a));

        a.reshape(1, 3, false);
        assertTrue(MatrixFeatures_DDRM.isVector(a));
    }

    /**
     * Check some trial cases.
     */
    @Test
    public void isPositiveDefinite() {
        DMatrixRMaj a = UtilEjml.parse_DDRM("2 0 0 2",2);
        DMatrixRMaj b = UtilEjml.parse_DDRM("0 1 1 0",2);
        DMatrixRMaj c = UtilEjml.parse_DDRM("0 0 0 0",2);

        assertTrue(MatrixFeatures_DDRM.isPositiveDefinite(a));
        assertFalse(MatrixFeatures_DDRM.isPositiveDefinite(b));
        assertFalse(MatrixFeatures_DDRM.isPositiveDefinite(c));

        // make sure the input isn't modified
        assertEquals(2, a.get(0, 0), UtilEjml.TEST_F64);
        assertEquals(2, a.get(1, 1), UtilEjml.TEST_F64);
    }

    @Test
    public void isPositiveSemidefinite() {
        DMatrixRMaj a = UtilEjml.parse_DDRM("2 0 0 2",2);
        DMatrixRMaj b = UtilEjml.parse_DDRM("0 1 1 0",2);
        DMatrixRMaj c = UtilEjml.parse_DDRM("0 0 0 0", 2);

        assertTrue(MatrixFeatures_DDRM.isPositiveSemidefinite(a));
        assertFalse(MatrixFeatures_DDRM.isPositiveSemidefinite(b));
        assertTrue(MatrixFeatures_DDRM.isPositiveSemidefinite(c));

        // make sure the input isn't modified
        assertEquals(2, a.get(0, 0), UtilEjml.TEST_F64);
        assertEquals(2, a.get(1, 1), UtilEjml.TEST_F64);
    }

    @Test
    public void isSquare() {
        DMatrixRMaj a = new DMatrixRMaj(5,4);

        assertFalse(MatrixFeatures_DDRM.isSquare(a));

        a.reshape(4, 4, false);
        assertTrue(MatrixFeatures_DDRM.isSquare(a));
    }

    @Test
    public void isDiagonalPositive() {
        DMatrixRMaj m = CommonOps_DDRM.identity(3);
        assertTrue(MatrixFeatures_DDRM.isDiagonalPositive(m));

        m.set(1, 1, -1);
        assertFalse(MatrixFeatures_DDRM.isDiagonalPositive(m));

        m.set(1, 1, Double.NaN);
        assertFalse(MatrixFeatures_DDRM.isDiagonalPositive(m));
    }

    @Test
    public void isSymmetric() {
        DMatrixRMaj m = CommonOps_DDRM.identity(3);
        m.set(1, 2, 5);m.set(2,1,5);
        assertTrue(MatrixFeatures_DDRM.isSymmetric(m));

        m.set(1, 2, 50);
        assertTrue(!MatrixFeatures_DDRM.isSymmetric(m));

        m.set(1, 2, Double.NaN);
        assertTrue(!MatrixFeatures_DDRM.isSymmetric(m));
    }

    @Test
    public void isSkewSymmetric() {
        DMatrixRMaj m = CommonOps_DDRM.identity(3);
        m.set(1, 2, 5);m.set(2,1,-5);
        assertTrue(MatrixFeatures_DDRM.isSkewSymmetric(m, UtilEjml.TEST_F64));

        m.set(1, 2, -5);
        assertTrue(!MatrixFeatures_DDRM.isSkewSymmetric(m, UtilEjml.TEST_F64));

        m.set(1, 2, Double.NaN);
        assertTrue(!MatrixFeatures_DDRM.isSkewSymmetric(m, UtilEjml.TEST_F64));
    }


    @Test
    public void isEquals() {
        String a = "-0.779094   1.682750   0.039239\n" +
                 "   1.304014  -1.880739   1.438741\n" +
                 "  -0.746918   1.382356  -0.520416";

        DMatrixRMaj m = parse_DDRM(a,3);
        DMatrixRMaj n = parse_DDRM(a,3);

        assertTrue(MatrixFeatures_DDRM.isEquals(m,n));

        n.set(2,1,-0.5);
        assertFalse(MatrixFeatures_DDRM.isEquals(m,n));

        m.set(2,1,Double.NaN);
        n.set(2, 1, Double.NaN);
        assertFalse(MatrixFeatures_DDRM.isEquals(m, n));
        m.set(2, 1, Double.POSITIVE_INFINITY);
        n.set(2,1,Double.POSITIVE_INFINITY);
        assertTrue(MatrixFeatures_DDRM.isEquals(m, n));
    }

    @Test
    public void isEquals_boolean() {
         BMatrixRMaj a = new BMatrixRMaj(3,4);
         BMatrixRMaj b = new BMatrixRMaj(3,4);

         RandomMatrices_DDRM.setRandomB(a,rand);
         b.set(a);

         assertTrue(MatrixFeatures_DDRM.isEquals(a, b));

         b.data[4] = !a.data[4];
         assertFalse(MatrixFeatures_DDRM.isEquals(a, b));
     }

    @Test
    public void isEquals_tol() {
        String a = "-0.779094   1.682750   0.039239\n" +
                 "   1.304014  -1.880739   1.438741\n" +
                 "  -0.746918   1.382356  -0.520416";

        DMatrixRMaj m = parse_DDRM(a,3);
        DMatrixRMaj n = parse_DDRM(a,3);

        assertTrue(MatrixFeatures_DDRM.isEquals(m,n,UtilEjml.TEST_F64_SQ));

        n.set(2,1,n.get(2,1)+ UtilEjml.EPS);
        assertTrue(MatrixFeatures_DDRM.isEquals(m,n,UtilEjml.TEST_F64_SQ));

        n.set(2,1,n.get(2,1)+UtilEjml.TEST_F64_SQ);
        assertFalse(MatrixFeatures_DDRM.isEquals(m, n, UtilEjml.TEST_F64_SQ));

        m.set(2,1,Double.NaN);
        n.set(2, 1, Double.NaN);
        assertFalse(MatrixFeatures_DDRM.isEquals(m, n, UtilEjml.TEST_F64_SQ));
        m.set(2, 1, Double.POSITIVE_INFINITY);
        n.set(2,1,Double.POSITIVE_INFINITY);
        assertFalse(MatrixFeatures_DDRM.isEquals(m, n, UtilEjml.TEST_F64_SQ));
    }

    @Test
    public void isEqualsTriangle() {

        // see if it works with different sized matrices
        for( int m = 2; m < 10; m+=3) {
            for( int n = 2; n < 10; n += 3 ) {
                DMatrixRMaj a = RandomMatrices_DDRM.rectangle(m,n,rand);
                DMatrixRMaj b = a.copy();

                // make the bottom triangle not the same
                b.set(m-1,0,0);

                assertTrue("m = "+m+" n = "+n, MatrixFeatures_DDRM.isEqualsTriangle(a,b, true, UtilEjml.TEST_F64));
                assertFalse(MatrixFeatures_DDRM.isEqualsTriangle(a,b, false, UtilEjml.TEST_F64));

                // make the upper triangle not the same
                b = a.copy();
                b.set(0,n-1,0);

                assertFalse(MatrixFeatures_DDRM.isEqualsTriangle(a,b, true, UtilEjml.TEST_F64));
                assertTrue(MatrixFeatures_DDRM.isEqualsTriangle(a,b, false, UtilEjml.TEST_F64));
            }
        }
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
        DMatrixRMaj A = new DMatrixRMaj(2,2);
        CommonOps_DDRM.fill(A, valA);
        DMatrixRMaj B = new DMatrixRMaj(2,2);
        CommonOps_DDRM.fill(B, valB);

        assertEquals(expected, MatrixFeatures_DDRM.isIdentical(A,B,tol));
    }

    @Test
    public void isInverse() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3,3,-1,1,rand);
        DMatrixRMaj A_inv = A.copy();

        CommonOps_DDRM.invert(A_inv);

        assertTrue(MatrixFeatures_DDRM.isInverse(A,A_inv,UtilEjml.TEST_F64));

        A_inv.set(1,2,3);
        assertFalse(MatrixFeatures_DDRM.isInverse(A,A_inv,UtilEjml.TEST_F64));

        A_inv.set(1,2,Double.NaN);
        assertFalse(MatrixFeatures_DDRM.isInverse(A,A_inv,UtilEjml.TEST_F64));
    }

    /**
     * Makes sure it isn't modifying the input
     */
    @Test
    public void isInverse_nomodify() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3,3,-1,1,rand);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(3,3,-1,1,rand);
        DMatrixRMaj A_copy = A.copy();
        DMatrixRMaj B_copy = B.copy();

        MatrixFeatures_DDRM.isInverse(A,B,UtilEjml.TEST_F64);

        assertTrue(MatrixFeatures_DDRM.isIdentical(A,A_copy,UtilEjml.TEST_F64));
        assertTrue(MatrixFeatures_DDRM.isIdentical(B,B_copy,UtilEjml.TEST_F64));
    }

    @Test
    public void hasNaN() {
        DMatrixRMaj m = new DMatrixRMaj(3,3);
        assertFalse(MatrixFeatures_DDRM.hasNaN(m));

        m.set(1,2,-Double.NaN);
        assertTrue(MatrixFeatures_DDRM.hasNaN(m));
    }

    @Test
    public void isOrthogonal() {
        // rotation matrices are orthogonal
        double c = Math.cos(0.1);
        double s = Math.sin(0.1);

        DMatrixRMaj A = new DMatrixRMaj(new double[][]{{c,s},{-s,c}});

        assertTrue(MatrixFeatures_DDRM.isOrthogonal(A,UtilEjml.TEST_F64_SQ));

        // try a negative case now
        A.set(0,1,495);

        assertFalse(MatrixFeatures_DDRM.isOrthogonal(A,UtilEjml.TEST_F64_SQ));

        A.set(0,1,Double.NaN);

        assertFalse(MatrixFeatures_DDRM.isOrthogonal(A,UtilEjml.TEST_F64_SQ));
    }

    @Test
    public void isRowsLinearIndependent() {
        // test a positive case
        DMatrixRMaj A = new DMatrixRMaj(2,3, true, 1, 2, 3, 2, 3, 4);
        assertTrue(MatrixFeatures_DDRM.isRowsLinearIndependent(A));

        // make sure the input wasn't modified
        DMatrixRMaj A_copy = new DMatrixRMaj(2,3, true, 1, 2, 3, 2, 3, 4);
        assertTrue(MatrixFeatures_DDRM.isIdentical(A,A_copy,UtilEjml.TEST_F64));

        // test negative case
        A = new DMatrixRMaj(2,3, true, 1, 2, 3, 1, 2, 3);
        assertFalse(MatrixFeatures_DDRM.isRowsLinearIndependent(A));
    }

    @Test
    public void isConstantVal() {
        DMatrixRMaj a = new DMatrixRMaj(3,4);

        CommonOps_DDRM.fill(a, 2.4);

        assertTrue(MatrixFeatures_DDRM.isConstantVal(a,2.4,UtilEjml.TEST_F64));
        assertFalse(MatrixFeatures_DDRM.isConstantVal(a,6,UtilEjml.TEST_F64));

        a.set(1,1,Double.NaN);
        assertFalse(MatrixFeatures_DDRM.isConstantVal(a,2.4,UtilEjml.TEST_F64));
    }

    @Test
    public void isIdentity() {
        DMatrixRMaj I = CommonOps_DDRM.identity(4);

        assertTrue(MatrixFeatures_DDRM.isIdentity(I,UtilEjml.TEST_F64));

        I.set(3,2,0.1);
        assertFalse(MatrixFeatures_DDRM.isIdentity(I,UtilEjml.TEST_F64));

        I.set(3,2,Double.NaN);
        assertFalse(MatrixFeatures_DDRM.isIdentity(I,UtilEjml.TEST_F64));
    }

    @Test
    public void isNegative() {
        DMatrixRMaj a = RandomMatrices_DDRM.rectangle(4,5,rand);
        DMatrixRMaj b = a.copy();
        CommonOps_DDRM.scale(-1,b);

        // test the positive case first
        assertTrue(MatrixFeatures_DDRM.isNegative(a,b,UtilEjml.TEST_F64));

        // now the negative case
        b.set(2,2,10);
        assertFalse(MatrixFeatures_DDRM.isNegative(a,b,UtilEjml.TEST_F64));

        b.set(2,2,Double.NaN);
        assertFalse(MatrixFeatures_DDRM.isNegative(a,b,UtilEjml.TEST_F64));
    }

    @Test
    public void isUpperTriangle() {
        // test matrices that are upper triangular to various degree hessenberg
        for( int hessenberg = 0; hessenberg < 2; hessenberg++ ) {
            DMatrixRMaj A = new DMatrixRMaj(6,6);
            for( int i = 0; i < A.numRows; i++ ) {
                int s = i <= hessenberg ? 0 : i-hessenberg;

                for( int j = s; j < A.numCols; j++ ) {
                   A.set(i,j,2);
                }
            }

            // test positive
            for( int i = hessenberg; i < A.numRows; i++ ) {
                assertTrue(MatrixFeatures_DDRM.isUpperTriangle(A,i,UtilEjml.TEST_F64));
            }

            // test negative
            for( int i = 0; i < hessenberg; i++ ) {
                assertFalse(MatrixFeatures_DDRM.isUpperTriangle(A,i,UtilEjml.TEST_F64));
            }

            // see if it handles NaN well
            A.set(4,0,Double.NaN);
            assertFalse(MatrixFeatures_DDRM.isUpperTriangle(A,0,UtilEjml.TEST_F64));
        }
    }

    @Test
    public void isLowerTriangle() {
        // test matrices that are upper triangular to various degree hessenberg
        for( int hessenberg = 0; hessenberg < 2; hessenberg++ ) {
            DMatrixRMaj A = new DMatrixRMaj(6,6);
            for( int i = 0; i < A.numRows; i++ ) {
                int s = i <= hessenberg ? 0 : i-hessenberg;

                for( int j = s; j < A.numCols; j++ ) {
                   A.set(i,j,2);
                }
            }
            CommonOps_DDRM.transpose(A);

            // test positive
            for( int i = hessenberg; i < A.numRows; i++ ) {
                assertTrue(MatrixFeatures_DDRM.isLowerTriangle(A,i,UtilEjml.TEST_F64));
            }

            // test negative
            for( int i = 0; i < hessenberg; i++ ) {
                assertFalse(MatrixFeatures_DDRM.isLowerTriangle(A,i,UtilEjml.TEST_F64));
            }

            // see if it handles NaN well
            A.set(0,4,Double.NaN);
            assertFalse(MatrixFeatures_DDRM.isLowerTriangle(A,0,UtilEjml.TEST_F64));
        }
    }


    @Test
    public void rank() {
        DMatrixRMaj a = UtilEjml.parse_DDRM("2 0 0 2",2);
        DMatrixRMaj a_copy = a.copy();

        assertEquals(2, MatrixFeatures_DDRM.rank(a));
        // make sure the input wasn't modified
        assertTrue(MatrixFeatures_DDRM.isIdentical(a,a_copy,UtilEjml.TEST_F64));

        a = UtilEjml.parse_DDRM("2 0 0 0",2);
        assertEquals(1, MatrixFeatures_DDRM.rank(a));
    }

    @Test
    public void rank_threshold() {
        DMatrixRMaj a = UtilEjml.parse_DDRM("2 0 0 2",2);
        DMatrixRMaj a_copy = a.copy();

        assertEquals(2, MatrixFeatures_DDRM.rank(a,10 * UtilEjml.EPS));
        // make sure the input wasn't modified
        assertTrue(MatrixFeatures_DDRM.isIdentical(a,a_copy,UtilEjml.TEST_F64));

        a = UtilEjml.parse_DDRM("2 0 0 1e-20",2);
        assertEquals(1, MatrixFeatures_DDRM.rank(a,10 * UtilEjml.EPS));

        // make sure it's using the threshold parameter
        a = UtilEjml.parse_DDRM("2 0 0 1e-20",2);
        assertEquals(2, MatrixFeatures_DDRM.rank(a, Math.pow(UtilEjml.EPS, 10) ));
    }

    @Test
    public void nullity() {
        DMatrixRMaj a = UtilEjml.parse_DDRM("2 0 0 2",2);
        DMatrixRMaj a_copy = a.copy();

        assertEquals(0, MatrixFeatures_DDRM.nullity(a));
        // make sure the input wasn't modified
        assertTrue(MatrixFeatures_DDRM.isIdentical(a,a_copy,UtilEjml.TEST_F64));

        a = UtilEjml.parse_DDRM("2 0 0 0",2);
        assertEquals(1, MatrixFeatures_DDRM.nullity(a));
    }

    @Test
    public void nullity_threshold() {
        DMatrixRMaj a = UtilEjml.parse_DDRM("2 0 0 2",2);
        DMatrixRMaj a_copy = a.copy();

        assertEquals(0, MatrixFeatures_DDRM.nullity(a, 10 * UtilEjml.EPS));
        // make sure the input wasn't modified
        assertTrue(MatrixFeatures_DDRM.isIdentical(a,a_copy,UtilEjml.TEST_F64));

        a = UtilEjml.parse_DDRM("2 0 0 1e-20",2);
        assertEquals(1, MatrixFeatures_DDRM.nullity(a, 10 * UtilEjml.EPS));

        // make sure it's using the threshold parameter
        a = UtilEjml.parse_DDRM("2 0 0 1e-20",2);
        assertEquals(0, MatrixFeatures_DDRM.nullity(a, Math.pow(UtilEjml.EPS, 10)));
    }

    @Test
    public void countNonZero() {
        DMatrixRMaj a = RandomMatrices_DDRM.rectangle(10,5,rand);

        a.set(0,3,0);
        a.set(1,3,0);
        a.set(2,3,0);

        int found = MatrixFeatures_DDRM.countNonZero(a);
        assertEquals(50-3, found);

        a.set(1,3, Double.NaN);
        assertEquals(50-3, found);

        a.set(1,3, Double.POSITIVE_INFINITY);
        assertEquals(50-3, found);
    }
}
