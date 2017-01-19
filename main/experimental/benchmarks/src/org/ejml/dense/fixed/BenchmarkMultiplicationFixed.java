/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.fixed;

import org.ejml.data.DMatrix3x3;
import org.ejml.data.DMatrix4x4;
import org.ejml.data.DMatrix6x6;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.ops.ConvertMatrixStruct_F64;

import java.util.Random;


/**
 * Feeds the algorithms matrices that are closer and closer to being singular
 * and sees at which point they break.
 *
 * @author Peter Abeles
 */
public class BenchmarkMultiplicationFixed {
    private static Random rand = new Random(234);

    private static DMatrixRMaj dm3x3_a = new DMatrixRMaj(3,3);
    private static DMatrixRMaj dm3x3_b = new DMatrixRMaj(3,3);
    private static DMatrixRMaj dm3x3_c = new DMatrixRMaj(3,3);

    private static DMatrixRMaj dm4x4_a = new DMatrixRMaj(4,4);
    private static DMatrixRMaj dm4x4_b = new DMatrixRMaj(4,4);
    private static DMatrixRMaj dm4x4_c = new DMatrixRMaj(4,4);

    private static DMatrixRMaj dm6x6_a = new DMatrixRMaj(6,6);
    private static DMatrixRMaj dm6x6_b = new DMatrixRMaj(6,6);
    private static DMatrixRMaj dm6x6_c = new DMatrixRMaj(6,6);


    private static DMatrix3x3 fixed3x3_a = new DMatrix3x3();
    private static DMatrix3x3 fixed3x3_b = new DMatrix3x3();
    private static DMatrix3x3 fixed3x3_c = new DMatrix3x3();

    private static DMatrix4x4 fixed4x4_a = new DMatrix4x4();
    private static DMatrix4x4 fixed4x4_b = new DMatrix4x4();
    private static DMatrix4x4 fixed4x4_c = new DMatrix4x4();

    private static DMatrix6x6 fixed6x6_a = new DMatrix6x6();
    private static DMatrix6x6 fixed6x6_b = new DMatrix6x6();
    private static DMatrix6x6 fixed6x6_c = new DMatrix6x6();

    public static long benchmark(DMatrixRMaj a, DMatrixRMaj b , DMatrixRMaj c , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.mult(a,b,c);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long benchmark(DMatrix3x3 a, DMatrix3x3 b , DMatrix3x3 c , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            CommonOps_DDF3.mult(a,b,c);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long benchmark(DMatrix4x4 a, DMatrix4x4 b , DMatrix4x4 c , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            CommonOps_DDF4.mult(a,b,c);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long benchmark(DMatrix6x6 a, DMatrix6x6 b , DMatrix6x6 c , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            CommonOps_DDF6.mult(a,b,c);
        }

        return System.currentTimeMillis() - prev;
    }

    public static void main( String arg[] ) {
        RandomMatrices_DDRM.setRandom(dm3x3_a,rand);
        RandomMatrices_DDRM.setRandom(dm3x3_b,rand);
        RandomMatrices_DDRM.setRandom(dm3x3_c,rand);

        RandomMatrices_DDRM.setRandom(dm4x4_a,rand);
        RandomMatrices_DDRM.setRandom(dm4x4_b,rand);
        RandomMatrices_DDRM.setRandom(dm4x4_c,rand);

        RandomMatrices_DDRM.setRandom(dm6x6_a,rand);
        RandomMatrices_DDRM.setRandom(dm6x6_b,rand);
        RandomMatrices_DDRM.setRandom(dm6x6_c,rand);

        ConvertMatrixStruct_F64.convert(dm3x3_a,fixed3x3_a);
        ConvertMatrixStruct_F64.convert(dm3x3_b,fixed3x3_b);
        ConvertMatrixStruct_F64.convert(dm3x3_c,fixed3x3_c);

        ConvertMatrixStruct_F64.convert(dm4x4_a,fixed4x4_a);
        ConvertMatrixStruct_F64.convert(dm4x4_b,fixed4x4_b);
        ConvertMatrixStruct_F64.convert(dm4x4_c,fixed4x4_c);

        ConvertMatrixStruct_F64.convert(dm6x6_a,fixed6x6_a);
        ConvertMatrixStruct_F64.convert(dm6x6_b,fixed6x6_b);
        ConvertMatrixStruct_F64.convert(dm6x6_c,fixed6x6_c);

        int numTrials = 100000000;

        System.out.println("Dense 3x3 = "+benchmark(dm3x3_a,dm3x3_b,dm3x3_c,numTrials));
        System.out.println("Fixed 3x3 = "+benchmark(fixed3x3_a,fixed3x3_b,fixed3x3_c,numTrials));

        numTrials = 50000000;


        System.out.println("Dense 4x4 = "+benchmark(dm4x4_a,dm4x4_b,dm4x4_c,numTrials));
        System.out.println("Fixed 4x4 = "+benchmark(fixed4x4_a,fixed4x4_b,fixed4x4_c,numTrials));

        numTrials = 10000000;

        System.out.println("Dense 6x6 = "+benchmark(dm6x6_a,dm6x6_b,dm6x6_c,numTrials));
        System.out.println("Fixed 6x6 = "+benchmark(fixed6x6_a,fixed6x6_b,fixed6x6_c,numTrials));
    }
}
