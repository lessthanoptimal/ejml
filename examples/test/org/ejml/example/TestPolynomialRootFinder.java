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

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestPolynomialRootFinder {

    @Test
    public void findRoots() {
        Complex_F64[] roots = PolynomialRootFinder.findRoots(4, 3, 2, 1);

        int numReal = 0;
        for( Complex_F64 c : roots ) {
            if( c.isReal() ) {
                checkRoot(c.real,4,3,2,1);
                numReal++;
            }
        }

        assertTrue(numReal>0);
    }

    private void checkRoot( double root , double ...coefs ) {
        double total = 0;

        double a = 1;
        for( double c : coefs ) {
            total += a*c;
            a *= root;
        }

        assertEquals(0,total, UtilEjml.TEST_F64);
    }
}