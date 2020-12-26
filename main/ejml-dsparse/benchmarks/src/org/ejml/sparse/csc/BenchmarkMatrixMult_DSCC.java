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
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
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
public class BenchmarkMatrixMult_DSCC {

    IGrowArray gw = new IGrowArray();
    DGrowArray gx = new DGrowArray();

    @State(Scope.Benchmark)
    public static class StateMult {
        @Param({"10000"})
        private int dimension;

        @Param({"100"})
        private int countPerColumn;

        DMatrixSparseCSC A, B, C;

        @Setup
        public void setup() {
            Random rand = new Random(345);
            A = RandomMatrices_DSCC.generateUniform(dimension, dimension, countPerColumn,-1,1, rand);
            B = CommonOps_DSCC.transpose(A, null, null);
            C = new DMatrixSparseCSC(1, 1);
        }
    }

    @State(Scope.Benchmark)
    public static class StateSolve {
        @Param({"1000"})
        private int dimension;

        @Param({"50"})
        private int countPerColumn;

        DMatrixSparseCSC A, B, X;

        @Setup
        public void setup() {
            Random rand = new Random(345);
            A = RandomMatrices_DSCC.generateUniform(dimension, dimension, countPerColumn,-1,1, rand);
            B = RandomMatrices_DSCC.generateUniform(dimension, 3, countPerColumn, -1, 1, rand);
            X = new DMatrixSparseCSC(1, 1);
        }
    }

    // @formatter:off
    @Benchmark public void mult( StateMult s ) { CommonOps_DSCC.mult(s.B, s.B, s.C, gw, gx); }
    @Benchmark public void solve( StateSolve s ) { CommonOps_DSCC.solve(s.A, s.B, s.X); }
    @Benchmark public void det( StateSolve s ) { CommonOps_DSCC.det(s.A); }
    // @formatter:on

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkMatrixMult_DSCC.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
