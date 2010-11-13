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

package org.ejml.alg.dense.decomposition.eig;

import org.ejml.alg.dense.decomposition.EigenDecomposition;
import org.ejml.alg.dense.decomposition.eig.symm.SymmetricQREigenHelper;
import org.ejml.alg.dense.decomposition.eig.symm.SymmetricQrAlgorithm;
import org.ejml.alg.dense.decomposition.hessenberg.TridiagonalSimilarDecomposition;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;


/**
 * <p>
 * Computes the eigenvalues and eigenvectors of a real symmetric matrix using the symmetric implicit QR algorithm.
 * Inside each iteration a QR decomposition of A<sub>i</sub>-p<sub>i</sub>I is implicitly computed.
 * </p>
 * <p>
 * This implementation is based on the algorithm is sketched out in:<br>
 * David S. Watkins, "Fundamentals of Matrix Computations," Second Edition. page 377-385
 * </p>
 *
 * @see org.ejml.alg.dense.decomposition.eig.symm.SymmetricQrAlgorithm
 * @see org.ejml.alg.dense.decomposition.hessenberg.TridiagonalSimilarDecomposition
 *
 * @author Peter Abeles
 */
public class SymmetricQRAlgorithmDecomposition implements EigenDecomposition {

    // computes a tridiagonal matrix whose eigenvalues are the same as the original
    // matrix and can be easily computed.
    private TridiagonalSimilarDecomposition decomp;
    // helper class for eigenvalue and eigenvector algorithms
    private SymmetricQREigenHelper helper;
    // computes the eigenvectors
    private SymmetricQrAlgorithm vector;

    // should it compute eigenvectors at the same time as the eigenvalues?
    private boolean computeVectorsWithValues = false;

    // where the found eigenvalues are stored
    private double values[];

    // where the tridiagonal matrix is stored
    private double diag[];
    private double off[];

    // temporary variable used to store/compute eigenvectors
    private DenseMatrix64F V;
    // the extracted eigenvectors
    private DenseMatrix64F eigenvectors[];

    // should it compute eigenvectors or just eigenvalues
    boolean computeVectors;

    public SymmetricQRAlgorithmDecomposition( boolean computeVectors ) {

        this.computeVectors = computeVectors;

        decomp = new TridiagonalSimilarDecomposition();
        helper = new SymmetricQREigenHelper();

        vector = new SymmetricQrAlgorithm(helper);
    }

    public void setComputeVectorsWithValues(boolean computeVectorsWithValues) {
        if( !computeVectors )
            throw new IllegalArgumentException("Compute eigenvalues has been set to false");

        this.computeVectorsWithValues = computeVectorsWithValues;
    }

    /**
     * Used to limit the number of internal QR iterations that the QR algorithm performs.  20
     * should be enough for most applications.
     *
     * @param max The maximum number of QR iterations it will perform.
     */
    public void setMaxIterations( int max ) {
        vector.setMaxIterations(max);
    }

    @Override
    public int getNumberOfEigenvalues() {
        return helper.getMatrixSize();
    }

    @Override
    public Complex64F getEigenvalue(int index) {
        return new Complex64F(values[index],0);
    }

    @Override
    public DenseMatrix64F getEigenVector(int index) {
        return eigenvectors[index];
    }

    /**
     * Decomposes the matrix using the QR algorithm.  Care was taken to minimize unnecisary memory copying
     * and cache skipping.
     *
     * @param orig The matrix which is being decomposed.  Not modified.
     * @return true if it decomposed the matrix or false if an error was detected.  This will not catch all errors.
     */
    @Override
    public boolean decompose(DenseMatrix64F orig) {
        // compute a similar tridiagonal matrix
        decomp.decompose(orig);

        // get raw decompose matrix in its internal format
        // this works for this particular decomposition algorithm only
        DenseMatrix64F QT = decomp.getQT();

        // Tell the helper to work with this matrix
        helper.init(QT);

        if( computeVectors ) {
            if( computeVectorsWithValues ) {
                return extractTogether(orig);
            }  else {
                return extractSeparate(orig);
            }
        } else {
            return computeEigenValues();
        }
    }

    @Override
    public boolean inputModified() {
        return false;
    }

    private boolean extractTogether(DenseMatrix64F orig) {
        // extract the orthogonal from the similar transform
        V = decomp.getQTran(V);

        // tell eigenvector algorithm to update this matrix as it computes the rotators
        helper.setQ(V);

        vector.setFastEigenvalues(false);

        // extract the eigenvalues
        if( !vector.process(null) )
            return false;

        // the V matrix contains the eigenvectors.  Convert those into column vectors
        eigenvectors = CommonOps.rowsToVector(V,eigenvectors);

        // save a copy of them since this data structure will be recycled next
        values = helper.copyEigenvalues(values);

        return true;
    }

    private boolean extractSeparate(DenseMatrix64F orig) {
        if (!computeEigenValues())
            return false;

        // ---- set up the helper to decompose the same tridiagonal matrix
        // swap arrays instead of copying them to make it slightly faster
        helper.reset(orig.numCols);
        diag = helper.swapDiag(diag);
        off = helper.swapOff(off);

        // extract the orthogonal from the similar transform
        V = decomp.getQTran(V);

        // tell eigenvector algorithm to update this matrix as it computes the rotators
        vector.setQ(V);

        // extract eigenvectors
        if( !vector.process(null, values) )
            return false;

        // the ordering of the eigenvalues might have changed
        values = helper.copyEigenvalues(values);
        // the V matrix contains the eigenvectors.  Convert those into column vectors
        eigenvectors = CommonOps.rowsToVector(V,eigenvectors);

        return true;
    }

   /**
     * Computes eigenvalues only
    *
     * @return
     */
    private boolean computeEigenValues() {
        // make a copy of the internal tridiagonal matrix data for later use
        diag = helper.copyDiag(diag);
        off = helper.copyOff(off);

       vector.setQ(null);
       vector.setFastEigenvalues(true);

        // extract the eigenvalues
        if( !vector.process(null) )
            return false;

        // save a copy of them since this data structure will be recycled next
        values = helper.copyEigenvalues(values);
        return true;
    }
}
