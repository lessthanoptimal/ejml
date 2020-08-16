/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

package org.ejml.example;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Uses JMH to compare the speed of different Kalman filter implementations.
 *
 * <pre>
 * Benchmark                                 Mode  Cnt     Score    Error  Units
 * BenchmarkKalmanPerformance.equations      avgt    5  1282.423 ± 30.398  ms/op
 * BenchmarkKalmanPerformance.operations     avgt    5  1022.874 ± 29.654  ms/op
 * BenchmarkKalmanPerformance.simple_matrix  avgt    5  1391.853 ± 23.626  ms/op
 * </pre>
 *
 * @author Peter Abeles
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value=1)
public class BenchmarkKalmanPerformance {

    private static final int NUM_TRIALS = 200;
    private static final int MAX_STEPS = 1000;
    private static final double T = 1.0;

    private static int measDOF = 8;

    DMatrixRMaj priorX = new DMatrixRMaj(9,1, true, 0.5, -0.2, 0, 0, 0.2, -0.9, 0, 0.2, -0.5);
    DMatrixRMaj priorP = CommonOps_DDRM.identity(9);

    DMatrixRMaj trueX = new DMatrixRMaj(9,1, true, 0, 0, 0, 0.2, 0.2, 0.2, 0.5, 0.1, 0.6);

    List<DMatrixRMaj> meas = createSimulatedMeas(trueX);

    DMatrixRMaj F = createF(T);
    DMatrixRMaj Q = createQ(T,0.1);
    DMatrixRMaj H = createH();

    @Benchmark
    public void operations() {
        evaluate(new KalmanFilterOperations());
    }

    @Benchmark
    public void equations() {
        evaluate(new KalmanFilterEquation());
    }

    @Benchmark
    public void simple_matrix() {
        evaluate(new KalmanFilterSimple());
    }

    private void evaluate(KalmanFilter filter) {
        filter.configure(F,Q,H);

        for( int trial = 0; trial < NUM_TRIALS; trial++ ) {
            filter.setState(priorX,priorP);
            processMeas(filter,meas);
        }
    }

    private List<DMatrixRMaj> createSimulatedMeas(DMatrixRMaj x ) {

        List<DMatrixRMaj> ret = new ArrayList<DMatrixRMaj>();

        DMatrixRMaj F = createF(T);
        DMatrixRMaj H = createH();

        DMatrixRMaj x_next = new DMatrixRMaj(x);
        DMatrixRMaj z = new DMatrixRMaj(H.numRows,1);

        for( int i = 0; i < MAX_STEPS; i++ ) {
            CommonOps_DDRM.mult(F,x,x_next);
            CommonOps_DDRM.mult(H,x_next,z);
            ret.add(z.copy());
            x.set(x_next);
        }

        return ret;
    }

    private void processMeas( KalmanFilter filter ,
                              List<DMatrixRMaj> meas )
    {
        DMatrixRMaj R = CommonOps_DDRM.identity(measDOF);

        for(DMatrixRMaj z : meas ) {
            filter.predict();
            filter.update(z,R);
        }
    }


    public static DMatrixRMaj createF(double T ) {
        double []a = new double[]{
                1, 0 , 0 , T , 0 , 0 , 0.5*T*T , 0 , 0 ,
                0, 1 , 0 , 0 , T , 0 , 0 , 0.5*T*T , 0 ,
                0, 0 , 1 , 0 , 0 , T , 0 , 0 , 0.5*T*T ,
                0, 0 , 0 , 1 , 0 , 0 , T , 0 , 0 ,
                0, 0 , 0 , 0 , 1 , 0 , 0 , T , 0 ,
                0, 0 , 0 , 0 , 0 , 1 , 0 , 0 , T ,
                0, 0 , 0 , 0 , 0 , 0 , 1 , 0 , 0 ,
                0, 0 , 0 , 0 , 0 , 0 , 0 , 1 , 0 ,
                0, 0 , 0 , 0 , 0 , 0 , 0 , 0 , 1 };

        return new DMatrixRMaj(9,9, true, a);
    }

    public static DMatrixRMaj createQ(double T , double var ) {
        DMatrixRMaj Q = new DMatrixRMaj(9,9);

        double a00 = (1.0/4.0)*T*T*T*T*var;
        double a01 = (1.0/2.0)*T*T*T*var;
        double a02 = (1.0/2.0)*T*T*var;
        double a11 = T*T*var;
        double a12 = T*var;
        double a22 = var;

        for( int i = 0; i < 3; i++ ) {
            Q.set(i,i,a00);
            Q.set(i,3+i,a01);
            Q.set(i,6+i,a02);
            Q.set(3+i,3+i,a11);
            Q.set(3+i,6+i,a12);
            Q.set(6+i,6+i,a22);
        }

        for( int y = 1; y < 9; y++ ) {
            for( int x = 0; x < y; x++ ) {
                Q.set(y,x, Q.get(x,y));
            }
        }

        return Q;
    }

    public static DMatrixRMaj createH() {
        DMatrixRMaj H = new DMatrixRMaj(measDOF,9);
        for( int i = 0; i < measDOF; i++ ) {
            H.set(i,i,1.0);
        }

        return H;
    }
}
