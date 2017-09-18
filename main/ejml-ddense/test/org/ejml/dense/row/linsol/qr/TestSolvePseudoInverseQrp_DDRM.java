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

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.decomposition.qr.QRColPivDecompositionHouseholderColumn_DDRM;
import org.ejml.dense.row.linsol.GenericLinearSolverChecks_DDRM;
import org.ejml.dense.row.linsol.GenericSolvePseudoInverseChecks_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.Test;

/**
 * @author Peter Abeles
 */
public class TestSolvePseudoInverseQrp_DDRM extends GenericLinearSolverChecks_DDRM {
    public TestSolvePseudoInverseQrp_DDRM() {
         shouldFailSingular = false;
    }

    @Override
    protected LinearSolverDense<DMatrixRMaj> createSolver(DMatrixRMaj A ) {
        return new SolvePseudoInverseQrp_DDRM(new QRColPivDecompositionHouseholderColumn_DDRM(),true);
    }

    @Test
    public void checkSingularBasic() {
        LinearSolverDense<DMatrixRMaj> solver =
                new SolvePseudoInverseQrp_DDRM(new QRColPivDecompositionHouseholderColumn_DDRM(),true);
        GenericSolvePseudoInverseChecks_DDRM checks = new GenericSolvePseudoInverseChecks_DDRM(solver);

        checks.all();
    }

    @Test
    public void checkSingularFull() {
        LinearSolverDense<DMatrixRMaj> solver =
                new SolvePseudoInverseQrp_DDRM(new QRColPivDecompositionHouseholderColumn_DDRM(),false);
        GenericSolvePseudoInverseChecks_DDRM checks = new GenericSolvePseudoInverseChecks_DDRM(solver);

        checks.all();
    }
}
