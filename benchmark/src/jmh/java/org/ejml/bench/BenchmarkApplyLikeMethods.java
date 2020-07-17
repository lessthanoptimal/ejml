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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Uses JMH to compare the speed of apply vs different hard-coded version.
 * .. basically does inlining work and thus vectorization still apply?
 *
 * @author Florentin Doerre
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 3)
public class BenchmarkApplyLikeMethods {

    IntStream dimensions = IntStream.of(100_000, 1_000_000, 10_000_000);

    Map<Integer, DMatrixSparseCSC> matrices = new HashMap<>() {{
        dimensions.forEach(i -> put(i, RandomMatrices_DSCC.rectangle(i, i, i / 10, new Random(42))));
    }};

    @Param({"100000", "1000000", "10000000"})
    private int dim;

    @Benchmark
    public void applyAdd() {
        CommonOps_DSCC.apply(matrices.get(dim), (x) -> x + 10);
    }

    @Benchmark
    public void applyScale() {
        CommonOps_DSCC.apply(matrices.get(dim), (x) -> x * 10);
    }

    @Benchmark
    public void applyDivide() {
        CommonOps_DSCC.apply(matrices.get(dim), (x) -> 10 / x);
    }

    @Benchmark
    public void applyAddAndScale() {
        CommonOps_DSCC.apply(matrices.get(dim), (x) -> 10 / x + 12);
    }

    @Benchmark
    public void scale() {
        DMatrixSparseCSC dMatrixSparseCSC = matrices.get(dim);
        CommonOps_DSCC.scale(10, dMatrixSparseCSC, dMatrixSparseCSC);
    }

    @Benchmark
    public void divide() {
        DMatrixSparseCSC dMatrixSparseCSC = matrices.get(dim);
        CommonOps_DSCC.divide(10, dMatrixSparseCSC, dMatrixSparseCSC);
    }
}
