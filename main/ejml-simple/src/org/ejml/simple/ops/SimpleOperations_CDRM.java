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
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.MatrixFeatures_CDRM;
import org.ejml.dense.row.NormOps_CDRM;
import org.ejml.ops.MatrixIO;
import org.ejml.simple.SimpleOperations;
import org.ejml.simple.UnsupportedOperation;

import java.io.PrintStream;

//CUSTOM ignore Complex_F64
//CUSTOM ignore org.ejml.data.Complex_F64;

/**
 * @author Peter Abeles
 */
public class SimpleOperations_CDRM implements SimpleOperations<CMatrixRMaj> {
    @Override
    public void set(CMatrixRMaj A, int row, int column, /**/double value) {
        A.set(row,column, (float)value, 0);
    }

    @Override
    public void set(CMatrixRMaj A, int row, int column, /**/double real, /**/double imaginary) {
        A.set(row,column, (float)real, (float)imaginary);
    }

    @Override
    public /**/double get(CMatrixRMaj A, int row, int column) {
        return (float)A.getReal(row,column);
    }

    @Override
    public void get(CMatrixRMaj A, int row, int column, Complex_F64 value) {
        int index = A.getIndex(row,column);
        value.real = A.data[index];
        value.imaginary = A.data[index+1];
    }

    @Override
    public void fill(CMatrixRMaj A, /**/double value) {
        CommonOps_CDRM.fill(A, (float)value,0);
    }

    @Override
    public void transpose(CMatrixRMaj input, CMatrixRMaj output) {
        CommonOps_CDRM.transpose(input,output);
    }

    @Override
    public void mult(CMatrixRMaj A, CMatrixRMaj B, CMatrixRMaj output) {
        CommonOps_CDRM.mult(A,B,output);
    }

    @Override
    public void multTransA(CMatrixRMaj A, CMatrixRMaj B, CMatrixRMaj output) {
        CommonOps_CDRM.multTransA(A,B,output);
    }

    @Override
    public void kron(CMatrixRMaj A, CMatrixRMaj B, CMatrixRMaj output) {
//        CommonOps_CDRM.kron(A,B,output);
        throw new UnsupportedOperation();
    }

    @Override
    public void plus(CMatrixRMaj A, CMatrixRMaj B, CMatrixRMaj output) {
        CommonOps_CDRM.add(A,B,output);
    }

    @Override
    public void minus(CMatrixRMaj A, CMatrixRMaj B, CMatrixRMaj output) {
        CommonOps_CDRM.subtract(A,B,output);
    }

    @Override
    public void minus(CMatrixRMaj A, /**/double b, CMatrixRMaj output) {
//        CommonOps_CDRM.subtract(A, (float)b, output);
        throw new UnsupportedOperation();
    }

    @Override
    public void plus(CMatrixRMaj A, /**/double b, CMatrixRMaj output) {
//        CommonOps_CDRM.add(A, (float)b, output);
        throw new UnsupportedOperation();
    }

    @Override
    public void plus(CMatrixRMaj A, /**/double beta, CMatrixRMaj b, CMatrixRMaj output) {
//        CommonOps_CDRM.add(A, (float)beta, b, output);
        throw new UnsupportedOperation();
    }

    @Override
    public void plus( /**/double alpha, CMatrixRMaj A, /**/double beta, CMatrixRMaj b, CMatrixRMaj output) {
        throw new UnsupportedOperation();
    }

    @Override
    public /**/double dot(CMatrixRMaj A, CMatrixRMaj v) {
//        return VectorVectorMult_FDRM.innerProd(A, v);
        throw new UnsupportedOperation();
    }

    @Override
    public void scale(CMatrixRMaj A, /**/double val, CMatrixRMaj output) {
//        CommonOps_CDRM.scale( (float)val, 0,A,output);
        throw new UnsupportedOperation();
    }

    @Override
    public void divide(CMatrixRMaj A, /**/double val, CMatrixRMaj output) {
//        CommonOps_CDRM.divide( A, (float)val,output);
        throw new UnsupportedOperation();
    }

    @Override
    public boolean invert(CMatrixRMaj A, CMatrixRMaj output) {
        return CommonOps_CDRM.invert(A,output);
    }

    @Override
    public void setIdentity(CMatrixRMaj A) {
        CommonOps_CDRM.setIdentity(A);
    }

    @Override
    public void pseudoInverse(CMatrixRMaj A, CMatrixRMaj output) {
//        CommonOps_CDRM.pinv(A,output);
        throw new UnsupportedOperation();
    }

    @Override
    public boolean solve(CMatrixRMaj A, CMatrixRMaj X, CMatrixRMaj B) {
        return CommonOps_CDRM.solve(A,B,X);
    }

    @Override
    public void zero(CMatrixRMaj A) {
        A.zero();
    }

    @Override
    public /**/double normF(CMatrixRMaj A) {
        return NormOps_CDRM.normF(A);
    }

    @Override
    public /**/double conditionP2(CMatrixRMaj A) {
//        return NormOps_CDRM.conditionP2(A);
        throw new UnsupportedOperation();
    }

    @Override
    public /**/double determinant(CMatrixRMaj A) {
        return CommonOps_CDRM.det(A).real;
    }

    @Override
    public /**/double trace(CMatrixRMaj A) {
//        return CommonOps_CDRM.trace(A);
        throw new UnsupportedOperation();
    }

    @Override
    public void setRow(CMatrixRMaj A, int row, int startColumn, /**/double... values) {
        for (int i = 0; i < values.length; i++) {
            A.set(row, startColumn + i, (float)values[i],0);
        }
    }

    @Override
    public void setColumn(CMatrixRMaj A, int column, int startRow,  /**/double... values) {
        for (int i = 0; i < values.length; i++) {
            A.set(startRow + i, column, (float)values[i],0);
        }
    }

    @Override
    public void extract(CMatrixRMaj src, int srcY0, int srcY1, int srcX0, int srcX1, CMatrixRMaj dst, int dstY0, int dstX0) {
        CommonOps_CDRM.extract(src,srcY0,srcY1,srcX0,srcX1,dst,dstY0,dstX0);
    }

    @Override
    public CMatrixRMaj diag(CMatrixRMaj A) {
        CMatrixRMaj output;
        if (MatrixFeatures_CDRM.isVector(A)) {
            int N = Math.max(A.numCols,A.numRows);
            output = new CMatrixRMaj(N,N);
            CommonOps_CDRM.diag(output,N,A.data);
        } else {
            int N = Math.min(A.numCols,A.numRows);
            output = new CMatrixRMaj(N,1);
            CommonOps_CDRM.extractDiag(A,output);
        }
        return output;
    }

    @Override
    public boolean hasUncountable(CMatrixRMaj M) {
        return MatrixFeatures_CDRM.hasUncountable(M);
    }

    @Override
    public void changeSign(CMatrixRMaj a) {
//        CommonOps_CDRM.changeSign(a);
        throw new UnsupportedOperation();
    }

    @Override
    public /**/double elementMaxAbs(CMatrixRMaj A) {
        return CommonOps_CDRM.elementMaxAbs(A);
    }

    @Override
    public /**/double elementMinAbs(CMatrixRMaj A) {
        return CommonOps_CDRM.elementMinAbs(A);
    }

    @Override
    public /**/double elementSum(CMatrixRMaj A) {
//        return CommonOps_CDRM.elementSum(A);
        throw new UnsupportedOperation();
    }

    @Override
    public void elementMult(CMatrixRMaj A, CMatrixRMaj B, CMatrixRMaj output) {
//        CommonOps_CDRM.elementMult(A,B,output);
        throw new UnsupportedOperation();
    }

    @Override
    public void elementDiv(CMatrixRMaj A, CMatrixRMaj B, CMatrixRMaj output) {
//        CommonOps_CDRM.elementDiv(A,B,output);
        throw new UnsupportedOperation();
    }

    @Override
    public void elementPower(CMatrixRMaj A, CMatrixRMaj B, CMatrixRMaj output) {
//        CommonOps_CDRM.elementPower(A,B,output);
        throw new UnsupportedOperation();

    }

    @Override
    public void elementPower(CMatrixRMaj A, /**/double b, CMatrixRMaj output) {
//        CommonOps_CDRM.elementPower(A, (float)b, output);
        throw new UnsupportedOperation();
    }

    @Override
    public void elementExp(CMatrixRMaj A, CMatrixRMaj output) {
//        CommonOps_CDRM.elementExp(A,output);
        throw new UnsupportedOperation();
    }

    @Override
    public void elementLog(CMatrixRMaj A, CMatrixRMaj output) {
//        CommonOps_CDRM.elementLog(A,output);
        throw new UnsupportedOperation();
    }

    @Override
    public boolean isIdentical(CMatrixRMaj A, CMatrixRMaj B, /**/double tol) {
        return MatrixFeatures_CDRM.isIdentical(A, B, (float)tol);
    }

    @Override
    public void print(PrintStream out, Matrix mat, String format) {
        MatrixIO.print(out, (CMatrixRMaj)mat, format);
    }
}
