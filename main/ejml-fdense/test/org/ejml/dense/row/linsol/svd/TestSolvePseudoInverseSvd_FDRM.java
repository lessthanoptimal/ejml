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

package org.ejml.dense.row.linsol.svd;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.linsol.GenericLinearSolverChecks_FDRM;
import org.ejml.dense.row.linsol.GenericSolvePseudoInverseChecks_FDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.jupiter.api.Test;

/**
 * @author Peter Abeles
 */
public class TestSolvePseudoInverseSvd_FDRM extends GenericLinearSolverChecks_FDRM {

    public TestSolvePseudoInverseSvd_FDRM() {
        this.shouldFailSingular = false;
    }

    @Override
    protected LinearSolverDense<FMatrixRMaj> createSolver(FMatrixRMaj A ) {
        return new SolvePseudoInverseSvd_FDRM(A.numRows,A.numCols);
    }

    @Test
    public void checkSingularBasic() {
        LinearSolverDense<FMatrixRMaj> solver = new SolvePseudoInverseSvd_FDRM(10,10);
        GenericSolvePseudoInverseChecks_FDRM checks = new GenericSolvePseudoInverseChecks_FDRM(solver);

        checks.all();
    }
}
