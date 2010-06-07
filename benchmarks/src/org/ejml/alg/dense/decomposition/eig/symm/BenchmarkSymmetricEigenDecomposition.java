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

package org.ejml.alg.dense.decomposition.eig.symm;

import org.ejml.alg.dense.decomposition.eig.SymmetricQRAlgorithmDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkSymmetricEigenDecomposition {
    public static long symmTogether( DenseMatrix64F orig , int numTrials ) {

        long prev = System.currentTimeMillis();

        SymmetricQRAlgorithmDecomposition alg = new SymmetricQRAlgorithmDecomposition(true);

        alg.setComputeVectorsWithValues(true);

        for( long i = 0; i < numTrials; i++ ) {
            if( !alg.decompose(orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    public static long symmSeparate( DenseMatrix64F orig , int numTrials ) {

        long prev = System.currentTimeMillis();

        SymmetricQRAlgorithmDecomposition alg = new SymmetricQRAlgorithmDecomposition(true);

        alg.setComputeVectorsWithValues(false);

        for( long i = 0; i < numTrials; i++ ) {
            if( !alg.decompose(orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    private static void runAlgorithms( DenseMatrix64F mat , int numTrials )
    {
        System.out.println("Together            = "+ symmTogether(mat,numTrials));
        System.out.println("Separate            = "+ symmSeparate(mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(232423);

        int size[] = new int[]{2,4,10,100,200,500,1000};
        int trials[] = new int[]{2000000,400000,80000,300,40,4,1};

        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Decomposing size %3d for %12d trials\n",w,trials[i]);

            DenseMatrix64F symMat = RandomMatrices.createSymmetric(w,-1,1,rand);

            runAlgorithms(symMat,trials[i]);
        }
    }
}