/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.block;

import org.ejml.dense.row.RandomMatrices_DDRM;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
@State(Scope.Benchmark)
@Fork(value = 1)
public class BenchmarkTileRankUpdate_F64 {
    @Param({"60"})
    public int m;

    public double[] A;
    public double[] C;

    private double[] C_template;

    @Setup
    public void setup() {
        Random rand = new Random(234);

        A = RandomMatrices_DDRM.rectangle(m, m*3, -1, 1, rand).data;
        C_template = RandomMatrices_DDRM.rectangle(m, m, -1, 1, rand).data;
        C = C_template.clone();
    }

    @Setup(Level.Invocation)
    public void resetC() {
        System.arraycopy(C_template, 0, C, 0, C.length);
    }

    @Benchmark public void tileMultMinusTransA() {
        TileRankUpdate_F64.tileMultMinusTransA(A,  C, m, m, m, 0, m*m*2, 0);
    }

    @Benchmark public void tileMultMinusTransA_U() {
        TileRankUpdate_F64.tileMultMinusTransA_U(A,  C, m, m, m, 0, m*m*2, 0);
    }

    @Benchmark public void tileMultMinusTransB() {
        TileRankUpdate_F64.tileMultMinusTransB(A,  C, m, m, m, 0, m*m*2, 0);
    }

    @Benchmark public void tileMultMinusTransB_L() {
        TileRankUpdate_F64.tileMultMinusTransB_L(A,  C, m, m, m, 0, m*m*2, 0);
    }

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkTileRankUpdate_F64.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}