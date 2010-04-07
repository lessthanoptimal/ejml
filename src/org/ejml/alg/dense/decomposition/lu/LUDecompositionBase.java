/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.dense.decomposition.lu;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.LUDecomposition;
import org.ejml.alg.dense.decomposition.TriangularSolver;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.SpecializedOps;


/**
 * <p>
 * Contains common data structures and operations for LU decomposition algorithms.
 * </p>
 * @author Peter Abeles
 */
public abstract class LUDecompositionBase implements LUDecomposition {
    // the decomposed matrix
    protected DenseMatrix64F LU;

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

    @Override
    public void setExpectedMaxSize( int numRows , int numCols )
    {
        LU = new DenseMatrix64F(numRows,numCols);

        this.dataLU = LU.data;
        maxWidth = Math.max(numRows,numCols);

        vv = new double[ maxWidth ];
        indx = new int[ maxWidth ];
        pivot = new int[ maxWidth ];
    }

    public DenseMatrix64F getLU() {
        return LU;
    }

    public int[] getIndx() {
        return indx;
    }

    public int[] getPivot() {
        return pivot;
    }

    /**
     * Writes the lower triangular matrix into the specified matrix.
     *
     * @param lower Where the lower triangular matrix is writen to.
     */
    @Override
    public DenseMatrix64F getLower( DenseMatrix64F lower )
    {
        int numRows = LU.numRows;
        int numCols = LU.numRows < LU.numCols ? LU.numRows : LU.numCols;

        if( lower == null ) {
            lower = new DenseMatrix64F(numRows,numCols);
        } else {
            if( lower.numCols != numCols || lower.numRows != numRows )
                throw new IllegalArgumentException("Unexpected matrix dimension");
            CommonOps.set(lower,0);
        }

        for( int i = 0; i < numCols; i++ ) {
            lower.set(i,i,1.0);

            for( int j = 0; j < i; j++ ) {
                lower.set(i,j, LU.get(i,j));
            }
        }

        if( numRows > numCols ) {
            for( int i = numCols; i < numRows; i++ ) {
                for( int j = 0; j < numCols; j++ ) {
                    lower.set(i,j, LU.get(i,j));
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
    public DenseMatrix64F getUpper( DenseMatrix64F upper )
    {
        int numRows = LU.numRows < LU.numCols ? LU.numRows : LU.numCols;
        int numCols = LU.numCols;

        if( upper == null ) {
            upper = new DenseMatrix64F(numRows, numCols);
        } else {
            if( upper.numCols != numCols || upper.numRows != numRows )
                throw new IllegalArgumentException("Unexpected matrix dimension");
            CommonOps.set(upper,0);
        }

        for( int i = 0; i < numRows; i++ ) {
            for( int j = i; j < numCols; j++ ) {
                upper.set(i,j, LU.get(i,j));
            }
        }

        return upper;
    }

    public DenseMatrix64F getPivot( DenseMatrix64F pivot ) {
        return SpecializedOps.pivotMatrix(pivot, this.pivot, LU.numRows, false);
    }

    protected void decomposeCommonInit(DenseMatrix64F a) {
        if( a.numRows > maxWidth || a.numCols > maxWidth ) {
            setExpectedMaxSize(a.numRows,a.numCols);
        }

        m = a.numRows;
        n = a.numCols;

        LU.setReshape(a);
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
    public double computeDeterminant() {
        if( m != n )
            throw new IllegalArgumentException("Must be a square matrix.");

        double ret = pivsign;

        for( int i = 0; i < n; i++ ) {
            ret *= dataLU[i* n +i];
        }

        return ret;
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
                for( int j = ii-1; j < i; j++ ) {
                    sum -= dataLU[i* n +j]*vv[j];
                }
            } else if( sum != 0.0 ) {
                ii=i+1;
            }
            vv[i] = sum;
        }

        // Solve U*X = Y;
        TriangularSolver.solveU(dataLU,vv,n);
    }

    public double[] _getVV() {
        return vv;
    }
}