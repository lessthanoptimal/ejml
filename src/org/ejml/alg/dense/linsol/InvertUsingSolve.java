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

package org.ejml.alg.dense.linsol;

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.RowD1Matrix64F;
import org.ejml.ops.CommonOps;


/**
 * A matrix can be easily inverted by solving a system with an identify matrix.  The only
 * disadvantage of this approach is that additional computations are required compared to
 * a specialized solution.
 *
 * @author Peter Abeles
 */
public class InvertUsingSolve {
    public static void invert( LinearSolver<DenseMatrix64F> solver , RowD1Matrix64F A , DenseMatrix64F A_inv ) {

        if( A.numRows != A_inv.numRows || A.numCols != A_inv.numCols) {
            throw new IllegalArgumentException("A and A_inv must have the same dimensions");
        }

        CommonOps.setIdentity(A_inv);

        solver.solve(A_inv,A_inv);
    }
}
