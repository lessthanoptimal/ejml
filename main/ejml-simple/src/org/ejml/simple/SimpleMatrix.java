/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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
 * SimpleMatrix K = P.mult(H.transpose().mult(S.invert()));
 * </p>
 *
 * <p>
 * Working with both a primitive matrix and SimpleMatrix in the same code base is easy.
 * To access the internal DMatrixRMaj in a SimpleMatrix simply call {@link SimpleMatrix#getMatrix()}.
 * To turn a DMatrixRMaj into a SimpleMatrix use {@link SimpleMatrix#wrap(org.ejml.data.Matrix)}. Not
 * all operations in EJML are provided for SimpleMatrix, but can be accessed by extracting the internal
 * matrix.
 * </p>
 *
 * <p>
 * EXTENDING: SimpleMatrix contains a list of narrowly focused functions for linear algebra. To harness
 * the functionality for another application and to the number of functions it supports it is recommended
 * that one extends {@link SimpleBase} instead. This way the returned matrix type's of SimpleMatrix functions
 * will be of the appropriate types. See StatisticsMatrix inside of the examples directory.
 * </p>
 *
 * <p>
 * PERFORMANCE: The disadvantage of using this class is that it is more resource intensive, since
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
 * <p>
 * If SimpleMatrix is extended then the protected function {link #createMatrix} should be extended and return
 * the child class. The results of SimpleMatrix operations will then be of the correct matrix type.
 * </p>
 *
 * <p>
 * The object oriented approach used in SimpleMatrix was originally inspired by Jama.
 * http://math.nist.gov/javanumerics/jama/
 * </p>
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

    public SimpleMatrix( int numRows, int numCols, Class type ) {
        this(numRows, numCols, MatrixType.lookup(type));
    }

    /**
     * Create a simple matrix of the specified type
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
     * Creates a new SimpleMatrix with the specified DMatrixRMaj used as its internal matrix. This means
     * that the reference is saved and calls made to the returned SimpleMatrix will modify the passed in DMatrixRMaj.
     *
     * @param internalMat The internal DMatrixRMaj of the returned SimpleMatrix. Will be modified.
     */
    public static SimpleMatrix wrap( Matrix internalMat ) {
        var ret = new SimpleMatrix();
        ret.setMatrix(internalMat);
        return ret;
    }

    /**
     * Returns a filled matrix (numRows x numCols) of the value a.
     * @param numRows The number of numRows.
     * @param numCols The number of columns.
     * @param a The number to fill the matrix with.
     * @return A matrix filled with the value a.
     */
    public static SimpleMatrix filled( int numRows, int numCols, double a ) {
        var res = new SimpleMatrix(numRows, numCols);
        res.fill(a);
        return res;
    }

    /**
     * Returns a matrix of ones.
     * @param numRows The number of numRows.
     * @param numCols The number of columns.
     * @return A matrix of ones.
     */
    public static SimpleMatrix ones( int numRows, int numCols ) {
        return filled(numRows, numCols, 1);
    }

    /**
     * Creates a new identity matrix with the specified size.
     *
     * @param width The width and height of the matrix.
     * @return An identity matrix.
     * @see CommonOps_DDRM#identity(int)
     */
    public static SimpleMatrix identity( int width ) {
        return identity(width, DMatrixRMaj.class);
    }

    public static SimpleMatrix identity( int width, Class<?> type ) {
        var ret = new SimpleMatrix(width, width, type);
        ret.ops.setIdentity(ret.mat);
        return ret;
    }

    /**
     * <p>
     * Creates a matrix where all but the diagonal elements are zero. The values
     * of the diagonal elements are specified by the parameter 'vals'.
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
     * Creates a real valued diagonal matrix of the specified type
     */
    public static SimpleMatrix diag( Class<?> type, double... vals ) {
        var M = new SimpleMatrix(vals.length, vals.length, type);
        for (int i = 0; i < vals.length; i++) {
            M.set(i, i, vals[i]);
        }
        return M;
    }

    /**
     * <p>
     * Creates a new SimpleMatrix with random elements drawn from a uniform distribution from minValue to maxValue.
     * </p>
     *
     * @param numRows The number of rows in the new matrix
     * @param numCols The number of columns in the new matrix
     * @param minValue Lower bound
     * @param maxValue Upper bound
     * @param rand The random number generator that's used to fill the matrix. @return The new random matrix.
     * @see RandomMatrices_DDRM#fillUniform(DMatrixRMaj, java.util.Random)
     */
    public static SimpleMatrix random_DDRM( int numRows, int numCols, double minValue, double maxValue, Random rand ) {
        var ret = new SimpleMatrix(numRows, numCols);
        RandomMatrices_DDRM.fillUniform((DMatrixRMaj)ret.mat, minValue, maxValue, rand);
        return ret;
    }

    public static SimpleMatrix random_FDRM( int numRows, int numCols, float minValue, float maxValue, Random rand ) {
        var ret = new SimpleMatrix(numRows, numCols, FMatrixRMaj.class);
        RandomMatrices_FDRM.fillUniform((FMatrixRMaj)ret.mat, minValue, maxValue, rand);
        return ret;
    }

    public static SimpleMatrix random_ZDRM( int numRows, int numCols, double minValue, double maxValue, Random rand ) {
        var ret = new SimpleMatrix(numRows, numCols, MatrixType.ZDRM);
        RandomMatrices_ZDRM.fillUniform((ZMatrixRMaj)ret.mat, minValue, maxValue, rand);
        return ret;
    }

    public static SimpleMatrix random_CDRM( int numRows, int numCols, float minValue, float maxValue, Random rand ) {
        var ret = new SimpleMatrix(numRows, numCols, MatrixType.CDRM);
        RandomMatrices_CDRM.fillUniform((CMatrixRMaj)ret.mat, minValue, maxValue, rand);
        return ret;
    }

    /**
     * <p>
     * Creates a new vector which is drawn from a multivariate normal distribution with zero mean
     * and the provided covariance.
     * </p>
     *
     * @param covariance Covariance of the multivariate normal distribution
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
