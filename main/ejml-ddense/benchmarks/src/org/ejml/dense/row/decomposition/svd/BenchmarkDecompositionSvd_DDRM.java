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

package org.ejml.dense.row.decomposition.svd;

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
public class BenchmarkDecompositionSvd_DDRM {
    //    @Param({"100", "500", "1000", "5000", "10000"})
    @Param({"1000"})
    public int size;

    public DMatrixRMaj A,U,V;

    SvdImplicitQrDecompose_DDRM svd = new SvdImplicitQrDecompose_DDRM(true,true,true,true);

    @Setup
    public void setup() {
        Random rand = new Random(234);

        A = RandomMatrices_DDRM.rectangle(size*2,size/2,-1,1, rand);
        U = new DMatrixRMaj(size,size);
        V = new DMatrixRMaj(size,size);
    }

    @Benchmark
    public void implicit_uv() {
        svd.decompose(A.copy());
        svd.getU(U,false);
        svd.getV(V,true);
    }

    @Benchmark
    public void implicit() {
        svd.decompose(A.copy());
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkDecompositionSvd_DDRM.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
