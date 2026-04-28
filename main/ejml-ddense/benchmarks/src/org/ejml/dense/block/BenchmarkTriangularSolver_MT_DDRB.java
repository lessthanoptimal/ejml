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
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
@State(Scope.Benchmark)
@Fork(value = 1)
public class BenchmarkTriangularSolver_MT_DDRB {
    //    @Param({"100", "500", "1000", "5000", "10000"})
    @Param({"2000"})
    public int size;

    @Param({"60"})
    public int blockLength;

    @Param({"true", "false"})
    public boolean upper;

    public DMatrixRBlock T, T_template, B, C;
    public DSubmatrixD1 Tsub, Bsub, Csub;

    @Setup
    public void setup() {
        var rand = new Random(234);

        T = MatrixOps_DDRB.createRandom(size, size, -1, 1, rand, blockLength);
        B = MatrixOps_DDRB.createRandom(size, size, -1, 1, rand, blockLength);
        C = MatrixOps_DDRB.createRandom(size, size, -1, 1, rand, blockLength);

        // Ensure A is numerically stable
        for (int i = 0; i < T.numCols; i++) {
            double v = T.get(i, i);
            T.set(i, i, v + Math.signum(v));
        }

        T_template = T.copy();

        Tsub = new DSubmatrixD1(T, 0, T.numRows, 0, T.numCols);
        Bsub = new DSubmatrixD1(B, 0, B.numRows, 0, B.numCols);
        Csub = new DSubmatrixD1(C, 0, C.numRows, 0, C.numCols);
    }

    @Setup(Level.Invocation)
    public void reset() {
        T.setTo(T_template);
    }

//    @Benchmark
//    public void invert_two() {
//        TriangularSolver_MT_DDRB.invert(blockLength, upper, Tsub, Bsub, workspace);
//    }

    @Benchmark
    public void lsolve() {
        TriangularSolver_MT_DDRB.lsolve(blockLength, upper, Tsub, Bsub, false);
    }

//    @Benchmark
//    public void lsolve_transT() {
//        TriangularSolver_MT_DDRB.lsolve(blockLength, upper, Tsub, Bsub, true);
//    }
//
//    @Benchmark
//    public void rsolve() {
//        TriangularSolver_MT_DDRB.rsolve(blockLength, upper, Tsub, Bsub, false);
//    }
//
//    @Benchmark
//    public void rsolve_transT() {
//        TriangularSolver_MT_DDRB.rsolve(blockLength, upper, Tsub, Bsub, true);
//    }
}
