/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.Matrix;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.ejml.ops.MatrixIO;
import org.ejml.simple.SimpleOperations;

import java.io.PrintStream;

/**
 * @author Peter Abeles
 */
public class SimpleOperations_DDRM implements SimpleOperations<DMatrixRMaj> {
    @Override
    public void transpose(DMatrixRMaj input, DMatrixRMaj output) {
        CommonOps_DDRM.transpose(input,output);
    }

    @Override
    public void mult(DMatrixRMaj A, DMatrixRMaj B, DMatrixRMaj output) {
        CommonOps_DDRM.mult(A,B,output);
    }

    @Override
    public void kron(DMatrixRMaj A, DMatrixRMaj B, DMatrixRMaj output) {
        CommonOps_DDRM.kron(A,B,output);
    }

    @Override
    public void plus(DMatrixRMaj A, DMatrixRMaj B, DMatrixRMaj output) {
        CommonOps_DDRM.add(A,B,output);
    }

    @Override
    public void minus(DMatrixRMaj A, DMatrixRMaj B, DMatrixRMaj output) {
        CommonOps_DDRM.subtract(A,B,output);
    }

    @Override
    public void minus(DMatrixRMaj A, /**/double b, DMatrixRMaj output) {
        CommonOps_DDRM.subtract(A, (double)b, output);
    }

    @Override
    public void plus(DMatrixRMaj A, /**/double b, DMatrixRMaj output) {
        CommonOps_DDRM.add(A, (double)b, output);
    }

    @Override
    public void plus(DMatrixRMaj A, /**/double beta, DMatrixRMaj b, DMatrixRMaj output) {
        CommonOps_DDRM.add(A, (double)beta, b, output);
    }

    @Override
    public /**/double dot(DMatrixRMaj A, DMatrixRMaj v) {
        return VectorVectorMult_DDRM.innerProd(A, v);
    }

    @Override
    public void scale(DMatrixRMaj A, /**/double val, DMatrixRMaj output) {
        CommonOps_DDRM.scale( (double)val, A,output);
    }

    @Override
    public void divide(DMatrixRMaj A, /**/double val, DMatrixRMaj output) {
        CommonOps_DDRM.divide( A, (double)val,output);
    }

    @Override
    public boolean invert(DMatrixRMaj A, DMatrixRMaj output) {
        return CommonOps_DDRM.invert(A,output);
    }

    @Override
    public void pseudoInverse(DMatrixRMaj A, DMatrixRMaj output) {
        CommonOps_DDRM.pinv(A,output);
    }

    @Override
    public boolean solve(DMatrixRMaj A, DMatrixRMaj X, DMatrixRMaj B) {
        return CommonOps_DDRM.solve(A,B,X);
    }

    @Override
    public void set(DMatrixRMaj A, /**/double val) {
        CommonOps_DDRM.fill(A, (double)val );
    }

    @Override
    public void zero(DMatrixRMaj A) {
        A.zero();
    }

    @Override
    public /**/double normF(DMatrixRMaj A) {
        return NormOps_DDRM.normF(A);
    }

    @Override
    public /**/double conditionP2(DMatrixRMaj A) {
        return NormOps_DDRM.conditionP2(A);
    }

    @Override
    public /**/double determinant(DMatrixRMaj A) {
        return CommonOps_DDRM.det(A);
    }

    @Override
    public /**/double trace(DMatrixRMaj A) {
        return CommonOps_DDRM.trace(A);
    }

    @Override
    public void setRow(DMatrixRMaj A, int row, int startColumn, /**/double... values) {
        for (int i = 0; i < values.length; i++) {
            A.set(row, startColumn + i, (double)values[i]);
        }
    }

    @Override
    public void setColumn(DMatrixRMaj A, int column, int startRow,  /**/double... values) {
        for (int i = 0; i < values.length; i++) {
            A.set(startRow + i, column, (double)values[i]);
        }
    }

    @Override
    public void extract(DMatrixRMaj src, int srcY0, int srcY1, int srcX0, int srcX1, DMatrixRMaj dst, int dstY0, int dstX0) {
        CommonOps_DDRM.extract(src,srcY0,srcY1,srcX0,srcX1,dst,dstY0,dstX0);
    }

    @Override
    public boolean hasUncountable(DMatrixRMaj M) {
        return MatrixFeatures_DDRM.hasUncountable(M);
    }

    @Override
    public void changeSign(DMatrixRMaj a) {
        CommonOps_DDRM.changeSign(a);
    }

    @Override
    public /**/double elementMaxAbs(DMatrixRMaj A) {
        return CommonOps_DDRM.elementMaxAbs(A);
    }

    @Override
    public /**/double elementSum(DMatrixRMaj A) {
        return CommonOps_DDRM.elementSum(A);
    }

    @Override
    public void elementMult(DMatrixRMaj A, DMatrixRMaj B, DMatrixRMaj output) {
        CommonOps_DDRM.elementMult(A,B,output);
    }

    @Override
    public void elementDiv(DMatrixRMaj A, DMatrixRMaj B, DMatrixRMaj output) {
        CommonOps_DDRM.elementDiv(A,B,output);
    }

    @Override
    public void elementPower(DMatrixRMaj A, DMatrixRMaj B, DMatrixRMaj output) {
        CommonOps_DDRM.elementPower(A,B,output);

    }

    @Override
    public void elementPower(DMatrixRMaj A, /**/double b, DMatrixRMaj output) {
        CommonOps_DDRM.elementPower(A, (double)b, output);
    }

    @Override
    public void elementExp(DMatrixRMaj A, DMatrixRMaj output) {
        CommonOps_DDRM.elementExp(A,output);
    }

    @Override
    public void elementLog(DMatrixRMaj A, DMatrixRMaj output) {
        CommonOps_DDRM.elementLog(A,output);
    }

    @Override
    public void print(PrintStream out, Matrix mat) {
        MatrixIO.print(out, (DMatrixRMaj)mat);
    }
}
