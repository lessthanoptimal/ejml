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
package org.ejml.bench.mult;

import org.ejml.bench.BaseBenchmark;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Florentin Doerre
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 2)
public class BaseBenchmarkMatrixMatrixMult {
    protected DMatrixSparseCSC otherMatrix;
    protected DMatrixSparseCSC result;

    protected DMatrixSparseCSC matrix;

    @Param({"100000"})
    private int dimension;

    // not 10^7 as out of java heap space was insufficient
    @Param({"200000"})
    private int elementCount;

    @Setup(Level.Invocation)
    public void setup() {
        matrix = RandomMatrices_DSCC.rectangle(dimension, dimension, elementCount, new Random(42));
        otherMatrix = RandomMatrices_DSCC.rectangle(dimension, dimension, elementCount, new Random(9000));
        result = new DMatrixSparseCSC(matrix.numRows, matrix.numCols);
    }

    @TearDown
    public void tearDown() {
        result.zero();
    }
}
