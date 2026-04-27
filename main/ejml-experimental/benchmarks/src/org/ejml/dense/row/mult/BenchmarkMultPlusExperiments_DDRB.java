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

package org.ejml.dense.row.mult;

import org.ejml.dense.block.InnerMultiplication_DDRB;
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
public class BenchmarkMultPlusExperiments_DDRB {

    @Param({"40", "60", "80"})
    public int m;

    public double[] A;
    public double[] B;
    public double[] C;
    public double[] scratch;

    private double[] C_template;

    public final double alpha = 1.5;

    @Setup
    public void setup() {
        Random rand = new Random(234);

        A = RandomMatrices_DDRM.rectangle(m, m, -1, 1, rand).data;
        B = RandomMatrices_DDRM.rectangle(m, m, -1, 1, rand).data;
        C_template = RandomMatrices_DDRM.rectangle(m, m, -1, 1, rand).data;
        C = C_template.clone();

        scratch = new double[m*m];
    }

    @Setup(Level.Invocation)
    public void resetC() {
        System.arraycopy(C_template, 0, C, 0, C.length);
    }

    @Benchmark public void baseline() {
        InnerMultiplication_DDRB.blockMultPlus(A, B, C, 0, 0, 0, m, m, m);
    }

    @Benchmark public void ikj_for() {
        BlockMultPlusExperiments_DDRB.blockMultPlus_ikj_for(A, B, C, 0, 0, 0, m, m, m);
    }

    @Benchmark public void ijk() {
        BlockMultPlusExperiments_DDRB.blockMultPlus_ijk(A, B, C, 0, 0, 0, m, m, m);
    }

    @Benchmark public void ikj_K4() {
        BlockMultPlusExperiments_DDRB.blockMultPlus_ikj_K4(A, B, C, 0, 0, 0, m, m, m);
    }

    @Benchmark public void ikj() {
        BlockMultPlusExperiments_DDRB.blockMultPlus_ikj(A, B, C, 0, 0, 0, m, m, m);
    }

    @Benchmark public void jik() {
        BlockMultPlusExperiments_DDRB.blockMultPlus_jik(A, B, C, 0, 0, 0, m, m, m);
    }

    @Benchmark public void jik_I4() {
        BlockMultPlusExperiments_DDRB.blockMultPlus_jik_I4(A, B, C, 0, 0, 0, m, m, m);
    }

    @Benchmark public void jki() {
        BlockMultPlusExperiments_DDRB.blockMultPlus_jki(A, B, C, 0, 0, 0, m, m, m);
    }

    @Benchmark public void kij() {
        BlockMultPlusExperiments_DDRB.blockMultPlus_kij(A, B, C, 0, 0, 0, m, m, m);
    }

    @Benchmark public void kij_I4() {
        BlockMultPlusExperiments_DDRB.blockMultPlus_kij_I4(A, B, C, 0, 0, 0, m, m, m);
    }

    @Benchmark public void kji() {
        BlockMultPlusExperiments_DDRB.blockMultPlus_kji(A, B, C, 0, 0, 0, m, m, m);
    }

    @Benchmark public void tile4x4() {
        BlockMultPlusExperiments_DDRB.blockMultPlus_tile4x4(A, B, C, 0, 0, 0, m, m, m);
    }

    @Benchmark public void packed() {
        BlockMultPlusExperiments_DDRB.blockMultPlus_packed(A, B, C, 0, 0, 0, m, m, m);
    }

    @Benchmark public void packed_scratch() {
        BlockMultPlusExperiments_DDRB.blockMultPlus_packed_scratch(A, B, C, 0, 0, 0, m, m, m, scratch);
    }

    @Benchmark public void packed_tile4x4_scratch() {
        BlockMultPlusExperiments_DDRB.blockMultPlus_packed_tile4x4_scratch(A, B, C, 0, 0, 0, m, m, m, scratch);
    }

    @Benchmark public void packed_jik_I4_scratch() {
        BlockMultPlusExperiments_DDRB.blockMultPlus_packed_jik_I4_scratch(A, B, C, 0, 0, 0, m, m, m, scratch);
    }

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkMultPlusExperiments_DDRB.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}