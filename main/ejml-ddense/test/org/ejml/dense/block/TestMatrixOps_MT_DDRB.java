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
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.generic.GenericMatrixOps_F64;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestMatrixOps_MT_DDRB {
    final static int BLOCK_LENGTH = 10;

    Random rand = new Random(234);

    /**
     * Tests for correctness multiplication of an entire matrix for all multiplication operations.
     */
    @Test
    void multiplication() {
        Method[] methods = MatrixOps_MT_DDRB.class.getDeclaredMethods();

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

    private void checkMult( Method funcThreads, boolean transA, boolean transB,
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
            funcThreads.invoke(null, A_b, B_b, C_b);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

//        C_d.print();
//        C_b.print();
        assertTrue(GenericMatrixOps_F64.isEquivalent(C_d, C_b, UtilEjml.TEST_F64));
    }
}

