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

/**
 * @author Peter Abeles
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value=2)
public class BenchmarkLinearSolverQR_DDRB {
    //    @Param({"100", "500", "1000", "5000", "10000"})
    @Param({"1000","2000"})
    public int size;

    public DMatrixRBlock A;
    public DMatrixRBlock X,B;

    QrHouseHolderSolver_DDRB householder = new QrHouseHolderSolver_DDRB();

    @Setup
    public void setup() {
        Random rand = new Random(234);

        A = MatrixOps_DDRB.createRandom(size*4,size/4,-1,1,rand);
        B = MatrixOps_DDRB.createRandom(A.numRows,20,-1,1, rand);
        X = A.create(1,1);
    }

    @Benchmark
    public void householder() {
        DMatrixRBlock A = householder.modifiesA() ? this.A.copy() : this.A;
        DMatrixRBlock B = householder.modifiesB() ? this.B.copy() : this.B;
        if( !householder.setA(A) )
            throw new RuntimeException("Bad");
        householder.solve(B, X);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkLinearSolverQR_DDRB.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
