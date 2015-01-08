/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.block.linsol.chol;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.block.linsol.qr.BlockQrHouseHolderSolver;
import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestBlockCholeskyOuterSolver {
    protected Random rand = new Random(234234);

    protected int r = 3;

    /**
     * Test positive examples against a variety of different inputs shapes.
     */
    @Test
    public void testPositiveSolve() {
        BlockCholeskyOuterSolver solver = new BlockCholeskyOuterSolver();

        for( int i = 1; i <= r*3; i++ ) {
            for( int j = 1; j <= r*3; j++ ) {
                BlockMatrix64F A = createMatrixSPD(i);
                BlockMatrix64F X = BlockMatrixOps.createRandom(i,j,-1,1,rand,r);
                BlockMatrix64F Y = new BlockMatrix64F(i,j,r);
                BlockMatrix64F X_found = new BlockMatrix64F(i,j,r);

                // compute the expected solution directly
                BlockMatrixOps.mult(A,X,Y);

                assertTrue(solver.setA(A.copy()));

                solver.solve(Y,X_found);

                assertTrue(BlockMatrixOps.isEquals(X,X_found,1e-8));
            }
        }
    }

    /**
     * Give it a matrix which is not SPD and see if it fails
     */
    @Test
    public void testNegativeSolve() {
        BlockCholeskyOuterSolver solver = new BlockCholeskyOuterSolver();

        BlockMatrix64F X = BlockMatrixOps.createRandom(7,7,-1,1,rand,r);

        assertFalse(solver.setA(X));
    }

    @Test
    public void testInvert() {
        BlockCholeskyOuterSolver solver = new BlockCholeskyOuterSolver();

        for( int i = 1; i <= r*3; i++ ) {
            BlockMatrix64F A = createMatrixSPD(i);
            BlockMatrix64F A_inv = BlockMatrixOps.createRandom(i,i,-1,1,rand,r);

            assertTrue(solver.setA(A.copy()));

            solver.invert(A_inv);

            BlockMatrix64F B = new BlockMatrix64F(i,i,r);

            BlockMatrixOps.mult(A,A_inv,B);

            assertTrue(GenericMatrixOps.isIdentity(B,1e-8));
        }
    }

    @Test
    public void testQuality() {
        BlockCholeskyOuterSolver solver = new BlockCholeskyOuterSolver();

        DenseMatrix64F A = CommonOps.diag(5,3,2,1);
        DenseMatrix64F B = CommonOps.diag(5,3,2,0.001);

        assertTrue(solver.setA(BlockMatrixOps.convert(A,r)));
        double qualityA = solver.quality();

        assertTrue(solver.setA(BlockMatrixOps.convert(B,r)));
        double qualityB = solver.quality();

        assertTrue(qualityB < qualityA);
        assertTrue(qualityB*10.0 < qualityA);
    }

    @Test
    public void testQuality_scale() {
        BlockCholeskyOuterSolver solver = new BlockCholeskyOuterSolver();

        DenseMatrix64F A = CommonOps.diag(5,3,2,1);
        DenseMatrix64F B = A.copy();
        CommonOps.scale(0.001,B);

        assertTrue(solver.setA(BlockMatrixOps.convert(A,r)));
        double qualityA = solver.quality();

        assertTrue(solver.setA(BlockMatrixOps.convert(B,r)));
        double qualityB = solver.quality();

        assertEquals(qualityB,qualityA,1e-8);
    }

    @Test
    public void testPositiveSolveNull() {
        BlockCholeskyOuterSolver solver = new BlockCholeskyOuterSolver();

        for( int i = 1; i <= r*3; i++ ) {
            for( int j = 1; j <= r*3; j++ ) {
                BlockMatrix64F A = createMatrixSPD(i);
                BlockMatrix64F X = BlockMatrixOps.createRandom(i,j,-1,1,rand,r);
                BlockMatrix64F Y = new BlockMatrix64F(i,j,r);
                BlockMatrix64F X_found = new BlockMatrix64F(i,j,r);

                // compute the expected solution directly
                BlockMatrixOps.mult(A,X,Y);

                assertTrue(solver.setA(A.copy()));

                solver.solve(Y,null);

                assertTrue(BlockMatrixOps.isEquals(X,Y,1e-8));
            }
        }
    }

    @Test
    public void modifiesA(){
        BlockMatrix64F A = createMatrixSPD(4);
        BlockMatrix64F A_orig = A.copy();

        BlockQrHouseHolderSolver solver = new BlockQrHouseHolderSolver();

        assertTrue(solver.setA(A));

        boolean modified = !MatrixFeatures.isEquals(A,A_orig);

        assertTrue(modified == solver.modifiesA());
    }

    @Test
    public void modifiesB(){
        BlockMatrix64F A = createMatrixSPD(4);

        BlockQrHouseHolderSolver solver = new BlockQrHouseHolderSolver();

        assertTrue(solver.setA(A));

        BlockMatrix64F B = BlockMatrixOps.createRandom(4,2,-1,1,rand,3);
        BlockMatrix64F B_orig = B.copy();
        BlockMatrix64F X = new BlockMatrix64F(A.numRows,B.numCols,3);

        solver.solve(B,X);

        boolean modified = !MatrixFeatures.isEquals(B_orig,B);

        assertTrue(modified == solver.modifiesB());
    }

    protected BlockMatrix64F createMatrixSPD( int width ) {
        DenseMatrix64F A = RandomMatrices.createSymmPosDef(width,rand);

        return BlockMatrixOps.convert(A,r);
    }
}
