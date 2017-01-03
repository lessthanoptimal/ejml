/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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

package org.ejml.simple;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.mult.VectorVectorMult_D32;
import org.ejml.alg.dense.mult.VectorVectorMult_D64;
import org.ejml.data.*;
import org.ejml.ops.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;


/**
 * Parent of {@link SimpleMatrix} implements all the standard matrix operations and uses
 * generics to allow the returned matrix type to be changed.  This class should be extended
 * instead of SimpleMatrix.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public abstract class SimpleBase <T extends SimpleBase> implements Serializable {

    /**
     * Internal matrix which this is a wrapper around.
     */
    protected Matrix mat;

    public SimpleBase( int numRows , int numCols ) {
        mat = new DenseMatrix64F(numRows, numCols);
    }

    protected SimpleBase() {
    }

    /**
     * Used internally for creating new instances of SimpleMatrix.  If SimpleMatrix is extended
     * by another class this function should be overridden so that the returned matrices are
     * of the correct type.
     *
     * @param numRows number of rows in the new matrix.
     * @param numCols number of columns in the new matrix.
     * @return A new matrix.
     */
    protected abstract T createMatrix( int numRows , int numCols );

    /**
     * <p>
     * Returns a reference to the matrix that it uses internally.  This is useful
     * when an operation is needed that is not provided by this class.
     * </p>
     *
     * @return Reference to the internal DenseMatrix64F.
     */
    public <T extends Matrix>T getMatrix() {
        return (T)mat;
    }

    public DenseMatrix64F matrix_F64() {
        return (DenseMatrix64F)mat;
    }

    public DenseMatrix32F matrix_F32() {
        return (DenseMatrix32F)mat;
    }


    /**
     * <p>
     * Returns the transpose of this matrix.<br>
     * a<sup>T</sup>
     * </p>
     *
     * @see CommonOps_D64#transpose(DenseMatrix64F,DenseMatrix64F)
     *
     * @return A matrix that is n by m.
     */
    public T transpose() {
        T ret = createMatrix(mat.getNumCols(),mat.getNumRows());

        if( bits() == 64 )
            CommonOps_D64.transpose((DenseMatrix64F)mat,(DenseMatrix64F)ret.getMatrix());
        else
            CommonOps_D32.transpose((DenseMatrix32F)mat,(DenseMatrix32F)ret.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Returns a matrix which is the result of matrix multiplication:<br>
     * <br>
     * c = a * b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
     * </p>
     *
     * @see CommonOps_D64#mult(org.ejml.data.RowD1Matrix64F , org.ejml.data.RowD1Matrix64F , org.ejml.data.RowD1Matrix64F)
     *
     * @param b A matrix that is n by bn. Not modified.
     *
     * @return The results of this operation.
     */
    public T mult( T b ) {
        T ret = createMatrix(mat.getNumRows(),b.getMatrix().getNumCols());

        if( bits() == 64 )
            CommonOps_D64.mult((DenseMatrix64F)mat,(DenseMatrix64F)b.getMatrix(),(DenseMatrix64F)ret.getMatrix());
        else
            CommonOps_D32.mult((DenseMatrix32F)mat,(DenseMatrix32F)b.getMatrix(),(DenseMatrix32F)ret.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Computes the Kronecker product between this matrix and the provided B matrix:<br>
     * <br>
     * C = kron(A,B)
     * </p>

     * @see CommonOps_D64#kron(DenseMatrix64F, DenseMatrix64F, DenseMatrix64F)
     *
     * @param B The right matrix in the operation. Not modified.
     * @return Kronecker product between this matrix and B.
     */
    public T kron( T B ) {
        T ret = createMatrix(mat.getNumRows()*B.numRows(),mat.getNumCols()*B.numCols());

        if( bits() == 64 )
            CommonOps_D64.kron((DenseMatrix64F)mat,(DenseMatrix64F)B.getMatrix(),(DenseMatrix64F)ret.getMatrix());
        else
            CommonOps_D32.kron((DenseMatrix32F)mat,(DenseMatrix32F)B.getMatrix(),(DenseMatrix32F)ret.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Returns the result of matrix addition:<br>
     * <br>
     * c = a + b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
     * </p>
     *
     * @see CommonOps_D64#mult(org.ejml.data.RowD1Matrix64F , org.ejml.data.RowD1Matrix64F , org.ejml.data.RowD1Matrix64F)
     *
     * @param b m by n matrix. Not modified.
     *
     * @return The results of this operation.
     */
    public T plus( T b ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_D64.addEquals((DenseMatrix64F)ret.getMatrix(),(DenseMatrix64F)b.getMatrix());
        else
            CommonOps_D32.addEquals((DenseMatrix32F)ret.getMatrix(),(DenseMatrix32F)b.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Returns the result of matrix subtraction:<br>
     * <br>
     * c = a - b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
     * </p>
     *
     * @see CommonOps_D64#subtract(org.ejml.data.D1Matrix64F , org.ejml.data.D1Matrix64F , org.ejml.data.D1Matrix64F)
     *
     * @param b m by n matrix. Not modified.
     *
     * @return The results of this operation.
     */
    public T minus( T b ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_D64.subtract((DenseMatrix64F)getMatrix(), (DenseMatrix64F)b.getMatrix(), (DenseMatrix64F)ret.getMatrix());
        else
            CommonOps_D32.subtract((DenseMatrix32F)getMatrix(), (DenseMatrix32F)b.getMatrix(), (DenseMatrix32F)ret.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Returns the result of matrix-double subtraction:<br>
     * <br>
     * c = a - b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in double.
     * </p>
     *
     * @see CommonOps_D64#subtract(org.ejml.data.D1Matrix64F , double , org.ejml.data.D1Matrix64F)
     *
     * @param b Value subtracted from each element
     *
     * @return The results of this operation.
     */
    public T minus( double b ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_D64.subtract((DenseMatrix64F)getMatrix(), b, (DenseMatrix64F)ret.getMatrix());
        else
            CommonOps_D32.subtract((DenseMatrix32F)getMatrix(), (float)b, (DenseMatrix32F)ret.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Returns the result of scalar addition:<br>
     * <br>
     * c = a + b<br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in double.
     * </p>
     *
     * @see CommonOps_D64#add( org.ejml.data.D1Matrix64F , double , org.ejml.data.D1Matrix64F)
     *
     * @param b Value added to each element
     *
     * @return A matrix that contains the results.
     */
    public T plus( double b ) {
        T ret = createMatrix(numRows(),numCols());

        if( bits() == 64 )
            CommonOps_D64.add((DenseMatrix64F)getMatrix(), b, (DenseMatrix64F)ret.getMatrix());
        else
            CommonOps_D32.add((DenseMatrix32F)getMatrix(), (float)b, (DenseMatrix32F)ret.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Performs a matrix addition and scale operation.<br>
     * <br>
     * c = a + &beta;*b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
     * </p>
     *
     * @see CommonOps_D64#add( org.ejml.data.D1Matrix64F , double , org.ejml.data.D1Matrix64F , org.ejml.data.D1Matrix64F)
     *
     * @param b m by n matrix. Not modified.
     *
     * @return A matrix that contains the results.
     */
    public T plus( double beta , T b ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_D64.addEquals((DenseMatrix64F)ret.getMatrix(),beta,(DenseMatrix64F)b.getMatrix());
        else
            CommonOps_D32.addEquals((DenseMatrix32F)ret.getMatrix(),(float)beta,(DenseMatrix32F)b.getMatrix());

        return ret;
    }

    /**
     * Computes the dot product (a.k.a. inner product) between this vector and vector 'v'.
     *
     * @param v The second vector in the dot product.  Not modified.
     * @return dot product
     */
    public double dot( T v ) {
        if( !isVector() ) {
            throw new IllegalArgumentException("'this' matrix is not a vector.");
        } else if( !v.isVector() ) {
            throw new IllegalArgumentException("'v' matrix is not a vector.");
        }

        if( bits() == 64 )
            return VectorVectorMult_D64.innerProd((DenseMatrix64F)mat,(DenseMatrix64F)v.getMatrix());
        else
            return VectorVectorMult_D32.innerProd((DenseMatrix32F)mat,(DenseMatrix32F)v.getMatrix());
    }

    /**
     * Returns true if this matrix is a vector.  A vector is defined as a matrix
     * that has either one row or column.
     *
     * @return Returns true for vectors and false otherwise.
     */
    public boolean isVector() {
        return mat.getNumRows() == 1 || mat.getNumCols() == 1;
    }

    /**
     * <p>
     * Returns the result of scaling each element by 'val':<br>
     * b<sub>i,j</sub> = val*a<sub>i,j</sub>
     * </p>
     *
     * @see CommonOps_D64#scale(double, org.ejml.data.D1Matrix64F)
     *
     * @param val The multiplication factor.
     * @return The scaled matrix.
     */
    public T scale( double val ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_D64.scale(val,(DenseMatrix64F)ret.getMatrix());
        else
            CommonOps_D32.scale((float)val,(DenseMatrix32F)ret.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Returns the result of dividing each element by 'val':
     * b<sub>i,j</sub> = a<sub>i,j</sub>/val
     * </p>
     *
     * @see CommonOps_D64#divide(org.ejml.data.D1Matrix64F,double)
     *
     * @param val Divisor.
     * @return Matrix with its elements divided by the specified value.
     */
    public T divide( double val ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_D64.divide((DenseMatrix64F)ret.getMatrix(),val);
        else
            CommonOps_D32.divide((DenseMatrix32F)ret.getMatrix(),(float)val);

        return ret;
    }

    /**
     * <p>
     * Returns the inverse of this matrix.<br>
     * <br>
     * b = a<sup>-1<sup><br>
     * </p>
     *
     * <p>
     * If the matrix could not be inverted then SingularMatrixException is thrown.  Even
     * if no exception is thrown the matrix could still be singular or nearly singular.
     * </p>
     *
     * @see CommonOps_D64#invert(DenseMatrix64F, DenseMatrix64F)
     *
     * @throws SingularMatrixException
     *
     * @return The inverse of this matrix.
     */
    public T invert() {
        T ret = createMatrix(mat.getNumRows(), mat.getNumCols());
        if (bits() == 64) {
            if (!CommonOps_D64.invert((DenseMatrix64F)mat, (DenseMatrix64F)ret.getMatrix())) {
                throw new SingularMatrixException();
            }
            if (MatrixFeatures_D64.hasUncountable((DenseMatrix64F)ret.getMatrix()))
                throw new SingularMatrixException("Solution has uncountable numbers");
        } else {
            if (!CommonOps_D32.invert((DenseMatrix32F)mat, (DenseMatrix32F)ret.getMatrix())) {
                throw new SingularMatrixException();
            }
            if (MatrixFeatures_D32.hasUncountable((DenseMatrix32F)ret.getMatrix()))
                throw new SingularMatrixException("Solution has uncountable numbers");
        }
        return ret;
    }

    /**
     * <p>
     * Computes the Moore-Penrose pseudo-inverse
     * </p>
     *
     * @return inverse computed using the pseudo inverse.
     */
    public T pseudoInverse() {
        T ret = createMatrix(mat.getNumCols(),mat.getNumRows());
        if (bits() == 64) {
            CommonOps_D64.pinv((DenseMatrix64F)mat, (DenseMatrix64F)ret.getMatrix());
        } else {
            CommonOps_D32.pinv((DenseMatrix32F)mat, (DenseMatrix32F)ret.getMatrix());
        }
        return ret;
    }

    /**
     * <p>
     * Solves for X in the following equation:<br>
     * <br>
     * x = a<sup>-1</sup>b<br>
     * <br>
     * where 'a' is this matrix and 'b' is an n by p matrix.
     * </p>
     *
     * <p>
     * If the system could not be solved then SingularMatrixException is thrown.  Even
     * if no exception is thrown 'a' could still be singular or nearly singular.
     * </p>
     *
     * @see CommonOps_D64#solve(DenseMatrix64F, DenseMatrix64F, DenseMatrix64F)
     *
     * @throws SingularMatrixException
     *
     * @param b n by p matrix. Not modified.
     * @return The solution for 'x' that is n by p.
     */
    public T solve( T b )
    {
        T x = createMatrix(mat.getNumCols(),b.getMatrix().getNumCols());

        if (bits() == 64) {
            if (!CommonOps_D64.solve((DenseMatrix64F)mat, (DenseMatrix64F)b.getMatrix(), (DenseMatrix64F)x.getMatrix()))
                throw new SingularMatrixException();

            if (MatrixFeatures_D64.hasUncountable((DenseMatrix64F)x.getMatrix()))
                throw new SingularMatrixException("Solution contains uncountable numbers");
        } else {
            if (!CommonOps_D32.solve((DenseMatrix32F)mat, (DenseMatrix32F)b.getMatrix(), (DenseMatrix32F)x.getMatrix()))
                throw new SingularMatrixException();

            if (MatrixFeatures_D32.hasUncountable((DenseMatrix32F)x.getMatrix()))
                throw new SingularMatrixException("Solution contains uncountable numbers");
        }

        return x;
    }


    /**
     * Sets the elements in this matrix to be equal to the elements in the passed in matrix.
     * Both matrix must have the same dimension.
     *
     * @param a The matrix whose value this matrix is being set to.
     */
    public void set( T a ) {
        mat.set(a.getMatrix());
    }


    /**
     * <p>
     * Sets all the elements in this matrix equal to the specified value.<br>
     * <br>
     * a<sub>ij</sub> = val<br>
     * </p>
     *
     * @see CommonOps_D64#fill(org.ejml.data.D1Matrix64F , double)
     *
     * @param val The value each element is set to.
     */
    public void set( double val ) {
        if (bits() == 64) {
            CommonOps_D64.fill((DenseMatrix64F)mat, val);
        } else {
            CommonOps_D32.fill((DenseMatrix32F)mat, (float)val);
        }
    }

    /**
     * Sets all the elements in the matrix equal to zero.
     *
     * @see CommonOps_D64#fill(org.ejml.data.D1Matrix64F , double)
     */
    public void zero() {
        if (bits() == 64) {
            ((DenseMatrix64F)mat).zero();
        } else {
            ((DenseMatrix32F)mat).zero();
        }
    }

    /**
     * <p>
     * Computes the Frobenius normal of the matrix:<br>
     * <br>
     * normF = Sqrt{  &sum;<sub>i=1:m</sub> &sum;<sub>j=1:n</sub> { a<sub>ij</sub><sup>2</sup>}   }
     * </p>
     *
     * @see NormOps_D64#normF(org.ejml.data.D1Matrix64F)
     *
     * @return The matrix's Frobenius normal.
     */
    public double normF() {
        if (bits() == 64) {
            return NormOps_D64.normF((DenseMatrix64F)mat);
        } else {
            return NormOps_D32.normF((DenseMatrix32F)mat);
        }
    }

    /**
     * <p>
     * The condition p = 2 number of a matrix is used to measure the sensitivity of the linear
     * system <b>Ax=b</b>.  A value near one indicates that it is a well conditioned matrix.
     * </p>
     *
     * @see NormOps_D64#conditionP2(DenseMatrix64F)
     *
     * @return The condition number.
     */
    public double conditionP2() {
        if (bits() == 64) {
            return NormOps_D64.conditionP2((DenseMatrix64F)mat);
        } else {
            return NormOps_D32.conditionP2((DenseMatrix32F)mat);
        }
    }

    /**
     * Computes the determinant of the matrix.
     *
     * @see CommonOps_D64#det(DenseMatrix64F)
     *
     * @return The determinant.
     */
    public double determinant() {
        if (bits() == 64) {
            double ret = CommonOps_D64.det((DenseMatrix64F)mat);
            // if the decomposition silently failed then the matrix is most likely singular
            if (UtilEjml.isUncountable(ret))
                return 0;
            return ret;
        } else {
            double ret = CommonOps_D32.det((DenseMatrix32F)mat);
            // if the decomposition silently failed then the matrix is most likely singular
            if (UtilEjml.isUncountable(ret))
                return 0;
            return ret;
        }
    }

    /**
     * <p>
     * Computes the trace of the matrix.
     * </p>
     *
     * @see CommonOps_D64#trace(org.ejml.data.RowD1Matrix64F)
     *
     * @return The trace of the matrix.
     */
    public double trace() {
        if (bits() == 64) {
            return CommonOps_D64.trace((DenseMatrix64F)mat);
        } else {
            return CommonOps_D32.trace((DenseMatrix32F)mat);
        }
    }

    /**
     * <p>
     * Reshapes the matrix to the specified number of rows and columns.  If the total number of elements
     * is <= number of elements it had before the data is saved.  Otherwise a new internal array is
     * declared and the old data lost.
     * </p>
     *
     * <p>
     * This is equivalent to calling A.getMatrix().reshape(numRows,numCols,false).
     * </p>
     *
     * @see org.ejml.data.DenseMatrix64F#reshape(int,int,boolean)
     *
     * @param numRows The new number of rows in the matrix.
     * @param numCols The new number of columns in the matrix.
     */
    public void reshape( int numRows , int numCols ) {
        if (bits() == 64) {
            ((DenseMatrix64F)mat).reshape(numRows, numCols, false);
        } else {
            ((DenseMatrix32F)mat).reshape(numRows, numCols, false);
        }
    }

    /**
     * Assigns the element in the Matrix to the specified value.  Performs a bounds check to make sure
     * the requested element is part of the matrix.
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @param value The element's new value.
     */
    public void set( int row , int col , double value ) {
        if (bits() == 64) {
            ((DenseMatrix64F)mat).set(row, col, value);
        } else {
            ((DenseMatrix32F)mat).set(row, col, (float)value);
        }
    }

    /**
     * Assigns an element a value based on its index in the internal array..
     *
     * @param index The matrix element that is being assigned a value.
     * @param value The element's new value.
     */
    public void set( int index , double value ) {
        if (bits() == 64) {
            ((DenseMatrix64F)mat).set(index, value);
        } else {
            ((DenseMatrix32F)mat).set(index, (float)value);
        }
    }

    /**
     * <p>
     * Assigns consecutive elements inside a row to the provided array.<br>
     * <br>
     * A(row,offset:(offset + values.length)) = values
     * </p>
     *
     * @param row The row that the array is to be written to.
     * @param offset The initial column that the array is written to.
     * @param values Values which are to be written to the row in a matrix.
     */
    public void setRow( int row , int offset , double ...values ) {
        if (bits() == 64) {
            DenseMatrix64F m = (DenseMatrix64F)mat;
            for (int i = 0; i < values.length; i++) {
                m.set(row, offset + i, values[i]);
            }
        } else {
            DenseMatrix32F m = (DenseMatrix32F)mat;
            for (int i = 0; i < values.length; i++) {
                m.set(row, offset + i, (float)values[i]);
            }
        }
    }

    /**
     * <p>
     * Assigns consecutive elements inside a column to the provided array.<br>
     * <br>
     * A(offset:(offset + values.length),column) = values
     * </p>
     *
     * @param column The column that the array is to be written to.
     * @param offset The initial column that the array is written to.
     * @param values Values which are to be written to the row in a matrix.
     */
    public void setColumn( int column , int offset , double ...values ) {
        if (bits() == 64) {
            DenseMatrix64F m = (DenseMatrix64F)mat;
            for (int i = 0; i < values.length; i++) {
                m.set(offset + i, column, values[i]);
            }
        } else {
            DenseMatrix32F m = (DenseMatrix32F)mat;
            for (int i = 0; i < values.length; i++) {
                m.set(offset + i, column, (float)values[i]);
            }
        }
    }

    /**
     * Returns the value of the specified matrix element.  Performs a bounds check to make sure
     * the requested element is part of the matrix.
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @return The value of the element.
     */
    public double get( int row , int col ) {
        if (bits() == 64) {
            return ((DenseMatrix64F)mat).get(row, col);
        } else {
            return ((DenseMatrix32F)mat).get(row, col);
        }
    }

    /**
     * Returns the value of the matrix at the specified index of the 1D row major array.
     *
     * @see org.ejml.data.DenseMatrix64F#get(int)
     *
     * @param index The element's index whose value is to be returned
     * @return The value of the specified element.
     */
    public double get( int index ) {
        if (bits() == 64) {
            return ((DenseMatrix64F)mat).data[index];
        } else {
            return ((DenseMatrix32F)mat).data[index];
        }
    }

    /**
     * Returns the index in the matrix's array.
     *
     * @see org.ejml.data.DenseMatrix64F#getIndex(int, int)
     *
     * @param row The row number.
     * @param col The column number.
     * @return The index of the specified element.
     */
    public int getIndex( int row , int col ) {
        return row * mat.getNumCols() + col;
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
    public MatrixIterator64F iterator(boolean rowMajor, int minRow, int minCol, int maxRow, int maxCol)
    {
        return new MatrixIterator64F((DenseMatrix64F)mat,rowMajor, minRow, minCol, maxRow, maxCol);
    }

    /**
     * Creates and returns a matrix which is idential to this one.
     *
     * @return A new identical matrix.
     */
    public T copy() {
        T ret = createMatrix(mat.getNumRows(),mat.getNumCols());
        ret.getMatrix().set(this.getMatrix());
        return ret;
    }

    /**
     * Returns the number of rows in this matrix.
     *
     * @return number of rows.
     */
    public int numRows() {
        return mat.getNumRows();
    }

    /**
     * Returns the number of columns in this matrix.
     *
     * @return number of columns.
     */
    public int numCols() {
        return mat.getNumCols();
    }

    /**
     * Returns the number of elements in this matrix, which is equal to
     * the number of rows times the number of columns.
     *
     * @return The number of elements in the matrix.
     */
    public int getNumElements() {
        if( bits() == 64 )
            return ((DenseMatrix64F)mat).getNumElements();
        else
            return ((DenseMatrix32F)mat).getNumElements();
    }


    /**
     * Prints the matrix to standard out.
     */
    public void print() {
        if( bits() == 64 ) {
            MatrixIO.print(System.out, (DenseMatrix64F)mat);
        } else {
            MatrixIO.print(System.out, (DenseMatrix32F) mat);
        }
    }

    /**
     * Prints the matrix to standard out with the specified precision.
     */
    public void print(int numChar , int precision) {
        if( bits() == 64 ) {
            MatrixIO.print(System.out, (DenseMatrix64F)mat, numChar, precision);
        } else {
            MatrixIO.print(System.out, (DenseMatrix32F)mat, numChar, precision);
        }
    }

    /**
     * <p>
     * Prints the matrix to standard out given a {@link java.io.PrintStream#printf) style floating point format,
     * e.g. print("%f").
     * </p>
     */
    public void print( String format ) {
        if( bits() == 64 ) {
            MatrixIO.print(System.out, (DenseMatrix64F)mat, format);
        } else {
            MatrixIO.print(System.out, (DenseMatrix32F)mat, format);
        }
    }

    /**
     * <p>
     * Converts the array into a string format for display purposes.
     * The conversion is done using {@link MatrixIO#print(java.io.PrintStream, org.ejml.data.RealMatrix64F)}.
     * </p>
     *
     * @return String representation of the matrix.
     */
    public String toString() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if( bits() == 64 ) {
            MatrixIO.print(new PrintStream(stream), (DenseMatrix64F)mat);
        } else {
            MatrixIO.print(new PrintStream(stream), (DenseMatrix32F)mat);
        }

        return stream.toString();
    }

    /**
     * <p>
     * Creates a new SimpleMatrix which is a submatrix of this matrix.
     * </p>
     * <p>
     * s<sub>i-y0 , j-x0</sub> = o<sub>ij</sub> for all y0 &le; i < y1 and x0 &le; j < x1<br>
     * <br>
     * where 's<sub>ij</sub>' is an element in the submatrix and 'o<sub>ij</sub>' is an element in the
     * original matrix.
     * </p>
     *
     * <p>
     * If any of the inputs are set to T.END then it will be set to the last row
     * or column in the matrix.
     * </p>
     *
     * @param y0 Start row.
     * @param y1 Stop row + 1.
     * @param x0 Start column.
     * @param x1 Stop column + 1.
     * @return The submatrix.
     */
    public T extractMatrix(int y0 , int y1, int x0 , int x1 ) {
        if( y0 == SimpleMatrix.END ) y0 = mat.getNumRows();
        if( y1 == SimpleMatrix.END ) y1 = mat.getNumRows();
        if( x0 == SimpleMatrix.END ) x0 = mat.getNumCols();
        if( x1 == SimpleMatrix.END ) x1 = mat.getNumCols();

        T ret = createMatrix(y1-y0,x1-x0);

        if( bits() == 64 ) {
            CommonOps_D64.extract((DenseMatrix64F)mat, y0, y1, x0, x1, (DenseMatrix64F)ret.getMatrix(), 0, 0);
        } else {
            CommonOps_D32.extract((DenseMatrix32F)mat, y0, y1, x0, x1, (DenseMatrix32F)ret.getMatrix(), 0, 0);
        }

        return ret;
    }

    /**
     * <p>
     * Extracts a row or column from this matrix. The returned vector will either be a row
     * or column vector depending on the input type.
     * </p>
     *
     * @param extractRow If true a row will be extracted.
     * @param element The row or column the vector is contained in.
     * @return Extracted vector.
     */
    public T extractVector( boolean extractRow , int element )
    {
        int length = extractRow ? mat.getNumCols() : mat.getNumRows();

        T ret = extractRow ? createMatrix(1,length) : createMatrix(length,1);

        if( bits() == 64 ) {
            if (extractRow) {
                SpecializedOps_D64.subvector((DenseMatrix64F)mat, element, 0, length, true, 0, (DenseMatrix64F)ret.getMatrix());
            } else {
                SpecializedOps_D64.subvector((DenseMatrix64F)mat, 0, element, length, false, 0, (DenseMatrix64F)ret.getMatrix());
            }
        } else {
            if (extractRow) {
                SpecializedOps_D32.subvector((DenseMatrix32F)mat, element, 0, length, true, 0, (DenseMatrix32F)ret.getMatrix());
            } else {
                SpecializedOps_D32.subvector((DenseMatrix32F)mat, 0, element, length, false, 0, (DenseMatrix32F)ret.getMatrix());
            }
        }

        return ret;
    }

    /**
     * <p>
     * Extracts the diagonal from this matrix and returns them inside a column vector.
     * </p>
     *
     * @see CommonOps_D64#extractDiag(DenseMatrix64F, DenseMatrix64F)
     * @return Diagonal elements inside a column vector.
     */
    public T extractDiag()
    {
        int N = Math.min(mat.getNumCols(),mat.getNumRows());

        T diag = createMatrix(N,1);

        if( bits() == 64 ) {
            CommonOps_D64.extractDiag((DenseMatrix64F)mat, (DenseMatrix64F)diag.getMatrix());
        } else {
            CommonOps_D32.extractDiag((DenseMatrix32F)mat, (DenseMatrix32F)diag.getMatrix());
        }

        return diag;
    }

    /**
     * Checks to see if matrix 'a' is the same as this matrix within the specified
     * tolerance.
     *
     * @param a The matrix it is being compared against.
     * @param tol How similar they must be to be equals.
     * @return If they are equal within tolerance of each other.
     */
    public boolean isIdentical(T a, double tol) {
        if( bits() == 64 ) {
            return MatrixFeatures_D64.isIdentical((DenseMatrix64F)mat, (DenseMatrix64F)a.getMatrix(), tol);
        } else {
            return MatrixFeatures_D32.isIdentical((DenseMatrix32F)mat, (DenseMatrix32F)a.getMatrix(), (float)tol);
        }
    }

    /**
     * Checks to see if any of the elements in this matrix are either NaN or infinite.
     *
     * @return True of an element is NaN or infinite.  False otherwise.
     */
    public boolean hasUncountable() {
        if( bits() == 64 ) {
            return MatrixFeatures_D64.hasUncountable((DenseMatrix64F)mat);
        } else {
            return MatrixFeatures_D32.hasUncountable((DenseMatrix32F)mat);
        }
    }

    /**
     * Computes a full Singular Value Decomposition (SVD) of this matrix with the
     * eigenvalues ordered from largest to smallest.
     *
     * @return SVD
     */
    public SimpleSVD<T> svd() {
        return new SimpleSVD(mat,false);
    }

    /**
     * Computes the SVD in either  compact format or full format.
     *
     * @return SVD of this matrix.
     */
    public SimpleSVD<T> svd( boolean compact ) {
        return new SimpleSVD(mat,compact);
    }

    /**
     * Returns the Eigen Value Decomposition (EVD) of this matrix.
     */
    public SimpleEVD<T> eig() {
        return new SimpleEVD(mat);
    }

    /**
     * Copy matrix B into this matrix at location (insertRow, insertCol).
     *
     * @param insertRow First row the matrix is to be inserted into.
     * @param insertCol First column the matrix is to be inserted into.
     * @param B The matrix that is being inserted.
     */
    public void insertIntoThis(int insertRow, int insertCol, T B) {
        if( bits() == 64 ) {
            CommonOps_D64.insert((DenseMatrix64F)B.getMatrix(), (DenseMatrix64F)mat, insertRow, insertCol);
        } else {
            CommonOps_D32.insert((DenseMatrix32F)B.getMatrix(),(DenseMatrix32F) mat, insertRow, insertCol);
        }
    }

    /**
     * <p>
     * Creates a new matrix that is a combination of this matrix and matrix B.  B is
     * written into A at the specified location if needed the size of A is increased by
     * growing it.  A is grown by padding the new area with zeros.
     * </p>
     *
     * <p>
     * While useful when adding data to a matrix which will be solved for it is also much
     * less efficient than predeclaring a matrix and inserting data into it.
     * </p>
     *
     * <p>
     * If insertRow or insertCol is set to SimpleMatrix.END then it will be combined
     * at the last row or column respectively.
     * <p>
     *
     * @param insertRow Row where matrix B is written in to.
     * @param insertCol Column where matrix B is written in to.
     * @param B The matrix that is written into A.
     * @return A new combined matrix.
     */
    public T combine( int insertRow, int insertCol, T B) {

        if( insertRow == SimpleMatrix.END ) {
            insertRow = mat.getNumRows();
        }

        if( insertCol == SimpleMatrix.END ) {
            insertCol = mat.getNumCols();
        }

        int maxRow = insertRow + B.numRows();
        int maxCol = insertCol + B.numCols();

        T ret;

        if( maxRow > mat.getNumRows() || maxCol > mat.getNumCols()) {
            int M = Math.max(maxRow,mat.getNumRows());
            int N = Math.max(maxCol,mat.getNumCols());

            ret = createMatrix(M,N);
            ret.insertIntoThis(0,0,this);
        } else {
            ret = copy();
        }

        ret.insertIntoThis(insertRow,insertCol,B);

        return ret;
    }

    /**
     * Returns the maximum absolute value of all the elements in this matrix.  This is
     * equivalent the the infinite p-norm of the matrix.
     *
     * @return Largest absolute value of any element.
     */
    public double elementMaxAbs() {
        if( bits() == 64 ) {
            return CommonOps_D64.elementMaxAbs((DenseMatrix64F)mat);
        } else {
            return CommonOps_D32.elementMaxAbs((DenseMatrix32F)mat);
        }
    }

    /**
     * Computes the sum of all the elements in the matrix.
     *
     * @return Sum of all the elements.
     */
    public double elementSum() {
        if( bits() == 64 ) {
            return CommonOps_D64.elementSum((DenseMatrix64F)mat);
        } else {
            return CommonOps_D32.elementSum((DenseMatrix32F)mat);
        }
    }

    /**
     * <p>
     * Returns a matrix which is the result of an element by element multiplication of 'this' and 'b':
     * c<sub>i,j</sub> = a<sub>i,j</sub>*b<sub>i,j</sub>
     * </p>
     *
     * @param b A simple matrix.
     * @return The element by element multiplication of 'this' and 'b'.
     */
    public T elementMult( T b )
    {
        T c = createMatrix(mat.getNumRows(),mat.getNumCols());

        if( bits() == 64 ) {
            CommonOps_D64.elementMult((DenseMatrix64F)mat, (DenseMatrix64F)b.getMatrix(), (DenseMatrix64F)c.getMatrix());
        } else {
            CommonOps_D32.elementMult((DenseMatrix32F)mat, (DenseMatrix32F)b.getMatrix(), (DenseMatrix32F)c.getMatrix());
        }

        return c;
    }

    /**
     * <p>
     * Returns a matrix which is the result of an element by element division of 'this' and 'b':
     * c<sub>i,j</sub> = a<sub>i,j</sub>/b<sub>i,j</sub>
     * </p>
     *
     * @param b A simple matrix.
     * @return The element by element division of 'this' and 'b'.
     */
    public T elementDiv( T b )
    {
        T c = createMatrix(mat.getNumRows(),mat.getNumCols());

        if( bits() == 64 ) {
            CommonOps_D64.elementDiv((DenseMatrix64F)mat, (DenseMatrix64F)b.getMatrix(), (DenseMatrix64F)c.getMatrix());
        } else {
            CommonOps_D32.elementDiv((DenseMatrix32F)mat, (DenseMatrix32F)b.getMatrix(), (DenseMatrix32F)c.getMatrix());
        }

        return c;
    }

    /**
     * <p>
     * Returns a matrix which is the result of an element by element power of 'this' and 'b':
     * c<sub>i,j</sub> = a<sub>i,j</sub> ^ b<sub>i,j</sub>
     * </p>
     *
     * @param b A simple matrix.
     * @return The element by element power of 'this' and 'b'.
     */
    public T elementPower( T b )
    {
        T c = createMatrix(mat.getNumRows(),mat.getNumCols());

        if( bits() == 64 ) {
            CommonOps_D64.elementPower((DenseMatrix64F)mat, (DenseMatrix64F)b.getMatrix(), (DenseMatrix64F)c.getMatrix());
        } else {
            CommonOps_D32.elementPower((DenseMatrix32F)mat, (DenseMatrix32F)b.getMatrix(), (DenseMatrix32F)c.getMatrix());
        }

        return c;
    }

    /**
     * <p>
     * Returns a matrix which is the result of an element by element power of 'this' and 'b':
     * c<sub>i,j</sub> = a<sub>i,j</sub> ^ b
     * </p>
     *
     * @param b Scalar
     * @return The element by element power of 'this' and 'b'.
     */
    public T elementPower( double b )
    {
        T c = createMatrix(mat.getNumRows(),mat.getNumCols());

        if( bits() == 64 ) {
            CommonOps_D64.elementPower((DenseMatrix64F)mat, b, (DenseMatrix64F)c.getMatrix());
        } else {
            CommonOps_D32.elementPower((DenseMatrix32F)mat, (float)b, (DenseMatrix32F)c.getMatrix());
        }

        return c;
    }

    /**
     * <p>
     * Returns a matrix which is the result of an element by element exp of 'this'
     * c<sub>i,j</sub> = Math.exp(a<sub>i,j</sub>)
     * </p>
     *
     * @return The element by element power of 'this' and 'b'.
     */
    public T elementExp()
    {
        T c = createMatrix(mat.getNumRows(),mat.getNumCols());

        if( bits() == 64 ) {
            CommonOps_D64.elementExp((DenseMatrix64F)mat, (DenseMatrix64F)c.getMatrix());
        } else {
            CommonOps_D32.elementExp((DenseMatrix32F)mat, (DenseMatrix32F)c.getMatrix());
        }

        return c;
    }

    /**
     * <p>
     * Returns a matrix which is the result of an element by element exp of 'this'
     * c<sub>i,j</sub> = Math.log(a<sub>i,j</sub>)
     * </p>
     *
     * @return The element by element power of 'this' and 'b'.
     */
    public T elementLog()
    {
        T c = createMatrix(mat.getNumRows(),mat.getNumCols());

        if( bits() == 64 ) {
            CommonOps_D64.elementLog((DenseMatrix64F)mat, (DenseMatrix64F)c.getMatrix());
        } else {
            CommonOps_D32.elementLog((DenseMatrix32F)mat, (DenseMatrix32F)c.getMatrix());
        }

        return c;
    }

    /**
     * <p>
     * Returns a new matrix whose elements are the negative of 'this' matrix's elements.<br>
     * <br>
     * b<sub>ij</sub> = -a<sub>ij</sub>
     * </p>
     *
     * @return A matrix that is the negative of the original.
     */
    public T negative() {
        T A = copy();
        if( bits() == 64 ) {
            CommonOps_D64.changeSign((DenseMatrix64F)A.getMatrix());
        } else {
            CommonOps_D32.changeSign((DenseMatrix32F)A.getMatrix());
        }
        return A;
    }

    /**
     * <p>
     * Saves this matrix to a file as a serialized binary object.
     * </p>
     *
     * @see MatrixIO#saveBin( org.ejml.data.RealMatrix64F, String)
     *
     * @param fileName
     * @throws java.io.IOException
     */
    public void saveToFileBinary( String fileName )
        throws IOException
    {
        MatrixIO.saveBin((DenseMatrix64F)mat, fileName);
    }

    /**
     * <p>
     * Loads a new matrix from a serialized binary file.
     * </p>
     *
     * @see MatrixIO#loadBin(String)
     *
     * @param fileName File which is to be loaded.
     * @return The matrix.
     * @throws IOException
     */
    public static SimpleMatrix loadBinary( String fileName )
            throws IOException {
        RealMatrix64F mat = MatrixIO.loadBin(fileName);

        // see if its a DenseMatrix64F
        if( mat instanceof DenseMatrix64F ) {
            return SimpleMatrix.wrap((DenseMatrix64F)mat);
        } else {
            // if not convert it into one and wrap it
            return SimpleMatrix.wrap( new DenseMatrix64F(mat));
        }
    }

    /**
     * <p>
     * Saves this matrix to a file in a CSV format.  For the file format see {@link MatrixIO}.
     * </p>
     *
     * @see MatrixIO#saveBin( org.ejml.data.RealMatrix64F, String)
     *
     * @param fileName
     * @throws java.io.IOException
     */
    public void saveToFileCSV( String fileName )
            throws IOException
    {
        MatrixIO.saveCSV((DenseMatrix64F)mat, fileName);
    }

    /**
     * <p>
     * Loads a new matrix from a CSV file.  For the file format see {@link MatrixIO}.
     * </p>
     *
     * @see MatrixIO#loadCSV(String)
     *
     * @param fileName File which is to be loaded.
     * @return The matrix.
     * @throws IOException
     */
    public T loadCSV( String fileName )
            throws IOException {
        RealMatrix64F mat = MatrixIO.loadCSV(fileName);

        T ret = createMatrix(1,1);

        // see if its a DenseMatrix64F
        if( mat instanceof DenseMatrix64F ) {
            ret.mat = (DenseMatrix64F)mat;
        } else {
            // if not convert it into one and wrap it
            ret.mat = new DenseMatrix64F(mat);
        }
        return ret;
    }

    /**
     * Returns true of the specified matrix element is valid element inside this matrix.
     * 
     * @param row Row index.
     * @param col Column index.
     * @return true if it is a valid element in the matrix.
     */
    public boolean isInBounds(int row, int col) {
        return row >= 0 && col >= 0 && row < mat.getNumRows() && col < mat.getNumCols();
    }

    /**
     * Prints the number of rows and column in this matrix.
     */
    public void printDimensions() {
        System.out.println("[rows = "+numRows()+" , cols = "+numCols()+" ]");
    }

    /**
     * Size of internal array elements.  32 or 64 bits
     */
    public int bits() {
        if( mat.getClass() == DenseMatrix64F.class ) {
            return 64;
        } if( mat.getClass() == DenseMatrix32F.class ) {
            return 32;
        } else {
            throw new RuntimeException("Unknown matrix type");
        }
    }
}
