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

package org.ejml.dense.block.decompose.qr;

import org.ejml.data.DMatrixRBlock;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.block.decomposition.qr.QRDecompositionHouseholder_MT_DDRB;
import org.ejml.interfaces.decomposition.QRDecomposition;
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
public class BenchmarkDecompositionQR_MT_DDRB {
    @Param({"1000", "2000"})
    public int size;

    public DMatrixRBlock A, A_template;

    QRDecomposition<DMatrixRBlock> qr = new QRDecompositionHouseholder_MT_DDRB();

    @Setup
    public void setup() {
        Random rand = new Random(234);

        A = MatrixOps_DDRB.createRandom(size*4, size/4, -1, 1, rand);
        A_template = A.copy();
    }

    @Setup(Level.Invocation) public void reset() {
        A.setTo(A_template);
    }

    @Benchmark public void decompose() {
        if (!qr.decompose(A))
            throw new RuntimeException("FAILED?!");
    }

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkDecompositionQR_MT_DDRB.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
