/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row;

import org.ejml.data.ZMatrixRMaj;
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
@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
@State(Scope.Benchmark)
@Fork(value = 1)
public class BenchmarkCommonOps_MT_ZDRM {

    //    @Param({"100", "500", "1000", "5000", "10000"})
    @Param({"5", "1000"})
    public int size;

    public ZMatrixRMaj A = new ZMatrixRMaj(1, 1);
    public ZMatrixRMaj B = new ZMatrixRMaj(1, 1);
    public ZMatrixRMaj C = new ZMatrixRMaj(1, 1);

    @Setup
    public void setup() {
        var rand = new Random(234);

        A.reshape(size, size);
        B.reshape(size, size);
        C.reshape(size, size);

        RandomMatrices_ZDRM.fillUniform(A, -1, 1, rand);
        RandomMatrices_ZDRM.fillUniform(B, -1, 1, rand);
        RandomMatrices_ZDRM.fillUniform(C, -1, 1, rand);
    }

    // @formatter:off
    @Benchmark public void mult_AAA() { CommonOps_MT_ZDRM.mult(A, B, C); }
    @Benchmark public void mult_sAAA() { CommonOps_MT_ZDRM.mult(1.2, -0.5, A, B, C); }
    @Benchmark public void multAdd_AAA() { CommonOps_MT_ZDRM.multAdd(A, B, C); }
    @Benchmark public void multAdd_sAAA() { CommonOps_MT_ZDRM.multAdd(1.2,-0.5, A, B, C); }
    @Benchmark public void multTransA_AAA() { CommonOps_MT_ZDRM.multTransA(A, B, C); }
    @Benchmark public void multTransA_sAAA() { CommonOps_MT_ZDRM.multTransA(1.2,-0.5, A, B, C); }
    @Benchmark public void multAddTransA_AAA() { CommonOps_MT_ZDRM.multAddTransA(A, B, C);}
    @Benchmark public void multAddTransA_sAAA() { CommonOps_MT_ZDRM.multAddTransA(1.2,-0.5, A, B, C);}
    @Benchmark public void multTransAB_AAA() { CommonOps_MT_ZDRM.multTransAB(A, B, C); }
    @Benchmark public void multTransAB_sAAA() { CommonOps_MT_ZDRM.multTransAB(1.2,-0.5, A, B, C); }
    @Benchmark public void multAddTransAB_AAA() { CommonOps_MT_ZDRM.multAddTransAB(A, B, C); }
    @Benchmark public void multAddTransAB_sAAA() { CommonOps_MT_ZDRM.multAddTransAB(1.2,-0.5, A, B, C); }
    @Benchmark public void multTransB_AAA() {  CommonOps_MT_ZDRM.multTransB(A, B, C); }
    @Benchmark public void multTransB_sAAA() {  CommonOps_MT_ZDRM.multTransB(1.2,-0.5, A, B, C); }
    // @formatter:on

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkCommonOps_MT_ZDRM.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
