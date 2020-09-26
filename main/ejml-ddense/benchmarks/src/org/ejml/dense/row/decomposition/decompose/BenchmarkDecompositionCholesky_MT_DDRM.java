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

package org.ejml.dense.row.decomposition.decompose;

import org.ejml.EjmlParameters;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.chol.CholeskyDecompositionBlock_MT_DDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
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
public class BenchmarkDecompositionCholesky_MT_DDRM {
    @Param({"100", "500", "1000", "5000", "10000"})
//    @Param({"2000","4000"})
    public int size;

    public DMatrixRMaj A,L;

    CholeskyDecomposition_F64<DMatrixRMaj> block = new CholeskyDecompositionBlock_MT_DDRM(EjmlParameters.BLOCK_WIDTH_CHOL);

    @Setup
    public void setup() {
        Random rand = new Random(234);

        A = RandomMatrices_DDRM.symmetricPosDef(size,rand);
        L = new DMatrixRMaj(1,1);
    }

    @Benchmark
    public void block() {
        if( !block.decompose(A.copy()) )
            throw new RuntimeException("FAILED?!");
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkDecompositionCholesky_MT_DDRM.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
