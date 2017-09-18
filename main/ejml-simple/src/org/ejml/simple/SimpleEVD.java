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

package org.ejml.simple;

import org.ejml.data.*;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition;
import org.ejml.interfaces.decomposition.EigenDecomposition_F32;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;

import java.util.ArrayList;
import java.util.List;


/**
 * Wrapper around EigenDecomposition for SimpleMatrix
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class SimpleEVD <T extends SimpleBase>
{
    private EigenDecomposition eig;

    Matrix mat;
    boolean is64;

    public SimpleEVD( Matrix mat )
    {
        this.mat = mat;
        this.is64 = mat instanceof DMatrixRMaj;

        if( is64) {
            eig = DecompositionFactory_DDRM.eig(mat.getNumCols(), true);
        } else {
            eig = DecompositionFactory_FDRM.eig(mat.getNumCols(), true);

        }
        if( !eig.decompose(mat))
            throw new RuntimeException("Eigenvalue Decomposition failed");
    }

    /**
     * Returns a list of all the eigenvalues
     */
    public List<Complex_F64> getEigenvalues() {
        List<Complex_F64> ret = new ArrayList<Complex_F64>();

        if( is64 ) {
            EigenDecomposition_F64 d = (EigenDecomposition_F64)eig;
            for (int i = 0; i < eig.getNumberOfEigenvalues(); i++) {
                ret.add(d.getEigenvalue(i));
            }
        } else {
            EigenDecomposition_F32 d = (EigenDecomposition_F32)eig;
            for (int i = 0; i < eig.getNumberOfEigenvalues(); i++) {
                Complex_F32 c = d.getEigenvalue(i);
                ret.add(new Complex_F64(c.real, c.imaginary));
            }
        }

        return ret;
    }

    /**
     * Returns the number of eigenvalues/eigenvectors.  This is the matrix's dimension.
     *
     * @return number of eigenvalues/eigenvectors.
     */
    public int getNumberOfEigenvalues() {
        return eig.getNumberOfEigenvalues();
    }

    /**
     * <p>
     * Returns an eigenvalue as a complex number.  For symmetric matrices the returned eigenvalue will always be a real
     * number, which means the imaginary component will be equal to zero.
     * </p>
     *
     * <p>
     * NOTE: The order of the eigenvalues is dependent upon the decomposition algorithm used.  This means that they may
     * or may not be ordered by magnitude.  For example the QR algorithm will returns results that are partially
     * ordered by magnitude, but this behavior should not be relied upon.
     * </p>
     *
     * @param index Index of the eigenvalue eigenvector pair.
     * @return An eigenvalue.
     */
    public Complex_F64 getEigenvalue(int index ) {
        if( is64 )
            return ((EigenDecomposition_F64)eig).getEigenvalue(index);
        else {
            Complex_F64 c = ((EigenDecomposition_F64)eig).getEigenvalue(index);
            return new Complex_F64(c.real, c.imaginary);
        }
    }

    /**
     * <p>
     * Used to retrieve real valued eigenvectors.  If an eigenvector is associated with a complex eigenvalue
     * then null is returned instead.
     * </p>
     *
     * @param index Index of the eigenvalue eigenvector pair.
     * @return If the associated eigenvalue is real then an eigenvector is returned, null otherwise.
     */
    public T getEigenVector( int index ) {
        Matrix v = eig.getEigenVector(index);
        if( v == null )
            return null;
        return (T)SimpleMatrix.wrap(v);
    }

    /**
     * <p>
     * Computes the quality of the computed decomposition.  A value close to or less than 1e-15
     * is considered to be within machine precision.
     * </p>
     *
     * <p>
     * This function must be called before the original matrix has been modified or else it will
     * produce meaningless results.
     * </p>
     *
     * @return Quality of the decomposition.
     */
    public /**/double quality() {
        if (is64) {
            return DecompositionFactory_DDRM.quality((DMatrixRMaj)mat, (EigenDecomposition_F64)eig);
        } else {
            return DecompositionFactory_FDRM.quality((FMatrixRMaj)mat, (EigenDecomposition_F32)eig);
        }
    }

    /**
     * Returns the underlying decomposition that this is a wrapper around.
     *
     * @return EigenDecomposition
     */
    public EigenDecomposition getEVD() {
        return eig;
    }

    /**
     * Returns the index of the eigenvalue which has the largest magnitude.
     *
     * @return index of the largest magnitude eigen value.
     */
    public int getIndexMax() {
        int indexMax = 0;
        double max = getEigenvalue(0).getMagnitude2();

        final int N = getNumberOfEigenvalues();
        for( int i = 1; i < N; i++ ) {
            double m = getEigenvalue(i).getMagnitude2();
            if( m > max ) {
                max = m;
                indexMax = i;
            }
        }

        return indexMax;
    }

    /**
     * Returns the index of the eigenvalue which has the smallest magnitude.
     *
     * @return index of the smallest magnitude eigen value.
     */
    public int getIndexMin() {
        int indexMin = 0;
        double min = getEigenvalue(0).getMagnitude2();

        final int N = getNumberOfEigenvalues();
        for( int i = 1; i < N; i++ ) {
            double m = getEigenvalue(i).getMagnitude2();
            if( m < min ) {
                min = m;
                indexMin = i;
            }
        }

        return indexMin;
    }
}