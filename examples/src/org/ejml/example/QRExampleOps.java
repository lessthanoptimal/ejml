/*
 * Copyright (c) 2009-2013, Peter Abeles. All Rights Reserved.
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

package org.ejml.example;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.NormOps;

/**
 * <p>
 * Example code for computing the QR decomposition of a matrix. It demonstrates how to
 * extract a submatrix and insert one matrix into another one using the operator interface.
 * </p>
 *
 * Note: This code is horribly inefficient and is for demonstration purposes only.
 *
 * @author Peter Abeles
 */
public class QRExampleOps {

    // where the QR decomposition is stored
    private DenseMatrix64F QR;

    // used for computing Q
    private double gammas[];

    /**
     * Computes the QR decomposition of the provided matrix.
     *
     * @param A Matrix which is to be decomposed.  Not modified.
     */
    public void decompose( DenseMatrix64F A ) {

        this.QR = A.copy();

        int N = Math.min(A.numCols,A.numRows);

        gammas = new double[ A.numCols ];

        DenseMatrix64F A_small = new DenseMatrix64F(A.numRows,A.numCols);
        DenseMatrix64F A_mod = new DenseMatrix64F(A.numRows,A.numCols);
        DenseMatrix64F v = new DenseMatrix64F(A.numRows,1);
        DenseMatrix64F Q_k = new DenseMatrix64F(A.numRows,A.numRows);

        for( int i = 0; i < N; i++ ) {
            // reshape temporary variables
            A_small.reshape(QR.numRows-i,QR.numCols-i,false);
            A_mod.reshape(A_small.numRows,A_small.numCols,false);
            v.reshape(A_small.numRows,1,false);
            Q_k.reshape(v.getNumElements(),v.getNumElements(),false);

            // use extract matrix to get the column that is to be zeroed
            CommonOps.extract(QR,i,QR.numRows,i,i+1,v,0,0);

            double max = CommonOps.elementMaxAbs(v);

            if( max > 0 && v.getNumElements() > 1 ) {
                // normalize to reduce overflow issues
                CommonOps.divide(max,v);

                // compute the magnitude of the vector
                double tau = NormOps.normF(v);

                if( v.get(0) < 0 )
                    tau *= -1.0;

                double u_0 = v.get(0) + tau;
                double gamma = u_0/tau;

                CommonOps.divide(u_0,v);
                v.set(0,1.0);

                // extract the submatrix of A which is being operated on
                CommonOps.extract(QR,i,QR.numRows,i,QR.numCols,A_small,0,0);

                // A = (I - &gamma;*u*u<sup>T</sup>)A
                CommonOps.setIdentity(Q_k);
                CommonOps.multAddTransB(-gamma,v,v,Q_k);
                CommonOps.mult(Q_k,A_small,A_mod);

                // save the results
                CommonOps.insert(A_mod, QR, i,i);
                CommonOps.insert(v, QR, i,i);
                QR.unsafe_set(i,i,-tau*max);

                // save gamma for recomputing Q later on
                gammas[i] = gamma;
            }
        }
    }

    /**
     * Returns the Q matrix.
     */
    public DenseMatrix64F getQ() {
        DenseMatrix64F Q = CommonOps.identity(QR.numRows);
        DenseMatrix64F Q_k = new DenseMatrix64F(QR.numRows,QR.numRows);
        DenseMatrix64F u = new DenseMatrix64F(QR.numRows,1);

        DenseMatrix64F temp = new DenseMatrix64F(QR.numRows,QR.numRows);

        int N = Math.min(QR.numCols,QR.numRows);

        // compute Q by first extracting the householder vectors from the
        // columns of QR and then applying it to Q
        for( int j = N-1; j>= 0; j-- ) {
            if( j + 1 < N )
                u.set(j+1,0);

            CommonOps.extract(QR,j, QR.numRows,j,j+1,u,j,0);
            u.set(j,1.0);

            // A = (I - &gamma;*u*u<sup>T</sup>)*A<br>
            CommonOps.setIdentity(Q_k);
            CommonOps.multAddTransB(-gammas[j],u,u,Q_k);
            CommonOps.mult(Q_k,Q,temp);
            Q.set(temp);
        }

        return Q;
    }

    /**
     * Returns the R matrix.
     */
    public DenseMatrix64F getR() {
        DenseMatrix64F R = new DenseMatrix64F(QR.numRows,QR.numCols);

        int N = Math.min(QR.numCols,QR.numRows);

        for( int i = 0; i < N; i++ ) {
            for( int j = i; j < QR.numCols; j++ ) {
                R.unsafe_set(i,j, QR.unsafe_get(i,j));
            }
        }

        return R;
    }
}