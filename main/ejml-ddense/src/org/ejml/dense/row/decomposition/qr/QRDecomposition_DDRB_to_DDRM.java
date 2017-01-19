/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decomposition.qr;

import org.ejml.EjmlParameters;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.block.decomposition.qr.QRDecompositionHouseholder_DDRB;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.decomposition.BaseDecomposition_DDRB_to_DDRM;
import org.ejml.interfaces.decomposition.QRDecomposition;


/**
 * Wrapper that allows {@link QRDecomposition}(DMatrixRBlock) to be used
 * as a {@link QRDecomposition}(DMatrixRMaj).
 *
 * @author Peter Abeles
 */
public class QRDecomposition_DDRB_to_DDRM
        extends BaseDecomposition_DDRB_to_DDRM implements QRDecomposition<DMatrixRMaj>  {

    public QRDecomposition_DDRB_to_DDRM() {
        super(new QRDecompositionHouseholder_DDRB(), EjmlParameters.BLOCK_WIDTH);
    }

    @Override
    public DMatrixRMaj getQ(DMatrixRMaj Q, boolean compact) {

        int minLength = Math.min(Ablock.numRows,Ablock.numCols);
        if( Q == null  ) {
            if( compact ) {
                Q = new DMatrixRMaj(Ablock.numRows,minLength);
                CommonOps_DDRM.setIdentity(Q);
            } else {
                Q = new DMatrixRMaj(Ablock.numRows,Ablock.numRows);
                CommonOps_DDRM.setIdentity(Q);
            }
        }

        DMatrixRBlock Qblock = new DMatrixRBlock();
        Qblock.numRows =  Q.numRows;
        Qblock.numCols =  Q.numCols;
        Qblock.blockLength = blockLength;
        Qblock.data = Q.data;

        ((QRDecompositionHouseholder_DDRB)alg).getQ(Qblock,compact);

        convertBlockToRow(Q.numRows,Q.numCols,Ablock.blockLength,Q.data);

        return Q;
    }

    @Override
    public DMatrixRMaj getR(DMatrixRMaj R, boolean compact) {
        DMatrixRBlock Rblock;

        Rblock = ((QRDecompositionHouseholder_DDRB)alg).getR(null,compact);

        if( R == null ) {
            R = new DMatrixRMaj(Rblock.numRows,Rblock.numCols);
        }
        MatrixOps_DDRB.convert(Rblock,R);

        return R;
    }

}
