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

package org.ejml.dense.row.decomposition;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.misc.UnrolledInverseFromMinor_DDRM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestTriangularSolver_DDRM extends EjmlStandardJUnit {
    @Test
    public void invert_inplace() {
        DMatrixRMaj L = createRandomLowerTriangular(3);

        DMatrixRMaj L_inv = L.copy();

        TriangularSolver_DDRM.invertLower(L_inv.data,L.numRows);

        DMatrixRMaj I = new DMatrixRMaj(L.numRows,L.numCols);

        CommonOps_DDRM.mult(L,L_inv,I);

        assertTrue(MatrixFeatures_DDRM.isIdentity(I,UtilEjml.TEST_F64));
    }

    @Test
    public void invert() {
        DMatrixRMaj L = createRandomLowerTriangular(3);

        DMatrixRMaj L_inv = L.copy();

        TriangularSolver_DDRM.invertLower(L.data,L_inv.data,L.numRows);

        DMatrixRMaj I = new DMatrixRMaj(L.numRows,L.numCols);

        CommonOps_DDRM.mult(L,L_inv,I);

        assertTrue(MatrixFeatures_DDRM.isIdentity(I, UtilEjml.TEST_F64));
    }

    @Test
    public void solveL_vector() {
        for( int m : new int[]{1,2,5,10,20,50}) {
            DMatrixRMaj L = createRandomLowerTriangular(m);

            DMatrixRMaj B = RandomMatrices_DDRM.rectangle(m, 1, rand);
            DMatrixRMaj X = B.copy();
            DMatrixRMaj found = RandomMatrices_DDRM.rectangle(m, 1, rand);

            TriangularSolver_DDRM.solveL(L.data, X.data, m);
            CommonOps_DDRM.mult(L, X, found);

            assertTrue(MatrixFeatures_DDRM.isIdentical(B, found, UtilEjml.TEST_F64));
        }
    }

    private DMatrixRMaj createRandomLowerTriangular(int size) {
        DMatrixRMaj L = RandomMatrices_DDRM.triangularLower(size,0,-1,1,rand);
        // make the diagonal elements close to 1 so that the system is easily solvable
        for (int i = 0; i < size; i++) {
            L.set(i,i,1.0 + (double)(rand.nextGaussian()*0.001));
        }
        return L;
    }

    private DMatrixRMaj createRandomUpperTriangular(int size) {
        DMatrixRMaj L = RandomMatrices_DDRM.triangularUpper(size,0,-1,1,rand);
        // make the diagonal elements close to 1 so that the system is easily solvable
        for (int i = 0; i < size; i++) {
            L.set(i,i,1.0 + (double)(rand.nextGaussian()*0.001));
        }
        return L;
    }

    @Test
    public void solveL_matrix() {
        for( int m : new int[]{1,2,5,10,20,50}) {
            DMatrixRMaj L = createRandomLowerTriangular(m);

            DMatrixRMaj B = RandomMatrices_DDRM.rectangle(m, 4, rand);
            DMatrixRMaj X = B.copy();
            DMatrixRMaj found = RandomMatrices_DDRM.rectangle(m, 4, rand);

            TriangularSolver_DDRM.solveL(L.data, X.data, m, 4);
            CommonOps_DDRM.mult(L, X, found);

            assertTrue(MatrixFeatures_DDRM.isIdentical(B, found, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void solveTranL() {
        DMatrixRMaj L = createRandomLowerTriangular(3);

        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(3,1,rand);
        DMatrixRMaj expected = RandomMatrices_DDRM.rectangle(3,1,rand);
        DMatrixRMaj found = B.copy();

        TriangularSolver_DDRM.solveTranL(L.data,found.data,3);

        CommonOps_DDRM.transpose(L);
        DMatrixRMaj L_inv = L.copy();
        UnrolledInverseFromMinor_DDRM.inv(L_inv,L_inv);
        CommonOps_DDRM.mult(L_inv,B,expected);

        assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found,UtilEjml.TEST_F64));
    }

    @Test
    public void solveU() {
        for( int m : new int[]{1,2,5,10,20,50}) {
            DMatrixRMaj U = createRandomUpperTriangular(m);

            DMatrixRMaj B = RandomMatrices_DDRM.rectangle(m, 1, rand);
            DMatrixRMaj X = B.copy();
            DMatrixRMaj found = RandomMatrices_DDRM.rectangle(m, 1, rand);

            TriangularSolver_DDRM.solveU(U.data, X.data, m);
            CommonOps_DDRM.mult(U, X, found);

            assertTrue(MatrixFeatures_DDRM.isIdentical(B, found, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void solveU_submatrix() {

        // create U and B.  Insert into a larger matrix
        DMatrixRMaj U_orig = RandomMatrices_DDRM.rectangle(3,3,rand);
        for( int i = 0; i < U_orig.numRows; i++ ) {
            for( int j = 0; j < i; j++ ) {
                U_orig.set(i,j,0);
            }
        }
        DMatrixRMaj U = new DMatrixRMaj(6,7);
        CommonOps_DDRM.insert(U_orig,U,2,3);
        
        
        DMatrixRMaj B_orig = RandomMatrices_DDRM.rectangle(3,2,rand);

        DMatrixRMaj B = new DMatrixRMaj(4,5);
        CommonOps_DDRM.insert(B_orig,B,1,2);
        
        // compute expected solution
        DMatrixRMaj U_inv = U_orig.copy();
        UnrolledInverseFromMinor_DDRM.inv(U_inv,U_inv);

        DMatrixRMaj expected = RandomMatrices_DDRM.rectangle(3,2,rand);

        int startU = 2*U.numCols+3;
        int strideU = U.numCols;
        int widthU = U_orig.numCols;
        int startB = 1*B.numCols+2;
        int strideB = B.numCols;
        int widthB = B_orig.numCols;
        TriangularSolver_DDRM.solveU(U.data,startU,strideU,widthU,B.data,startB,strideB,widthB);

        DMatrixRMaj found = CommonOps_DDRM.extract(B,1,4,2,4);
        CommonOps_DDRM.mult(U_inv,B_orig,expected);

        assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found,UtilEjml.TEST_F64));
    }
}
