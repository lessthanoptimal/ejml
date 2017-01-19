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

package org.ejml.dense.block;

import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;

import java.util.Random;


/**
 * Compare block against other transpose for DMatrixRMaj
 *
 *  @author Peter Abeles
 */
public class BenchmarkBlockTranspose {

    static Random rand = new Random(234);

    public static long transposeDenseInPlace(DMatrixRMaj mat , int numTrials) {

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.transpose(mat);
        }
        long curr = System.currentTimeMillis();

        return curr-prev;
    }

    public static long transposeDense(DMatrixRMaj mat , int numTrials) {


        DMatrixRMaj tran = new DMatrixRMaj(mat.numCols,mat.numRows);

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.transpose(mat,tran);
        }
        long curr = System.currentTimeMillis();

        return curr-prev;
    }

    public static long transposeBlock(DMatrixRMaj mat , int numTrials) {

        DMatrixRBlock A = new DMatrixRBlock(mat.numRows,mat.numCols);
        DMatrixRBlock A_t = new DMatrixRBlock(mat.numCols,mat.numRows);

        MatrixOps_DDRB.convert(mat,A);

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixOps_DDRB.transpose(A,A_t);
        }
        long curr = System.currentTimeMillis();

        return curr-prev;
    }

    public static void main( String args[] ) {

        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5000,5000,rand);

        int N = 5;

        System.out.println("In place  : "+transposeDenseInPlace(A,N));
        System.out.println("Standard  : "+transposeDense(A,N));
        System.out.println("Block     : "+transposeBlock(A,N));
    }
}
