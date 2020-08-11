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

package org.ejml.dense.row.linsol.qr;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.decomposition.qr.QRDecompositionHouseholderColumn_FDRM;
import org.ejml.dense.row.decomposition.qr.QrUpdate_FDRM;
import org.ejml.dense.row.linsol.AdjustableLinearSolver_FDRM;


/**
 * A solver for QR decomposition that can efficiently modify the previous decomposition when
 * data is added or removed.
 *
 * @author Peter Abeles
 */
public class AdjLinearSolverQr_FDRM extends LinearSolverQr_FDRM implements AdjustableLinearSolver_FDRM {

    private QrUpdate_FDRM update;

    private FMatrixRMaj A;

    public AdjLinearSolverQr_FDRM() {
        super( new QRDecompositionHouseholderColumn_FDRM() );
    }

    @Override
    public void setMaxSize( int maxRows , int maxCols ) {
        // allow it some room to grow
        maxRows += 5;

        super.setMaxSize(maxRows,maxCols);

        update = new QrUpdate_FDRM(maxRows,maxCols,true);
        A = new FMatrixRMaj(maxRows,maxCols);
    }

    /**
     * Compute the A matrix from the Q and R matrices.
     *
     * @return The A matrix.
     */
    @Override
    public FMatrixRMaj getA() {
        if( A.data.length < numRows*numCols ) {
            A = new FMatrixRMaj(numRows,numCols);
        }
        A.reshape(numRows,numCols, false);
        CommonOps_FDRM.mult(Q,R,A);

        return A;
    }

    @Override
    public boolean addRowToA(float[] A_row , int rowIndex ) {
        // see if it needs to grow the data structures
        if( numRows + 1 > maxRows) {
            // grow by 10%
            int grow = maxRows / 10;
            if( grow < 1 ) grow = 1;
            maxRows = numRows + grow;
            Q.reshape(maxRows,maxRows,true);
            R.reshape(maxRows,maxCols,true);
        }

        update.addRow(Q,R,A_row,rowIndex,true);
        numRows++;

        return true;
    }

    @Override
    public boolean removeRowFromA(int index) {
        update.deleteRow(Q,R,index,true);
        numRows--;
        return true;
    }

}
