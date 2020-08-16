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

package org.ejml.dense.row.decompose.qr;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.MatrixFeatures_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.ejml.ops.ComplexMath_F64;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestQrHelperFunctions_ZDRM {

    Random rand = new Random(234);

    @Test
    public void findMax() {
        double[] u = new double[50];

        for (int i = 0; i < u.length; i++) {
            u[i] = (rand.nextDouble()-0.5)*20;
        }

        int offset = 4;
        int length = 5;

        double max = 0;
        for (int i = 0; i < length; i++) {
            double real = u[i*2+offset*2];
            double img = u[i*2+offset*2+1];

            if( real*real + img*img > max ) {
                max = real*real + img*img;
            }
        }

        max = Math.sqrt(max);

        assertEquals(max,QrHelperFunctions_ZDRM.findMax(u,offset,length),UtilEjml.TEST_F64);
    }

    @Test
    public void divideElements_startU() {
        double[] u = new double[12*2];
        for (int i = 0; i < u.length; i++ ) {
            u[i] = (rand.nextDouble()*0.5-1.0)*2;
        }
        double[] found = u.clone();

        Complex_F64 A = new Complex_F64(rand.nextDouble(),rand.nextDouble());
        Complex_F64 U = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        int j = 3;
        int numRows = 8;
        int startU = 2;

        QrHelperFunctions_ZDRM.divideElements(j,numRows,found,startU,A.real,A.imaginary);

        for (int i = 0; i < 12; i++) {
            int index = i * 2;

            if( i >= j+startU && i < numRows+startU ) {
                U.real = u[index];
                U.imaginary = u[index + 1];
                ComplexMath_F64.divide(U, A, expected);

                assertEquals(expected.real, found[index], UtilEjml.TEST_F64);
                assertEquals(expected.imaginary, found[index + 1], UtilEjml.TEST_F64);
            } else {
                assertEquals(u[index],found[index],UtilEjml.TEST_F64);
                assertEquals(u[index+1],found[index+1],UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void computeTauGammaAndDivide() {
        double[] u = new double[12*2];
        for (int i = 0; i < u.length; i++ ) {
            u[i] = (rand.nextDouble()*0.5-1.0)*2;
        }

        double max = 2.0;
        int j = 2;
        int numRows = 6;

        Complex_F64 expectedTau = new Complex_F64();
        double[] expectedU = u.clone();

        for (int i = j; i < numRows; i++) {
            Complex_F64 U = new Complex_F64(u[i*2],u[i*2+1]);
            Complex_F64 div = new Complex_F64();
            ComplexMath_F64.divide(U,new Complex_F64(max,0),div);

            expectedU[i*2] = div.real;
            expectedU[i*2+1] = div.imaginary;
        }
        double normX = 0;
        for (int i = j; i < numRows; i++) {
            normX += expectedU[i*2]*expectedU[i*2] + expectedU[i*2+1]*expectedU[i*2+1];
        }
        normX = Math.sqrt(normX);
        double realX0 = expectedU[j*2];
        double imagX0 = expectedU[j*2+1];

        double magX0 = Math.sqrt(realX0*realX0 + imagX0*imagX0);
        expectedTau.real      = realX0*normX/magX0;
        expectedTau.imaginary = imagX0*normX/magX0;

        double realU0 = realX0 + expectedTau.real;
        double imagU0 = imagX0 + expectedTau.imaginary;

        //
        double normU = 1;
        Complex_F64 B = new Complex_F64(realU0,imagU0);
        for (int i = j+1; i < numRows; i++) {
            Complex_F64 A = new Complex_F64( expectedU[i*2], expectedU[i*2+1]);
            Complex_F64 result = new Complex_F64();
            ComplexMath_F64.divide(A,B,result);
            normU += result.getMagnitude2();
        }
        double expectedGamma = 2.0/normU;

        Complex_F64 foundTau = new Complex_F64();
        double[] foundU = u.clone();
        double foundGamma = QrHelperFunctions_ZDRM.computeTauGammaAndDivide(j,numRows,foundU,max,foundTau);

        for (int i = 0; i < expectedU.length; i++) {
            assertEquals(expectedU[i],foundU[i],UtilEjml.TEST_F64);
        }

        assertEquals(expectedTau.real,foundTau.real,UtilEjml.TEST_F64);
        assertEquals(expectedTau.imaginary,foundTau.imaginary, UtilEjml.TEST_F64);

        assertEquals(expectedGamma,foundGamma,UtilEjml.TEST_F64);
    }

    @Test
    public void rank1UpdateMultR() {
        double[] u = new double[12*2];
        double[] uoff = new double[12*2+2];
        double[] subU = new double[12*2];
        double[] _temp = new double[u.length];
        double gamma = 0.6;

        for (int i = 0; i < u.length; i++) {
            u[i] = uoff[i+2] = (rand.nextDouble()*0.5-1.0)*2;
        }

        for (int i = 1; i < 12; i++) {
            ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(i,i,rand);

            for (int j = 1; j <= i; j += 2) {
                ZMatrixRMaj subA = CommonOps_ZDRM.extract(A,A.numRows-j,A.numRows,A.numRows-j,A.numRows);
                System.arraycopy(u,(A.numRows-j)*2,subU,0,j*2);
                ZMatrixRMaj expected = rank1UpdateMultR(subA, gamma,subU);

                ZMatrixRMaj found = A.copy();
                QrHelperFunctions_ZDRM.rank1UpdateMultR(found, uoff, 1, gamma,
                        A.numRows - j, A.numRows - j, A.numRows, _temp);

                ZMatrixRMaj subFound = CommonOps_ZDRM.extract(found,A.numRows-j,A.numRows,A.numRows-j,A.numRows);

                outsideIdentical(A, found, j);
                assertTrue(MatrixFeatures_ZDRM.isEquals(expected, subFound, UtilEjml.TEST_F64));
            }
        }
    }

    private ZMatrixRMaj rank1UpdateMultR(ZMatrixRMaj A , double gamma, double[] u) {
        ZMatrixRMaj U = new ZMatrixRMaj(A.numCols,1);
        U.data = u;
        ZMatrixRMaj UUt = new ZMatrixRMaj(A.numCols,A.numCols);
        CommonOps_ZDRM.multTransB(-gamma,0,U,U,UUt);

        ZMatrixRMaj expected = A.copy();
        CommonOps_ZDRM.multAdd(UUt,A,expected);

        return expected;
    }

    @Test
    public void rank1UpdateMultL() {
        double[] u = new double[12*2];
        double[] subU = new double[12*2];
        double gamma = 0.23;

        for (int i = 0; i < u.length; i++) {
            u[i] = (rand.nextDouble()*0.5-1.0)*2;
        }

        for (int i = 1; i < 12; i++) {
            ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(i,i,rand);

            for (int j = 1; j <= i; j += 2) {
                ZMatrixRMaj subA = CommonOps_ZDRM.extract(A,A.numRows-j,A.numRows,A.numRows-j,A.numRows);
                System.arraycopy(u,(A.numRows-j)*2,subU,0,j*2);
                ZMatrixRMaj expected = rank1UpdateMultL(subA,gamma,subU);

                ZMatrixRMaj found = A.copy();
                QrHelperFunctions_ZDRM.rank1UpdateMultL(found,u,0,gamma,
                        A.numRows-j,A.numRows-j,A.numRows);

                ZMatrixRMaj subFound = CommonOps_ZDRM.extract(found,A.numRows-j,A.numRows,A.numRows-j,A.numRows);

                outsideIdentical(A, found, j);
                assertTrue(MatrixFeatures_ZDRM.isEquals(expected, subFound, UtilEjml.TEST_F64));
            }
        }
    }

    private ZMatrixRMaj rank1UpdateMultL(ZMatrixRMaj A , double gamma, double[] u) {
        ZMatrixRMaj U = new ZMatrixRMaj(A.numCols,1);
        U.data = u;
        ZMatrixRMaj UUt = new ZMatrixRMaj(A.numCols,A.numCols);
        CommonOps_ZDRM.multTransB(-gamma,0,U,U,UUt);

        ZMatrixRMaj expected = A.copy();
        CommonOps_ZDRM.multAdd(A,UUt,expected);

        return expected;
    }

    private void outsideIdentical(ZMatrixRMaj A , ZMatrixRMaj B , int width ) {

        int outside = A.numRows-width;

        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
                if( i < outside || j < outside ) {
                    assertEquals(A.getReal(i,j),B.getReal(i,j),UtilEjml.TEST_F64);
                    assertEquals(A.getImag(i, j), B.getImag(i,j),UtilEjml.TEST_F64);
                }
            }
        }
    }

    @Test
    public void extractHouseholderColumn() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(6,5,rand);

        double[] u = new double[6*2];

        QrHelperFunctions_ZDRM.extractHouseholderColumn(A,1,5,1,u,1);

        assertEquals(1 , u[4], UtilEjml.TEST_F64);
        assertEquals(0 , u[5], UtilEjml.TEST_F64);

        for (int i = 2; i < 5; i++) {
            double real = A.getReal(i,1);
            double imag = A.getImag(i,1);

            assertEquals(u[(i+1)*2]   , real , UtilEjml.TEST_F64);
            assertEquals(u[(i+1)*2+1] , imag , UtilEjml.TEST_F64);
        }
    }

    @Test
    public void extractHouseholderRow() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(5,6,rand);

        double[] u = new double[6*2];

        QrHelperFunctions_ZDRM.extractHouseholderRow(A,1,1,5,u,1);

        assertEquals(1 , u[4], UtilEjml.TEST_F64);
        assertEquals(0 , u[5], UtilEjml.TEST_F64);

        for (int i = 2; i < 5; i++) {
            double real = A.getReal(1,i);
            double imag = A.getImag(1,i);

            assertEquals(u[(i+1)*2]   , real , UtilEjml.TEST_F64);
            assertEquals(u[(i+1)*2+1] , imag , UtilEjml.TEST_F64);
        }
    }

    @Test
    public void extractColumnAndMax() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(5,6,rand);

        A.set(2,1,10,0);

        double[] u = new double[6*2];
        double max = QrHelperFunctions_ZDRM.extractColumnAndMax(A,1,5,1,u,1);

        assertEquals(10, max, UtilEjml.TEST_F64);

        for (int i = 1; i < 5; i++) {
            double real = A.getReal(i,1);
            double imag = A.getImag(i,1);

            assertEquals(u[(i+1)*2]   , real , UtilEjml.TEST_F64);
            assertEquals(u[(i+1)*2+1] , imag , UtilEjml.TEST_F64);
        }
    }

    @Test
    public void computeRowMax() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(5,6,rand);

        A.set(1,2,10,0);

        double max = QrHelperFunctions_ZDRM.computeRowMax(A,1,1,5);

        assertEquals(10, max, UtilEjml.TEST_F64);
    }
}
