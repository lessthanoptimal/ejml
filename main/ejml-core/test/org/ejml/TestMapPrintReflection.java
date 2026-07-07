/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ejml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMapPrintReflection {
    @Test void basic() {
        var a = new Foo();
        String found = a.formatMap(new MapPrintFormat().withPrecision(2));
        assertEquals("{moo: 1.12, too: 2.36, zoo: -23, poo: a, stuff: {-9, 4, 2}}", found);
    }

    public static class Foo implements MapFormattable {
        double moo = 1.123456;
        float too = 2.3567f;
        int zoo = -23;
        char poo = 'a';
        int[] stuff = new int[]{-9, 4, 2};

        @Override public String formatMap( MapPrintFormat format ) {
            return MapPrintReflect.formatMap(this, format);
        }
    }
}
