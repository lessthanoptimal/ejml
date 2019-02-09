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
import org.ejml.data.Matrix;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.MatrixFeatures_ZDRM;
import org.ejml.dense.row.NormOps_ZDRM;
import org.ejml.ops.MatrixIO;
import org.ejml.simple.SimpleOperations;
import org.ejml.simple.UnsupportedOperation;

import java.io.PrintStream;

//CUSTOM ignore Complex_F64
//CUSTOM ignore org.ejml.data.Complex_F64;

/**
 * @author Peter Abeles
 */
public class SimpleOperations_ZDRM implements SimpleOperations<ZMatrixRMaj> {
    @Override
    public void set(ZMatrixRMaj A, int row, int column, /**/double value) {
        A.set(row,column, (double)value, 0);
    }

    @Override
    public void set(ZMatrixRMaj A, int row, int column, /**/double real, /**/double imaginary) {
        A.set(row,column, (double)real, (double)imaginary);
    }

    @Override
    public /**/double get(ZMatrixRMaj A, int row, int column) {
        return (double)A.getReal(row,column);
    }

    @Override
    public void get(ZMatrixRMaj A, int row, int column, Complex_F64 value) {
        int index = A.getIndex(row,column);
        value.real = A.data[index];
        value.imaginary = A.data[index+1];
    }

    @Override
    public void fill(ZMatrixRMaj A, /**/double value) {
        CommonOps_ZDRM.fill(A, (double)value,0);
    }

    @Override
    public void transpose(ZMatrixRMaj input, ZMatrixRMaj output) {
        CommonOps_ZDRM.transpose(input,output);
    }

    @Override
    public void mult(ZMatrixRMaj A, ZMatrixRMaj B, ZMatrixRMaj output) {
        CommonOps_ZDRM.mult(A,B,output);
    }

    @Override
    public void multTransA(ZMatrixRMaj A, ZMatrixRMaj B, ZMatrixRMaj output) {
        CommonOps_ZDRM.multTransA(A,B,output);
    }

    @Override
    public void kron(ZMatrixRMaj A, ZMatrixRMaj B, ZMatrixRMaj output) {
//        CommonOps_ZDRM.kron(A,B,output);
        throw new UnsupportedOperation();
    }

    @Override
    public void plus(ZMatrixRMaj A, ZMatrixRMaj B, ZMatrixRMaj output) {
        CommonOps_ZDRM.add(A,B,output);
    }

    @Override
    public void minus(ZMatrixRMaj A, ZMatrixRMaj B, ZMatrixRMaj output) {
        CommonOps_ZDRM.subtract(A,B,output);
    }

    @Override
    public void minus(ZMatrixRMaj A, /**/double b, ZMatrixRMaj output) {
//        CommonOps_ZDRM.subtract(A, (double)b, output);
        throw new UnsupportedOperation();
    }

    @Override
    public void plus(ZMatrixRMaj A, /**/double b, ZMatrixRMaj output) {
//        CommonOps_ZDRM.add(A, (double)b, output);
        throw new UnsupportedOperation();
    }

    @Override
    public void plus(ZMatrixRMaj A, /**/double beta, ZMatrixRMaj b, ZMatrixRMaj output) {
//        CommonOps_ZDRM.add(A, (double)beta, b, output);
        throw new UnsupportedOperation();
    }

    @Override
    public void plus( /**/double alpha, ZMatrixRMaj A, /**/double beta, ZMatrixRMaj b, ZMatrixRMaj output) {
        throw new UnsupportedOperation();
    }

    @Override
    public /**/double dot(ZMatrixRMaj A, ZMatrixRMaj v) {
//        return VectorVectorMult_DDRM.innerProd(A, v);
        throw new UnsupportedOperation();
    }

    @Override
    public void scale(ZMatrixRMaj A, /**/double val, ZMatrixRMaj output) {
//        CommonOps_ZDRM.scale( (double)val, 0,A,output);
        throw new UnsupportedOperation();
    }

    @Override
    public void divide(ZMatrixRMaj A, /**/double val, ZMatrixRMaj output) {
//        CommonOps_ZDRM.divide( A, (double)val,output);
        throw new UnsupportedOperation();
    }

    @Override
    public boolean invert(ZMatrixRMaj A, ZMatrixRMaj output) {
        return CommonOps_ZDRM.invert(A,output);
    }

    @Override
    public void setIdentity(ZMatrixRMaj A) {
        CommonOps_ZDRM.setIdentity(A);
    }

    @Override
    public void pseudoInverse(ZMatrixRMaj A, ZMatrixRMaj output) {
//        CommonOps_ZDRM.pinv(A,output);
        throw new UnsupportedOperation();
    }

    @Override
    public boolean solve(ZMatrixRMaj A, ZMatrixRMaj X, ZMatrixRMaj B) {
        return CommonOps_ZDRM.solve(A,B,X);
    }

    @Override
    public void zero(ZMatrixRMaj A) {
        A.zero();
    }

    @Override
    public /**/double normF(ZMatrixRMaj A) {
        return NormOps_ZDRM.normF(A);
    }

    @Override
    public /**/double conditionP2(ZMatrixRMaj A) {
//        return NormOps_ZDRM.conditionP2(A);
        throw new UnsupportedOperation();
    }

    @Override
    public /**/double determinant(ZMatrixRMaj A) {
        return CommonOps_ZDRM.det(A).real;
    }

    @Override
    public /**/double trace(ZMatrixRMaj A) {
//        return CommonOps_ZDRM.trace(A);
        throw new UnsupportedOperation();
    }

    @Override
    public void setRow(ZMatrixRMaj A, int row, int startColumn, /**/double... values) {
        for (int i = 0; i < values.length; i++) {
            A.set(row, startColumn + i, (double)values[i],0);
        }
    }

    @Override
    public void setColumn(ZMatrixRMaj A, int column, int startRow,  /**/double... values) {
        for (int i = 0; i < values.length; i++) {
            A.set(startRow + i, column, (double)values[i],0);
        }
    }

    @Override
    public void extract(ZMatrixRMaj src, int srcY0, int srcY1, int srcX0, int srcX1, ZMatrixRMaj dst, int dstY0, int dstX0) {
        CommonOps_ZDRM.extract(src,srcY0,srcY1,srcX0,srcX1,dst,dstY0,dstX0);
    }

    @Override
    public ZMatrixRMaj diag(ZMatrixRMaj A) {
        ZMatrixRMaj output;
        if (MatrixFeatures_ZDRM.isVector(A)) {
            int N = Math.max(A.numCols,A.numRows);
            output = new ZMatrixRMaj(N,N);
            CommonOps_ZDRM.diag(output,N,A.data);
        } else {
            int N = Math.min(A.numCols,A.numRows);
            output = new ZMatrixRMaj(N,1);
            CommonOps_ZDRM.extractDiag(A,output);
        }
        return output;
    }

    @Override
    public boolean hasUncountable(ZMatrixRMaj M) {
        return MatrixFeatures_ZDRM.hasUncountable(M);
    }

    @Override
    public void changeSign(ZMatrixRMaj a) {
//        CommonOps_ZDRM.changeSign(a);
        throw new UnsupportedOperation();
    }

    @Override
    public /**/double elementMaxAbs(ZMatrixRMaj A) {
        return CommonOps_ZDRM.elementMaxAbs(A);
    }

    @Override
    public /**/double elementMinAbs(ZMatrixRMaj A) {
        return CommonOps_ZDRM.elementMinAbs(A);
    }

    @Override
    public /**/double elementSum(ZMatrixRMaj A) {
//        return CommonOps_ZDRM.elementSum(A);
        throw new UnsupportedOperation();
    }

    @Override
    public void elementMult(ZMatrixRMaj A, ZMatrixRMaj B, ZMatrixRMaj output) {
//        CommonOps_ZDRM.elementMult(A,B,output);
        throw new UnsupportedOperation();
    }

    @Override
    public void elementDiv(ZMatrixRMaj A, ZMatrixRMaj B, ZMatrixRMaj output) {
//        CommonOps_ZDRM.elementDiv(A,B,output);
        throw new UnsupportedOperation();
    }

    @Override
    public void elementPower(ZMatrixRMaj A, ZMatrixRMaj B, ZMatrixRMaj output) {
//        CommonOps_ZDRM.elementPower(A,B,output);
        throw new UnsupportedOperation();

    }

    @Override
    public void elementPower(ZMatrixRMaj A, /**/double b, ZMatrixRMaj output) {
//        CommonOps_ZDRM.elementPower(A, (double)b, output);
        throw new UnsupportedOperation();
    }

    @Override
    public void elementExp(ZMatrixRMaj A, ZMatrixRMaj output) {
//        CommonOps_ZDRM.elementExp(A,output);
        throw new UnsupportedOperation();
    }

    @Override
    public void elementLog(ZMatrixRMaj A, ZMatrixRMaj output) {
//        CommonOps_ZDRM.elementLog(A,output);
        throw new UnsupportedOperation();
    }

    @Override
    public boolean isIdentical(ZMatrixRMaj A, ZMatrixRMaj B, /**/double tol) {
        return MatrixFeatures_ZDRM.isIdentical(A, B, (double)tol);
    }

    @Override
    public void print(PrintStream out, Matrix mat, String format) {
        MatrixIO.print(out, (ZMatrixRMaj)mat, format);
    }
}
