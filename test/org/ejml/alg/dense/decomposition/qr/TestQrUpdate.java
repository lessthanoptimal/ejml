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

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.alg.dense.decomposition.QRDecomposition;
import org.ejml.alg.dense.mult.SubmatrixOps;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestQrUpdate {

    Random rand = new Random(0x345345);

    /**
     * Adds a row to a matrix at various points and updates the QR decomposition.
     * This is then checked by multiplying Q by R and seeing if the augmented A matrix
     * is the result
     */
    @Test
    public void testInsertRow() {
        int n = 3;

        for( int m = 3; m < 6; m++ ) {
            for( int insert = 0; insert < m; insert++ ) {
                checkInsert(m, n, insert);
            }
        }
    }

    @Test
    public void testRemoveRow() {
        int n = 3;

        for( int m = 4; m < 6; m++ ) {
            for( int remove = 0; remove < m; remove++ ) {
                checkRemove(m, n, remove);
            }
        }
    }

    private void checkRemove(int m, int n, int remove) {
        DenseMatrix64F A = RandomMatrices.createRandom(m,n,rand);
        DenseMatrix64F Q = RandomMatrices.createRandom(m,m,rand);
        DenseMatrix64F R = new DenseMatrix64F(m,n);

        // compute what the A matrix would look like without the row
        DenseMatrix64F A_e = RandomMatrices.createRandom(m-1,n,rand);
        SubmatrixOps.setSubMatrix(A,A_e,0,0,0,0,remove,n);
        SubmatrixOps.setSubMatrix(A,A_e,remove+1,0,remove,0,m-remove-1,n);

        QRDecomposition decomp = new QRDecompositionHouseholderColumn();

        // now compute the results by removing elements from A
        decomp.decompose(A);
        Q.reshape(m,m, false);
        decomp.getQ(Q,false);
        decomp.getR(R,false);

        QrUpdate update = new QrUpdate(m,n);

        update.deleteRow(Q,R,remove,true);

        assertTrue(MatrixFeatures.isOrthogonal(update.getU_tran(),1e-6));

        DenseMatrix64F A_r = RandomMatrices.createRandom(m-1,n,rand);
        CommonOps.mult(Q,R,A_r);


        // see if the augmented A matrix is correct extracted from the adjusted Q and R matrices
        assertTrue(MatrixFeatures.isIdentical(A_e,A_r,1e-6));
    }

    private void checkInsert(int m, int n, int insert) {
        DenseMatrix64F A = RandomMatrices.createRandom(m,n,rand);
        DenseMatrix64F Q = RandomMatrices.createRandom(m+1,m+1,rand);
        DenseMatrix64F R = new DenseMatrix64F(m+1,n);

        // the row that is to be inserted
        double row[] = new double[]{1,2,3};

        // create the modified A
        DenseMatrix64F A_e = RandomMatrices.createRandom(m+1,n,rand);
        SubmatrixOps.setSubMatrix(A,A_e,0,0,0,0,insert,n);
        System.arraycopy(row, 0, A_e.data, insert * n, n);
        SubmatrixOps.setSubMatrix(A,A_e,insert,0,insert+1,0,m-insert,n);

        QRDecomposition decomp = new QRDecompositionHouseholderColumn();

        decomp.decompose(A);
        Q.reshape(m,m, false);
        decomp.getQ(Q,false);
        R.reshape(m,n,false);
        decomp.getR(R,false);

        DenseMatrix64F Qmod = createQMod(Q,insert);

        QrUpdate update = new QrUpdate(m+1,n);

        update.addRow(Q,R,row,insert,true);

        DenseMatrix64F Z = new DenseMatrix64F(m+1,m+1);
        CommonOps.multTransB(Qmod,update.getU_tran(),Z);
        // see if the U matrix has the expected features
        assertTrue(MatrixFeatures.isOrthogonal(Z,1e-6));

        // see if the process that updates Q from U is valid
        assertTrue(MatrixFeatures.isIdentical(Q,Z,1e-6));

        DenseMatrix64F A_r = RandomMatrices.createRandom(m+1,n,rand);
        CommonOps.mult(Q,R,A_r);

        // see if the augmented A matrix is correct extracted from the adjusted Q and R matrices
        assertTrue(MatrixFeatures.isIdentical(A_e,A_r,1e-6));
    }

    public static DenseMatrix64F createQMod( DenseMatrix64F Q , int insertRow ) {
        DenseMatrix64F Qmod = new DenseMatrix64F(Q.numRows+1,Q.numCols+1);

        SubmatrixOps.setSubMatrix(Q,Qmod,0,0,0,1,insertRow,Q.numCols);
        Qmod.set(insertRow,0,1);
        SubmatrixOps.setSubMatrix(Q,Qmod,insertRow,0,insertRow+1,1,Q.numRows-insertRow,Q.numCols);

        return Qmod;
    }
}
