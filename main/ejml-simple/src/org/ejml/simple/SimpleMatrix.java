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
import org.ejml.dense.row.*;
import org.ejml.ops.DConvertMatrixStruct;
import org.ejml.ops.FConvertMatrixStruct;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>
 * {@link SimpleMatrix} is a wrapper around a primitive matrix type
 * (for example, {@link DMatrixRMaj} or {@link FMatrixSparseCSC}) that provides an
 * easy to use object oriented interface for performing matrix operations. It is designed to be
 * more accessible to novice programmers and provide a way to rapidly code up solutions by simplifying
 * memory management and providing easy to use functions.
 * </p>
 *
 * <p>
 * Most functions in SimpleMatrix do not modify the original matrix. Instead they
 * create a new SimpleMatrix instance which is modified and returned. This greatly simplifies memory
 * management and writing of code in general. It also allows operations to be chained, as is shown
 * below:<br>
 * <br>
 * {@code SimpleMatrix K = P.mult(H.transpose().mult(S.invert()));}
 * </p>
 *
 * <p>
 * Working with both a primitive matrix and SimpleMatrix in the same code base is easy.
 * To access the internal Matrix in a SimpleMatrix simply call {@link SimpleMatrix#getMatrix()}.
 * To turn a Matrix into a SimpleMatrix use {@link SimpleMatrix#wrap(org.ejml.data.Matrix)}. Not
 * all operations in EJML are provided for SimpleMatrix, but can be accessed by extracting the internal
 * matrix.
 * </p>
 *
 * <p>
 * The object oriented approach used in SimpleMatrix was originally inspired by
 * <a href=http://math.nist.gov/javanumerics/jama/>JAMA</a>.
 * </p>
 *
 * <h3>Extending</h3>
 * <p>
 * SimpleMatrix contains a list of narrowly focused functions for linear algebra. To harness
 * the functionality for another application and to the number of functions it supports it is recommended
 * that one extends {@link SimpleBase} instead. This way the returned matrix type's of SimpleMatrix functions
 * will be of the appropriate types. See StatisticsMatrix inside of the examples directory.
 * </p>
 *
 * <p>
 * If SimpleMatrix is extended then the protected function {@link #createMatrix} should be extended and return
 * the child class. The results of SimpleMatrix operations will then be of the correct matrix type.
 * </p>
 *
 * <h3>Performance</h3>
 * <p>
 * The disadvantage of using this class is that it is more resource intensive, since
 * it creates a new matrix each time an operation is performed. This makes the JavaVM work harder and
 * Java automatically initializes the matrix to be all zeros. Typically operations on small matrices
 * or operations that have a runtime linear with the number of elements are the most affected. More
 * computationally intensive operations have only a slight unnoticeable performance loss. MOST PEOPLE
 * SHOULD NOT WORRY ABOUT THE SLIGHT LOSS IN PERFORMANCE.
 * </p>
 *
 * <p>
 * It is hard to judge how significant the performance hit will be in general. Often the performance
 * hit is insignificant since other parts of the application are more processor intensive or the bottle
 * neck is a more computationally complex operation. The best approach is benchmark and then optimize the code.
 * </p>
 *
 * <h3>Creating matrices</h3>
 * <table>
 *     <tr><th>Method</th><th>Description</th></tr>
 *     <tr><td>{@link #SimpleMatrix(int, int, Class)}</td>
 *     <td>Create a matrix filled with zeros with the specified internal type.</td></tr>
 *     <tr><td>{@link #SimpleMatrix(int, int, MatrixType)}</td>
 *     <td>Create a matrix filled with zeros with the specified internal matrix type.</td></tr>
 *     <tr><td>{@link #SimpleMatrix(int, int)}</td>
 *     <td>Create a matrix filled with zeros.</td></tr>
 *     <tr><td>{@link #SimpleMatrix(int, int, boolean, double...)}</td>
 *     <td>Create a matrix with the provided double values, in either row-major or column-major order.</td></tr>
 *     <tr><td>{@link #SimpleMatrix(int, int, boolean, float...)}</td>
 *     <td>Create a matrix with the provided float values, in either row-major or column-major order.</td></tr>
 *     <tr><td>{@link #SimpleMatrix(double[][])}</td>
 *     <td>Create a matrix from a 2D double array.</td></tr>
 *     <tr><td>{@link #SimpleMatrix(float[][])}</td>
 *     <td>Create a matrix from a 2D float array.</td></tr>
 *     <tr><td>{@link #SimpleMatrix(double[])}</td>
 *     <td>Create a column vector from a 1D double array.</td></tr>
 *     <tr><td>{@link #SimpleMatrix(float[])}</td>
 *     <td>Create a column vector from a 1D float array.</td></tr>
 *     <tr><td>{@link #SimpleMatrix(Matrix)}</td>
 *     <td>Create a matrix copying the provided Matrix.</td></tr>
 *     <tr><td>{@link #SimpleMatrix(SimpleMatrix)}</td>
 *     <td>Create a matrix copying the provided SimpleMatrix.</td></tr>
 *     <tr><td>{@link #wrap(Matrix)}</td>
 *     <td>Create a matrix wrapping the provided Matrix.</td></tr>
 *     <tr><td>{@link #filled(int, int, double)}</td>
 *     <td>Create a matrix filled with the specified value.</td></tr>
 *     <tr><td>{@link #ones(int, int)}</td>
 *     <td>Create a matrix filled with ones.</td></tr>
 *     <tr><td>{@link #diag(double...)}</td>
 *     <td>Create a diagonal matrix.</td></tr>
 *     <tr><td>{@link #diag(Class, double...)}</td>
 *     <td>Create a diagonal matrix with the specified internal type.</td></tr>
 *     <tr><td>{@link #identity(int)}</td>
 *     <td>Create an identity matrix.</td></tr>
 *     <tr><td>{@link #identity(int, Class)}</td>
 *     <td>Create an identity matrix with the specified internal type.</td></tr>
 *     <tr><td>{@link #random(int, int)}</td>
 *     <td>Create a random {@link DMatrixRMaj} with values drawn from a continuous uniform distribution on the
 *         unit interval.</td></tr>
 *     <tr><td>{@link #random_DDRM(int, int, double, double, Random)}</td>
 *     <td>Create a random {@link DMatrixRMaj} with values drawn from a continuous uniform distribution using the
 *         provided random number generator.</td></tr>
 *     <tr><td>{@link #random_DDRM(int, int)}</td>
 *     <td>Create a random {@link DMatrixRMaj} with values drawn from a continuous uniform distribution on the
 *         unit interval.</td></tr>
 *     <tr><td>{@link #random_FDRM(int, int, float, float, Random)}</td>
 *     <td>Create a random {@link FMatrixRMaj} with values drawn from a continuous uniform distribution using the
 *         provided random number generator.</td></tr>
 *     <tr><td>{@link #random_FDRM(int, int)}</td>
 *     <td>Create a random {@link FMatrixRMaj} with values drawn from a continuous uniform distribution on the
 *         unit interval.</td></tr>
 *     <tr><td>{@link #random_ZDRM(int, int, double, double, Random)}</td>
 *     <td>Create a random {@link ZMatrixRMaj} with values drawn from a continuous uniform distribution using the
 *         provided random number generator.</td></tr>
 *     <tr><td>{@link #random_ZDRM(int, int)}</td>
 *     <td>Create a random {@link ZMatrixRMaj} with values drawn from a continuous uniform distribution on the
 *         unit interval.</td></tr>
 *     <tr><td>{@link #random_CDRM(int, int, float, float, Random)}</td>
 *     <td>Create a random {@link CMatrixRMaj} with values drawn from a continuous uniform distribution using the
 *         provided random number generator.</td></tr>
 *     <tr><td>{@link #random_CDRM(int, int)}</td>
 *     <td>Create a random {@link CMatrixRMaj} with values drawn from a continuous uniform distribution on the
 *         unit interval.</td></tr>
 *     <tr><td>{@link #randomNormal(SimpleMatrix, Random)}</td>
 *     <td>Create a random vector drawn from a multivariate normal distribution
 *         with the specified covariance.</td></tr>
 *     <tr><td>{@link #createLike()}</td>
 *     <td>Create a matrix with the same shape and internal type as this matrix.</td></tr>
 *     <tr><td>{@link #copy()}</td>
 *     <td>Create a copy of this matrix.</td></tr>
 * </table>
 *
 * <h3>Getting elements, rows and columns</h3>
 * <table>
 *     <tr><th>Method</th><th>Description</th></tr>
 *     <tr><td>{@link #get(int)}</td>
 *     <td>Get the value of the {@code i}<sup>th</sup> entry in row-major order.</td></tr>
 *     <tr><td>{@link #get(int, int)}</td>
 *     <td>Get the value of the {@code i,j}<sup>th</sup> entry.</td></tr>
 *     <tr><td>{@link #get(int, int, Complex_F64)}</td>
 *     <td>Get the value of the {@code i,j}<sup>th</sup> entry as a complex number.</td></tr>
 *     <tr><td>{@link #getReal(int, int)}</td>
 *     <td>Get the real component of the {@code i,j}<sup>th</sup> entry.</td></tr>
 *     <tr><td>{@link #getImaginary(int, int)}</td>
 *     <td>Get the imaginary component of the {@code i,j}<sup>th</sup> entry.</td></tr>
 *     <tr><td>{@link #getImag(int, int)}</td>
 *     <td>Alias for {@link #getImaginary(int, int)}</td></tr>
 *     <tr><td>{@link #getRow(int)}</td>
 *     <td>Get the {@code i}<sup>th</sup> row.</td></tr>
 *     <tr><td>{@link #getColumn(int)}</td>
 *     <td>Get the {@code j}<sup>th</sup> column.</td></tr>
 *     <tr><td>{@link #getRows(int, int)}</td>
 *     <td>Extract the specified rows.</td></tr>
 *     <tr><td>{@link #getColumns(int, int)}</td>
 *     <td>Extract the specified columns.</td></tr>
 *     <tr><td>{@link #extractVector(boolean, int)}</td>
 *     <td>Extract the specified row or column vector.</td></tr>
 *     <tr><td>{@link #extractMatrix(int, int, int, int)}</td>
 *     <td>Extract the specified submatrix.</td></tr>
 *     <tr><td>{@link #diag()}</td>
 *     <td>Extract the matrix diagonal, or construct a diagonal matrix from a vector.</td></tr>
 * </table>
 *
 * <h3>Setting elements, rows and columns</h3>
 * <table>
 *     <tr><th>Method</th><th>Description</th></tr>
 *     <tr><td>{@link #set(int, double)}</td>
 *     <td>Set the value of the {@code i}<sup>th</sup> entry in row-major order.</td></tr>
 *     <tr><td>{@link #set(int, int, double)}</td>
 *     <td>Set the value of the {@code i,j}<sup>th</sup> entry.</td></tr>
 *     <tr><td>{@link #set(int, int, Complex_F64)}</td>
 *     <td>Set the value of the {@code i,j}<sup>th</sup> entry as a complex number.</td></tr>
 *     <tr><td>{@link #set(int, int, double, double)}</td>
 *     <td>Set the real and imaginary components of the {@code i,j}<sup>th</sup> entry.</td></tr>
 *     <tr><td>{@link #setRow(int, ConstMatrix)}</td>
 *     <td>Set the {@code i}<sup>th</sup> row.</td></tr>
 *     <tr><td>{@link #setRow(int, int, double...)}</td>
 *     <td>Set the values in the {@code i}<sup>th</sup> row.</td></tr>
 *     <tr><td>{@link #setColumn(int, ConstMatrix)}</td>
 *     <td>Set the {@code j}<sup>th</sup> column.</td></tr>
 *     <tr><td>{@link #setColumn(int, int, double...)}</td>
 *     <td>Set the values in the {@code j}<sup>th</sup> column.</td></tr>
 *     <tr><td>{@link #setTo(SimpleBase)}</td>
 *     <td>Set the elements of this matrix to be equal to elements from another matrix.</td></tr>
 *     <tr><td>{@link #insertIntoThis(int, int, SimpleBase)}</td>
 *     <td>Insert values from another matrix, starting in position {@code i,j}.</td></tr>
 *     <tr><td>{@link #fill(double)}</td>
 *     <td>Set all elements of this matrix to be equal to specified value.</td></tr>
 *     <tr><td>{@link #fillComplex(double, double)}</td>
 *     <td>Set all elements of this matrix to be equal to specified complex value.</td></tr>
 *     <tr><td>{@link #zero()}</td>
 *     <td>Set all elements of this matrix to zero.</td></tr>
 * </table>
 *
 * <h3>Basic operations</h3>
 * <table>
 *     <tr><th>Method</th><th>Description</th></tr>
 *     <tr><td>{@link #plus(double)}</td>
 *     <td>Add a scalar value.</td></tr>
 *     <tr><td>{@link #plusComplex(double, double)}</td>
 *     <td>Add a complex scalar value.</td></tr>
 *     <tr><td>{@link #plus(ConstMatrix)}</td>
 *     <td>Add another matrix.</td></tr>
 *     <tr><td>{@link #plus(double, ConstMatrix)}</td>
 *     <td>Add another matrix, first applying the specified scale factor.</td></tr>
 *     <tr><td>{@link #minus(double)}</td>
 *     <td>Subtract a scalar value.</td></tr>
 *     <tr><td>{@link #minusComplex(double, double)}</td>
 *     <td>Subtract a complex scalar value.</td></tr>
 *     <tr><td>{@link #minus(ConstMatrix)}</td>
 *     <td>Subtract another matrix.</td></tr>
 *     <tr><td>{@link #scale(double)}</td>
 *     <td>Multiply by a scalar value.</td></tr>
 *     <tr><td>{@link #scaleComplex(double, double)}</td>
 *     <td>Multiply by a complex scalar value.</td></tr>
 *     <tr><td>{@link #divide(double)}</td>
 *     <td>Divided by a scalar value.</td></tr>
 *     <tr><td>{@link #mult(ConstMatrix)}</td>
 *     <td>Multiply with another matrix.</td></tr>
 *     <tr><td>{@link #dot(ConstMatrix)}</td>
 *     <td>Calculate the dot product with another vector.</td></tr>
 *     <tr><td>{@link #negative()}</td>
 *     <td>Get the negative of each entry.</td></tr>
 *     <tr><td>{@link #real()}</td>
 *     <td>Get the real component of each entry.</td></tr>
 *     <tr><td>{@link #imaginary()}</td>
 *     <td>Get the imaginary component of each entry.</td></tr>
 *     <tr><td>{@link #imag()}</td>
 *     <td>Alias for {@link #imaginary()}.</td></tr>
 *     <tr><td>{@link #magnitude()}</td>
 *     <td>Get the imaginary component of each entry.</td></tr>
 *     <tr><td>{@link #transpose()}</td>
 *     <td>Get the transpose.</td></tr>
 *     <tr><td>{@link #transposeConjugate()}</td>
 *     <td>Get the conjugate transpose.</td></tr>
 *     <tr><td>{@link #equation(String, Object...)}</td>
 *     <td>Perform an equation in place on the matrix.</td></tr>
 * </table>
 *
 * <h3>Elementwise operations</h3>
 * <table>
 *     <tr><th>Method</th><th>Description</th></tr>
 *     <tr><td>{@link #elementMult(ConstMatrix)}</td>
 *     <td>Perform element by element multiplication with another matrix.</td></tr>
 *     <tr><td>{@link #elementDiv(ConstMatrix)}</td>
 *     <td>Perform element by element division with another matrix.</td></tr>
 *     <tr><td>{@link #elementPower(double)}</td>
 *     <td>Raise each entry to the specified power.</td></tr>
 *     <tr><td>{@link #elementPower(ConstMatrix)}</td>
 *     <td>Raise each entry to the corresponding power in another matrix.</td></tr>
 *     <tr><td>{@link #elementExp()}</td>
 *     <td>Compute the exponent of each entry.</td></tr>
 *     <tr><td>{@link #elementLog()}</td>
 *     <td>Compute the logarithm of each entry.</td></tr>
 *     <tr><td>{@link #elementOp(SimpleOperations.ElementOpReal)}</td>
 *     <td>Apply the specified real-valued function to each entry.</td></tr>
 *     <tr><td>{@link #elementOp(SimpleOperations.ElementOpComplex)}</td>
 *     <td>Apply the specified complex-valued function to each entry.</td></tr>
 * </table>
 *
 * <h3>Aggregations</h3>
 * <table>
 *     <tr><th>Method</th><th>Description</th></tr>
 *     <tr><td>{@link #elementSum()}</td>
 *     <td>Compute the sum of all elements of this matrix.</td></tr>
 *     <tr><td>{@link #elementSumComplex()}</td>
 *     <td>Compute the sum of all elements of a complex matrix.</td></tr>
 *     <tr><td>{@link #elementMax()}</td>
 *     <td>Compute the maximum of all elements of this matrix.</td></tr>
 *     <tr><td>{@link #elementMaxAbs()}</td>
 *     <td>Compute the maximum absolute value of all elements of this matrix.</td></tr>
 *     <tr><td>{@link #elementMin()}</td>
 *     <td>Compute the minimum of all elements of this matrix.</td></tr>
 *     <tr><td>{@link #elementMinAbs()}</td>
 *     <td>Compute the minimum absolute value of all elements of this matrix.</td></tr>
 * </table>
 *
 * <h3>Linear algebra</h3>
 * <table>
 *     <tr><th>Method</th><th>Description</th></tr>
 *     <tr><td>{@link #solve(ConstMatrix)}</td>
 *     <td>Solve the equation {@code Ax = b}.</td></tr>
 *     <tr><td>{@link #conditionP2()}</td>
 *     <td>Compute the matrix condition number.</td></tr>
 *     <tr><td>{@link #invert()}</td>
 *     <td>Compute the matrix inverse.</td></tr>
 *     <tr><td>{@link #pseudoInverse()}</td>
 *     <td>Compute the Moore-Penrose pseudo-inverse.</td></tr>
 *     <tr><td>{@link #determinant()}</td>
 *     <td>Compute the determinant.</td></tr>
 *     <tr><td>{@link #determinantComplex()}</td>
 *     <td>Compute the determinant of a complex matrix.</td></tr>
 *     <tr><td>{@link #trace()}</td>
 *     <td>Compute the trace.</td></tr>
 *     <tr><td>{@link #traceComplex()}</td>
 *     <td>Compute the trace of a complex matrix.</td></tr>
 *     <tr><td>{@link #normF()}</td>
 *     <td>Compute the Frobenius norm.</td></tr>
 *     <tr><td>{@link #eig()}</td>
 *     <td>Compute the eigenvalue decomposition.</td></tr>
 *     <tr><td>{@link #svd()}</td>
 *     <td>Compute the singular value decomposition.</td></tr>
 *     <tr><td>{@link #svd(boolean)}</td>
 *     <td>Compute the singular value decomposition in compact or full format.</td></tr>
 * </table>
 *
 * <h3>Combining matrices</h3>
 * <table>
 *     <tr><th>Method</th><th>Description</th></tr>
 *     <tr><td>{@link #combine(int, int, ConstMatrix)}</td>
 *     <td>Combine with another matrix.</td></tr>
 *     <tr><td>{@link #concatRows(ConstMatrix...)}</td>
 *     <td>Concatenate vertically with one or more other matrices.</td></tr>
 *     <tr><td>{@link #concatColumns(ConstMatrix...)}</td>
 *     <td>Concatenate horizontally with one or more other matrices.</td></tr>
 *     <tr><td>{@link #kron(ConstMatrix)}</td>
 *     <td>Compute the Kronecker product with another matrix.</td></tr>
 * </table>
 *
 * <h3>Matrix properties</h3>
 * <table>
 *     <tr><th>Method</th><th>Description</th></tr>
 *     <tr><td>{@link #getNumRows()}</td>
 *     <td>Get the number of rows.</td></tr>
 *     <tr><td>{@link #getNumCols()}</td>
 *     <td>Get the number of columns.</td></tr>
 *     <tr><td>{@link #getNumElements()}</td>
 *     <td>Get the number of elements.</td></tr>
 *     <tr><td>{@link #bits()}</td>
 *     <td>Get the size of the internal array elements (32 or 64).</td></tr>
 *     <tr><td>{@link #isVector()}</td>
 *     <td>Check if this matrix is a vector.</td></tr>
 *     <tr><td>{@link #isIdentical(ConstMatrix, double)}</td>
 *     <td>Check if this matrix is the same as another matrix, up to the specified tolerance.</td></tr>
 *     <tr><td>{@link #hasUncountable()}</td>
 *     <td>Check if any of the matrix elements are NaN or infinite.</td></tr>
 * </table>
 *
 * <h3>Converting and reshaping</h3>
 * <table>
 *     <tr><th>Method</th><th>Description</th></tr>
 *     <tr><td>{@link #convertToComplex()}</td>
 *     <td>Convert to a complex matrix.</td></tr>
 *     <tr><td>{@link #convertToDense()}</td>
 *     <td>Convert to a dense matrix.</td></tr>
 *     <tr><td>{@link #convertToSparse()}</td>
 *     <td>Convert to a sparse matrix.</td></tr>
 *     <tr><td>{@link #reshape(int, int)}</td>
 *     <td>Change the number of rows and columns.</td></tr>
 * </table>
 *
 * <h3>Accessing the internal matrix</h3>
 * <table>
 *     <tr><th>Method</th><th>Description</th></tr>
 *     <tr><td>{@link #getType()}</td>
 *     <td>Get the type of the wrapped matrix.</td></tr>
 *     <tr><td>{@link #getMatrix()}</td>
 *     <td>Get the wrapped matrix.</td></tr>
 *     <tr><td>{@link #getDDRM()}</td>
 *     <td>Get the wrapped matrix as a {@link DMatrixRMaj}.</td></tr>
 *     <tr><td>{@link #getFDRM()}</td>
 *     <td>Get the wrapped matrix as a {@link FMatrixRMaj}.</td></tr>
 *     <tr><td>{@link #getZDRM()}</td>
 *     <td>Get the wrapped matrix as a {@link ZMatrixRMaj}.</td></tr>
 *     <tr><td>{@link #getCDRM()}</td>
 *     <td>Get the wrapped matrix as a {@link CMatrixRMaj}.</td></tr>
 *     <tr><td>{@link #getDSCC()}</td>
 *     <td>Get the wrapped matrix as a {@link DMatrixSparseCSC}.</td></tr>
 *     <tr><td>{@link #getFSCC()}</td>
 *     <td>Get the wrapped matrix as a {@link FMatrixSparseCSC}.</td></tr>
 * </table>
 *
 * <h3>Loading and saving</h3>
 * <table>
 *     <tr><th>Method</th><th>Description</th></tr>
 *     <tr><td>{@link #loadCSV(String)}</td>
 *     <td>Load a matrix from a CSV file.</td></tr>
 *     <tr><td>{@link #saveToFileCSV(String)}</td>
 *     <td>Save this matrix to a CSV file.</td></tr>
 *     <tr><td>{@link #saveToMatrixMarket(String)}</td>
 *     <td>Save this matrix in matrix market format.</td></tr>
 * </table>
 *
 * <h3>Miscellaneous</h3>
 * <table>
 *     <tr><th>Method</th><th>Description</th></tr>
 *     <tr><td>{@link #iterator(boolean, int, int, int, int)}</td>
 *     <td>Create an iterator for traversing a submatrix.</td></tr>
 *     <tr><td>{@link #getIndex(int, int)}</td>
 *     <td>Get the row-major index corresponding to {@code i,j}.</td></tr>
 *     <tr><td>{@link #isInBounds(int, int)}</td>
 *     <td>Check if the indices {@code i,j} are in bounds.</td></tr>
 *     <tr><td>{@link #toString()}</td>
 *     <td>Get the string representation of the matrix.</td></tr>
 *     <tr><td>{@link #toArray2()}</td>
 *     <td>Convert the matrix to a 2D array of doubles.</td></tr>
 *     <tr><td>{@link #print()}</td>
 *     <td>Print the matrix to standard out.</td></tr>
 *     <tr><td>{@link #print(String)}</td>
 *     <td>Print the matrix to standard out using the specified floating point format.</td></tr>
 *     <tr><td>{@link #printDimensions()}</td>
 *     <td>Print the number of rows and columns.</td></tr>
 * </table>
 *
 * @author Peter Abeles
 */
public class SimpleMatrix extends SimpleBase<SimpleMatrix> {

    /**
     * A simplified way to reference the last row or column in the matrix for some functions.
     */
    public static final int END = Integer.MAX_VALUE;

    /**
     * <p>
     * Creates a new matrix which has the same value as the matrix encoded in the
     * provided array. The input matrix's format can either be row-major or
     * column-major.
     * </p>
     *
     * <p>
     * Note that 'data' is a variable argument type, so either 1D arrays or a set of numbers can be
     * passed in:<br>
     * SimpleMatrix a = new SimpleMatrix(2,2,true,new double[]{1,2,3,4});<br>
     * SimpleMatrix b = new SimpleMatrix(2,2,true,1,2,3,4);<br>
     * <br>
     * Both are equivalent.
     * </p>
     *
     * @param numRows The number of rows.
     * @param numCols The number of columns.
     * @param rowMajor If the array is encoded in a row-major or a column-major format.
     * @param data The formatted 1D array. Not modified.
     * @see DMatrixRMaj#DMatrixRMaj(int, int, boolean, double...)
     */
    public SimpleMatrix( int numRows, int numCols, boolean rowMajor, double... data ) {
        setMatrix(new DMatrixRMaj(numRows, numCols, rowMajor, data));
    }

    /**
     * <p>
     * Creates a new matrix which has the same value as the matrix encoded in the
     * provided array. The input matrix's format can either be row-major or
     * column-major.
     * </p>
     *
     * <p>
     * Note that 'data' is a variable argument type, so either 1D arrays or a set of numbers can be
     * passed in:<br>
     * SimpleMatrix a = new SimpleMatrix(2,2,true,new float[]{1,2,3,4});<br>
     * SimpleMatrix b = new SimpleMatrix(2,2,true,1,2,3,4);<br>
     * <br>
     * Both are equivalent.
     * </p>
     *
     * @param numRows The number of rows.
     * @param numCols The number of columns.
     * @param rowMajor If the array is encoded in a row-major or a column-major format.
     * @param data The formatted 1D array. Not modified.
     * @see FMatrixRMaj#FMatrixRMaj(int, int, boolean, float...)
     */
    public SimpleMatrix( int numRows, int numCols, boolean rowMajor, float... data ) {
        setMatrix(new FMatrixRMaj(numRows, numCols, rowMajor, data));
    }

    /**
     * <p>
     * Creates a matrix with the values and shape defined by the 2D array 'data'.
     * It is assumed that 'data' has a row-major formatting:<br>
     * <br>
     * data[ row ][ column ]
     * </p>
     *
     * @param data 2D array representation of the matrix. Not modified.
     * @see DMatrixRMaj#DMatrixRMaj(double[][])
     */
    public SimpleMatrix( double[][] data ) {
        setMatrix(new DMatrixRMaj(data));
    }

    /**
     * <p>
     * Creates a matrix with the values and shape defined by the 2D array 'data'.
     * It is assumed that 'data' has a row-major formatting:<br>
     * <br>
     * data[ row ][ column ]
     * </p>
     *
     * @param data 2D array representation of the matrix. Not modified.
     * @see FMatrixRMaj#FMatrixRMaj(float[][])
     */
    public SimpleMatrix( float[][] data ) {
        setMatrix(new FMatrixRMaj(data));
    }

    /**
     * Creates a column vector with the values and shape defined by the 1D array 'data'.
     *
     * @param data 1D array representation of the vector. Not modified.
     */
    public SimpleMatrix( double[] data ) {
        setMatrix(new DMatrixRMaj(data.length, 1, true, data));
    }

    /**
     * Creates a column vector with the values and shape defined by the 1D array 'data'.
     *
     * @param data 1D array representation of the vector. Not modified.
     */
    public SimpleMatrix( float[] data ) {
        setMatrix(new FMatrixRMaj(data.length, 1, true, data));
    }

    /**
     * Creates a new matrix that is initially set to zero with the specified dimensions. This will wrap a
     * {@link DMatrixRMaj}.
     *
     * @param numRows The number of rows in the matrix.
     * @param numCols The number of columns in the matrix.
     */
    public SimpleMatrix( int numRows, int numCols ) {
        setMatrix(new DMatrixRMaj(numRows, numCols));
    }

    /**
     * Creates a new matrix that is initially set to zero with the specified dimensions and type.
     *
     * @param numRows The number of rows in the matrix.
     * @param numCols The number of columns in the matrix.
     * @param type The matrix type
     */
    public SimpleMatrix( int numRows, int numCols, Class<?> type ) {
        this(numRows, numCols, MatrixType.lookup(type));
    }

    /**
     * Creates a new matrix that is initially set to zero with the specified dimensions and matrix type.
     *
     * @param numRows The number of rows in the matrix.
     * @param numCols The number of columns in the matrix.
     * @param type The matrix type
     */
    public SimpleMatrix( int numRows, int numCols, MatrixType type ) {
        switch (type) {
            case DDRM -> setMatrix(new DMatrixRMaj(numRows, numCols));
            case FDRM -> setMatrix(new FMatrixRMaj(numRows, numCols));
            case ZDRM -> setMatrix(new ZMatrixRMaj(numRows, numCols));
            case CDRM -> setMatrix(new CMatrixRMaj(numRows, numCols));
            case DSCC -> setMatrix(new DMatrixSparseCSC(numRows, numCols));
            case FSCC -> setMatrix(new FMatrixSparseCSC(numRows, numCols));
            default -> throw new RuntimeException("Unknown matrix type");
        }
    }

    /**
     * Creates a new SimpleMatrix which is identical to the original.
     *
     * @param orig The matrix which is to be copied. Not modified.
     */
    public SimpleMatrix( SimpleMatrix orig ) {
        setMatrix(orig.mat.copy());
    }

    /**
     * Creates a new SimpleMatrix which is a copy of the Matrix.
     *
     * @param orig The original matrix whose value is copied. Not modified.
     */
    public SimpleMatrix( Matrix orig ) {
        Matrix mat;
        if (orig instanceof DMatrixRBlock) {
            var a = new DMatrixRMaj(orig.getNumRows(), orig.getNumCols());
            DConvertMatrixStruct.convert((DMatrixRBlock)orig, a);
            mat = a;
        } else if (orig instanceof FMatrixRBlock) {
            var a = new FMatrixRMaj(orig.getNumRows(), orig.getNumCols());
            FConvertMatrixStruct.convert((FMatrixRBlock)orig, a);
            mat = a;
        } else {
            mat = orig.copy();
        }
        setMatrix(mat);
    }

    /**
     * Constructor for internal library use only. Nothing is configured and is intended for serialization.
     */
    protected SimpleMatrix() {}

    /**
     * Creates a new SimpleMatrix with the specified Matrix used as its internal matrix. This means
     * that the reference is saved and calls made to the returned SimpleMatrix will modify the passed in Matrix.
     *
     * @param internalMat The internal Matrix of the returned SimpleMatrix. Will be modified.
     */
    public static SimpleMatrix wrap( Matrix internalMat ) {
        var ret = new SimpleMatrix();
        ret.setMatrix(internalMat);
        return ret;
    }

    /**
     * Creates a new matrix filled with the specified value. This will wrap a {@link DMatrixRMaj}.
     *
     * @param numRows The number of rows in the matrix.
     * @param numCols The number of columns in the matrix.
     * @param a The value to fill the matrix with.
     * @return A matrix filled with the value a.
     */
    public static SimpleMatrix filled( int numRows, int numCols, double a ) {
        var res = new SimpleMatrix(numRows, numCols);
        res.fill(a);
        return res;
    }

    /**
     * Creates a new matrix filled with ones. This will wrap a {@link DMatrixRMaj}.
     *
     * @param numRows The number of rows in the matrix.
     * @param numCols The number of columns in the matrix.
     * @return A matrix of ones.
     */
    public static SimpleMatrix ones( int numRows, int numCols ) {
        return filled(numRows, numCols, 1);
    }

    /**
     * Creates a new identity matrix with the specified size. This will wrap a {@link DMatrixRMaj}.
     *
     * @param width The width and height of the matrix.
     * @return An identity matrix.
     * @see CommonOps_DDRM#identity(int)
     */
    public static SimpleMatrix identity( int width ) {
        return identity(width, DMatrixRMaj.class);
    }

    /**
     * Creates a new identity matrix with the specified size and type.
     *
     * @param width The width and height of the matrix.
     * @param type The matrix type
     * @return An identity matrix.
     */
    public static SimpleMatrix identity( int width, Class<?> type ) {
        var ret = new SimpleMatrix(width, width, type);
        ret.ops.setIdentity(ret.mat);
        return ret;
    }

    /**
     * <p>
     * Creates a matrix where all but the diagonal elements are zero. The values
     * of the diagonal elements are specified by the parameter 'vals'. This will wrap a {@link DMatrixRMaj}.
     * </p>
     *
     * <p>
     * To extract the diagonal elements from a matrix see {@link #diag()}.
     * </p>
     *
     * @param vals The values of the diagonal elements.
     * @return A diagonal matrix.
     * @see CommonOps_DDRM#diag(double...)
     */
    public static SimpleMatrix diag( double... vals ) {
        return wrap(CommonOps_DDRM.diag(vals));
    }

    /**
     * Creates a matrix where all but the diagonal elements are zero. The values
     * of the diagonal elements are specified by the parameter 'vals'.
     *
     * @param type The matrix type
     * @param vals The values of the diagonal elements.
     * @return A diagonal matrix.
     */
    public static SimpleMatrix diag( Class<?> type, double... vals ) {
        var M = new SimpleMatrix(vals.length, vals.length, type);
        for (int i = 0; i < vals.length; i++) {
            M.set(i, i, vals[i]);
        }
        return M;
    }

    /**
     * Creates a random matrix with values drawn from the continuous uniform distribution from minValue (inclusive) to
     * maxValue (exclusive). This will wrap a {@link DMatrixRMaj}.
     *
     * @param numRows The number of rows in the new matrix
     * @param numCols The number of columns in the new matrix
     * @param minValue Lower bound
     * @param maxValue Upper bound
     * @param rand The random number generator that's used to fill the matrix.
     * @return The new random matrix.
     * @see RandomMatrices_DDRM#fillUniform(DMatrixD1, double, double, java.util.Random)
     */
    public static SimpleMatrix random_DDRM( int numRows, int numCols, double minValue, double maxValue, Random rand ) {
        var ret = new SimpleMatrix(numRows, numCols);
        RandomMatrices_DDRM.fillUniform((DMatrixRMaj)ret.mat, minValue, maxValue, rand);
        return ret;
    }

    /**
     * Creates a random matrix with values drawn from the continuous uniform distribution from 0.0 (inclusive) to
     * 1.0 (exclusive).
     *
     * @param numRows The number of rows in the new matrix
     * @param numCols The number of columns in the new matrix
     * @see #random_DDRM(int, int)
     */
    public static SimpleMatrix random( int numRows, int numCols ) {
        return random_DDRM(numRows, numCols, 0.0, 1.0, ThreadLocalRandom.current());
    }

    /**
     * Creates a random matrix with values drawn from the continuous uniform distribution from 0.0 (inclusive) to
     * 1.0 (exclusive).
     * The random number generator is {@link ThreadLocalRandom#current()}. This will wrap a {@link DMatrixRMaj}.
     *
     * @param numRows The number of rows in the new matrix
     * @param numCols The number of columns in the new matrix
     */
    public static SimpleMatrix random_DDRM( int numRows, int numCols ) {
        return random_DDRM(numRows, numCols, 0.0, 1.0, ThreadLocalRandom.current());
    }

    /**
     * Creates a random matrix with values drawn from the continuous uniform distribution from minValue (inclusive) to
     * maxValue (exclusive). This will wrap a {@link FMatrixRMaj}.
     *
     * @param numRows The number of rows in the new matrix
     * @param numCols The number of columns in the new matrix
     * @param minValue Lower bound
     * @param maxValue Upper bound
     * @param rand The random number generator that's used to fill the matrix.
     * @return The new random matrix.
     * @see RandomMatrices_FDRM#fillUniform(FMatrixD1, float, float, java.util.Random)
     */
    public static SimpleMatrix random_FDRM( int numRows, int numCols, float minValue, float maxValue, Random rand ) {
        var ret = new SimpleMatrix(numRows, numCols, FMatrixRMaj.class);
        RandomMatrices_FDRM.fillUniform((FMatrixRMaj)ret.mat, minValue, maxValue, rand);
        return ret;
    }

    /**
     * Creates a random matrix with values drawn from the continuous uniform distribution from 0.0 (inclusive) to
     * 1.0 (exclusive). The random number generator is {@link ThreadLocalRandom#current()}.
     * This will wrap a {@link FMatrixRMaj}.
     *
     * @param numRows The number of rows in the new matrix
     * @param numCols The number of columns in the new matrix
     */
    public static SimpleMatrix random_FDRM( int numRows, int numCols ) {
        return random_FDRM(numRows, numCols, 0.0f, 1.0f, ThreadLocalRandom.current());
    }

    /**
     * Creates a random matrix with real and complex components drawn from the continuous uniform distribution from
     * minValue (inclusive) to maxValue (exclusive). This will wrap a {@link ZMatrixRMaj}.
     *
     * @param numRows The number of rows in the new matrix
     * @param numCols The number of columns in the new matrix
     * @param minValue Lower bound
     * @param maxValue Upper bound
     * @param rand The random number generator that's used to fill the matrix.
     * @return The new random matrix.
     * @see RandomMatrices_ZDRM#fillUniform(ZMatrixD1, double, double, java.util.Random)
     */
    public static SimpleMatrix random_ZDRM( int numRows, int numCols, double minValue, double maxValue, Random rand ) {
        var ret = new SimpleMatrix(numRows, numCols, MatrixType.ZDRM);
        RandomMatrices_ZDRM.fillUniform((ZMatrixRMaj)ret.mat, minValue, maxValue, rand);
        return ret;
    }

    /**
     * Creates a random matrix with values drawn from the continuous uniform distribution from 0.0 (inclusive) to
     * 1.0 (exclusive). The random number generator is {@link ThreadLocalRandom#current()}.
     * This will wrap a {@link ZMatrixRMaj}.
     *
     * @param numRows The number of rows in the new matrix
     * @param numCols The number of columns in the new matrix
     */
    public static SimpleMatrix random_ZDRM( int numRows, int numCols ) {
        return random_ZDRM(numRows, numCols, 0.0, 1.0, ThreadLocalRandom.current());
    }

    /**
     * Creates a random matrix with real and complex components drawn from the continuous uniform distribution from
     * minValue (inclusive) to maxValue (exclusive). This will wrap a {@link CMatrixRMaj}.
     *
     * @param numRows The number of rows in the new matrix
     * @param numCols The number of columns in the new matrix
     * @param minValue Lower bound
     * @param maxValue Upper bound
     * @param rand The random number generator that's used to fill the matrix.
     * @return The new random matrix.
     * @see RandomMatrices_CDRM#fillUniform(CMatrixD1, float, float, java.util.Random)
     */
    public static SimpleMatrix random_CDRM( int numRows, int numCols, float minValue, float maxValue, Random rand ) {
        var ret = new SimpleMatrix(numRows, numCols, MatrixType.CDRM);
        RandomMatrices_CDRM.fillUniform((CMatrixRMaj)ret.mat, minValue, maxValue, rand);
        return ret;
    }

    /**
     * Creates a random matrix with values drawn from the continuous uniform distribution from 0.0 (inclusive) to
     * 1.0 (exclusive). The random number generator is {@link ThreadLocalRandom#current()}.
     * This will wrap a {@link CMatrixRMaj}.
     *
     * @param numRows The number of rows in the new matrix
     * @param numCols The number of columns in the new matrix
     */
    public static SimpleMatrix random_CDRM( int numRows, int numCols ) {
        return random_CDRM(numRows, numCols, 0.0f, 1.0f, ThreadLocalRandom.current());
    }

    /**
     * <p>
     * Creates a new vector which is drawn from a multivariate normal distribution with zero mean
     * and the provided covariance.
     * </p>
     *
     * @param covariance Covariance of the multivariate normal distribution
     * @param random The random number generator that's used to fill the matrix.
     * @return Vector randomly drawn from the distribution
     * @see CovarianceRandomDraw_DDRM
     */
    public static SimpleMatrix randomNormal( SimpleMatrix covariance, Random random ) {
        var found = new SimpleMatrix(covariance.numRows(), 1, covariance.getType());
        switch (found.getType()) {
            case DDRM -> {
                var draw = new CovarianceRandomDraw_DDRM(random, covariance.getMatrix());
                draw.next(found.getMatrix());
            }
            case FDRM -> {
                var draw = new CovarianceRandomDraw_FDRM(random, covariance.getMatrix());
                draw.next(found.getMatrix());
            }
            default -> throw new IllegalArgumentException("Matrix type is currently not supported");
        }

        return found;
    }

    @Override
    protected SimpleMatrix createMatrix( int numRows, int numCols, MatrixType type ) {
        return new SimpleMatrix(numRows, numCols, type);
    }

    @Override
    protected SimpleMatrix wrapMatrix( Matrix m ) {
        return new SimpleMatrix(m);
    }

    // TODO should this function be added back?  It makes the code hard to read when its used
//    /**
//     * <p>
//     * Performs one of the following matrix multiplication operations:<br>
//     * <br>
//     * c = a * b <br>
//     * c = a<sup>T</sup> * b <br>
//     * c = a * b <sup>T</sup><br>
//     * c = a<sup>T</sup> * b <sup>T</sup><br>
//     * <br>
//     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
//     * </p>
//     *
//     * @see CommonOps#mult(DMatrixRMaj, DMatrixRMaj, DMatrixRMaj)
//     * @see CommonOps#multTransA(DMatrixRMaj, DMatrixRMaj, DMatrixRMaj)
//     * @see CommonOps#multTransB(DMatrixRMaj, DMatrixRMaj, DMatrixRMaj)
//     * @see CommonOps#multTransAB(DMatrixRMaj, DMatrixRMaj, DMatrixRMaj)
//     *
//     * @param tranA If true matrix A is transposed.
//     * @param tranB If true matrix B is transposed.
//     * @param b A matrix that is n by bn. Not modified.
//     *
//     * @return The results of this operation.
//     */
//    public SimpleMatrix mult( boolean tranA , boolean tranB , SimpleMatrix b) {
//        SimpleMatrix ret;
//
//        if( tranA && tranB ) {
//            ret = createMatrix(mat.numCols,b.mat.numRows);
//            CommonOps.multTransAB(mat,b.mat,ret.mat);
//        } else if( tranA ) {
//            ret = createMatrix(mat.numCols,b.mat.numCols);
//            CommonOps.multTransA(mat,b.mat,ret.mat);
//        } else if( tranB ) {
//            ret = createMatrix(mat.numRows,b.mat.numRows);
//            CommonOps.multTransB(mat,b.mat,ret.mat);
//        }  else  {
//            ret = createMatrix(mat.numRows,b.mat.numCols);
//            CommonOps.mult(mat,b.mat,ret.mat);
//        }
//
//        return ret;
//    }

}
