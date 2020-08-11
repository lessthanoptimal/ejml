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
import org.ejml.data.FGrowArray;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.dense.row.linsol.qr.SolveNullSpaceQRP_FDRM;
import org.ejml.dense.row.linsol.qr.SolveNullSpaceQR_FDRM;
import org.ejml.dense.row.linsol.svd.SolveNullSpaceSvd_FDRM;
import org.ejml.interfaces.SolveNullSpace;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F32;

import java.util.Arrays;


/**
 * Operations related to singular value decomposition.
 *
 * @author Peter Abeles
 */
public class SingularOps_FDRM {

    /**
     * Returns an array of all the singular values in A sorted in ascending order
     *
     * @param A Matrix. Not modified.
     * @return singular values
     */
    public static float[] singularValues( FMatrixRMaj A ) {
        SingularValueDecomposition_F32<FMatrixRMaj> svd = DecompositionFactory_FDRM.svd(A.numRows,A.numCols,false,true,true);

        if( svd.inputModified() ) {
            A = A.copy();
        }
        if( !svd.decompose(A)) {
            throw new RuntimeException("SVD Failed!");
        }

        float sv[] = svd.getSingularValues();
        Arrays.sort(sv,0,svd.numberOfSingularValues());

        // change the ordering to ascending
        for (int i = 0; i < sv.length/2; i++) {
            float tmp = sv[i];
            sv[i] = sv[sv.length-i-1];
            sv[sv.length-i-1] = tmp;
        }

        return sv;
    }

    /**
     * Computes the ratio of the smallest value to the largest. Does not assume
     * the array is sorted first
     * @param sv array
     * @return smallest / largest
     */
    public static float ratioSmallestOverLargest( float []sv ) {
        if( sv.length == 0 )
            return Float.NaN;

        float min = sv[0];
        float max = min;

        for (int i = 1; i < sv.length; i++) {
            float v = sv[i];
            if( v > max )
                max = v;
            else if( v < min )
                min = v;
        }

        return  min/max;
    }

    /**
     * Returns the matrix's rank
     *
     * @param A Matrix. Not modified.
     * @param threshold Tolerance used to determine of a singular value is singular.
     * @return The rank of the decomposed matrix.
     */
    public static int rank( FMatrixRMaj A , float threshold ) {
        SingularValueDecomposition_F32<FMatrixRMaj> svd = DecompositionFactory_FDRM.svd(A.numRows,A.numCols,false,true,true);

        if( svd.inputModified() ) {
            A = A.copy();
        }
        if( !svd.decompose(A)) {
            throw new RuntimeException("SVD Failed!");
        }

        float sv[] = svd.getSingularValues();

        int count = 0;
        for (int i = 0; i < sv.length; i++) {
            if( sv[i] >= threshold ) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the matrix's rank. Automatic selection of threshold
     *
     * @param A Matrix. Not modified.
     * @return The rank of the decomposed matrix.
     */
    public static int rank( FMatrixRMaj A  ) {
        SingularValueDecomposition_F32<FMatrixRMaj> svd = DecompositionFactory_FDRM.svd(A.numRows,A.numCols,false,true,true);

        if( svd.inputModified() ) {
            A = A.copy();
        }
        if( !svd.decompose(A)) {
            throw new RuntimeException("SVD Failed!");
        }

        int N = svd.numberOfSingularValues();
        float sv[] = svd.getSingularValues();

        float threshold = singularThreshold(sv,N);
        int count = 0;
        for (int i = 0; i < sv.length; i++) {
            if( sv[i] >= threshold ) {
                count++;
            }
        }
        return count;
    }

    /**
     * Computes the SVD and sorts singular values in descending order. While easier to use this can reduce performance
     * when performed on small matrices numerous times.
     *
     * U*W*V<sup>T</sup> = A
     *
     * @param A (Input) Matrix being decomposed
     * @param U (Output) Storage for U. If null then it's ignored.
     * @param sv (Output) sorted list of singular values. Can be null.
     * @param Vt (Output) Storage for transposed V. Can be null.
     */
    public static boolean svd(FMatrixRMaj A, FMatrixRMaj U , FGrowArray sv , FMatrixRMaj Vt ) {

        boolean needU = U != null;
        boolean needV = Vt != null;

        SingularValueDecomposition_F32<FMatrixRMaj> svd =
                DecompositionFactory_FDRM.svd(A.numRows,A.numCols,needU,needV,true);

        if( svd.inputModified() ) {
            A = A.copy();
        }

        if( !svd.decompose(A)) {
            return false;
        }

        int N = Math.min(A.numCols,A.numRows);

        if( needU )
            svd.getU(U,false);
        if( needV )
            svd.getV(Vt,true);

        sv.reshape(N);
        System.arraycopy(svd.getSingularValues(), 0, sv.data, 0, N);

        descendingOrder(U,false,sv.data,N,Vt,true);

        return true;
    }


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
    public static void descendingOrder(FMatrixRMaj U , boolean tranU ,
                                       FMatrixRMaj W ,
                                       FMatrixRMaj V , boolean tranV )
    {
        int numSingular = Math.min(W.numRows,W.numCols);

        checkSvdMatrixSize(U, tranU, W, V, tranV);

        for( int i = 0; i < numSingular; i++ ) {
            float bigValue=-1;
            int bigIndex=-1;

            // find the smallest singular value in the submatrix
            for( int j = i; j < numSingular; j++ ) {
                float v = W.get(j,j);

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

            float tmp = W.get(i,i);
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
     * Similar to {@link #descendingOrder(FMatrixRMaj, boolean, FMatrixRMaj, FMatrixRMaj, boolean)}
     * but takes in an array of singular values instead.
     * </p>
     *
     * @param U Matrix. Modified.
     * @param tranU is U transposed or not.
     * @param singularValues Array of singular values. Modified.
     * @param singularLength Number of elements in singularValues array
     * @param V Matrix. Modified.
     * @param tranV is V transposed or not.
     */
    public static void descendingOrder(FMatrixRMaj U , boolean tranU ,
                                       float singularValues[] ,
                                       int singularLength ,
                                       FMatrixRMaj V , boolean tranV )
    {
//        checkSvdMatrixSize(U, tranU, W, V, tranV);

        for( int i = 0; i < singularLength; i++ ) {
            float bigValue=-1;
            int bigIndex=-1;

            // find the smallest singular value in the submatrix
            for( int j = i; j < singularLength; j++ ) {
                float v = singularValues[j];

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

            float tmp = singularValues[i];
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
    public static void checkSvdMatrixSize(FMatrixRMaj U, boolean tranU, FMatrixRMaj W, FMatrixRMaj V, boolean tranV ) {
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

    private static void swapRowOrCol(FMatrixRMaj M, boolean tran, int i, int bigIndex) {
        float tmp;
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
     * @param tol Threshold for selecting singular values.  Try UtilEjml.F_EPS.
     * @return The null space.
     */
    public static FMatrixRMaj nullSpace(SingularValueDecomposition_F32<FMatrixRMaj> svd ,
                                          FMatrixRMaj nullSpace , float tol )
    {
        int N = svd.numberOfSingularValues();
        float s[] = svd.getSingularValues();

        FMatrixRMaj V = svd.getV(null,true);

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
            nullSpace = new FMatrixRMaj(numVectors,svd.numCols());
        } else {
            nullSpace.reshape(numVectors,svd.numCols());
        }

        // now extract the vectors
        int count = 0;
        for( int i = 0; i < N; i++ ) {
            if( s[i] <= tol ) {
                CommonOps_FDRM.extract(V, i,i+1,0, V.numCols,nullSpace,count++,0);
            }
        }
        for( int i = N; i < svd.numCols(); i++ ) {
            CommonOps_FDRM.extract(V, i,i+1,0, V.numCols,nullSpace,count++,0);
        }

        CommonOps_FDRM.transpose(nullSpace);

        return nullSpace;
    }

    /**
     * Computes the null space using QR decomposition. This is much faster than using SVD
     * @param A (Input) Matrix
     * @param totalSingular Number of singular values
     * @return Null space
     */
    public static FMatrixRMaj nullspaceQR( FMatrixRMaj A , int totalSingular ) {
        SolveNullSpaceQR_FDRM solver = new SolveNullSpaceQR_FDRM();

        FMatrixRMaj nullspace = new FMatrixRMaj(1,1);

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
    public static FMatrixRMaj nullspaceQRP( FMatrixRMaj A , int totalSingular ) {
        SolveNullSpaceQRP_FDRM solver = new SolveNullSpaceQRP_FDRM();

        FMatrixRMaj nullspace = new FMatrixRMaj(1,1);

        if( !solver.process(A,totalSingular,nullspace))
            throw new RuntimeException("Solver failed. try SVD based method instead?");

        return nullspace;
    }

    /**
     * Computes the null space using SVD. Slowest bust most stable way to find the solution
     *
     * @param A (Input) Matrix
     * @param totalSingular Number of singular values
     * @return Null space
     */
    public static FMatrixRMaj nullspaceSVD( FMatrixRMaj A , int totalSingular ) {
        SolveNullSpace<FMatrixRMaj> solver = new SolveNullSpaceSvd_FDRM();

        FMatrixRMaj nullspace = new FMatrixRMaj(1,1);

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
    public static FMatrixRMaj nullVector(SingularValueDecomposition_F32<FMatrixRMaj> svd ,
                                           boolean isRight ,
                                           FMatrixRMaj nullVector )
    {
        int N = svd.numberOfSingularValues();
        float s[] = svd.getSingularValues();

        FMatrixRMaj A = isRight ? svd.getV(null,true) : svd.getU(null,false);

        if( isRight ) {
            if( A.numRows != svd.numCols() ) {
                throw new IllegalArgumentException("Can't compute the null space using a compact SVD for a matrix of this size.");
            }

            if( nullVector == null ) {
                nullVector = new FMatrixRMaj(svd.numCols(),1);
            } else {
                nullVector.reshape(svd.numCols(),1);
            }
        } else {
            if (A.numCols != svd.numRows()) {
                throw new IllegalArgumentException("Can't compute the null space using a compact SVD for a matrix of this size.");
            }

            if (nullVector == null) {
                nullVector = new FMatrixRMaj(svd.numRows(), 1);
            } else {
                nullVector.reshape(svd.numRows(), 1);
            }
        }

        int smallestIndex = -1;

        if( isRight && svd.numCols() > svd.numRows() )
            smallestIndex = svd.numCols()-1;
        else if( !isRight && svd.numCols() < svd.numRows() )
            smallestIndex = svd.numRows()-1;
        else {
            // find the smallest singular value
            float smallestValue = Float.MAX_VALUE;

            for( int i = 0; i < N; i++ ) {
                if( s[i] < smallestValue ) {
                    smallestValue = s[i];
                    smallestIndex = i;
                }
            }
        }

        // extract the null space
        if( isRight )
            SpecializedOps_FDRM.subvector(A,smallestIndex,0,A.numRows,true,0,nullVector);
        else
            SpecializedOps_FDRM.subvector(A,0,smallestIndex,A.numRows,false,0,nullVector);

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
    public static float singularThreshold( SingularValueDecomposition_F32 svd ) {
        return singularThreshold(svd,UtilEjml.F_EPS);
    }

    public static float singularThreshold( SingularValueDecomposition_F32 svd , float tolerance ) {

        float w[]= svd.getSingularValues();

        int N = svd.numberOfSingularValues();

        return singularThreshold( w, N, tolerance);
    }

    private static float singularThreshold( float[] w, int N) {
        return singularThreshold(w, N, UtilEjml.F_EPS );
    }

    private static float singularThreshold( float[] w, int N , float tolerance ) {
        float largest = 0;
        for(int j = 0; j < N; j++ ) {
            if( w[j] > largest)
                largest = w[j];
        }

        return N*largest*tolerance;
    }

    /**
     * Extracts the rank of a matrix using a preexisting decomposition and default threshold.
     *
     * @see #singularThreshold(SingularValueDecomposition_F32)
     *
     * @param svd A precomputed decomposition.  Not modified.
     * @return The rank of the decomposed matrix.
     */
    public static int rank( SingularValueDecomposition_F32 svd ) {
        float threshold = singularThreshold(svd);
        return rank(svd,threshold);
    }

    /**
     * Extracts the rank of a matrix using a preexisting decomposition.
     *
     * @see #singularThreshold(SingularValueDecomposition_F32)
     *
     * @param svd A precomputed decomposition.  Not modified.
     * @param threshold Tolerance used to determine of a singular value is singular.
     * @return The rank of the decomposed matrix.
     */
    public static int rank(SingularValueDecomposition_F32 svd , float threshold ) {
        int numRank=0;

        float w[]= svd.getSingularValues();

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
     * @see #singularThreshold(SingularValueDecomposition_F32)
     *
     * @param svd A precomputed decomposition.  Not modified.
     * @return The nullity of the decomposed matrix.
     */
    public static int nullity( SingularValueDecomposition_F32 svd  ) {
        float threshold = singularThreshold(svd);
        return nullity(svd, threshold);
    }

    /**
     * Extracts the nullity of a matrix using a preexisting decomposition.
     *
     * @see #singularThreshold(SingularValueDecomposition_F32)
     *
     * @param svd A precomputed decomposition.  Not modified.
     * @param threshold Tolerance used to determine of a singular value is singular.
     * @return The nullity of the decomposed matrix.
     */
    public static int nullity(SingularValueDecomposition_F32 svd , float threshold ) {
        int ret = 0;

        float w[]= svd.getSingularValues();

        int N = svd.numberOfSingularValues();

        int numCol = svd.numCols();

        for( int j = 0; j < N; j++ ) {
            if( w[j] <= threshold) ret++;
        }
        return ret + numCol-N;
    }

    /**
     * Returns the matrix's nullity
     *
     * @param A Matrix. Not modified.
     * @param threshold Tolerance used to determine of a singular value is singular.
     * @return nullity
     */
    public static int nullity( FMatrixRMaj A , float threshold ) {
        SingularValueDecomposition_F32<FMatrixRMaj> svd = DecompositionFactory_FDRM.svd(A.numRows,A.numCols,false,true,true);

        if( svd.inputModified() ) {
            A = A.copy();
        }
        if( !svd.decompose(A)) {
            throw new RuntimeException("SVD Failed!");
        }

        float sv[] = svd.getSingularValues();

        int count = 0;
        for (int i = 0; i < sv.length; i++) {
            if( sv[i] <= threshold ) {
                count++;
            }
        }
        return count;
    }
}
