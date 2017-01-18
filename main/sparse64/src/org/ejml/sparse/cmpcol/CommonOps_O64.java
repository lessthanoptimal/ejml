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

package org.ejml.sparse.cmpcol;

import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.SMatrixCmpC_F64;
import org.ejml.sparse.cmpcol.misc.ImplCommonOps_O64;
import org.ejml.sparse.cmpcol.mult.ImplSparseSparseMult_O64;

import java.util.Arrays;

/**
 * @author Peter Abeles
 */
public class CommonOps_O64 {

    /**
     * Checks to see if row indicies are sorted into ascending order.  O(N)
     * @return true if sorted and false if not
     */
    public static boolean checkIndicesSorted( SMatrixCmpC_F64 A ) {
        for (int j = 0; j < A.numCols; j++) {
            int idx0 = A.col_idx[j];
            int idx1 = A.col_idx[j+1];

            if( idx0 != idx1 && A.nz_rows[idx0] >= A.numRows )
                return false;

            for (int i = idx0+1; i < idx1; i++) {
                int row = A.nz_rows[i];
                if( A.nz_rows[i-1] >= row)
                    return false;
                if( row >= A.numRows )
                    return false;
            }
        }
        return true;
    }

    public static boolean checkSortedFlag( SMatrixCmpC_F64 A ) {
        if( A.indicesSorted )
            return checkIndicesSorted(A);
        return true;
    }

    /**
     * Perform matrix transpose
     *
     * @param a Input matrix.  Not modified
     * @param a_t Storage for transpose of 'a'.  Must be correct shape.  data length might be adjusted.
     * @param work Optional work matrix.  null or of length a.numRows
     */
    public static void transpose(SMatrixCmpC_F64 a , SMatrixCmpC_F64 a_t , int work[] ) {
        if( a_t.numRows != a.numCols || a_t.numCols != a.numRows )
            throw new IllegalArgumentException("Unexpected shape for transpose matrix");

        a_t.growMaxLength(a.nz_length, false);
        a_t.nz_length = a.nz_length;

        ImplCommonOps_O64.transpose(a, a_t, work);
    }

    public static void mult(SMatrixCmpC_F64 A , SMatrixCmpC_F64 B , SMatrixCmpC_F64 C ) {
        mult(A,B,C,null,null);
    }

    /**
     * Performs matrix multiplication.  C = A*B
     *
     * @param A Matrix
     * @param B Matrix
     * @param C Storage for results.  Data length is increased if increased if insufficient.
     * @param workA (Optional) Storage for internal work.  null or array of length A.numRows
     * @param workB (Optional) Storage for internal work.  null or array of length A.numRows
     */
    public static void mult(SMatrixCmpC_F64 A , SMatrixCmpC_F64 B , SMatrixCmpC_F64 C ,
                            int workA[], double workB[] )
    {
        if( A.numRows != C.numRows || B.numCols != C.numCols )
            throw new IllegalArgumentException("Inconsistent matrix shapes");

        ImplSparseSparseMult_O64.mult(A,B,C, workA, workB);
    }

    /**
     * Performs matrix multiplication.  C = A*B
     *
     * @param A Matrix
     * @param B Dense Matrix
     * @param C Dense Matrix
     */
    public static void mult(SMatrixCmpC_F64 A , DMatrixRow_F64 B , DMatrixRow_F64 C )
    {
        if( A.numRows != C.numRows || B.numCols != C.numCols )
            throw new IllegalArgumentException("Inconsistent matrix shapes");

        ImplSparseSparseMult_O64.mult(A,B,C);
    }

    /**
     * Performs matrix addition:<br>
     * C = &alpha;A + &beta;B
     *
     * @param alpha scalar value multiplied against A
     * @param A Matrix
     * @param beta scalar value multiplied against B
     * @param B Matrix
     * @param C Output matrix.
     * @param workC (Optional) Work space.  null or as long as A.rows.
     */
    public static void add(double alpha , SMatrixCmpC_F64 A , double beta , SMatrixCmpC_F64 B , SMatrixCmpC_F64 C ,
                           double workC[] )
    {
        if( A.numRows != B.numRows || A.numCols != B.numCols || A.numRows != C.numRows || A.numCols != C.numCols)
            throw new IllegalArgumentException("Inconsistent matrix shapes");

        ImplCommonOps_O64.add(alpha,A,beta,B,C, workC);
    }

    public static SMatrixCmpC_F64 identity( int length ) {
        return identity(length, length);
    }

    public static SMatrixCmpC_F64 identity( int numRows , int numCols ) {
        int min = Math.min(numRows, numCols);
        SMatrixCmpC_F64 A = new SMatrixCmpC_F64(numRows, numCols, min);

        Arrays.fill(A.nz_values,0,min,1);
        for (int i = 1; i <= min; i++) {
            A.col_idx[i] = i;
            A.nz_rows[i-1] = i-1;
        }
        for (int i = min+1; i <= numCols; i++) {
            A.col_idx[i] = min;
        }

        return A;
    }

    public static void scale(double scalar, SMatrixCmpC_F64 A, SMatrixCmpC_F64 B) {
        if( A.numRows != B.numRows || A.numCols != B.numCols )
            throw new IllegalArgumentException("Unexpected shape for transpose matrix");
        B.copyStructure(A);

        for(int i = 0; i < A.nz_length; i++ ) {
            B.nz_values[i] = A.nz_values[i]*scalar;
        }
    }

    public static void divide(SMatrixCmpC_F64 A , double scalar , SMatrixCmpC_F64 B ) {
        if( A.numRows != B.numRows || A.numCols != B.numCols )
            throw new IllegalArgumentException("Unexpected shape for transpose matrix");
        B.copyStructure(A);

        for(int i = 0; i < A.nz_length; i++ ) {
            B.nz_values[i] = A.nz_values[i]/scalar;
        }
    }

    public static double elementMinAbs( SMatrixCmpC_F64 A ) {
        if( A.nz_length == 0)
            return 0;

        double min = A.isFull() ? Math.abs(A.nz_values[0]) : 0;
        for(int i = 0; i < A.nz_length; i++ ) {
            double val = Math.abs(A.nz_values[i]);
            if( val < min ) {
                min = val;
            }
        }

        return min;
    }

    public static double elementMaxAbs( SMatrixCmpC_F64 A ) {
        if( A.nz_length == 0)
            return 0;

        double max = A.isFull() ? Math.abs(A.nz_values[0]) : 0;
        for(int i = 0; i < A.nz_length; i++ ) {
            double val = Math.abs(A.nz_values[i]);
            if( val > max ) {
                max = val;
            }
        }

        return max;
    }

    public static double elementMin( SMatrixCmpC_F64 A ) {
        if( A.nz_length == 0)
            return 0;

        double min = A.isFull() ? A.nz_values[0] : 0;
        for(int i = 0; i < A.nz_length; i++ ) {
            double val = A.nz_values[i];
            if( val < min ) {
                min = val;
            }
        }

        return min;
    }

    public static double elementMax( SMatrixCmpC_F64 A ) {
        if( A.nz_length == 0)
            return 0;

        double max = A.isFull() ? A.nz_values[0] : 0;
        for(int i = 0; i < A.nz_length; i++ ) {
            double val = A.nz_values[i];
            if( val > max ) {
                max = val;
            }
        }

        return max;
    }

    public static SMatrixCmpC_F64 diag( double... values ) {
        int N = values.length;
        SMatrixCmpC_F64 A = new SMatrixCmpC_F64(N,N,N);

        for (int i = 0; i < N; i++) {
            A.col_idx[i+1] = i+1;
            A.nz_rows[i] = i;
            A.nz_values[i] = values[i];
        }

        return A;
    }
}
