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

package org.ejml.equation;

/**
 * Interface for an ordered sequence of integer values
 *
 * @author Peter Abeles
 */
public interface IntegerSequence {

    int length();

    void reset();

    int next();

    boolean hasNext();

    /**
     * An array of integers which was explicitly specified
     */
    class Array implements IntegerSequence {

        int array[];
        int where;

        public Array(int[] array) {
            this.array = array;
        }

        @Override
        public int length() {
            return array.length;
        }

        @Override
        public void reset() {
            where = 0;
        }

        @Override
        public int next() {
            return array[where++];
        }

        @Override
        public boolean hasNext() {
            return where < array.length;
        }

        public int[] getArray() {
            return array;
        }
    }

    /**
     * A sequence of integers which has been specified using a start number, end number, and step size.
     *
     * 2:3:21 = 2 5 8 11 14 17 20
     */
    class For implements IntegerSequence {

        int start;
        int step;
        int end;

        int length;
        int where;

        public For(int start, int step, int end) {
            this.start = start;
            this.step = step;
            this.end = end;

            length = (end-start)/step+1;
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public void reset() {
            where = 0;
        }

        @Override
        public int next() {
            return start + step*where++;
        }

        @Override
        public boolean hasNext() {
            return where <= length;
        }

        public int getStart() {
            return start;
        }

        public int getStep() {
            return step;
        }

        public int getEnd() {
            return end;
        }
    }
}
