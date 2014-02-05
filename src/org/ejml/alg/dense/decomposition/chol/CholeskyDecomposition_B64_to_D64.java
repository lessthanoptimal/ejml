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

package org.ejml.alg.dense.decomposition.chol;

import org.ejml.EjmlParameters;
import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.block.decomposition.chol.CholeskyOuterForm_B64;
import org.ejml.alg.dense.decomposition.BaseDecomposition_B64_to_D64;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.decomposition.CholeskyDecomposition;


/**
 * Wrapper around {@link org.ejml.alg.block.decomposition.chol.CholeskyOuterForm_B64} that allows
 * it to process DenseMatrix64F.
 *
 * @author Peter Abeles
 */
public class CholeskyDecomposition_B64_to_D64
        extends BaseDecomposition_B64_to_D64 implements CholeskyDecomposition<DenseMatrix64F> {

    public CholeskyDecomposition_B64_to_D64(boolean lower) {
        super(new CholeskyOuterForm_B64(lower), EjmlParameters.BLOCK_WIDTH);
    }

    @Override
    public boolean isLower() {
        return ((CholeskyOuterForm_B64)alg).isLower();
    }

    @Override
    public DenseMatrix64F getT(DenseMatrix64F T) {
        BlockMatrix64F T_block = ((CholeskyOuterForm_B64)alg).getT(null);

        if( T == null ) {
            T = new DenseMatrix64F(T_block.numRows,T_block.numCols);
        }

        BlockMatrixOps.convert(T_block,T);
        // todo set zeros
        return T;
    }
}
