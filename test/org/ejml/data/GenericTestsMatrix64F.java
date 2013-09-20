/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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

package org.ejml.data;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public abstract class GenericTestsMatrix64F {

    protected abstract ReshapeMatrix64F createMatrix( int numRows , int numCols );

    public void allTests() {
        testGetNumRows();
        testGetNumCols();
        testSetAndGet_2D();
        testSetAndGet_2D_unsafe();
    }

    public void testGetNumRows() {
        ReshapeMatrix64F mat = createMatrix(2,3);

        assertEquals(2,mat.getNumRows());
    }

    public void testGetNumCols() {
        ReshapeMatrix64F mat = createMatrix(2,3);

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
        ReshapeMatrix64F mat = createMatrix(m, n);

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
        ReshapeMatrix64F mat = createMatrix(m, n);

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
