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

package org.ejml.dense.block.linsol.qr;

import org.ejml.data.DMatrixRBlock;
import org.ejml.dense.block.MatrixOps_DDRB;
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
public class BenchmarkLinearSolverQR_MT_DDRB {
    @State(Scope.Benchmark)
    public static class SolveState {
        @Param({"1000", "2000"})
        public int size;

        public DMatrixRBlock A, B, X;

        @Setup public void setup() {
            var rand = new Random(234);
            A = MatrixOps_DDRB.createRandom(size*4, size/4, -1, 1, rand);
            B = MatrixOps_DDRB.createRandom(A.numRows, 20, -1, 1, rand);
            X = A.create(A.numCols, B.numCols);
        }
    }

    @State(Scope.Benchmark)
    public static class InvertState {
        @Param({"750", "1500"})
        public int size;

        public DMatrixRBlock A, X;

        @Setup public void setup() {
            var rand = new Random(234);
            A = MatrixOps_DDRB.createRandom(size, size, -1, 1, rand);
            X = A.createLike();
        }
    }

    QrHouseholderSolver_MT_DDRB householder = new QrHouseholderSolver_MT_DDRB();

    @Benchmark
    public void solve_householder( BenchmarkLinearSolverQR_DDRB.SolveState s ) {
        DMatrixRBlock A = householder.modifiesA() ? s.A.copy() : s.A;
        DMatrixRBlock B = householder.modifiesB() ? s.B.copy() : s.B;
        if (!householder.setA(A))
            throw new RuntimeException("Bad");
        householder.solve(B, s.X);
    }

    @Benchmark
    public void invert_householder( BenchmarkLinearSolverQR_DDRB.InvertState s ) {
        DMatrixRBlock A = householder.modifiesA() ? s.A.copy() : s.A;
        if (!householder.setA(A))
            throw new RuntimeException("Bad");
        householder.invert(s.X);
    }

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkLinearSolverQR_MT_DDRB.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
