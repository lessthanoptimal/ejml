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

package org.ejml.alg.dense.linsol.qr;

import org.ejml.alg.block.linsol.qr.QrHouseHolderSolver_B64;
import org.ejml.alg.dense.linsol.LinearSolver_B64_to_D64;
import org.ejml.data.RowMatrix_F64;


/**
 * Wrapper around {@link QrHouseHolderSolver_B64} that allows it to process
 * {@link RowMatrix_F64}.
 *
 * @author Peter Abeles
 */
public class LinearSolverQrBlock64_D64 extends LinearSolver_B64_to_D64 {

    public LinearSolverQrBlock64_D64() {
        super(new QrHouseHolderSolver_B64());
    }
}
