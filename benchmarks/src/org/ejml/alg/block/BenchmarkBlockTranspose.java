/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Compare block against other transpose for DenseMatrix64F
 *
 *  @author Peter Abeles
 */
public class BenchmarkBlockTranspose {

    static Random rand = new Random(234);

    public static long transposeDenseInPlace( DenseMatrix64F mat , int numTrials) {

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps.transpose(mat);
        }
        long curr = System.currentTimeMillis();

        return curr-prev;
    }

    public static long transposeDense( DenseMatrix64F mat , int numTrials) {


        DenseMatrix64F tran = new DenseMatrix64F(mat.numCols,mat.numRows);

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps.transpose(mat,tran);
        }
        long curr = System.currentTimeMillis();

        return curr-prev;
    }

    public static long transposeBlock( DenseMatrix64F mat , int numTrials) {

        BlockMatrix64F A = new BlockMatrix64F(mat.numRows,mat.numCols);
        BlockMatrix64F A_t = new BlockMatrix64F(mat.numCols,mat.numRows);

        BlockMatrixOps.convert(mat,A);

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            BlockMatrixOps.transpose(A,A_t);
        }
        long curr = System.currentTimeMillis();

        return curr-prev;
    }

    public static void main( String args[] ) {

        DenseMatrix64F A = RandomMatrices.createRandom(5000,5000,rand);

        int N = 5;

        System.out.println("In place  : "+transposeDenseInPlace(A,N));
        System.out.println("Standard  : "+transposeDense(A,N));
        System.out.println("Block     : "+transposeBlock(A,N));
    }
}
