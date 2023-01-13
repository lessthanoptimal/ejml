/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.linsol.chol;

import org.ejml.EjmlStandardJUnit;
import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverCholLDL_DDRM extends EjmlStandardJUnit {
    @Test void testInverseAndSolve() {
        DMatrixRMaj A = new DMatrixRMaj(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);
        DMatrixRMaj b = new DMatrixRMaj(3,1, true, 17, 97, 320);
        DMatrixRMaj x = RandomMatrices_DDRM.rectangle(3,1,rand);

        LinearSolverCholLDL_DDRM solver = new LinearSolverCholLDL_DDRM();
        assertTrue(solver.setA(A));
        solver.invert(A);
        solver.solve(b,x);


        DMatrixRMaj A_inv = new DMatrixRMaj(3,3, true, 1.453515, -0.199546, -0.013605, -0.199546, 0.167800, -0.034014, -0.013605, -0.034014, 0.020408);
        DMatrixRMaj x_expected = new DMatrixRMaj(3,1, true, 1, 2, 3);

        EjmlUnitTests.assertEquals(A_inv,A, UtilEjml.TEST_F64_SQ);
        EjmlUnitTests.assertEquals(x_expected,x,UtilEjml.TEST_F64_SQ);
    }
}
