/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.decomposition.lu.LUDecompositionBase_FDRM;


/**
 * For each column in the B matrix it makes a copy, which is then solved for and
 * writen into X.  By making a copy of the column cpu cache issues are reduced.
 *
 * @author Peter Abeles
 */
public class LinearSolverLu_FDRM extends LinearSolverLuBase_FDRM {

    boolean doImprove = false;

    public LinearSolverLu_FDRM(LUDecompositionBase_FDRM decomp) {
        super(decomp);
    }

    public LinearSolverLu_FDRM(LUDecompositionBase_FDRM decomp, boolean doImprove) {
        super(decomp);
        this.doImprove = doImprove;
    }


    @Override
    public void solve(FMatrixRMaj B, FMatrixRMaj X) {
        if( B.numRows != numRows )
            throw new IllegalArgumentException("Unexpected dimensions for X: X rows = "+X.numRows+" expected = "+numRows);
        X.reshape(numCols,B.numCols);

        int numCols = B.numCols;

        float dataB[] = B.data;
        float dataX[] = X.data;

        float []vv = decomp._getVV();

//        for( int j = 0; j < numCols; j++ ) {
//            for( int i = 0; i < this.numCols; i++ ) vv[i] = dataB[i*numCols+j];
//            decomp._solveVectorInternal(vv);
//            for( int i = 0; i < this.numCols; i++ ) dataX[i*numCols+j] = vv[i];
//        }
        for( int j = 0; j < numCols; j++ ) {
            int index = j;
            for( int i = 0; i < this.numCols; i++ , index += numCols ) vv[i] = dataB[index];
            decomp._solveVectorInternal(vv);
            index = j;
            for( int i = 0; i < this.numCols; i++ , index += numCols ) dataX[index] = vv[i];
        }

        if( doImprove ) {
            improveSol(B,X);
        }
    }
}
