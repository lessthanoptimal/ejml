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

package org.ejml.dense.row.decomposition.eig.symm;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.eig.SymmetricQRAlgorithmDecomposition_DDRM;
import org.ejml.dense.row.decomposition.hessenberg.TridiagonalDecompositionHouseholder_DDRM;
import org.ejml.dense.row.decomposition.hessenberg.TridiagonalDecomposition_DDRB_to_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F64;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkSymmetricEigenDecomposition {
    public static long symmTogether(DMatrixRMaj orig , int numTrials ) {

        long prev = System.currentTimeMillis();

        TridiagonalSimilarDecomposition_F64<DMatrixRMaj> decomp =  DecompositionFactory_DDRM.tridiagonal(orig.numRows);
        SymmetricQRAlgorithmDecomposition_DDRM alg = new SymmetricQRAlgorithmDecomposition_DDRM(decomp,true);

        alg.setComputeVectorsWithValues(true);

        for( long i = 0; i < numTrials; i++ ) {
            if( !DecompositionFactory_DDRM.decomposeSafe(alg,orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    public static long symmSeparate(DMatrixRMaj orig , int numTrials ) {

        long prev = System.currentTimeMillis();

        TridiagonalSimilarDecomposition_F64<DMatrixRMaj> decomp =  DecompositionFactory_DDRM.tridiagonal(orig.numRows);
        SymmetricQRAlgorithmDecomposition_DDRM alg = new SymmetricQRAlgorithmDecomposition_DDRM(decomp,true);

        alg.setComputeVectorsWithValues(false);

        for( long i = 0; i < numTrials; i++ ) {
            if( !DecompositionFactory_DDRM.decomposeSafe(alg,orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    public static long standardTridiag(DMatrixRMaj orig , int numTrials ) {
        TridiagonalSimilarDecomposition_F64<DMatrixRMaj> decomp = new TridiagonalDecompositionHouseholder_DDRM();
        SymmetricQRAlgorithmDecomposition_DDRM alg = new SymmetricQRAlgorithmDecomposition_DDRM(decomp,true);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !DecompositionFactory_DDRM.decomposeSafe(alg,orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    public static long blockTridiag(DMatrixRMaj orig , int numTrials ) {

        TridiagonalSimilarDecomposition_F64<DMatrixRMaj> decomp = new TridiagonalDecomposition_DDRB_to_DDRM();
        SymmetricQRAlgorithmDecomposition_DDRM alg = new SymmetricQRAlgorithmDecomposition_DDRM(decomp,true);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !DecompositionFactory_DDRM.decomposeSafe(alg,orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    public static long defaultSymm(DMatrixRMaj orig , int numTrials ) {

        EigenDecomposition<DMatrixRMaj> alg = DecompositionFactory_DDRM.eig(orig.numCols, true, true);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !DecompositionFactory_DDRM.decomposeSafe(alg,orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }


    private static void runAlgorithms(DMatrixRMaj mat , int numTrials )
    {
//        System.out.println("Together            = "+ symmTogether(mat,numTrials));
//        System.out.println("Separate            = "+ symmSeparate(mat,numTrials));
        System.out.println("Standard            = "+ standardTridiag(mat,numTrials));
        System.out.println("Block               = "+ blockTridiag(mat,numTrials));
        System.out.println("Default             = "+ defaultSymm(mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(232423);

        int size[] = new int[]{2,4,10,100,200,500,1000,2000,5000};
        int trials[] = new int[]{2000000,400000,80000,300,40,4,1,1,1};

        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Decomposing size %3d for %12d trials\n",w,trials[i]);

            DMatrixRMaj symMat = RandomMatrices_DDRM.createSymmetric(w,-1,1,rand);

            runAlgorithms(symMat,trials[i]);
        }
    }
}