/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.misc.UnrolledInverseFromMinor_D64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps_D64;
import org.ejml.ops.MatrixFeatures_D64;
import org.ejml.ops.RandomMatrices_D64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestTriangularSolver_D64 {

    Random rand = new Random(0xff);


    @Test
    public void invert_inplace() {
        DenseMatrix64F L = createRandomLowerTriangular();

        DenseMatrix64F L_inv = L.copy();

        TriangularSolver_D64.invertLower(L_inv.data,L.numRows);

        DenseMatrix64F I = new DenseMatrix64F(L.numRows,L.numCols);

        CommonOps_D64.mult(L,L_inv,I);

        assertTrue(MatrixFeatures_D64.isIdentity(I,UtilEjml.TEST_64F));
    }

    @Test
    public void invert() {
        DenseMatrix64F L = createRandomLowerTriangular();

        DenseMatrix64F L_inv = L.copy();

        TriangularSolver_D64.invertLower(L.data,L_inv.data,L.numRows);

        DenseMatrix64F I = new DenseMatrix64F(L.numRows,L.numCols);

        CommonOps_D64.mult(L,L_inv,I);

        assertTrue(MatrixFeatures_D64.isIdentity(I, UtilEjml.TEST_64F));
    }

    @Test
    public void solveL_vector() {
        DenseMatrix64F L = createRandomLowerTriangular();

        DenseMatrix64F L_inv = L.copy();
        UnrolledInverseFromMinor_D64.inv(L_inv,L_inv);

        DenseMatrix64F B = RandomMatrices_D64.createRandom(3,1,rand);
        DenseMatrix64F expected = RandomMatrices_D64.createRandom(3,1,rand);
        DenseMatrix64F found = B.copy();

        TriangularSolver_D64.solveL(L.data,found.data,3);
        CommonOps_D64.mult(L_inv,B,expected);


        assertTrue(MatrixFeatures_D64.isIdentical(expected,found,UtilEjml.TEST_64F));
    }

    private DenseMatrix64F createRandomLowerTriangular() {
        DenseMatrix64F L = RandomMatrices_D64.createRandom(3,3,rand);
        for( int i = 0; i < L.numRows; i++ ) {
            for( int j = i+1; j < L.numCols; j++ ) {
                L.set(i,j,0);
            }
        }
        return L;
    }

    @Test
    public void solveL_matrix() {
        DenseMatrix64F L = createRandomLowerTriangular();

        DenseMatrix64F L_inv = L.copy();
        UnrolledInverseFromMinor_D64.inv(L_inv,L_inv);

        DenseMatrix64F B = RandomMatrices_D64.createRandom(3,4,rand);
        DenseMatrix64F expected = RandomMatrices_D64.createRandom(3,4,rand);
        DenseMatrix64F found = B.copy();

        TriangularSolver_D64.solveL(L.data,found.data,3,4);
        CommonOps_D64.mult(L_inv,B,expected);

        assertTrue(MatrixFeatures_D64.isIdentical(expected,found,UtilEjml.TEST_64F));
    }

    @Test
    public void solveTranL() {
        DenseMatrix64F L = createRandomLowerTriangular();

        DenseMatrix64F B = RandomMatrices_D64.createRandom(3,1,rand);
        DenseMatrix64F expected = RandomMatrices_D64.createRandom(3,1,rand);
        DenseMatrix64F found = B.copy();

        TriangularSolver_D64.solveTranL(L.data,found.data,3);

        CommonOps_D64.transpose(L);
        DenseMatrix64F L_inv = L.copy();
        UnrolledInverseFromMinor_D64.inv(L_inv,L_inv);
        CommonOps_D64.mult(L_inv,B,expected);

        assertTrue(MatrixFeatures_D64.isIdentical(expected,found,UtilEjml.TEST_64F));
    }

    @Test
    public void solveU() {
        DenseMatrix64F U = RandomMatrices_D64.createRandom(3,3,rand);
        for( int i = 0; i < U.numRows; i++ ) {
            for( int j = 0; j < i; j++ ) {
                U.set(i,j,0);
            }
        }

        DenseMatrix64F U_inv = U.copy();
        UnrolledInverseFromMinor_D64.inv(U_inv,U_inv);

        DenseMatrix64F B = RandomMatrices_D64.createRandom(3,1,rand);
        DenseMatrix64F expected = RandomMatrices_D64.createRandom(3,1,rand);
        DenseMatrix64F found = B.copy();

        TriangularSolver_D64.solveU(U.data,found.data,3);
        CommonOps_D64.mult(U_inv,B,expected);

        assertTrue(MatrixFeatures_D64.isIdentical(expected,found,UtilEjml.TEST_64F));
    }

    @Test
    public void solveU_submatrix() {

        // create U and B.  Insert into a larger matrix
        DenseMatrix64F U_orig = RandomMatrices_D64.createRandom(3,3,rand);
        for( int i = 0; i < U_orig.numRows; i++ ) {
            for( int j = 0; j < i; j++ ) {
                U_orig.set(i,j,0);
            }
        }
        DenseMatrix64F U = new DenseMatrix64F(6,7);
        CommonOps_D64.insert(U_orig,U,2,3);
        
        
        DenseMatrix64F B_orig = RandomMatrices_D64.createRandom(3,2,rand);

        DenseMatrix64F B = new DenseMatrix64F(4,5);
        CommonOps_D64.insert(B_orig,B,1,2);
        
        // compute expected solution
        DenseMatrix64F U_inv = U_orig.copy();
        UnrolledInverseFromMinor_D64.inv(U_inv,U_inv);

        DenseMatrix64F expected = RandomMatrices_D64.createRandom(3,2,rand);

        int startU = 2*U.numCols+3;
        int strideU = U.numCols;
        int widthU = U_orig.numCols;
        int startB = 1*B.numCols+2;
        int strideB = B.numCols;
        int widthB = B_orig.numCols;
        TriangularSolver_D64.solveU(U.data,startU,strideU,widthU,B.data,startB,strideB,widthB);

        DenseMatrix64F found = CommonOps_D64.extract(B,1,4,2,4);
        CommonOps_D64.mult(U_inv,B_orig,expected);

        assertTrue(MatrixFeatures_D64.isIdentical(expected,found,UtilEjml.TEST_64F));
    }
}
