/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.block.linsol.chol;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.dense.linsol.LinearSolver;
import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
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
    public void testPositive() {
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

                assertTrue(BlockMatrixOps.isIdentical(X,X_found,1e-8));
            }
        }
    }

    /**
     * Give it a matrix which is not SPD and see if it fails
     */
    @Test
    public void testNegative() {
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

    protected BlockMatrix64F createMatrixSPD( int width ) {
        DenseMatrix64F A = RandomMatrices.createSymmPosDef(width,rand);

        return BlockMatrixOps.convert(A,r);
    }
}
