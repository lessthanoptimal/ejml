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

/**
 * @author Peter Abeles
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 2)
public class BenchmarkMatrixMult_MT_DDRB {
    //    @Param({"100", "500", "1000", "5000", "10000"})
    @Param({"1000"})
    public int size;

    //    @Param({"5","10","20","40","80","120"})
    @Param({"80"})
    public int blockLength;

    public DMatrixRBlock A, B, C;
    public DSubmatrixD1 Asub, Bsub, Csub;

    @Setup
    public void setup() {
        Random rand = new Random(234);

        A = MatrixOps_DDRB.createRandom(size, size, -1, 1, rand, blockLength);
        B = MatrixOps_DDRB.createRandom(size, size, -1, 1, rand, blockLength);
        C = MatrixOps_DDRB.createRandom(size, size, -1, 1, rand, blockLength);

        Asub = new DSubmatrixD1(A, 0, A.numRows, 0, A.numCols);
        Bsub = new DSubmatrixD1(B, 0, B.numRows, 0, B.numCols);
        Csub = new DSubmatrixD1(C, 0, C.numRows, 0, C.numCols);
    }

    @Benchmark
    public void mult() {
        MatrixMult_MT_DDRB.mult(blockLength, Asub, Bsub, Csub);
    }

    @Benchmark
    public void multMinus() {
        MatrixMult_MT_DDRB.multMinus(blockLength, Asub, Bsub, Csub);
    }

    @Benchmark
    public void multPlus() {
        MatrixMult_MT_DDRB.multPlus(blockLength, Asub, Bsub, Csub);
    }

    @Benchmark
    public void multMinusTransA() {
        MatrixMult_MT_DDRB.multMinusTransA(blockLength, Asub, Bsub, Csub);
    }

    @Benchmark
    public void multPlusTransA() {
        MatrixMult_MT_DDRB.multPlusTransA(blockLength, Asub, Bsub, Csub);
    }

    @Benchmark
    public void multTransA() {
        MatrixMult_MT_DDRB.multTransA(blockLength, Asub, Bsub, Csub);
    }

    @Benchmark
    public void multTransB() {
        MatrixMult_DDRB.multTransB(blockLength, Asub, Bsub, Csub);
    }

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkMatrixMult_MT_DDRB.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
