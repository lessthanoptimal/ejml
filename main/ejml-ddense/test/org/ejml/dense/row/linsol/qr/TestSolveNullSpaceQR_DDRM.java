/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.linsol.qr;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestSolveNullSpaceQR_DDRM {
    Random rand = new Random(23423);

    @Test
    public void random_1() {

        SolveNullSpaceQR_DDRM solver = new SolveNullSpaceQR_DDRM();

        DMatrixRMaj ns = new DMatrixRMaj(1,1);

        int cols = 5;
        double[] sv = new double[cols];
        for (int rows : new int[]{cols,10,20,200}) {
            DMatrixRMaj r = new DMatrixRMaj(rows,1);

            for (int i = 0; i < 10; i++) {
                for ( int v = 0; v < cols; v++ ) {
                    sv[v] = rand.nextDouble()*4;
                }
                sv[rand.nextInt(cols)] = 0;

                DMatrixRMaj A = RandomMatrices_DDRM.singular(rows,cols,rand,sv);

                DMatrixRMaj copy = A.copy();

                assertTrue(solver.process(copy,1,ns));
                // make sure it's not a trivial solution
                assertEquals(1, NormOps_DDRM.normF(ns),1e-4);

                // test it with the definition of a null space
                CommonOps_DDRM.mult(A,ns,r);

                assertEquals(0,NormOps_DDRM.normF(r),1e-4);
            }
        }
    }

    @Test
    public void random_N() {
        SolveNullSpaceQR_DDRM solver = new SolveNullSpaceQR_DDRM();

        DMatrixRMaj ns = new DMatrixRMaj(1,1);

        int cols = 10;
        int rows = 500;
        double[] sv = new double[cols];
        int ns_length = 3;

        DMatrixRMaj r = new DMatrixRMaj(rows,ns_length);

        for ( int v = 0; v < cols; v++ ) {
            sv[v] = rand.nextDouble()*4;
        }
        for (int i = 0; i < ns_length; i++) {
            sv[i] = 0;
        }
        DMatrixRMaj A = RandomMatrices_DDRM.singular(rows,cols,rand,sv);

        DMatrixRMaj copy = A.copy();

        assertTrue(solver.process(copy,ns_length,ns));
        assertEquals(ns_length,ns.numCols);


        // make sure it's not a trivial solution
        for (int i = 0; i < ns_length; i++) {
            DMatrixRMaj column = CommonOps_DDRM.extractColumn(ns,ns.numCols-3+i,null);
            assertEquals(1, NormOps_DDRM.normF(column),1e-4);
        }

        CommonOps_DDRM.mult(A,ns,r);
        assertEquals(0,NormOps_DDRM.normF(r),1e-4);
    }

    @Test
    public void underDetermined() {
        SolveNullSpaceQR_DDRM solver = new SolveNullSpaceQR_DDRM();

        DMatrixRMaj ns = new DMatrixRMaj(1,1);

        int rows = 6;
        int cols = 9;
        double[] sv = new double[cols];
        int ns_length = cols-rows;

        DMatrixRMaj r = new DMatrixRMaj(rows,ns_length);

        for ( int v = 0; v < cols; v++ ) {
            sv[v] = rand.nextDouble()*4;
        }

        DMatrixRMaj A = RandomMatrices_DDRM.singular(rows,cols,rand,sv);

        DMatrixRMaj copy = A.copy();

        assertTrue(solver.process(copy,ns_length,ns));
        assertEquals(ns_length,ns.numCols);

        // make sure it's not a trivial solution
        for (int i = 0; i < ns_length; i++) {
            DMatrixRMaj column = CommonOps_DDRM.extractColumn(ns,i,null);
            assertEquals(1, NormOps_DDRM.normF(column),1e-4);
        }

        CommonOps_DDRM.mult(A,ns,r);
        assertEquals(0,NormOps_DDRM.normF(r),1e-4);
    }
}