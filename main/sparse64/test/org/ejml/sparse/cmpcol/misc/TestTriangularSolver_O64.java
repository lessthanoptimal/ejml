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

package org.ejml.sparse.cmpcol.misc;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.SMatrixCmpC_F64;
import org.ejml.dense.row.MatrixFeatures_R64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.ejml.sparse.cmpcol.CommonOps_O64;
import org.ejml.sparse.cmpcol.RandomMatrices_O64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestTriangularSolver_O64 {

    Random rand = new Random(234);

    @Test
    public void solveL_denseX() {
        for (int nz_size : new int[]{5, 8, 10, 20}) {
            SMatrixCmpC_F64 L = RandomMatrices_O64.createLowerTriangular(5, 0, nz_size, -1, 1, rand);
            DMatrixRow_F64 b = RandomMatrices_R64.createRandom(5, 1, rand);
            DMatrixRow_F64 x = b.copy();

            TriangularSolver_O64.solveL(L, x.data);

            DMatrixRow_F64 found = x.createLike();
            CommonOps_O64.mult(L, x, found);

            assertTrue(MatrixFeatures_R64.isIdentical(found, b, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void solveU_denseX() {
        for (int nz_size : new int[]{5, 8, 10, 20}) {
            SMatrixCmpC_F64 L = RandomMatrices_O64.createLowerTriangular(5, 0, nz_size, -1, 1, rand);
            SMatrixCmpC_F64 U = new SMatrixCmpC_F64(5, 5, L.length);
            CommonOps_O64.transpose(L, U, null);

            DMatrixRow_F64 b = RandomMatrices_R64.createRandom(5, 1, rand);
            DMatrixRow_F64 x = b.copy();

            TriangularSolver_O64.solveU(U, x.data);

            DMatrixRow_F64 found = x.createLike();
            CommonOps_O64.mult(U, x, found);

            assertTrue(MatrixFeatures_R64.isIdentical(found, b, UtilEjml.TEST_F64));
        }
    }

}