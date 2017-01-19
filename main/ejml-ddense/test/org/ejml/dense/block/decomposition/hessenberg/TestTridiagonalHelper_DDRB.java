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

package org.ejml.dense.block.decomposition.hessenberg;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DSubmatrixD1;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.hessenberg.TridiagonalDecompositionHouseholderOrig_DDRM;
import org.ejml.generic.GenericMatrixOps_F64;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestTridiagonalHelper_DDRB {

    Random rand = new Random(234324);
    int r = 3;

    /**
     * Compare against a simple tridiagonalization implementation
     */
    @Test
    public void tridiagUpperRow() {

        int offX = 0;
        int offY = 0;

        // test it out on a variety of sizes
        for( int width = 1; width <= 3*r; width++ ) {
//            System.out.println("********* width "+width);

            // create a random symmetric matrix
            SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_DDRM.symmetric(width,-1,1,rand));

            TridiagonalDecompositionHouseholderOrig_DDRM decomp = new TridiagonalDecompositionHouseholderOrig_DDRM();
            decomp.decompose(A.matrix_F64());

            DSubmatrixD1 Ab = insertIntoBlock(offX,offY,A,r);
            DSubmatrixD1 V = new DSubmatrixD1(new DMatrixRBlock(r,offX+A.numCols(),r));
            V.col0 = offX;
            V.row1 = Ab.row1-Ab.row0;
            int gammaOffset = offX;
            double gammas[] = new double[gammaOffset+A.numCols()];
            TridiagonalHelper_DDRB.tridiagUpperRow(r, Ab, gammas, V);

            DMatrixRMaj expected = decomp.getQT();

            // see if the decomposed matrix is the same
            for( int i = 0; i < r; i++ ) {
                for( int j = i; j < width; j++ ) {
                    assertEquals(i+" "+j,expected.get(i,j),Ab.get(i,j), UtilEjml.TEST_F64);
                }
            }
            // check the gammas
            for( int i = 0; i < Math.min(width-1,r); i++ ) {
                assertEquals(decomp.getGamma(i+1),gammas[i+gammaOffset],UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void computeW_row() {

        for( int width = r; width <= 3*r; width++ ) {
//            System.out.println("width!!!  "+width);
            double betas[] = new double[ r ];
            for( int i = 0; i < r; i++ )
                betas[i] = i + 0.5;

            SimpleMatrix A = SimpleMatrix.random64(r,width, -1.0 , 1.0 ,rand);

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
            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.matrix_F64(),r);
            DMatrixRBlock Wb = new DMatrixRBlock(Ab.numRows,Ab.numCols,r);

            DSubmatrixD1 Ab_sub = new DSubmatrixD1(Ab);
            DSubmatrixD1 Wb_sub = new DSubmatrixD1(Wb);

            TridiagonalHelper_DDRB.computeW_row(r, Ab_sub, Wb_sub, betas, 0);

            // see if the result is the same
            assertTrue(GenericMatrixOps_F64.isEquivalent(Wb,W.matrix_F64(),UtilEjml.TEST_F64));
        }
    }

    @Test
    public void applyReflectorsToRow() {

        // try different offsets to make sure there are no implicit assumptions
        for( int offX = 0; offX <= r; offX += r) {
            for( int offY = 0; offY <= r; offY += r) {
                SimpleMatrix A = SimpleMatrix.random64(2*r+2,2*r+2, -1.0 , 1.0 ,rand);
                A = A.mult(A.transpose());
                SimpleMatrix A_orig = A.copy();
                SimpleMatrix V = SimpleMatrix.random64(r,A.numCols(), -1.0 , 1.0 ,rand);

                DSubmatrixD1 Ab = insertIntoBlock(offY,offX,A,r);
                DSubmatrixD1 Vb = insertIntoBlock(0,offX,V,r);

                int row = r-1;

                // manually apply "reflectors" to A
                for( int i = 0; i < row; i++ ) {
                    SimpleMatrix u = A_orig.extractVector(true,i).transpose();
                    SimpleMatrix v = V.extractVector(true,i).transpose();

                    for( int j = 0; j <= i; j++ ) {
                        u.set(j,0.0);
                    }
                    u.set(i+1,1.0);

                    A = A.plus(u.mult(v.transpose())).plus(v.mult(u.transpose()));
                }
                // apply the reflector to that row
                TridiagonalHelper_DDRB.applyReflectorsToRow(r, Ab, Vb, row);

                // compare to manually computed solution
                for( int i = row; i < A.numCols(); i++ ) {
                    assertEquals(A.get(row,i),Ab.get(row,i),UtilEjml.TEST_F64);
                }
            }
        }
    }

    private static DSubmatrixD1 insertIntoBlock(int offRow , int offCol , SimpleMatrix A , int r )
    {
        DMatrixRMaj B = new DMatrixRMaj(offRow+A.numRows(),offCol+A.numCols());
        CommonOps_DDRM.insert(A.matrix_F64(),B,offRow,offCol);

        DMatrixRBlock C = MatrixOps_DDRB.convert(B,r);
        return new DSubmatrixD1(C,offRow,C.numRows,offCol,C.numCols);
    }

    @Test
    public void multA_u() {
        SimpleMatrix A = SimpleMatrix.random64(2*r+2,2*r+2, -1.0 , 1.0 ,rand);
        // make a symmetric so that this mult will work
        A = A.transpose().mult(A);

        DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.matrix_F64(),r);
        DMatrixRBlock V = new DMatrixRBlock(r,Ab.numCols,r);

        int row = 1;

        SimpleMatrix u = A.extractVector(true,row).transpose();
        for( int i = 0; i <= row; i++ ) {
            u.set(i,0);
        }
        u.set(row+1,1);

        SimpleMatrix v = A.mult(u).transpose();

        TridiagonalHelper_DDRB.multA_u(r, new DSubmatrixD1(Ab),
                new DSubmatrixD1(V), row);

        for( int i = row+1; i < A.numCols(); i++ ) {
            assertEquals(v.get(i),V.get(row,i),UtilEjml.TEST_F64);
        }
    }

    /**
     * Check by performing the calculation manually
     */
    @Test
    public void computeY() {
        SimpleMatrix A = SimpleMatrix.random64(2*r+2,2*r+2, -1.0 , 1.0 ,rand);
        A = A.transpose().mult(A); // needs to be symmetric to pass
        SimpleMatrix Vo = SimpleMatrix.random64(r,A.numCols(), -1.0 , 1.0 ,rand);

        for( int row = 0; row < r; row++ ) {
            SimpleMatrix AA = A.copy();
            SimpleMatrix u = A.extractVector(true,row).transpose();
            SimpleMatrix y;

            double gamma = 1.3;

            // zero elements that should already be zero
            for( int i = 0; i < row; i++ ) {
                u.set(i,0);
                for( int j = i+2; j < A.numRows(); j++ ) {
                    AA.set(i,j,0);
                    AA.set(j,i,0);
                }
            }
            u.set(row,0);
            u.set(row+1,1);

            if( row > 0 ) {
                SimpleMatrix U = A.extractMatrix(0,row,0,A.numCols()).transpose();
                SimpleMatrix V = Vo.extractMatrix(0,row,0,A.numCols()).transpose();

                for( int i = 0; i < row; i++ ) {
                    for( int j = 0; j <= i; j++ ) {
                        U.set(j,i,0);
                    }
                    U.set(i+1,i,1);
                }

                y = AA.plus(U.mult(V.transpose())).plus(V.mult(U.transpose())).mult(u);
            } else {
                y = AA.mult(u);
            }

            y = y.scale(-gamma);

            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.matrix_F64(),r);
            DMatrixRBlock Vb = MatrixOps_DDRB.convert(Vo.matrix_F64(),r);

            TridiagonalHelper_DDRB.computeY(r, new DSubmatrixD1(Ab), new DSubmatrixD1(Vb), row, gamma);

            for( int i = row+1; i < A.numCols(); i++ ) {
                assertEquals(Vb.get(row,i),y.get(i),UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void computeRowOfV() {
        SimpleMatrix A = SimpleMatrix.random64(2*r+2,2*r+2, -1.0 , 1.0 , rand);
        SimpleMatrix V = SimpleMatrix.random64(r,A.numCols(), -1.0 , 1.0 , rand);

        double gamma = 2.3;

        for( int row = 0; row < r; row++ ) {
            SimpleMatrix u = A.extractVector(true,row).transpose();
            SimpleMatrix y = V.extractVector(true,row).transpose();

            for( int i = 0; i <= row; i++ ) {
                u.set(i,0);
            }
            u.set(row+1,1);

            SimpleMatrix v = y.plus(u.scale(-(gamma/2.0)*u.dot(y)));

            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.matrix_F64(),r);
            DMatrixRBlock Vb = MatrixOps_DDRB.convert(V.matrix_F64(),r);

            TridiagonalHelper_DDRB.computeRowOfV(r, new DSubmatrixD1(Ab), new DSubmatrixD1(Vb),
                    row, gamma);

            for( int i = row+1; i < A.numCols(); i++ ) {
                assertEquals(Vb.get(row,i),v.get(i),UtilEjml.TEST_F64);
            }
        }
    }
}
