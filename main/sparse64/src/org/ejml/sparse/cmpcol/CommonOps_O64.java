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

import org.ejml.data.SMatrixCC_F64;
import org.ejml.sparse.cmpcol.misc.ImplCommonOps_O64;
import org.ejml.sparse.cmpcol.mult.ImplSparseSparseMult_O64;

/**
 * @author Peter Abeles
 */
public class CommonOps_O64 {

    /**
     * Perform matrix transpose
     *
     * @param a Input matrix.  Not modified
     * @param a_t Storage for transpose of 'a'.  Must be correct shape.  data length might be adjusted.
     * @param work Optional work matrix.  null or of length a.numRows
     */
    public static void transpose(SMatrixCC_F64 a , SMatrixCC_F64 a_t , int work[] ) {
        if( a_t.numRows != a.numCols || a_t.numCols != a.numRows )
            throw new IllegalArgumentException("Unexpected shape for transpose matrix");

        a_t.growMaxLength(a.length, false);
        a_t.length = a.length;

        ImplCommonOps_O64.transpose(a, a_t, work);
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
    public static void mult(SMatrixCC_F64 A , SMatrixCC_F64 B , SMatrixCC_F64 C ,
                            int workA[], double workB[] )
    {
        if( A.numRows != C.numRows || B.numCols != C.numCols )
            throw new IllegalArgumentException("Inconsistent matrix shapes");

        ImplSparseSparseMult_O64.mult(A,B,C, workA, workB);
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
    public static void add(double alpha , SMatrixCC_F64 A , double beta , SMatrixCC_F64 B , SMatrixCC_F64 C ,
                           double workC[] )
    {
        if( A.numRows != B.numRows || A.numCols != B.numCols || A.numRows != C.numRows || A.numCols != C.numCols)
            throw new IllegalArgumentException("Inconsistent matrix shapes");

        ImplCommonOps_O64.add(alpha,A,beta,B,C, workC);
    }
}
