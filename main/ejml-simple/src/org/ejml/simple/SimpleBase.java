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

package org.ejml.simple;

import org.ejml.UtilEjml;
import org.ejml.data.*;
import org.ejml.dense.row.*;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_FDRM;
import org.ejml.equation.Equation;
import org.ejml.ops.MatrixIO;

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

    static final long serialVersionUID = 2342556642L;

    /**
     * Internal matrix which this is a wrapper around.
     */
    protected Matrix mat;

    public SimpleBase( int numRows , int numCols ) {
        mat = new DMatrixRMaj(numRows, numCols);
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

    protected abstract T wrapMatrix( Matrix m );

    /**
     * <p>
     * Returns a reference to the matrix that it uses internally.  This is useful
     * when an operation is needed that is not provided by this class.
     * </p>
     *
     * @return Reference to the internal DMatrixRMaj.
     */
    public <T extends Matrix>T getMatrix() {
        return (T)mat;
    }

    public DMatrixRMaj matrix_F64() {
        return (DMatrixRMaj)mat;
    }

    public FMatrixRMaj matrix_F32() {
        return (FMatrixRMaj)mat;
    }


    /**
     * <p>
     * Returns the transpose of this matrix.<br>
     * a<sup>T</sup>
     * </p>
     *
     * @see CommonOps_DDRM#transpose(DMatrixRMaj, DMatrixRMaj)
     *
     * @return A matrix that is n by m.
     */
    public T transpose() {
        T ret = createMatrix(mat.getNumCols(),mat.getNumRows());

        if( bits() == 64 )
            CommonOps_DDRM.transpose((DMatrixRMaj)mat,(DMatrixRMaj)ret.getMatrix());
        else
            CommonOps_FDRM.transpose((FMatrixRMaj)mat,(FMatrixRMaj)ret.getMatrix());

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
     * @see CommonOps_DDRM#mult(DMatrix1Row, DMatrix1Row, DMatrix1Row)
     *
     * @param b A matrix that is n by bn. Not modified.
     *
     * @return The results of this operation.
     */
    public T mult( T b ) {
        T ret = createMatrix(mat.getNumRows(),b.getMatrix().getNumCols());

        if( bits() == 64 )
            CommonOps_DDRM.mult((DMatrixRMaj)mat,(DMatrixRMaj)b.getMatrix(),(DMatrixRMaj)ret.getMatrix());
        else
            CommonOps_FDRM.mult((FMatrixRMaj)mat,(FMatrixRMaj)b.getMatrix(),(FMatrixRMaj)ret.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Computes the Kronecker product between this matrix and the provided B matrix:<br>
     * <br>
     * C = kron(A,B)
     * </p>

     * @see CommonOps_DDRM#kron(DMatrixRMaj, DMatrixRMaj, DMatrixRMaj)
     *
     * @param B The right matrix in the operation. Not modified.
     * @return Kronecker product between this matrix and B.
     */
    public T kron( T B ) {
        T ret = createMatrix(mat.getNumRows()*B.numRows(),mat.getNumCols()*B.numCols());

        if( bits() == 64 )
            CommonOps_DDRM.kron((DMatrixRMaj)mat,(DMatrixRMaj)B.getMatrix(),(DMatrixRMaj)ret.getMatrix());
        else
            CommonOps_FDRM.kron((FMatrixRMaj)mat,(FMatrixRMaj)B.getMatrix(),(FMatrixRMaj)ret.getMatrix());

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
     * @see CommonOps_DDRM#mult(DMatrix1Row, DMatrix1Row, DMatrix1Row)
     *
     * @param b m by n matrix. Not modified.
     *
     * @return The results of this operation.
     */
    public T plus( T b ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_DDRM.addEquals((DMatrixRMaj)ret.getMatrix(),(DMatrixRMaj)b.getMatrix());
        else
            CommonOps_FDRM.addEquals((FMatrixRMaj)ret.getMatrix(),(FMatrixRMaj)b.getMatrix());

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
     * @see CommonOps_DDRM#subtract(DMatrixD1, DMatrixD1, DMatrixD1)
     *
     * @param b m by n matrix. Not modified.
     *
     * @return The results of this operation.
     */
    public T minus( T b ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_DDRM.subtract((DMatrixRMaj)getMatrix(), (DMatrixRMaj)b.getMatrix(), (DMatrixRMaj)ret.getMatrix());
        else
            CommonOps_FDRM.subtract((FMatrixRMaj)getMatrix(), (FMatrixRMaj)b.getMatrix(), (FMatrixRMaj)ret.getMatrix());

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
     * @see CommonOps_DDRM#subtract(DMatrixD1, double , DMatrixD1)
     *
     * @param b Value subtracted from each element
     *
     * @return The results of this operation.
     */
    public T minus( double b ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_DDRM.subtract((DMatrixRMaj)getMatrix(), b, (DMatrixRMaj)ret.getMatrix());
        else
            CommonOps_FDRM.subtract((FMatrixRMaj)getMatrix(), (float)b, (FMatrixRMaj)ret.getMatrix());

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
     * @see CommonOps_DDRM#add( DMatrixD1, double , DMatrixD1)
     *
     * @param b Value added to each element
     *
     * @return A matrix that contains the results.
     */
    public T plus( double b ) {
        T ret = createMatrix(numRows(),numCols());

        if( bits() == 64 )
            CommonOps_DDRM.add((DMatrixRMaj)getMatrix(), b, (DMatrixRMaj)ret.getMatrix());
        else
            CommonOps_FDRM.add((FMatrixRMaj)getMatrix(), (float)b, (FMatrixRMaj)ret.getMatrix());

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
     * @see CommonOps_DDRM#add( DMatrixD1, double , DMatrixD1, DMatrixD1)
     *
     * @param b m by n matrix. Not modified.
     *
     * @return A matrix that contains the results.
     */
    public T plus( double beta , T b ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_DDRM.addEquals((DMatrixRMaj)ret.getMatrix(),beta,(DMatrixRMaj)b.getMatrix());
        else
            CommonOps_FDRM.addEquals((FMatrixRMaj)ret.getMatrix(),(float)beta,(FMatrixRMaj)b.getMatrix());

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
            return VectorVectorMult_DDRM.innerProd((DMatrixRMaj)mat,(DMatrixRMaj)v.getMatrix());
        else
            return VectorVectorMult_FDRM.innerProd((FMatrixRMaj)mat,(FMatrixRMaj)v.getMatrix());
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
     * @see CommonOps_DDRM#scale(double, DMatrixD1)
     *
     * @param val The multiplication factor.
     * @return The scaled matrix.
     */
    public T scale( double val ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_DDRM.scale(val,(DMatrixRMaj)ret.getMatrix());
        else
            CommonOps_FDRM.scale((float)val,(FMatrixRMaj)ret.getMatrix());

        return ret;
    }

    /**
     * <p>
     * Returns the result of dividing each element by 'val':
     * b<sub>i,j</sub> = a<sub>i,j</sub>/val
     * </p>
     *
     * @see CommonOps_DDRM#divide(DMatrixD1,double)
     *
     * @param val Divisor.
     * @return Matrix with its elements divided by the specified value.
     */
    public T divide( double val ) {
        T ret = copy();

        if( bits() == 64 )
            CommonOps_DDRM.divide((DMatrixRMaj)ret.getMatrix(),val);
        else
            CommonOps_FDRM.divide((FMatrixRMaj)ret.getMatrix(),(float)val);

        return ret;
    }

    /**
     * <p>
     * Returns the inverse of this matrix.<br>
     * <br>
     * b = a<sup>-1</sup><br>
     * </p>
     *
     * <p>
     * If the matrix could not be inverted then SingularMatrixException is thrown.  Even
     * if no exception is thrown the matrix could still be singular or nearly singular.
     * </p>
     *
     * @see CommonOps_DDRM#invert(DMatrixRMaj, DMatrixRMaj)
     *
     * @throws SingularMatrixException
     *
     * @return The inverse of this matrix.
     */
    public T invert() {
        T ret = createMatrix(mat.getNumRows(), mat.getNumCols());
        if (bits() == 64) {
            if (!CommonOps_DDRM.invert((DMatrixRMaj)mat, (DMatrixRMaj)ret.getMatrix())) {
                throw new SingularMatrixException();
            }
            if (MatrixFeatures_DDRM.hasUncountable((DMatrixRMaj)ret.getMatrix()))
                throw new SingularMatrixException("Solution has uncountable numbers");
        } else {
            if (!CommonOps_FDRM.invert((FMatrixRMaj)mat, (FMatrixRMaj)ret.getMatrix())) {
                throw new SingularMatrixException();
            }
            if (MatrixFeatures_FDRM.hasUncountable((FMatrixRMaj)ret.getMatrix()))
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
            CommonOps_DDRM.pinv((DMatrixRMaj)mat, (DMatrixRMaj)ret.getMatrix());
        } else {
            CommonOps_FDRM.pinv((FMatrixRMaj)mat, (FMatrixRMaj)ret.getMatrix());
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
     * @see CommonOps_DDRM#solve(DMatrixRMaj, DMatrixRMaj, DMatrixRMaj)
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
            if (!CommonOps_DDRM.solve((DMatrixRMaj)mat, (DMatrixRMaj)b.getMatrix(), (DMatrixRMaj)x.getMatrix()))
                throw new SingularMatrixException();

            if (MatrixFeatures_DDRM.hasUncountable((DMatrixRMaj)x.getMatrix()))
                throw new SingularMatrixException("Solution contains uncountable numbers");
        } else {
            if (!CommonOps_FDRM.solve((FMatrixRMaj)mat, (FMatrixRMaj)b.getMatrix(), (FMatrixRMaj)x.getMatrix()))
                throw new SingularMatrixException();

            if (MatrixFeatures_FDRM.hasUncountable((FMatrixRMaj)x.getMatrix()))
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
     * @see CommonOps_DDRM#fill(DMatrixD1, double)
     *
     * @param val The value each element is set to.
     */
    public void set( double val ) {
        if (bits() == 64) {
            CommonOps_DDRM.fill((DMatrixRMaj)mat, val);
        } else {
            CommonOps_FDRM.fill((FMatrixRMaj)mat, (float)val);
        }
    }

    /**
     * Sets all the elements in the matrix equal to zero.
     *
     * @see CommonOps_DDRM#fill(DMatrixD1, double)
     */
    public void zero() {
        if (bits() == 64) {
            ((DMatrixRMaj)mat).zero();
        } else {
            ((FMatrixRMaj)mat).zero();
        }
    }

    /**
     * <p>
     * Computes the Frobenius normal of the matrix:<br>
     * <br>
     * normF = Sqrt{  &sum;<sub>i=1:m</sub> &sum;<sub>j=1:n</sub> { a<sub>ij</sub><sup>2</sup>}   }
     * </p>
     *
     * @see NormOps_DDRM#normF(DMatrixD1)
     *
     * @return The matrix's Frobenius normal.
     */
    public double normF() {
        if (bits() == 64) {
            return NormOps_DDRM.normF((DMatrixRMaj)mat);
        } else {
            return NormOps_FDRM.normF((FMatrixRMaj)mat);
        }
    }

    /**
     * <p>
     * The condition p = 2 number of a matrix is used to measure the sensitivity of the linear
     * system <b>Ax=b</b>.  A value near one indicates that it is a well conditioned matrix.
     * </p>
     *
     * @see NormOps_DDRM#conditionP2(DMatrixRMaj)
     *
     * @return The condition number.
     */
    public double conditionP2() {
        if (bits() == 64) {
            return NormOps_DDRM.conditionP2((DMatrixRMaj)mat);
        } else {
            return NormOps_FDRM.conditionP2((FMatrixRMaj)mat);
        }
    }

    /**
     * Computes the determinant of the matrix.
     *
     * @see CommonOps_DDRM#det(DMatrixRMaj)
     *
     * @return The determinant.
     */
    public double determinant() {
        if (bits() == 64) {
            double ret = CommonOps_DDRM.det((DMatrixRMaj)mat);
            // if the decomposition silently failed then the matrix is most likely singular
            if (UtilEjml.isUncountable(ret))
                return 0;
            return ret;
        } else {
            double ret = CommonOps_FDRM.det((FMatrixRMaj)mat);
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
     * @see CommonOps_DDRM#trace(DMatrix1Row)
     *
     * @return The trace of the matrix.
     */
    public double trace() {
        if (bits() == 64) {
            return CommonOps_DDRM.trace((DMatrixRMaj)mat);
        } else {
            return CommonOps_FDRM.trace((FMatrixRMaj)mat);
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
     * @see DMatrixRMaj#reshape(int,int,boolean)
     *
     * @param numRows The new number of rows in the matrix.
     * @param numCols The new number of columns in the matrix.
     */
    public void reshape( int numRows , int numCols ) {
        if (bits() == 64) {
            ((DMatrixRMaj)mat).reshape(numRows, numCols, false);
        } else {
            ((FMatrixRMaj)mat).reshape(numRows, numCols, false);
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
            ((DMatrixRMaj)mat).set(row, col, value);
        } else {
            ((FMatrixRMaj)mat).set(row, col, (float)value);
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
            ((DMatrixRMaj)mat).set(index, value);
        } else {
            ((FMatrixRMaj)mat).set(index, (float)value);
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
            DMatrixRMaj m = (DMatrixRMaj)mat;
            for (int i = 0; i < values.length; i++) {
                m.set(row, offset + i, values[i]);
            }
        } else {
            FMatrixRMaj m = (FMatrixRMaj)mat;
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
            DMatrixRMaj m = (DMatrixRMaj)mat;
            for (int i = 0; i < values.length; i++) {
                m.set(offset + i, column, values[i]);
            }
        } else {
            FMatrixRMaj m = (FMatrixRMaj)mat;
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
            return ((DMatrixRMaj)mat).get(row, col);
        } else {
            return ((FMatrixRMaj)mat).get(row, col);
        }
    }

    /**
     * Returns the value of the matrix at the specified index of the 1D row major array.
     *
     * @see DMatrixRMaj#get(int)
     *
     * @param index The element's index whose value is to be returned
     * @return The value of the specified element.
     */
    public double get( int index ) {
        if (bits() == 64) {
            return ((DMatrixRMaj)mat).data[index];
        } else {
            return ((FMatrixRMaj)mat).data[index];
        }
    }

    /**
     * Returns the index in the matrix's array.
     *
     * @see DMatrixRMaj#getIndex(int, int)
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
    public DMatrixIterator iterator(boolean rowMajor, int minRow, int minCol, int maxRow, int maxCol)
    {
        return new DMatrixIterator((DMatrixRMaj)mat,rowMajor, minRow, minCol, maxRow, maxCol);
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
            return ((DMatrixRMaj)mat).getNumElements();
        else
            return ((FMatrixRMaj)mat).getNumElements();
    }


    /**
     * Prints the matrix to standard out.
     */
    public void print() {
        if( bits() == 64 ) {
            MatrixIO.print(System.out, (DMatrixRMaj)mat);
        } else {
            MatrixIO.print(System.out, (FMatrixRMaj) mat);
        }
    }

    /**
     * Prints the matrix to standard out with the specified precision.
     */
    public void print(int numChar , int precision) {
        if( bits() == 64 ) {
            MatrixIO.print(System.out, (DMatrixRMaj)mat, numChar, precision);
        } else {
            MatrixIO.print(System.out, (FMatrixRMaj)mat, numChar, precision);
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
            MatrixIO.print(System.out, (DMatrixRMaj)mat, format);
        } else {
            MatrixIO.print(System.out, (FMatrixRMaj)mat, format);
        }
    }

    /**
     * <p>
     * Converts the array into a string format for display purposes.
     * The conversion is done using {@link MatrixIO#print(java.io.PrintStream, DMatrix)}.
     * </p>
     *
     * @return String representation of the matrix.
     */
    public String toString() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if( bits() == 64 ) {
            MatrixIO.print(new PrintStream(stream), (DMatrixRMaj)mat);
        } else {
            MatrixIO.print(new PrintStream(stream), (FMatrixRMaj)mat);
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
     * If any of the inputs are set to SimpleMatrix.END then it will be set to the last row
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
            CommonOps_DDRM.extract((DMatrixRMaj)mat, y0, y1, x0, x1, (DMatrixRMaj)ret.getMatrix(), 0, 0);
        } else {
            CommonOps_FDRM.extract((FMatrixRMaj)mat, y0, y1, x0, x1, (FMatrixRMaj)ret.getMatrix(), 0, 0);
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
                SpecializedOps_DDRM.subvector((DMatrixRMaj)mat, element, 0, length, true, 0, (DMatrixRMaj)ret.getMatrix());
            } else {
                SpecializedOps_DDRM.subvector((DMatrixRMaj)mat, 0, element, length, false, 0, (DMatrixRMaj)ret.getMatrix());
            }
        } else {
            if (extractRow) {
                SpecializedOps_FDRM.subvector((FMatrixRMaj)mat, element, 0, length, true, 0, (FMatrixRMaj)ret.getMatrix());
            } else {
                SpecializedOps_FDRM.subvector((FMatrixRMaj)mat, 0, element, length, false, 0, (FMatrixRMaj)ret.getMatrix());
            }
        }

        return ret;
    }

    /**
     * <p>
     * If a vector then a square matrix is returned if a matrix then a vector of diagonal ements is returned
     * </p>
     *
     * @see CommonOps_DDRM#extractDiag(DMatrixRMaj, DMatrixRMaj)
     * @return Diagonal elements inside a vector or a square matrix with the same diagonal elements.
     */
    public T diag()
    {
        T diag;
        if( bits() == 64 ) {
            if (MatrixFeatures_DDRM.isVector(mat)) {
                int N = Math.max(mat.getNumCols(),mat.getNumRows());
                diag = createMatrix(N,N);
                CommonOps_DDRM.diag((DMatrixRMaj)diag.getMatrix(),N,((DMatrixRMaj)mat).data);
            } else {
                int N = Math.min(mat.getNumCols(),mat.getNumRows());
                diag = createMatrix(N,1);
                CommonOps_DDRM.extractDiag((DMatrixRMaj)mat, (DMatrixRMaj)diag.getMatrix());
            }
        } else {
            if (MatrixFeatures_FDRM.isVector(mat)) {
                int N = Math.max(mat.getNumCols(),mat.getNumRows());
                diag = createMatrix(N,N);
                CommonOps_FDRM.diag((FMatrixRMaj)diag.getMatrix(),N,((FMatrixRMaj)mat).data);
            } else {
                int N = Math.min(mat.getNumCols(),mat.getNumRows());
                diag = createMatrix(N,1);
                CommonOps_FDRM.extractDiag((FMatrixRMaj)mat, (FMatrixRMaj)diag.getMatrix());
            }
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
            return MatrixFeatures_DDRM.isIdentical((DMatrixRMaj)mat, (DMatrixRMaj)a.getMatrix(), tol);
        } else {
            return MatrixFeatures_FDRM.isIdentical((FMatrixRMaj)mat, (FMatrixRMaj)a.getMatrix(), (float)tol);
        }
    }

    /**
     * Checks to see if any of the elements in this matrix are either NaN or infinite.
     *
     * @return True of an element is NaN or infinite.  False otherwise.
     */
    public boolean hasUncountable() {
        if( bits() == 64 ) {
            return MatrixFeatures_DDRM.hasUncountable((DMatrixRMaj)mat);
        } else {
            return MatrixFeatures_FDRM.hasUncountable((FMatrixRMaj)mat);
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
            CommonOps_DDRM.insert((DMatrixRMaj)B.getMatrix(), (DMatrixRMaj)mat, insertRow, insertCol);
        } else {
            CommonOps_FDRM.insert((FMatrixRMaj)B.getMatrix(),(FMatrixRMaj) mat, insertRow, insertCol);
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
            return CommonOps_DDRM.elementMaxAbs((DMatrixRMaj)mat);
        } else {
            return CommonOps_FDRM.elementMaxAbs((FMatrixRMaj)mat);
        }
    }

    /**
     * Computes the sum of all the elements in the matrix.
     *
     * @return Sum of all the elements.
     */
    public double elementSum() {
        if( bits() == 64 ) {
            return CommonOps_DDRM.elementSum((DMatrixRMaj)mat);
        } else {
            return CommonOps_FDRM.elementSum((FMatrixRMaj)mat);
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
            CommonOps_DDRM.elementMult((DMatrixRMaj)mat, (DMatrixRMaj)b.getMatrix(), (DMatrixRMaj)c.getMatrix());
        } else {
            CommonOps_FDRM.elementMult((FMatrixRMaj)mat, (FMatrixRMaj)b.getMatrix(), (FMatrixRMaj)c.getMatrix());
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
            CommonOps_DDRM.elementDiv((DMatrixRMaj)mat, (DMatrixRMaj)b.getMatrix(), (DMatrixRMaj)c.getMatrix());
        } else {
            CommonOps_FDRM.elementDiv((FMatrixRMaj)mat, (FMatrixRMaj)b.getMatrix(), (FMatrixRMaj)c.getMatrix());
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
            CommonOps_DDRM.elementPower((DMatrixRMaj)mat, (DMatrixRMaj)b.getMatrix(), (DMatrixRMaj)c.getMatrix());
        } else {
            CommonOps_FDRM.elementPower((FMatrixRMaj)mat, (FMatrixRMaj)b.getMatrix(), (FMatrixRMaj)c.getMatrix());
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
            CommonOps_DDRM.elementPower((DMatrixRMaj)mat, b, (DMatrixRMaj)c.getMatrix());
        } else {
            CommonOps_FDRM.elementPower((FMatrixRMaj)mat, (float)b, (FMatrixRMaj)c.getMatrix());
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
            CommonOps_DDRM.elementExp((DMatrixRMaj)mat, (DMatrixRMaj)c.getMatrix());
        } else {
            CommonOps_FDRM.elementExp((FMatrixRMaj)mat, (FMatrixRMaj)c.getMatrix());
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
            CommonOps_DDRM.elementLog((DMatrixRMaj)mat, (DMatrixRMaj)c.getMatrix());
        } else {
            CommonOps_FDRM.elementLog((FMatrixRMaj)mat, (FMatrixRMaj)c.getMatrix());
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
            CommonOps_DDRM.changeSign((DMatrixRMaj)A.getMatrix());
        } else {
            CommonOps_FDRM.changeSign((FMatrixRMaj)A.getMatrix());
        }
        return A;
    }

    /**
     * <p>Allows you to perform an equation in-place on this matrix by specifying the right hand side.  For information on how to define an equation
     * see {@link org.ejml.equation.Equation}.  The variable sequence alternates between variable and it's label String.
     * This matrix is by default labeled as 'A', but is a string is the first object in 'variables' then it will take
     * on that value.  The variable passed in can be any data type supported by Equation can be passed in.
     * This includes matrices and scalars.</p>
     *
     * Examples:<br>
     * <pre>
     * perform("A = A + B",matrix,"B");     // Matrix addition
     * perform("A + B",matrix,"B");         // Matrix addition with implicit 'A = '
     * perform("A(5,:) = B",matrix,"B");    // Insert a row defined by B into A
     * perform("[A;A]");                    // stack A twice with implicit 'A = '
     * perform("Q = B + 2","Q",matrix,"B"); // Specify the name of 'this' as Q
     *
     * </pre>
     *
     * @param equation String representing the symbol equation
     * @param variables List of variable names and variables
     */
    public void equation(String equation , Object ...variables ) {
        if( variables.length >= 25 )
            throw new IllegalArgumentException("Too many variables!  At most 25");

        if( !(mat instanceof DMatrixRMaj))
            return;

        Equation eq = new Equation();

        String nameThis = "A";
        int offset = 0;
        if( variables.length > 0 && variables[0] instanceof String ) {
            nameThis = (String)variables[0];
            offset = 1;

            if( variables.length%2 != 1 )
                throw new IllegalArgumentException("Expected and odd length for variables");
        } else {
            if( variables.length%2 != 0 )
                throw new IllegalArgumentException("Expected and even length for variables");
        }
        eq.alias((DMatrixRMaj)mat,nameThis);

        for( int i = offset; i < variables.length; i += 2 ) {
            if( !(variables[i+1] instanceof String))
                throw new IllegalArgumentException("String expected at variables index "+i);
            Object o = variables[i];
            String name = (String)variables[i+1];

            if( SimpleBase.class.isAssignableFrom(o.getClass())) {
                eq.alias(((SimpleBase)o).matrix_F64(),name);
            } else if( o instanceof DMatrixRMaj) {
                eq.alias((DMatrixRMaj)o, name);
            } else if( o instanceof Double ){
                eq.alias((Double)o,name);
            } else if( o instanceof Integer ){
                eq.alias((Integer)o,name);
            } else {
                String type = o == null ? "null" : o.getClass().getSimpleName();
                throw new IllegalArgumentException("Variable type not supported by Equation! "+type);
            }
        }

        // see if the assignment is implicit
        if( !equation.contains("=")) {
            equation = nameThis+" = "+equation;
        }

        eq.process(equation);
    }

    /**
     * <p>
     * Saves this matrix to a file as a serialized binary object.
     * </p>
     *
     * @see MatrixIO#saveBin( DMatrix, String)
     *
     * @param fileName
     * @throws java.io.IOException
     */
    public void saveToFileBinary( String fileName )
        throws IOException
    {
        MatrixIO.saveBin((DMatrixRMaj)mat, fileName);
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
        DMatrix mat = MatrixIO.loadBin(fileName);

        // see if its a DMatrixRMaj
        if( mat instanceof DMatrixRMaj) {
            return SimpleMatrix.wrap((DMatrixRMaj)mat);
        } else {
            // if not convert it into one and wrap it
            return SimpleMatrix.wrap( new DMatrixRMaj(mat));
        }
    }

    /**
     * <p>
     * Saves this matrix to a file in a CSV format.  For the file format see {@link MatrixIO}.
     * </p>
     *
     * @see MatrixIO#saveBin( DMatrix, String)
     *
     * @param fileName
     * @throws java.io.IOException
     */
    public void saveToFileCSV( String fileName )
            throws IOException
    {
        MatrixIO.saveCSV((DMatrixRMaj)mat, fileName);
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
        DMatrix mat = MatrixIO.loadCSV(fileName);

        T ret = createMatrix(1,1);

        // see if its a DMatrixRMaj
        if( mat instanceof DMatrixRMaj) {
            ret.mat = (DMatrixRMaj)mat;
        } else {
            // if not convert it into one and wrap it
            ret.mat = new DMatrixRMaj(mat);
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
        if( mat.getClass() == DMatrixRMaj.class ) {
            return 64;
        } else if( mat.getClass() == FMatrixRMaj.class ) {
            return 32;
        } else {
            throw new RuntimeException("Unknown matrix type");
        }
    }

    /**
     * <p>Concatinates all the matrices together along their columns.  If the rows do not match the upper elements
     * are set to zero.</p>
     *
     * A = [ m[0] , ... , m[n-1] ]
     *
     * @param A Set of matrices
     * @return Resulting matrix
     */
    public T concatColumns( SimpleBase ...A ) {
        Matrix combined;
        if( mat.getClass() == DMatrixRMaj.class ) {
            DMatrixRMaj m[] = new DMatrixRMaj[A.length+1];
            m[0] = (DMatrixRMaj)mat;
            for (int i = 0; i < A.length; i++) {
                m[1] = A[i].matrix_F64();
            }
            combined = CommonOps_DDRM.concatColumns(m);
        } else if( mat.getClass() == FMatrixRMaj.class ) {
            FMatrixRMaj m[] = new FMatrixRMaj[A.length+1];
            m[0] = (FMatrixRMaj)mat;
            for (int i = 0; i < A.length; i++) {
                m[1] = A[i].matrix_F32();
            }
            combined = CommonOps_FDRM.concatColumns(m);
        } else {
            throw new RuntimeException("Unknown matrix type");
        }
        return wrapMatrix(combined);
    }

    /**
     * <p>Concatinates all the matrices together along their columns.  If the rows do not match the upper elements
     * are set to zero.</p>
     *
     * A = [ m[0] ; ... ; m[n-1] ]
     *
     * @param A Set of matrices
     * @return Resulting matrix
     */
    public T concatRows( SimpleBase ...A ) {
        Matrix combined;
        if( mat.getClass() == DMatrixRMaj.class ) {
            DMatrixRMaj m[] = new DMatrixRMaj[A.length+1];
            m[0] = (DMatrixRMaj)mat;
            for (int i = 0; i < A.length; i++) {
                m[1] = A[i].matrix_F64();
            }
            combined = CommonOps_DDRM.concatRows(m);
        } else if( mat.getClass() == FMatrixRMaj.class ) {
            FMatrixRMaj m[] = new FMatrixRMaj[A.length+1];
            m[0] = (FMatrixRMaj)mat;
            for (int i = 0; i < A.length; i++) {
                m[1] = A[i].matrix_F32();
            }
            combined = CommonOps_FDRM.concatRows(m);
        } else {
            throw new RuntimeException("Unknown matrix type");
        }
        return wrapMatrix(combined);
    }

    /**
     * Extracts the specified rows from the matrix.
     * @param begin First row.  Inclusive.
     * @param end Last row + 1.
     * @return Submatrix that contains the specified rows.
     */
    public T rows( int begin , int end ) {
        return extractMatrix(begin,end,0,SimpleMatrix.END);
    }

    /**
     * Extracts the specified rows from the matrix.
     * @param begin First row.  Inclusive.
     * @param end Last row + 1.
     * @return Submatrix that contains the specified rows.
     */
    public T cols( int begin , int end ) {
        return extractMatrix(0,SimpleMatrix.END, begin, end);
    }
}
