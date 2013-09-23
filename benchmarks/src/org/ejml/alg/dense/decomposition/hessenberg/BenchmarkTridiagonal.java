/*
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

/*
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

package org.ejml.alg.dense.decomposition.hessenberg;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkTridiagonal {


    public static long basic( DenseMatrix64F orig , int numTrials ) {

        TridiagonalDecompositionHouseholder alg = new TridiagonalDecompositionHouseholder();

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( alg.inputModified())
                alg.decompose(orig.copy());
            else
                alg.decompose(orig);

            alg.getQ(null,false);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long alt( DenseMatrix64F orig , int numTrials ) {

        TridiagonalDecompositionHouseholderOrig alg = new TridiagonalDecompositionHouseholderOrig();

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            alg.decompose(orig);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long block( DenseMatrix64F orig , int numTrials ) {


        TridiagonalDecompositionBlock alg = new TridiagonalDecompositionBlock();

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( alg.inputModified())
                alg.decompose(orig.copy());
            else
                alg.decompose(orig);

            alg.getQ(null,false);
        }

        return System.currentTimeMillis() - prev;
    }

    private static void runAlgorithms( DenseMatrix64F mat , int numTrials )
    {
        System.out.println("basic            = "+ basic(mat,numTrials));
//        System.out.println("alt              = "+ alt(mat,numTrials));
        System.out.println("block            = "+ block(mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,10,100,500,1000,2000,5000};
        int trials[] = new int[]{(int)8e6,(int)2e6,(int)2e5,600,12,3,1,1,1};

        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 3; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Processing size %3d for %12d trials\n",w,trials[i]);

            System.out.print("* Creating matrix ");
            DenseMatrix64F mat = RandomMatrices.createRandom(w,w,rand);
            System.out.println("  Done.");
            runAlgorithms(mat,trials[i]);
        }
    }
}