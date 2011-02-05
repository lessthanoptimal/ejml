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

package org.ejml.alg.block.decomposition.hessenberg;

import org.ejml.alg.dense.decomposition.hessenberg.TridiagonalSimilarDecomposition;
import org.ejml.data.BlockMatrix64F;


/**
 * <p>
 * Tridiagonal similar decomposition for block matrices.  Orthogonal matrices are computed using
 * householder vectors.
 * </p>
 *
 * <p>
 * Based off algorithm in section 2 of J. J. Dongarra, D. C. Sorensen, S. J. Hammarling,
 * "Block Reduction of Matrices to Condensed Forms for Eigenvalue Computations" Journal of
 * Computations and Applied Mathematics 27 (1989) 215-227
 * </p>
 *
 * @author Peter Abeles
 */
public class TridiagonalDecompositionBlockHouseholder
        implements TridiagonalSimilarDecomposition<BlockMatrix64F> {
    
    @Override
    public BlockMatrix64F getT(BlockMatrix64F T) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public BlockMatrix64F getQ(BlockMatrix64F Q, boolean transposed) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void getDiagonal(double[] diag, double[] off) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean decompose(BlockMatrix64F orig) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean inputModified() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
