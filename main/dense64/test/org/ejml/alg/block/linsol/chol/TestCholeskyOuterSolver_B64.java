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

package org.ejml.alg.block.linsol.chol;

import org.ejml.UtilEjml;
import org.ejml.alg.block.MatrixOps_B64;
import org.ejml.alg.block.linsol.qr.QrHouseHolderSolver_B64;
import org.ejml.alg.generic.GenericMatrixOps_F64;
import org.ejml.data.DMatrixBlock_F64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.MatrixFeatures_R64;
import org.ejml.ops.RandomMatrices_R64;
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
                DMatrixBlock_F64 A = createMatrixSPD(i);
                DMatrixBlock_F64 X = MatrixOps_B64.createRandom(i,j,-1,1,rand,r);
                DMatrixBlock_F64 Y = new DMatrixBlock_F64(i,j,r);
                DMatrixBlock_F64 X_found = new DMatrixBlock_F64(i,j,r);

                // compute the expected solution directly
                MatrixOps_B64.mult(A,X,Y);

                assertTrue(solver.setA(A.copy()));

                solver.solve(Y,X_found);

                assertTrue(MatrixOps_B64.isEquals(X,X_found,UtilEjml.TEST_F64));
            }
        }
    }

    /**
     * Give it a matrix which is not SPD and see if it fails
     */
    @Test
    public void testNegativeSolve() {
        CholeskyOuterSolver_B64 solver = new CholeskyOuterSolver_B64();

        DMatrixBlock_F64 X = MatrixOps_B64.createRandom(7,7,-1,1,rand,r);

        assertFalse(solver.setA(X));
    }

    @Test
    public void testInvert() {
        CholeskyOuterSolver_B64 solver = new CholeskyOuterSolver_B64();

        for( int i = 1; i <= r*3; i++ ) {
            DMatrixBlock_F64 A = createMatrixSPD(i);
            DMatrixBlock_F64 A_inv = MatrixOps_B64.createRandom(i,i,-1,1,rand,r);

            assertTrue(solver.setA(A.copy()));

            solver.invert(A_inv);

            DMatrixBlock_F64 B = new DMatrixBlock_F64(i,i,r);

            MatrixOps_B64.mult(A,A_inv,B);

            assertTrue(GenericMatrixOps_F64.isIdentity(B, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void testQuality() {
        CholeskyOuterSolver_B64 solver = new CholeskyOuterSolver_B64();

        DMatrixRow_F64 A = CommonOps_R64.diag(5,3,2,1);
        DMatrixRow_F64 B = CommonOps_R64.diag(5,3,2,0.001);

        assertTrue(solver.setA(MatrixOps_B64.convert(A,r)));
        double qualityA = (double)solver.quality();

        assertTrue(solver.setA(MatrixOps_B64.convert(B,r)));
        double qualityB = (double)solver.quality();

        assertTrue(qualityB < qualityA);
        assertTrue(qualityB*10.0 < qualityA);
    }

    @Test
    public void testQuality_scale() {
        CholeskyOuterSolver_B64 solver = new CholeskyOuterSolver_B64();

        DMatrixRow_F64 A = CommonOps_R64.diag(5,3,2,1);
        DMatrixRow_F64 B = A.copy();
        CommonOps_R64.scale(0.001,B);

        assertTrue(solver.setA(MatrixOps_B64.convert(A,r)));
        double qualityA = (double)solver.quality();

        assertTrue(solver.setA(MatrixOps_B64.convert(B,r)));
        double qualityB = (double)solver.quality();

        assertEquals(qualityB,qualityA,UtilEjml.TEST_F64);
    }

    @Test
    public void testPositiveSolveNull() {
        CholeskyOuterSolver_B64 solver = new CholeskyOuterSolver_B64();

        for( int i = 1; i <= r*3; i++ ) {
            for( int j = 1; j <= r*3; j++ ) {
                DMatrixBlock_F64 A = createMatrixSPD(i);
                DMatrixBlock_F64 X = MatrixOps_B64.createRandom(i,j,-1,1,rand,r);
                DMatrixBlock_F64 Y = new DMatrixBlock_F64(i,j,r);
                DMatrixBlock_F64 X_found = new DMatrixBlock_F64(i,j,r);

                // compute the expected solution directly
                MatrixOps_B64.mult(A,X,Y);

                assertTrue(solver.setA(A.copy()));

                solver.solve(Y,null);

                assertTrue(MatrixOps_B64.isEquals(X,Y,UtilEjml.TEST_F64));
            }
        }
    }

    @Test
    public void modifiesA(){
        DMatrixBlock_F64 A = createMatrixSPD(4);
        DMatrixBlock_F64 A_orig = A.copy();

        QrHouseHolderSolver_B64 solver = new QrHouseHolderSolver_B64();

        assertTrue(solver.setA(A));

        boolean modified = !MatrixFeatures_R64.isEquals(A,A_orig);

        assertTrue(modified == solver.modifiesA());
    }

    @Test
    public void modifiesB(){
        DMatrixBlock_F64 A = createMatrixSPD(4);

        QrHouseHolderSolver_B64 solver = new QrHouseHolderSolver_B64();

        assertTrue(solver.setA(A));

        DMatrixBlock_F64 B = MatrixOps_B64.createRandom(4,2,-1,1,rand,3);
        DMatrixBlock_F64 B_orig = B.copy();
        DMatrixBlock_F64 X = new DMatrixBlock_F64(A.numRows,B.numCols,3);

        solver.solve(B,X);

        boolean modified = !MatrixFeatures_R64.isEquals(B_orig,B);

        assertTrue(modified == solver.modifiesB());
    }

    protected DMatrixBlock_F64 createMatrixSPD(int width ) {
        DMatrixRow_F64 A = RandomMatrices_R64.createSymmPosDef(width,rand);

        return MatrixOps_B64.convert(A,r);
    }
}
