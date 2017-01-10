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

package org.ejml.alg.fixed;

import org.ejml.data.DMatrixFixed3x3_F64;
import org.ejml.data.DMatrixFixed4x4_F64;
import org.ejml.data.DMatrixFixed6x6_F64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.ConvertMatrixStruct_F64;
import org.ejml.ops.RandomMatrices_R64;

import java.util.Random;


/**
 * Feeds the algorithms matrices that are closer and closer to being singular
 * and sees at which point they break.
 *
 * @author Peter Abeles
 */
public class BenchmarkInverseFixed {
    private static Random rand = new Random(234);

    private static DMatrixRow_F64 dm3x3_a = new DMatrixRow_F64(3,3);
    private static DMatrixRow_F64 dm3x3_b = new DMatrixRow_F64(3,3);

    private static DMatrixRow_F64 dm4x4_a = new DMatrixRow_F64(4,4);
    private static DMatrixRow_F64 dm4x4_b = new DMatrixRow_F64(4,4);

    private static DMatrixFixed3x3_F64 fixed3x3_a = new DMatrixFixed3x3_F64();
    private static DMatrixFixed3x3_F64 fixed3x3_b = new DMatrixFixed3x3_F64();

    private static DMatrixFixed4x4_F64 fixed4x4_a = new DMatrixFixed4x4_F64();
    private static DMatrixFixed4x4_F64 fixed4x4_b = new DMatrixFixed4x4_F64();

    private static DMatrixFixed6x6_F64 fixed6x6_a = new DMatrixFixed6x6_F64();
    private static DMatrixFixed6x6_F64 fixed6x6_b = new DMatrixFixed6x6_F64();

    public static long benchmark(DMatrixRow_F64 a, DMatrixRow_F64 b , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            CommonOps_R64.invert(a,b);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long benchmark(DMatrixFixed3x3_F64 a, DMatrixFixed3x3_F64 b , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            FixedOps3_F64.invert(a,b);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long benchmark(DMatrixFixed4x4_F64 a, DMatrixFixed4x4_F64 b , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            FixedOps4_F64.invert(a,b);
        }

        return System.currentTimeMillis() - prev;
    }

    public static void main( String arg[] ) {
        RandomMatrices_R64.setRandom(dm3x3_a,rand);
        RandomMatrices_R64.setRandom(dm3x3_b,rand);

        RandomMatrices_R64.setRandom(dm4x4_a,rand);
        RandomMatrices_R64.setRandom(dm4x4_b,rand);

        ConvertMatrixStruct_F64.convert(dm3x3_a,fixed3x3_a);
        ConvertMatrixStruct_F64.convert(dm3x3_b,fixed3x3_b);

        ConvertMatrixStruct_F64.convert(dm4x4_a,fixed4x4_a);
        ConvertMatrixStruct_F64.convert(dm4x4_b,fixed4x4_b);

        int numTrials = 100000000;

        System.out.println("Fixed 3x3 = "+benchmark(fixed3x3_a,fixed3x3_b,numTrials));
        System.out.println("Dense 3x3 = "+benchmark(dm3x3_a,dm3x3_b,numTrials));

        System.out.println("Fixed 4x4 = "+benchmark(fixed4x4_a,fixed4x4_b,numTrials));
        System.out.println("Dense 4x4 = "+benchmark(dm4x4_a,dm4x4_b,numTrials));

    }
}
