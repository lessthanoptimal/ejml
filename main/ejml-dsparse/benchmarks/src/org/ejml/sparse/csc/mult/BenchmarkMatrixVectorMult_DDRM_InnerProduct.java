/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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
package org.ejml.sparse.csc.mult;

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparse;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.mult.MatrixVectorMult_DDRM;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 1)
public class BenchmarkMatrixVectorMult_DDRM_InnerProduct {

    @Param({"5", "1000", "3000"})
    public int size;

    @Param({"0.2", "0.4", "0.6", "0.8"})
    public double density;

    private DMatrixSparse A;
    private DMatrixRMaj B;
    private double[] a;

    @Setup
    public void setUp() {
        Random rand = new Random(234);
        B = RandomMatrices_DDRM.symmetricPosDef(size, rand);
        A = RandomMatrices_DSCC.rectangle(size, 1, (int) (density * size), rand);
        a = new DMatrixRMaj(A).data;
    }

    @Benchmark public void innerProduct() { MatrixVectorMult_DDRM.innerProduct(a, 0, B, a, 0);}
    @Benchmark public void innerProduct_sparse() { MatrixVectorMult_DDRM.innerProduct(A, B, A);}
    @Benchmark public void innerProduct_sparse_csc() { MatrixVectorMult_DDRM.innerProduct((DMatrixSparseCSC) A, B, (DMatrixSparseCSC) A);}
    @Benchmark public void innerProduct_symmetric_sparse() { MatrixVectorMult_DDRM.innerProductSelfSymmetrical(A, B);}
    @Benchmark public void innerProduct_symmetric_sparse_csc() { MatrixVectorMult_DDRM.innerProductSelfSymmetrical((DMatrixSparseCSC) A, B);}

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkMatrixVectorMult_DDRM_InnerProduct.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}