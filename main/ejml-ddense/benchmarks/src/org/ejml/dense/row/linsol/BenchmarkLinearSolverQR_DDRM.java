/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.qr.QRColPivDecompositionHouseholderColumn_DDRM;
import org.ejml.dense.row.linsol.qr.LinearSolverQrHouseCol_DDRM;
import org.ejml.dense.row.linsol.qr.LinearSolverQrpHouseCol_DDRM;
import org.ejml.dense.row.linsol.qr.SolvePseudoInverseQrp_DDRM;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Peter Abeles
 */
@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 2)
public class BenchmarkLinearSolverQR_DDRM {
    //    @Param({"100", "500", "1000", "5000", "10000"})
    @Param({"10", "2000"})
    public int size;

    public DMatrixRMaj A, X, B;

    LinearSolverQrHouseCol_DDRM houseCol = new LinearSolverQrHouseCol_DDRM();
    LinearSolverQrpHouseCol_DDRM houseQrp = new LinearSolverQrpHouseCol_DDRM(
            new QRColPivDecompositionHouseholderColumn_DDRM(),true);
    SolvePseudoInverseQrp_DDRM pinvQrp = new SolvePseudoInverseQrp_DDRM(
            new QRColPivDecompositionHouseholderColumn_DDRM(),true);

    @Setup
    public void setup() {
        Random rand = new Random(234);

        A = RandomMatrices_DDRM.rectangle(size*2, size/2, -1, 1, rand);
        B = RandomMatrices_DDRM.rectangle(A.numRows, 20, -1, 1, rand);
        X = new DMatrixRMaj(A.numRows, B.numCols);
    }

    @Benchmark
    public void houseCol() {
        DMatrixRMaj A = houseCol.modifiesA() ? this.A.copy() : this.A;
        DMatrixRMaj B = houseCol.modifiesB() ? this.B.copy() : this.B;
        houseCol.setA(A);
        houseCol.solve(B, X);
    }

    @Benchmark
    public void houseQrp() {
        DMatrixRMaj A = houseQrp.modifiesA() ? this.A.copy() : this.A;
        DMatrixRMaj B = houseQrp.modifiesB() ? this.B.copy() : this.B;
        houseQrp.setA(A);
        houseQrp.solve(B, X);
    }

    @Benchmark
    public void pinvQrp() {
        DMatrixRMaj A = pinvQrp.modifiesA() ? this.A.copy() : this.A;
        DMatrixRMaj B = pinvQrp.modifiesB() ? this.B.copy() : this.B;
        pinvQrp.setA(A);
        pinvQrp.solve(B, X);
    }

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkLinearSolverQR_DDRM.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
