/*
 * Copyright (c) 2009-2019, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.mult;

import org.ejml.data.DMatrix1Row;
import org.ejml.data.DMatrixD1;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Experiment to see if the JVM is smart enough to realize that them matrix size is fixed if inputs are constant
 * parameters and will automatically unroll.
 *
 * @author Peter Abeles
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value=1)
public class BenchmarkMultUnrolling_DDRM {

    // TODO try unrolling IKJ instead of IJK

    // try 3 or 6
    int N = 24;
    DMatrixRMaj A = new DMatrixRMaj(N,N);
    DMatrixRMaj B = new DMatrixRMaj(N,N);
    DMatrixRMaj C = new DMatrixRMaj(N,N);


//    @Setup
//    public void setup() {
//        Random rand = new Random(234);
//
//        RandomMatrices_DDRM.fillUniform(A,rand);
//        RandomMatrices_DDRM.fillUniform(B,rand);
//        RandomMatrices_DDRM.fillUniform(C,rand);
//
//    }
//
//    @Benchmark
//    public void square_var() {
//        multSquare(A,B,C,N);
//    }
//
//    @Benchmark
//    public void square_fixed() {
//        multSquare(A,B,C,6);
////        multSquare(A,B,C,3);
//    }
//
//    @Benchmark
//    public void col_switch() {
//        if( A.numCols==B.numRows) {
//            switch(A.numCols) {
//                case 1:multCol(A, B, C, 1);return;
//                case 2:multCol(A, B, C, 2);return;
//                case 3:multCol(A, B, C, 3);return;
//                case 4:multCol(A, B, C, 4);return;
//                case 5:multCol(A, B, C, 5);return;
//                case 6:multCol(A, B, C, 6);return;
//            }
//        }
//        CommonOps_DDRM.mult(A,B,C);
//    }
//
//    @Benchmark
//    public void square_switch() {
//        if( A.numCols==A.numRows&&B.numCols==B.numRows) {
//            switch(A.numCols) {
//                case 1:multSquare(A, B, C, 1);return;
//                case 2:multSquare(A, B, C, 2);return;
//                case 3:multSquare(A, B, C, 3);return;
//                case 4:multSquare(A, B, C, 4);return;
//                case 5:multSquare(A, B, C, 5);return;
//                case 6:multSquare(A, B, C, 6);return;
//            }
//        }
//        CommonOps_DDRM.mult(A,B,C);
//    }
//
//    @Benchmark
//    public void square_check() {
//        if( A.numCols==A.numRows&&B.numCols==B.numRows) {
//            multSquare(A, B, C, A.numCols);
//        }
//        CommonOps_DDRM.mult(A,B,C);
//    }

//    @Benchmark
//    public void array_6() {
//        mult6(A.data,0,B.data,0,C.data,0,A.numRows,B.numCols);
//    }

//    @Benchmark
//    public void unrolled_3x3() {
//        mult3x3(A,B,C);
//    }

//    @Benchmark
//    public void unrolled_col() {
//        switch(A.numCols) {
//            case 3:multCol3(A,B,C);return;
//            case 6:multCol6(A,B,C);return;
//        }
//        CommonOps_DDRM.mult(A,B,C);
//    }

    @Benchmark
    public void array_unrollCol() {
        if( !MatrixUnroll_Mult_DDRM.mult(A.data,0,B.data,0,C.data,0,A.numRows,A.numCols,B.numCols)) {
            CommonOps_DDRM.mult(A,B,C);
        }
    }

    @Benchmark
    public void array_jik() {
        if( !MatrixUnroll_MultJIK_DDRM.mult(A.data,0,B.data,0,C.data,0,A.numRows,A.numCols,B.numCols)) {
            CommonOps_DDRM.mult(A,B,C);
        }
    }

    @Benchmark
    public void mult_general() {
        CommonOps_DDRM.mult(A,B,C);
    }

//    @Benchmark
//    public void array() {
//        mult(A.data,B.data,C.data,A.numRows,A.numCols,B.numCols);
//    }

    static void mult(final double[] A, final double[] B , final double[]C , final int rowA , final int colA , final int colB ) {
        int aIndexStart = 0;
        int cIndex = 0;
        for( int i = 0; i < rowA; i++ ) {
            for( int j = 0; j < colB; j++ ) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + colA;
                while( indexA < end ) {
                    total += A[indexA++] * B[indexB];
                    indexB += colB;
                }

                C[ cIndex++ ] = total;
            }
            aIndexStart += colA;
        }
    }

    // could be used to multiply inner block matrices
    static void mult6(final double[] A , final int offsetA ,
                      final double[] B , final int offsetB ,
                      final double[] C , final int offsetC ,
                      final int rowA , final int colB ) {
        int indexA = offsetA;
        int cIndex = offsetC;
        for( int i = 0; i < rowA; i++ ) {
            double a1 = A[indexA++];
            double a2 = A[indexA++];
            double a3 = A[indexA++];
            double a4 = A[indexA++];
            double a5 = A[indexA++];
            double a6 = A[indexA++];

            for( int j = 0; j < colB; j++ ) {
                double total = 0;

                int indexB = offsetB+j;
                total += a1 * B[indexB];indexB += colB;
                total += a2 * B[indexB];indexB += colB;
                total += a3 * B[indexB];indexB += colB;
                total += a4 * B[indexB];indexB += colB;
                total += a5 * B[indexB];indexB += colB;
                total += a6 * B[indexB];

                C[ cIndex++ ] = total;
            }
        }
    }

    static void multCol(final DMatrix1Row A, final DMatrixD1 B, final DMatrixD1 C, final int colA ) {
        int aIndexStart = 0;
        int cIndex = 0;
        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < B.numCols; j++ ) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + colA;
                while( indexA < end ) {
                    total += A.data[indexA++] * B.data[indexB];
                    indexB += B.numCols;
                }

                C.data[ cIndex++ ] = total;
            }
            aIndexStart += colA;
        }
    }

    static void multSquare(final DMatrix1Row A, final DMatrixD1 B, final DMatrixD1 C, final int size ) {
        int aIndexStart = 0;
        int cIndex = 0;
        for( int i = 0; i < size; i++ ) {
            for( int j = 0; j < size; j++ ) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + size;
                while( indexA < end ) {
                    total += A.data[indexA++] * B.data[indexB];
                    indexB += size;
                }

                C.data[ cIndex++ ] = total;
            }
            aIndexStart += size;
        }
    }


    static void mult3x3(final DMatrix1Row A, final DMatrixD1 B, final DMatrixD1 C) {
        double a11 = A.data[0], a12 = A.data[1], a13 = A.data[2];
        double a21 = A.data[3], a22 = A.data[4], a23 = A.data[5];
        double a31 = A.data[6], a32 = A.data[7], a33 = A.data[8];

        double b11 = B.data[0], b12 = B.data[1], b13 = B.data[2];
        double b21 = B.data[3], b22 = B.data[4], b23 = B.data[5];
        double b31 = B.data[6], b32 = B.data[7], b33 = B.data[8];

        C.data[0] = a11*b11 + a12*b21 + a13*b31;
        C.data[1] = a11*b12 + a12*b22 + a13*b32;
        C.data[2] = a11*b13 + a12*b23 + a13*b33;
        C.data[3] = a21*b11 + a22*b21 + a23*b31;
        C.data[4] = a21*b12 + a22*b22 + a23*b32;
        C.data[5] = a21*b13 + a22*b23 + a23*b33;
        C.data[6] = a31*b11 + a32*b21 + a33*b31;
        C.data[7] = a31*b12 + a32*b22 + a33*b32;
        C.data[8] = a31*b13 + a32*b23 + a33*b33;
    }

    static void multCol3(final DMatrix1Row A, final DMatrixD1 B, final DMatrixD1 C ) {
        int indexA = 0;
        int cIndex = 0;
        for( int i = 0; i < A.numRows; i++ ) {
            double a1 = A.data[indexA++];
            double a2 = A.data[indexA++];
            double a3 = A.data[indexA++];

            for( int j = 0; j < B.numCols; j++ ) {
                double total = 0;

                int indexB = j;
                total += a1 * B.data[indexB];indexB += B.numCols;
                total += a2 * B.data[indexB];indexB += B.numCols;
                total += a3 * B.data[indexB];

                C.data[ cIndex++ ] = total;
            }
        }
    }

    static void multCol6(final DMatrix1Row A, final DMatrixD1 B, final DMatrixD1 C ) {
        int indexA = 0;
        int cIndex = 0;
        for( int i = 0; i < A.numRows; i++ ) {
            double a1 = A.data[indexA++];
            double a2 = A.data[indexA++];
            double a3 = A.data[indexA++];
            double a4 = A.data[indexA++];
            double a5 = A.data[indexA++];
            double a6 = A.data[indexA++];

            for( int j = 0; j < B.numCols; j++ ) {
                double total = 0;

                int indexB = j;
                total += a1 * B.data[indexB];indexB += B.numCols;
                total += a2 * B.data[indexB];indexB += B.numCols;
                total += a3 * B.data[indexB];indexB += B.numCols;
                total += a4 * B.data[indexB];indexB += B.numCols;
                total += a5 * B.data[indexB];indexB += B.numCols;
                total += a6 * B.data[indexB];

                C.data[ cIndex++ ] = total;
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkMultUnrolling_DDRM.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
