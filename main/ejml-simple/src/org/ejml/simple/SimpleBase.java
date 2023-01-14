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

import org.ejml.UtilEjml;
import org.ejml.data.*;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.equation.Equation;
import org.ejml.ops.ConvertMatrixType;
import org.ejml.ops.DConvertMatrixStruct;
import org.ejml.ops.FConvertMatrixStruct;
import org.ejml.ops.MatrixIO;
import org.ejml.simple.ops.*;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/**
 * Parent of {@link SimpleMatrix} implements all the standard matrix operations and uses
 * generics to allow the returned matrix type to be changed. This class should be extended
 * instead of SimpleMatrix.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked", "NullAway.Init", "ForLoopReplaceableByForEach"})
public abstract class SimpleBase<T extends SimpleBase<T>> implements ImmutableMatrix, Serializable {

    static final long serialVersionUID = 2342556642L;

    /**
     * Internal matrix which this is a wrapper around.
     */
    protected Matrix mat;
    protected SimpleOperations ops;

    protected transient AutomaticSimpleMatrixConvert convertType = new AutomaticSimpleMatrixConvert();

    protected SimpleBase( int numRows, int numCols ) {
        setMatrix(new DMatrixRMaj(numRows, numCols));
    }

    protected SimpleBase() {}

    private void readObject( java.io.ObjectInputStream in )
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        convertType = new AutomaticSimpleMatrixConvert();
    }

    /**
     * Used internally for creating new instances of SimpleMatrix. If SimpleMatrix is extended
     * by another class this function should be overridden so that the returned matrices are
     * of the correct type.
     *
     * @param numRows number of rows in the new matrix.
     * @param numCols number of columns in the new matrix.
     * @param type Type of matrix it should create
     * @return A new matrix.
     */
    protected abstract T createMatrix( int numRows, int numCols, MatrixType type );

    /**
     * Creates a real matrix with the same floating type as 'this'
     */
    protected T createRealMatrix( int numRows, int numCols ) {
        MatrixType type = getType().getBits() == 32 ? MatrixType.FDRM : MatrixType.DDRM;
        return createMatrix(numRows, numCols, type);
    }

    /**
     * Creates a complex matrix with the same floating type as 'this'
     */
    protected T createComplexMatrix( int numRows, int numCols ) {
        MatrixType type = getType().getBits() == 32 ? MatrixType.CDRM : MatrixType.ZDRM;
        return createMatrix(numRows, numCols, type);
    }

    protected abstract T wrapMatrix( Matrix m );

    /**
     * <p>
     * Returns a reference to the matrix that it uses internally. This is useful
     * when an operation is needed that is not provided by this class.
     * </p>
     *
     * @return Reference to the internal DMatrixRMaj.
     */
    public <InnerType extends Matrix> InnerType getMatrix() {
        return (InnerType)mat;
    }

    public DMatrixRMaj getDDRM() {
        return (mat.getType() == MatrixType.DDRM) ? (DMatrixRMaj)mat : (DMatrixRMaj)ConvertMatrixType.convert(mat, MatrixType.DDRM);
    }

    public FMatrixRMaj getFDRM() {
        return (mat.getType() == MatrixType.FDRM) ? (FMatrixRMaj)mat : (FMatrixRMaj)ConvertMatrixType.convert(mat, MatrixType.FDRM);
    }

    public ZMatrixRMaj getZDRM() {
        return (mat.getType() == MatrixType.ZDRM) ? (ZMatrixRMaj)mat : (ZMatrixRMaj)ConvertMatrixType.convert(mat, MatrixType.ZDRM);
    }

    public CMatrixRMaj getCDRM() {
        return (mat.getType() == MatrixType.CDRM) ? (CMatrixRMaj)mat : (CMatrixRMaj)ConvertMatrixType.convert(mat, MatrixType.CDRM);
    }

    public DMatrixSparseCSC getDSCC() {
        return (mat.getType() == MatrixType.DSCC) ? (DMatrixSparseCSC)mat : (DMatrixSparseCSC)ConvertMatrixType.convert(mat, MatrixType.DSCC);
    }

    public FMatrixSparseCSC getFSCC() {
        return (mat.getType() == MatrixType.FSCC) ? (FMatrixSparseCSC)mat : (FMatrixSparseCSC)ConvertMatrixType.convert(mat, MatrixType.FSCC);
    }

    protected static SimpleOperations lookupOps( MatrixType type ) {
        return switch (type) {
            case DDRM -> new SimpleOperations_DDRM();
            case FDRM -> new SimpleOperations_FDRM();
            case ZDRM -> new SimpleOperations_ZDRM();
            case CDRM -> new SimpleOperations_CDRM();
            case DSCC -> new SimpleOperations_DSCC();
            case FSCC -> new SimpleOperations_FSCC();
            default -> throw new RuntimeException("Unknown Matrix Type. " + type);
        };
    }

    /** {@inheritDoc} */
    @Override public T transpose() {
        T ret = createMatrix(mat.getNumCols(), mat.getNumRows(), mat.getType());

        ops.transpose(mat, ret.mat);

        return ret;
    }

    /** {@inheritDoc} */
    @Override public T transposeConjugate() {
        if (getType().isReal()) {
            return transpose();
        }

        T ret = createMatrix(mat.getNumCols(), mat.getNumRows(), mat.getType());
        if (getType().getBits() == 32) {
            CommonOps_CDRM.transposeConjugate(getCDRM(), ret.getCDRM());
        } else {
            CommonOps_ZDRM.transposeConjugate(getZDRM(), ret.getZDRM());
        }
        return ret;
    }

    /** {@inheritDoc} */
    @Override public T mult( ImmutableMatrix _B ) {
        T B = (T)_B;
        convertType.specify(this, B);

        // Look to see if there is a special function for handling this case
        if (this.mat.getType() != B.getType()) {
            Method m = findAlternative("mult", mat, B.mat, convertType.commonType.getClassType());
            if (m != null) {
                T ret = wrapMatrix(convertType.commonType.create(1, 1));
                invoke(m, this.mat, B.mat, ret.mat);
                return ret;
            }
        }

        // Otherwise convert into a common matrix type if necessary
        T A = convertType.convert(this);
        B = convertType.convert(B);

        T ret = A.createMatrix(mat.getNumRows(), B.getMatrix().getNumCols(), A.getType());

        A.ops.mult(A.mat, B.mat, ret.mat);

        return ret;
    }

    /** {@inheritDoc} */
    @Override public T kron( ImmutableMatrix _B ) {
        T B = (T)_B;
        convertType.specify(this, B);
        T A = convertType.convert(this);
        B = convertType.convert(B);

        T ret = A.createMatrix(mat.getNumRows()*B.numRows(), mat.getNumCols()*B.numCols(), A.getType());

        A.ops.kron(A.mat, B.mat, ret.mat);

        return ret;
    }

    /** {@inheritDoc} */
    @Override public T plus( ImmutableMatrix _B ) {
        T B = (T)_B;
        convertType.specify(this, B);
        T A = convertType.convert(this);
        B = convertType.convert(B);

        T ret = A.createMatrix(mat.getNumRows(), mat.getNumCols(), A.getType());

        A.ops.plus(A.mat, B.mat, ret.mat);

        return ret;
    }

    /** {@inheritDoc} */
    @Override public T minus( ImmutableMatrix _B ) {
        T B = (T)_B;
        convertType.specify(this, B);
        T A = convertType.convert(this);
        B = convertType.convert(B);
        T ret = A.createLike();

        A.ops.minus(A.mat, B.mat, ret.mat);
        return ret;
    }

    /** {@inheritDoc} */
    @Override public T minus( double b ) {
        T ret = createLike();
        ops.minus(mat, b, ret.mat);
        return ret;
    }

    /** {@inheritDoc} */
    @Override public T minusComplex( double real, double imag ) {
        try {
            T ret = createLike();
            ops.minusComplex(mat, real, imag, ret.getMatrix());
            return ret;
        } catch (ConvertToImaginaryException e) {
            // Input matrix isn't complex therefor output isn't complex either
            T converted = createComplexMatrix(1, 1);
            converted.setMatrix(ConvertMatrixType.convert(mat, converted.getType()));
            return converted.minusComplex(real, imag);
        }
    }

    /** {@inheritDoc} */
    @Override public T plus( double b ) {
        T ret = createLike();
        ops.plus(mat, b, ret.mat);
        return ret;
    }

    /** {@inheritDoc} */
    @Override public T plusComplex( double real, double imag ) {
        try {
            T ret = createLike();
            ops.plusComplex(mat, real, imag, ret.getMatrix());
            return ret;
        } catch (ConvertToImaginaryException e) {
            // Input matrix isn't complex therefor output isn't complex either
            T converted = createComplexMatrix(1, 1);
            converted.setMatrix(ConvertMatrixType.convert(mat, converted.getType()));
            return converted.plusComplex(real, imag);
        }
    }

    /** {@inheritDoc} */
    @Override public T plus( double beta, ImmutableMatrix _B ) {
        T B = (T)_B;
        convertType.specify(this, B);
        T A = convertType.convert(this);
        B = convertType.convert(B);

        T ret = A.createLike();
        A.ops.plus(A.mat, beta, B.mat, ret.mat);
        return ret;
    }

    /** {@inheritDoc} */
    @Override public double dot( ImmutableMatrix _v ) {
        T v = (T)_v;
        convertType.specify(this, v);
        T A = convertType.convert(this);
        v = convertType.convert(v);

        if (!isVector()) {
            throw new IllegalArgumentException("'this' matrix is not a vector.");
        } else if (!v.isVector()) {
            throw new IllegalArgumentException("'v' matrix is not a vector.");
        }

        return A.ops.dot(A.mat, v.getMatrix());
    }

    /** {@inheritDoc} */
    @Override public boolean isVector() {
        return mat.getNumRows() == 1 || mat.getNumCols() == 1;
    }

    /** {@inheritDoc} */
    @Override public T scale( double val ) {
        T ret = createLike();
        ops.scale(mat, val, ret.getMatrix());
        return ret;
    }

    /** {@inheritDoc} */
    @Override public T scaleComplex( double real, double imag ) {
        try {
            T ret = createLike();
            ops.scaleComplex(mat, real, imag, ret.getMatrix());
            return ret;
        } catch (ConvertToImaginaryException e) {
            // Input matrix isn't complex therefor output isn't complex either
            T converted = createComplexMatrix(1, 1);
            converted.setMatrix(ConvertMatrixType.convert(mat, converted.getType()));
            return converted.scaleComplex(real, imag);
        }
    }

    /** {@inheritDoc} */
    @Override public T divide( double val ) {
        T ret = createLike();
        ops.divide(mat, val, ret.getMatrix());
        return ret;
    }

    /** {@inheritDoc} */
    @Override public T invert() {
        T ret = createLike();

        if (!ops.invert(mat, ret.mat))
            throw new SingularMatrixException();
        if (ops.hasUncountable(ret.mat))
            throw new SingularMatrixException("Solution contains uncountable numbers");

        return ret;
    }

    /** {@inheritDoc} */
    @Override public T pseudoInverse() {
        T ret = createLike();
        ops.pseudoInverse(mat, ret.mat);
        return ret;
    }

    /** {@inheritDoc} */
    @Override public T solve( ImmutableMatrix _B ) {
        T B = (T)_B;
        convertType.specify(this, B);

        // Look to see if there is a special function for handling this case
        if (this.mat.getType() != B.getType()) {
            Method m = findAlternative("solve", mat, B.mat, convertType.commonType.getClassType());
            if (m != null) {
                T ret = wrapMatrix(convertType.commonType.create(1, 1));
                invoke(m, this.mat, B.mat, ret.mat); // TODO handle boolean return from solve
                return ret;
            }
        }

        T A = convertType.convert(this);
        B = convertType.convert(B);

        T x = A.createMatrix(mat.getNumCols(), B.getMatrix().getNumCols(), A.getType());

        if (!A.ops.solve(A.mat, x.mat, B.mat))
            throw new SingularMatrixException();
        if (A.ops.hasUncountable(x.mat))
            throw new SingularMatrixException("Solution contains uncountable numbers");

        return x;
    }

    /**
     * Sets the elements in this matrix to be equal to the elements in the passed in matrix.
     * Both matrix must have the same dimension.
     *
     * @param a The matrix whose value this matrix is being set to.
     */
    public void setTo( T a ) {
        if (a.getType() == getType())
            mat.setTo(a.getMatrix());
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
     * @param val The value each element is set to.
     * @see CommonOps_DDRM#fill(DMatrixD1, double)
     */
    public void fill( double val ) {
        try {
            ops.fill(mat, val);
        } catch (ConvertToDenseException e) {
            convertToDense();
            fill(val);
        }
    }

    /**
     * In-place fills the matrix with a complex value. If the matrix is real valued, then it will become a complex
     * matrix.
     */
    public void fillComplex( double real, double imaginary ) {
        // change it into a complex matrix
        if (getType().isReal()) {
            setMatrix(createComplexMatrix(getNumRows(), getNumCols()).mat);
        }

        if (getType().getBits() == 32) {
            CommonOps_CDRM.fill(getCDRM(), (float)real, (float)imaginary);
        } else {
            CommonOps_ZDRM.fill(getZDRM(), real, imaginary);
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

    /** {@inheritDoc} */
    @Override public double normF() {
        return ops.normF(mat);
    }

    /** {@inheritDoc} */
    @Override public double conditionP2() {
        return ops.conditionP2(mat);
    }

    /** {@inheritDoc} */
    @Override public double determinant() {
        double ret = ops.determinant(mat);
        if (UtilEjml.isUncountable(ret))
            return 0;
        return ret;
    }

    /** {@inheritDoc} */
    @Override public Complex_F64 determinantComplex() {
        Complex_F64 ret = ops.determinantComplex(mat);
        if (UtilEjml.isUncountable(ret.real))
            ret.setTo(0, 0);
        return ret;
    }

    /** {@inheritDoc} */
    @Override public double trace() {
        return ops.trace(mat);
    }

    /** {@inheritDoc} */
    @Override public Complex_F64 traceComplex() {
        return ops.traceComplex(mat);
    }

    /**
     * <p>
     * Reshapes the matrix to the specified number of rows and columns. If the total number of elements
     * is &le; number of elements it had before the data is saved. Otherwise a new internal array is
     * declared and the old data lost.
     * </p>
     *
     * <p>
     * This is equivalent to calling A.getMatrix().reshape(numRows,numCols,false).
     * </p>
     *
     * @param numRows The new number of rows in the matrix.
     * @param numCols The new number of columns in the matrix.
     * @see DMatrixRMaj#reshape(int, int, boolean)
     */
    public void reshape( int numRows, int numCols ) {
        if (mat.getType().isFixed()) {
            throw new IllegalArgumentException("Can't reshape a fixed sized matrix");
        } else {
            ((ReshapeMatrix)mat).reshape(numRows, numCols);
        }
    }

    /**
     * Assigns the element in the Matrix to the specified value. Performs a bounds check to make sure
     * the requested element is part of the matrix.
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @param value The element's new value.
     */
    public void set( int row, int col, double value ) {
        ops.set(mat, row, col, value);
    }

    /**
     * Assigns an element a value based on its index in the internal array.
     *
     * @param index The matrix element that is being assigned a value.
     * @param value The element's new value.
     */
    public void set( int index, double value ) {
        if (mat.getType() == MatrixType.DDRM) {
            ((DMatrixRMaj)mat).set(index, value);
        } else if (mat.getType() == MatrixType.FDRM) {
            ((FMatrixRMaj)mat).set(index, (float)value);
        } else {
            throw new RuntimeException("Not supported yet for this matrix type");
        }
    }

    /**
     * Used to set the complex value of a matrix element.
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @param real Real component of assigned value
     * @param imaginary Imaginary component of assigned value
     */
    public void set( int row, int col, double real, double imaginary ) {
        if (imaginary == 0) {
            set(row, col, real);
        } else {
            ops.set(mat, row, col, real, imaginary);
        }
    }

    /**
     * Used to set the complex value of a matrix element.
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @param value The value that the element is being assigned to
     */
    public void set( int row, int col, Complex_F64 value ) {
        set(row, col, value.real, value.imaginary);
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
    public void setRow( int row, int startColumn, double... values ) {
        ops.setRow(mat, row, startColumn, values);
    }

    /**
     * <p>Copies the vector into the specified row. The 'src' vector can be a row or column vector as long as it
     * has the correct length.</p>
     *
     * @param row Row in 'this'
     * @param src Vector which is to be copied into the row
     */
    public void setRow( int row, SimpleMatrix src ) {
        if (!src.isVector())
            throw new IllegalArgumentException("Input matrix must be a vector");
        if (src.getNumElements() != numCols())
            throw new IllegalArgumentException("Number of elements must match number of columns. src=" +
                    src.getNumElements() + " cols=" + numCols());

        convertType.specify(this, src);

        // Does it need to convert the type of 'this'?
        if (convertType.commonType != getType()) {
            setMatrix(convertType.convert(this).mat);
        }

        // See if it's a row or column vector and grab the appropriate elements.
        double[] vector = src.numRows() < src.numCols() ?
                src.ops.getRow(src.mat, 0, 0, src.getNumElements()) :
                src.ops.getColumn(src.mat, 0, 0, src.getNumElements());

        // If src is real but output is complex, convert the vector.
        if (src.getType().isReal() && !getType().isReal()) {
            vector = vectorRealToComplex(vector);
        }

        setRow(row, 0, vector);
        // NOTE: For sparse to sparse this method is very inefficient...
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
    public void setColumn( int column, int startRow, double... values ) {
        ops.setColumn(mat, column, startRow, values);
    }

    /**
     * <p>Copies the vector into the specified column. The 'src' vector can be a row or column vector as long as it
     * has the correct length.</p>
     *
     * @param column Column in 'this'
     * @param src Vector which is to be copied into the column
     */
    public void setColumn( int column, SimpleMatrix src ) {
        if (!src.isVector())
            throw new IllegalArgumentException("Input matrix must be a vector");
        if (src.getNumElements() != numRows())
            throw new IllegalArgumentException("Number of elements must match number of rows. src=" +
                    src.getNumElements() + " cols=" + numRows());

        convertType.specify(this, src);

        // Does it need to convert the type of 'this'?
        if (convertType.commonType != getType()) {
            setMatrix(convertType.convert(this).mat);
        }

        // See if it's a row or column vector and grab the appropriate elements.
        double[] vector = src.numRows() < src.numCols() ?
                src.ops.getRow(src.mat, 0, 0, src.getNumElements()) :
                src.ops.getColumn(src.mat, 0, 0, src.getNumElements());

        // If src is real but output is complex, convert the vector.
        if (src.getType().isReal() && !getType().isReal()) {
            vector = vectorRealToComplex(vector);
        }

        setColumn(column, 0, vector);
        // NOTE: For sparse to sparse this method is very inefficient...
    }

    /**
     * Converts a real array/vector into a complex one by setting imaginary component to zero
     */
    private static double[] vectorRealToComplex( double[] input ) {
        var output = new double[input.length*2];
        for (int i = 0; i < input.length; i++) {
            output[i*2] = input[i];
            output[i*2 + 1] = 0.0;
        }
        return output;
    }

    /** {@inheritDoc} */
    @Override public double get( int row, int col ) {
        return ops.get(mat, row, col);
    }

    /** {@inheritDoc} */
    @Override public double get( int index ) {
        MatrixType type = mat.getType();

        if (type.isReal()) {
            if (type.getBits() == 64) {
                return ((DMatrixRMaj)mat).data[index];
            } else {
                return ((FMatrixRMaj)mat).data[index];
            }
        } else {
            throw new IllegalArgumentException("Complex matrix. Call get(int,Complex64F) instead");
        }
    }

    /** {@inheritDoc} */
    @Override public void get( int row, int col, Complex_F64 output ) {
        ops.get(mat, row, col, output);
    }

    /** {@inheritDoc} */
    @Override public double getReal( int row, int col ) {
        return ops.getReal(mat, row, col);
    }

    /** {@inheritDoc} */
    @Override public double getImaginary( int row, int col ) {
        return ops.getImaginary(mat, row, col);
    }

    /** {@inheritDoc} */
    @Override public int getIndex( int row, int col ) {
        return row*mat.getNumCols() + col;
    }

    /** {@inheritDoc} */
    @Override public DMatrixIterator iterator( boolean rowMajor, int minRow, int minCol, int maxRow, int maxCol ) {
        return new DMatrixIterator((DMatrixRMaj)mat, rowMajor, minRow, minCol, maxRow, maxCol);
    }

    /** {@inheritDoc} */
    @Override public T copy() {
        T ret = createLike();
        ret.getMatrix().setTo(this.getMatrix());
        return ret;
    }

    /**
     * Returns the number of rows in this matrix.
     *
     * @return number of rows.
     * @deprecated Inconsistent API. Use {@link #getNumRows()} instead.
     */
    @Deprecated
    public int numRows() {
        return mat.getNumRows();
    }

    /**
     * Returns the number of columns in this matrix.
     *
     * @return number of columns.
     * @deprecated Inconsistent API. Use {@link #getNumCols()} instead.
     */
    @Deprecated
    public int numCols() {
        return mat.getNumCols();
    }

    /** {@inheritDoc} */
    @Override public int getNumRows() {
        return mat.getNumRows();
    }

    /** {@inheritDoc} */
    @Override public int getNumCols() {
        return mat.getNumCols();
    }

    /** {@inheritDoc} */
    @Override public void print() {
        mat.print();
    }

    /** {@inheritDoc} */
    @Override public void print( String format ) {
        ops.print(System.out, mat, format);
    }

    /** {@inheritDoc} */
    @Override public double[][] toArray2() {
        double[][] array = new double[mat.getNumRows()][mat.getNumCols()];
        for (int r = 0; r < mat.getNumRows(); r++) {
            for (int c = 0; c < mat.getNumCols(); c++) {
                array[r][c] = get(r, c);
            }
        }
        return array;
    }

    /**
     * <p>
     * Converts the array into a string format for display purposes.
     * The conversion is done using {@link MatrixIO#print(java.io.PrintStream, DMatrix)}.
     * </p>
     *
     * @return String representation of the matrix.
     */
    @Override
    public String toString() {
        var stream = new ByteArrayOutputStream();
        var p = new PrintStream(stream);

        MatrixIO.print(p, mat);

        return stream.toString(StandardCharsets.UTF_8);
    }

    /** {@inheritDoc} */
    @Override public T extractMatrix( int y0, int y1, int x0, int x1 ) {
        if (y0 == SimpleMatrix.END) y0 = mat.getNumRows();
        if (y1 == SimpleMatrix.END) y1 = mat.getNumRows();
        if (x0 == SimpleMatrix.END) x0 = mat.getNumCols();
        if (x1 == SimpleMatrix.END) x1 = mat.getNumCols();

        T ret = createMatrix(y1 - y0, x1 - x0, mat.getType());

        ops.extract(mat, y0, y1, x0, x1, ret.mat, 0, 0);

        return ret;
    }

    /** {@inheritDoc} */
    @Override public T extractVector( boolean extractRow, int element ) {
        if (extractRow) {
            return extractMatrix(element, element + 1, 0, SimpleMatrix.END);
        } else {
            return extractMatrix(0, SimpleMatrix.END, element, element + 1);
        }
    }

    /** {@inheritDoc} */
    @Override public T getRow( int row ) {
        return extractVector(true, row);
    }

    /** {@inheritDoc} */
    @Override public T getColumn( int col ) {
        return extractVector(false, col);
    }

    /** {@inheritDoc} */
    @Override public T diag() {
        return wrapMatrix(ops.diag(mat));
    }

    /** {@inheritDoc} */
    @Override public boolean isIdentical( ImmutableMatrix _a, double tol ) {
        T a = (T)_a;
        if (a.getType() != getType())
            return false;
        return ops.isIdentical(mat, a.mat, tol);
    }

    /** {@inheritDoc} */
    @Override public boolean hasUncountable() {
        return ops.hasUncountable(mat);
    }

    /**
     * Computes a full Singular Value Decomposition (SVD) of this matrix with the
     * eigenvalues ordered from largest to smallest.
     *
     * @return SVD
     */
    public SimpleSVD<T> svd() {
        return new SimpleSVD<>(mat, false);
    }

    /**
     * Computes the SVD in either  compact format or full format.
     *
     * @return SVD of this matrix.
     */
    public SimpleSVD<T> svd( boolean compact ) {
        return new SimpleSVD<>(mat, compact);
    }

    /**
     * Returns the Eigen Value Decomposition (EVD) of this matrix.
     */
    public SimpleEVD<T> eig() {
        return new SimpleEVD<>(mat);
    }

    /**
     * Copy matrix B into this matrix at location (insertRow, insertCol).
     *
     * @param insertRow First row the matrix is to be inserted into.
     * @param insertCol First column the matrix is to be inserted into.
     * @param B The matrix that is being inserted.
     */
    public void insertIntoThis( int insertRow, int insertCol, T B ) {
        convertType.specify(this, B);
        B = convertType.convert(B);

        // See if this type's need to be changed or not
        if (convertType.commonType == getType()) {
            insert(B.mat, mat, insertRow, insertCol);
        } else {
            T A = convertType.convert(this);
            A.insert(B.mat, A.mat, insertRow, insertCol);
            setMatrix(A.mat);
        }
    }

    void insert( Matrix src, Matrix dst, int destY0, int destX0 ) {
        ops.extract(src, 0, src.getNumRows(), 0, src.getNumCols(), dst, destY0, destX0);
    }

    /** {@inheritDoc} */
    @Override public T combine( int insertRow, int insertCol, ImmutableMatrix _B ) {
        T B = (T)_B;
        convertType.specify(this, B);
        T A = convertType.convert(this);
        B = convertType.convert(B);

        if (insertRow == SimpleMatrix.END) {
            insertRow = mat.getNumRows();
        }

        if (insertCol == SimpleMatrix.END) {
            insertCol = mat.getNumCols();
        }

        int maxRow = insertRow + B.numRows();
        int maxCol = insertCol + B.numCols();

        T ret;

        if (maxRow > mat.getNumRows() || maxCol > mat.getNumCols()) {
            int M = Math.max(maxRow, mat.getNumRows());
            int N = Math.max(maxCol, mat.getNumCols());

            ret = A.createMatrix(M, N, A.getType());
            ret.insertIntoThis(0, 0, A);
        } else {
            ret = A.copy();
        }

        ret.insertIntoThis(insertRow, insertCol, B);

        return ret;
    }

    /** {@inheritDoc} */
    @Override public double elementMax() {
        return ops.elementMax(mat);
    }

    /** {@inheritDoc} */
    @Override public double elementMin() {
        return ops.elementMin(mat);
    }

    /** {@inheritDoc} */
    @Override public double elementMaxAbs() {
        return ops.elementMaxAbs(mat);
    }

    /** {@inheritDoc} */
    @Override public double elementMinAbs() {
        return ops.elementMinAbs(mat);
    }

    /** {@inheritDoc} */
    @Override public double elementSum() {
        return ops.elementSum(mat);
    }

    /** {@inheritDoc} */
    @Override public Complex_F64 elementSumComplex() {
        var sum = new Complex_F64();
        ops.elementSumComplex(mat, sum);
        return sum;
    }

    /** {@inheritDoc} */
    @Override public T elementMult( ImmutableMatrix _B ) {
        T B = (T)_B;
        convertType.specify(this, B);
        T A = convertType.convert(this);
        B = convertType.convert(B);

        T c = A.createLike();
        A.ops.elementMult(A.mat, B.mat, c.mat);
        return c;
    }

    /** {@inheritDoc} */
    @Override public T elementDiv( ImmutableMatrix _B ) {
        T B = (T)_B;
        convertType.specify(this, (T)B);
        T A = convertType.convert(this);
        B = convertType.convert((T)B);

        T c = A.createLike();
        A.ops.elementDiv(A.mat, B.mat, c.mat);
        return c;
    }

    /** {@inheritDoc} */
    @Override public T elementPower( ImmutableMatrix _B ) {
        T B = (T)_B;
        convertType.specify(this, B);
        T A = convertType.convert(this);
        B = convertType.convert(B);

        T c = A.createLike();
        A.ops.elementPower(A.mat, B.mat, c.mat);
        return c;
    }

    /** {@inheritDoc} */
    @Override public T elementPower( double b ) {
        T c = createLike();
        ops.elementPower(mat, b, c.mat);
        return c;
    }

    /** {@inheritDoc} */
    @Override public T elementExp() {
        T c = createLike();
        ops.elementExp(mat, c.mat);
        return c;
    }

    /** {@inheritDoc} */
    @Override public T elementLog() {
        T c = createLike();
        ops.elementLog(mat, c.mat);
        return c;
    }

    /** {@inheritDoc} */
    @Override public T elementOp( SimpleOperations.ElementOpReal op ) {
        T c = createLike();
        ops.elementOp(mat, op, c.mat);
        return c;
    }

    /** {@inheritDoc} */
    @Override public T elementOp( SimpleOperations.ElementOpComplex op ) {
        T c = createLike();
        try {
            ops.elementOp(mat, op, c.mat);
        } catch (ConvertToImaginaryException e) {
            // Input matrix isn't complex therefor output isn't complex either
            T converted = createComplexMatrix(1, 1);
            converted.setMatrix(ConvertMatrixType.convert(mat, converted.getType()));

            // Try again with a complex matrix that is the equivalent of the input matrix
            return converted.elementOp(op);
        }
        return c;
    }

    /** {@inheritDoc} */
    @Override public T negative() {
        T A = copy();
        ops.changeSign(A.mat);
        return A;
    }

    /** {@inheritDoc} */
    @Override public T conjugate() {
        T A = copy();

        if (A.getType().isReal())
            return A;

        if (A.getType().getBits() == 32) {
            CommonOps_CDRM.conjugate(getCDRM(), A.getCDRM());
        } else {
            CommonOps_ZDRM.conjugate(getZDRM(), A.getZDRM());
        }

        return A;
    }

    /** {@inheritDoc} */
    @Override public T magnitude() {
        T A = createRealMatrix(mat.getNumRows(), mat.getNumCols());

        if (getType().isReal()) {
            if (getType().getBits() == 32) {
                CommonOps_FDRM.abs(getFDRM(), A.getFDRM());
            } else {
                CommonOps_DDRM.abs(getDDRM(), A.getDDRM());
            }
        } else {
            if (getType().getBits() == 32) {
                CommonOps_CDRM.magnitude(getCDRM(), A.getFDRM());
            } else {
                CommonOps_ZDRM.magnitude(getZDRM(), A.getDDRM());
            }
        }

        return A;
    }

    /**
     * <p>Allows you to perform an equation in-place on this matrix by specifying the right hand side. For information on how to define an equation
     * see {@link org.ejml.equation.Equation}. The variable sequence alternates between variable and it's label String.
     * This matrix is by default labeled as 'A', but is a string is the first object in 'variables' then it will take
     * on that value. The variable passed in can be any data type supported by Equation can be passed in.
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
    public void equation( String equation, Object... variables ) {
        if (variables.length >= 25)
            throw new IllegalArgumentException("Too many variables!  At most 25");

        if (!(mat instanceof DMatrixRMaj))
            return;

        Equation eq = new Equation();

        String nameThis = "A";
        int offset = 0;
        if (variables.length > 0 && variables[0] instanceof String) {
            nameThis = (String)variables[0];
            offset = 1;

            if (variables.length%2 != 1)
                throw new IllegalArgumentException("Expected and odd length for variables");
        } else {
            if (variables.length%2 != 0)
                throw new IllegalArgumentException("Expected and even length for variables");
        }
        eq.alias((DMatrixRMaj)mat, nameThis);

        for (int i = offset; i < variables.length; i += 2) {
            if (!(variables[i + 1] instanceof String name))
                throw new IllegalArgumentException("String expected at variables index " + i);
            Object o = variables[i];

            if (SimpleBase.class.isAssignableFrom(o.getClass())) {
                eq.alias(((SimpleBase<T>)o).getDDRM(), name);
            } else if (o instanceof DMatrixRMaj) {
                eq.alias((DMatrixRMaj)o, name);
            } else if (o instanceof Double) {
                eq.alias((Double)o, name);
            } else if (o instanceof Integer) {
                eq.alias((Integer)o, name);
            } else {
                String type = o.getClass().getSimpleName();
                throw new IllegalArgumentException("Variable type not supported by Equation! " + type);
            }
        }

        // see if the assignment is implicit
        if (!equation.contains("=")) {
            equation = nameThis + " = " + equation;
        }

        eq.process(equation);
    }

    /** {@inheritDoc} */
    @Override public void saveToFileCSV( String fileName ) throws IOException {
        MatrixIO.saveDenseCSV((DMatrixRMaj)mat, fileName);
    }

    /** {@inheritDoc} */
    @Override public void saveToMatrixMarket( String fileName ) throws IOException {
        final String format = ".15e";

        try (var writer = new FileWriter(fileName, StandardCharsets.UTF_8)) {
            if (mat instanceof DMatrixRMaj)
                MatrixIO.saveMatrixMarket((DMatrixRMaj)mat, format, writer);
            else if (mat instanceof FMatrixRMaj)
                MatrixIO.saveMatrixMarket((FMatrixRMaj)mat, format, writer);
            else if (mat instanceof DMatrixSparseCSC)
                MatrixIO.saveMatrixMarket((DMatrixSparseCSC)mat, format, writer);
            else if (mat instanceof FMatrixSparseCSC)
                MatrixIO.saveMatrixMarket((FMatrixSparseCSC)mat, format, writer);
            else
                throw new IllegalArgumentException("Internal matrix type isn'y yet support for matrix market");
        }
    }

    /**
     * <p>
     * Loads a new matrix from a CSV file. For the file format see {@link MatrixIO}.
     * The returned matrix will be the same matrix type as 'this'.
     * </p>
     *
     * @param fileName File which is to be loaded.
     * @return The matrix.
     * @see MatrixIO#loadCSV(String, boolean)
     */
    public T loadCSV( String fileName ) throws IOException {
        DMatrix mat = MatrixIO.loadCSV(fileName, true);

        T ret = createMatrix(1, 1, mat.getType());

        ret.setMatrix(mat);

        return ret;
    }

    /** {@inheritDoc} */
    @Override public boolean isInBounds( int row, int col ) {
        return row >= 0 && col >= 0 && row < mat.getNumRows() && col < mat.getNumCols();
    }

    /**
     * Prints the number of rows and column in this matrix.
     */
    public void printDimensions() {
        System.out.println("[rows = " + numRows() + " , cols = " + numCols() + " ]");
    }

    /** {@inheritDoc} */
    @Override public int bits() {
        return mat.getType().getBits();
    }

    /**
     * <p>Concatenates all the matrices together along their columns. If the rows do not match the upper elements
     * are set to zero.</p>
     *
     * A = [ this, m[0] , ... , m[n-1] ]
     *
     * @param matrices Set of matrices
     * @return Resulting matrix
     */
    public T concatColumns( SimpleBase<?>... matrices ) {
        convertType.specify0(this, matrices);
        T A = convertType.convert(this);

        int numCols = A.numCols();
        int numRows = A.numRows();
        for (int i = 0; i < matrices.length; i++) {
            numRows = Math.max(numRows, matrices[i].numRows());
            numCols += matrices[i].numCols();
        }

        SimpleMatrix combined = SimpleMatrix.wrap(convertType.commonType.create(numRows, numCols));

        A.ops.extract(A.mat, 0, A.numRows(), 0, A.numCols(), combined.mat, 0, 0);
        int col = A.numCols();
        for (int i = 0; i < matrices.length; i++) {
            Matrix m = convertType.convert(matrices[i]).mat;
            int cols = m.getNumCols();
            int rows = m.getNumRows();
            A.ops.extract(m, 0, rows, 0, cols, combined.mat, 0, col);
            col += cols;
        }

        return (T)combined;
    }

    /**
     * <p>Concatenates all the matrices together along their columns. If the rows do not match the upper elements
     * are set to zero.</p>
     *
     * A = [ this; m[0] ; ... ; m[n-1] ]
     *
     * @param matrices Set of matrices
     * @return Resulting matrix
     */
    public T concatRows( SimpleBase<?>... matrices ) {
        convertType.specify0(this, matrices);
        T A = convertType.convert(this);

        int numCols = A.numCols();
        int numRows = A.numRows();
        for (int i = 0; i < matrices.length; i++) {
            numRows += matrices[i].numRows();
            numCols = Math.max(numCols, matrices[i].numCols());
        }

        SimpleMatrix combined = SimpleMatrix.wrap(convertType.commonType.create(numRows, numCols));

        A.ops.extract(A.mat, 0, A.numRows(), 0, A.numCols(), combined.mat, 0, 0);
        int row = A.numRows();
        for (int i = 0; i < matrices.length; i++) {
            Matrix m = convertType.convert(matrices[i]).mat;
            int cols = m.getNumCols();
            int rows = m.getNumRows();
            A.ops.extract(m, 0, rows, 0, cols, combined.mat, row, 0);
            row += rows;
        }

        return (T)combined;
    }

    /** {@inheritDoc} */
    @Override public T rows( int begin, int end ) {
        return extractMatrix(begin, end, 0, SimpleMatrix.END);
    }

    /** {@inheritDoc} */
    @Override public T cols( int begin, int end ) {
        return extractMatrix(0, SimpleMatrix.END, begin, end);
    }

    /** {@inheritDoc} */
    @Override public MatrixType getType() {
        return mat.getType();
    }

    /** {@inheritDoc} */
    @Override public T real() {
        T ret = createRealMatrix(mat.getNumRows(), mat.getNumCols());

        // If it's a real matrix just return a copy
        if (mat.getType().isReal()) {
            return ret.wrapMatrix(mat.copy());
        }

        if (mat.getType().getBits() == 32) {
            return ret.wrapMatrix(CommonOps_CDRM.real((CMatrixD1)mat, null));
        } else {
            return ret.wrapMatrix(CommonOps_ZDRM.real((ZMatrixD1)mat, null));
        }
    }

    /** {@inheritDoc} */
    @Override public T imaginary() {
        T ret = createRealMatrix(mat.getNumRows(), mat.getNumCols());

        // If it's a real matrix just return a matrix full of zeros
        if (mat.getType().isReal()) {
            return ret.wrapMatrix(mat.createLike());
        }

        if (mat.getType().getBits() == 32) {
            return ret.wrapMatrix(CommonOps_CDRM.imaginary((CMatrixD1)mat, null));
        } else {
            return ret.wrapMatrix(CommonOps_ZDRM.imaginary((ZMatrixD1)mat, null));
        }
    }

    /** {@inheritDoc} */
    @Override public T createLike() {
        return createMatrix(numRows(), numCols(), getType());
    }

    protected void setMatrix( Matrix mat ) {
        this.mat = mat;
        this.ops = lookupOps(mat.getType());
    }

    @Nullable Method findAlternative( String method, Object... arguments ) {
        Method[] methods = ops.getClass().getMethods();
        for (int methodIdx = 0; methodIdx < methods.length; methodIdx++) {
            if (!methods[methodIdx].getName().equals(method))
                continue;

            Class<?>[] paramTypes = methods[methodIdx].getParameterTypes();
            if (paramTypes.length != arguments.length)
                continue;

            // look for an exact match only
            boolean match = true;
            for (int j = 0; j < paramTypes.length; j++) {
                if (arguments[j] instanceof Class) {
                    if (paramTypes[j] != arguments[j]) {
                        match = false;
                        break;
                    }
                } else if (paramTypes[j] != arguments[j].getClass()) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return methods[methodIdx];
            }
        }
        return null;
    }

    public void invoke( Method m, Object... inputs ) {
        try {
            m.invoke(ops, inputs);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Switches from a dense to sparse matrix
     */
    public void convertToSparse() {
        switch (mat.getType()) {
            case DDRM: {
                DMatrixSparseCSC m = new DMatrixSparseCSC(mat.getNumRows(), mat.getNumCols());
                DConvertMatrixStruct.convert((DMatrixRMaj)mat, m, 0);
                setMatrix(m);
            }
            break;
            case FDRM: {
                FMatrixSparseCSC m = new FMatrixSparseCSC(mat.getNumRows(), mat.getNumCols());
                FConvertMatrixStruct.convert((FMatrixRMaj)mat, m, 0);
                setMatrix(m);
            }
            break;

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
        switch (mat.getType()) {
            case DSCC: {
                DMatrix m = new DMatrixRMaj(mat.getNumRows(), mat.getNumCols());
                DConvertMatrixStruct.convert((DMatrix)mat, m);
                setMatrix(m);
            }
            break;
            case FSCC: {
                FMatrix m = new FMatrixRMaj(mat.getNumRows(), mat.getNumCols());
                FConvertMatrixStruct.convert((FMatrix)mat, m);
                setMatrix(m);
            }
            break;
            case DDRM:
            case FDRM:
            case ZDRM:
            case CDRM:
                break;
            default:
                throw new RuntimeException("Not a sparse matrix!");
        }
    }

    /**
     * Switches from a real to complex matrix
     */
    public void convertToComplex() {
        switch (mat.getType()) {
            case DDRM -> setMatrix(ConvertMatrixType.convert(mat, MatrixType.ZDRM));

            case FDRM -> setMatrix(ConvertMatrixType.convert(mat, MatrixType.CDRM));

            case ZDRM, CDRM -> {
            }
            default -> throw new RuntimeException("Conversion not supported!");
        }
    }
}
