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

package org.ejml.alg.dense.decomposition.bidiagonal;

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.SimpleMatrix;
import org.ejml.ops.SpecializedOps;


/**
 * A slower but much simpler version of {@link BidiagonalDecompositionRow} that internally uses
 * SimpleMatrix and explicitly computes the householder matrices.  This was easier to code up and is
 * used to validate other implementations.
 *
 * @author Peter Abeles
 */
public class BidiagonalDecompositionNaive {
    private SimpleMatrix U;
    private SimpleMatrix B;
    private SimpleMatrix V;

    // number of rows
    private int m;
    // number of columns
    private int n;
    // smallest of m and n
    private int min;


    DenseMatrix64F u;

    public SimpleMatrix getU() {
        return U;
    }

    public SimpleMatrix getB() {
        return B;
    }

    public SimpleMatrix getV() {
        return V;
    }

    /**
     * Computes the decomposition of the provided matrix.  If no errors are detected then true is returned,
     * false otherwise.
     * @param A  The matrix that is being decomposed.  Not modified.
     * @return If it detects any errors or not.
     */
    public boolean decompose( DenseMatrix64F A )
    {
        init(A);
        return _decompose();
    }

    protected void init(DenseMatrix64F A) {
        m = A.numRows;
        n = A.numCols;

        min = Math.min(m,n);

        U = SimpleMatrix.identity(m);
        B = new SimpleMatrix(A);
        V = SimpleMatrix.identity(n);

        int max = Math.max(m,n);
        u = new DenseMatrix64F(max,1);
    }



    /**
     * Internal function for computing the decomposition.
     */
    private boolean _decompose() {
        for( int k = 0; k < min; k++ ) {
            computeU(k);
            computeV(k);
        }

        return true;
    }

    protected void computeU( int k) {
        u.reshape(m,1, false);
        double u[] = this.u.data;

        // find the largest value in this column
        // this is used to normalize the column and mitigate overflow/underflow
        double max = 0;

        for( int i = k; i < m; i++ ) {
            // copy the householder vector to vector outside of the matrix to reduce caching issues
            // big improvement on larger matrices and a relatively small performance hit on small matrices.
            double val = u[i] = B.get(i,k);
            val = Math.abs(val);
            if( val > max )
                max = val;
        }

        if( max > 0 ) {
            // -------- set up the reflector Q_k

            double tau = 0;
            // normalize to reduce overflow/underflow
            // and compute tau for the reflector
            for( int i = k; i < m; i++ ) {
                double val = u[i] /= max;
                tau += val*val;
            }

            tau = Math.sqrt(tau);

            if( u[k] < 0 )
                tau = -tau;

            // write the reflector into the lower left column of the matrix
            double nu = u[k] + tau;
            u[k] = 1.0;

            for( int i = k+1; i < m; i++ ) {
                u[i] /= nu;
            }

            SimpleMatrix Q_k = SimpleMatrix.wrap(SpecializedOps.createReflector(this.u,nu/tau));
            U = U.mult(Q_k);
            B = Q_k.mult(B);
        }
    }

    protected void computeV(int k) {
        u.reshape(n,1, false);
        u.zero();
        double u[] = this.u.data;


        // find the largest value in this column
        // this is used to normalize the column and mitigate overflow/underflow
        double max = 0;
        
        for( int i = k+1; i < n; i++ ) {
            // copy the householder vector to vector outside of the matrix to reduce caching issues
            // big improvement on larger matrices and a relatively small performance hit on small matrices.
            double val = u[i] = B.get(k,i);
            val = Math.abs(val);
            if( val > max )
                max = val;
        }

        if( max > 0 ) {
            // -------- set up the reflector Q_k

            double tau = 0;
            // normalize to reduce overflow/underflow
            // and compute tau for the reflector
            for( int i = k+1; i < n; i++ ) {
                double val = u[i] /= max;
                tau += val*val;
            }

            tau = Math.sqrt(tau);

            if( u[k+1] < 0 )
                tau = -tau;

            // write the reflector into the lower left column of the matrix
            double nu = u[k+1] + tau;
            u[k+1] = 1.0;

            for( int i = k+2; i < n; i++ ) {
                u[i] /= nu;
            }

            // ---------- multiply on the left by Q_k
            SimpleMatrix Q_k = SimpleMatrix.wrap(SpecializedOps.createReflector(this.u,nu/tau));

            V = V.mult(Q_k);
            B = B.mult(Q_k);
        }
    }

}