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

package org.ejml.data;

import org.ejml.UtilEjml;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public abstract class GenericTestsSparseMatrix_F64 extends GenericTestsDenseMatrix_F64
{
    Random rand = new Random(234);

    public boolean assignable = true;

    public abstract Matrix_F64 createSparse( int numRows , int numCols );

    public abstract Matrix_F64 createSparse(SMatrixTriplet_F64 orig);

    @Override
    protected Matrix_F64 createMatrix(int numRows, int numCols) {

        // define a sparse matrix with every element filled.  It should act low a slow and inefficient
        // row matrix now
        SMatrixTriplet_F64 t = new SMatrixTriplet_F64(numRows,numCols,numRows*numCols);

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                t.addItem(row,col, rand.nextGaussian());
            }
        }

        return createSparse(t);
    }

    @Test
    public void set() {
        Matrix_F64 m = createSparse(3,4);

        if( assignable ) {
            m.set(1, 2, 10);
            m.set(1, 2, 15);

            assertEquals(15, m.get(1, 2), UtilEjml.TEST_F64);
        } else {
            try {
                m.set(1,2,10);
                fail("Should have thrown an exception");
            } catch( RuntimeException ignore){}
        }
    }

    @Test
    public void get() {
        SMatrixTriplet_F64 tmp = new SMatrixTriplet_F64(3,4,1);
        tmp.addItem(1,2,5);

        Matrix_F64 m = createSparse(tmp);

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
    public void remove() {
        fail("Implement");
    }
}
