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

package org.ejml.dense.row.decompose.qr;

import org.ejml.data.Complex_F32;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.decompose.UtilDecompositons_CDRM;
import org.ejml.interfaces.decomposition.QRDecomposition;


/**
 * <p>
 * Householder QR decomposition is rich in operations along the columns of the matrix.  This can be
 * taken advantage of by solving for the Q matrix in a column major format to reduce the number
 * of CPU cache misses and the number of copies that are performed.
 * </p>
 *
 * @see QRDecompositionHouseholder_CDRM
 *
 * @author Peter Abeles
 */
public class QRDecompositionHouseholderColumn_CDRM implements QRDecomposition<CMatrixRMaj> {

    /**
     * Where the Q and R matrices are stored.  R is stored in the
     * upper triangular portion and Q on the lower bit.  Lower columns
     * are where u is stored.  Q_k = (I - gamma_k*u_k*u_k^T).
     */
    protected float dataQR[][]; // [ column][ row ]

    // used internally to store temporary data
    protected float v[];

    // dimension of the decomposed matrices
    protected int numCols; // this is 'n'
    protected int numRows; // this is 'm'
    protected int minLength;

    // the computed gamma for Q_k matrix
    protected float gammas[];
    // local variables
    protected float gamma;
    protected Complex_F32 tau = new Complex_F32();

    // did it encounter an error?
    protected boolean error;

    public void setExpectedMaxSize( int numRows , int numCols ) {
        this.numCols = numCols;
        this.numRows = numRows;
        minLength = Math.min(numCols,numRows);
        int maxLength = Math.max(numCols,numRows);

        if( dataQR == null || dataQR.length < numCols || dataQR[0].length < numRows*2 ) {
            dataQR = new float[ numCols ][  numRows*2 ];
            v = new float[ maxLength*2 ];
            gammas = new float[ minLength ];
        }

        if( v.length < maxLength*2 ) {
            v = new float[ maxLength*2 ];
        }
        if( gammas.length < minLength ) {
            gammas = new float[ minLength ];
        }
    }

    /**
     * Returns the combined QR matrix in a 2D array format that is column major.
     *
     * @return The QR matrix in a 2D matrix column major format. [ column ][ row ]
     */
    public float[][] getQR() {
        return dataQR;
    }

    /**
     * Computes the Q matrix from the imformation stored in the QR matrix.  This
     * operation requires about 4(m<sup>2</sup>n-mn<sup>2</sup>+n<sup>3</sup>/3) flops.
     *
     * @param Q The orthogonal Q matrix.
     */
    @Override
    public CMatrixRMaj getQ(CMatrixRMaj Q , boolean compact ) {
        if( compact )
            Q = UtilDecompositons_CDRM.checkIdentity(Q,numRows,minLength);
        else
            Q = UtilDecompositons_CDRM.checkIdentity(Q,numRows,numRows);

        for( int j = minLength-1; j >= 0; j-- ) {
            float u[] = dataQR[j];

            float vvReal = u[j*2];
            float vvImag = u[j*2+1];

            u[j*2]   = 1;
            u[j*2+1] = 0;
            float gammaReal = gammas[j];

            QrHelperFunctions_CDRM.rank1UpdateMultR(Q, u,0, gammaReal,j, j, numRows, v);

            u[j*2]   = vvReal;
            u[j*2+1] = vvImag;
        }

        return Q;
    }

    /**
     * Returns an upper triangular matrix which is the R in the QR decomposition.  If compact then the input
     * expected to be size = [min(rows,cols) , numCols] otherwise size = [numRows,numCols].
     *
     * @param R Storage for upper triangular matrix.
     * @param compact If true then a compact matrix is expected.
     */
    @Override
    public CMatrixRMaj getR(CMatrixRMaj R, boolean compact) {
        if( compact )
            R = UtilDecompositons_CDRM.checkZerosLT(R,minLength,numCols);
        else
            R = UtilDecompositons_CDRM.checkZerosLT(R,numRows,numCols);

        for( int j = 0; j < numCols; j++ ) {
            float colR[] = dataQR[j];
            int l = Math.min(j,numRows-1);
            for( int i = 0; i <= l; i++ ) {
                R.set(i,j,colR[i*2],colR[i*2+1]);
            }
        }

        return R;
    }

    /**
     * <p>
     * To decompose the matrix 'A' it must have full rank.  'A' is a 'm' by 'n' matrix.
     * It requires about 2n*m<sup>2</sup>-2m<sup>2</sup>/3 flops.
     * </p>
     *
     * <p>
     * The matrix provided here can be of different
     * dimension than the one specified in the constructor.  It just has to be smaller than or equal
     * to it.
     * </p>
     */
    @Override
    public boolean decompose( CMatrixRMaj A ) {
        setExpectedMaxSize(A.numRows, A.numCols);

        convertToColumnMajor(A);

        error = false;

        for( int j = 0; j < minLength; j++ ) {
            householder(j);
            updateA(j);
        }

        return !error;
    }

    @Override
    public boolean inputModified() {
        return false;
    }

    /**
     * Converts the standard row-major matrix into a column-major vector
     * that is advantageous for this problem.
     *
     * @param A original matrix that is to be decomposed.
     */
    protected void convertToColumnMajor(CMatrixRMaj A) {
        for( int x = 0; x < numCols; x++ ) {
            float colQ[] = dataQR[x];
            int indexCol = 0;
            for( int y = 0; y < numRows; y++ ) {
                int index = (y*numCols+x)*2;
                colQ[indexCol++] = A.data[index];
                colQ[indexCol++] = A.data[index+1];
            }
        }
    }

    /**
     * <p>
     * Computes the householder vector "u" for the first column of submatrix j.  Note this is
     * a specialized householder for this problem.  There is some protection against
     * overfloaw and underflow.
     * </p>
     * <p>
     * Q = I - &gamma;uu<sup>T</sup>
     * </p>
     * <p>
     * This function finds the values of 'u' and '&gamma;'.
     * </p>
     *
     * @param j Which submatrix to work off of.
     */
    protected void householder( int j )
    {
        final float u[] = dataQR[j];

        // find the largest value in this column
        // this is used to normalize the column and mitigate overflow/underflow
        final float max = QrHelperFunctions_CDRM.findMax(u, j, numRows - j);

        if( max == 0.0f ) {
            gamma = 0;
            error = true;
        } else {
            // computes tau and gamma, and normalizes u by max
            gamma = QrHelperFunctions_CDRM.computeTauGammaAndDivide(j, numRows, u, max, tau);

            // divide u by u_0
//            float u_0 = u[j] + tau;
            float real_u_0 = u[j*2] + tau.real;
            float imag_u_0 = u[j*2+1] + tau.imaginary;
            QrHelperFunctions_CDRM.divideElements(j + 1, numRows, u, 0, real_u_0,imag_u_0 );

            tau.real *= max;
            tau.imaginary *= max;

            u[j*2]   = -tau.real;
            u[j*2+1] = -tau.imaginary;
        }

        gammas[j] = gamma;
    }

    /**
     * <p>
     * Takes the results from the householder computation and updates the 'A' matrix.<br>
     * <br>
     * A = (I - &gamma;*u*u<sup>H</sup>)A
     * </p>
     *
     * @param w The submatrix.
     */
    protected void updateA( int w )
    {
        final float u[] = dataQR[w];

        for( int j = w+1; j < numCols; j++ ) {

            final float colQ[] = dataQR[j];
            // first element in u is assumed to be 1.0f + 0*i
            float realSum = colQ[w*2];
            float imagSum = colQ[w*2+1];

            for( int k = w+1; k < numRows; k++ ) {
                float realU = u[k*2];
                float imagU = -u[k*2+1];

                float realQ = colQ[k*2];
                float imagQ = colQ[k*2+1];

                realSum += realU*realQ - imagU*imagQ;
                imagSum += imagU*realQ + realU*imagQ;
            }
            realSum *= gamma;
            imagSum *= gamma;

            colQ[w*2  ] -= realSum;
            colQ[w*2+1] -= imagSum;

            for( int i = w+1; i < numRows; i++ ) {
                float realU = u[i*2];
                float imagU = u[i*2+1];

                colQ[i*2]  -= realU*realSum - imagU*imagSum;
                colQ[i*2+1]-= imagU*realSum + realU*imagSum;
            }
        }
    }

    public float[] getGammas() {
        return gammas;
    }
}