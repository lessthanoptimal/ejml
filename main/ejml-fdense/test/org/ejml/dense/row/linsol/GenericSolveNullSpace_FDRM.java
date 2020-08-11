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

package org.ejml.dense.row.linsol;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.NormOps_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.interfaces.SolveNullSpace;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public abstract class GenericSolveNullSpace_FDRM {
    public Random rand = new Random(23423);

    public abstract SolveNullSpace<FMatrixRMaj> createSolver();

    @Test
    public void random_1() {

        SolveNullSpace<FMatrixRMaj> solver = createSolver();

        FMatrixRMaj ns = new FMatrixRMaj(1,1);

        int cols = 5;
        float[] sv = new float[cols];
        for (int rows : new int[]{cols,10,20,200}) {
            FMatrixRMaj r = new FMatrixRMaj(rows,1);

            for (int i = 0; i < 10; i++) {
                for ( int v = 0; v < cols; v++ ) {
                    sv[v] = rand.nextFloat()*4;
                }
                sv[rand.nextInt(cols)] = 0;

                FMatrixRMaj A = RandomMatrices_FDRM.singular(rows,cols,rand,sv);

                FMatrixRMaj copy = A.copy();

                assertTrue(solver.process(copy,1,ns));
                // make sure it's not a trivial solution
                assertEquals(1, NormOps_FDRM.normF(ns),1e-4);

                // test it with the definition of a null space
                CommonOps_FDRM.mult(A,ns,r);

                assertEquals(0,NormOps_FDRM.normF(r),1e-4);
            }
        }
    }

    @Test
    public void random_N() {
        SolveNullSpace<FMatrixRMaj> solver = createSolver();

        FMatrixRMaj ns = new FMatrixRMaj(1,1);

        int cols = 10;
        int rows = 500;
        float[] sv = new float[cols];
        int ns_length = 3;

        FMatrixRMaj r = new FMatrixRMaj(rows,ns_length);

        for ( int v = 0; v < cols; v++ ) {
            sv[v] = rand.nextFloat()*4;
        }
        for (int i = 0; i < ns_length; i++) {
            sv[i] = 0;
        }
        FMatrixRMaj A = RandomMatrices_FDRM.singular(rows,cols,rand,sv);

        FMatrixRMaj copy = A.copy();

        assertTrue(solver.process(copy,ns_length,ns));
        assertEquals(ns_length,ns.numCols);


        // make sure it's not a trivial solution
        for (int i = 0; i < ns_length; i++) {
            FMatrixRMaj column = CommonOps_FDRM.extractColumn(ns,ns.numCols-3+i,null);
            assertEquals(1, NormOps_FDRM.normF(column),1e-4);
        }

        CommonOps_FDRM.mult(A,ns,r);
        assertEquals(0,NormOps_FDRM.normF(r),1e-4);
    }

    @Test
    public void underDetermined() {
        SolveNullSpace<FMatrixRMaj> solver = createSolver();

        FMatrixRMaj ns = new FMatrixRMaj(1,1);

        int rows = 6;
        int cols = 9;
        float[] sv = new float[cols];
        int ns_length = cols-rows;

        FMatrixRMaj r = new FMatrixRMaj(rows,ns_length);

        for ( int v = 0; v < cols; v++ ) {
            sv[v] = rand.nextFloat()*4;
        }

        FMatrixRMaj A = RandomMatrices_FDRM.singular(rows,cols,rand,sv);

        FMatrixRMaj copy = A.copy();

        assertTrue(solver.process(copy,ns_length,ns));
        assertEquals(ns_length,ns.numCols);

        // make sure it's not a trivial solution
        for (int i = 0; i < ns_length; i++) {
            FMatrixRMaj column = CommonOps_FDRM.extractColumn(ns,i,null);
            assertEquals(1, NormOps_FDRM.normF(column),1e-4);
        }

        CommonOps_FDRM.mult(A,ns,r);
        assertEquals(0,NormOps_FDRM.normF(r),1e-4);
    }

    @Test
    public void handleHardMatrix() {
        float[][] array = new float[][]{
                {0.0000000000f, 0.0000000000f, 0.0000000000f, 1.3416407865f, -1.4638501094f, -1.0000000000f, 1.9492820629f, -2.1268410962f, -1.4529090667f},
                {-1.3416407865f, 1.4638501094f, 1.0000000000f, 0.0000000000f, 0.0000000000f, 0.0000000000f, 1.8336130868f, -2.0006358965f, -1.3666945021f},
                {0.0000000000f, 0.0000000000f, 0.0000000000f, 0.4472135955f, -1.4638501094f, -1.0000000000f, 0.6533542111f, -2.1386036627f, -1.4609444293f},
                {-0.4472135955f, 1.4638501094f, 1.0000000000f, 0.0000000000f, 0.0000000000f, 0.0000000000f, 0.2118801923f, -0.6935407283f, -0.4737785131f},
                {0.0000000000f, 0.0000000000f, 0.0000000000f, -0.4472135955f, -1.4638501094f, -1.0000000000f, -0.6569585349f, -2.1504015817f, -1.4690039423f},
                {0.4472135955f, 1.4638501094f, 1.0000000000f, 0.0000000000f, 0.0000000000f, 0.0000000000f, 0.1886441486f, 0.6174829218f, 0.4218211399f},
                {0.0000000000f, 0.0000000000f, 0.0000000000f, -1.3416407865f, -1.4638501094f, -1.0000000000f, -1.9817211233f, -2.1622350128f, -1.4770877147f},
                {1.3416407865f, 1.4638501094f, 1.0000000000f, 0.0000000000f, 0.0000000000f, 0.0000000000f, 1.7711222385f, 1.9324527912f, 1.3201165739f},
                {0.0000000000f, 0.0000000000f, 0.0000000000f, 1.3416407865f, -0.8783100657f, -1.0000000000f, 1.1636115267f, -0.7617625572f, -0.8673048243f},
                {-1.3416407865f, 0.8783100657f, 1.0000000000f, 0.0000000000f, 0.0000000000f, 0.0000000000f, 1.8193230223f, -1.1910264947f, -1.3560433169f},
                {0.0000000000f, 0.0000000000f, 0.0000000000f, 0.4472135955f, -0.8783100657f, -1.0000000000f, 0.3910691485f, -0.7680445608f, -0.8744572001f}};

        SolveNullSpace<FMatrixRMaj> solver = createSolver();

        FMatrixRMaj ns = new FMatrixRMaj(1,1);

        FMatrixRMaj A = new FMatrixRMaj(array);

        assertTrue(solver.process(A.copy(),1,ns));
        assertEquals(1,ns.numCols);

        // make sure it's not a trivial solution
        for (int i = 0; i < 1; i++) {
            FMatrixRMaj column = CommonOps_FDRM.extractColumn(ns,i,null);
            assertEquals(1, NormOps_FDRM.normF(column),1e-4);
        }

        FMatrixRMaj r = new FMatrixRMaj(A.numRows,1);
        CommonOps_FDRM.mult(A,ns,r);
        assertEquals(0,NormOps_FDRM.normF(r),1e-4);
    }
}