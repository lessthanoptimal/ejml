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
import org.ejml.data.Complex_F32;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.IGrowArray;
import org.ejml.dense.row.SpecializedOps_FDRM;
import org.ejml.dense.row.decomposition.TriangularSolver_FDRM;
import org.ejml.dense.row.decomposition.UtilDecompositons_FDRM;
import org.ejml.interfaces.decomposition.LUDecomposition_F32;


/**
 * <p>
 * Contains common data structures and operations for LU decomposition algorithms.
 * </p>
 * @author Peter Abeles
 */
public abstract class LUDecompositionBase_FDRM
        implements LUDecomposition_F32<FMatrixRMaj> {
    // the decomposed matrix
    protected FMatrixRMaj LU;

    // it can decompose a matrix up to this size
    protected int maxWidth=-1;

    // the shape of the matrix
    protected int m,n;
    // data in the matrix
    protected float dataLU[];

    // used in set, solve, invert
    protected float vv[];
    // used in set
    protected int indx[];
    protected int pivot[];

    // used by determinant
    protected float pivsign;

    Complex_F32 det = new Complex_F32();

    public void setExpectedMaxSize( int numRows , int numCols )
    {
        LU = new FMatrixRMaj(numRows,numCols);

        this.dataLU = LU.data;
        maxWidth = Math.max(numRows,numCols);

        vv = new float[ maxWidth ];
        indx = new int[ maxWidth ];
        pivot = new int[ maxWidth ];
    }

    public FMatrixRMaj getLU() {
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
    public FMatrixRMaj getLower(FMatrixRMaj lower )
    {
        int numRows = LU.numRows;
        int numCols = LU.numRows < LU.numCols ? LU.numRows : LU.numCols;

        lower = UtilDecompositons_FDRM.checkZerosUT(lower,numRows,numCols);

        for( int i = 0; i < numCols; i++ ) {
            lower.unsafe_set(i,i,1.0f);

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
    public FMatrixRMaj getUpper(FMatrixRMaj upper )
    {
        int numRows = LU.numRows < LU.numCols ? LU.numRows : LU.numCols;
        int numCols = LU.numCols;

        upper = UtilDecompositons_FDRM.checkZerosLT(upper,numRows,numCols);

        for( int i = 0; i < numRows; i++ ) {
            for( int j = i; j < numCols; j++ ) {
                upper.unsafe_set(i,j, LU.unsafe_get(i,j));
            }
        }

        return upper;
    }

    @Override
    public FMatrixRMaj getRowPivot(FMatrixRMaj pivot ) {
        return SpecializedOps_FDRM.pivotMatrix(pivot, this.pivot, LU.numRows, false);
    }

    @Override
    public int[] getRowPivotV(IGrowArray pivot) {
        return UtilEjml.pivotVector(this.pivot,LU.numRows,pivot);
    }

    protected void decomposeCommonInit(FMatrixRMaj a) {
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
            if( Math.abs(dataLU[i* n +i]) < UtilEjml.F_EPS )
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
    public Complex_F32 computeDeterminant() {
        if( m != n )
            throw new IllegalArgumentException("Must be a square matrix.");

        float ret = pivsign;

        int total = m*n;
        for( int i = 0; i < total; i += n + 1 ) {
            ret *= dataLU[i];
        }

        det.real = ret;
        det.imaginary = 0;

        return det;
    }

    public /**/double quality() {
        return SpecializedOps_FDRM.qualityTriangular(LU);
    }

    /**
     * a specialized version of solve that avoid additional checks that are not needed.
     */
    public void _solveVectorInternal( float []vv )
    {
        // Solve L*Y = B
        int ii = 0;

        for( int i = 0; i < n; i++ ) {
            int ip = indx[i];
            float sum = vv[ip];
            vv[ip] = vv[i];
            if( ii != 0 ) {
//                for( int j = ii-1; j < i; j++ )
//                    sum -= dataLU[i* n +j]*vv[j];
                int index = i*n + ii-1;
                for( int j = ii-1; j < i; j++ )
                    sum -= dataLU[index++]*vv[j];
            } else if( sum != 0.0f ) {
                ii=i+1;
            }
            vv[i] = sum;
        }

        // Solve U*X = Y;
        TriangularSolver_FDRM.solveU(dataLU,vv,n);
    }

    public float[] _getVV() {
        return vv;
    }
}