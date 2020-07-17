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
package org.ejml.bench;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Uses JMH to compare the speed of apply vs different hard-coded version.
 * .. basically does inlining work and thus vectorization still apply?
 *
 *  (with +UseSuperWord)
 *  Benchmark                                   (dimension)  (sparsity)  Mode  Cnt  Score   Error  Units
 *  BenchmarkApplyLikeMethods.applyAdd               100000         0.1  avgt    5  0.006 ± 0.004  ms/op
 *  BenchmarkApplyLikeMethods.applyAddAndScale       100000         0.1  avgt    5  0.014 ± 0.020  ms/op
 *  BenchmarkApplyLikeMethods.applyDivide            100000         0.1  avgt    5  0.012 ± 0.007  ms/op
 *  BenchmarkApplyLikeMethods.applyScale             100000         0.1  avgt    5  0.007 ± 0.001  ms/op
 *  BenchmarkApplyLikeMethods.divide                 100000         0.1  avgt    5  0.010 ± 0.021  ms/op
 *  BenchmarkApplyLikeMethods.scale                  100000         0.1  avgt    5  0.003 ± 0.001  ms/op
 * @author Florentin Doerre
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 2)
public class BenchmarkApplyLikeMethods {

    DMatrixSparseCSC matrix;

    @Param({"100000"})
    private int dimension;

    @Param({"0.1"})
    private float sparsity;

    @Setup(Level.Invocation)
    public void setup() {
        matrix = RandomMatrices_DSCC.rectangle(dimension, dimension, Math.round(dimension * sparsity), new Random(42));
    }

    @Benchmark
    public void applyAdd() {
        CommonOps_DSCC.apply(matrix, (x) -> x + 10);
    }

    @Benchmark
    public void applyScale() {
        CommonOps_DSCC.apply(matrix, (x) -> x * 10);
    }

    @Benchmark
    public void applyDivide() {
        CommonOps_DSCC.apply(matrix, (x) -> 10 / x);
    }

    @Benchmark
    public void applyAddAndScale() {
        CommonOps_DSCC.apply(matrix, (x) -> 10 / x + 12);
    }

    @Benchmark
    public void scale() {
        CommonOps_DSCC.scale(10, matrix, matrix);
    }

    @Benchmark
    public void divide() {
        CommonOps_DSCC.divide(10, matrix, matrix);
    }
}
