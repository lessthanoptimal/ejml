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

package org.ejml.alg.dense.decomposition.svd.implicitqr;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.eig.EigenvalueSmall;
import org.ejml.data.DenseMatrix64F;

import java.util.Random;


/**
 * <p>
 * Computes the QR decomposition of a bidiagonal matrix.  Internally this matrix is stored as
 * two arrays.  Shifts can either be provided to it or it can generate the shifts on its own.
 * It optionally computes the U and V matrices.  This comparability allows it to be used to
 * compute singular values and associated matrices efficiently.<br>
 * <br>
 * A = U*S*V<sup>T</sup><br>
 * where A is the original m by n matrix.
 * </p>
 *
 * <p>
 * Based off of the outline provided in:<br>
 * <br>
 * David S. Watkins, "Fundamentals of Matrix Computations," Second Edition. Page 404-411
 * </p>
 *
 * <p>
 * Note: To watch it process the matrix step by step uncomment commented out code.
 * </p>
 *
 * @author Peter Abeles
 */
public class SvdImplicitQrAlgorithm {

    // used in exceptional shifts
    protected Random rand = new Random(0x34671e);

    // U and V matrices in singular value decomposition.  Stored in the transpose
    // to reduce cache jumps
    protected DenseMatrix64F Ut;
    protected DenseMatrix64F Vt;

    // number of times it has performed an implicit step, the most costly part of the
    // algorithm
    protected int totalSteps;

    // max value in original matrix.  used to test for zeros
    protected double maxValue;

    // matrix's size
    protected int N;

    // used to compute eigenvalues directly
    protected EigenvalueSmall eigenSmall = new EigenvalueSmall();

    // how many exception shifts has it performed
    protected int numExceptional;
    // the step number of the last exception shift
    protected int nextExceptional;

    // diagonal elements in the matrix
    protected double diag[];
    // the off diagonal elements
    protected double off[];
    // value of the bulge
    double bulge;

    // the submatrix its working on
    protected int x1;
    protected int x2;

    // how many cycles has it run through looking for the current singular value
    int steps;

    // where splits are performed
    protected int splits[];
    protected int numSplits;

    // After this many iterations it will perform an exceptional
    private int exceptionalThresh = 15;
    private int maxIterations = exceptionalThresh*100;

    // should the steps use a sequence of predefined lambdas?
    boolean followScript;

    // --------- variables for scripted step
    // if following a sequence of steps, this is the point at which it decides its
    // going no where and needs to use a different step
    private static final int giveUpOnKnown = 10;
    private double values[];

    //can it compute singularvalues directly
    private boolean fastValues = false;

    // if not in scripted mode is it looking for new zeros first?
    private boolean findingZeros;

    double c,s;

    // for debugging
//    SimpleMatrix B;

    public SvdImplicitQrAlgorithm( boolean fastValues ) {
        this.fastValues = fastValues;
    }

    public SvdImplicitQrAlgorithm() {

    }

    public DenseMatrix64F getUt() {
        return Ut;
    }

    public void setUt(DenseMatrix64F ut) {
        Ut = ut;
    }

    public DenseMatrix64F getVt() {
        return Vt;
    }

    public void setVt(DenseMatrix64F vt) {
        Vt = vt;
    }

    /**
     *
     */
    public void setMatrix( int numRows , int numCols, double diag[], double off[] ) {
        initParam(numRows,numCols);
        this.diag = diag;
        this.off = off;

        maxValue = Math.abs(diag[0]);
        for( int i = 1; i < N; i++ ) {
            double a = Math.abs(diag[i]);
            double b = Math.abs(off[i-1]);

            if( a > maxValue ) {
                maxValue = Math.abs(a);
            }
            if( b > maxValue ) {
                maxValue = Math.abs(b);
            }
        }
    }

    public double[] swapDiag( double diag[] ) {
        double[] ret = this.diag;
        this.diag = diag;
        return ret;
    }

    public double[] swapOff( double off[] ) {
        double[] ret = this.off;
        this.off = off;
        return ret;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public void initParam( int M , int N ) {
        if( N > M )
            throw new RuntimeException("Must be a square or tall matrix");

        this.N = N;

        if( splits == null || splits.length < N ) {
            splits = new int[N];
        }

        x1 = 0;
        x2 = this.N-1;

        steps = 0;
        totalSteps = 0;
        numSplits = 0;
        numExceptional = 0;
        nextExceptional = exceptionalThresh;
    }

    public boolean process() {
        this.followScript = false;
        findingZeros = true;

        return _process();
    }

    /**
     * Perform a sequence of steps based off of the singular values provided.
     *
     * @param values
     * @return
     */
    public boolean process(double values[] ) {
        this.followScript = true;
        this.values = values;
        this.findingZeros = false;

        return _process();
    }

    public boolean _process() {
        // it is a zero matrix
        if( maxValue == 0 )
            return true;
        while( x2 >= 0 ) {
            // if it has cycled too many times give up
            if( steps > maxIterations ) {
                return false;
            }

            if( x1 == x2 ) {
//                System.out.println("steps = "+steps+"  script = "+followScript+" at "+x1);
//                System.out.println("Split");
                // see if it is done processing this submatrix
                resetSteps();
                if( !nextSplit() )
                    break;
            } else if( fastValues && x2-x1 == 1 ) {
                // There are analytical solutions to this case. Just compute them directly.
                resetSteps();
                eigenBB_2x2(x1);
                setSubmatrix(x2,x2);
            } else if( steps >= nextExceptional ){
                exceptionShift();
            } else {
                // perform a step
                if (!checkForAndHandleZeros()) {
                    if( followScript ) {
                        performScriptedStep();
                    } else {
                        performDynamicStep();
                    }
                }
            }

//            printMatrix();
        }

        return true;
    }

    /**
     * Here the lambda in the implicit step is determined dynamically.  At first
     * it selects zeros to quickly reveal singular values that are zero or close to zero.
     * Then it computes it using a Wilkinson shift.
     */
    private void performDynamicStep() {
        // initially look for singular values of zero
        if( findingZeros ) {
            if( steps > 6 ) {
                findingZeros = false;
            } else {
                double scale = computeBulgeScale();
                performImplicitSingleStep(scale,0,false);
            }
        } else {
            // For very large and very small numbers the only way to prevent overflow/underflow
            // is to have a common scale between the wilkinson shift and the implicit single step
            // What happens if you don't is that when the wilkinson shift returns the value it
            // computed it multiplies it by the scale twice, which will cause an overflow
            double scale = computeBulgeScale();
            // use the wilkinson shift to perform a step
            double lambda = selectWilkinsonShift(scale);

            performImplicitSingleStep(scale,lambda,false);
        }
    }

    /**
     * Shifts are performed based upon singular values computed previously.  If it does not converge
     * using one of those singular values it uses a Wilkinson shift instead.
     */
    private void performScriptedStep() {
        double scale = computeBulgeScale();
        if( steps > giveUpOnKnown ) {
            // give up on the script
            followScript = false;
        } else {
            // use previous singular value to step
            double s = values[x2]/scale;
            performImplicitSingleStep(scale,s*s,false);
        }
    }

    public void incrementSteps() {
        steps++;
        totalSteps++;
    }

    public boolean isOffZero(int i) {
        double bottom = Math.abs(diag[i])+Math.abs(diag[i+1]);

        return Math.abs(off[i]) <= bottom* UtilEjml.EPS;
    }

    public boolean isDiagonalZero(int i) {
//        return Math.abs(diag[i]) <= maxValue* UtilEjml.EPS;

        double bottom = Math.abs(diag[i+1])+Math.abs(off[i]);

        return Math.abs(diag[i]) <= bottom* UtilEjml.EPS;
    }

    public void resetSteps() {
        steps = 0;
        nextExceptional = exceptionalThresh;
        numExceptional = 0;
    }

    /**
     * Tells it to process the submatrix at the next split.  Should be called after the
     * current submatrix has been processed.
     */
    public boolean nextSplit() {
        if( numSplits == 0 )
            return false;
        x2 = splits[--numSplits];
        if( numSplits > 0 )
            x1 = splits[numSplits-1]+1;
        else
            x1 = 0;

        return true;
    }

    /**
     * Given the lambda value perform an implicit QR step on the matrix.
     *
     * B^T*B-lambda*I
     *
     * @param lambda Stepping factor.
     */
    public void performImplicitSingleStep(double scale , double lambda , boolean byAngle) {
        createBulge(x1,lambda,scale,byAngle);

        for( int i = x1; i < x2-1 && bulge != 0.0; i++ ) {
            removeBulgeLeft(i,true);
            if( bulge == 0 )
                break;
            removeBulgeRight(i);
        }

        if( bulge != 0 )
            removeBulgeLeft(x2-1,false);

        incrementSteps();
    }

    /**
     * Multiplied a transpose orthogonal matrix Q by the specified rotator.  This is used
     * to update the U and V matrices.  Updating the transpose of the matrix is faster
     * since it only modifies the rows.
     *
     *
     * @param Q Orthogonal matrix
     * @param m Coordinate of rotator.
     * @param n Coordinate of rotator.
     * @param c cosine of rotator.
     * @param s sine of rotator.
     */
    protected void updateRotator( DenseMatrix64F Q , int m, int n, double c, double s) {
        int rowA = m*Q.numCols;
        int rowB = n*Q.numCols;

//        for( int i = 0; i < Q.numCols; i++ ) {
//            double a = Q.get(rowA+i);
//            double b = Q.get(rowB+i);
//            Q.set( rowA+i, c*a + s*b);
//            Q.set( rowB+i, -s*a + c*b);
//        }
//        System.out.println("------ AFter Update Rotator "+m+" "+n);
//        Q.print();
//        System.out.println();
        int endA = rowA + Q.numCols;
        for( ; rowA != endA; rowA++ , rowB++ ) {
            double a = Q.get(rowA);
            double b = Q.get(rowB);
            Q.set(rowA, c*a + s*b);
            Q.set(rowB, -s*a + c*b);
        }
    }

    private double computeBulgeScale() {
        double b11 = diag[x1];
        double b12 = off[x1];

        return Math.max( Math.abs(b11) , Math.abs(b12));
//
//        double b22 = diag[x1+1];
//
//        double scale = Math.max( Math.abs(b11) , Math.abs(b12));
//
//        return Math.max(scale,Math.abs(b22));
    }

    /**
     * Performs a similar transform on B<sup>T</sup>B-pI
     */
    protected void createBulge( int x1 , double p , double scale , boolean byAngle ) {
        double b11 = diag[x1];
        double b12 = off[x1];
        double b22 = diag[x1+1];

        if( byAngle ) {
            c = Math.cos(p);
            s = Math.sin(p);
        } else {
            // normalize to improve resistance to overflow/underflow
            double u1 = (b11/scale)*(b11/scale)-p;
            double u2 = (b12/scale)*(b11/scale);

            double gamma = Math.sqrt(u1*u1 + u2*u2);

            c = u1/gamma;
            s = u2/gamma;
        }

        // multiply the rotator on the top left.
        diag[x1] = b11*c + b12*s;
        off[x1] = b12*c - b11*s;
        diag[x1+1] = b22*c;
        bulge = b22*s;

//        SimpleMatrix Q = createQ(x1, c, s, false);
//        B=B.mult(Q);
//
//        B.print();
//        printMatrix();
//        System.out.println("  bulge = "+bulge);

        if( Vt != null ) {
            updateRotator(Vt,x1,x1+1,c,s);

//            SimpleMatrix.wrap(Ut).mult(B).mult(SimpleMatrix.wrap(Vt).transpose()).print();
//            printMatrix();
//            System.out.println("bulge = "+bulge);
//            System.out.println();
        }
    }

    /**
     * Computes a rotator that will set run to zero (?)
     */
    protected void computeRotator( double rise , double run )
    {
//        double gamma = Math.sqrt(rise*rise + run*run);
//
//        c = rise/gamma;
//        s = run/gamma;

        // See page 384 of Fundamentals of Matrix Computations 2nd
          if( Math.abs(rise) < Math.abs(run)) {
              double k = rise/run;

              double bottom = Math.sqrt(1.0d+k*k);
              s = 1.0/bottom;
              c = k/bottom;
          } else {
              double t = run/rise;
              double bottom = Math.sqrt(1.0d + t*t);
              c = 1.0/bottom;
              s = t/bottom;
          }
    }

    protected void removeBulgeLeft( int x1 , boolean notLast ) {
        double b11 = diag[x1];
        double b12 = off[x1];
        double b22 = diag[x1+1];

        computeRotator(b11,bulge);

        // apply rotator on the left
        diag[x1] = c*b11 + s*bulge;
        off[x1] = c*b12 + s*b22;
        diag[x1+1] = c*b22-s*b12;

        if( notLast ) {
            double b23 = off[x1+1];
            bulge = s*b23;
            off[x1+1] = c*b23;
        }

//        SimpleMatrix Q = createQ(x1, c, s, true);
//        B=Q.mult(B);
//
//        B.print();
//        printMatrix();
//        System.out.println("  bulge = "+bulge);

        if( Ut != null ) {
            updateRotator(Ut,x1,x1+1,c,s);

//            SimpleMatrix.wrap(Ut).mult(B).mult(SimpleMatrix.wrap(Vt).transpose()).print();
//            printMatrix();
//            System.out.println("bulge = "+bulge);
//            System.out.println();
        }
    }

    protected void removeBulgeRight( int x1 ) {
        double b12 = off[x1];
        double b22 = diag[x1+1];
        double b23 = off[x1+1];

        computeRotator(b12,bulge);

        // apply rotator on the right
        off[x1] = b12*c + bulge*s;
        diag[x1+1] = b22*c + b23*s;
        off[x1+1] = -b22*s + b23*c;

        double b33 = diag[x1+2];
        diag[x1+2] = b33*c;
        bulge = b33*s;

//        SimpleMatrix Q = createQ(x1+1, c, s, false);
//        B=B.mult(Q);
//
//        B.print();
//        printMatrix();
//        System.out.println("  bulge = "+bulge);

        if( Vt != null ) {
            updateRotator(Vt,x1+1,x1+2,c,s);

//            SimpleMatrix.wrap(Ut).mult(B).mult(SimpleMatrix.wrap(Vt).transpose()).print();
//            printMatrix();
//            System.out.println("bulge = "+bulge);
//            System.out.println();
        }
    }


    public void setSubmatrix(int x1, int x2) {
        this.x1 = x1;
        this.x2 = x2;
    }

    /**
     * Selects the Wilkinson's shift for B<sup>T</sup>B.  See page 410.  It is guaranteed to converge
     * and converges fast in practice.
     *
     * @param scale Scale factor used to help prevent overflow/underflow
     * @return Shifting factor lambda/(scale*scale)
     */
    public double selectWilkinsonShift( double scale ) {

        double a11,a22;

        if( x2-x1 > 1 ) {
            double d1 = diag[x2-1] / scale;
            double o1 = off[x2-2] / scale;
            double d2 = diag[x2] / scale;
            double o2 = off[x2-1] / scale;

            a11 = o1*o1 + d1*d1;
            a22 = o2*o2 + d2*d2;

            eigenSmall.symm2x2_fast(a11 , o2*d1 , a22);
        } else {
            double a = diag[x2-1]/scale;
            double b = off[x2-1]/scale;
            double c = diag[x2]/scale;

            a11 = a*a;
            a22 = b*b + c*c;

            eigenSmall.symm2x2_fast(a11, a*b , a22);
        }

        // return the eigenvalue closest to a22
        double diff0 = Math.abs(eigenSmall.value0.real-a22);
        double diff1 = Math.abs(eigenSmall.value1.real-a22);

        return diff0 < diff1 ? eigenSmall.value0.real :  eigenSmall.value1.real;
    }

    /**
     * Computes the eigenvalue of the 2 by 2 matrix B<sup>T</sup>B
     */
    protected void eigenBB_2x2( int x1 ) {
        double b11 = diag[x1];
        double b12 = off[x1];
        double b22 = diag[x1+1];

        // normalize to reduce overflow
        double absA = Math.abs(b11);
        double absB = Math.abs(b12);
        double absC = Math.abs(b22);

        double scale = absA > absB ? absA : absB;
        if( absC > scale ) scale = absC;

        // see if it is a pathological case.  the diagonal must already be zero
        // and the eigenvalues are all zero.  so just return
        if( scale == 0 )
            return;

        b11 /= scale;
        b12 /= scale;
        b22 /= scale;

        eigenSmall.symm2x2_fast(b11*b11, b11*b12 , b12*b12+b22*b22);

        off[x1] = 0;
        diag[x1] = scale*Math.sqrt(eigenSmall.value0.real);
        double sgn = Math.signum(eigenSmall.value1.real);
        diag[x1+1] = sgn*scale*Math.sqrt(Math.abs(eigenSmall.value1.real));

    }


    /**
     * Checks to see if either the diagonal element or off diagonal element is zero.  If one is
     * then it performs a split or pushes it off the matrix.
     *
     * @return True if there was a zero.
     */
    protected boolean checkForAndHandleZeros() {
        // check for zeros along off diagonal
        for( int i = x2-1; i >= x1; i-- ) {
            if( isOffZero(i) ) {
//                System.out.println("steps at split = "+steps);
                resetSteps();
                splits[numSplits++] = i;
                x1 = i+1;
                return true;
            }
        }

        // check for zeros along diagonal
        for( int i = x2-1; i >= x1; i-- ) {
            if( isDiagonalZero(i)) {
//                System.out.println("steps at split = "+steps);
                pushRight(i);
                resetSteps();
                splits[numSplits++] = i;
                x1 = i+1;
                return true;
            }
        }
        return false;
    }

    /**
     * If there is a zero on the diagonal element, the off diagonal element needs pushed
     * off so that all the algorithms assumptions are two and so that it can split the matrix.
     */
    private void pushRight( int row ) {
        if( isOffZero(row))
            return;

//        B = createB();
//        B.print();
        rotatorPushRight(row);
        int end = N-2-row;
        for( int i = 0; i < end && bulge != 0; i++ ) {
            rotatorPushRight2(row,i+2);
        }
//        }
    }

    /**
     * Start pushing the element off to the right.
     */
    private void rotatorPushRight( int m )
    {
        double b11 = off[m];
        double b21 = diag[m+1];

        computeRotator(b21,-b11);

        // apply rotator on the right
        off[m] = 0;
        diag[m+1] = b21*c-b11*s;

        if( m+2 < N) {
            double b22 = off[m+1];
            off[m+1] = b22*c;
            bulge =  b22*s;
        }  else {
            bulge = 0;
        }

//        SimpleMatrix Q = createQ(m,m+1, c, s, true);
//        B=Q.mult(B);
//
//        B.print();
//        printMatrix();
//        System.out.println("  bulge = "+bulge);
//        System.out.println();

        if( Ut != null ) {
            updateRotator(Ut,m,m+1,c,s);

//            SimpleMatrix.wrap(Ut).mult(B).mult(SimpleMatrix.wrap(Vt).transpose()).print();
//            printMatrix();
//            System.out.println("bulge = "+bulge);
//            System.out.println();
        }
    }

    /**
     * Used to finish up pushing the bulge off the matrix.
     */
    private void rotatorPushRight2( int m , int offset)
    {
        double b11 = bulge;
        double b12 = diag[m+offset];

        computeRotator(b12,-b11);

        diag[m+offset] = b12*c-b11*s;

        if( m+offset<N-1) {
            double b22 = off[m+offset];
            off[m+offset] = b22*c;
            bulge = b22*s;
        }

//        SimpleMatrix Q = createQ(m,m+offset, c, s, true);
//        B=Q.mult(B);
//
//        B.print();
//        printMatrix();
//        System.out.println("  bulge = "+bulge);
//        System.out.println();

        if( Ut != null ) {
            updateRotator(Ut,m,m+offset,c,s);

//            SimpleMatrix.wrap(Ut).mult(B).mult(SimpleMatrix.wrap(Vt).transpose()).print();
//            printMatrix();
//            System.out.println("bulge = "+bulge);
//            System.out.println();
        }
    }

    /**
     * It is possible for the QR algorithm to get stuck in a loop because of symmetries.  This happens
     * more often with larger matrices.  By taking a random step it can break the symmetry and finish.
     */
    public void exceptionShift() {
        numExceptional++;
        double mag = 0.05 * numExceptional;
        if (mag > 1.0) mag = 1.0;

        double angle = 2.0 * Math.PI * (rand.nextDouble() - 0.5) * mag;
        performImplicitSingleStep(0, angle, true);

        // allow more convergence time
        nextExceptional = steps + exceptionalThresh;  // (numExceptional+1)*
    }

    public void printMatrix() {
        System.out.print("Off Diag[ ");
        for( int j = 0; j < N-1; j++ ) {
            System.out.printf("%5.2f ",off[j]);
        }
        System.out.println();
        System.out.print("    Diag[ ");
        for( int j = 0; j < N; j++ ) {
            System.out.printf("%5.2f ",diag[j]);
        }
        System.out.println();
    }


    public int getNumberOfSingularValues() {
        return N;
    }

    public double getSingularValue( int index ) {
        return diag[index];
    }

    public void setFastValues(boolean b) {
        fastValues = b;
    }


    public double[] getSingularValues() {
        return diag;
    }

    public double[] getDiag() {
        return diag;
    }

    public double[] getOff() {
        return off;
    }

    public double getMaxValue() {
        return maxValue;
    }
}
