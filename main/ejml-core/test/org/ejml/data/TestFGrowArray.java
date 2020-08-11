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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestFGrowArray {
    @Test
    public void length() {
        FGrowArray array = new FGrowArray();
        assertEquals(0,array.length());

        array = new FGrowArray(5);
        assertEquals(5,array.length());
    }

    @Test
    public void reshape() {
        FGrowArray array = new FGrowArray();
        assertEquals(0,array.length());

        array.reshape(10);
        assertEquals(10,array.data.length);
        assertEquals(10,array.length());

        array.reshape(5);
        assertEquals(10,array.data.length);
        assertEquals(5,array.length());
    }

    @Test
    public void growInternal() {
        FGrowArray array = new FGrowArray();
        array.reshape(10);
        for (int i = 0; i < array.length; i++) {
            array.data[i] = i;
        }
        int expected = array.data.length+4;
        array.growInternal(4);
        assertEquals(expected,array.data.length);
        for (int i = 0; i < array.length; i++) {
            assertEquals(i, array.get(i), UtilEjml.TEST_F32);
        }
    }

    @Test
    public void get() {
        FGrowArray array = new FGrowArray(8);
        array.reshape(5);
        array.data[0] = 1;
        array.data[2] = 3;
        array.data[3] = 2;

        assertEquals(1,array.get(0), UtilEjml.TEST_F32);
        assertEquals(0,array.get(1), UtilEjml.TEST_F32);
        assertEquals(3,array.get(2), UtilEjml.TEST_F32);
        assertEquals(2,array.get(3), UtilEjml.TEST_F32);

        try {
            array.get(5);
            fail("should have thrown an exception");
        } catch( RuntimeException ignore ){}
        try {
            array.get(-1);
            fail("should have thrown an exception");
        } catch( RuntimeException ignore ){}

    }

    @Test
    public void set() {
        FGrowArray array = new FGrowArray(8);
        array.reshape(5);
        array.set(0,1);
        array.set(2,3);
        array.set(3,2);

        assertEquals(1,array.data[0], UtilEjml.TEST_F32);
        assertEquals(0,array.data[1], UtilEjml.TEST_F32);
        assertEquals(3,array.data[2], UtilEjml.TEST_F32);
        assertEquals(2,array.data[3], UtilEjml.TEST_F32);

        try {
            array.set(5,2);
            fail("should have thrown an exception");
        } catch( RuntimeException ignore ){}
        try {
            array.set(-1,2);
            fail("should have thrown an exception");
        } catch( RuntimeException ignore ){}
    }

    @Test
    public void free() {
        IGrowArray array = new IGrowArray(8);
        array.free();
        assertEquals(0,array.length());
        assertEquals(0,array.data.length);
    }

}
