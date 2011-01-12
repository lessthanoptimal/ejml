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

package org.ejml.alg.block.decomposition.qr;

import org.junit.Test;

/**
 * @author Peter Abeles
 */
public class TestBlockMatrix64HouseholderQR {

    @Test
    public void generic() {
        BlockMatrix64HouseholderQR decomp = new BlockMatrix64HouseholderQR();

        GenericBlock64QrDecompositionTests tests;
        tests = new GenericBlock64QrDecompositionTests(decomp);

        tests.allTests();
    }

    @Test
    public void genericSaveW() {
        BlockMatrix64HouseholderQR decomp = new BlockMatrix64HouseholderQR();
        decomp.setSaveW(true);

        GenericBlock64QrDecompositionTests tests;
        tests = new GenericBlock64QrDecompositionTests(decomp);

        tests.allTests();
    }
}
