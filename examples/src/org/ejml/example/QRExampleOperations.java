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

package org.ejml.example;

import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.NormOps_R64;

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
public class QRExampleOperations {

    // where the QR decomposition is stored
    private RowMatrix_F64 QR;

    // used for computing Q
    private double gammas[];

    /**
     * Computes the QR decomposition of the provided matrix.
     *
     * @param A Matrix which is to be decomposed.  Not modified.
     */
    public void decompose( RowMatrix_F64 A ) {

        this.QR = A.copy();

        int N = Math.min(A.numCols,A.numRows);

        gammas = new double[ A.numCols ];

        RowMatrix_F64 A_small = new RowMatrix_F64(A.numRows,A.numCols);
        RowMatrix_F64 A_mod = new RowMatrix_F64(A.numRows,A.numCols);
        RowMatrix_F64 v = new RowMatrix_F64(A.numRows,1);
        RowMatrix_F64 Q_k = new RowMatrix_F64(A.numRows,A.numRows);

        for( int i = 0; i < N; i++ ) {
            // reshape temporary variables
            A_small.reshape(QR.numRows-i,QR.numCols-i,false);
            A_mod.reshape(A_small.numRows,A_small.numCols,false);
            v.reshape(A_small.numRows,1,false);
            Q_k.reshape(v.getNumElements(),v.getNumElements(),false);

            // use extract matrix to get the column that is to be zeroed
            CommonOps_R64.extract(QR,i,QR.numRows,i,i+1,v,0,0);

            double max = CommonOps_R64.elementMaxAbs(v);

            if( max > 0 && v.getNumElements() > 1 ) {
                // normalize to reduce overflow issues
                CommonOps_R64.divide(v,max);

                // compute the magnitude of the vector
                double tau = NormOps_R64.normF(v);

                if( v.get(0) < 0 )
                    tau *= -1.0;

                double u_0 = v.get(0) + tau;
                double gamma = u_0/tau;

                CommonOps_R64.divide(v,u_0);
                v.set(0,1.0);

                // extract the submatrix of A which is being operated on
                CommonOps_R64.extract(QR,i,QR.numRows,i,QR.numCols,A_small,0,0);

                // A = (I - &gamma;*u*u<sup>T</sup>)A
                CommonOps_R64.setIdentity(Q_k);
                CommonOps_R64.multAddTransB(-gamma,v,v,Q_k);
                CommonOps_R64.mult(Q_k,A_small,A_mod);

                // save the results
                CommonOps_R64.insert(A_mod, QR, i,i);
                CommonOps_R64.insert(v, QR, i,i);
                QR.unsafe_set(i,i,-tau*max);

                // save gamma for recomputing Q later on
                gammas[i] = gamma;
            }
        }
    }

    /**
     * Returns the Q matrix.
     */
    public RowMatrix_F64 getQ() {
        RowMatrix_F64 Q = CommonOps_R64.identity(QR.numRows);
        RowMatrix_F64 Q_k = new RowMatrix_F64(QR.numRows,QR.numRows);
        RowMatrix_F64 u = new RowMatrix_F64(QR.numRows,1);

        RowMatrix_F64 temp = new RowMatrix_F64(QR.numRows,QR.numRows);

        int N = Math.min(QR.numCols,QR.numRows);

        // compute Q by first extracting the householder vectors from the columns of QR and then applying it to Q
        for( int j = N-1; j>= 0; j-- ) {
            CommonOps_R64.extract(QR,j, QR.numRows,j,j+1,u,j,0);
            u.set(j,1.0);

            // A = (I - &gamma;*u*u<sup>T</sup>)*A<br>
            CommonOps_R64.setIdentity(Q_k);
            CommonOps_R64.multAddTransB(-gammas[j],u,u,Q_k);
            CommonOps_R64.mult(Q_k,Q,temp);
            Q.set(temp);
        }

        return Q;
    }

    /**
     * Returns the R matrix.
     */
    public RowMatrix_F64 getR() {
        RowMatrix_F64 R = new RowMatrix_F64(QR.numRows,QR.numCols);

        int N = Math.min(QR.numCols,QR.numRows);

        for( int i = 0; i < N; i++ ) {
            for( int j = i; j < QR.numCols; j++ ) {
                R.unsafe_set(i,j, QR.unsafe_get(i,j));
            }
        }

        return R;
    }
}