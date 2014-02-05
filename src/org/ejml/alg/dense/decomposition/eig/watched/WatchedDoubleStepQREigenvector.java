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

package org.ejml.alg.dense.decomposition.eig.watched;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.TriangularSolver;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.LinearSolverFactory;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps;
import org.ejml.ops.NormOps;
import org.ejml.ops.SpecializedOps;


/**
 * @author Peter Abeles
 */
public class WatchedDoubleStepQREigenvector {

    WatchedDoubleStepQREigen implicit;

    // Q matrix from double step QR
    DenseMatrix64F Q;


    DenseMatrix64F eigenvectors[];

    DenseMatrix64F eigenvectorTemp;

    LinearSolver solver;

    Complex64F origEigenvalues[];
    int N;

    int splits[];
    int numSplits;

    int x1,x2;

    int indexVal;
    boolean onscript;

    public boolean process( WatchedDoubleStepQREigen implicit , DenseMatrix64F A , DenseMatrix64F Q_h )
    {
        this.implicit = implicit;

        if( N != A.numRows ) {
            N = A.numRows;
            Q = new DenseMatrix64F(N,N);
            splits = new int[N];
            origEigenvalues = new Complex64F[N];
            eigenvectors = new DenseMatrix64F[N];
            eigenvectorTemp = new DenseMatrix64F(N,1);

            solver = LinearSolverFactory.linear(0);
        } else {
//            UtilEjml.setnull(eigenvectors);
            eigenvectors = new DenseMatrix64F[N];
        }
        System.arraycopy(implicit.eigenvalues,0,origEigenvalues,0,N);

        implicit.setup(A);
        implicit.setQ(Q);
        numSplits = 0;
        onscript = true;

//        System.out.println("Orig A");
//        A.print("%12.10f");

        if( !findQandR() )
            return false;

        return extractVectors(Q_h);
    }

    public boolean extractVectors( DenseMatrix64F Q_h ) {

        UtilEjml.memset(eigenvectorTemp.data,0);
        // extract eigenvectors from the shur matrix
        // start at the top left corner of the matrix
        boolean triangular = true;
        for( int i = 0; i < N; i++ ) {

            Complex64F c = implicit.eigenvalues[N-i-1];

            if( triangular && !c.isReal() )
                triangular = false;

            if( c.isReal() && eigenvectors[N-i-1] == null) {
                solveEigenvectorDuplicateEigenvalue(c.real,i,triangular);
            }
        }

        // translate the eigenvectors into the frame of the original matrix
        if( Q_h != null ) {
            DenseMatrix64F temp = new DenseMatrix64F(N,1);
            for( int i = 0; i < N; i++ ) {
                DenseMatrix64F v = eigenvectors[i];

                if( v != null ) {
                    CommonOps.mult(Q_h,v,temp);
                    eigenvectors[i] = temp;
                    temp = v;
                }
            }
        }

        return true;
    }

    private void solveEigenvectorDuplicateEigenvalue( double real , int first , boolean isTriangle ) {

        double scale = Math.abs(real);
        if( scale == 0 ) scale = 1;

        eigenvectorTemp.reshape(N,1, false);
        eigenvectorTemp.zero();

        if( first > 0 ) {
            if( isTriangle ) {
                solveUsingTriangle(real, first , eigenvectorTemp);
            } else {
                solveWithLU(real, first , eigenvectorTemp);
            }
        }

        eigenvectorTemp.reshape(N,1, false);

        for( int i = first; i < N; i++ ) {
            Complex64F c = implicit.eigenvalues[N-i-1];

            if( c.isReal() && Math.abs(c.real-real)/scale < 100.0*UtilEjml.EPS ) {
                eigenvectorTemp.data[i] = 1;

                DenseMatrix64F v = new DenseMatrix64F(N,1);
                CommonOps.multTransA(Q,eigenvectorTemp,v);
                eigenvectors[N-i-1] = v;
                NormOps.normalizeF(v);

                eigenvectorTemp.data[i] = 0;
            }
        }
    }

    private void solveUsingTriangle(double real, int index, DenseMatrix64F r ) {
        for( int i = 0; i < index; i++ ) {
            implicit.A.add(i,i,-real);
        }

        SpecializedOps.subvector(implicit.A,0,index,index,false,0,r);
        CommonOps.changeSign(r);

        TriangularSolver.solveU(implicit.A.data,r.data,implicit.A.numRows,0,index);

        for( int i = 0; i < index; i++ ) {
            implicit.A.add(i,i,real);
        }
    }

    private void solveWithLU(double real, int index, DenseMatrix64F r ) {
        DenseMatrix64F A = new DenseMatrix64F(index,index);

        CommonOps.extract(implicit.A,0,index,0,index,A,0,0);

        for( int i = 0; i < index; i++ ) {
            A.add(i,i,-real);
        }

        r.reshape(index,1, false);

        SpecializedOps.subvector(implicit.A,0,index,index,false,0,r);
        CommonOps.changeSign(r);

        // TODO this must be very inefficient
        if( !solver.setA(A))
            throw new RuntimeException("Solve failed");
        solver.solve(r,r);
    }

    public boolean findQandR() {
        CommonOps.setIdentity(Q);

        x1 = 0;
        x2 = N-1;

        // use the already computed eigenvalues to recompute the Q and R matrices
        indexVal = 0;
        while( indexVal < N ) {
            if (!findNextEigenvalue()) {
                return false;
            }
        }

//        Q.print("%1.10f");
//
//        implicit.A.print("%1.10f");

        return true;
    }

    private boolean findNextEigenvalue() {
        boolean foundEigen = false;
        while( !foundEigen && implicit.steps < implicit.maxIterations ) {
//            implicit.A.print();
            implicit.incrementSteps();

            if( x2 < x1 ) {
                moveToNextSplit();
            } else if( x2-x1 == 0 ) {
                implicit.addEigenAt(x1);
                x2--;
                indexVal++;
                foundEigen = true;
            } else if( x2-x1 == 1 && !implicit.isReal2x2(x1,x2)) {
                implicit.addComputedEigen2x2(x1,x2);
                x2 -= 2;
                indexVal += 2;
                foundEigen = true;
            } else if( implicit.steps-implicit.lastExceptional > implicit.exceptionalThreshold ) {
//                implicit.A.print("%e");
                //System.err.println("If it needs to do an exceptional shift then something went very bad.");
//                return false;
                implicit.exceptionalShift(x1,x2);
                implicit.lastExceptional = implicit.steps;
            } else if( implicit.isZero(x2,x2-1)) {
                // check for convergence
                implicit.addEigenAt(x2);
                foundEigen = true;
                x2--;
                indexVal++;
            } else {
                checkSplitPerformImplicit();
            }
        }
        return foundEigen;
    }


    private void checkSplitPerformImplicit() {
        // check for splits
        for( int i = x2; i > x1; i-- ) {
            if( implicit.isZero(i,i-1)) {
                x1 = i;
                splits[numSplits++] = i-1;
                // reduce the scope of what it is looking at
                return;
            }
        }
        // first try using known eigenvalues in the same order they were originally found
        if( onscript) {
            if( implicit.steps > implicit.exceptionalThreshold/2  ) {
                onscript = false;
            } else {
                Complex64F a = origEigenvalues[indexVal];

                // if no splits are found perform an implicit step
                if( a.isReal() ) {
                    implicit.performImplicitSingleStep(x1,x2, a.getReal());
                } else if( x2 < N-2 ) {
                    implicit.performImplicitDoubleStep(x1,x2, a.real,a.imaginary);
                } else {
                    onscript = false;
                }
            }
        } else {
            // that didn't work so try a modified order
            if( x2-x1 >= 1 && x2 < N-2 )
                implicit.implicitDoubleStep(x1,x2);
            else
                implicit.performImplicitSingleStep(x1,x2,implicit.A.get(x2,x2));
        }
    }


    private void moveToNextSplit() {
        if( numSplits <= 0 )
            throw new RuntimeException("bad");

        x2 = splits[--numSplits];

        if( numSplits > 0 ) {
            x1 = splits[numSplits-1]+1;
        } else {
            x1 = 0;
        }
    }

    public DenseMatrix64F getQ() {
        return Q;
    }

    public WatchedDoubleStepQREigen getImplicit() {
        return implicit;
    }

    public DenseMatrix64F[] getEigenvectors() {
        return eigenvectors;
    }

    public Complex64F[] getEigenvalues() {
        return implicit.eigenvalues;
    }
}
