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

package org.ejml.sparse.cmpcol;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.SMatrixCmpC_F64;
import org.ejml.data.SMatrixTriplet_F64;
import org.ejml.dense.row.CommonOps_R64;
import org.ejml.dense.row.MatrixFeatures_R64;
import org.ejml.sparse.ConvertSparseMatrix_F64;
import org.junit.Test;

import java.util.Random;

import static org.ejml.dense.row.RandomMatrices_R64.createRandom;
import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestCommonOps_O64 {

    Random rand = new Random(234);

    @Test
    public void isRowOrderValid() {
        SMatrixTriplet_F64 orig = new SMatrixTriplet_F64(3,5,6);

        orig.addItem(0,0, 5);
        orig.addItem(1,0, 6);
        orig.addItem(2,0, 7);

        orig.addItem(1,2, 5);

        SMatrixCmpC_F64 a = ConvertSparseMatrix_F64.convert(orig,(SMatrixCmpC_F64)null);

        // test positive case first
        assertTrue(CommonOps_O64.checkIndicesSorted(a));

        // test negative case second
        a.nz_rows[1] = 3;
        assertFalse(CommonOps_O64.checkIndicesSorted(a));
    }

    @Test
    public void transpose_shapes() {
        CommonOps_O64.transpose(
                RandomMatrices_O64.rectangle(5,5,5,rand),
                RandomMatrices_O64.rectangle(5,5,7,rand),null);
        CommonOps_O64.transpose(
                RandomMatrices_O64.rectangle(4,5,5,rand),
                RandomMatrices_O64.rectangle(5,4,7,rand),null);

        try {
            CommonOps_O64.transpose(
                    RandomMatrices_O64.rectangle(4,5,5,rand),
                    RandomMatrices_O64.rectangle(6,4,7,rand),null);
            fail("exception expected");
        } catch( RuntimeException ignore){}

        try {
            CommonOps_O64.transpose(
                    RandomMatrices_O64.rectangle(4,5,5,rand),
                    RandomMatrices_O64.rectangle(5,5,7,rand),null);
            fail("exception expected");
        } catch( RuntimeException ignore){}

    }

    @Test
    public void mult_s_s_shapes() {
        check_s_s_mult(
                RandomMatrices_O64.rectangle(5, 6, 5, rand),
                RandomMatrices_O64.rectangle(6, 4, 7, rand),
                RandomMatrices_O64.rectangle(5, 4, 7, rand), false);

        check_s_s_mult(
                RandomMatrices_O64.rectangle(5, 6, 5, rand),
                RandomMatrices_O64.rectangle(6, 4, 7, rand),
                RandomMatrices_O64.rectangle(5, 5, 7, rand), true);
        check_s_s_mult(
                RandomMatrices_O64.rectangle(5, 6, 5, rand),
                RandomMatrices_O64.rectangle(6, 4, 7, rand),
                RandomMatrices_O64.rectangle(6, 4, 7, rand), true);
        check_s_s_mult(
                RandomMatrices_O64.rectangle(5, 6, 5, rand),
                RandomMatrices_O64.rectangle(6, 4, 7, rand),
                RandomMatrices_O64.rectangle(6, 4, 7, rand), true);
    }

    private void check_s_s_mult(SMatrixCmpC_F64 A , SMatrixCmpC_F64 B, SMatrixCmpC_F64 C, boolean exception ) {
        try {
            CommonOps_O64.mult(A,B,C,null,null);
            assertTrue(CommonOps_O64.checkSortedFlag(C));


            if( exception )
                fail("exception expected");
            DMatrixRow_F64 denseA = ConvertSparseMatrix_F64.convert(A,(DMatrixRow_F64)null);
            DMatrixRow_F64 denseB = ConvertSparseMatrix_F64.convert(B,(DMatrixRow_F64)null);
            DMatrixRow_F64 expected = new DMatrixRow_F64(A.numRows,B.numCols);

            CommonOps_R64.mult(denseA,denseB,expected);

            DMatrixRow_F64 found = ConvertSparseMatrix_F64.convert(C,(DMatrixRow_F64)null);
            assertTrue(MatrixFeatures_R64.isIdentical(expected,found, UtilEjml.TEST_F64));

        } catch( RuntimeException ignore){
            if( !exception )
                fail("no exception expected");
        }
    }

    @Test
    public void mult_s_d_shapes() {
        check_s_d_mult(
                RandomMatrices_O64.rectangle(5, 6, 5, rand),
                createRandom(6, 4, rand),
                createRandom(5, 4, rand), false);

        check_s_d_mult(
                RandomMatrices_O64.rectangle(5, 6, 5, rand),
                createRandom(6, 4, rand),
                createRandom(5, 5, rand), true);
        check_s_d_mult(
                RandomMatrices_O64.rectangle(5, 6, 5, rand),
                createRandom(6, 4, rand),
                createRandom(6, 4, rand), true);
        check_s_d_mult(
                RandomMatrices_O64.rectangle(5, 6, 5, rand),
                createRandom(6, 4, rand),
                createRandom(6, 4, rand), true);
    }

    private void check_s_d_mult(SMatrixCmpC_F64 A , DMatrixRow_F64 B, DMatrixRow_F64 found, boolean exception ) {
        try {
            CommonOps_O64.mult(A,B,found);

            if( exception )
                fail("exception expected");
            DMatrixRow_F64 denseA = ConvertSparseMatrix_F64.convert(A,(DMatrixRow_F64)null);
            DMatrixRow_F64 expected = new DMatrixRow_F64(A.numRows,B.numCols);

            CommonOps_R64.mult(denseA,B,expected);

            assertTrue(MatrixFeatures_R64.isIdentical(expected,found, UtilEjml.TEST_F64));

        } catch( RuntimeException ignore){
            if( !exception )
                fail("no exception expected");
        }
    }

    @Test
    public void add_shapes() {
        check_add(
                RandomMatrices_O64.rectangle(5, 6, 5, rand),
                RandomMatrices_O64.rectangle(5, 6, 5, rand),
                RandomMatrices_O64.rectangle(5, 6, 5, rand), false);
        check_add(
                RandomMatrices_O64.rectangle(5, 6, 5*6, rand),
                RandomMatrices_O64.rectangle(5, 6, 5*6, rand),
                RandomMatrices_O64.rectangle(5, 6, 5*6, rand), false);
        check_add(
                RandomMatrices_O64.rectangle(5, 6, 0, rand),
                RandomMatrices_O64.rectangle(5, 6, 0, rand),
                RandomMatrices_O64.rectangle(5, 6, 0, rand), false);
        check_add(
                RandomMatrices_O64.rectangle(5, 6, 20, rand),
                RandomMatrices_O64.rectangle(5, 6, 16, rand),
                RandomMatrices_O64.rectangle(5, 6, 0, rand), false);
        check_add(
                RandomMatrices_O64.rectangle(5, 6, 5, rand),
                RandomMatrices_O64.rectangle(5, 5, 5, rand),
                RandomMatrices_O64.rectangle(5, 6, 5, rand), true);
        check_add(
                RandomMatrices_O64.rectangle(5, 6, 5, rand),
                RandomMatrices_O64.rectangle(5, 6, 5, rand),
                RandomMatrices_O64.rectangle(5, 5, 5, rand), true);
        check_add(
                RandomMatrices_O64.rectangle(5, 6, 5, rand),
                RandomMatrices_O64.rectangle(4, 6, 5, rand),
                RandomMatrices_O64.rectangle(5, 6, 5, rand), true);
        check_add(
                RandomMatrices_O64.rectangle(5, 6, 5, rand),
                RandomMatrices_O64.rectangle(5, 6, 5, rand),
                RandomMatrices_O64.rectangle(4, 6, 5, rand), true);

    }

    private void check_add(SMatrixCmpC_F64 A , SMatrixCmpC_F64 B, SMatrixCmpC_F64 C, boolean exception ) {
        double alpha = 1.5;
        double beta = -0.6;
        try {
            CommonOps_O64.add(alpha,A,beta,B,C,null, null);
            assertTrue(CommonOps_O64.checkSortedFlag(C));

            if( exception )
                fail("exception expected");
            DMatrixRow_F64 denseA = ConvertSparseMatrix_F64.convert(A,(DMatrixRow_F64)null);
            DMatrixRow_F64 denseB = ConvertSparseMatrix_F64.convert(B,(DMatrixRow_F64)null);
            DMatrixRow_F64 expected = new DMatrixRow_F64(A.numRows,B.numCols);

            CommonOps_R64.add(alpha,denseA,beta,denseB,expected);

            DMatrixRow_F64 found = ConvertSparseMatrix_F64.convert(C,(DMatrixRow_F64)null);
            assertTrue(MatrixFeatures_R64.isIdentical(expected,found, UtilEjml.TEST_F64));

        } catch( RuntimeException ignore){
            if( !exception )
                fail("no exception expected");
        }
    }

    @Test
    public void identity_r_c() {
        identity_r_c(CommonOps_O64.identity(10,15));
        identity_r_c(CommonOps_O64.identity(15,10));
        identity_r_c(CommonOps_O64.identity(10,10));
    }

    public void identity_r_c( SMatrixCmpC_F64 A) {
        assertTrue(CommonOps_O64.checkSortedFlag(A));
        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                if( row == col )
                    assertEquals(1.0,A.get(row,col), UtilEjml.TEST_F64);
                else
                    assertEquals(0.0,A.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }


    @Test
    public void scale() {

        double scale = 2.1;

        for( int length : new int[]{0,2,6,15,30} ) {
            SMatrixCmpC_F64 A = RandomMatrices_O64.rectangle(6,5,length,rand);
            DMatrixRow_F64  Ad = ConvertSparseMatrix_F64.convert(A,(DMatrixRow_F64)null);

            SMatrixCmpC_F64 B = new SMatrixCmpC_F64(A.numRows,A.numCols,0);
            DMatrixRow_F64 expected = new DMatrixRow_F64(A.numRows,A.numCols);

            CommonOps_O64.scale(scale, A, B);
            CommonOps_R64.scale(scale,Ad,expected);

            DMatrixRow_F64 found = ConvertSparseMatrix_F64.convert(B,(DMatrixRow_F64)null);

            assertTrue(MatrixFeatures_R64.isEquals(expected,found, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void divide() {
        double denominator = 2.1;

        for( int length : new int[]{0,2,6,15,30} ) {
            SMatrixCmpC_F64 A = RandomMatrices_O64.rectangle(6,5,length,rand);
            DMatrixRow_F64  Ad = ConvertSparseMatrix_F64.convert(A,(DMatrixRow_F64)null);

            SMatrixCmpC_F64 B = new SMatrixCmpC_F64(A.numRows,A.numCols,0);
            DMatrixRow_F64 expected = new DMatrixRow_F64(A.numRows,A.numCols);

            CommonOps_O64.divide(A,denominator, B);
            CommonOps_R64.divide(Ad,denominator, expected);

            DMatrixRow_F64 found = ConvertSparseMatrix_F64.convert(B,(DMatrixRow_F64)null);

            assertTrue(MatrixFeatures_R64.isEquals(expected,found, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void elementMinAbs() {
        for( int length : new int[]{0,2,6,15,30} ) {
            SMatrixCmpC_F64 A = RandomMatrices_O64.rectangle(6,5,length,rand);
            DMatrixRow_F64 Ad = ConvertSparseMatrix_F64.convert(A,(DMatrixRow_F64)null);

            double found = CommonOps_O64.elementMinAbs(A);
            double expected = CommonOps_R64.elementMinAbs(Ad);

            assertEquals(expected,found, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementMaxAbs() {
        for( int length : new int[]{0,2,6,15,30} ) {
            SMatrixCmpC_F64 A = RandomMatrices_O64.rectangle(6,5,length,rand);

            double found = CommonOps_O64.elementMaxAbs(A);
            double expected = CommonOps_R64.elementMaxAbs(ConvertSparseMatrix_F64.convert(A,(DMatrixRow_F64)null));

            assertEquals(expected,found, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementMin() {
        for( int length : new int[]{0,2,6,15,30} ) {
            SMatrixCmpC_F64 A = RandomMatrices_O64.rectangle(6,5,length,1,3,rand);

            double found = CommonOps_O64.elementMin(A);
            double expected = CommonOps_R64.elementMin(ConvertSparseMatrix_F64.convert(A,(DMatrixRow_F64)null));

            assertEquals(expected,found, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementMax() {
        for( int length : new int[]{0,2,6,15,30} ) {
            SMatrixCmpC_F64 A = RandomMatrices_O64.rectangle(6,5,length,-2,-1,rand);

            double found = CommonOps_O64.elementMax(A);
            double expected = CommonOps_R64.elementMax(ConvertSparseMatrix_F64.convert(A,(DMatrixRow_F64)null));

            assertEquals(expected,found, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void diag() {
        double d[] = new double[]{1.2,2.2,3.3};

        SMatrixCmpC_F64 A = CommonOps_O64.diag(d);

        assertEquals(3,A.numRows);
        assertEquals(3,A.numCols);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if( row != col ) {
                    assertEquals(0,A.get(row,col), UtilEjml.TEST_F64);
                } else {
                    assertEquals(d[row],A.get(row,col), UtilEjml.TEST_F64);
                }
            }
        }
    }
}