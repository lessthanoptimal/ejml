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
