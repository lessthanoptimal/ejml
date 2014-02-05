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

package org.ejml.alg.dense.decomposition.hessenberg;

import org.ejml.EjmlParameters;
import org.ejml.alg.block.decomposition.hessenberg.TridiagonalDecompositionHouseholder_B64;
import org.ejml.alg.dense.decomposition.BaseDecomposition_B64_to_D64;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition;
import org.ejml.ops.CommonOps;


/**
 * Wrapper around a block implementation of TridiagonalSimilarDecomposition
 *
 * @author Peter Abeles
 */
public class TridiagonalDecomposition_B64_to_D64
        extends BaseDecomposition_B64_to_D64
        implements TridiagonalSimilarDecomposition<DenseMatrix64F> {


    public TridiagonalDecomposition_B64_to_D64() {
        this(EjmlParameters.BLOCK_WIDTH);
    }

    public TridiagonalDecomposition_B64_to_D64(int blockSize) {
        super(new TridiagonalDecompositionHouseholder_B64(),blockSize);
    }

    @Override
    public DenseMatrix64F getT(DenseMatrix64F T) {
        int N = Ablock.numRows;

        if( T == null ) {
            T = new DenseMatrix64F(N,N);
        } else {
            CommonOps.fill(T, 0);
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
    public DenseMatrix64F getQ(DenseMatrix64F Q, boolean transposed) {
        if( Q == null ) {
            Q = new DenseMatrix64F(Ablock.numRows,Ablock.numCols);
        }

        BlockMatrix64F Qblock = new BlockMatrix64F();
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
