/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
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
import org.ejml.alg.dense.decomposition.QRPDecomposition;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionCommon;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner;
import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt;
import org.ejml.alg.dense.decomposition.qr.QRColPivDecompositionHouseholderColumn;
import org.ejml.alg.dense.linsol.chol.LinearSolverChol;
import org.ejml.alg.dense.linsol.chol.LinearSolverCholBlock64;
import org.ejml.alg.dense.linsol.lu.LinearSolverLu;
import org.ejml.alg.dense.linsol.qr.AdjLinearSolverQr;
import org.ejml.alg.dense.linsol.qr.LinearSolverQrBlock64;
import org.ejml.alg.dense.linsol.qr.LinearSolverQrHouseCol;
import org.ejml.alg.dense.linsol.qr.LinearSolverQrp;
import org.ejml.alg.dense.linsol.svd.SolvePseudoInverse;
import org.ejml.data.DenseMatrix64F;


/**
 * A factory for generating solvers for systems of the form A*x=b, where A and B are known and x is unknown. 
 *
 * @author Peter Abeles
 */
public class LinearSolverFactory {

    /**
     * Creates a general purpose solver.  Use this if you are not sure what you need.
     *
     * @param numRows The number of rows that the decomposition is optimized for.
     * @param numCols The number of columns that the decomposition is optimized for.
     */
    public static LinearSolver<DenseMatrix64F> general( int numRows , int numCols ) {
        if( numRows == numCols )
            return linear(numRows);
        else
            return leastSquares(numRows,numCols);
    }

    /**
     * Creates a solver for linear systems.  The A matrix will have dimensions (m,m).
     *
     * @return A new linear solver.
     */
    public static LinearSolver<DenseMatrix64F> linear( int matrixSize ) {
        return new LinearSolverLu(new LUDecompositionAlt());
    }

    /**
     * Creates a good general purpose solver for over determined systems and returns the optimal least-squares
     * solution.  The A matrix will have dimensions (m,n) where m &ge; n.
     *
     * @param numRows The number of rows that the decomposition is optimized for.
     * @param numCols The number of columns that the decomposition is optimized for.
     * @return A new least-squares solver for over determined systems.
     */
    public static LinearSolver<DenseMatrix64F> leastSquares( int numRows , int numCols ) {
        if(numCols < EjmlParameters.SWITCH_BLOCK64_QR )  {
            return new LinearSolverQrHouseCol();
        } else {
            if( EjmlParameters.MEMORY == EjmlParameters.MemoryUsage.FASTER )
                return new LinearSolverQrBlock64();
            else
                return new LinearSolverQrHouseCol();
        }
    }

    /**
     * Creates a solver for symmetric positive definite matrices.
     *
     * @return A new solver for symmetric positive definite matrices.
     */
    public static LinearSolver<DenseMatrix64F> symmPosDef( int matrixWidth ) {
        if(matrixWidth < EjmlParameters.SWITCH_BLOCK64_CHOLESKY )  {
            CholeskyDecompositionCommon decomp = new CholeskyDecompositionInner(true);
            return new LinearSolverChol(decomp);
        } else {
            if( EjmlParameters.MEMORY == EjmlParameters.MemoryUsage.FASTER )
                return new LinearSolverCholBlock64();
            else {
                CholeskyDecompositionCommon decomp = new CholeskyDecompositionInner(true);
                return new LinearSolverChol(decomp);
            }
        }
    }

    /**
     * Creates a solver which can come up with a partial solution for a singular matrix.
     * The matrix is decomposed using QR with column pivots.  Variables which can't
     * be solved for because its singular are set to the input.
     *
     * @return A new solver which can handle
     */
    public static LinearSolver<DenseMatrix64F> leastSquaresQrPivot(int matrixWidth) {
        QRPDecomposition<DenseMatrix64F> decomposition =
                new QRColPivDecompositionHouseholderColumn();

        return new LinearSolverQrp(decomposition);
    }

    /**
     * <p>
     * Returns a solver which uses the pseudo inverse.  Useful when a matrix
     * needs to be inverted which is singular.  Two variants of pseudo inverse are provided.  SVD
     * will tend to be the most robust but the slowest and QR decomposition with column pivots will
     * be faster, but less robust.
     * </p>
     *
     * @param useSVD If true SVD will be used, otherwise QR with column pivot will be used.
     * @return Solver for singular matrices.
     */
    public static LinearSolver<DenseMatrix64F> pseudoInverse( boolean useSVD ) {
        if( useSVD )
            return new SolvePseudoInverse();
        else
            return new LinearSolverQrp(new QRColPivDecompositionHouseholderColumn());
    }

    /**
     * Create a solver which can efficiently add and remove elements instead of recomputing
     * everything from scratch.
     */
    public static AdjustableLinearSolver adjustable() {
        return new AdjLinearSolverQr();
    }
}
