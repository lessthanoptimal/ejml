package org.ejml.alg.block.decomposition.qr;

import org.ejml.alg.block.BlockInnerMultiplication;
import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.block.decomposition.BlockQRDecomposition;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.SimpleMatrix;
import org.ejml.ops.CommonOps;


/**
 * @author Peter Abeles
 */
// todo comment
public class BlockMatrix64HouseholderQR implements BlockQRDecomposition {


    BlockMatrix64F dataA;

    BlockMatrix64F dataW = new BlockMatrix64F(1,1);
    BlockMatrix64F dataWTA = new BlockMatrix64F(1,1);

    int blockLength;
    D1Submatrix64F A = new D1Submatrix64F();
    D1Submatrix64F Y = new D1Submatrix64F();
    D1Submatrix64F W = new D1Submatrix64F(dataW);
    D1Submatrix64F WTA = new D1Submatrix64F(dataWTA);
    double temp[] = new double[1];
    double gammas[] = new double[1];

    public BlockMatrix64F getQR() {
        return dataA;
    }

    @Override
    public BlockMatrix64F getQ(BlockMatrix64F Q, boolean compact) {
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
        // Q = Q*(I - &gamma; W*Y^T)
        // R = Q^T*A  = Q3^T * Q2^T * Q1^t * A
        // Q1 * Q2 * Q3 R = Q*R = A

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

            // compute householder stuff
            BlockHouseHolder.computeW_Column(blockLength,Y,W,temp, gammas,Y.col0);

//            SimpleMatrix sW = W.extract();
//            SimpleMatrix sY = Y.extract();
//            SimpleMatrix sQ = subQ.extract();
//
//            for( int j = 0; j < blockLength; j++ ) {
//                for( int k = 0; k < j; k++ ) {
//                    sY.set(k,j,0);
//                }
//                sY.set(j,j,1);
//            }
//
//            sQ.plus(sW.mult(sY.transpose().mult(sQ))).print();

            BlockHouseHolder.multTransA(blockLength,Y,subQ,WTA);
            BlockInnerMultiplication.multAdd(blockLength,W,WTA,subQ);
        }

        return Q;
    }

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

    @Override
    public boolean decompose(BlockMatrix64F orig) {

        setup(orig);

        int m = Math.min(orig.numCols,orig.numRows);

        for( int j = 0; j < m; j += blockLength ) {
            Y.col0 = j;
            Y.col1 = Math.min( orig.numCols , Y.col0 + blockLength );
            W.col1 = Y.col1 - Y.col0;
            Y.row0 = j;
            W.row0 = j;
            // compute the QR decomposition of the left most block column
            if( !BlockHouseHolder.decomposeQR_block_col(blockLength,Y,gammas) ) {
                return false;
            }
            // Update the remainder of the matrix using the reflectors just computed
            updateA(A);
        }

        return true;
    }

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
     * A = (I + W Y<sup>T</sup>)<sup>T</sup>A<BR>
     * A = A + Y (W<sup>T</sup>A)<BR>
     * where A is a submatrix.
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
            BlockInnerMultiplication.multTransA(blockLength,W,A,WTA);
            BlockHouseHolder.multAdd_zeros(blockLength,Y,WTA,A);
        }
    }

    @Override
    public boolean modifyInput() {
        return true;
    }
}
