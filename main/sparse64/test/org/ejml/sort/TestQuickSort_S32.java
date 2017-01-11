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

package org.ejml.sort;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestQuickSort_S32 {

    Random rand = new Random(234);

    @Test
    public void sort() {
        int[] data = new int[20];
        for (int i = 0; i < data.length; i++) {
            data[i] = rand.nextInt();
        }

        int[] indexes = new int[ data.length ];

        QuickSort_S32 sorter = new QuickSort_S32();

        sorter.sort(data,15,indexes);

        for (int i = 1; i < 15; i++) {
            assertTrue(data[indexes[i-1]]<data[indexes[i]]);
        }
    }
}