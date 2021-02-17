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

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.masks.DMaskFactory;
import org.ejml.masks.Mask;
import org.ejml.ops.DSemiRing;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.mult.MatrixVectorMultWithSemiRing_DSCC;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Florentin Dörre
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 2)
public class BenchmarkMaskedOps_DSCC {

    @Param({"100000"})
    private int dimension;

    @Param({"100"})
    private int countPerColumn;

    DMatrixSparseCSC A;
    DMatrixSparseCSC B;
    Mask mask;
    DSemiRing semiRing = DSemiRings.PLUS_TIMES;
    double[] v, result;

    @Setup
    public void setup() {
        Random rand = new Random(345);
        A = RandomMatrices_DSCC.generateUniform(dimension, dimension, countPerColumn, -1, 1, rand);
        B = CommonOps_DSCC.transpose(A, null, null);
        v = new double[dimension];
        result = new double[dimension];
        for (int i = 0; i < countPerColumn*5; i++) {
            int index = Math.abs(rand.nextInt(dimension));
            v[index] = rand.nextDouble();
        }

        mask = DMaskFactory.builder(v).build();
    }

    // @formatter:off
    @Benchmark public void vxm() { MatrixVectorMultWithSemiRing_DSCC.mult(A, v, result, semiRing, mask); }
    @Benchmark public void mxv() { MatrixVectorMultWithSemiRing_DSCC.mult(v, A, result, semiRing, mask); }
    // @formatter:on

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkMaskedOps_DSCC.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
