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
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.block.decomposition.chol.CholeskyOuterForm_DDRB;
import org.ejml.dense.row.decomposition.BaseDecomposition_DDRB_to_DDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;


/**
 * Wrapper around {@link org.ejml.dense.block.decomposition.chol.CholeskyOuterForm_DDRB} that allows
 * it to process DMatrixRMaj.
 *
 * @author Peter Abeles
 */
public class CholeskyDecomposition_DDRB_to_DDRM
        extends BaseDecomposition_DDRB_to_DDRM implements CholeskyDecomposition_F64<DMatrixRMaj> {

    public CholeskyDecomposition_DDRB_to_DDRM(boolean lower) {
        super(new CholeskyOuterForm_DDRB(lower), EjmlParameters.BLOCK_WIDTH);
    }

    @Override
    public boolean isLower() {
        return ((CholeskyOuterForm_DDRB)alg).isLower();
    }

    @Override
    public DMatrixRMaj getT(DMatrixRMaj T) {
        DMatrixRBlock T_block = ((CholeskyOuterForm_DDRB)alg).getT(null);

        if( T == null ) {
            T = new DMatrixRMaj(T_block.numRows,T_block.numCols);
        }

        MatrixOps_DDRB.convert(T_block,T);
        // todo set zeros
        return T;
    }

    @Override
    public Complex_F64 computeDeterminant() {
        return ((CholeskyOuterForm_DDRB)alg).computeDeterminant();
    }
}
