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
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DScalar;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.qr.QrHelperFunctions_DDRM;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestQrHelperFunctions_DSCC {

    Random rand = new Random(234);

    @Test
    public void applyHouseholder() {
        DMatrixSparseCSC V = UtilEjml.parse_DSCC(
                      "1 0 0 0 0 " +
                        "2 1 0 0 0 "+
                        "0 3 1 0 0 " +
                        "0 0 0 1 0 " +
                        "4 1 4 5 1",5);

        double []x = new double[]{1,2,3,4,5};
        QrHelperFunctions_DSCC.applyHouseholder(V,1,2.1,x);

        // hand computed solution
        double []expected = new double[]{1,-31.6,-97.8,4,-28.6};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i],x[i],UtilEjml.TEST_F64);
        }
    }

    @Test
    public void rank1UpdateMultR() {
        DMatrixSparseCSC V = RandomMatrices_DSCC.rectangle(5,6,20,rand);
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(5,5,18,rand);

        DMatrixSparseCSC v = new DMatrixSparseCSC(5,1,0);
        CommonOps_DSCC.extractColumn(V,2,v);

        double gamma = 2.1;

        // Compute results using other more verbose functions
        // C = (I - gamma*v*v<')*A
        // v'*A
        DMatrixSparseCSC vA = new DMatrixSparseCSC(1,A.numCols,0);
        DMatrixSparseCSC v_t = CommonOps_DSCC.transpose(v,null,null);
        CommonOps_DSCC.mult(v_t,A,vA,null,null);
        // B = -gamma*v*(v'*A)
        DMatrixSparseCSC B = new DMatrixSparseCSC(A.numRows,A.numCols,0);
        CommonOps_DSCC.mult(v,vA,B);
        CommonOps_DSCC.scale(-gamma,B,B);
        // expected = A + B
        DMatrixSparseCSC expected = new DMatrixSparseCSC(A.numRows,A.numCols,0);
        CommonOps_DSCC.add(1.0,A,1,B,expected,null,null);

        // compute results using the function being tested
        DMatrixSparseCSC found = new DMatrixSparseCSC(A.numRows,0,0);
        QrHelperFunctions_DSCC.rank1UpdateMultR(V,2,gamma,A,found,null,null);
        assertTrue(CommonOps_DSCC.checkStructure(found));

        EjmlUnitTests.assertEquals(expected,found,UtilEjml.TEST_F64);
    }

    @Test
    public void computeHouseholder() {
        int N = 10;
        int offset = 1;
        DMatrixRMaj x = RandomMatrices_DDRM.rectangle(N,1,rand);
        DMatrixRMaj v = x.copy();

        DScalar beta = new DScalar();

        double max = QrHelperFunctions_DDRM.findMax(v.data,offset,N-offset);
        double tau = QrHelperFunctions_DSCC.computeHouseholder(v.data,offset,N,max,beta);

        QrHelperFunctions_DDRM.rank1UpdateMultR(x,v.data,beta.value,0,offset,N,new double[N]);

        assertEquals(tau,x.data[offset],UtilEjml.TEST_F64);
        for (int i = offset+1; i < N; i++) {
            assertEquals(0,x.data[i], UtilEjml.TEST_F64);
        }
    }
}
