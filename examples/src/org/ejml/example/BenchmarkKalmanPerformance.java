/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.example;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.util.ArrayList;
import java.util.List;

/**
 * Compares how fast the filters all run relative to each other.
 *
 * @author Peter Abeles
 */
public class BenchmarkKalmanPerformance {

    private static final int NUM_TRIALS = 200;
    private static final int MAX_STEPS = 1000;
    private static final double T = 1.0;

    private static int measDOF = 8;

    List<KalmanFilter> filters = new ArrayList<KalmanFilter>();

    public void run() {
        DenseMatrix64F priorX = new DenseMatrix64F(9,1, true, 0.5, -0.2, 0, 0, 0.2, -0.9, 0, 0.2, -0.5);
        DenseMatrix64F priorP = CommonOps.identity(9);

        DenseMatrix64F trueX = new DenseMatrix64F(9,1, true, 0, 0, 0, 0.2, 0.2, 0.2, 0.5, 0.1, 0.6);

        List<DenseMatrix64F> meas = createSimulatedMeas(trueX);

        DenseMatrix64F F = createF(T);
        DenseMatrix64F Q = createQ(T,0.1);
        DenseMatrix64F H = createH();

        for(KalmanFilter f : filters ) {

            long timeBefore = System.currentTimeMillis();

            f.configure(F,Q,H);

            for( int trial = 0; trial < NUM_TRIALS; trial++ ) {
                f.setState(priorX,priorP);
                processMeas(f,meas);
            }

            long timeAfter = System.currentTimeMillis();

            System.out.println("Filter = "+f.getClass().getSimpleName());
            System.out.println("Elapsed time: "+(timeAfter-timeBefore));

            System.gc();
        }
    }

    private List<DenseMatrix64F> createSimulatedMeas( DenseMatrix64F x ) {

        List<DenseMatrix64F> ret = new ArrayList<DenseMatrix64F>();

        DenseMatrix64F F = createF(T);
        DenseMatrix64F H = createH();

//        UtilEjml.print(F);
//        UtilEjml.print(H);

        DenseMatrix64F x_next = new DenseMatrix64F(x);
        DenseMatrix64F z = new DenseMatrix64F(H.numRows,1);

        for( int i = 0; i < MAX_STEPS; i++ ) {
            CommonOps.mult(F,x,x_next);
            CommonOps.mult(H,x_next,z);
            ret.add(z.copy());
            x.set(x_next);
        }

        return ret;
    }

    private void processMeas( KalmanFilter f ,
                              List<DenseMatrix64F> meas )
    {
        DenseMatrix64F R = CommonOps.identity(measDOF);

        for(DenseMatrix64F z : meas ) {
            f.predict();
            f.update(z,R);
        }
    }


    public static DenseMatrix64F createF( double T ) {
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

        return new DenseMatrix64F(9,9, true, a);
    }

    public static DenseMatrix64F createQ( double T , double var ) {
        DenseMatrix64F Q = new DenseMatrix64F(9,9);

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

    public static DenseMatrix64F createH() {
        DenseMatrix64F H = new DenseMatrix64F(measDOF,9);
        for( int i = 0; i < measDOF; i++ ) {
            H.set(i,i,1.0);
        }

        return H;
    }

    public static void main( String args[] ) {
        BenchmarkKalmanPerformance benchmark = new BenchmarkKalmanPerformance();

        benchmark.filters.add( new KalmanFilterAlg() );
        benchmark.filters.add( new KalmanFilterOps());
        benchmark.filters.add( new KalmanFilterSimple());


        benchmark.run();
    }
}
