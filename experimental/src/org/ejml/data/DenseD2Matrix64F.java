/*
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

/*
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

import java.io.Serializable;


/**
 * A dense matrix where the array is stored as a 2D array.
 *
 * @author Peter Abeles
 */
public class DenseD2Matrix64F extends ReshapeMatrix64F implements Serializable {

    /**
     * Where the raw data for the matrix is stored.  The format is type dependent.
     */
    public double[][] data;

    public DenseD2Matrix64F( int numRows , int numCols ) {
        data = new double[ numRows ][numCols];
        this.numRows = numRows;
        this.numCols = numCols;
    }

//    public double[][] getData() {
//        return data;
//    }

    @Override
    public void reshape(int numRows, int numCols, boolean saveValues) {
        if( numRows <= data.length ) {
            this.numRows = numRows;
        } else {
            throw new IllegalArgumentException("Requested number of rows is too great.");
        }

        if( numCols <= data[0].length ) {
            this.numCols = numCols;
        } else {
            throw new IllegalArgumentException("Requested number of columns is too great.");
        }
    }

    @Override
    public double get(int row, int col) {
        return data[row][col];
    }

    @Override
    public void set(int row, int col, double val) {
        data[row][col] = val;
    }

    @Override
    public double unsafe_get( int row, int col) {
        return get(row,col);
    }

    @Override
    public void unsafe_set( int row, int col, double val) {
        set(row,col,val);
    }

    @Override
    public int getNumElements() {
        return numRows*numCols;
    }

    @Override
    public void print() {
    }

    @Override
    public <T extends Matrix64F> T copy() {
        return null;
    }
}