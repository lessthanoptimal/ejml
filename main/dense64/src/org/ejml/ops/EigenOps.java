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

package org.ejml.ops;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.eig.EigenPowerMethod;
import org.ejml.alg.dense.mult.VectorVectorMult;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.Eigenpair64F;
import org.ejml.factory.LinearSolverFactory;
import org.ejml.interfaces.decomposition.EigenDecomposition;
import org.ejml.interfaces.linsol.LinearSolver;


/**
 * Additional functions related to eigenvalues and eigenvectors of a matrix.
 *
 * @author Peter Abeles
 */
public class EigenOps {
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
    public static double computeEigenValue( DenseMatrix64F A , DenseMatrix64F eigenVector )
    {
        double bottom = VectorVectorMult.innerProd(eigenVector,eigenVector);
        double top = VectorVectorMult.innerProdA(eigenVector,A,eigenVector);

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
    public static Eigenpair64F computeEigenVector( DenseMatrix64F A , double eigenvalue )
    {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("Must be a square matrix.");

        DenseMatrix64F M = new DenseMatrix64F(A.numRows,A.numCols);

        DenseMatrix64F x = new DenseMatrix64F(A.numRows,1);
        DenseMatrix64F b = new DenseMatrix64F(A.numRows,1);

        CommonOps.fill(b, 1);
        
        // perturb the eigenvalue slightly so that its not an exact solution the first time
//        eigenvalue -= eigenvalue*UtilEjml.EPS*10;

        double origEigenvalue = eigenvalue;

        SpecializedOps.addIdentity(A,M,-eigenvalue);

        double threshold = NormOps.normPInf(A)*UtilEjml.EPS;

        double prevError = Double.MAX_VALUE;
        boolean hasWorked = false;

        LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.linear(M.numRows);

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
            if( MatrixFeatures.hasUncountable(x)) {
                failed = true;
            }

            if( failed ) {
                if( !hasWorked ) {
                     // if it failed on the first trial try perturbing it some more
                    double val = i % 2 == 0 ? 1.0-perp : 1.0 + perp;
                    // maybe this should be turn into a parameter allowing the user
                    // to configure the wise of each step

                    eigenvalue = origEigenvalue * Math.pow(val,i/2+1);
                    SpecializedOps.addIdentity(A,M,-eigenvalue);
                } else {
                    // otherwise assume that it was so accurate that the matrix was singular
                    // and return that result
                    return new Eigenpair64F(eigenvalue,b);
                }
            } else {
                hasWorked = true;
                
                b.set(x);
                NormOps.normalizeF(b);

                // compute the residual
                CommonOps.mult(M,b,x);
                double error = NormOps.normPInf(x);

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
                        return new Eigenpair64F(eigenvalue,b);

                    // update everything
                    prevError = error;
                    eigenvalue = VectorVectorMult.innerProdA(b,A,b);
                }

                SpecializedOps.addIdentity(A,M,-eigenvalue);
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
    public static Eigenpair64F dominantEigenpair( DenseMatrix64F A ) {

        EigenPowerMethod power = new EigenPowerMethod(A.numRows);

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
    public static double [] boundLargestEigenValue( DenseMatrix64F A , double []bound ) {
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
    public static DenseMatrix64F createMatrixD( EigenDecomposition eig )
    {
        int N = eig.getNumberOfEigenvalues();

        DenseMatrix64F D = new DenseMatrix64F( N , N );

        for( int i = 0; i < N; i++ ) {
            Complex64F c = eig.getEigenvalue(i);

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
    public static DenseMatrix64F createMatrixV( EigenDecomposition<DenseMatrix64F> eig )
    {
        int N = eig.getNumberOfEigenvalues();

        DenseMatrix64F V = new DenseMatrix64F( N , N );

        for( int i = 0; i < N; i++ ) {
            Complex64F c = eig.getEigenvalue(i);

            if( c.isReal() ) {
                DenseMatrix64F v = eig.getEigenVector(i);

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
