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

package org.ejml.sparse.csc;

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
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
public class BenchmarkCommonOps_DSCC {

    @Param({"100000"})
    private int dimension;

    @Param({"10000000"})
    private int elementCount;

    DMatrixSparseCSC A;
    DMatrixSparseCSC B;
    DMatrixSparseCSC C;
    DMatrixRMaj C_ddrm;

    IGrowArray gw = new IGrowArray();
    DGrowArray gx = new DGrowArray();

    double[] array;

    @Setup
    public void setup() {
        Random rand = new Random(345);
        A = RandomMatrices_DSCC.rectangle(dimension, dimension, elementCount, rand);
        B = CommonOps_DSCC.transpose(A, null, null);
        C = new DMatrixSparseCSC(1, 1);
        C_ddrm = new DMatrixRMaj(1, 1);
        array = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            array[i] = rand.nextDouble();
        }
    }

    @Benchmark public void add() { CommonOps_DSCC.add(1.5, B, 2.5, B, C, gw, gx); }
    @Benchmark public void dotInnerColumns() { CommonOps_DSCC.dotInnerColumns(A, 2, B, 1, gw, gx); }
    @Benchmark public void transpose() { CommonOps_DSCC.transpose(A, C, gw); }
    @Benchmark public void maxCols() { CommonOps_DSCC.maxCols(A, C_ddrm); }
    @Benchmark public void minCols() { CommonOps_DSCC.minCols(A, C_ddrm); }
    @Benchmark public void maxAbsCols() { CommonOps_DSCC.maxAbsCols(A, C_ddrm); }
    @Benchmark public void maxRows() { CommonOps_DSCC.maxRows(A, C_ddrm, gw); }
    @Benchmark public void minRows() { CommonOps_DSCC.minRows(A, C_ddrm, gw); }
    @Benchmark public void sumRows() { CommonOps_DSCC.sumRows(A, C_ddrm); }
    @Benchmark public void sumCols() { CommonOps_DSCC.sumCols(A, C_ddrm); }
    @Benchmark public void multRows() { CommonOps_DSCC.multRows(array,0,A); }
    @Benchmark public void divideRows() { CommonOps_DSCC.divideRows(array, 0, A); }
    @Benchmark public void multColumns() { CommonOps_DSCC.multColumns(A, array,0); }
    @Benchmark public void divideColumns() { CommonOps_DSCC.divideColumns(A, array, 0); }
    @Benchmark public void multRowsCols() { CommonOps_DSCC.multRowsCols(array,0,A,array,0); }
    @Benchmark public void divideRowsCols() { CommonOps_DSCC.divideRowsCols(array,0,A,array,0); }
    @Benchmark public void elementSum() { CommonOps_DSCC.elementSum(A); }
    @Benchmark public void elementMax() { CommonOps_DSCC.elementMax(A); }
    @Benchmark public void elementMaxAbs() { CommonOps_DSCC.elementMaxAbs(A); }
    @Benchmark public void elementMin() { CommonOps_DSCC.elementMin(A); }
    @Benchmark public void elementMinAbs() { CommonOps_DSCC.elementMinAbs(A); }
    @Benchmark public void elementMult() { CommonOps_DSCC.elementMult(A, B, C, gw, gx); }
    @Benchmark public void apply() { CommonOps_DSCC.apply(A, Math::sqrt, C); }
    @Benchmark public void changeSign() { CommonOps_DSCC.changeSign(A, C); }
    @Benchmark public void scale() { CommonOps_DSCC.scale(0.2, A, C); }
    @Benchmark public void fill() { C.reshape(4000,4000);CommonOps_DSCC.fill(C,0.9); }
    @Benchmark public void identity() { CommonOps_DSCC.identity(dimension); }
    @Benchmark public void trace() { CommonOps_DSCC.trace(A); }
    @Benchmark public void zero() { CommonOps_DSCC.zero(A,0,A.numRows,0,A.numCols); }
    @Benchmark public void removeZeros() { CommonOps_DSCC.removeZeros(A,1e-8); }

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkCommonOps_DSCC.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
