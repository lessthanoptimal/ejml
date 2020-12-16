/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row;

import org.ejml.data.BMatrixRMaj;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.ZMatrixRMaj;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 1)
public class BenchmarkCommonOps_ZDRM {
    @Param({"5", "1000"})
    public int size;

    public ZMatrixRMaj A, B, C;
    public ZMatrixRMaj Va, Vb;
    public BMatrixRMaj binary;
    public double[] array_v;
    public DMatrixRMaj R;

    @Setup
    public void setup() {
        System.out.println("Size = " + size);
        Random rand = new Random(234);

        A = new ZMatrixRMaj(size, size);
        B = new ZMatrixRMaj(size, size);
        C = new ZMatrixRMaj(size, size);
        Va = new ZMatrixRMaj(size, 1);
        Vb = new ZMatrixRMaj(size, 1);
        R = new DMatrixRMaj(size, size);
        binary = new BMatrixRMaj(size, size);

        RandomMatrices_ZDRM.fillUniform(A, -1, 1, rand);
        RandomMatrices_ZDRM.fillUniform(B, -1, 1, rand);
        RandomMatrices_ZDRM.fillUniform(C, -1, 1, rand);
        RandomMatrices_ZDRM.fillUniform(Va, -1, 1, rand);
        RandomMatrices_ZDRM.fillUniform(Vb, -1, 1, rand);

        array_v = new double[size*2];
        for (int i = 0; i < array_v.length; i++) {
            array_v[i] = rand.nextDouble() - 0.5;
        }
        for (int i = 0; i < size*size; i++) {
            binary.data[i] = rand.nextBoolean();
        }
    }

    // @formatter:off
    @Benchmark public void mult() {CommonOps_ZDRM.mult(A, B, C);}
    @Benchmark public void mult_alpha() {CommonOps_ZDRM.mult(2.1, 1.5, A, B, C);}
    @Benchmark public void multAdd() {CommonOps_ZDRM.multAdd(A, B, C);}
    @Benchmark public void multAdd_alpha() {CommonOps_ZDRM.multAdd(2.1, 1.5, A, B, C);}
    @Benchmark public void multTransA() {CommonOps_ZDRM.multTransA(A, B, C);}
    @Benchmark public void multTransA_alpha() {CommonOps_ZDRM.multTransA(2.1, 1.5, A, B, C);}
    @Benchmark public void multAddTransA() {CommonOps_ZDRM.multAddTransA(A, B, C);}
    @Benchmark public void multAddTransA_alpha() {CommonOps_ZDRM.multAddTransA(2.1, 1.5, A, B, C);}
    @Benchmark public void multTransAB() {CommonOps_ZDRM.multTransAB(A, B, C);}
    @Benchmark public void multTransAB_alpha() {CommonOps_ZDRM.multTransAB(2.1, 1.5, A, B, C);}
    @Benchmark public void multAddTransAB() {CommonOps_ZDRM.multAddTransAB(A, B, C);}
    @Benchmark public void multAddTransAB_alpha() {CommonOps_ZDRM.multAddTransAB(2.1, 1.5, A, B, C);}
    @Benchmark public void multTransB() {CommonOps_ZDRM.multTransB(A, B, C);}
    @Benchmark public void multTransB_alpha() {CommonOps_ZDRM.multTransB(2.1, 1.5, A, B, C);}
    @Benchmark public void multAddTransB() {CommonOps_ZDRM.multAddTransB(A, B, C);}
    @Benchmark public void multAddTransB_alpha() {CommonOps_ZDRM.multAddTransB(2.1, 1.5, A, B, C);}
    @Benchmark public void solve() {B.reshape(A.numRows, 5); check(CommonOps_ZDRM.solve(A, B, C));}
    @Benchmark public void det() {CommonOps_ZDRM.det(A);}
    @Benchmark public void diag() {CommonOps_ZDRM.diag(array_v);}
    @Benchmark public void setIdentity() {CommonOps_ZDRM.setIdentity(A);}
    @Benchmark public void identity_A() {CommonOps_ZDRM.identity(size);}
    @Benchmark public void identity_AA() {CommonOps_ZDRM.identity(size, size/2);}
    @Benchmark public void extractDiag() {CommonOps_ZDRM.extractDiag(A, C);}
    @Benchmark public void stripReal() {CommonOps_ZDRM.stripReal(A, R);}
    @Benchmark public void stripImaginary() {CommonOps_ZDRM.stripImaginary(A, R);}
    @Benchmark public void magnitude() {CommonOps_ZDRM.magnitude(A, R);}
    @Benchmark public void conjugate() {CommonOps_ZDRM.conjugate(A, C);}
    @Benchmark public void fill() {CommonOps_ZDRM.fill(A, -0.5, 1.2);}
    @Benchmark public void add() {CommonOps_ZDRM.add(A, B, C);}
    @Benchmark public void subtract() {CommonOps_ZDRM.subtract(A, B, C);}
    @Benchmark public void scale() {C.setTo(A); CommonOps_ZDRM.scale(-0.5, 1.2, C);}
    @Benchmark public void transpose_A() {CommonOps_ZDRM.transpose(A);}
    @Benchmark public void transposeConjugate_A() {CommonOps_ZDRM.transposeConjugate(A);}
    @Benchmark public void transpose_AA() {CommonOps_ZDRM.transpose(A, C);}
    @Benchmark public void transposeConjugate_AA() {CommonOps_ZDRM.transposeConjugate(A, C);}
    @Benchmark public void invert_A() {check(CommonOps_ZDRM.invert(A));}
    @Benchmark public void invert_AA() {check(CommonOps_ZDRM.invert(A, C));}
    @Benchmark public void elementMultiply_As() {CommonOps_ZDRM.elementMultiply(A, 1.2, -0.5, C);}
    @Benchmark public void elementDivide_As() {CommonOps_ZDRM.elementDivide(A, 1.2, -0.5, C);}
    @Benchmark public void elementDivide_sA() {CommonOps_ZDRM.elementDivide(1.2, -0.5, A, C);}
    @Benchmark public void elementMinReal() {CommonOps_ZDRM.elementMinReal(A);}
    @Benchmark public void elementMinImaginary() {CommonOps_ZDRM.elementMinImaginary(A);}
    @Benchmark public void elementMaxReal() {CommonOps_ZDRM.elementMaxReal(A);}
    @Benchmark public void elementMaxImaginary() {CommonOps_ZDRM.elementMaxImaginary(A);}
    @Benchmark public void elementMaxMagnitude2() {CommonOps_ZDRM.elementMaxMagnitude2(A);}
    @Benchmark public void elementMaxAbs() {CommonOps_ZDRM.elementMaxAbs(A);}
    @Benchmark public void elementMinAbs() {CommonOps_ZDRM.elementMinAbs(A);}
    @Benchmark public void extract_a() {CommonOps_ZDRM.extract(A, 1, size - 1, 1, size - 1, B, 0, 1);}
    @Benchmark public void extract_b() {CommonOps_ZDRM.extract(A, 1, size - 1, 1, size - 1);}
    @Benchmark public void CommonOps_ZDRM() {CommonOps_ZDRM.columnsToVector(A, null);}

    public static void check( boolean result ) {if (!result) throw new RuntimeException("Must be true");}
    // @formatter:on

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkCommonOps_ZDRM.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
