/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.misc;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.interfaces.linsol.ReducedRowEchelonForm_F32;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Generalized checks for Gauss-Jordan implementations
 *
 * @author Peter Abeles
 */
public abstract class GeneralReducedRowEchelonFormChecks_FDRM {

    Random rand = new Random(234);

    ReducedRowEchelonForm_F32 alg;

    public GeneralReducedRowEchelonFormChecks_FDRM(ReducedRowEchelonForm_F32 alg) {
        this.alg = alg;
    }

    /**
     * See if it is reducing systems into RREF
     */
    @Test
    public void testFormat() {
        for( int i = 1; i < 10; i++ ) {
            // test square
            checkFormatRandom(1+i,1+i);
            // test wide
            checkFormatRandom(3 + i, 4 + i * 2);
            // test tall
            checkFormatRandom(4 + i * 2,3 + i);
        }
    }

    /**
     * Solve several linear systems and check against solution
     */
    @Test
    public void testSolution() {
        checkSolutionRandom(3,4,3);
        checkSolutionRandom(3,5,3);
        // Tall won't work because with this test because the system is inconsistent
//        checkSolutionRandom(10,4,3);
    }

    @Test
    public void testSingular() {
        FMatrixRMaj A = new FMatrixRMaj(3,4,true,1,2,3,4,3,5,6,7,2,4,6,8,-3,4,9,3);

        FMatrixRMaj found = A.copy();
        alg.reduce(found,3);

        checkRref(found,3);

        FMatrixRMaj A1 = CommonOps_FDRM.extract(A,0,3,0,3);
        FMatrixRMaj X = CommonOps_FDRM.extract(found,0,3,3,4);
        FMatrixRMaj B = new FMatrixRMaj(3,1);

        CommonOps_FDRM.mult(A1,X,B);

        for( int i = 0; i < 3; i++ )
            assertEquals(A.get(i,3),B.get(i,0), UtilEjml.TEST_F32);
    }

    /**
     * Feed it specific matrices and see if it dies a horrible death
     */
    @Test
    public void spotTests() {
        FMatrixRMaj A = new FMatrixRMaj(4,6,true,
                0,0,1,-1,-1,4,
                2,4,2,4,2,4,
                2,4,3,3,3,4,
                3,6,6,3,6,6);

        alg.reduce(A,5);
        checkRref(A,5);
    }

    private void checkFormatRandom(int numRows, int numCols) {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(numRows,numCols,-1,1,rand);
        FMatrixRMaj found = A.copy();

        alg.reduce(found,numCols);

        checkRref(found, numCols);
    }

    private void checkSolutionRandom(int numRows, int numCols , int solWidth ) {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(numRows,numCols,-1,1,rand);
        FMatrixRMaj found = A.copy();

        alg.reduce(found,solWidth);

        checkRref(found,solWidth);

        FMatrixRMaj A1 = CommonOps_FDRM.extract(A,0,numRows,0,solWidth);
        FMatrixRMaj X = CommonOps_FDRM.extract(found,0,solWidth,solWidth,numCols);
        FMatrixRMaj B = new FMatrixRMaj(numRows,numCols-solWidth);

        CommonOps_FDRM.mult(A1,X,B);

        for( int i = 0; i < numRows; i++ )
            for( int j = 0; j < numCols-solWidth; j++ )
                assertEquals(A.get(i,j+solWidth),B.get(i,j),UtilEjml.TEST_F32);
    }


    /**
     * Checks to see if the provided matrix is in reduced row echelon format
     * @param A
     */
    private void checkRref(FMatrixRMaj A , int systemWidth ) {
        int prevLeading = -1;

        for( int row = 0; row < A.numRows; row++ ) {

            // find the next leading
            for( int col = 0; col < systemWidth; col++ ) {
                float val = A.get(row,col);

                if( val == 1 ) {
                    if( prevLeading > col )
                        fail("The next leading one should be at a later column than the previous");
                    prevLeading = col;

                    for( int i = 0; i < A.numRows; i++ ) {
                        if( i == row ) continue;
                        assertEquals(0, A.get(i, col), "Column should be all zeros, except at the leading");
                    }

                    break;
                } else {
                    assertEquals(0, val, UtilEjml.TEST_F32,"Should be all zeros before the leading 1");
                }
            }
        }
    }
}
