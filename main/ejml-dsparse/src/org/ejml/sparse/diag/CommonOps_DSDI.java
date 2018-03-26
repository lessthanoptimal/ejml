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

package org.ejml.sparse.diag;

import org.ejml.data.DMatrixDiag;
import org.ejml.data.DMatrixRMaj;

/**
 * Common matrix operations for {@link org.ejml.data.DMatrixDiag}
 *
 * @author Peter Abeles
 */
public class CommonOps_DSDI {
    /**
     * C = A*B
     */
    public void mult(DMatrixDiag A , DMatrixDiag B , DMatrixDiag C) {
        if( A.numCols != B.numRows )
            throw new IllegalArgumentException("A and B do not have compatible dimensions");
        int length = A.length();
        if( length != B.length() )
            throw new IllegalArgumentException("Number of diagonal elements in A and B do not match");

        C.reshape(A.numRows,B.numCols);
        for (int i = 0; i < length; i++) {
            C.data[i] = A.data[i]*B.data[i];
        }
    }

    /**
     * C = A*B
     */
    public void mult(DMatrixRMaj A , DMatrixDiag B , DMatrixRMaj C) {
        if( A.numCols != B.numRows )
            throw new IllegalArgumentException("A and B do not have compatible dimensions");
        C.reshape(A.numRows,B.numCols);

        int lengthB = B.length();

        int indexC = 0;
        for (int row = 0; row < C.numRows; row++) {
            int indexA = row*A.numCols;
            for (int col = 0; col < C.numCols; col++, indexC++ ) {
                C.data[indexC] = A.data[indexA+col];

            }
        }
    }

    /**
     * C = A*B
     */
    public void mult(DMatrixDiag A , DMatrixRMaj B , DMatrixRMaj C) {

    }

    /**
     * C = A<sup>T</sup>*B
     */
    public void multTransA(DMatrixRMaj A , DMatrixDiag B , DMatrixRMaj C) {

    }

    /**
     * C = A*B<sup>T</sup>
     */
    public void multTransB(DMatrixDiag A , DMatrixRMaj B , DMatrixRMaj C) {

    }

}
