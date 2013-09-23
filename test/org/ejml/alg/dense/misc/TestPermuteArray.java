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

package org.ejml.alg.dense.misc;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


/**
 * @author Peter Abeles
 */
public class TestPermuteArray {

    int fact( int N ) {
        int ret = 1;

        while( N > 0 ) {
            ret *= N--;
        }

        return ret;
    }

    /**
     * Sees if the expected number of permutations are created and that they are all
     * unique
     */
    @Test
    public void permuteList() {
        int N = 4;

        List<int[]> perms = PermuteArray.createList(N);

        checkPermutationList(N, perms);
    }

    private void checkPermutationList(int n, List<int[]> perms) {
        assertEquals(PermuteArray.fact(n),perms.size());

        // make sure each permutation in the list is unique
        for( int i = 0; i < perms.size(); i++ ) {
            int a[] = perms.get(i);

            assertEquals(4,a.length);

            for( int j = i+1; j < perms.size(); j++ ) {
                int b[] = perms.get(j);

                boolean identical = true;
                for( int k = 0; k < n; k++ ) {
                    if( a[k] != b[k] ) {
                        identical = false;
                        break;
                    }
                }
                assertFalse(identical);
            }
        }
    }


    @Test
    public void next() {
        // create a list of all the permutations
        PermuteArray alg = new PermuteArray(4);

        List<int[]> perms = new ArrayList<int[]>();
        for(;;) {
            int d[] = alg.next();
            if( d == null )
                break;

            perms.add(d.clone());
        }

        // see if the list is correct
        checkPermutationList(4, perms);
    }
}
