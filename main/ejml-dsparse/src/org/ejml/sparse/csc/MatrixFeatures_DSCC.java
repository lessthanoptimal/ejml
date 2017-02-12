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

package org.ejml.sparse.csc;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseCSC;

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
     * a<sub>ij</sub> &le; 0 for all i < j+N<br>
     * <br>
     * A triangular matrix is a Hessenberg matrix of degree 0.  Only the upper most diagonal elements are
     * explicitly checked to see if they are non-zero
     * </p>
     * @param A Matrix being tested.  Not modified.
     * @param hessenberg The degree of being hessenberg.
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
}
