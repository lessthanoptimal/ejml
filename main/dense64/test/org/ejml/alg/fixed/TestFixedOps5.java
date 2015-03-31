/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.fixed;

import org.ejml.data.FixedMatrix5_64F;
import org.ejml.data.FixedMatrix5x5_64F;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestFixedOps5 extends CompareFixedToCommonOps {

    public TestFixedOps5() {
        super(FixedOps5.class);
    }

    @Test
    public void diag() {
        FixedMatrix5x5_64F m = new FixedMatrix5x5_64F(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25);
        FixedMatrix5_64F found = new FixedMatrix5_64F();

        FixedOps5.diag(m,found);

        assertEquals(1,found.a1,1e-8);
        assertEquals(7,found.a2,1e-8);
        assertEquals(13,found.a3,1e-8);
        assertEquals(19,found.a4,1e-8);
        assertEquals(25,found.a5,1e-8);
    }
}
