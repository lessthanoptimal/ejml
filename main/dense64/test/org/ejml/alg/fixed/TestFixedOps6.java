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

import org.ejml.data.FixedMatrix6_64F;
import org.ejml.data.FixedMatrix6x6_64F;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestFixedOps6 extends CompareFixedToCommonOps {

    public TestFixedOps6() {
        super(FixedOps6.class);
    }

    @Test
    public void diag() {
        FixedMatrix6x6_64F m = new FixedMatrix6x6_64F();
        for( int i = 0; i < 36; i++ )
            m.set(i/6,i%6,i+1);
        FixedMatrix6_64F found = new FixedMatrix6_64F();

        FixedOps6.diag(m,found);

        assertEquals(1,found.a1,1e-8);
        assertEquals(8,found.a2,1e-8);
        assertEquals(15,found.a3,1e-8);
        assertEquals(22,found.a4,1e-8);
        assertEquals(29,found.a5,1e-8);
        assertEquals(36,found.a6,1e-8);
    }
}
