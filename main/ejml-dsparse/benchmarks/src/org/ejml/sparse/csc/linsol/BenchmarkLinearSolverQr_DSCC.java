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

package org.ejml.sparse.csc.linsol;

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.ops.ConvertDMatrixStruct;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.ejml.sparse.csc.decomposition.qr.QrLeftLookingDecomposition_DSCC;
import org.ejml.sparse.csc.linsol.qr.LinearSolverQrLeftLooking_DSCC;
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
public class BenchmarkLinearSolverQr_DSCC {

    @Param({"1000"})
    private int dimension;

    @Param({"20000"})
    private int elementCount;

    DMatrixSparseCSC A;
    DMatrixSparseCSC B;
    DMatrixSparseCSC X;
    DMatrixRMaj X_ddrm;
    DMatrixRMaj B_ddrm;

    LinearSolverQrLeftLooking_DSCC solver = new LinearSolverQrLeftLooking_DSCC(
            new QrLeftLookingDecomposition_DSCC(null));

    @Setup
    public void setup() {
        Random rand = new Random(345);
        A = RandomMatrices_DSCC.rectangle(dimension, dimension, elementCount, rand);
        B = RandomMatrices_DSCC.rectangle(dimension, 4, dimension*2, rand);
        B_ddrm = ConvertDMatrixStruct.convert(B, (DMatrixRMaj)null);
        X = new DMatrixSparseCSC(1, 1);
        X_ddrm = new DMatrixRMaj(1, 1);
    }

    @Benchmark public void solveSparse() {
        if (!solver.setA(A))
            throw new RuntimeException("Failed");
        solver.solveSparse(B,X);
    }

    @Benchmark public void solve() {
        if (!solver.setA(A))
            throw new RuntimeException("Failed");
        solver.solve(B_ddrm,X_ddrm);
    }

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkLinearSolverQr_DSCC.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
