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

package org.ejml.sparse.csc;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.ops.ConvertDMatrixSparse;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestCommonOps_DSCC {

    Random rand = new Random(234);

    @Test
    public void isRowOrderValid() {
        DMatrixSparseTriplet orig = new DMatrixSparseTriplet(3,5,6);

        orig.addItem(0,0, 5);
        orig.addItem(1,0, 6);
        orig.addItem(2,0, 7);

        orig.addItem(1,2, 5);

        DMatrixSparseCSC a = ConvertDMatrixSparse.convert(orig,(DMatrixSparseCSC)null);

        // test positive case first
        assertTrue(CommonOps_DSCC.checkIndicesSorted(a));

        // test negative case second
        a.nz_rows[1] = 3;
        assertFalse(CommonOps_DSCC.checkIndicesSorted(a));
    }

    @Test
    public void checkDuplicates() {
        DMatrixSparseCSC orig = new DMatrixSparseCSC(3,2,6);
        orig.nz_length = 6;

        orig.col_idx[0] = 0;
        orig.col_idx[1] = 3;
        orig.col_idx[2] = 6;

        orig.nz_rows[0] = 0;
        orig.nz_rows[1] = 1;
        orig.nz_rows[2] = 2;
        orig.nz_rows[3] = 0;
        orig.nz_rows[4] = 1;
        orig.nz_rows[5] = 2;

        assertFalse(CommonOps_DSCC.checkDuplicateElements(orig));
        orig.nz_rows[1] = 2;
        assertTrue(CommonOps_DSCC.checkDuplicateElements(orig));

    }

    @Test
    public void transpose_shapes() {
        CommonOps_DSCC.transpose(
                RandomMatrices_DSCC.rectangle(5,5,5,rand),
                RandomMatrices_DSCC.rectangle(5,5,7,rand),null);
        CommonOps_DSCC.transpose(
                RandomMatrices_DSCC.rectangle(4,5,5,rand),
                RandomMatrices_DSCC.rectangle(5,4,7,rand),null);

        try {
            CommonOps_DSCC.transpose(
                    RandomMatrices_DSCC.rectangle(4,5,5,rand),
                    RandomMatrices_DSCC.rectangle(6,4,7,rand),null);
            fail("exception expected");
        } catch( RuntimeException ignore){}

        try {
            CommonOps_DSCC.transpose(
                    RandomMatrices_DSCC.rectangle(4,5,5,rand),
                    RandomMatrices_DSCC.rectangle(5,5,7,rand),null);
            fail("exception expected");
        } catch( RuntimeException ignore){}

    }

    @Test
    public void mult_s_s_shapes() {
        check_s_s_mult(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(6, 4, 7, rand),
                RandomMatrices_DSCC.rectangle(5, 4, 7, rand), false);

        check_s_s_mult(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(6, 4, 7, rand),
                RandomMatrices_DSCC.rectangle(5, 5, 7, rand), true);
        check_s_s_mult(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(6, 4, 7, rand),
                RandomMatrices_DSCC.rectangle(6, 4, 7, rand), true);
        check_s_s_mult(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(6, 4, 7, rand),
                RandomMatrices_DSCC.rectangle(6, 4, 7, rand), true);
    }

    private void check_s_s_mult(DMatrixSparseCSC A , DMatrixSparseCSC B, DMatrixSparseCSC C, boolean exception ) {
        try {
            CommonOps_DSCC.mult(A,B,C,null,null);
            assertTrue(CommonOps_DSCC.checkStructure(C));

            if( exception )
                fail("exception expected");
            DMatrixRMaj denseA = ConvertDMatrixSparse.convert(A,(DMatrixRMaj)null);
            DMatrixRMaj denseB = ConvertDMatrixSparse.convert(B,(DMatrixRMaj)null);
            DMatrixRMaj expected = new DMatrixRMaj(A.numRows,B.numCols);

            CommonOps_DDRM.mult(denseA,denseB,expected);

            DMatrixRMaj found = ConvertDMatrixSparse.convert(C,(DMatrixRMaj)null);
            assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found, UtilEjml.TEST_F64));

        } catch( RuntimeException ignore){
            if( !exception )
                fail("no exception expected");
        }
    }

    @Test
    public void mult_s_d_shapes() {
        check_s_d_mult(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DDRM.rectangle(6, 4, rand),
                RandomMatrices_DDRM.rectangle(5, 4, rand), false);

        check_s_d_mult(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DDRM.rectangle(6, 4, rand),
                RandomMatrices_DDRM.rectangle(5, 5, rand), true);
        check_s_d_mult(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DDRM.rectangle(6, 4, rand),
                RandomMatrices_DDRM.rectangle(6, 4, rand), true);
        check_s_d_mult(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DDRM.rectangle(6, 4, rand),
                RandomMatrices_DDRM.rectangle(6, 4, rand), true);
    }

    private void check_s_d_mult(DMatrixSparseCSC A , DMatrixRMaj B, DMatrixRMaj found, boolean exception ) {
        try {
            CommonOps_DSCC.mult(A,B,found);

            if( exception )
                fail("exception expected");
            DMatrixRMaj denseA = ConvertDMatrixSparse.convert(A,(DMatrixRMaj)null);
            DMatrixRMaj expected = new DMatrixRMaj(A.numRows,B.numCols);

            CommonOps_DDRM.mult(denseA,B,expected);

            assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found, UtilEjml.TEST_F64));

        } catch( RuntimeException ignore){
            if( !exception )
                fail("no exception expected");
        }
    }

    @Test
    public void add_shapes() {
        check_add(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand), false);
        check_add(
                RandomMatrices_DSCC.rectangle(5, 6, 5*6, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 5*6, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 5*6, rand), false);
        check_add(
                RandomMatrices_DSCC.rectangle(5, 6, 0, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 0, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 0, rand), false);
        check_add(
                RandomMatrices_DSCC.rectangle(5, 6, 20, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 16, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 0, rand), false);
        check_add(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(5, 5, 5, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand), true);
        check_add(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(5, 5, 5, rand), true);
        check_add(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(4, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand), true);
        check_add(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(4, 6, 5, rand), true);

    }

    private void check_add(DMatrixSparseCSC A , DMatrixSparseCSC B, DMatrixSparseCSC C, boolean exception ) {
        double alpha = 1.5;
        double beta = -0.6;
        try {
            CommonOps_DSCC.add(alpha,A,beta,B,C, null, null);
            assertTrue(CommonOps_DSCC.checkStructure(C));

            if( exception )
                fail("exception expected");
            DMatrixRMaj denseA = ConvertDMatrixSparse.convert(A,(DMatrixRMaj)null);
            DMatrixRMaj denseB = ConvertDMatrixSparse.convert(B,(DMatrixRMaj)null);
            DMatrixRMaj expected = new DMatrixRMaj(A.numRows,B.numCols);

            CommonOps_DDRM.add(alpha,denseA,beta,denseB,expected);

            DMatrixRMaj found = ConvertDMatrixSparse.convert(C,(DMatrixRMaj)null);
            assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found, UtilEjml.TEST_F64));

        } catch( RuntimeException ignore){
            if( !exception )
                fail("no exception expected");
        }
    }

    @Test
    public void identity_r_c() {
        identity_r_c(CommonOps_DSCC.identity(10,15));
        identity_r_c(CommonOps_DSCC.identity(15,10));
        identity_r_c(CommonOps_DSCC.identity(10,10));
    }

    public void identity_r_c( DMatrixSparseCSC A) {
        assertTrue(CommonOps_DSCC.checkSortedFlag(A));
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
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,5,length,rand);
            DMatrixRMaj  Ad = ConvertDMatrixSparse.convert(A,(DMatrixRMaj)null);

            DMatrixSparseCSC B = new DMatrixSparseCSC(A.numRows,A.numCols,0);
            DMatrixRMaj expected = new DMatrixRMaj(A.numRows,A.numCols);

            CommonOps_DSCC.scale(scale, A, B);
            CommonOps_DDRM.scale(scale,Ad,expected);

            assertTrue(CommonOps_DSCC.checkStructure(B));
            DMatrixRMaj found = ConvertDMatrixSparse.convert(B,(DMatrixRMaj)null);

            assertTrue(MatrixFeatures_DDRM.isEquals(expected,found, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void divide() {
        double denominator = 2.1;

        for( int length : new int[]{0,2,6,15,30} ) {
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,5,length,rand);
            DMatrixRMaj  Ad = ConvertDMatrixSparse.convert(A,(DMatrixRMaj)null);

            DMatrixSparseCSC B = new DMatrixSparseCSC(A.numRows,A.numCols,0);
            DMatrixRMaj expected = new DMatrixRMaj(A.numRows,A.numCols);

            CommonOps_DSCC.divide(A,denominator, B);
            CommonOps_DDRM.divide(Ad,denominator, expected);

            assertTrue(CommonOps_DSCC.checkStructure(B));
            DMatrixRMaj found = ConvertDMatrixSparse.convert(B,(DMatrixRMaj)null);

            assertTrue(MatrixFeatures_DDRM.isEquals(expected,found, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void elementMinAbs() {
        for( int length : new int[]{0,2,6,15,30} ) {
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,5,length,rand);
            DMatrixRMaj Ad = ConvertDMatrixSparse.convert(A,(DMatrixRMaj)null);

            double found = CommonOps_DSCC.elementMinAbs(A);
            double expected = CommonOps_DDRM.elementMinAbs(Ad);

            assertEquals(expected,found, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementMaxAbs() {
        for( int length : new int[]{0,2,6,15,30} ) {
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,5,length,rand);

            double found = CommonOps_DSCC.elementMaxAbs(A);
            double expected = CommonOps_DDRM.elementMaxAbs(ConvertDMatrixSparse.convert(A,(DMatrixRMaj)null));

            assertEquals(expected,found, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementMin() {
        for( int length : new int[]{0,2,6,15,30} ) {
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,5,length,1,3,rand);

            double found = CommonOps_DSCC.elementMin(A);
            double expected = CommonOps_DDRM.elementMin(ConvertDMatrixSparse.convert(A,(DMatrixRMaj)null));

            assertEquals(expected,found, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementMax() {
        for( int length : new int[]{0,2,6,15,30} ) {
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,5,length,-2,-1,rand);

            double found = CommonOps_DSCC.elementMax(A);
            double expected = CommonOps_DDRM.elementMax(ConvertDMatrixSparse.convert(A,(DMatrixRMaj)null));

            assertEquals(expected,found, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void diag() {
        double d[] = new double[]{1.2,2.2,3.3};

        DMatrixSparseCSC A = CommonOps_DSCC.diag(d);

        assertTrue(CommonOps_DSCC.checkStructure(A));
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

    @Test
    public void permutationMatrix() {
        int p[] = new int[]{2,0,1};

        DMatrixSparseCSC P = new DMatrixSparseCSC(3,3,3);
        CommonOps_DSCC.permutationMatrix(p,P);

        DMatrixRMaj found = ConvertDMatrixSparse.convert(P,(DMatrixRMaj)null);
        DMatrixRMaj expected = UtilEjml.parse_DDRM(
                "0 0 1 " +
                   "1 0 0 " +
                   "0 1 0 ", 3);

        assertTrue(CommonOps_DSCC.checkStructure(P));
        assertTrue(MatrixFeatures_DDRM.isEquals(expected,found));
    }

    @Test
    public void permutationVector() {
        DMatrixSparseCSC P = UtilEjml.parse_DSCC(
                     "0 0 1 " +
                        "1 0 0 " +
                        "0 1 0 ", 3);

        int found[] = new int[3];
        CommonOps_DSCC.permutationVector(P,found);

        int expected[] = new int[]{2,0,1};
        for (int i = 0; i < 3; i++) {
            assertEquals(expected[i],expected[i]);
        }
    }

    @Test
    public void permutationInverse() {
        int p[] = new int[]{2,0,1,3};
        int found[] = new int[4];

        CommonOps_DSCC.permutationInverse(p,found,p.length);

        DMatrixSparseCSC A = CommonOps_DSCC.permutationMatrix(p,null);
        DMatrixSparseCSC B = CommonOps_DSCC.permutationMatrix(found,null);

        assertTrue(MatrixFeatures_DSCC.isTranspose(A,B, UtilEjml.TEST_F64));
    }

    @Test
    public void permuteRowInv() {
        int permRow[] = new int[]{2,0,3,1};
        int permRowInv[] = new int[4];
        CommonOps_DSCC.permutationInverse(permRow,permRowInv,permRow.length);

        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(4,4,12,-1,1,rand);
        DMatrixSparseCSC B = new DMatrixSparseCSC(4,4,1);

        CommonOps_DSCC.permuteRowInv(permRowInv, A, B);
        assertFalse(B.indicesSorted);
        assertTrue(CommonOps_DSCC.checkStructure(B));

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                assertEquals(A.get(permRow[row],col), B.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void permute_matrix() {
        int permRow[] = new int[]{2,0,3,1};
        int permCol[] = new int[]{1,2,0,3};

        int permRowInv[] = new int[4];
        CommonOps_DSCC.permutationInverse(permRow,permRowInv,permRow.length);

        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(4,4,12,-1,1,rand);
        DMatrixSparseCSC B = new DMatrixSparseCSC(4,4,1);

        CommonOps_DSCC.permute(permRowInv, A, permCol, B);
        assertFalse(B.indicesSorted);
        assertTrue(CommonOps_DSCC.checkStructure(B));

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                assertEquals(A.get(permRow[row],permCol[col]), B.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void permute_vector() {
        int perm[] = new int[]{2,0,3,1};
        double x[] = new double[]{2,3,4,5};
        double b[] = new double[4];

        CommonOps_DSCC.permute(perm,x,b,4);

        for (int i = 0; i < 4; i++) {
            assertEquals(x[perm[i]], b[i], UtilEjml.TEST_F64);
        }
    }


    @Test
    public void permuteInv_vector() {
        int perm[] = new int[]{2,0,3,1};
        double x[] = new double[]{2,3,4,5};
        double b[] = new double[4];

        CommonOps_DSCC.permuteInv(perm,x,b,4);

        for (int i = 0; i < 4; i++) {
            assertEquals(x[i],b[perm[i]],UtilEjml.TEST_F64);
        }
    }

    @Test
    public void permuteSymmetric() {
        int perm[] = new int[]{4,0,3,1,2};

        DMatrixSparseCSC A = RandomMatrices_DSCC.symmetric(5,7,-1,1,rand);

        int permInv[] = new int[perm.length];
        CommonOps_DSCC.permutationInverse(perm,permInv,perm.length);

        DMatrixSparseCSC B = new DMatrixSparseCSC(5,5,0);

        CommonOps_DSCC.permuteSymmetric(A, permInv, B, null);
        assertFalse(B.indicesSorted);
        assertTrue(CommonOps_DSCC.checkStructure(B));

        for (int row = 0; row < 5; row++) {
            for (int col = row; col < 5; col++) {
                assertEquals(A.get(perm[row],perm[col]), B.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void concatColumns() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(10,5,15,rand);
        DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(3,5,8,rand);

        DMatrixSparseCSC C = CommonOps_DSCC.concatColumns(A,B,null);
        assertTrue(CommonOps_DSCC.checkStructure(C));

        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                assertEquals(C.get(row,col),A.get(row,col), UtilEjml.TEST_F64);
            }
        }
        for (int row = 0; row < B.numRows; row++) {
            for (int col = 0; col < B.numCols; col++) {
                assertEquals(C.get(row+A.numRows,col),B.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void concatRows() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(5,10,15,rand);
        DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(5,3,8,rand);

        DMatrixSparseCSC C = CommonOps_DSCC.concatRows(A,B,null);
        assertTrue(CommonOps_DSCC.checkStructure(C));

        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                assertEquals(C.get(row,col),A.get(row,col), UtilEjml.TEST_F64);
            }
        }
        for (int row = 0; row < B.numRows; row++) {
            for (int col = 0; col < B.numCols; col++) {
                assertEquals(C.get(row,col+A.numCols),B.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }
}