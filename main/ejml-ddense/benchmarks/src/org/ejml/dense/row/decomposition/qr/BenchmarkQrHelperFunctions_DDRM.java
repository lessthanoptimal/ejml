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

package org.ejml.dense.row.decomposition.qr;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value=2)
public class BenchmarkQrHelperFunctions_DDRM {
    @Param({"1000"})
    public int size;

    public DMatrixRMaj A,Q,R;
    public double[] u;
    public double[] v;

    @Setup
    public void setup() {
        Random rand = new Random(234);

        A = RandomMatrices_DDRM.rectangle(size*2,size/2,-1,1, rand);
        Q = new DMatrixRMaj(size,size);
        R = new DMatrixRMaj(Q.numCols,Q.numCols);

        v = new double[size];
        u = new double[size];
        for (int i = 0; i < size; i++) {
            u[i] = rand.nextGaussian();
        }
    }

    @Benchmark
    public void rank1UpdateMultR_u0() {
        QrHelperFunctions_DDRM.rank1UpdateMultR_u0(Q, u, 1.0, 1.2, 0, 0, size, v);
    }

    @Benchmark
    public void rank1UpdateMultR() {
        QrHelperFunctions_DDRM.rank1UpdateMultR(Q, u, 0, 1.2, 0, 0, size, v);
    }

    @Benchmark
    public void rank1UpdateMultL() {
        QrHelperFunctions_DDRM.rank1UpdateMultL(Q, u, 1.0, 0, 0, size);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkQrHelperFunctions_DDRM.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
