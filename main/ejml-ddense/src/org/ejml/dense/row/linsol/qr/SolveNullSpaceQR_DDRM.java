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

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.decomposition.qr.QRDecompositionHouseholderTran_DDRM;
import org.ejml.interfaces.SolveNullSpace;

/**
 * <p>Uses QR decomposition to find the null-space for a matrix of any shape if the number of
 * singular values is known. WARNING: This only uses the first several rows in the input matrix. The rest are
 * ignored.</p>
 *
 * Solves for A<sup>T</sup>=QR and the last column in Q is the null space.
 *
 * @author Peter Abeles
 */
public class SolveNullSpaceQR_DDRM implements SolveNullSpace<DMatrixRMaj> {
    CustomizedQR decomposition = new CustomizedQR();

    // Storage for Q matrix
    DMatrixRMaj Q = new DMatrixRMaj(1,1);

    /**
     * Finds the null space of A
     * @param A (Input) Matrix. Modified
     * @param numSingularValues Number of singular values
     * @param nullspace Storage for null-space
     * @return true if successful or false if it failed
     */
    @Override
    public boolean process(DMatrixRMaj A , int numSingularValues, DMatrixRMaj nullspace ) {
        decomposition.decompose(A);

        if( A.numRows > A.numCols ) {
            Q.reshape(A.numCols,Math.min(A.numRows,A.numCols));
            decomposition.getQ(Q, true);
        } else {
            Q.reshape(A.numCols, A.numCols);
            decomposition.getQ(Q, false);
        }

        nullspace.reshape(Q.numRows,numSingularValues);
        CommonOps_DDRM.extract(Q,0,Q.numRows,Q.numCols-numSingularValues,Q.numCols,nullspace,0,0);

        return true;
    }

    @Override
    public boolean inputModified() {
        return true;
    }

    /**
     * Special/Hack version of QR decomposition to avoid copying memory and pointless transposes
     */
    private static class CustomizedQR extends QRDecompositionHouseholderTran_DDRM {

        @Override
        public void setExpectedMaxSize( int numRows , int numCols ) {
            this.numCols = numCols;
            this.numRows = numRows;
            minLength = Math.min(numCols,numRows);
            int maxLength = Math.max(numCols,numRows);

            // Don't delcare QR. It will use the input matrix for worspace
            if( v == null ) {
                v = new double[ maxLength ];
                gammas = new double[ minLength ];
            }

            if( v.length < maxLength ) {
                v = new double[ maxLength ];
            }
            if( gammas.length < minLength ) {
                gammas = new double[ minLength ];
            }
        }

        /**
         * Modified decomposition which assumes the input is a transpose of the matrix
         */
        @Override
        public boolean decompose( DMatrixRMaj A_tran ) {
            // There is a "subtle" hack in the line below. Instead of passing in (cols,rows) I'm passing in
            // (cols,cols) that's because we don't care about updating everything past the cols
            setExpectedMaxSize(A_tran.numCols, Math.min(A_tran.numRows,A_tran.numCols));

            // use the input matrix for its workspace
            this.QR = A_tran;

            error = false;

            for( int j = 0; j < minLength; j++ ) {
                householder(j);
                updateA(j);
            }

            return !error;
        }

    }

    public DMatrixRMaj getQ() {
        return Q;
    }
}
