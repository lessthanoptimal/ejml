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


/**
 * For each column in the B matrix it makes a copy, which is then solved for and
 * writen into X.  By making a copy of the column cpu cache issues are reduced.
 *
 * @author Peter Abeles
 */
public class LinearSolverLu_CDRM extends LinearSolverLuBase_CDRM {

    public LinearSolverLu_CDRM(LUDecompositionBase_CDRM decomp) {
        super(decomp);
    }



    @Override
    public void solve(CMatrixRMaj b, CMatrixRMaj x) {
        if( b.numCols != x.numCols || b.numRows != numRows || x.numRows != numCols) {
            throw new IllegalArgumentException("Unexpected matrix size");
        }

        int bnumCols = b.numCols;
        int bstride = b.getRowStride();

        float dataB[] = b.data;
        float dataX[] = x.data;

        float []vv = decomp._getVV();

//        for( int j = 0; j < numCols; j++ ) {
//            for( int i = 0; i < this.numCols; i++ ) vv[i] = dataB[i*numCols+j];
//            decomp._solveVectorInternal(vv);
//            for( int i = 0; i < this.numCols; i++ ) dataX[i*numCols+j] = vv[i];
//        }
        for( int j = 0; j < bnumCols; j++ ) {
            int index = j*2;
            for( int i = 0; i < numRows; i++ , index += bstride ) {
                vv[i*2]   = dataB[index];
                vv[i*2+1] = dataB[index+1];
            }
            decomp._solveVectorInternal(vv);
            index = j*2;
            for( int i = 0; i < numRows; i++ , index += bstride ) {
                dataX[index]   = vv[i*2];
                dataX[index+1] = vv[i*2+1];
            }
        }

    }
}
