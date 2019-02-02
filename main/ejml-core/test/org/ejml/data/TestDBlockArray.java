/*
 * Copyright (c) 2009-2019, Peter Abeles. All Rights Reserved.
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

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestDBlockArray {

    @Test
    public void add() {
        DBlockArray a = new DBlockArray(3);
        for (int i = 0; i < 10; i++) {
            a.add(i);
            assertEquals(i+1,a.length());
            assertEquals(i,a.get(i), UtilEjml.TEST_F64);
        }
    }

    @Test
    public void getBlock() {
        DBlockArray a = new DBlockArray(3);
        assertTrue(a.data[0]==a.getBlock(0));
        assertTrue(a.data[0]==a.getBlock(1));
        assertTrue(a.data[0]==a.getBlock(2));
        assertTrue(null==a.getBlock(3));
    }

    @Test
    public void getBlockLength() {
        DBlockArray a = new DBlockArray(3);
        assertEquals(0,a.getBlockLength(0));
        a.resize(4);
        assertEquals(3,a.getBlockLength(0));
        assertEquals(1,a.getBlockLength(1));
    }

    @Test
    public void getTail() {
        fail("implement");
    }

    @Test
    public void set_get() {
        DBlockArray a = new DBlockArray(3);

        a.resize(5);
        assertEquals(0,a.get(1), UtilEjml.TEST_F64);
        a.set(1,1.5);
        assertEquals(1.5,a.get(1), UtilEjml.TEST_F64);

        // across block boundaries
        for (int i = 0; i < 5; i++) {
            a.set(i, i + 1.5);
            assertEquals(i + 1.5, a.get(i), UtilEjml.TEST_F64);
        }
    }

    @Test
    public void unsafe_getTail() {
        fail("implement");
    }

    @Test
    public void unsafe_set_get() {
        DBlockArray a = new DBlockArray(3);

        a.resize(5);
        assertEquals(0,a.unsafe_get(1), UtilEjml.TEST_F64);
        a.unsafe_set(1,1.5);
        assertEquals(1.5,a.unsafe_get(1), UtilEjml.TEST_F64);

        // across block boundaries
        for (int i = 0; i < 5; i++) {
            a.unsafe_set(i, i + 1.5);
            assertEquals(i + 1.5, a.unsafe_get(i), UtilEjml.TEST_F64);
        }
    }

    /**
     * Test remove when the number of elements is less than a single block
     */
    @Test
    public void remove_lessThanBlock() {

    }

    /**
     * Test remove when the number of elements is more than a block
     */
    @Test
    public void remove_moreThanBlock() {

    }

    @Test
    public void resize() {
        DBlockArray a = new DBlockArray(3);
        a.resize(21);
        assertEquals(21,a.length);
        assertEquals(21/3+1 , a.blockCount);
        assertTrue(a.data.length >= 21/3);
        assertTrue(a.computeAllocated() >= 21 );
    }

    @Test
    public void computeAllocated() {
        fail("implement");
    }

    @Test
    public void set_blockarray() {
        fail("implement");
    }

    @Test
    public void clear() {
        fail("implement");
    }

    @Test
    public void shrink() {
        fail("implement");
    }

    @Test
    public void free() {
        fail("implement");
    }

    @Test
    public void getBlockSize() {
        fail("implement");
    }
}