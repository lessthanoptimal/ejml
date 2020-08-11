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

import org.ejml.data.Complex_F64;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.Matrix;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.NormOps_FDRM;
import org.ejml.dense.row.mult.VectorVectorMult_FDRM;
import org.ejml.ops.MatrixIO;
import org.ejml.simple.SimpleOperations;

import java.io.PrintStream;

//CUSTOM ignore Complex_F64
//CUSTOM ignore org.ejml.data.Complex_F64;

/**
 * @author Peter Abeles
 */
public class SimpleOperations_FDRM implements SimpleOperations<FMatrixRMaj> {

    @Override
    public void set(FMatrixRMaj A, int row, int column, /**/double value) {
        A.set(row,column, (float)value);
    }

    @Override
    public void set(FMatrixRMaj A, int row, int column, /**/double real, /**/double imaginary) {
        throw new IllegalArgumentException("Does not support imaginary values");
    }

    @Override
    public /**/double get(FMatrixRMaj A, int row, int column) {
        return (float)A.get(row,column);
    }

    @Override
    public void get(FMatrixRMaj A, int row, int column, Complex_F64 value) {
        value.real = A.get(row,column);
        value.imaginary = 0;
    }

    @Override
    public void fill(FMatrixRMaj A, /**/double value) {
        CommonOps_FDRM.fill(A, (float)value);
    }

    @Override
    public void transpose(FMatrixRMaj input, FMatrixRMaj output) {
        CommonOps_FDRM.transpose(input,output);
    }

    @Override
    public void mult(FMatrixRMaj A, FMatrixRMaj B, FMatrixRMaj output) {
        CommonOps_FDRM.mult(A,B,output);
    }

    @Override
    public void multTransA(FMatrixRMaj A, FMatrixRMaj B, FMatrixRMaj output) {
        CommonOps_FDRM.multTransA(A,B,output);
    }

    @Override
    public void kron(FMatrixRMaj A, FMatrixRMaj B, FMatrixRMaj output) {
        CommonOps_FDRM.kron(A,B,output);
    }

    @Override
    public void plus(FMatrixRMaj A, FMatrixRMaj B, FMatrixRMaj output) {
        CommonOps_FDRM.add(A,B,output);
    }

    @Override
    public void minus(FMatrixRMaj A, FMatrixRMaj B, FMatrixRMaj output) {
        CommonOps_FDRM.subtract(A,B,output);
    }

    @Override
    public void minus(FMatrixRMaj A, /**/double b, FMatrixRMaj output) {
        CommonOps_FDRM.subtract(A, (float)b, output);
    }

    @Override
    public void plus(FMatrixRMaj A, /**/double b, FMatrixRMaj output) {
        CommonOps_FDRM.add(A, (float)b, output);
    }

    @Override
    public void plus(FMatrixRMaj A, /**/double beta, FMatrixRMaj b, FMatrixRMaj output) {
        CommonOps_FDRM.add(A, (float)beta, b, output);
    }

    @Override
    public void plus( /**/double alpha, FMatrixRMaj A, /**/double beta, FMatrixRMaj b, FMatrixRMaj output) {
        CommonOps_FDRM.add( (float)alpha, A, (float)beta,b,output);
    }

    @Override
    public /**/double dot(FMatrixRMaj A, FMatrixRMaj v) {
        return VectorVectorMult_FDRM.innerProd(A, v);
    }

    @Override
    public void scale(FMatrixRMaj A, /**/double val, FMatrixRMaj output) {
        CommonOps_FDRM.scale( (float)val, A,output);
    }

    @Override
    public void divide(FMatrixRMaj A, /**/double val, FMatrixRMaj output) {
        CommonOps_FDRM.divide( A, (float)val,output);
    }

    @Override
    public boolean invert(FMatrixRMaj A, FMatrixRMaj output) {
        return CommonOps_FDRM.invert(A,output);
    }

    @Override
    public void setIdentity(FMatrixRMaj A) {
        CommonOps_FDRM.setIdentity(A);
    }

    @Override
    public void pseudoInverse(FMatrixRMaj A, FMatrixRMaj output) {
        CommonOps_FDRM.pinv(A,output);
    }

    @Override
    public boolean solve(FMatrixRMaj A, FMatrixRMaj X, FMatrixRMaj B) {
        return CommonOps_FDRM.solve(A,B,X);
    }

    @Override
    public void zero(FMatrixRMaj A) {
        A.zero();
    }

    @Override
    public /**/double normF(FMatrixRMaj A) {
        return NormOps_FDRM.normF(A);
    }

    @Override
    public /**/double conditionP2(FMatrixRMaj A) {
        return NormOps_FDRM.conditionP2(A);
    }

    @Override
    public /**/double determinant(FMatrixRMaj A) {
        return CommonOps_FDRM.det(A);
    }

    @Override
    public /**/double trace(FMatrixRMaj A) {
        return CommonOps_FDRM.trace(A);
    }

    @Override
    public void setRow(FMatrixRMaj A, int row, int startColumn, /**/double... values) {
        for (int i = 0; i < values.length; i++) {
            A.set(row, startColumn + i, (float)values[i]);
        }
    }

    @Override
    public void setColumn(FMatrixRMaj A, int column, int startRow,  /**/double... values) {
        for (int i = 0; i < values.length; i++) {
            A.set(startRow + i, column, (float)values[i]);
        }
    }

    @Override
    public void extract(FMatrixRMaj src, int srcY0, int srcY1, int srcX0, int srcX1, FMatrixRMaj dst, int dstY0, int dstX0) {
        CommonOps_FDRM.extract(src,srcY0,srcY1,srcX0,srcX1,dst,dstY0,dstX0);
    }

    @Override
    public FMatrixRMaj diag(FMatrixRMaj A) {
        FMatrixRMaj output;
        if (MatrixFeatures_FDRM.isVector(A)) {
            int N = Math.max(A.numCols,A.numRows);
            output = new FMatrixRMaj(N,N);
            CommonOps_FDRM.diag(output,N,A.data);
        } else {
            int N = Math.min(A.numCols,A.numRows);
            output = new FMatrixRMaj(N,1);
            CommonOps_FDRM.extractDiag(A,output);
        }
        return output;
    }

    @Override
    public boolean hasUncountable(FMatrixRMaj M) {
        return MatrixFeatures_FDRM.hasUncountable(M);
    }

    @Override
    public void changeSign(FMatrixRMaj a) {
        CommonOps_FDRM.changeSign(a);
    }

    @Override
    public /**/double elementMaxAbs(FMatrixRMaj A) {
        return CommonOps_FDRM.elementMaxAbs(A);
    }

    @Override
    public /**/double elementMinAbs(FMatrixRMaj A) {
        return CommonOps_FDRM.elementMinAbs(A);
    }

    @Override
    public /**/double elementSum(FMatrixRMaj A) {
        return CommonOps_FDRM.elementSum(A);
    }

    @Override
    public void elementMult(FMatrixRMaj A, FMatrixRMaj B, FMatrixRMaj output) {
        CommonOps_FDRM.elementMult(A,B,output);
    }

    @Override
    public void elementDiv(FMatrixRMaj A, FMatrixRMaj B, FMatrixRMaj output) {
        CommonOps_FDRM.elementDiv(A,B,output);
    }

    @Override
    public void elementPower(FMatrixRMaj A, FMatrixRMaj B, FMatrixRMaj output) {
        CommonOps_FDRM.elementPower(A,B,output);

    }

    @Override
    public void elementPower(FMatrixRMaj A, /**/double b, FMatrixRMaj output) {
        CommonOps_FDRM.elementPower(A, (float)b, output);
    }

    @Override
    public void elementExp(FMatrixRMaj A, FMatrixRMaj output) {
        CommonOps_FDRM.elementExp(A,output);
    }

    @Override
    public void elementLog(FMatrixRMaj A, FMatrixRMaj output) {
        CommonOps_FDRM.elementLog(A,output);
    }

    @Override
    public boolean isIdentical(FMatrixRMaj A, FMatrixRMaj B, /**/double tol) {
        return MatrixFeatures_FDRM.isIdentical(A,B, (float)tol);
    }

    @Override
    public void print(PrintStream out, Matrix mat, String format) {
        MatrixIO.print(out, (FMatrixRMaj)mat, format);
    }
}
