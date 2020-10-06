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

package org.ejml.example;

import org.ejml.concurrency.EjmlConcurrency;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.CommonOps_MT_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;

import java.util.Random;

/**
 * Concurrent or multi-threaded algorithms are a recent addition to EJML. Classes with concurrent implementations
 * can be identified with _MT_ in the class name. For example CommonOps_MT_DDRM will contain concurrent implementations
 * of operations such as matrix multiplication for dense row-major algorithms. Not everything has a concurrent
 * implementation yet and in some cases entirely new algorithms will need to be implemented.
 *
 * @author Peter Abeles
 */
public class ExampleConcurrent {
    public static void main( String[] args ) {
        // Create a few random matrices that we will multiply and decompose
        var rand = new Random(0xBEEF);
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4000,4000,-1,1,rand);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(A.numCols,1000,-1,1,rand);
        DMatrixRMaj C = new DMatrixRMaj(1,1);

        // First do a concurrent matrix multiply using the default number of threads
        System.out.println("Matrix Multiply, threads="+EjmlConcurrency.getMaxThreads());
        long time0 = System.currentTimeMillis();
        CommonOps_MT_DDRM.mult(A,B,C);
        long time1 = System.currentTimeMillis();
        System.out.println("Elapsed time "+(time1-time0)+" (ms)");

        // Set it to two threads
        EjmlConcurrency.setMaxThreads(2);
        System.out.println("Matrix Multiply, threads="+EjmlConcurrency.getMaxThreads());
        long time2 = System.currentTimeMillis();
        CommonOps_MT_DDRM.mult(A,B,C);
        long time3 = System.currentTimeMillis();
        System.out.println("Elapsed time "+(time3-time2)+" (ms)");

        // Then let's compare it against the single thread implementation
        System.out.println("Matrix Multiply, Single Thread");
        long time4 = System.currentTimeMillis();
        CommonOps_DDRM.mult(A,B,C);
        long time5 = System.currentTimeMillis();
        System.out.println("Elapsed time "+(time5-time4)+" (ms)");

        // Setting the number of threads to 1 then running am MT implementation actually calls completely different
        // code than the regular function calls and will be less efficient. This will probably only be evident on
        // small matrices though

        // If the future we will provide a way to optionally automatically switch to concurrent implementations
        // for larger when calling standard functions.
    }
}
