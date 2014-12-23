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

package org.ejml.alg.block.decomposition.qr;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderTran_D64;
import org.ejml.alg.dense.mult.VectorVectorMult;
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
public class TestBlockHouseHolder {

    Random rand = new Random(234);

    // the block length
    int r = 3;

    SimpleMatrix A, Y,V,W;

    @Test
    public void decomposeQR_block_col() {
        DenseMatrix64F A = RandomMatrices.createRandom(r*2+r-1,r,-1,1,rand);
        BlockMatrix64F Ab = BlockMatrixOps.convert(A,r);

        QRDecompositionHouseholderTran_D64 algTest = new QRDecompositionHouseholderTran_D64();
        assertTrue(algTest.decompose(A));

        double gammas[] = new double[A.numCols];
        BlockHouseHolder.decomposeQR_block_col(r,new D1Submatrix64F(Ab),gammas);

        DenseMatrix64F expected = CommonOps.transpose(algTest.getQR(),null);

        assertTrue(GenericMatrixOps.isEquivalent(expected,Ab,1e-8));
    }

    @Test
    public void rank1UpdateMultR_Col() {

        // check various sized matrices
        double gamma = 2.5;
        A = SimpleMatrix.random(r*2+r-1,r*2-1,-1,1,rand);

        SimpleMatrix U = A.extractMatrix(0,A.numRows(),1,2);
        U.set(0,0,0);
        U.set(1,0,1);

        SimpleMatrix V = A.extractMatrix(0,A.numRows(),2,3);
        SimpleMatrix expected = V.minus(U.mult(U.transpose().mult(V)).scale(gamma));

        BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);

        BlockHouseHolder.rank1UpdateMultR_Col(r,new D1Submatrix64F(Ab),1,gamma);

        for( int i = 1; i < expected.numRows(); i++ ) {
            assertEquals(expected.get(i,0),Ab.get(i,2),1e-8);
        }
    }

    @Test
    public void rank1UpdateMultR_TopRow() {
        double gamma = 2.5;
        A = SimpleMatrix.random(r*2+r-1,r*2-1,-1,1,rand);

        SimpleMatrix U = A.extractMatrix(0,A.numRows(),1,2);
        U.set(0,0,0);
        U.set(1,0,1);

        BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);

        BlockHouseHolder.rank1UpdateMultR_TopRow(r,new D1Submatrix64F(Ab),1,gamma);

        // check all the columns now
        for( int i = 0; i < r; i++ ) {
            for( int j = r; j < A.numCols(); j++ ) {
                SimpleMatrix V = A.extractMatrix(0,A.numRows(),j,j+1);
                SimpleMatrix expected = V.minus(U.mult(U.transpose().mult(V)).scale(gamma));

                assertEquals(i+" "+j,expected.get(i,0),Ab.get(i,j),1e-8);
            }
        }
    }

    @Test
    public void rank1UpdateMultL_Row() {
        double gamma = 2.5;
        A = SimpleMatrix.random(r*2+r-1,r*2+r-1,-1,1,rand);

        SimpleMatrix U = A.extractMatrix(1,2,0,A.numCols()).transpose();
        U.set(0,0);
        U.set(1,1);

        SimpleMatrix expected = A.minus( A.mult(U).mult(U.transpose()).scale(gamma) );

        BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);

        BlockHouseHolder.rank1UpdateMultL_Row(r,new D1Submatrix64F(Ab),1,1,gamma);

        for( int j = 1; j < expected.numCols(); j++ ) {
            assertEquals(expected.get(2,j),Ab.get(2,j),1e-8);
        }
    }

    @Test
    public void rank1UpdateMultL_LeftCol() {
        double gamma = 2.5;
        A = SimpleMatrix.random(r*2+r-1,r*2+r-1,-1,1,rand);

        int row = 0;
        int zeroOffset = 1;
        SimpleMatrix U = A.extractMatrix(row,row+1,0,A.numCols()).transpose();
        for( int i = 0; i < row+zeroOffset; i++ )
            U.set(i,0);
        U.set(row+zeroOffset,1);

        SimpleMatrix expected = A.minus( A.mult(U).mult(U.transpose()).scale(gamma) );

        BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);

        BlockHouseHolder.rank1UpdateMultL_LeftCol(r,new D1Submatrix64F(Ab),row,gamma,zeroOffset);

        for( int i = r; i < A.numRows(); i++ ) {
            for( int j = 0; j < r; j++ ) {
                assertEquals(expected.get(i,j),Ab.get(i,j),1e-8);
            }
        }
    }

    /**
     * Check inner product when column blocks have two different widths
     */
    @Test
    public void innerProdCol() {
        DenseMatrix64F A = RandomMatrices.createRandom(r*2+r-1,r*3-1,-1,1,rand);
        BlockMatrix64F Ab = BlockMatrixOps.convert(A,r);

        int row = 0;
        int innerCol = 1;
        for( int colBlock = 0; colBlock < r*2; colBlock+=r) {
            int colA = colBlock+innerCol;
            int colB = colA+innerCol+1;
            int widthA = Math.min(r,A.numCols - (colA-colA%r));
            int widthB = Math.min(r,A.numCols - (colB-colB%r));

            DenseMatrix64F v0 = CommonOps.extract(A,row,A.numRows,colA,colA+1);
            DenseMatrix64F v1 = CommonOps.extract(A,row,A.numRows,colB,colB+1);
            for( int j = 0; j < innerCol; j++ ) {
                v0.set(j,0.0);
            }
            v0.set(innerCol,1.0);

            double expected = VectorVectorMult.innerProd(v0,v1);

            D1Submatrix64F subAb = new D1Submatrix64F(Ab,row,A.numRows,colBlock,A.numCols);

            double found = BlockHouseHolder.innerProdCol(r,subAb,colA-colBlock,widthA,colB-colBlock,widthB);

            assertEquals(expected,found,1e-8);
        }
    }


    @Test
    public void innerProdRow() {
        DenseMatrix64F A = RandomMatrices.createRandom(r*3-1,r*2+r-1,-1,1,rand);
        BlockMatrix64F Ab = BlockMatrixOps.convert(A,r);

        int zeroOffset = 1;
        for( int rowBlock = 0; rowBlock < r*2; rowBlock+=r) {
            int rowA = 2;
            int rowB = 1;

            DenseMatrix64F v0 = CommonOps.extract(A,rowBlock+rowA,rowBlock+rowA+1,0,A.numCols);
            DenseMatrix64F v1 = CommonOps.extract(A,rowBlock+rowB,rowBlock+rowB+1,0,A.numCols);
            for( int j = 0; j < rowA+zeroOffset; j++ ) {
                v0.set(j,0.0);
            }
            v0.set(rowA+zeroOffset,1.0);

            double expected = VectorVectorMult.innerProd(v0,v1);

            D1Submatrix64F subAb = new D1Submatrix64F(Ab,rowBlock,A.numRows,0,A.numCols);

            double found = BlockHouseHolder.innerProdRow(r, subAb,rowA,subAb,rowB,zeroOffset);

            assertEquals(expected,found,1e-8);
        }
    }

    @Test
    public void divideElementsCol() {

        double div = 1.5;
        int col = 1;
        BlockMatrix64F A = BlockMatrixOps.createRandom(r*2+r-1,r,-1,1,rand,r);
        BlockMatrix64F A_orig = A.copy();

        BlockHouseHolder.divideElementsCol(r,new D1Submatrix64F(A),col,div);

        for( int i = col+1; i < A.numRows; i++ ) {
            assertEquals(A_orig.get(i,col)/div , A.get(i,col),1e-8);
        }
    }

    @Test
    public void scale_row() {

        double div = 1.5;
        int row = 1;
        BlockMatrix64F A = BlockMatrixOps.createRandom(r*2+r-1,r*2+1,-1,1,rand,r);
        BlockMatrix64F A_orig = A.copy();

        BlockHouseHolder.scale_row(r,new D1Submatrix64F(A),new D1Submatrix64F(A),row,1,div);

        // check the one
        assertEquals(div,A.get(row,row+1),1e-8);
        // check the rest
        for( int i = row+2; i < A.numCols; i++ ) {
            assertEquals(A_orig.get(row,i)*div , A.get(row,i),1e-8);
        }
    }

    @Test
    public void add_row() {
        int rowA=0;
        int rowB=1;
        int rowC=2;

        double alpha = 1.5;
        double beta = -0.7;

        for( int width = 1; width <= 3*r; width++ ) {
//            System.out.println("width "+width);
            int end = width;

            SimpleMatrix A = SimpleMatrix.random(r,width,-1,1,rand);
            SimpleMatrix B = SimpleMatrix.random(r,width,-1,1,rand);
            BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);
            BlockMatrix64F Bb = BlockMatrixOps.convert(B.getMatrix(),r);
            BlockMatrix64F Cb = Ab.copy();

            // turn A into householder vectors
            for( int i = 0; i < A.numRows(); i++ ) {
                for( int j = 0; j <= i; j++ ) {
                    if( A.isInBounds(i,j))
                        A.set(i,j,0);
                }
                if( A.isInBounds(i,i+1) )
                    A.set(i,i+1,1);
            }

            SimpleMatrix a = A.extractVector(true,rowA).scale(alpha);
            SimpleMatrix b = B.extractVector(true,rowB).scale(beta);
            SimpleMatrix c = a.plus(b);

            BlockHouseHolder.add_row(r,
                    new D1Submatrix64F(Ab),rowA, alpha,
                    new D1Submatrix64F(Bb),rowB, beta ,
                    new D1Submatrix64F(Cb),rowC, 1,end);

            // skip over the zeros
            for( int j = rowA+1; j < end; j++ ) {
                assertEquals(c.get(j), Cb.get(rowC,j),1e-8);
            }
        }
    }

    @Test
    public void computeTauAndDivideCol() {

        double max = 1.5;
        int col = 1;
        BlockMatrix64F A = BlockMatrixOps.createRandom(r*2+r-1,r,-1,1,rand,r);
        BlockMatrix64F A_orig = A.copy();

        // manual alg
        double expected = 0;
        for( int i = col; i < A.numRows; i++ ) {
            double val = A.get(i,col)/max;
            expected += val*val;
        }
        expected = Math.sqrt(expected);

        double found = BlockHouseHolder.computeTauAndDivideCol(r,new D1Submatrix64F(A),col,max);

        assertEquals(expected,found,1e-8);

        for( int i = col; i < A.numRows; i++ ) {
            assertEquals(A_orig.get(i,col)/max , A.get(i,col),1e-8);
        }

    }

    @Test
    public void computeTauAndDivideRow() {
        double max = 1.5;
        int row = 1;
        int colStart = row+1;
        BlockMatrix64F A = BlockMatrixOps.createRandom(r*2+r-1,r*2+1,-1,1,rand,r);
        BlockMatrix64F A_orig = A.copy();

        // manual alg
        double expected = 0;
        for( int j = colStart; j < A.numCols; j++ ) {
            double val = A.get(row,j)/max;
            expected += val*val;
        }
        expected = Math.sqrt(expected);

        double found = BlockHouseHolder.computeTauAndDivideRow(r,new D1Submatrix64F(A),row,colStart,max);

        assertEquals(expected,found,1e-8);

        for( int j = colStart; j < A.numCols; j++ ) {
            assertEquals(A_orig.get(row,j)/max , A.get(row,j),1e-8);
        }
    }

    @Test
    public void testFindMaxCol() {
        BlockMatrix64F A = BlockMatrixOps.createRandom(r*2+r-1,r,-1,1,rand,r);

        // make sure it ignores the first element
        A.set(0,1,100000);
        A.set(5,1,-2346);

        double max = BlockHouseHolder.findMaxCol(r,new D1Submatrix64F(A),1);

        assertEquals(2346,max,1e-8);
    }

    @Test
    public void testFindMaxRow() {
        BlockMatrix64F A = BlockMatrixOps.createRandom(r*2+r-1,r*2-1,-1,1,rand,r);

        // make sure it ignores the first element
        A.set(1,1,100000);
        A.set(1,4,-2346);

        double max = BlockHouseHolder.findMaxRow(r,new D1Submatrix64F(A),1,2);

        assertEquals(2346,max,1e-8);
    }

    @Test
    public void computeW_Column() {
        double betas[] = new double[]{1.2,2,3};

        A = SimpleMatrix.random(r*2+r-1,r,-1,1,rand);

        // Compute W directly using SimpleMatrix
        SimpleMatrix V = A.extractMatrix(0,A.numRows(),0,1);
        V.set(0,0,1);
        SimpleMatrix Y = V;
        SimpleMatrix W = V.scale(-betas[0]);

        for( int i = 1; i < A.numCols(); i++ ) {
            V = A.extractMatrix(0,A.numRows(),i,i+1);

            for( int j = 0; j < i; j++ )
                V.set(j,0,0);
            V.set(i,0,1);

            SimpleMatrix z = V.plus(W.mult(Y.transpose().mult(V))).scale(-betas[i]);
            W = W.combine(0,i,z);
            Y = Y.combine(0,i,V);
        }

        // now compute it using the block matrix stuff
        double temp[] = new double[ r ];

        BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);
        BlockMatrix64F Wb = new BlockMatrix64F(Ab.numRows,Ab.numCols,r);

        D1Submatrix64F Ab_sub = new D1Submatrix64F(Ab);
        D1Submatrix64F Wb_sub = new D1Submatrix64F(Wb);

        BlockHouseHolder.computeW_Column(r,Ab_sub,Wb_sub,temp,betas,0);

        // see if the result is the same
        assertTrue(GenericMatrixOps.isEquivalent(Wb,W.getMatrix(),1e-8));
    }

    @Test
    public void initializeW() {
        initMatrices(r-1);

        double beta = 1.5;

        BlockMatrix64F Wb = BlockMatrixOps.convert(W.getMatrix(),r);
        BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);

        D1Submatrix64F Wb_sub = new D1Submatrix64F(Wb,0, W.numRows(), 0, r);
        D1Submatrix64F Yb_sub = new D1Submatrix64F(Ab,0, A.numRows(), 0, r);

        BlockHouseHolder.initializeW(r,Wb_sub,Yb_sub,r,beta);

        assertEquals(-beta,Wb.get(0,0),1e-8);

        for( int i = 1; i < Wb.numRows; i++ ) {
            assertEquals(-beta*Ab.get(i,0),Wb.get(i,0),1e-8);
        }
    }

    @Test
    public void computeZ() {
        int M = r-1;
        initMatrices(M);

        double beta = 2.5;

        BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);
        BlockMatrix64F Aw = BlockMatrixOps.convert(W.getMatrix(),r);

        // need to extract only the elements in W that are currently being used when
        // computing the expected Z
        W = W.extractMatrix(0,W.numRows(),0,M);
        SimpleMatrix T = SimpleMatrix.random(M,1,-1,1,rand);

        // -beta * (V + W*T)
        SimpleMatrix expected = V.plus(W.mult(T)).scale(-beta);

        BlockHouseHolder.computeZ(r,new D1Submatrix64F(Ab,0, A.numRows(), 0, r),new D1Submatrix64F(Aw,0, A.numRows(), 0, r),
                M,T.getMatrix().data,beta);

        for( int i = 0; i < A.numRows(); i++ ) {
            assertEquals(expected.get(i),Aw.get(i,M),1e-8);
        }
    }

    @Test
    public void computeY_t_V() {
        int M = r-2;
        initMatrices(M);

        // Y'*V
        SimpleMatrix expected = Y.transpose().mult(V);

        BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);
        double found[] = new double[ M ];

        BlockHouseHolder.computeY_t_V(r,new D1Submatrix64F(Ab,0, A.numRows(), 0, r),M,found);

        for( int i = 0; i < M; i++ ) {
            assertEquals(expected.get(i),found[i],1e-8);
        }
    }

    private void initMatrices( int M ) {
        A = SimpleMatrix.random(r*2+r-1,r,-1,1,rand);

        // create matrices that are used to test
        Y = A.extractMatrix(0,A.numRows(),0,M);
        V = A.extractMatrix(0,A.numRows(),M,M+1);

        // add in zeros and ones
        setZerosY();
        for( int i = 0; i < M; i++ ) {
            V.set(i,0);
        }
        V.set(M,1);

        W = SimpleMatrix.random(r*2+r-1,r,-1,1,rand);
    }

    private void setZerosY() {
        for( int j = 0; j < Y.numCols(); j++ ) {
            for( int i = 0; i < j; i++ ) {
                Y.set(i,j,0);
            }
            Y.set(j,j,1);
        }
    }
}
