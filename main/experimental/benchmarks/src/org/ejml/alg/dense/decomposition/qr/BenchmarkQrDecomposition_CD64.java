/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.alg.dense.decompose.qr.QRDecompositionHouseholderColumn_CD64;
import org.ejml.alg.dense.decompose.qr.QRDecompositionHouseholderTran_CD64;
import org.ejml.alg.dense.decompose.qr.QRDecompositionHouseholder_CD64;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.ops.CRandomMatrices;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkQrDecomposition_CD64 {

    public static long generic(QRDecomposition<CDenseMatrix64F> alg,  CDenseMatrix64F orig , int numTrials ) {

        long prev = System.currentTimeMillis();
        CDenseMatrix64F B;
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

    private static void runAlgorithms( CDenseMatrix64F mat , int numTrials )
    {
        System.out.println("basic            = "+ generic( new QRDecompositionHouseholder_CD64(), mat,numTrials));
        System.out.println("column           = "+ generic( new QRDecompositionHouseholderColumn_CD64() ,mat,numTrials));
        System.out.println("tran             = "+ generic( new QRDecompositionHouseholderTran_CD64() , mat,numTrials));
//        System.out.println("pivot column     = "+ generic( new QRColPivDecompositionHouseholderColumn_CD64() , mat,numTrials));

//        System.out.println("block  native    = "+ block(mat,numTrials));
//        System.out.println("block wrapper    = "+ generic( new QRDecomposition_B64_to_D64() , mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,10,100,500,1000,2000,4000};
        int trials[] = new int[]{(int)2e6,(int)5e5,(int)1e4,200,2,1,1,1,1};

        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];
            CDenseMatrix64F mat = CRandomMatrices.createRandom(w * 4, w / 1, rand);
             System.out.printf("Decomposing size [ %5d  , %5d ] for %12d trials\n",mat.numRows,mat.numCols,trials[i]);
            runAlgorithms(mat,trials[i]);
        }
    }
}