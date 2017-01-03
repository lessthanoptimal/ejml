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

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.FixedMatrix3x3_64F;
import org.ejml.data.FixedMatrix4x4_64F;
import org.ejml.data.FixedMatrix6x6_64F;
import org.ejml.ops.CommonOps_D64;
import org.ejml.ops.ConvertMatrixStruct_F64;
import org.ejml.ops.RandomMatrices_D64;

import java.util.Random;


/**
 * Feeds the algorithms matrices that are closer and closer to being singular
 * and sees at which point they break.
 *
 * @author Peter Abeles
 */
public class BenchmarkInverseFixed {
    private static Random rand = new Random(234);

    private static DenseMatrix64F dm3x3_a = new DenseMatrix64F(3,3);
    private static DenseMatrix64F dm3x3_b = new DenseMatrix64F(3,3);

    private static DenseMatrix64F dm4x4_a = new DenseMatrix64F(4,4);
    private static DenseMatrix64F dm4x4_b = new DenseMatrix64F(4,4);

    private static DenseMatrix64F dm6x6_a = new DenseMatrix64F(6,6);
    private static DenseMatrix64F dm6x6_b = new DenseMatrix64F(6,6);

    private static FixedMatrix3x3_64F fixed3x3_a = new FixedMatrix3x3_64F();
    private static FixedMatrix3x3_64F fixed3x3_b = new FixedMatrix3x3_64F();

    private static FixedMatrix4x4_64F fixed4x4_a = new FixedMatrix4x4_64F();
    private static FixedMatrix4x4_64F fixed4x4_b = new FixedMatrix4x4_64F();

    private static FixedMatrix6x6_64F fixed6x6_a = new FixedMatrix6x6_64F();
    private static FixedMatrix6x6_64F fixed6x6_b = new FixedMatrix6x6_64F();

    public static long benchmark(DenseMatrix64F a, DenseMatrix64F b , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            CommonOps_D64.invert(a,b);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long benchmark(FixedMatrix3x3_64F a, FixedMatrix3x3_64F b , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            FixedOps3_D64.invert(a,b);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long benchmark(FixedMatrix4x4_64F a, FixedMatrix4x4_64F b , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            FixedOps4_D64.invert(a,b);
        }

        return System.currentTimeMillis() - prev;
    }

    public static void main( String arg[] ) {
        RandomMatrices_D64.setRandom(dm3x3_a,rand);
        RandomMatrices_D64.setRandom(dm3x3_b,rand);

        RandomMatrices_D64.setRandom(dm4x4_a,rand);
        RandomMatrices_D64.setRandom(dm4x4_b,rand);

        RandomMatrices_D64.setRandom(dm6x6_a,rand);
        RandomMatrices_D64.setRandom(dm6x6_b,rand);

        ConvertMatrixStruct_F64.convert(dm3x3_a,fixed3x3_a);
        ConvertMatrixStruct_F64.convert(dm3x3_b,fixed3x3_b);

        ConvertMatrixStruct_F64.convert(dm4x4_a,fixed4x4_a);
        ConvertMatrixStruct_F64.convert(dm4x4_b,fixed4x4_b);

        ConvertMatrixStruct_F64.convert(dm6x6_a,fixed6x6_a);
        ConvertMatrixStruct_F64.convert(dm6x6_b,fixed6x6_b);

        int numTrials = 100000;

        System.out.println("Dense 3x3 = "+benchmark(dm3x3_a,dm3x3_b,numTrials));
        System.out.println("Fixed 3x3 = "+benchmark(fixed3x3_a,fixed3x3_b,numTrials));

        System.out.println("Dense 4x4 = "+benchmark(dm4x4_a,dm4x4_b,numTrials));
        System.out.println("Fixed 4x4 = "+benchmark(fixed4x4_a,fixed4x4_b,numTrials));

    }
}
