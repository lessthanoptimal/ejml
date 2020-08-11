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

package org.ejml.sparse.csc.decomposition.qr;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.data.FScalar;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.decomposition.qr.QrHelperFunctions_FDRM;
import org.ejml.sparse.csc.CommonOps_FSCC;
import org.ejml.sparse.csc.RandomMatrices_FSCC;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestQrHelperFunctions_FSCC {

    Random rand = new Random(234);

    @Test
    public void applyHouseholder() {
        FMatrixSparseCSC V = UtilEjml.parse_FSCC(
                      "1 0 0 0 0 " +
                        "2 1 0 0 0 "+
                        "0 3 1 0 0 " +
                        "0 0 0 1 0 " +
                        "4 1 4 5 1",5);

        float []x = new float[]{1,2,3,4,5};
        QrHelperFunctions_FSCC.applyHouseholder(V,1,2.1f,x);

        // hand computed solution
        float []expected = new float[]{1,-31.6f,-97.8f,4,-28.6f};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i],x[i],UtilEjml.TEST_F32);
        }
    }

    @Test
    public void rank1UpdateMultR() {
        FMatrixSparseCSC V = RandomMatrices_FSCC.rectangle(5,6,20,rand);
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(5,5,18,rand);

        FMatrixSparseCSC v = new FMatrixSparseCSC(5,1,0);
        CommonOps_FSCC.extractColumn(V,2,v);

        float gamma = 2.1f;

        // Compute results using other more verbose functions
        // C = (I - gamma*v*v<')*A
        // v'*A
        FMatrixSparseCSC vA = new FMatrixSparseCSC(1,A.numCols,0);
        CommonOps_FSCC.multTransA(v,A,vA,null,null);
        // B = -gamma*v*(v'*A)
        FMatrixSparseCSC B = new FMatrixSparseCSC(A.numRows,A.numCols,0);
        CommonOps_FSCC.mult(v,vA,B);
        CommonOps_FSCC.scale(-gamma,B,B);
        // expected = A + B
        FMatrixSparseCSC expected = new FMatrixSparseCSC(A.numRows,A.numCols,0);
        CommonOps_FSCC.add(1.0f,A,1,B,expected,null,null);

        // compute results using the function being tested
        FMatrixSparseCSC found = new FMatrixSparseCSC(A.numRows,0,0);
        QrHelperFunctions_FSCC.rank1UpdateMultR(V,2,gamma,A,found,null,null);
        assertTrue(CommonOps_FSCC.checkStructure(found));

        EjmlUnitTests.assertEquals(expected,found,UtilEjml.TEST_F32);
    }

    @Test
    public void computeHouseholder() {
        int N = 10;
        int offset = 1;
        FMatrixRMaj x = RandomMatrices_FDRM.rectangle(N,1,rand);
        FMatrixRMaj v = x.copy();

        FScalar beta = new FScalar();

        float max = QrHelperFunctions_FDRM.findMax(v.data,offset,N-offset);
        float tau = QrHelperFunctions_FSCC.computeHouseholder(v.data,offset,N,max,beta);

        QrHelperFunctions_FDRM.rank1UpdateMultR(x,v.data,beta.value,0,offset,N,new float[N]);

        assertEquals(tau,x.data[offset],UtilEjml.TEST_F32);
        for (int i = offset+1; i < N; i++) {
            assertEquals(0,x.data[i], UtilEjml.TEST_F32);
        }
    }
}
