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

package org.ejml.dense.row.decomposition.bidiagonal;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.decomposition.qr.QrHelperFunctions_MT_DDRM;
import org.ejml.interfaces.decomposition.BidiagonalDecomposition_F64;

/**
 * <p>
 * Performs a {@link BidiagonalDecomposition_F64} using householder reflectors. This is efficient on wide or
 * square matrices.
 * </p>
 *
 * @author Peter Abeles
 */
public class BidiagonalDecompositionRow_MT_DDRM
        extends BidiagonalDecompositionRow_DDRM {
    @Override
    protected void rank1UpdateMultL( DMatrixRMaj A, double gamma, int colA0, int w0, int w1 ) {
        QrHelperFunctions_MT_DDRM.rank1UpdateMultL(A, u, gamma, colA0, w0, w1);
    }

    @Override
    protected void rank1UpdateMultR( DMatrixRMaj A, double gamma, int colA0, int w0, int w1 ) {
        QrHelperFunctions_MT_DDRM.rank1UpdateMultR(A, u, gamma, colA0, w0, w1, this.b);
    }
}
