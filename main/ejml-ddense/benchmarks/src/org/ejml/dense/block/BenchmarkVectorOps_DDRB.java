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

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
@State(Scope.Benchmark)
@Fork(value = 1)
public class BenchmarkVectorOps_DDRB {
    @Param({"2000"})
    public int size;

    @Param({"60"})
    public int blockLength;

    public DMatrixRBlock A, B, C;
    public DSubmatrixD1 Asub, Bsub, Csub;

    @Setup
    public void setup() {
        var rand = new Random(234);

        A = MatrixOps_DDRB.createRandom(blockLength - 2, size, -1, 1, rand, blockLength);
        B = MatrixOps_DDRB.createRandom(size, size, -1, 1, rand, blockLength);
        C = MatrixOps_DDRB.createRandom(blockLength + 2, size, -1, 1, rand, blockLength);

        Asub = new DSubmatrixD1(A, 0, A.numRows, 0, A.numCols);
        Bsub = new DSubmatrixD1(B, 0, B.numRows, 0, B.numCols);
        Csub = new DSubmatrixD1(C, 0, C.numRows, 0, C.numCols);
    }

    @Benchmark
    public void scale_row() {
        VectorOps_DDRB.scale_row(blockLength, Asub, 4, 1.5, Bsub, 2, 0, size);
    }

    @Benchmark
    public void div_row() {
        VectorOps_DDRB.div_row(blockLength, Asub, 4, 1.5, Bsub, 2, 0, size);
    }

    @Benchmark
    public void add_row() {
        VectorOps_DDRB.add_row(blockLength, Asub, 4, 1.5, Bsub, 2, -0.5,
                Csub, 3, 0, size);
    }

    @Benchmark
    public void dot_row() {
        VectorOps_DDRB.dot_row(blockLength, Asub, 2, Bsub, 3, 0, size);
    }

    @Benchmark
    public void dot_row_col() {
        VectorOps_DDRB.dot_row_col(blockLength, Asub, 2, Bsub, 4, 0, size);
    }
}
