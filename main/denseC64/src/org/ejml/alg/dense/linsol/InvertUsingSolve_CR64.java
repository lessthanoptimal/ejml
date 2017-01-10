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

package org.ejml.alg.dense.linsol;

import org.ejml.data.DMatrixRow_C64;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps_CR64;


/**
 * A matrix can be easily inverted by solving a system with an identify matrix.  The only
 * disadvantage of this approach is that additional computations are required compared to
 * a specialized solution.
 *
 * @author Peter Abeles
 */
public class InvertUsingSolve_CR64 {

    public static void invert(LinearSolver<DMatrixRow_C64> solver , DMatrixRow_C64 A , DMatrixRow_C64 A_inv , DMatrixRow_C64 storage) {

        if( A.numRows != A_inv.numRows || A.numCols != A_inv.numCols) {
            throw new IllegalArgumentException("A and A_inv must have the same dimensions");
        }

        CommonOps_CR64.setIdentity(storage);

        solver.solve(storage,A_inv);
    }

    public static void invert(LinearSolver<DMatrixRow_C64> solver , DMatrixRow_C64 A , DMatrixRow_C64 A_inv ) {

        if( A.numRows != A_inv.numRows || A.numCols != A_inv.numCols) {
            throw new IllegalArgumentException("A and A_inv must have the same dimensions");
        }

        CommonOps_CR64.setIdentity(A_inv);

        solver.solve(A_inv,A_inv);
    }
}
