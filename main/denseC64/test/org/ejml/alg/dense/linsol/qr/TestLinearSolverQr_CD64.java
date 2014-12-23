/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.linsol.qr;

import org.ejml.alg.dense.decompose.qr.QRDecompositionHouseholderColumn_CD64;
import org.ejml.alg.dense.linsol.GenericCLinearSolverChecks;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.interfaces.linsol.LinearSolver;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverQr_CD64 extends GenericCLinearSolverChecks {

    public TestLinearSolverQr_CD64() {
//         shouldFailSingular = false;
    }

    @Override
    protected LinearSolver<CDenseMatrix64F> createSolver( CDenseMatrix64F A ) {
        return new LinearSolverQr_CD64(new QRDecompositionHouseholderColumn_CD64());
    }
}