/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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

package org.ejml.data;

import org.ejml.EjmlStandardJUnit;
import org.junit.jupiter.api.Test;

/**
 * @author Peter Abeles
 */
public class TestBlockD3Matrix64F extends EjmlStandardJUnit {
    @Test void testGeneric() {
        GenericTestsDMatrix g;
        g = new GenericTestsDMatrix() {
            @Override
            protected DMatrix createMatrix( int numRows, int numCols ) {
                return new BlockD3Matrix64F(numRows, numCols, 10);
            }
        };

        g.allTests();
    }
}