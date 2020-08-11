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

package org.ejml.dense.row.linsol.lu;

import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.decompose.lu.LUDecompositionBase_CDRM;
import org.ejml.dense.row.linsol.LinearSolverAbstract_CDRM;

import java.util.Arrays;


/**
 * @author Peter Abeles
 */
public abstract class LinearSolverLuBase_CDRM extends LinearSolverAbstract_CDRM {

    protected LUDecompositionBase_CDRM decomp;

    public LinearSolverLuBase_CDRM(LUDecompositionBase_CDRM decomp) {
        this.decomp = decomp;

    }

    @Override
    public boolean setA(CMatrixRMaj A) {
        _setA(A);

        return decomp.decompose(A);
    }

    @Override
    public /**/double quality() {
        return decomp.quality();
    }

    @Override
    public void invert(CMatrixRMaj A_inv) {
        float []vv = decomp._getVV();
        CMatrixRMaj LU = decomp.getLU();

        if( A_inv.numCols != LU.numCols || A_inv.numRows != LU.numRows )
            throw new IllegalArgumentException("Unexpected matrix dimension");

        int n = A.numCols;

        float dataInv[] = A_inv.data;
        int strideAinv = A_inv.getRowStride();

        for( int j = 0; j < n; j++ ) {
            // don't need to change inv into an identity matrix before hand
            Arrays.fill(vv,0,n*2,0);
            vv[j*2] = 1;
            vv[j*2+1] = 0;

            decomp._solveVectorInternal(vv);
//            for( int i = 0; i < n; i++ ) dataInv[i* n +j] = vv[i];
            int index = j*2;
            for( int i = 0; i < n; i++ , index += strideAinv) {
                dataInv[index] = vv[i*2];
                dataInv[index+1] = vv[i*2+1];
            }
        }
    }

    @Override
    public boolean modifiesA() {
        return false;
    }

    @Override
    public boolean modifiesB() {
        return false;
    }

    @Override
    public LUDecompositionBase_CDRM getDecomposition() {
        return decomp;
    }
}