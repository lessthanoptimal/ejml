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
import org.ejml.dense.row.linsol.qr.LinearSolverQrHouseCol_MT_DDRM;
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
public class BenchmarkLinearSolverQR_MT_DDRM {
    //    @Param({"100", "500", "1000", "5000", "10000"})
    @Param({"10", "2000"})
    public int size;

    public DMatrixRMaj A, X, B;

    LinearSolverQrHouseCol_MT_DDRM houseCol = new LinearSolverQrHouseCol_MT_DDRM();

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

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkLinearSolverQR_MT_DDRM.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
