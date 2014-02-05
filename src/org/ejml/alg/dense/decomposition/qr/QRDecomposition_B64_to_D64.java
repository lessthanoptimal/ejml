/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.EjmlParameters;
import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.block.decomposition.qr.QRDecompositionHouseholder_B64;
import org.ejml.alg.dense.decomposition.BaseDecomposition_B64_to_D64;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.ops.CommonOps;


/**
 * Wrapper that allows {@link QRDecomposition}(BlockMatrix64F) to be used
 * as a {@link QRDecomposition}(DenseMatrix64F).
 *
 * @author Peter Abeles
 */
public class QRDecomposition_B64_to_D64
        extends BaseDecomposition_B64_to_D64 implements QRDecomposition<DenseMatrix64F>  {

    public QRDecomposition_B64_to_D64() {
        super(new QRDecompositionHouseholder_B64(), EjmlParameters.BLOCK_WIDTH);
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

        ((QRDecompositionHouseholder_B64)alg).getQ(Qblock,compact);

        convertBlockToRow(Q.numRows,Q.numCols,Ablock.blockLength,Q.data);

        return Q;
    }

    @Override
    public DenseMatrix64F getR(DenseMatrix64F R, boolean compact) {
        BlockMatrix64F Rblock;

        Rblock = ((QRDecompositionHouseholder_B64)alg).getR(null,compact);

        if( R == null ) {
            R = new DenseMatrix64F(Rblock.numRows,Rblock.numCols);
        }
        BlockMatrixOps.convert(Rblock,R);

        return R;
    }

}
