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

/**
 * @author Peter Abeles
 */
public class SortCoupledArray {

    int tmp[] = new int[0];

    int copyA[] = new int[0];
    double copyB[] = new double[0];
    
    QuickSort_S32 quicksort = new QuickSort_S32();

    public void sort( int segments[] , int length, int valuesA[], double valuesB[] ) {
        for (int i = 1; i < length; i++) {
            int x0 = segments[i-1];
            int x1 = segments[i];

            sort( x0, x1-x0, valuesA, valuesB);
        }
    }
    
    private void sort( int offset , int length , int valuesA[], double valuesB[] ) {

        if( length <= 1 )
            return;

        if( tmp.length < length ) {
            tmp = new int[length];
            copyA = new int[ length ];
            copyB = new double[ length ];
        }
        
        System.arraycopy(valuesA,offset,copyA,0,length);
        System.arraycopy(valuesB,offset,copyB,0,length);

        quicksort.sort(copyA,length,tmp);

        for (int i = 0; i < length; i++) {
            valuesA[offset+i] = copyA[tmp[i]];
            valuesB[offset+i] = copyB[tmp[i]];
        }
    }
}
