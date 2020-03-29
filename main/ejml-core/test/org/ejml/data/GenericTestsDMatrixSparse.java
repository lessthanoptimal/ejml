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

package org.ejml.data;

import org.ejml.UtilEjml;
import org.junit.Test;

import java.util.Iterator;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public abstract class GenericTestsDMatrixSparse extends GenericTestsDMatrix
{
    Random rand = new Random(234);

    public boolean assignable = true;

    public abstract DMatrixSparse createSparse(int numRows , int numCols );

    public abstract DMatrixSparse createSparse(DMatrixSparseTriplet orig);

    public abstract boolean isStructureValid( DMatrixSparse m );

    @Override
    protected DMatrix createMatrix(int numRows, int numCols) {

        // define a sparse matrix with every element filled.  It should act low a slow and inefficient
        // row matrix now
        DMatrixSparseTriplet t = new DMatrixSparseTriplet(numRows,numCols,numRows*numCols);

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                t.addItem(row,col, (double)rand.nextGaussian());
            }
        }

        return createSparse(t);
    }

    @Test
    public void set() {
        DMatrixSparse m = createSparse(3,4);

        if( assignable ) {
            DMatrixSparse orig = m.copy();

            // Test the situation where a new element is added to the sparse matrix
            m.set(1, 2, 10);
            assertEquals(10, m.get(1, 2), UtilEjml.TEST_F64);

            // make sure nothing else was modified
            for (int row = 0; row < m.getNumRows(); row++) {
                for (int col = 0; col < m.getNumCols(); col++) {
                    if( row != 1 && col != 2 )
                        assertEquals(orig.get(row,col),m.get(row,col), UtilEjml.TEST_F64);
                }
            }
            assertTrue(isStructureValid(m));

            // modify the same element again
            m.set(1, 2, 20);
            assertEquals(20, m.get(1, 2), UtilEjml.TEST_F64);
            assertTrue(isStructureValid(m));

            // another test case.  empty matrix
            m.zero();
            m.set(1, 2, 15);
            assertEquals(15, m.get(1, 2), UtilEjml.TEST_F64);
            for (int row = 0; row < m.getNumRows(); row++) {
                for (int col = 0; col < m.getNumCols(); col++) {
                    if( row != 1 && col != 2 )
                        assertEquals(0,m.get(row,col), UtilEjml.TEST_F64);
                }
            }

        } else {
            try {
                m.set(1,2,10);
                fail("Should have thrown an exception");
            } catch( RuntimeException ignore){}
        }
    }

    @Test
    public void get() {
        DMatrixSparseTriplet tmp = new DMatrixSparseTriplet(3,4,1);
        tmp.addItem(1,2,5);

        DMatrixSparse m = createSparse(tmp);

        m.set(1,2, 5);

        for (int row = 0; row < m.getNumRows(); row++) {
            for (int col = 0; col < m.getNumCols(); col++) {
                double found = m.get(row,col);
                if( row == 1 && col == 2)
                    assertEquals(5, found, UtilEjml.TEST_F64);
                else
                    assertEquals(0, found, UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void remove_case0() {
        DMatrixSparse m = (DMatrixSparse)createMatrix(3,4);
        DMatrixSparse c = m.copy();

        assertTrue( m.isAssigned(1,2) );
        m.remove(1,2);
        assertFalse( m.isAssigned(1,2) );
        m.remove(1,2); // see if it blows up if it removes nothing
        assertFalse( m.isAssigned(1,2) );

        assertTrue(isStructureValid(m));

        // rest of the matrix should be the same
        for (int row = 0; row < m.getNumRows(); row++) {
            for (int col = 0; col < m.getNumCols(); col++) {
                if( row != 1 && col != 2 ) {
                    assertEquals(c.get(row,col),m.get(row,col), UtilEjml.TEST_F64);
                }
            }
        }
    }

    @Test
    public void remove_case1() {
        DMatrixSparse m = (DMatrixSparse)createMatrix(3,3);
        // create an identify matrix
        m.zero();
        for (int i = 0; i < 3; i++) {
            m.set(i,i,i+1);
        }

        // remove the middle element
        m.remove(1,1);
        assertFalse( m.isAssigned(1,1) );

        // make sure it's the expected length
        for (int row = 0; row < m.getNumRows(); row++) {
            for (int col = 0; col < m.getNumCols(); col++) {
                if( row != col ) {
                    assertEquals(0,m.get(row,col),UtilEjml.TEST_F64);
                } else if( row != 1 ){
                    assertEquals(row+1,m.get(row,col),UtilEjml.TEST_F64);
                } else {
                    assertEquals(0,m.get(row,col),UtilEjml.TEST_F64);
                }
            }
        }
    }

    @Test
    public void reshape() {
        DMatrixSparse m = (DMatrixSparse)createMatrix(3,4);
        m.reshape(2,4,8);
        assertEquals(0,m.getNumElements());
        assertEquals(2,m.getNumRows());
        assertEquals(4,m.getNumCols());
    }

    @Test
    public void iterator() {
        DMatrixSparse m = createSparse(3,4);
        assertFalse( m.createCoordinateIterator().hasNext() );

        m.set(0,2,3.0);
        m.set(2,3,2.0);

        Iterator<DMatrixSparse.CoordinateRealValue> iter = m.createCoordinateIterator();
        int[] matched = new int[2];
        while( iter.hasNext() ) {
            DMatrixSparse.CoordinateRealValue value = iter.next();
            if( value.row == 0 && value.col == 2 && value.value == 3.0 ) {
                matched[0]++;
            } else if( value.row == 2 && value.col == 3 && value.value == 2.0 ) {
                matched[1]++;
            } else {
                fail("Unexpected value");
            }
        }
        assertEquals(1,matched[0]);
        assertEquals(1,matched[1]);
    }

}
