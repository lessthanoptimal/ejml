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
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
@State(Scope.Benchmark)
@Fork(value = 1)
public class BenchmarkRankUpdate_DDRB {
    //    @Param({"100", "500", "1000", "5000", "10000"})
    @Param({"3000"})
    public int size;

    //    @Param({"5","10","20","40","80","120"})
    @Param({"60"})
    public int blockLength;

    public DMatrixRBlock A, A_template, B, B_tran;
    public DSubmatrixD1 Asub, Bsub, BTranSub;

    public final double alpha = 1.5;

    @Setup
    public void setup() {
        var rand = new Random(234);

        A = MatrixOps_DDRB.createRandom(size, size, -1, 1, rand, blockLength);
        A_template = A.copy();
        B = MatrixOps_DDRB.createRandom(blockLength, size, -1, 1, rand, blockLength);
        B_tran = MatrixOps_DDRB.createRandom(size, blockLength, -1, 1, rand, blockLength);

        Asub = new DSubmatrixD1(A, 0, A.numRows, 0, A.numCols);
        Bsub = new DSubmatrixD1(B, 0, B.numRows, 0, B.numCols);
        BTranSub= new DSubmatrixD1(B_tran, 0, B_tran.numRows, 0, B_tran.numCols);
    }

    @Setup(Level.Invocation)
    public void resetA() {
        A.setTo(A_template);
    }

    @Benchmark public void rankNUpdate() {
        RankUpdate_DDRB.rankNUpdate(blockLength, alpha, Asub, Bsub);
    }

    @Benchmark public void symmRankNMinus_U() {
        RankUpdate_DDRB.symmRankNMinus_U(blockLength, Asub, Bsub);
    }

    @Benchmark public void symmRankNMinus_L() {
        RankUpdate_DDRB.symmRankNMinus_L(blockLength, Asub, BTranSub);
    }

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkRankUpdate_DDRB.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}