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

package org.ejml.alg.dense.decomposition.svd.implicitqr;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.eig.EigenvalueSmall;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.SimpleMatrix;

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
// TODO would this be faster if diag and off diag were interpolated into a single array?
// TODO print out all the steps again.  I think there might be some cancelations
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
    private int maxIterations = exceptionalThresh*15;

    // should the steps use a sequence of predefined lambdas?
    boolean followScript;

    // --------- variables for scripted step
    // if following a sequence of steps, this is the point at which it decides its
    // going no where and needs to use a different step
    private int giveUpOnKnown = 10;
    private double values[];

    //can it compute singularvalues directly
    private boolean fastValues = false;

    // if not in scripted mode is it looking for new zeros first?
    private boolean findingZeros;

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
     * @param A An upper bidiagonal matrix.
     */
    public void setMatrix( DenseMatrix64F A ) {
//        System.out.println("Bidiagonal input matrix");
//        A.print();
        initParam(A.numRows,A.numCols);
//        B = new SimpleMatrix(A);
//        B.print();

        maxValue = diag[0] = A.data[0];
        maxValue = Math.abs(maxValue);
        for( int i = 1; i < N; i++ ) {
            double a = diag[i] = A.data[i*A.numCols+i];
            double b = off[i-1] = A.get(i-1,i);

            if( Math.abs(a) > maxValue ) {
                maxValue = Math.abs(a);
            }
            if( Math.abs(b) > maxValue ) {
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

        if( diag == null || diag.length < N ) {
            diag = new double[N];
            off = new double[N-1];
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

        return Math.abs(off[i]) <= 0.5*bottom* UtilEjml.EPS;
    }

    public boolean isDiagonalZero(int i) {
        return Math.abs(diag[i]) <= maxValue* UtilEjml.EPS;
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

        for( int i = 0; i < Q.numCols; i++ ) {
            double a = Q.data[rowA+i];
            double b = Q.data[rowB+i];
            Q.data[rowA+i] = c*a + s*b;
            Q.data[rowB+i] = -s*a + c*b;
        }
//        System.out.println("------ AFter Update Rotator "+m+" "+n);
//        Q.print();
//        System.out.println();
//        int endA = rowA + Q.numCols;
//        for( ; rowA != endA; rowA++ , rowB++ ) {
//            double a = Q.data[rowA];
//            double b = Q.data[rowB];
//            Q.data[rowA] = c*a + s*b;
//            Q.data[rowB] = -s*a + c*b;
//        }
    }

    private double computeBulgeScale() {
        double b11 = diag[x1];
        double b12 = off[x1];

        return Math.max( Math.abs(b11) , Math.abs(b12));
    }

    /**
     * Performs a similar transform on B<sup>T</sup>B-pI
     */
    protected void createBulge( int x1 , double p , double scale , boolean byAngle ) {
        double b11 = diag[x1];
        double b12 = off[x1];
        double b22 = diag[x1+1];

        double c,s;
        if( byAngle ) {
            c = Math.cos(p);
            s = Math.sin(p);
        } else {
            // normalize to improve resistance to overflow/underflow
            double u1 = (b11/scale)*(b11/scale)-p;
            double u2 = (b12/scale)*(b11/scale);

            double alpha = Math.sqrt(u1*u1 + u2*u2);

            if( UtilEjml.isUncountable(alpha)) {
                System.out.println("crap");
            }

            c = u1 / alpha;
            s = u2 / alpha;
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

    protected void removeBulgeLeft( int x1 , boolean notLast ) {
        double b11 = diag[x1];
        double b12 = off[x1];
        double b22 = diag[x1+1];

        // normalize to improve resistance to overflow/underflow
        double abs11 = Math.abs(b11);
        double absBulge = Math.abs(bulge);

        // this function is only called if the bulge is not zero, thus scale cannot be zero
        double scale = absBulge > abs11 ? absBulge : abs11;

        abs11/=scale;
        absBulge/=scale;

        double gamma = scale*Math.sqrt(abs11*abs11+absBulge*absBulge);

        double c = b11/gamma;
        double s = bulge/gamma;

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

        // normalize to improve resistance to overflow/underflow
        double abs12 = Math.abs(b12);
        double absBulge = Math.abs(bulge);

        // this function is only called if the bulge is not zero, thus scale cannot be zero
        double scale = absBulge > abs12 ? absBulge : abs12;

        abs12/=scale;
        absBulge/=scale;

        double gamma = scale*Math.sqrt(abs12*abs12+absBulge*absBulge);

        double c = b12/gamma;
        double s = bulge/gamma;

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

        double check;

        if( x2-x1 > 1 ) {
            double d1 = diag[x2-1] / scale;
            double o1 = off[x2-2] / scale;
            double d2 = diag[x2] / scale;
            double o2 = off[x2-1] / scale;

            eigenSmall.symm2x2_fast(o1*o1 + d1*d1 , o2*d1 , o2*o2 + d2*d2);

            // the shift will be the eigenvalue that is closest to the value below
            check = o2*o2 + d2*d2;

            double diff0 = Math.abs(eigenSmall.value0.real-check);
            double diff1 = Math.abs(eigenSmall.value1.real-check);

            return diff0 < diff1 ? eigenSmall.value0.real :  eigenSmall.value1.real;
        } else {
            double a = diag[x2-1]/scale;
            double b = off[x2-1]/scale;
            double c = diag[x2]/scale;

            eigenSmall.symm2x2_fast(a*a + b*b , a*b , c*c);

            return eigenSmall.value0.getReal();
        }
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

        eigenSmall.symm2x2_fast(b11*b11,b11*b12,b12*b12+b22*b22);

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

        // normalize to improve resistance to overflow/underflow
        double abs11 = Math.abs(b11);
        double abs21 = Math.abs(b21);

        double scale = abs11 > abs21 ? abs11 : abs21;

        if( scale == 0.0 ) {
            // nothing to do since they are both zero
            bulge = 0;
            return;
        }

        abs11/=scale;
        abs21/=scale;

        double gamma = scale*Math.sqrt(abs11*abs11+abs21*abs21);

        double c = b21/gamma;
        double s = -b11/gamma;

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

        // normalize to improve resistance to overflow/underflow
        double abs11 = Math.abs(b11);
        double abs12 = Math.abs(b12);

        // the scale can never be zero since this is not called if the bulge is zero
        double scale = abs11 > abs12 ? abs11 : abs12;

        abs11/=scale;
        abs12/=scale;

        double gamma = scale*Math.sqrt(abs11*abs11+abs12*abs12);

        double c = b12/gamma;
        double s = -b11/gamma;

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
        double angle = Math.PI*(rand.nextDouble()-0.5)*0.05;
        performImplicitSingleStep(0,angle,true);

        numExceptional++;
        // allow more convergence time
        nextExceptional = steps+exceptionalThresh;  // (numExceptional+1)*
    }

    /**
     * Creates a Q matrix for debugging purposes.
     */
    private SimpleMatrix createQ(int x1, double c, double s , boolean transposed ) {
        return createQ(x1,x1+1,c,s,transposed);
    }

    /**
     * Creates a Q matrix for debugging purposes.
     */
    private SimpleMatrix createQ(int x1, int x2 , double c, double s , boolean transposed ) {
        SimpleMatrix Q = SimpleMatrix.identity(N);
        Q.set(x1,x1,c);
        if( transposed ) {
            Q.set(x1,x2,s);
            Q.set(x2,x1,-s);
        } else {
            Q.set(x1,x2,-s);
            Q.set(x2,x1,s);
        }
        Q.set(x2,x2,c);
        return Q;
    }

    private SimpleMatrix createB() {
        SimpleMatrix B = new SimpleMatrix(N,N);

        for( int i = 0; i < N-1; i++ ) {
            B.set(i,i,diag[i]);
            B.set(i,i+1,off[i]);
        }
        B.set(N-1,N-1,diag[N-1]);

        return B;
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
