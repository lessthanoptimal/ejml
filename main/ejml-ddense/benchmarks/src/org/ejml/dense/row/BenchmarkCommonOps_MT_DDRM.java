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

package org.ejml.dense.row;

import org.ejml.data.DMatrixRMaj;
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
@BenchmarkMode({Mode.AverageTime,Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 2)
public class BenchmarkCommonOps_MT_DDRM {

    //    @Param({"100", "500", "1000", "5000", "10000"})
    @Param({"5","1000"})
    public int size;

    public DMatrixRMaj A = new DMatrixRMaj(1, 1);
    public DMatrixRMaj B = new DMatrixRMaj(1, 1);
    public DMatrixRMaj C = new DMatrixRMaj(1, 1);

    @Setup
    public void setup() {
        Random rand = new Random(234);

        A.reshape(size, size);
        B.reshape(size, size);
        C.reshape(size, size);

        RandomMatrices_DDRM.fillUniform(A, -1, 1, rand);
        RandomMatrices_DDRM.fillUniform(B, -1, 1, rand);
        RandomMatrices_DDRM.fillUniform(C, -1, 1, rand);
    }

    @Benchmark public void mult_AAA() {CommonOps_MT_DDRM.mult(A, B, C);}
    @Benchmark public void mult_sAAA() { CommonOps_MT_DDRM.mult(1.2, A, B, C); }
    @Benchmark public void multAdd_AAA() { CommonOps_MT_DDRM.multAdd(A, B, C); }
    @Benchmark public void multAdd_sAAA() { CommonOps_MT_DDRM.multAdd(1.2,A, B, C); }
    @Benchmark public void multTransA_AAA() { CommonOps_MT_DDRM.multTransA(A, B, C); }
    @Benchmark public void multTransA_sAAA() { CommonOps_MT_DDRM.multTransA(1.2,A, B, C); }
    @Benchmark public void multAddTransA_AAA() { CommonOps_MT_DDRM.multAddTransA(A, B, C);}
    @Benchmark public void multAddTransA_sAAA() { CommonOps_MT_DDRM.multAddTransA(1.2,A, B, C);}
    @Benchmark public void multTransAB_AAA() { CommonOps_MT_DDRM.multTransAB(A, B, C); }
    @Benchmark public void multTransAB_sAAA() { CommonOps_MT_DDRM.multTransAB(1.2,A, B, C); }
    @Benchmark public void multAddTransAB_AAA() { CommonOps_MT_DDRM.multAddTransAB(A, B, C); }
    @Benchmark public void multAddTransAB_sAAA() { CommonOps_MT_DDRM.multAddTransAB(1.2,A, B, C); }
    @Benchmark public void multTransB_AAA() {  CommonOps_MT_DDRM.multTransB(A, B, C); }
    @Benchmark public void multTransB_sAAA() {  CommonOps_MT_DDRM.multTransB(1.2,A, B, C); }
    @Benchmark public void transpose_inplace() {  CommonOps_MT_DDRM.transpose(A); }
    @Benchmark public void transpose() { CommonOps_MT_DDRM.transpose(A, B); }


    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkCommonOps_MT_DDRM.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
