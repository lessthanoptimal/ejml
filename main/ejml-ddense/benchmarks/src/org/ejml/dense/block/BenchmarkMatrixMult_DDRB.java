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
public class BenchmarkMatrixMult_DDRB {
    @Param({"1000"})
    public int size;

    @Param({"60"})
    public int blockLength;

//    @Param({"square", "tall", "wide", "long"})
    @Param({"square"})
    public String shape;

    public DMatrixRBlock A, B, C;
    public DSubmatrixD1 Asub, Bsub, Csub;

    int M, N, P;

    @Setup
    public void setup() {
        var rand = new Random(234);

        // Approximately keep the FLOPS the same across all scenarios
        // The dimensions are made extreme to highlight deficiencies in concurrent implementations
        switch (shape) {
            case "square" -> M = N = P = (int)(1.587*size);
            case "tall" -> {
                M = 4*size;
                N = blockLength;
                P = size*size/blockLength;
            }
            case "wide" -> {
                M = blockLength;
                N = size*size/blockLength;
                P = 4*size;
            }
            case "long" -> {
                M = size*size/blockLength;
                N = 4*size;
                P = blockLength;
            }
            default -> throw new RuntimeException("Unknown option");
        }

        A = MatrixOps_DDRB.createRandom(M, N, -1, 1, rand, blockLength);
        B = MatrixOps_DDRB.createRandom(N, P, -1, 1, rand, blockLength);
        C = MatrixOps_DDRB.createRandom(M, P, -1, 1, rand, blockLength);

        Asub = new DSubmatrixD1(A, 0, A.numRows, 0, A.numCols);
        Bsub = new DSubmatrixD1(B, 0, B.numRows, 0, B.numCols);
        Csub = new DSubmatrixD1(C, 0, C.numRows, 0, C.numCols);
    }

    /// Adjust the shape so that
    private void adjustShapes( boolean transA, boolean transB ) {
        A.reshape(transA ? N : M, transA ? M : N);
        B.reshape(transB ? P : N, transB ? N : P);
        Asub = new DSubmatrixD1(A, 0, A.numRows, 0, A.numCols);
        Bsub = new DSubmatrixD1(B, 0, B.numRows, 0, B.numCols);
    }

    @Benchmark public void mult() {
        MatrixMult_DDRB.mult(blockLength, Asub, Bsub, Csub);
    }

    @Benchmark public void multTransA() {
        adjustShapes(true, false);
        MatrixMult_DDRB.multTransA(blockLength, Asub, Bsub, Csub);
    }

    @Benchmark public void multTransB() {
        adjustShapes(false, true);
        MatrixMult_DDRB.multTransB(blockLength, Asub, Bsub, Csub);
    }

    @Benchmark public void multMinus() {
        MatrixMult_DDRB.multMinus(blockLength, Asub, Bsub, Csub);
    }

    @Benchmark public void multMinusTransA() {
        adjustShapes(true, false);
        MatrixMult_DDRB.multMinusTransA(blockLength, Asub, Bsub, Csub);
    }

    @Benchmark public void multMinusTransB() {
        adjustShapes(false, true);
        MatrixMult_DDRB.multMinusTransB(blockLength, Asub, Bsub, Csub);
    }

    @Benchmark public void multPlus() {
        MatrixMult_DDRB.multPlus(blockLength, Asub, Bsub, Csub);
    }

    @Benchmark public void multPlusTransA() {
        adjustShapes(true, false);
        MatrixMult_DDRB.multPlusTransA(blockLength, Asub, Bsub, Csub);
    }

    @Benchmark public void multPlusTransB() {
        adjustShapes(false, true);
        MatrixMult_DDRB.multPlusTransB(blockLength, Asub, Bsub, Csub);
    }

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkMatrixMult_DDRB.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
