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

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.Eigenpair_F64;
import org.ejml.dense.row.*;
import org.ejml.interfaces.decomposition.EigenDecomposition;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;
import org.ejml.simple.SimpleMatrix;

import java.util.Random;

import static org.ejml.dense.row.decomposition.CheckDecompositionInterface_DDRM.safeDecomposition;
import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public abstract class GeneralEigenDecompositionCheck_DDRM {

    Random rand = new Random(895723);

    public abstract EigenDecomposition_F64 createDecomposition();

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
        EigenDecomposition_F64 alg = createDecomposition();

        assertFalse(alg.decompose(new DMatrixRMaj(0,0)));
    }

    /**
     * Create a variety of different random matrices of different sizes and sees if they pass the standard
     * eigen decompositions tests.
     */
    public void checkRandom() {
        int sizes[] = new int[]{1,2,5,10,20,50,100,200};

        EigenDecomposition_F64 alg = createDecomposition();

        for( int s = 2; s < sizes.length; s++ ) {
            int N = sizes[s];
//            System.out.println("N = "+N);

            for( int i = 0; i < 2; i++ ) {
                DMatrixRMaj A = RandomMatrices_DDRM.createRandom(N,N,-1,1,rand);

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
        DMatrixRMaj A = new DMatrixRMaj(3,3, true, 0.907265, 0.832472, 0.255310, 0.667810, 0.871323, 0.612657, 0.025059, 0.126475, 0.427002);

        EigenDecomposition_F64 alg = createDecomposition();

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
//        DMatrixRMaj companion = new DMatrixRMaj(n, n);
//        for (int i = 0; i < n; i++) {
//            companion.set(i, n - 1, -polynomial[i] / polynomial[n]);
//        }
//        for (int i = 1; i < n; i++) {
//            companion.set(i, i - 1, 1);
//        }
//
//        // the eigenvalues of the companion matrix matches the roots of the polynomial
//        EigenDecomposition_F64 dense = createDecomposition();
//        assertTrue(safeDecomposition(dense,companion));
//
//        // see if the roots are zero
//        for( int i = 0; i < dense.getNumberOfEigenvalues(); i++ ) {
//            Complex_F64 c = dense.getEigenvalue(i);
//
//            if( !c.isReal() ) {
//                continue;
//            }
//
//            double total = 0;
//            for( int j = 0; j < polynomial.length; j++ ) {
//                total += polynomial[j]* Math.pow(c.real,j);
//            }
//
//            assertEquals(0,total,1e-12);
//        }
//
//
//        performStandardTests(dense,companion,n);
    }

    /**
     * Sees if it correctly computed the eigenvalues.  Does not check eigenvectors.
     */
    public void checkKnownReal_JustValue() {
        DMatrixRMaj A = new DMatrixRMaj(3,3, true, 0.907265, 0.832472, 0.255310, 0.667810, 0.871323, 0.612657, 0.025059, 0.126475, 0.427002);

        EigenDecomposition_F64 alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));

        testForEigenvalue(alg,A,1.686542,0,1);
        testForEigenvalue(alg,A,0.079014,0,1);
        testForEigenvalue(alg,A,0.440034,0,1);
    }

    /**
     * Sees if it correctly computed the eigenvalues.  Does not check eigenvectors.
     */
    public void checkKnownSymmetric_JustValue() {
        DMatrixRMaj A = new DMatrixRMaj(3,3, true,
                0.98139,   0.78650,   0.78564,
                0.78650,   1.03207,   0.29794,
                0.78564,   0.29794,   0.91926);
        EigenDecomposition_F64 alg = createDecomposition();

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
        DMatrixRMaj A = new DMatrixRMaj(3,3, true, -0.418284, 0.279875, 0.452912, -0.093748, -0.045179, 0.310949, 0.250513, -0.304077, -0.031414);

        EigenDecomposition_F64 alg = createDecomposition();

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
                DMatrixRMaj A = RandomMatrices_DDRM.createSymmetric(N,-1,1,rand);

                EigenDecomposition_F64 alg = createDecomposition();

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
        DMatrixRMaj A = new DMatrixRMaj(5,5, true, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0);

        EigenDecomposition_F64 alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));

        performStandardTests(alg,A,1);
    }

    public void checkIdentity() {
        DMatrixRMaj I = CommonOps_DDRM.identity(4);

        EigenDecomposition_F64 alg = createDecomposition();

        assertTrue(safeDecomposition(alg,I));

        performStandardTests(alg,I,4);

        testForEigenpair(alg,1,0,1,0,0,0);
        testForEigenpair(alg,1,0,0,1,0,0);
        testForEigenpair(alg,1,0,0,0,1,0);
        testForEigenpair(alg,1,0,0,0,0,1);
    }

    public void checkAllZeros() {
        DMatrixRMaj A = new DMatrixRMaj(5,5);

        EigenDecomposition_F64 alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));

        performStandardTests(alg,A,5);
        testEigenvalues(alg,0);
    }

    public void checkWithSomeRepeatedValuesSymm() {
        EigenDecomposition_F64 alg = createDecomposition();

        checkSymmetricMatrix(alg,2,-3,-3,-3);
        checkSymmetricMatrix(alg,2,-3,2,2);
        checkSymmetricMatrix(alg,1,1,1,2);
    }

    public void checkWithSingularSymm() {

        EigenDecomposition_F64 alg = createDecomposition();

        checkSymmetricMatrix(alg,1,0,1,2);
    }

    /**
     * Creates a random symmetric matrix with the specified eigenvalues.  It then
     * checks to see if it has the expected results.
     */
    private void checkSymmetricMatrix(EigenDecomposition_F64 alg , double ...ev ) {
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
            DMatrixRMaj A = RandomMatrices_DDRM.createEigenvaluesSymm(ev.length,rand,ev);

            assertTrue(safeDecomposition(alg,A));

            performStandardTests(alg,A,ev.length);

            for( int j = 0; j < ev.length; j++ ) {
                testForEigenvalue(alg,A,ev[j],0,numRepeated[j]);
            }
        }
    }

    public void checkSmallValue( boolean symmetric) {

//        System.out.println("Symmetric = "+symmetric);
        EigenDecomposition_F64 alg = createDecomposition();

        for( int i = 0; i < 20; i++ ) {
            DMatrixRMaj A = symmetric ?
                    RandomMatrices_DDRM.createSymmetric(4,-1,1,rand) :
                    RandomMatrices_DDRM.createRandom(4,4,-1,1,rand);

            CommonOps_DDRM.scale( Math.pow(UtilEjml.EPS,12) ,A);

            assertTrue(safeDecomposition(alg,A));

//        A.print("%15.13e");

            performStandardTests(alg,A,-1);
        }
    }

    public void checkLargeValue( boolean symmetric) {

        EigenDecomposition_F64 alg = createDecomposition();

        for( int i = 0; i < 20; i++ ) {
            DMatrixRMaj A = symmetric ?
                    RandomMatrices_DDRM.createSymmetric(4,-1,1,rand) :
                    RandomMatrices_DDRM.createRandom(4,4,-1,1,rand);

            CommonOps_DDRM.scale( Math.pow(UtilEjml.EPS,-2) ,A);

            assertTrue(safeDecomposition(alg,A));

            performStandardTests(alg,A,-1);
        }
    }

    /**
     * If the eigenvalues are all known, real, and the same this can be used to check them.
     */
    public void testEigenvalues(EigenDecomposition_F64 alg , double expected ) {

        for( int i = 0; i < alg.getNumberOfEigenvalues(); i++ ) {
            Complex_F64 c = alg.getEigenvalue(i);

            assertTrue(c.isReal());

            assertEquals(expected,c.real,UtilEjml.TEST_F64);
        }
    }

    /**
     * Preforms standard tests that can be performed on any decomposition without prior knowledge of
     * what the results should be.
     */
    public void performStandardTests(EigenDecomposition_F64 alg , DMatrixRMaj A , int numReal )
    {

        // basic sanity tests
        assertEquals(A.numRows,alg.getNumberOfEigenvalues());

        if( numReal >= 0 ) {
            for( int i = 0; i < A.numRows; i++ ) {
                Complex_F64 v = alg.getEigenvalue(i);

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

//        checkCharacteristicEquation(dense,A);
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
        DMatrixRMaj A = new DMatrixRMaj(matrix);

        EigenDecomposition_F64 alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));

        performStandardTests(alg,A,1);
    }

    /**
     * Checks to see if an eigenvalue is complex then the eigenvector is null.  If it is real it
     * then checks to see if the equation A*v = lambda*v holds true.
     */
    public void testPairsConsistent(EigenDecomposition_F64<DMatrixRMaj> alg , DMatrixRMaj A )
    {
//        System.out.println("-------------------------------------------------------------------------");
        int N = alg.getNumberOfEigenvalues();

        DMatrixRMaj tempA = new DMatrixRMaj(N,1);
        DMatrixRMaj tempB = new DMatrixRMaj(N,1);
        
        for( int i = 0; i < N; i++ ) {
            Complex_F64 c = alg.getEigenvalue(i);
            DMatrixRMaj v = alg.getEigenVector(i);

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
                assertFalse(MatrixFeatures_DDRM.hasUncountable(v));

                CommonOps_DDRM.mult(A,v,tempA);
                CommonOps_DDRM.scale(c.real,v,tempB);

                double max = NormOps_DDRM.normPInf(A);
                if( max == 0 ) max = 1;

                double error = SpecializedOps_DDRM.diffNormF(tempA,tempB)/max;

                if( error > UtilEjml.TEST_F64) {
                    System.out.println("Original matrix:");
                    A.print();
                    System.out.println("Eigenvalue = "+c.real);
                    Eigenpair_F64 p = EigenOps_DDRM.computeEigenVector(A,c.real);
                    p.vector.print();
                    v.print();


                    CommonOps_DDRM.mult(A,p.vector,tempA);
                    CommonOps_DDRM.scale(c.real,p.vector,tempB);

                    max = NormOps_DDRM.normPInf(A);

                    System.out.println("error before = "+error);
                    error = SpecializedOps_DDRM.diffNormF(tempA,tempB)/max;
                    System.out.println("error after = "+error);
                    A.print("%f");
                    System.out.println();
                    fail("Error was too large");
                }

                assertTrue(error <= UtilEjml.TEST_F64);
            }
        }
    }

    /**
     * Takes a real eigenvalue and computes its eigenvector.  then sees if it is similar to the adjusted
     * eigenvalue
     */
    public void testEigenvalueConsistency( EigenDecomposition_F64 alg ,
                                           DMatrixRMaj A )
    {
        int N = alg.getNumberOfEigenvalues();

        DMatrixRMaj AV = new DMatrixRMaj(N,1);
        DMatrixRMaj LV = new DMatrixRMaj(N,1);

        for( int i = 0; i < N; i++ ) {
            Complex_F64 c = alg.getEigenvalue(i);

            if( c.isReal() ) {
                Eigenpair_F64 p = EigenOps_DDRM.computeEigenVector(A,c.getReal());

                if( p != null ) {
                    CommonOps_DDRM.mult(A,p.vector,AV);
                    CommonOps_DDRM.scale(c.getReal(),p.vector,LV);
                    double error = SpecializedOps_DDRM.diffNormF(AV,LV);
//                    System.out.println("error = "+error);
                    assertTrue(error < UtilEjml.TEST_F64);
                }
            }
        }
    }

    /**
     * See if eigenvalues cause the characteristic equation to have a value of zero
     */
    public void checkCharacteristicEquation( EigenDecomposition_F64 alg ,
                                             DMatrixRMaj A ) {
        int N = alg.getNumberOfEigenvalues();

        SimpleMatrix a = SimpleMatrix.wrap(A);

        for( int i = 0; i < N; i++ ) {
            Complex_F64 c = alg.getEigenvalue(i);

            if( c.isReal() ) {
                // test using the characteristic equation
                double det = (double)SimpleMatrix.identity(A.numCols).scale(c.real).minus(a).determinant();

                // extremely crude test.  given perfect data this is probably considered a failure...  However,
                // its hard to tell what a good test value actually is.
                assertEquals(0, det, 0.1);
            }
        }
    }

    /**
     * Checks to see if all the real eigenvectors are linearly independent of each other.
     */
    public void testVectorsLinearlyIndependent( EigenDecomposition<DMatrixRMaj> alg ) {
        int N = alg.getNumberOfEigenvalues();

        // create a matrix out of the eigenvectors
        DMatrixRMaj A = new DMatrixRMaj(N,N);

        int off = 0;
        for( int i = 0; i < N; i++ ) {
            DMatrixRMaj v = alg.getEigenVector(i);

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

        assertTrue(MatrixFeatures_DDRM.isRowsLinearIndependent(A));
    }

    /**
     * Sees if the pair of eigenvalue and eigenvector was found in the decomposition.
     */
    public void testForEigenpair(EigenDecomposition_F64<DMatrixRMaj> alg , double valueReal ,
                                 double valueImg , double... vector )
    {
        int N = alg.getNumberOfEigenvalues();

        int numMatched = 0;
        for( int i = 0; i < N; i++ ) {
            Complex_F64 c = alg.getEigenvalue(i);

            if( Math.abs(c.real-valueReal) < UtilEjml.TEST_F64_SQ && Math.abs(c.imaginary-valueImg) < UtilEjml.TEST_F64_SQ) {

                if( c.isReal() ) {
                    if( vector.length > 0 ) {
                        DMatrixRMaj v = alg.getEigenVector(i);
                        DMatrixRMaj e = new DMatrixRMaj(N,1, true, vector);

                        double error = SpecializedOps_DDRM.diffNormF(e,v);
                        CommonOps_DDRM.changeSign(e);
                        double error2 = SpecializedOps_DDRM.diffNormF(e,v);


                        if(error < 10*UtilEjml.TEST_F64_SQ || error2 < 10*UtilEjml.TEST_F64_SQ)
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

    public void testForEigenvalue( EigenDecomposition_F64 alg ,
                                   DMatrixRMaj A,
                                   double valueReal ,
                                   double valueImg , int numMatched )
    {
        int N = alg.getNumberOfEigenvalues();

        int numFound = 0;
        for( int i = 0; i < N; i++ ) {
            Complex_F64 c = alg.getEigenvalue(i);

            if( Math.abs(c.real-valueReal) < UtilEjml.TEST_F64_SQ && Math.abs(c.imaginary-valueImg) < UtilEjml.TEST_F64_SQ) {
                numFound++;
            }
        }

        assertEquals(numMatched,numFound);
    }
}
