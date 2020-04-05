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
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestDMatrixIterator {

    Random rand = new Random(234234);

    @Test
    public void allRow() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3,6,rand);

        DMatrixIterator iter = A.iterator(true,0, 0, A.numRows-1, A.numCols-1);

        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                assertTrue(iter.hasNext());
                assertEquals(A.get(i,j),iter.next(), UtilEjml.TEST_F64);
            }
        }
        assertTrue(!iter.hasNext());
    }

    @Test
    public void allCol() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3,6,rand);

        DMatrixIterator iter = A.iterator(false,0, 0, A.numRows-1, A.numCols-1);

        for( int j = 0; j < A.numCols; j++ ) {
            for( int i = 0; i < A.numRows; i++ ) {
                assertTrue(iter.hasNext());
                assertEquals(A.get(i,j),iter.next(),UtilEjml.TEST_F64);
            }
        }
        assertTrue(!iter.hasNext());
    }

    @Test
    public void subRow() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3,6,rand);

        DMatrixIterator iter = A.iterator(true,1, 2 , A.numRows-2, A.numCols-1);

        for( int i = 1; i < A.numRows-1; i++ ) {
            for( int j = 2; j < A.numCols; j++ ) {
                assertTrue(iter.hasNext());
                assertEquals(A.get(i,j),iter.next(),UtilEjml.TEST_F64);
            }
        }
        assertTrue(!iter.hasNext());

    }

    @Test
    public void subCol() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3,6,rand);

        DMatrixIterator iter = A.iterator(false,1, 2 , A.numRows-2, A.numCols-1);

        for( int j = 2; j < A.numCols; j++ ) {
            for( int i = 1; i < A.numRows-1; i++ ) {
                assertTrue(iter.hasNext());
                assertEquals(A.get(i,j),iter.next(),UtilEjml.TEST_F64);
            }
        }
        assertTrue(!iter.hasNext());
    }

}
