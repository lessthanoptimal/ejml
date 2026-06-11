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

import org.ejml.dense.row.RandomMatrices_DDRM;
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
public class BenchmarkTileTriangularSolver_F64 {
    @Param({"60"})
    public int m;

    public int n;

    public double[] L;
    public double[] U;
    public double[] B;
    public double[] L_inv;
    public double[] U_inv;

    private double[] L_template;
    private double[] U_template;
    private double[] B_template;

    @Setup
    public void setup() {
        Random rand = new Random(234);

        n = m*2;

        L_template = RandomMatrices_DDRM.triangularLower(m, 0, -1, 1, rand).data;
        U_template = RandomMatrices_DDRM.triangularUpper(m, 0, -1, 1, rand).data;

        // Ensure the matrix is not nearly singular by pushing diagonal elements far from 1
        for (int i = 0; i < m; i++) {
            double dl = L_template[i*m + i];
            L_template[i*m + i] = (dl >= 0 ? dl + 1.0 : dl - 1.0);
            double du = U_template[i*m + i];
            U_template[i*m + i] = (du >= 0 ? du + 1.0 : du - 1.0);
        }

        B_template = RandomMatrices_DDRM.rectangle(m, n, -1, 1, rand).data;

        L = L_template.clone();
        U = U_template.clone();
        B = B_template.clone();
        L_inv = new double[m*m];
        U_inv = new double[m*m];
    }

    @Setup(Level.Invocation)
    public void reset() {
        System.arraycopy(L_template, 0, L, 0, L.length);
        System.arraycopy(U_template, 0, U, 0, U.length);
        System.arraycopy(B_template, 0, B, 0, B.length);
    }

    @Benchmark public void invertLower_two() {
        TileTriangularSolver_F64.invertLower(L, L_inv, m, 0, 0);
    }

    @Benchmark public void invertLower_inplace() {
        TileTriangularSolver_F64.invertLower(L, m, 0);
    }

    @Benchmark public void invertUpper_two() {
        TileTriangularSolver_F64.invertUpper(U, U_inv, m, 0, 0);
    }

    @Benchmark public void invertUpper_inplace() {
        TileTriangularSolver_F64.invertUpper(U, m, 0);
    }

    @Benchmark public void invertUpperTran() {
        TileTriangularSolver_F64.invertUpperTran(U, U_inv, m, 0, 0);
    }

    @Benchmark public void lsolveLow() {
        TileTriangularSolver_F64.lsolveLow(L, B, m, n, m, 0, 0);
    }

    @Benchmark public void lsolveLowTrans() {
        TileTriangularSolver_F64.lsolveLowTrans(L, B, m, n, m, 0, 0);
    }

    @Benchmark public void lsolveLowBTrans() {
        TileTriangularSolver_F64.lsolveLowBTrans(L, B, m, n, m, 0, 0);
    }

    @Benchmark public void lsolveLowTransBTrans() {
        TileTriangularSolver_F64.lsolveLowTransBTrans(L, B, m, n, m, 0, 0);
    }

    @Benchmark public void lsolveUpp() {
        TileTriangularSolver_F64.lsolveUpp(U, B, m, n, m, 0, 0);
    }

    @Benchmark public void lsolveUppTrans() {
        TileTriangularSolver_F64.lsolveUppTrans(U, B, m, n, m, 0, 0);
    }

    @Benchmark public void lsolveUppBTrans() {
        TileTriangularSolver_F64.lsolveUppBTrans(U, B, m, n, m, 0, 0);
    }

    @Benchmark public void lsolveUppTransBTrans() {
        TileTriangularSolver_F64.lsolveUppTransBTrans(U, B, m, n, m, 0, 0);
    }

    @Benchmark public void rsolveLow() {
        TileTriangularSolver_F64.rsolveLow(L, B, m, n, m, 0, 0);
    }

    @Benchmark public void rsolveLowTrans() {
        TileTriangularSolver_F64.rsolveLowTrans(L, B, m, n, m, 0, 0);
    }

    @Benchmark public void rsolveLowBTrans() {
        TileTriangularSolver_F64.rsolveLowBTrans(L, B, m, n, m, 0, 0);
    }

    @Benchmark public void rsolveLowTransBTrans() {
        TileTriangularSolver_F64.rsolveLowTransBTrans(L, B, m, n, m, 0, 0);
    }

    @Benchmark public void rsolveUpp() {
        TileTriangularSolver_F64.rsolveUpp(U, B, m, n, m, 0, 0);
    }

    @Benchmark public void rsolveUppTrans() {
        TileTriangularSolver_F64.rsolveUppTrans(U, B, m, n, m, 0, 0);
    }

    @Benchmark public void rsolveUppBTrans() {
        TileTriangularSolver_F64.rsolveUppBTrans(U, B, m, n, m, 0, 0);
    }

    @Benchmark public void rsolveUppTransBTrans() {
        TileTriangularSolver_F64.rsolveUppTransBTrans(U, B, m, n, m, 0, 0);
    }

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkTileTriangularSolver_F64.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}