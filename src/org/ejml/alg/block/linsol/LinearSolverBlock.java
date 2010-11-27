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

package org.ejml.alg.block.linsol;

import org.ejml.data.BlockMatrix64F;


/**
 * <p>
 * Computes solutions for systems that have the form:<br>
 * A*X = B.<br>
 * where A &isin; &real; <sup>m &times; n</sup>, X &isin; &real; <sup>n &times; p</sup>,
 * B &isin; &real; <sup>m &times; p</sup>, and m &ge; n.
 * </p>
 * <p>
 *  If m = n then there is an exact solution, if m > n then it is an over determined system and a best fit
 * in the least-squares sense solution is found instead.  If no solution can be found
 * for A then false is returned by {@link #setA}.
 * </p>
 *
 * <p>
 * A function is provided for computing the inverse of A.  This can be accomplished by using the solve function
 * also, but sometimes there are faster ways to compute the inverse.  Solvers are also designed to be
 * reused, which allows more efficient use of memory.
 * </p>
 *
 * @author Peter Abeles
 */
public interface LinearSolverBlock {


    /**
     * <p>
     * Specifies the A matrix in the linear equation.  A reference
     * is saved internally and the matrix might be modified modified.  To determine if
     * the input matrix is modified call {@link #modifiesA()}.  This
     * reference is discarded the next time this function is called.
     * </p>
     *
     * <p>
     * If this value returns true that does not guarantee a valid solution was generated.  This
     * is because some decompositions don't detect singular matrices.
     * </p>
     *
     * @param A System matrix. Might modified. Reference saved.
     * @return true True if the system can be solved for.
     */
    public boolean setA( BlockMatrix64F A );

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
     * Solves for X in the linear system, AX=B.
     * </p>
     * <p>
     * In some implementations B and X can be the same instance of a variable.
     * </p>
     *
     * @param B A matrix &real; <sup>m &times; p</sup>.  Not modified.
     * @param X A matrix &real; <sup>n &times; p</sup>, where the solution is written to.  Modified.
     */
    public void solve( BlockMatrix64F B , BlockMatrix64F X );


    /**
     * Computes the inverse of A matrix and writes the results to the
     * provided matrix.
     *
     * @param A_inv Where the inverted matrix saved. Modified.
     */
    public void invert( BlockMatrix64F A_inv );

    /**
     * Returns true if the input matrix is modified.
     *
     * @return If the input matrix is modified or not.
     */
    public boolean modifiesA();

    public boolean modifiesB();

}
