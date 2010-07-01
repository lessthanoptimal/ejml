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

import org.ejml.EjmlParameters;
import org.ejml.alg.dense.decomposition.DecompositionFactory;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionBlock;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionCommon;
import org.ejml.alg.dense.linsol.chol.LinearSolverChol;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;


/**
 * <p>
 * The pseudo-inverse is used to solve an over determined system:<br>
 * x=inv(A<sup>T</sup>A)A<sup>T</sup>b<br>
 * where A &isin; &real; <sup>m &times; n</sup> and m &ge; n.
 * </p>
 * <p>
 * Thus allowing a linear solver to be used that can only invert square matrices.  The downside of
 * this approach is that the final solution is less precise in some situations.  {@link org.ejml.alg.dense.decomposition.QRDecomposition QRDecomposition}
 * will produce a more accurate solution in these situations.
 * </p>
 *
 * <p>
 * This pseudo-inverse is also known as the Moore-Penrose pseudo-inverse and can be easily derived
 * through matrix algebra.
 * </p>
 *
 * @author Peter Abeles
 */
public class SolvePseudoInverse implements LinearSolver {

    // linear solver that is used to invert the matrix
    private LinearSolver inverter;

    // reference to the original matrix
    private DenseMatrix64F A;
    private DenseMatrix64F ATA;
    // the results of the pseudo-inverse
    private DenseMatrix64F pinv;

    // it can solve a system that has up to this size
    private int maxRows=-1;
    private int maxCols=-1;

    /**
     * Creates a new solver from an arbitrary linear solver.
     *
     * @param inverter Used to compute an inverse of a matrix.
     */
    public SolvePseudoInverse( LinearSolver inverter ) {
        this.inverter = inverter;
    }

    /**
     * Creates a new solver using a cholesky decomposition as its default solver.
     *
     * @param maxCols An estimate of how large of a matrix it might be inverting.
     * Better to overestimate than underestimate.
     */
    public SolvePseudoInverse( int maxCols ) {
        this(new LinearSolverChol((CholeskyDecompositionCommon) DecompositionFactory.chol(maxCols,false,true)));
    }

    /**
     * Creates a new solver using a cholesky decomposition as its default solver.
     */
    public SolvePseudoInverse() {
        this(new LinearSolverChol(new CholeskyDecompositionBlock(false, EjmlParameters.BLOCK_WIDTH)));
    }

    public void setMaxSize( int maxRows , int maxCols ) {
        this.maxRows = maxRows;
        this.maxCols = maxCols;

        ATA = new DenseMatrix64F(maxCols,maxCols);
        pinv = new DenseMatrix64F(maxCols,maxRows);
    }

    @Override
    public boolean setA(DenseMatrix64F A) {
        if( A.numRows > maxRows || A.numCols > maxCols ) {
            setMaxSize(A.numRows,A.numCols);
        }

        this.A = A;
        ATA.reshape(A.numCols,A.numCols, false);

        // compute the pseudo inverse
        CommonOps.multTransA(A,A,ATA);

        if( !inverter.setA(ATA) ) {
            return false;
        }
        inverter.invert(ATA);
        CommonOps.multTransB(ATA,A, pinv);

        return true;
    }

    @Override
    public void solve( DenseMatrix64F b, DenseMatrix64F x) {
        CommonOps.mult(pinv,b,x);
    }

    @Override
    public DenseMatrix64F getA() {
        return A;
    }

    @Override
    public void invert(DenseMatrix64F A_inv) {
        A_inv.set(pinv);
    }
}
