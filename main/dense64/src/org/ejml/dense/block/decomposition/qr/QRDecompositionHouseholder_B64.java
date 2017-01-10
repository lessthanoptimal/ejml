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

package org.ejml.dense.block.decomposition.qr;

import org.ejml.data.D1Submatrix_F64;
import org.ejml.data.DMatrixBlock_F64;
import org.ejml.dense.block.MatrixMult_B64;
import org.ejml.dense.block.MatrixOps_B64;
import org.ejml.interfaces.decomposition.QRDecomposition;


/**
 * <p>
 * QR decomposition for {@link DMatrixBlock_F64} using householder reflectors.  The decomposition is
 * performed by computing a QR decomposition for each block column as is normally done, see {@link org.ejml.dense.row.decomposition.qr.QRDecompositionHouseholder_R64}.
 * The reflectors are then combined and applied to the remainder of the matrix.  This process is repeated
 * until all the block columns have been processed
 * </p>
 *
 * <p>
 * The input matrix is modified and used to store the decomposition.  Reflectors are stored in the lower triangle
 * columns.  The first element of the reflector is implicitly assumed to be one.
 * </p>
 *
 * <p>
 * Each iteration can be sketched as follows:
 * <pre>
 * QR_Decomposition( A(:,i-r to i) )
 * W=computeW( A(:,i-r to i) )
 * A(:,i:n) = (I + W*Y<sup>T</sup>)<sup>T</sup>A(:,i:n)
 * </pre>
 * Where r is the block size, i is the submatrix being considered, A is the input matrix,
 * Y is a matrix containing the reflectors just computed,
 * and W is computed using {@link BlockHouseHolder_B64#computeW_Column}.
 * </p>
 *
 * <p>
 * Based upon "Block Householder QR Factorization" pg 255 in "Matrix Computations"
 * 3rd Ed. 1996 by Gene H. Golub and Charles F. Van Loan.
 * </p>
 *
 * @author Peter Abeles
 */
public class QRDecompositionHouseholder_B64
        implements QRDecomposition<DMatrixBlock_F64> {

    // the input matrix which is overwritten with the decomposition.
    // Reflectors are stored in the lower triangular portion. The R matrix is stored
    // in the upper triangle portion
    private DMatrixBlock_F64 dataA;

    // where the computed W matrix is stored
    private DMatrixBlock_F64 dataW = new DMatrixBlock_F64(1,1);
    // Matrix used to store an intermediate calculation
    private DMatrixBlock_F64 dataWTA = new DMatrixBlock_F64(1,1);

    // size of the inner matrix block.
    private int blockLength;
    
    // The submatrices which are being manipulated in each iteration
    private D1Submatrix_F64 A = new D1Submatrix_F64();
    private D1Submatrix_F64 Y = new D1Submatrix_F64();
    private D1Submatrix_F64 W = new D1Submatrix_F64(dataW);
    private D1Submatrix_F64 WTA = new D1Submatrix_F64(dataWTA);
    private double temp[] = new double[1];
    // stores the computed gammas
    private double gammas[] = new double[1];

    // save the W matrix the first time it is computed in the decomposition
    private boolean saveW = false;

    /**
     * This is the input matrix after it has been overwritten with the decomposition.
     *
     * @return Internal matrix used to store decomposition.
     */
    public DMatrixBlock_F64 getQR() {
        return dataA;
    }

    /**
     * <p>
     * Sets if it should internally save the W matrix before performing the decomposition.  Must
     * be set before decomposition the matrix.
     * </p>
     *
     * <p>
     * Saving W can result in about a 5% savings when solving systems around a height of 5k.  The
     * price is that it needs to save a matrix the size of the input matrix.
     * </p>
     *
     * @param saveW If the W matrix should be saved or not.
     */
    public void setSaveW(boolean saveW) {
        this.saveW = saveW;
    }

    /**
     * @inheritDoc
     */
    @Override
    public DMatrixBlock_F64 getQ(DMatrixBlock_F64 Q, boolean compact) {
        Q = initializeQ(Q, dataA.numRows , dataA.numCols  , blockLength , compact);
 
        applyQ(Q,true);

        return Q;
    }

    /**
     * Sanity checks the input or declares a new matrix.  Return matrix is an identity matrix.
     */
    public static DMatrixBlock_F64 initializeQ(DMatrixBlock_F64 Q,
                                              int numRows , int numCols , int blockLength ,
                                              boolean compact) {
        int minLength = Math.min(numRows,numCols);
        if( compact ) {
            if( Q == null ) {
                Q = new DMatrixBlock_F64(numRows,minLength,blockLength);
                MatrixOps_B64.setIdentity(Q);
            } else {
                if( Q.numRows != numRows || Q.numCols != minLength ) {
                    throw new IllegalArgumentException("Unexpected matrix dimension. Found "+Q.numRows+" "+Q.numCols);
                } else {
                    MatrixOps_B64.setIdentity(Q);
                }
            }
        } else {
            if( Q == null ) {
                Q = new DMatrixBlock_F64(numRows,numRows,blockLength);
                MatrixOps_B64.setIdentity(Q);
            } else {
                if( Q.numRows != numRows || Q.numCols != numRows ) {
                    throw new IllegalArgumentException("Unexpected matrix dimension. Found "+Q.numRows+" "+Q.numCols);
                } else {
                    MatrixOps_B64.setIdentity(Q);
                }
            }
        }
        return Q;
    }

    /**
     * <p>
     * Multiplies the provided matrix by Q using householder reflectors.  This is more
     * efficient that computing Q then applying it to the matrix.
     * </p>
     *
     * <p>
     * B = Q * B
     * </p>
     *
     * @param B Matrix which Q is applied to.  Modified.
     */
    public void applyQ( DMatrixBlock_F64 B ) {
        applyQ(B,false);
    }

    /**
     * Specialized version of applyQ() that allows the zeros in an identity matrix
     * to be taken advantage of depending on if isIdentity is true or not.
     *
     * @param B
     * @param isIdentity If B is an identity matrix.
     */
    public void applyQ(DMatrixBlock_F64 B , boolean isIdentity ) {
        int minDimen = Math.min(dataA.numCols,dataA.numRows);

        D1Submatrix_F64 subB = new D1Submatrix_F64(B);

        W.col0 = W.row0 = 0;
        Y.row1 = W.row1 = dataA.numRows;
        WTA.row0 = WTA.col0 = 0;

        int start = minDimen - minDimen % blockLength;
        if( start == minDimen )
            start -= blockLength;
        if( start < 0 )
            start = 0;

        // (Q1^T * (Q2^T * (Q3^t * A)))
        for( int i = start; i >= 0; i -= blockLength ) {

            Y.col0 = i;
            Y.col1 = Math.min(Y.col0+blockLength,dataA.numCols);
            Y.row0 = i;
            if( isIdentity )
                subB.col0 = i;
            subB.row0 = i;

            setW();
            WTA.row1 = Y.col1-Y.col0;
            WTA.col1 = subB.col1-subB.col0;
            WTA.original.reshape(WTA.row1,WTA.col1,false);

            // Compute W matrix from reflectors stored in Y
            if( !saveW )
                BlockHouseHolder_B64.computeW_Column(blockLength,Y,W,temp, gammas,Y.col0);

            // Apply the Qi to Q
            BlockHouseHolder_B64.multTransA_vecCol(blockLength,Y,subB,WTA);
            MatrixMult_B64.multPlus(blockLength,W,WTA,subB);
        }
    }

    /**
     * <p>
     * Multiplies the provided matrix by Q<sup>T</sup> using householder reflectors.  This is more
     * efficient that computing Q then applying it to the matrix.
     * </p>
     *
     * <p>
     * Q = Q*(I - &gamma; W*Y^T)<br>
     * QR = A => R = Q^T*A  = (Q3^T * (Q2^T * (Q1^t * A)))
     * </p>
     *
     * @param B Matrix which Q is applied to.  Modified.
     */
    public void applyQTran( DMatrixBlock_F64 B ) {
        int minDimen = Math.min(dataA.numCols,dataA.numRows);

        D1Submatrix_F64 subB = new D1Submatrix_F64(B);

        W.col0 = W.row0 = 0;
        Y.row1 = W.row1 = dataA.numRows;
        WTA.row0 = WTA.col0 = 0;

        // (Q3^T * (Q2^T * (Q1^t * A)))
        for( int i = 0; i < minDimen; i += blockLength ) {

            Y.col0 = i;
            Y.col1 = Math.min(Y.col0+blockLength,dataA.numCols);
            Y.row0 = i;
            
            subB.row0 = i;
//            subB.row1 = B.numRows;
//            subB.col0 = 0;
//            subB.col1 = B.numCols;

            setW();
//            W.original.reshape(W.row1,W.col1,false);
            WTA.row0 = 0;
            WTA.col0 = 0;
            WTA.row1 = W.col1-W.col0;
            WTA.col1 = subB.col1-subB.col0;
            WTA.original.reshape(WTA.row1,WTA.col1,false);

            // Compute W matrix from reflectors stored in Y
            if( !saveW )
                BlockHouseHolder_B64.computeW_Column(blockLength,Y,W,temp, gammas,Y.col0);

            // Apply the Qi to Q
            MatrixMult_B64.multTransA(blockLength,W,subB,WTA);
            BlockHouseHolder_B64.multAdd_zeros(blockLength,Y,WTA,subB);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public DMatrixBlock_F64 getR(DMatrixBlock_F64 R, boolean compact) {
        int min = Math.min(dataA.numRows,dataA.numCols);

        if( R == null ) {
            if( compact ) {
                R = new DMatrixBlock_F64(min,dataA.numCols,blockLength);
            } else {
                R = new DMatrixBlock_F64(dataA.numRows,dataA.numCols,blockLength);
            }
        } else {
            if( compact ) {
                if( R.numCols != dataA.numCols || R.numRows != min ) {
                    throw new IllegalArgumentException("Unexpected dimension.");
                }
            } else if( R.numCols != dataA.numCols || R.numRows != dataA.numRows ) {
                throw new IllegalArgumentException("Unexpected dimension.");
            }
        }

        MatrixOps_B64.zeroTriangle(false,R);
        MatrixOps_B64.copyTriangle(true,dataA,R);

        return R;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean decompose(DMatrixBlock_F64 orig) {
        setup(orig);

        int m = Math.min(orig.numCols,orig.numRows);

        // process the matrix one column block at a time and overwrite the input matrix
        for( int j = 0; j < m; j += blockLength ) {
            Y.col0 = j;
            Y.col1 = Math.min( orig.numCols , Y.col0 + blockLength );
            Y.row0 = j;

            // compute the QR decomposition of the left most block column
            // this overwrites the original input matrix
            if( !BlockHouseHolder_B64.decomposeQR_block_col(blockLength,Y,gammas) ) {
                return false;
            }

            // Update the remainder of the matrix using the reflectors just computed
            updateA(A);
        }

        return true;
    }

    /**
     * Adjust submatrices and helper data structures for the input matrix.  Must be called
     * before the decomposition can be computed.
     *
     * @param orig
     */
    private void setup(DMatrixBlock_F64 orig) {
        blockLength = orig.blockLength;
        dataW.blockLength = blockLength;
        dataWTA.blockLength = blockLength;

        this.dataA = orig;
        A.original = dataA;

        int l = Math.min(blockLength,orig.numCols);
        dataW.reshape(orig.numRows,l,false);
        dataWTA.reshape(l,orig.numRows,false);
        Y.original = orig;
        Y.row1 = W.row1 = orig.numRows;
        if( temp.length < blockLength )
            temp = new double[blockLength];
        if( gammas.length < orig.numCols )
            gammas = new double[ orig.numCols ];

        if( saveW ) {
            dataW.reshape(orig.numRows,orig.numCols,false);
        }
    }

    /**
     * <p>
     * A = (I + W Y<sup>T</sup>)<sup>T</sup>A<BR>
     * A = A + Y (W<sup>T</sup>A)<BR>
     * <br>
     * where A is a submatrix of the input matrix.
     * </p>
     */
    protected void updateA( D1Submatrix_F64 A )
    {
        setW();

        A.row0 = Y.row0;
        A.row1 = Y.row1;
        A.col0 = Y.col1;
        A.col1 = Y.original.numCols;

        WTA.row0 = 0;
        WTA.col0 = 0;
        WTA.row1 = W.col1-W.col0;
        WTA.col1 = A.col1-A.col0;
        WTA.original.reshape(WTA.row1,WTA.col1,false);

        if( A.col1 > A.col0 ) {
            BlockHouseHolder_B64.computeW_Column(blockLength,Y,W,temp, gammas,Y.col0);

            MatrixMult_B64.multTransA(blockLength,W,A,WTA);
            BlockHouseHolder_B64.multAdd_zeros(blockLength,Y,WTA,A);
        } else if( saveW ) {
            BlockHouseHolder_B64.computeW_Column(blockLength,Y,W,temp, gammas,Y.col0);
        }
    }

    /**
     * Sets the submatrix of W up give Y is already configured and if it is being cached or not.
     */
    private void setW() {
        if( saveW ) {
            W.col0 = Y.col0;
            W.col1 = Y.col1;
            W.row0 = Y.row0;
            W.row1 = Y.row1;
        } else {
            W.col1 = Y.col1 - Y.col0;
            W.row0 = Y.row0;
        }
    }

    /**
     * The input matrix is always modified.
     *
     * @return Returns true since the input matrix is modified.
     */
    @Override
    public boolean inputModified() {
        return true;
    }
}
