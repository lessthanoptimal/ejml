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

package org.ejml.dense.row.decompose.eig;

import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.*;
import org.ejml.dense.row.decomposition.eig.EigenvalueExtractor_DDRM;
import org.ejml.dense.row.decomposition.eig.watched.WatchedDoubleStepQREigenvalue_DDRM;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * A stress test that sees how well eigenvalues of Hessenberg matrices can be computed.
 *
 * @author Peter Abeles
 */
public class RealEigenvalueHessenbergStressTest {

    double tol = 1e-10;

    Random rand = new Random(0x3434);

    EigenvalueExtractor_DDRM extractor;

//    public RealEigenvalueStressTest(EigenvalueExtractor extractor) {
//        this.extractor = extractor;
//    }

    int numCantFindEigenvector;

    public RealEigenvalueHessenbergStressTest() {
        extractor = new WatchedDoubleStepQREigenvalue_DDRM();
    }

    public void evaluateRandom() {

        int sizes[] = new int[]{3,5,10,20,50,100,200};

        int totalFailed = 0;
        numCantFindEigenvector = 0;
        for( int sizeIndex = 0; sizeIndex < sizes.length; sizeIndex++ ) {

            int n = sizes[sizeIndex];
            System.out.println("Matrix size = "+n);

            long startTime = System.currentTimeMillis();
            for( int i = 0; i < 100; i++ ) {
                DMatrixRMaj A = RandomMatrices_DDRM.triangularUpper(n,1,-1,1,rand);

                extractor.process(A);

                if( isAllComplex() )
                    continue;

                totalFailed += checkEigenvalues(A);

                if( System.currentTimeMillis() - startTime > 10000 ) {
                    System.out.println("i = "+i);
                    startTime = System.currentTimeMillis();
                }
            }
        }

        System.out.println("Num failed = "+totalFailed);
        System.out.println("Num couldn't find eigenvector = "+numCantFindEigenvector);
    }

    private int checkEigenvalues(DMatrixRMaj a ) {
        Complex_F64[]ev = extractor.getEigenvalues();

//        a.print("%14.5e");

        int numFailed = 0;

        double totalError = 0;

        for( int j = 0; j < extractor.getNumberOfEigenvalues(); j++ ) {
            if( ev[j].imaginary != 0 )
                continue;

            DMatrixRMaj v = EigenOps_DDRM.computeEigenVector(a,ev[j].real).vector;
            Complex_F64 c = ev[j];

            if( v == null || MatrixFeatures_DDRM.hasUncountable(v)) {
                a.print("%f");
                System.out.println("Can't find eigen vector?!?!");
                numCantFindEigenvector++;
                EigenOps_DDRM.computeEigenVector(a,ev[j].real);
                continue;
            }

            double error = computeError(a,v,c.getReal());

            if( error > tol ) {
//                System.out.println("Failed on this matrix:");
//                a.print("%f");
                numFailed++;
                EigenOps_DDRM.computeEigenVector(a,ev[j].real);
            }

            totalError += error;
        }

        totalError /= extractor.getNumberOfEigenvalues();
        System.out.println("Mean error = "+(totalError));

        return numFailed;
    }

    private double computeError(DMatrixRMaj A, DMatrixRMaj v , double eigenvalue ) {

        if( v == null ) {   
            throw new RuntimeException("WTF crappy tool");
        }

//        A.print();
//        System.out.println("Eigen value = "+eigenvalue);
//        NormOps.normalizeF(v);
//        v.print();
        DMatrixRMaj l = new DMatrixRMaj(A.numRows,1);

        CommonOps_DDRM.mult(A,v,l);
        CommonOps_DDRM.scale(eigenvalue,v);

        double top = SpecializedOps_DDRM.diffNormF(l,v);
        double bottom = NormOps_DDRM.normF(v);

        if( Double.isNaN(top) || Double.isInfinite(top) || Double.isNaN(bottom) || Double.isInfinite(bottom) )
            System.out.println("bad stuff");

        double result = top/bottom;

//        if( result >= tol || Double.isNaN(result) || Double.isInfinite(result))
//            System.out.println("Crap");

        return result;
    }

    public void evaluateScalingUp() {
        double []scales = new double[]{1.0,10.0,1e100,1e200,1e306};

        evaluateScaling(scales);
    }

    public void evaluateScalingDown() {
        double []scales = new double[]{1.0,1e-1,1e-100,1e-200,1e-290};

        evaluateScaling(scales);
    }

    private void evaluateScaling(double[] scales) {
        int totalFailures[] = new int[scales.length];

        for( int n = 3; n < 100; n++ ) {
            System.out.println("Matrix size = "+n);

            DMatrixRMaj A = RandomMatrices_DDRM.triangularUpper(n,1,-1,1,rand);
            if( !extractor.process(A) ){
                throw new RuntimeException("Failed!");
            }
            while( isAllComplex()) {
                System.out.println("Trying to find a matrix that isn't all complex number");
                A = RandomMatrices_DDRM.triangularUpper(n,1,-1,1,rand);
                extractor.process(A);
            }

            for( int indexScale = 0; indexScale < scales.length; indexScale++ ) {
                double s = scales[indexScale];

                System.out.println("Scale = "+s);
                DMatrixRMaj B = A.copy();

                CommonOps_DDRM.scale(s,B);

                if( !extractor.process(B) ) {
                    System.out.println("  Failed to converge.");
                    continue;
                }

//                B.print("%15.6e");
                totalFailures[indexScale] += checkEigenvalues(B);
            }
        }

        System.out.println("------------ results --------------");
        for( int i = 0; i < totalFailures.length; i++ ) {
            System.out.printf("  %7.2e fail =  %d\n",scales[i],totalFailures[i]);
        }
    }

    /**
     * See if a totally zero matrix messes it up
     */
    public void testMatrix0() {
        DMatrixRMaj A = new DMatrixRMaj(5,5);

        if( !extractor.process(A) ){
            throw new RuntimeException("Failed!");
        }

        assertEquals(5,extractor.getNumberOfEigenvalues());

        for( int i = 0 ; i < 5; i++ ) {
            Complex_F64 c = extractor.getEigenvalues()[i];

            assertEquals(0,c.imaginary,1e-12);
            assertEquals(0,c.getReal(),1e-12);
        }
    }

    public void testMatrixNegHessenberg() {
        DMatrixRMaj A = new DMatrixRMaj(5,5, true, 0, 1, 2, 3, 5, 0, 0, 4, 9, 3, 0, 0, 0, -1, 3, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0);

        if( !extractor.process(A) ){
            throw new RuntimeException("Failed!");
        }

        assertEquals(5,extractor.getNumberOfEigenvalues());

        for( int i = 0 ; i < 5; i++ ) {
            Complex_F64 c = extractor.getEigenvalues()[i];

            assertEquals(0,c.imaginary,1e-12);
            assertEquals(0,c.getReal(),1e-12);
        }
    }

    /**
     * Special case that requires exceptional shifts to work
     */
    public void testMatrixExceptional() {
        DMatrixRMaj A = new DMatrixRMaj(5,5, true, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0);
        if( !extractor.process(A) ){
            throw new RuntimeException("Failed!");
        }

        assertEquals(5,extractor.getNumberOfEigenvalues());
        // TODO compare to expected eigenvalues
    }

    public void testMatrixZeroButUpperDiag() {
        DMatrixRMaj A = new DMatrixRMaj(5,5, true, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 4, 0);
        if( !extractor.process(A) ){
            throw new RuntimeException("Failed!");
        }

        assertEquals(5,extractor.getNumberOfEigenvalues());
        for( int i = 0 ; i < 5; i++ ) {
            Complex_F64 c = extractor.getEigenvalues()[i];

            assertEquals(0,c.imaginary,1e-12);
            assertEquals(0,c.getReal(),1e-12);
        }
    }

    public void testMatrixVerySmallButUpperDiag() {

        DMatrixRMaj A = new DMatrixRMaj(5,5);

        for( int i = 0; i < 5; i++ ) {
            int start = i < 2 ? 0 : i-1;

            for( int j = start; j < 5; j++ ) {
                A.set(i,j,1e-32);
            }
        }

        for( int i = 0; i < 4; i++) {
            A.set(i+1,i,i+1);
        }

        if( !extractor.process(A) ){
            throw new RuntimeException("Failed!");
        }

        assertEquals(5,extractor.getNumberOfEigenvalues());
        for( int i = 0 ; i < 5; i++ ) {
            Complex_F64 c = extractor.getEigenvalues()[i];

            assertEquals(0,c.imaginary,1e-12);
            assertEquals(0,c.getReal(),1e-12);
        }
    }

    public void testMatrixAlmostAllOnes() {

        DMatrixRMaj A = new DMatrixRMaj(5,5);

        for( int i = 0; i < 5; i++ ) {
            int start = i < 2 ? 0 : i-1;

            for( int j = start; j < 5; j++ ) {
                A.set(i,j,1);
            }
        }

        A.set(2,2,1e-32);
        A.set(3,2,1e-32);

        if( !extractor.process(A) ){
            throw new RuntimeException("Failed!");
        }

//        A.print("%15.5e");

        assertEquals(5,extractor.getNumberOfEigenvalues());
        // TODO add expected list of eigenvalues
    }

    private boolean hasComplex() {
        Complex_F64[]ev = extractor.getEigenvalues();

        for( int j = 0; j < extractor.getNumberOfEigenvalues(); j++ ) {
            if( ev[j].getImaginary() != 0 )
                return true;
        }

        return false;
    }

    private boolean isAllComplex() {
        Complex_F64[]ev = extractor.getEigenvalues();

        for( int j = 0; j < extractor.getNumberOfEigenvalues(); j++ ) {
            if( ev[j].getImaginary() == 0 )
                return false;
        }

        return true;
    }

//    public static void main( String []args ) {
//        EigenvalueExtractor extractor = new PrintDoubleStepQREigenvalue();
//
//        RealEigenvalueStressTest test = new RealEigenvalueStressTest(extractor);
//
////        test.evaluateRandom();
////        test.evaluateScalingUp();
//        test.evaluateScalingDown();
//    }
}
