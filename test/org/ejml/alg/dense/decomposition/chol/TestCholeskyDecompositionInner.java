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

package org.ejml.alg.dense.decomposition.chol;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.CholeskyDecomposition;
import org.junit.Test;

import static org.ejml.alg.dense.decomposition.CheckDecompositionInterface.checkModifiedInput;


/**
 * @author Peter Abeles
 */
public class TestCholeskyDecompositionInner extends GenericCholeskyTests {

    @Override
    public CholeskyDecomposition<DenseMatrix64F> create(boolean lower) {
        return new CholeskyDecompositionInner(lower);
    }

    @Test
    public void checkModifyInput() {
        checkModifiedInput(new CholeskyDecompositionInner(true));
        checkModifiedInput(new CholeskyDecompositionInner(false));
    }
}
