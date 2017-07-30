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

package org.ejml.sparse.csc.decomposition.lu;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.interfaces.decomposition.LUDecomposition_F64;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.misc.TriangularSolver_DSCC;

/**
 * LU Decomposition using a left looking algorithm for {@link DMatrixSparseCSC}.
 *
 * <p>NOTE: Based mostly on the algorithm described on page 86 in csparse. cs_lu</p>
 *
 * @author Peter Abeles
 */
public class LuUpLooking_DSCC
    implements LUDecomposition_F64<DMatrixSparseCSC>
{
    // algorithm which computes the fill reduction permutation
    private ComputePermutation<DMatrixSparseCSC> reduceFill;
    private IGrowArray gq = new IGrowArray(); // storage for reduce fill's permutation
    private IGrowArray gqinv = new IGrowArray();
    private DMatrixSparseCSC Ap = new DMatrixSparseCSC(1,1,0); // permuated A matrix

    // storage for LU decomposition
    private DMatrixSparseCSC L = new DMatrixSparseCSC(0,0,0);
    private DMatrixSparseCSC U = new DMatrixSparseCSC(0,0,0);

    // row pivot matrix, for numerical stability
    private int pinv[] = new int[0];

    // tolerance deciding if a number is zero
    private double tol = UtilEjml.EPS;

    // work space variables
    private double x[] = new double[0];
    private IGrowArray gxi = new IGrowArray(); // storage for non-zero pattern
    private IGrowArray gw = new IGrowArray();

    // true if a singular matrix is detected
    private boolean singular;

    public LuUpLooking_DSCC(ComputePermutation<DMatrixSparseCSC> reduceFill) {
        this.reduceFill = reduceFill;
    }

    @Override
    public boolean decompose(DMatrixSparseCSC A) {
        initialize(A);

        // Apply optional fill reduction permutation
        DMatrixSparseCSC C;
        if( reduceFill != null ) {
            reduceFill.process(A,gq);
            gqinv.reshape(gq.length);
            CommonOps_DSCC.permutationInverse(gq.data, gqinv.data, gq.length);
            CommonOps_DSCC.permuteRowInv(gqinv.data, A,Ap);
            C = Ap;
        } else {
            C = A;
        }

        return performLU(C);
    }

    private void initialize(DMatrixSparseCSC A) {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("Expected square matrix");

        int n = A.numRows;
        // number of non-zero elements can only be easily estimated because of pivots
        L.reshape(n,n,4*A.nz_length+n);
        L.nz_length = 0;
        U.reshape(n,n,4*A.nz_length+n);
        U.nz_length = 0;

        singular = false;
        if( pinv.length != n ) {
            pinv = new int[n];
            x = new double[n];
        }

        for (int i = 0; i < n; i++) {
            pinv[i] = -1;
            L.col_idx[i] = 0;
        }
    }

    private boolean performLU(DMatrixSparseCSC A ) {
        int n = A.numRows;
        int q[] = reduceFill != null ? gq.data : null;

        // main loop for computing L and U
        for (int k = 0; k < n; k++) {
            //--------- Triangular Solve
            L.col_idx[k] = L.nz_length;  // start of column k
            U.col_idx[k] = U.nz_length;

            // grow storage in L and U if needed
            if( L.nz_length+n > L.nz_values.length )
                L.growMaxLength(2*L.nz_values.length+n, true);
            if( U.nz_length+n > U.nz_values.length )
                U.growMaxLength(2*U.nz_values.length+n, true);

            int col = reduceFill != null ? q[k] : k;
            int top = TriangularSolver_DSCC.solve(L,true,A,col,x,pinv,gxi,gw);
            int []xi = gxi.data;

            //--------- Find the Next Pivot. That will be the column with the largest value
            int ipiv = -1;
            double a = -Double.MAX_VALUE;
            for (int p = top; p < n; p++) {
                int i = xi[p];                  // x(i) is nonzero
                if( pinv[i]< 0 ) {
                    double t;
                    if( (t = Math.abs(x[i])) > a ) {
                        a = t;
                        ipiv = i;
                    }
                } else {
                    U.nz_rows[U.nz_length] = pinv[i];
                    U.nz_values[U.nz_length++] = x[i];
                }
            }
            if( ipiv == -1 || a <= 0 ) {
                singular = true;
                return false;
            }
            if( pinv[col] < 0 && Math.abs(x[col]) >= a*tol ) {
                ipiv = col;
            }

            //---------- Divide by the pivot
            double pivot = x[ipiv];
            U.nz_rows[U.nz_length] = k;
            U.nz_values[U.nz_length++] = pivot;      // last entry in U(:k) us U(k,k)
            pinv[ipiv] = k;                          // ipiv is the kth pivot row
            L.nz_rows[L.nz_length] = ipiv;           // First entry L(:,k) is L(k,k) = 1
            L.nz_values[L.nz_length++] = 1;

            for (int p = top; p < n; p++) {
                int i = xi[p];
                if( pinv[i] < 0 ) {                  // x(i) is entry in L(:,k)
                    L.nz_rows[L.nz_length] = i;
                    L.nz_values[L.nz_length++] = x[i]/pivot;
                }
                x[i] = 0;
            }
        }
        //----------- Finalize L and U
        L.col_idx[n] = L.nz_length;
        U.col_idx[n] = U.nz_length;
        for (int p = 0; p < L.nz_length; p++) {
            L.nz_rows[p] = pinv[ L.nz_rows[p]];
        }

//        System.out.println("  reduce "+(reduceFill!=null));
//        System.out.print("  pinv[ ");
//        for (int i = 0; i < A.numCols; i++) {
//            System.out.printf("%2d ",pinv[i]);
//        }
//        System.out.println(" ]");

        return true;
    }

    @Override
    public Complex_F64 computeDeterminant() {
        double value = 1;
        for (int i = 0; i < U.numCols; i++) {
            value *= U.nz_values[U.col_idx[i+1]-1];
        }
        return new Complex_F64(value,0);
    }

    @Override
    public DMatrixSparseCSC getLower(DMatrixSparseCSC lower) {
        if( lower == null )
            lower = new DMatrixSparseCSC(1,1,0);
        lower.set(L);
        return lower;
    }

    @Override
    public DMatrixSparseCSC getUpper(DMatrixSparseCSC upper) {
        if( upper == null )
            upper = new DMatrixSparseCSC(1,1,0);
        upper.set(U);
        return upper;
    }

    @Override
    public DMatrixSparseCSC getPivot(DMatrixSparseCSC pivot) { // todo rename to pivot column?
        if( pivot == null )
            pivot = new DMatrixSparseCSC(L.numCols,L.numCols,0);
        pivot.reshape(L.numCols,L.numCols,L.numCols);
        CommonOps_DSCC.permutationMatrix(pinv, false, L.numCols,pivot);
        return pivot;
    }

    @Override
    public boolean isSingular() {
        return singular;
    }

    @Override
    public boolean inputModified() {
        return false;
    }

    public double getTol() {
        return tol;
    }

    public void setTol(double tol) {
        this.tol = tol;
    }

    public int[] getPinv() {
        return pinv;
    }

    public DMatrixSparseCSC getL() {
        return L;
    }

    public DMatrixSparseCSC getU() {
        return U;
    }

    public ComputePermutation<DMatrixSparseCSC> getReduceFill() {
        return reduceFill;
    }

    public int[] getReducePermutation() {
        return gq.data;
    }
}
