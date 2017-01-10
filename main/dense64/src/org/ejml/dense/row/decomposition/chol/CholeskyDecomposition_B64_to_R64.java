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

package org.ejml.dense.row.decomposition.chol;

import org.ejml.EjmlParameters;
import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixBlock_F64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.block.MatrixOps_B64;
import org.ejml.dense.block.decomposition.chol.CholeskyOuterForm_B64;
import org.ejml.dense.row.decomposition.BaseDecomposition_B64_to_R64;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;


/**
 * Wrapper around {@link org.ejml.dense.block.decomposition.chol.CholeskyOuterForm_B64} that allows
 * it to process DMatrixRow_F64.
 *
 * @author Peter Abeles
 */
public class CholeskyDecomposition_B64_to_R64
        extends BaseDecomposition_B64_to_R64 implements CholeskyDecomposition_F64<DMatrixRow_F64> {

    public CholeskyDecomposition_B64_to_R64(boolean lower) {
        super(new CholeskyOuterForm_B64(lower), EjmlParameters.BLOCK_WIDTH);
    }

    @Override
    public boolean isLower() {
        return ((CholeskyOuterForm_B64)alg).isLower();
    }

    @Override
    public DMatrixRow_F64 getT(DMatrixRow_F64 T) {
        DMatrixBlock_F64 T_block = ((CholeskyOuterForm_B64)alg).getT(null);

        if( T == null ) {
            T = new DMatrixRow_F64(T_block.numRows,T_block.numCols);
        }

        MatrixOps_B64.convert(T_block,T);
        // todo set zeros
        return T;
    }

    @Override
    public Complex_F64 computeDeterminant() {
        return ((CholeskyOuterForm_B64)alg).computeDeterminant();
    }
}
