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

package org.ejml.sparse.csc.mult;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.mult.MatrixVectorMult_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.ejml.ops.ConvertDMatrixStruct;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestMatrixVectorMult_DSCC {
    Random rand = new Random(234);

    @Test
    public void mult_A_v() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,4,14,rand);

        int offset = 2;
        double v[] = new double[]{0,1,2,3,4,5,6,7};
        double found[] = new double[7];

        DMatrixRMaj Ad = ConvertDMatrixStruct.convert(A,(DMatrixRMaj)null);
        DMatrixRMaj vd = new DMatrixRMaj(4,1);
        System.arraycopy(v,2,vd.data,0,4);

        DMatrixRMaj expected = new DMatrixRMaj(6,1);

        MatrixVectorMult_DSCC.mult(A,v,offset,found,1);
        MatrixVectorMult_DDRM.mult(Ad,vd,expected);

        for (int i = 0; i < A.numRows; i++) {
            assertEquals(expected.data[i],found[i+1], UtilEjml.TEST_F64);
        }
    }

    @Test
    public void multAdd_A_v() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,4,14,rand);

        int offset = 2;
        double v[] = new double[]{0,1,2,3,4,5,6,7};
        double found[] = new double[7];
        found[2] = 3;


        DMatrixRMaj Ad = ConvertDMatrixStruct.convert(A,(DMatrixRMaj)null);
        DMatrixRMaj vd = new DMatrixRMaj(4,1);
        System.arraycopy(v,2,vd.data,0,4);

        DMatrixRMaj expected = new DMatrixRMaj(6,1);
        expected.data[1] = 3;

        MatrixVectorMult_DSCC.multAdd(A,v,offset,found,1);
        MatrixVectorMult_DDRM.multAdd(Ad,vd,expected);

        for (int i = 0; i < A.numRows; i++) {
            assertEquals(expected.data[i],found[i+1], UtilEjml.TEST_F64);
        }
    }

    @Test
    public void mult_v_A() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,4,14,rand);

        int offset = 1;
        double v[] = new double[]{0,1,2,3,4,5,6,7};
        double found[] = new double[5];

        DMatrixRMaj Ad = ConvertDMatrixStruct.convert(A,(DMatrixRMaj)null);
        DMatrixRMaj vd = new DMatrixRMaj(6,1);
        System.arraycopy(v,offset,vd.data,0,A.numRows);

        DMatrixRMaj expected = new DMatrixRMaj(6,1);

        MatrixVectorMult_DSCC.mult(v,offset,A,found,1);
        CommonOps_DDRM.multTransA(vd,Ad,expected);

        for (int i = 0; i < A.numCols; i++) {
            assertEquals(expected.data[i],found[i+1], UtilEjml.TEST_F64);
        }
    }

    @Test
    public void innerProduct() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,4,14,rand);

        int offsetV = 1;
        double v[] = new double[]{0,1,2,3,4,5,6,7};
        int offsetW = 2;
        double w[] = new double[]{2,0,1,9,3,1,6,7};

        double found = MatrixVectorMult_DSCC.innerProduct(v,offsetV,A,w,offsetW);

        DMatrixRMaj Ad = ConvertDMatrixStruct.convert(A,(DMatrixRMaj)null);
        DMatrixRMaj vd = new DMatrixRMaj(A.numRows,1);
        DMatrixRMaj wd = new DMatrixRMaj(A.numCols,1);
        System.arraycopy(v,offsetV,vd.data,0,A.numRows);
        System.arraycopy(w,offsetW,wd.data,0,A.numCols);

        double expected = VectorVectorMult_DDRM.innerProdA(vd,Ad,wd);

        assertEquals(expected,found, UtilEjml.TEST_F64);
    }
}