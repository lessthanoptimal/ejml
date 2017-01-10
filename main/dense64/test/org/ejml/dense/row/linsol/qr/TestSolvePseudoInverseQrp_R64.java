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

package org.ejml.dense.row.linsol.qr;

import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.row.decomposition.qr.QRColPivDecompositionHouseholderColumn_R64;
import org.ejml.dense.row.linsol.GenericLinearSolverChecks_R64;
import org.ejml.dense.row.linsol.GenericSolvePseudoInverseChecks_R64;
import org.ejml.interfaces.linsol.LinearSolver;
import org.junit.Test;

/**
 * @author Peter Abeles
 */
public class TestSolvePseudoInverseQrp_R64 extends GenericLinearSolverChecks_R64 {
    public TestSolvePseudoInverseQrp_R64() {
         shouldFailSingular = false;
    }

    @Override
    protected LinearSolver<DMatrixRow_F64> createSolver(DMatrixRow_F64 A ) {
        return new SolvePseudoInverseQrp_R64(new QRColPivDecompositionHouseholderColumn_R64(),true);
    }

    @Test
    public void checkSingularBasic() {
        LinearSolver<DMatrixRow_F64> solver =
                new SolvePseudoInverseQrp_R64(new QRColPivDecompositionHouseholderColumn_R64(),true);
        GenericSolvePseudoInverseChecks_R64 checks = new GenericSolvePseudoInverseChecks_R64(solver);

        checks.all();
    }

    @Test
    public void checkSingularFull() {
        LinearSolver<DMatrixRow_F64> solver =
                new SolvePseudoInverseQrp_R64(new QRColPivDecompositionHouseholderColumn_R64(),false);
        GenericSolvePseudoInverseChecks_R64 checks = new GenericSolvePseudoInverseChecks_R64(solver);

        checks.all();
    }
}
