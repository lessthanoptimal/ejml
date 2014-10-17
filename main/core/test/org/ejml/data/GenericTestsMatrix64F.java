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

package org.ejml.data;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public abstract class GenericTestsMatrix64F {

    protected abstract RealMatrix64F createMatrix( int numRows , int numCols );

    public void allTests() {
        testGetNumRows();
        testGetNumCols();
        testSetAndGet_2D();
        testSetAndGet_2D_unsafe();
    }

    public void testGetNumRows() {
        RealMatrix64F mat = createMatrix(2,3);

        assertEquals(2,mat.getNumRows());
    }

    public void testGetNumCols() {
        RealMatrix64F mat = createMatrix(2,3);

        assertEquals(3,mat.getNumCols());
    }

    public void testSetAndGet_2D() {
        // test a variety of different shapes.  Added rigor needed
        // to properly test block matrix.
        checkSetAndGet(10, 12);
        checkSetAndGet(12, 10);
        checkSetAndGet(10, 10);
        checkSetAndGet(19, 5);
        checkSetAndGet(5, 19);
        checkSetAndGet(19, 19);
    }

    private void checkSetAndGet(int m, int n) {
        RealMatrix64F mat = createMatrix(m, n);

        for( int i = 0; i < m; i++ ) {
            for( int j = 0; j < n; j++ ) {
                mat.set(i,j, i* m +j);
            }
        }

        for( int i = 0; i < m; i++ ) {
            for( int j = 0; j < n; j++ ) {
                double found = mat.get(i,j);

                assertEquals(i* m +j,found,1e-8);
            }
        }
    }

    public void testSetAndGet_2D_unsafe() {
        // test a variety of different shapes.  Added rigor needed
        // to properly test block matrix.
        checkSetAndGet_unsafe(10, 12);
        checkSetAndGet_unsafe(12, 10);
        checkSetAndGet_unsafe(10, 10);
        checkSetAndGet_unsafe(19, 5);
        checkSetAndGet_unsafe(5, 19);
        checkSetAndGet_unsafe(19, 19);
    }

    private void checkSetAndGet_unsafe(int m, int n) {
        RealMatrix64F mat = createMatrix(m, n);

        for( int i = 0; i < m; i++ ) {
            for( int j = 0; j < n; j++ ) {
                mat.unsafe_set(i,j, i* m +j);
            }
        }

        for( int i = 0; i < m; i++ ) {
            for( int j = 0; j < n; j++ ) {
                double found = mat.unsafe_get(i,j);

                assertEquals(i* m +j,found,1e-8);
            }
        }
    }

}
