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

package org.ejml.dense.blockd3;

import org.ejml.UtilEjml;
import org.ejml.data.BlockD3Matrix64F;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.generic.GenericMatrixOps_F64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestBlockD3MatrixOps {
    final static int BLOCK_LENGTH = 10;

    Random rand = new Random(234);

    @Test
    public void convert_dense_to_block() {
        checkConvert_dense_to_block(10,10);
        checkConvert_dense_to_block(5,8);
        checkConvert_dense_to_block(12,16);
        checkConvert_dense_to_block(16,12);
        checkConvert_dense_to_block(21,27);
        checkConvert_dense_to_block(28,5);
        checkConvert_dense_to_block(5,28);
        checkConvert_dense_to_block(20,20);
    }

    private void checkConvert_dense_to_block( int m , int n ) {
        DMatrixRMaj A = RandomMatrices_DDRM.createRandom(m,n,rand);
        BlockD3Matrix64F B = new BlockD3Matrix64F(A.numRows,A.numCols,BLOCK_LENGTH);

        BlockD3MatrixOps.convert(A,B);

        assertTrue( GenericMatrixOps_F64.isEquivalent(A,B, UtilEjml.TEST_F64));
    }

    @Test
    public void convert_block_to_dense() {
        checkBlockToDense(10,10);
        checkBlockToDense(5,8);
        checkBlockToDense(12,16);
        checkBlockToDense(16,12);
        checkBlockToDense(21,27);
        checkBlockToDense(28,5);
        checkBlockToDense(5,28);
        checkBlockToDense(20,20);
    }

    private void checkBlockToDense( int m , int n ) {
        DMatrixRMaj A = new DMatrixRMaj(m,n);
        BlockD3Matrix64F B = BlockD3MatrixOps.random(m,n,-1,1,rand,BLOCK_LENGTH);

        BlockD3MatrixOps.convert(B,A);

        assertTrue( GenericMatrixOps_F64.isEquivalent(A,B,UtilEjml.TEST_F64));
    }

        @Test
    public void mult() {
        // trivial case
        checkMult(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH);

        // stuff larger than the block size
        checkMult(BLOCK_LENGTH+1, BLOCK_LENGTH, BLOCK_LENGTH);
        checkMult(BLOCK_LENGTH, BLOCK_LENGTH+1, BLOCK_LENGTH);
        checkMult(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH+1);
        checkMult(BLOCK_LENGTH+1, BLOCK_LENGTH+1, BLOCK_LENGTH+1);

        // stuff smaller than the block size
        checkMult(BLOCK_LENGTH-1, BLOCK_LENGTH, BLOCK_LENGTH);
        checkMult(BLOCK_LENGTH, BLOCK_LENGTH-1, BLOCK_LENGTH);
        checkMult(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH-1);
        checkMult(BLOCK_LENGTH-1, BLOCK_LENGTH-1, BLOCK_LENGTH-1);

        // stuff multiple blocks
        checkMult(BLOCK_LENGTH*2, BLOCK_LENGTH, BLOCK_LENGTH);
        checkMult(BLOCK_LENGTH, BLOCK_LENGTH*2, BLOCK_LENGTH);
        checkMult(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH*2);
        checkMult(BLOCK_LENGTH*2, BLOCK_LENGTH*2, BLOCK_LENGTH*2);
        checkMult(BLOCK_LENGTH*2+4, BLOCK_LENGTH*2+3, BLOCK_LENGTH*2+2);

    }

    private void checkMult(int m, int n, int o) {
        DMatrixRMaj A_d = RandomMatrices_DDRM.createRandom(m, n,rand);
        DMatrixRMaj B_d = RandomMatrices_DDRM.createRandom(n, o,rand);
        DMatrixRMaj C_d = new DMatrixRMaj(m, o);

        BlockD3Matrix64F A_b = BlockD3MatrixOps.convert(A_d,BLOCK_LENGTH);
        BlockD3Matrix64F B_b = BlockD3MatrixOps.convert(B_d,BLOCK_LENGTH);
        BlockD3Matrix64F C_b = BlockD3MatrixOps.random(m, o, -1 , 1 , rand , BLOCK_LENGTH);

        CommonOps_DDRM.mult(A_d,B_d,C_d);
        BlockD3MatrixOps.mult(A_b,B_b,C_b);

        assertTrue( GenericMatrixOps_F64.isEquivalent(C_d,C_b,UtilEjml.TEST_F64));
    }
}
