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

package org.ejml.sparse.csc;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.data.FMatrixSparseTriplet;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.ops.ConvertFMatrixStruct;
import org.ejml.sparse.csc.mult.CheckMatrixMultShape_FSCC;
import org.ejml.sparse.csc.mult.ImplSparseSparseMult_FSCC;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestCommonOps_FSCC {

    private final Random rand = new Random(234);

    @Test
    void checkShape() {
        CheckMatrixMultShape_FSCC checkShape = new CheckMatrixMultShape_FSCC(CommonOps_FSCC.class);
        checkShape.checkAll();
    }

    @Test
    public void checkIndicesSorted() {
        FMatrixSparseTriplet orig = new FMatrixSparseTriplet(3,5,6);

        orig.addItem(0,0, 5);
        orig.addItem(1,0, 6);
        orig.addItem(2,0, 7);

        orig.addItem(1,2, 5);

        FMatrixSparseCSC a = ConvertFMatrixStruct.convert(orig,(FMatrixSparseCSC)null);

        // test positive case first
        assertTrue(CommonOps_FSCC.checkIndicesSorted(a));

        // test negative case second
        a.nz_rows[1] = 3;
        assertFalse(CommonOps_FSCC.checkIndicesSorted(a));
    }

    @Test
    public void checkDuplicates() {
        FMatrixSparseCSC orig = new FMatrixSparseCSC(3,2,6);
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

        assertFalse(CommonOps_FSCC.checkDuplicateElements(orig));
        orig.nz_rows[1] = 2;
        assertTrue(CommonOps_FSCC.checkDuplicateElements(orig));
    }

    @Test
    public void transpose_shapes() {
        CommonOps_FSCC.transpose(
                RandomMatrices_FSCC.rectangle(5,5,5,rand),
                RandomMatrices_FSCC.rectangle(5,5,7,rand),null);
        CommonOps_FSCC.transpose(
                RandomMatrices_FSCC.rectangle(4,5,5,rand),
                RandomMatrices_FSCC.rectangle(5,4,7,rand),null);

        {
            FMatrixSparseCSC b = RandomMatrices_FSCC.rectangle(6,4,7,rand);
            CommonOps_FSCC.transpose(RandomMatrices_FSCC.rectangle(4,5,5,rand),b,null);
            assertEquals(5,b.numRows);
        }

        {
            FMatrixSparseCSC b = RandomMatrices_FSCC.rectangle(5,5,7,rand);
            CommonOps_FSCC.transpose(RandomMatrices_FSCC.rectangle(4,5,5,rand),b,null);
            assertEquals(4,b.numCols);
        }

    }

    @Test
    public void mult_s_s_shapes() {
        // multiple trials to test more sparse structures
        for (int trial = 0; trial < 50; trial++) {
            check_s_s_mult(
                    RandomMatrices_FSCC.rectangle(5, 6, 5, rand),
                    RandomMatrices_FSCC.rectangle(6, 4, 7, rand),
                    RandomMatrices_FSCC.rectangle(5, 4, 7, rand), false);

            check_s_s_mult(
                    RandomMatrices_FSCC.rectangle(5, 7, 5, rand),
                    RandomMatrices_FSCC.rectangle(6, 4, 7, rand),
                    RandomMatrices_FSCC.rectangle(5, 5, 7, rand), true);
            check_s_s_mult(
                    RandomMatrices_FSCC.rectangle(5, 6, 5, rand),
                    RandomMatrices_FSCC.rectangle(6, 4, 7, rand),
                    RandomMatrices_FSCC.rectangle(5, 5, 7, rand), false);
            check_s_s_mult(
                    RandomMatrices_FSCC.rectangle(5, 6, 5, rand),
                    RandomMatrices_FSCC.rectangle(6, 4, 7, rand),
                    RandomMatrices_FSCC.rectangle(6, 4, 7, rand), false);
            check_s_s_mult(
                    RandomMatrices_FSCC.rectangle(5, 6, 5, rand),
                    RandomMatrices_FSCC.rectangle(6, 4, 7, rand),
                    RandomMatrices_FSCC.rectangle(6, 4, 7, rand), false);
        }
    }

    private void check_s_s_mult(FMatrixSparseCSC A , FMatrixSparseCSC B, FMatrixSparseCSC C, boolean exception ) {
        FMatrixRMaj denseA = ConvertFMatrixStruct.convert(A,(FMatrixRMaj)null);
        FMatrixRMaj denseB = ConvertFMatrixStruct.convert(B,(FMatrixRMaj)null);
        FMatrixRMaj expected = new FMatrixRMaj(A.numRows,B.numCols);

        FMatrixSparseCSC A_t = CommonOps_FSCC.transpose(A,null,null);
        FMatrixSparseCSC B_t = CommonOps_FSCC.transpose(B,null,null);

        FMatrixRMaj denseA_t = CommonOps_FDRM.transpose(denseA,null);
        FMatrixRMaj denseB_t = CommonOps_FDRM.transpose(denseB,null);

        for( int i = 0; i < 2; i++ ) {
            boolean transA = i==1;
            for (int j = 0; j < 2; j++) {
                boolean transB = j==1;

                try {
                    if( transA ) {
                        if( transB ) {
                            continue;
                        } else {
                            CommonOps_FSCC.multTransA(A_t,B,C,null,null);
                            CommonOps_FDRM.multTransA(denseA_t,denseB,expected);
                        }
                    } else if( transB ) {
                        CommonOps_FSCC.multTransB(A,B_t,C,null,null);
                        CommonOps_FDRM.multTransB(denseA,denseB_t,expected);
                    } else {
                        CommonOps_FSCC.mult(A,B,C,null,null);
                        CommonOps_FDRM.mult(denseA,denseB,expected);
                    }
                    assertTrue(CommonOps_FSCC.checkStructure(C));

                    if( exception )
                        fail("exception expected");

                    FMatrixRMaj found = ConvertFMatrixStruct.convert(C,(FMatrixRMaj)null);
                    assertTrue(MatrixFeatures_FDRM.isIdentical(expected,found, UtilEjml.TEST_F32));
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
                RandomMatrices_FSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_FDRM.rectangle(6, 4, rand),
                RandomMatrices_FDRM.rectangle(5, 4, rand), false);

        check_s_d_mult(
                RandomMatrices_FSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_FDRM.rectangle(7, 4, rand),
                RandomMatrices_FDRM.rectangle(5, 4, rand), true);

        // Matrix C is resized
        check_s_d_mult(
                RandomMatrices_FSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_FDRM.rectangle(6, 4, rand),
                RandomMatrices_FDRM.rectangle(5, 5, rand), false);
        check_s_d_mult(
                RandomMatrices_FSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_FDRM.rectangle(6, 4, rand),
                RandomMatrices_FDRM.rectangle(6, 4, rand), false);
        check_s_d_mult(
                RandomMatrices_FSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_FDRM.rectangle(6, 4, rand),
                RandomMatrices_FDRM.rectangle(6, 4, rand), false);
    }

    private void check_s_d_mult(FMatrixSparseCSC A , FMatrixRMaj B, FMatrixRMaj C, boolean exception ) {
        FMatrixRMaj denseA = ConvertFMatrixStruct.convert(A,(FMatrixRMaj)null);
        FMatrixRMaj expected = C.copy();

        FMatrixSparseCSC A_t = CommonOps_FSCC.transpose(A,null,null);
        FMatrixRMaj B_t = CommonOps_FDRM.transpose(B,null);
        FMatrixRMaj denseA_t = CommonOps_FDRM.transpose(denseA,null);

        for( int i = 0; i < 2; i++ ) {
            boolean transA = i == 1;
            for (int j = 0; j < 2; j++) {
                boolean transB = j == 1;
                for( int k = 0; k < 2; k++ ) {
                    boolean add = k == 1;
                    try {
                        if( add ) {
                            if (transA) {
                                if (transB) {
                                    CommonOps_FSCC.multAddTransAB(A_t, B_t, C);
                                    CommonOps_FDRM.multAddTransAB(denseA_t, B_t, expected);
                                } else {
                                    CommonOps_FSCC.multAddTransA(A_t, B, C);
                                    CommonOps_FDRM.multAddTransA(denseA_t, B, expected);
                                }
                            } else if (transB) {
                                CommonOps_FSCC.multAddTransB(A, B_t, C);
                                CommonOps_FDRM.multAddTransB(denseA, B_t, expected);
                            } else {
                                CommonOps_FSCC.multAdd(A, B, C);
                                CommonOps_FDRM.multAdd(denseA, B, expected);
                            }
                        } else {
                            if (transA) {
                                if (transB) {
                                    CommonOps_FSCC.multTransAB(A_t, B_t, C);
                                    CommonOps_FDRM.multTransAB(denseA_t, B_t, expected);
                                } else {
                                    CommonOps_FSCC.multTransA(A_t, B, C);
                                    CommonOps_FDRM.multTransA(denseA_t, B, expected);
                                }
                            } else if (transB) {
                                CommonOps_FSCC.multTransB(A, B_t, C);
                                CommonOps_FDRM.multTransB(denseA, B_t, expected);
                            } else {
                                CommonOps_FSCC.mult(A, B, C);
                                CommonOps_FDRM.mult(denseA, B, expected);
                            }
                        }

                        if (exception)
                            fail("exception expected");

                        assertTrue(MatrixFeatures_FDRM.isIdentical(expected, C, UtilEjml.TEST_F32));

                    } catch (RuntimeException ignore) {
                        if (!exception)
                            fail("no exception expected");
                    }
                }
            }
        }
    }

    /**
     * See if it adds correctly when the last column is empty. This was a bug once.
     */
    @Test
    public void add_empty_columns() {
        FMatrixSparseTriplet trip_A = new FMatrixSparseTriplet(5,6,6);
        FMatrixSparseTriplet trip_B = new FMatrixSparseTriplet(5,6,6);
        for (int i = 0; i < 5; i++) {
            trip_A.set(i,i,1.0f);
            trip_B.set(Math.min(4,i+1),i,1.0f);
        }
        FMatrixSparseCSC A = ConvertFMatrixStruct.convert(trip_A,(FMatrixSparseCSC)null);
        FMatrixSparseCSC B = ConvertFMatrixStruct.convert(trip_B,(FMatrixSparseCSC)null);
        FMatrixSparseCSC C = new FMatrixSparseCSC(1,1);
        check_add(A,B,C,false);
    }

    @Test
    public void add_shapes() {
        check_add(
                RandomMatrices_FSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_FSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_FSCC.rectangle(5, 6, 5, rand), false);
        check_add(
                RandomMatrices_FSCC.rectangle(5, 6, 5*6, rand),
                RandomMatrices_FSCC.rectangle(5, 6, 5*6, rand),
                RandomMatrices_FSCC.rectangle(5, 6, 5*6, rand), false);
        check_add(
                RandomMatrices_FSCC.rectangle(5, 6, 0, rand),
                RandomMatrices_FSCC.rectangle(5, 6, 0, rand),
                RandomMatrices_FSCC.rectangle(5, 6, 0, rand), false);
        check_add(
                RandomMatrices_FSCC.rectangle(5, 6, 20, rand),
                RandomMatrices_FSCC.rectangle(5, 6, 16, rand),
                RandomMatrices_FSCC.rectangle(5, 6, 0, rand), false);
        check_add(
                RandomMatrices_FSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_FSCC.rectangle(5, 5, 5, rand),
                RandomMatrices_FSCC.rectangle(5, 6, 5, rand), true);
        check_add(
                RandomMatrices_FSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_FSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_FSCC.rectangle(5, 5, 5, rand), false);
        check_add(
                RandomMatrices_FSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_FSCC.rectangle(4, 6, 5, rand),
                RandomMatrices_FSCC.rectangle(5, 6, 5, rand), true);
        check_add(
                RandomMatrices_FSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_FSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_FSCC.rectangle(4, 6, 5, rand), false);

    }

    private void check_add(FMatrixSparseCSC A , FMatrixSparseCSC B, FMatrixSparseCSC C, boolean exception ) {
        float alpha = 1.5f;
        float beta = -0.6f;
        try {
            CommonOps_FSCC.add(alpha,A,beta,B,C, null, null);
            assertTrue(CommonOps_FSCC.checkStructure(C));

            if( exception )
                fail("exception expected");
            FMatrixRMaj denseA = ConvertFMatrixStruct.convert(A,(FMatrixRMaj)null);
            FMatrixRMaj denseB = ConvertFMatrixStruct.convert(B,(FMatrixRMaj)null);
            FMatrixRMaj expected = new FMatrixRMaj(A.numRows,B.numCols);

            CommonOps_FDRM.add(alpha,denseA,beta,denseB,expected);

            FMatrixRMaj found = ConvertFMatrixStruct.convert(C,(FMatrixRMaj)null);
            assertTrue(MatrixFeatures_FDRM.isIdentical(expected,found, UtilEjml.TEST_F32));

        } catch( RuntimeException ignore){
            if( !exception )
                fail("no exception expected");
        }
    }

    @Test
    public void identity_r_c() {
        identity_r_c(CommonOps_FSCC.identity(10,15));
        identity_r_c(CommonOps_FSCC.identity(15,10));
        identity_r_c(CommonOps_FSCC.identity(10,10));
    }

    private void identity_r_c( FMatrixSparseCSC A) {
        assertTrue(CommonOps_FSCC.checkSortedFlag(A));
        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                if( row == col )
                    assertEquals(1.0f,A.get(row,col), UtilEjml.TEST_F32);
                else
                    assertEquals(0.0f,A.get(row,col), UtilEjml.TEST_F32);
            }
        }
    }


    @Test
    public void scale() {

        float scale = 2.1f;

        for( int length : new int[]{0,2,6,15,30} ) {
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(6,5,length,rand);
            FMatrixRMaj  Ad = ConvertFMatrixStruct.convert(A,(FMatrixRMaj)null);

            FMatrixSparseCSC B = new FMatrixSparseCSC(A.numRows,A.numCols,0);
            FMatrixRMaj expected = new FMatrixRMaj(A.numRows,A.numCols);

            CommonOps_FSCC.scale(scale, A, B);
            CommonOps_FDRM.scale(scale,Ad,expected);

            assertTrue(CommonOps_FSCC.checkStructure(B));
            FMatrixRMaj found = ConvertFMatrixStruct.convert(B,(FMatrixRMaj)null);

            assertTrue(MatrixFeatures_FDRM.isEquals(expected,found, UtilEjml.TEST_F32));
        }
    }

    @Test
    public void scale_sameInstance() {

        float scale = 2.1f;

        for( int length : new int[]{0,2,6,15,30} ) {
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(6,5,length,rand);
            FMatrixSparseCSC B = new FMatrixSparseCSC(A.numRows,A.numCols,0);

            CommonOps_FSCC.scale(scale, A, B);
            CommonOps_FSCC.scale(scale, A, A);
            assertTrue(CommonOps_FSCC.checkStructure(A));

            EjmlUnitTests.assertEquals(A,B, UtilEjml.TEST_F32);
        }
    }

    @Test
    public void divide_matrix_scalar() {
        float denominator = 2.1f;

        for( int length : new int[]{0,2,6,15,30} ) {
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(6,5,length,rand);
            FMatrixRMaj  Ad = ConvertFMatrixStruct.convert(A,(FMatrixRMaj)null);

            FMatrixSparseCSC B = new FMatrixSparseCSC(A.numRows,A.numCols,0);
            FMatrixRMaj expected = new FMatrixRMaj(A.numRows,A.numCols);

            CommonOps_FSCC.divide(A,denominator, B);
            CommonOps_FDRM.divide(Ad,denominator, expected);

            assertTrue(CommonOps_FSCC.checkStructure(B));
            FMatrixRMaj found = ConvertFMatrixStruct.convert(B,(FMatrixRMaj)null);

            assertTrue(MatrixFeatures_FDRM.isEquals(expected,found, UtilEjml.TEST_F32));
        }
    }

    @Test
    public void divide_scalar_matrix() {
        float numerator = 2.1f;

        for( int length : new int[]{0,2,6,15,30} ) {
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(6,5,length,rand);
            FMatrixSparseCSC O = A.copy();

            FMatrixSparseCSC B = new FMatrixSparseCSC(A.numRows,A.numCols,0);

            CommonOps_FSCC.divide(numerator,A, B);
            assertTrue(CommonOps_FSCC.checkStructure(B));
            CommonOps_FSCC.divide(numerator,A, A);
            assertTrue(CommonOps_FSCC.checkStructure(A));

            for (int row = 0; row < A.numRows; row++) {
                for (int col = 0; col < A.numCols; col++) {
                    float v = O.get(row,col);
                    if( v == 0 )
                        continue;

                    assertEquals(numerator/v, A.get(row,col), UtilEjml.TEST_F32);
                    assertEquals(numerator/v, B.get(row,col), UtilEjml.TEST_F32);
                }
            }
        }
    }

    @Test
    public void elementMinAbs() {
        for( int length : new int[]{0,2,6,15,30} ) {
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(6,5,length,rand);
            FMatrixRMaj Ad = ConvertFMatrixStruct.convert(A,(FMatrixRMaj)null);

            float found = CommonOps_FSCC.elementMinAbs(A);
            float expected = CommonOps_FDRM.elementMinAbs(Ad);

            assertEquals(expected,found, UtilEjml.TEST_F32);
        }
    }

    @Test
    public void elementMaxAbs() {
        for( int length : new int[]{0,2,6,15,30} ) {
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(6,5,length,rand);

            float found = CommonOps_FSCC.elementMaxAbs(A);
            float expected = CommonOps_FDRM.elementMaxAbs(ConvertFMatrixStruct.convert(A,(FMatrixRMaj)null));

            assertEquals(expected,found, UtilEjml.TEST_F32);
        }
    }

    @Test
    public void elementMin() {
        for( int length : new int[]{0,2,6,15,30} ) {
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(6,5,length,1,3,rand);

            float found = CommonOps_FSCC.elementMin(A);
            float expected = CommonOps_FDRM.elementMin(ConvertFMatrixStruct.convert(A,(FMatrixRMaj)null));

            assertEquals(expected,found, UtilEjml.TEST_F32);
        }
    }

    @Test
    public void elementMax() {
        for( int length : new int[]{0,2,6,15,30} ) {
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(6,5,length,-2,-1,rand);

            float found = CommonOps_FSCC.elementMax(A);
            float expected = CommonOps_FDRM.elementMax(ConvertFMatrixStruct.convert(A,(FMatrixRMaj)null));

            assertEquals(expected,found, UtilEjml.TEST_F32);
        }
    }

    @Test
    public void elementSum() {
        for (int trial = 0; trial < 50; trial++) {
            int rows = rand.nextInt(8)+1;
            int cols = rand.nextInt(8)+1;

            int nz = RandomMatrices_FSCC.nonzero(rows,cols,0.05f,0.8f,rand);
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(rows,cols,nz,rand);

            float expected = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    expected += A.get(i,j);
                }
            }

            float found = CommonOps_FSCC.elementSum(A);

            assertEquals(expected,found, UtilEjml.TEST_F32);
        }
    }

    @Test
    public void elementMult() {
        for (int trial = 0; trial < 50; trial++) {
//            System.out.println("------------------- Trial "+trial);
            int rows = rand.nextInt(8)+1;
            int cols = rand.nextInt(8)+1;

            int nz_a = RandomMatrices_FSCC.nonzero(rows,cols,0.05f,0.8f,rand);
            int nz_b = RandomMatrices_FSCC.nonzero(rows,cols,0.05f,0.8f,rand);

            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(rows,cols,nz_a,rand);
            FMatrixSparseCSC B = RandomMatrices_FSCC.rectangle(rows,cols,nz_b,rand);
            FMatrixSparseCSC C = RandomMatrices_FSCC.rectangle(rows,cols,rows*cols/5,rand);

            CommonOps_FSCC.elementMult(A,B,C,null,null);
            assertTrue(CommonOps_FSCC.checkStructure(C));

            int nz_c = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    float expected = A.get(i,j)*B.get(i,j);
                    assertEquals(expected,C.get(i,j), UtilEjml.TEST_F32);
                    if( expected != 0 )
                        nz_c++;
                }
            }

            assertEquals(nz_c,C.nz_length);
        }
    }

    @Test
    public void columnMaxAbs() {
        for (int trial = 0; trial < 50; trial++) {
//            System.out.println("------------------- Trial "+trial);
            int rows = rand.nextInt(8)+1;
            int cols = rand.nextInt(8)+1;

            int nz_a = RandomMatrices_FSCC.nonzero(rows,cols,0.05f,0.8f,rand);

            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(rows,cols,nz_a,rand);
            float values[] = new float[A.numCols];

            CommonOps_FSCC.columnMaxAbs(A,values);

            for (int j = 0; j < cols; j++) {
                float expected = 0;
                for (int i = 0; i < rows; i++) {
                    expected = Math.max(Math.abs(A.get(i,j)),expected);
                }
                assertEquals(expected,values[j], UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void multColumns() {
        for (int trial = 0; trial < 50; trial++) {
//            System.out.println("------------------- Trial "+trial);
            int rows = rand.nextInt(8)+1;
            int cols = rand.nextInt(8)+1;

            int nz_a = RandomMatrices_FSCC.nonzero(rows,cols,0.05f,0.8f,rand);
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(rows,cols,nz_a,rand);
            FMatrixSparseCSC found = A.copy();

            float values[] = new float[A.numCols];
            for (int i = 0; i < A.numCols; i++) {
                values[i] = rand.nextFloat()+0.1f;
            }
            CommonOps_FSCC.multColumns(found,values,0);
            assertTrue(CommonOps_FSCC.checkStructure(found));

            for (int j = 0; j < cols; j++) {
                for (int i = 0; i < rows; i++) {
                    assertEquals(A.get(i,j)*values[j], found.get(i,j), UtilEjml.TEST_F32);
                }
            }
        }
    }

    @Test
    public void divideColumns() {
        for (int trial = 0; trial < 50; trial++) {
//            System.out.println("------------------- Trial "+trial);
            int rows = rand.nextInt(8)+1;
            int cols = rand.nextInt(8)+1;

            int nz_a = RandomMatrices_FSCC.nonzero(rows,cols,0.05f,0.8f,rand);
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(rows,cols,nz_a,rand);
            FMatrixSparseCSC found = A.copy();

            float values[] = new float[A.numCols];
            for (int i = 0; i < A.numCols; i++) {
                values[i] = rand.nextFloat()+0.1f;
            }
            CommonOps_FSCC.divideColumns(found,values,0);
            assertTrue(CommonOps_FSCC.checkStructure(found));

            for (int j = 0; j < cols; j++) {
                for (int i = 0; i < rows; i++) {
                    assertEquals(A.get(i,j)/values[j], found.get(i,j), UtilEjml.TEST_F32);
                }
            }
        }
    }

    @Test
    public void multRowsCols() {
        for (int trial = 0; trial < 50; trial++) {
//            System.out.println("------------------- Trial "+trial);
            int rows = rand.nextInt(8)+1;
            int cols = rand.nextInt(8)+1;

            int nz_b = RandomMatrices_FSCC.nonzero(rows,cols,0.05f,0.8f,rand);
            FMatrixSparseCSC B = RandomMatrices_FSCC.rectangle(rows,cols,nz_b,rand);
            FMatrixSparseCSC found = B.copy();

            float diagA[] = new float[B.numRows];
            for (int i = 0; i < B.numRows; i++) {
                diagA[i] = rand.nextFloat()+0.1f;
            }

            float diagC[] = new float[B.numCols];
            for (int i = 0; i < B.numCols; i++) {
                diagC[i] = rand.nextFloat()+0.1f;
            }


            FMatrixSparseCSC A = CommonOps_FSCC.diag(null,diagA,0,B.numRows);
            FMatrixSparseCSC C = CommonOps_FSCC.diag(null,diagC,0,B.numCols);

            FMatrixSparseCSC AB = new FMatrixSparseCSC(1,1);
            CommonOps_FSCC.mult(A,B,AB);
            FMatrixSparseCSC expected = new FMatrixSparseCSC(1,1);
            CommonOps_FSCC.mult(AB,C,expected);

            CommonOps_FSCC.multRowsCols(diagA,0,found,diagC,0);
            assertTrue(CommonOps_FSCC.checkStructure(found));

            expected.sortIndices(null);
            found.sortIndices(null);
            assertTrue( MatrixFeatures_FSCC.isEquals(expected,found, UtilEjml.TEST_F32));
        }
    }

    @Test
    public void divideRowsCols() {
        for (int trial = 0; trial < 50; trial++) {
//            System.out.println("------------------- Trial "+trial);
            int rows = rand.nextInt(8)+1;
            int cols = rand.nextInt(8)+1;

            int nz_b = RandomMatrices_FSCC.nonzero(rows,cols,0.05f,0.8f,rand);
            FMatrixSparseCSC B = RandomMatrices_FSCC.rectangle(rows,cols,nz_b,rand);
            FMatrixSparseCSC found = B.copy();

            float diagA[] = new float[B.numRows];
            for (int i = 0; i < B.numRows; i++) {
                diagA[i] = rand.nextFloat()+0.1f;
            }

            float diagC[] = new float[B.numCols];
            for (int i = 0; i < B.numCols; i++) {
                diagC[i] = rand.nextFloat()+0.1f;
            }


            FMatrixSparseCSC A = CommonOps_FSCC.diag(null,diagA,0,B.numRows);
            FMatrixSparseCSC C = CommonOps_FSCC.diag(null,diagC,0,B.numCols);

            // invert the matrices
            CommonOps_FSCC.divide(1.0f,A,A);
            CommonOps_FSCC.divide(1.0f,C,C);

            FMatrixSparseCSC AB = new FMatrixSparseCSC(1,1);
            CommonOps_FSCC.mult(A,B,AB);
            FMatrixSparseCSC expected = new FMatrixSparseCSC(1,1);
            CommonOps_FSCC.mult(AB,C,expected);

            CommonOps_FSCC.divideRowsCols(diagA,0,found,diagC,0);
            assertTrue(CommonOps_FSCC.checkStructure(found));

            expected.sortIndices(null);
            found.sortIndices(null);
            assertTrue( MatrixFeatures_FSCC.isEquals(expected,found, UtilEjml.TEST_F32));
        }
    }


    @Test
    public void diag() {
        float d[] = new float[]{1.2f,2.2f,3.3f};

        FMatrixSparseCSC A = CommonOps_FSCC.diag(d);

        assertTrue(CommonOps_FSCC.checkStructure(A));
        assertEquals(3,A.numRows);
        assertEquals(3,A.numCols);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if( row != col ) {
                    assertEquals(0,A.get(row,col), UtilEjml.TEST_F32);
                } else {
                    assertEquals(d[row],A.get(row,col), UtilEjml.TEST_F32);
                }
            }
        }
    }

    @Test
    public void extractDiag_S() {
        FMatrixSparseCSC a = RandomMatrices_FSCC.rectangle(3, 4, 5, 0, 1, rand);

        for (int i = 0; i < 3; i++) {
            a.set(i, i, i + 1);
        }

        FMatrixSparseCSC v = new FMatrixSparseCSC(3, 1, 3);
        CommonOps_FSCC.extractDiag(a, v);
        assertTrue(CommonOps_FSCC.checkStructure(v));

        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, v.get(i, 0), 1e-8);
        }

        // Row and column vectors have separate code. Test row vector now
        v = new FMatrixSparseCSC(1, 3, 3);
        CommonOps_FSCC.extractDiag(a, v);
        assertTrue(CommonOps_FSCC.checkStructure(v));

        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, v.get(0, i), 1e-8);
        }
    }

    @Test
    public void extractDiag_D() {
        FMatrixSparseCSC a = RandomMatrices_FSCC.rectangle(3, 4, 5, 0, 1, rand);

        for (int i = 0; i < 3; i++) {
            a.set(i, i, i + 1);
        }

        FMatrixRMaj v = new FMatrixRMaj(3, 1);
        CommonOps_FSCC.extractDiag(a, v);

        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, v.get(i, 0), 1e-8);
        }

        // Row and column vectors have seperate code. Test row vector now
        v = new FMatrixRMaj(1, 3);
        CommonOps_FSCC.extractDiag(a, v);

        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, v.get(0, i), 1e-8);
        }
    }

    @Test
    public void permutationMatrix() {
        int p[] = new int[]{2,0,1};

        FMatrixSparseCSC P = new FMatrixSparseCSC(3,3,3);
        CommonOps_FSCC.permutationMatrix(p, false, 3,P);

        FMatrixRMaj found = ConvertFMatrixStruct.convert(P,(FMatrixRMaj)null);
        FMatrixRMaj expected = UtilEjml.parse_FDRM(
                "0 0 1 " +
                   "1 0 0 " +
                   "0 1 0 ", 3);

        assertTrue(CommonOps_FSCC.checkStructure(P));
        assertTrue(MatrixFeatures_FDRM.isEquals(expected,found));

        CommonOps_FSCC.permutationMatrix(p, true, 3,P);

        found = ConvertFMatrixStruct.convert(P,(FMatrixRMaj)null);
        expected = UtilEjml.parse_FDRM(
                      "0 1 0 " +
                        "0 0 1 " +
                        "1 0 0 ", 3);

        assertTrue(CommonOps_FSCC.checkStructure(P));
        assertTrue(MatrixFeatures_FDRM.isEquals(expected,found));
    }

    @Test
    public void permutationVector() {
        FMatrixSparseCSC P = UtilEjml.parse_FSCC(
                     "0 0 1 " +
                        "1 0 0 " +
                        "0 1 0 ", 3);

        int found[] = new int[3];
        CommonOps_FSCC.permutationVector(P,found);

        int expected[] = new int[]{2,0,1};
        for (int i = 0; i < 3; i++) {
            assertEquals(expected[i],expected[i]);
        }
    }

    @Test
    public void permutationInverse() {
        int p[] = new int[]{2,0,1,3};
        int found[] = new int[4];

        CommonOps_FSCC.permutationInverse(p,found,p.length);

        FMatrixSparseCSC A = CommonOps_FSCC.permutationMatrix(p, false, p.length,null);
        FMatrixSparseCSC B = CommonOps_FSCC.permutationMatrix(found, false, p.length,null);

        assertTrue(MatrixFeatures_FSCC.isTranspose(A,B, UtilEjml.TEST_F32));
    }

    @Test
    public void permuteRowInv() {
        int permRow[] = new int[]{2,0,3,1};
        int permRowInv[] = new int[4];
        CommonOps_FSCC.permutationInverse(permRow,permRowInv,permRow.length);

        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(4,4,12,-1,1,rand);
        FMatrixSparseCSC B = new FMatrixSparseCSC(4,4,1);

        CommonOps_FSCC.permuteRowInv(permRowInv, A, B);
        assertFalse(B.indicesSorted);
        assertTrue(CommonOps_FSCC.checkStructure(B));

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                assertEquals(A.get(permRow[row],col), B.get(row,col), UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void permute_matrix() {
        int permRow[] = new int[]{2,0,3,1};
        int permCol[] = new int[]{1,2,0,3};

        int permRowInv[] = new int[4];
        CommonOps_FSCC.permutationInverse(permRow,permRowInv,permRow.length);

        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(4,4,12,-1,1,rand);
        FMatrixSparseCSC B = new FMatrixSparseCSC(4,4,1);

        CommonOps_FSCC.permute(permRowInv, A, permCol, B);
        assertFalse(B.indicesSorted);
        assertTrue(CommonOps_FSCC.checkStructure(B));

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                assertEquals(A.get(permRow[row],permCol[col]), B.get(row,col), UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void permute_vector() {
        int perm[] = new int[]{2,0,3,1};
        float x[] = new float[]{2,3,4,5};
        float b[] = new float[4];

        CommonOps_FSCC.permute(perm,x,b,4);

        for (int i = 0; i < 4; i++) {
            assertEquals(x[perm[i]], b[i], UtilEjml.TEST_F32);
        }
    }


    @Test
    public void permuteInv_vector() {
        int perm[] = new int[]{2,0,3,1};
        float x[] = new float[]{2,3,4,5};
        float b[] = new float[4];

        CommonOps_FSCC.permuteInv(perm,x,b,4);

        for (int i = 0; i < 4; i++) {
            assertEquals(x[i],b[perm[i]],UtilEjml.TEST_F32);
        }
    }

    @Test
    public void permuteSymmetric() {
        int perm[] = new int[]{4,0,3,1,2};

        FMatrixSparseCSC A = RandomMatrices_FSCC.symmetric(5,7,-1,1,rand);

        int permInv[] = new int[perm.length];
        CommonOps_FSCC.permutationInverse(perm,permInv,perm.length);

        FMatrixSparseCSC B = new FMatrixSparseCSC(5,5,0);

        CommonOps_FSCC.permuteSymmetric(A, permInv, B, null);
        assertFalse(B.indicesSorted);
        assertTrue(CommonOps_FSCC.checkStructure(B));

        for (int row = 0; row < 5; row++) {
            for (int col = row; col < 5; col++) {
                assertEquals(A.get(perm[row],perm[col]), B.get(row,col), UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void concatRows() {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(10,5,15,rand);
        FMatrixSparseCSC B = RandomMatrices_FSCC.rectangle(3,5,8,rand);

        FMatrixSparseCSC C = CommonOps_FSCC.concatRows(A,B,null);
        assertTrue(CommonOps_FSCC.checkStructure(C));

        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                assertEquals(C.get(row,col),A.get(row,col), UtilEjml.TEST_F32);
            }
        }
        for (int row = 0; row < B.numRows; row++) {
            for (int col = 0; col < B.numCols; col++) {
                assertEquals(C.get(row+A.numRows,col),B.get(row,col), UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void concatColumns() {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(5,10,15,rand);
        FMatrixSparseCSC B = RandomMatrices_FSCC.rectangle(5,3,8,rand);

        FMatrixSparseCSC C = CommonOps_FSCC.concatColumns(A,B,null);
        assertTrue(CommonOps_FSCC.checkStructure(C));

        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                assertEquals(C.get(row,col),A.get(row,col), UtilEjml.TEST_F32);
            }
        }
        for (int row = 0; row < B.numRows; row++) {
            for (int col = 0; col < B.numCols; col++) {
                assertEquals(C.get(row,col+A.numCols),B.get(row,col), UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void extractColumn() {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(5,10,15,rand);

        FMatrixSparseCSC B = CommonOps_FSCC.extractColumn(A,2,null);
        assertTrue(CommonOps_FSCC.checkStructure(B));

        for (int row = 0; row < A.numRows; row++) {
            assertEquals( A.get(row,2), B.get(row,0), UtilEjml.TEST_F32);
        }
    }

    @Test
    public void extractRows() {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(5,10,15,rand);

        FMatrixSparseCSC B = CommonOps_FSCC.extractRows(A,2,4,null);

        assertEquals(2,B.numRows);
        assertEquals(10,B.numCols);

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < B.numRows; col++) {
                assertEquals( A.get(row+2,col), B.get(row,col), UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void extract() {
        for (int trial = 0; trial < 20; trial++) {
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(5,6, 15, rand);

            FMatrixSparseCSC B = RandomMatrices_FSCC.rectangle(2,3, 4, rand);

            CommonOps_FSCC.extract(A, 1, 3, 2, 5, B, 0, 0);
            assertTrue(CommonOps_FSCC.checkStructure(B));

            for( int i = 1; i < 3; i++ ) {
                for( int j = 2; j < 5; j++ ) {
                    if( A.isAssigned(i,j)) {
                        assertEquals(A.get(i, j), B.get(i - 1, j - 2), UtilEjml.TEST_F32);
                    } else {
                        assertEquals(0, B.get(i - 1, j - 2), UtilEjml.TEST_F32);
                    }
                }
            }
        }
    }

    @Test
    public void fill() {
        for (int i = 0; i < 10; i++) {
            fill(1,1);
            fill(10,1);
            fill(1,10);
            fill(4,7);
        }
    }

    public void fill( int numRows , int numCols ) {
        int nz_total = numRows*numCols/2;
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(numRows,numCols,nz_total,rand);

        CommonOps_FSCC.fill(A,1.2f);

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                assertEquals(1.2f,A.get(row,col), UtilEjml.TEST_F32 );
            }
        }
    }

    @Test
    public void sumCols() {
        for (int i = 0; i < 10; i++) {
            sumCols(1,1);
            sumCols(10,1);
            sumCols(1,10);
            sumCols(4,7);
        }
    }

    public void sumCols( int numRows , int numCols ) {
        int nz_total = numRows*numCols/2;
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(numRows,numCols,nz_total,rand);

        FMatrixRMaj B = CommonOps_FSCC.sumCols(A,null);
        assertEquals(1,B.numRows);
        assertEquals(numCols,B.numCols);

        for (int col = 0; col < numCols; col++) {
            float sum = 0;
            for (int row = 0; row < numRows; row++) {
                sum += A.get(row,col);
            }
            assertEquals(sum,B.get(0,col), UtilEjml.TEST_F32);
        }

        // see of it properly resets the matrix
        FMatrixRMaj C = B.copy();
        B.numRows=1;B.numCols=2;
        CommonOps_FSCC.sumCols(A,B);
        assertTrue( MatrixFeatures_FDRM.isEquals(C,B));
    }

    @Test
    public void minCols() {
        for (int i = 0; i < 10; i++) {
            minCols(1,1);
            minCols(10,1);
            minCols(1,10);
            minCols(4,7);
        }
    }

    public void minCols( int numRows , int numCols ) {
        int nz_total = numRows*numCols/2;
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(numRows,numCols,nz_total,rand);

        FMatrixRMaj B = CommonOps_FSCC.minCols(A,null);
        assertEquals(1,B.numRows);
        assertEquals(numCols,B.numCols);

        for (int col = 0; col < numCols; col++) {
            float min = Float.MAX_VALUE;
            for (int row = 0; row < numRows; row++) {
                min = Math.min(min,A.get(row,col));
            }
            assertEquals(min,B.get(0,col), UtilEjml.TEST_F32);
        }

        // see of it properly resets the matrix
        FMatrixRMaj C = B.copy();
        B.numRows=1;B.numCols=2;
        CommonOps_FSCC.minCols(A,B);
        assertTrue( MatrixFeatures_FDRM.isEquals(C,B));
    }

    @Test
    public void maxCols() {
        for (int i = 0; i < 10; i++) {
            maxCols(1,1);
            maxCols(10,1);
            maxCols(1,10);
            maxCols(4,7);
        }
    }

    public void maxCols( int numRows , int numCols ) {
        int nz_total = numRows*numCols/2;
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(numRows,numCols,nz_total,rand);

        FMatrixRMaj B = CommonOps_FSCC.maxCols(A,null);
        assertEquals(1,B.numRows);
        assertEquals(numCols,B.numCols);

        for (int col = 0; col < numCols; col++) {
            float max = -Float.MAX_VALUE;
            for (int row = 0; row < numRows; row++) {
                max = Math.max(max,A.get(row,col));
            }
            assertEquals(max,B.get(0,col), UtilEjml.TEST_F32);
        }

        // see of it properly resets the matrix
        FMatrixRMaj C = B.copy();
        B.numRows=1;B.numCols=2;
        CommonOps_FSCC.maxCols(A,B);
        assertTrue( MatrixFeatures_FDRM.isEquals(C,B));
    }

    @Test
    public void sumRows() {
        for (int i = 0; i < 10; i++) {
            sumRows(1,1);
            sumRows(10,1);
            sumRows(1,10);
            sumRows(4,7);
        }
    }

    public void sumRows( int numRows , int numCols ) {
        int nz_total = numRows*numCols/2;
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(numRows,numCols,nz_total,rand);

        FMatrixRMaj B = CommonOps_FSCC.sumRows(A,null);
        assertEquals(numRows,B.numRows);
        assertEquals(1,B.numCols);

        for (int row = 0; row < numRows; row++) {
            float sum = 0;
            for (int col = 0; col < numCols; col++) {
                sum += A.get(row,col);
            }
            assertEquals(sum,B.get(row,0), UtilEjml.TEST_F32);
        }

        // see of it properly resets the matrix
        FMatrixRMaj C = B.copy();
        B.numRows=1;B.numCols=2;
        CommonOps_FSCC.sumRows(A,B);
        assertTrue( MatrixFeatures_FDRM.isEquals(C,B));
    }

    @Test
    public void minRows() {
        for (int i = 0; i < 10; i++) {
            minRows(1,1);
            minRows(10,1);
            minRows(1,10);
            minRows(4,7);
            minRows(7,4);
        }
    }

    public void minRows( int numRows , int numCols ) {
        int nz_total = numRows*numCols/2;
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(numRows,numCols,nz_total,rand);

        FMatrixRMaj B = CommonOps_FSCC.minRows(A,null,null);
        assertEquals(numRows,B.numRows);
        assertEquals(1,B.numCols);

        for (int row = 0; row < numRows; row++) {
            float min = Float.MAX_VALUE;
            for (int col = 0; col < numCols; col++) {
                min = Math.min(min,A.get(row,col));
            }
            assertEquals(min,B.get(row,0), UtilEjml.TEST_F32);
        }

        // see of it properly resets the matrix
        FMatrixRMaj C = B.copy();
        B.numRows=1;B.numCols=2;
        CommonOps_FSCC.minRows(A,B,null);
        assertTrue( MatrixFeatures_FDRM.isEquals(C,B));
    }

    @Test
    public void maxRows() {
        for (int i = 0; i < 10; i++) {
            maxRows(1,1);
            maxRows(10,1);
            maxRows(1,10);
            maxRows(4,7);
            maxRows(7,4);
        }
    }

    public void maxRows( int numRows , int numCols ) {
        int nz_total = numRows*numCols/2;
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(numRows,numCols,nz_total,rand);

        FMatrixRMaj B = CommonOps_FSCC.maxRows(A,null,null);
        assertEquals(numRows,B.numRows);
        assertEquals(1,B.numCols);

        for (int row = 0; row < numRows; row++) {
            float max = -Float.MAX_VALUE;
            for (int col = 0; col < numCols; col++) {
                max = Math.max(max,A.get(row,col));
            }
            assertEquals(max,B.get(row,0), UtilEjml.TEST_F32);
        }

        // see of it properly resets the matrix
        FMatrixRMaj C = B.copy();
        B.numRows=1;B.numCols=2;
        CommonOps_FSCC.maxRows(A,B,null);
        assertTrue( MatrixFeatures_FDRM.isEquals(C,B));
    }

    @Test
    public void zero_range() {
        for (int trial = 0; trial < 20; trial++) {
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(5, 6, 15, rand);
            FMatrixSparseCSC A_orig = A.copy();

            CommonOps_FSCC.zero(A,1,3,2,5);
            assertTrue(CommonOps_FSCC.checkStructure(A));

            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 6; j++) {
                    if( i >= 1 && i < 3 && j >= 2 && j < 5 ) {
                        assertFalse(A.isAssigned(i,j));
                    } else {
                        assertEquals(A.get(i, j), A_orig.get(i, j), UtilEjml.TEST_F32);
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
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(8,4,20,rand);
        FMatrixSparseCSC B = RandomMatrices_FSCC.rectangle(8,6,30,rand);

        float found = CommonOps_FSCC.dotInnerColumns(A,1,B,3,null,null);
        float expected = ImplSparseSparseMult_FSCC.dotInnerColumns(A,1,B,3,null,null);

        assertEquals(expected,found,UtilEjml.TEST_F32);
    }

    @Test
    public void solve_dense() {
        solve_dense(5,5);
        solve_dense(10,5);
    }
    private void solve_dense(int m , int n ) {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(m,n,m*n/2,rand);
        RandomMatrices_FSCC.ensureNotSingular(A,rand);
        FMatrixRMaj X = new FMatrixRMaj(1,1); // arbitrary size
        FMatrixRMaj expected = RandomMatrices_FDRM.rectangle(n,5,rand);
        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(m,5,rand);

        CommonOps_FSCC.mult(A,expected,B);
        assertTrue(CommonOps_FSCC.solve(A,B,X));

        EjmlUnitTests.assertEquals(expected, X, UtilEjml.TEST_F32);
    }

    @Test
    public void solve_sparse() {
        solve_sparse(5,5);
        solve_sparse(10,5);
    }
    private void solve_sparse(int m , int n ) {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(m,n,m*n/2,rand);
        RandomMatrices_FSCC.ensureNotSingular(A,rand);
        FMatrixSparseCSC X = new FMatrixSparseCSC(1,1,1);
        FMatrixSparseCSC expected = RandomMatrices_FSCC.rectangle(n,5,n*5/2,rand);
        FMatrixSparseCSC B = RandomMatrices_FSCC.rectangle(m,5,m*5/2,rand);

        CommonOps_FSCC.mult(A,expected,B);
        assertTrue(CommonOps_FSCC.solve(A,B,X));

        EjmlUnitTests.assertEquals(expected, X, UtilEjml.TEST_F32);
    }


    @Test
    public void invert() {
        for( int m : new int[]{1,2,5,15,40}) {
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(m, m, m * m / 4 + 1, rand);
            RandomMatrices_FSCC.ensureNotSingular(A, rand);

            FMatrixRMaj Ad = new FMatrixRMaj(m, m);
            ConvertFMatrixStruct.convert(A, Ad);

            FMatrixRMaj Ainv = new FMatrixRMaj(m, m);

            assertTrue(CommonOps_FSCC.invert(A, Ainv));


            FMatrixRMaj found = new FMatrixRMaj(m, m);
            CommonOps_FDRM.mult(Ad, Ainv, found);
            assertTrue(MatrixFeatures_FDRM.isIdentity(found, UtilEjml.TEST_F32));
        }
    }

    @Test
    public void det() {
        FMatrixSparseCSC A = UtilEjml.parse_FSCC("7 2 3 0 2 0 6 3 9",3);

        assertEquals(90, CommonOps_FSCC.det(A), UtilEjml.TEST_F32);
    }

    /**
     * A more rigorous test is done in the Impl class
     */
    @Test
    public void removeZeros_two() {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(4,5,6,-1,1,rand);
        FMatrixSparseCSC B = new FMatrixSparseCSC(1,1,1);

        CommonOps_FSCC.removeZeros(A,B,0.01f);

        assertEquals(A.numRows,B.numRows);
        assertEquals(A.numCols,B.numCols);
        for (int j = 0; j < A.numRows; j++) {
            for (int k = 0; k < A.numRows; k++) {
                float val = B.get(j,k);
                assertTrue(Math.abs(val) > 0.01f || val == 0,"val = "+val);
            }
        }
    }

    @Test
    public void multRows() {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(4,5,6,-1,1,rand);

        float factors[] = new float[]{0.1f,0.2f,0.3f,0.4f};

        FMatrixSparseCSC B = A.copy();
        CommonOps_FSCC.multRows(factors, 0, B);

        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
                assertEquals(A.get(i,j)*factors[i],B.get(i,j), UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void divideRows() {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(4,5,6,-1,1,rand);

        float factors[] = new float[]{0.1f,0.2f,0.3f,0.4f};

        FMatrixSparseCSC B = A.copy();
        CommonOps_FSCC.divideRows(factors, 0, B);
        assertTrue(CommonOps_FSCC.checkStructure(B));

        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
                assertEquals(A.get(i,j)/factors[i],B.get(i,j), UtilEjml.TEST_F32);
            }
        }
    }


    /**
     * A more rigorous test is done in the Impl class
     */
    @Test
    public void removeZeros_one() {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(4,5,6,-1,1,rand);

        CommonOps_FSCC.removeZeros(A,0.01f);

        for (int j = 0; j < A.numRows; j++) {
            for (int k = 0; k < A.numRows; k++) {
                float val = A.get(j,k);
                assertTrue(Math.abs(val) > 0.01f || val == 0,"val = "+val);
            }
        }
    }

    @Test
    public void changeSign() {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(4,5,6,-1,1,rand);
        FMatrixSparseCSC B = A.copy();

        CommonOps_FSCC.changeSign(A,A);

        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
                assertEquals(-B.get(i,i),A.get(i,i), UtilEjml.TEST_F32);
            }
        }

        A = B.copy();
        FMatrixSparseCSC C = RandomMatrices_FSCC.rectangle(4,5,6,-1,1,rand);

        CommonOps_FSCC.changeSign(A,C);

        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
                assertEquals(B.get(i,i),A.get(i,i), UtilEjml.TEST_F32);
                assertEquals(-B.get(i,i),C.get(i,i), UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void trace() {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(4,5,14,-1,1,rand);

        float expected = 0;
        for (int i = 0; i < 4; i++) {
            expected += A.get(i,i);
        }

        float found = CommonOps_FSCC.trace(A);
        assertEquals(expected,found,UtilEjml.TEST_F32);
    }

    @Test
    public void applyFunc() {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(10, 10, 20, rand);
        FMatrixSparseCSC B = A.copy();
        CommonOps_FSCC.apply(A, x -> 2 * x + 1, B);

        float[] expectedResult = new float[A.nz_length];
        for (int i = 0; i < A.nz_length; i++) {
            expectedResult[i] = A.nz_values[i] * 2 + 1;
        }

        assertTrue(Arrays.equals(A.col_idx, B.col_idx));
        assertTrue(Arrays.equals(A.nz_rows, B.nz_rows));
        assertTrue(Arrays.equals(expectedResult, B.nz_values));
    }

    @Test
    public void reduceScalar() {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(10, 10, 20, rand);
        float result = CommonOps_FSCC.reduceScalar(A, 0, (acc, x) -> acc + x);

        float expectedResult = 0;
        for (int i = 0; i < A.getNumElements(); i++) {
            expectedResult += A.nz_values[i];
        }

        assertTrue(expectedResult == result);
    }

    @Test
    public void reduceColumnWise() {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(10, 10, 20, rand);

        FMatrixRMaj result = CommonOps_FSCC.reduceColumnWise(A, 0, (acc, x) -> acc + x, null);

        for (int i = 0; i < A.numCols; i++) {
            FMatrixSparseCSC colVector = CommonOps_FSCC.extractColumn(A, i, null);
            float expected = 0;
            for (int j = 0; j < colVector.nz_length; j++) {
                expected += colVector.nz_values[j];
            }
            assertEquals(expected, result.get(i));
        }
    }

    @Test
    public void reduceRowWise() {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(10, 10, 20, rand);

        FMatrixRMaj result = CommonOps_FSCC.reduceRowWise(A, 0, (acc, x) -> acc + x, null);

        for (int i = 0; i < A.numCols; i++) {
            FMatrixSparseCSC A_trans = CommonOps_FSCC.transpose(A, null, null);
            // as A_t[i,:]  == A[:,i]
            FMatrixSparseCSC rowVector = CommonOps_FSCC.extractColumn(A_trans, i, null);
            float expected = 0;
            for (int j = 0; j < rowVector.nz_length; j++) {
                expected += rowVector.nz_values[j];
            }
            assertEquals(expected, result.get(i));
        }
    }
}