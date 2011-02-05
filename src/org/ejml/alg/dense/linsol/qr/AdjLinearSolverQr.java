/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.dense.linsol.qr;

import org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderColumn;
import org.ejml.alg.dense.decomposition.qr.QrUpdate;
import org.ejml.alg.dense.linsol.AdjustableLinearSolver;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;


/**
 * A solver for QR decomposition that can efficiently modify the previous decomposition when
 * data is added or removed.
 *
 * @author Peter Abeles
 */
public class AdjLinearSolverQr extends LinearSolverQr implements AdjustableLinearSolver {

    private QrUpdate update;

    private DenseMatrix64F A;

    public AdjLinearSolverQr() {
        super( new QRDecompositionHouseholderColumn() );
    }

    @Override
    public void setMaxSize( int maxRows , int maxCols ) {
        // allow it some room to grow
        maxRows += 5;

        super.setMaxSize(maxRows,maxCols);

        update = new QrUpdate(maxRows,maxCols,true);
        A = new DenseMatrix64F(maxRows,maxCols);
    }

    /**
     * Compute the A matrix from the Q and R matrices.
     *
     * @return The A matrix.
     */
    @Override
    public DenseMatrix64F getA() {
        if( A.data.length < numRows*numCols ) {
            A = new DenseMatrix64F(numRows,numCols);
        }
        A.reshape(numRows,numCols, false);
        CommonOps.mult(Q,R,A);

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
