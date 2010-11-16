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

import org.ejml.alg.dense.misc.UnrolledInverseFromMinor;
import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * @author Peter Abeles
 */
public class TestBlockTriangularSolver {

    Random rand = new Random(234534);

    @Test
    public void invert_two() {
        // block size
        int r = 3;

        double temp[] = new double[r*r];

        for( int size = 1; size <= 9; size++ ) {
            BlockMatrix64F T = BlockMatrixOps.createRandom(size,size,-1,1,rand,r);
            BlockMatrixOps.zeroTriangle(true,T);

            BlockMatrix64F T_inv = T.copy();

            BlockTriangularSolver.invert(r,false,new D1Submatrix64F(T),new D1Submatrix64F(T_inv),temp);

            BlockMatrix64F C = new BlockMatrix64F(size,size,r);

            BlockMatrixOps.mult(T,T_inv,C);

            assertTrue(GenericMatrixOps.isIdentity(C,1e-8));

            // see if passing in the same matrix instance twice messes it up or not
            BlockTriangularSolver.invert(r,false,new D1Submatrix64F(T),new D1Submatrix64F(T),temp);

            assertTrue(BlockMatrixOps.isIdentical(T,T_inv,1e-8));
        }
    }

    @Test
    public void invert_one() {
        // block size
        int r = 3;

        double temp[] = new double[r*r];

        for( int size = 1; size <= 9; size++ ) {
            BlockMatrix64F T = BlockMatrixOps.createRandom(size,size,-1,1,rand,r);
            BlockMatrixOps.zeroTriangle(true,T);

            BlockMatrix64F T_inv = T.copy();

            BlockTriangularSolver.invert(r,false,new D1Submatrix64F(T_inv),temp);

            BlockMatrix64F C = new BlockMatrix64F(size,size,r);

            BlockMatrixOps.mult(T,T_inv,C);

            assertTrue(GenericMatrixOps.isIdentity(C,1e-8));
        }
    }


    /**
     * Test solving several different triangular systems with different sizes.
     */
    @Test
    public void testSolve() {
        // block size
        int r = 3;

        for( int dir = 0; dir < 2; dir++ ) {
            boolean upper = dir == 0;
            for( int triangleSize = 1; triangleSize <= 9; triangleSize++ ) {
                for( int cols = 1; cols <= 9; cols++ ) {
//                System.out.println("triangle "+triangleSize+" cols "+cols);
                    BlockMatrix64F T = BlockMatrixOps.createRandom(triangleSize,triangleSize,-1,1,rand,r);
                    BlockMatrixOps.zeroTriangle(true,T);

                    if( upper ) {
                        T=BlockMatrixOps.transpose(T,null);
                    }

                    BlockMatrix64F B = BlockMatrixOps.createRandom(triangleSize,cols,-1,1,rand,r);
                    BlockMatrix64F Y = new BlockMatrix64F(B.numRows,B.numCols,r);

                    checkSolve(T,B,Y,r,upper,false);
                    checkSolve(T,B,Y,r,upper,true);
                }
            }
        }
    }

    /**
     * Checks to see if BlockTriangularSolver.solve produces the expected output given
     * these inputs.  The solution is computed directly.
     */
    private void checkSolve( BlockMatrix64F T , BlockMatrix64F B , BlockMatrix64F Y ,
                             int r , boolean upper , boolean transT )
    {
        if( transT ) {
            BlockMatrix64F T_tran = BlockMatrixOps.transpose(T,null);

            // Compute Y directly from the expected result B
            BlockMatrixOps.mult(T_tran,B,Y);
        } else {
            // Compute Y directly from the expected result B
            BlockMatrixOps.mult(T,B,Y);
        }

        // Y is overwritten with the solution
        BlockTriangularSolver.solve(r,upper,new D1Submatrix64F(T),new D1Submatrix64F(Y),transT);

        assertTrue( BlockMatrixOps.isIdentical(B,Y,1e-8));
    }


    /**
     * Check all permutations of solve for submatrices
     */
    @Test
    public void testSolveBlock() {
        check_solveBlock_submatrix(false,false,false);
        check_solveBlock_submatrix(true,false,false);
        check_solveBlock_submatrix(false,true,false);
        check_solveBlock_submatrix(true,true,false);
//        check_solveBlock_submatrix(false,false,true);
        check_solveBlock_submatrix(true,false,true);
//        check_solveBlock_submatrix(false,true,true);
//        check_solveBlock_submatrix(false,true,true);
    }

    /**
     * Checks to see if solve functions that use sub matrices as input work correctly
     */
    private void check_solveBlock_submatrix( boolean solveL , boolean transT , boolean transB ) {
        // compute expected solution
        DenseMatrix64F L = createRandomLowerTriangular(3);
        DenseMatrix64F B = RandomMatrices.createRandom(3,5,rand);
        DenseMatrix64F X = new DenseMatrix64F(3,5);

        if( !solveL ) {
            CommonOps.transpose(L);
        }

        if( transT ) {
           CommonOps.transpose(L);
        }

        CommonOps.solve(L,B,X);

        // do it again using block matrices
        BlockMatrix64F b_L = BlockMatrixOps.convert(L,3);
        BlockMatrix64F b_B = BlockMatrixOps.convert(B,3);

        D1Submatrix64F sub_L = new D1Submatrix64F(b_L,0, 3, 0, 3);
        D1Submatrix64F sub_B = new D1Submatrix64F(b_B,0, 3, 0, 5);

        if( transT ) {
            sub_L.original = BlockMatrixOps.transpose((BlockMatrix64F)sub_L.original,null);
            TestBlockMultiplication.transposeSub(sub_L);
        }

        if( transB ) {
            sub_B.original = b_B = BlockMatrixOps.transpose((BlockMatrix64F)sub_B.original,null);
            TestBlockMultiplication.transposeSub(sub_B);
            CommonOps.transpose(X);
        }

//        sub_L.original.print();
//        sub_B.original.print();

        BlockTriangularSolver.solveBlock(3,!solveL,sub_L,sub_B,transT,transB);

        assertTrue(GenericMatrixOps.isEquivalent(X,b_B,1e-10));
    }

    private DenseMatrix64F createRandomLowerTriangular( int N ) {
        DenseMatrix64F U = RandomMatrices.createUpperTriangle(N,0,-1,1,rand);

        CommonOps.transpose(U);

        return U;
    }
}
