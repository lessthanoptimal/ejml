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

package org.ejml.dense.row;

import org.ejml.UtilEjml;
import org.ejml.data.CMatrixRMaj;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestRandomMatrices_CDRM {
    
    Random rand = new Random(234);

    @Test
    public void rectangle_min_max() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(30,20,-1,1,rand);

        checkRandomRange(A);
    }

    @Test
    public void fillUniform() {
        CMatrixRMaj A = new CMatrixRMaj(5,4);

        RandomMatrices_CDRM.fillUniform(A,rand);

        checkUniform1(A);
    }

    private void checkUniform1(CMatrixRMaj a) {
        assertEquals(5, a.numRows);
        assertEquals(4, a.numCols);

        float totalReal = 0;
        float totalImg = 0;
        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < a.numCols; j++ ) {
                float real = a.getReal(i,j);
                float img = a.getImag(i, j);

                assertTrue( real >= 0);
                assertTrue( real <= 1);
                totalReal += real;

                assertTrue( img >= 0);
                assertTrue( img <= 1);
                totalImg += img;
            }
        }

        assertTrue(totalReal>0);
        assertTrue(totalImg>0);
    }

    @Test
    public void fillUniform_min_max() {
        CMatrixRMaj A = new CMatrixRMaj(30,20);
        RandomMatrices_CDRM.fillUniform(A,-1,1,rand);

        checkRandomRange(A);
    }

    private void checkRandomRange(CMatrixRMaj a) {
        assertEquals(30, a.numRows);
        assertEquals(20, a.numCols);

        int numRealNeg = 0;
        int numRealPos = 0;
        int numImgNeg = 0;
        int numImgPos = 0;

        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < a.numCols; j++ ) {
                float real = a.getReal(i,j);
                float img = a.getImag(i,j);

                if( real < 0 )
                    numRealNeg++;
                else
                    numRealPos++;

                if( Math.abs(real) > 1 )
                    fail("Out of range");

                if( img < 0 )
                    numImgNeg++;
                else
                    numImgPos++;

                if( Math.abs(img) > 1 )
                    fail("Out of range");
            }
        }

        assertTrue(numRealNeg>0);
        assertTrue(numRealPos>0);

        assertTrue(numImgNeg>0);
        assertTrue(numImgPos>0);
    }


    @Test
    public void hermitianPosDef() {
        for( int i = 1; i < 20; i++ ) {
            CMatrixRMaj A = RandomMatrices_CDRM.hermitianPosDef(i, rand);

            assertTrue(MatrixFeatures_CDRM.isPositiveDefinite(A));
        }
    }

    @Test
    public void hermitian() {
        CMatrixRMaj A = RandomMatrices_CDRM.hermitian(10, -1, 1, rand);

        assertTrue(MatrixFeatures_CDRM.isHermitian(A, UtilEjml.TEST_F32));

        // see if it has the expected range of elements
        float min = CommonOps_CDRM.elementMinReal(A);
        float max = CommonOps_CDRM.elementMaxReal(A);

        assertTrue(min < 0 && min >= -1);
        assertTrue(max > 0 && max <=  1);

        min = CommonOps_CDRM.elementMinImaginary(A);
        max = CommonOps_CDRM.elementMaxImaginary(A);

        assertTrue(min < 0 && min >= -1);
        assertTrue(max > 0 && max <=  1);
    }
//
//    @Test
//    public void createUpperTriangle() {
//        for( int hess = 0; hess < 3; hess++ ) {
//            CMatrixRMaj A = RandomMatrices_CDRM.createUpperTriangle(10,hess,-1,1,rand);
//
//            assertTrue(MatrixFeatures.isUpperTriangle(A,hess,UtilEjml.TEST_F32));
//
//            // quick sanity check to make sure it could be proper
//            assertTrue(A.get(hess,0) != 0 );
//
//            // see if it has the expected range of elements
//            float min = CommonOps.elementMin(A);
//            float max = CommonOps.elementMax(A);
//
//            assertTrue(min < 0 && min >= -1);
//            assertTrue(max > 0 && max <= 1);
//        }
//    }
}
