/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
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
            assertEquals(coef[i],found[i],1e-8);
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
            assertTrue(Math.abs(coef[i]-found[i])>1e-8);
        }

        //remove the outlier
        alg.removeWorstFit();

        // now see if the solution is perfect
        found = alg.getCoef();

        for( int i = 0; i < coef.length; i++ ) {
            assertEquals(coef[i],found[i],1e-8);
        }
    }
}
