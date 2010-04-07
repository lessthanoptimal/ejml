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

package org.ejml.alg.dense.linsol.gj;

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.UtilTestMatrix;
import org.junit.Test;


/**
 * @author Peter Abeles
 */
public class TestGaussJordanNoPivot {


    /**
     * This algorithm only works for fairly simplic matrices
     */
    @Test
    public void testTrivial() {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 5, 2, 3, 1.5, -2, 8, -3, 4.7, -0.5);
        DenseMatrix64F x = new DenseMatrix64F(3,1);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, 18, 21.5, 4.9000);

        GaussJordanNoPivot solver = new GaussJordanNoPivot();
        solver.setA(A);
        solver.solve(b,x);

        DenseMatrix64F inv = new DenseMatrix64F(3,3, true, 0.1616965, -0.0667108, -0.0971946, 0.1027170, -0.0287166, 0.1568368, -0.0046388, 0.1303291, 0.0574332);
        DenseMatrix64F x_expected = new DenseMatrix64F(3,1, true, 1, 2, 3);

        UtilTestMatrix.checkEquals(inv,A,1e-6);
        UtilTestMatrix.checkEquals(x_expected,x);
    }

    /**
     * Give it a matrix where it will fail since it can't pivot the elements around
     */
    @Test(expected=IllegalArgumentException.class)
    public void testRequirePivot() {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 0, 1, 2, -2, 4, 9, 0.5, 0, 5);
        DenseMatrix64F x = new DenseMatrix64F(3,1);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, 8, 33, 15.5);

        GaussJordanNoPivot solver = new GaussJordanNoPivot();
        solver.setA(A);
        solver.solve(b,x);
    }
}
