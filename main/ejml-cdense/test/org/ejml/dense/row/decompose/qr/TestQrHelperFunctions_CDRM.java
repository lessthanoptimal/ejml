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
import org.ejml.data.Complex_F32;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.MatrixFeatures_CDRM;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.ejml.ops.ComplexMath_F32;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestQrHelperFunctions_CDRM {

    Random rand = new Random(234);

    @Test
    public void findMax() {
        float u[] = new float[50];

        for (int i = 0; i < u.length; i++) {
            u[i] = (rand.nextFloat()-0.5f)*20;
        }

        int offset = 4;
        int length = 5;

        float max = 0;
        for (int i = 0; i < length; i++) {
            float real = u[i*2+offset*2];
            float img = u[i*2+offset*2+1];

            if( real*real + img*img > max ) {
                max = real*real + img*img;
            }
        }

        max = (float)Math.sqrt(max);

        assertEquals(max,QrHelperFunctions_CDRM.findMax(u,offset,length),UtilEjml.TEST_F32);
    }

    @Test
    public void divideElements_startU() {
        float u[] = new float[12*2];
        for (int i = 0; i < u.length; i++ ) {
            u[i] = (rand.nextFloat()*0.5f-1.0f)*2;
        }
        float found[] = u.clone();

        Complex_F32 A = new Complex_F32(rand.nextFloat(),rand.nextFloat());
        Complex_F32 U = new Complex_F32();
        Complex_F32 expected = new Complex_F32();

        int j = 3;
        int numRows = 8;
        int startU = 2;

        QrHelperFunctions_CDRM.divideElements(j,numRows,found,startU,A.real,A.imaginary);

        for (int i = 0; i < 12; i++) {
            int index = i * 2;

            if( i >= j+startU && i < numRows+startU ) {
                U.real = u[index];
                U.imaginary = u[index + 1];
                ComplexMath_F32.divide(U, A, expected);

                assertEquals(expected.real, found[index], UtilEjml.TEST_F32);
                assertEquals(expected.imaginary, found[index + 1], UtilEjml.TEST_F32);
            } else {
                assertEquals(u[index],found[index],UtilEjml.TEST_F32);
                assertEquals(u[index+1],found[index+1],UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void computeTauGammaAndDivide() {
        float u[] = new float[12*2];
        for (int i = 0; i < u.length; i++ ) {
            u[i] = (rand.nextFloat()*0.5f-1.0f)*2;
        }

        float max = 2.0f;
        int j = 2;
        int numRows = 6;

        Complex_F32 expectedTau = new Complex_F32();
        float expectedGamma = 0;
        float[] expectedU = u.clone();

        for (int i = j; i < numRows; i++) {
            Complex_F32 U = new Complex_F32(u[i*2],u[i*2+1]);
            Complex_F32 div = new Complex_F32();
            ComplexMath_F32.divide(U,new Complex_F32(max,0),div);

            expectedU[i*2] = div.real;
            expectedU[i*2+1] = div.imaginary;
        }
        float normX = 0;
        for (int i = j; i < numRows; i++) {
            normX += expectedU[i*2]*expectedU[i*2] + expectedU[i*2+1]*expectedU[i*2+1];
        }
        normX = (float)Math.sqrt(normX);
        float realX0 = expectedU[j*2];
        float imagX0 = expectedU[j*2+1];

        float magX0 = (float)Math.sqrt(realX0*realX0 + imagX0*imagX0);
        expectedTau.real      = realX0*normX/magX0;
        expectedTau.imaginary = imagX0*normX/magX0;

        float realU0 = realX0 + expectedTau.real;
        float imagU0 = imagX0 + expectedTau.imaginary;

        //
        float normU = 1;
        Complex_F32 B = new Complex_F32(realU0,imagU0);
        for (int i = j+1; i < numRows; i++) {
            Complex_F32 A = new Complex_F32( expectedU[i*2], expectedU[i*2+1]);
            Complex_F32 result = new Complex_F32();
            ComplexMath_F32.divide(A,B,result);
            normU += result.getMagnitude2();
        }
        expectedGamma = 2.0f/normU;

        Complex_F32 foundTau = new Complex_F32();
        float[] foundU = u.clone();
        float foundGamma = QrHelperFunctions_CDRM.computeTauGammaAndDivide(j,numRows,foundU,max,foundTau);

        for (int i = 0; i < expectedU.length; i++) {
            assertEquals(expectedU[i],foundU[i],UtilEjml.TEST_F32);
        }

        assertEquals(expectedTau.real,foundTau.real,UtilEjml.TEST_F32);
        assertEquals(expectedTau.imaginary,foundTau.imaginary, UtilEjml.TEST_F32);

        assertEquals(expectedGamma,foundGamma,UtilEjml.TEST_F32);
    }

    @Test
    public void rank1UpdateMultR() {
        float u[] = new float[12*2];
        float uoff[] = new float[12*2+2];
        float subU[] = new float[12*2];
        float _temp[] = new float[u.length];
        float gamma = 0.6f;

        for (int i = 0; i < u.length; i++) {
            u[i] = uoff[i+2] = (rand.nextFloat()*0.5f-1.0f)*2;
        }

        for (int i = 1; i < 12; i++) {
            CMatrixRMaj A = RandomMatrices_CDRM.rectangle(i,i,rand);

            for (int j = 1; j <= i; j += 2) {
                CMatrixRMaj subA = CommonOps_CDRM.extract(A,A.numRows-j,A.numRows,A.numRows-j,A.numRows);
                System.arraycopy(u,(A.numRows-j)*2,subU,0,j*2);
                CMatrixRMaj expected = rank1UpdateMultR(subA, gamma,subU);

                CMatrixRMaj found = A.copy();
                QrHelperFunctions_CDRM.rank1UpdateMultR(found, uoff, 1, gamma,
                        A.numRows - j, A.numRows - j, A.numRows, _temp);

                CMatrixRMaj subFound = CommonOps_CDRM.extract(found,A.numRows-j,A.numRows,A.numRows-j,A.numRows);

                outsideIdentical(A, found, j);
                assertTrue(MatrixFeatures_CDRM.isEquals(expected, subFound, UtilEjml.TEST_F32));
            }
        }
    }

    private CMatrixRMaj rank1UpdateMultR(CMatrixRMaj A , float gamma, float u[] ) {
        CMatrixRMaj U = new CMatrixRMaj(A.numCols,1);
        U.data = u;
        CMatrixRMaj UUt = new CMatrixRMaj(A.numCols,A.numCols);
        CommonOps_CDRM.multTransB(-gamma,0,U,U,UUt);

        CMatrixRMaj expected = A.copy();
        CommonOps_CDRM.multAdd(UUt,A,expected);

        return expected;
    }

    @Test
    public void rank1UpdateMultL() {
        float u[] = new float[12*2];
        float subU[] = new float[12*2];
        float gamma = 0.23f;

        for (int i = 0; i < u.length; i++) {
            u[i] = (rand.nextFloat()*0.5f-1.0f)*2;
        }

        for (int i = 1; i < 12; i++) {
            CMatrixRMaj A = RandomMatrices_CDRM.rectangle(i,i,rand);

            for (int j = 1; j <= i; j += 2) {
                CMatrixRMaj subA = CommonOps_CDRM.extract(A,A.numRows-j,A.numRows,A.numRows-j,A.numRows);
                System.arraycopy(u,(A.numRows-j)*2,subU,0,j*2);
                CMatrixRMaj expected = rank1UpdateMultL(subA,gamma,subU);

                CMatrixRMaj found = A.copy();
                QrHelperFunctions_CDRM.rank1UpdateMultL(found,u,0,gamma,
                        A.numRows-j,A.numRows-j,A.numRows);

                CMatrixRMaj subFound = CommonOps_CDRM.extract(found,A.numRows-j,A.numRows,A.numRows-j,A.numRows);

                outsideIdentical(A, found, j);
                assertTrue(MatrixFeatures_CDRM.isEquals(expected, subFound, UtilEjml.TEST_F32));
            }
        }
    }

    private CMatrixRMaj rank1UpdateMultL(CMatrixRMaj A , float gamma, float u[] ) {
        CMatrixRMaj U = new CMatrixRMaj(A.numCols,1);
        U.data = u;
        CMatrixRMaj UUt = new CMatrixRMaj(A.numCols,A.numCols);
        CommonOps_CDRM.multTransB(-gamma,0,U,U,UUt);

        CMatrixRMaj expected = A.copy();
        CommonOps_CDRM.multAdd(A,UUt,expected);

        return expected;
    }

    private void outsideIdentical(CMatrixRMaj A , CMatrixRMaj B , int width ) {

        int outside = A.numRows-width;

        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
                if( i < outside || j < outside ) {
                    assertEquals(A.getReal(i,j),B.getReal(i,j),UtilEjml.TEST_F32);
                    assertEquals(A.getImag(i, j), B.getImag(i,j),UtilEjml.TEST_F32);
                }
            }
        }
    }

    @Test
    public void extractHouseholderColumn() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(6,5,rand);

        float u[] = new float[6*2];

        QrHelperFunctions_CDRM.extractHouseholderColumn(A,1,5,1,u,1);

        assertEquals(1 , u[4], UtilEjml.TEST_F32);
        assertEquals(0 , u[5], UtilEjml.TEST_F32);

        for (int i = 2; i < 5; i++) {
            float real = A.getReal(i,1);
            float imag = A.getImag(i,1);

            assertEquals(u[(i+1)*2]   , real , UtilEjml.TEST_F32);
            assertEquals(u[(i+1)*2+1] , imag , UtilEjml.TEST_F32);
        }
    }

    @Test
    public void extractHouseholderRow() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(5,6,rand);

        float u[] = new float[6*2];

        QrHelperFunctions_CDRM.extractHouseholderRow(A,1,1,5,u,1);

        assertEquals(1 , u[4], UtilEjml.TEST_F32);
        assertEquals(0 , u[5], UtilEjml.TEST_F32);

        for (int i = 2; i < 5; i++) {
            float real = A.getReal(1,i);
            float imag = A.getImag(1,i);

            assertEquals(u[(i+1)*2]   , real , UtilEjml.TEST_F32);
            assertEquals(u[(i+1)*2+1] , imag , UtilEjml.TEST_F32);
        }
    }

    @Test
    public void extractColumnAndMax() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(5,6,rand);

        A.set(2,1,10,0);

        float u[] = new float[6*2];
        float max = QrHelperFunctions_CDRM.extractColumnAndMax(A,1,5,1,u,1);

        assertEquals(10, max, UtilEjml.TEST_F32);

        for (int i = 1; i < 5; i++) {
            float real = A.getReal(i,1);
            float imag = A.getImag(i,1);

            assertEquals(u[(i+1)*2]   , real , UtilEjml.TEST_F32);
            assertEquals(u[(i+1)*2+1] , imag , UtilEjml.TEST_F32);
        }
    }

    @Test
    public void computeRowMax() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(5,6,rand);

        A.set(1,2,10,0);

        float max = QrHelperFunctions_CDRM.computeRowMax(A,1,1,5);

        assertEquals(10, max, UtilEjml.TEST_F32);
    }
}
