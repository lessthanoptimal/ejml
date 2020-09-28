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

package org.ejml.dense.block;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DSubmatrixD1;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.generic.GenericMatrixOps_F64;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestMatrixOps_DDRB {

    final static int BLOCK_LENGTH = 10;
    Random rand = new Random(234);

    @Test
    void convert_dense_to_block() {
        checkConvert_dense_to_block(10, 10);
        checkConvert_dense_to_block(5, 8);
        checkConvert_dense_to_block(12, 16);
        checkConvert_dense_to_block(16, 12);
        checkConvert_dense_to_block(21, 27);
        checkConvert_dense_to_block(28, 5);
        checkConvert_dense_to_block(5, 28);
        checkConvert_dense_to_block(20, 20);
    }

    private void checkConvert_dense_to_block( int m, int n ) {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(m, n, rand);
        DMatrixRBlock B = new DMatrixRBlock(A.numRows, A.numCols, BLOCK_LENGTH);

        MatrixOps_DDRB.convert(A, B);

        assertTrue(GenericMatrixOps_F64.isEquivalent(A, B, UtilEjml.TEST_F64));
    }

    @Test
    void convertInplace_DDRM_DDRB() {
        int r = 3;
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(23, 21, rand);
        DMatrixRMaj A_orig = A.copy();

        DMatrixRBlock B = MatrixOps_DDRB.convertInplace(A,new DMatrixRBlock(1,1,r),null);

        assertTrue(GenericMatrixOps_F64.isEquivalent(A_orig, B, UtilEjml.TEST_F64));
        assertSame(A.data, B.data);
    }

    @Test
    void convertInplace_DDRB_DDRM() {
        int r = 3;
        DMatrixRBlock B = MatrixOps_DDRB.createRandom(23, 21, -1, 1, rand, r);
        DMatrixRBlock B_orig = B.copy();

        DMatrixRMaj A = MatrixOps_DDRB.convertInplace(B,null,null);

        assertTrue(GenericMatrixOps_F64.isEquivalent(B_orig, A, UtilEjml.TEST_F64));
        assertSame(B.data, A.data);
    }

    @Test
    void convertInline_dense_to_block() {
        for (int i = 2; i < 30; i += 5) {
            for (int j = 2; j < 30; j += 5) {
                checkConvertInline_dense_to_block(i, j);
            }
        }
    }

    private void checkConvertInline_dense_to_block( int m, int n ) {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(m, n, rand);
        DMatrixRMaj A_orig = A.copy();

        MatrixOps_DDRB.convertRowToBlock(m, n, BLOCK_LENGTH, A.data, null);
        DMatrixRBlock B = DMatrixRBlock.wrap(A.data, A.numRows, A.numCols, BLOCK_LENGTH);

        assertTrue(GenericMatrixOps_F64.isEquivalent(A_orig, B, UtilEjml.TEST_F64));
    }

    @Test
    void convert_block_to_dense() {
        checkBlockToDense(10, 10);
        checkBlockToDense(5, 8);
        checkBlockToDense(12, 16);
        checkBlockToDense(16, 12);
        checkBlockToDense(21, 27);
        checkBlockToDense(28, 5);
        checkBlockToDense(5, 28);
        checkBlockToDense(20, 20);
    }

    private void checkBlockToDense( int m, int n ) {
        DMatrixRMaj A = new DMatrixRMaj(m, n);
        DMatrixRBlock B = MatrixOps_DDRB.createRandom(m, n, -1, 1, rand);

        MatrixOps_DDRB.convert(B, A);

        assertTrue(GenericMatrixOps_F64.isEquivalent(A, B, UtilEjml.TEST_F64));
    }

    @Test
    void convertInline_block_to_dense() {
        for (int i = 2; i < 30; i += 5) {
            for (int j = 2; j < 30; j += 5) {
                checkConvertInline_block_to_dense(i, j);
            }
        }
    }

    private void checkConvertInline_block_to_dense( int m, int n ) {
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(m, n, -1, 1, rand, BLOCK_LENGTH);
        DMatrixRBlock A_orig = A.copy();

        MatrixOps_DDRB.convertBlockToRow(m, n, BLOCK_LENGTH, A.data, null);
        DMatrixRMaj B = DMatrixRMaj.wrap(A.numRows, A.numCols, A.data);

        assertTrue(GenericMatrixOps_F64.isEquivalent(A_orig, B, UtilEjml.TEST_F64));
    }

    /**
     * Makes sure the bounds check on input matrices for mult() is done correctly
     */
    @Test
    void testMultInputChecks() {
        Method[] methods = MatrixOps_DDRB.class.getDeclaredMethods();

        int numFound = 0;
        for (Method m : methods) {
            String name = m.getName();

            if (!name.contains("mult"))
                continue;

            boolean transA = false;
            boolean transB = false;

            if (name.contains("TransA"))
                transA = true;

            if (name.contains("TransB"))
                transB = true;

            checkMultInput(m, transA, transB);
            numFound++;
        }

        // make sure all the functions were in fact tested
        assertEquals(3, numFound);
    }

    /**
     * Makes sure exceptions are thrown for badly shaped input matrices.
     */
    private void checkMultInput( Method func, boolean transA, boolean transB ) {
        // bad block size
        DMatrixRBlock A = new DMatrixRBlock(5, 4, 3);
        DMatrixRBlock B = new DMatrixRBlock(4, 6, 3);
        DMatrixRBlock C = new DMatrixRBlock(5, 6, 4);

        invokeErrorCheck(func, transA, transB, A, B, C);
        C.blockLength = 3;
        B.blockLength = 4;
        invokeErrorCheck(func, transA, transB, A, B, C);
        B.blockLength = 3;
        A.blockLength = 4;
        invokeErrorCheck(func, transA, transB, A, B, C);
        A.blockLength = 3;

        // check for bad size C
        C.numCols = 7;
        invokeErrorCheck(func, transA, transB, A, B, C);
        C.numCols = 6;
        C.numRows = 4;
        invokeErrorCheck(func, transA, transB, A, B, C);

        // make A and B incompatible
        A.numCols = 3;
        invokeErrorCheck(func, transA, transB, A, B, C);
    }

    private void invokeErrorCheck( Method func, boolean transA, boolean transB,
                                   DMatrixRBlock a, DMatrixRBlock b, DMatrixRBlock c ) {

        if (transA)
            a = MatrixOps_DDRB.transpose(a, null);
        if (transB)
            b = MatrixOps_DDRB.transpose(b, null);

        try {
            func.invoke(null, a, b, c);
            fail("No exception");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (!(e.getCause() instanceof IllegalArgumentException))
                fail("Unexpected exception: " + e.getCause().getMessage());
        }
    }

    /**
     * Tests for correctness multiplication of an entire matrix for all multiplication operations.
     */
    @Test
    void multiplication() {
        Method[] methods = MatrixOps_DDRB.class.getDeclaredMethods();

        int numFound = 0;
        for (Method m : methods) {
            String name = m.getName();

            if (!name.contains("mult"))
                continue;

//            System.out.println("name = "+name);

            boolean transA = false;
            boolean transB = false;

            if (name.contains("TransA"))
                transA = true;

            if (name.contains("TransB"))
                transB = true;

            checkMult(m, transA, transB);
            numFound++;
        }

        // make sure all the functions were in fact tested
        assertEquals(3, numFound);
    }

    /**
     * Test the method against various matrices of different sizes and shapes which have partial
     * blocks.
     */
    private void checkMult( Method func, boolean transA, boolean transB ) {
        // trivial case
        checkMult(func, transA, transB, BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH);

        // stuff larger than the block size
        checkMult(func, transA, transB, BLOCK_LENGTH + 1, BLOCK_LENGTH, BLOCK_LENGTH);
        checkMult(func, transA, transB, BLOCK_LENGTH, BLOCK_LENGTH + 1, BLOCK_LENGTH);
        checkMult(func, transA, transB, BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH + 1);
        checkMult(func, transA, transB, BLOCK_LENGTH + 1, BLOCK_LENGTH + 1, BLOCK_LENGTH + 1);

        // stuff smaller than the block size
        checkMult(func, transA, transB, BLOCK_LENGTH - 1, BLOCK_LENGTH, BLOCK_LENGTH);
        checkMult(func, transA, transB, BLOCK_LENGTH, BLOCK_LENGTH - 1, BLOCK_LENGTH);
        checkMult(func, transA, transB, BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH - 1);
        checkMult(func, transA, transB, BLOCK_LENGTH - 1, BLOCK_LENGTH - 1, BLOCK_LENGTH - 1);

        // stuff multiple blocks
        checkMult(func, transA, transB, BLOCK_LENGTH*2, BLOCK_LENGTH, BLOCK_LENGTH);
        checkMult(func, transA, transB, BLOCK_LENGTH, BLOCK_LENGTH*2, BLOCK_LENGTH);
        checkMult(func, transA, transB, BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH*2);
        checkMult(func, transA, transB, BLOCK_LENGTH*2, BLOCK_LENGTH*2, BLOCK_LENGTH*2);
        checkMult(func, transA, transB, BLOCK_LENGTH*2 + 4, BLOCK_LENGTH*2 + 3, BLOCK_LENGTH*2 + 2);
    }

    private void checkMult( Method func, boolean transA, boolean transB,
                            int m, int n, int o ) {
        DMatrixRMaj A_d = RandomMatrices_DDRM.rectangle(m, n, rand);
        DMatrixRMaj B_d = RandomMatrices_DDRM.rectangle(n, o, rand);
        DMatrixRMaj C_d = new DMatrixRMaj(m, o);

        DMatrixRBlock A_b = MatrixOps_DDRB.convert(A_d, BLOCK_LENGTH);
        DMatrixRBlock B_b = MatrixOps_DDRB.convert(B_d, BLOCK_LENGTH);
        DMatrixRBlock C_b = MatrixOps_DDRB.createRandom(m, o, -1, 1, rand, BLOCK_LENGTH);

        if (transA)
            A_b = MatrixOps_DDRB.transpose(A_b, null);

        if (transB)
            B_b = MatrixOps_DDRB.transpose(B_b, null);

        CommonOps_DDRM.mult(A_d, B_d, C_d);
        try {
            func.invoke(null, A_b, B_b, C_b);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

//        C_d.print();
//        C_b.print();
        assertTrue(GenericMatrixOps_F64.isEquivalent(C_d, C_b, UtilEjml.TEST_F64));
    }

    @Test
    void convertTranSrc_block_to_dense() {
        checkTranSrcBlockToDense(10, 10);
        checkTranSrcBlockToDense(5, 8);
        checkTranSrcBlockToDense(12, 16);
        checkTranSrcBlockToDense(16, 12);
        checkTranSrcBlockToDense(21, 27);
        checkTranSrcBlockToDense(28, 5);
        checkTranSrcBlockToDense(5, 28);
        checkTranSrcBlockToDense(20, 20);
    }

    private void checkTranSrcBlockToDense( int m, int n ) {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(m, n, rand);
        DMatrixRMaj A_t = new DMatrixRMaj(n, m);
        DMatrixRBlock B = new DMatrixRBlock(n, m, BLOCK_LENGTH);

        CommonOps_DDRM.transpose(A, A_t);
        MatrixOps_DDRB.convertTranSrc(A, B);

        assertTrue(GenericMatrixOps_F64.isEquivalent(A_t, B, UtilEjml.TEST_F64));
    }

    @Test
    void transpose() {
        checkTranspose(10, 10);
        checkTranspose(5, 8);
        checkTranspose(12, 16);
        checkTranspose(16, 12);
        checkTranspose(21, 27);
        checkTranspose(28, 5);
        checkTranspose(5, 28);
        checkTranspose(20, 20);
    }

    private void checkTranspose( int m, int n ) {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(m, n, rand);
        DMatrixRMaj A_t = new DMatrixRMaj(n, m);

        DMatrixRBlock B = new DMatrixRBlock(A.numRows, A.numCols, BLOCK_LENGTH);
        DMatrixRBlock B_t = new DMatrixRBlock(n, m, BLOCK_LENGTH);

        MatrixOps_DDRB.convert(A, B);

        CommonOps_DDRM.transpose(A, A_t);
        MatrixOps_DDRB.transpose(B, B_t);

        assertTrue(GenericMatrixOps_F64.isEquivalent(A_t, B_t, UtilEjml.TEST_F64));
    }

    @Test
    void zeroTriangle_upper() {
        int r = 3;

        for (int numRows = 2; numRows <= 6; numRows += 2) {
            for (int numCols = 2; numCols <= 6; numCols += 2) {
                DMatrixRBlock B = MatrixOps_DDRB.createRandom(numRows, numCols, -1, 1, rand, r);
                MatrixOps_DDRB.zeroTriangle(true, B);

                for (int i = 0; i < B.numRows; i++) {
                    for (int j = 0; j < B.numCols; j++) {
                        if (j <= i)
                            assertNotEquals(B.get(i, j), 0);
                        else
                            assertEquals(B.get(i, j), 0);
                    }
                }
            }
        }
    }

    @Test
    void zeroTriangle_lower() {

        int r = 3;

        for (int numRows = 2; numRows <= 6; numRows += 2) {
            for (int numCols = 2; numCols <= 6; numCols += 2) {
                DMatrixRBlock B = MatrixOps_DDRB.createRandom(numRows, numCols, -1, 1, rand, r);

                MatrixOps_DDRB.zeroTriangle(false, B);

                for (int i = 0; i < B.numRows; i++) {
                    for (int j = 0; j < B.numCols; j++) {
                        if (j >= i)
                            assertNotEquals(B.get(i, j), 0);
                        else
                            assertEquals(B.get(i, j), 0);
                    }
                }
            }
        }
    }

    @Test
    void copyTriangle() {

        int r = 3;

        // test where src and dst are the same size
        for (int numRows = 2; numRows <= 6; numRows += 2) {
            for (int numCols = 2; numCols <= 6; numCols += 2) {
                DMatrixRBlock A = MatrixOps_DDRB.createRandom(numRows, numCols, -1, 1, rand, r);
                DMatrixRBlock B = new DMatrixRBlock(numRows, numCols, r);

                MatrixOps_DDRB.copyTriangle(true, A, B);

                for (int i = 0; i < numRows; i++) {
                    for (int j = 0; j < numCols; j++) {
                        if (j >= i)
                            assertEquals(B.get(i, j), A.get(i, j));
                        else
                            assertEquals(B.get(i, j), 0);
                    }
                }

                CommonOps_DDRM.fill(B, 0);
                MatrixOps_DDRB.copyTriangle(false, A, B);

                for (int i = 0; i < numRows; i++) {
                    for (int j = 0; j < numCols; j++) {
                        if (j <= i)
                            assertEquals(B.get(i, j), A.get(i, j));
                        else
                            assertEquals(B.get(i, j), 0);
                    }
                }
            }
        }

        // now the dst will be smaller than the source
        DMatrixRBlock B = new DMatrixRBlock(r + 1, r + 1, r);
        for (int numRows = 4; numRows <= 6; numRows += 1) {
            for (int numCols = 4; numCols <= 6; numCols += 1) {
                DMatrixRBlock A = MatrixOps_DDRB.createRandom(numRows, numCols, -1, 1, rand, r);
                CommonOps_DDRM.fill(B, 0);

                MatrixOps_DDRB.copyTriangle(true, A, B);

                for (int i = 0; i < B.numRows; i++) {
                    for (int j = 0; j < B.numCols; j++) {
                        if (j >= i)
                            assertEquals(B.get(i, j), A.get(i, j));
                        else
                            assertEquals(B.get(i, j), 0);
                    }
                }

                CommonOps_DDRM.fill(B, 0);
                MatrixOps_DDRB.copyTriangle(false, A, B);

                for (int i = 0; i < B.numRows; i++) {
                    for (int j = 0; j < B.numCols; j++) {
                        if (j <= i)
                            assertEquals(B.get(i, j), A.get(i, j));
                        else
                            assertEquals(B.get(i, j), 0);
                    }
                }
            }
        }
    }

    @Test
    void setIdentity() {
        int r = 3;

        for (int numRows = 2; numRows <= 6; numRows += 2) {
            for (int numCols = 2; numCols <= 6; numCols += 2) {
                DMatrixRBlock A = MatrixOps_DDRB.createRandom(numRows, numCols, -1, 1, rand, r);

                MatrixOps_DDRB.setIdentity(A);

                for (int i = 0; i < numRows; i++) {
                    for (int j = 0; j < numCols; j++) {
                        if (i == j)
                            assertEquals(1.0, A.get(i, j), UtilEjml.TEST_F64);
                        else
                            assertEquals(0.0, A.get(i, j), UtilEjml.TEST_F64);
                    }
                }
            }
        }
    }

    @Test
    void convertSimple() {
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(4, 6, -1, 1, rand, 3);

        SimpleMatrix S = new SimpleMatrix(A);

        assertEquals(A.numRows, S.numRows());
        assertEquals(A.numCols, S.numCols());

        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
                assertEquals(A.get(i, j), S.get(i, j), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    void identity() {
        // test square
        DMatrixRBlock A = MatrixOps_DDRB.identity(4, 4, 3);
        assertTrue(GenericMatrixOps_F64.isIdentity(A, UtilEjml.TEST_F64));

        // test wide
        A = MatrixOps_DDRB.identity(4, 5, 3);
        assertTrue(GenericMatrixOps_F64.isIdentity(A, UtilEjml.TEST_F64));

        // test tall
        A = MatrixOps_DDRB.identity(5, 4, 3);
        assertTrue(GenericMatrixOps_F64.isIdentity(A, UtilEjml.TEST_F64));
    }

    @Test
    void extractAligned() {
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(10, 11, -1, 1, rand, 3);
        DMatrixRBlock B = new DMatrixRBlock(9, 11, 3);

        MatrixOps_DDRB.extractAligned(A, B);

        for (int i = 0; i < B.numRows; i++) {
            for (int j = 0; j < B.numCols; j++) {
                assertEquals(A.get(i, j), B.get(i, j), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    void blockAligned() {
        int r = 3;
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(10, 11, -1, 1, rand, r);

        DSubmatrixD1 S = new DSubmatrixD1(A);

        assertTrue(MatrixOps_DDRB.blockAligned(r, S));

        S.row0 = r;
        S.col0 = 2*r;

        assertTrue(MatrixOps_DDRB.blockAligned(r, S));

        // test negative cases
        S.row0 = r - 1;
        assertFalse(MatrixOps_DDRB.blockAligned(r, S));
        S.row0 = 0;
        S.col0 = 1;
        assertFalse(MatrixOps_DDRB.blockAligned(r, S));
        S.col0 = 0;
        S.row1 = 8;
        assertFalse(MatrixOps_DDRB.blockAligned(r, S));
        S.row1 = 10;
        S.col0 = 10;
        assertFalse(MatrixOps_DDRB.blockAligned(r, S));
    }
}
