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

package org.ejml.dense.row.decomposition.eig;

import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.decomposition.eig.symm.SymmetricQREigenHelper_DDRM;
import org.ejml.dense.row.decomposition.eig.symm.SymmetricQrAlgorithm_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F64;


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
 * @see SymmetricQrAlgorithm_DDRM
 * @see org.ejml.dense.row.decomposition.hessenberg.TridiagonalDecompositionHouseholder_DDRM
 *
 * @author Peter Abeles
 */
public class SymmetricQRAlgorithmDecomposition_DDRM
        implements EigenDecomposition_F64<DMatrixRMaj> {

    // computes a tridiagonal matrix whose eigenvalues are the same as the original
    // matrix and can be easily computed.
    private TridiagonalSimilarDecomposition_F64<DMatrixRMaj> decomp;
    // helper class for eigenvalue and eigenvector algorithms
    private SymmetricQREigenHelper_DDRM helper;
    // computes the eigenvectors
    private SymmetricQrAlgorithm_DDRM vector;

    // should it compute eigenvectors at the same time as the eigenvalues?
    private boolean computeVectorsWithValues = false;

    // where the found eigenvalues are stored
    private double values[];

    // where the tridiagonal matrix is stored
    private double diag[];
    private double off[];

    private double diagSaved[];
    private double offSaved[];

    // temporary variable used to store/compute eigenvectors
    private DMatrixRMaj V;
    // the extracted eigenvectors
    private DMatrixRMaj eigenvectors[];

    // should it compute eigenvectors or just eigenvalues
    boolean computeVectors;

    public SymmetricQRAlgorithmDecomposition_DDRM(TridiagonalSimilarDecomposition_F64<DMatrixRMaj> decomp,
                                                 boolean computeVectors) {

        this.decomp = decomp;
        this.computeVectors = computeVectors;

        helper = new SymmetricQREigenHelper_DDRM();

        vector = new SymmetricQrAlgorithm_DDRM(helper);
    }

    public SymmetricQRAlgorithmDecomposition_DDRM(boolean computeVectors) {

        this(DecompositionFactory_DDRM.tridiagonal(0),computeVectors);
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
    public Complex_F64 getEigenvalue(int index) {
        return new Complex_F64(values[index],0);
    }

    @Override
    public DMatrixRMaj getEigenVector(int index) {
        return eigenvectors[index];
    }

    /**
     * Decomposes the matrix using the QR algorithm.  Care was taken to minimize unnecessary memory copying
     * and cache skipping.
     *
     * @param orig The matrix which is being decomposed.  Not modified.
     * @return true if it decomposed the matrix or false if an error was detected.  This will not catch all errors.
     */
    @Override
    public boolean decompose(DMatrixRMaj orig) {
        if( orig.numCols != orig.numRows )
            throw new IllegalArgumentException("Matrix must be square.");
        if( orig.numCols <= 0 )
            return false;

        int N = orig.numRows;

        // compute a similar tridiagonal matrix
        if( !decomp.decompose(orig) )
            return false;

        if( diag == null || diag.length < N) {
            diag = new double[N];
            off = new double[N-1];
        }
        decomp.getDiagonal(diag,off);

        // Tell the helper to work with this matrix
        helper.init(diag,off,N);

        if( computeVectors ) {
            if( computeVectorsWithValues ) {
                return extractTogether();
            }  else {
                return extractSeparate(N);
            }
        } else {
            return computeEigenValues();
        }
    }

    @Override
    public boolean inputModified() {
        return decomp.inputModified();
    }

    private boolean extractTogether() {
        // extract the orthogonal from the similar transform
        V = decomp.getQ(V,true);

        // tell eigenvector algorithm to update this matrix as it computes the rotators
        helper.setQ(V);

        vector.setFastEigenvalues(false);

        // extract the eigenvalues
        if( !vector.process(-1,null,null) )
            return false;

        // the V matrix contains the eigenvectors.  Convert those into column vectors
        eigenvectors = CommonOps_DDRM.rowsToVector(V,eigenvectors);

        // save a copy of them since this data structure will be recycled next
        values = helper.copyEigenvalues(values);

        return true;
    }

    private boolean extractSeparate(int numCols) {
        if (!computeEigenValues())
            return false;

        // ---- set up the helper to decompose the same tridiagonal matrix
        // swap arrays instead of copying them to make it slightly faster
        helper.reset(numCols);
        diagSaved = helper.swapDiag(diagSaved);
        offSaved = helper.swapOff(offSaved);

        // extract the orthogonal from the similar transform
        V = decomp.getQ(V,true);

        // tell eigenvector algorithm to update this matrix as it computes the rotators
        vector.setQ(V);

        // extract eigenvectors
        if( !vector.process(-1,null,null, values) )
            return false;

        // the ordering of the eigenvalues might have changed
        values = helper.copyEigenvalues(values);
        // the V matrix contains the eigenvectors.  Convert those into column vectors
        eigenvectors = CommonOps_DDRM.rowsToVector(V,eigenvectors);

        return true;
    }

   /**
     * Computes eigenvalues only
    *
     * @return
     */
    private boolean computeEigenValues() {
       // make a copy of the internal tridiagonal matrix data for later use
       diagSaved = helper.copyDiag(diagSaved);
       offSaved = helper.copyOff(offSaved);

       vector.setQ(null);
       vector.setFastEigenvalues(true);

       // extract the eigenvalues
       if( !vector.process(-1,null,null) )
           return false;

       // save a copy of them since this data structure will be recycled next
       values = helper.copyEigenvalues(values);
       return true;
   }
}
