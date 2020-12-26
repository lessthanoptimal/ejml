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

package org.ejml.sparse.csc;

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import pabeles.concurrency.GrowArray;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Peter Abeles
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 1)
public class BenchmarkMatrixMultDense_MT_DSCC {

    @Param({"3000"})
    private int dimension;

    @Param({"50"})
    private int countPerColumn;

    DMatrixSparseCSC A,A_small;
    DMatrixRMaj B = new DMatrixRMaj(1, 1);
    DMatrixRMaj C = new DMatrixRMaj(1, 1);

    GrowArray<DGrowArray> work = new GrowArray<>(DGrowArray::new);

    @Setup
    public void setup() {
        Random rand = new Random(2345);
        A =RandomMatrices_DSCC.generateUniform(dimension, dimension, countPerColumn,-1,1, rand);
        A_small = RandomMatrices_DSCC.generateUniform(dimension/4, dimension/4, countPerColumn/4, -1,1,rand);
        B = RandomMatrices_DDRM.rectangle(dimension, dimension, -1, 1, rand);
        C = B.create(dimension, dimension);
    }

    // @formatter:off
    @Benchmark public void mult() { CommonOps_MT_DSCC.mult(A, B, C, work); }
    @Benchmark public void multAdd() { CommonOps_MT_DSCC.multAdd(A, B, C, work); }
    @Benchmark public void multTransA() { CommonOps_MT_DSCC.multTransA(A, B, C, work); }
    @Benchmark public void multAddTransA() { CommonOps_MT_DSCC.multAddTransA(A, B, C, work); }
    @Benchmark public void multTransB() { CommonOps_MT_DSCC.multTransB(A, B, C, work); }
    @Benchmark public void multAddTransB() { CommonOps_MT_DSCC.multAddTransB(A, B, C, work); }
    @Benchmark public void multTransAB() { CommonOps_MT_DSCC.multTransAB(A, B, C); }
    @Benchmark public void multAddTransAB() { CommonOps_MT_DSCC.multAddTransAB(A, B, C); }
//    @Benchmark public void invert() { CommonOps_MT_DSCC.invert(A_small, C); }
    // @formatter:on

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkMatrixMultDense_MT_DSCC.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
