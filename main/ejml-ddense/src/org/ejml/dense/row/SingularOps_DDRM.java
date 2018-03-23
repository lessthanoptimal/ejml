/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.linsol.qr.SolveNullSpaceQRP_DDRM;
import org.ejml.dense.row.linsol.qr.SolveNullSpaceQR_DDRM;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;


/**
 * Operations related to singular value decomposition.
 *
 * @author Peter Abeles
 */
public class SingularOps_DDRM {

    /**
     * <p>
     * Adjusts the matrices so that the singular values are in descending order.
     * </p>
     *
     * <p>
     * In most implementations of SVD the singular values are automatically arranged in in descending
     * order.  In EJML this is not the case since it is often not needed and some computations can
     * be saved by not doing that.
     * </p>
     *
     * @param U Matrix. Modified.
     * @param tranU is U transposed or not.
     * @param W Diagonal matrix with singular values. Modified.
     * @param V Matrix. Modified.
     * @param tranV is V transposed or not.
     */
    // TODO the number of copies can probably be reduced here
    public static void descendingOrder(DMatrixRMaj U , boolean tranU ,
                                       DMatrixRMaj W ,
                                       DMatrixRMaj V , boolean tranV )
    {
        int numSingular = Math.min(W.numRows,W.numCols);

        checkSvdMatrixSize(U, tranU, W, V, tranV);

        for( int i = 0; i < numSingular; i++ ) {
            double bigValue=-1;
            int bigIndex=-1;

            // find the smallest singular value in the submatrix
            for( int j = i; j < numSingular; j++ ) {
                double v = W.get(j,j);

                if( v > bigValue ) {
                    bigValue = v;
                    bigIndex = j;
                }
            }

            // only swap if the current index is not the smallest
            if( bigIndex == i)
                continue;

            if( bigIndex == -1 ) {
                // there is at least one uncountable singular value.  just stop here
                break;
            }

            double tmp = W.get(i,i);
            W.set(i,i,bigValue);
            W.set(bigIndex,bigIndex,tmp);

            if( V != null ) {
                swapRowOrCol(V, tranV, i, bigIndex);
            }

            if( U != null ) {
                swapRowOrCol(U, tranU, i, bigIndex);
            }
        }
    }

    /**
     * <p>
     * Similar to {@link #descendingOrder(DMatrixRMaj, boolean, DMatrixRMaj, DMatrixRMaj, boolean)}
     * but takes in an array of singular values instead.
     * </p>
     *
     * @param U Matrix. Modified.
     * @param tranU is U transposed or not.
     * @param singularValues Array of singular values. Modified.
     * @param numSingularValues Number of elements in singularValues array
     * @param V Matrix. Modified.
     * @param tranV is V transposed or not.
     */
    public static void descendingOrder(DMatrixRMaj U , boolean tranU ,
                                       double singularValues[] ,
                                       int numSingularValues ,
                                       DMatrixRMaj V , boolean tranV )
    {
//        checkSvdMatrixSize(U, tranU, W, V, tranV);

        for( int i = 0; i < numSingularValues; i++ ) {
            double bigValue=-1;
            int bigIndex=-1;

            // find the smallest singular value in the submatrix
            for( int j = i; j < numSingularValues; j++ ) {
                double v = singularValues[j];

                if( v > bigValue ) {
                    bigValue = v;
                    bigIndex = j;
                }
            }

            // only swap if the current index is not the smallest
            if( bigIndex == i)
                continue;

            if( bigIndex == -1 ) {
                // there is at least one uncountable singular value.  just stop here
                break;
            }

            double tmp = singularValues[i];
            singularValues[i] = bigValue;
            singularValues[bigIndex] = tmp;

            if( V != null ) {
                swapRowOrCol(V, tranV, i, bigIndex);
            }

            if( U != null ) {
                swapRowOrCol(U, tranU, i, bigIndex);
            }
        }
    }

    /**
     * Checks to see if all the provided matrices are the expected size for an SVD.  If an error is encountered
     * then an exception is thrown.  This automatically handles compact and non-compact formats
     */
    public static void checkSvdMatrixSize(DMatrixRMaj U, boolean tranU, DMatrixRMaj W, DMatrixRMaj V, boolean tranV ) {
        int numSingular = Math.min(W.numRows,W.numCols);
        boolean compact = W.numRows == W.numCols;

        if( compact ) {
            if( U != null ) {
                if( tranU && U.numRows != numSingular )
                    throw new IllegalArgumentException("Unexpected size of matrix U");
                else if( !tranU && U.numCols != numSingular )
                    throw new IllegalArgumentException("Unexpected size of matrix U");
            }

            if( V != null ) {
            if( tranV && V.numRows != numSingular )
                throw new IllegalArgumentException("Unexpected size of matrix V");
            else if( !tranV && V.numCols != numSingular )
                throw new IllegalArgumentException("Unexpected size of matrix V");
            }
        } else {
            if( U != null && U.numRows != U.numCols )
                throw new IllegalArgumentException("Unexpected size of matrix U");
            if( V != null && V.numRows != V.numCols )
                throw new IllegalArgumentException("Unexpected size of matrix V");
            if( U != null && U.numRows != W.numRows )
                throw new IllegalArgumentException("Unexpected size of W");
            if( V != null && V.numRows != W.numCols )
                throw new IllegalArgumentException("Unexpected size of W");
        }
    }

    private static void swapRowOrCol(DMatrixRMaj M, boolean tran, int i, int bigIndex) {
        double tmp;
        if( tran ) {
            // swap the rows
            for( int col = 0; col < M.numCols; col++ ) {
                tmp = M.get(i,col);
                M.set(i,col,M.get(bigIndex,col));
                M.set(bigIndex,col,tmp);
            }
        } else {
            // swap the columns
            for( int row = 0; row < M.numRows; row++ ) {
                tmp = M.get(row,i);
                M.set(row,i,M.get(row,bigIndex));
                M.set(row,bigIndex,tmp);
            }
        }
    }

    /**
     * <p>
     * Returns the null-space from the singular value decomposition. The null space is a set of non-zero vectors that
     * when multiplied by the original matrix return zero.
     * </p>
     *
     * <p>
     * The null space is found by extracting the columns in V that are associated singular values less than
     * or equal to the threshold. In some situations a non-compact SVD is required.
     * </p>
     *
     * @param svd A precomputed decomposition.  Not modified.
     * @param nullSpace Storage for null space.  Will be reshaped as needed.  Modified.
     * @param tol Threshold for selecting singular values.  Try UtilEjml.EPS.
     * @return The null space.
     */
    public static DMatrixRMaj nullSpace(SingularValueDecomposition_F64<DMatrixRMaj> svd ,
                                          DMatrixRMaj nullSpace , double tol )
    {
        int N = svd.numberOfSingularValues();
        double s[] = svd.getSingularValues();

        DMatrixRMaj V = svd.getV(null,true);

        if( V.numRows != svd.numCols() ) {
            throw new IllegalArgumentException("Can't compute the null space using a compact SVD for a matrix of this size.");
        }

        // first determine the size of the null space
        int numVectors = svd.numCols()-N;

        for( int i = 0; i < N; i++ ) {
            if( s[i] <= tol ) {
                numVectors++;
            }
        }

        // declare output data
        if( nullSpace == null ) {
            nullSpace = new DMatrixRMaj(numVectors,svd.numCols());
        } else {
            nullSpace.reshape(numVectors,svd.numCols());
        }

        // now extract the vectors
        int count = 0;
        for( int i = 0; i < N; i++ ) {
            if( s[i] <= tol ) {
                CommonOps_DDRM.extract(V, i,i+1,0, V.numCols,nullSpace,count++,0);
            }
        }
        for( int i = N; i < svd.numCols(); i++ ) {
            CommonOps_DDRM.extract(V, i,i+1,0, V.numCols,nullSpace,count++,0);
        }

        CommonOps_DDRM.transpose(nullSpace);

        return nullSpace;
    }

    /**
     * Computes the null space using QR decomposition. This is much faster than using SVD
     * @param A (Input) Matrix
     * @param totalSingular Number of singular values
     * @return Null space
     */
    public static DMatrixRMaj nullspaceQR( DMatrixRMaj A , int totalSingular ) {
        SolveNullSpaceQR_DDRM solver = new SolveNullSpaceQR_DDRM();

        DMatrixRMaj nullspace = new DMatrixRMaj(1,1);

        if( !solver.process(A,totalSingular,nullspace))
            throw new RuntimeException("Solver failed. try SVD based method instead?");

        return nullspace;
    }

    /**
     * Computes the null space using QRP decomposition. This is faster than using SVD but slower than QR.
     * Much more stable than QR though.
     * @param A (Input) Matrix
     * @param totalSingular Number of singular values
     * @return Null space
     */
    public static DMatrixRMaj nullspaceQRP( DMatrixRMaj A , int totalSingular ) {
        SolveNullSpaceQRP_DDRM solver = new SolveNullSpaceQRP_DDRM();

        DMatrixRMaj nullspace = new DMatrixRMaj(1,1);

        if( !solver.process(A,totalSingular,nullspace))
            throw new RuntimeException("Solver failed. try SVD based method instead?");

        return nullspace;
    }

    /**
     * <p>
     * The vector associated will the smallest singular value is returned as the null space
     * of the decomposed system.  A right null space is returned if 'isRight' is set to true,
     * and a left null space if false.
     * </p>
     *
     * @param svd A precomputed decomposition.  Not modified.
     * @param isRight true for right null space and false for left null space.  Right is more commonly used.
     * @param nullVector Optional storage for a vector for the null space.  Modified.
     * @return Vector in V associated with smallest singular value..
     */
    public static DMatrixRMaj nullVector(SingularValueDecomposition_F64<DMatrixRMaj> svd ,
                                           boolean isRight ,
                                           DMatrixRMaj nullVector )
    {
        int N = svd.numberOfSingularValues();
        double s[] = svd.getSingularValues();

        DMatrixRMaj A = isRight ? svd.getV(null,true) : svd.getU(null,false);

        if( isRight ) {
            if( A.numRows != svd.numCols() ) {
                throw new IllegalArgumentException("Can't compute the null space using a compact SVD for a matrix of this size.");
            }

            if( nullVector == null ) {
                nullVector = new DMatrixRMaj(svd.numCols(),1);
            }
        } else {
            if( A.numCols != svd.numRows() ) {
                throw new IllegalArgumentException("Can't compute the null space using a compact SVD for a matrix of this size.");
            }

            if( nullVector == null ) {
                nullVector = new DMatrixRMaj(svd.numRows(),1);
            }
        }

        int smallestIndex = -1;

        if( isRight && svd.numCols() > svd.numRows() )
            smallestIndex = svd.numCols()-1;
        else if( !isRight && svd.numCols() < svd.numRows() )
            smallestIndex = svd.numRows()-1;
        else {
            // find the smallest singular value
            double smallestValue = Double.MAX_VALUE;

            for( int i = 0; i < N; i++ ) {
                if( s[i] < smallestValue ) {
                    smallestValue = s[i];
                    smallestIndex = i;
                }
            }
        }

        // extract the null space
        if( isRight )
            SpecializedOps_DDRM.subvector(A,smallestIndex,0,A.numRows,true,0,nullVector);
        else
            SpecializedOps_DDRM.subvector(A,0,smallestIndex,A.numRows,false,0,nullVector);

        return nullVector;
    }

    /**
     * Returns a reasonable threshold for singular values.<br><br>
     *
     * tol = max (size (A)) * largest sigma * eps;
     *
     * @param svd A precomputed decomposition.  Not modified.
     * @return threshold for singular values
     */
    public static double singularThreshold( SingularValueDecomposition_F64 svd ) {
        double largest = 0;
        double w[]= svd.getSingularValues();

        int N = svd.numberOfSingularValues();

        for( int j = 0; j < N; j++ ) {
            if( w[j] > largest)
                largest = w[j];
        }

        int M = Math.max(svd.numCols(),svd.numRows());
        return M*largest* UtilEjml.EPS;
    }

    /**
     * Extracts the rank of a matrix using a preexisting decomposition and default threshold.
     *
     * @see #singularThreshold(SingularValueDecomposition_F64)
     *
     * @param svd A precomputed decomposition.  Not modified.
     * @return The rank of the decomposed matrix.
     */
    public static int rank( SingularValueDecomposition_F64 svd ) {
        double threshold = singularThreshold(svd);
        return rank(svd,threshold);
    }

    /**
     * Extracts the rank of a matrix using a preexisting decomposition.
     *
     * @see #singularThreshold(SingularValueDecomposition_F64)
     *
     * @param svd A precomputed decomposition.  Not modified.
     * @param threshold Tolerance used to determine of a singular value is singular.
     * @return The rank of the decomposed matrix.
     */
    public static int rank(SingularValueDecomposition_F64 svd , double threshold ) {
        int numRank=0;

        double w[]= svd.getSingularValues();

        int N = svd.numberOfSingularValues();

        for( int j = 0; j < N; j++ ) {
            if( w[j] > threshold)
                numRank++;
        }

        return numRank;
    }

    /**
     * Extracts the nullity of a matrix using a preexisting decomposition and default threshold.
     *
     * @see #singularThreshold(SingularValueDecomposition_F64)
     *
     * @param svd A precomputed decomposition.  Not modified.
     * @return The nullity of the decomposed matrix.
     */
    public static int nullity( SingularValueDecomposition_F64 svd  ) {
        double threshold = singularThreshold(svd);
        return nullity(svd, threshold);
    }

    /**
     * Extracts the nullity of a matrix using a preexisting decomposition.
     *
     * @see #singularThreshold(SingularValueDecomposition_F64)
     *
     * @param svd A precomputed decomposition.  Not modified.
     * @param threshold Tolerance used to determine of a singular value is singular.
     * @return The nullity of the decomposed matrix.
     */
    public static int nullity(SingularValueDecomposition_F64 svd , double threshold ) {
        int ret = 0;

        double w[]= svd.getSingularValues();

        int N = svd.numberOfSingularValues();

        int numCol = svd.numCols();

        for( int j = 0; j < N; j++ ) {
            if( w[j] <= threshold) ret++;
        }
        return ret + numCol-N;
    }

}
