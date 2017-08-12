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

package org.ejml.dense.row.decomposition.lu;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.IGrowArray;
import org.ejml.dense.row.SpecializedOps_DDRM;
import org.ejml.dense.row.decomposition.TriangularSolver_DDRM;
import org.ejml.dense.row.decomposition.UtilDecompositons_DDRM;
import org.ejml.interfaces.decomposition.LUDecomposition_F64;


/**
 * <p>
 * Contains common data structures and operations for LU decomposition algorithms.
 * </p>
 * @author Peter Abeles
 */
public abstract class LUDecompositionBase_DDRM
        implements LUDecomposition_F64<DMatrixRMaj> {
    // the decomposed matrix
    protected DMatrixRMaj LU;

    // it can decompose a matrix up to this size
    protected int maxWidth=-1;

    // the shape of the matrix
    protected int m,n;
    // data in the matrix
    protected double dataLU[];

    // used in set, solve, invert
    protected double vv[];
    // used in set
    protected int indx[];
    protected int pivot[];

    // used by determinant
    protected double pivsign;

    Complex_F64 det = new Complex_F64();

    public void setExpectedMaxSize( int numRows , int numCols )
    {
        LU = new DMatrixRMaj(numRows,numCols);

        this.dataLU = LU.data;
        maxWidth = Math.max(numRows,numCols);

        vv = new double[ maxWidth ];
        indx = new int[ maxWidth ];
        pivot = new int[ maxWidth ];
    }

    public DMatrixRMaj getLU() {
        return LU;
    }

    public int[] getIndx() {
        return indx;
    }

    public int[] getPivot() {
        return pivot;
    }

    @Override
    public boolean inputModified() {
        return false;
    }

    /**
     * Writes the lower triangular matrix into the specified matrix.
     *
     * @param lower Where the lower triangular matrix is written to.
     */
    @Override
    public DMatrixRMaj getLower(DMatrixRMaj lower )
    {
        int numRows = LU.numRows;
        int numCols = LU.numRows < LU.numCols ? LU.numRows : LU.numCols;

        lower = UtilDecompositons_DDRM.checkZerosUT(lower,numRows,numCols);

        for( int i = 0; i < numCols; i++ ) {
            lower.unsafe_set(i,i,1.0);

            for( int j = 0; j < i; j++ ) {
                lower.unsafe_set(i,j, LU.unsafe_get(i,j));
            }
        }

        if( numRows > numCols ) {
            for( int i = numCols; i < numRows; i++ ) {
                for( int j = 0; j < numCols; j++ ) {
                    lower.unsafe_set(i,j, LU.unsafe_get(i,j));
                }
            }
        }
        return lower;
    }

    /**
     * Writes the upper triangular matrix into the specified matrix.
     *
     * @param upper Where the upper triangular matrix is writen to.
     */
    @Override
    public DMatrixRMaj getUpper(DMatrixRMaj upper )
    {
        int numRows = LU.numRows < LU.numCols ? LU.numRows : LU.numCols;
        int numCols = LU.numCols;

        upper = UtilDecompositons_DDRM.checkZerosLT(upper,numRows,numCols);

        for( int i = 0; i < numRows; i++ ) {
            for( int j = i; j < numCols; j++ ) {
                upper.unsafe_set(i,j, LU.unsafe_get(i,j));
            }
        }

        return upper;
    }

    @Override
    public DMatrixRMaj getRowPivot(DMatrixRMaj pivot ) {
        return SpecializedOps_DDRM.pivotMatrix(pivot, this.pivot, LU.numRows, false);
    }

    @Override
    public int[] getRowPivotV(IGrowArray pivot) {
        return UtilEjml.pivotVector(this.pivot,LU.numRows,pivot);
    }

    protected void decomposeCommonInit(DMatrixRMaj a) {
        if( a.numRows > maxWidth || a.numCols > maxWidth ) {
            setExpectedMaxSize(a.numRows,a.numCols);
        }

        m = a.numRows;
        n = a.numCols;

        LU.set(a);
        for (int i = 0; i < m; i++) {
            pivot[i] = i;
        }
        pivsign = 1;
    }

    /**
     * Determines if the decomposed matrix is singular.  This function can return
     * false and the matrix be almost singular, which is still bad.
     *
     * @return true if singular false otherwise.
     */
    @Override
    public boolean isSingular() {
        for( int i = 0; i < m; i++ ) {
            if( Math.abs(dataLU[i* n +i]) < UtilEjml.EPS )
                return true;
        }
        return false;
    }

    /**
     * Computes the determinant from the LU decomposition.
     *
     * @return The matrix's determinant.
     */
    @Override
    public Complex_F64 computeDeterminant() {
        if( m != n )
            throw new IllegalArgumentException("Must be a square matrix.");

        double ret = pivsign;

        int total = m*n;
        for( int i = 0; i < total; i += n + 1 ) {
            ret *= dataLU[i];
        }

        det.real = ret;
        det.imaginary = 0;

        return det;
    }

    public /**/double quality() {
        return SpecializedOps_DDRM.qualityTriangular(LU);
    }

    /**
     * a specialized version of solve that avoid additional checks that are not needed.
     */
    public void _solveVectorInternal( double []vv )
    {
        // Solve L*Y = B
        int ii = 0;

        for( int i = 0; i < n; i++ ) {
            int ip = indx[i];
            double sum = vv[ip];
            vv[ip] = vv[i];
            if( ii != 0 ) {
//                for( int j = ii-1; j < i; j++ )
//                    sum -= dataLU[i* n +j]*vv[j];
                int index = i*n + ii-1;
                for( int j = ii-1; j < i; j++ )
                    sum -= dataLU[index++]*vv[j];
            } else if( sum != 0.0 ) {
                ii=i+1;
            }
            vv[i] = sum;
        }

        // Solve U*X = Y;
        TriangularSolver_DDRM.solveU(dataLU,vv,n);
    }

    public double[] _getVV() {
        return vv;
    }
}