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

package org.ejml.dense.row;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.DEigenpair;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.decomposition.eig.EigenPowerMethod_DDRM;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;
import org.ejml.interfaces.linsol.LinearSolverDense;


/**
 * Additional functions related to eigenvalues and eigenvectors of a matrix.
 *
 * @author Peter Abeles
 */
public class EigenOps_DDRM {
    /**
     * <p>
     * Given matrix A and an eigen vector of A, compute the corresponding eigen value.  This is
     * the Rayleigh quotient.<br>
     * <br>
     * x<sup>T</sup>Ax / x<sup>T</sup>x
     * </p>
     *
     *
     * @param A Matrix. Not modified.
     * @param eigenVector An eigen vector of A. Not modified.
     * @return The corresponding eigen value.
     */
    public static double computeEigenValue(DMatrixRMaj A , DMatrixRMaj eigenVector )
    {
        double bottom = VectorVectorMult_DDRM.innerProd(eigenVector,eigenVector);
        double top = VectorVectorMult_DDRM.innerProdA(eigenVector,A,eigenVector);

        return top/bottom;
    }

    /**
     * <p>
     * Given an eigenvalue it computes an eigenvector using inverse iteration:
     * <br>
     * for i=1:MAX {<br>
     *   (A - &mu;I)z<sup>(i)</sup> = q<sup>(i-1)</sup><br>
     *   q<sup>(i)</sup> = z<sup>(i)</sup> / ||z<sup>(i)</sup>||<br>
     * &lambda;<sup>(i)</sup> =  q<sup>(i)</sup><sup>T</sup> A  q<sup>(i)</sup><br>
     * }<br>
     * </p>
     * <p>
     * NOTE: If there is another eigenvalue that is very similar to the provided one then there
     * is a chance of it converging towards that one instead.  The larger a matrix is the more
     * likely this is to happen.
     * </p>
     * @param A Matrix whose eigenvector is being computed.  Not modified.
     * @param eigenvalue The eigenvalue in the eigen pair.
     * @return The eigenvector or null if none could be found.
     */
    public static DEigenpair computeEigenVector(DMatrixRMaj A , double eigenvalue )
    {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("Must be a square matrix.");

        DMatrixRMaj M = new DMatrixRMaj(A.numRows,A.numCols);

        DMatrixRMaj x = new DMatrixRMaj(A.numRows,1);
        DMatrixRMaj b = new DMatrixRMaj(A.numRows,1);

        CommonOps_DDRM.fill(b, 1);
        
        // perturb the eigenvalue slightly so that its not an exact solution the first time
//        eigenvalue -= eigenvalue*UtilEjml.EPS*10;

        double origEigenvalue = eigenvalue;

        SpecializedOps_DDRM.addIdentity(A,M,-eigenvalue);

        double threshold = NormOps_DDRM.normPInf(A)*UtilEjml.EPS;

        double prevError = Double.MAX_VALUE;
        boolean hasWorked = false;

        LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.linear(M.numRows);

        double perp = 0.0001;

        for( int i = 0; i < 200; i++ ) {
            boolean failed = false;
            // if the matrix is singular then the eigenvalue is within machine precision
            // of the true value, meaning that x must also be.
            if( !solver.setA(M) ) {
                failed = true;
            } else {
                solver.solve(b,x);
            }

            // see if solve silently failed
            if( MatrixFeatures_DDRM.hasUncountable(x)) {
                failed = true;
            }

            if( failed ) {
                if( !hasWorked ) {
                     // if it failed on the first trial try perturbing it some more
                    double val = i % 2 == 0 ? 1.0-perp : 1.0 + perp;
                    // maybe this should be turn into a parameter allowing the user
                    // to configure the wise of each step

                    eigenvalue = origEigenvalue * Math.pow(val,i/2+1);
                    SpecializedOps_DDRM.addIdentity(A,M,-eigenvalue);
                } else {
                    // otherwise assume that it was so accurate that the matrix was singular
                    // and return that result
                    return new DEigenpair(eigenvalue,b);
                }
            } else {
                hasWorked = true;
                
                b.set(x);
                NormOps_DDRM.normalizeF(b);

                // compute the residual
                CommonOps_DDRM.mult(M,b,x);
                double error = NormOps_DDRM.normPInf(x);

                if( error-prevError > UtilEjml.EPS*10) {
                    // if the error increased it is probably converging towards a different
                    // eigenvalue
//                    CommonOps.set(b,1);
                    prevError = Double.MAX_VALUE;
                    hasWorked = false;
                    double val = i % 2 == 0 ? 1.0-perp : 1.0 + perp;
                    eigenvalue = origEigenvalue * Math.pow(val,1);
                } else {
                    // see if it has converged
                    if(error <= threshold || Math.abs(prevError-error) <= UtilEjml.EPS)
                        return new DEigenpair(eigenvalue,b);

                    // update everything
                    prevError = error;
                    eigenvalue = VectorVectorMult_DDRM.innerProdA(b,A,b);
                }

                SpecializedOps_DDRM.addIdentity(A,M,-eigenvalue);
            }
        }

        return null;
    }


    /**
     * <p>
     * Computes the dominant eigen vector for a matrix.  The dominant eigen vector is an
     * eigen vector associated with the largest eigen value.
     * </p>
     *
     * <p>
     * WARNING: This function uses the power method.  There are known cases where it will not converge.
     * It also seems to converge to non-dominant eigen vectors some times.  Use at your own risk.
     * </p>
     *
     * @param A A matrix.  Not modified.
     */
    // TODO maybe do the regular power method, estimate the eigenvalue, then shift invert?
    public static DEigenpair dominantEigenpair(DMatrixRMaj A ) {

        EigenPowerMethod_DDRM power = new EigenPowerMethod_DDRM(A.numRows);

        // eh maybe 0.1 is a good value.  who knows.
        if( !power.computeShiftInvert(A,0.1) )
            return null;

        return null;//power.getEigenVector();
    }

    /**
     * <p>
     * Generates a bound for the largest eigen value of the provided matrix using Perron-Frobenius
     * theorem.   This function only applies to non-negative real matrices.
     * </p>
     *
     * <p>
     * For "stochastic" matrices (Markov process) this should return one for the upper and lower bound.
     * </p>
     *
     * @param A Square matrix with positive elements.  Not modified.
     * @param bound Where the results are stored.  If null then a matrix will be declared. Modified.
     * @return Lower and upper bound in the first and second elements respectively.
     */
    public static double [] boundLargestEigenValue(DMatrixRMaj A , double []bound ) {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("A must be a square matrix.");

        double min = Double.MAX_VALUE;
        double max = 0;

        int n = A.numRows;

        for( int i = 0; i < n; i++ ) {
            double total = 0;
            for( int j = 0; j < n; j++ ) {
                double v = A.get(i,j);
                if( v < 0 ) throw new IllegalArgumentException("Matrix must be positive");

                total += v;
            }

            if( total < min ) {
                min = total;
            }

            if( total > max ) {
                max = total;
            }
        }

        if( bound == null )
            bound = new double[2];

        bound[0] = min;
        bound[1] = max;

        return bound;
    }

    /**
     * <p>
     * A diagonal matrix where real diagonal element contains a real eigenvalue.  If an eigenvalue
     * is imaginary then zero is stored in its place.
     * </p>
     *
     * @param eig An eigenvalue decomposition which has already decomposed a matrix.
     * @return A diagonal matrix containing the eigenvalues.
     */
    public static DMatrixRMaj createMatrixD(EigenDecomposition_F64 eig )
    {
        int N = eig.getNumberOfEigenvalues();

        DMatrixRMaj D = new DMatrixRMaj( N , N );

        for( int i = 0; i < N; i++ ) {
            Complex_F64 c = eig.getEigenvalue(i);

            if( c.isReal() ) {
                D.set(i,i,c.real);
            }
        }

        return D;
    }

    /**
     * <p>
     * Puts all the real eigenvectors into the columns of a matrix.  If an eigenvalue is imaginary
     * then the corresponding eigenvector will have zeros in its column.
     * </p>
     * 
     * @param eig An eigenvalue decomposition which has already decomposed a matrix.
     * @return An m by m matrix containing eigenvectors in its columns.
     */
    public static DMatrixRMaj createMatrixV(EigenDecomposition_F64<DMatrixRMaj> eig )
    {
        int N = eig.getNumberOfEigenvalues();

        DMatrixRMaj V = new DMatrixRMaj( N , N );

        for( int i = 0; i < N; i++ ) {
            Complex_F64 c = eig.getEigenvalue(i);

            if( c.isReal() ) {
                DMatrixRMaj v = eig.getEigenVector(i);

                if( v != null ) {
                    for( int j = 0; j < N; j++ ) {
                        V.set(j,i,v.get(j,0));
                    }
                }
            }
        }

        return V;
    }
}
