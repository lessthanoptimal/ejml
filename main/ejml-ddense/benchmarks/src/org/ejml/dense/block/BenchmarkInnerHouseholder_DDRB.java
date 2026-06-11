/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.block;

import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DSubmatrixD1;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
@State(Scope.Benchmark)
@Fork(value = 1)
public class BenchmarkInnerHouseholder_DDRB {
    @Param({"3000"})
    public int m;

    // Size of inner block
    public int blockLength = 60;

    public DMatrixRBlock A, A_template;
    public DSubmatrixD1 Asub;
    public double[] gamma;

    public final double gamma0 = 1.2;

    @Setup public void setup() {
        var rand = new Random(234);

        A = MatrixOps_DDRB.createRandom(m, m, -1, 1, rand, blockLength);
        A_template = A.copy();
        Asub = new DSubmatrixD1(A, 0, A.numRows, 0, A.numCols);
        gamma = new double[m];
    }

    // Ensures inputs are the same each iteration
    @Setup(Level.Iteration)
    public void reset() {
        A.setTo(A_template);
    }

    //@formatter:off
    @Benchmark public boolean computeHouseholderCol() { return InnerHouseholder_DDRB.computeHouseholderCol(blockLength, Asub, gamma, 0); }
    @Benchmark public boolean computeHouseholderRow() { return InnerHouseholder_DDRB.computeHouseholderRow(blockLength, Asub, gamma, 0); }
    @Benchmark public void rank1UpdateMultR_Col() { InnerHouseholder_DDRB.rank1UpdateMultR_Col(blockLength, Asub, 0, gamma0); }
    @Benchmark public void rank1UpdateMultR_TopRow() { InnerHouseholder_DDRB.rank1UpdateMultR_TopRow(blockLength, Asub, 0, gamma0); }
    @Benchmark public void rank1UpdateMultL_Row() { InnerHouseholder_DDRB.rank1UpdateMultL_Row(blockLength, Asub, 0, 1, gamma0); }
    @Benchmark public double innerProdCol() { return InnerHouseholder_DDRB.innerProdCol(blockLength, Asub, 0, blockLength, 1, blockLength); }
    @Benchmark public double innerProdRow() { return InnerHouseholder_DDRB.innerProdRow(blockLength, Asub, 0, Asub, 1, 1); }
    @Benchmark public double findMaxCol() { return InnerHouseholder_DDRB.findMaxCol(blockLength, Asub, 0); }
    @Benchmark public double findMaxRow() { return InnerHouseholder_DDRB.findMaxRow(blockLength, Asub, 0, 1); }
    //@formatter:on

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkInnerHouseholder_DDRB.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
