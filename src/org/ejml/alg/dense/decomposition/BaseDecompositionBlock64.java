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

package org.ejml.alg.dense.decomposition;

import org.ejml.EjmlParameters;
import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;


/**
 * Generic interface for wrapping a {@link BlockMatrix64F} decomposition for
 * processing of {@link org.ejml.data.DenseMatrix64F}.
 *
 * @author Peter Abeles
 */
public class BaseDecompositionBlock64 implements DecompositionInterface<DenseMatrix64F> {

    protected DecompositionInterface<BlockMatrix64F> alg;

    protected double[]tmp;
    protected BlockMatrix64F Ablock = new BlockMatrix64F();

    public BaseDecompositionBlock64(DecompositionInterface<BlockMatrix64F> alg) {
        this.alg = alg;
    }

    @Override
    public boolean decompose(DenseMatrix64F A) {
        Ablock.numRows = A.numRows;
        Ablock.numCols = A.numCols;
        Ablock.blockLength = EjmlParameters.BLOCK_WIDTH;
        Ablock.data = A.data;

        int tmpLength = Math.min( Ablock.blockLength , A.numRows ) * A.numCols;

        if( tmp == null || tmp.length < tmpLength )
            tmp = new double[ tmpLength ];

        // doing an in-place convert is much more memory efficient at the cost of a little
        // but of CPU
        BlockMatrixOps.convertRowToBlock(A.numRows,A.numCols,Ablock.blockLength,A.data,tmp);

        boolean ret = alg.decompose(Ablock);

        // convert it back to the normal format if it wouldn't have been modified
        if( !alg.inputModified() ) {
            BlockMatrixOps.convertBlockToRow(A.numRows,A.numCols,Ablock.blockLength,A.data,tmp);
        }

        return ret;
    }

    @Override
    public boolean inputModified() {
        return alg.inputModified();
    }
}
