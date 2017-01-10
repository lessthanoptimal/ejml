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

package org.ejml.alg.block.linsol.qr;

import org.ejml.UtilEjml;
import org.ejml.alg.block.MatrixOps_B64;
import org.ejml.alg.generic.GenericMatrixOps_F64;
import org.ejml.data.DMatrixBlock_F64;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.MatrixFeatures_R64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestQrHouseHolderSolver_B64 {

    Random rand = new Random(23423);

    /**
     * Test positive examples against a variety of different inputs shapes.
     */
    @Test
    public void testPositiveSolve() {
        int r = 3;
        QrHouseHolderSolver_B64 solver = new QrHouseHolderSolver_B64();

        for( int i = 1; i <= r*3; i++ ) {
            for( int j = i; j <= r*3; j++ ) {
                for( int k = 1; k <= r*3; k++ ) {
//                    System.out.println("i = "+i+" j = "+j+" k = "+k);
                    DMatrixBlock_F64 A = MatrixOps_B64.createRandom(j,i,-1,1,rand,r);
                    DMatrixBlock_F64 X = MatrixOps_B64.createRandom(i,k,-1,1,rand,r);
                    DMatrixBlock_F64 Y = new DMatrixBlock_F64(j,k,r);
                    DMatrixBlock_F64 X_found = new DMatrixBlock_F64(i,k,r);

                    // compute the expected solution directly
                    MatrixOps_B64.mult(A,X,Y);

                    assertTrue(solver.setA(A.copy()));

                    solver.solve(Y,X_found);

                    assertTrue(MatrixOps_B64.isEquals(X,X_found, UtilEjml.TEST_F64));
                }
            }
        }
    }

    @Test
    public void testInvert() {
        int r = 3;
        QrHouseHolderSolver_B64 solver = new QrHouseHolderSolver_B64();

        for( int i = 1; i <= r*3; i++ ) {
            DMatrixBlock_F64 A = MatrixOps_B64.createRandom(i,i,-1,1,rand,r);

            DMatrixBlock_F64 A_orig = A.copy();
            DMatrixBlock_F64 I = new DMatrixBlock_F64(i,i,r);

            assertTrue(solver.setA(A.copy()));

            solver.invert(A);

            // A times its inverse is an identity matrix
            MatrixOps_B64.mult(A,A_orig,I);

            assertTrue(GenericMatrixOps_F64.isIdentity(I,UtilEjml.TEST_F64));
        }
    }

    @Test
    public void testQuality() {
        DMatrixBlock_F64 A = MatrixOps_B64.convert(CommonOps_R64.diag(4,3,2,1),3);
        DMatrixBlock_F64 B = MatrixOps_B64.convert(CommonOps_R64.diag(4,3,2,0.1),3);

        // see if a matrix with smaller singular value has a worse quality
        QrHouseHolderSolver_B64 solver = new QrHouseHolderSolver_B64();
        assertTrue(solver.setA(A.copy()));
        double qualityA = (double)solver.quality();

        assertTrue(solver.setA(B.copy()));
        double qualityB = (double)solver.quality();

        assertTrue(qualityB<qualityA);
        assertEquals(qualityB*10.0,qualityA,UtilEjml.TEST_F64);
    }

    /**
     * Checks to see if quality is scale invariant.
     */
    @Test
    public void testQuality_scale() {
        DMatrixBlock_F64 A = MatrixOps_B64.convert(CommonOps_R64.diag(4,3,2,1),3);
        DMatrixBlock_F64 B = A.copy();
        CommonOps_R64.scale(2,B);

        // see if a matrix with smaller singular value has a worse quality
        QrHouseHolderSolver_B64 solver = new QrHouseHolderSolver_B64();
        assertTrue(solver.setA(A.copy()));
        double qualityA = (double)solver.quality();

        assertTrue(solver.setA(B.copy()));
        double qualityB = (double)solver.quality();

        assertEquals(qualityA,qualityB,UtilEjml.TEST_F64);
    }

    @Test
    public void modifiesA(){
        DMatrixBlock_F64 A = MatrixOps_B64.createRandom(4,4,-1,1,rand,3);
        DMatrixBlock_F64 A_orig = A.copy();

        QrHouseHolderSolver_B64 solver = new QrHouseHolderSolver_B64();

        assertTrue(solver.setA(A));

        boolean modified = !MatrixFeatures_R64.isEquals(A,A_orig);

        assertTrue(modified == solver.modifiesA());
    }

    @Test
    public void modifiesB(){
        DMatrixBlock_F64 A = MatrixOps_B64.createRandom(4,4,-1,1,rand,3);

        QrHouseHolderSolver_B64 solver = new QrHouseHolderSolver_B64();

        assertTrue(solver.setA(A));

        DMatrixBlock_F64 B = MatrixOps_B64.createRandom(4,2,-1,1,rand,3);
        DMatrixBlock_F64 B_orig = B.copy();
        DMatrixBlock_F64 X = new DMatrixBlock_F64(A.numRows,B.numCols,3);

        solver.solve(B,X);

        boolean modified = !MatrixFeatures_R64.isEquals(B_orig,B);

        assertTrue(modified == solver.modifiesB());
    }

}
