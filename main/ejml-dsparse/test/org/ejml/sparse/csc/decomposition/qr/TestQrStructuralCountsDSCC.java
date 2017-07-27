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

package org.ejml.sparse.csc.decomposition.qr;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.decomposition.qr.QRDecompositionHouseholderColumn_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.ops.ConvertDMatrixStruct;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestQrStructuralCountsDSCC {

    Random rand = new Random(324);

    /**
     * Hand computed using an example from the book
     */
    @Test
    public void process() {

        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                     "1 0 0 1 0 0 1 0 " +
                        "0 1 1 0 0 0 1 0 "+
                        "0 0 1 1 0 0 0 0 " +
                        "1 0 0 1 0 0 1 0 " +
                        "0 0 0 0 1 1 0 0 " +
                        "0 0 0 0 1 1 0 1 " +
                        "0 1 1 0 0 0 1 1 " +
                        "0 0 0 0 1 1 1 0 ",8);

        QrStructuralCounts_DSCC alg = new QrStructuralCounts_DSCC();

        alg.process(A);

        assertEquals(16,alg.nz_in_V); // includes diagonal
        assertEquals(24,alg.nz_in_R); // includes diagonal
        assertEquals(alg.m,alg.m2);

        int []pinv = alg.getPinv();
        int []p = new int[8];
        CommonOps_DSCC.permutationInverse(pinv,p,8);

        // fill in the matrix using a naive algorithm
        DMatrixSparseCSC filled = fillInV(A);
        filled.print();
        for (int i = 0; i < 8; i++) {
            System.out.println("i = "+i);
            assertTrue( filled.isAssigned(p[i],i));
        }
    }

    private DMatrixSparseCSC fillInV(DMatrixSparseCSC A )
    {
        DMatrixSparseCSC B = A.copy();

        int nz[] = new int[ B.numRows ];

        for (int col = 0; col < B.numCols; col++) {
//            System.out.println("#############  "+col);
            // list of non zero rows in this column
            nz[0] = -1;
            int nz_c = 0;
            for (int i = col+1; i < B.numRows; i++) {
                if( A.isAssigned(i,col) ) {
                    nz[nz_c++] = i;
                }
            }

            // sweep through the other columns and see if one of the rows match. if it does fill in the others
            for (int i = col+1; i < B.numCols; i++) {
                for (int j = 0; j < nz_c; j++) {
                    if( B.get(nz[j],i) != 0 ) {
                        for (int k = 0; k < nz_c; k++) {
                            if (nz[k] > i) {
                                B.set(nz[k], i, 1);
                            }
                        }
                        break;
                    }
                }
            }
//            B.print();
        }
        return B;
    }

    @Test
    public void process_random() {
        final QrStructuralCounts_DSCC alg = new QrStructuralCounts_DSCC();
        randomChecks(new Check() {
            @Override
            public void check(DMatrixSparseCSC A) {

                alg.process(A);
                A.print();

                System.out.println("rows = "+A.numRows);

                compareToFilledV(fillInV(A),alg.nz_in_V);

                int m2 = alg.m2;
                int []pinv = alg.getPinv();
                int []p = new int[m2];
                CommonOps_DSCC.permutationInverse(pinv,p,m2);

//                // fill in the matrix using a naive algorithm
//                DMatrixSparseCSC filled = fillInV(A);
//                filled.print();
//                for (int i = 0; i < A.numCols; i++) {
//                    System.out.println("p["+i+"] = "+p[i]);
//                    if( p[i] < A.numRows )  // ignore fictitious rows
//                        assertTrue( filled.isAssigned(p[i],i));
//                }
            }
        });
    }

    private void compareToFilledV(DMatrixSparseCSC A , int foundV ) {
        int countV = 0;
        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
                if( A.isAssigned(i,j) ) {
                    if( i >= j ){
                        countV++;
                    }
                }
            }
        }
        A.print();
        System.out.println("estimated "+foundV+"   actual "+countV);
        assertTrue(countV <= foundV);
    }

    private void compareToDense( DMatrixSparseCSC A , int foundR , int foundV ) {
        DMatrixRMaj D = new DMatrixRMaj(A.numRows,A.numCols);
        ConvertDMatrixStruct.convert(A,D);

        QRDecompositionHouseholderColumn_DDRM decomp =
                (QRDecompositionHouseholderColumn_DDRM)DecompositionFactory_DDRM.qr(1,1);
        decomp.decompose(D);
        double[][] F = decomp.getQR();
        int countR = 0;
        int countV = 0;
        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
                if( F[j][i] != 0 ) {
//                    System.out.print("*");
                    if( j < i ) {
                        countR++;
                    } else if( j > i ){
                        countV++;
                    } else {
                        countR++;
                        countV++;
                    }
                } else {
//                    System.out.print(".");
                }
            }
            System.out.println();
            System.out.println("dense    "+countV+"    "+countR);
            System.out.println("estimate "+foundV+"    "+foundR);


//            assertTrue(countV <= foundV);
//            assertTrue(countR <= foundR);
        }
    }

    @Test
    public void createRowElementLinkedLists() {
        final QrStructuralCounts_DSCC alg = new QrStructuralCounts_DSCC();
        randomChecks(new Check() {
            @Override
            public void check(DMatrixSparseCSC A) {
                alg.init(A);
                int []w = alg.gwork.data;
                alg.findMinElementIndexInRows(alg.leftmost);
                alg.createRowElementLinkedLists(alg.leftmost,w );

//                A.print();

                // compute the number of times each column is the first element in each row
                // this will be the queue size
                int colIsFirst[] = new int[A.numCols];
                // the last row in which the column is the first element
                int lastRow[] = new int[A.numCols];
                int firstRow[] = new int[A.numCols];
                int rowToFirstCol[] = new int[ A.numRows ];
                Arrays.fill(firstRow,0,lastRow.length,-1);
                Arrays.fill(lastRow,0,lastRow.length,-1);
                for (int row = 0; row < A.numRows; row++) {
                    rowToFirstCol[row] = -1;
                    for (int col = 0; col < A.numCols; col++) {
                        if (A.isAssigned(row, col)) {
                            rowToFirstCol[row] = col;
                            colIsFirst[col]++;
                            lastRow[col] = row;
                            if( firstRow[col] == -1 )
                                firstRow[col] = row;
                            break;
                        }
                    }
                }

                // check the linked list structure
                for (int col = 0; col < A.numCols; col++) {
                    assertEquals(colIsFirst[col],w[alg.nque+col]);
                    assertEquals(firstRow[col],w[alg.head+col]);
                    assertEquals(lastRow[col],w[alg.tail+col]);
                }
                for (int row = 0; row < A.numRows; row++) {
                    int col = alg.leftmost[row];
//                    System.out.println("row "+row+" col "+col);

                    if( col >= 0 ) {
                        int last = -1;
                        for (int i = row + 1; i < A.numRows; i++) {
                            if (alg.leftmost[i] == col) {
                                last = i;
                                break;
                            }
                        }
                        assertEquals(last, w[alg.next + row]);
                    } else {
                        // not set
                    }
                }
            }
        });
    }

    @Test
    public void findMinElementIndexInRows() {
        final QrStructuralCounts_DSCC alg = new QrStructuralCounts_DSCC();
        randomChecks(new Check() {
            @Override
            public void check(DMatrixSparseCSC A) {
                int leftMost[] = new int[A.numRows];

                alg.init(A);
                alg.findMinElementIndexInRows(leftMost);

                for (int row = 0; row < A.numRows; row++) {
                    boolean found = false;
                    for (int col = 0; col < A.numCols; col++) {
                        if( A.get(row,col) != 0 ) {
                            assertEquals(col,leftMost[row]);
                            found = true;
                            break;
                        }
                    }
                    if( !found )
                        assertEquals(-1,leftMost[row]);
                }
            }
        });
    }

    private void randomChecks( Check check ) {
        for (int mc = 0; mc < 100; mc++) {
            int numCols = rand.nextInt(10) + 1;
            int numRows = numCols+rand.nextInt(5);
            numRows = numCols; // TODO remove hack
            int nz = RandomMatrices_DSCC.nonzero(numRows, numCols, 0.01, 0.5, rand);
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(numRows,numCols,nz,rand);

            check.check(A);
        }
    }

    private interface Check {
        void check( DMatrixSparseCSC A );
    }

}
