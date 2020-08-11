/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRBlock;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.block.MatrixOps_FDRB;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.decomposition.qr.QRDecompositionHouseholderTran_FDRM;
import org.ejml.generic.GenericMatrixOps_F32;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Generic tests that test the compliance of an implementation of QRDecomposition(FMatrixRBlock).
 *
 * @author Peter Abeles
 */
public class GenericBlock64QrDecompositionTests_FDRB {
    Random rand = new Random(324);

    int r = 3;

    QRDecompositionHouseholder_FDRB alg;

    public GenericBlock64QrDecompositionTests_FDRB(QRDecompositionHouseholder_FDRB alg) {
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
                FMatrixRBlock A = MatrixOps_FDRB.createRandom(i,j,-1,1,rand,r);

                assertTrue(alg.decompose(A.copy()));

                FMatrixRBlock Q = alg.getQ(null,false);

                FMatrixRBlock B = MatrixOps_FDRB.createRandom(i,j,-1,1,rand,r);
                FMatrixRBlock expected = new FMatrixRBlock(i,j,r);

                MatrixOps_FDRB.multTransA(Q,B,expected);
                alg.applyQTran(B);

                assertTrue(MatrixFeatures_FDRM.isIdentical(expected,B,UtilEjml.TEST_F32));
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
                FMatrixRBlock A = MatrixOps_FDRB.createRandom(i,j,-1,1,rand,r);

                assertTrue(alg.decompose(A.copy()));

                FMatrixRBlock Q = alg.getQ(null,false);

                FMatrixRBlock B = MatrixOps_FDRB.createRandom(i,j,-1,1,rand,r);
                FMatrixRBlock expected = new FMatrixRBlock(i,j,r);

                MatrixOps_FDRB.mult(Q,B,expected);
                alg.applyQ(B);

                assertTrue(MatrixFeatures_FDRM.isIdentical(expected,B,UtilEjml.TEST_F32));
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
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(numRows,numCols,-1,1,rand);
        FMatrixRBlock Ab = MatrixOps_FDRB.convert(A,r);

        QRDecompositionHouseholderTran_FDRM algCheck = new QRDecompositionHouseholderTran_FDRM();
        assertTrue(algCheck.decompose(A));

        assertTrue(alg.decompose(Ab));

        FMatrixRMaj expected = CommonOps_FDRM.transpose(algCheck.getQR(),null);
//        expected.print();
//        Ab.print();

        EjmlUnitTests.assertEquals(expected,Ab, UtilEjml.TEST_F32);
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
        FMatrixRBlock A = MatrixOps_FDRB.createRandom(numRows,numCols,-1,1,rand,r);

        assertTrue(alg.decompose(A.copy()));

        FMatrixRBlock Q = alg.getQ(null,compact);
        FMatrixRBlock R = alg.getR(null,compact);

        FMatrixRBlock found = new FMatrixRBlock(numRows,numCols,r);

        MatrixOps_FDRB.mult(Q,R,found);

        assertTrue(GenericMatrixOps_F32.isEquivalent(A,found,UtilEjml.TEST_F32));
    }
}
