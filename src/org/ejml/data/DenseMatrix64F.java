/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.data;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.mult.MatrixDimensionException;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixIO;


/**
 * <p>
 * DenseMatrix64F is a dense matrix with elements that are 64-bit floats (doubles).  A matrix
 * is the fundamental data structure in linear algebra.  Unlike a sparse matrix, there is no
 * compression in a dense matrix and every element is stored in memory.  This allows for fast
 * reads and writes to the matrix.
 * </p>
 *
 * <p>
 * To keep the code manageable and the library easier to use only basic functions for accessing and editing elements
 * are provided in this class.  The numerous operations which can be performed on DenseMatrix64F
 * are contained in various other classes, where the most common operations can be found in
 * the {@link org.ejml.ops.CommonOps} and {@link org.ejml.ops.SpecializedOps} classes.
 * </p>
 *
 * <p>
 * The matrix is stored internally in a row-major 1D array format:<br>
 * <br>
 * data[ y*numCols + x ] = data[y][x]<br>
 * <br>
 * For example:<br>
 * data =
 * <table border="1">
 * <tr>
 * <td>a<sub>11</sub></td>
 * <td>a<sub>12</sub></td>
 * <td>a<sub>13</sub></td>
 * <td>a<sub>14</sub></td>
 * <td>a<sub>21</sub></td>
 * <td>a<sub>22</sub></td>
 * <td>a<sub>23</sub></td>
 * <td>a<sub>24</sub></td>
 * <td>a<sub>31</sub></td>
 * <td>a<sub>32</sub></td>
 * <td>a<sub>33</sub></td>
 * <td>a<sub>34</sub></td>
 * <td>a<sub>41</sub></td>
 * <td>a<sub>42</sub></td>
 * <td>a<sub>43</sub></td>
 * <td>a<sub>44</sub></td>
 * </tr>
 * </table>
 * </p>
 *
 *
 * <p>
 * An alternative to working directly with DenseMatrix64 is {@link org.ejml.data.SimpleMatrix}.
 * SimpleMatrix is a wrapper around DenseMatrix64F that provides an easier to use object oriented way of manipulating
 * matrices, at the cost of efficiency.
 * </p>
 *
 * @see org.ejml.ops.CommonOps
 * @see org.ejml.ops.SpecializedOps
 * @see org.ejml.data.SimpleMatrix
 *
 * @author Peter Abeles
 */
public class DenseMatrix64F extends RowD1Matrix64F {

    /**
     * <p>
     * Creates a new matrix which has the same value as the matrix encoded in the
     * provided array.  The input matrix's format can either be row-major or
     * column-major and is translated to the native row-major format.
     * </p>
     *
     * @param numRows The number of rows.
     * @param numCols The number of columns.
     * @param rowMajor If the array is encoded in a row-major or a column-major format.
     * @param data The formatted 1D array. Not modified.
     */
    public DenseMatrix64F(int numRows, int numCols, boolean rowMajor, double... data) {
        final int length = numRows * numCols;
        this.data = new double[ length ];

        this.numRows = numRows;
        this.numCols = numCols;

        set(numRows,numCols, rowMajor, data);
    }

    /**
     * <p>
     * Creates a matrix with the values and shape defined by the 2D array 'data'.
     * The formatting in 'data' is the following:<br>
     *  <br>
     * data[ row ][ column ]
     * </p>
     * @param data 2D array representation of the matrix. Not modified.
     */
    public DenseMatrix64F( double data[][] ) {
        this.numRows = data.length;
        this.numCols = data[0].length;

        this.data = new double[ numRows*numCols ];

        int pos = 0;
        for( int i = 0; i < numRows; i++ ) {
            double []row = data[i];

            if( row.length != numCols ) {
                throw new IllegalArgumentException("All rows must have the same length");
            }

            System.arraycopy(row,0,this.data,pos,numCols);

            pos += numCols;
        }
    }

    /**
     * Creates a new Matrix with the specified shape whose elements initially
     * have the value of zero.
     *
     * @param numRows The number of rows in the matrix.
     * @param numCols The number of columns in the matrix.
     */
    public DenseMatrix64F( int numRows  , int numCols ) {
        data = new double[ numRows * numCols ];

        this.numRows = numRows;
        this.numCols = numCols;
    }

    /**
     * Creates a new matrix which is equivalent to the provided matrix.  Note that
     * the length of the data will be determined by the shape of the matrix.
     *
     * @param orig The matrix which is to be copied.  This is not modified or saved.
     */
    public DenseMatrix64F( DenseMatrix64F orig ) {
        this(orig.numRows,orig.numCols);
        System.arraycopy(orig.data, 0, this.data, 0, orig.getNumElements());
    }

    /**
     * This declares an array that can store a matrix up to the specified length.  This is usefull
     * when a matrix's size will be growing and it is desirable to avoid reallocating memory.
     *
     * @param length The size of the matrice's data array.
     */
    public DenseMatrix64F( int length ) {
        data = new double[ length ];
    }

    /**
     * Default constructor where nothing is assigned.  In general this should not be used.
     */
    public DenseMatrix64F(){}

    /**
     * Creates a new DenseMatrix64F which contains the same information as the provided Matrix64F.
     *
     * @param mat Matrix whose values will be copied.  Not modified.
     */
    public DenseMatrix64F(Matrix64F mat) {
        this(mat.numRows,mat.numCols);
        for( int i = 0; i < numRows; i++ ) {
            for( int j = 0; j < numCols; j++ ) {
                set(i,j, mat.get(i,j));
            }
        }
    }

    /**
     * Creates a new DenseMatrix64F around the provided data.  The data must encode
     * a row-major matrix.  Any modification to the returned matrix will modify the
     * provided data.
     *
     * @param numRows Number of rows in the matrix.
     * @param numCols Number of columns in the matrix.
     * @param data Data that is being wrapped. Referenced Saved.
     * @return A matrix which references the provided data internally.
     */
    public static DenseMatrix64F wrap( int numRows , int numCols , double []data ) {
        DenseMatrix64F s = new DenseMatrix64F();
        s.data = data;
        s.numRows = numRows;
        s.numCols = numCols;

        return s;
    }

    /**
     * <p>
     * Changes the number of rows and columns in the matrix.  If the requested matrix shape
     * can be performed with the current data array then nothing is changed but the matrix's
     * shape.  If the data's length is too small then a new array is declared.
     * </p>
     *
     * <p>
     * If saveValue is set to true then the data is value each element is guaranteed to not change.
     * Otherwise the old values will be lost if the matrix needs to grow.
     * </p>
     *
     * @param numRows The new number of rows in the matrix.
     * @param numCols The new number of columns in the matrix.
     * @param saveValues If true then the value of each element will not change.  Typically this should be false.
     */
    @Override
    public void reshape(int numRows, int numCols, boolean saveValues) {
        if( data.length < numRows * numCols ) {
            double []d = new double[ numRows*numCols ];

            if( saveValues ) {
                System.arraycopy(data,0,d,0,getNumElements());
            }

            this.data = d;
        }

        this.numRows = numRows;
        this.numCols = numCols;
    }

    /**
     * <p>
     * Assigns the element in the Matrix to the specified value.  Performs a bounds check to make sure
     * the requested element is part of the matrix. <br>
     * <br>
     * a<sub>ij</sub> = value<br>
     * </p>
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @param value The element's new value.
     */
    @Override
    public void set( int row , int col , double value ) {
        if( col < 0 || col >= numCols || row < 0 || row >= numRows ) {
            throw new IllegalArgumentException("Specified element is out of bounds");
        }

        data[ row * numCols + col ] = value;
    }

    @Override
    public void unsafe_set( int row , int col , double value ) {
        data[ row * numCols + col ] = value;
    }

    /**
     * <p>
     * Adds 'value' to the specified element in the matrix.<br>
     * <br>
     * a<sub>ij</sub> = a<sub>ij</sub> + value<br>
     * </p>
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @param value The value that is added to the element
     */
    // todo move to commonops
    public void add( int row , int col , double value ) {
        if( col < 0 || col >= numCols || row < 0 || row >= numRows ) {
            throw new IllegalArgumentException("Specified element is out of bounds");
        }

        data[ row * numCols + col ] += value;
    }

    /**
     * Returns the value of the specified matrix element.  Performs a bounds check to make sure
     * the requested element is part of the matrix.
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @return The value of the element.
     */
    @Override
    public double get( int row , int col ) {
        if( col < 0 || col >= numCols || row < 0 || row >= numRows ) {
            throw new IllegalArgumentException("Specified element is out of bounds: "+row+" "+col);
        }

        return data[ row * numCols + col ];
    }

    @Override
    public double unsafe_get( int row , int col ) {
        return data[ row * numCols + col ];
    }

    @Override
    public int getIndex( int row , int col ) {
        return row * numCols + col;
    }

    /**
     * Determins if the specified element is inside the bounds of the Matrix.
     *
     * @param row The element's row.
     * @param col The elements' column.
     * @return True if it is inside the matrices bound, false otherwise.
     */
    public boolean isInBounds( int row  , int col ) {
        return( col >= 0 && col < numCols && row >= 0 && row < numRows );
    }

    /**
     * Returns the number of elements in this matrix, which is equal to
     * the number of rows times the number of columns.
     *
     * @return The number of elements in the matrix.
     */
    @Override
    public int getNumElements() {
        return numRows*numCols;
    }


    /**
     * <p>
     * Sets the value and shape of this matrix to be identical to the specified matrix. The width and height are
     * changed to match the matrix that has been provided.  If more memory is needed then a new data array is
     * declared.<br>
     * <br>
     * a.numRows = b.numRows<br>
     * a.numCols = b.numCols<br>
     * <br>
     * a<sub>ij</sub> = b<sub>ij</sub><br>
     * <br>
     *
     * <p>
     * @param b The matrix that this matrix is to be set equal to.
     */
    public void setReshape( DenseMatrix64F b)
    {
        int dataLength = b.getNumElements();

        if( data.length < dataLength ) {
            data = new double[ dataLength ];
        }

        this.numRows = b.numRows;
        this.numCols = b.numCols;

        System.arraycopy(b.data, 0, this.data, 0, dataLength);
    }

    /**
     * Sets the value of this matrix to be the same as the value of the provided matrix.  Both
     * matrices must have the same shape:<br>
     * <br>
     * a<sub>ij</sub> = b<sub>ij</sub><br>
     * <br>
     *
     * @param b The matrix that this matrix is to be set equal to.
     */
    public void set( DenseMatrix64F b )
    {
        if( numRows != b.numRows || numCols != b.numCols ) {
            throw new MatrixDimensionException("The two matrices do not have compatible shapes.");
        }

        int dataLength = b.getNumElements();

        System.arraycopy(b.data, 0, this.data, 0, dataLength);
    }

    /**
     * Sets this matrix equal to the matrix encoded in the array.
     *
     * @param numRows The number of rows.
	 * @param numCols The number of columns.
	 * @param rowMajor If the array is encoded in a row-major or a column-major format.
	 * @param data The formatted 1D array. Not modified.
	 */
    public void set(int numRows, int numCols, boolean rowMajor, double ...data)
    {
        int length = numRows*numCols;

        if( numRows != this.numRows || numCols != this.numCols)
            throw new IllegalArgumentException("Unexpected matrix shape.");
        if( length > this.data.length )
            throw new IllegalArgumentException("The length of this matrix's data array is too small.");

        if( rowMajor ) {
            System.arraycopy(data,0,this.data,0,length);
        } else {
            int index = 0;
            for( int i = 0; i < numRows; i++ ) {
                for( int j = 0; j < numCols; j++ ) {
                    this.data[index++] = data[j*numRows+i];
                }
            }
        }
    }

    /**
     * Sets all elements equal to zero.
     */
    public void zero() {
        CommonOps.set(this,0.0);
    }

    /**
     * Creates and returns a matrix which is idential to this one.
     *
     * @return A new identical matrix.
     */
    public DenseMatrix64F copy() {
        return new DenseMatrix64F(this);
    }

    /**
     * Prints the value of this matrix to the screen.  For more options see
     * {@link UtilEjml}
     *
     */
    @Override
    public void print() {
        MatrixIO.print(this);
    }

    /**
     * Prints the value of this matrix to the screen.  For more options see
     * {@link UtilEjml}
     *
     * @param format The format which each element is printed uses.  See printf.
     */
    public void print( String format ) {
        MatrixIO.print(this,format);
    }
}
