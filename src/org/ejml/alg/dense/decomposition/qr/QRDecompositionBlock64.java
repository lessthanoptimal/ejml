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

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.EjmlParameters;
import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.block.decomposition.qr.BlockMatrix64HouseholderQR;
import org.ejml.alg.dense.decomposition.BaseDecompositionBlock64;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.QRDecomposition;
import org.ejml.ops.CommonOps;


/**
 * Wrapper that allows {@link QRDecomposition}(BlockMatrix64F) to be used
 * as a {@link QRDecomposition}(DenseMatrix64F).
 *
 * @author Peter Abeles
 */
public class QRDecompositionBlock64
        extends BaseDecompositionBlock64 implements QRDecomposition<DenseMatrix64F>  {

    public QRDecompositionBlock64() {
        super(new BlockMatrix64HouseholderQR(), EjmlParameters.BLOCK_WIDTH);
    }

    @Override
    public DenseMatrix64F getQ(DenseMatrix64F Q, boolean compact) {

        int minLength = Math.min(Ablock.numRows,Ablock.numCols);
        if( Q == null  ) {
            if( compact ) {
                Q = new DenseMatrix64F(Ablock.numRows,minLength);
                CommonOps.setIdentity(Q);
            } else {
                Q = new DenseMatrix64F(Ablock.numRows,Ablock.numRows);
                CommonOps.setIdentity(Q);
            }
        }

        BlockMatrix64F Qblock = new BlockMatrix64F();
        Qblock.numRows =  Q.numRows;
        Qblock.numCols =  Q.numCols;
        Qblock.blockLength = blockLength;
        Qblock.data = Q.data;

        ((BlockMatrix64HouseholderQR)alg).getQ(Qblock,compact);

        convertBlockToRow(Q.numRows,Q.numCols,Ablock.blockLength,Q.data);

        return Q;
    }

    @Override
    public DenseMatrix64F getR(DenseMatrix64F R, boolean compact) {
        BlockMatrix64F Rblock;

        Rblock = ((BlockMatrix64HouseholderQR)alg).getR(null,compact);

        if( R == null ) {
            R = new DenseMatrix64F(Rblock.numRows,Rblock.numCols);
        }
        BlockMatrixOps.convert(Rblock,R);

        return R;
    }

}
