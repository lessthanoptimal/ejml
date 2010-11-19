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

package org.ejml.alg.dense.decomposition;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.block.decomposition.BlockDecompositionInterface;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;


/**
 * Generic interface for wrapping a {@link BlockMatrix64F} decomposition for
 * processing of {@link org.ejml.data.DenseMatrix64F}.
 *
 * @author Peter Abeles
 */
public class BaseDecompositionBlock64 implements DecompositionInterface {

    protected BlockDecompositionInterface alg;

    protected BlockMatrix64F Ablock = new BlockMatrix64F(1,1);

    public BaseDecompositionBlock64(BlockDecompositionInterface alg) {
        this.alg = alg;
    }

    @Override
    public boolean decompose(DenseMatrix64F A) {
        Ablock.reshape(A.numRows,A.numCols,false);

        BlockMatrixOps.convert(A,Ablock);

        return alg.decompose(Ablock);
    }

    @Override
    public boolean inputModified() {
        return false;
    }
}
