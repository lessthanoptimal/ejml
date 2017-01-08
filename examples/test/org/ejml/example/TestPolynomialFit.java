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

package org.ejml.example;

import org.ejml.UtilEjml;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestPolynomialFit {

    /**
     * Test with perfect data
     */
    @Test
    public void testPerfect() {
        double coef[] = new double[]{1,-2,3};

        double x[] = new double[]{-2,1,0.5,2,3,4,5,7,8,9.2,10.2,4.3,6.7};
        double y[] = new double[ x.length ];

        for( int i = 0; i < y.length; i++ ) {
            double v = 0;
            double xx = 1;
            for (double c : coef) {
                v += c * xx;
                xx *= x[i];
            }

            y[i] = v;
        }

        PolynomialFit alg = new PolynomialFit(2);

        alg.fit(x,y);

        double found[] = alg.getCoef();

        for( int i = 0; i < coef.length; i++ ) {
            assertEquals(coef[i],found[i], UtilEjml.TEST_F64);
        }
    }

    /**
     * Make one of the observations way off and see if it is removed
     */
    @Test
    public void testNoise() {
        double coef[] = new double[]{1,-2,3};

        double x[] = new double[]{-2,1,0.5,2,3,4,5,7,8,9.2,10.2,4.3,6.7};
        double y[] = new double[ x.length ];

        for( int i = 0; i < y.length; i++ ) {
            double v = 0;
            double xx = 1;
            for (double c : coef) {
                v += c * xx;
                xx *= x[i];
            }

            y[i] = v;
        }

        y[4] += 3.5;

        PolynomialFit alg = new PolynomialFit(2);

        alg.fit(x,y);

        double found[] = alg.getCoef();

        // the coefficients that it initialy computes should be incorrect

        for( int i = 0; i < coef.length; i++ ) {
            assertTrue(Math.abs(coef[i]-found[i])>UtilEjml.TEST_F64);
        }

        //remove the outlier
        alg.removeWorstFit();

        // now see if the solution is perfect
        found = alg.getCoef();

        for( int i = 0; i < coef.length; i++ ) {
            assertEquals(coef[i],found[i],UtilEjml.TEST_F64);
        }
    }
}
