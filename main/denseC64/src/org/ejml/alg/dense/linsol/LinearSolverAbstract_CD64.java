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

package org.ejml.alg.dense.linsol;

import org.ejml.data.CDenseMatrix64F;
import org.ejml.interfaces.linsol.LinearSolver;


/**
 * <p>
 * An abstract class that provides some common functionality and a default implementation
 * of invert that uses the solve function of the child class.
 * </p>
 *
 * <p>
 * The extending class must explicity call {@link #_setA(org.ejml.data.CDenseMatrix64F)}
 * inside of its {@link #setA} function.
 * </p>
 * 
 * @author Peter Abeles
 */
public abstract class LinearSolverAbstract_CD64 implements LinearSolver<CDenseMatrix64F> {

    protected CDenseMatrix64F A;
    protected int numRows;
    protected int numCols;
    protected int stride;

    public CDenseMatrix64F getA() {
        return A;
    }

    protected void _setA(CDenseMatrix64F A) {
        this.A = A;
        this.numRows = A.numRows;
        this.numCols = A.numCols;
        this.stride = numCols*2;
    }

    @Override
    public void invert(CDenseMatrix64F A_inv) {
        CInvertUsingSolve.invert(this,A,A_inv);
    }
}
