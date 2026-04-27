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
public class BenchmarkInnerMultiplication_DDRB {

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

    @Benchmark public void blockMultPlus() {
        InnerMultiplication_DDRB.blockMultPlus(A, B, C, 0, 0, 0, m, m, m);
    }

//    @Benchmark public void blockMultPlusTransA() {
//        InnerMultiplication_DDRB.blockMultPlusTransA(A, B, C, 0, 0, 0, m, m, m);
//    }
//
//    @Benchmark public void blockMultPlusTransB() {
//        InnerMultiplication_DDRB.blockMultPlusTransB(A, B, C, 0, 0, 0, m, m, m);
//    }
//
//    @Benchmark public void blockMultMinus() {
//        InnerMultiplication_DDRB.blockMultMinus(A, B, C, 0, 0, 0, m, m, m);
//    }
//
//    @Benchmark public void blockMultMinusTransA() {
//        InnerMultiplication_DDRB.blockMultMinusTransA(A, B, C, 0, 0, 0, m, m, m);
//    }
//
//    @Benchmark public void blockMultMinusTransB() {
//        InnerMultiplication_DDRB.blockMultMinusTransB(A, B, C, 0, 0, 0, m, m, m);
//    }
//
//    @Benchmark public void blockMultSet() {
//        InnerMultiplication_DDRB.blockMultSet(A, B, C, 0, 0, 0, m, m, m);
//    }
//
//    @Benchmark public void blockMultSetTransA() {
//        InnerMultiplication_DDRB.blockMultSetTransA(A, B, C, 0, 0, 0, m, m, m);
//    }
//
//    @Benchmark public void blockMultSetTransB() {
//        InnerMultiplication_DDRB.blockMultSetTransB(A, B, C, 0, 0, 0, m, m, m);
//    }
//
//    @Benchmark public void blockMultPlus_alpha() {
//        InnerMultiplication_DDRB.blockMultPlus(alpha, A, B, C, 0, 0, 0, m, m, m);
//    }
//
//    @Benchmark public void blockMultPlusTransA_alpha() {
//        InnerMultiplication_DDRB.blockMultPlusTransA(alpha, A, B, C, 0, 0, 0, m, m, m);
//    }
//
//    @Benchmark public void blockMultPlusTransB_alpha() {
//        InnerMultiplication_DDRB.blockMultPlusTransB(alpha, A, B, C, 0, 0, 0, m, m, m);
//    }
//
//    @Benchmark public void blockMultSet_alpha() {
//        InnerMultiplication_DDRB.blockMultSet(alpha, A, B, C, 0, 0, 0, m, m, m);
//    }
//
//    @Benchmark public void blockMultSetTransA_alpha() {
//        InnerMultiplication_DDRB.blockMultSetTransA(alpha, A, B, C, 0, 0, 0, m, m, m);
//    }
//
//    @Benchmark public void blockMultSetTransB_alpha() {
//        InnerMultiplication_DDRB.blockMultSetTransB(alpha, A, B, C, 0, 0, 0, m, m, m);
//    }

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkInnerMultiplication_DDRB.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}