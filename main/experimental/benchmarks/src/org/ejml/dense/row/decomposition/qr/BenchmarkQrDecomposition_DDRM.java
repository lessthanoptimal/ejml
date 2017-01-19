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

package org.ejml.dense.row.decomposition.qr;

import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.block.decomposition.qr.QRDecompositionHouseholder_DDRB;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.interfaces.decomposition.QRDecomposition;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkQrDecomposition_DDRM {

    public static long generic(QRDecomposition<DMatrixRMaj> alg, DMatrixRMaj orig , int numTrials ) {

        long prev = System.currentTimeMillis();
        DMatrixRMaj B;
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

    public static long block(DMatrixRMaj orig , int numTrials ) {

        DMatrixRBlock A = MatrixOps_DDRB.convert(orig);
        QRDecompositionHouseholder_DDRB alg = new QRDecompositionHouseholder_DDRB();

        DMatrixRBlock B;

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

    private static void runAlgorithms(DMatrixRMaj mat , int numTrials )
    {
//        System.out.println("basic            = "+ generic( new QRDecompositionHouseholder(), mat,numTrials));
        System.out.println("column           = "+ generic( new QRDecompositionHouseholderColumn_DDRM() ,mat,numTrials));
        System.out.println("tran             = "+ generic( new QRDecompositionHouseholderTran_DDRM() , mat,numTrials));
        System.out.println("pivot column     = "+ generic( new QRColPivDecompositionHouseholderColumn_DDRM() , mat,numTrials));

//        System.out.println("block  native    = "+ block(mat,numTrials));
        System.out.println("block wrapper    = "+ generic( new QRDecomposition_DDRB_to_DDRM() , mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,10,100,500,1000,2000,4000};
        int trials[] = new int[]{(int)2e6,(int)5e5,(int)1e5,400,5,1,1,1,1};

        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];
            DMatrixRMaj mat = RandomMatrices_DDRM.createRandom(w*4,w/1,rand);
             System.out.printf("Decomposing size [ %5d  , %5d ] for %12d trials\n",mat.numRows,mat.numCols,trials[i]);
            runAlgorithms(mat,trials[i]);
        }
    }
}