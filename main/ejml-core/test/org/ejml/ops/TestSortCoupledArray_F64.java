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

package org.ejml.ops;

import org.ejml.UtilEjml;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestSortCoupledArray_F64 {

    Random rand = new Random(234);

    @Test
    public void sort() {

        int bins[] = new int[5];
        bins[0] = 0;
        bins[1] = 2;
        bins[2] = 2;
        bins[3] = 6;
        bins[4] = 10;

        int dataA[] = new int[10];
        double dataB[] = new double[10];

        for (int i = 0; i < dataA.length; i++) {
            dataA[i] = rand.nextInt();
            dataB[i] = rand.nextDouble();
        }

        int origA[] = dataA.clone();
        double origB[] = dataB.clone();

        SortCoupledArray_F64 sorter = new SortCoupledArray_F64();

        sorter.quick(bins,5,dataA,dataB);

        for (int i = 1; i < bins.length; i++) {
            int idx0 = bins[i-1];
            int idx1 = bins[i];

            for (int j = idx0+1; j < idx1; j++) {
                assertTrue(dataA[j-1]<dataA[j]);
            }
            for (int j = idx0; j < idx1; j++) {
                checkMatch(dataA[j],dataB[j],origA,origB);
            }
        }
    }

    private void checkMatch( int foundA , double foundB , int origA[] , double origB[]) {
        for (int i = 0; i < origA.length; i++) {
            if( origA[i] == foundA ) {
                assertEquals(foundB,origB[i], UtilEjml.TEST_F64);
                return;
            }
        }
        fail("can't find match");
    }
}