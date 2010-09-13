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

package org.ejml.alg.block;

import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestBlockMatrixOps {

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
        BlockMatrix64F B = new BlockMatrix64F(A.numRows,A.numCols,BLOCK_LENGTH);

        BlockMatrixOps.convert(A,B);

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
        BlockMatrix64F B = BlockMatrixOps.createRandom(m,n,-1,1,rand);

        BlockMatrixOps.convert(B,A);

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

        BlockMatrix64F A_b = BlockMatrixOps.convert(A_d,BLOCK_LENGTH);
        BlockMatrix64F B_b = BlockMatrixOps.convert(B_d,BLOCK_LENGTH);
        BlockMatrix64F C_b = BlockMatrixOps.createRandom(m, o, -1 , 1 , rand , BLOCK_LENGTH);

        CommonOps.mult(A_d,B_d,C_d);
        BlockMatrixOps.mult(A_b,B_b,C_b);

        assertTrue( GenericMatrixOps.isEquivalent(C_d,C_b,1e-8));
    }

    @Test
    public void multTransA() {
        // trivial case
        checkMultTransA(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH);

        // stuff larger than the block size
        checkMultTransA(BLOCK_LENGTH+1, BLOCK_LENGTH, BLOCK_LENGTH);
        checkMultTransA(BLOCK_LENGTH, BLOCK_LENGTH+1, BLOCK_LENGTH);
        checkMultTransA(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH+1);
        checkMultTransA(BLOCK_LENGTH+1, BLOCK_LENGTH+1, BLOCK_LENGTH+1);

        // stuff smaller than the block size
        checkMultTransA(BLOCK_LENGTH-1, BLOCK_LENGTH, BLOCK_LENGTH);
        checkMultTransA(BLOCK_LENGTH, BLOCK_LENGTH-1, BLOCK_LENGTH);
        checkMultTransA(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH-1);
        checkMultTransA(BLOCK_LENGTH-1, BLOCK_LENGTH-1, BLOCK_LENGTH-1);

        // stuff multiple blocks
        checkMultTransA(BLOCK_LENGTH*2, BLOCK_LENGTH, BLOCK_LENGTH);
        checkMultTransA(BLOCK_LENGTH, BLOCK_LENGTH*2, BLOCK_LENGTH);
        checkMultTransA(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH*2);
        checkMultTransA(BLOCK_LENGTH*2, BLOCK_LENGTH*2, BLOCK_LENGTH*2);
        checkMultTransA(BLOCK_LENGTH*2+4, BLOCK_LENGTH*2+3, BLOCK_LENGTH*2+2);

    }

    private void checkMultTransA(int m, int n, int o) {
        DenseMatrix64F A_d = RandomMatrices.createRandom(n, m,rand);
        DenseMatrix64F B_d = RandomMatrices.createRandom(n, o,rand);
        DenseMatrix64F C_d = new DenseMatrix64F(m, o);

        BlockMatrix64F A_b = BlockMatrixOps.convert(A_d,BLOCK_LENGTH);
        BlockMatrix64F B_b = BlockMatrixOps.convert(B_d,BLOCK_LENGTH);
        BlockMatrix64F C_b = BlockMatrixOps.createRandom(m, o, -1 , 1 , rand , BLOCK_LENGTH);

        CommonOps.multTransA(A_d,B_d,C_d);
        BlockMatrixOps.multTransA(A_b,B_b,C_b);

        assertTrue( GenericMatrixOps.isEquivalent(C_d,C_b,1e-8));
    }

    @Test
    public void multTransB() {
        // trivial case
        checkMultTransB(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH);

        // stuff larger than the block size
        checkMultTransB(BLOCK_LENGTH+1, BLOCK_LENGTH, BLOCK_LENGTH);
        checkMultTransB(BLOCK_LENGTH, BLOCK_LENGTH+1, BLOCK_LENGTH);
        checkMultTransB(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH+1);
        checkMultTransB(BLOCK_LENGTH+1, BLOCK_LENGTH+1, BLOCK_LENGTH+1);

        // stuff smaller than the block size
        checkMultTransB(BLOCK_LENGTH-1, BLOCK_LENGTH, BLOCK_LENGTH);
        checkMultTransB(BLOCK_LENGTH, BLOCK_LENGTH-1, BLOCK_LENGTH);
        checkMultTransB(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH-1);
        checkMultTransB(BLOCK_LENGTH-1, BLOCK_LENGTH-1, BLOCK_LENGTH-1);

        // stuff multiple blocks
        checkMultTransB(BLOCK_LENGTH*2, BLOCK_LENGTH, BLOCK_LENGTH);
        checkMultTransB(BLOCK_LENGTH, BLOCK_LENGTH*2, BLOCK_LENGTH);
        checkMultTransB(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH*2);
        checkMultTransB(BLOCK_LENGTH*2, BLOCK_LENGTH*2, BLOCK_LENGTH*2);
        checkMultTransB(BLOCK_LENGTH*2+4, BLOCK_LENGTH*2+3, BLOCK_LENGTH*2+2);

    }

    private void checkMultTransB(int m, int n, int o) {
        DenseMatrix64F A_d = RandomMatrices.createRandom(m, n,rand);
        DenseMatrix64F B_d = RandomMatrices.createRandom(o, n,rand);
        DenseMatrix64F C_d = new DenseMatrix64F(m, o);

        BlockMatrix64F A_b = BlockMatrixOps.convert(A_d,BLOCK_LENGTH);
        BlockMatrix64F B_b = BlockMatrixOps.convert(B_d,BLOCK_LENGTH);
        BlockMatrix64F C_b = BlockMatrixOps.createRandom(m, o, -1 , 1 , rand , BLOCK_LENGTH);

        CommonOps.multTransB(A_d,B_d,C_d);
        BlockMatrixOps.multTransB(A_b,B_b,C_b);

        assertTrue( GenericMatrixOps.isEquivalent(C_d,C_b,1e-8));
    }

    @Test
    public void convertTranSrc_block_to_dense() {
        checkTranSrcBlockToDense(10,10);
        checkTranSrcBlockToDense(5,8);
        checkTranSrcBlockToDense(12,16);
        checkTranSrcBlockToDense(16,12);
        checkTranSrcBlockToDense(21,27);
        checkTranSrcBlockToDense(28,5);
        checkTranSrcBlockToDense(5,28);
        checkTranSrcBlockToDense(20,20);
    }

    private void checkTranSrcBlockToDense( int m , int n ) {
        DenseMatrix64F A = RandomMatrices.createRandom(m,n,rand);
        DenseMatrix64F A_t = new DenseMatrix64F(n,m);
        BlockMatrix64F B = new BlockMatrix64F(n,m,BLOCK_LENGTH);

        CommonOps.transpose(A,A_t);
        BlockMatrixOps.convertTranSrc(A,B);

        assertTrue( GenericMatrixOps.isEquivalent(A_t,B,1e-8));
    }

    @Test
    public void transpose() {
        checkTranspose(10,10);
        checkTranspose(5,8);
        checkTranspose(12,16);
        checkTranspose(16,12);
        checkTranspose(21,27);
        checkTranspose(28,5);
        checkTranspose(5,28);
        checkTranspose(20,20);
    }

    private void checkTranspose( int m , int n ) {
        DenseMatrix64F A = RandomMatrices.createRandom(m,n,rand);
        DenseMatrix64F A_t = new DenseMatrix64F(n,m);

        BlockMatrix64F B = new BlockMatrix64F(A.numRows,A.numCols,BLOCK_LENGTH);
        BlockMatrix64F B_t = new BlockMatrix64F(n,m,BLOCK_LENGTH);

        BlockMatrixOps.convert(A,B);

        CommonOps.transpose(A,A_t);
        BlockMatrixOps.transpose(B,B_t);

        assertTrue( GenericMatrixOps.isEquivalent(A_t,B_t,1e-8));
    }
}
