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
import org.ejml.data.DMatrixBlock_F64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.block.decomposition.hessenberg.TridiagonalDecompositionHouseholder_B64;
import org.ejml.dense.row.CommonOps_R64;
import org.ejml.dense.row.decomposition.BaseDecomposition_B64_to_R64;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F64;


/**
 * Wrapper around a block implementation of TridiagonalSimilarDecomposition_F64
 *
 * @author Peter Abeles
 */
public class TridiagonalDecomposition_B64_to_R64
        extends BaseDecomposition_B64_to_R64
        implements TridiagonalSimilarDecomposition_F64<DMatrixRow_F64> {


    public TridiagonalDecomposition_B64_to_R64() {
        this(EjmlParameters.BLOCK_WIDTH);
    }

    public TridiagonalDecomposition_B64_to_R64(int blockSize) {
        super(new TridiagonalDecompositionHouseholder_B64(),blockSize);
    }

    @Override
    public DMatrixRow_F64 getT(DMatrixRow_F64 T) {
        int N = Ablock.numRows;

        if( T == null ) {
            T = new DMatrixRow_F64(N,N);
        } else {
            CommonOps_R64.fill(T, 0);
        }

        double[] diag = new double[ N ];
        double[] off = new double[ N ];

        ((TridiagonalDecompositionHouseholder_B64)alg).getDiagonal(diag,off);

        T.unsafe_set(0,0,diag[0]);
        for( int i = 1; i < N; i++ ) {
            T.unsafe_set(i,i,diag[i]);
            T.unsafe_set(i,i-1,off[i-1]);
            T.unsafe_set(i-1,i,off[i-1]);
        }

        return T;
    }

    @Override
    public DMatrixRow_F64 getQ(DMatrixRow_F64 Q, boolean transposed) {
        if( Q == null ) {
            Q = new DMatrixRow_F64(Ablock.numRows,Ablock.numCols);
        }

        DMatrixBlock_F64 Qblock = new DMatrixBlock_F64();
        Qblock.numRows =  Q.numRows;
        Qblock.numCols =  Q.numCols;
        Qblock.blockLength = blockLength;
        Qblock.data = Q.data;

        ((TridiagonalDecompositionHouseholder_B64)alg).getQ(Qblock,transposed);

        convertBlockToRow(Q.numRows,Q.numCols,Ablock.blockLength,Q.data);

        return Q;
    }

    @Override
    public void getDiagonal(double[] diag, double[] off) {
        ((TridiagonalDecompositionHouseholder_B64)alg).getDiagonal(diag,off);
    }
}
