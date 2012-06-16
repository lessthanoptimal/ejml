/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.chol;

import org.ejml.EjmlParameters;
import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.block.decomposition.chol.BlockCholeskyOuterForm;
import org.ejml.alg.dense.decomposition.BaseDecompositionBlock64;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.CholeskyDecomposition;


/**
 * Wrapper around {@link org.ejml.alg.block.decomposition.chol.BlockCholeskyOuterForm} that allows
 * it to process DenseMatrix64F.
 *
 * @author Peter Abeles
 */
public class CholeskyDecompositionBlock64
        extends BaseDecompositionBlock64 implements CholeskyDecomposition<DenseMatrix64F> {

    public CholeskyDecompositionBlock64( boolean lower ) {
        super(new BlockCholeskyOuterForm(lower), EjmlParameters.BLOCK_WIDTH);
    }

    @Override
    public boolean isLower() {
        return ((BlockCholeskyOuterForm)alg).isLower();
    }

    @Override
    public DenseMatrix64F getT(DenseMatrix64F T) {
        BlockMatrix64F T_block = ((BlockCholeskyOuterForm)alg).getT(null);

        if( T == null ) {
            T = new DenseMatrix64F(T_block.numRows,T_block.numCols);
        }

        BlockMatrixOps.convert(T_block,T);
        // todo set zeros
        return T;
    }
}
