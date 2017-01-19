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

package org.ejml.dense.row.decomposition.svd;

import org.ejml.data.DMatrixRMaj;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;

/**
 * Wraps around a {@link SingularValueDecomposition} and ensures that the input is not modified.
 *
 * @author Peter Abeles
 */
public class SafeSvd_DDRM
        implements SingularValueDecomposition_F64<DMatrixRMaj>
{
    // the decomposition algorithm
    SingularValueDecomposition_F64<DMatrixRMaj> alg;
    // storage for the input if it would be modified
    DMatrixRMaj work = new DMatrixRMaj(1,1);

    public SafeSvd_DDRM(SingularValueDecomposition_F64<DMatrixRMaj> alg) {
        this.alg = alg;
    }

    @Override
    public double[] getSingularValues() {
        return alg.getSingularValues();
    }

    @Override
    public int numberOfSingularValues() {
        return alg.numberOfSingularValues();
    }

    @Override
    public boolean isCompact() {
        return alg.isCompact();
    }

    @Override
    public DMatrixRMaj getU(DMatrixRMaj U, boolean transposed) {
        return alg.getU(U,transposed);
    }

    @Override
    public DMatrixRMaj getV(DMatrixRMaj V, boolean transposed) {
        return alg.getV(V,transposed);
    }

    @Override
    public DMatrixRMaj getW(DMatrixRMaj W) {
        return alg.getW(W);
    }

    @Override
    public int numRows() {
        return alg.numRows();
    }

    @Override
    public int numCols() {
        return alg.numCols();
    }

    @Override
    public boolean decompose(DMatrixRMaj orig) {
        if( alg.inputModified() ) {
            work.reshape(orig.numRows,orig.numCols);
            work.set(orig);
            return alg.decompose(work);
        } else {
            return alg.decompose(orig);
        }
    }

    @Override
    public boolean inputModified() {
        return false;
    }
}
