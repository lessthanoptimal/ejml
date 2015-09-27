/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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
import org.ejml.equation.Equation;
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
public class QRExampleEquation {

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

        Equation eq = new Equation();

        this.QR = A.copy();

        int N = Math.min(A.numCols,A.numRows);

        gammas = new double[ A.numCols ];

        for( int i = 0; i < N; i++ ) {
            // update temporary variables
            eq.alias(QR.numRows-i,"Ni",QR,"QR",i,"i");

            // Place the column that should be zeroed into v
            eq.process("v=QR(i:,i)");
            // Note that v is lazily created above.  Need direct access to it, which is done below.
            DenseMatrix64F v = eq.lookupMatrix("v");

            double maxV = CommonOps.elementMaxAbs(v);
            eq.alias(maxV,"maxV");

            if( maxV > 0 && v.getNumElements() > 1 ) {
                // normalize to reduce overflow issues
                eq.process("v=v/maxV");

                // compute the magnitude of the vector
                double tau = NormOps.normF(v);

                if( v.get(0) < 0 )
                    tau *= -1.0;

                eq.alias(tau,"tau");
                eq.process("u_0 = v(0,0)+tau");
                eq.process("gamma = u_0/tau");
                eq.process("v=v/u_0");
                eq.process("v(0,0)=1");
                eq.process("QR(i:,i:) = (eye(Ni) - gamma*v*v')*QR(i:,i:)");
                eq.process("QR(i:,i) = v");
                eq.process("QR(i,i) = -1*tau*maxV");

                // save gamma for recomputing Q later on
                gammas[i] = eq.lookupDouble("gamma");
            }
        }
    }

    /**
     * Returns the Q matrix.
     */
    public DenseMatrix64F getQ() {
        Equation eq = new Equation();

        DenseMatrix64F Q = CommonOps.identity(QR.numRows);
        DenseMatrix64F u = new DenseMatrix64F(QR.numRows,1);

        int N = Math.min(QR.numCols,QR.numRows);

        eq.alias(u,"u",Q,"Q",QR,"QR",QR.numRows,"r");

        // compute Q by first extracting the householder vectors from the columns of QR and then applying it to Q
        for( int j = N-1; j>= 0; j-- ) {
            eq.alias(j,"j",gammas[j],"gamma");

            eq.process("u(j:,0) = [1 ; QR((j+1):,j)]");
            eq.process("Q=(eye(r)-gamma*u*u')*Q");
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