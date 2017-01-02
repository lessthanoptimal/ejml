/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.DenseMatrixBool;
import org.junit.Test;

import java.util.Random;

import static org.ejml.UtilEjml.parseD64;
import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestMatrixFeatures_D64 {

    Random rand = new Random(0xff24);

    @Test
    public void hasUncountable() {
        DenseMatrix64F a = new DenseMatrix64F(4,4);

        // check a negative case first
        assertFalse(MatrixFeatures_D64.hasUncountable(a));

        // check two positve cases with different types of uncountables
        a.set(2, 2, Double.NaN);
        assertTrue(MatrixFeatures_D64.hasUncountable(a));

        a.set(2, 2, Double.POSITIVE_INFINITY);
        assertTrue(MatrixFeatures_D64.hasUncountable(a));
    }

    @Test
    public void isZeros() {
        DenseMatrix64F a = new DenseMatrix64F(4,4);
        a.set(0, 0, 1);

        assertFalse(MatrixFeatures_D64.isZeros(a, 0.1));
        assertTrue(MatrixFeatures_D64.isZeros(a, 2));
    }

    @Test
    public void isVector() {
        DenseMatrix64F a = new DenseMatrix64F(4,4);

        assertFalse(MatrixFeatures_D64.isVector(a));

        a.reshape(3, 1, false);
        assertTrue(MatrixFeatures_D64.isVector(a));

        a.reshape(1, 3, false);
        assertTrue(MatrixFeatures_D64.isVector(a));
    }

    /**
     * Check some trial cases.
     */
    @Test
    public void isPositiveDefinite() {
        DenseMatrix64F a = UtilEjml.parseD64("2 0 0 2",2);
        DenseMatrix64F b = UtilEjml.parseD64("0 1 1 0",2);
        DenseMatrix64F c = UtilEjml.parseD64("0 0 0 0",2);

        assertTrue(MatrixFeatures_D64.isPositiveDefinite(a));
        assertFalse(MatrixFeatures_D64.isPositiveDefinite(b));
        assertFalse(MatrixFeatures_D64.isPositiveDefinite(c));

        // make sure the input isn't modified
        assertEquals(2, a.get(0, 0), UtilEjml.TEST_64F);
        assertEquals(2, a.get(1, 1), UtilEjml.TEST_64F);
    }

    @Test
    public void isPositiveSemidefinite() {
        DenseMatrix64F a = UtilEjml.parseD64("2 0 0 2",2);
        DenseMatrix64F b = UtilEjml.parseD64("0 1 1 0",2);
        DenseMatrix64F c = UtilEjml.parseD64("0 0 0 0", 2);

        assertTrue(MatrixFeatures_D64.isPositiveSemidefinite(a));
        assertFalse(MatrixFeatures_D64.isPositiveSemidefinite(b));
        assertTrue(MatrixFeatures_D64.isPositiveSemidefinite(c));

        // make sure the input isn't modified
        assertEquals(2, a.get(0, 0), UtilEjml.TEST_64F);
        assertEquals(2, a.get(1, 1), UtilEjml.TEST_64F);
    }

    @Test
    public void isSquare() {
        DenseMatrix64F a = new DenseMatrix64F(5,4);

        assertFalse(MatrixFeatures_D64.isSquare(a));

        a.reshape(4, 4, false);
        assertTrue(MatrixFeatures_D64.isSquare(a));
    }

    @Test
    public void isDiagonalPositive() {
        DenseMatrix64F m = CommonOps_D64.identity(3);
        assertTrue(MatrixFeatures_D64.isDiagonalPositive(m));

        m.set(1, 1, -1);
        assertFalse(MatrixFeatures_D64.isDiagonalPositive(m));

        m.set(1, 1, Double.NaN);
        assertFalse(MatrixFeatures_D64.isDiagonalPositive(m));
    }

    @Test
    public void isSymmetric() {
        DenseMatrix64F m = CommonOps_D64.identity(3);
        m.set(1, 2, 5);m.set(2,1,5);
        assertTrue(MatrixFeatures_D64.isSymmetric(m));

        m.set(1, 2, 50);
        assertTrue(!MatrixFeatures_D64.isSymmetric(m));

        m.set(1, 2, Double.NaN);
        assertTrue(!MatrixFeatures_D64.isSymmetric(m));
    }

    @Test
    public void isSkewSymmetric() {
        DenseMatrix64F m = CommonOps_D64.identity(3);
        m.set(1, 2, 5);m.set(2,1,-5);
        assertTrue(MatrixFeatures_D64.isSkewSymmetric(m, UtilEjml.TEST_64F));

        m.set(1, 2, -5);
        assertTrue(!MatrixFeatures_D64.isSkewSymmetric(m, UtilEjml.TEST_64F));

        m.set(1, 2, Double.NaN);
        assertTrue(!MatrixFeatures_D64.isSkewSymmetric(m, UtilEjml.TEST_64F));
    }


    @Test
    public void isEquals() {
        String a = "-0.779094   1.682750   0.039239\n" +
                 "   1.304014  -1.880739   1.438741\n" +
                 "  -0.746918   1.382356  -0.520416";

        DenseMatrix64F m = parseD64(a,3);
        DenseMatrix64F n = parseD64(a,3);

        assertTrue(MatrixFeatures_D64.isEquals(m,n));

        n.set(2,1,-0.5);
        assertFalse(MatrixFeatures_D64.isEquals(m,n));

        m.set(2,1,Double.NaN);
        n.set(2, 1, Double.NaN);
        assertFalse(MatrixFeatures_D64.isEquals(m, n));
        m.set(2, 1, Double.POSITIVE_INFINITY);
        n.set(2,1,Double.POSITIVE_INFINITY);
        assertTrue(MatrixFeatures_D64.isEquals(m, n));
    }

    @Test
    public void isEquals_boolean() {
         DenseMatrixBool a = new DenseMatrixBool(3,4);
         DenseMatrixBool b = new DenseMatrixBool(3,4);

         RandomMatrices_D64.setRandomB(a,rand);
         b.set(a);

         assertTrue(MatrixFeatures_D64.isEquals(a, b));

         b.data[4] = !a.data[4];
         assertFalse(MatrixFeatures_D64.isEquals(a, b));
     }

    @Test
    public void isEquals_tol() {
        String a = "-0.779094   1.682750   0.039239\n" +
                 "   1.304014  -1.880739   1.438741\n" +
                 "  -0.746918   1.382356  -0.520416";

        DenseMatrix64F m = parseD64(a,3);
        DenseMatrix64F n = parseD64(a,3);

        assertTrue(MatrixFeatures_D64.isEquals(m,n,1e-6));

        n.set(2,1,n.get(2,1)+1e-25);
        assertTrue(MatrixFeatures_D64.isEquals(m,n,1e-6));

        n.set(2,1,n.get(2,1)+1e-2);
        assertFalse(MatrixFeatures_D64.isEquals(m, n, 1e-6));

        m.set(2,1,Double.NaN);
        n.set(2, 1, Double.NaN);
        assertFalse(MatrixFeatures_D64.isEquals(m, n, 1e-6));
        m.set(2, 1, Double.POSITIVE_INFINITY);
        n.set(2,1,Double.POSITIVE_INFINITY);
        assertFalse(MatrixFeatures_D64.isEquals(m, n, 1e-6));
    }

    @Test
    public void isEqualsTriangle() {

        // see if it works with different sized matrices
        for( int m = 2; m < 10; m+=3) {
            for( int n = 2; n < 10; n += 3 ) {
                DenseMatrix64F a = RandomMatrices_D64.createRandom(m,n,rand);
                DenseMatrix64F b = a.copy();

                // make the bottom triangle not the same
                b.set(m-1,0,0);

                assertTrue("m = "+m+" n = "+n, MatrixFeatures_D64.isEqualsTriangle(a,b, true, UtilEjml.TEST_64F));
                assertFalse(MatrixFeatures_D64.isEqualsTriangle(a,b, false, UtilEjml.TEST_64F));

                // make the upper triangle not the same
                b = a.copy();
                b.set(0,n-1,0);

                assertFalse(MatrixFeatures_D64.isEqualsTriangle(a,b, true, UtilEjml.TEST_64F));
                assertTrue(MatrixFeatures_D64.isEqualsTriangle(a,b, false, UtilEjml.TEST_64F));
            }
        }
    }

    @Test
    public void isIdentical() {

        double values[] = new double[]{1.0,Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY};

        for( int i = 0; i < values.length; i++ ) {
            for( int j = 0; j < values.length; j++ ) {
                checkIdentical(values[i],values[j],1e-8,i==j);
            }
        }

        checkIdentical(1.0,1.5,1e-8,false);
        checkIdentical(1.5,1.0,1e-8,false);
        checkIdentical(1.0,1.0000000001,1e-8,true);
        checkIdentical(1.0,Double.NaN,1e-8,false);
        checkIdentical(Double.NaN,1.0,1e-8,false);
    }

    private void checkIdentical( double valA , double valB , double tol , boolean expected ) {
        DenseMatrix64F A = new DenseMatrix64F(2,2);
        CommonOps_D64.fill(A, valA);
        DenseMatrix64F B = new DenseMatrix64F(2,2);
        CommonOps_D64.fill(B, valB);

        assertEquals(expected, MatrixFeatures_D64.isIdentical(A,B,tol));
    }

    @Test
    public void isInverse() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(3,3,-1,1,rand);
        DenseMatrix64F A_inv = A.copy();

        CommonOps_D64.invert(A_inv);

        assertTrue(MatrixFeatures_D64.isInverse(A,A_inv,1e-10));

        A_inv.set(1,2,3);
        assertFalse(MatrixFeatures_D64.isInverse(A,A_inv,1e-10));

        A_inv.set(1,2,Double.NaN);
        assertFalse(MatrixFeatures_D64.isInverse(A,A_inv,1e-10));
    }

    /**
     * Makes sure it isn't modifying the input
     */
    @Test
    public void isInverse_nomodify() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(3,3,-1,1,rand);
        DenseMatrix64F B = RandomMatrices_D64.createRandom(3,3,-1,1,rand);
        DenseMatrix64F A_copy = A.copy();
        DenseMatrix64F B_copy = B.copy();

        MatrixFeatures_D64.isInverse(A,B,1e-10);

        assertTrue(MatrixFeatures_D64.isIdentical(A,A_copy,UtilEjml.TEST_64F));
        assertTrue(MatrixFeatures_D64.isIdentical(B,B_copy,UtilEjml.TEST_64F));
    }

    @Test
    public void hasNaN() {
        DenseMatrix64F m = new DenseMatrix64F(3,3);
        assertFalse(MatrixFeatures_D64.hasNaN(m));

        m.set(1,2,-Double.NaN);
        assertTrue(MatrixFeatures_D64.hasNaN(m));
    }

    @Test
    public void isOrthogonal() {
        // rotation matrices are orthogonal
        double c = Math.cos(0.1);
        double s = Math.sin(0.1);

        DenseMatrix64F A = new DenseMatrix64F(new double[][]{{c,s},{-s,c}});

        assertTrue(MatrixFeatures_D64.isOrthogonal(A,1e-6f));

        // try a negative case now
        A.set(0,1,495);

        assertFalse(MatrixFeatures_D64.isOrthogonal(A,1e-6f));

        A.set(0,1,Double.NaN);

        assertFalse(MatrixFeatures_D64.isOrthogonal(A,1e-6f));
    }

    @Test
    public void isRowsLinearIndependent() {
        // test a positive case
        DenseMatrix64F A = new DenseMatrix64F(2,3, true, 1, 2, 3, 2, 3, 4);
        assertTrue(MatrixFeatures_D64.isRowsLinearIndependent(A));

        // make sure the input wasn't modified
        DenseMatrix64F A_copy = new DenseMatrix64F(2,3, true, 1, 2, 3, 2, 3, 4);
        assertTrue(MatrixFeatures_D64.isIdentical(A,A_copy,UtilEjml.TEST_64F));

        // test negative case
        A = new DenseMatrix64F(2,3, true, 1, 2, 3, 1, 2, 3);
        assertFalse(MatrixFeatures_D64.isRowsLinearIndependent(A));
    }

    @Test
    public void isConstantVal() {
        DenseMatrix64F a = new DenseMatrix64F(3,4);

        CommonOps_D64.fill(a, 2.4);

        assertTrue(MatrixFeatures_D64.isConstantVal(a,2.4,UtilEjml.TEST_64F));
        assertFalse(MatrixFeatures_D64.isConstantVal(a,6,UtilEjml.TEST_64F));

        a.set(1,1,Double.NaN);
        assertFalse(MatrixFeatures_D64.isConstantVal(a,2.4,UtilEjml.TEST_64F));
    }

    @Test
    public void isIdentity() {
        DenseMatrix64F I = CommonOps_D64.identity(4);

        assertTrue(MatrixFeatures_D64.isIdentity(I,UtilEjml.TEST_64F));

        I.set(3,2,0.1);
        assertFalse(MatrixFeatures_D64.isIdentity(I,UtilEjml.TEST_64F));

        I.set(3,2,Double.NaN);
        assertFalse(MatrixFeatures_D64.isIdentity(I,UtilEjml.TEST_64F));
    }

    @Test
    public void isNegative() {
        DenseMatrix64F a = RandomMatrices_D64.createRandom(4,5,rand);
        DenseMatrix64F b = a.copy();
        CommonOps_D64.scale(-1,b);

        // test the positive case first
        assertTrue(MatrixFeatures_D64.isNegative(a,b,UtilEjml.TEST_64F));

        // now the negative case
        b.set(2,2,10);
        assertFalse(MatrixFeatures_D64.isNegative(a,b,UtilEjml.TEST_64F));

        b.set(2,2,Double.NaN);
        assertFalse(MatrixFeatures_D64.isNegative(a,b,UtilEjml.TEST_64F));
    }

    @Test
    public void isUpperTriangle() {
        // test matrices that are upper triangular to various degree hessenberg
        for( int hessenberg = 0; hessenberg < 2; hessenberg++ ) {
            DenseMatrix64F A = new DenseMatrix64F(6,6);
            for( int i = 0; i < A.numRows; i++ ) {
                int s = i <= hessenberg ? 0 : i-hessenberg;

                for( int j = s; j < A.numCols; j++ ) {
                   A.set(i,j,2);
                }
            }

            // test positive
            for( int i = hessenberg; i < A.numRows; i++ ) {
                assertTrue(MatrixFeatures_D64.isUpperTriangle(A,i,UtilEjml.TEST_64F));
            }

            // test negative
            for( int i = 0; i < hessenberg; i++ ) {
                assertFalse(MatrixFeatures_D64.isUpperTriangle(A,i,UtilEjml.TEST_64F));
            }

            // see if it handles NaN well
            A.set(4,0,Double.NaN);
            assertFalse(MatrixFeatures_D64.isUpperTriangle(A,0,UtilEjml.TEST_64F));
        }
    }

    @Test
    public void isLowerTriangle() {
        // test matrices that are upper triangular to various degree hessenberg
        for( int hessenberg = 0; hessenberg < 2; hessenberg++ ) {
            DenseMatrix64F A = new DenseMatrix64F(6,6);
            for( int i = 0; i < A.numRows; i++ ) {
                int s = i <= hessenberg ? 0 : i-hessenberg;

                for( int j = s; j < A.numCols; j++ ) {
                   A.set(i,j,2);
                }
            }
            CommonOps_D64.transpose(A);

            // test positive
            for( int i = hessenberg; i < A.numRows; i++ ) {
                assertTrue(MatrixFeatures_D64.isLowerTriangle(A,i,UtilEjml.TEST_64F));
            }

            // test negative
            for( int i = 0; i < hessenberg; i++ ) {
                assertFalse(MatrixFeatures_D64.isLowerTriangle(A,i,UtilEjml.TEST_64F));
            }

            // see if it handles NaN well
            A.set(0,4,Double.NaN);
            assertFalse(MatrixFeatures_D64.isLowerTriangle(A,0,UtilEjml.TEST_64F));
        }
    }


    @Test
    public void rank() {
        DenseMatrix64F a = UtilEjml.parseD64("2 0 0 2",2);
        DenseMatrix64F a_copy = a.copy();

        assertEquals(2, MatrixFeatures_D64.rank(a));
        // make sure the input wasn't modified
        assertTrue(MatrixFeatures_D64.isIdentical(a,a_copy,UtilEjml.TEST_64F));

        a = UtilEjml.parseD64("2 0 0 0",2);
        assertEquals(1, MatrixFeatures_D64.rank(a));
    }

    @Test
    public void rank_threshold() {
        DenseMatrix64F a = UtilEjml.parseD64("2 0 0 2",2);
        DenseMatrix64F a_copy = a.copy();

        assertEquals(2, MatrixFeatures_D64.rank(a,1e-14));
        // make sure the input wasn't modified
        assertTrue(MatrixFeatures_D64.isIdentical(a,a_copy,UtilEjml.TEST_64F));

        a = UtilEjml.parseD64("2 0 0 1e-20",2);
        assertEquals(1, MatrixFeatures_D64.rank(a,1e-14));

        // make sure it's using the threshold parameter
        a = UtilEjml.parseD64("2 0 0 1e-20",2);
        assertEquals(2, MatrixFeatures_D64.rank(a,1e-200));
    }

    @Test
    public void nullity() {
        DenseMatrix64F a = UtilEjml.parseD64("2 0 0 2",2);
        DenseMatrix64F a_copy = a.copy();

        assertEquals(0, MatrixFeatures_D64.nullity(a));
        // make sure the input wasn't modified
        assertTrue(MatrixFeatures_D64.isIdentical(a,a_copy,UtilEjml.TEST_64F));

        a = UtilEjml.parseD64("2 0 0 0",2);
        assertEquals(1, MatrixFeatures_D64.nullity(a));
    }

    @Test
    public void nullity_threshold() {
        DenseMatrix64F a = UtilEjml.parseD64("2 0 0 2",2);
        DenseMatrix64F a_copy = a.copy();

        assertEquals(0, MatrixFeatures_D64.nullity(a, 1e-14));
        // make sure the input wasn't modified
        assertTrue(MatrixFeatures_D64.isIdentical(a,a_copy,UtilEjml.TEST_64F));

        a = UtilEjml.parseD64("2 0 0 1e-20",2);
        assertEquals(1, MatrixFeatures_D64.nullity(a, 1e-14));

        // make sure it's using the threshold parameter
        a = UtilEjml.parseD64("2 0 0 1e-20",2);
        assertEquals(0, MatrixFeatures_D64.nullity(a, 1e-200));
    }
}
