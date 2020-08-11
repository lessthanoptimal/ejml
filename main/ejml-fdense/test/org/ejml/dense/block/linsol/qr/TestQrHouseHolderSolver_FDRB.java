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

package org.ejml.dense.block.linsol.qr;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRBlock;
import org.ejml.dense.block.MatrixOps_FDRB;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.generic.GenericMatrixOps_F32;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestQrHouseHolderSolver_FDRB {

    Random rand = new Random(23423);

    /**
     * Test positive examples against a variety of different inputs shapes.
     */
    @Test
    public void testPositiveSolve() {
        int r = 3;
        QrHouseHolderSolver_FDRB solver = new QrHouseHolderSolver_FDRB();

        for( int i = 1; i <= r*3; i++ ) {
            for( int j = i; j <= r*3; j++ ) {
                for( int k = 1; k <= r*3; k++ ) {
//                    System.out.println("i = "+i+" j = "+j+" k = "+k);
                    FMatrixRBlock A = MatrixOps_FDRB.createRandom(j,i,-1,1,rand,r);
                    FMatrixRBlock X = MatrixOps_FDRB.createRandom(i,k,-1,1,rand,r);
                    FMatrixRBlock Y = new FMatrixRBlock(j,k,r);
                    FMatrixRBlock X_found = new FMatrixRBlock(i,k,r);

                    // compute the expected solution directly
                    MatrixOps_FDRB.mult(A,X,Y);

                    assertTrue(solver.setA(A.copy()));

                    solver.solve(Y,X_found);

                    assertTrue(MatrixOps_FDRB.isEquals(X,X_found, UtilEjml.TEST_F32));
                }
            }
        }
    }

    @Test
    public void testInvert() {
        int r = 3;
        QrHouseHolderSolver_FDRB solver = new QrHouseHolderSolver_FDRB();

        for( int i = 1; i <= r*3; i++ ) {
            FMatrixRBlock A = MatrixOps_FDRB.createRandom(i,i,-1,1,rand,r);

            FMatrixRBlock A_orig = A.copy();
            FMatrixRBlock I = new FMatrixRBlock(i,i,r);

            assertTrue(solver.setA(A.copy()));

            solver.invert(A);

            // A times its inverse is an identity matrix
            MatrixOps_FDRB.mult(A,A_orig,I);

            assertTrue(GenericMatrixOps_F32.isIdentity(I,UtilEjml.TEST_F32));
        }
    }

    @Test
    public void testQuality() {
        FMatrixRBlock A = MatrixOps_FDRB.convert(CommonOps_FDRM.diag(4,3,2,1),3);
        FMatrixRBlock B = MatrixOps_FDRB.convert(CommonOps_FDRM.diag(4,3,2,0.1f),3);

        // see if a matrix with smaller singular value has a worse quality
        QrHouseHolderSolver_FDRB solver = new QrHouseHolderSolver_FDRB();
        assertTrue(solver.setA(A.copy()));
        float qualityA = (float)solver.quality();

        assertTrue(solver.setA(B.copy()));
        float qualityB = (float)solver.quality();

        assertTrue(qualityB<qualityA);
        assertEquals(qualityB*10.0f,qualityA,UtilEjml.TEST_F32);
    }

    /**
     * Checks to see if quality is scale invariant.
     */
    @Test
    public void testQuality_scale() {
        FMatrixRBlock A = MatrixOps_FDRB.convert(CommonOps_FDRM.diag(4,3,2,1),3);
        FMatrixRBlock B = A.copy();
        CommonOps_FDRM.scale(2,B);

        // see if a matrix with smaller singular value has a worse quality
        QrHouseHolderSolver_FDRB solver = new QrHouseHolderSolver_FDRB();
        assertTrue(solver.setA(A.copy()));
        float qualityA = (float)solver.quality();

        assertTrue(solver.setA(B.copy()));
        float qualityB = (float)solver.quality();

        assertEquals(qualityA,qualityB,UtilEjml.TEST_F32);
    }

    @Test
    public void modifiesA(){
        FMatrixRBlock A = MatrixOps_FDRB.createRandom(4,4,-1,1,rand,3);
        FMatrixRBlock A_orig = A.copy();

        QrHouseHolderSolver_FDRB solver = new QrHouseHolderSolver_FDRB();

        assertTrue(solver.setA(A));

        boolean modified = !MatrixFeatures_FDRM.isEquals(A,A_orig);

        assertTrue(modified == solver.modifiesA());
    }

    @Test
    public void modifiesB(){
        FMatrixRBlock A = MatrixOps_FDRB.createRandom(4,4,-1,1,rand,3);

        QrHouseHolderSolver_FDRB solver = new QrHouseHolderSolver_FDRB();

        assertTrue(solver.setA(A));

        FMatrixRBlock B = MatrixOps_FDRB.createRandom(4,2,-1,1,rand,3);
        FMatrixRBlock B_orig = B.copy();
        FMatrixRBlock X = new FMatrixRBlock(A.numRows,B.numCols,3);

        solver.solve(B,X);

        boolean modified = !MatrixFeatures_FDRM.isEquals(B_orig,B);

        assertTrue(modified == solver.modifiesB());
    }

}
