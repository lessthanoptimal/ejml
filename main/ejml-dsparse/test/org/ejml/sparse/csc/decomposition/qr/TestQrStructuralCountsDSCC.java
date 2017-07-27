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

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestQrStructuralCountsDSCC {

    Random rand = new Random(324);

    @Test
    public void process_random() {
        final QrStructuralCounts_DSCC alg = new QrStructuralCounts_DSCC();
        randomChecks(new Check() {
            @Override
            public void check(DMatrixSparseCSC A) {
                alg.process(A);

                DMatrixSparseCSC B = A.copy();

                int m2 = A.numRows;
                int nz[] = new int[ B.numRows ];

                for (int col = 0; col < B.numCols; col++) {
                    // list of non zero rows in this column
                    nz[0] = -1;
                    int nz_c = 0;
                    for (int i = col; i < B.numRows; i++) {
                        if( A.isAssigned(i,col) ) {
                            nz[nz_c++] = i;
                        }
                    }

                    // sweep through the other columns and see if one of the rows match. if it does fill in the others
                    for (int i = col+1; i < B.numCols; i++) {
                        for (int j = 0; j < nz_c; j++) {
                            if( B.get(nz[j],i) != 0 ) {
                                for (int k = 0; k < nz_c; k++) {
                                    B.set(nz[k],i,1);
                                }
                                break;
                            }
                        }
                    }
                }

                int nz_count_V = 0;
                int nz_count_R = 0;
                for (int row = 0; row < B.numRows; row++) {
                    boolean found = false;
                    for (int col = 0; col < B.numCols; col++) {
                        if (B.isAssigned(row, col) ) {
                            found = true;
                            if( col >= row ) {
                                nz_count_R++;
                            } else {
                                nz_count_V++;
                            }
                        }
                    }
                    if( !found ) {
                        m2++;
                    }
                }
                A.print();
                B.print();

                System.out.println("m2 "+m2+"  "+alg.m2);
                System.out.println("nz_count_V "+nz_count_V+"  "+alg.nz_in_V);
                System.out.println("nz_count_R "+nz_count_R+"  "+alg.nz_in_R);
                System.out.println();

                assertTrue(nz_count_V <= alg.nz_in_V);
                assertTrue(nz_count_R <= alg.nz_in_R);
            }
        });
    }

    @Test
    public void countNonZeroInR() {
        fail("implement");
    }

    @Test
    public void countNonZeroInV() {
        fail("implement");
    }

    @Test
    public void countNonZeroUsingLinkedList() {
        fail("implement");
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

                A.print();

                for (int row = 0; row < A.numRows; row++) {
                    int expected = w[alg.head+row];
                    int count = 0;
                    for (int col = 0; col < A.numCols; col++) {
                        if( A.get(row,col) != 0 ) {
                            assertEquals(col,expected);
                            expected = w[alg.next+count++];
                        }
                    }
                    assertEquals(-1,w[alg.next+count]);
                    assertEquals(count,w[alg.nque+row]);
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
            int numRows = rand.nextInt(10) + 1;
            int numCols = rand.nextInt(10) + 1;
            int nz = RandomMatrices_DSCC.nonzero(numRows, numCols, 0.01, 0.5, rand);
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(numRows,numCols,nz,rand);

            check.check(A);
        }
    }

    private interface Check {
        void check( DMatrixSparseCSC A );
    }

}
