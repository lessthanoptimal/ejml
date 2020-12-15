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

package org.ejml.dense.row.linsol.lu;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.lu.LUDecompositionAlt_DDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverLuBase_DDRM {

    Random rand = new Random(0x334);

    /**
     * Make sure that improve solution doesn't make things worse.  This test does
     * not realy test to see if it makes things better.
     */
    @Test
    public void testImproveSol_noharm() {
        DMatrixRMaj A = new DMatrixRMaj(3,3, true, 0, 1, 2, -2, 4, 9, 0.5, 0, 5);
        DMatrixRMaj b = new DMatrixRMaj(3,1, true, 8, 33, 15.5);
        DMatrixRMaj x = RandomMatrices_DDRM.rectangle(3,1,rand);
        DMatrixRMaj x_improved = new DMatrixRMaj(3,1);

        LUDecompositionAlt_DDRM alg = new LUDecompositionAlt_DDRM();

        x_improved.setTo(x);

        LinearSolverLu_DDRM solver = new LinearSolverLu_DDRM(alg);
        assertTrue(solver.setA(A));
        solver.solve(x,b);
        solver.improveSol(x_improved,b);

//        DMatrixRMaj x_truth = new DMatrixRMaj(3,1,new double[]{1,2,3});

        EjmlUnitTests.assertEquals(x,x_improved, UtilEjml.TEST_F64);
    }
}
