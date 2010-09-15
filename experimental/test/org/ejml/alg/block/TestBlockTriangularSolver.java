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

import org.ejml.alg.dense.misc.UnrolledInverseFromMinor;
import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;



/**
 * @author Peter Abeles
 */
public class TestBlockTriangularSolver {

    Random rand = new Random(234534);


    @Test
    public void solveL_submatrix() {
        // compute expected solution
        DenseMatrix64F L = createRandomLowerTriangular(3);
        DenseMatrix64F B = RandomMatrices.createRandom(3,5,rand);
        DenseMatrix64F X = new DenseMatrix64F(3,5);

        CommonOps.solve(L,B,X);

        // do it again using block matrices
        BlockMatrix64F b_L = BlockMatrixOps.convert(L,3);
        BlockMatrix64F b_B = BlockMatrixOps.convert(B,3);

        BlockTriangularSolver.solveL(3,new D1Submatrix64F(b_L,0,0,3,3),new D1Submatrix64F(b_B,0,0,3,5));

        assertTrue(GenericMatrixOps.isEquivalent(X,b_B,1e-10));
    }

    /**
     * Tests the lower triangular array solver for one block at a time.
     */
    @Test
    public void solveL_array_offset() {
        int offsetL = 2;
        int offsetB = 3;

        DenseMatrix64F L = createRandomLowerTriangular(3);

        DenseMatrix64F L_inv = L.copy();
        UnrolledInverseFromMinor.inv(L_inv,L_inv);

        DenseMatrix64F B = RandomMatrices.createRandom(3,4,rand);
        DenseMatrix64F expected = RandomMatrices.createRandom(3,4,rand);
        DenseMatrix64F found = B.copy();

        // create arrays that are offset from the original
        // use two different offsets to make sure it doesn't confuse them internally
        double dataL[] = offsetArray(L.data,offsetL);
        double dataB[] = offsetArray(found.data,offsetB);

        BlockTriangularSolver.solveL(dataL,dataB,3,4,offsetL,offsetB);

        // put the solution into B, minus the offset
        System.arraycopy(dataB,offsetB,found.data,0,found.data.length);
        CommonOps.mult(L_inv,B,expected);

        assertTrue(MatrixFeatures.isIdentical(expected,found,1e-8));
    }

    private DenseMatrix64F createRandomLowerTriangular( int N ) {
        DenseMatrix64F U = RandomMatrices.createUpperTriangle(N,0,-1,1,rand);

        CommonOps.transpose(U);

        return U;
    }

    private double[] offsetArray( double[] orig , int offset )
    {
        double[] ret = new double[ orig.length + offset ];

        System.arraycopy(orig,0,ret,offset,orig.length);

        return ret;
    }
}
