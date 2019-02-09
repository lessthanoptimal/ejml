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

    void set( T A , int row , int column , double value );

    void set( T A , int row , int column , double real, double imaginary );

    double get( T A , int row , int column );

    void get(T A , int row , int column , Complex_F64 value );

    void fill( T A , double value );

    void transpose( T input , T output );

    void mult( T A , T B, T output );

    void multTransA(T A , T B, T output );

    void kron( T A , T B , T output );

    void plus( T A , T B , T output);

    void minus( T A , T B , T output );

    void minus( T A , double b , T output );

    void plus( T A , double b , T output);

    void plus( T A , double beta , T b, T output );

    void plus( double alpha , T A , double beta , T b, T output );

    double dot(T A , T v );

    void scale( T A , double val , T output );

    void divide( T A , double val , T output );

    boolean invert( T A , T output );

    void setIdentity(T A );

    void pseudoInverse( T A , T output );

    boolean solve( T A , T X , T B);

    void zero( T A );

    double normF( T A );

    double conditionP2( T A );

    double determinant( T A );

    double trace( T A );

    void setRow( T A , int row , int startColumn , double ...values );

    void setColumn( T A , int column , int startRow , double ...values );

    void extract( T src,
                  int srcY0, int srcY1,
                  int srcX0, int srcX1,
                  T dst ,
                  int dstY0, int dstX0 );

    T diag( T A );

    boolean hasUncountable(T M);

    void changeSign( T a );

    double elementMaxAbs(T A);

    double elementMinAbs(T A);

    double elementSum(T A);

    void elementMult( T A , T B , T output );

    void elementDiv( T A , T B , T output );

    void elementPower( T A , T B , T output );

    void elementPower( T A , double b, T output );

    void elementExp( T A , T output );

    void elementLog( T A , T output );

    boolean isIdentical(T A, T B, double tol);

    void print(PrintStream out , Matrix mat , String format );
}
