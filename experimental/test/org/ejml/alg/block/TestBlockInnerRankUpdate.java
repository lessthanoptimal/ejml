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

import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.data.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * @author Peter Abeles
 */
public class TestBlockInnerRankUpdate {

    Random rand = new Random(234234);

    int N = 4;

    /**
     * Test it where the matrix being updated is a full block
     */
    // TODO merge single and multiple into one function with a for loop
    @Test
    public void rankNUpdate_single() {
        double alpha = -2.0;
        SimpleMatrix origA = SimpleMatrix.random(N,N,-1,1,rand);
        SimpleMatrix origB = SimpleMatrix.random(N-2,N,-1,1,rand);

        BlockMatrix64F blockA = BlockMatrixOps.convert(origA.getMatrix(),N);
        BlockMatrix64F blockB = BlockMatrixOps.convert(origB.getMatrix(),N);

        D1Submatrix64F subA = new D1Submatrix64F(blockA,0,0,origA.numRows(),origA.numCols());
        D1Submatrix64F subB = new D1Submatrix64F(blockB,0,0,origB.numRows(),origB.numCols());

        SimpleMatrix expected = origA.plus(origB.transpose().mult(origB).scale(alpha));
        BlockInnerRankUpdate.rankNUpdate(N,alpha,subA,subB);


        assertTrue(GenericMatrixOps.isEquivalent(expected.getMatrix(),blockA,1e-8));

    }

    /**
     * Test it where the matrix being updated is composed of multiple blocks with a partial
     * block at the end.  This tests multiple blocks and a block that is less than the full block size.
     */
    @Test
    public void rankNUpdate_multiple() {
        double alpha = -2.0;
        SimpleMatrix origA = SimpleMatrix.random(N*2+1,N*2+1,-1,1,rand);
        SimpleMatrix origB = SimpleMatrix.random(N-2,N*2+1,-1,1,rand);

        BlockMatrix64F blockA = BlockMatrixOps.convert(origA.getMatrix(),N);
        BlockMatrix64F blockB = BlockMatrixOps.convert(origB.getMatrix(),N);

        D1Submatrix64F subA = new D1Submatrix64F(blockA,0,0,origA.numRows(),origA.numCols());
        D1Submatrix64F subB = new D1Submatrix64F(blockB,0,0,origB.numRows(),origB.numCols());

        SimpleMatrix expected = origA.plus(origB.transpose().mult(origB).scale(alpha));
        BlockInnerRankUpdate.rankNUpdate(N,alpha,subA,subB);

        assertTrue(GenericMatrixOps.isEquivalent(expected.getMatrix(),blockA,1e-8));
    }

    @Test
    public void symmRankNUpdate_U() {
        fail("Implement");
    }
}
