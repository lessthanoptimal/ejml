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

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.block.decomposition.qr.BlockMatrix64HouseholderQR;
import org.ejml.alg.dense.decomposition.QRDecomposition;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkQrDecomposition {

    public static long generic(QRDecomposition alg,  DenseMatrix64F orig , int numTrials ) {

        long prev = System.currentTimeMillis();
        DenseMatrix64F B;
        for( long i = 0; i < numTrials; i++ ) {
            if( alg.inputModified())
                B = orig.copy();
            else
                B = orig;
            if( !alg.decompose(B) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }


    public static long block( DenseMatrix64F orig , int numTrials ) {

        BlockMatrix64F A = BlockMatrixOps.convert(orig);
        BlockMatrix64HouseholderQR alg = new BlockMatrix64HouseholderQR();

        BlockMatrix64F B;

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( alg.inputModified())
                B = A.copy();
            else
                B = A;
            if( !alg.decompose(B) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    private static void runAlgorithms( DenseMatrix64F mat , int numTrials )
    {
//        System.out.println("basic            = "+ generic( new QRDecompositionHouseholder(), mat,numTrials));
        System.out.println("column           = "+ generic( new QRDecompositionHouseholderColumn() ,mat,numTrials));
        System.out.println("tran             = "+ generic( new QRDecompositionHouseholderTran() , mat,numTrials));
//        System.out.println("block  native    = "+ block(mat,numTrials));
//        System.out.println("block wrapper    = "+ generic( new QRDecompositionBlock64() , mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,10,100,500,1000,2000,4000};
        int trials[] = new int[]{(int)2e6,(int)5e5,(int)1e5,400,5,1,1,1,1};

        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];
            DenseMatrix64F mat = RandomMatrices.createRandom(w*4,w/1,rand);
             System.out.printf("Decomposing size [ %5d  , %5d ] for %12d trials\n",mat.numRows,mat.numCols,trials[i]);
            runAlgorithms(mat,trials[i]);
        }
    }
}