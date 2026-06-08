/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.block;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DSubmatrixD1;
import org.ejml.generic.GenericMatrixOps_F64;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
@SuppressWarnings({"NullAway.Init"})
class TestHouseholder_DDRB extends EjmlStandardJUnit {
    // the block length
    int r = 3;

    SimpleMatrix A, Y, V, W;

    @Test
    void computeW_Column() {
        double[] betas = new double[]{1.2, 2, 3};

        A = SimpleMatrix.random_DDRM(r*2 + r - 1, r, -1.0, 1.0, rand);

        // Compute W directly using SimpleMatrix
        SimpleMatrix V = A.extractMatrix(0, A.numRows(), 0, 1);
        V.set(0, 0, 1);
        SimpleMatrix Y = V;
        SimpleMatrix W = V.scale(-betas[0]);

        for (int i = 1; i < A.numCols(); i++) {
            V = A.extractMatrix(0, A.numRows(), i, i + 1);

            for (int j = 0; j < i; j++)
                V.set(j, 0, 0);
            V.set(i, 0, 1);

            SimpleMatrix z = V.plus(W.mult(Y.transpose().mult(V))).scale(-betas[i]);
            W = W.combine(0, i, z);
            Y = Y.combine(0, i, V);
        }

        // now compute it using the block matrix stuff
        DMatrixRBlock Ab = MatrixOps_DDRB.convert((DMatrixRMaj)A.getMatrix(), r);
        DMatrixRBlock Wb = new DMatrixRBlock(Ab.numRows, Ab.numCols, r);

        DSubmatrixD1 Ab_sub = new DSubmatrixD1(Ab);
        DSubmatrixD1 Wb_sub = new DSubmatrixD1(Wb);

        Householder_DDRB.computeW_Column(r, Ab_sub, Wb_sub, null, betas, 0);

        // see if the result is the same
        assertTrue(GenericMatrixOps_F64.isEquivalent(Wb, (DMatrixRMaj)W.getMatrix(), UtilEjml.TEST_F64));
    }

    @Test
    public void computeW_Row() {

        for( int width = r; width <= 3*r; width++ ) {
//            System.out.println("width!!!  "+width);
            double betas[] = new double[ r ];
            for( int i = 0; i < r; i++ )
                betas[i] = i + 0.5;

            SimpleMatrix A = SimpleMatrix.random_DDRM(r,width, -1.0 , 1.0 ,rand);

            // Compute W directly using SimpleMatrix
            SimpleMatrix v = A.extractVector(true,0);
            v.set(0,0);
            v.set(1,1);
            SimpleMatrix Y = v;
            SimpleMatrix W = v.scale(-betas[0]);

            for( int i = 1; i < A.numRows(); i++ ) {
                v = A.extractVector(true,i);

                for( int j = 0; j <= i; j++ )
                    v.set(j,0);
                if( i+1 < A.numCols())
                    v.set(i+1,1);

                SimpleMatrix z = v.transpose().plus(W.transpose().mult(Y.mult(v.transpose()))).scale(-betas[i]);

                W = W.combine(i,0,z.transpose());
                Y = Y.combine(i,0,v);
            }

            // now compute it using the block matrix stuff
            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.getDDRM(),r);
            DMatrixRBlock Wb = new DMatrixRBlock(Ab.numRows,Ab.numCols,r);

            DSubmatrixD1 Ab_sub = new DSubmatrixD1(Ab);
            DSubmatrixD1 Wb_sub = new DSubmatrixD1(Wb);

            Householder_DDRB.computeW_Row(r, Ab_sub, Wb_sub, betas, 0);

            // see if the result is the same
            assertTrue(GenericMatrixOps_F64.isEquivalent(Wb,W.getDDRM(),UtilEjml.TEST_F64));
        }
    }

    @Test
    void initializeW() {
        initMatrices(r - 1);

        double beta = 1.5;

        DMatrixRBlock Wb = MatrixOps_DDRB.convert((DMatrixRMaj)W.getMatrix(), r);
        DMatrixRBlock Ab = MatrixOps_DDRB.convert((DMatrixRMaj)A.getMatrix(), r);

        DSubmatrixD1 Wb_sub = new DSubmatrixD1(Wb, 0, W.numRows(), 0, r);
        DSubmatrixD1 Yb_sub = new DSubmatrixD1(Ab, 0, A.numRows(), 0, r);

        Householder_DDRB.initializeW(r, Wb_sub, Yb_sub, r, beta);

        assertEquals(-beta, Wb.get(0, 0), UtilEjml.TEST_F64);

        for (int i = 1; i < Wb.numRows; i++) {
            assertEquals(-beta*Ab.get(i, 0), Wb.get(i, 0), UtilEjml.TEST_F64);
        }
    }

    @Test
    void computeZ() {
        int M = r - 1;
        initMatrices(M);

        double beta = 2.5;

        DMatrixRBlock Ab = MatrixOps_DDRB.convert((DMatrixRMaj)A.getMatrix(), r);
        DMatrixRBlock Aw = MatrixOps_DDRB.convert((DMatrixRMaj)W.getMatrix(), r);

        // need to extract only the elements in W that are currently being used when
        // computing the expected Z
        W = W.extractMatrix(0, W.numRows(), 0, M);
        SimpleMatrix T = SimpleMatrix.random_DDRM(M, 1, -1, 1, rand);

        // -beta * (V + W*T)
        SimpleMatrix expected = V.plus(W.mult(T)).scale(-beta);

        Householder_DDRB.computeZ(r, new DSubmatrixD1(Ab, 0, A.numRows(), 0, r),
                new DSubmatrixD1(Aw, 0, A.numRows(), 0, r),
                M, T.getDDRM().data, beta);

        for (int i = 0; i < A.numRows(); i++) {
            assertEquals(expected.get(i), Aw.get(i, M), UtilEjml.TEST_F64);
        }
    }

    @Test
    void computeY_t_V() {
        int M = r - 2;
        initMatrices(M);

        // Y'*V
        SimpleMatrix expected = Y.transpose().mult(V);

        DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.getDDRM(), r);
        double[] found = new double[M];

        Householder_DDRB.computeY_t_V(r, new DSubmatrixD1(Ab, 0, A.numRows(), 0, r), M, found);

        for (int i = 0; i < M; i++) {
            assertEquals(expected.get(i), found[i], UtilEjml.TEST_F64);
        }
    }

    @Test
    public void multPlusTransA() {
        for( int width = r+1; width <= r*3; width++ ) {
            SimpleMatrix A = SimpleMatrix.random_DDRM(width,width, -1.0, 1.0,rand);
            SimpleMatrix U = SimpleMatrix.random_DDRM(r,width, -1.0, 1.0 ,rand);
            SimpleMatrix V = SimpleMatrix.random_DDRM(r,width, -1.0, 1.0 ,rand);

            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.getDDRM(),r);
            DMatrixRBlock Ub = MatrixOps_DDRB.convert(U.getDDRM(),r);
            DMatrixRBlock Vb = MatrixOps_DDRB.convert(V.getDDRM(),r);

            SimpleMatrix expected = A.plus(U.transpose().mult(V));

            Householder_DDRB.multPlusTransA(r, new DSubmatrixD1(Ub)
                    , new DSubmatrixD1(Vb), new DSubmatrixD1(Ab));


            for( int i = r; i < width; i++ ) {
                for( int j = i; j < width; j++ ) {
                    assertEquals(expected.get(i,j),Ab.get(i,j),UtilEjml.TEST_F64,i+" "+j);
                }
            }
        }
    }

    private void initMatrices( int M ) {
        A = SimpleMatrix.random_DDRM(r*2 + r - 1, r, -1.0, 1.0, rand);

        // create matrices that are used to test
        Y = A.extractMatrix(0, A.numRows(), 0, M);
        V = A.extractMatrix(0, A.numRows(), M, M + 1);

        // add in zeros and ones
        setZerosY();
        for (int i = 0; i < M; i++) {
            V.set(i, 0);
        }
        V.set(M, 1);

        W = SimpleMatrix.random_DDRM(r*2 + r - 1, r, -1.0, 1.0, rand);
    }

    private void setZerosY() {
        for (int j = 0; j < Y.numCols(); j++) {
            for (int i = 0; i < j; i++) {
                Y.set(i, j, 0);
            }
            Y.set(j, j, 1);
        }
    }
}
