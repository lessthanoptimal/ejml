/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.Complex64F;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestPolynomialRootFinder {

    @Test
    public void findRoots() {
        Complex64F[] roots = PolynomialRootFinder.findRoots(4, 3, 2, 1);

        int numReal = 0;
        for( Complex64F c : roots ) {
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

        assertEquals(0,total,1e-8);
    }
}