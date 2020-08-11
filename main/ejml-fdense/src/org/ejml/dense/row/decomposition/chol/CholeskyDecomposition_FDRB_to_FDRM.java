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
import org.ejml.data.Complex_F32;
import org.ejml.data.FMatrixRBlock;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.block.MatrixOps_FDRB;
import org.ejml.dense.block.decomposition.chol.CholeskyOuterForm_FDRB;
import org.ejml.dense.row.decomposition.BaseDecomposition_FDRB_to_FDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F32;


/**
 * Wrapper around {@link org.ejml.dense.block.decomposition.chol.CholeskyOuterForm_FDRB} that allows
 * it to process FMatrixRMaj.
 *
 * @author Peter Abeles
 */
public class CholeskyDecomposition_FDRB_to_FDRM
        extends BaseDecomposition_FDRB_to_FDRM implements CholeskyDecomposition_F32<FMatrixRMaj> {

    public CholeskyDecomposition_FDRB_to_FDRM(boolean lower) {
        super(new CholeskyOuterForm_FDRB(lower), EjmlParameters.BLOCK_WIDTH);
    }

    @Override
    public boolean isLower() {
        return ((CholeskyOuterForm_FDRB)alg).isLower();
    }

    @Override
    public FMatrixRMaj getT(FMatrixRMaj T) {
        FMatrixRBlock T_block = ((CholeskyOuterForm_FDRB)alg).getT(null);

        if( T == null ) {
            T = new FMatrixRMaj(T_block.numRows,T_block.numCols);
        }

        MatrixOps_FDRB.convert(T_block,T);
        // todo set zeros
        return T;
    }

    @Override
    public Complex_F32 computeDeterminant() {
        return ((CholeskyOuterForm_FDRB)alg).computeDeterminant();
    }
}
