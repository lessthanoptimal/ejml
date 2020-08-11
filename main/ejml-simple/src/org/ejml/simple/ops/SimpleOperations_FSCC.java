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

package org.ejml.simple.ops;

import org.ejml.data.*;
import org.ejml.ops.MatrixIO;
import org.ejml.simple.ConvertToDenseException;
import org.ejml.simple.ConvertToImaginaryException;
import org.ejml.simple.SimpleSparseOperations;
import org.ejml.sparse.csc.CommonOps_FSCC;
import org.ejml.sparse.csc.MatrixFeatures_FSCC;
import org.ejml.sparse.csc.NormOps_FSCC;

import java.io.PrintStream;

/**
 * @author Peter Abeles
 */
public class SimpleOperations_FSCC implements SimpleSparseOperations<FMatrixSparseCSC,FMatrixRMaj> {

    // Workspace variables
    public transient IGrowArray gw = new IGrowArray();
    public transient FGrowArray gx = new FGrowArray();

    @Override
    public void set(FMatrixSparseCSC A, int row, int column, /**/double value) {
        A.set(row,column, (float)value);
    }

    @Override
    public void set(FMatrixSparseCSC A, int row, int column, /**/double real, /**/double imaginary) {
        throw new ConvertToImaginaryException();
    }

    @Override
    public /**/double get(FMatrixSparseCSC A, int row, int column) {
        return A.get(row,column);
    }

    @Override
    public void get(FMatrixSparseCSC A, int row, int column, /**/Complex_F64 value) {
        value.real = A.get(row,column);
        value.imaginary = 0;
    }

    @Override
    public void fill(FMatrixSparseCSC A, /**/double value) {
        if( value == 0 ) {
            A.zero();
        } else {
            throw new ConvertToDenseException();
        }
    }

    @Override
    public void transpose(FMatrixSparseCSC input, FMatrixSparseCSC output) {
        CommonOps_FSCC.transpose(input,output,gw);
    }

    @Override
    public void mult(FMatrixSparseCSC A, FMatrixSparseCSC B, FMatrixSparseCSC output) {
        CommonOps_FSCC.mult(A,B,output);
    }

    @Override
    public void multTransA(FMatrixSparseCSC A, FMatrixSparseCSC B, FMatrixSparseCSC output) {
        CommonOps_FSCC.multTransA(A,B,output,gw,gx);
    }

    @Override
    public void extractDiag(FMatrixSparseCSC input, FMatrixRMaj output) {
        CommonOps_FSCC.extractDiag(input,output);
    }

    @Override
    public void multTransA(FMatrixSparseCSC A, FMatrixRMaj B, FMatrixRMaj output) {
        CommonOps_FSCC.multTransA(A,B,output);
    }

    public void mult(FMatrixSparseCSC A , FMatrixRMaj B , FMatrixRMaj output ) {
        CommonOps_FSCC.mult(A,B,output);
    }

    @Override
    public void kron(FMatrixSparseCSC A, FMatrixSparseCSC B, FMatrixSparseCSC output) {
//        CommonOps_FSCC.kron(A,B,output);
        throw new RuntimeException("Unsupported");
    }

    @Override
    public void plus(FMatrixSparseCSC A, FMatrixSparseCSC B, FMatrixSparseCSC output) {
        CommonOps_FSCC.add(1,A,1,B,output, null, null);
    }

    @Override
    public void minus(FMatrixSparseCSC A, FMatrixSparseCSC B, FMatrixSparseCSC output) {
        CommonOps_FSCC.add(1,A,-1,B,output, null, null);
    }

    @Override
    public void minus(FMatrixSparseCSC A, /**/double b, FMatrixSparseCSC output) {
        throw new ConvertToDenseException();
    }

    @Override
    public void plus(FMatrixSparseCSC A, /**/double b, FMatrixSparseCSC output) {
        throw new ConvertToDenseException();
    }

    @Override
    public void plus(FMatrixSparseCSC A, /**/double beta, FMatrixSparseCSC b, FMatrixSparseCSC output) {
        CommonOps_FSCC.add(1, A, (float)beta, b, output,gw,gx);
    }

    @Override
    public void plus( /**/double alpha, FMatrixSparseCSC A, /**/double beta, FMatrixSparseCSC b, FMatrixSparseCSC output) {
        CommonOps_FSCC.add( (float)alpha, A, (float)beta, b, output,gw,gx);
    }

    @Override
    public /**/double dot(FMatrixSparseCSC A, FMatrixSparseCSC v) {
        return CommonOps_FSCC.dotInnerColumns(A,0, v,0,gw,gx);
    }

    @Override
    public void scale(FMatrixSparseCSC A, /**/double val, FMatrixSparseCSC output) {
        CommonOps_FSCC.scale( (float)val, A,output);
    }

    @Override
    public void divide(FMatrixSparseCSC A, /**/double val, FMatrixSparseCSC output) {
        CommonOps_FSCC.divide( A, (float)val, output);
    }

    @Override
    public boolean invert(FMatrixSparseCSC A, FMatrixSparseCSC output) {
        return solve(A,output,CommonOps_FSCC.identity(A.numRows,A.numCols));
    }

    @Override
    public void setIdentity(FMatrixSparseCSC A) {
        CommonOps_FSCC.setIdentity(A);
    }

    @Override
    public void pseudoInverse(FMatrixSparseCSC A, FMatrixSparseCSC output) {
        throw new RuntimeException("Unsupported");
    }

    @Override
    public boolean solve(FMatrixSparseCSC A, FMatrixSparseCSC X, FMatrixSparseCSC B) {
        return CommonOps_FSCC.solve(A, X, B);
    }

    public boolean solve(FMatrixSparseCSC A, FMatrixRMaj X, FMatrixRMaj B) {
        return CommonOps_FSCC.solve(A, X, B);
    }

    @Override
    public void zero(FMatrixSparseCSC A) {
        A.zero();
    }

    @Override
    public /**/double normF(FMatrixSparseCSC A) {
        return NormOps_FSCC.normF(A);
    }

    @Override
    public /**/double conditionP2(FMatrixSparseCSC A) {
        throw new RuntimeException("Unsupported");
    }

    @Override
    public /**/double determinant(FMatrixSparseCSC A) {
        return CommonOps_FSCC.det(A);
    }

    @Override
    public /**/double trace(FMatrixSparseCSC A) {
        return CommonOps_FSCC.trace(A);
    }

    @Override
    public void setRow(FMatrixSparseCSC A, int row, int startColumn, /**/double... values) {
        // TODO Update with a more efficient algorithm
        for (int i = 0; i < values.length; i++) {
            A.set(row, startColumn + i, (float)values[i]);
        }
        // check to see if value are zero, if so ignore them

        // Do a pass through the matrix and see how many elements need to be added

        // see if the existing storage is enough

        // If it is enough ...
        // starting from the tail, move a chunk, insert, move the next chunk, ...etc

        // If not enough, create new arrays and construct it
    }

    @Override
    public void setColumn(FMatrixSparseCSC A, int column, int startRow,  /**/double... values) {
        // TODO Update with a more efficient algorithm
        for (int i = 0; i < values.length; i++) {
            A.set(startRow + i, column, (float)values[i]);
        }
    }

    @Override
    public void extract(FMatrixSparseCSC src, int srcY0, int srcY1, int srcX0, int srcX1, FMatrixSparseCSC dst, int dstY0, int dstX0) {
        CommonOps_FSCC.extract(src,srcY0,srcY1,srcX0,srcX1,dst,dstY0,dstX0);
    }

    @Override
    public FMatrixSparseCSC diag(FMatrixSparseCSC A) {
        FMatrixSparseCSC output;
        if (MatrixFeatures_FSCC.isVector(A)) {
            int N = Math.max(A.numCols,A.numRows);
            output = new FMatrixSparseCSC(N,N);
            CommonOps_FSCC.diag(output,A.nz_values,0,N);
        } else {
            int N = Math.min(A.numCols,A.numRows);
            output = new FMatrixSparseCSC(N,1);
            CommonOps_FSCC.extractDiag(A,output);
        }
        return output;
    }

    @Override
    public boolean hasUncountable(FMatrixSparseCSC M) {
        return MatrixFeatures_FSCC.hasUncountable(M);
    }

    @Override
    public void changeSign(FMatrixSparseCSC a) {
        CommonOps_FSCC.changeSign(a,a);
    }

    @Override
    public /**/double elementMaxAbs(FMatrixSparseCSC A) {
        return CommonOps_FSCC.elementMaxAbs(A);
    }

    @Override
    public /**/double elementMinAbs(FMatrixSparseCSC A) {
        return CommonOps_FSCC.elementMinAbs(A);
    }

    @Override
    public /**/double elementSum(FMatrixSparseCSC A) {
        return CommonOps_FSCC.elementSum(A);
    }

    @Override
    public void elementMult(FMatrixSparseCSC A, FMatrixSparseCSC B, FMatrixSparseCSC output) {
        CommonOps_FSCC.elementMult(A,B,output,null,null);
    }

    @Override
    public void elementDiv(FMatrixSparseCSC A, FMatrixSparseCSC B, FMatrixSparseCSC output) {
        throw new ConvertToDenseException();
    }

    @Override
    public void elementPower(FMatrixSparseCSC A, FMatrixSparseCSC B, FMatrixSparseCSC output) {
        throw new ConvertToDenseException();
    }

    @Override
    public void elementPower(FMatrixSparseCSC A, /**/double b, FMatrixSparseCSC output) {
        throw new ConvertToDenseException();
    }

    @Override
    public void elementExp(FMatrixSparseCSC A, FMatrixSparseCSC output) {
        throw new ConvertToDenseException();
    }

    @Override
    public void elementLog(FMatrixSparseCSC A, FMatrixSparseCSC output) {
        throw new ConvertToDenseException();
    }

    @Override
    public boolean isIdentical(FMatrixSparseCSC A, FMatrixSparseCSC B, /**/double tol) {
        return MatrixFeatures_FSCC.isEqualsSort(A, B, (float)tol);
    }

    @Override
    public void print(PrintStream out, Matrix mat, String format ) {
        MatrixIO.print(out, (FMatrixSparseCSC)mat, format);
    }
}
