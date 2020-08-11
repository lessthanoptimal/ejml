/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.MatrixFeatures_CDRM;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.ejml.dense.row.decompose.lu.LUDecompositionAlt_CDRM;
import org.ejml.dense.row.linsol.lu.LinearSolverLu_CDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestInvertUsingSolve_CDRM {

    Random rand = new Random(0xff);
    float tol = UtilEjml.TEST_F32;

    /**
     * See if it can invert a matrix that is known to be invertable.
     */
    @Test
    public void invert() {
        CMatrixRMaj A = new CMatrixRMaj(3,3, true, 0,0, 1,0, 2,0, -2,0, 4,0, 9,0, 0.5f,0, 0,0, 5,0);
        CMatrixRMaj A_inv = RandomMatrices_CDRM.rectangle(3, 3, rand);

        LUDecompositionAlt_CDRM decomp = new LUDecompositionAlt_CDRM();
        LinearSolverDense<CMatrixRMaj> solver = new LinearSolverLu_CDRM(decomp);

        solver.setA(A);
        InvertUsingSolve_CDRM.invert(solver,A,A_inv);

        CMatrixRMaj I = RandomMatrices_CDRM.rectangle(3,3,rand);

        CommonOps_CDRM.mult(A, A_inv, I);

        assertTrue(MatrixFeatures_CDRM.isIdentity(I,tol));
    }
}
