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

/**
 * A double array which can have its size changed
 *
 * @author Peter Abeles
 */
public class DGrowArray {
    public double data[];
    public int length;

    public DGrowArray(int length) {
        this.data = new double[length];
        this.length = length;
    }

    public DGrowArray() {
        this(0);
    }

    public int length() {
        return length;
    }

    /**
     * Changes the array's length and doesn't attempt to preserve previous values if a new array is required
     *
     * @param length New array length
     */
    public void reshape( int length ) {
        if( data.length < length ) {
            data = new double[length];
        }
        this.length = length;
    }

    /**
     * Increases the internal array's length by the specified amount. Previous values are preserved.
     * The length value is not modified since this does not change the 'meaning' of the array, just
     * increases the amount of data which can be stored in it.
     *
     * this.data = new data_type[ data.length + amount ]
     *
     *
     * @param amount Number of elements added to the internal array's length
     */
    public void growInternal(int amount ) {
        double tmp[] = new double[ data.length + amount ];

        System.arraycopy(data,0,tmp,0,data.length);
        this.data = tmp;
    }

    public void set( DGrowArray original ) {
        reshape(original.length);
        System.arraycopy(original.data,0,data,0,original.length);
    }

    public double get( int index ) {
        if( index < 0 || index >= length )
            throw new IllegalArgumentException("Out of bounds");
        return data[index];
    }

    public void set( int index , double value ) {
        if( index< 0 || index >= length )
            throw new IllegalArgumentException("Out of bounds");
        data[index] = value;
    }

    public void free() {
        data = new double[0];
        length = 0;
    }
}
