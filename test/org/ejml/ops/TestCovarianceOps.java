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

package org.ejml.ops;

import org.ejml.data.DenseMatrix64F;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestCovarianceOps {
    @Test
    public void isValid() {
        // nothing is wrong with it
        DenseMatrix64F m = CommonOps.identity(3);
        assertEquals(0, CovarianceOps.isValid(m));

        // negative diagonal term
        m.set(1,1,-3);
        assertEquals(1, CovarianceOps.isValid(m));

        // not symetric
        m = CommonOps.identity(3);
        m.set(1,0,30);
        assertEquals(2, CovarianceOps.isValid(m));

        // not positive definite
        m = CommonOps.identity(3);
        m.set(1,2,-400);
        m.set(2,1,-400);
        assertEquals(3, CovarianceOps.isValid(m));
    }
}
