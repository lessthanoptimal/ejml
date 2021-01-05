/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.ElementLocation;
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
public class BenchmarkCommonOps_DDRM {

    //    @Param({"100", "500", "1000", "5000", "10000"})
    @Param({"5", "1000"})
    public int size;

    public DMatrixRMaj A = new DMatrixRMaj(1, 1);
    public DMatrixRMaj B = new DMatrixRMaj(1, 1);
    public DMatrixRMaj C = new DMatrixRMaj(1, 1);
    public DMatrixRMaj Va = new DMatrixRMaj(1, 1);
    public DMatrixRMaj Vb = new DMatrixRMaj(1, 1);
    public BMatrixRMaj binary = new BMatrixRMaj(1, 1);
    public double[] array_v;
    public ElementLocation loc = new ElementLocation();

    @Setup
    public void setup() {
        var rand = new Random(234);

        A.reshape(size, size);
        B.reshape(size, size);
        C.reshape(size, size);
        Va.reshape(size, 1);
        Vb.reshape(size, 1);
        binary.reshape(size, size);

        RandomMatrices_DDRM.fillUniform(A, -1, 1, rand);
        RandomMatrices_DDRM.fillUniform(B, -1, 1, rand);
        RandomMatrices_DDRM.fillUniform(C, -1, 1, rand);
        RandomMatrices_DDRM.fillUniform(Va, -1, 1, rand);
        RandomMatrices_DDRM.fillUniform(Vb, -1, 1, rand);

        array_v = new double[size];
        for (int i = 0; i < size; i++) {
            array_v[i] = rand.nextDouble();
        }
        for (int i = 0; i < size*size; i++) {
            binary.data[i] = rand.nextBoolean();
        }
    }

    // @formatter:off
    @Benchmark public void mult() {CommonOps_DDRM.mult(A, B, C);}
    @Benchmark public void multAdd() { CommonOps_DDRM.multAdd(A, B, C); }
    @Benchmark public void mult_alpha() { CommonOps_DDRM.mult(2.1, A, B, C); }
    @Benchmark public void multTransA() { CommonOps_DDRM.multTransA(A, B, C); }
    @Benchmark public void multAddTransA() { CommonOps_DDRM.multAddTransA(A, B, C);}
    @Benchmark public void multAddTransA_alpha() { CommonOps_DDRM.multAddTransA(2.1, A, B, C);}
    @Benchmark public void multTransAB() { CommonOps_DDRM.multTransAB(A, B, C); }
    @Benchmark public void multAddTransAB() { CommonOps_DDRM.multAddTransAB(A, B, C); }
    @Benchmark public void multAddTransAB_alpha() { CommonOps_DDRM.multAddTransAB(2.1, A, B, C); }
    @Benchmark public void multTransB() { CommonOps_DDRM.multTransB(A, B, C); }
    @Benchmark public void multAddTransB() { CommonOps_DDRM.multAddTransB(A, B, C); }
    @Benchmark public void multAddTransB_alpha() { CommonOps_DDRM.multAddTransB(2.1, A, B, C); }
    @Benchmark public void transpose_inplace() { CommonOps_DDRM.transpose(A); }
    @Benchmark public void transpose() { CommonOps_DDRM.transpose(A,C); }
    @Benchmark public void dot() { CommonOps_DDRM.dot(Va, Vb); }
    @Benchmark public void multInner() { CommonOps_DDRM.multInner(A, C); }
    @Benchmark public void multOuter() { CommonOps_DDRM.multOuter(A, C); }
    @Benchmark public void solve() { B.reshape(A.numRows, 5); check(CommonOps_DDRM.solve(A, B, C));  }
    //    @Benchmark public void solveSPD() { B.reshape(A.numRows,5);CommonOps_DDRM.solveSPD(A, B, C); }
    @Benchmark public void trace() { CommonOps_DDRM.trace(A); }
    @Benchmark public void det() { CommonOps_DDRM.det(A); }
    @Benchmark public void invert_one() { check(CommonOps_DDRM.invert(A)); }
    @Benchmark public void invert_two() { check(CommonOps_DDRM.invert(A, C)); }
    //    @Benchmark public void invertSPD() { check(CommonOps_DDRM.invertSPD(A,C)); }
    @Benchmark public void pinv() { CommonOps_DDRM.pinv(A, C); }
    @Benchmark public void columnsToVector() { CommonOps_DDRM.columnsToVector(A, null); }
    @Benchmark public void rowsToVector() { CommonOps_DDRM.rowsToVector(A, null); }
    @Benchmark public void setIdentity() { CommonOps_DDRM.setIdentity(A); }
    @Benchmark public void identity_one() { CommonOps_DDRM.identity(size); }
    @Benchmark public void identity_two() { CommonOps_DDRM.identity(size, size/2); }
    @Benchmark public void diag() { CommonOps_DDRM.diag(1, 2, 3, 4, 5, 6, 7, 8, 9, 10); }
    @Benchmark public void diagR() { CommonOps_DDRM.diagR(5, 4, 1, 2, 3, 4, 5, 6, 7, 8); }
    @Benchmark public void kron() { B.reshape(5,5);CommonOps_DDRM.kron(A, B, C); }
    @Benchmark public void extract_a() { CommonOps_DDRM.extract(A, 1, size - 1, 1, size - 1, B, 0, 1); }
    @Benchmark public void extract_b() { CommonOps_DDRM.extract(A, 1, size - 1, 1, size - 1, B); }
    @Benchmark public void extract_c() { B.reshape(A.numRows - 2, A.numCols - 2); CommonOps_DDRM.extract(A, 1, 1, B); }
    @Benchmark public void extract_d() { CommonOps_DDRM.extract(A, 1, size - 1, 1, size - 1); }
    @Benchmark public void extract_e() { CommonOps_DDRM.extract(A, new int[]{1, 3, 4}, 3, new int[]{0, 1, 3}, 3, C); }
    @Benchmark public void extract_vector() { CommonOps_DDRM.extract(Va, new int[]{1, 3, 4}, 3, Vb); }
    //    @Benchmark public void insert_rowcol() { B.reshape(3, 3); CommonOps_DDRM.insert(A, B, new int[]{0, 1, 3}, 3, new int[]{2, 1, 0}, 3); }
    @Benchmark public void extractDiag() { CommonOps_DDRM.extractDiag(A, C); }
    @Benchmark public void extractRow() { CommonOps_DDRM.extractRow(A, 1, Vb); }
    @Benchmark public void extractColumn() { CommonOps_DDRM.extractColumn(A, 1, Vb); }
    @Benchmark public void removeColumns() { CommonOps_DDRM.removeColumns(A.copy(), 1, A.numCols - 1); }
    @Benchmark public void insert_whole() { C.reshape(A.numRows - 2, A.numCols - 2); CommonOps_DDRM.insert(C, B, 1, 0); }
    @Benchmark public void elementMax() { CommonOps_DDRM.elementMax(A); }
    @Benchmark public void elementMaxAbs() { CommonOps_DDRM.elementMaxAbs(A); }
    @Benchmark public void elementMin() { CommonOps_DDRM.elementMin(A); }
    @Benchmark public void elementMinAbs() { CommonOps_DDRM.elementMinAbs(A); }
    @Benchmark public void elementMax_loc() { CommonOps_DDRM.elementMax(A, loc); }
    @Benchmark public void elementMaxAbs_loc() { CommonOps_DDRM.elementMaxAbs(A, loc); }
    @Benchmark public void elementMin_loc() { CommonOps_DDRM.elementMin(A, loc); }
    @Benchmark public void elementMinAbs_loc() { CommonOps_DDRM.elementMinAbs(A, loc); }
    @Benchmark public void elementMult_AB() { CommonOps_DDRM.elementMult(A, B); }
    @Benchmark public void elementMult_ABC() { CommonOps_DDRM.elementMult(A, B, C); }
    @Benchmark public void elementDiv_AB() { CommonOps_DDRM.elementDiv(A, B); }
    @Benchmark public void elementDiv_ABC() { CommonOps_DDRM.elementDiv(A, B, C); }
    @Benchmark public void elementSum() { CommonOps_DDRM.elementSum(A); }
    @Benchmark public void elementSumAbs() { CommonOps_DDRM.elementSumAbs(A); }
    @Benchmark public void elementPower_AB() { CommonOps_DDRM.elementPower(A, B, C); }
    @Benchmark public void elementPower_sAB() { CommonOps_DDRM.elementPower(1.2, B, C); }
    @Benchmark public void elementPower_ABs() { CommonOps_DDRM.elementPower(A, 1.2, C); }
    @Benchmark public void elementLog() { CommonOps_DDRM.elementLog(A, C); }
    @Benchmark public void elementExp() { CommonOps_DDRM.elementExp(A, C); }
    @Benchmark public void multRows() { A.setTo(B); CommonOps_DDRM.multRows(array_v, A); }
    @Benchmark public void divideRows() {  A.setTo(B);  CommonOps_DDRM.divideRows(array_v, A); }
    @Benchmark public void multCols() { A.setTo(B); CommonOps_DDRM.multCols(A, array_v); }
    @Benchmark public void divideCols() { A.setTo(B); CommonOps_DDRM.divideCols(A, array_v); }
    @Benchmark public void divideRowsCols() { A.setTo(B);  CommonOps_DDRM.divideRowsCols(array_v, 0, A, array_v, 0); }
    @Benchmark public void sumRows() { CommonOps_DDRM.sumRows(A, C); }
    @Benchmark public void minRows() { CommonOps_DDRM.minRows(A, C); }
    @Benchmark public void sumCols() { CommonOps_DDRM.sumCols(A, C); }
    @Benchmark public void minCols() { CommonOps_DDRM.minCols(A, C); }
    @Benchmark public void maxCols() { CommonOps_DDRM.maxCols(A, C); }
    @Benchmark public void addEquals() { C.setTo(A); CommonOps_DDRM.addEquals(C, B); }
    @Benchmark public void addEquals_alpha() { C.setTo(A); CommonOps_DDRM.addEquals(C, 1.2, B); }
    @Benchmark public void add() { CommonOps_DDRM.add(A, B, C); }
    @Benchmark public void add_beta() { CommonOps_DDRM.add(A, 1.2, B, C); }
    @Benchmark public void add_alpha_beta() { CommonOps_DDRM.add(0.8, A, 1.2, B, C); }
    @Benchmark public void add_alpha() { CommonOps_DDRM.add(0.8, A, B, C); }
    @Benchmark public void add_inplace_alpha() { C.setTo(A); CommonOps_DDRM.add(C, 1.2); }
    @Benchmark public void add_alpha_output() { CommonOps_DDRM.add(A, 1.2, C); }
    @Benchmark public void subtract_alpha_output() { CommonOps_DDRM.subtract(A, 1.2, C); }
    @Benchmark public void subtract_sAA() { CommonOps_DDRM.subtract(1.2, A, C); }
    @Benchmark public void subtractEquals() { C.setTo(A); CommonOps_DDRM.subtractEquals(C, B); }
    @Benchmark public void subtract_AAA() { CommonOps_DDRM.subtract(A, B, C); }
    @Benchmark public void scale_sA() { C.setTo(A); CommonOps_DDRM.scale(1.2, C); }
    @Benchmark public void scale_sAA() { CommonOps_DDRM.scale(1.2, A, C); }
    @Benchmark public void scaleRow() { CommonOps_DDRM.scaleRow(1.2, A, 2); }
    @Benchmark public void scaleCol() { CommonOps_DDRM.scaleCol(1.2, A, 2); }
    @Benchmark public void divide_sA() { C.setTo(A); CommonOps_DDRM.divide(1.2, C); }
    @Benchmark public void divide_As() { C.setTo(A); CommonOps_DDRM.divide(C, 1.2); }
    @Benchmark public void divide_sAA() { CommonOps_DDRM.divide(1.2, A, C); }
    @Benchmark public void divide_AsA() { CommonOps_DDRM.divide(A, 1.2, C); }
    @Benchmark public void changeSign_A() { C.setTo(A); CommonOps_DDRM.changeSign(C);}
    @Benchmark public void changeSign_AA() { CommonOps_DDRM.changeSign(A, C); }
    @Benchmark public void fill() { C.setTo(A); CommonOps_DDRM.fill(C, 1.2);}
    @Benchmark public void rref() { CommonOps_DDRM.rref(A, -1, C); }
    @Benchmark public void elementLessThan() { CommonOps_DDRM.elementLessThan(A, 0.5, binary); }
    @Benchmark public void elementLessThanOrEqual() { CommonOps_DDRM.elementLessThanOrEqual(A, 0.5, binary); }
    @Benchmark public void elementMoreThan() { CommonOps_DDRM.elementMoreThan(A, 0.5, binary); }
    @Benchmark public void elementMoreThanOrEqual() { CommonOps_DDRM.elementMoreThanOrEqual(A, 0.5, binary); }
    @Benchmark public void elementLessThan_AA() { CommonOps_DDRM.elementLessThan(A, B, binary); }
    @Benchmark public void elementLessThanOrEqual_AA() { CommonOps_DDRM.elementLessThanOrEqual(A, B, binary); }
    @Benchmark public void elements() { CommonOps_DDRM.elements(A, binary, C); }
    @Benchmark public void concatColumns() { CommonOps_DDRM.concatColumns(A, B, C); }
    @Benchmark public void concatColumnsMulti() { CommonOps_DDRM.concatColumnsMulti(A, B); }
    @Benchmark public void concatRows() { CommonOps_DDRM.concatRows(A, B, C); }
    @Benchmark public void concatRowsMulti() { CommonOps_DDRM.concatRowsMulti(A, B); }
    //    @Benchmark public void permuteRowInv() { CommonOps_DDRM.permuteRowInv(A,B,C); }
    @Benchmark public void abs_AA() { CommonOps_DDRM.abs(A, C); }
    @Benchmark public void abs_A() { CommonOps_DDRM.abs(A); }
    @Benchmark public void symmLowerToFull() { CommonOps_DDRM.symmLowerToFull(A); }
    @Benchmark public void symmUpperToFull() { CommonOps_DDRM.symmUpperToFull(A); }
    @Benchmark public void apply_div() { CommonOps_DDRM.apply(A, ( v ) -> v/1.2, C); }
    @Benchmark public void apply_abs() { CommonOps_DDRM.apply(A, Math::abs); }
    // @formatter:on

    public static void check( boolean result ) { if (!result) throw new RuntimeException("Must be true"); }

    public static void main( String[] args ) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkCommonOps_DDRM.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
