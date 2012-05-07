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

package org.ejml.alg.dense.decomposition.hessenberg;

import org.ejml.EjmlParameters;
import org.ejml.alg.block.decomposition.hessenberg.TridiagonalDecompositionBlockHouseholder;
import org.ejml.alg.dense.decomposition.BaseDecompositionBlock64;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;


/**
 * Wrapper around a block implementation of TridiagonalSimilarDecomposition
 *
 * @author Peter Abeles
 */
public class TridiagonalDecompositionBlock 
        extends BaseDecompositionBlock64
        implements TridiagonalSimilarDecomposition<DenseMatrix64F>  {


    public TridiagonalDecompositionBlock() {
        this(EjmlParameters.BLOCK_WIDTH);
    }

    public TridiagonalDecompositionBlock( int blockSize ) {
        super(new TridiagonalDecompositionBlockHouseholder(),blockSize);
    }

    @Override
    public DenseMatrix64F getT(DenseMatrix64F T) {
        int N = Ablock.numRows;

        if( T == null ) {
            T = new DenseMatrix64F(N,N);
        } else {
            CommonOps.set(T,0);
        }

        double[] diag = new double[ N ];
        double[] off = new double[ N ];

        ((TridiagonalDecompositionBlockHouseholder)alg).getDiagonal(diag,off);

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

        ((TridiagonalDecompositionBlockHouseholder)alg).getQ(Qblock,transposed);

        convertBlockToRow(Q.numRows,Q.numCols,Ablock.blockLength,Q.data);

        return Q;
    }

    @Override
    public void getDiagonal(double[] diag, double[] off) {
        ((TridiagonalDecompositionBlockHouseholder)alg).getDiagonal(diag,off);
    }
}
