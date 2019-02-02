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
 * An array of primitive arrays which emulates a continuous chunk of memory.
 * Useful when the array is growing
 *
 * @author Peter Abeles
 */
public class DBlockArray {
    // number of elements in a block. Private tp prevent people from changing it. That would be bad
    private int BLOCK_SIZE;
    // to reduce the number of array copies more blocks are allocated than needed
    // arrays at the end can be null
    public double[][] data = new double[10][];
    // number of blocks defined
    public int blockCount;
    // number of elements total
    public int length;

    public DBlockArray(int blockSize) {
        this.BLOCK_SIZE = blockSize;
        this.data[0] = new double[this.BLOCK_SIZE];
        blockCount = 0;
        this.length = 0;
    }

    public DBlockArray() {
        this(1024);
    }

    public int length() {
        return length;
    }

    /**
     * Adds an element to the end of the list
     */
    public void add( double value ) {
        int block = length/BLOCK_SIZE;
        if( block >= blockCount ) {
            checkGrowBlockBins(block+1);

            if( data[blockCount] == null ) {
                data[blockCount] = new double[BLOCK_SIZE];
            }
            blockCount++;
        }
        data[block][length%BLOCK_SIZE] = value;
        length += 1;
    }

    /**
     * Increases the number of blocks so that the specified number is allocated. Extra are allocated.
     * @param desired Desired number of blocks
     */
    private void checkGrowBlockBins(int desired ) {
        // see if the number of blocks needs to be increased
        if( desired >= data.length ) {
            // increase the number of blocks, adding more than is needed
            // while making sure int isn't overloaded
            int s = desired + Math.max(1000,desired);
            if( s < 0 )
                s = Integer.MAX_VALUE;
            double[][] tmp = new double[s][];
            System.arraycopy(data,0,tmp,0,data.length);
            data = tmp;
        }
    }

    public double[] getBlock( int index ) {
        return data[index/BLOCK_SIZE];
    }

    /**
     * Number of used elements in a block
     */
    public int getBlockLength( int blockIndex ) {
        return Math.min(BLOCK_SIZE, length-blockIndex*BLOCK_SIZE);
    }

    public double getTail( int index ) {
        return get(length-index-1);
    }

    /**
     * Returns the value of the element at the specified index in the array. Performs a bounds check
     * @param index index in the array
     * @return value
     */
    public double get( int index ) {
        if( index < 0 || index >= length )
            throw new IllegalArgumentException("Out of bounds. "+index+" length="+length);

        int block = index/ BLOCK_SIZE;
        return data[block][index% BLOCK_SIZE];
    }

    /**
     * Sets the value of the element at the specified index in the array. Performs a bounds check
     * @param index index in the array
     * @param value The new value
     */
    public void set( int index , double value ) {
        if( index < 0 || index >= length )
            throw new IllegalArgumentException("Out of bounds. "+index+" length="+length);

        int block = index/ BLOCK_SIZE;
        data[block][index% BLOCK_SIZE] = value;
    }

    /**
     * Removes all elements from the array from idx0 to idx1, inclusive.
     * @param idx0 first index, inclusive
     * @param idx1 second index, inclusive
     */
    public void remove( int idx0 , int idx1 ) {
        if( idx1 < idx0 )
            throw new IllegalArgumentException("Failed idx0 < idx1.");
        if( idx1 >= length )
            throw new IllegalArgumentException("bad: idx1 >= length");

        int length = idx1 - idx0 + 1;
        if( length < BLOCK_SIZE ) {
            int block0 = idx0/BLOCK_SIZE;
            int where0 = idx0%BLOCK_SIZE;
            double b0[] = data[block0];

            int lengthInBlock = Math.max(BLOCK_SIZE-where0,length);
            for (int i = 0; i < lengthInBlock; i++) {
                b0[where0+i] = b0[where0+i+length];
            }
            int lengthInNextBlock = length-lengthInBlock;
            if( lengthInNextBlock > 0 ) {
                double b1[] = data[block0+1];

            }

            if( where0 + length < BLOCK_SIZE ) {

            } else {

            }

        } else {

        }

        // TODO update length and number of blocks
    }

    public double unsafe_getTail( int index ) {
        return unsafe_get(length-index-1);
    }

    public double unsafe_get( int index ) {
        int block = index/ BLOCK_SIZE;
        return data[block][index% BLOCK_SIZE];
    }

    public void unsafe_set( int index , double value ) {
        int block = index/ BLOCK_SIZE;
        data[block][index% BLOCK_SIZE] = value;
    }

    /**
     * Allocates enough memory so that the specified number of elements can be saved
     *
     * @param length The new desired length
     */
    public void resize( int length ) {
        // compute the number of blocks required
        int blocks = length/BLOCK_SIZE+1;

        // Ensure that the storage for these blocks have been allocated
        if( data.length < blocks ) {
            checkGrowBlockBins(blocks);
        }

        // if the number of blocks will be increased make sure arrays have been declared already
        for (int i = blockCount; i < blocks; i++) {
            if( data[i] == null )
                data[i] = new double[BLOCK_SIZE];
        }
        this.blockCount = blocks;
        this.length = length;
    }

    /**
     * Computes the number of elements which can be stored in this array without declaring any more
     * memory
     */
    public int computeAllocated() {
        int total = 0;
        for (int i = 0; i < data.length && data[i] != null; i++) {
            total += BLOCK_SIZE;
        }
        return total;
    }

    /**
     * Assigns the values in src into this array. The specific data structures might be slightly different. The
     * block size of the two arrays needs to be identical.
     * @param src Original array being copied
     */
    public void set( DBlockArray src ) {
        if( src.getBlockSize() != BLOCK_SIZE )
            throw new IllegalArgumentException("Block sizes not the same!");
        checkGrowBlockBins(src.blockCount);
        for (int i = 0; i < src.blockCount; i++) {
            double dst[] = data[i];
            if( dst == null ) {
                dst = data[i] = new double[BLOCK_SIZE];
            }
            System.arraycopy(src.data[i],0,dst,0,BLOCK_SIZE);
        }

        this.blockCount = src.blockCount;
        this.length = src.length;
    }

    public void clear() {
        blockCount = 0;
        length = 0;
    }

    /**
     * Discards unused blocks
     */
    public void shrink() {
        if( blockCount < data.length ) {
            double[][] tmp = new double[blockCount][];
            System.arraycopy(data,0,tmp,0,tmp.length);
            data = tmp;
        }
    }

    /**
     * Frees all allocated memory and puts it back into the initial state
     */
    public void free() {
        data = new double[10][BLOCK_SIZE];
        blockCount = 0;
        length = 0;
    }

    /**
     * Returns the size of a block
     */
    public int getBlockSize() {
        return BLOCK_SIZE;
    }
}
