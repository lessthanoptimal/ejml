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

package org.ejml.dense.block.decomposition.qr;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRBlock;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FSubmatrixD1;
import org.ejml.dense.block.MatrixOps_FDRB;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.decomposition.qr.QRDecompositionHouseholderTran_FDRM;
import org.ejml.dense.row.mult.VectorVectorMult_FDRM;
import org.ejml.generic.GenericMatrixOps_F32;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestBlockHouseHolder_FDRB {

    Random rand = new Random(234);

    // the block length
    int r = 3;

    SimpleMatrix A, Y,V,W;

    @Test
    public void decomposeQR_block_col() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(r*2+r-1,r,-1,1,rand);
        FMatrixRBlock Ab = MatrixOps_FDRB.convert(A,r);

        QRDecompositionHouseholderTran_FDRM algTest = new QRDecompositionHouseholderTran_FDRM();
        assertTrue(algTest.decompose(A));

        float gammas[] = new float[A.numCols];
        BlockHouseHolder_FDRB.decomposeQR_block_col(r,new FSubmatrixD1(Ab),gammas);

        FMatrixRMaj expected = CommonOps_FDRM.transpose(algTest.getQR(),null);

        assertTrue(GenericMatrixOps_F32.isEquivalent(expected,Ab,UtilEjml.TEST_F32));
    }

    @Test
    public void rank1UpdateMultR_Col() {

        // check various sized matrices
        float gamma = 2.5f;
        A = SimpleMatrix.random_FDRM(r*2+r-1,r*2-1,-1,1,rand);

        SimpleMatrix U = A.extractMatrix(0,A.numRows(),1,2);
        U.set(0,0,0);
        U.set(1,0,1);

        SimpleMatrix V = A.extractMatrix(0,A.numRows(),2,3);
        SimpleMatrix expected = V.minus(U.mult(U.transpose().mult(V)).scale(gamma));

        FMatrixRBlock Ab = MatrixOps_FDRB.convert((FMatrixRMaj)A.getMatrix(),r);

        BlockHouseHolder_FDRB.rank1UpdateMultR_Col(r,new FSubmatrixD1(Ab),1,gamma);

        for( int i = 1; i < expected.numRows(); i++ ) {
            assertEquals(expected.get(i,0),Ab.get(i,2),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void rank1UpdateMultR_TopRow() {
        float gamma = 2.5f;
        A = SimpleMatrix.random_FDRM(r*2+r-1,r*2-1, -1.0f , 1.0f ,rand);

        SimpleMatrix U = A.extractMatrix(0,A.numRows(),1,2);
        U.set(0,0,0);
        U.set(1,0,1);

        FMatrixRBlock Ab = MatrixOps_FDRB.convert((FMatrixRMaj)A.getMatrix(),r);

        BlockHouseHolder_FDRB.rank1UpdateMultR_TopRow(r,new FSubmatrixD1(Ab),1,gamma);

        // check all the columns now
        for( int i = 0; i < r; i++ ) {
            for( int j = r; j < A.numCols(); j++ ) {
                SimpleMatrix V = A.extractMatrix(0,A.numRows(),j,j+1);
                SimpleMatrix expected = V.minus(U.mult(U.transpose().mult(V)).scale(gamma));

                assertEquals(expected.get(i,0),Ab.get(i,j),UtilEjml.TEST_F32,i+" "+j);
            }
        }
    }

    @Test
    public void rank1UpdateMultL_Row() {
        float gamma = 2.5f;
        A = SimpleMatrix.random_FDRM(r*2+r-1,r*2+r-1, -1.0f , 1.0f ,rand);

        SimpleMatrix U = A.extractMatrix(1,2,0,A.numCols()).transpose();
        U.set(0,0);
        U.set(1,1);

        SimpleMatrix expected = A.minus( A.mult(U).mult(U.transpose()).scale(gamma) );

        FMatrixRBlock Ab = MatrixOps_FDRB.convert((FMatrixRMaj)A.getMatrix(),r);

        BlockHouseHolder_FDRB.rank1UpdateMultL_Row(r,new FSubmatrixD1(Ab),1,1,gamma);

        for( int j = 1; j < expected.numCols(); j++ ) {
            assertEquals(expected.get(2,j),Ab.get(2,j),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void rank1UpdateMultL_LeftCol() {
        float gamma = 2.5f;
        A = SimpleMatrix.random_FDRM(r*2+r-1,r*2+r-1, -1.0f , 1.0f ,rand);

        int row = 0;
        int zeroOffset = 1;
        SimpleMatrix U = A.extractMatrix(row,row+1,0,A.numCols()).transpose();
        for( int i = 0; i < row+zeroOffset; i++ )
            U.set(i,0);
        U.set(row+zeroOffset,1);

        SimpleMatrix expected = A.minus( A.mult(U).mult(U.transpose()).scale(gamma) );

        FMatrixRBlock Ab = MatrixOps_FDRB.convert((FMatrixRMaj)A.getMatrix(),r);

        BlockHouseHolder_FDRB.rank1UpdateMultL_LeftCol(r,new FSubmatrixD1(Ab),row,gamma,zeroOffset);

        for( int i = r; i < A.numRows(); i++ ) {
            for( int j = 0; j < r; j++ ) {
                assertEquals(expected.get(i,j),Ab.get(i,j),UtilEjml.TEST_F32);
            }
        }
    }

    /**
     * Check inner product when column blocks have two different widths
     */
    @Test
    public void innerProdCol() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(r*2+r-1,r*3-1,-1,1,rand);
        FMatrixRBlock Ab = MatrixOps_FDRB.convert(A,r);

        int row = 0;
        int innerCol = 1;
        for( int colBlock = 0; colBlock < r*2; colBlock+=r) {
            int colA = colBlock+innerCol;
            int colB = colA+innerCol+1;
            int widthA = Math.min(r,A.numCols - (colA-colA%r));
            int widthB = Math.min(r,A.numCols - (colB-colB%r));

            FMatrixRMaj v0 = CommonOps_FDRM.extract(A,row,A.numRows,colA,colA+1);
            FMatrixRMaj v1 = CommonOps_FDRM.extract(A,row,A.numRows,colB,colB+1);
            for( int j = 0; j < innerCol; j++ ) {
                v0.set(j,0.0f);
            }
            v0.set(innerCol,1.0f);

            float expected = VectorVectorMult_FDRM.innerProd(v0,v1);

            FSubmatrixD1 subAb = new FSubmatrixD1(Ab,row,A.numRows,colBlock,A.numCols);

            float found = BlockHouseHolder_FDRB.innerProdCol(r,subAb,colA-colBlock,widthA,colB-colBlock,widthB);

            assertEquals(expected,found,UtilEjml.TEST_F32);
        }
    }


    @Test
    public void innerProdRow() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(r*3-1,r*2+r-1,-1,1,rand);
        FMatrixRBlock Ab = MatrixOps_FDRB.convert(A,r);

        int zeroOffset = 1;
        for( int rowBlock = 0; rowBlock < r*2; rowBlock+=r) {
            int rowA = 2;
            int rowB = 1;

            FMatrixRMaj v0 = CommonOps_FDRM.extract(A,rowBlock+rowA,rowBlock+rowA+1,0,A.numCols);
            FMatrixRMaj v1 = CommonOps_FDRM.extract(A,rowBlock+rowB,rowBlock+rowB+1,0,A.numCols);
            for( int j = 0; j < rowA+zeroOffset; j++ ) {
                v0.set(j,0.0f);
            }
            v0.set(rowA+zeroOffset,1.0f);

            float expected = VectorVectorMult_FDRM.innerProd(v0,v1);

            FSubmatrixD1 subAb = new FSubmatrixD1(Ab,rowBlock,A.numRows,0,A.numCols);

            float found = BlockHouseHolder_FDRB.innerProdRow(r, subAb,rowA,subAb,rowB,zeroOffset);

            assertEquals(expected,found,UtilEjml.TEST_F32);
        }
    }

    @Test
    public void divideElementsCol() {

        float div = 1.5f;
        int col = 1;
        FMatrixRBlock A = MatrixOps_FDRB.createRandom(r*2+r-1,r,-1,1,rand,r);
        FMatrixRBlock A_orig = A.copy();

        BlockHouseHolder_FDRB.divideElementsCol(r,new FSubmatrixD1(A),col,div);

        for( int i = col+1; i < A.numRows; i++ ) {
            assertEquals(A_orig.get(i,col)/div , A.get(i,col),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void scale_row() {

        float div = 1.5f;
        int row = 1;
        FMatrixRBlock A = MatrixOps_FDRB.createRandom(r*2+r-1,r*2+1,-1,1,rand,r);
        FMatrixRBlock A_orig = A.copy();

        BlockHouseHolder_FDRB.scale_row(r,new FSubmatrixD1(A),new FSubmatrixD1(A),row,1,div);

        // check the one
        assertEquals(div,A.get(row,row+1), UtilEjml.TEST_F32);
        // check the rest
        for( int i = row+2; i < A.numCols; i++ ) {
            assertEquals(A_orig.get(row,i)*div , A.get(row,i),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void add_row() {
        int rowA=0;
        int rowB=1;
        int rowC=2;

        float alpha = 1.5f;
        float beta = -0.7f;

        for( int width = 1; width <= 3*r; width++ ) {
//            System.out.println("width "+width);
            int end = width;

            SimpleMatrix A = SimpleMatrix.random_FDRM(r,width,-1.0f,1.0f,rand);
            SimpleMatrix B = SimpleMatrix.random_FDRM(r,width,-1.0f,1.0f,rand);
            FMatrixRBlock Ab = MatrixOps_FDRB.convert((FMatrixRMaj)A.getMatrix(),r);
            FMatrixRBlock Bb = MatrixOps_FDRB.convert((FMatrixRMaj)B.getMatrix(),r);
            FMatrixRBlock Cb = Ab.copy();

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

            BlockHouseHolder_FDRB.add_row(r,
                    new FSubmatrixD1(Ab),rowA, alpha,
                    new FSubmatrixD1(Bb),rowB, beta ,
                    new FSubmatrixD1(Cb),rowC, 1,end);

            // skip over the zeros
            for( int j = rowA+1; j < end; j++ ) {
                assertEquals(c.get(j), Cb.get(rowC,j),UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void computeTauAndDivideCol() {

        float max = 1.5f;
        int col = 1;
        FMatrixRBlock A = MatrixOps_FDRB.createRandom(r*2+r-1,r,-1,1,rand,r);
        FMatrixRBlock A_orig = A.copy();

        // manual dense
        float expected = 0;
        for( int i = col; i < A.numRows; i++ ) {
            float val = A.get(i,col)/max;
            expected += val*val;
        }
        expected = (float)Math.sqrt(expected);
        if( A.get(col,col) < 0 )
            expected *= -1;

        float found = BlockHouseHolder_FDRB.computeTauAndDivideCol(r,new FSubmatrixD1(A),col,max);

        assertEquals(expected,found,UtilEjml.TEST_F32);

        for( int i = col; i < A.numRows; i++ ) {
            assertEquals(A_orig.get(i,col)/max , A.get(i,col),UtilEjml.TEST_F32);
        }

    }

    @Test
    public void computeTauAndDivideRow() {
        float max = 1.5f;
        int row = 1;
        int colStart = row+1;
        FMatrixRBlock A = MatrixOps_FDRB.createRandom(r*2+r-1,r*2+1,-1,1,rand,r);
        FMatrixRBlock A_orig = A.copy();

        // manual dense
        float expected = 0;
        for( int j = colStart; j < A.numCols; j++ ) {
            float val = A.get(row,j)/max;
            expected += val*val;
        }
        expected = (float)Math.sqrt(expected);
        if( A.get(row,colStart) < 0 )
            expected *= -1;

        float found = BlockHouseHolder_FDRB.computeTauAndDivideRow(r,new FSubmatrixD1(A),row,colStart,max);

        assertEquals(expected,found,UtilEjml.TEST_F32);

        for( int j = colStart; j < A.numCols; j++ ) {
            assertEquals(A_orig.get(row,j)/max , A.get(row,j),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void testFindMaxCol() {
        FMatrixRBlock A = MatrixOps_FDRB.createRandom(r*2+r-1,r,-1,1,rand,r);

        // make sure it ignores the first element
        A.set(0,1,100000);
        A.set(5,1,-2346);

        float max = BlockHouseHolder_FDRB.findMaxCol(r,new FSubmatrixD1(A),1);

        assertEquals(2346,max,UtilEjml.TEST_F32);
    }

    @Test
    public void testFindMaxRow() {
        FMatrixRBlock A = MatrixOps_FDRB.createRandom(r*2+r-1,r*2-1,-1,1,rand,r);

        // make sure it ignores the first element
        A.set(1,1,100000);
        A.set(1,4,-2346);

        float max = BlockHouseHolder_FDRB.findMaxRow(r,new FSubmatrixD1(A),1,2);

        assertEquals(2346,max,UtilEjml.TEST_F32);
    }

    @Test
    public void computeW_Column() {
        float betas[] = new float[]{1.2f,2,3};

        A = SimpleMatrix.random_FDRM(r*2+r-1,r, -1.0f , 1.0f ,rand);

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
        float temp[] = new float[ r ];

        FMatrixRBlock Ab = MatrixOps_FDRB.convert((FMatrixRMaj)A.getMatrix(),r);
        FMatrixRBlock Wb = new FMatrixRBlock(Ab.numRows,Ab.numCols,r);

        FSubmatrixD1 Ab_sub = new FSubmatrixD1(Ab);
        FSubmatrixD1 Wb_sub = new FSubmatrixD1(Wb);

        BlockHouseHolder_FDRB.computeW_Column(r,Ab_sub,Wb_sub,temp,betas,0);

        // see if the result is the same
        assertTrue(GenericMatrixOps_F32.isEquivalent(Wb,(FMatrixRMaj)W.getMatrix(),UtilEjml.TEST_F32));
    }

    @Test
    public void initializeW() {
        initMatrices(r-1);

        float beta = 1.5f;

        FMatrixRBlock Wb = MatrixOps_FDRB.convert((FMatrixRMaj)W.getMatrix(),r);
        FMatrixRBlock Ab = MatrixOps_FDRB.convert((FMatrixRMaj)A.getMatrix(),r);

        FSubmatrixD1 Wb_sub = new FSubmatrixD1(Wb,0, W.numRows(), 0, r);
        FSubmatrixD1 Yb_sub = new FSubmatrixD1(Ab,0, A.numRows(), 0, r);

        BlockHouseHolder_FDRB.initializeW(r,Wb_sub,Yb_sub,r,beta);

        assertEquals(-beta,Wb.get(0,0),UtilEjml.TEST_F32);

        for( int i = 1; i < Wb.numRows; i++ ) {
            assertEquals(-beta*Ab.get(i,0),Wb.get(i,0),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void computeZ() {
        int M = r-1;
        initMatrices(M);

        float beta = 2.5f;

        FMatrixRBlock Ab = MatrixOps_FDRB.convert((FMatrixRMaj)A.getMatrix(),r);
        FMatrixRBlock Aw = MatrixOps_FDRB.convert((FMatrixRMaj)W.getMatrix(),r);

        // need to extract only the elements in W that are currently being used when
        // computing the expected Z
        W = W.extractMatrix(0,W.numRows(),0,M);
        SimpleMatrix T = SimpleMatrix.random_FDRM(M,1,-1,1,rand);

        // -beta * (V + W*T)
        SimpleMatrix expected = V.plus(W.mult(T)).scale(-beta);

        BlockHouseHolder_FDRB.computeZ(r,new FSubmatrixD1(Ab,0, A.numRows(), 0, r),
                new FSubmatrixD1(Aw,0, A.numRows(), 0, r),
                M,T.getFDRM().data,beta);

        for( int i = 0; i < A.numRows(); i++ ) {
            assertEquals(expected.get(i),Aw.get(i,M),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void computeY_t_V() {
        int M = r-2;
        initMatrices(M);

        // Y'*V
        SimpleMatrix expected = Y.transpose().mult(V);

        FMatrixRBlock Ab = MatrixOps_FDRB.convert(A.getFDRM(),r);
        float found[] = new float[ M ];

        BlockHouseHolder_FDRB.computeY_t_V(r,new FSubmatrixD1(Ab,0, A.numRows(), 0, r),M,found);

        for( int i = 0; i < M; i++ ) {
            assertEquals(expected.get(i),found[i],UtilEjml.TEST_F32);
        }
    }

    private void initMatrices( int M ) {
        A = SimpleMatrix.random_FDRM(r*2+r-1,r, -1.0f , 1.0f ,rand);

        // create matrices that are used to test
        Y = A.extractMatrix(0,A.numRows(),0,M);
        V = A.extractMatrix(0,A.numRows(),M,M+1);

        // add in zeros and ones
        setZerosY();
        for( int i = 0; i < M; i++ ) {
            V.set(i,0);
        }
        V.set(M,1);

        W = SimpleMatrix.random_FDRM(r*2+r-1,r, -1.0f , 1.0f ,rand);
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
