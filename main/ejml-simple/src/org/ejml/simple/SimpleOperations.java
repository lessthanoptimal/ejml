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

import org.ejml.data.Complex_F64;
import org.ejml.data.Matrix;

import java.io.PrintStream;
import java.io.Serializable;

/**
 * High level interface for operations inside of SimpleMatrix for one matrix type.
 *
 * @author Peter Abeles
 */
public interface SimpleOperations<T extends Matrix> extends Serializable {

    void set( T A, int row, int column, double value );

    void set( T A, int row, int column, double real, double imaginary );

    double get( T A, int row, int column );

    void get( T A, int row, int column, Complex_F64 value );

    double getReal( T A, int row, int column );

    double getImaginary( T A, int row, int column );

    void fill( T A, double value );

    void transpose( T input, T output );

    void mult( T A, T B, T output );

    void multTransA( T A, T B, T output );

    void kron( T A, T B, T output );

    void plus( T A, T B, T output );

    /** output[i,j] = A[i,j] - B[i,j] */
    void minus( T A, T B, T output );

    /** output[i,j] = A[i,j] - b */
    void minus( T A, double b, T output );

    default void minusComplex( T A, double real, double imag, T output ) {
        // If the value isn't actually complex, treat it like a real value
        if (imag == 0.0) {
            minus(A, real, output);
            return;
        }
        throw new ConvertToImaginaryException();
    }

    /** output[i,j] = A[i,j] + b */
    void plus( T A, double b, T output );

    default void plusComplex( T A, double real, double imag, T output ) {
        // If the value isn't actually complex, treat it like a real value
        if (imag == 0.0) {
            plus(A, real, output);
            return;
        }
        throw new ConvertToImaginaryException();
    }

    /** output[i,j] = A[i,j] + beta*b[i,j] */
    void plus( T A, double beta, T b, T output );

    /** output[i,j] = alpha*A[i,j] + beta*b[i,j] */
    void plus( double alpha, T A, double beta, T b, T output );

    double dot( T A, T v );

    /** Multiplies each element by val. Val is a real number */
    void scale( T A, double val, T output );

    default void scaleComplex( T A, double real, double imag, T output ) {
        // If the value isn't actually complex, treat it like a real value
        if (imag == 0.0) {
            scale(A, real, output);
            return;
        }
        throw new ConvertToImaginaryException();
    }

    /** Divides each element by val. Val is a real number */
    void divide( T A, double val, T output );

    boolean invert( T A, T output );

    void setIdentity( T A );

    void pseudoInverse( T A, T output );

    boolean solve( T A, T X, T B );

    void zero( T A );

    double normF( T A );

    double conditionP2( T A );

    double determinant( T A );

    default Complex_F64 determinantComplex( T A ) {
        var output = new Complex_F64();

        // by default assume it's a real matrix
        output.imaginary = 0;
        output.real = determinant(A);

        return output;
    }

    double trace( T A );

    default Complex_F64 traceComplex( T A ) {
        var output = new Complex_F64();

        // by default assume it's a real matrix
        output.imaginary = 0;
        output.real = trace(A);

        return output;
    }

    void setRow( T A, int row, int startColumn, double... values );

    void setColumn( T A, int column, int startRow, double... values );

    double[] getRow( T A, int row, int col0, int col1 );

    double[] getColumn( T A, int col, int row0, int row1 );

    void extract( T src,
                  int srcY0, int srcY1,
                  int srcX0, int srcX1,
                  T dst,
                  int dstY0, int dstX0 );

    T diag( T A );

    boolean hasUncountable( T M );

    void changeSign( T a );

    double elementMax( T A );

    double elementMin( T A );

    double elementMaxAbs( T A );

    double elementMinAbs( T A );

    double elementSum( T A );

    default void elementSumComplex( T A, Complex_F64 output ) {
        // by default assume it's a real matrix
        output.imaginary = 0;
        output.real = elementSum(A);
    }

    void elementMult( T A, T B, T output );

    void elementDiv( T A, T B, T output );

    void elementPower( T A, T B, T output );

    void elementPower( T A, double b, T output );

    void elementExp( T A, T output );

    void elementLog( T A, T output );

    boolean isIdentical( T A, T B, double tol );

    void print( PrintStream out, Matrix mat, String format );

    void elementOp( T A, ElementOpReal op, T output );

    void elementOp( T A, ElementOpComplex op, T output );

    @FunctionalInterface interface ElementOpReal {
        double op( int row, int col, double value );
    }

    @FunctionalInterface interface ElementOpComplex {
        /**
         * @param value (Input) value of element in input matrix. (Output) value that output matrix will have
         */
        void op( int row, int col, Complex_F64 value );
    }
}
