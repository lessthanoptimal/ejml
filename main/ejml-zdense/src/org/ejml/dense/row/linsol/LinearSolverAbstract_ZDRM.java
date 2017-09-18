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

package org.ejml.dense.row.linsol;

import org.ejml.data.ZMatrixRMaj;
import org.ejml.interfaces.linsol.LinearSolverDense;


/**
 * <p>
 * An abstract class that provides some common functionality and a default implementation
 * of invert that uses the solve function of the child class.
 * </p>
 *
 * <p>
 * The extending class must explicity call {@link #_setA(ZMatrixRMaj)}
 * inside of its {@link #setA} function.
 * </p>
 * 
 * @author Peter Abeles
 */
public abstract class LinearSolverAbstract_ZDRM implements LinearSolverDense<ZMatrixRMaj> {

    protected ZMatrixRMaj A;
    protected int numRows;
    protected int numCols;
    protected int stride;

    public ZMatrixRMaj getA() {
        return A;
    }

    protected void _setA(ZMatrixRMaj A) {
        this.A = A;
        this.numRows = A.numRows;
        this.numCols = A.numCols;
        this.stride = numCols*2;
    }

    @Override
    public void invert(ZMatrixRMaj A_inv) {
        InvertUsingSolve_ZDRM.invert(this,A,A_inv);
    }
}
