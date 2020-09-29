/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decomposition.svd;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_MT_DDRM;
import org.ejml.dense.row.decomposition.bidiagonal.BidiagonalDecompositionRow_MT_DDRM;
import org.ejml.dense.row.decomposition.bidiagonal.BidiagonalDecompositionTall_MT_DDRM;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Concurrent version of {@link SvdImplicitQrDecompose_DDRM}</p>
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class SvdImplicitQrDecompose_MT_DDRM extends SvdImplicitQrDecompose_DDRM {

    public SvdImplicitQrDecompose_MT_DDRM( boolean compact, boolean computeU, boolean computeV,
                                           boolean canUseTallBidiagonal ) {
        super(compact, computeU, computeV, canUseTallBidiagonal);
    }

    @Override
    protected void transpose( @NotNull DMatrixRMaj V, DMatrixRMaj Vt ) {
        CommonOps_MT_DDRM.transpose(Vt, V);
    }

    @Override
    protected void declareBidiagonalDecomposition() {
        if (canUseTallBidiagonal && numRows > numCols*2 && !computeU) {
            if (bidiag == null || !(bidiag instanceof BidiagonalDecompositionTall_MT_DDRM)) {
                bidiag = new BidiagonalDecompositionTall_MT_DDRM();
            }
        } else if (bidiag == null || !(bidiag instanceof BidiagonalDecompositionRow_MT_DDRM)) {
            bidiag = new BidiagonalDecompositionRow_MT_DDRM();
        }
    }
}
