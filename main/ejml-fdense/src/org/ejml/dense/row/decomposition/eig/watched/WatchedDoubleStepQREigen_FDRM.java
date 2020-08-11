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

package org.ejml.dense.row.decomposition.eig.watched;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F32;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.decomposition.eig.EigenvalueSmall_F32;
import org.ejml.dense.row.decomposition.qr.QrHelperFunctions_FDRM;

import java.util.Random;

/**
 * <p>
 * The float step implicit Eigenvalue decomposition algorithm is fairly complicated and needs to be designed so that
 * it can handle several special cases.  To aid in development and debugging this class was created.  It allows
 * individual components to be tested and to print out their results.  This shows how each step is performed.
 * </p>
 *
 * <p>
 * Do not use this class to compute the eigenvalues since it is much slower than a non-debug implementation.
 * </p>
 *
 */
// TODO make rank1UpdateMultR efficient once again by setting 0 to x1 and creating a new one that updates all the rows
// TODO option of modifying original matrix
public class WatchedDoubleStepQREigen_FDRM {

    private Random rand = new Random(0x2342);

    private int N;

    FMatrixRMaj A;
    private FMatrixRMaj u;
    private float gamma;

    private FMatrixRMaj _temp;

    // how many steps did it take to find the eigenvalue
    int numStepsFind[];
    int steps;
    Complex_F32 eigenvalues[];
    int numEigen;

    // computes eigenvalues for 2 by 2 submatrices
    private EigenvalueSmall_F32 valueSmall = new EigenvalueSmall_F32();

    private float temp[] = new float[9];

    private boolean printHumps = false;
    boolean checkHessenberg = false;
    private boolean checkOrthogonal = false;
    private boolean checkUncountable = false;

    private boolean useStandardEq = false;
    private boolean useCareful2x2 = true;

    private boolean normalize = true;

    int lastExceptional;
    int numExceptional;
    int exceptionalThreshold = 20;
    int maxIterations = exceptionalThreshold*20;

    public boolean createR = true;

    public FMatrixRMaj Q;

    public void incrementSteps() {
        steps++;
    }

    public void setQ( FMatrixRMaj Q ) {
        this.Q = Q;
    }

    private void addEigenvalue( float v ) {
        numStepsFind[numEigen] = steps;
        eigenvalues[numEigen].set(v,0);
        numEigen++;
        steps = 0;
        lastExceptional = 0;
    }

    private void addEigenvalue( float v , float i ) {
        numStepsFind[numEigen] = steps;
        eigenvalues[numEigen].set(v,i);
        numEigen++;
        steps = 0;
        lastExceptional = 0;
    }

    public void setChecks( boolean hessenberg , boolean orthogonal , boolean uncountable ) {
        this.checkHessenberg = hessenberg;
        this.checkOrthogonal = orthogonal;
        this.checkUncountable = uncountable;
    }


    public boolean isZero( int x1 , int x2 ) {
        // this provides a relative threshold for when dealing with very large/small numbers
        float target = Math.abs(A.get(x1,x2));

        float above = Math.abs(A.get(x1-1,x2));

        // according to Matrix Computations page 352 this is what is done in Eispack
        float right = Math.abs(A.get(x1,x2+1));
        return target <= 0.5f*UtilEjml.F_EPS*(above+right);
    }

    public void setup( FMatrixRMaj A ) {
        if( A.numRows != A.numCols )
            throw new RuntimeException("Must be square") ;

        if( N != A.numRows ) {
            N = A.numRows;

            this.A = A.copy();
            u = new FMatrixRMaj(A.numRows,1);

            _temp = new FMatrixRMaj(A.numRows,1);
            numStepsFind = new int[ A.numRows ];
        } else {
            this.A.set(A);
            UtilEjml.memset(numStepsFind,0,numStepsFind.length);
        }

        // zero all the off numbers that should be zero for a hessenberg matrix
        for( int i = 2; i < N; i++ ) {
            for( int j = 0; j < i-1; j++ ) {
                this.A.set(i,j,0);
            }
        }

        eigenvalues = new Complex_F32[ A.numRows ];
        for( int i = 0; i < eigenvalues.length; i++ ) {
            eigenvalues[i] = new Complex_F32();
        }

        numEigen = 0;
        lastExceptional = 0;
        numExceptional = 0;
        steps = 0;
    }

    /**
     * Perform a shift in a random direction that is of the same magnitude as the elements in the matrix.
     */
    public void exceptionalShift( int x1 , int x2) {
         if( printHumps )
            System.out.println("Performing exceptional implicit float step");

        // perform a random shift that is of the same magnitude as the matrix
        float val = Math.abs(A.get(x2,x2));

        if( val == 0 )
            val = 1;

        numExceptional++;
        // the closer the value is the better it handles identical eigenvalues cases
        float p = 1.0f - (float)Math.pow(0.1f,numExceptional);
        val *= p+2.0f*(1.0f-p)*(rand.nextFloat()-0.5f);

        if( rand.nextBoolean() )
            val = -val;

        performImplicitSingleStep(x1,x2,val);

        lastExceptional = steps;
    }

    /**
     * Performs an implicit float step using the values contained in the lower right hand side
     * of the submatrix for the estimated eigenvector values.
     * @param x1
     * @param x2
     */
    public void implicitDoubleStep( int x1 , int x2 ) {
        if( printHumps )
            System.out.println("Performing implicit float step");

         // compute the wilkinson shift
        float z11 = A.get(x2 - 1, x2 - 1);
        float z12 = A.get(x2 - 1, x2);
        float z21 = A.get(x2, x2 - 1);
        float z22 = A.get(x2, x2);

        float a11 = A.get(x1,x1);
        float a21 = A.get(x1+1,x1);
        float a12 = A.get(x1,x1+1);
        float a22 = A.get(x1+1,x1+1);
        float a32 = A.get(x1+2,x1+1);

        if( normalize ) {
            temp[0] = a11;temp[1] = a21;temp[2] = a12;temp[3] = a22;temp[4] = a32;
            temp[5] = z11;temp[6] = z22;temp[7] = z12;temp[8] = z21;

            float max = Math.abs(temp[0]);
            for( int j = 1; j < temp.length; j++ ) {
                if( Math.abs(temp[j]) > max )
                    max = Math.abs(temp[j]);
            }
            a11 /= max;a21 /= max;a12 /= max;a22 /= max;a32 /= max;
            z11 /= max;z22 /= max;z12 /= max;z21 /= max;
        }

        // these equations are derived when the eigenvalues are extracted from the lower right
        // 2 by 2 matrix.  See page 388 of Fundamentals of Matrix Computations 2nd ed for details.
        float b11,b21,b31;
        if( useStandardEq ) {
            b11 = ((a11- z11)*(a11- z22)- z21 * z12)/a21 + a12;
            b21 = a11 + a22 - z11 - z22;
            b31 = a32;
        } else {
            // this is different from the version in the book and seems in my testing to be more resilient to
            // over flow issues
            b11 = ((a11- z11)*(a11- z22)- z21 * z12) + a12*a21;
            b21 = (a11 + a22 - z11 - z22)*a21;
            b31 = a32*a21;
        }

        performImplicitDoubleStep(x1, x2, b11 , b21 , b31 );
    }

    /**
     * Performs an implicit float step given the set of two imaginary eigenvalues provided.
     * Since one eigenvalue is the complex conjugate of the other only one set of real and imaginary
     * numbers is needed.
     *
     * @param x1 upper index of submatrix.
     * @param x2 lower index of submatrix.
     * @param real Real component of each of the eigenvalues.
     * @param img Imaginary component of one of the eigenvalues.
     */
    public void performImplicitDoubleStep(int x1, int x2 , float real , float img  ) {
        float a11 = A.get(x1,x1);
        float a21 = A.get(x1+1,x1);
        float a12 = A.get(x1,x1+1);
        float a22 = A.get(x1+1,x1+1);
        float a32 = A.get(x1+2,x1+1);

        float p_plus_t = 2.0f*real;
        float p_times_t = real*real + img*img;

        float b11,b21,b31;
        if( useStandardEq ) {
            b11 = (a11*a11 - p_plus_t*a11+p_times_t)/a21 + a12;
            b21 = a11+a22-p_plus_t;
            b31 = a32;
        } else {
            // this is different from the version in the book and seems in my testing to be more resilient to
            // over flow issues
            b11 = (a11*a11 - p_plus_t*a11+p_times_t) + a12*a21;
            b21 = (a11+a22-p_plus_t)*a21;
            b31 = a32*a21;
        }

        performImplicitDoubleStep(x1, x2, b11, b21, b31);
    }

    private void performImplicitDoubleStep(int x1, int x2,
                                           float b11 , float b21 , float b31 ) {
        if( !bulgeDoubleStepQn(x1,b11,b21,b31,0,false) )
            return;

        // get rid of the bump
        if( Q != null ) {
            QrHelperFunctions_FDRM.rank1UpdateMultR(Q, u.data, gamma, 0, x1, x1 + 3, _temp.data);
            if( checkOrthogonal && !MatrixFeatures_FDRM.isOrthogonal(Q,UtilEjml.TEST_F32) ) {
                u.print();

                Q.print();
                throw new RuntimeException("Bad");
            }
        }

        if( printHumps ) {
            System.out.println("Applied first Q matrix, it should be humped now. A = ");
            A.print("%12.3fe");
            System.out.println("Pushing the hump off the matrix.");
        }

        // perform float steps
        for( int i = x1; i < x2-2; i++ ) {
            if( bulgeDoubleStepQn(i) && Q != null ) {
                QrHelperFunctions_FDRM.rank1UpdateMultR(Q, u.data, gamma, 0, i + 1, i + 4, _temp.data);
                if( checkOrthogonal && !MatrixFeatures_FDRM.isOrthogonal(Q,UtilEjml.TEST_F32) )
                    throw new RuntimeException("Bad");
            }

            if( printHumps ) {
                System.out.println("i = "+i+" A = ");
                A.print("%12.3fe");
            }
        }
        if( printHumps )
            System.out.println("removing last bump");
        // the last one has to be a single step
        if( x2-2 >= 0 && bulgeSingleStepQn(x2-2) && Q != null ) {
            QrHelperFunctions_FDRM.rank1UpdateMultR(Q, u.data, gamma, 0, x2 - 1, x2 + 1, _temp.data);
            if( checkOrthogonal && !MatrixFeatures_FDRM.isOrthogonal(Q,UtilEjml.TEST_F32) )
                throw new RuntimeException("Bad");

        }
        if( printHumps ) {
            System.out.println(" A = ");
            A.print("%12.3fe");
        }
//        A.print("%12.3fe");

        if( checkHessenberg && !MatrixFeatures_FDRM.isUpperTriangle(A,1,UtilEjml.TEST_F32)) {
            A.print("%12.3fe");
            throw new RuntimeException("Bad matrix");
        }
    }


    public void performImplicitSingleStep(int x1, int x2 , float eigenvalue ) {
        if( !createBulgeSingleStep(x1,eigenvalue) )
            return;

        // get rid of the bump
        if( Q != null ) {
            QrHelperFunctions_FDRM.rank1UpdateMultR(Q, u.data, gamma, 0, x1, x1 + 2, _temp.data);
            if( checkOrthogonal && !MatrixFeatures_FDRM.isOrthogonal(Q,UtilEjml.TEST_F32) )
                throw new RuntimeException("Bad");
        }

        if( printHumps ) {
            System.out.println("Applied first Q matrix, it should be humped now. A = ");
            A.print("%12.3fe");
            System.out.println("Pushing the hump off the matrix.");
        }

        // perform simple steps
        for( int i = x1; i < x2-1; i++ ) {
            if( bulgeSingleStepQn(i) && Q != null ) {
                QrHelperFunctions_FDRM.rank1UpdateMultR(Q, u.data, gamma, 0, i + 1, i + 3, _temp.data);
                if( checkOrthogonal && !MatrixFeatures_FDRM.isOrthogonal(Q,UtilEjml.TESTP_F32) )
                    throw new RuntimeException("Bad");
            }

            if( printHumps ) {
                System.out.println("i = "+i+" A = ");
                A.print("%12.3fe");
            }
        }

        if( checkHessenberg && !MatrixFeatures_FDRM.isUpperTriangle(A,1,UtilEjml.TESTP_F32)) {
            A.print("%12.3fe");
            throw new RuntimeException("Bad matrix");
        }
    }

    public boolean createBulgeSingleStep( int x1 , float eigenvalue ) {

        float b11 = A.get(x1,x1) - eigenvalue;
        float b21 = A.get(x1+1,x1);

        float threshold = Math.abs(A.get(x1,x1))*UtilEjml.F_EPS;

        return bulgeSingleStepQn(x1,b11,b21,threshold,false);
    }

    public boolean bulgeDoubleStepQn( int i ) {
        float a11 = A.get(i+1,i);
        float a21 = A.get(i+2,i);
        float a31 = A.get(i+3,i);

        float threshold = Math.abs(A.get(i,i))*UtilEjml.F_EPS;

        return bulgeDoubleStepQn(i+1,a11,a21,a31,threshold,true);
    }

    public boolean bulgeDoubleStepQn( int i ,
                                      float a11, float a21 , float a31,
                                      float threshold , boolean set )
    {
        float max;
        if( normalize ) {
            float absA11 = Math.abs(a11);
            float absA21 = Math.abs(a21);
            float absA31 = Math.abs(a31);

            max = absA11 > absA21 ? absA11 : absA21;
            if( absA31 > max ) max = absA31;

//           if( max <= Math.abs(A.get(i,i))*UtilEjml.F_EPS ) {
            if( max <= threshold ) {
                if( set ) {
                    A.set(i,i-1,0);
                    A.set(i+1,i-1,0);
                    A.set(i+2,i-1,0);
                }
                return false;
            }

            a11 /= max;
            a21 /= max;
            a31 /= max;
        } else {
            max = 1;
        }

        // compute the reflector using the b's above

        float tau = (float)Math.sqrt(a11*a11 + a21*a21 + a31*a31);
        if( a11 < 0 ) tau = -tau;

        float div = a11+tau;

        u.set(i,0,1);
        u.set(i+1,0,a21/div);
        u.set(i+2,0,a31/div);

        gamma = div/tau;

        // compute A_1 = Q_1^T * A * Q_1

        // apply Q*A  - just do the 3 rows
        QrHelperFunctions_FDRM.rank1UpdateMultR(A, u.data, gamma, 0, i, i + 3, _temp.data);

        if( set ) {
            A.set(i,i-1,-max*tau);
            A.set(i+1,i-1,0);
            A.set(i+2,i-1,0);
        }

        if( printHumps ) {
            System.out.println("  After Q.   A =");
            A.print();
        }

        // apply A*Q - just the three things
        QrHelperFunctions_FDRM.rank1UpdateMultL(A, u.data, gamma, 0, i, i + 3);

//        System.out.println("  after Q*A*Q ");
//        A.print();

        if(checkUncountable && MatrixFeatures_FDRM.hasUncountable(A)) {
            throw new RuntimeException("bad matrix");
        }

        return true;
    }

    public boolean bulgeSingleStepQn( int i )
    {
        float a11 = A.get(i+1,i);
        float a21 = A.get(i+2,i);

        float threshold = Math.abs(A.get(i,i))*UtilEjml.F_EPS;

        return bulgeSingleStepQn(i+1,a11,a21,threshold,true);
    }

    public boolean bulgeSingleStepQn( int i ,
                                      float a11 , float a21 ,
                                      float threshold , boolean set)
    {

        float max;
        if( normalize ) {
            max = Math.abs(a11);
            if( max < Math.abs(a21)) max = Math.abs(a21);

//            if( max <= Math.abs(A.get(i,i))*UtilEjml.F_EPS ) {
            if( max <= threshold ) {
//                System.out.println("i = "+i);
//                A.print();
                if( set ) {
                    A.set(i,i-1,0);
                    A.set(i+1,i-1,0);
                }
                return false;
            }

            a11 /= max;
            a21 /= max;
        } else {
            max = 1;
        }

        // compute the reflector using the b's above

        float tau = (float)Math.sqrt(a11*a11 + a21*a21);
        if( a11 < 0 ) tau = -tau;

        float div = a11+tau;

        u.set(i,0,1);
        u.set(i+1,0,a21/div);

        gamma = div/tau;

        // compute A_1 = Q_1^T * A * Q_1

        // apply Q*A  - just do the 3 rows
        QrHelperFunctions_FDRM.rank1UpdateMultR(A, u.data, gamma, 0, i, i + 2, _temp.data);

        if( set ) {
            A.set(i,i-1,-max*tau);
            A.set(i+1,i-1,0);
        }

        // apply A*Q - just the three things
        QrHelperFunctions_FDRM.rank1UpdateMultL(A, u.data, gamma, 0, i, i + 2);

        if(checkUncountable && MatrixFeatures_FDRM.hasUncountable(A)) {
            throw new RuntimeException("bad matrix");
        }

        return true;
    }

    public void eigen2by2_scale( float a11 , float a12 , float a21 , float a22 )
    {
        float abs11 = Math.abs(a11);
        float abs22 = Math.abs(a22);
        float abs12 = Math.abs(a12);
        float abs21 = Math.abs(a21);

        float max = abs11 > abs22 ? abs11 : abs22;
        if( max < abs12 ) max = abs12;
        if( max < abs21 ) max = abs21;

        if( max == 0 ) {
            valueSmall.value0.real = 0;
            valueSmall.value0.imaginary = 0;
            valueSmall.value1.real = 0;
            valueSmall.value1.imaginary = 0;
        } else {
            a12 /= max; a21 /= max; a11/=max;a22/=max;

            if( useCareful2x2 ) {
                valueSmall.value2x2(a11,a12,a21,a22);
            } else {
                valueSmall.value2x2_fast(a11,a12,a21,a22);
            }
            valueSmall.value0.real *= max;
            valueSmall.value0.imaginary *= max;
            valueSmall.value1.real *= max;
            valueSmall.value1.imaginary *= max;

        }

//        System.out.printf("eigen (%6.3ff , %6.3ff) (%6.3ff , %6.3ff)\n",p0_real,p0_img,p1_real,p1_img);
    }

    public int getNumberOfEigenvalues() {
        return numEigen;
    }

    public Complex_F32[] getEigenvalues() {
        return eigenvalues;
    }

    public void addComputedEigen2x2(int x1,int x2) {
        eigen2by2_scale(A.get(x1,x1),A.get(x1,x2),A.get(x2,x1),A.get(x2,x2));

        if( checkUncountable &&
                (Float.isNaN(valueSmall.value0.real) || Float.isNaN(valueSmall.value1.real)) ) {
            throw new RuntimeException("Uncountable");
        }

        addEigenvalue(valueSmall.value0.real,valueSmall.value0.imaginary);
        addEigenvalue(valueSmall.value1.real,valueSmall.value1.imaginary);
    }

    public boolean isReal2x2( int x1 , int x2 ) {
        eigen2by2_scale(A.get(x1,x1),A.get(x1,x2),A.get(x2,x1),A.get(x2,x2));

        return valueSmall.value0.isReal();
    }

    public void addEigenAt( int x1 ) {
        addEigenvalue(A.get(x1,x1));
    }

    public void printSteps() {
        for( int i = 0; i < N; i++ ) {
            System.out.println("Step["+i+"] = "+numStepsFind[i]);
        }
    }
}
