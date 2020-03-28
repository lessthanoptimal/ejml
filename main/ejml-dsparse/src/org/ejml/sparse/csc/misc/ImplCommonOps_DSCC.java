/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.sparse.csc.misc;

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.sparse.csc.CommonOps_DSCC;

import javax.annotation.Nullable;
import java.util.Arrays;

import static org.ejml.UtilEjml.adjust;
import static org.ejml.sparse.csc.mult.ImplSparseSparseMult_DSCC.multAddColA;

/**
 * Implementation class.  Not recommended for direct use.  Instead use {@link CommonOps_DSCC}
 * instead.
 *
 * @author Peter Abeles
 */
public class ImplCommonOps_DSCC {

    /**
     * Performs a matrix transpose.
     *
     * @param A Original matrix.  Not modified.
     * @param C Storage for transposed 'a'.  Reshaped.
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     */
    public static void transpose(DMatrixSparseCSC A , DMatrixSparseCSC C , @Nullable IGrowArray gw ) {
        int []work = adjust(gw,A.numRows,A.numRows);
        C.reshape(A.numCols,A.numRows,A.nz_length);

        // compute the histogram for each row in 'a'
        int idx0 = A.col_idx[0];
        for (int j = 1; j <= A.numCols; j++) {
            int idx1 = A.col_idx[j];
            for (int i = idx0; i < idx1; i++) {
                if( A.nz_rows.length <= i)
                    throw new RuntimeException("Egads");
                work[A.nz_rows[i]]++;
            }
            idx0 = idx1;
        }

        // construct col_idx in the transposed matrix
        C.histogramToStructure(work);
        System.arraycopy(C.col_idx,0,work,0,C.numCols);

        // fill in the row indexes
        idx0 = A.col_idx[0];
        for (int j = 1; j <= A.numCols; j++) {
            int col = j-1;
            int idx1 = A.col_idx[j];
            for (int i = idx0; i < idx1; i++) {
                int row = A.nz_rows[i];
                int index = work[row]++;
                C.nz_rows[index] = col;
                C.nz_values[index] = A.nz_values[i];
            }
            idx0 = idx1;
        }
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
                           @Nullable IGrowArray gw, @Nullable DGrowArray gx)
    {
        double []x = adjust(gx,A.numRows);
        int []w = adjust(gw,A.numRows,A.numRows);

        C.indicesSorted = false;
        C.nz_length = 0;

        for (int col = 0; col < A.numCols; col++) {
            C.col_idx[col] = C.nz_length;

            multAddColA(A,col,alpha,C,col+1,x,w);
            multAddColA(B,col,beta,C,col+1,x,w);

            // take the values in the dense vector 'x' and put them into 'C'
            int idxC0 = C.col_idx[col];
            int idxC1 = C.col_idx[col+1];

            for (int i = idxC0; i < idxC1; i++) {
                C.nz_values[i] = x[C.nz_rows[i]];
            }
        }
        C.col_idx[A.numCols] = C.nz_length;
    }

    /**
     * Adds the results of adding a column in A and B as a new column in C.<br>
     * C(:,end+1) = &alpha;*A(:,colA) + &beta;*B(:,colB)
     *
     * @param alpha scalar
     * @param A matrix
     * @param colA column in A
     * @param beta scalar
     * @param B matrix
     * @param colB column in B
     * @param C Column in C
     * @param gw workspace
     */
    public static void addColAppend(double alpha, DMatrixSparseCSC A, int colA, double beta, DMatrixSparseCSC B, int colB,
                                    DMatrixSparseCSC C, @Nullable IGrowArray gw)
    {
        if( A.numRows != B.numRows || A.numRows != C.numRows)
            throw new IllegalArgumentException("Number of rows in A, B, and C do not match");

        int idxA0 = A.col_idx[colA];
        int idxA1 = A.col_idx[colA+1];
        int idxB0 = B.col_idx[colB];
        int idxB1 = B.col_idx[colB+1];

        C.growMaxColumns(++C.numCols,true);
        C.growMaxLength(C.nz_length+idxA1-idxA0+idxB1-idxB0,true);

        int []w = adjust(gw,A.numRows);
        Arrays.fill(w,0,A.numRows,-1);

        for (int i = idxA0; i < idxA1; i++) {
            int row = A.nz_rows[i];
            C.nz_rows[C.nz_length] = row;
            C.nz_values[C.nz_length] = alpha*A.nz_values[i];
            w[row] = C.nz_length++;
        }

        for (int i = idxB0; i < idxB1; i++) {
            int row = B.nz_rows[i];
            if( w[row] != -1 ) {
                C.nz_values[w[row]] += beta*B.nz_values[i];
            } else {
                C.nz_values[C.nz_length] = beta*B.nz_values[i];
                C.nz_rows[C.nz_length++] = row;
            }
        }
        C.col_idx[C.numCols] = C.nz_length;
    }

    /**
     * Performs element-wise multiplication:<br>
     * C_ij = A_ij * B_ij
     *
     * @param A (Input) Matrix
     * @param B (Input) Matrix
     * @param C (Output) Matrix.
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     * @param gx (Optional) Storage for internal workspace.  Can be null.
     */
    public static void elementMult( DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC C,
                                    @Nullable IGrowArray gw, @Nullable DGrowArray gx)
    {
        double []x = adjust(gx,A.numRows);
        int []w = adjust(gw,A.numRows);
        Arrays.fill(w,0,A.numRows,-1); // fill with -1. This will be a value less than column

        C.growMaxLength(Math.min(A.nz_length,B.nz_length),false);
        C.indicesSorted = false; // Hmm I think if B is storted then C will be sorted...
        C.nz_length = 0;

        for (int col = 0; col < A.numCols; col++) {
            int idxA0 = A.col_idx[col];
            int idxA1 = A.col_idx[col+1];
            int idxB0 = B.col_idx[col];
            int idxB1 = B.col_idx[col+1];

            // compute the maximum number of elements that there can be in this row
            int maxInRow = Math.min(idxA1-idxA0,idxB1-idxB0);

            // make sure there are enough non-zero elements in C
            if( C.nz_length+maxInRow > C.nz_values.length )
                C.growMaxLength(C.nz_values.length+maxInRow,true);

            // update the structure of C
            C.col_idx[col] = C.nz_length;

            // mark the rows that appear in A and save their value
            for (int i = idxA0; i < idxA1; i++) {
                int row = A.nz_rows[i];
                w[row] = col;
                x[row] = A.nz_values[i];
            }

            // If a row appears in A and B, multiply and set as an element in C
            for (int i = idxB0; i < idxB1; i++) {
                int row = B.nz_rows[i];
                if( w[row] == col ) {
                    C.nz_values[C.nz_length] = x[row]*B.nz_values[i];
                    C.nz_rows[C.nz_length++] = row;
                }
            }
        }
        C.col_idx[C.numCols] = C.nz_length;
    }

    public static void removeZeros( DMatrixSparseCSC input , DMatrixSparseCSC  output , double tol ) {
        output.reshape(input.numRows, input.numCols, input.nz_length);
        output.nz_length = 0;

        for (int i = 0; i < input.numCols; i++) {
            output.col_idx[i] = output.nz_length;

            int idx0 = input.col_idx[i];
            int idx1 = input.col_idx[i+1];

            for (int j = idx0; j < idx1; j++) {
                double val = input.nz_values[j];
                if( Math.abs(val) > tol ) {
                    output.nz_rows[output.nz_length] = input.nz_rows[j];
                    output.nz_values[output.nz_length++] = val;
                }
            }
        }
        output.col_idx[output.numCols] = output.nz_length;
    }

    public static void removeZeros( DMatrixSparseCSC A , double tol ) {

        int offset = 0;
        for (int i = 0; i < A.numCols; i++) {
            int idx0 = A.col_idx[i]+offset;
            int idx1 = A.col_idx[i+1];

            for (int j = idx0; j < idx1; j++) {
                double val = A.nz_values[j];
                if( Math.abs(val) > tol ) {
                    A.nz_rows[j-offset] = A.nz_rows[j];
                    A.nz_values[j-offset] = val;
                } else {
                    offset++;
                }
            }
            A.col_idx[i+1] -= offset;
        }
        A.nz_length -= offset;
    }

    /**
     * Given a symmetric matrix which is represented by a lower triangular matrix convert it back into
     * a full symmetric matrix
     *
     * @param A (Input) Lower triangular matrix
     * @param B (Output) Symmetric matrix.
     * @param gw (Optional) Workspace. Can be null.
     */
    public static void symmLowerToFull( DMatrixSparseCSC A , DMatrixSparseCSC B , @Nullable IGrowArray gw )
    {
        if( A.numCols != A.numRows )
            throw new IllegalArgumentException("Must be a lower triangular square matrix");

        int N = A.numCols;
        int w[] = adjust(gw,N,N);
        B.reshape(N,N,A.nz_length*2);
        B.indicesSorted = false;

        //=== determine the row counts of the full matrix
        for (int col = 0; col < N; col++) {
            int idx0 = A.col_idx[col];
            int idx1 = A.col_idx[col+1];

            // We know the length of the lower part of this column already
            w[col] += idx1 - idx0;

            // add elements to the top of the other columns along row with index 'col'
            for (int i = idx0; i < idx1; i++) {
                int row = A.nz_rows[i];
                if( row > col ) {
                    w[row]++;
                }
            }
        }

        // Update the structure of B
        B.histogramToStructure(w);

        // Zero W again. It's being used to keep track of how many elements have been added to a column already
        Arrays.fill(w,0,N,0);
        // Fill in matrix
        for (int col = 0; col < N; col++) {

            int idx0 = A.col_idx[col];
            int idx1 = A.col_idx[col+1];

            int lengthA = idx1 - idx0;
            int lengthB = B.col_idx[col+1] - B.col_idx[col];

            // Copy the non-zero values from A into B along the columns while taking in account the upper
            // elements already copied
            System.arraycopy(A.nz_values,idx0,B.nz_values,B.col_idx[col]+lengthB-lengthA,lengthA);
            System.arraycopy(A.nz_rows,idx0,B.nz_rows,B.col_idx[col]+lengthB-lengthA,lengthA);

            // Copy this column into the upper portion of B
            for (int i = idx0; i < idx1; i++) {
                int row = A.nz_rows[i];
                if( row > col ) {
                    int indexB = B.col_idx[row] + w[row]++;
                    B.nz_rows[indexB] = col;
                    B.nz_values[indexB] = A.nz_values[i];
                }
            }
        }

    }
}
