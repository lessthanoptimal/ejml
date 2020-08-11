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

import org.ejml.data.Complex_F32;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.decomposition.eig.symm.SymmetricQREigenHelper_FDRM;
import org.ejml.dense.row.decomposition.eig.symm.SymmetricQrAlgorithm_FDRM;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F32;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F32;


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
 * @see SymmetricQrAlgorithm_FDRM
 * @see org.ejml.dense.row.decomposition.hessenberg.TridiagonalDecompositionHouseholder_FDRM
 *
 * @author Peter Abeles
 */
public class SymmetricQRAlgorithmDecomposition_FDRM
        implements EigenDecomposition_F32<FMatrixRMaj> {

    // computes a tridiagonal matrix whose eigenvalues are the same as the original
    // matrix and can be easily computed.
    private TridiagonalSimilarDecomposition_F32<FMatrixRMaj> decomp;
    // helper class for eigenvalue and eigenvector algorithms
    private SymmetricQREigenHelper_FDRM helper;
    // computes the eigenvectors
    private SymmetricQrAlgorithm_FDRM vector;

    // should it compute eigenvectors at the same time as the eigenvalues?
    private boolean computeVectorsWithValues = false;

    // where the found eigenvalues are stored
    private float values[];

    // where the tridiagonal matrix is stored
    private float diag[];
    private float off[];

    private float diagSaved[];
    private float offSaved[];

    // temporary variable used to store/compute eigenvectors
    private FMatrixRMaj V;
    // the extracted eigenvectors
    private FMatrixRMaj eigenvectors[];

    // should it compute eigenvectors or just eigenvalues
    boolean computeVectors;

    public SymmetricQRAlgorithmDecomposition_FDRM(TridiagonalSimilarDecomposition_F32<FMatrixRMaj> decomp,
                                                 boolean computeVectors) {

        this.decomp = decomp;
        this.computeVectors = computeVectors;

        helper = new SymmetricQREigenHelper_FDRM();

        vector = new SymmetricQrAlgorithm_FDRM(helper);
    }

    public SymmetricQRAlgorithmDecomposition_FDRM(boolean computeVectors) {

        this(DecompositionFactory_FDRM.tridiagonal(0),computeVectors);
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
    public Complex_F32 getEigenvalue(int index) {
        return new Complex_F32(values[index],0);
    }

    @Override
    public FMatrixRMaj getEigenVector(int index) {
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
    public boolean decompose(FMatrixRMaj orig) {
        if( orig.numCols != orig.numRows )
            throw new IllegalArgumentException("Matrix must be square.");
        if( orig.numCols <= 0 )
            return false;

        int N = orig.numRows;

        // compute a similar tridiagonal matrix
        if( !decomp.decompose(orig) )
            return false;

        if( diag == null || diag.length < N) {
            diag = new float[N];
            off = new float[N-1];
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
        eigenvectors = CommonOps_FDRM.rowsToVector(V,eigenvectors);

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
        eigenvectors = CommonOps_FDRM.rowsToVector(V,eigenvectors);

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
