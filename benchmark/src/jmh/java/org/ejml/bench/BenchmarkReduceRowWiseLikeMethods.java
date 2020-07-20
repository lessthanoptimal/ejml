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

import org.ejml.data.DMatrixRMaj;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;

/**
 * Uses JMH to compare the speed of reduceRowWise vs different hard-coded version.
 * .. basically does inlining work and thus vectorization still apply?
 *
 * @author Florentin Doerre
 */
public class BenchmarkReduceRowWiseLikeMethods extends BaseBenchmark {
    DMatrixRMaj output;

    @Override
    @Setup(Level.Invocation)
    public void setup() {
        super.setup();
        output = new DMatrixRMaj(matrix.numRows, 1);
    }


    @Benchmark
    public void reduceRowWiseSum(Blackhole bh) {
        bh.consume(CommonOps_DSCC.reduceRowWise(matrix, 0, (x, y) -> x + y, output));
    }

    @Benchmark
    public void reduceRowWiseMax(Blackhole bh) {
        bh.consume(CommonOps_DSCC.reduceRowWise(matrix, 0, (x, y) -> (x >= y) ? x : y, output));
    }

    @Benchmark
    public void sumRows(Blackhole bh) {
        bh.consume(CommonOps_DSCC.sumRows(matrix, output));
    }

    @Benchmark
    public void maxRows(Blackhole bh) {
        bh.consume(CommonOps_DSCC.maxRows(matrix, output, null));
    }
}
