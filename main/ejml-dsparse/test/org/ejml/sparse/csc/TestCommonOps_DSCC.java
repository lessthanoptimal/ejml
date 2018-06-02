/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.ops.ConvertDMatrixStruct;
import org.ejml.sparse.csc.mult.ImplSparseSparseMult_DSCC;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestCommonOps_DSCC {

    private Random rand = new Random(234);

    @Test
    public void isRowOrderValid() {
        DMatrixSparseTriplet orig = new DMatrixSparseTriplet(3,5,6);

        orig.addItem(0,0, 5);
        orig.addItem(1,0, 6);
        orig.addItem(2,0, 7);

        orig.addItem(1,2, 5);

        DMatrixSparseCSC a = ConvertDMatrixStruct.convert(orig,(DMatrixSparseCSC)null);

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
        DMatrixRMaj denseA = ConvertDMatrixStruct.convert(A,(DMatrixRMaj)null);
        DMatrixRMaj denseB = ConvertDMatrixStruct.convert(B,(DMatrixRMaj)null);
        DMatrixRMaj expected = new DMatrixRMaj(A.numRows,B.numCols);

        DMatrixSparseCSC A_t = CommonOps_DSCC.transpose(A,null,null);
        DMatrixSparseCSC B_t = CommonOps_DSCC.transpose(B,null,null);

        DMatrixRMaj denseA_t = CommonOps_DDRM.transpose(denseA,null);
        DMatrixRMaj denseB_t = CommonOps_DDRM.transpose(denseB,null);

        for( int i = 0; i < 2; i++ ) {
            boolean transA = i==1;
            for (int j = 0; j < 2; j++) {
                boolean transB = j==1;

                try {
                    if( transA ) {
                        if( transB ) {
                            continue;
                        } else {
                            CommonOps_DSCC.multTransA(A_t,B,C,null,null);
                            CommonOps_DDRM.multTransA(denseA_t,denseB,expected);
                        }
                    } else if( transB ) {
                        CommonOps_DSCC.multTransB(A,B_t,C,null,null);
                        CommonOps_DDRM.multTransB(denseA,denseB_t,expected);
                    } else {
                        CommonOps_DSCC.mult(A,B,C,null,null);
                        CommonOps_DDRM.mult(denseA,denseB,expected);
                    }
                    assertTrue(CommonOps_DSCC.checkStructure(C));

                    if( exception )
                        fail("exception expected");

                    DMatrixRMaj found = ConvertDMatrixStruct.convert(C,(DMatrixRMaj)null);
                    assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found, UtilEjml.TEST_F64));
                } catch( RuntimeException ignore){
                    if( !exception )
                        fail("no exception expected");
                }
            }
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

    private void check_s_d_mult(DMatrixSparseCSC A , DMatrixRMaj B, DMatrixRMaj C, boolean exception ) {
        DMatrixRMaj denseA = ConvertDMatrixStruct.convert(A,(DMatrixRMaj)null);
        DMatrixRMaj expected = new DMatrixRMaj(A.numRows,B.numCols);

        DMatrixSparseCSC A_t = CommonOps_DSCC.transpose(A,null,null);
        DMatrixRMaj B_t = CommonOps_DDRM.transpose(B,null);
        DMatrixRMaj denseA_t = CommonOps_DDRM.transpose(denseA,null);

        for( int i = 0; i < 2; i++ ) {
            boolean transA = i == 1;
            for (int j = 0; j < 2; j++) {
                boolean transB = j == 1;
                try {
                    if( transA ) {
                        if( transB ) {
                            CommonOps_DSCC.multTransAB(A_t,B_t,C);
                            CommonOps_DDRM.multTransAB(denseA_t,B_t,expected);
                        } else {
                            CommonOps_DSCC.multTransA(A_t,B,C,null);
                            CommonOps_DDRM.multTransA(denseA_t,B,expected);
                        }
                    } else if( transB ) {
                        CommonOps_DSCC.multTransB(A,B_t,C);
                        CommonOps_DDRM.multTransB(denseA,B_t,expected);
                    } else {
                        CommonOps_DSCC.mult(A,B,C);
                        CommonOps_DDRM.mult(denseA,B,expected);
                    }

                    if (exception)
                        fail("exception expected");

                    assertTrue(MatrixFeatures_DDRM.isIdentical(expected, C, UtilEjml.TEST_F64));

                } catch (RuntimeException ignore) {
                    if (!exception)
                        fail("no exception expected");
                }
            }
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
            DMatrixRMaj denseA = ConvertDMatrixStruct.convert(A,(DMatrixRMaj)null);
            DMatrixRMaj denseB = ConvertDMatrixStruct.convert(B,(DMatrixRMaj)null);
            DMatrixRMaj expected = new DMatrixRMaj(A.numRows,B.numCols);

            CommonOps_DDRM.add(alpha,denseA,beta,denseB,expected);

            DMatrixRMaj found = ConvertDMatrixStruct.convert(C,(DMatrixRMaj)null);
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

    private void identity_r_c( DMatrixSparseCSC A) {
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
            DMatrixRMaj  Ad = ConvertDMatrixStruct.convert(A,(DMatrixRMaj)null);

            DMatrixSparseCSC B = new DMatrixSparseCSC(A.numRows,A.numCols,0);
            DMatrixRMaj expected = new DMatrixRMaj(A.numRows,A.numCols);

            CommonOps_DSCC.scale(scale, A, B);
            CommonOps_DDRM.scale(scale,Ad,expected);

            assertTrue(CommonOps_DSCC.checkStructure(B));
            DMatrixRMaj found = ConvertDMatrixStruct.convert(B,(DMatrixRMaj)null);

            assertTrue(MatrixFeatures_DDRM.isEquals(expected,found, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void scale_sameInstance() {

        double scale = 2.1;

        for( int length : new int[]{0,2,6,15,30} ) {
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,5,length,rand);
            DMatrixSparseCSC B = new DMatrixSparseCSC(A.numRows,A.numCols,0);

            CommonOps_DSCC.scale(scale, A, B);
            CommonOps_DSCC.scale(scale, A, A);
            assertTrue(CommonOps_DSCC.checkStructure(A));

            EjmlUnitTests.assertEquals(A,B, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void divide() {
        double denominator = 2.1;

        for( int length : new int[]{0,2,6,15,30} ) {
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,5,length,rand);
            DMatrixRMaj  Ad = ConvertDMatrixStruct.convert(A,(DMatrixRMaj)null);

            DMatrixSparseCSC B = new DMatrixSparseCSC(A.numRows,A.numCols,0);
            DMatrixRMaj expected = new DMatrixRMaj(A.numRows,A.numCols);

            CommonOps_DSCC.divide(A,denominator, B);
            CommonOps_DDRM.divide(Ad,denominator, expected);

            assertTrue(CommonOps_DSCC.checkStructure(B));
            DMatrixRMaj found = ConvertDMatrixStruct.convert(B,(DMatrixRMaj)null);

            assertTrue(MatrixFeatures_DDRM.isEquals(expected,found, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void elementMinAbs() {
        for( int length : new int[]{0,2,6,15,30} ) {
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,5,length,rand);
            DMatrixRMaj Ad = ConvertDMatrixStruct.convert(A,(DMatrixRMaj)null);

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
            double expected = CommonOps_DDRM.elementMaxAbs(ConvertDMatrixStruct.convert(A,(DMatrixRMaj)null));

            assertEquals(expected,found, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementMin() {
        for( int length : new int[]{0,2,6,15,30} ) {
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,5,length,1,3,rand);

            double found = CommonOps_DSCC.elementMin(A);
            double expected = CommonOps_DDRM.elementMin(ConvertDMatrixStruct.convert(A,(DMatrixRMaj)null));

            assertEquals(expected,found, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementMax() {
        for( int length : new int[]{0,2,6,15,30} ) {
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,5,length,-2,-1,rand);

            double found = CommonOps_DSCC.elementMax(A);
            double expected = CommonOps_DDRM.elementMax(ConvertDMatrixStruct.convert(A,(DMatrixRMaj)null));

            assertEquals(expected,found, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementSum() {
        for (int trial = 0; trial < 50; trial++) {
            int rows = rand.nextInt(8)+1;
            int cols = rand.nextInt(8)+1;

            int nz = RandomMatrices_DSCC.nonzero(rows,cols,0.05,0.8,rand);
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(rows,cols,nz,rand);

            double expected = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    expected += A.get(i,j);
                }
            }

            double found = CommonOps_DSCC.elementSum(A);

            assertEquals(expected,found, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementMult() {
        for (int trial = 0; trial < 50; trial++) {
//            System.out.println("------------------- Trial "+trial);
            int rows = rand.nextInt(8)+1;
            int cols = rand.nextInt(8)+1;

            int nz_a = RandomMatrices_DSCC.nonzero(rows,cols,0.05,0.8,rand);
            int nz_b = RandomMatrices_DSCC.nonzero(rows,cols,0.05,0.8,rand);

            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(rows,cols,nz_a,rand);
            DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(rows,cols,nz_b,rand);
            DMatrixSparseCSC C = RandomMatrices_DSCC.rectangle(rows,cols,rows*cols/5,rand);

            CommonOps_DSCC.elementMult(A,B,C,null,null);
            assertTrue(CommonOps_DSCC.checkStructure(C));

            int nz_c = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    double expected = A.get(i,j)*B.get(i,j);
                    assertEquals(expected,C.get(i,j), UtilEjml.TEST_F64);
                    if( expected != 0 )
                        nz_c++;
                }
            }

            assertEquals(nz_c,C.nz_length);
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
    public void extractDiag_S() {
        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(3, 4, 5, 0, 1, rand);

        for (int i = 0; i < 3; i++) {
            a.set(i, i, i + 1);
        }

        DMatrixSparseCSC v = new DMatrixSparseCSC(3, 1, 3);
        CommonOps_DSCC.extractDiag(a, v);
        assertTrue(CommonOps_DSCC.checkStructure(v));

        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, v.get(i, 0), 1e-8);
        }

        // Row and column vectors have separate code. Test row vector now
        v = new DMatrixSparseCSC(1, 3, 3);
        CommonOps_DSCC.extractDiag(a, v);
        assertTrue(CommonOps_DSCC.checkStructure(v));

        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, v.get(0, i), 1e-8);
        }
    }

    @Test
    public void extractDiag_D() {
        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(3, 4, 5, 0, 1, rand);

        for (int i = 0; i < 3; i++) {
            a.set(i, i, i + 1);
        }

        DMatrixRMaj v = new DMatrixRMaj(3, 1);
        CommonOps_DSCC.extractDiag(a, v);

        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, v.get(i, 0), 1e-8);
        }

        // Row and column vectors have seperate code. Test row vector now
        v = new DMatrixRMaj(1, 3);
        CommonOps_DSCC.extractDiag(a, v);

        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, v.get(0, i), 1e-8);
        }
    }

    @Test
    public void permutationMatrix() {
        int p[] = new int[]{2,0,1};

        DMatrixSparseCSC P = new DMatrixSparseCSC(3,3,3);
        CommonOps_DSCC.permutationMatrix(p, false, 3,P);

        DMatrixRMaj found = ConvertDMatrixStruct.convert(P,(DMatrixRMaj)null);
        DMatrixRMaj expected = UtilEjml.parse_DDRM(
                "0 0 1 " +
                   "1 0 0 " +
                   "0 1 0 ", 3);

        assertTrue(CommonOps_DSCC.checkStructure(P));
        assertTrue(MatrixFeatures_DDRM.isEquals(expected,found));

        CommonOps_DSCC.permutationMatrix(p, true, 3,P);

        found = ConvertDMatrixStruct.convert(P,(DMatrixRMaj)null);
        expected = UtilEjml.parse_DDRM(
                      "0 1 0 " +
                        "0 0 1 " +
                        "1 0 0 ", 3);

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

        DMatrixSparseCSC A = CommonOps_DSCC.permutationMatrix(p, false, p.length,null);
        DMatrixSparseCSC B = CommonOps_DSCC.permutationMatrix(found, false, p.length,null);

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
    public void concatRows() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(10,5,15,rand);
        DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(3,5,8,rand);

        DMatrixSparseCSC C = CommonOps_DSCC.concatRows(A,B,null);
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
    public void concatColumns() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(5,10,15,rand);
        DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(5,3,8,rand);

        DMatrixSparseCSC C = CommonOps_DSCC.concatColumns(A,B,null);
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

    @Test
    public void extractColumn() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(5,10,15,rand);

        DMatrixSparseCSC B = CommonOps_DSCC.extractColumn(A,2,null);

        for (int row = 0; row < A.numRows; row++) {
            assertEquals( A.get(row,2), B.get(row,0), UtilEjml.TEST_F64);
        }
    }

    @Test
    public void extractRows() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(5,10,15,rand);

        DMatrixSparseCSC B = CommonOps_DSCC.extractRows(A,2,4,null);

        assertEquals(2,B.numRows);
        assertEquals(10,B.numCols);

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < B.numRows; col++) {
                assertEquals( A.get(row+2,col), B.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void extract() {
        for (int trial = 0; trial < 20; trial++) {
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(5,6, 15, rand);

            DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(2,3, 4, rand);

            CommonOps_DSCC.extract(A, 1, 3, 2, 5, B, 0, 0);
            assertTrue(CommonOps_DSCC.checkStructure(B));

            for( int i = 1; i < 3; i++ ) {
                for( int j = 2; j < 5; j++ ) {
                    if( A.isAssigned(i,j)) {
                        assertEquals(A.get(i, j), B.get(i - 1, j - 2), UtilEjml.TEST_F64);
                    } else {
                        assertEquals(0, B.get(i - 1, j - 2), UtilEjml.TEST_F64);
                    }
                }
            }
        }
    }

    @Test
    public void zero_range() {
        for (int trial = 0; trial < 20; trial++) {
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(5, 6, 15, rand);
            DMatrixSparseCSC A_orig = A.copy();

            CommonOps_DSCC.zero(A,1,3,2,5);
            assertTrue(CommonOps_DSCC.checkStructure(A));

            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 6; j++) {
                    if( i >= 1 && i < 3 && j >= 2 && j < 5 ) {
                        assertFalse(A.isAssigned(i,j));
                    } else {
                        assertEquals(A.get(i, j), A_orig.get(i, j), UtilEjml.TEST_F64);
                    }
                }
            }
        }
    }

    /**
     * Just does a comparison to the impl version
     */
    @Test
    public void dotInnerColumns() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(8,4,20,rand);
        DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(8,6,30,rand);

        double found = CommonOps_DSCC.dotInnerColumns(A,1,B,3,null,null);
        double expected = ImplSparseSparseMult_DSCC.dotInnerColumns(A,1,B,3,null,null);

        assertEquals(expected,found,UtilEjml.TEST_F64);
    }

    @Test
    public void solve() {
        solve(5,5);
        solve(10,5);
    }
    private void solve( int m , int n ) {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(m,n,m*n/2,rand);
        RandomMatrices_DSCC.ensureNotSingular(A,rand);
        DMatrixRMaj X = RandomMatrices_DDRM.rectangle(n,5,rand);
        DMatrixRMaj expected = RandomMatrices_DDRM.rectangle(n,5,rand);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(m,5,rand);

        CommonOps_DSCC.mult(A,expected,B);
        assertTrue(CommonOps_DSCC.solve(A,B,X));

        EjmlUnitTests.assertEquals(expected, X, UtilEjml.TEST_F64);
    }

    @Test
    public void invert() {
        for( int m : new int[]{1,2,5,15,40}) {
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(m, m, m * m / 4 + 1, rand);
            RandomMatrices_DSCC.ensureNotSingular(A, rand);

            DMatrixRMaj Ad = new DMatrixRMaj(m, m);
            ConvertDMatrixStruct.convert(A, Ad);

            DMatrixRMaj Ainv = new DMatrixRMaj(m, m);

            assertTrue(CommonOps_DSCC.invert(A, Ainv));


            DMatrixRMaj found = new DMatrixRMaj(m, m);
            CommonOps_DDRM.mult(Ad, Ainv, found);
            assertTrue(MatrixFeatures_DDRM.isIdentity(found, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void det() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC("7 2 3 0 2 0 6 3 9",3);

        assertEquals(90, CommonOps_DSCC.det(A), UtilEjml.TEST_F64);
    }

    /**
     * A more rigorous test is done in the Impl class
     */
    @Test
    public void removeZeros_two() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(4,5,6,-1,1,rand);
        DMatrixSparseCSC B = new DMatrixSparseCSC(1,1,1);

        CommonOps_DSCC.removeZeros(A,B,0.01);

        assertEquals(A.numRows,B.numRows);
        assertEquals(A.numCols,B.numCols);
        for (int j = 0; j < A.numRows; j++) {
            for (int k = 0; k < A.numRows; k++) {
                double val = B.get(j,k);
                assertTrue("val = "+val,Math.abs(val) > 0.01 || val == 0);
            }
        }
    }

    /**
     * A more rigorous test is done in the Impl class
     */
    @Test
    public void removeZeros_one() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(4,5,6,-1,1,rand);

        CommonOps_DSCC.removeZeros(A,0.01);

        for (int j = 0; j < A.numRows; j++) {
            for (int k = 0; k < A.numRows; k++) {
                double val = A.get(j,k);
                assertTrue("val = "+val,Math.abs(val) > 0.01 || val == 0);
            }
        }
    }

    @Test
    public void changeSign() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(4,5,6,-1,1,rand);
        DMatrixSparseCSC B = A.copy();

        CommonOps_DSCC.changeSign(A,A);

        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
                assertEquals(-B.get(i,i),A.get(i,i), UtilEjml.TEST_F64);
            }
        }

        A = B.copy();
        DMatrixSparseCSC C = RandomMatrices_DSCC.rectangle(4,5,6,-1,1,rand);

        CommonOps_DSCC.changeSign(A,C);

        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
                assertEquals(B.get(i,i),A.get(i,i), UtilEjml.TEST_F64);
                assertEquals(-B.get(i,i),C.get(i,i), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void trace() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(4,5,14,-1,1,rand);

        double expected = 0;
        for (int i = 0; i < 4; i++) {
            expected += A.get(i,i);
        }

        double found = CommonOps_DSCC.trace(A);
        assertEquals(expected,found,UtilEjml.TEST_F64);
    }
}