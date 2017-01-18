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

package org.ejml.sparse.cmpcol.mult;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.SMatrixCmpC_F64;
import org.ejml.dense.row.CommonOps_R64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.ejml.sparse.ConvertSparseMatrix_F64;
import org.ejml.sparse.cmpcol.CommonOps_O64;
import org.ejml.sparse.cmpcol.RandomMatrices_O64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestImplSparseSparseMult_O64 {

    Random rand = new Random(234);

    @Test
    public void mult_s_s() {
        for (int i = 0; i < 10; i++) {
            mult_s_s(24,30,20);
            mult_s_s(15,15,20);
            mult_s_s(15,15,5);
            mult_s_s(4,5,0);
        }
    }

    private void mult_s_s(int elementsA , int elementsB , int elementsC ) {
        SMatrixCmpC_F64 a = RandomMatrices_O64.rectangle(4,6,elementsA,-1,1,rand);
        SMatrixCmpC_F64 b = RandomMatrices_O64.rectangle(6,5,elementsB,-1,1,rand);
        SMatrixCmpC_F64 c = RandomMatrices_O64.rectangle(4,5,elementsC,-1,1,rand);

        ImplSparseSparseMult_O64.mult(a,b,c, null, null);
        assertTrue(CommonOps_O64.checkSortedFlag(c));

        DMatrixRow_F64 dense_a = ConvertSparseMatrix_F64.convert(a,(DMatrixRow_F64)null);
        DMatrixRow_F64 dense_b = ConvertSparseMatrix_F64.convert(b,(DMatrixRow_F64)null);
        DMatrixRow_F64 dense_c = new DMatrixRow_F64(dense_a.numRows, dense_b.numCols);

        CommonOps_R64.mult(dense_a, dense_b, dense_c);

        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(row+" "+col,dense_c.get(row,col), c.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void mult_s_d() {
        for (int i = 0; i < 10; i++) {
            mult_s_d(24);
            mult_s_d(15);
            mult_s_d(4);
        }
    }

    private void mult_s_d(int elementsA) {
        SMatrixCmpC_F64 a = RandomMatrices_O64.rectangle(4,6,elementsA,-1,1,rand);
        DMatrixRow_F64 b = RandomMatrices_R64.createRandom(6,5,-1,1,rand);
        DMatrixRow_F64 c = RandomMatrices_R64.createRandom(4,5,-1,1,rand);

        ImplSparseSparseMult_O64.mult(a,b,c);

        DMatrixRow_F64 dense_a = ConvertSparseMatrix_F64.convert(a,(DMatrixRow_F64)null);
        DMatrixRow_F64 expected_c = RandomMatrices_R64.createRandom(4,5,-1,1,rand);

        CommonOps_R64.mult(dense_a, b, expected_c);

        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(row+" "+col,expected_c.get(row,col), c.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }
}