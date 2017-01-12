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

package org.ejml.sparse.cmpcol;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.SMatrixCC_F64;
import org.ejml.dense.row.CommonOps_R64;
import org.ejml.dense.row.MatrixFeatures_R64;
import org.ejml.sparse.ConvertSparseMatrix_F64;
import org.junit.Test;

import java.util.Random;

import static org.ejml.sparse.cmpcol.RandomMatrices_O64.uniform;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestCommonOps_O64 {

    Random rand = new Random(234);

    @Test
    public void transpose_shapes() {
        CommonOps_O64.transpose(
                uniform(5,5,5,rand),
                uniform(5,5,7,rand),null);
        CommonOps_O64.transpose(
                uniform(4,5,5,rand),
                uniform(5,4,7,rand),null);

        try {
            CommonOps_O64.transpose(
                    uniform(4,5,5,rand),
                    uniform(6,4,7,rand),null);
            fail("exception expected");
        } catch( RuntimeException ignore){}

        try {
            CommonOps_O64.transpose(
                    uniform(4,5,5,rand),
                    uniform(5,5,7,rand),null);
            fail("exception expected");
        } catch( RuntimeException ignore){}

    }

    @Test
    public void mult_shapes() {
        check_mult(
                uniform(5, 6, 5, rand),
                uniform(6, 4, 7, rand),
                uniform(5, 4, 7, rand), false);

        check_mult(
                uniform(5, 6, 5, rand),
                uniform(6, 4, 7, rand),
                uniform(5, 5, 7, rand), true);
        check_mult(
                uniform(5, 6, 5, rand),
                uniform(6, 4, 7, rand),
                uniform(6, 4, 7, rand), true);
        check_mult(
                uniform(5, 6, 5, rand),
                uniform(6, 4, 7, rand),
                uniform(6, 4, 7, rand), true);
    }

    private void check_mult(SMatrixCC_F64 A , SMatrixCC_F64 B, SMatrixCC_F64 C, boolean exception ) {
        try {
            CommonOps_O64.mult(A,B,C,null,null);

            if( exception )
                fail("exception expected");
            DMatrixRow_F64 denseA = ConvertSparseMatrix_F64.convert(A,(DMatrixRow_F64)null);
            DMatrixRow_F64 denseB = ConvertSparseMatrix_F64.convert(B,(DMatrixRow_F64)null);
            DMatrixRow_F64 expected = new DMatrixRow_F64(A.numRows,B.numCols);

            CommonOps_R64.mult(denseA,denseB,expected);

            DMatrixRow_F64 found = ConvertSparseMatrix_F64.convert(C,(DMatrixRow_F64)null);
            assertTrue(MatrixFeatures_R64.isIdentical(expected,found, UtilEjml.TEST_F64));

        } catch( RuntimeException ignore){
            if( !exception )
                fail("no exception expected");
        }
    }

    @Test
    public void add_shapes() {
        check_add(
                uniform(5, 6, 5, rand),
                uniform(5, 6, 5, rand),
                uniform(5, 6, 5, rand), false);
        check_add(
                uniform(5, 6, 5*6, rand),
                uniform(5, 6, 5*6, rand),
                uniform(5, 6, 5*6, rand), false);
        check_add(
                uniform(5, 6, 0, rand),
                uniform(5, 6, 0, rand),
                uniform(5, 6, 0, rand), false);
        check_add(
                uniform(5, 6, 20, rand),
                uniform(5, 6, 16, rand),
                uniform(5, 6, 0, rand), false);
        check_add(
                uniform(5, 6, 5, rand),
                uniform(5, 5, 5, rand),
                uniform(5, 6, 5, rand), true);
        check_add(
                uniform(5, 6, 5, rand),
                uniform(5, 6, 5, rand),
                uniform(5, 5, 5, rand), true);
        check_add(
                uniform(5, 6, 5, rand),
                uniform(4, 6, 5, rand),
                uniform(5, 6, 5, rand), true);
        check_add(
                uniform(5, 6, 5, rand),
                uniform(5, 6, 5, rand),
                uniform(4, 6, 5, rand), true);

    }

    private void check_add(SMatrixCC_F64 A , SMatrixCC_F64 B, SMatrixCC_F64 C, boolean exception ) {
        double alpha = 1.5;
        double beta = -0.6;
        try {
            CommonOps_O64.add(alpha,A,beta,B,C,null,null,null);

            if( exception )
                fail("exception expected");
            DMatrixRow_F64 denseA = ConvertSparseMatrix_F64.convert(A,(DMatrixRow_F64)null);
            DMatrixRow_F64 denseB = ConvertSparseMatrix_F64.convert(B,(DMatrixRow_F64)null);
            DMatrixRow_F64 expected = new DMatrixRow_F64(A.numRows,B.numCols);

            CommonOps_R64.add(alpha,denseA,beta,denseB,expected);

            DMatrixRow_F64 found = ConvertSparseMatrix_F64.convert(C,(DMatrixRow_F64)null);
            assertTrue(MatrixFeatures_R64.isIdentical(expected,found, UtilEjml.TEST_F64));

        } catch( RuntimeException ignore){
            if( !exception )
                fail("no exception expected");
        }
    }

    public static void checkEquals(SMatrixCC_F64 A , DMatrixRow_F64 B , double tol ) {
        DMatrixRow_F64 denseA = ConvertSparseMatrix_F64.convert(A,(DMatrixRow_F64)null);

        assertTrue(MatrixFeatures_R64.isIdentical(denseA,B,tol));
    }
}