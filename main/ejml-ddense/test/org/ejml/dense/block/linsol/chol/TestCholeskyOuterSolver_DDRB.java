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

package org.ejml.dense.block.linsol.chol;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.block.linsol.qr.QrHouseHolderSolver_DDRB;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.generic.GenericMatrixOps_F64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestCholeskyOuterSolver_DDRB {
    protected Random rand = new Random(234234);

    protected int r = 3;

    /**
     * Test positive examples against a variety of different inputs shapes.
     */
    @Test
    public void testPositiveSolve() {
        CholeskyOuterSolver_DDRB solver = new CholeskyOuterSolver_DDRB();

        for( int i = 1; i <= r*3; i++ ) {
            for( int j = 1; j <= r*3; j++ ) {
                DMatrixRBlock A = createMatrixSPD(i);
                DMatrixRBlock X = MatrixOps_DDRB.createRandom(i,j,-1,1,rand,r);
                DMatrixRBlock Y = new DMatrixRBlock(i,j,r);
                DMatrixRBlock X_found = new DMatrixRBlock(i,j,r);

                // compute the expected solution directly
                MatrixOps_DDRB.mult(A,X,Y);

                assertTrue(solver.setA(A.copy()));

                solver.solve(Y,X_found);

                assertTrue(MatrixOps_DDRB.isEquals(X,X_found,UtilEjml.TEST_F64));
            }
        }
    }

    /**
     * Give it a matrix which is not SPD and see if it fails
     */
    @Test
    public void testNegativeSolve() {
        CholeskyOuterSolver_DDRB solver = new CholeskyOuterSolver_DDRB();

        DMatrixRBlock X = MatrixOps_DDRB.createRandom(7,7,-1,1,rand,r);

        assertFalse(solver.setA(X));
    }

    @Test
    public void testInvert() {
        CholeskyOuterSolver_DDRB solver = new CholeskyOuterSolver_DDRB();

        for( int i = 1; i <= r*3; i++ ) {
            DMatrixRBlock A = createMatrixSPD(i);
            DMatrixRBlock A_inv = MatrixOps_DDRB.createRandom(i,i,-1,1,rand,r);

            assertTrue(solver.setA(A.copy()));

            solver.invert(A_inv);

            DMatrixRBlock B = new DMatrixRBlock(i,i,r);

            MatrixOps_DDRB.mult(A,A_inv,B);

            assertTrue(GenericMatrixOps_F64.isIdentity(B, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void testQuality() {
        CholeskyOuterSolver_DDRB solver = new CholeskyOuterSolver_DDRB();

        DMatrixRMaj A = CommonOps_DDRM.diag(5,3,2,1);
        DMatrixRMaj B = CommonOps_DDRM.diag(5,3,2,0.001);

        assertTrue(solver.setA(MatrixOps_DDRB.convert(A,r)));
        double qualityA = (double)solver.quality();

        assertTrue(solver.setA(MatrixOps_DDRB.convert(B,r)));
        double qualityB = (double)solver.quality();

        assertTrue(qualityB < qualityA);
        assertTrue(qualityB*10.0 < qualityA);
    }

    @Test
    public void testQuality_scale() {
        CholeskyOuterSolver_DDRB solver = new CholeskyOuterSolver_DDRB();

        DMatrixRMaj A = CommonOps_DDRM.diag(5,3,2,1);
        DMatrixRMaj B = A.copy();
        CommonOps_DDRM.scale(0.001,B);

        assertTrue(solver.setA(MatrixOps_DDRB.convert(A,r)));
        double qualityA = (double)solver.quality();

        assertTrue(solver.setA(MatrixOps_DDRB.convert(B,r)));
        double qualityB = (double)solver.quality();

        assertEquals(qualityB,qualityA,UtilEjml.TEST_F64);
    }

    @Test
    public void testPositiveSolveNull() {
        CholeskyOuterSolver_DDRB solver = new CholeskyOuterSolver_DDRB();

        for( int i = 1; i <= r*3; i++ ) {
            for( int j = 1; j <= r*3; j++ ) {
                DMatrixRBlock A = createMatrixSPD(i);
                DMatrixRBlock X = MatrixOps_DDRB.createRandom(i,j,-1,1,rand,r);
                DMatrixRBlock Y = new DMatrixRBlock(i,j,r);
                DMatrixRBlock X_found = new DMatrixRBlock(i,j,r);

                // compute the expected solution directly
                MatrixOps_DDRB.mult(A,X,Y);

                assertTrue(solver.setA(A.copy()));

                solver.solve(Y,null);

                assertTrue(MatrixOps_DDRB.isEquals(X,Y,UtilEjml.TEST_F64));
            }
        }
    }

    @Test
    public void modifiesA(){
        DMatrixRBlock A = createMatrixSPD(4);
        DMatrixRBlock A_orig = A.copy();

        QrHouseHolderSolver_DDRB solver = new QrHouseHolderSolver_DDRB();

        assertTrue(solver.setA(A));

        boolean modified = !MatrixFeatures_DDRM.isEquals(A,A_orig);

        assertTrue(modified == solver.modifiesA());
    }

    @Test
    public void modifiesB(){
        DMatrixRBlock A = createMatrixSPD(4);

        QrHouseHolderSolver_DDRB solver = new QrHouseHolderSolver_DDRB();

        assertTrue(solver.setA(A));

        DMatrixRBlock B = MatrixOps_DDRB.createRandom(4,2,-1,1,rand,3);
        DMatrixRBlock B_orig = B.copy();
        DMatrixRBlock X = new DMatrixRBlock(A.numRows,B.numCols,3);

        solver.solve(B,X);

        boolean modified = !MatrixFeatures_DDRM.isEquals(B_orig,B);

        assertTrue(modified == solver.modifiesB());
    }

    protected DMatrixRBlock createMatrixSPD(int width ) {
        DMatrixRMaj A = RandomMatrices_DDRM.createSymmPosDef(width,rand);

        return MatrixOps_DDRB.convert(A,r);
    }
}
