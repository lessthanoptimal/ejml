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
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestCCommonOps {
    @Test
    public void elementMinReal() {
        CDenseMatrix64F m = new CDenseMatrix64F(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-6, CCommonOps.elementMinReal(m),1e-8);
    }

    @Test
    public void elementMinImaginary() {
        CDenseMatrix64F m = new CDenseMatrix64F(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-5, CCommonOps.elementMinImaginary(m), 1e-8);
    }

    @Test
    public void elementMaxReal() {
        CDenseMatrix64F m = new CDenseMatrix64F(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-6 + 11 * 2, CCommonOps.elementMaxReal(m), 1e-8);
    }

    @Test
    public void elementMaxImaginary() {
        CDenseMatrix64F m = new CDenseMatrix64F(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-5 + 11 * 2, CCommonOps.elementMaxImaginary(m), 1e-8);
    }

}