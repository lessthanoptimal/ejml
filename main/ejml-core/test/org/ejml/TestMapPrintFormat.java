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

public class TestMapPrintFormat extends EjmlStandardJUnit {
    @Test void pair_builder_double() {
        var builder = new StringBuilder();
        var alg = new MapPrintFormat();
        alg.precision = 3;
        alg.pair(builder, "foo", 1.12345, true);
        assertEquals("foo: 1.123, ", builder.toString());

        builder.delete(0, builder.length());
        alg.pair(builder, "foo", 1.12345, false);
        assertEquals("foo: 1.123", builder.toString());
    }

    @Test void pair_double() {
        var alg = new MapPrintFormat();
        alg.precision = 3;
        assertEquals("foo: 1.123, ", alg.pair("foo", 1.12345, true));
        assertEquals("foo: 1.123", alg.pair("foo", 1.12345, false));
    }

    @Test void pair_string() {
        var alg = new MapPrintFormat();
        assertEquals("foo: bar, ", alg.pair("foo", "bar", true));
        assertEquals("foo: bar", alg.pair("foo", "bar", false));
    }

    @Test void pair_builder_double_array() {
        var builder = new StringBuilder();
        var alg = new MapPrintFormat();
        alg.precision = 3;
        alg.pair(builder, "foo", new double[]{1, 2, 3.1234}, true);
        assertEquals("foo: {1, 2, 3.123}, ", builder.toString());

        builder.delete(0, builder.length());
        alg.pair(builder, "foo", new double[]{1, 2, 3.1234}, false);
        assertEquals("foo: {1, 2, 3.123}", builder.toString());
    }

    @Test void pair_builder_float_array() {
        var builder = new StringBuilder();
        var alg = new MapPrintFormat();
        alg.precision = 3;
        alg.pair(builder, "foo", new float[]{1, 2, 3.1234f}, true);
        assertEquals("foo: {1, 2, 3.123}, ", builder.toString());

        builder.delete(0, builder.length());
        alg.pair(builder, "foo", new float[]{1, 2, 3.1234f}, false);
        assertEquals("foo: {1, 2, 3.123}", builder.toString());
    }

    @Test void tostring_MapPrintFormat() {
        String found = MapPrintFormat.DEFAULT.toString(new WorkObj());
        assertEquals("WorkObj foo", found);
    }

    private static class WorkObj implements MapFormattable {
        @Override public String formatMap( MapPrintFormat format ) {
            return "foo";
        }
    }
}
