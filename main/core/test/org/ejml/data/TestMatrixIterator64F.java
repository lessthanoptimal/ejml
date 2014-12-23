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

import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestMatrixIterator64F {

    Random rand = new Random(234234);

    @Test
    public void allRow() {
        DenseMatrix64F A = RandomMatrices.createRandom(3,6,rand);

        MatrixIterator64F iter = A.iterator(true,0, 0, A.numRows-1, A.numCols-1);

        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                assertTrue(iter.hasNext());
                assertEquals(A.get(i,j),iter.next(),1e-8);
            }
        }
        assertTrue(!iter.hasNext());
    }

    @Test
    public void allCol() {
        DenseMatrix64F A = RandomMatrices.createRandom(3,6,rand);

        MatrixIterator64F iter = A.iterator(false,0, 0, A.numRows-1, A.numCols-1);

        for( int j = 0; j < A.numCols; j++ ) {
            for( int i = 0; i < A.numRows; i++ ) {
                assertTrue(iter.hasNext());
                assertEquals(A.get(i,j),iter.next(),1e-8);
            }
        }
        assertTrue(!iter.hasNext());
    }

    @Test
    public void subRow() {
        DenseMatrix64F A = RandomMatrices.createRandom(3,6,rand);

        MatrixIterator64F iter = A.iterator(true,1, 2 , A.numRows-2, A.numCols-1);

        for( int i = 1; i < A.numRows-1; i++ ) {
            for( int j = 2; j < A.numCols; j++ ) {
                assertTrue(iter.hasNext());
                assertEquals(A.get(i,j),iter.next(),1e-8);
            }
        }
        assertTrue(!iter.hasNext());

    }

    @Test
    public void subCol() {
        DenseMatrix64F A = RandomMatrices.createRandom(3,6,rand);

        MatrixIterator64F iter = A.iterator(false,1, 2 , A.numRows-2, A.numCols-1);

        for( int j = 2; j < A.numCols; j++ ) {
            for( int i = 1; i < A.numRows-1; i++ ) {
                assertTrue(iter.hasNext());
                assertEquals(A.get(i,j),iter.next(),1e-8);
            }
        }
        assertTrue(!iter.hasNext());
    }

}
