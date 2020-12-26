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

package org.ejml.dense.row.misc;

import org.ejml.EjmlParameters;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Compare concurrent vs non-concurrent functions in CommonOps
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 2)
public class BenchmarkTransposeAlgs_DDRM {
    //    @Param({"100", "500", "1000", "5000", "10000"})
    @Param({"1000"})
    public int size;

    public DMatrixRMaj S1 = new DMatrixRMaj(1, 1);
    public DMatrixRMaj S2 = new DMatrixRMaj(1, 1);
    public DMatrixRMaj R1 = new DMatrixRMaj(1, 1);
    public DMatrixRMaj R2 = new DMatrixRMaj(1, 1);

    @Setup
    public void setup() {
        Random rand = new Random(234);

        S1.reshape(size, size);
        S2.reshape(size, size);
        R1.reshape(size*10/7, size*7/10);
        R2.reshape(R1.numCols, R1.numRows);

        RandomMatrices_DDRM.fillUniform(S1, -1, 1, rand);
        RandomMatrices_DDRM.fillUniform(R1, -1, 1, rand);
    }

    // @formatter:off
    @Benchmark public void square_inplace() { TransposeAlgs_DDRM.square(S1); }
    @Benchmark public void square_standard() { TransposeAlgs_DDRM.standard(S1, S2); }
    @Benchmark public void standard() { TransposeAlgs_DDRM.standard(R1, R2); }
    @Benchmark public void block() { TransposeAlgs_DDRM.block(R1, R2, EjmlParameters.BLOCK_WIDTH); }
    @Benchmark public void block_2x() { TransposeAlgs_DDRM.block(R1, R2, 2*EjmlParameters.BLOCK_WIDTH); }
    // @formatter:on

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkTransposeAlgs_DDRM.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
