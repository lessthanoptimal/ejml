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

package org.ejml.sparse.csc;

import org.ejml.concurrency.GrowArray;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.mult.Workspace_MT_DSCC;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

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
@Fork(value = 2)
public class BenchmarkCommonOps_MT_DSCC {

    @Param({"100000"})
    private int dimension;

    @Param({"10000000"})
    private int elementCount;

    DMatrixSparseCSC A;
    DMatrixSparseCSC B;
    DMatrixSparseCSC C;

    GrowArray<Workspace_MT_DSCC> listWork = new GrowArray<>(Workspace_MT_DSCC::new);

    @Setup
    public void setup() {
        A = RandomMatrices_DSCC.rectangle(dimension, dimension, elementCount, new Random(42));
        B = CommonOps_DSCC.transpose(A, null, null);
        C = new DMatrixSparseCSC(1, 1);
    }

    @Benchmark
    public void add() {
        CommonOps_MT_DSCC.add(1.5, B, 2.5, B, C, listWork);
    }

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkCommonOps_MT_DSCC.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
