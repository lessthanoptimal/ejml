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

package org.ejml.dense.block.decomposition.qr;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixBlock_F64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.block.MatrixOps_B64;
import org.ejml.dense.row.CommonOps_R64;
import org.ejml.dense.row.MatrixFeatures_R64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.ejml.dense.row.decomposition.qr.QRDecompositionHouseholderTran_R64;
import org.ejml.generic.GenericMatrixOps_F64;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * Generic tests that test the compliance of an implementation of QRDecomposition(DMatrixBlock_F64).
 *
 * @author Peter Abeles
 */
public class GenericBlock64QrDecompositionTests_B64 {
    Random rand = new Random(324);

    int r = 3;

    QRDecompositionHouseholder_B64 alg;

    public GenericBlock64QrDecompositionTests_B64(QRDecompositionHouseholder_B64 alg) {
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
                DMatrixBlock_F64 A = MatrixOps_B64.createRandom(i,j,-1,1,rand,r);

                assertTrue(alg.decompose(A.copy()));

                DMatrixBlock_F64 Q = alg.getQ(null,false);

                DMatrixBlock_F64 B = MatrixOps_B64.createRandom(i,j,-1,1,rand,r);
                DMatrixBlock_F64 expected = new DMatrixBlock_F64(i,j,r);

                MatrixOps_B64.multTransA(Q,B,expected);
                alg.applyQTran(B);

                assertTrue(MatrixFeatures_R64.isIdentical(expected,B,UtilEjml.TEST_F64));
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
                DMatrixBlock_F64 A = MatrixOps_B64.createRandom(i,j,-1,1,rand,r);

                assertTrue(alg.decompose(A.copy()));

                DMatrixBlock_F64 Q = alg.getQ(null,false);

                DMatrixBlock_F64 B = MatrixOps_B64.createRandom(i,j,-1,1,rand,r);
                DMatrixBlock_F64 expected = new DMatrixBlock_F64(i,j,r);

                MatrixOps_B64.mult(Q,B,expected);
                alg.applyQ(B);

                assertTrue(MatrixFeatures_R64.isIdentical(expected,B,UtilEjml.TEST_F64));
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
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(numRows,numCols,-1,1,rand);
        DMatrixBlock_F64 Ab = MatrixOps_B64.convert(A,r);

        QRDecompositionHouseholderTran_R64 algCheck = new QRDecompositionHouseholderTran_R64();
        assertTrue(algCheck.decompose(A));

        assertTrue(alg.decompose(Ab));

        DMatrixRow_F64 expected = CommonOps_R64.transpose(algCheck.getQR(),null);
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
        DMatrixBlock_F64 A = MatrixOps_B64.createRandom(numRows,numCols,-1,1,rand,r);

        assertTrue(alg.decompose(A.copy()));

        DMatrixBlock_F64 Q = alg.getQ(null,compact);
        DMatrixBlock_F64 R = alg.getR(null,compact);

        DMatrixBlock_F64 found = new DMatrixBlock_F64(numRows,numCols,r);

        MatrixOps_B64.mult(Q,R,found);

        assertTrue(GenericMatrixOps_F64.isEquivalent(A,found,UtilEjml.TEST_F64));
    }
}
