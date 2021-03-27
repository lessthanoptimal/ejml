/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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
package org.ejml.masks;

/**
 * Mask implementation backed by a primitive array
 */
public class DMaskPrimitive extends Mask {
    // Values interpreted as a row-major dense matrix
    private final double[] values;
    /**
     * Number of columns of the wrapped matrix
     */
    public final int numCols;
    /**
     * Value representing that the entry is not set in the mask
     */
    public final double zeroElement;

    public DMaskPrimitive( double[] values, int numCols, boolean negated, double zeroElement ) {
        // for dense structures they cannot be used for structural masks
        super(negated);
        this.values = values;
        this.numCols = numCols;
        this.zeroElement = zeroElement;
    }

    @Override
    public boolean isSet( int row, int col ) {
        // XOR as negated flips the mask flag
        return negated ^ (values[row*numCols + col] != zeroElement);
    }

    @Override
    public int getNumCols() {
        return numCols;
    }

    @Override
    public int getNumRows() {
        return values.length/numCols;
    }

    @Override
    public void setIndexColumn( int column ) {}

    @Override
    public int maxMaskedEntries() {
        return values.length;
    }

    @Override
    public boolean isSet( int index ) {
        return negated ^ (values[index] != zeroElement);
    }

    /**
     * Utility class to build {@link DMaskPrimitive}
     */
    public static class Builder extends MaskBuilder<DMaskPrimitive> {
        private double[] values;
        private int numCols = 1;
        private double zeroElement = 0;

        public Builder( double[] values ) {
            this.values = values;
        }

        /**
         * @param numCols Number of columns in the values
         */
        public Builder withNumCols( int numCols ) {
            this.numCols = numCols;
            return this;
        }

        /**
         * @param zeroElement Value to represent the zero-element in the mask
         */
        public Builder withZeroElement( double zeroElement ) {
            this.zeroElement = zeroElement;
            return this;
        }

        @Override
        public DMaskPrimitive build() {
            return new DMaskPrimitive(values, numCols, negated, zeroElement);
        }
    }
}
