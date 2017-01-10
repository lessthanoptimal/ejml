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

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.EjmlParameters;
import org.ejml.alg.block.MatrixOps_B64;
import org.ejml.alg.block.decomposition.qr.QRDecompositionHouseholder_B64;
import org.ejml.alg.dense.decomposition.BaseDecomposition_B64_to_R64;
import org.ejml.data.DMatrixBlock_F64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.ops.CommonOps_R64;


/**
 * Wrapper that allows {@link QRDecomposition}(DMatrixBlock_F64) to be used
 * as a {@link QRDecomposition}(DMatrixRow_F64).
 *
 * @author Peter Abeles
 */
public class QRDecomposition_B64_to_R64
        extends BaseDecomposition_B64_to_R64 implements QRDecomposition<DMatrixRow_F64>  {

    public QRDecomposition_B64_to_R64() {
        super(new QRDecompositionHouseholder_B64(), EjmlParameters.BLOCK_WIDTH);
    }

    @Override
    public DMatrixRow_F64 getQ(DMatrixRow_F64 Q, boolean compact) {

        int minLength = Math.min(Ablock.numRows,Ablock.numCols);
        if( Q == null  ) {
            if( compact ) {
                Q = new DMatrixRow_F64(Ablock.numRows,minLength);
                CommonOps_R64.setIdentity(Q);
            } else {
                Q = new DMatrixRow_F64(Ablock.numRows,Ablock.numRows);
                CommonOps_R64.setIdentity(Q);
            }
        }

        DMatrixBlock_F64 Qblock = new DMatrixBlock_F64();
        Qblock.numRows =  Q.numRows;
        Qblock.numCols =  Q.numCols;
        Qblock.blockLength = blockLength;
        Qblock.data = Q.data;

        ((QRDecompositionHouseholder_B64)alg).getQ(Qblock,compact);

        convertBlockToRow(Q.numRows,Q.numCols,Ablock.blockLength,Q.data);

        return Q;
    }

    @Override
    public DMatrixRow_F64 getR(DMatrixRow_F64 R, boolean compact) {
        DMatrixBlock_F64 Rblock;

        Rblock = ((QRDecompositionHouseholder_B64)alg).getR(null,compact);

        if( R == null ) {
            R = new DMatrixRow_F64(Rblock.numRows,Rblock.numCols);
        }
        MatrixOps_B64.convert(Rblock,R);

        return R;
    }

}
