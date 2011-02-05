/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.block.linsol.qr;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.BlockMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestBlockQrHouseHolderSolver {

    Random rand = new Random(23423);

    /**
     * Test positive examples against a variety of different inputs shapes.
     */
    @Test
    public void testPositiveSolve() {
        int r = 3;
        BlockQrHouseHolderSolver solver = new BlockQrHouseHolderSolver();

        for( int i = 1; i <= r*3; i++ ) {
            for( int j = i; j <= r*3; j++ ) {
                for( int k = 1; k <= r*3; k++ ) {
//                    System.out.println("i = "+i+" j = "+j+" k = "+k);
                    BlockMatrix64F A = BlockMatrixOps.createRandom(j,i,-1,1,rand,r);
                    BlockMatrix64F X = BlockMatrixOps.createRandom(i,k,-1,1,rand,r);
                    BlockMatrix64F Y = new BlockMatrix64F(j,k,r);
                    BlockMatrix64F X_found = new BlockMatrix64F(i,k,r);

                    // compute the expected solution directly
                    BlockMatrixOps.mult(A,X,Y);

                    assertTrue(solver.setA(A.copy()));

                    solver.solve(Y,X_found);

                    assertTrue(BlockMatrixOps.isEquals(X,X_found,1e-8));
                }
            }
        }
    }

    @Test
    public void testInvert() {
        int r = 3;
        BlockQrHouseHolderSolver solver = new BlockQrHouseHolderSolver();

        for( int i = 1; i <= r*3; i++ ) {
            BlockMatrix64F A = BlockMatrixOps.createRandom(i,i,-1,1,rand,r);

            BlockMatrix64F A_orig = A.copy();
            BlockMatrix64F I = new BlockMatrix64F(i,i,r);

            assertTrue(solver.setA(A.copy()));

            solver.invert(A);

            // A times its inverse is an identity matrix
            BlockMatrixOps.mult(A,A_orig,I);

            assertTrue(GenericMatrixOps.isIdentity(I,1e-8));
        }
    }

    @Test
    public void testQuality() {
        BlockMatrix64F A = BlockMatrixOps.convert(CommonOps.diag(4,3,2,1),3);
        BlockMatrix64F B = BlockMatrixOps.convert(CommonOps.diag(4,3,2,0.1),3);

        // see if a matrix with smaller singular value has a worse quality
        BlockQrHouseHolderSolver solver = new BlockQrHouseHolderSolver();
        assertTrue(solver.setA(A.copy()));
        double qualityA = solver.quality();

        assertTrue(solver.setA(B.copy()));
        double qualityB = solver.quality();

        assertTrue(qualityB<qualityA);
        assertEquals(qualityB*10.0,qualityA,1e-8);
    }

    /**
     * Checks to see if quality is scale invariant.
     */
    @Test
    public void testQuality_scale() {
        BlockMatrix64F A = BlockMatrixOps.convert(CommonOps.diag(4,3,2,1),3);
        BlockMatrix64F B = A.copy();
        CommonOps.scale(2,B);

        // see if a matrix with smaller singular value has a worse quality
        BlockQrHouseHolderSolver solver = new BlockQrHouseHolderSolver();
        assertTrue(solver.setA(A.copy()));
        double qualityA = solver.quality();

        assertTrue(solver.setA(B.copy()));
        double qualityB = solver.quality();

        assertEquals(qualityA,qualityB,1e-8);
    }

    @Test
    public void modifiesA(){
        BlockMatrix64F A = BlockMatrixOps.createRandom(4,4,-1,1,rand,3);
        BlockMatrix64F A_orig = A.copy();

        BlockQrHouseHolderSolver solver = new BlockQrHouseHolderSolver();

        assertTrue(solver.setA(A));

        boolean modified = !MatrixFeatures.isEquals(A,A_orig);

        assertTrue(modified == solver.modifiesA());
    }

    @Test
    public void modifiesB(){
        BlockMatrix64F A = BlockMatrixOps.createRandom(4,4,-1,1,rand,3);

        BlockQrHouseHolderSolver solver = new BlockQrHouseHolderSolver();

        assertTrue(solver.setA(A));

        BlockMatrix64F B = BlockMatrixOps.createRandom(4,2,-1,1,rand,3);
        BlockMatrix64F B_orig = B.copy();
        BlockMatrix64F X = new BlockMatrix64F(A.numRows,B.numCols,3);

        solver.solve(B,X);

        boolean modified = !MatrixFeatures.isEquals(B_orig,B);

        assertTrue(modified == solver.modifiesB());
    }

}
