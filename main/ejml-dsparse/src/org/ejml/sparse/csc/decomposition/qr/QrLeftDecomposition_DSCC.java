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

package org.ejml.sparse.csc.decomposition.qr;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.interfaces.decomposition.QRPDecomposition_F64;
import org.ejml.sparse.DecompositionSparseInterface;
import org.ejml.sparse.FillReducing;

/**
 * Left-looking QR decomposition algorithm for sparse matrices.
 *
 * <p>NOTE: See qr_left on page 71 and cs_qr() in csparse </p>
 *
 * @author Peter Abeles
 */
public class QrLeftDecomposition_DSCC implements
        QRPDecomposition_F64<DMatrixSparseCSC>, // TODO create a sparse cholesky interface?
        DecompositionSparseInterface<DMatrixSparseCSC>
{
    int m,n;
    FillReducing permutation;

    QrStructuralCounts_DSCC structure = new QrStructuralCounts_DSCC();
    IGrowArray gwork = new IGrowArray();

    public QrLeftDecomposition_DSCC( FillReducing permutation ) {
        this.permutation = permutation;

        // use the same work space to reduce the overall memory foot print
        this.structure.setGwork(gwork);
    }

    @Override
    public boolean decompose(DMatrixSparseCSC A) {
        structure.process(A);

        init(A);
        int w[] = gwork.data;

        return false;
    }

    private void init( DMatrixSparseCSC A ) {
        this.m = A.numRows;
        this.n = A.numCols;
    }

    @Override
    public void setSingularThreshold(double threshold) {

    }

    @Override
    public int getRank() {
        return 0;
    }

    @Override
    public int[] getPivots() {
        return new int[0];
    }

    @Override
    public DMatrixSparseCSC getPivotMatrix(DMatrixSparseCSC P) {
        return null;
    }

    @Override
    public DMatrixSparseCSC getQ(DMatrixSparseCSC Q, boolean compact) {
        return null;
    }

    @Override
    public DMatrixSparseCSC getR(DMatrixSparseCSC R, boolean compact) {
        return null;
    }

    @Override
    public boolean inputModified() {
        return false;
    }
}
