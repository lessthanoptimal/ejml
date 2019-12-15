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

package org.ejml.simple;

import org.ejml.UtilEjml;
import org.ejml.data.*;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.equation.Equation;
import org.ejml.ops.ConvertDMatrixStruct;
import org.ejml.ops.ConvertFMatrixStruct;
import org.ejml.ops.MatrixIO;
import org.ejml.simple.ops.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Parent of {@link SimpleMatrix} implements all the standard matrix operations and uses
 * generics to allow the returned matrix type to be changed.  This class should be extended
 * instead of SimpleMatrix.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public abstract class SimpleBase <T extends SimpleBase<T>> implements Serializable {

    static final long serialVersionUID = 2342556642L;

    /**
     * Internal matrix which this is a wrapper around.
     */
    protected Matrix mat;
    protected SimpleOperations ops;

    protected transient AutomaticSimpleMatrixConvert convertType = new AutomaticSimpleMatrixConvert();

    public SimpleBase( int numRows , int numCols ) {
        setMatrix(new DMatrixRMaj(numRows, numCols));
    }

    protected SimpleBase() {
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        convertType = new AutomaticSimpleMatrixConvert();
    }

    /**
     * Used internally for creating new instances of SimpleMatrix.  If SimpleMatrix is extended
     * by another class this function should be overridden so that the returned matrices are
     * of the correct type.
     *
     * @param numRows number of rows in the new matrix.
     * @param numCols number of columns in the new matrix.
     * @param type Type of matrix it should create
     * @return A new matrix.
     */
    protected abstract T createMatrix(int numRows, int numCols, MatrixType type);

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

    public DMatrixRMaj getDDRM() {
        return (DMatrixRMaj)mat;
    }

    public FMatrixRMaj getFDRM() {
        return (FMatrixRMaj)mat;
    }

    public ZMatrixRMaj getZDRM() {
        return (ZMatrixRMaj)mat;
    }

    public CMatrixRMaj getCDRM() {
        return (CMatrixRMaj)mat;
    }

    public DMatrixSparseCSC getDSCC() {
        return (DMatrixSparseCSC)mat;
    }

    public FMatrixSparseCSC getFSCC() {
        return (FMatrixSparseCSC)mat;
    }

    protected static SimpleOperations lookupOps( MatrixType type ) {
        switch( type ) {
            case DDRM: return new SimpleOperations_DDRM();
            case FDRM: return new SimpleOperations_FDRM();
            case ZDRM: return new SimpleOperations_ZDRM();
            case CDRM: return new SimpleOperations_CDRM();
            case DSCC: return new SimpleOperations_DSCC();
            case FSCC: return new SimpleOperations_FSCC();
        }
        throw new RuntimeException("Unknown Matrix Type. "+type);
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
        T ret = createMatrix(mat.getNumCols(),mat.getNumRows(), mat.getType());

        ops.transpose(mat,ret.mat);

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
     * @param B A matrix that is n by bn. Not modified.
     *
     * @return The results of this operation.
     */
    public T mult( T B ) {
        convertType.specify(this,B);

        // Look to see if there is a special function for handling this case
        if( this.mat.getType() != B.getType() ) {
            Method m = findAlternative("mult",mat,B.mat,convertType.commonType.getClassType());
            if( m != null ) {
                T ret = wrapMatrix(convertType.commonType.create(1,1));
                invoke(m,this.mat,B.mat,ret.mat);
                return ret;
            }
        }

        // Otherwise convert into a common matrix type if necessary
        T A = convertType.convert(this);
        B = convertType.convert(B);

        T ret = A.createMatrix(mat.getNumRows(),B.getMatrix().getNumCols(), A.getType());

        A.ops.mult(A.mat,B.mat,ret.mat);

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
        convertType.specify(this,B);
        T A = convertType.convert(this);
        B = convertType.convert(B);

        T ret = A.createMatrix(mat.getNumRows()*B.numRows(),mat.getNumCols()*B.numCols(), A.getType());

        A.ops.kron(A.mat,B.mat,ret.mat);

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
     * @param B m by n matrix. Not modified.
     *
     * @return The results of this operation.
     */
    public T plus( T B ) {
        convertType.specify(this,B);
        T A = convertType.convert(this);
        B = convertType.convert(B);

        T ret = A.createMatrix(mat.getNumRows(),mat.getNumCols(), A.getType());

        A.ops.plus(A.mat,B.mat,ret.mat);

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
     * @param B m by n matrix. Not modified.
     *
     * @return The results of this operation.
     */
    public T minus( T B ) {
        convertType.specify(this,B);
        T A = convertType.convert(this);
        B = convertType.convert(B);
        T ret = A.createLike();

        A.ops.minus(A.mat,B.mat,ret.mat);
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
        T ret = createLike();
        ops.minus(mat,b,ret.mat);
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
        T ret = createLike();
        ops.plus(mat,b,ret.mat);
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
     * @param B m by n matrix. Not modified.
     *
     * @return A matrix that contains the results.
     */
    public T plus( double beta , T B ) {
        convertType.specify(this,B);
        T A = convertType.convert(this);
        B = convertType.convert(B);

        T ret = A.createLike();
        A.ops.plus(A.mat,beta,B.mat,ret.mat);
        return ret;
    }

    /**
     * Computes the dot product (a.k.a. inner product) between this vector and vector 'v'.
     *
     * @param v The second vector in the dot product.  Not modified.
     * @return dot product
     */
    public double dot( T v ) {
        convertType.specify(this,v);
        T A = convertType.convert(this);
        v = convertType.convert(v);

        if( !isVector() ) {
            throw new IllegalArgumentException("'this' matrix is not a vector.");
        } else if( !v.isVector() ) {
            throw new IllegalArgumentException("'v' matrix is not a vector.");
        }

        return A.ops.dot(A.mat,v.getMatrix());
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
        T ret = createLike();
        ops.scale(mat,val,ret.getMatrix());
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
        T ret = createLike();
        ops.divide(mat,val,ret.getMatrix());
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
        T ret = createLike();

        if( !ops.invert(mat,ret.mat))
            throw new SingularMatrixException();
        if( ops.hasUncountable(ret.mat))
            throw new SingularMatrixException("Solution contains uncountable numbers");

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
        T ret = createLike();
        ops.pseudoInverse(mat,ret.mat);
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
     * @param B n by p matrix. Not modified.
     * @return The solution for 'x' that is n by p.
     */
    public T solve( T B )
    {
        convertType.specify(this,B);

        // Look to see if there is a special function for handling this case
        if( this.mat.getType() != B.getType() ) {
            Method m = findAlternative("solve",mat,B.mat,convertType.commonType.getClassType());
            if( m != null ) {
                T ret = wrapMatrix(convertType.commonType.create(1,1));
                invoke(m,this.mat,B.mat,ret.mat); // TODO handle boolean return from solve
                return ret;
            }
        }

        T A = convertType.convert(this);
        B = convertType.convert(B);

        T x = A.createMatrix(mat.getNumCols(),B.getMatrix().getNumCols(), A.getType());

        if( !A.ops.solve(A.mat,x.mat,B.mat))
            throw new SingularMatrixException();
        if( A.ops.hasUncountable(x.mat))
            throw new SingularMatrixException("Solution contains uncountable numbers");

        return x;
    }


    /**
     * Sets the elements in this matrix to be equal to the elements in the passed in matrix.
     * Both matrix must have the same dimension.
     *
     * @param a The matrix whose value this matrix is being set to.
     */
    public void set( T a ) {
        if( a.getType() == getType() )
            mat.set(a.getMatrix());
        else {
            setMatrix(a.mat.copy());
        }
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
    public void fill(double val ) {
        try {
            ops.fill(mat, val);
        } catch( ConvertToDenseException e) {
            convertToDense();
            fill(val);
        }
    }

    /**
     * Sets all the elements in the matrix equal to zero.
     *
     * @see CommonOps_DDRM#fill(DMatrixD1, double)
     */
    public void zero() {
        fill(0);
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
        return ops.normF(mat);
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
        return ops.conditionP2(mat);
    }

    /**
     * Computes the determinant of the matrix.
     *
     * @see CommonOps_DDRM#det(DMatrixRMaj)
     *
     * @return The determinant.
     */
    public double determinant() {
        double ret = ops.determinant(mat);
        if (UtilEjml.isUncountable(ret))
            return 0;
        return ret;
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
        return ops.trace(mat);
    }

    /**
     * <p>
     * Reshapes the matrix to the specified number of rows and columns.  If the total number of elements
     * is &le; number of elements it had before the data is saved.  Otherwise a new internal array is
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
        if( mat.getType().isFixed() ) {
            throw new IllegalArgumentException("Can't reshape a fixed sized matrix");
        } else {
            ((ReshapeMatrix)mat).reshape(numRows, numCols);
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
        ops.set(mat, row, col, value);
    }

    /**
     * Assigns an element a value based on its index in the internal array..
     *
     * @param index The matrix element that is being assigned a value.
     * @param value The element's new value.
     */
    public void set( int index , double value ) {
        if( mat.getType() == MatrixType.DDRM ) {
            ((DMatrixRMaj) mat).set(index, value);
        } else if( mat.getType() == MatrixType.FDRM ) {
            ((FMatrixRMaj) mat).set(index, (float)value);
        } else {
            throw new RuntimeException("Not supported yet for this matrix type");
        }
    }

    /**
     * Used to set the complex value of a matrix element.
     * @param row The row of the element.
     * @param col The column of the element.
     * @param real Real component of assigned value
     * @param imaginary Imaginary component of assigned value
     */
    public void set( int row , int col , double real , double imaginary ) {
        if( imaginary == 0 ) {
            set(row,col,real);
        } else {
            ops.set(mat,row,col, real, imaginary);
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
     * @param startColumn The initial column that the array is written to.
     * @param values Values which are to be written to the row in a matrix.
     */
    public void setRow( int row , int startColumn , double ...values ) {
        ops.setRow(mat,row,startColumn,values);
    }

    /**
     * <p>
     * Assigns consecutive elements inside a column to the provided array.<br>
     * <br>
     * A(offset:(offset + values.length),column) = values
     * </p>
     *
     * @param column The column that the array is to be written to.
     * @param startRow The initial column that the array is written to.
     * @param values Values which are to be written to the row in a matrix.
     */
    public void setColumn( int column , int startRow , double ...values ) {
        ops.setColumn(mat,column,startRow,values);
    }

    /**
     * Returns the value of the specified matrix element.  Performs a bounds check to make sure
     * the requested element is part of the matrix.
     *
     * NOTE: Complex matrices will throw an exception
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @return The value of the element.
     */
    public double get( int row , int col ) {
        return ops.get(mat,row,col);
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
        MatrixType type = mat.getType();

        if( type.isReal()) {
            if (type.getBits() == 64) {
                return ((DMatrixRMaj) mat).data[index];
            } else {
                return ((FMatrixRMaj) mat).data[index];
            }
        } else {
            throw new IllegalArgumentException("Complex matrix. Call get(int,Complex64F) instead");
        }
    }

    /**
     * Used to get the complex value of a matrix element.
     * @param row The row of the element.
     * @param col The column of the element.
     * @param output Storage for the value
     */
    public void get( int row , int col , Complex_F64 output ) {
        ops.get(mat,row,col,output);
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
        T ret = createLike();
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
        return mat.getNumCols()*mat.getNumRows();
    }

    /**
     * Prints the matrix to standard out.
     */
    public void print() {
        mat.print();
    }

    /**
     * <p>
     * Prints the matrix to standard out given a {@link java.io.PrintStream#printf} style floating point format,
     * e.g. print("%f").
     * </p>
     */
    public void print( String format ) {
        ops.print(System.out,mat,format);
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
        PrintStream p = new PrintStream(stream);

        MatrixIO.print(p,mat);

        return stream.toString();
    }

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
    public T extractMatrix(int y0 , int y1, int x0 , int x1 ) {
        if( y0 == SimpleMatrix.END ) y0 = mat.getNumRows();
        if( y1 == SimpleMatrix.END ) y1 = mat.getNumRows();
        if( x0 == SimpleMatrix.END ) x0 = mat.getNumCols();
        if( x1 == SimpleMatrix.END ) x1 = mat.getNumCols();

        T ret = createMatrix(y1-y0,x1-x0, mat.getType());

        ops.extract(mat, y0, y1, x0, x1, ret.mat, 0, 0);

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
        if( extractRow ) {
            return extractMatrix(element,element+1,0,SimpleMatrix.END);
        } else {
            return extractMatrix(0,SimpleMatrix.END,element,element+1);
        }
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
        return wrapMatrix(ops.diag(mat));
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
        if( a.getType() != getType() )
            return false;
        return ops.isIdentical(mat,a.mat,tol);
    }

    /**
     * Checks to see if any of the elements in this matrix are either NaN or infinite.
     *
     * @return True of an element is NaN or infinite.  False otherwise.
     */
    public boolean hasUncountable() {
        return ops.hasUncountable(mat);
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
        convertType.specify(this,B);
        B = convertType.convert(B);

        // See if this type's need to be changed or not
        if( convertType.commonType == getType() ) {
            insert(B.mat,mat,insertRow,insertCol);
        } else {
            T A = convertType.convert(this);
            A.insert(B.mat,A.mat,insertRow,insertCol);
            setMatrix(A.mat);
        }
    }

    void insert( Matrix src , Matrix dst , int destY0 , int destX0 ) {
        ops.extract(src, 0, src.getNumRows(), 0, src.getNumCols(), dst, destY0, destX0);
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
        convertType.specify(this,B);
        T A = convertType.convert(this);
        B = convertType.convert(B);

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

            ret = A.createMatrix(M,N, A.getType());
            ret.insertIntoThis(0,0,A);
        } else {
            ret = A.copy();
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
        return ops.elementMaxAbs(mat);
    }

    /**
     * Returns the minimum absolute value of all the elements in this matrix.
     *
     * @return Smallest absolute value of any element.
     */
    public double elementMinAbs() {
        return ops.elementMinAbs(mat);
    }


    /**
     * Computes the sum of all the elements in the matrix.
     *
     * @return Sum of all the elements.
     */
    public double elementSum() {
        return ops.elementSum(mat);
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
        convertType.specify(this,b);
        T A = convertType.convert(this);
        b = convertType.convert(b);

        T c = A.createLike();
        A.ops.elementMult(A.mat,b.mat,c.mat);
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
        convertType.specify(this,b);
        T A = convertType.convert(this);
        b = convertType.convert(b);

        T c = A.createLike();
        A.ops.elementDiv(A.mat,b.mat,c.mat);
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
        convertType.specify(this,b);
        T A = convertType.convert(this);
        b = convertType.convert(b);

        T c = A.createLike();
        A.ops.elementPower(A.mat,b.mat,c.mat);
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
        T c = createLike();
        ops.elementPower(mat,b,c.mat);
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
        T c = createLike();
        ops.elementExp(mat,c.mat);
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
        T c = createLike();
        ops.elementLog(mat,c.mat);
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
        ops.changeSign(A.mat);
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
                eq.alias(((SimpleBase)o).getDDRM(),name);
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
        MatrixIO.saveDenseCSV((DMatrixRMaj)mat, fileName);
    }

    /**
     * <p>
     * Loads a new matrix from a CSV file.  For the file format see {@link MatrixIO}.
     * </p>
     *
     * @see MatrixIO#loadCSV(String,boolean)
     *
     * @param fileName File which is to be loaded.
     * @return The matrix.
     * @throws IOException
     */
    public T loadCSV( String fileName )
            throws IOException {
        DMatrix mat = MatrixIO.loadCSV(fileName,true);

        T ret = createMatrix(1,1, mat.getType());

        ret.setMatrix(mat);

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
        return mat.getType().getBits();
    }

    /**
     * <p>Concatinates all the matrices together along their columns.  If the rows do not match the upper elements
     * are set to zero.</p>
     *
     * A = [ this, m[0] , ... , m[n-1] ]
     *
     * @param matrices Set of matrices
     * @return Resulting matrix
     */
    public T concatColumns( SimpleBase ...matrices ) {
        convertType.specify0(this,matrices);
        T A = convertType.convert(this);

        int numCols = A.numCols();
        int numRows = A.numRows();
        for (int i = 0; i < matrices.length; i++) {
            numRows = Math.max(numRows,matrices[i].numRows());
            numCols += matrices[i].numCols();
        }

        SimpleMatrix combined = SimpleMatrix.wrap(convertType.commonType.create(numRows,numCols));

        A.ops.extract(A.mat,0,A.numRows(),0,A.numCols(),combined.mat,0,0);
        int col = A.numCols();
        for (int i = 0; i < matrices.length; i++) {
            Matrix m = convertType.convert(matrices[i]).mat;
            int cols = m.getNumCols();
            int rows = m.getNumRows();;
            A.ops.extract(m,0,rows,0,cols,combined.mat,0,col);
            col += cols;
        }

        return (T)combined;
    }

    /**
     * <p>Concatinates all the matrices together along their columns.  If the rows do not match the upper elements
     * are set to zero.</p>
     *
     * A = [ this; m[0] ; ... ; m[n-1] ]
     *
     * @param matrices Set of matrices
     * @return Resulting matrix
     */
    public T concatRows( SimpleBase ... matrices ) {
        convertType.specify0(this,matrices);
        T A = convertType.convert(this);

        int numCols = A.numCols();
        int numRows = A.numRows();
        for (int i = 0; i < matrices.length; i++) {
            numRows += matrices[i].numRows();
            numCols = Math.max(numCols,matrices[i].numCols());
        }

        SimpleMatrix combined = SimpleMatrix.wrap(convertType.commonType.create(numRows,numCols));

        A.ops.extract(A.mat,0,A.numRows(),0,A.numCols(),combined.mat,0,0);
        int row = A.numRows();
        for (int i = 0; i < matrices.length; i++) {
            Matrix m = convertType.convert(matrices[i]).mat;
            int cols = m.getNumCols();
            int rows = m.getNumRows();;
            A.ops.extract(m,0,rows,0,cols,combined.mat,row,0);
            row += rows;
        }

        return (T)combined;
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

    /**
     * Returns the type of matrix is is wrapping.
     */
    public MatrixType getType() {
        return mat.getType();
    }

    /**
     * Creates a matrix that is the same type and shape
     * @return New matrix
     */
    public T createLike() {
        return createMatrix(numRows(),numCols(),getType());
    }

    protected void setMatrix( Matrix mat ) {
        this.mat = mat;
        this.ops = lookupOps(mat.getType());
    }


    Method findAlternative( String method , Object... arguments ) {
        Method[] methods = ops.getClass().getMethods();
        for (int methodIdx = 0; methodIdx < methods.length; methodIdx++) {
            if (!methods[methodIdx].getName().equals(method))
                continue;

            Class<?>[] paramTypes = methods[methodIdx].getParameterTypes();
            if( paramTypes.length != arguments.length )
                continue;

            // look for an exact match only
            boolean match = true;
            for (int j = 0; j < paramTypes.length; j++) {
                if( arguments[j] instanceof Class ) {
                    if( paramTypes[j] != arguments[j]) {
                        match = false;
                        break;
                    }
                } else if( paramTypes[j] != arguments[j].getClass() ) {
                    match = false;
                    break;
                }
            }
            if( match ) {
                return methods[methodIdx];
            }
        }
        return null;
    }

    public void invoke( Method m , Object... inputs ) {
        try {
            m.invoke(ops,inputs);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Switches from a dense to sparse matrix
     */
    public void convertToSparse() {
        switch ( mat.getType() ) {
            case DDRM: {
                DMatrixSparseCSC m = new DMatrixSparseCSC(mat.getNumRows(), mat.getNumCols());
                ConvertDMatrixStruct.convert((DMatrixRMaj) mat, m,0);
                setMatrix(m);
            } break;
            case FDRM: {
                FMatrixSparseCSC m = new FMatrixSparseCSC(mat.getNumRows(), mat.getNumCols());
                ConvertFMatrixStruct.convert((FMatrixRMaj) mat, m,0);
                setMatrix(m);
            } break;

            case DSCC:
            case FSCC:
                break;
            default:
                throw new RuntimeException("Conversion not supported!");
        }
    }

    /**
     * Switches from a sparse to dense matrix
     */
    public void convertToDense() {
        switch ( mat.getType() ) {
            case DSCC: {
                DMatrix m = new DMatrixRMaj(mat.getNumRows(), mat.getNumCols());
                ConvertDMatrixStruct.convert((DMatrix) mat, m);
                setMatrix(m);
            } break;
            case FSCC: {
                FMatrix m = new FMatrixRMaj(mat.getNumRows(), mat.getNumCols());
                ConvertFMatrixStruct.convert((FMatrix) mat, m);
                setMatrix(m);
            } break;
            case DDRM:
            case FDRM:
            case ZDRM:
            case CDRM:
                break;
            default:
                throw new RuntimeException("Not a sparse matrix!");
        }
    }
}
