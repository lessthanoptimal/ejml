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

package org.ejml.alg.dense.linsol.lu;

import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt;
import org.ejml.alg.dense.linsol.GenericLinearSolverChecks;
import org.ejml.alg.dense.linsol.LinearSolver;
import org.ejml.data.DenseMatrix64F;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverLuKJI extends GenericLinearSolverChecks {

    public TestLinearSolverLuKJI() {
        shouldWorkRectangle = false;
        shouldFailSingular = false;
    }

    @Override
    protected LinearSolver<DenseMatrix64F> createSolver( DenseMatrix64F A ) {
        LUDecompositionAlt decomp = new LUDecompositionAlt();

        return new LinearSolverLuKJI(decomp);
    }
}