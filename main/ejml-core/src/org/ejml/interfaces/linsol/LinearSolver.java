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

package org.ejml.interfaces.linsol;

import org.ejml.data.Matrix;
import org.ejml.interfaces.decomposition.DecompositionInterface;


/**
 * <p>Base class for Linear Solvers.</p>
 *
 * @see LinearSolverDense
 *
 * @author Peter Abeles
 */
public interface LinearSolver<S extends Matrix, D extends Matrix> {

    /**
     * <p>
     * Specifies the A matrix in the linear equation.  A reference might be saved
     * and it might also be modified depending on the implementation.  If it is modified
     * then {@link #modifiesA()} will return true.
     * </p>
     *
     * <p>
     * If this value returns true that does not guarantee a valid solution was generated.  This
     * is because some decompositions don't detect singular matrices.
     * </p>
     *
     * @param A The 'A' matrix in the linear equation. Might be modified or save the reference.
     * @return true if it can be processed.
     */
    boolean setA(S A);

    /**
     * <p>
     * Returns a very quick to compute measure of how singular the system is.  This measure will
     * be invariant to the scale of the matrix and always be positive, with larger values
     * indicating it is less singular.  If not supported by the solver then the runtime
     * exception IllegalArgumentException is thrown.  This is NOT the matrix's condition.
     * </p>
     *
     * <p>
     * How this function is implemented is not specified.  One possible implementation is the following:
     * In many decompositions a triangular matrix
     * is extracted.  The determinant of a triangular matrix is easily computed and once normalized
     * to be scale invariant and its absolute value taken it will provide functionality described above.
     * </p>
     *
     * @return The quality of the linear system.
     */
    double quality();

    /**
     * <p>
     * Solves for X in the linear system, A*X=B.
     * </p>
     * <p>
     * In some implementations 'B' and 'X' can be the same instance of a variable.  Call
     * {@link #modifiesB()} to determine if 'B' is modified.
     * </p>
     *
     * @param B A matrix &real; <sup>m &times; p</sup>.  Might be modified.
     * @param X A matrix &real; <sup>n &times; p</sup>, where the solution is written to.  Modified.
     */
    void solve(D B, D X);

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
