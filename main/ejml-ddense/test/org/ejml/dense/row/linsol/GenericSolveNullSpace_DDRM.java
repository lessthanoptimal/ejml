/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlStandardJUnit;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.interfaces.SolveNullSpace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class GenericSolveNullSpace_DDRM extends EjmlStandardJUnit {
    public abstract SolveNullSpace<DMatrixRMaj> createSolver();

    @Test void random_1() {
        SolveNullSpace<DMatrixRMaj> solver = createSolver();

        DMatrixRMaj ns = new DMatrixRMaj(1, 1);

        int cols = 5;
        double[] sv = new double[cols];
        for (int rows : new int[]{cols, 10, 20, 200}) {
            DMatrixRMaj r = new DMatrixRMaj(rows, 1);

            for (int i = 0; i < 10; i++) {
                for (int v = 0; v < cols; v++) {
                    sv[v] = rand.nextDouble()*4;
                }
                sv[rand.nextInt(cols)] = 0;

                DMatrixRMaj A = RandomMatrices_DDRM.singular(rows, cols, rand, sv);

                DMatrixRMaj copy = A.copy();

                assertTrue(solver.process(copy, 1, ns));
                // make sure it's not a trivial solution
                assertEquals(1, NormOps_DDRM.normF(ns), 1e-4);

                // test it with the definition of a null space
                CommonOps_DDRM.mult(A, ns, r);

                assertEquals(0, NormOps_DDRM.normF(r), 1e-4);
            }
        }
    }

    @Test void random_N() {
        SolveNullSpace<DMatrixRMaj> solver = createSolver();

        DMatrixRMaj ns = new DMatrixRMaj(1, 1);

        int cols = 10;
        int rows = 500;
        double[] sv = new double[cols];
        int ns_length = 3;

        DMatrixRMaj r = new DMatrixRMaj(rows, ns_length);

        for (int v = 0; v < cols; v++) {
            sv[v] = rand.nextDouble()*4;
        }
        for (int i = 0; i < ns_length; i++) {
            sv[i] = 0;
        }
        DMatrixRMaj A = RandomMatrices_DDRM.singular(rows, cols, rand, sv);

        DMatrixRMaj copy = A.copy();

        assertTrue(solver.process(copy, ns_length, ns));
        assertEquals(ns_length, ns.numCols);


        // make sure it's not a trivial solution
        for (int i = 0; i < ns_length; i++) {
            DMatrixRMaj column = CommonOps_DDRM.extractColumn(ns, ns.numCols - 3 + i, null);
            assertEquals(1, NormOps_DDRM.normF(column), 1e-4);
        }

        CommonOps_DDRM.mult(A, ns, r);
        assertEquals(0, NormOps_DDRM.normF(r), 1e-4);
    }

    @Test void underDetermined() {
        SolveNullSpace<DMatrixRMaj> solver = createSolver();

        DMatrixRMaj ns = new DMatrixRMaj(1, 1);

        int rows = 6;
        int cols = 9;
        double[] sv = new double[cols];
        int ns_length = cols - rows;

        DMatrixRMaj r = new DMatrixRMaj(rows, ns_length);

        for (int v = 0; v < cols; v++) {
            sv[v] = rand.nextDouble()*4;
        }

        DMatrixRMaj A = RandomMatrices_DDRM.singular(rows, cols, rand, sv);

        DMatrixRMaj copy = A.copy();

        assertTrue(solver.process(copy, ns_length, ns));
        assertEquals(ns_length, ns.numCols);

        // make sure it's not a trivial solution
        for (int i = 0; i < ns_length; i++) {
            DMatrixRMaj column = CommonOps_DDRM.extractColumn(ns, i, null);
            assertEquals(1, NormOps_DDRM.normF(column), 1e-4);
        }

        CommonOps_DDRM.mult(A, ns, r);
        assertEquals(0, NormOps_DDRM.normF(r), 1e-4);
    }

    @Test public void handleHardMatrix() {
        double[][] array = new double[][]{
                {0.0000000000, 0.0000000000, 0.0000000000, 1.3416407865, -1.4638501094, -1.0000000000, 1.9492820629, -2.1268410962, -1.4529090667},
                {-1.3416407865, 1.4638501094, 1.0000000000, 0.0000000000, 0.0000000000, 0.0000000000, 1.8336130868, -2.0006358965, -1.3666945021},
                {0.0000000000, 0.0000000000, 0.0000000000, 0.4472135955, -1.4638501094, -1.0000000000, 0.6533542111, -2.1386036627, -1.4609444293},
                {-0.4472135955, 1.4638501094, 1.0000000000, 0.0000000000, 0.0000000000, 0.0000000000, 0.2118801923, -0.6935407283, -0.4737785131},
                {0.0000000000, 0.0000000000, 0.0000000000, -0.4472135955, -1.4638501094, -1.0000000000, -0.6569585349, -2.1504015817, -1.4690039423},
                {0.4472135955, 1.4638501094, 1.0000000000, 0.0000000000, 0.0000000000, 0.0000000000, 0.1886441486, 0.6174829218, 0.4218211399},
                {0.0000000000, 0.0000000000, 0.0000000000, -1.3416407865, -1.4638501094, -1.0000000000, -1.9817211233, -2.1622350128, -1.4770877147},
                {1.3416407865, 1.4638501094, 1.0000000000, 0.0000000000, 0.0000000000, 0.0000000000, 1.7711222385, 1.9324527912, 1.3201165739},
                {0.0000000000, 0.0000000000, 0.0000000000, 1.3416407865, -0.8783100657, -1.0000000000, 1.1636115267, -0.7617625572, -0.8673048243},
                {-1.3416407865, 0.8783100657, 1.0000000000, 0.0000000000, 0.0000000000, 0.0000000000, 1.8193230223, -1.1910264947, -1.3560433169},
                {0.0000000000, 0.0000000000, 0.0000000000, 0.4472135955, -0.8783100657, -1.0000000000, 0.3910691485, -0.7680445608, -0.8744572001}};

        SolveNullSpace<DMatrixRMaj> solver = createSolver();

        DMatrixRMaj ns = new DMatrixRMaj(1, 1);

        DMatrixRMaj A = new DMatrixRMaj(array);

        assertTrue(solver.process(A.copy(), 1, ns));
        assertEquals(1, ns.numCols);

        // make sure it's not a trivial solution
        for (int i = 0; i < 1; i++) {
            DMatrixRMaj column = CommonOps_DDRM.extractColumn(ns, i, null);
            assertEquals(1, NormOps_DDRM.normF(column), 1e-4);
        }

        DMatrixRMaj r = new DMatrixRMaj(A.numRows, 1);
        CommonOps_DDRM.mult(A, ns, r);
        assertEquals(0, NormOps_DDRM.normF(r), 1e-4);
    }
}