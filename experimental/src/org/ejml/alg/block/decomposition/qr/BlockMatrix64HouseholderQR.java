/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.block.decomposition.qr;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.block.BlockMultiplication;
import org.ejml.alg.block.decomposition.BlockQRDecomposition;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;


/**
 * <p>
 * QR decomposition for {@link BlockMatrix64F} using householder reflectors.  The decomposition is
 * performed by computing a QR decomposition for each block column as is normally done, see {@link org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholder}.
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
 * QR_Decomposition( A(:,i-r,i) )
 * W=computeW( A(:,i-r,i) )
 * A(:,i:n) = (I + W*Y<sup>T</sup>)<sup>T</sup>A(:,i:n)
 * </pre>
 * Where r is the block size, i is the submatrix being considered, A is the input matrix,
 * Y is a matrix containing the reflectors just computed,
 * and W is computed using {@link BlockHouseHolder#computeW_Column}.
 * </p>
 *
 * <p>
 * Based upon "Block Householder QR Factorization" pg 255 in "Matrix Computations"
 * 3rd Ed. 1996 by Gene H. Golub and Charles F. Van Loan.
 * </p>
 *
 * @author Peter Abeles
 */
public class BlockMatrix64HouseholderQR implements BlockQRDecomposition {

    // the input matrix which is overwritten with the decomposition.
    // Reflectors are stored in the lower triangular portion. The R matrix is stored
    // in the upper triangle portion
    private BlockMatrix64F dataA;

    // where the computed W matrix is stored
    private BlockMatrix64F dataW = new BlockMatrix64F(1,1);
    // Matrix used to store an intermediate calculation
    private BlockMatrix64F dataWTA = new BlockMatrix64F(1,1);

    // size of the inner matrix block.
    private int blockLength;
    
    // The submatrices which are being manipulated in each iteration
    private D1Submatrix64F A = new D1Submatrix64F();
    private D1Submatrix64F Y = new D1Submatrix64F();
    private D1Submatrix64F W = new D1Submatrix64F(dataW);
    private D1Submatrix64F WTA = new D1Submatrix64F(dataWTA);
    private double temp[] = new double[1];
    // stores the computed gammas
    private double gammas[] = new double[1];

    /**
     * This is the input matrix after it has been overwritten with the decomposition.
     *
     * @return Internal matrix used to store decomposition.
     */
    public BlockMatrix64F getQR() {
        return dataA;
    }

    /**
     * @inheritDoc
     */
    @Override
    public BlockMatrix64F getQ(BlockMatrix64F Q, boolean compact) {

        // sanity check input Q matrix or declare a new matrix to store results
        int minLength = Math.min(dataA.numRows,dataA.numCols);
        if( compact ) {
            if( Q == null ) {
                Q = new BlockMatrix64F(dataA.numRows,minLength,blockLength);
                BlockMatrixOps.setIdentity(Q);
            } else {
                if( Q.numRows != dataA.numRows || Q.numCols != minLength ) {
                    throw new IllegalArgumentException("Unexpected matrix dimension.");
                } else {
                    BlockMatrixOps.setIdentity(Q);
                }
            }
        } else {
            if( Q == null ) {
                Q = new BlockMatrix64F(dataA.numRows,dataA.numRows,blockLength);
                BlockMatrixOps.setIdentity(Q);
            } else {
                if( Q.numRows != dataA.numRows || Q.numCols != dataA.numRows ) {
                    throw new IllegalArgumentException("Unexpected matrix dimension.");
                } else {
                    BlockMatrixOps.setIdentity(Q);
                }
            }
        }

        D1Submatrix64F subQ = new D1Submatrix64F(Q);

        int minDimen = Math.min(dataA.numCols,dataA.numRows);

        // Apply reflectors in reverse order.  See equations below.  Note parentheses
        // Q = Q*(I - &gamma; W*Y^T)
        // R = Q^T*A  = (Q3^T * (Q2^T * (Q1^t * A)))
        // (Q1 * (Q2 * (Q3 R))) = Q*R = A

        W.col0 = W.row0 = 0;
        Y.row1 = W.row1 = dataA.numRows;
        WTA.row0 = WTA.col0 = 0;

        int start = minDimen - minDimen % blockLength;
        if( start == minDimen )
            start -= blockLength;
        if( start < 0 )
            start = 0;

        for( int i = start; i >= 0; i -= blockLength ) {

            Y.col0 = i;
            Y.col1 = Math.min(Y.col0+blockLength,dataA.numCols);
            Y.row0 = i;
            subQ.col0 = i;
            subQ.row0 = i;

            W.col1 = Y.col1-Y.col0;
            W.row0 = Y.row0;
            WTA.row1 = Y.col1-Y.col0;
            WTA.col1 = subQ.col1-subQ.col0;
            WTA.original.reshape(WTA.row1,WTA.col1,false);

            // Compute W matrix from reflectors stored in Y
            BlockHouseHolder.computeW_Column(blockLength,Y,W,temp, gammas,Y.col0);

            // Apply the Qi to Q
            BlockHouseHolder.multTransA(blockLength,Y,subQ,WTA);
            BlockMultiplication.multPlus(blockLength,W,WTA,subQ);
        }

        return Q;
    }

    /**
     * @inheritDoc
     */
    @Override
    public BlockMatrix64F getR(BlockMatrix64F R, boolean compact) {
        int min = Math.min(dataA.numRows,dataA.numCols);

        if( R == null ) {
            if( compact ) {
                R = new BlockMatrix64F(min,dataA.numCols,blockLength);
            } else {
                R = new BlockMatrix64F(dataA.numRows,dataA.numCols,blockLength);
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

        BlockMatrixOps.zeroTriangle(false,R);
        BlockMatrixOps.copyTriangle(true,dataA,R);

        return R;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean decompose(BlockMatrix64F orig) {
        setup(orig);

        int m = Math.min(orig.numCols,orig.numRows);

        // process the matrix one column block at a time and overwrite the input matrix
        for( int j = 0; j < m; j += blockLength ) {
            Y.col0 = j;
            Y.col1 = Math.min( orig.numCols , Y.col0 + blockLength );
            W.col1 = Y.col1 - Y.col0;
            Y.row0 = j;
            W.row0 = j;

            // compute the QR decomposition of the left most block column
            // this overwrites the original input matrix
            if( !BlockHouseHolder.decomposeQR_block_col(blockLength,Y,gammas) ) {
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
    private void setup(BlockMatrix64F orig) {
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
    }

    /**
     * <p>
     * A = (I + W Y<sup>T</sup>)<sup>T</sup>A<BR>
     * A = A + Y (W<sup>T</sup>A)<BR>
     * <br>
     * where A is a submatrix of the input matrix.
     * </p>
     */
    protected void updateA( D1Submatrix64F A )
    {
        BlockHouseHolder.computeW_Column(blockLength,Y,W,temp, gammas,Y.col0);

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
            BlockMultiplication.multTransA(blockLength,W,A,WTA);
            BlockHouseHolder.multAdd_zeros(blockLength,Y,WTA,A);
        }
    }

    /**
     * The input matrix is always modified.
     *
     * @return Returns true since the input matrix is modified.
     */
    @Override
    public boolean modifyInput() {
        return true;
    }
}
