/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.linsol.lu;

import org.ejml.alg.dense.decomposition.lu.LUDecompositionBase;
import org.ejml.data.DenseMatrix64F;


/**
 * For each column in the B matrix it makes a copy, which is then solved for and
 * writen into X.  By making a copy of the column cpu cache issues are reduced.
 *
 * @author Peter Abeles
 */
public class LinearSolverLu extends LinearSolverLuBase {

    boolean doImprove = false;

    public LinearSolverLu( LUDecompositionBase decomp ) {
        super(decomp);
    }

    public LinearSolverLu( LUDecompositionBase decomp , boolean doImprove ) {
        super(decomp);
        this.doImprove = doImprove;
    }


    @Override
    public void solve(DenseMatrix64F b, DenseMatrix64F x) {
        if( b.numCols != x.numCols && b.numRows != numCols && x.numRows != numCols) {
            throw new IllegalArgumentException("Unexpected matrix size");
        }

        int numCols = b.numCols;


        DenseMatrix64F vv = decomp._getVV();

//        for( int j = 0; j < numCols; j++ ) {
//            for( int i = 0; i < this.numCols; i++ ) vv[i] = dataB[i*numCols+j];
//            decomp._solveVectorInternal(vv);
//            for( int i = 0; i < this.numCols; i++ ) dataX[i*numCols+j] = vv[i];
//        }
        for( int j = 0; j < numCols; j++ ) {
            int index = j;
            for( int i = 0; i < this.numCols; i++ , index += numCols ) vv.set(i , b.get(index));
            decomp._solveVectorInternal(vv);
            index = j;
            for( int i = 0; i < this.numCols; i++ , index += numCols ) x.set(index , vv.get(i) );
        }

        if( doImprove ) {
            improveSol(b,x);
        }
    }
}
