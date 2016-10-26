/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.eig;

import org.ejml.UtilEjml;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.Eigenpair64F;
import org.ejml.interfaces.decomposition.EigenDecomposition;
import org.ejml.ops.*;
import org.ejml.simple.SimpleMatrix;

import java.util.Random;

import static org.ejml.alg.dense.decomposition.CheckDecompositionInterface.safeDecomposition;
import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public abstract class GeneralEigenDecompositionCheck {

    Random rand = new Random(895723);

    public abstract EigenDecomposition createDecomposition();

    boolean computeVectors;

    public void allTests() {
        computeVectors = true;

        checkSizeZero();
        checkRandom();
        checkKnownReal();
        checkKnownComplex();
        checkCompanionMatrix();
        checkRandomSymmetric();
        checkExceptional();
        checkIdentity();
        checkAllZeros();
        rand = new Random(2934);
        checkWithSomeRepeatedValuesSymm();
        checkWithSingularSymm();
        checkSmallValue(false);
        checkSmallValue(true);
        checkLargeValue(false);
        checkLargeValue(true);

        checkFailure0();
    }

    /**
     * Tests for when it just computes eigenvalues
     */
    public void justEigenValues() {
        computeVectors = false;

        checkKnownReal_JustValue();
        checkKnownSymmetric_JustValue();
        checkCompanionMatrix();
    }

    public void checkSizeZero() {
        EigenDecomposition alg = createDecomposition();

        assertFalse(alg.decompose(new DenseMatrix64F(0,0)));
    }

    /**
     * Create a variety of different random matrices of different sizes and sees if they pass the standard
     * eigen decompositions tests.
     */
    public void checkRandom() {
        int sizes[] = new int[]{1,2,5,10,20,50,100,200};

        EigenDecomposition alg = createDecomposition();

        for( int s = 2; s < sizes.length; s++ ) {
            int N = sizes[s];
//            System.out.println("N = "+N);

            for( int i = 0; i < 2; i++ ) {
                DenseMatrix64F A = RandomMatrices.createRandom(N,N,-1,1,rand);

                assertTrue(safeDecomposition(alg,A));

                performStandardTests(alg,A,-1);
            }
        }
    }

    /**
     * Compare results against a simple matrix with known results where all the eigenvalues
     * are real.  Octave was used to test the known values.
     */
    public void checkKnownReal() {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 0.907265, 0.832472, 0.255310, 0.667810, 0.871323, 0.612657, 0.025059, 0.126475, 0.427002);

        EigenDecomposition alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));
        performStandardTests(alg,A,-1);

        testForEigenpair(alg,1.686542,0,-0.739990,-0.667630,-0.081761);
        testForEigenpair(alg,0.079014,0,-0.658665,0.721163,-0.214673);
        testForEigenpair(alg,0.440034,0,-0.731422,0.211711,0.648229);
    }

    /**
     * Found to be a stressing case that broke a version of the general EVD algorithm.  It is a companion matrix
     * for a polynomial used to find the zeros.
     *
     * Discovered by exratt@googlema*l.com
     */
    public void checkCompanionMatrix() {
//        double[] polynomial = {
//                5.392104631674957e7,
//                -7.717841412372049e8,
//                -1.4998803087543774e7,
//                -30110.074181432814,
//                -16.0
//        };
//
////        double polynomial[] = new double[]{
////                0.0817011296749115,
////                -0.8100357949733734,
////                -0.8667608685791492,
////                2.2995666563510895,
////                0.8879469335079193,
////                -4.16266793012619,
////                -1.527034044265747,
////                2.201415002346039,
////                0.5391231775283813,
////                -0.41334158182144165};
//
//        // build companion matrix
//        int n = polynomial.length - 1;
//        DenseMatrix64F companion = new DenseMatrix64F(n, n);
//        for (int i = 0; i < n; i++) {
//            companion.set(i, n - 1, -polynomial[i] / polynomial[n]);
//        }
//        for (int i = 1; i < n; i++) {
//            companion.set(i, i - 1, 1);
//        }
//
//        // the eigenvalues of the companion matrix matches the roots of the polynomial
//        EigenDecomposition alg = createDecomposition();
//        assertTrue(safeDecomposition(alg,companion));
//
//        // see if the roots are zero
//        for( int i = 0; i < alg.getNumberOfEigenvalues(); i++ ) {
//            Complex64F c = alg.getEigenvalue(i);
//
//            if( !c.isReal() ) {
//                continue;
//            }
//
//            double total = 0;
//            for( int j = 0; j < polynomial.length; j++ ) {
//                total += polynomial[j]*Math.pow(c.real,j);
//            }
//
//            assertEquals(0,total,1e-12);
//        }
//
//
//        performStandardTests(alg,companion,n);
    }

    /**
     * Sees if it correctly computed the eigenvalues.  Does not check eigenvectors.
     */
    public void checkKnownReal_JustValue() {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 0.907265, 0.832472, 0.255310, 0.667810, 0.871323, 0.612657, 0.025059, 0.126475, 0.427002);

        EigenDecomposition alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));

        testForEigenvalue(alg,A,1.686542,0,1);
        testForEigenvalue(alg,A,0.079014,0,1);
        testForEigenvalue(alg,A,0.440034,0,1);
    }

    /**
     * Sees if it correctly computed the eigenvalues.  Does not check eigenvectors.
     */
    public void checkKnownSymmetric_JustValue() {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true,
                0.98139,   0.78650,   0.78564,
                0.78650,   1.03207,   0.29794,
                0.78564,   0.29794,   0.91926);
        EigenDecomposition alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));

        testForEigenvalue(alg,A,0.00426,0,1);
        testForEigenvalue(alg,A,0.67856,0,1);
        testForEigenvalue(alg,A,2.24989,0,1);
    }

    /**
     * Compare results against a simple matrix with known results where some the eigenvalues
     * are real and some are complex.
     */
    public void checkKnownComplex() {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, -0.418284, 0.279875, 0.452912, -0.093748, -0.045179, 0.310949, 0.250513, -0.304077, -0.031414);

        EigenDecomposition alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));
        performStandardTests(alg,A,-1);

        testForEigenpair(alg,-0.39996,0,0.87010,0.43425,-0.23314);
        testForEigenpair(alg,-0.04746,0.02391);
        testForEigenpair(alg,-0.04746,-0.02391);
    }

    /**
     * Check results against symmetric matrices that are randomly generated
     */
    public void checkRandomSymmetric() {
        for( int N = 1; N <= 15; N++ ) {
            for( int i = 0; i < 20; i++ ) {
                DenseMatrix64F A = RandomMatrices.createSymmetric(N,-1,1,rand);

                EigenDecomposition alg = createDecomposition();

                assertTrue(safeDecomposition(alg,A));

                performStandardTests(alg,A,N);
            }
        }
    }

    /**
     * For some eigenvector algorithms this is a difficult matrix that requires a special
     * check for.  If it fails that check it will either loop forever or exit before converging.
     */
    public void checkExceptional() {
        DenseMatrix64F A = new DenseMatrix64F(5,5, true, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0);

        EigenDecomposition alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));

        performStandardTests(alg,A,1);
    }

    public void checkIdentity() {
        DenseMatrix64F I = CommonOps.identity(4);

        EigenDecomposition alg = createDecomposition();

        assertTrue(safeDecomposition(alg,I));

        performStandardTests(alg,I,4);

        testForEigenpair(alg,1,0,1,0,0,0);
        testForEigenpair(alg,1,0,0,1,0,0);
        testForEigenpair(alg,1,0,0,0,1,0);
        testForEigenpair(alg,1,0,0,0,0,1);
    }

    public void checkAllZeros() {
        DenseMatrix64F A = new DenseMatrix64F(5,5);

        EigenDecomposition alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));

        performStandardTests(alg,A,5);
        testEigenvalues(alg,0);
    }

    public void checkWithSomeRepeatedValuesSymm() {
        EigenDecomposition alg = createDecomposition();

        checkSymmetricMatrix(alg,2,-3,-3,-3);
        checkSymmetricMatrix(alg,2,-3,2,2);
        checkSymmetricMatrix(alg,1,1,1,2);
    }

    public void checkWithSingularSymm() {

        EigenDecomposition alg = createDecomposition();

        checkSymmetricMatrix(alg,1,0,1,2);
    }

    /**
     * Creates a random symmetric matrix with the specified eigenvalues.  It then
     * checks to see if it has the expected results.
     */
    private void checkSymmetricMatrix(EigenDecomposition alg , double ...ev ) {
        int numRepeated[] = new int[ ev.length ];

        for( int i = 0; i < ev.length ; i++ ) {
            int num = 0;

            for (double anEv : ev) {
                if (ev[i] == anEv)
                    num++;
            }
            numRepeated[i] = num;
        }

        for( int i = 0; i < 200; i++ ) {
            DenseMatrix64F A = RandomMatrices.createEigenvaluesSymm(ev.length,rand,ev);

            assertTrue(safeDecomposition(alg,A));

            performStandardTests(alg,A,ev.length);

            for( int j = 0; j < ev.length; j++ ) {
                testForEigenvalue(alg,A,ev[j],0,numRepeated[j]);
            }
        }
    }

    public void checkSmallValue( boolean symmetric) {

//        System.out.println("Symmetric = "+symmetric);
        EigenDecomposition alg = createDecomposition();

        for( int i = 0; i < 20; i++ ) {
            DenseMatrix64F A = symmetric ?
                    RandomMatrices.createSymmetric(4,-1,1,rand) :
                    RandomMatrices.createRandom(4,4,-1,1,rand);

            CommonOps.scale(1e-200,A);

            assertTrue(safeDecomposition(alg,A));

//        A.print("%15.13e");

            performStandardTests(alg,A,-1);
        }
    }

    public void checkLargeValue( boolean symmetric) {

        EigenDecomposition alg = createDecomposition();

        for( int i = 0; i < 20; i++ ) {
            DenseMatrix64F A = symmetric ?
                    RandomMatrices.createSymmetric(4,-1,1,rand) :
                    RandomMatrices.createRandom(4,4,-1,1,rand);

            CommonOps.scale(1e100,A);

            assertTrue(safeDecomposition(alg,A));

            performStandardTests(alg,A,-1);
        }
    }

    /**
     * If the eigenvalues are all known, real, and the same this can be used to check them.
     */
    public void testEigenvalues( EigenDecomposition alg , double expected ) {

        for( int i = 0; i < alg.getNumberOfEigenvalues(); i++ ) {
            Complex64F c = alg.getEigenvalue(i);

            assertTrue(c.isReal());

            assertEquals(expected,c.real,1e-8);
        }
    }

    /**
     * Preforms standard tests that can be performed on any decomposition without prior knowledge of
     * what the results should be.
     */
    public void performStandardTests( EigenDecomposition alg , DenseMatrix64F A , int numReal )
    {

        // basic sanity tests
        assertEquals(A.numRows,alg.getNumberOfEigenvalues());

        if( numReal >= 0 ) {
            for( int i = 0; i < A.numRows; i++ ) {
                Complex64F v = alg.getEigenvalue(i);

                assertFalse( Double.isNaN(v.getReal() ));
                if( v.isReal() )
                    numReal--;
                else if( Math.abs(v.getImaginary()) < 10*UtilEjml.EPS)
                    numReal--;
            }

            // if there are more than the expected number of real eigenvalues this will
            // be negative
            assertEquals(0,numReal);
        }

//        checkCharacteristicEquation(alg,A);
        if( computeVectors ) {
            testPairsConsistent(alg,A);
            testVectorsLinearlyIndependent(alg);
        } else {
            testEigenvalueConsistency(alg,A);
        }
    }

    /**
     * A failure condition that was found in the wild.
     *
     * Found by user343 on github
     */
    public void checkFailure0() {
        double[][] matrix = new double[][] {
                {1, 0, 0},
                {0.01, 0, -1},
                {0.01, 1, 0}};
        DenseMatrix64F A = new DenseMatrix64F(matrix);

        EigenDecomposition alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));

        performStandardTests(alg,A,1);
    }

    /**
     * Checks to see if an eigenvalue is complex then the eigenvector is null.  If it is real it
     * then checks to see if the equation A*v = lambda*v holds true.
     */
    public void testPairsConsistent( EigenDecomposition<DenseMatrix64F> alg , DenseMatrix64F A )
    {
//        System.out.println("-------------------------------------------------------------------------");
        int N = alg.getNumberOfEigenvalues();

        DenseMatrix64F tempA = new DenseMatrix64F(N,1);
        DenseMatrix64F tempB = new DenseMatrix64F(N,1);
        
        for( int i = 0; i < N; i++ ) {
            Complex64F c = alg.getEigenvalue(i);
            DenseMatrix64F v = alg.getEigenVector(i);

            if( Double.isInfinite(c.real) || Double.isNaN(c.real) ||
                    Double.isInfinite(c.imaginary) || Double.isNaN(c.imaginary))
                fail("Uncountable eigenvalue");

            if( !c.isReal() ) {
                assertTrue(v==null);
            } else {
                assertTrue(v != null );
//                if( MatrixFeatures.hasUncountable(v)) {
//                    throw new RuntimeException("Egads");
//                }
                assertFalse(MatrixFeatures.hasUncountable(v));

                CommonOps.mult(A,v,tempA);
                CommonOps.scale(c.real,v,tempB);

                double max = NormOps.normPInf(A);
                if( max == 0 ) max = 1;

                double error = SpecializedOps.diffNormF(tempA,tempB)/max;

                if( error > 1e-12 ) {
                    System.out.println("Original matrix:");
                    A.print();
                    System.out.println("Eigenvalue = "+c.real);
                    Eigenpair64F p = EigenOps.computeEigenVector(A,c.real);
                    p.vector.print();
                    v.print();


                    CommonOps.mult(A,p.vector,tempA);
                    CommonOps.scale(c.real,p.vector,tempB);

                    max = NormOps.normPInf(A);

                    System.out.println("error before = "+error);
                    error = SpecializedOps.diffNormF(tempA,tempB)/max;
                    System.out.println("error after = "+error);
                    A.print("%f");
                    System.out.println();
                    fail("Error was too large");
                }

                assertTrue(error <= 1e-12);
            }
        }
    }

    /**
     * Takes a real eigenvalue and computes its eigenvector.  then sees if it is similar to the adjusted
     * eigenvalue
     */
    public void testEigenvalueConsistency( EigenDecomposition alg ,
                                           DenseMatrix64F A )
    {
        int N = alg.getNumberOfEigenvalues();

        DenseMatrix64F AV = new DenseMatrix64F(N,1);
        DenseMatrix64F LV = new DenseMatrix64F(N,1);

        for( int i = 0; i < N; i++ ) {
            Complex64F c = alg.getEigenvalue(i);

            if( c.isReal() ) {
                Eigenpair64F p = EigenOps.computeEigenVector(A,c.getReal());

                if( p != null ) {
                    CommonOps.mult(A,p.vector,AV);
                    CommonOps.scale(c.getReal(),p.vector,LV);
                    double error = SpecializedOps.diffNormF(AV,LV);
//                    System.out.println("error = "+error);
                    assertTrue(error<1e-12);
                }
            }
        }
    }

    /**
     * See if eigenvalues cause the characteristic equation to have a value of zero
     */
    public void checkCharacteristicEquation( EigenDecomposition alg ,
                                             DenseMatrix64F A ) {
        int N = alg.getNumberOfEigenvalues();

        SimpleMatrix a = SimpleMatrix.wrap(A);

        for( int i = 0; i < N; i++ ) {
            Complex64F c = alg.getEigenvalue(i);

            if( c.isReal() ) {
                // test using the characteristic equation
                double det = SimpleMatrix.identity(A.numCols).scale(c.real).minus(a).determinant();

                // extremely crude test.  given perfect data this is probably considered a failure...  However,
                // its hard to tell what a good test value actually is.
                assertEquals(0, det, 0.1);
            }
        }
    }

    /**
     * Checks to see if all the real eigenvectors are linearly independent of each other.
     */
    public void testVectorsLinearlyIndependent( EigenDecomposition<DenseMatrix64F> alg ) {
        int N = alg.getNumberOfEigenvalues();

        // create a matrix out of the eigenvectors
        DenseMatrix64F A = new DenseMatrix64F(N,N);

        int off = 0;
        for( int i = 0; i < N; i++ ) {
            DenseMatrix64F v = alg.getEigenVector(i);

            // it can only handle real eigenvectors
            if( v == null )
                off++;
            else {
                for( int j = 0; j < N; j++ ) {
                    A.set(i-off,j,v.get(j));
                }
            }
        }

        // see if there are any real eigenvectors
        if( N == off )
            return;

        A.reshape(N-off,N, false);

        assertTrue(MatrixFeatures.isRowsLinearIndependent(A));
    }

    /**
     * Sees if the pair of eigenvalue and eigenvector was found in the decomposition.
     */
    public void testForEigenpair( EigenDecomposition<DenseMatrix64F> alg , double valueReal ,
                                  double valueImg , double... vector )
    {
        int N = alg.getNumberOfEigenvalues();

        int numMatched = 0;
        for( int i = 0; i < N; i++ ) {
            Complex64F c = alg.getEigenvalue(i);

            if( Math.abs(c.real-valueReal) < 1e-4 && Math.abs(c.imaginary-valueImg) < 1e-4) {

                if( c.isReal() ) {
                    if( vector.length > 0 ) {
                        DenseMatrix64F v = alg.getEigenVector(i);
                        DenseMatrix64F e = new DenseMatrix64F(N,1, true, vector);

                        double error = SpecializedOps.diffNormF(e,v);
                        CommonOps.changeSign(e);
                        double error2 = SpecializedOps.diffNormF(e,v);


                        if(error < 1e-3 || error2 < 1e-3)
                            numMatched++;
                    } else {
                        numMatched++;
                    }
                } else if( !c.isReal() ) {
                    numMatched++;
                }
            }
        }

        assertEquals(1,numMatched);
    }

    public void testForEigenvalue( EigenDecomposition alg ,
                                   DenseMatrix64F A,
                                   double valueReal ,
                                   double valueImg , int numMatched )
    {
        int N = alg.getNumberOfEigenvalues();

        int numFound = 0;
        for( int i = 0; i < N; i++ ) {
            Complex64F c = alg.getEigenvalue(i);

            if( Math.abs(c.real-valueReal) < 1e-4 && Math.abs(c.imaginary-valueImg) < 1e-4) {
                numFound++;
            }
        }

        assertEquals(numMatched,numFound);
    }
}
