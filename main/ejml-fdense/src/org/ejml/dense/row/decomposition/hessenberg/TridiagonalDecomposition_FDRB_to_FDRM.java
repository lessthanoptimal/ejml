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

package org.ejml.dense.row.decomposition.hessenberg;

import org.ejml.EjmlParameters;
import org.ejml.data.FMatrixRBlock;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.block.decomposition.hessenberg.TridiagonalDecompositionHouseholder_FDRB;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.decomposition.BaseDecomposition_FDRB_to_FDRM;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F32;


/**
 * Wrapper around a block implementation of TridiagonalSimilarDecomposition_F32
 *
 * @author Peter Abeles
 */
public class TridiagonalDecomposition_FDRB_to_FDRM
        extends BaseDecomposition_FDRB_to_FDRM
        implements TridiagonalSimilarDecomposition_F32<FMatrixRMaj> {


    public TridiagonalDecomposition_FDRB_to_FDRM() {
        this(EjmlParameters.BLOCK_WIDTH);
    }

    public TridiagonalDecomposition_FDRB_to_FDRM(int blockSize) {
        super(new TridiagonalDecompositionHouseholder_FDRB(),blockSize);
    }

    @Override
    public FMatrixRMaj getT(FMatrixRMaj T) {
        int N = Ablock.numRows;

        if( T == null ) {
            T = new FMatrixRMaj(N,N);
        } else {
            CommonOps_FDRM.fill(T, 0);
        }

        float[] diag = new float[ N ];
        float[] off = new float[ N ];

        ((TridiagonalDecompositionHouseholder_FDRB)alg).getDiagonal(diag,off);

        T.unsafe_set(0,0,diag[0]);
        for( int i = 1; i < N; i++ ) {
            T.unsafe_set(i,i,diag[i]);
            T.unsafe_set(i,i-1,off[i-1]);
            T.unsafe_set(i-1,i,off[i-1]);
        }

        return T;
    }

    @Override
    public FMatrixRMaj getQ(FMatrixRMaj Q, boolean transposed) {
        if( Q == null ) {
            Q = new FMatrixRMaj(Ablock.numRows,Ablock.numCols);
        }

        FMatrixRBlock Qblock = new FMatrixRBlock();
        Qblock.numRows =  Q.numRows;
        Qblock.numCols =  Q.numCols;
        Qblock.blockLength = blockLength;
        Qblock.data = Q.data;

        ((TridiagonalDecompositionHouseholder_FDRB)alg).getQ(Qblock,transposed);

        convertBlockToRow(Q.numRows,Q.numCols,Ablock.blockLength,Q.data);

        return Q;
    }

    @Override
    public void getDiagonal(float[] diag, float[] off) {
        ((TridiagonalDecompositionHouseholder_FDRB)alg).getDiagonal(diag,off);
    }
}
