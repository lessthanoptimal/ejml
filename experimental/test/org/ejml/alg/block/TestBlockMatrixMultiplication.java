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

package org.ejml.alg.block;

import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * @author Peter Abeles
 */
// TODO use reflections in these tests for different permutations of mult
public class TestBlockMatrixMultiplication {

    private static Random rand = new Random(234234);

    private static final int blockLength = 4;
    
    private static final int numRows = 10;
    private static final int numCols = 13;


    /**
     * Checks to see if matrix multiplcation handles submatrix correctly
     */
    @Test
    public void mult_submatrix() {
        // the submatrix is the same size as the originals
        checkMult_submatirx( sub(0,0,numRows,numCols),sub(0,0,numCols,numRows));

        // submatrix has a size in multiples of the block
        checkMult_submatirx( sub(blockLength,blockLength,blockLength*2,blockLength*2),
                sub(blockLength,blockLength,blockLength*2,blockLength*2));

        // submatrix row and column ends at a fraction of a block
        checkMult_submatirx( sub(blockLength,blockLength,numRows,numCols),
                sub(blockLength,blockLength,numCols,numRows));
    }

    /**
     * Multiplies the two sub-matrices together.  Checks to see if the same result
     * is found when multiplied using the normal algorithm versus the submatrix one.
     */
    private static void checkMult_submatirx( D1Submatrix64F A , D1Submatrix64F B ) {
        BlockMatrix64F origA = BlockMatrixOps.createRandom(numRows,numCols,-1,1, rand, blockLength);
        BlockMatrix64F origB = BlockMatrixOps.createRandom(numCols,numRows,-1,1, rand, blockLength);

        BlockMatrix64F subC = new BlockMatrix64F(numRows,numRows, blockLength);


        A.original = origA;
        B.original = origB;
        int w = B.col1-B.col0;
        int h = A.row1-A.row0;
        D1Submatrix64F C = new D1Submatrix64F(subC,0,0,w,h);

        DenseMatrix64F rmC = multByExtract(A,B);
        // TODO fix by putting the results in C at the specified location inside
        BlockMatrixMultiplication.mult(blockLength,A,B,C);

        System.out.println("------------");
        rmC.print();
        subC.print();

        for( int i = C.row0; i < C.row1; i++ ) {
            for( int j = C.col0; j < C.col1; j++ ) {
                double diff = Math.abs(subC.get(i,j) - rmC.get(i-C.row0,j-C.col0));
                if( diff >= 1e-12 ) {
                    System.out.println(subC.get(i,j)+"  "+rmC.get(i-C.row0,j-C.col0));
                    System.out.println("crap");
                }
                assertTrue(diff < 1e-12);
            }
        }
    }

    private static D1Submatrix64F sub( int row0 , int col0 , int row1 , int col1 ) {
        return new D1Submatrix64F(null,row0,col0,row1,col1);
    }

    private static DenseMatrix64F multByExtract( D1Submatrix64F subA , D1Submatrix64F subB )
    {
        DenseMatrix64F rmA = BlockMatrixOps.convert((BlockMatrix64F)subA.original,null);
        DenseMatrix64F rmB = BlockMatrixOps.convert((BlockMatrix64F)subB.original,null);

        DenseMatrix64F A = new DenseMatrix64F(subA.row1-subA.row0,subA.col1-subA.col0);
        CommonOps.extract(rmA,subA.row0,subA.row1-1,subA.col0,subA.col1-1,A,0,0);
        DenseMatrix64F B = new DenseMatrix64F(subB.row1-subB.row0,subB.col1-subB.col0);
        CommonOps.extract(rmB,subB.row0,subB.row1-1,subB.col0,subB.col1-1,B,0,0);

        DenseMatrix64F C = new DenseMatrix64F(A.numRows,B.numCols);

        CommonOps.mult(A,B,C);

        return C;
    }

    @Test
    public void multTransA() {
        fail("Implement");
    }

    @Test
    public void multTransB() {
        fail("Implement");
    }

}
