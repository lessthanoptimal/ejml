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

package org.ejml.alg.dense.linsol.chol;

import org.ejml.alg.dense.linsol.LinearSolver;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.EjmlUnitTests;
import org.ejml.ops.RandomMatrices;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class BaseCholeskySolveTests {

    Random rand = new Random(0x45);

    public void standardTests( LinearSolver solver ) {
        testSolve(solver);
        testInvert(solver);
        testQuality(solver);
        testQuality_scale(solver);
    }

    public void testSolve( LinearSolver solver ) {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, 17, 97, 320);
        DenseMatrix64F x = RandomMatrices.createRandom(3,1,rand);
        DenseMatrix64F A_orig = A.copy();
        DenseMatrix64F B_orig = b.copy();

        assertTrue(solver.setA(A));
        solver.solve(b,x);

        // see if the input got modified
        EjmlUnitTests.assertEquals(A,A_orig,1e-5);
        EjmlUnitTests.assertEquals(b,B_orig,1e-5);

        DenseMatrix64F x_expected = new DenseMatrix64F(3,1, true, 1, 2, 3);

        EjmlUnitTests.assertEquals(x_expected,x,1e-6);
    }

    public void testInvert( LinearSolver solver ) {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);

        assertTrue(solver.setA(A));
        solver.invert(A);

        DenseMatrix64F A_inv = new DenseMatrix64F(3,3, true, 1.453515, -0.199546, -0.013605, -0.199546, 0.167800, -0.034014, -0.013605, -0.034014, 0.020408);

        EjmlUnitTests.assertEquals(A_inv,A,1e-5);
    }

    public void testQuality( LinearSolver solver ) {
        DenseMatrix64F A = CommonOps.diag(3,2,1);
        DenseMatrix64F B = CommonOps.diag(3,2,0.001);

        assertTrue(solver.setA(A));
        double qualityA = solver.quality();

        assertTrue(solver.setA(B));
        double qualityB = solver.quality();

        assertTrue(qualityB < qualityA);
    }

    public void testQuality_scale( LinearSolver solver ) {
        DenseMatrix64F A = CommonOps.diag(3,2,1);
        DenseMatrix64F B = A.copy();
        CommonOps.scale(0.001,B);

        assertTrue(solver.setA(A));
        double qualityA = solver.quality();

        assertTrue(solver.setA(B));
        double qualityB = solver.quality();

        assertEquals(qualityB,qualityA,1e-8);
    }
}
