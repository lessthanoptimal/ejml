/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decompose.lu;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decompose.CTriangularSolver;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.interfaces.decomposition.LUDecomposition;
import org.ejml.ops.CCommonOps;
import org.ejml.ops.CSpecializedOps;


/**
 * <p>
 * Contains common data structures and operations for LU decomposition algorithms.
 * </p>
 * @author Peter Abeles
 */
public abstract class LUDecompositionBase_CD64
        implements LUDecomposition<CDenseMatrix64F> {
    // the decomposed matrix
    protected CDenseMatrix64F LU;

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

    public void setExpectedMaxSize( int numRows , int numCols )
    {
        LU = new CDenseMatrix64F(numRows,numCols);

        this.dataLU = LU.data;
        maxWidth = Math.max(numRows,numCols);

        vv = new double[ maxWidth ];
        indx = new int[ maxWidth ];
        pivot = new int[ maxWidth ];
    }

    public CDenseMatrix64F getLU() {
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
    public CDenseMatrix64F getLower( CDenseMatrix64F lower )
    {
        int numRows = LU.numRows;
        int numCols = LU.numRows < LU.numCols ? LU.numRows : LU.numCols;

        if( lower == null ) {
            lower = new CDenseMatrix64F(numRows,numCols);
        } else {
            if( lower.numCols != numCols || lower.numRows != numRows )
                throw new IllegalArgumentException("Unexpected matrix dimension");
            CCommonOps.fill(lower,0, 0);
        }

        for( int i = 0; i < numCols; i++ ) {
            lower.set(i,i,1.0,0.0);

            for( int j = 0; j < i; j++ ) {
                int indexLU = LU.getIndex(i,j);
                int indexL = lower.getIndex(i,j);

                double real = LU.data[indexLU];
                double imaginary = LU.data[indexLU+1];

                lower.data[indexL] = real;
                lower.data[indexL+1] = imaginary;
            }
        }

        if( numRows > numCols ) {
            for( int i = numCols; i < numRows; i++ ) {
                for( int j = 0; j < numCols; j++ ) {
                    int indexLU = LU.getIndex(i,j);
                    int indexL = lower.getIndex(i,j);

                    double real = LU.data[indexLU];
                    double imaginary = LU.data[indexLU+1];

                    lower.data[indexL] = real;
                    lower.data[indexL+1] = imaginary;
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
    public CDenseMatrix64F getUpper( CDenseMatrix64F upper )
    {
        int numRows = LU.numRows < LU.numCols ? LU.numRows : LU.numCols;
        int numCols = LU.numCols;

        if( upper == null ) {
            upper = new CDenseMatrix64F(numRows, numCols);
        } else {
            if( upper.numCols != numCols || upper.numRows != numRows )
                throw new IllegalArgumentException("Unexpected matrix dimension");
            CCommonOps.fill(upper, 0,0);
        }

        for( int i = 0; i < numRows; i++ ) {
            for( int j = i; j < numCols; j++ ) {
                int indexLU = LU.getIndex(i,j);
                int indexU = upper.getIndex(i,j);

                double real = LU.data[indexLU];
                double imaginary = LU.data[indexLU+1];

                upper.data[indexU] = real;
                upper.data[indexU+1] = imaginary;
            }
        }

        return upper;
    }

    public CDenseMatrix64F getPivot( CDenseMatrix64F pivot ) {
        return CSpecializedOps.pivotMatrix(pivot, this.pivot, LU.numRows, false);
    }

    protected void decomposeCommonInit(CDenseMatrix64F a) {
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
        // TODO update for complex
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
    public double computeDeterminant() {
        // TODO update for complex
        if( m != n )
            throw new IllegalArgumentException("Must be a square matrix.");

        double ret = pivsign;

        int total = m*n;
        for( int i = 0; i < total; i += n + 1 ) {
            ret *= dataLU[i];
        }

        return ret;
    }

    public double quality() {
        return CSpecializedOps.qualityTriangular(LU);
    }

    /**
     * a specialized version of solve that avoid additional checks that are not needed.
     */
    public void _solveVectorInternal( double []vv )
    {
        // TODO update for complex
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
        CTriangularSolver.solveU(dataLU, vv, n);
    }

    public double[] _getVV() {
        return vv;
    }
}