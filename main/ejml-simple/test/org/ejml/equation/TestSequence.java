/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.equation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestSequence {

    int total;

    /**
     * Checks the order in which operations are run
     */
    @Test
    public void order() {
        Sequence s = new Sequence();
        s.addOperation(new Foo("a",0));
        s.addOperation(new Foo("b",1));

        total = 0;
        s.perform();
        assertEquals(2,total);
    }

    public class Foo extends Operation {

        int expected;

        protected Foo(String name , int expected ) {
            super(name);
            this.expected = expected;
        }

        @Override
        public void process() {
            assertEquals(expected, total);
            total++;
        }
    }

}
