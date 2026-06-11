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
public class BenchmarkTileMultiplication_F64 {

    @Param({"60"})
    public int m;

    public double[] A;
    public double[] B;
    public double[] C;

    private double[] C_template;

    public final double alpha = 1.5;

    @Setup
    public void setup() {
        Random rand = new Random(234);

        A = RandomMatrices_DDRM.rectangle(m, m, -1, 1, rand).data;
        B = RandomMatrices_DDRM.rectangle(m, m, -1, 1, rand).data;
        C_template = RandomMatrices_DDRM.rectangle(m, m, -1, 1, rand).data;
        C = C_template.clone();
    }

    @Setup(Level.Invocation)
    public void resetC() {
        System.arraycopy(C_template, 0, C, 0, C.length);
    }

    @Benchmark public void tileMultPlus() {
        TileMultiplication_F64.tileMultPlus(A, B, C, m, m, m, 0, 0, 0);
    }

    @Benchmark public void tileMultPlusTransA() {
        TileMultiplication_F64.tileMultPlusTransA(A, B, C, m, m, m, 0, 0, 0);
    }

    @Benchmark public void tileMultPlusTransB() {
        TileMultiplication_F64.tileMultPlusTransB(A, B, C, m, m, m, 0, 0, 0);
    }

    @Benchmark public void tileMultMinus() {
        TileMultiplication_F64.tileMultMinus(A, B, C, m, m, m, 0, 0, 0);
    }

    @Benchmark public void tileMultMinusTransA() {
        TileMultiplication_F64.tileMultMinusTransA(A, B, C, m, m, m, 0, 0, 0);
    }

    @Benchmark public void tileMultMinusTransB() {
        TileMultiplication_F64.tileMultMinusTransB(A, B, C, m, m, m, 0, 0, 0);
    }

    @Benchmark public void tileMultSet() {
        TileMultiplication_F64.tileMultSet(A, B, C, m, m, m, 0, 0, 0);
    }

    @Benchmark public void tileMultSetTransA() {
        TileMultiplication_F64.tileMultSetTransA(A, B, C, m, m, m, 0, 0, 0);
    }

    @Benchmark public void tileMultSetTransB() {
        TileMultiplication_F64.tileMultSetTransB(A, B, C, m, m, m, 0, 0, 0);
    }

    @Benchmark public void tileMultPlus_alpha() {
        TileMultiplication_F64.tileMultPlus(alpha, A, B, C, m, m, m, 0, 0, 0);
    }

    @Benchmark public void tileMultPlusTransA_alpha() {
        TileMultiplication_F64.tileMultPlusTransA(alpha, A, B, C, m, m, m, 0, 0, 0);
    }

    @Benchmark public void tileMultPlusTransB_alpha() {
        TileMultiplication_F64.tileMultPlusTransB(alpha, A, B, C, m, m, m, 0, 0, 0);
    }

    @Benchmark public void tileMultSet_alpha() {
        TileMultiplication_F64.tileMultSet(alpha, A, B, C, m, m, m, 0, 0, 0);
    }

    @Benchmark public void tileMultSetTransA_alpha() {
        TileMultiplication_F64.tileMultSetTransA(alpha, A, B, C, m, m, m, 0, 0, 0);
    }

    @Benchmark public void tileMultSetTransB_alpha() {
        TileMultiplication_F64.tileMultSetTransB(alpha, A, B, C, m, m, m, 0, 0, 0);
    }

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkTileMultiplication_F64.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}