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

package org.ejml.dense.row.linsol.qr;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.NormOps_FDRM;
import org.ejml.dense.row.decomposition.qr.QRColPivDecompositionHouseholderColumn_FDRM;
import org.ejml.interfaces.SolveNullSpace;

/**
 * <p>Uses QR decomposition to find the null-space for a matrix of any shape if the number of
 * singular values is known.=</p>
 *
 * Solves for A<sup>T</sup>=QR and the last column in Q is the null space.
 *
 * @author Peter Abeles
 */
public class SolveNullSpaceQRP_FDRM implements SolveNullSpace<FMatrixRMaj> {
    CustomizedQRP decomposition = new CustomizedQRP();

    // Storage for Q matrix
    FMatrixRMaj Q = new FMatrixRMaj(1,1);

    /**
     * Finds the null space of A
     * @param A (Input) Matrix. Modified
     * @param numSingularValues Number of singular values
     * @param nullspace Storage for null-space
     * @return true if successful or false if it failed
     */
    public boolean process(FMatrixRMaj A , int numSingularValues, FMatrixRMaj nullspace ) {
        decomposition.decompose(A);

        if( A.numRows > A.numCols ) {
            Q.reshape(A.numCols,Math.min(A.numRows,A.numCols));
            decomposition.getQ(Q, true);
        } else {
            Q.reshape(A.numCols, A.numCols);
            decomposition.getQ(Q, false);
        }

        nullspace.reshape(Q.numRows,numSingularValues);
        CommonOps_FDRM.extract(Q,0,Q.numRows,Q.numCols-numSingularValues,Q.numCols,nullspace,0,0);

        return true;
    }

    private float check(FMatrixRMaj A, FMatrixRMaj nullspace ) {
        FMatrixRMaj r = new FMatrixRMaj(A.numRows,nullspace.numCols);
        CommonOps_FDRM.mult(A,nullspace,r);

        return NormOps_FDRM.normF(r);
    }

    @Override
    public boolean inputModified() {
        return decomposition.inputModified();
    }

    /**
     * Special/Hack version of QR decomposition to avoid copying memory and pointless transposes
     */
    private static class CustomizedQRP extends QRColPivDecompositionHouseholderColumn_FDRM {

        protected void convertToColumnMajor(FMatrixRMaj A) {
            for( int x = 0; x < numCols; x++ ) {
                System.arraycopy(A.data,x*A.numCols,dataQR[x],0,numRows);
            }
        }

        /**
         * Modified decomposition which assumes the input is a transpose of the matrix.
         * The decomposition needs to be applied to the transpose of A not A. This will do that adjustment
         * inplace
         */
        @Override
        public boolean decompose( FMatrixRMaj A ) {
            // Unlike the QR decomposition the entire matrix has to be considered because any of the columns
            // could be pivoted in
            setExpectedMaxSize(A.numCols,A.numRows);

            convertToColumnMajor(A);

            // initialize pivot variables
            setupPivotInfo();

            // go through each column and perform the decomposition
            for (int j = 0; j < minLength; j++) {
                if (j > 0)
                    updateNorms(j);
                swapColumns(j);
                // if its degenerate stop processing
                if (!householderPivot(j))
                    break;
                updateA(j);
                rank = j + 1;
            }

            return true;
        }

    }

    public FMatrixRMaj getQ() {
        return Q;
    }
}
