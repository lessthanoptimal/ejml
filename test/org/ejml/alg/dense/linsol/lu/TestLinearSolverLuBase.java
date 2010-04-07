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

package org.ejml.alg.dense.linsol.lu;

import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.UtilTestMatrix;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverLuBase {

    Random rand = new Random(0x334);

    /**
     * Make sure that improve solution doesn't make things worse.  This test does
     * not realy test to see if it makes things better.
     */
    @Test
    public void testImproveSol_noharm() {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 0, 1, 2, -2, 4, 9, 0.5, 0, 5);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, 8, 33, 15.5);
        DenseMatrix64F x = RandomMatrices.createRandom(3,1,rand);
        DenseMatrix64F x_improved = new DenseMatrix64F(3,1);

        LUDecompositionAlt alg = new LUDecompositionAlt();

        x_improved.set(x);

        LinearSolverLu solver = new LinearSolverLu(alg);
        assertTrue(solver.setA(A));
        solver.solve(x,b);
        solver.improveSol(x_improved,b);

//        DenseMatrix64F x_truth = new DenseMatrix64F(3,1,new double[]{1,2,3});

        UtilTestMatrix.checkEquals(x,x_improved);
    }
}
