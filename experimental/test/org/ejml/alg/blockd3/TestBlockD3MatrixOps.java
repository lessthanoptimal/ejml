/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.blockd3;

import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.BlockD3Matrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;
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
        DenseMatrix64F A = RandomMatrices.createRandom(m,n,rand);
        BlockD3Matrix64F B = new BlockD3Matrix64F(A.numRows,A.numCols,BLOCK_LENGTH);

        BlockD3MatrixOps.convert(A,B);

        assertTrue( GenericMatrixOps.isEquivalent(A,B,1e-8));
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
        DenseMatrix64F A = new DenseMatrix64F(m,n);
        BlockD3Matrix64F B = BlockD3MatrixOps.random(m,n,-1,1,rand,BLOCK_LENGTH);

        BlockD3MatrixOps.convert(B,A);

        assertTrue( GenericMatrixOps.isEquivalent(A,B,1e-8));
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
        DenseMatrix64F A_d = RandomMatrices.createRandom(m, n,rand);
        DenseMatrix64F B_d = RandomMatrices.createRandom(n, o,rand);
        DenseMatrix64F C_d = new DenseMatrix64F(m, o);

        BlockD3Matrix64F A_b = BlockD3MatrixOps.convert(A_d,BLOCK_LENGTH);
        BlockD3Matrix64F B_b = BlockD3MatrixOps.convert(B_d,BLOCK_LENGTH);
        BlockD3Matrix64F C_b = BlockD3MatrixOps.random(m, o, -1 , 1 , rand , BLOCK_LENGTH);

        CommonOps.mult(A_d,B_d,C_d);
        BlockD3MatrixOps.mult(A_b,B_b,C_b);

        assertTrue( GenericMatrixOps.isEquivalent(C_d,C_b,1e-8));
    }
}
