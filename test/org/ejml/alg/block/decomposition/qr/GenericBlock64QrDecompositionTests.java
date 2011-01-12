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

package org.ejml.alg.block.decomposition.qr;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderTran;
import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * Generic tests that test the compliance of an implementation of QRDecomposition(BlockMatrix64F).
 *
 * @author Peter Abeles
 */
public class GenericBlock64QrDecompositionTests {
    Random rand = new Random(324);

    int r = 3;

    BlockMatrix64HouseholderQR alg;

    public GenericBlock64QrDecompositionTests(BlockMatrix64HouseholderQR alg) {
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
                BlockMatrix64F A = BlockMatrixOps.createRandom(i,j,-1,1,rand,r);

                assertTrue(alg.decompose(A.copy()));

                BlockMatrix64F Q = alg.getQ(null,false);

                BlockMatrix64F B = BlockMatrixOps.createRandom(i,j,-1,1,rand,r);
                BlockMatrix64F expected = new BlockMatrix64F(i,j,r);

                BlockMatrixOps.multTransA(Q,B,expected);
                alg.applyQTran(B);

                assertTrue(MatrixFeatures.isIdentical(expected,B,1e-8));
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
                BlockMatrix64F A = BlockMatrixOps.createRandom(i,j,-1,1,rand,r);

                assertTrue(alg.decompose(A.copy()));

                BlockMatrix64F Q = alg.getQ(null,false);

                BlockMatrix64F B = BlockMatrixOps.createRandom(i,j,-1,1,rand,r);
                BlockMatrix64F expected = new BlockMatrix64F(i,j,r);

                BlockMatrixOps.mult(Q,B,expected);
                alg.applyQ(B);

                assertTrue(MatrixFeatures.isIdentical(expected,B,1e-8));
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
        DenseMatrix64F A = RandomMatrices.createRandom(numRows,numCols,-1,1,rand);
        BlockMatrix64F Ab = BlockMatrixOps.convert(A,r);

        QRDecompositionHouseholderTran algCheck = new QRDecompositionHouseholderTran();
        assertTrue(algCheck.decompose(A));

        assertTrue(alg.decompose(Ab));

        DenseMatrix64F expected = CommonOps.transpose(algCheck.getQR(),null);
//        expected.print();
//        Ab.print();

        assertTrue(GenericMatrixOps.isEquivalent(expected,Ab,1e-8));
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
        BlockMatrix64F A = BlockMatrixOps.createRandom(numRows,numCols,-1,1,rand,r);

        assertTrue(alg.decompose(A.copy()));

        BlockMatrix64F Q = alg.getQ(null,compact);
        BlockMatrix64F R = alg.getR(null,compact);

        BlockMatrix64F found = new BlockMatrix64F(numRows,numCols,r);

        BlockMatrixOps.mult(Q,R,found);

        assertTrue(GenericMatrixOps.isEquivalent(A,found,1e-8));
    }
}
