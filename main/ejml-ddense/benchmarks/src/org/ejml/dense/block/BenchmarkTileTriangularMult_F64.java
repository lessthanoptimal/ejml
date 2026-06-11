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
public class BenchmarkTileTriangularMult_F64 {
    @Param({"60"})
    public int m;

    public double[] T, B, C;

    private double[] T_template, B_template, C_template;

    @Setup public void setup() {
        var rand = new Random(234);

        T_template = RandomMatrices_DDRM.rectangle(m, m, -1, 1, rand).data;
        B_template = RandomMatrices_DDRM.rectangle(m, m, -1, 1, rand).data;
        C_template = RandomMatrices_DDRM.rectangle(m, m, -1, 1, rand).data;

        T = T_template.clone();
        B = B_template.clone();
        C = C_template.clone();
    }

    @Setup(Level.Invocation)
    public void reset() {
        System.arraycopy(T_template, 0, T, 0, T.length);
        System.arraycopy(B_template, 0, B, 0, B.length);
        System.arraycopy(C_template, 0, C, 0, C.length);
    }

    //@formatter:off
    @Benchmark public void lmultUnitLow() {TileTriangularMult_F64.lmultUnitLow(T, B, C, m, m, m, m, m, 0, 0, 0);}
    @Benchmark public void lmultAddUnitLow() {TileTriangularMult_F64.lmultAddUnitLow(T, B, C, m, m, m, m, m, 0, 0, 0);}
    @Benchmark public void lmultUnitLowTransT() {TileTriangularMult_F64.lmultUnitLowTransT(T, B, C, m, m, m, m, m, 0, 0, 0);}
    @Benchmark public void lmultAddUnitLowTransT() {TileTriangularMult_F64.lmultAddUnitLowTransT(T, B, C, m, m, m, m, m, 0, 0, 0);}
    @Benchmark public void lmultUnitUpp() {TileTriangularMult_F64.lmultUnitUpp(T, B, C, m, m, m, m, m, 0, 0, 0);}
    @Benchmark public void lmultAddUnitUpp() {TileTriangularMult_F64.lmultAddUnitUpp(T, B, C, m, m, m, m, m, 0, 0, 0);}
    @Benchmark public void lmultUnitUppTransT() {TileTriangularMult_F64.lmultUnitUppTransT(T, B, C, m, m, m, m, m, 0, 0, 0);}
    @Benchmark public void lmultAddUnitUppTransT() {TileTriangularMult_F64.lmultAddUnitUppTransT(T, B, C, m, m, m, m, m, 0, 0, 0);}
    @Benchmark public void rmultAddUnitLow() {TileTriangularMult_F64.rmultAddUnitLow(T, B, C, m, m, m, m, m, 0, 0, 0);}
    @Benchmark public void rmultAddUnitUpp() {TileTriangularMult_F64.rmultAddUnitUpp(T, B, C, m, m, m, m, m, 0, 0, 0);}
    @Benchmark public void rmultUnitUppTransT() {TileTriangularMult_F64.rmultUnitUppTransT(T, B, C, m, m, m, m, m, 0, 0, 0);}
    //@formatter:on

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkTileTriangularMult_F64.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}