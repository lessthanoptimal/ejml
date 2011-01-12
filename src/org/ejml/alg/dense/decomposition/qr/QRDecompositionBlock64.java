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

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.block.decomposition.qr.BlockMatrix64HouseholderQR;
import org.ejml.alg.dense.decomposition.BaseDecompositionBlock64;
import org.ejml.alg.dense.decomposition.QRDecomposition;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;


/**
 * Wrapper that allows {@link QRDecomposition}(BlockMatrix64F) to be used
 * as a {@link QRDecomposition}(DenseMatrix64F).
 *
 * @author Peter Abeles
 */
public class QRDecompositionBlock64
        extends BaseDecompositionBlock64 implements QRDecomposition<DenseMatrix64F>  {

    public QRDecompositionBlock64() {
        super(new BlockMatrix64HouseholderQR());
    }

    @Override
    public DenseMatrix64F getQ(DenseMatrix64F Q, boolean compact) {

        BlockMatrix64F Qblock;

        Qblock = ((BlockMatrix64HouseholderQR)alg).getQ(null,compact);

        if( Q == null ) {
            Q = new DenseMatrix64F(Qblock.numRows,Qblock.numCols);
        }
        BlockMatrixOps.convert(Qblock,Q);

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
