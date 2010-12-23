/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.dense.linsol;

import org.ejml.data.Matrix64F;


/**
 * <p>
 * An implementation of LinearSolver solves a linear system or inverts a matrix.  It masks more complex
 * implementation details, while giving the programmer control over memory management and performance.
 * To quickly detect nearly singular matrices without computing the SVD the {@link #quality()}
 * function is provided.
 * </p>
 *
 * <p>
 * A linear system is defined as:
 * A*X = B.<br>
 * where A &isin; &real; <sup>m &times; n</sup>, X &isin; &real; <sup>n &times; p</sup>,
 * B &isin; &real; <sup>m &times; p</sup>.  Different implementations can solve different
 * types and shapes in input matrices and have different memory and runtime performance.
 *</p>

 * <p>
 * To solve a system:<br>
 * <ol>
 * <li> Call {@link #setA(org.ejml.data.Matrix64F)}
 * <li> Call {@link #solve(org.ejml.data.Matrix64F, org.ejml.data.Matrix64F)}.
 * </ol>
 * </p>
 *
 * <p>
 * To invert a matrix:<br>
 * <ol>
 * <li> Call {@link #setA(org.ejml.data.Matrix64F)}
 * <li> Call {@link #invert(org.ejml.data.Matrix64F)}.
 * </ol>
 * A matrix can also be inverted by passing in an identity matrix to solve, but this will be
 * slower and more memory intensive than the specialized invert() function.
 * </p>
 *
 * <p>
 * <b>IMPORTANT:</b> Depending upon the implementation, input matrices might be overwritten by
 * the solver.  This
 * reduces memory and computational requirements and give more control to the programmer.  If
 * the input matrices need to be not modified then {@link LinearSolverSafe} can be used.  The
 * functions {@link #modifiesA()} and {@link #modifiesB()} specify which input matrices are being
 * modified.
 * </p>
 *
 * @author Peter Abeles
 */
public interface LinearSolver< T extends Matrix64F> {

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
     * @param A The 'A' matrix in the linear equation. Might be modified or have the reference.
     * @return true if it can be processed.
     */
    public boolean setA( T A );

    /**
     * <p>
     * Returns a very quick to compute measure of how singular the system is.  This measure will
     * be invariant to the scale of the matrix and always be positive, with larger values
     * indicating it is less singular.  If not supported by the solver then the runtime
     * exception IllegalArgumentException is thrown.  This is NOT the matrix's condition.
     * <p>
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
    public double quality();

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
    public void solve( T B , T X );


    /**
     * Computes the inverse of of the 'A' matrix passed into {@link #setA(org.ejml.data.Matrix64F)}
     * and writes the results to the provided matrix.  If 'A_inv' needs to be different from 'A'
     * is implementation dependent.
     *
     * @param A_inv Where the inverted matrix saved. Modified.
     */
    public void invert( T A_inv );

    /**
     * Returns true if the passed in matrix to {@link #setA(org.ejml.data.Matrix64F)}
     * is modified.
     *
     * @return true if A is modified in setA().
     */
    public boolean modifiesA();

    /**
     * Returns true if the passed in 'B' matrix to {@link #solve(org.ejml.data.Matrix64F, org.ejml.data.Matrix64F)}
     * is modified.
     *
     * @return true if B is modified in solve(B,X).
     */
    public boolean modifiesB();
}
