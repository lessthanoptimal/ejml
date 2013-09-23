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

package org.ejml;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkElement {
    static Random rand = new Random(234);

    public static void main( String args[] ) {
        long N = 10000000;

        double num = 2.5;

        DenseMatrix64F A = RandomMatrices.createRandom(10,10,rand);

        long timeBefore = System.currentTimeMillis();
        for( int i = 0; i < N; i++ ) {
            CommonOps.divide(num,A);
        }
        long timeAfter = System.currentTimeMillis();

        System.out.println("div = "+(timeAfter-timeBefore));

        A = RandomMatrices.createRandom(10,10,rand);

        timeBefore = System.currentTimeMillis();
        for( int i = 0; i < N; i++ ) {
            CommonOps.scale(num,A);
        }
        timeAfter = System.currentTimeMillis();

        System.out.println("scale = "+(timeAfter-timeBefore));
    }
}
