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

import org.ejml.alg.block.linsol.LinearSolverBlock;
import org.ejml.data.BlockMatrix64F;


/**
 * @author Peter Abeles
 */
public class BlockCholeskyOuterSolver implements LinearSolverBlock {

    

    @Override
    public BlockMatrix64F getA() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean setA(BlockMatrix64F A) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public double quality() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void solve(BlockMatrix64F B, BlockMatrix64F X) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void invert(BlockMatrix64F A_inv) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean inputModified() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
