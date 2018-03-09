/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.Matrix;
import org.ejml.ops.MatrixIO;
import org.ejml.simple.ConvertToDenseException;
import org.ejml.simple.SimpleOperations;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.MatrixFeatures_DSCC;
import org.ejml.sparse.csc.NormOps_DSCC;

import java.io.PrintStream;

/**
 * @author Peter Abeles
 */
public class SimpleOperations_SPARSE implements SimpleOperations<DMatrixSparseCSC> {
    
    @Override
    public void transpose(DMatrixSparseCSC input, DMatrixSparseCSC output) {
        CommonOps_DSCC.transpose(input,output,null);
    }

    @Override
    public void mult(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC output) {
        CommonOps_DSCC.mult(A,B,output);
    }

    @Override
    public void kron(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC output) {
//        CommonOps_DSCC.kron(A,B,output);
        throw new RuntimeException("Unsupported");
    }

    @Override
    public void plus(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC output) {
        CommonOps_DSCC.add(1,A,1,B,output, null, null);
    }

    @Override
    public void minus(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC output) {
        CommonOps_DSCC.add(1,A,-1,B,output, null, null);
    }

    @Override
    public void minus(DMatrixSparseCSC A, /**/double b, DMatrixSparseCSC output) {
        throw new ConvertToDenseException();
    }

    @Override
    public void plus(DMatrixSparseCSC A, /**/double b, DMatrixSparseCSC output) {
        throw new ConvertToDenseException();
    }

    @Override
    public void plus(DMatrixSparseCSC A, /**/double beta, DMatrixSparseCSC b, DMatrixSparseCSC output) {
        CommonOps_DSCC.add(1, A, (double)beta, b, output,null,null);
    }

    @Override
    public /**/double dot(DMatrixSparseCSC A, DMatrixSparseCSC v) {
        return CommonOps_DSCC.dotInnerColumns(A,0, v,0,null,null);
    }

    @Override
    public void scale(DMatrixSparseCSC A, /**/double val, DMatrixSparseCSC output) {
        CommonOps_DSCC.scale( (double)val, A,output);
    }

    @Override
    public void divide(DMatrixSparseCSC A, /**/double val, DMatrixSparseCSC output) {
        CommonOps_DSCC.divide( A, (double)val, output);
    }

    @Override
    public boolean invert(DMatrixSparseCSC A, DMatrixSparseCSC output) {
        throw new RuntimeException("Unsupported");
    }

    @Override
    public void setIdentity(DMatrixSparseCSC A) {
        CommonOps_DSCC.setIdentity(A);
    }

    @Override
    public void pseudoInverse(DMatrixSparseCSC A, DMatrixSparseCSC output) {
        throw new RuntimeException("Unsupported");
    }

    @Override
    public boolean solve(DMatrixSparseCSC A, DMatrixSparseCSC X, DMatrixSparseCSC B) {
        throw new RuntimeException("Unsupported");
    }

    @Override
    public void set(DMatrixSparseCSC A, /**/double val) {
        throw new ConvertToDenseException();
    }

    @Override
    public void zero(DMatrixSparseCSC A) {
        A.zero();
    }

    @Override
    public /**/double normF(DMatrixSparseCSC A) {
        return NormOps_DSCC.normF(A);
    }

    @Override
    public /**/double conditionP2(DMatrixSparseCSC A) {
        throw new RuntimeException("Unsupported");
    }

    @Override
    public /**/double determinant(DMatrixSparseCSC A) {
        return CommonOps_DSCC.det(A);
    }

    @Override
    public /**/double trace(DMatrixSparseCSC A) {
        return CommonOps_DSCC.trace(A);
    }

    @Override
    public void setRow(DMatrixSparseCSC A, int row, int startColumn, /**/double... values) {
        // TODO Update with a more efficient algorithm
        for (int i = 0; i < values.length; i++) {
            A.set(row, startColumn + i, (double)values[i]);
        }
        // check to see if value are zero, if so ignore them

        // Do a pass through the matrix and see how many elements need to be added

        // see if the existing storage is enough

        // If it is enough ...
        // starting from the tail, move a chunk, insert, move the next chunk, ...etc

        // If not enough, create new arrays and construct it
    }

    @Override
    public void setColumn(DMatrixSparseCSC A, int column, int startRow,  /**/double... values) {
        // TODO Update with a more efficient algorithm
        for (int i = 0; i < values.length; i++) {
            A.set(startRow + i, column, (double)values[i]);
        }
    }

    @Override
    public void extract(DMatrixSparseCSC src, int srcY0, int srcY1, int srcX0, int srcX1, DMatrixSparseCSC dst, int dstY0, int dstX0) {
        CommonOps_DSCC.extract(src,srcY0,srcY1,srcX0,srcX1,dst,dstY0,dstX0);
    }

    @Override
    public boolean hasUncountable(DMatrixSparseCSC M) {
        return MatrixFeatures_DSCC.hasUncountable(M);
    }

    @Override
    public void changeSign(DMatrixSparseCSC a) {
        CommonOps_DSCC.changeSign(a,a);
    }

    @Override
    public /**/double elementMaxAbs(DMatrixSparseCSC A) {
        return CommonOps_DSCC.elementMaxAbs(A);
    }

    @Override
    public /**/double elementSum(DMatrixSparseCSC A) {
        return CommonOps_DSCC.elementSum(A);
    }

    @Override
    public void elementMult(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC output) {
        CommonOps_DSCC.elementMult(A,B,output,null,null);
    }

    @Override
    public void elementDiv(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC output) {
        throw new ConvertToDenseException();
    }

    @Override
    public void elementPower(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC output) {
        throw new ConvertToDenseException();
    }

    @Override
    public void elementPower(DMatrixSparseCSC A, /**/double b, DMatrixSparseCSC output) {
        throw new ConvertToDenseException();
    }

    @Override
    public void elementExp(DMatrixSparseCSC A, DMatrixSparseCSC output) {
        throw new ConvertToDenseException();
    }

    @Override
    public void elementLog(DMatrixSparseCSC A, DMatrixSparseCSC output) {
        throw new ConvertToDenseException();
    }

    @Override
    public void print(PrintStream out, Matrix mat) {
        MatrixIO.print(out, (DMatrixSparseCSC)mat);
    }
}
