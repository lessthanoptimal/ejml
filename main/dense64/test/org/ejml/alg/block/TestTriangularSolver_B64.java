/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.block;

import org.ejml.UtilEjml;
import org.ejml.alg.generic.GenericMatrixOps_F64;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps_D64;
import org.ejml.ops.RandomMatrices_D64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestTriangularSolver_B64 {

    Random rand = new Random(234534);

    @Test
    public void invert_two() {
        // block size
        int r = 3;

        double temp[] = new double[r*r];

        for( int size = 1; size <= 9; size++ ) {
            BlockMatrix64F T = MatrixOps_B64.createRandom(size,size,-1,1,rand,r);
            MatrixOps_B64.zeroTriangle(true,T);

            BlockMatrix64F T_inv = T.copy();

            TriangularSolver_B64.invert(r,false,new D1Submatrix64F(T),new D1Submatrix64F(T_inv),temp);

            BlockMatrix64F C = new BlockMatrix64F(size,size,r);

            MatrixOps_B64.mult(T,T_inv,C);

            assertTrue(GenericMatrixOps_F64.isIdentity(C,UtilEjml.TEST_64F));

            // see if passing in the same matrix instance twice messes it up or not
            TriangularSolver_B64.invert(r,false,new D1Submatrix64F(T),new D1Submatrix64F(T),temp);

            assertTrue(MatrixOps_B64.isEquals(T,T_inv, UtilEjml.TEST_64F));
        }
    }

    @Test
    public void invert_one() {
        // block size
        int r = 3;

        double temp[] = new double[r*r];

        for( int size = 1; size <= 9; size++ ) {
            BlockMatrix64F T = MatrixOps_B64.createRandom(size,size,-1,1,rand,r);
            MatrixOps_B64.zeroTriangle(true,T);

            BlockMatrix64F T_inv = T.copy();

            TriangularSolver_B64.invert(r,false,new D1Submatrix64F(T_inv),temp);

            BlockMatrix64F C = new BlockMatrix64F(size,size,r);

            MatrixOps_B64.mult(T,T_inv,C);

            assertTrue(GenericMatrixOps_F64.isIdentity(C,UtilEjml.TEST_64F));
        }
    }


    /**
     * Test solving several different triangular systems with different sizes.
     * All matrices begin and end along block boundaries.
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
                    BlockMatrix64F T = MatrixOps_B64.createRandom(triangleSize,triangleSize,-1,1,rand,r);
                    MatrixOps_B64.zeroTriangle(true,T);

                    if( upper ) {
                        T= MatrixOps_B64.transpose(T,null);
                    }

                    BlockMatrix64F B = MatrixOps_B64.createRandom(triangleSize,cols,-1,1,rand,r);
                    BlockMatrix64F Y = new BlockMatrix64F(B.numRows,B.numCols,r);

                    checkSolve(T,B,Y,r,upper,false);
                    checkSolve(T,B,Y,r,upper,true);

                    // test cases where the submatrix is not aligned with the inner
                    // blocks
                    checkSolveUnaligned(T,B,Y,r,upper,false);
                    checkSolveUnaligned(T,B,Y,r,upper,true);
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
            BlockMatrix64F T_tran = MatrixOps_B64.transpose(T,null);

            // Compute Y directly from the expected result B
            MatrixOps_B64.mult(T_tran,B,Y);
        } else {
            // Compute Y directly from the expected result B
            MatrixOps_B64.mult(T,B,Y);
        }

        // Y is overwritten with the solution
        TriangularSolver_B64.solve(r,upper,new D1Submatrix64F(T),new D1Submatrix64F(Y),transT);

        assertTrue( MatrixOps_B64.isEquals(B,Y, UtilEjml.TEST_64F_SQ));
    }

    /**
     * Checks to see if BlockTriangularSolver.solve produces the expected output given
     * these inputs.  The solution is computed directly.
     */
    private void checkSolveUnaligned( BlockMatrix64F T , BlockMatrix64F B , BlockMatrix64F Y ,
                                      int r , boolean upper , boolean transT )
    {
        BlockMatrix64F T2;

        if( upper )
            T2 = MatrixOps_B64.createRandom(T.numRows+1,T.numCols,-1,1,rand,T.blockLength);
        else
            T2 = MatrixOps_B64.createRandom(T.numRows,T.numCols+1,-1,1,rand,T.blockLength);

        CommonOps_D64.insert(T,T2,0,0);

        if( transT ) {
            BlockMatrix64F T_tran = MatrixOps_B64.transpose(T,null);

            // Compute Y directly from the expected result B
            MatrixOps_B64.mult(T_tran,B,Y);
        } else {
            // Compute Y directly from the expected result B
            MatrixOps_B64.mult(T,B,Y);
        }

        int size = T.numRows;

        // Y is overwritten with the solution
        TriangularSolver_B64.solve(r,upper,new D1Submatrix64F(T2,0,size,0,size),new D1Submatrix64F(Y),transT);

        assertTrue( "Failed upper = "+upper+" transT = "+transT+" T.length "+T.numRows+" B.cols "+B.numCols,
                MatrixOps_B64.isEquals(B,Y, UtilEjml.TEST_64F_SQ));
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
        DenseMatrix64F B = RandomMatrices_D64.createRandom(3,5,rand);
        DenseMatrix64F X = new DenseMatrix64F(3,5);

        if( !solveL ) {
            CommonOps_D64.transpose(L);
        }

        if( transT ) {
           CommonOps_D64.transpose(L);
        }

        CommonOps_D64.solve(L,B,X);

        // do it again using block matrices
        BlockMatrix64F b_L = MatrixOps_B64.convert(L,3);
        BlockMatrix64F b_B = MatrixOps_B64.convert(B,3);

        D1Submatrix64F sub_L = new D1Submatrix64F(b_L,0, 3, 0, 3);
        D1Submatrix64F sub_B = new D1Submatrix64F(b_B,0, 3, 0, 5);

        if( transT ) {
            sub_L.original = MatrixOps_B64.transpose((BlockMatrix64F)sub_L.original,null);
            TestMatrixMult_B64.transposeSub(sub_L);
        }

        if( transB ) {
            sub_B.original = b_B = MatrixOps_B64.transpose((BlockMatrix64F)sub_B.original,null);
            TestMatrixMult_B64.transposeSub(sub_B);
            CommonOps_D64.transpose(X);
        }

//        sub_L.original.print();
//        sub_B.original.print();

        TriangularSolver_B64.solveBlock(3,!solveL,sub_L,sub_B,transT,transB);

        assertTrue(GenericMatrixOps_F64.isEquivalent(X,b_B,UtilEjml.TEST_64F));
    }

    private DenseMatrix64F createRandomLowerTriangular( int N ) {
        DenseMatrix64F U = RandomMatrices_D64.createUpperTriangle(N,0,-1,1,rand);

        CommonOps_D64.transpose(U);

        return U;
    }
}
