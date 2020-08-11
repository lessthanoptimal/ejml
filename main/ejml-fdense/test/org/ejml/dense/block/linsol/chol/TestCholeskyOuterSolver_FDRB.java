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

package org.ejml.dense.block.linsol.chol;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRBlock;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.block.MatrixOps_FDRB;
import org.ejml.dense.block.linsol.qr.QrHouseHolderSolver_FDRB;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.generic.GenericMatrixOps_F32;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public class TestCholeskyOuterSolver_FDRB {
    protected Random rand = new Random(234234);

    protected int r = 3;

    /**
     * Test positive examples against a variety of different inputs shapes.
     */
    @Test
    public void testPositiveSolve() {
        CholeskyOuterSolver_FDRB solver = new CholeskyOuterSolver_FDRB();

        for( int i = 1; i <= r*3; i++ ) {
            for( int j = 1; j <= r*3; j++ ) {
                FMatrixRBlock A = createMatrixSPD(i);
                FMatrixRBlock X = MatrixOps_FDRB.createRandom(i,j,-1,1,rand,r);
                FMatrixRBlock Y = new FMatrixRBlock(i,j,r);
                FMatrixRBlock X_found = new FMatrixRBlock(i,j,r);

                // compute the expected solution directly
                MatrixOps_FDRB.mult(A,X,Y);

                assertTrue(solver.setA(A.copy()));

                solver.solve(Y,X_found);

                assertTrue(MatrixOps_FDRB.isEquals(X,X_found,UtilEjml.TEST_F32));
            }
        }
    }

    /**
     * Give it a matrix which is not SPD and see if it fails
     */
    @Test
    public void testNegativeSolve() {
        CholeskyOuterSolver_FDRB solver = new CholeskyOuterSolver_FDRB();

        FMatrixRBlock X = MatrixOps_FDRB.createRandom(7,7,-1,1,rand,r);

        assertFalse(solver.setA(X));
    }

    @Test
    public void testInvert() {
        CholeskyOuterSolver_FDRB solver = new CholeskyOuterSolver_FDRB();

        for( int i = 1; i <= r*3; i++ ) {
            FMatrixRBlock A = createMatrixSPD(i);
            FMatrixRBlock A_inv = MatrixOps_FDRB.createRandom(i,i,-1,1,rand,r);

            assertTrue(solver.setA(A.copy()));

            solver.invert(A_inv);

            FMatrixRBlock B = new FMatrixRBlock(i,i,r);

            MatrixOps_FDRB.mult(A,A_inv,B);

            assertTrue(GenericMatrixOps_F32.isIdentity(B, UtilEjml.TEST_F32));
        }
    }

    @Test
    public void testQuality() {
        CholeskyOuterSolver_FDRB solver = new CholeskyOuterSolver_FDRB();

        FMatrixRMaj A = CommonOps_FDRM.diag(5,3,2,1);
        FMatrixRMaj B = CommonOps_FDRM.diag(5,3,2,0.001f);

        assertTrue(solver.setA(MatrixOps_FDRB.convert(A,r)));
        float qualityA = (float)solver.quality();

        assertTrue(solver.setA(MatrixOps_FDRB.convert(B,r)));
        float qualityB = (float)solver.quality();

        assertTrue(qualityB < qualityA);
        assertTrue(qualityB*10.0f < qualityA);
    }

    @Test
    public void testQuality_scale() {
        CholeskyOuterSolver_FDRB solver = new CholeskyOuterSolver_FDRB();

        FMatrixRMaj A = CommonOps_FDRM.diag(5,3,2,1);
        FMatrixRMaj B = A.copy();
        CommonOps_FDRM.scale(0.001f,B);

        assertTrue(solver.setA(MatrixOps_FDRB.convert(A,r)));
        float qualityA = (float)solver.quality();

        assertTrue(solver.setA(MatrixOps_FDRB.convert(B,r)));
        float qualityB = (float)solver.quality();

        assertEquals(qualityB,qualityA,UtilEjml.TEST_F32);
    }

    @Test
    public void testPositiveSolveNull() {
        CholeskyOuterSolver_FDRB solver = new CholeskyOuterSolver_FDRB();

        for( int i = 1; i <= r*3; i++ ) {
            for( int j = 1; j <= r*3; j++ ) {
                FMatrixRBlock A = createMatrixSPD(i);
                FMatrixRBlock X = MatrixOps_FDRB.createRandom(i,j,-1,1,rand,r);
                FMatrixRBlock Y = new FMatrixRBlock(i,j,r);
                FMatrixRBlock X_found = new FMatrixRBlock(i,j,r);

                // compute the expected solution directly
                MatrixOps_FDRB.mult(A,X,Y);

                assertTrue(solver.setA(A.copy()));

                solver.solve(Y,null);

                assertTrue(MatrixOps_FDRB.isEquals(X,Y,UtilEjml.TEST_F32));
            }
        }
    }

    @Test
    public void modifiesA(){
        FMatrixRBlock A = createMatrixSPD(4);
        FMatrixRBlock A_orig = A.copy();

        QrHouseHolderSolver_FDRB solver = new QrHouseHolderSolver_FDRB();

        assertTrue(solver.setA(A));

        boolean modified = !MatrixFeatures_FDRM.isEquals(A,A_orig);

        assertTrue(modified == solver.modifiesA());
    }

    @Test
    public void modifiesB(){
        FMatrixRBlock A = createMatrixSPD(4);

        QrHouseHolderSolver_FDRB solver = new QrHouseHolderSolver_FDRB();

        assertTrue(solver.setA(A));

        FMatrixRBlock B = MatrixOps_FDRB.createRandom(4,2,-1,1,rand,3);
        FMatrixRBlock B_orig = B.copy();
        FMatrixRBlock X = new FMatrixRBlock(A.numRows,B.numCols,3);

        solver.solve(B,X);

        boolean modified = !MatrixFeatures_FDRM.isEquals(B_orig,B);

        assertTrue(modified == solver.modifiesB());
    }

    protected FMatrixRBlock createMatrixSPD(int width ) {
        FMatrixRMaj A = RandomMatrices_FDRM.symmetricPosDef(width,rand);

        return MatrixOps_FDRB.convert(A,r);
    }
}
