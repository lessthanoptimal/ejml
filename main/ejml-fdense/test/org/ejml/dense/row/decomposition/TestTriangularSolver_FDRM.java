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

package org.ejml.dense.row.decomposition;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.misc.UnrolledInverseFromMinor_FDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestTriangularSolver_FDRM {

    private Random rand = new Random(0xff);

    @Test
    public void invert_inplace() {
        FMatrixRMaj L = createRandomLowerTriangular(3);

        FMatrixRMaj L_inv = L.copy();

        TriangularSolver_FDRM.invertLower(L_inv.data,L.numRows);

        FMatrixRMaj I = new FMatrixRMaj(L.numRows,L.numCols);

        CommonOps_FDRM.mult(L,L_inv,I);

        assertTrue(MatrixFeatures_FDRM.isIdentity(I,UtilEjml.TEST_F32));
    }

    @Test
    public void invert() {
        FMatrixRMaj L = createRandomLowerTriangular(3);

        FMatrixRMaj L_inv = L.copy();

        TriangularSolver_FDRM.invertLower(L.data,L_inv.data,L.numRows);

        FMatrixRMaj I = new FMatrixRMaj(L.numRows,L.numCols);

        CommonOps_FDRM.mult(L,L_inv,I);

        assertTrue(MatrixFeatures_FDRM.isIdentity(I, UtilEjml.TEST_F32));
    }

    @Test
    public void solveL_vector() {
        for( int m : new int[]{1,2,5,10,20,50}) {
            FMatrixRMaj L = createRandomLowerTriangular(m);

            FMatrixRMaj B = RandomMatrices_FDRM.rectangle(m, 1, rand);
            FMatrixRMaj X = B.copy();
            FMatrixRMaj found = RandomMatrices_FDRM.rectangle(m, 1, rand);

            TriangularSolver_FDRM.solveL(L.data, X.data, m);
            CommonOps_FDRM.mult(L, X, found);

            assertTrue(MatrixFeatures_FDRM.isIdentical(B, found, UtilEjml.TEST_F32));
        }
    }

    private FMatrixRMaj createRandomLowerTriangular(int size) {
        FMatrixRMaj L = RandomMatrices_FDRM.triangularLower(size,0,-1,1,rand);
        // make the diagonal elements close to 1 so that the system is easily solvable
        for (int i = 0; i < size; i++) {
            L.set(i,i,1.0f + (float)(rand.nextGaussian()*0.001f));
        }
        return L;
    }

    private FMatrixRMaj createRandomUpperTriangular(int size) {
        FMatrixRMaj L = RandomMatrices_FDRM.triangularUpper(size,0,-1,1,rand);
        // make the diagonal elements close to 1 so that the system is easily solvable
        for (int i = 0; i < size; i++) {
            L.set(i,i,1.0f + (float)(rand.nextGaussian()*0.001f));
        }
        return L;
    }

    @Test
    public void solveL_matrix() {
        for( int m : new int[]{1,2,5,10,20,50}) {
            FMatrixRMaj L = createRandomLowerTriangular(m);

            FMatrixRMaj B = RandomMatrices_FDRM.rectangle(m, 4, rand);
            FMatrixRMaj X = B.copy();
            FMatrixRMaj found = RandomMatrices_FDRM.rectangle(m, 4, rand);

            TriangularSolver_FDRM.solveL(L.data, X.data, m, 4);
            CommonOps_FDRM.mult(L, X, found);

            assertTrue(MatrixFeatures_FDRM.isIdentical(B, found, UtilEjml.TEST_F32));
        }
    }

    @Test
    public void solveTranL() {
        FMatrixRMaj L = createRandomLowerTriangular(3);

        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(3,1,rand);
        FMatrixRMaj expected = RandomMatrices_FDRM.rectangle(3,1,rand);
        FMatrixRMaj found = B.copy();

        TriangularSolver_FDRM.solveTranL(L.data,found.data,3);

        CommonOps_FDRM.transpose(L);
        FMatrixRMaj L_inv = L.copy();
        UnrolledInverseFromMinor_FDRM.inv(L_inv,L_inv);
        CommonOps_FDRM.mult(L_inv,B,expected);

        assertTrue(MatrixFeatures_FDRM.isIdentical(expected,found,UtilEjml.TEST_F32));
    }

    @Test
    public void solveU() {
        for( int m : new int[]{1,2,5,10,20,50}) {
            FMatrixRMaj U = createRandomUpperTriangular(m);

            FMatrixRMaj B = RandomMatrices_FDRM.rectangle(m, 1, rand);
            FMatrixRMaj X = B.copy();
            FMatrixRMaj found = RandomMatrices_FDRM.rectangle(m, 1, rand);

            TriangularSolver_FDRM.solveU(U.data, X.data, m);
            CommonOps_FDRM.mult(U, X, found);

            assertTrue(MatrixFeatures_FDRM.isIdentical(B, found, UtilEjml.TEST_F32));
        }
    }

    @Test
    public void solveU_submatrix() {

        // create U and B.  Insert into a larger matrix
        FMatrixRMaj U_orig = RandomMatrices_FDRM.rectangle(3,3,rand);
        for( int i = 0; i < U_orig.numRows; i++ ) {
            for( int j = 0; j < i; j++ ) {
                U_orig.set(i,j,0);
            }
        }
        FMatrixRMaj U = new FMatrixRMaj(6,7);
        CommonOps_FDRM.insert(U_orig,U,2,3);
        
        
        FMatrixRMaj B_orig = RandomMatrices_FDRM.rectangle(3,2,rand);

        FMatrixRMaj B = new FMatrixRMaj(4,5);
        CommonOps_FDRM.insert(B_orig,B,1,2);
        
        // compute expected solution
        FMatrixRMaj U_inv = U_orig.copy();
        UnrolledInverseFromMinor_FDRM.inv(U_inv,U_inv);

        FMatrixRMaj expected = RandomMatrices_FDRM.rectangle(3,2,rand);

        int startU = 2*U.numCols+3;
        int strideU = U.numCols;
        int widthU = U_orig.numCols;
        int startB = 1*B.numCols+2;
        int strideB = B.numCols;
        int widthB = B_orig.numCols;
        TriangularSolver_FDRM.solveU(U.data,startU,strideU,widthU,B.data,startB,strideB,widthB);

        FMatrixRMaj found = CommonOps_FDRM.extract(B,1,4,2,4);
        CommonOps_FDRM.mult(U_inv,B_orig,expected);

        assertTrue(MatrixFeatures_FDRM.isIdentical(expected,found,UtilEjml.TEST_F32));
    }
}
