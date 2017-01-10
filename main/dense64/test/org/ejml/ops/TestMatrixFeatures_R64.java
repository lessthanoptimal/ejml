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
import org.ejml.data.DMatrixRow_B;
import org.ejml.data.DMatrixRow_F64;
import org.junit.Test;

import java.util.Random;

import static org.ejml.UtilEjml.parse_R64;
import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestMatrixFeatures_R64 {

    Random rand = new Random(0xff24);

    @Test
    public void hasUncountable() {
        DMatrixRow_F64 a = new DMatrixRow_F64(4,4);

        // check a negative case first
        assertFalse(MatrixFeatures_R64.hasUncountable(a));

        // check two positve cases with different types of uncountables
        a.set(2, 2, Double.NaN);
        assertTrue(MatrixFeatures_R64.hasUncountable(a));

        a.set(2, 2, Double.POSITIVE_INFINITY);
        assertTrue(MatrixFeatures_R64.hasUncountable(a));
    }

    @Test
    public void isZeros() {
        DMatrixRow_F64 a = new DMatrixRow_F64(4,4);
        a.set(0, 0, 1);

        assertFalse(MatrixFeatures_R64.isZeros(a, 0.1));
        assertTrue(MatrixFeatures_R64.isZeros(a, 2));
    }

    @Test
    public void isVector() {
        DMatrixRow_F64 a = new DMatrixRow_F64(4,4);

        assertFalse(MatrixFeatures_R64.isVector(a));

        a.reshape(3, 1, false);
        assertTrue(MatrixFeatures_R64.isVector(a));

        a.reshape(1, 3, false);
        assertTrue(MatrixFeatures_R64.isVector(a));
    }

    /**
     * Check some trial cases.
     */
    @Test
    public void isPositiveDefinite() {
        DMatrixRow_F64 a = UtilEjml.parse_R64("2 0 0 2",2);
        DMatrixRow_F64 b = UtilEjml.parse_R64("0 1 1 0",2);
        DMatrixRow_F64 c = UtilEjml.parse_R64("0 0 0 0",2);

        assertTrue(MatrixFeatures_R64.isPositiveDefinite(a));
        assertFalse(MatrixFeatures_R64.isPositiveDefinite(b));
        assertFalse(MatrixFeatures_R64.isPositiveDefinite(c));

        // make sure the input isn't modified
        assertEquals(2, a.get(0, 0), UtilEjml.TEST_F64);
        assertEquals(2, a.get(1, 1), UtilEjml.TEST_F64);
    }

    @Test
    public void isPositiveSemidefinite() {
        DMatrixRow_F64 a = UtilEjml.parse_R64("2 0 0 2",2);
        DMatrixRow_F64 b = UtilEjml.parse_R64("0 1 1 0",2);
        DMatrixRow_F64 c = UtilEjml.parse_R64("0 0 0 0", 2);

        assertTrue(MatrixFeatures_R64.isPositiveSemidefinite(a));
        assertFalse(MatrixFeatures_R64.isPositiveSemidefinite(b));
        assertTrue(MatrixFeatures_R64.isPositiveSemidefinite(c));

        // make sure the input isn't modified
        assertEquals(2, a.get(0, 0), UtilEjml.TEST_F64);
        assertEquals(2, a.get(1, 1), UtilEjml.TEST_F64);
    }

    @Test
    public void isSquare() {
        DMatrixRow_F64 a = new DMatrixRow_F64(5,4);

        assertFalse(MatrixFeatures_R64.isSquare(a));

        a.reshape(4, 4, false);
        assertTrue(MatrixFeatures_R64.isSquare(a));
    }

    @Test
    public void isDiagonalPositive() {
        DMatrixRow_F64 m = CommonOps_R64.identity(3);
        assertTrue(MatrixFeatures_R64.isDiagonalPositive(m));

        m.set(1, 1, -1);
        assertFalse(MatrixFeatures_R64.isDiagonalPositive(m));

        m.set(1, 1, Double.NaN);
        assertFalse(MatrixFeatures_R64.isDiagonalPositive(m));
    }

    @Test
    public void isSymmetric() {
        DMatrixRow_F64 m = CommonOps_R64.identity(3);
        m.set(1, 2, 5);m.set(2,1,5);
        assertTrue(MatrixFeatures_R64.isSymmetric(m));

        m.set(1, 2, 50);
        assertTrue(!MatrixFeatures_R64.isSymmetric(m));

        m.set(1, 2, Double.NaN);
        assertTrue(!MatrixFeatures_R64.isSymmetric(m));
    }

    @Test
    public void isSkewSymmetric() {
        DMatrixRow_F64 m = CommonOps_R64.identity(3);
        m.set(1, 2, 5);m.set(2,1,-5);
        assertTrue(MatrixFeatures_R64.isSkewSymmetric(m, UtilEjml.TEST_F64));

        m.set(1, 2, -5);
        assertTrue(!MatrixFeatures_R64.isSkewSymmetric(m, UtilEjml.TEST_F64));

        m.set(1, 2, Double.NaN);
        assertTrue(!MatrixFeatures_R64.isSkewSymmetric(m, UtilEjml.TEST_F64));
    }


    @Test
    public void isEquals() {
        String a = "-0.779094   1.682750   0.039239\n" +
                 "   1.304014  -1.880739   1.438741\n" +
                 "  -0.746918   1.382356  -0.520416";

        DMatrixRow_F64 m = parse_R64(a,3);
        DMatrixRow_F64 n = parse_R64(a,3);

        assertTrue(MatrixFeatures_R64.isEquals(m,n));

        n.set(2,1,-0.5);
        assertFalse(MatrixFeatures_R64.isEquals(m,n));

        m.set(2,1,Double.NaN);
        n.set(2, 1, Double.NaN);
        assertFalse(MatrixFeatures_R64.isEquals(m, n));
        m.set(2, 1, Double.POSITIVE_INFINITY);
        n.set(2,1,Double.POSITIVE_INFINITY);
        assertTrue(MatrixFeatures_R64.isEquals(m, n));
    }

    @Test
    public void isEquals_boolean() {
         DMatrixRow_B a = new DMatrixRow_B(3,4);
         DMatrixRow_B b = new DMatrixRow_B(3,4);

         RandomMatrices_R64.setRandomB(a,rand);
         b.set(a);

         assertTrue(MatrixFeatures_R64.isEquals(a, b));

         b.data[4] = !a.data[4];
         assertFalse(MatrixFeatures_R64.isEquals(a, b));
     }

    @Test
    public void isEquals_tol() {
        String a = "-0.779094   1.682750   0.039239\n" +
                 "   1.304014  -1.880739   1.438741\n" +
                 "  -0.746918   1.382356  -0.520416";

        DMatrixRow_F64 m = parse_R64(a,3);
        DMatrixRow_F64 n = parse_R64(a,3);

        assertTrue(MatrixFeatures_R64.isEquals(m,n,UtilEjml.TEST_F64_SQ));

        n.set(2,1,n.get(2,1)+ UtilEjml.EPS);
        assertTrue(MatrixFeatures_R64.isEquals(m,n,UtilEjml.TEST_F64_SQ));

        n.set(2,1,n.get(2,1)+UtilEjml.TEST_F64_SQ);
        assertFalse(MatrixFeatures_R64.isEquals(m, n, UtilEjml.TEST_F64_SQ));

        m.set(2,1,Double.NaN);
        n.set(2, 1, Double.NaN);
        assertFalse(MatrixFeatures_R64.isEquals(m, n, UtilEjml.TEST_F64_SQ));
        m.set(2, 1, Double.POSITIVE_INFINITY);
        n.set(2,1,Double.POSITIVE_INFINITY);
        assertFalse(MatrixFeatures_R64.isEquals(m, n, UtilEjml.TEST_F64_SQ));
    }

    @Test
    public void isEqualsTriangle() {

        // see if it works with different sized matrices
        for( int m = 2; m < 10; m+=3) {
            for( int n = 2; n < 10; n += 3 ) {
                DMatrixRow_F64 a = RandomMatrices_R64.createRandom(m,n,rand);
                DMatrixRow_F64 b = a.copy();

                // make the bottom triangle not the same
                b.set(m-1,0,0);

                assertTrue("m = "+m+" n = "+n, MatrixFeatures_R64.isEqualsTriangle(a,b, true, UtilEjml.TEST_F64));
                assertFalse(MatrixFeatures_R64.isEqualsTriangle(a,b, false, UtilEjml.TEST_F64));

                // make the upper triangle not the same
                b = a.copy();
                b.set(0,n-1,0);

                assertFalse(MatrixFeatures_R64.isEqualsTriangle(a,b, true, UtilEjml.TEST_F64));
                assertTrue(MatrixFeatures_R64.isEqualsTriangle(a,b, false, UtilEjml.TEST_F64));
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
        DMatrixRow_F64 A = new DMatrixRow_F64(2,2);
        CommonOps_R64.fill(A, valA);
        DMatrixRow_F64 B = new DMatrixRow_F64(2,2);
        CommonOps_R64.fill(B, valB);

        assertEquals(expected, MatrixFeatures_R64.isIdentical(A,B,tol));
    }

    @Test
    public void isInverse() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(3,3,-1,1,rand);
        DMatrixRow_F64 A_inv = A.copy();

        CommonOps_R64.invert(A_inv);

        assertTrue(MatrixFeatures_R64.isInverse(A,A_inv,UtilEjml.TEST_F64));

        A_inv.set(1,2,3);
        assertFalse(MatrixFeatures_R64.isInverse(A,A_inv,UtilEjml.TEST_F64));

        A_inv.set(1,2,Double.NaN);
        assertFalse(MatrixFeatures_R64.isInverse(A,A_inv,UtilEjml.TEST_F64));
    }

    /**
     * Makes sure it isn't modifying the input
     */
    @Test
    public void isInverse_nomodify() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(3,3,-1,1,rand);
        DMatrixRow_F64 B = RandomMatrices_R64.createRandom(3,3,-1,1,rand);
        DMatrixRow_F64 A_copy = A.copy();
        DMatrixRow_F64 B_copy = B.copy();

        MatrixFeatures_R64.isInverse(A,B,UtilEjml.TEST_F64);

        assertTrue(MatrixFeatures_R64.isIdentical(A,A_copy,UtilEjml.TEST_F64));
        assertTrue(MatrixFeatures_R64.isIdentical(B,B_copy,UtilEjml.TEST_F64));
    }

    @Test
    public void hasNaN() {
        DMatrixRow_F64 m = new DMatrixRow_F64(3,3);
        assertFalse(MatrixFeatures_R64.hasNaN(m));

        m.set(1,2,-Double.NaN);
        assertTrue(MatrixFeatures_R64.hasNaN(m));
    }

    @Test
    public void isOrthogonal() {
        // rotation matrices are orthogonal
        double c = Math.cos(0.1);
        double s = Math.sin(0.1);

        DMatrixRow_F64 A = new DMatrixRow_F64(new double[][]{{c,s},{-s,c}});

        assertTrue(MatrixFeatures_R64.isOrthogonal(A,UtilEjml.TEST_F64_SQ));

        // try a negative case now
        A.set(0,1,495);

        assertFalse(MatrixFeatures_R64.isOrthogonal(A,UtilEjml.TEST_F64_SQ));

        A.set(0,1,Double.NaN);

        assertFalse(MatrixFeatures_R64.isOrthogonal(A,UtilEjml.TEST_F64_SQ));
    }

    @Test
    public void isRowsLinearIndependent() {
        // test a positive case
        DMatrixRow_F64 A = new DMatrixRow_F64(2,3, true, 1, 2, 3, 2, 3, 4);
        assertTrue(MatrixFeatures_R64.isRowsLinearIndependent(A));

        // make sure the input wasn't modified
        DMatrixRow_F64 A_copy = new DMatrixRow_F64(2,3, true, 1, 2, 3, 2, 3, 4);
        assertTrue(MatrixFeatures_R64.isIdentical(A,A_copy,UtilEjml.TEST_F64));

        // test negative case
        A = new DMatrixRow_F64(2,3, true, 1, 2, 3, 1, 2, 3);
        assertFalse(MatrixFeatures_R64.isRowsLinearIndependent(A));
    }

    @Test
    public void isConstantVal() {
        DMatrixRow_F64 a = new DMatrixRow_F64(3,4);

        CommonOps_R64.fill(a, 2.4);

        assertTrue(MatrixFeatures_R64.isConstantVal(a,2.4,UtilEjml.TEST_F64));
        assertFalse(MatrixFeatures_R64.isConstantVal(a,6,UtilEjml.TEST_F64));

        a.set(1,1,Double.NaN);
        assertFalse(MatrixFeatures_R64.isConstantVal(a,2.4,UtilEjml.TEST_F64));
    }

    @Test
    public void isIdentity() {
        DMatrixRow_F64 I = CommonOps_R64.identity(4);

        assertTrue(MatrixFeatures_R64.isIdentity(I,UtilEjml.TEST_F64));

        I.set(3,2,0.1);
        assertFalse(MatrixFeatures_R64.isIdentity(I,UtilEjml.TEST_F64));

        I.set(3,2,Double.NaN);
        assertFalse(MatrixFeatures_R64.isIdentity(I,UtilEjml.TEST_F64));
    }

    @Test
    public void isNegative() {
        DMatrixRow_F64 a = RandomMatrices_R64.createRandom(4,5,rand);
        DMatrixRow_F64 b = a.copy();
        CommonOps_R64.scale(-1,b);

        // test the positive case first
        assertTrue(MatrixFeatures_R64.isNegative(a,b,UtilEjml.TEST_F64));

        // now the negative case
        b.set(2,2,10);
        assertFalse(MatrixFeatures_R64.isNegative(a,b,UtilEjml.TEST_F64));

        b.set(2,2,Double.NaN);
        assertFalse(MatrixFeatures_R64.isNegative(a,b,UtilEjml.TEST_F64));
    }

    @Test
    public void isUpperTriangle() {
        // test matrices that are upper triangular to various degree hessenberg
        for( int hessenberg = 0; hessenberg < 2; hessenberg++ ) {
            DMatrixRow_F64 A = new DMatrixRow_F64(6,6);
            for( int i = 0; i < A.numRows; i++ ) {
                int s = i <= hessenberg ? 0 : i-hessenberg;

                for( int j = s; j < A.numCols; j++ ) {
                   A.set(i,j,2);
                }
            }

            // test positive
            for( int i = hessenberg; i < A.numRows; i++ ) {
                assertTrue(MatrixFeatures_R64.isUpperTriangle(A,i,UtilEjml.TEST_F64));
            }

            // test negative
            for( int i = 0; i < hessenberg; i++ ) {
                assertFalse(MatrixFeatures_R64.isUpperTriangle(A,i,UtilEjml.TEST_F64));
            }

            // see if it handles NaN well
            A.set(4,0,Double.NaN);
            assertFalse(MatrixFeatures_R64.isUpperTriangle(A,0,UtilEjml.TEST_F64));
        }
    }

    @Test
    public void isLowerTriangle() {
        // test matrices that are upper triangular to various degree hessenberg
        for( int hessenberg = 0; hessenberg < 2; hessenberg++ ) {
            DMatrixRow_F64 A = new DMatrixRow_F64(6,6);
            for( int i = 0; i < A.numRows; i++ ) {
                int s = i <= hessenberg ? 0 : i-hessenberg;

                for( int j = s; j < A.numCols; j++ ) {
                   A.set(i,j,2);
                }
            }
            CommonOps_R64.transpose(A);

            // test positive
            for( int i = hessenberg; i < A.numRows; i++ ) {
                assertTrue(MatrixFeatures_R64.isLowerTriangle(A,i,UtilEjml.TEST_F64));
            }

            // test negative
            for( int i = 0; i < hessenberg; i++ ) {
                assertFalse(MatrixFeatures_R64.isLowerTriangle(A,i,UtilEjml.TEST_F64));
            }

            // see if it handles NaN well
            A.set(0,4,Double.NaN);
            assertFalse(MatrixFeatures_R64.isLowerTriangle(A,0,UtilEjml.TEST_F64));
        }
    }


    @Test
    public void rank() {
        DMatrixRow_F64 a = UtilEjml.parse_R64("2 0 0 2",2);
        DMatrixRow_F64 a_copy = a.copy();

        assertEquals(2, MatrixFeatures_R64.rank(a));
        // make sure the input wasn't modified
        assertTrue(MatrixFeatures_R64.isIdentical(a,a_copy,UtilEjml.TEST_F64));

        a = UtilEjml.parse_R64("2 0 0 0",2);
        assertEquals(1, MatrixFeatures_R64.rank(a));
    }

    @Test
    public void rank_threshold() {
        DMatrixRow_F64 a = UtilEjml.parse_R64("2 0 0 2",2);
        DMatrixRow_F64 a_copy = a.copy();

        assertEquals(2, MatrixFeatures_R64.rank(a,10 * UtilEjml.EPS));
        // make sure the input wasn't modified
        assertTrue(MatrixFeatures_R64.isIdentical(a,a_copy,UtilEjml.TEST_F64));

        a = UtilEjml.parse_R64("2 0 0 1e-20",2);
        assertEquals(1, MatrixFeatures_R64.rank(a,10 * UtilEjml.EPS));

        // make sure it's using the threshold parameter
        a = UtilEjml.parse_R64("2 0 0 1e-20",2);
        assertEquals(2, MatrixFeatures_R64.rank(a, Math.pow(UtilEjml.EPS, 10) ));
    }

    @Test
    public void nullity() {
        DMatrixRow_F64 a = UtilEjml.parse_R64("2 0 0 2",2);
        DMatrixRow_F64 a_copy = a.copy();

        assertEquals(0, MatrixFeatures_R64.nullity(a));
        // make sure the input wasn't modified
        assertTrue(MatrixFeatures_R64.isIdentical(a,a_copy,UtilEjml.TEST_F64));

        a = UtilEjml.parse_R64("2 0 0 0",2);
        assertEquals(1, MatrixFeatures_R64.nullity(a));
    }

    @Test
    public void nullity_threshold() {
        DMatrixRow_F64 a = UtilEjml.parse_R64("2 0 0 2",2);
        DMatrixRow_F64 a_copy = a.copy();

        assertEquals(0, MatrixFeatures_R64.nullity(a, 10 * UtilEjml.EPS));
        // make sure the input wasn't modified
        assertTrue(MatrixFeatures_R64.isIdentical(a,a_copy,UtilEjml.TEST_F64));

        a = UtilEjml.parse_R64("2 0 0 1e-20",2);
        assertEquals(1, MatrixFeatures_R64.nullity(a, 10 * UtilEjml.EPS));

        // make sure it's using the threshold parameter
        a = UtilEjml.parse_R64("2 0 0 1e-20",2);
        assertEquals(0, MatrixFeatures_R64.nullity(a, Math.pow(UtilEjml.EPS, 10)));
    }
}
