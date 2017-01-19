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

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.decomposition.qr.QRDecompositionHouseholderColumn_DDRM;
import org.ejml.dense.row.decomposition.qr.QrUpdate_DDRM;
import org.ejml.dense.row.linsol.AdjustableLinearSolver_DDRM;


/**
 * A solver for QR decomposition that can efficiently modify the previous decomposition when
 * data is added or removed.
 *
 * @author Peter Abeles
 */
public class AdjLinearSolverQr_DDRM extends LinearSolverQr_DDRM implements AdjustableLinearSolver_DDRM {

    private QrUpdate_DDRM update;

    private DMatrixRMaj A;

    public AdjLinearSolverQr_DDRM() {
        super( new QRDecompositionHouseholderColumn_DDRM() );
    }

    @Override
    public void setMaxSize( int maxRows , int maxCols ) {
        // allow it some room to grow
        maxRows += 5;

        super.setMaxSize(maxRows,maxCols);

        update = new QrUpdate_DDRM(maxRows,maxCols,true);
        A = new DMatrixRMaj(maxRows,maxCols);
    }

    /**
     * Compute the A matrix from the Q and R matrices.
     *
     * @return The A matrix.
     */
    @Override
    public DMatrixRMaj getA() {
        if( A.data.length < numRows*numCols ) {
            A = new DMatrixRMaj(numRows,numCols);
        }
        A.reshape(numRows,numCols, false);
        CommonOps_DDRM.mult(Q,R,A);

        return A;
    }

    @Override
    public boolean addRowToA(double[] A_row , int rowIndex ) {
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
