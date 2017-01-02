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

package org.ejml.alg.block.linsol.chol;

import org.ejml.UtilEjml;
import org.ejml.alg.block.MatrixOps_B64;
import org.ejml.alg.block.linsol.qr.QrHouseHolderSolver_B64;
import org.ejml.alg.generic.GenericMatrixOps_F64;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps_D64;
import org.ejml.ops.MatrixFeatures_D64;
import org.ejml.ops.RandomMatrices_D64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestCholeskyOuterSolver_B64 {
    protected Random rand = new Random(234234);

    protected int r = 3;

    /**
     * Test positive examples against a variety of different inputs shapes.
     */
    @Test
    public void testPositiveSolve() {
        CholeskyOuterSolver_B64 solver = new CholeskyOuterSolver_B64();

        for( int i = 1; i <= r*3; i++ ) {
            for( int j = 1; j <= r*3; j++ ) {
                BlockMatrix64F A = createMatrixSPD(i);
                BlockMatrix64F X = MatrixOps_B64.createRandom(i,j,-1,1,rand,r);
                BlockMatrix64F Y = new BlockMatrix64F(i,j,r);
                BlockMatrix64F X_found = new BlockMatrix64F(i,j,r);

                // compute the expected solution directly
                MatrixOps_B64.mult(A,X,Y);

                assertTrue(solver.setA(A.copy()));

                solver.solve(Y,X_found);

                assertTrue(MatrixOps_B64.isEquals(X,X_found,UtilEjml.TEST_64F));
            }
        }
    }

    /**
     * Give it a matrix which is not SPD and see if it fails
     */
    @Test
    public void testNegativeSolve() {
        CholeskyOuterSolver_B64 solver = new CholeskyOuterSolver_B64();

        BlockMatrix64F X = MatrixOps_B64.createRandom(7,7,-1,1,rand,r);

        assertFalse(solver.setA(X));
    }

    @Test
    public void testInvert() {
        CholeskyOuterSolver_B64 solver = new CholeskyOuterSolver_B64();

        for( int i = 1; i <= r*3; i++ ) {
            BlockMatrix64F A = createMatrixSPD(i);
            BlockMatrix64F A_inv = MatrixOps_B64.createRandom(i,i,-1,1,rand,r);

            assertTrue(solver.setA(A.copy()));

            solver.invert(A_inv);

            BlockMatrix64F B = new BlockMatrix64F(i,i,r);

            MatrixOps_B64.mult(A,A_inv,B);

            assertTrue(GenericMatrixOps_F64.isIdentity(B, UtilEjml.TEST_64F));
        }
    }

    @Test
    public void testQuality() {
        CholeskyOuterSolver_B64 solver = new CholeskyOuterSolver_B64();

        DenseMatrix64F A = CommonOps_D64.diag(5,3,2,1);
        DenseMatrix64F B = CommonOps_D64.diag(5,3,2,0.001);

        assertTrue(solver.setA(MatrixOps_B64.convert(A,r)));
        double qualityA = solver.quality();

        assertTrue(solver.setA(MatrixOps_B64.convert(B,r)));
        double qualityB = solver.quality();

        assertTrue(qualityB < qualityA);
        assertTrue(qualityB*10.0 < qualityA);
    }

    @Test
    public void testQuality_scale() {
        CholeskyOuterSolver_B64 solver = new CholeskyOuterSolver_B64();

        DenseMatrix64F A = CommonOps_D64.diag(5,3,2,1);
        DenseMatrix64F B = A.copy();
        CommonOps_D64.scale(0.001,B);

        assertTrue(solver.setA(MatrixOps_B64.convert(A,r)));
        double qualityA = solver.quality();

        assertTrue(solver.setA(MatrixOps_B64.convert(B,r)));
        double qualityB = solver.quality();

        assertEquals(qualityB,qualityA,UtilEjml.TEST_64F);
    }

    @Test
    public void testPositiveSolveNull() {
        CholeskyOuterSolver_B64 solver = new CholeskyOuterSolver_B64();

        for( int i = 1; i <= r*3; i++ ) {
            for( int j = 1; j <= r*3; j++ ) {
                BlockMatrix64F A = createMatrixSPD(i);
                BlockMatrix64F X = MatrixOps_B64.createRandom(i,j,-1,1,rand,r);
                BlockMatrix64F Y = new BlockMatrix64F(i,j,r);
                BlockMatrix64F X_found = new BlockMatrix64F(i,j,r);

                // compute the expected solution directly
                MatrixOps_B64.mult(A,X,Y);

                assertTrue(solver.setA(A.copy()));

                solver.solve(Y,null);

                assertTrue(MatrixOps_B64.isEquals(X,Y,UtilEjml.TEST_64F));
            }
        }
    }

    @Test
    public void modifiesA(){
        BlockMatrix64F A = createMatrixSPD(4);
        BlockMatrix64F A_orig = A.copy();

        QrHouseHolderSolver_B64 solver = new QrHouseHolderSolver_B64();

        assertTrue(solver.setA(A));

        boolean modified = !MatrixFeatures_D64.isEquals(A,A_orig);

        assertTrue(modified == solver.modifiesA());
    }

    @Test
    public void modifiesB(){
        BlockMatrix64F A = createMatrixSPD(4);

        QrHouseHolderSolver_B64 solver = new QrHouseHolderSolver_B64();

        assertTrue(solver.setA(A));

        BlockMatrix64F B = MatrixOps_B64.createRandom(4,2,-1,1,rand,3);
        BlockMatrix64F B_orig = B.copy();
        BlockMatrix64F X = new BlockMatrix64F(A.numRows,B.numCols,3);

        solver.solve(B,X);

        boolean modified = !MatrixFeatures_D64.isEquals(B_orig,B);

        assertTrue(modified == solver.modifiesB());
    }

    protected BlockMatrix64F createMatrixSPD( int width ) {
        DenseMatrix64F A = RandomMatrices_D64.createSymmPosDef(width,rand);

        return MatrixOps_B64.convert(A,r);
    }
}
