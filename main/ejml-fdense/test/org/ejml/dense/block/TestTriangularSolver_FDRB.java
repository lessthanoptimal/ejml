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
import org.ejml.data.FMatrixRBlock;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FSubmatrixD1;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.generic.GenericMatrixOps_F32;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestTriangularSolver_FDRB {

    Random rand = new Random(234534);

    @Test
    public void invert_two() {
        // block size
        int r = 3;

        float temp[] = new float[r*r];

        for( int size = 1; size <= 9; size++ ) {
            FMatrixRBlock T = MatrixOps_FDRB.createRandom(size,size,-1,1,rand,r);
            MatrixOps_FDRB.zeroTriangle(true,T);

            FMatrixRBlock T_inv = T.copy();

            TriangularSolver_FDRB.invert(r,false,new FSubmatrixD1(T),new FSubmatrixD1(T_inv),temp);

            FMatrixRBlock C = new FMatrixRBlock(size,size,r);

            MatrixOps_FDRB.mult(T,T_inv,C);

            assertTrue(GenericMatrixOps_F32.isIdentity(C,UtilEjml.TEST_F32));

            // see if passing in the same matrix instance twice messes it up or not
            TriangularSolver_FDRB.invert(r,false,new FSubmatrixD1(T),new FSubmatrixD1(T),temp);

            assertTrue(MatrixOps_FDRB.isEquals(T,T_inv, UtilEjml.TEST_F32));
        }
    }

    @Test
    public void invert_one() {
        // block size
        int r = 3;

        float temp[] = new float[r*r];

        for( int size = 1; size <= 9; size++ ) {
            FMatrixRBlock T = MatrixOps_FDRB.createRandom(size,size,-1,1,rand,r);
            MatrixOps_FDRB.zeroTriangle(true,T);

            FMatrixRBlock T_inv = T.copy();

            TriangularSolver_FDRB.invert(r,false,new FSubmatrixD1(T_inv),temp);

            FMatrixRBlock C = new FMatrixRBlock(size,size,r);

            MatrixOps_FDRB.mult(T,T_inv,C);

            assertTrue(GenericMatrixOps_F32.isIdentity(C,UtilEjml.TEST_F32));
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
                    FMatrixRBlock T = MatrixOps_FDRB.createRandom(triangleSize,triangleSize,-1,1,rand,r);
                    MatrixOps_FDRB.zeroTriangle(true,T);

                    if( upper ) {
                        T= MatrixOps_FDRB.transpose(T,null);
                    }

                    FMatrixRBlock B = MatrixOps_FDRB.createRandom(triangleSize,cols,-1,1,rand,r);
                    FMatrixRBlock Y = new FMatrixRBlock(B.numRows,B.numCols,r);

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
    private void checkSolve(FMatrixRBlock T , FMatrixRBlock B , FMatrixRBlock Y ,
                            int r , boolean upper , boolean transT )
    {
        if( transT ) {
            FMatrixRBlock T_tran = MatrixOps_FDRB.transpose(T,null);

            // Compute Y directly from the expected result B
            MatrixOps_FDRB.mult(T_tran,B,Y);
        } else {
            // Compute Y directly from the expected result B
            MatrixOps_FDRB.mult(T,B,Y);
        }

        // Y is overwritten with the solution
        TriangularSolver_FDRB.solve(r,upper,new FSubmatrixD1(T),new FSubmatrixD1(Y),transT);

        assertTrue( MatrixOps_FDRB.isEquals(B,Y, UtilEjml.TEST_F32_SQ));
    }

    /**
     * Checks to see if BlockTriangularSolver.solve produces the expected output given
     * these inputs.  The solution is computed directly.
     */
    private void checkSolveUnaligned(FMatrixRBlock T , FMatrixRBlock B , FMatrixRBlock Y ,
                                     int r , boolean upper , boolean transT )
    {
        FMatrixRBlock T2;

        if( upper )
            T2 = MatrixOps_FDRB.createRandom(T.numRows+1,T.numCols,-1,1,rand,T.blockLength);
        else
            T2 = MatrixOps_FDRB.createRandom(T.numRows,T.numCols+1,-1,1,rand,T.blockLength);

        CommonOps_FDRM.insert(T,T2,0,0);

        if( transT ) {
            FMatrixRBlock T_tran = MatrixOps_FDRB.transpose(T,null);

            // Compute Y directly from the expected result B
            MatrixOps_FDRB.mult(T_tran,B,Y);
        } else {
            // Compute Y directly from the expected result B
            MatrixOps_FDRB.mult(T,B,Y);
        }

        int size = T.numRows;

        // Y is overwritten with the solution
        TriangularSolver_FDRB.solve(r,upper,new FSubmatrixD1(T2,0,size,0,size),new FSubmatrixD1(Y),transT);

        assertTrue(MatrixOps_FDRB.isEquals(B,Y, UtilEjml.TEST_F32_SQ),
                "Failed upper = "+upper+" transT = "+transT+" T.length "+T.numRows+" B.cols "+B.numCols);
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
        FMatrixRMaj L = createRandomLowerTriangular(3);
        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(3,5,rand);
        FMatrixRMaj X = new FMatrixRMaj(3,5);

        if( !solveL ) {
            CommonOps_FDRM.transpose(L);
        }

        if( transT ) {
           CommonOps_FDRM.transpose(L);
        }

        CommonOps_FDRM.solve(L,B,X);

        // do it again using block matrices
        FMatrixRBlock b_L = MatrixOps_FDRB.convert(L,3);
        FMatrixRBlock b_B = MatrixOps_FDRB.convert(B,3);

        FSubmatrixD1 sub_L = new FSubmatrixD1(b_L,0, 3, 0, 3);
        FSubmatrixD1 sub_B = new FSubmatrixD1(b_B,0, 3, 0, 5);

        if( transT ) {
            sub_L.original = MatrixOps_FDRB.transpose((FMatrixRBlock)sub_L.original,null);
            TestMatrixMult_FDRB.transposeSub(sub_L);
        }

        if( transB ) {
            sub_B.original = b_B = MatrixOps_FDRB.transpose((FMatrixRBlock)sub_B.original,null);
            TestMatrixMult_FDRB.transposeSub(sub_B);
            CommonOps_FDRM.transpose(X);
        }

//        sub_L.original.print();
//        sub_B.original.print();

        TriangularSolver_FDRB.solveBlock(3,!solveL,sub_L,sub_B,transT,transB);

        assertTrue(GenericMatrixOps_F32.isEquivalent(X,b_B,UtilEjml.TEST_F32));
    }

    private FMatrixRMaj createRandomLowerTriangular(int N ) {
        FMatrixRMaj U = RandomMatrices_FDRM.triangularUpper(N,0,-1,1,rand);

        CommonOps_FDRM.transpose(U);

        return U;
    }
}
