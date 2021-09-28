/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.block.decomposition.qr;

import org.ejml.EjmlStandardJUnit;
import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.qr.QRDecompositionHouseholderTran_DDRM;
import org.ejml.generic.GenericMatrixOps_F64;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Generic tests that test the compliance of an implementation of QRDecomposition(DMatrixRBlock).
 *
 * @author Peter Abeles
 */
public class GenericBlock64QrDecompositionTests_DDRB extends EjmlStandardJUnit {
    int r = 3;

    QRDecompositionHouseholder_DDRB alg;

    public GenericBlock64QrDecompositionTests_DDRB(QRDecompositionHouseholder_DDRB alg) {
        this.alg = alg;
    }

    /**
     * Runs all the tests.
     */
    public void allTests() {
        applyQ();
        applyQTran();
        checkInternalData();
        fullDecomposition();
    }

    /**
     * Test applyQTran() by explicitly computing Q and compare the results of multiplying
     * a matrix by Q<sup>T</sup> and applying Q to it.
     */
    public void applyQTran() {
        for( int i = 1; i <= 3*r; i++ ) {
            for( int j = 1; j <= 3*r; j++ ) {
                DMatrixRBlock A = MatrixOps_DDRB.createRandom(i,j,-1,1,rand,r);

                assertTrue(alg.decompose(A.copy()));

                DMatrixRBlock Q = alg.getQ(null,false);

                DMatrixRBlock B = MatrixOps_DDRB.createRandom(i,j,-1,1,rand,r);
                DMatrixRBlock expected = new DMatrixRBlock(i,j,r);

                MatrixOps_DDRB.multTransA(Q,B,expected);
                alg.applyQTran(B);

                assertTrue(MatrixFeatures_DDRM.isIdentical(expected,B,UtilEjml.TEST_F64));
            }
        }
    }

    /**
     * Test applyQ() by explicitly computing Q and compare the results of multiplying
     * a matrix by Q and applying Q to it.
     */
    public void applyQ() {
        for( int i = 1; i <= 3*r; i++ ) {
            for( int j = 1; j <= 3*r; j++ ) {
                DMatrixRBlock A = MatrixOps_DDRB.createRandom(i,j,-1,1,rand,r);

                assertTrue(alg.decompose(A.copy()));

                DMatrixRBlock Q = alg.getQ(null,false);

                DMatrixRBlock B = MatrixOps_DDRB.createRandom(i,j,-1,1,rand,r);
                DMatrixRBlock expected = new DMatrixRBlock(i,j,r);

                MatrixOps_DDRB.mult(Q,B,expected);
                alg.applyQ(B);

                assertTrue(MatrixFeatures_DDRM.isIdentical(expected,B,UtilEjml.TEST_F64));
            }
        }
    }

    /**
     * Decomposes the matrix and checks the internal data structure for correctness.
     */
    public void checkInternalData() {
        for( int i = 1; i <= 3*r; i++ ) {
            for( int j = 1; j <= 3*r; j++ ) {
//                System.out.println("i = "+i+" j = "+j);
                checkSize(i,j);
            }
        }
    }

    private void checkSize( int numRows , int numCols ) {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(numRows,numCols,-1,1,rand);
        DMatrixRBlock Ab = MatrixOps_DDRB.convert(A,r);

        QRDecompositionHouseholderTran_DDRM algCheck = new QRDecompositionHouseholderTran_DDRM();
        assertTrue(algCheck.decompose(A));

        assertTrue(alg.decompose(Ab));

        DMatrixRMaj expected = CommonOps_DDRM.transpose(algCheck.getQR(),null);
//        expected.print();
//        Ab.print();

        EjmlUnitTests.assertEquals(expected,Ab, UtilEjml.TEST_F64);
    }

    /**
     * Decomposes the matrix and computes Q and R.  Verifies the results by
     * multiplying Q and R together and seeing if it gets A.
     */
    public void fullDecomposition() {
        for( int i = 1; i <= 3*r; i++ ) {
            for( int j = 1; j <= 3*r; j++ ) {
//                i=4;j=4;
//                System.out.println("i = "+i+" j = "+j);
                checkFullDecomposition(i,j,true);
                checkFullDecomposition(i,j,false);
            }
        }
    }

    private void checkFullDecomposition( int numRows , int numCols , boolean compact ) {
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(numRows,numCols,-1,1,rand,r);

        assertTrue(alg.decompose(A.copy()));

        DMatrixRBlock Q = alg.getQ(null,compact);
        DMatrixRBlock R = alg.getR(null,compact);

        DMatrixRBlock found = new DMatrixRBlock(numRows,numCols,r);

        MatrixOps_DDRB.mult(Q,R,found);

        assertTrue(GenericMatrixOps_F64.isEquivalent(A,found,UtilEjml.TEST_F64));
    }
}
