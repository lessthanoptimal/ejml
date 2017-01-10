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

package org.ejml.alg.dense.decomposition;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.misc.UnrolledInverseFromMinor_R64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.MatrixFeatures_R64;
import org.ejml.ops.RandomMatrices_R64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestTriangularSolver_R64 {

    Random rand = new Random(0xff);


    @Test
    public void invert_inplace() {
        DMatrixRow_F64 L = createRandomLowerTriangular();

        DMatrixRow_F64 L_inv = L.copy();

        TriangularSolver_R64.invertLower(L_inv.data,L.numRows);

        DMatrixRow_F64 I = new DMatrixRow_F64(L.numRows,L.numCols);

        CommonOps_R64.mult(L,L_inv,I);

        assertTrue(MatrixFeatures_R64.isIdentity(I,UtilEjml.TEST_F64));
    }

    @Test
    public void invert() {
        DMatrixRow_F64 L = createRandomLowerTriangular();

        DMatrixRow_F64 L_inv = L.copy();

        TriangularSolver_R64.invertLower(L.data,L_inv.data,L.numRows);

        DMatrixRow_F64 I = new DMatrixRow_F64(L.numRows,L.numCols);

        CommonOps_R64.mult(L,L_inv,I);

        assertTrue(MatrixFeatures_R64.isIdentity(I, UtilEjml.TEST_F64));
    }

    @Test
    public void solveL_vector() {
        DMatrixRow_F64 L = createRandomLowerTriangular();

        DMatrixRow_F64 L_inv = L.copy();
        UnrolledInverseFromMinor_R64.inv(L_inv,L_inv);

        DMatrixRow_F64 B = RandomMatrices_R64.createRandom(3,1,rand);
        DMatrixRow_F64 expected = RandomMatrices_R64.createRandom(3,1,rand);
        DMatrixRow_F64 found = B.copy();

        TriangularSolver_R64.solveL(L.data,found.data,3);
        CommonOps_R64.mult(L_inv,B,expected);


        assertTrue(MatrixFeatures_R64.isIdentical(expected,found,UtilEjml.TEST_F64));
    }

    private DMatrixRow_F64 createRandomLowerTriangular() {
        DMatrixRow_F64 L = RandomMatrices_R64.createRandom(3,3,rand);
        for( int i = 0; i < L.numRows; i++ ) {
            for( int j = i+1; j < L.numCols; j++ ) {
                L.set(i,j,0);
            }
        }
        return L;
    }

    @Test
    public void solveL_matrix() {
        DMatrixRow_F64 L = createRandomLowerTriangular();

        DMatrixRow_F64 L_inv = L.copy();
        UnrolledInverseFromMinor_R64.inv(L_inv,L_inv);

        DMatrixRow_F64 B = RandomMatrices_R64.createRandom(3,4,rand);
        DMatrixRow_F64 expected = RandomMatrices_R64.createRandom(3,4,rand);
        DMatrixRow_F64 found = B.copy();

        TriangularSolver_R64.solveL(L.data,found.data,3,4);
        CommonOps_R64.mult(L_inv,B,expected);

        assertTrue(MatrixFeatures_R64.isIdentical(expected,found,UtilEjml.TEST_F64));
    }

    @Test
    public void solveTranL() {
        DMatrixRow_F64 L = createRandomLowerTriangular();

        DMatrixRow_F64 B = RandomMatrices_R64.createRandom(3,1,rand);
        DMatrixRow_F64 expected = RandomMatrices_R64.createRandom(3,1,rand);
        DMatrixRow_F64 found = B.copy();

        TriangularSolver_R64.solveTranL(L.data,found.data,3);

        CommonOps_R64.transpose(L);
        DMatrixRow_F64 L_inv = L.copy();
        UnrolledInverseFromMinor_R64.inv(L_inv,L_inv);
        CommonOps_R64.mult(L_inv,B,expected);

        assertTrue(MatrixFeatures_R64.isIdentical(expected,found,UtilEjml.TEST_F64));
    }

    @Test
    public void solveU() {
        DMatrixRow_F64 U = RandomMatrices_R64.createRandom(3,3,rand);
        for( int i = 0; i < U.numRows; i++ ) {
            for( int j = 0; j < i; j++ ) {
                U.set(i,j,0);
            }
        }

        DMatrixRow_F64 U_inv = U.copy();
        UnrolledInverseFromMinor_R64.inv(U_inv,U_inv);

        DMatrixRow_F64 B = RandomMatrices_R64.createRandom(3,1,rand);
        DMatrixRow_F64 expected = RandomMatrices_R64.createRandom(3,1,rand);
        DMatrixRow_F64 found = B.copy();

        TriangularSolver_R64.solveU(U.data,found.data,3);
        CommonOps_R64.mult(U_inv,B,expected);

        assertTrue(MatrixFeatures_R64.isIdentical(expected,found,UtilEjml.TEST_F64));
    }

    @Test
    public void solveU_submatrix() {

        // create U and B.  Insert into a larger matrix
        DMatrixRow_F64 U_orig = RandomMatrices_R64.createRandom(3,3,rand);
        for( int i = 0; i < U_orig.numRows; i++ ) {
            for( int j = 0; j < i; j++ ) {
                U_orig.set(i,j,0);
            }
        }
        DMatrixRow_F64 U = new DMatrixRow_F64(6,7);
        CommonOps_R64.insert(U_orig,U,2,3);
        
        
        DMatrixRow_F64 B_orig = RandomMatrices_R64.createRandom(3,2,rand);

        DMatrixRow_F64 B = new DMatrixRow_F64(4,5);
        CommonOps_R64.insert(B_orig,B,1,2);
        
        // compute expected solution
        DMatrixRow_F64 U_inv = U_orig.copy();
        UnrolledInverseFromMinor_R64.inv(U_inv,U_inv);

        DMatrixRow_F64 expected = RandomMatrices_R64.createRandom(3,2,rand);

        int startU = 2*U.numCols+3;
        int strideU = U.numCols;
        int widthU = U_orig.numCols;
        int startB = 1*B.numCols+2;
        int strideB = B.numCols;
        int widthB = B_orig.numCols;
        TriangularSolver_R64.solveU(U.data,startU,strideU,widthU,B.data,startB,strideB,widthB);

        DMatrixRow_F64 found = CommonOps_R64.extract(B,1,4,2,4);
        CommonOps_R64.mult(U_inv,B_orig,expected);

        assertTrue(MatrixFeatures_R64.isIdentical(expected,found,UtilEjml.TEST_F64));
    }
}
