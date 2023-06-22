/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.*;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.ops.MatrixIO;

import java.io.IOException;

/**
 * <p>Interface that only implements operations in {@link SimpleBase} that are read only.</p>
 *
 * <p>
 * A "shallow immutable" matrix where none of its API allow you to modify the matrix. However (similar to const
 * in C++) you can access the modifiable matrix by downcasting to {@link SimpleMatrix} or by externally modifying
 * in a function that has access to the original object. This interface acts as a strong suggestion that the matrix
 * should not be modified. However, the only way to ensure that no external code modifies this matrix is to create a
 * local copy that can't be accessed externally.
 * </p>
 *
 * <p>NOTE: Implementations of ConstMatrix must extend {@link SimpleBase} or else it won't work when given as
 * input to any class based off of {@link SimpleBase}.</p>
 *
 * @author Peter Abeles
 */
public interface ConstMatrix<T extends ConstMatrix<T>> {
    /**
     * <p>
     * Returns the transpose of this matrix.<br>
     * a<sup>T</sup>
     * </p>
     *
     * @return A matrix that is n by m.
     * @see CommonOps_DDRM#transpose(DMatrixRMaj, DMatrixRMaj)
     */
    T transpose();

    /**
     * Returns a matrix that is the conjugate transpose. If real then this is the
     * same as calling {@link #transpose()}.
     */
    T transposeConjugate();

    /**
     * <p>
     * Returns a matrix which is the result of matrix multiplication:<br>
     * <br>
     * c = a * b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
     * </p>
     *
     * @param B A matrix that is n by p. Not modified.
     * @return The results of this operation.
     * @see CommonOps_DDRM#mult(DMatrix1Row, DMatrix1Row, DMatrix1Row)
     */
    T mult( ConstMatrix<?> B );

    /**
     * <p>
     * Computes the Kronecker product between this matrix and the provided B matrix:<br>
     * <br>
     * C = kron(A,B)
     * </p>
     *
     * @param B The right matrix in the operation. Not modified.
     * @return Kronecker product between this matrix and B.
     * @see CommonOps_DDRM#kron(DMatrixRMaj, DMatrixRMaj, DMatrixRMaj)
     */
    T kron( ConstMatrix<?> B );

    /**
     * <p>
     * Returns the result of matrix addition:<br>
     * <br>
     * c = a + b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
     * </p>
     *
     * @param B m by n matrix. Not modified.
     * @return The results of this operation.
     */
    T plus( ConstMatrix<?> B );

    /**
     * <p>
     * Returns the result of matrix subtraction:<br>
     * <br>
     * c = a - b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
     * </p>
     *
     * @param B m by n matrix. Not modified.
     * @return The results of this operation.
     * @see CommonOps_DDRM#subtract(DMatrixD1, DMatrixD1, DMatrixD1)
     */
    T minus( ConstMatrix<?> B );

    /**
     * <p>
     * Returns the result of matrix-double subtraction:<br>
     * <br>
     * c = a - b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in double.
     * </p>
     *
     * <p>NOTE: If the matrix is complex then 'b' will be treated like a complex number with imaginary = 0.</p>
     *
     * @param b Value subtracted from each element
     * @return The results of this operation.
     * @see CommonOps_DDRM#subtract(DMatrixD1, double, DMatrixD1)
     */
    T minus( double b );

    /**
     * Subtracts a complex scalar from each element in the matrix. If the matrix is real, then it will
     * return a complex matrix unless the imaginary component of the scalar is zero.
     *
     * @param real Real component of scalar value
     * @param imag Imaginary component of scalar value
     * @return The results of this operation.
     */
    T minusComplex( double real, double imag );

    /**
     * <p>
     * Returns the result of scalar addition:<br>
     * <br>
     * c = a + b<br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in double.
     * </p>
     *
     * <p>NOTE: If the matrix is complex then 'b' will be treated like a complex number with imaginary = 0.</p>
     *
     * @param b Value added to each element
     * @return A matrix that contains the results.
     * @see CommonOps_DDRM#add(DMatrixD1, double, DMatrixD1)
     */
    T plus( double b );

    /**
     * Adds a complex scalar from each element in the matrix. If the matrix is real, then it will
     * return a complex matrix unless the imaginary component of the scalar is zero.
     *
     * @param real Real component of scalar value
     * @param imag Imaginary component of scalar value
     * @return The results of this operation.
     */
    T plusComplex( double real, double imag );

    /**
     * <p>
     * Performs a matrix addition and scale operation.<br>
     * <br>
     * c = a + &beta;*b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
     * </p>
     *
     * <p>NOTE: If the matrix is complex then 'b' will be treated like a complex number with imaginary = 0.</p>
     *
     * @param B m by n matrix. Not modified.
     * @return A matrix that contains the results.
     * @see CommonOps_DDRM#add(DMatrixD1, double, DMatrixD1, DMatrixD1)
     */
    T plus( double beta, ConstMatrix<?> B );

    /**
     * Computes the dot product (or inner product) between this vector and vector 'v'.
     *
     * @param v The second vector in the dot product. Not modified.
     * @return dot product
     */
    double dot( ConstMatrix<?> v );

    /**
     * Returns true if this matrix is a vector. A vector is defined as a matrix
     * that has either one row or column.
     *
     * @return Returns true for vectors and false otherwise.
     */
    boolean isVector();

    /**
     * <p>
     * Returns the result of scaling each element by 'val':<br>
     * b<sub>i,j</sub> = val*a<sub>i,j</sub>
     * </p>
     *
     * @param val The multiplication factor. If matrix is complex then the imaginary component is zero.
     * @return The scaled matrix.
     * @see CommonOps_DDRM#scale(double, DMatrixD1)
     */
    T scale( double val );

    /**
     * Scales/multiplies each element in the matrix by the complex number. If the matrix is real, then it will
     * return a complex matrix unless the imaginary component of the scalar is zero.
     *
     * @param real Real component of scalar value
     * @param imag Imaginary component of scalar value
     * @return Scaled matrix
     */
    T scaleComplex( double real, double imag );

    /**
     * <p>
     * Returns the result of dividing each element by 'val':
     * b<sub>i,j</sub> = a<sub>i,j</sub>/val
     * </p>
     *
     * @param val Divisor. If matrix is complex then the imaginary component is zero.
     * @return Matrix with its elements divided by the specified value.
     * @see CommonOps_DDRM#divide(DMatrixD1, double)
     */
    T divide( double val );

    /**
     * <p>
     * Returns the inverse of this matrix.<br>
     * <br>
     * b = a<sup>-1</sup><br>
     * </p>
     *
     * <p>
     * If the matrix could not be inverted then SingularMatrixException is thrown. Even
     * if no exception is thrown the matrix could still be singular or nearly singular.
     * </p>
     *
     * @return The inverse of this matrix.
     * @see CommonOps_DDRM#invert(DMatrixRMaj, DMatrixRMaj)
     */
    T invert();

    /**
     * <p>
     * Computes the Moore-Penrose pseudo-inverse
     * </p>
     *
     * @return inverse computed using the pseudo inverse.
     */
    T pseudoInverse();

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
     * If the system could not be solved then SingularMatrixException is thrown. Even
     * if no exception is thrown 'a' could still be singular or nearly singular.
     * </p>
     *
     * @param B n by p matrix. Not modified.
     * @return The solution for 'x' that is n by p.
     * @see CommonOps_DDRM#solve(DMatrixRMaj, DMatrixRMaj, DMatrixRMaj)
     */
    T solve( ConstMatrix<?> B );

    /**
     * <p>
     * Computes the Frobenius normal of the matrix:<br>
     * <br>
     * normF = Sqrt{  &sum;<sub>i=1:m</sub> &sum;<sub>j=1:n</sub> { a<sub>ij</sub><sup>2</sup>}   }
     * </p>
     *
     * @return The matrix's Frobenius normal.
     * @see NormOps_DDRM#normF(DMatrixD1)
     */
    double normF();

    /**
     * <p>
     * The condition p = 2 number of a matrix is used to measure the sensitivity of the linear
     * system <b>Ax=b</b>. A value near one indicates that it is a well conditioned matrix.
     * </p>
     *
     * @return The condition number.
     * @see NormOps_DDRM#conditionP2(DMatrixRMaj)
     */
    double conditionP2();

    /**
     * Computes the determinant of the matrix.
     *
     * @return The determinant.
     * @see CommonOps_DDRM#det(DMatrixRMaj)
     */
    double determinant();

    /**
     * Computes the determinant of a complex matrix. If the matrix is real then the imaginary component
     * is always zero.
     *
     * @return The determinant.
     * @see CommonOps_ZDRM#det(ZMatrixRMaj)
     */
    Complex_F64 determinantComplex();

    /**
     * <p>
     * Computes the trace of the matrix.
     * </p>
     *
     * @return The trace of the matrix.
     * @see CommonOps_DDRM#trace(DMatrix1Row)
     */
    double trace();

    /**
     * <p>
     * Computes the trace of a complex matrix. If the matrix is real then the imaginary component
     * is always zero.
     * </p>
     *
     * @return The trace of the matrix.
     * @see CommonOps_ZDRM#trace(ZMatrixRMaj, Complex_F64)
     */
    Complex_F64 traceComplex();

    /**
     * Returns the value of the specified matrix element. Performs a bounds check to make sure
     * the requested element is part of the matrix.
     *
     * <p>NOTE: Complex matrices will throw an exception</p>
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @return The value of the element.
     */
    double get( int row, int col );

    /**
     * Returns the value of the matrix at the specified index of the 1D row major array.
     *
     * @param index The element's index whose value is to be returned
     * @return The value of the specified element.
     * @see DMatrixRMaj#get(int)
     */
    double get( int index );

    /**
     * Used to get the complex value of a matrix element.
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @param output Storage for the value
     */
    void get( int row, int col, Complex_F64 output );

    /**
     * Returns the real component of the element. If a real matrix this is the same as calling {@link #get(int, int)}.
     *
     * @param row The row of the element.
     * @param col The column of the element.
     */
    double getReal( int row, int col );

    /**
     * Returns the imaginary component of the element. If a real matrix this will always be zero.
     *
     * @param row The row of the element.
     * @param col The column of the element.
     */
    double getImaginary( int row, int col );

    /** Shorthand for {@link #getImaginary(int, int)} */
    default double getImag( int row, int col ) {
        return getImaginary(row, col);
    }

    /**
     * Returns the index in the matrix's array.
     *
     * @param row The row number.
     * @param col The column number.
     * @return The index of the specified element.
     * @see DMatrixRMaj#getIndex(int, int)
     */
    int getIndex( int row, int col );

    /**
     * Creates a new iterator for traversing through a submatrix inside this matrix. It can be traversed
     * by row or by column. Range of elements is inclusive, e.g. minRow = 0 and maxRow = 1 will include rows
     * 0 and 1. The iteration starts at (minRow,minCol) and ends at (maxRow,maxCol)
     *
     * @param rowMajor true means it will traverse through the submatrix by row first, false by columns.
     * @param minRow first row it will start at.
     * @param minCol first column it will start at.
     * @param maxRow last row it will stop at.
     * @param maxCol last column it will stop at.
     * @return A new MatrixIterator
     */
    DMatrixIterator iterator( boolean rowMajor, int minRow, int minCol, int maxRow, int maxCol );

    /**
     * Creates and returns a matrix which is identical to this one.
     *
     * @return A new identical matrix.
     */
    T copy();

    /**
     * Returns the number of rows in this matrix.
     *
     * @return number of rows.
     */
    int getNumRows();

    /**
     * Returns the number of columns in this matrix.
     *
     * @return number of columns.
     */
    int getNumCols();

    /**
     * Returns the number of elements in this matrix, which is equal to
     * the number of rows times the number of columns.
     *
     * @return The number of elements in the matrix.
     */
    default int getNumElements() {
        return getNumRows()*getNumCols();
    }

    /**
     * Prints the matrix to standard out.
     */
    void print();

    /**
     * <p>
     * Prints the matrix to standard out given a {@link java.io.PrintStream#printf} style floating point format,
     * e.g. print("%f").
     * </p>
     */
    void print( String format );

    /**
     * Returns 2D array of doubles using the {@link SimpleBase#get(int, int)} method.
     *
     * @return 2D array of doubles.
     */
    double[][] toArray2();

    /**
     * <p>
     * Creates a new SimpleMatrix which is a submatrix of this matrix.
     * </p>
     * <p>
     * s<sub>i-y0 , j-x0</sub> = o<sub>ij</sub> for all y0 &le; i &lt; y1 and x0 &le; j &lt; x1<br>
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
    T extractMatrix( int y0, int y1, int x0, int x1 );

    /**
     * <p>
     * Extracts a row or column from this matrix. The returned vector will either be a row
     * or column vector depending on the input type.
     * </p>
     *
     * @param extractRow If true a row will be extracted.
     * @param element The row or column the vector is contained in.
     * @return Extracted vector.
     * @see #getRow(int)
     * @see #getColumn(int)
     */
    T extractVector( boolean extractRow, int element );

    /**
     * Returns the specified row in 'this' matrix as a row vector.
     *
     * @param row Row in the matrix
     * @return Extracted vector
     * @see #extractVector(boolean, int)
     */
    T getRow( int row );

    /**
     * Returns the specified column in 'this' matrix as a column vector.
     *
     * @param col Column in the matrix
     * @return Extracted vector
     * @see #extractVector(boolean, int)
     */
    T getColumn( int col );

    /**
     * Extracts the specified rows from the matrix.
     *
     * @param begin First row (inclusive).
     * @param end Last row (exclusive).
     * @return Submatrix that contains the specified rows.
     * @deprecated Inconsistent API. Use {@link #getRows(int, int)} instead.
     */
    @Deprecated
    T rows( int begin, int end );

    /**
     * Extracts the specified rows from the matrix.
     *
     * @param begin First row (inclusive).
     * @param end Last row (exclusive).
     * @return Submatrix that contains the specified rows.
     */
    T getRows( int begin, int end );

    /**
     * Extracts the specified columns from the matrix.
     *
     * @param begin First column (inclusive).
     * @param end Last column (exclusive).
     * @return Submatrix that contains the specified columns.
     * @deprecated Inconsistent API. Use {@link #getColumns(int, int)} instead.
     */
    @Deprecated
    T cols( int begin, int end );

    /**
     * Extracts the specified columns from the matrix.
     *
     * @param begin First column (inclusive).
     * @param end Last column (exclusive).
     * @return Submatrix that contains the specified columns.
     */
    T getColumns( int begin, int end );

    /**
     * <p>
     * If a vector then a square matrix is returned if a matrix then a vector of diagonal ements is returned
     * </p>
     *
     * @return Diagonal elements inside a vector or a square matrix with the same diagonal elements.
     * @see CommonOps_DDRM#extractDiag(DMatrixRMaj, DMatrixRMaj)
     */
    T diag();

    /**
     * Checks to see if matrix 'a' is the same as this matrix within the specified
     * tolerance.
     *
     * @param a The matrix it is being compared against.
     * @param tol How similar they must be to be equals.
     * @return If they are equal within tolerance of each other.
     */
    boolean isIdentical( ConstMatrix<?> a, double tol );

    /**
     * Checks to see if any of the elements in this matrix are either NaN or infinite.
     *
     * @return True of an element is NaN or infinite. False otherwise.
     */
    boolean hasUncountable();

    /**
     * <p>
     * Creates a new matrix that is a combination of this matrix and matrix B. B is
     * written into A at the specified location if needed the size of A is increased by
     * growing it. A is grown by padding the new area with zeros.
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
    T combine( int insertRow, int insertCol, ConstMatrix<?> B );

    /**
     * Returns the maximum real value of all the elements in this matrix.
     *
     * @return Largest real value of any element.
     */
    double elementMax();

    /**
     * Returns the minimum real value of all the elements in this matrix.
     *
     * @return Smallest real value of any element.
     */
    double elementMin();

    /**
     * Returns the maximum absolute value of all the elements in this matrix. This is
     * equivalent to the infinite p-norm of the matrix.
     *
     * @return Largest absolute value of any element.
     */
    double elementMaxAbs();

    /**
     * Returns the minimum absolute value of all the elements in this matrix.
     *
     * @return Smallest absolute value of any element.
     */
    double elementMinAbs();

    /**
     * Computes the sum of all the elements in the matrix. Only works on real matrices.
     *
     * @return Sum of all the elements.
     */
    double elementSum();

    /**
     * Computes the sum of all the elements in the matrix. Works with both real and complex matrices.
     *
     * @return Sum of all the elements.
     */
    Complex_F64 elementSumComplex();

    /**
     * <p>
     * Returns a matrix which is the result of an element by element multiplication of 'this' and 'b':
     * c<sub>i,j</sub> = a<sub>i,j</sub>*b<sub>i,j</sub>
     * </p>
     *
     * @param b A simple matrix.
     * @return The element by element multiplication of 'this' and 'b'.
     */
    T elementMult( ConstMatrix<?> b );

    /**
     * <p>
     * Returns a matrix which is the result of an element by element division of 'this' and 'b':
     * c<sub>i,j</sub> = a<sub>i,j</sub>/b<sub>i,j</sub>
     * </p>
     *
     * @param b A simple matrix.
     * @return The element by element division of 'this' and 'b'.
     */
    T elementDiv( ConstMatrix<?> b );

    /**
     * <p>
     * Returns a matrix which is the result of an element by element power of 'this' and 'b':
     * c<sub>i,j</sub> = a<sub>i,j</sub> ^ b<sub>i,j</sub>
     * </p>
     *
     * @param b A simple matrix.
     * @return The element by element power of 'this' and 'b'.
     */
    T elementPower( ConstMatrix<?> b );

    /**
     * <p>
     * Returns a matrix which is the result of an element by element power of 'this' and 'b':
     * c<sub>i,j</sub> = a<sub>i,j</sub> ^ b
     * </p>
     *
     * @param b Scalar
     * @return The element by element power of 'this' and 'b'.
     */
    T elementPower( double b );

    /**
     * <p>
     * Returns a matrix which is the result of an element by element exp of 'this'
     * c<sub>i,j</sub> = Math.exp(a<sub>i,j</sub>)
     * </p>
     *
     * @return The element by element power of 'this' and 'b'.
     */
    T elementExp();

    /**
     * <p>
     * Returns a matrix which is the result of an element by element exp of 'this'
     * c<sub>i,j</sub> = Math.log(a<sub>i,j</sub>)
     * </p>
     *
     * @return The element by element power of 'this' and 'b'.
     */
    T elementLog();

    /**
     * <p>Applies a user defined real-valued function to a real-valued matrix.</p>
     * c<sub>i,j</sub> = op(i, j, a<sub>i,j</sub>)
     *
     * <p>If the matrix is sparse then this is only applied to non-zero elements</p>
     */
    T elementOp( SimpleOperations.ElementOpReal op );

    /**
     * <p>Applies a user defined complex-valued function to a real or complex-valued matrix.</p>
     * c<sub>i,j</sub> = op(i, j, a<sub>i,j</sub>)
     *
     * <p>If the matrix is sparse then this is only applied to non-zero elements</p>
     */
    T elementOp( SimpleOperations.ElementOpComplex op );

    /**
     * <p>
     * Returns a new matrix whose elements are the negative of 'this' matrix's elements.<br>
     * <br>
     * b<sub>ij</sub> = -a<sub>ij</sub>
     * </p>
     *
     * @return A matrix that is the negative of the original.
     */
    T negative();

    /**
     * <p>Returns the complex conjugate of this matrix.</p>
     */
    T conjugate();

    /**
     * <p>Returns a real matrix that has the complex magnitude of each element in the matrix. For a real
     * matrix this is the abs()</p>
     */
    T magnitude();

    /**
     * <p>
     * Saves this matrix to a file in a CSV format. For the file format see {@link MatrixIO}.
     * </p>
     */
    void saveToFileCSV( String fileName ) throws IOException;

    /**
     * <p>
     * Saves this matrix to a file in a matrix market format. For the file format see {@link MatrixIO}.
     * </p>
     */
    void saveToMatrixMarket( String fileName ) throws IOException;

    /**
     * Returns true of the specified matrix element is valid element inside this matrix.
     *
     * @param row Row index.
     * @param col Column index.
     * @return true if it is a valid element in the matrix.
     */
    boolean isInBounds( int row, int col );

    /**
     * Size of internal array elements. 32 or 64 bits
     */
    int bits();

    /**
     * <p>Concatenates all the matrices together along their columns. If the rows do not match the upper elements
     * are set to zero.</p>
     *
     * A = [ this, m[0] , ... , m[n-1] ]
     *
     * @param matrices Set of matrices
     * @return Resulting matrix
     */
    T concatColumns( ConstMatrix<?>... matrices );

    /**
     * <p>Concatenates all the matrices together along their columns. If the rows do not match the upper elements
     * are set to zero.</p>
     *
     * A = [ this; m[0] ; ... ; m[n-1] ]
     *
     * @param matrices Set of matrices
     * @return Resulting matrix
     */
    T concatRows( ConstMatrix<?>... matrices );

    /**
     * Returns the type of matrix it is wrapping.
     */
    MatrixType getType();

    /**
     * Returns a matrix that contains the real valued portion of a complex matrix. For a real valued matrix
     * this will return a copy.
     */
    T real();

    /**
     * Returns a matrix that contains the imaginary valued portion of a complex matrix. For a real
     * valued matrix this will return a matrix full of zeros.
     */
    T imaginary();

    /** Convenience function. See {@link #imaginary()} */
    default T imag() {
        return imaginary();
    }

    /**
     * Creates a matrix that is the same type and shape
     *
     * @return New matrix
     */
    T createLike();
}
