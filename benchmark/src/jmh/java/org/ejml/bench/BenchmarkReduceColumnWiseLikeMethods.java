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
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Uses JMH to compare the speed of reduceColumnWise vs different hard-coded version.
 * .. basically does inlining work and thus vectorization still apply?
 *
 * @author Florentin Doerre
 */
public class BenchmarkReduceColumnWiseLikeMethods extends BaseBenchmark {
    DMatrixRMaj output;

    @Override
    @Setup(Level.Invocation)
    public void setup() {
        super.setup();
        output = new DMatrixRMaj(matrix.numRows, 1);
    }

    @Benchmark
    public void reduceColumnWiseMaxAbs(Blackhole bh) {
        bh.consume(CommonOps_DSCC.reduceColumnWise(matrix, 0, (x, y) -> (x >= Math.abs(y)) ? x : Math.abs(y), output));
    }

    @Benchmark
    public void columnMaxAbs(Blackhole bh) {
        CommonOps_DSCC.columnMaxAbs(matrix, output.data);
    }

    // f.i. divideColumns is not directly translate-able as it allows different values per column
}
