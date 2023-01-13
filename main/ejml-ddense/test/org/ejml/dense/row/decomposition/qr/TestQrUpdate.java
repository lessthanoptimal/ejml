/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decomposition.qr;

import org.ejml.EjmlStandardJUnit;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.mult.SubmatrixOps_DDRM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestQrUpdate extends EjmlStandardJUnit {
    /**
     * Adds a row to a matrix at various points and updates the QR decomposition.
     * This is then checked by multiplying Q by R and seeing if the augmented A matrix
     * is the result
     */
    @Test void testInsertRow() {
        int n = 3;

        for (int m = 3; m < 6; m++) {
            for (int insert = 0; insert < m; insert++) {
                checkInsert(m, n, insert);
            }
        }
    }

    @Test void testRemoveRow() {
        int n = 3;

        for (int m = 4; m < 6; m++) {
            for (int remove = 0; remove < m; remove++) {
                checkRemove(m, n, remove);
            }
        }
    }

    private void checkRemove( int m, int n, int remove ) {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(m, n, rand);
        DMatrixRMaj Q = RandomMatrices_DDRM.rectangle(m, m, rand);
        DMatrixRMaj R = new DMatrixRMaj(m, n);

        // compute what the A matrix would look like without the row
        DMatrixRMaj A_e = RandomMatrices_DDRM.rectangle(m - 1, n, rand);
        SubmatrixOps_DDRM.setSubMatrix(A, A_e, 0, 0, 0, 0, remove, n);
        SubmatrixOps_DDRM.setSubMatrix(A, A_e, remove + 1, 0, remove, 0, m - remove - 1, n);

        var decomp = new QRDecompositionHouseholderColumn_DDRM();

        // now compute the results by removing elements from A
        decomp.decompose(A);
        Q.reshape(m, m, false);
        decomp.getQ(Q, false);
        decomp.getR(R, false);

        QrUpdate_DDRM update = new QrUpdate_DDRM(m, n);

        update.deleteRow(Q, R, remove, true);

        assertTrue(MatrixFeatures_DDRM.isOrthogonal(update.getU_tran(), 1e-6));

        DMatrixRMaj A_r = RandomMatrices_DDRM.rectangle(m - 1, n, rand);
        CommonOps_DDRM.mult(Q, R, A_r);

        // see if the augmented A matrix is correct extracted from the adjusted Q and R matrices
        assertTrue(MatrixFeatures_DDRM.isIdentical(A_e, A_r, 1e-6));
    }

    private void checkInsert( int m, int n, int insert ) {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(m, n, rand);
        DMatrixRMaj Q = RandomMatrices_DDRM.rectangle(m + 1, m + 1, rand);
        DMatrixRMaj R = new DMatrixRMaj(m + 1, n);

        // the row that is to be inserted
        double row[] = new double[]{1, 2, 3};

        // create the modified A
        DMatrixRMaj A_e = RandomMatrices_DDRM.rectangle(m + 1, n, rand);
        SubmatrixOps_DDRM.setSubMatrix(A, A_e, 0, 0, 0, 0, insert, n);
        System.arraycopy(row, 0, A_e.data, insert*n, n);
        SubmatrixOps_DDRM.setSubMatrix(A, A_e, insert, 0, insert + 1, 0, m - insert, n);

        var decomp = new QRDecompositionHouseholderColumn_DDRM();

        decomp.decompose(A);
        Q.reshape(m, m, false);
        decomp.getQ(Q, false);
        R.reshape(m, n, false);
        decomp.getR(R, false);

        DMatrixRMaj Qmod = createQMod(Q, insert);

        QrUpdate_DDRM update = new QrUpdate_DDRM(m + 1, n);

        update.addRow(Q, R, row, insert, true);

        DMatrixRMaj Z = new DMatrixRMaj(m + 1, m + 1);
        CommonOps_DDRM.multTransB(Qmod, update.getU_tran(), Z);
        // see if the U matrix has the expected features
        assertTrue(MatrixFeatures_DDRM.isOrthogonal(Z, 1e-6));

        // see if the process that updates Q from U is valid
        assertTrue(MatrixFeatures_DDRM.isIdentical(Q, Z, 1e-6));

        DMatrixRMaj A_r = RandomMatrices_DDRM.rectangle(m + 1, n, rand);
        CommonOps_DDRM.mult(Q, R, A_r);

        // see if the augmented A matrix is correct extracted from the adjusted Q and R matrices
        assertTrue(MatrixFeatures_DDRM.isIdentical(A_e, A_r, 1e-6));
    }

    public static DMatrixRMaj createQMod( DMatrixRMaj Q, int insertRow ) {
        var Qmod = new DMatrixRMaj(Q.numRows + 1, Q.numCols + 1);

        SubmatrixOps_DDRM.setSubMatrix(Q, Qmod, 0, 0, 0, 1, insertRow, Q.numCols);
        Qmod.set(insertRow, 0, 1);
        SubmatrixOps_DDRM.setSubMatrix(Q, Qmod, insertRow, 0, insertRow + 1, 1, Q.numRows - insertRow, Q.numCols);

        return Qmod;
    }
}
