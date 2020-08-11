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

import org.ejml.data.FMatrixRMaj;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F32;

/**
 * Wraps around a {@link SingularValueDecomposition} and ensures that the input is not modified.
 *
 * @author Peter Abeles
 */
public class SafeSvd_FDRM
        implements SingularValueDecomposition_F32<FMatrixRMaj>
{
    // the decomposition algorithm
    SingularValueDecomposition_F32<FMatrixRMaj> alg;
    // storage for the input if it would be modified
    FMatrixRMaj work = new FMatrixRMaj(1,1);

    public SafeSvd_FDRM(SingularValueDecomposition_F32<FMatrixRMaj> alg) {
        this.alg = alg;
    }

    @Override
    public float[] getSingularValues() {
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
    public FMatrixRMaj getU(FMatrixRMaj U, boolean transposed) {
        return alg.getU(U,transposed);
    }

    @Override
    public FMatrixRMaj getV(FMatrixRMaj V, boolean transposed) {
        return alg.getV(V,transposed);
    }

    @Override
    public FMatrixRMaj getW(FMatrixRMaj W) {
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
    public boolean decompose(FMatrixRMaj orig) {
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
