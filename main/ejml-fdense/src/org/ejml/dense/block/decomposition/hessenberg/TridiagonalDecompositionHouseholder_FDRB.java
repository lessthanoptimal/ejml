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

package org.ejml.dense.block.decomposition.hessenberg;

import org.ejml.data.FMatrixRBlock;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FSubmatrixD1;
import org.ejml.dense.block.MatrixMult_FDRB;
import org.ejml.dense.block.decomposition.qr.QRDecompositionHouseholder_FDRB;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F32;

import static org.ejml.dense.block.InnerMultiplication_FDRB.blockMultPlusTransA;


/**
 * <p>
 * Tridiagonal similar decomposition for block matrices.  Orthogonal matrices are computed using
 * householder vectors.
 * </p>
 *
 * <p>
 * Based off algorithm in section 2 of J. J. Dongarra, D. C. Sorensen, S. J. Hammarling,
 * "Block Reduction of Matrices to Condensed Forms for Eigenvalue Computations" Journal of
 * Computations and Applied Mathematics 27 (1989) 215-227<br>
 * <br>
 * Computations of Householder reflectors has been modified from what is presented in that paper to how 
 * it is performed in "Fundamentals of Matrix Computations" 2nd ed. by David S. Watkins.
 * </p>
 *
 * @author Peter Abeles
 */
public class TridiagonalDecompositionHouseholder_FDRB
        implements TridiagonalSimilarDecomposition_F32<FMatrixRBlock> {

    // matrix which is being decomposed
    // householder vectors are stored along the upper triangle rows
    protected FMatrixRBlock A;
    // temporary storage for block computations
    protected FMatrixRBlock V = new FMatrixRBlock(1,1);
    // stores intermediate results in matrix multiplication
    protected FMatrixRBlock tmp = new FMatrixRBlock(1,1);
    protected float gammas[] = new float[1];

    // temporary storage for zeros and ones in U
    protected FMatrixRMaj zerosM = new FMatrixRMaj(1,1);

    @Override
    public FMatrixRBlock getT(FMatrixRBlock T) {
        if( T == null ) {
            T = new FMatrixRBlock(A.numRows,A.numCols,A.blockLength);
        } else {
            if( T.numRows != A.numRows || T.numCols != A.numCols )
                throw new IllegalArgumentException("T must have the same dimensions as the input matrix");

            CommonOps_FDRM.fill(T, 0);
        }

        T.set(0,0,A.data[0]);
        for( int i = 1; i < A.numRows; i++ ) {
            float d = A.get(i-1,i);
            T.set(i,i,A.get(i,i));
            T.set(i-1,i,d);
            T.set(i,i-1,d);
        }

        return T;
    }

    @Override
    public FMatrixRBlock getQ(FMatrixRBlock Q, boolean transposed) {
        Q = QRDecompositionHouseholder_FDRB.initializeQ(Q, A.numRows, A.numCols, A.blockLength, false);

        int height = Math.min(A.blockLength,A.numRows);
        V.reshape(height,A.numCols,false);
        tmp.reshape(height,A.numCols,false);

        FSubmatrixD1 subQ = new FSubmatrixD1(Q);
        FSubmatrixD1 subU = new FSubmatrixD1(A);
        FSubmatrixD1 subW = new FSubmatrixD1(V);
        FSubmatrixD1 tmp = new FSubmatrixD1(this.tmp);


        int N = A.numRows;

        int start = N - N % A.blockLength;
        if( start == N )
            start -= A.blockLength;
        if( start < 0 )
            start = 0;

        // (Q1^T * (Q2^T * (Q3^t * A)))
        for( int i = start; i >= 0; i -= A.blockLength ) {
            int blockSize = Math.min(A.blockLength,N-i);

            subW.col0 = i;
            subW.row1 = blockSize;
            subW.original.reshape(subW.row1,subW.col1,false);

            if( transposed ) {
                tmp.row0 = i;
                tmp.row1 = A.numCols;
                tmp.col0 = 0;
                tmp.col1 = blockSize;
            } else {
                tmp.col0 = i;
                tmp.row1 = blockSize;
            }
            tmp.original.reshape(tmp.row1,tmp.col1,false);

            subU.col0 = i;
            subU.row0 = i;
            subU.row1 = subU.row0+blockSize;

            // zeros and ones are saved and overwritten in U so that standard matrix multiplication can be used
            copyZeros(subU);

            // compute W for Q(i) = ( I + W*Y^T)
            TridiagonalHelper_FDRB.computeW_row(A.blockLength, subU, subW, gammas, i);

            subQ.col0 = i;
            subQ.row0 = i;

            // Apply the Qi to Q
            // Qi = I + W*U^T

            // Note that U and V are really row vectors.  but standard notation assumed they are column vectors.
            // which is why the functions called don't match the math above

            // (I + W*U^T)*Q
            // F=U^T*Q(i)
            if( transposed )
                MatrixMult_FDRB.multTransB(A.blockLength,subQ,subU,tmp);
            else
                MatrixMult_FDRB.mult(A.blockLength,subU,subQ,tmp);
            // Q(i+1) = Q(i) + W*F
            if( transposed )
                MatrixMult_FDRB.multPlus(A.blockLength,tmp,subW,subQ);
            else
                MatrixMult_FDRB.multPlusTransA(A.blockLength,subW,tmp,subQ);

            replaceZeros(subU);
        }

        return Q;
    }

    private void copyZeros( FSubmatrixD1 subU ) {
        int N = Math.min(A.blockLength,subU.col1-subU.col0);
        for( int i = 0; i < N; i++ ) {
            // save the zeros
            for( int j = 0; j <= i; j++ ) {
                zerosM.unsafe_set(i,j,subU.get(i,j));
                subU.set(i,j,0);
            }
            // save the one
            if( subU.col0 + i + 1 < subU.original.numCols ) {
                zerosM.unsafe_set(i,i+1,subU.get(i,i+1));
                subU.set(i,i+1,1);
            }
        }
    }

    private void replaceZeros( FSubmatrixD1 subU ) {
        int N = Math.min(A.blockLength,subU.col1-subU.col0);
        for( int i = 0; i < N; i++ ) {
            // save the zeros
            for( int j = 0; j <= i; j++ ) {
                subU.set(i,j,zerosM.get(i,j));
            }
            // save the one
            if( subU.col0 + i + 1 < subU.original.numCols ) {
                subU.set(i,i+1,zerosM.get(i,i+1));
            }
        }
    }

    @Override
    public void getDiagonal(float[] diag, float[] off) {
        diag[0] = A.data[0];
        for( int i = 1; i < A.numRows; i++ ) {
            diag[i] = A.get(i,i);
            off[i-1] = A.get(i-1,i);
        }
    }

    @Override
    public boolean decompose(FMatrixRBlock orig) {
        if( orig.numCols != orig.numRows )
            throw new IllegalArgumentException("Input matrix must be square.");

        init(orig);

        FSubmatrixD1 subA = new FSubmatrixD1(A);
        FSubmatrixD1 subV = new FSubmatrixD1(V);
        FSubmatrixD1 subU = new FSubmatrixD1(A);

        int N = orig.numCols;

        for( int i = 0; i < N; i += A.blockLength ) {
//            System.out.println("-------- triag i "+i);
            int height = Math.min(A.blockLength,A.numRows-i);

            subA.col0 = subU.col0 = i;
            subA.row0 = subU.row0 = i;

            subU.row1 = subU.row0 + height;

            subV.col0 = i;
            subV.row1 = height;
            subV.original.reshape(subV.row1,subV.col1,false);

            // bidiagonalize the top row
            TridiagonalHelper_FDRB.tridiagUpperRow(A.blockLength, subA, gammas, subV);

            // apply Householder reflectors to the lower portion using block multiplication

            if( subU.row1 < orig.numCols) {
                // take in account the 1 in the last row.  The others are skipped over.
                float before = subU.get(A.blockLength-1,A.blockLength);
                subU.set(A.blockLength-1,A.blockLength,1);

                // A = A + U*V^T + V*U^T
                multPlusTransA(A.blockLength,subU,subV,subA);
                multPlusTransA(A.blockLength,subV,subU,subA);

                subU.set(A.blockLength-1,A.blockLength,before);
            }
        }

        return true;
    }

    /**
     * C = C + A^T*B
     *
     * @param blockLength
     * @param A row block vector
     * @param B row block vector
     * @param C
     */
    public static void multPlusTransA(int blockLength ,
                                      FSubmatrixD1 A , FSubmatrixD1 B ,
                                      FSubmatrixD1 C )
    {
        int heightA = Math.min( blockLength , A.row1 - A.row0 );

        for( int i = C.row0+blockLength; i < C.row1; i += blockLength ) {
            int heightC = Math.min( blockLength , C.row1 - i );

            int indexA = A.row0*A.original.numCols + (i-C.row0+A.col0)*heightA;

            for( int j = i; j < C.col1; j += blockLength ) {
                int widthC = Math.min( blockLength , C.col1 - j );

                int indexC = i*C.original.numCols + j*heightC;
                int indexB = B.row0*B.original.numCols + (j-C.col0+B.col0)*heightA;

                blockMultPlusTransA(A.original.data,B.original.data,C.original.data,
                            indexA,indexB,indexC,heightA,heightC,widthC);
            }
        }
    }

    private void init( FMatrixRBlock orig ) {
        this.A = orig;

        int height = Math.min(A.blockLength,A.numRows);
        V.reshape(height,A.numCols,A.blockLength,false);
        tmp.reshape(height,A.numCols,A.blockLength,false);

        if( gammas.length < A.numCols )
            gammas = new float[ A.numCols ];

        zerosM.reshape(A.blockLength,A.blockLength+1,false);
    }

    @Override
    public boolean inputModified() {
        return true;
    }
}
