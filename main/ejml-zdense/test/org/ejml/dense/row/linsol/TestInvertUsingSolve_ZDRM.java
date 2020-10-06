/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.linsol;

import org.ejml.UtilEjml;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.MatrixFeatures_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.ejml.dense.row.decompose.lu.LUDecompositionAlt_ZDRM;
import org.ejml.dense.row.linsol.lu.LinearSolverLu_ZDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestInvertUsingSolve_ZDRM {

    Random rand = new Random(0xff);
    double tol = UtilEjml.TEST_F64;

    /**
     * See if it can invert a matrix that is known to be invertable.
     */
    @Test
    public void invert() {
        ZMatrixRMaj A = new ZMatrixRMaj(3, 3, true, 0, 0, 1, 0, 2, 0, -2, 0, 4, 0, 9, 0, 0.5, 0, 0, 0, 5, 0);
        ZMatrixRMaj A_inv = RandomMatrices_ZDRM.rectangle(3, 3, rand);

        LUDecompositionAlt_ZDRM decomp = new LUDecompositionAlt_ZDRM();
        LinearSolverDense<ZMatrixRMaj> solver = new LinearSolverLu_ZDRM(decomp);

        solver.setA(A);
        InvertUsingSolve_ZDRM.invert(solver, A, A_inv);

        ZMatrixRMaj I = RandomMatrices_ZDRM.rectangle(3, 3, rand);

        CommonOps_ZDRM.mult(A, A_inv, I);

        assertTrue(MatrixFeatures_ZDRM.isIdentity(I, tol));
    }
}
