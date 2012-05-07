/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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

package org.ejml.example;

import org.ejml.simple.SimpleMatrix;

import static org.ejml.simple.SimpleMatrix.END;

/**
 * <p>
 * Example code for computing the QR decomposition of a matrix. It demonstrates how to
 * extract a submatrix and insert one matrix into another one using SimpleMatrix.
 * </p>
 *
 * Note: This code is horribly inefficient and is for demonstration purposes only.
 *
 * @author Peter Abeles
 */
public class QRExampleSimple {

    // where the QR decomposition is stored
    private SimpleMatrix QR;

    // used for computing Q
    private double gammas[];

    /**
     * Computes the QR decomposition of the provided matrix.
     *
     * @param A Matrix which is to be decomposed.  Not modified.
     */
    public void decompose( SimpleMatrix A ) {

        this.QR = A.copy();

        int N = Math.min(A.numCols(),A.numRows());
        gammas = new double[ A.numCols() ];

        for( int i = 0; i < N; i++ ) {
            // use extract matrix to get the column that is to be zeroed
            SimpleMatrix v = QR.extractMatrix(i, END,i,i+1);
            double max = v.elementMaxAbs();

            if( max > 0 && v.getNumElements() > 1 ) {
                // normalize to reduce overflow issues
                v = v.divide(max);

                // compute the magnitude of the vector
                double tau = v.normF();

                if( v.get(0) < 0 )
                    tau *= -1.0;

                double u_0 = v.get(0) + tau;
                double gamma = u_0/tau;

                v = v.divide(u_0);
                v.set(0,1.0);

                // extract the submatrix of A which is being operated on
                SimpleMatrix A_small = QR.extractMatrix(i,END,i,END);

                // A = (I - &gamma;*u*u<sup>T</sup>)A
                A_small = A_small.plus(-gamma,v.mult(v.transpose()).mult(A_small));

                // save the results
                QR.insertIntoThis(i,i,A_small);
                QR.insertIntoThis(i+1,i,v.extractMatrix(1,END,0,1));

                // save gamma for recomputing Q later on
                gammas[i] = gamma;
            }
        }
    }

    /**
     * Returns the Q matrix.
     */
    public SimpleMatrix getQ() {
        SimpleMatrix Q = SimpleMatrix.identity(QR.numRows());

        int N = Math.min(QR.numCols(),QR.numRows());

        // compute Q by first extracting the householder vectors from the
        // columns of QR and then applying it to Q
        for( int j = N-1; j>= 0; j-- ) {
            SimpleMatrix u = new SimpleMatrix(QR.numRows(),1);
            u.insertIntoThis(j,0,QR.extractMatrix(j, END,j,j+1));
            u.set(j,1.0);

            // A = (I - &gamma;*u*u<sup>T</sup>)*A<br>
            Q = Q.plus(-gammas[j],u.mult(u.transpose()).mult(Q));
        }

        return Q;
    }

    /**
     * Returns the R matrix.
     */
    public SimpleMatrix getR() {
        SimpleMatrix R = new SimpleMatrix(QR.numRows(),QR.numCols());

        int N = Math.min(QR.numCols(),QR.numRows());

        for( int i = 0; i < N; i++ ) {
            for( int j = i; j < QR.numCols(); j++ ) {
                R.set(i,j, QR.get(i,j));
            }
        }

        return R;
    }
}