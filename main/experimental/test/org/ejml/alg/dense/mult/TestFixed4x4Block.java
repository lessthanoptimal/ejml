/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.mult;

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.FixedMatrix4x4_64F;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestFixed4x4Block {

    Random rand = new Random(234);

    @Test
    public void get() {
        Fixed4x4Block A = new Fixed4x4Block(3,2);

        A.blocks[2].a23 = 5;

        int row = (2/2)*4 + 1;
        int col = (2%2)*4 + 2;

        assertEquals(5,A.get(row,col),1e-8);
    }

    @Test
    public void set_index() {
        Fixed4x4Block A = new Fixed4x4Block(3,2);

        int row = (2/2)*4 + 1;
        int col = (2%2)*4 + 2;

        A.set(row,col,5);

        assertEquals(5,A.blocks[2].a23,1e-8);
    }

    @Test
    public void getNumRows() {
        Fixed4x4Block A = new Fixed4x4Block(3,2);
        assertEquals(12,A.getNumRows());
    }

    @Test
    public void getNumCols() {
        Fixed4x4Block A = new Fixed4x4Block(3,2);
        assertEquals(8,A.getNumCols());
    }

    @Test
    public void set_matrix() {
        DenseMatrix64F A = RandomMatrices.createRandom(4*12,4*7,rand);
        Fixed4x4Block B = new Fixed4x4Block(3,4);

        B.set(A,3,4);

        for (int i = 0; i < 3*4; i++) {
            for (int j = 0; j < 4*4; j++) {
                assertEquals(A.get(3+i,4+j),B.get(i,j),1e-8);
            }
        }
    }

    @Test
    public void setBlock() {
        DenseMatrix64F A = RandomMatrices.createRandom(6,7,rand);
        FixedMatrix4x4_64F B = new FixedMatrix4x4_64F();

        Fixed4x4Block.setBlock(A,2,3,B);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(A.get(2+i,3+j),B.get(i,j),1e-8);
            }
        }
    }
}
