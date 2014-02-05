/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.block.decomposition.hessenberg;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.dense.decomposition.hessenberg.TridiagonalDecompositionHouseholderOrig_D64;
import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestTridiagonalHelper_B64 {

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
            SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices.createSymmetric(width,-1,1,rand));

            TridiagonalDecompositionHouseholderOrig_D64 decomp = new TridiagonalDecompositionHouseholderOrig_D64();
            decomp.decompose(A.getMatrix());

            D1Submatrix64F Ab = insertIntoBlock(offX,offY,A,r);
            D1Submatrix64F V = new D1Submatrix64F(new BlockMatrix64F(r,offX+A.numCols(),r));
            V.col0 = offX;
            V.row1 = Ab.row1-Ab.row0;
            int gammaOffset = offX;
            double gammas[] = new double[gammaOffset+A.numCols()];
            TridiagonalHelper_B64.tridiagUpperRow(r, Ab, gammas, V);

            DenseMatrix64F expected = decomp.getQT();

            // see if the decomposed matrix is the same
            for( int i = 0; i < r; i++ ) {
                for( int j = i; j < width; j++ ) {
                    assertEquals(i+" "+j,expected.get(i,j),Ab.get(i,j),1e-8);
                }
            }
            // check the gammas
            for( int i = 0; i < Math.min(width-1,r); i++ ) {
                assertEquals(decomp.getGamma(i+1),gammas[i+gammaOffset],1e-8);
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

            SimpleMatrix A = SimpleMatrix.random(r,width,-1,1,rand);

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
            BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);
            BlockMatrix64F Wb = new BlockMatrix64F(Ab.numRows,Ab.numCols,r);

            D1Submatrix64F Ab_sub = new D1Submatrix64F(Ab);
            D1Submatrix64F Wb_sub = new D1Submatrix64F(Wb);

            TridiagonalHelper_B64.computeW_row(r, Ab_sub, Wb_sub, betas, 0);

            // see if the result is the same
            assertTrue(GenericMatrixOps.isEquivalent(Wb,W.getMatrix(),1e-8));
        }
    }

    @Test
    public void applyReflectorsToRow() {

        // try different offsets to make sure there are no implicit assumptions
        for( int offX = 0; offX <= r; offX += r) {
            for( int offY = 0; offY <= r; offY += r) {
                SimpleMatrix A = SimpleMatrix.random(2*r+2,2*r+2,-1,1,rand);
                A = A.mult(A.transpose());
                SimpleMatrix A_orig = A.copy();
                SimpleMatrix V = SimpleMatrix.random(r,A.numCols(),-1,1,rand);

                D1Submatrix64F Ab = insertIntoBlock(offY,offX,A,r);
                D1Submatrix64F Vb = insertIntoBlock(0,offX,V,r);

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
                TridiagonalHelper_B64.applyReflectorsToRow(r, Ab, Vb, row);

                // compare to manually computed solution
                for( int i = row; i < A.numCols(); i++ ) {
                    assertEquals(A.get(row,i),Ab.get(row,i),1e-8);
                }
            }
        }
    }

    private static D1Submatrix64F insertIntoBlock( int offRow , int offCol , SimpleMatrix A , int r )
    {
        DenseMatrix64F B = new DenseMatrix64F(offRow+A.numRows(),offCol+A.numCols());
        CommonOps.insert(A.getMatrix(),B,offRow,offCol);

        BlockMatrix64F C = BlockMatrixOps.convert(B,r);
        return new D1Submatrix64F(C,offRow,C.numRows,offCol,C.numCols);
    }

    @Test
    public void multA_u() {
        SimpleMatrix A = SimpleMatrix.random(2*r+2,2*r+2,-1,1,rand);
        // make a symmetric so that this mult will work
        A = A.transpose().mult(A);

        BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);
        BlockMatrix64F V = new BlockMatrix64F(r,Ab.numCols,r);

        int row = 1;

        SimpleMatrix u = A.extractVector(true,row).transpose();
        for( int i = 0; i <= row; i++ ) {
            u.set(i,0);
        }
        u.set(row+1,1);

        SimpleMatrix v = A.mult(u).transpose();

        TridiagonalHelper_B64.multA_u(r, new D1Submatrix64F(Ab),
                new D1Submatrix64F(V), row);

        for( int i = row+1; i < A.numCols(); i++ ) {
            assertEquals(v.get(i),V.get(row,i),1e-8);
        }
    }

    /**
     * Check by performing the calculation manually
     */
    @Test
    public void computeY() {
        SimpleMatrix A = SimpleMatrix.random(2*r+2,2*r+2,-1,1,rand);
        A = A.transpose().mult(A); // needs to be symmetric to pass
        SimpleMatrix Vo = SimpleMatrix.random(r,A.numCols(),-1,1,rand);

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

            BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);
            BlockMatrix64F Vb = BlockMatrixOps.convert(Vo.getMatrix(),r);

            TridiagonalHelper_B64.computeY(r, new D1Submatrix64F(Ab), new D1Submatrix64F(Vb), row, gamma);

            for( int i = row+1; i < A.numCols(); i++ ) {
                assertEquals(Vb.get(row,i),y.get(i),1e-8);
            }
        }
    }

    @Test
    public void computeRowOfV() {
        SimpleMatrix A = SimpleMatrix.random(2*r+2,2*r+2,-1,1,rand);
        SimpleMatrix V = SimpleMatrix.random(r,A.numCols(),-1,1,rand);

        double gamma = 2.3;

        for( int row = 0; row < r; row++ ) {
            SimpleMatrix u = A.extractVector(true,row).transpose();
            SimpleMatrix y = V.extractVector(true,row).transpose();

            for( int i = 0; i <= row; i++ ) {
                u.set(i,0);
            }
            u.set(row+1,1);

            SimpleMatrix v = y.plus(u.scale(-(gamma/2.0)*u.dot(y)));

            BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);
            BlockMatrix64F Vb = BlockMatrixOps.convert(V.getMatrix(),r);

            TridiagonalHelper_B64.computeRowOfV(r, new D1Submatrix64F(Ab), new D1Submatrix64F(Vb),
                    row, gamma);

            for( int i = row+1; i < A.numCols(); i++ ) {
                assertEquals(Vb.get(row,i),v.get(i),1e-8);
            }
        }
    }
}
