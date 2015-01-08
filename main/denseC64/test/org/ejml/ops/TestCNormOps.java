/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.ops;

import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestCNormOps {

    Random rand = new Random(234);

    @Test
    public void normF() {
        CDenseMatrix64F a = CRandomMatrices.createRandom(1,7,rand);

        Complex64F b = new Complex64F();
        double total = 0;
        for (int i = 0; i < a.numRows; i++) {
            for (int j = 0; j < a.numCols; j++) {
                a.get(i,j,b);
                total += b.real*b.real + b.imaginary*b.imaginary;
            }
        }

        double expected = Math.sqrt(total);
        double found = CNormOps.normF(a);

        assertEquals(expected,found,1e-8);
    }
}