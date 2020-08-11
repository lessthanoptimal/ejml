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

package org.ejml.dense.row.decomposition.eig;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.NormOps_FDRM;
import org.ejml.dense.row.SpecializedOps_FDRM;
import org.ejml.dense.row.factory.LinearSolverFactory_FDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;


/**
 * <p>
 * The power method is an iterative method that can be used to find dominant eigen vector in
 * a matrix.  Computing <b>A<sup>n</sup>q</b> for larger and larger values of n, where q is a vector.  Eventually the
 * dominant (if there is any) eigen vector will "win".
 * <p>
 *
 * <p>
 * Shift implementations find the eigen value of the matrix B=A-pI instead.  This matrix has the
 * same eigen vectors, but can converge much faster if p is chosen wisely.
 * </p>
 *
 * <p>
 * See section 5.3f in "Fundamentals of Matrix Computations" Second Edition, David S. Watkins.
 * </p>
 *
 * <p>
 * WARNING:  These functions have well known conditions where they will not converge or converge
 * very slowly and are only used in special situations in practice.  I have also seen it converge
 * to none dominant eigen vectors.
 * </p>
 *
 *
 * @author Peter Abeles
 */
public class EigenPowerMethod_FDRM {

    // used to determine convergence
    private float tol = UtilEjml.TESTP_F32;


    private FMatrixRMaj q0,q1,q2;

    private int maxIterations = 20;

    private FMatrixRMaj B;
    private FMatrixRMaj seed;

    /**
     *
     * @param size The size of the matrix which can be processed.
     */
    public EigenPowerMethod_FDRM(int size ) {
        q0 = new FMatrixRMaj(size,1);
        q1 = new FMatrixRMaj(size,1);
        q2 = new FMatrixRMaj(size,1);

        B = new FMatrixRMaj(size,size);
    }

    /**
     * Sets the value of the vector to use in the start of the iterations.
     *
     * @param seed The initial seed vector in the iteration.
     */
    public void setSeed(FMatrixRMaj seed) {
        this.seed = seed;
    }

    /**
     *
     * @param maxIterations
     * @param tolerance
     */
    public void setOptions(int maxIterations, float tolerance) {
        this.maxIterations = maxIterations;
        this.tol = tolerance;
    }

    /**
     * This method computes the eigen vector with the largest eigen value by using the
     * direct power method. This technique is the easiest to implement, but the slowest to converge.
     * Works only if all the eigenvalues are real.
     * 
     * @param A The matrix. Not modified.
     * @return If it converged or not.
     */
    public boolean computeDirect( FMatrixRMaj A ) {

        initPower(A);

        boolean converged = false;

        for( int i = 0; i < maxIterations && !converged; i++ ) {
//            q0.print();
            
            CommonOps_FDRM.mult(A,q0,q1);
            float s = NormOps_FDRM.normPInf(q1);
            CommonOps_FDRM.divide(q1,s,q2);

            converged = checkConverged(A);
        }

        return converged;
    }


    private void initPower(FMatrixRMaj A ) {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("A must be a square matrix.");

        if( seed != null ) {
            q0.set(seed);
        } else {
            for( int i = 0; i < A.numRows; i++ ) {
                q0.data[i] = 1;
            }
        }
    }

    /**
     * Test for convergence by seeing if the element with the largest change
     * is smaller than the tolerance.  In some test cases it alternated between
     * the + and - values of the eigen vector.  When this happens it seems to have "converged"
     * to a non-dominant eigen vector.    At least in the case I looked at.  I haven't devoted
     * a lot of time into this issue...
     */
    private boolean checkConverged(FMatrixRMaj A) {
        float worst = 0;
        float worst2 = 0;
        for( int j = 0; j < A.numRows; j++ ) {
            float val = Math.abs(q2.data[j] - q0.data[j]);
            if( val > worst ) worst = val;
            val = Math.abs(q2.data[j] + q0.data[j]);
            if( val > worst2 ) worst2 = val;
        }

        // swap vectors
        FMatrixRMaj temp = q0;
        q0 = q2;
        q2 = temp;

        if( worst < tol )
            return true;
        else if( worst2 < tol )
            return true;
        else
            return false;
    }

    /**
     * Computes the most dominant eigen vector of A using a shifted matrix.
     * The shifted matrix is defined as <b>B = A - &alpha;I</b> and can converge faster
     * if &alpha; is chosen wisely.  In general it is easier to choose a value for &alpha;
     * that will converge faster with the shift-invert strategy than this one.
     *
     * @param A The matrix.
     * @param alpha Shifting factor.
     * @return If it converged or not.
     */
    public boolean computeShiftDirect(FMatrixRMaj A , float alpha) {
        SpecializedOps_FDRM.addIdentity(A,B,-alpha);

        return computeDirect(B);
    }

    /**
     * Computes the most dominant eigen vector of A using an inverted shifted matrix.
     * The inverted shifted matrix is defined as <b>B = (A - &alpha;I)<sup>-1</sup></b> and
     * can converge faster if &alpha; is chosen wisely.
     *
     * @param A An invertible square matrix matrix.
     * @param alpha Shifting factor.
     * @return If it converged or not.
     */
    public boolean computeShiftInvert(FMatrixRMaj A , float alpha ) {
        initPower(A);

        LinearSolverDense solver = LinearSolverFactory_FDRM.linear(A.numCols);

        SpecializedOps_FDRM.addIdentity(A,B,-alpha);
        solver.setA(B);

        boolean converged = false;

        for( int i = 0; i < maxIterations && !converged; i++ ) {
            solver.solve(q0,q1);
            float s = NormOps_FDRM.normPInf(q1);
            CommonOps_FDRM.divide(q1,s,q2);

            converged = checkConverged(A);
        }

         return converged;
    }

    public FMatrixRMaj getEigenVector() {
        return q0;
    }
}
