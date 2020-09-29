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

package org.ejml.dense.block.decompose.hessenberg;

import org.ejml.data.DMatrixRBlock;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.block.decomposition.hessenberg.TridiagonalDecompositionHouseholder_DDRB;
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
@Fork(value = 2)
public class BenchmarkDecompositionHessenberg_DDRB {
    //    @Param({"100", "500", "1000", "5000", "10000"})
    @Param({"2000"})
    public int size;

    public DMatrixRBlock S, A;

    TridiagonalDecompositionHouseholder_DDRB tridiagonal = new TridiagonalDecompositionHouseholder_DDRB();

    @Setup
    public void setup() {
        Random rand = new Random(234);

        S = MatrixOps_DDRB.convert(RandomMatrices_DDRM.symmetric(size, -1, 1, rand));
    }

    @Benchmark
    public void tridiagonal() {
        DMatrixRBlock A = tridiagonal.inputModified() ? S.copy() : S;
        tridiagonal.decompose(A);
    }

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkDecompositionHessenberg_DDRB.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
