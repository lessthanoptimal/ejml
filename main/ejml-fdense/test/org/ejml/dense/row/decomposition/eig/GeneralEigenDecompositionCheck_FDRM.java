/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.Complex_F32;
import org.ejml.data.FEigenpair;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.*;
import org.ejml.interfaces.decomposition.EigenDecomposition;
import org.ejml.interfaces.decomposition.EigenDecomposition_F32;
import org.ejml.simple.SimpleMatrix;

import java.util.Random;

import static org.ejml.dense.row.decomposition.CheckDecompositionInterface_FDRM.safeDecomposition;
import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public abstract class GeneralEigenDecompositionCheck_FDRM {

    Random rand = new Random(895723);

    public abstract EigenDecomposition_F32 createDecomposition();

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
        EigenDecomposition_F32 alg = createDecomposition();

        assertFalse(alg.decompose(new FMatrixRMaj(0,0)));
    }

    /**
     * Create a variety of different random matrices of different sizes and sees if they pass the standard
     * eigen decompositions tests.
     */
    public void checkRandom() {
        int sizes[] = new int[]{1,2,5,10,20,50,100,200};

        EigenDecomposition_F32 alg = createDecomposition();

        for( int s = 2; s < sizes.length; s++ ) {
            int N = sizes[s];
//            System.out.println("N = "+N);

            for( int i = 0; i < 2; i++ ) {
                FMatrixRMaj A = RandomMatrices_FDRM.rectangle(N,N,-1,1,rand);

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
        FMatrixRMaj A = new FMatrixRMaj(3,3, true, 0.907265f, 0.832472f, 0.255310f, 0.667810f, 0.871323f, 0.612657f, 0.025059f, 0.126475f, 0.427002f);

        EigenDecomposition_F32 alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));
        performStandardTests(alg,A,-1);

        testForEigenpair(alg,1.686542f,0,-0.739990f,-0.667630f,-0.081761f);
        testForEigenpair(alg,0.079014f,0,-0.658665f,0.721163f,-0.214673f);
        testForEigenpair(alg,0.440034f,0,-0.731422f,0.211711f,0.648229f);
    }

    /**
     * Found to be a stressing case that broke a version of the general EVD algorithm.  It is a companion matrix
     * for a polynomial used to find the zeros.
     *
     * Discovered by exratt@googlema*l.com
     */
    public void checkCompanionMatrix() {
//        float[] polynomial = {
//                5.392104631674957e7f,
//                -7.717841412372049e8f,
//                -1.4998803087543774e7f,
//                -30110.074181432814f,
//                -16.0f
//        };
//
////        float polynomial[] = new float[]{
////                0.0817011296749115f,
////                -0.8100357949733734f,
////                -0.8667608685791492f,
////                2.2995666563510895f,
////                0.8879469335079193f,
////                -4.16266793012619f,
////                -1.527034044265747f,
////                2.201415002346039f,
////                0.5391231775283813f,
////                -0.41334158182144165f};
//
//        // build companion matrix
//        int n = polynomial.length - 1;
//        FMatrixRMaj companion = new FMatrixRMaj(n, n);
//        for (int i = 0; i < n; i++) {
//            companion.set(i, n - 1, -polynomial[i] / polynomial[n]);
//        }
//        for (int i = 1; i < n; i++) {
//            companion.set(i, i - 1, 1);
//        }
//
//        // the eigenvalues of the companion matrix matches the roots of the polynomial
//        EigenDecomposition_F32 dense = createDecomposition();
//        assertTrue(safeDecomposition(dense,companion));
//
//        // see if the roots are zero
//        for( int i = 0; i < dense.getNumberOfEigenvalues(); i++ ) {
//            Complex_F32 c = dense.getEigenvalue(i);
//
//            if( !c.isReal() ) {
//                continue;
//            }
//
//            float total = 0;
//            for( int j = 0; j < polynomial.length; j++ ) {
//                total += polynomial[j]* (float)Math.pow(c.real,j);
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
        FMatrixRMaj A = new FMatrixRMaj(3,3, true, 0.907265f, 0.832472f, 0.255310f, 0.667810f, 0.871323f, 0.612657f, 0.025059f, 0.126475f, 0.427002f);

        EigenDecomposition_F32 alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));

        testForEigenvalue(alg,A,1.686542f,0,1);
        testForEigenvalue(alg,A,0.079014f,0,1);
        testForEigenvalue(alg,A,0.440034f,0,1);
    }

    /**
     * Sees if it correctly computed the eigenvalues.  Does not check eigenvectors.
     */
    public void checkKnownSymmetric_JustValue() {
        FMatrixRMaj A = new FMatrixRMaj(3,3, true,
                0.98139f,   0.78650f,   0.78564f,
                0.78650f,   1.03207f,   0.29794f,
                0.78564f,   0.29794f,   0.91926f);
        EigenDecomposition_F32 alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));

        testForEigenvalue(alg,A,0.00426f,0,1);
        testForEigenvalue(alg,A,0.67856f,0,1);
        testForEigenvalue(alg,A,2.24989f,0,1);
    }

    /**
     * Compare results against a simple matrix with known results where some the eigenvalues
     * are real and some are complex.
     */
    public void checkKnownComplex() {
        FMatrixRMaj A = new FMatrixRMaj(3,3, true, -0.418284f, 0.279875f, 0.452912f, -0.093748f, -0.045179f, 0.310949f, 0.250513f, -0.304077f, -0.031414f);

        EigenDecomposition_F32 alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));
        performStandardTests(alg,A,-1);

        testForEigenpair(alg,-0.39996f,0,0.87010f,0.43425f,-0.23314f);
        testForEigenpair(alg,-0.04746f,0.02391f);
        testForEigenpair(alg,-0.04746f,-0.02391f);
    }

    /**
     * Check results against symmetric matrices that are randomly generated
     */
    public void checkRandomSymmetric() {
        for( int N = 1; N <= 15; N++ ) {
            for( int i = 0; i < 20; i++ ) {
                FMatrixRMaj A = RandomMatrices_FDRM.symmetric(N,-1,1,rand);

                EigenDecomposition_F32 alg = createDecomposition();

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
        FMatrixRMaj A = new FMatrixRMaj(5,5, true, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0);

        EigenDecomposition_F32 alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));

        performStandardTests(alg,A,1);
    }

    public void checkIdentity() {
        FMatrixRMaj I = CommonOps_FDRM.identity(4);

        EigenDecomposition_F32 alg = createDecomposition();

        assertTrue(safeDecomposition(alg,I));

        performStandardTests(alg,I,4);

        testForEigenpair(alg,1,0,1,0,0,0);
        testForEigenpair(alg,1,0,0,1,0,0);
        testForEigenpair(alg,1,0,0,0,1,0);
        testForEigenpair(alg,1,0,0,0,0,1);
    }

    public void checkAllZeros() {
        FMatrixRMaj A = new FMatrixRMaj(5,5);

        EigenDecomposition_F32 alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));

        performStandardTests(alg,A,5);
        testEigenvalues(alg,0);
    }

    public void checkWithSomeRepeatedValuesSymm() {
        EigenDecomposition_F32 alg = createDecomposition();

        checkSymmetricMatrix(alg,2,-3,-3,-3);
        checkSymmetricMatrix(alg,2,-3,2,2);
        checkSymmetricMatrix(alg,1,1,1,2);
    }

    public void checkWithSingularSymm() {

        EigenDecomposition_F32 alg = createDecomposition();

        checkSymmetricMatrix(alg,1,0,1,2);
    }

    /**
     * Creates a random symmetric matrix with the specified eigenvalues.  It then
     * checks to see if it has the expected results.
     */
    private void checkSymmetricMatrix(EigenDecomposition_F32 alg , float ...ev ) {
        int numRepeated[] = new int[ ev.length ];

        for( int i = 0; i < ev.length ; i++ ) {
            int num = 0;

            for (float anEv : ev) {
                if (ev[i] == anEv)
                    num++;
            }
            numRepeated[i] = num;
        }

        for( int i = 0; i < 200; i++ ) {
            FMatrixRMaj A = RandomMatrices_FDRM.symmetricWithEigenvalues(ev.length,rand,ev);

            assertTrue(safeDecomposition(alg,A));

            performStandardTests(alg,A,ev.length);

            for( int j = 0; j < ev.length; j++ ) {
                testForEigenvalue(alg,A,ev[j],0,numRepeated[j]);
            }
        }
    }

    public void checkSmallValue( boolean symmetric) {

//        System.out.println("Symmetric = "+symmetric);
        EigenDecomposition_F32 alg = createDecomposition();

        for( int i = 0; i < 20; i++ ) {
            FMatrixRMaj A = symmetric ?
                    RandomMatrices_FDRM.symmetric(4,-1,1,rand) :
                    RandomMatrices_FDRM.rectangle(4,4,-1,1,rand);

            CommonOps_FDRM.scale( (float)Math.pow(UtilEjml.F_EPS,12) ,A);

            assertTrue(safeDecomposition(alg,A));

//        A.print("%15.13fe");

            performStandardTests(alg,A,-1);
        }
    }

    public void checkLargeValue( boolean symmetric) {

        EigenDecomposition_F32 alg = createDecomposition();

        for( int i = 0; i < 20; i++ ) {
            FMatrixRMaj A = symmetric ?
                    RandomMatrices_FDRM.symmetric(4,-1,1,rand) :
                    RandomMatrices_FDRM.rectangle(4,4,-1,1,rand);

            CommonOps_FDRM.scale( (float)Math.pow(UtilEjml.F_EPS,-2) ,A);

            assertTrue(safeDecomposition(alg,A));

            performStandardTests(alg,A,-1);
        }
    }

    /**
     * If the eigenvalues are all known, real, and the same this can be used to check them.
     */
    public void testEigenvalues(EigenDecomposition_F32 alg , float expected ) {

        for( int i = 0; i < alg.getNumberOfEigenvalues(); i++ ) {
            Complex_F32 c = alg.getEigenvalue(i);

            assertTrue(c.isReal());

            assertEquals(expected,c.real,UtilEjml.TEST_F32);
        }
    }

    /**
     * Preforms standard tests that can be performed on any decomposition without prior knowledge of
     * what the results should be.
     */
    public void performStandardTests(EigenDecomposition_F32 alg , FMatrixRMaj A , int numReal )
    {

        // basic sanity tests
        assertEquals(A.numRows,alg.getNumberOfEigenvalues());

        if( numReal >= 0 ) {
            for( int i = 0; i < A.numRows; i++ ) {
                Complex_F32 v = alg.getEigenvalue(i);

                assertFalse( Float.isNaN(v.getReal() ));
                if( v.isReal() )
                    numReal--;
                else if( Math.abs(v.getImaginary()) < 10*UtilEjml.F_EPS)
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
        float[][] matrix = new float[][] {
                {1, 0, 0},
                {0.01f, 0, -1},
                {0.01f, 1, 0}};
        FMatrixRMaj A = new FMatrixRMaj(matrix);

        EigenDecomposition_F32 alg = createDecomposition();

        assertTrue(safeDecomposition(alg,A));

        performStandardTests(alg,A,1);
    }

    /**
     * Checks to see if an eigenvalue is complex then the eigenvector is null.  If it is real it
     * then checks to see if the equation A*v = lambda*v holds true.
     */
    public void testPairsConsistent(EigenDecomposition_F32<FMatrixRMaj> alg , FMatrixRMaj A )
    {
//        System.out.println("-------------------------------------------------------------------------");
        int N = alg.getNumberOfEigenvalues();

        FMatrixRMaj tempA = new FMatrixRMaj(N,1);
        FMatrixRMaj tempB = new FMatrixRMaj(N,1);
        
        for( int i = 0; i < N; i++ ) {
            Complex_F32 c = alg.getEigenvalue(i);
            FMatrixRMaj v = alg.getEigenVector(i);

            if( Float.isInfinite(c.real) || Float.isNaN(c.real) ||
                    Float.isInfinite(c.imaginary) || Float.isNaN(c.imaginary))
                fail("Uncountable eigenvalue");

            if( !c.isReal() ) {
                assertTrue(v==null);
            } else {
                assertTrue(v != null );
//                if( MatrixFeatures.hasUncountable(v)) {
//                    throw new RuntimeException("Egads");
//                }
                assertFalse(MatrixFeatures_FDRM.hasUncountable(v));

                CommonOps_FDRM.mult(A,v,tempA);
                CommonOps_FDRM.scale(c.real,v,tempB);

                float max = NormOps_FDRM.normPInf(A);
                if( max == 0 ) max = 1;

                float error = SpecializedOps_FDRM.diffNormF(tempA,tempB)/max;

                if( error > UtilEjml.TEST_F32) {
                    System.out.println("Original matrix:");
                    A.print();
                    System.out.println("Eigenvalue = "+c.real);
                    FEigenpair p = EigenOps_FDRM.computeEigenVector(A,c.real);
                    p.vector.print();
                    v.print();


                    CommonOps_FDRM.mult(A,p.vector,tempA);
                    CommonOps_FDRM.scale(c.real,p.vector,tempB);

                    max = NormOps_FDRM.normPInf(A);

                    System.out.println("error before = "+error);
                    error = SpecializedOps_FDRM.diffNormF(tempA,tempB)/max;
                    System.out.println("error after = "+error);
                    A.print("%f");
                    System.out.println();
                    fail("Error was too large");
                }

                assertTrue(error <= UtilEjml.TEST_F32);
            }
        }
    }

    /**
     * Takes a real eigenvalue and computes its eigenvector.  then sees if it is similar to the adjusted
     * eigenvalue
     */
    public void testEigenvalueConsistency( EigenDecomposition_F32 alg ,
                                           FMatrixRMaj A )
    {
        int N = alg.getNumberOfEigenvalues();

        FMatrixRMaj AV = new FMatrixRMaj(N,1);
        FMatrixRMaj LV = new FMatrixRMaj(N,1);

        for( int i = 0; i < N; i++ ) {
            Complex_F32 c = alg.getEigenvalue(i);

            if( c.isReal() ) {
                FEigenpair p = EigenOps_FDRM.computeEigenVector(A,c.getReal());

                if( p != null ) {
                    CommonOps_FDRM.mult(A,p.vector,AV);
                    CommonOps_FDRM.scale(c.getReal(),p.vector,LV);
                    float error = SpecializedOps_FDRM.diffNormF(AV,LV);
//                    System.out.println("error = "+error);
                    assertTrue(error < UtilEjml.TEST_F32);
                }
            }
        }
    }

    /**
     * See if eigenvalues cause the characteristic equation to have a value of zero
     */
    public void checkCharacteristicEquation( EigenDecomposition_F32 alg ,
                                             FMatrixRMaj A ) {
        int N = alg.getNumberOfEigenvalues();

        SimpleMatrix a = SimpleMatrix.wrap(A);

        for( int i = 0; i < N; i++ ) {
            Complex_F32 c = alg.getEigenvalue(i);

            if( c.isReal() ) {
                // test using the characteristic equation
                float det = (float)SimpleMatrix.identity(A.numCols).scale(c.real).minus(a).determinant();

                // extremely crude test.  given perfect data this is probably considered a failure...  However,
                // its hard to tell what a good test value actually is.
                assertEquals(0, det, 0.1f);
            }
        }
    }

    /**
     * Checks to see if all the real eigenvectors are linearly independent of each other.
     */
    public void testVectorsLinearlyIndependent( EigenDecomposition<FMatrixRMaj> alg ) {
        int N = alg.getNumberOfEigenvalues();

        // create a matrix out of the eigenvectors
        FMatrixRMaj A = new FMatrixRMaj(N,N);

        int off = 0;
        for( int i = 0; i < N; i++ ) {
            FMatrixRMaj v = alg.getEigenVector(i);

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

        assertTrue(MatrixFeatures_FDRM.isRowsLinearIndependent(A));
    }

    /**
     * Sees if the pair of eigenvalue and eigenvector was found in the decomposition.
     */
    public void testForEigenpair(EigenDecomposition_F32<FMatrixRMaj> alg , float valueReal ,
                                 float valueImg , float... vector )
    {
        int N = alg.getNumberOfEigenvalues();

        int numMatched = 0;
        for( int i = 0; i < N; i++ ) {
            Complex_F32 c = alg.getEigenvalue(i);

            if( Math.abs(c.real-valueReal) < UtilEjml.TEST_F32_SQ && Math.abs(c.imaginary-valueImg) < UtilEjml.TEST_F32_SQ) {

                if( c.isReal() ) {
                    if( vector.length > 0 ) {
                        FMatrixRMaj v = alg.getEigenVector(i);
                        FMatrixRMaj e = new FMatrixRMaj(N,1, true, vector);

                        float error = SpecializedOps_FDRM.diffNormF(e,v);
                        CommonOps_FDRM.changeSign(e);
                        float error2 = SpecializedOps_FDRM.diffNormF(e,v);


                        if(error < 10*UtilEjml.TEST_F32_SQ || error2 < 10*UtilEjml.TEST_F32_SQ)
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

    public void testForEigenvalue( EigenDecomposition_F32 alg ,
                                   FMatrixRMaj A,
                                   float valueReal ,
                                   float valueImg , int numMatched )
    {
        int N = alg.getNumberOfEigenvalues();

        int numFound = 0;
        for( int i = 0; i < N; i++ ) {
            Complex_F32 c = alg.getEigenvalue(i);

            if( Math.abs(c.real-valueReal) < UtilEjml.TEST_F32_SQ && Math.abs(c.imaginary-valueImg) < UtilEjml.TEST_F32_SQ) {
                numFound++;
            }
        }

        assertEquals(numMatched,numFound);
    }
}
