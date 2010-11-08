package org.ejml.alg.block.decomposition.qr;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderTran;
import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.SimpleMatrix;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestBlockMatrix64HouseholderQR {
    Random rand = new Random(324);

    int r = 3;

    @Test
    public void checkInternalData() {
        checkSize(r-1,r-1);
        checkSize(r,r);
        checkSize(3*r,r);
        checkSize(r,3*r);
        checkSize(3*r,3*r);
        checkSize(3*r+r-1,r);
        checkSize(r,3*r+r-1);
    }

    private void checkSize( int numRows , int numCols ) {
        DenseMatrix64F A = RandomMatrices.createRandom(numRows,numCols,-1,1,rand);
        BlockMatrix64F Ab = BlockMatrixOps.convert(A,r);

        QRDecompositionHouseholderTran algCheck = new QRDecompositionHouseholderTran();
        assertTrue(algCheck.decompose(A));

        BlockMatrix64HouseholderQR alg = new BlockMatrix64HouseholderQR();
        assertTrue(alg.decompose(Ab));

        DenseMatrix64F expected = CommonOps.transpose(algCheck.getQR(),null);
//        expected.print();
//        Ab.print();

        assertTrue(GenericMatrixOps.isEquivalent(expected,Ab,1e-8));
    }

    @Test
    public void fullDecomposition() {
        checkFullDecomposition(r-1,r-1,true);
        checkFullDecomposition(r,r,true);
        checkFullDecomposition(3*r,r,true);
        checkFullDecomposition(r,3*r,true);
        checkFullDecomposition(3*r,3*r,true);
        checkFullDecomposition(3*r+r-1,r,true);
        checkFullDecomposition(r,3*r+r-1,true);

        checkFullDecomposition(r-1,r-1,false);
        checkFullDecomposition(r,r,false);
        checkFullDecomposition(3*r,r,false);
        checkFullDecomposition(r,3*r,false);
        checkFullDecomposition(3*r,3*r,false);
        checkFullDecomposition(3*r+r-1,r,false);
        checkFullDecomposition(r,3*r+r-1,false);
    }

    private void checkFullDecomposition( int numRows , int numCols , boolean compact ) {
        BlockMatrix64F A = BlockMatrixOps.createRandom(numRows,numCols,-1,1,rand,r);


        BlockMatrix64HouseholderQR alg = new BlockMatrix64HouseholderQR();
        assertTrue(alg.decompose(A.copy()));

        BlockMatrix64F Q = alg.getQ(null,compact);
        BlockMatrix64F R = alg.getR(null,compact);

        BlockMatrix64F found = new BlockMatrix64F(numRows,numCols,r);

        BlockMatrixOps.mult(Q,R,found);

        assertTrue(GenericMatrixOps.isEquivalent(A,found,1e-8));
    }
}
