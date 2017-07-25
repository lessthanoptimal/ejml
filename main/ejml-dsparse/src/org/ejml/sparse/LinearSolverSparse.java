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

package org.ejml.sparse;

import org.ejml.data.Matrix;
import org.ejml.interfaces.decomposition.DecompositionInterface;


/**

 * @author Peter Abeles
 */
// Ideas. Base linear solver will have two inputs
//    Matrix type specific solvers, e.g. LinearSolver_DDRM implements LinearSolver<DMatrixRMaj,DMatrixRMaj>
// have sparse solver extend LinearSolver but add functions to it
public interface LinearSolverSparse<S extends Matrix, D extends Matrix> {

    boolean setA(S A);

    double quality();

    void lockStructure();

    boolean isStructureLocked();

    /**
     * <p>
     * Solves for X in the linear system, A*X=B.
     * </p>
     * <p>
     * In some implementations 'B' and 'X' can be the same instance of a variable.  Call
     * {@link #modifiesB()} to determine if 'B' is modified.
     * </p>
     *
     * @param B A matrix &real; <sup>m &times; p</sup>. Might be modified.
     * @param X A matrix &real; <sup>n &times; p</sup>, where the solution is written to.  Modified.
     */
    void solve(D B, D X);

    // TODO add a solve for sparse matrices?

    /**
     * Returns true if the passed in matrix to {@link #setA(Matrix)}
     * is modified.
     *
     * @return true if A is modified in setA().
     */
    boolean modifiesA();

    /**
     * Returns true if the passed in 'B' matrix to {@link #solve(Matrix, Matrix)}
     * is modified.
     *
     * @return true if B is modified in solve(B,X).
     */
    boolean modifiesB();


    /**
     * If a decomposition class was used internally then this will return that class.
     * Most linear solvers decompose the input matrix into a more simplistic form.
     * However some solutions do not require decomposition, e.g. inverse by minor.
     * @param <D> Decomposition type
     * @return Internal decomposition class.  If there is none then null.
     */
    <D extends DecompositionInterface>D getDecomposition();
}
