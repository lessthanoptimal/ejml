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

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.interfaces.decomposition.LUSparseDecomposition_F64;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.factory.DecompositionFactory_DSCC;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;
import org.ejml.sparse.csc.misc.ImplCommonOps_DSCC;
import org.ejml.sparse.csc.mult.ImplSparseSparseMult_DSCC;

import java.util.Arrays;

import static org.ejml.sparse.csc.misc.TriangularSolver_DSCC.adjustClear;

/**
 * @author Peter Abeles
 */
public class CommonOps_DSCC {

    /**
     * Checks to see if row indicies are sorted into ascending order.  O(N)
     * @return true if sorted and false if not
     */
    public static boolean checkIndicesSorted( DMatrixSparseCSC A ) {
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

    public static boolean checkStructure( DMatrixSparseCSC A ) {
        if( A.col_idx.length < A.numCols+1)
            return false;
        if( A.col_idx[A.numCols] != A.nz_length )
            return false;
        if( A.nz_rows.length < A.nz_length)
            return false;
        if( A.nz_values.length < A.nz_length)
            return false;
        if( A.col_idx[0] != 0 )
            return false;
        for (int i = 0; i < A.numCols; i++) {
            if( A.col_idx[i] > A.col_idx[i+1] ) {
                return false;
            }
            if( A.col_idx[i+1]-A.col_idx[i] > A.numRows)
                return false;
        }
        if( !checkSortedFlag(A))
            return false;
        if( checkDuplicateElements(A))
            return false;
        return true;
    }

    public static boolean checkSortedFlag( DMatrixSparseCSC A ) {
        if( A.indicesSorted )
            return checkIndicesSorted(A);
        return true;
    }

    /**
     * Checks for duplicate elements. A is sorted
     * @param A Matrix to be tested.
     * @return true if duplicates or false if false duplicates
     */
    public static boolean checkDuplicateElements(DMatrixSparseCSC A ) {
        A = A.copy(); // create a copy so that it doesn't modify A
        A.sortIndices(null);
        return !checkSortedFlag(A);
    }

    /**
     * Perform matrix transpose
     *
     * @param a Input matrix.  Not modified
     * @param a_t Storage for transpose of 'a'.  Must be correct shape.  data length might be adjusted.
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     */
    public static void transpose(DMatrixSparseCSC a , DMatrixSparseCSC a_t , IGrowArray gw ) {
        if( a_t.numRows != a.numCols || a_t.numCols != a.numRows )
            throw new IllegalArgumentException("Unexpected shape for transpose matrix");

        a_t.growMaxLength(a.nz_length, false);
        a_t.nz_length = a.nz_length;

        ImplCommonOps_DSCC.transpose(a, a_t, gw);
    }

    public static void mult(DMatrixSparseCSC A , DMatrixSparseCSC B , DMatrixSparseCSC C ) {
        mult(A,B,C,null,null);
    }

    /**
     * Performs matrix multiplication.  C = A*B
     *
     * @param A (Input) Matrix. Not modified.
     * @param B (Input) Matrix. Not modified.
     * @param C (Output) Storage for results.  Data length is increased if increased if insufficient.
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     * @param gx (Optional) Storage for internal workspace.  Can be null.
     */
    public static void mult(DMatrixSparseCSC A , DMatrixSparseCSC B , DMatrixSparseCSC C ,
                            IGrowArray gw, DGrowArray gx )
    {
        if( A.numRows != C.numRows || B.numCols != C.numCols )
            throw new IllegalArgumentException("Inconsistent matrix shapes");

        ImplSparseSparseMult_DSCC.mult(A,B,C, gw, gx);
    }

    public static void multTransA(DMatrixSparseCSC A , DMatrixSparseCSC B , DMatrixSparseCSC C ,
                            IGrowArray gw, DGrowArray gx )
    {
        if( A.numCols != C.numRows || B.numCols != C.numCols )
            throw new IllegalArgumentException("Inconsistent matrix shapes");

        ImplSparseSparseMult_DSCC.multTransA(A,B,C,gw,gx);
    }

    /**
     * Performs matrix multiplication.  C = A*B<sup>T</sup>. B needs to be sorted and will be sorted if it
     * has not already been sorted.
     *
     * @param A (Input) Matrix. Not modified.
     * @param B (Input) Matrix. Value not modified but indicies will be sorted if not sorted already.
     * @param C (Output) Storage for results.  Data length is increased if increased if insufficient.
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     * @param gx (Optional) Storage for internal workspace.  Can be null.
     */
    public static void multTransB(DMatrixSparseCSC A , DMatrixSparseCSC B , DMatrixSparseCSC C ,
                                  IGrowArray gw, DGrowArray gx )
    {
        if( A.numRows != C.numRows || B.numRows != C.numCols )
            throw new IllegalArgumentException("Inconsistent matrix shapes");

        if( !B.isIndicesSorted() )
            B.sortIndices(null);

        ImplSparseSparseMult_DSCC.multTransB(A,B,C,gw,gx);
    }


    /**
     * Performs matrix multiplication.  C = A*B
     *
     * @param A Matrix
     * @param B Dense Matrix
     * @param C Dense Matrix
     */
    public static void mult(DMatrixSparseCSC A , DMatrixRMaj B , DMatrixRMaj C )
    {
        if( A.numRows != C.numRows || B.numCols != C.numCols )
            throw new IllegalArgumentException("Inconsistent matrix shapes");

        ImplSparseSparseMult_DSCC.mult(A,B,C);
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
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     * @param gx (Optional) Storage for internal workspace.  Can be null.
     */
    public static void add(double alpha, DMatrixSparseCSC A, double beta, DMatrixSparseCSC B, DMatrixSparseCSC C,
                           IGrowArray gw, DGrowArray gx)
    {
        if( A.numRows != B.numRows || A.numCols != B.numCols || A.numRows != C.numRows || A.numCols != C.numCols)
            throw new IllegalArgumentException("Inconsistent matrix shapes");

        ImplCommonOps_DSCC.add(alpha,A,beta,B,C, gw, gx);
    }

    public static DMatrixSparseCSC identity(int length ) {
        return identity(length, length);
    }

    public static DMatrixSparseCSC identity(int numRows , int numCols ) {
        int min = Math.min(numRows, numCols);
        DMatrixSparseCSC A = new DMatrixSparseCSC(numRows, numCols, min);
        setToIdentity(A);
        return A;
    }

    public static void setToIdentity(DMatrixSparseCSC A ) {
        int min = Math.min(A.numRows, A.numCols);
        A.growMaxLength(min,false);
        A.nz_length = min;

        Arrays.fill(A.nz_values,0,min,1);
        for (int i = 1; i <= min; i++) {
            A.col_idx[i] = i;
            A.nz_rows[i-1] = i-1;
        }
        for (int i = min+1; i <= A.numCols; i++) {
            A.col_idx[i] = min;
        }
    }


    /**
     * B = scalar*A.   A and B can be the same instance.
     *
     * @param scalar (Input) Scalar value
     * @param A (Input) Matrix. Not modified.
     * @param B (Output) Matrix. Modified.
     */
    public static void scale(double scalar, DMatrixSparseCSC A, DMatrixSparseCSC B) {
        if( A != B ) {
            if (A.numRows != B.numRows || A.numCols != B.numCols)
                throw new IllegalArgumentException("A and B must have the same shape");
            B.copyStructure(A);

            for(int i = 0; i < A.nz_length; i++ ) {
                B.nz_values[i] = A.nz_values[i]*scalar;
            }
        } else {
            for(int i = 0; i < A.nz_length; i++ ) {
                B.nz_values[i] *= scalar;
            }
        }
    }

    /**
     * B = A/scalar.   A and B can be the same instance.
     *
     * @param scalar (Input) Scalar value
     * @param A (Input) Matrix. Not modified.
     * @param B (Output) Matrix. Modified.
     */
    public static void divide(DMatrixSparseCSC A , double scalar , DMatrixSparseCSC B ) {
        if( A.numRows != B.numRows || A.numCols != B.numCols )
            throw new IllegalArgumentException("Unexpected shape for transpose matrix");
        if( A != B ) {
            B.copyStructure(A);

            for (int i = 0; i < A.nz_length; i++) {
                B.nz_values[i] = A.nz_values[i] / scalar;
            }
        } else {
            for (int i = 0; i < A.nz_length; i++) {
                A.nz_values[i] /= scalar;
            }
        }
    }

    /**
     * B = -A.   Changes the sign of elements in A and stores it in B. A and B can be the same instance.
     *
     * @param A (Input) Matrix. Not modified.
     * @param B (Output) Matrix. Modified.
     */
    public static void changeSign(DMatrixSparseCSC A , DMatrixSparseCSC B ) {
        if( A.numRows != B.numRows || A.numCols != B.numCols )
            throw new IllegalArgumentException("Unexpected shape for transpose matrix");
        if( A != B ) {
            B.copyStructure(A);
        }

        for (int i = 0; i < A.nz_length; i++) {
            B.nz_values[i] = -A.nz_values[i];
        }
    }

    /**
     * Returns the value of the element with the largest abs()
     * @param A (Input) Matrix. Not modified.
     * @return scalar
     */
    public static double elementMinAbs( DMatrixSparseCSC A ) {
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

    public static double elementMaxAbs( DMatrixSparseCSC A ) {
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

    public static double elementMin( DMatrixSparseCSC A ) {
        if( A.nz_length == 0)
            return 0;

        // if every element is assigned a value then the first element can be a minimum.
        // Otherwise zero needs to be considered
        double min = A.isFull() ? A.nz_values[0] : 0;
        for(int i = 0; i < A.nz_length; i++ ) {
            double val = A.nz_values[i];
            if( val < min ) {
                min = val;
            }
        }

        return min;
    }

    public static double elementMax( DMatrixSparseCSC A ) {
        if( A.nz_length == 0)
            return 0;

        // if every element is assigned a value then the first element can be a max.
        // Otherwise zero needs to be considered
        double max = A.isFull() ? A.nz_values[0] : 0;
        for(int i = 0; i < A.nz_length; i++ ) {
            double val = A.nz_values[i];
            if( val > max ) {
                max = val;
            }
        }

        return max;
    }

    public static double elementSum( DMatrixSparseCSC A ) {
        if( A.nz_length == 0)
            return 0;

        double sum = 0;
        for(int i = 0; i < A.nz_length; i++ ) {
            sum += A.nz_values[i];
        }

        return sum;
    }

    /**
     * Performs an element-wise multiplication.<br>
     * C[i,j] = A[i,j]*B[i,j]<br>
     * All matrices must have the same shape.
     *
     * @param A (Input) Matrix.
     * @param B (Input) Matrix
     * @param C (Ouptut) Matrix.
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     * @param gx (Optional) Storage for internal workspace.  Can be null.
     */
    public static void elementMult( DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC C ,
                                    IGrowArray gw, DGrowArray gx) {
        if( A.numCols != B.numCols || A.numRows != B.numRows || A.numCols != C.numCols || A.numRows != C.numRows )
            throw new IllegalArgumentException("All inputs must have the same number of rows and columns");

        ImplCommonOps_DSCC.elementMult(A,B,C,gw,gx);
    }

    /**
     * Returns a diagonal matrix with the specified diagonal elements.
     * @param values values of diagonal elements
     * @return A diagonal matrix
     */
    public static DMatrixSparseCSC diag(double... values ) {
        int N = values.length;
        DMatrixSparseCSC A = new DMatrixSparseCSC(N,N,N);
        A.nz_length = N;

        for (int i = 0; i < N; i++) {
            A.col_idx[i+1] = i+1;
            A.nz_rows[i] = i;
            A.nz_values[i] = values[i];
        }

        return A;
    }

    /**
     * Converts the permutation vector into a matrix. B = P*A.  B[p[i],:] = A[i,:]
     * @param p (Input) Permutation vector
     * @param inverse (Input) If it is the inverse. B[i,:] = A[p[i],:)
     * @param P (Output) Permutation matrix
     */
    public static DMatrixSparseCSC permutationMatrix(int[] p, boolean inverse, int N, DMatrixSparseCSC P) {

        if( P == null )
            P = new DMatrixSparseCSC(N,N,N);
        else
            P.reshape(N,N,N);
        P.indicesSorted = true;

        // each column should have one element inside of it
        if( !inverse ) {
            for (int i = 0; i < N; i++) {
                P.col_idx[i + 1] = i + 1;
                P.nz_rows[p[i]] = i;
                P.nz_values[i] = 1;
            }
        } else {
            for (int i = 0; i < N; i++) {
                P.col_idx[i + 1] = i + 1;
                P.nz_rows[i] = p[i];
                P.nz_values[i] = 1;
            }
        }

        return P;
    }

    /**
     * Converts the permutation matrix into a vector
     * @param P (Input) Permutation matrix
     * @param vector (Output) Permutation vector
     */
    public static void permutationVector( DMatrixSparseCSC P , int[] vector) {
        if( P.numCols != P.numRows ) {
            throw new IllegalArgumentException("Expected a square matrix");
        } else if( P.nz_length != P.numCols ) {
            throw new IllegalArgumentException("Expected N non-zero elements in permutation matrix");
        } else if( vector.length < P.numCols ) {
            throw new IllegalArgumentException("vector is too short");
        }

        int M = P.numCols;

        for (int i = 0; i < M; i++) {
            if( P.col_idx[i+1] != i+1 )
                throw new IllegalArgumentException("Unexpected number of elements in a column");

            vector[P.nz_rows[i]] = i;
        }
    }

    /**
     * Computes the inverse permutation vector
     *
     * @param original Original permutation vector
     * @param inverse It's inverse
     */
    public static void permutationInverse( int []original , int []inverse , int length ) {
        for (int i = 0; i < length; i++) {
            inverse[original[i]] = i;
        }
    }
    public static int[] permutationInverse( int []original , int length ) {
        int inverse[] = new int[length];
        permutationInverse(original,inverse,length);
        return inverse;
    }

    /**
     * Applies the row permutation specified by the vector to the input matrix and save the results
     * in the output matrix.  output[perm[j],:] = input[j,:]
     *
     * @param permInv (Input) Inverse permutation vector.  Specifies new order of the rows.
     * @param input (Input) Matrix which is to be permuted
     * @param output (Output) Matrix which has the permutation stored in it.  Is reshaped.
     */
    public static void permuteRowInv(int permInv[], DMatrixSparseCSC input, DMatrixSparseCSC output) {
        if( input.numRows > permInv.length )
            throw new IllegalArgumentException("permutation vector must have at least as many elements as input has rows");

        output.reshape(input.numRows,input.numCols,input.nz_length);
        output.indicesSorted = false;

        System.arraycopy(input.nz_values,0,output.nz_values,0,input.nz_length);
        System.arraycopy(input.col_idx,0,output.col_idx,0,input.numCols+1);

        int idx0 = 0;
        for (int i = 0; i < input.numCols; i++) {
            int idx1 = output.col_idx[i+1];

            for (int j = idx0; j < idx1; j++) {
                output.nz_rows[j] = permInv[input.nz_rows[j]];
            }
            idx0 = idx1;
        }
    }

    /**
     * Applies the forward column and inverse row permutation specified by the two vector to the input matrix
     * and save the results in the output matrix. output[permRow[j],permCol[i]] = input[j,i]
     * @param permRowInv (Input) Inverse row permutation vector
     * @param input (Input) Matrix which is to be permuted
     * @param permCol (Input) Column permutation vector
     * @param output (Output) Matrix which has the permutation stored in it.  Is reshaped.
     */
    public static void permute(int permRowInv[], DMatrixSparseCSC input, int permCol[], DMatrixSparseCSC output) {
        if( permRowInv!= null && input.numRows != permRowInv.length )
            throw new IllegalArgumentException("Number of column in input must match length of rowInv");
        if( permCol != null && input.numCols != permCol.length )
            throw new IllegalArgumentException("Number of rows in input must match length of colInv");

        output.reshape(input.numRows,input.numCols,input.nz_length);
        output.indicesSorted = false;
        output.col_idx[0] = 0;

        int N = input.numCols;

        // traverse through in order for the output columns
        int outputNZ = 0;
        for (int i = 0; i < N; i++) {
            int inputCol = permCol!=null?permCol[i]:i; // column of input to source from
            int inputNZ = input.col_idx[inputCol];
            int total = input.col_idx[inputCol+1]- inputNZ; // total nz in this column

            output.col_idx[i+1] = output.col_idx[i] + total;

            for (int j = 0; j < total; j++) {
                int row = input.nz_rows[inputNZ];
                output.nz_rows[outputNZ] = permRowInv!=null?permRowInv[row]:row;
                output.nz_values[outputNZ++] = input.nz_values[inputNZ++];
            }
        }
    }

    /**
     * Permutes a vector.  output[i] = input[perm[i]]
     *
     * @param perm (Input) permutation vector
     * @param input (Input) Vector which is to be permuted
     * @param output (Output) Where the permuted vector is stored.
     * @param N Number of elements in the vector.
     */
    public static void permute( int[] perm , double []input , double[]output , int N ) {
        for (int k = 0; k < N; k++) {
            output[k] = input[perm[k]];
        }
    }

    /**
     * Permutes a vector in the inverse.  output[perm[k]] = input[k]
     *
     * @param perm (Input) permutation vector
     * @param input (Input) Vector which is to be permuted
     * @param output (Output) Where the permuted vector is stored.
     * @param N Number of elements in the vector.
     */
    public static void permuteInv( int[] perm , double []input , double[]output , int N ) {
        for (int k = 0; k < N; k++) {
            output[perm[k]] = input[k];
        }
    }


    /**
     * Applies the permutation to upper triangular symmetric matrices. Typically a symmetric matrix only stores the
     * upper triangular part, so normal permutation will have undesirable results, e.g. the zeros will get mixed
     * in and will no longer be symmetric. This algorithm will handle the implicit lower triangular and construct
     * new upper triangular matrix.
     *
     * <p>See page cs_symperm() on Page 22 of "Direct Methods for Sparse Linear Systems"</p>
     *
     * @param input (Input) Upper triangular symmetric matrix which is to be permuted.
     *              Entries below the diagonal are ignored.
     * @param permInv (Input) Inverse permutation vector.  Specifies new order of the rows and columns.
     * @param output (Output) Upper triangular symmetric matrix which has the permutation stored in it.  Reshaped.
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     */
    public static void permuteSymmetric( DMatrixSparseCSC input, int permInv[], DMatrixSparseCSC output , IGrowArray gw ) {
        if( input.numRows != input.numCols )
            throw new IllegalArgumentException("Input must be a square matrix");
        if( input.numRows != permInv.length )
            throw new IllegalArgumentException("Number of column in input must match length of permInv");
        if( input.numCols != permInv.length )
            throw new IllegalArgumentException("Number of rows in input must match length of permInv");

        int N = input.numCols;

        int w[] = adjustClear(gw,N); // histogram with column counts

        output.reshape(N,N,0);
        output.indicesSorted = false;
        output.col_idx[0] = 0;

        // determine column counts for output
        for (int j = 0; j < N; j++) {
            int j2 = permInv[j];
            int idx0 = input.col_idx[j];
            int idx1 = input.col_idx[j+1];

            for (int p = idx0; p < idx1; p++) {
                int i = input.nz_rows[p];
                if( i > j ) // ignore the lower triangular portion
                    continue;
                int i2 = permInv[i];

                w[i2>j2?i2:j2]++;
            }
        }

        // update structure of output
        output.colsum(w);

        for (int j = 0; j < N; j++) {
            // column j of Input is row j2 of Output
            int j2 = permInv[j];
            int idx0 = input.col_idx[j];
            int idx1 = input.col_idx[j+1];

            for (int p = idx0; p < idx1; p++) {
                int i = input.nz_rows[p];
                if( i > j ) // ignore the lower triangular portion
                    continue;

                int i2 = permInv[i];
                // row i of Input is row i2 of Output
                int q = w[i2>j2?i2:j2]++;
                output.nz_rows[q] = i2<j2?i2:j2;
                output.nz_values[q] = input.nz_values[p];
            }
        }
    }

    /**
     * Concats two matrices along their rows (vertical).
     *
     * @param top Matrix on the top
     * @param bottom Matrix on the bototm
     * @param out (Output) (Optional) Storage for combined matrix. Resized.
     * @return Combination of the two matrices
     */
    public static DMatrixSparseCSC concatRows(DMatrixSparseCSC top , DMatrixSparseCSC bottom ,
                                              DMatrixSparseCSC out )
    {
        if( top.numCols != bottom.numCols )
            throw new IllegalArgumentException("Number of columns must match");
        if( out == null )
            out = new DMatrixSparseCSC(0,0,0);

        out.reshape(top.numRows+bottom.numRows,top.numCols,top.nz_length+bottom.nz_length);

        int index = 0;
        for (int i = 0; i < top.numCols; i++) {
            int top0 = top.col_idx[i];
            int top1 = top.col_idx[i+1];

            int bot0 = bottom.col_idx[i];
            int bot1 = bottom.col_idx[i+1];

            int out0 = out.col_idx[i];
            int out1 = out0 + top1-top0 + bot1-bot0;
            out.col_idx[i+1] = out1;

            for (int j = top0; j < top1; j++ , index++) {
                out.nz_values[index] = top.nz_values[j];
                out.nz_rows[index] = top.nz_rows[j];
            }
            for (int j = bot0; j < bot1; j++ , index++) {
                out.nz_values[index] = bottom.nz_values[j];
                out.nz_rows[index] = top.numRows + bottom.nz_rows[j];
            }
        }

        return out;
    }

    /**
     * Concats two matrices along their columns (horizontal).
     *
     * @param left Matrix on  the left
     * @param right Matrix on the right
     * @param out (Output) (Optional) Storage for combined matrix. Resized.
     * @return Combination of the two matrices
     */
    public static DMatrixSparseCSC concatColumns(DMatrixSparseCSC left , DMatrixSparseCSC right ,
                                                 DMatrixSparseCSC out )
    {
        if( left.numRows != right.numRows )
            throw new IllegalArgumentException("Number of rows must match");
        if( out == null )
            out = new DMatrixSparseCSC(0,0,0);

        out.reshape(left.numRows,left.numCols+right.numCols,left.nz_length+right.nz_length);

        System.arraycopy(left.col_idx,0,out.col_idx,0,left.numCols+1);
        System.arraycopy(left.nz_rows,0,out.nz_rows,0,left.nz_length);
        System.arraycopy(left.nz_values,0,out.nz_values,0,left.nz_length);

        int index = left.nz_length;
        for (int i = 0; i < right.numCols; i++) {
            int r0 = right.col_idx[i];
            int r1 = right.col_idx[i+1];

            out.col_idx[left.numCols+i] = index;
            out.col_idx[left.numCols+i+1] = index + (r1 - r0);

            for (int j = r0; j < r1; j++, index++) {
                out.nz_rows[index] = right.nz_rows[j];
                out.nz_values[index] = right.nz_values[j];
            }
        }

        return out;
    }

    /**
     * Extracts a column from A and stores it into out.
     *
     * @param A (Input) Source matrix. not modified.
     * @param column The column in A
     * @param out (Output, Optional) Storage for column vector
     * @return The column of A.
     */
    public static DMatrixSparseCSC extractColumn(DMatrixSparseCSC A , int column , DMatrixSparseCSC out ) {

        if( out == null )
            out = new DMatrixSparseCSC(1,1,1);

        int idx0 = A.col_idx[column];
        int idx1 = A.col_idx[column+1];

        out.reshape(A.numRows,1,idx1-idx0);


        out.col_idx[0] = 0;
        out.col_idx[1] = out.nz_length;

        System.arraycopy(A.nz_values,idx0,out.nz_values,0,out.nz_length);
        System.arraycopy(A.nz_rows,idx0,out.nz_rows,0,out.nz_length);

        return out;
    }

    /**
     * Creates a submatrix by extracting the specified rows from A. rows = {row0 %le; i %le; row1}.
     * @param A (Input) matrix
     * @param row0 First row. Inclusive
     * @param row1 Last row+1.
     * @param out (Output, Option) Storage for output matrix
     * @return The submatrix
     */
    public static DMatrixSparseCSC extractRows(DMatrixSparseCSC A , int row0 , int row1 , DMatrixSparseCSC out ) {

        if( out == null )
            out = new DMatrixSparseCSC(1,1,1);

        out.reshape(row1-row0,A.numCols,A.nz_length);
        out.col_idx[0] = 0;
        out.nz_length = 0;

        for (int col = 0; col < A.numCols; col++) {
            int idx0 = A.col_idx[col];
            int idx1 = A.col_idx[col+1];

            for (int i = idx0; i < idx1; i++) {
                int row = A.nz_rows[i];
                if( row >= row0 && row < row1 ) {
                    out.nz_values[out.nz_length] = A.nz_values[i];
                    out.nz_rows[out.nz_length++] = row-row0;
                }
            }
            out.col_idx[col+1] = out.nz_length;
        }

        return out;
    }

    /**
     * <p>
     * Extracts a submatrix from 'src' and inserts it in a submatrix in 'dst'.
     * </p>
     * <p>
     * s<sub>i-y0 , j-x0</sub> = o<sub>ij</sub> for all y0 &le; i &lt; y1 and x0 &le; j &lt; x1 <br>
     * <br>
     * where 's<sub>ij</sub>' is an element in the submatrix and 'o<sub>ij</sub>' is an element in the
     * original matrix.
     * </p>
     *
     * <p>WARNING: This is a very slow operation for sparse matrices. The current implementation is simple but
     * involves excessive memory copying.</p>
     *
     * @param src The original matrix which is to be copied.  Not modified.
     * @param srcX0 Start column.
     * @param srcX1 Stop column+1.
     * @param srcY0 Start row.
     * @param srcY1 Stop row+1.
     * @param dst Where the submatrix are stored.  Modified.
     * @param dstY0 Start row in dst.
     * @param dstX0 start column in dst.
     */
    public static void extract(DMatrixSparseCSC src, int srcY0, int srcY1, int srcX0, int srcX1,
                               DMatrixSparseCSC dst, int dstY0, int dstX0)
    {
        if( srcY1 < srcY0 || srcY0 < 0 || srcY1 > src.getNumRows() )
            throw new IllegalArgumentException("srcY1 < srcY0 || srcY0 < 0 || srcY1 > src.numRows");
        if( srcX1 < srcX0 || srcX0 < 0 || srcX1 > src.getNumCols() )
            throw new IllegalArgumentException("srcX1 < srcX0 || srcX0 < 0 || srcX1 > src.numCols");

        int w = srcX1-srcX0;
        int h = srcY1-srcY0;

        if( dstY0+h > dst.getNumRows() )
            throw new IllegalArgumentException("dst is too small in rows");
        if( dstX0+w > dst.getNumCols() )
            throw new IllegalArgumentException("dst is too small in columns");

        // NOTE: One possible optimization would be to determine the non-zero pattern in dst after the change is
        //       applied, modify it's structure, then copy the values in. That way you aren't shifting memory constantly.
        //
        // NOTE: Another optimization would be to sort the src so that it doesn't need to go through every row
        for (int colSrc = srcX0; colSrc < srcX1; colSrc++) {
            int idxS0 = src.col_idx[colSrc];
            int idxS1 = src.col_idx[colSrc+1];

            for (int i = idxS0; i < idxS1; i++) {
                int row = src.nz_rows[i];
                if( row >= srcY0 && row < srcY1 ) {
                    dst.set(row-srcY0+dstY0,colSrc-srcX0+dstX0, src.nz_values[i]);
                }
            }
        }
    }

    /**
     * Computes the inner product of two column vectors taken from the input matrices.
     *
     * <p>dot = A(:,colA)'*B(:,colB)</p>
     *
     * @param A Matrix
     * @param colA Column in A
     * @param B Matrix
     * @param colB Column in B
     * @return Dot product
     */
    public static double dotInnerColumns( DMatrixSparseCSC A , int colA , DMatrixSparseCSC B , int colB,
                                          IGrowArray gw , DGrowArray gx)
    {
        return ImplSparseSparseMult_DSCC.dotInnerColumns(A,colA,B,colB,gw,gx);
    }

    /**
     * <p>
     * Solves for x in the following equation:<br>
     * <br>
     * A*x = b
     * </p>
     *
     * <p>
     * If the system could not be solved then false is returned.  If it returns true
     * that just means the algorithm finished operating, but the results could still be bad
     * because 'A' is singular or nearly singular.
     * </p>
     *
     * <p>
     * If repeat calls to solve are being made then one should consider using {@link LinearSolverFactory_DSCC}
     * instead.
     * </p>
     *
     * <p>
     * It is ok for 'b' and 'x' to be the same matrix.
     * </p>
     *
     * @param a (Input) A matrix that is m by n. Not modified.
     * @param b (Input) A matrix that is n by k. Not modified.
     * @param x (Output) A matrix that is m by k. Modified.
     *
     * @return true if it could invert the matrix false if it could not.
     */
    public static boolean solve(DMatrixSparseCSC a ,
                                DMatrixRMaj b ,
                                DMatrixRMaj x )
    {
        LinearSolverSparse<DMatrixSparseCSC,DMatrixRMaj> solver;
        if( a.numRows > a.numCols ) {
            solver = LinearSolverFactory_DSCC.qr(FillReducing.NONE);// todo specify a filling that makes sense
        } else {
            solver = LinearSolverFactory_DSCC.lu(FillReducing.NONE);
        }

        // Ensure that the input isn't modified
        if( solver.modifiesA() )
            a = a.copy();

        if( solver.modifiesB() )
            b = b.copy();

        // decompose then solve the matrix
        if( !solver.setA(a) )
            return false;

        solver.solve(b, x);
        return true;
    }

    /**
     * <p>
     * Performs a matrix inversion operation that does not modify the original
     * and stores the results in another matrix.  The two matrices must have the
     * same dimension.<br>
     * <br>
     * B = A<sup>-1</sup>
     * </p>
     *
     * <p>
     * If the algorithm could not invert the matrix then false is returned.  If it returns true
     * that just means the algorithm finished.  The results could still be bad
     * because the matrix is singular or nearly singular.
     * </p>
     *
     * <p>
     * For medium to large matrices there might be a slight performance boost to using
     * {@link LinearSolverFactory_DSCC} instead.
     * </p>
     *
     * @param A (Input) The matrix that is to be inverted. Not modified.
     * @param inverse (Output) Where the inverse matrix is stored.  Modified.
     * @return true if it could invert the matrix false if it could not.
     */
    public static boolean invert(DMatrixSparseCSC A, DMatrixRMaj inverse ) {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("A must be a square matrix");
        if( A.numRows != inverse.numRows || A.numCols != inverse.numCols )
            throw new IllegalArgumentException("A and inverse must have the same shape.");

        LinearSolverSparse<DMatrixSparseCSC,DMatrixRMaj> solver;
        solver = LinearSolverFactory_DSCC.lu(FillReducing.NONE);

        // Ensure that the input isn't modified
        if( solver.modifiesA() )
            A = A.copy();

        DMatrixRMaj I = CommonOps_DDRM.identity(A.numRows);

        // decompose then solve the matrix
        if( !solver.setA(A) )
            return false;

        solver.solve(I, inverse);
        return true;
    }

    /**
     * Returns the determinant of the matrix.  If the inverse of the matrix is also
     * needed, then using {@link org.ejml.interfaces.decomposition.LUDecomposition_F64} directly (or any
     * similar algorithm) can be more efficient.
     *
     * @param A The matrix whose determinant is to be computed.  Not modified.
     * @return The determinant.
     */
    public static double det( DMatrixSparseCSC A ) {
        LUSparseDecomposition_F64<DMatrixSparseCSC> alg = DecompositionFactory_DSCC.lu(FillReducing.NONE);

        if( alg.inputModified() ) {
            A = A.copy();
        }

        if( !alg.decompose(A) )
            return 0.0;
        return alg.computeDeterminant().real;
    }

    /**
     * Copies all elements from input into output which are &gt; tol.
     * @param input (Input) input matrix. Not modified.
     * @param output (Output) Output matrix. Modified and shaped to match input.
     * @param tol Tolerance for defining zero
     */
    public static void removeZeros( DMatrixSparseCSC input , DMatrixSparseCSC  output , double tol ) {
        ImplCommonOps_DSCC.removeZeros(input,output,tol);
    }

    /**
     * Removes all elements from the matrix that are &gt; tol. The modification is done in place and no temporary
     * storage is declared.
     *
     * @param A (Input/Output) input matrix. Modified.
     * @param tol Tolerance for defining zero
     */
    public static void removeZeros( DMatrixSparseCSC A , double tol ) {
        ImplCommonOps_DSCC.removeZeros(A, tol);
    }
}

