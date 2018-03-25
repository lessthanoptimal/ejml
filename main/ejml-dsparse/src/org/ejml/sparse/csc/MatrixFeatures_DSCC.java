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

package org.ejml.sparse.csc;

import org.ejml.UtilEjml;
import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.interfaces.decomposition.CholeskySparseDecomposition;
import org.ejml.sparse.csc.decomposition.chol.CholeskyUpLooking_DSCC;

/**
 * @author Peter Abeles
 */
public class MatrixFeatures_DSCC {

    public static boolean isEquals(DMatrixSparseCSC a , DMatrixSparseCSC b ) {
        if( !a.indicesSorted || !b.indicesSorted )
            throw new IllegalArgumentException("Inputs must have sorted indices");

        if( !isSameStructure(a,b) )
            return false;

        for (int i = 0; i < a.nz_length; i++) {
            if( a.nz_values[i] != b.nz_values[i] )
                return false;
        }
        return true;
    }

    public static boolean isEquals(DMatrixSparseCSC a , DMatrixSparseCSC b , double tol ) {
        if( !a.indicesSorted || !b.indicesSorted )
            throw new IllegalArgumentException("Inputs must have sorted indices");
        if( !isSameStructure(a,b) )
            return false;

        for (int i = 0; i < a.nz_length; i++) {
            if( Math.abs(a.nz_values[i]-b.nz_values[i]) > tol )
                return false;
        }
        return true;
    }

    public static boolean isEqualsSort(DMatrixSparseCSC a , DMatrixSparseCSC b , double tol ) {
        if( !a.indicesSorted )
            a.sortIndices(null);
        if( !b.indicesSorted )
            b.sortIndices(null);
        if( !isSameStructure(a,b) )
            return false;

        for (int i = 0; i < a.nz_length; i++) {
            if( Math.abs(a.nz_values[i]-b.nz_values[i]) > tol )
                return false;
        }
        return true;
    }

    public static boolean isIdenticalSort(DMatrixSparseCSC a , DMatrixSparseCSC b , double tol ) {
        if( !a.indicesSorted )
            a.sortIndices(null);
        if( !b.indicesSorted )
            b.sortIndices(null);
        if( !isSameStructure(a,b) )
            return false;

        for (int i = 0; i < a.nz_length; i++) {
            if( !UtilEjml.isIdentical(a.nz_values[i],b.nz_values[i], tol))
                return false;
        }
        return true;
    }

    /**
     * Checks to see if the two matrices have the same shape and same pattern of non-zero elements
     *
     * @param a Matrix
     * @param b Matrix
     * @return true if the structure is the same
     */
    public static boolean isSameStructure(DMatrixSparseCSC a , DMatrixSparseCSC b) {
        if( a.numRows == b.numRows && a.numCols == b.numCols && a.nz_length == b.nz_length) {
            for (int i = 0; i <= a.numCols; i++) {
                if( a.col_idx[i] != b.col_idx[i] )
                    return false;
            }
            for (int i = 0; i < a.nz_length; i++) {
                if( a.nz_rows[i] != b.nz_rows[i] )
                    return false;
            }
            return true;
        }
        return false;
    }

    public static boolean hasUncountable( DMatrixSparseCSC A ) {
        for(int i = 0; i < A.nz_length; i++ ) {
            if(UtilEjml.isUncountable(A.nz_values[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isZeros(DMatrixSparseCSC A , double tol ) {
        for(int i = 0; i < A.nz_length; i++ ) {
            if(Math.abs(A.nz_values[i]) > tol) {
                return false;
            }
        }
        return true;
    }

    public static boolean isIdentity(DMatrixSparseCSC A , double tol ) {
        if( A.numCols != A.numRows )
            return false;

        if( A.nz_length != A.numCols )
            return false;

        for( int i = 1; i <= A.numCols; i++ ) {
            if( A.col_idx[i] != i)
                return false;
            if( Math.abs(A.nz_values[i-1]-1) > tol )
                return false;
        }
        return true;
    }

    /**
     * <p>
     * Checks to see if a matrix is lower triangular or Hessenberg. A Hessenberg matrix of degree N
     * has the following property:<br>
     * <br>
     * a<sub>ij</sub> &le; 0 for all i &lt; j+N<br>
     * <br>
     * A triangular matrix is a Hessenberg matrix of degree 0.  Only the upper most diagonal elements are
     * explicitly checked to see if they are non-zero
     * </p>
     * @param A Matrix being tested.  Not modified.
     * @param hessenberg The degree of being hessenberg.
     * @param tol How not zero diagonal elements must be.
     * @return If it is an upper triangular/hessenberg matrix or not.
     */
    public static boolean isLowerTriangle(DMatrixSparseCSC A , int hessenberg , double tol )
    {
        if( A.numCols != A.numRows )
            return false;

        // diagonal elements must be non-zero
        if( A.nz_length < A.numCols-hessenberg )
            return false;

        for (int col = 0; col < A.numCols; col++) {
            int idx0 = A.col_idx[col];
            int idx1 = A.col_idx[col+1];

            // at least one element in each column
            if( col >= hessenberg ) {
                if (idx0 == idx1)
                    return false;

                // first element must be (i,i)
                if (A.nz_rows[idx0] != Math.max(0, col - hessenberg))
                    return false;
            }

            // diagonal elements must not be zero
            if( col-hessenberg >= 0 && Math.abs(A.nz_values[idx0]) <= tol )
                return false;
        }

        return true;
    }

    public static boolean isTranspose( DMatrixSparseCSC A , DMatrixSparseCSC B , double tol ) {
        if( A.numCols != B.numRows || A.numRows != B.numCols )
            return false;
        if( A.nz_length != B.nz_length )
            return false;
        if( !A.indicesSorted )
            throw new IllegalArgumentException("A must have sorted indicies");

        DMatrixSparseCSC Btran = new DMatrixSparseCSC(B.numCols,B.numRows,B.nz_length);

        CommonOps_DSCC.transpose(B,Btran,null);
        Btran.sortIndices(null);

        for (int i = 0; i < B.nz_length; i++) {
            if( A.nz_rows[i] != Btran.nz_rows[i] )
                return false;
            if( Math.abs(A.nz_values[i] - Btran.nz_values[i]) > tol )
                return false;
        }
        return true;
    }

    /**
     * Returns true if the input is a vector
     * @param a A matrix or vector
     * @return true if it's a vector.  Column or row.
     */
    public static boolean isVector(DMatrixSparseCSC a) {
        return (a.numCols == 1 && a.numRows > 1) || (a.numRows == 1 && a.numCols>1);
    }

    /**
     * Checks to see if the matrix is symmetric to within tolerance.
     *
     * @param A Matrix being tested.  Not modified.
     * @param tol Tolerance that defines how similar two values must be to be considered identical
     * @return true if symmetric or false if not
     */
    public static boolean isSymmetric( DMatrixSparseCSC A , double tol ) {
        if( A.numRows != A.numCols )
            return false;

        int N = A.numCols;

        for (int i = 0; i < N; i++) {
            int idx0 = A.col_idx[i];
            int idx1 = A.col_idx[i+1];

            for (int index = idx0; index < idx1; index++) {
                int j = A.nz_rows[index];
                double value_ji = A.nz_values[index];
                double value_ij = A.get(i,j);

                if( Math.abs(value_ij-value_ji) > tol )
                    return false;
            }
        }

        return true;
    }

    /**
     * <p>
     * Checks to see if the matrix is positive definite.
     * </p>
     * <p>
     * x<sup>T</sup> A x &gt; 0<br>
     * for all x where x is a non-zero vector and A is a symmetric matrix.
     * </p>
     *
     * @param A square symmetric matrix. Not modified.
     *
     * @return True if it is positive definite and false if it is not.
     */
    public static boolean isPositiveDefinite( DMatrixSparseCSC A ) {
        if( A.numRows != A.numCols )
            return false;

        CholeskySparseDecomposition<DMatrixSparseCSC> chol = new CholeskyUpLooking_DSCC();
        return chol.decompose(A);
    }

    /**
     * <p>
     * Checks to see if a matrix is orthogonal or isometric.
     * </p>
     *
     * @param Q The matrix being tested. Not modified.
     * @param tol Tolerance.
     * @return True if it passes the test.
     */
    public static boolean isOrthogonal(DMatrixSparseCSC Q , double tol )
    {
        if( Q.numRows < Q.numCols ) {
            throw new IllegalArgumentException("The number of rows must be more than or equal to the number of columns");
        }

        IGrowArray gw=new IGrowArray();
        DGrowArray gx=new DGrowArray();

        for( int i = 0; i < Q.numRows; i++ ) {

            for( int j = i+1; j < Q.numCols; j++ ) {
                double val = CommonOps_DSCC.dotInnerColumns(Q,i,Q,j,gw,gx);

                if( !(Math.abs(val) <= tol))
                    return false;
            }
        }

        return true;
    }
}
