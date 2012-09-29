/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.svd;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.SingularValueDecomposition;

/**
 * Wraps around a {@link SingularValueDecomposition} and ensures that the input is not modified.
 *
 * @author Peter Abeles
 */
public class SafeSvd
        implements SingularValueDecomposition<DenseMatrix64F>
{
    // the decomposition algorithm
    SingularValueDecomposition<DenseMatrix64F> alg;
    // storage for the input if it would be modified
    DenseMatrix64F work = new DenseMatrix64F(1,1);

    public SafeSvd(SingularValueDecomposition<DenseMatrix64F> alg) {
        this.alg = alg;
    }

    @Override
    public double[] getSingularValues() {
        return alg.getSingularValues();
    }

    @Override
    public int numberOfSingularValues() {
        return alg.numberOfSingularValues();
    }

    @Override
    public boolean isCompact() {
        return alg.isCompact();
    }

    @Override
    public DenseMatrix64F getU(DenseMatrix64F U, boolean transposed) {
        return alg.getU(U,transposed);
    }

    @Override
    public DenseMatrix64F getV(DenseMatrix64F V, boolean transposed) {
        return alg.getV(V,transposed);
    }

    @Override
    public DenseMatrix64F getW(DenseMatrix64F W) {
        return alg.getW(W);
    }

    @Override
    public int numRows() {
        return alg.numRows();
    }

    @Override
    public int numCols() {
        return alg.numCols();
    }

    @Override
    public boolean decompose(DenseMatrix64F orig) {
        if( alg.inputModified() ) {
            work.reshape(orig.numRows,orig.numCols);
            work.set(orig);
            return alg.decompose(work);
        } else {
            return alg.decompose(orig);
        }
    }

    @Override
    public boolean inputModified() {
        return false;
    }
}
