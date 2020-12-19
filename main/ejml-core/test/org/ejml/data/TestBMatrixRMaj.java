/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestBMatrixRMaj {
    @Test
    public void getNumElements() {
        BMatrixRMaj M = new BMatrixRMaj(4,5);
        assertEquals(20, M.getNumElements());
    }

    @Test
    public void getIndex() {
        BMatrixRMaj M = new BMatrixRMaj(4,5);
        assertEquals(2*5+3, M.getIndex(2,3));
    }

    @Test
    public void reshape() {
        BMatrixRMaj M = new BMatrixRMaj(4,5);
        boolean data[] = M.data;

        M.reshape(3, 4);
        assertEquals(12, M.getNumElements());
        assertTrue(data == M.data);

        M.reshape(6, 7);
        assertEquals(6 * 7, M.getNumElements());
        assertTrue(6*7 <= M.data.length);
        assertTrue(data != M.data);

    }

    @Test
    public void getNumRows() {
        BMatrixRMaj M = new BMatrixRMaj(4,5);
        assertEquals(4, M.getNumRows());
    }

    @Test
    public void getNumCols() {
        BMatrixRMaj M = new BMatrixRMaj(4,5);
        assertEquals(5, M.getNumCols());
    }

    @Test
    public void copy() {
        BMatrixRMaj M = new BMatrixRMaj(4,5);
        M.set(2,3,true);

        BMatrixRMaj N = M.copy();
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                if( row == 2 && col == 3 )
                    assertEquals(true,N.get(2,3));
                else
                    assertEquals(false,N.get(row,col));
            }
        }
    }

    @Test
    public void set_matrix() {
        BMatrixRMaj M = new BMatrixRMaj(4,5);
        M.set(2,3,true);

        BMatrixRMaj N = new BMatrixRMaj(4,5);
        N.setTo(M);

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                if( row == 2 && col == 3 )
                    assertEquals(true,N.get(2,3));
                else
                    assertEquals(false,N.get(row,col));
            }
        }
    }

    @Test
    public void set_get_row_col() {
        BMatrixRMaj M = new BMatrixRMaj(4,5);

        assertEquals(false, M.get(2, 3));
        M.set(2, 3, true);
        assertEquals(true, M.get(2, 3));

        try {
            M.set(6,6,true);
            fail("Should have thrown an exception");
        } catch( IllegalArgumentException ignore){}
        try {
            M.get(6, 6);
            fail("Should have thrown an exception");
        } catch( IllegalArgumentException ignore){}

    }

    @Test
    public void unsafe_set_get_row_col() {
        BMatrixRMaj M = new BMatrixRMaj(4,5);

        assertEquals(false, M.get(2, 3));
        M.set(2, 3, true);
        assertEquals(true, M.get(2, 3));
    }

    @Test
    public void fill() {
        BMatrixRMaj M = new BMatrixRMaj(4,5);
        M.set(0,1,true);
        M.set(3,1,true);

        M.fill(false);
        for (int i = 0; i < M.getNumElements(); i++) {
            assertFalse(M.get(i));
        }
        M.fill(true);
        for (int i = 0; i < M.getNumElements(); i++) {
            assertTrue(M.get(i));
        }
    }

    @Test
    public void sum() {
        BMatrixRMaj M = new BMatrixRMaj(4,5);

        assertEquals(0,M.sum());

        M.set(0,1,true);
        M.set(3,1,true);

        assertEquals(2,M.sum());

    }

}