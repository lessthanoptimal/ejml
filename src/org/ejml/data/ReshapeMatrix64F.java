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

/**
 * An abstract class for all 64 bit floating point rectangular matrices which can be reshaped.
 *
 * @author Peter Abeles
 */
public abstract class ReshapeMatrix64F implements Matrix64F {

    private static final long serialVersionUID = 423423451942L;
    
    /**
     * Number of rows in the matrix.
     */
    public int numRows;
    /**
     * Number of columns in the matrix.
     */
    public int numCols;


    /**
     * <p>
     * Changes the number of rows and columns in the matrix, allowing its size to grow or shrink.
     * If the saveValues flag is set to true, then the previous values will be maintained, but
     * reassigned to new elements in a row-major ordering.  If saveValues is false values will only
     * be maintained when the requested size is less than or equal to the internal array size.
     * The primary use for this function is to encourage data reuse and avoid unnecessarily declaring
     * and initialization of new memory.
     * </p>
     *
     * <p>
     * Examples:<br>
     * [ 1 2 ; 3 4 ] -> reshape( 2 , 3 , true ) = [ 1 2 3 ; 4 0 0 ]<br>
     * [ 1 2 ; 3 4 ] -> reshape( 1 , 2 , true ) = [ 1 2 ]<br>
     * [ 1 2 ; 3 4 ] -> reshape( 1 , 2 , false ) = [ 1 2 ]<br>
     * [ 1 2 ; 3 4 ] -> reshape( 2 , 3 , false ) = [ 0 0 0 ; 0 0 0 ]
     * </p>
     *
     * @param numRows The new number of rows in the matrix.
     * @param numCols The new number of columns in the matrix.
     * @param saveValues If true then the value of each element will be save using a row-major reordering.  Typically this should be false.
     */
    public abstract void reshape(int numRows, int numCols, boolean saveValues);

    /**
     * Equivalent to invoking reshape(numRows,numCols,false);
     *
     * @param numRows The new number of rows in the matrix.
     * @param numCols The new number of columns in the matrix.
     */
    public void reshape( int numRows , int numCols ) {
        reshape(numRows,numCols,false);
    }

    /**
     * Creates a new iterator for traversing through a submatrix inside this matrix.  It can be traversed
     * by row or by column.  Range of elements is inclusive, e.g. minRow = 0 and maxRow = 1 will include rows
     * 0 and 1.  The iteration starts at (minRow,minCol) and ends at (maxRow,maxCol)
     *
     * @param rowMajor true means it will traverse through the submatrix by row first, false by columns.
     * @param minRow first row it will start at.
     * @param minCol first column it will start at.
     * @param maxRow last row it will stop at.
     * @param maxCol last column it will stop at.
     * @return A new MatrixIterator
     */
    public MatrixIterator iterator(boolean rowMajor, int minRow, int minCol, int maxRow, int maxCol)
    {
        return new MatrixIterator(this,rowMajor, minRow, minCol, maxRow, maxCol);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumRows() {
        return numRows;    
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumCols() {
        return numCols;
    }

    /**
     * Sets the number of rows.
     *
     * @param numRows Number of rows
     */
    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }

    /**
     * Sets the number of columns.
     *
     * @param numCols Number of columns
     */
    public void setNumCols(int numCols) {
        this.numCols = numCols;
    }

    /**
     * Assigns the value of 'this' matrix to be the same as 'A'.  The shape of
     * both matrices must be the same.
     *
     * @param A The matrix whose value is to be copied into 'this'.
     */
    public void set( ReshapeMatrix64F A ) {
        if( A.numRows != numRows )
            throw new IllegalArgumentException("Unexpected number of rows.");

        if( A.numCols != numCols )
            throw new IllegalArgumentException("Unexpected number of columns.");


        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                set(i,j,A.get(i,j));
            }
        }
    }
}
