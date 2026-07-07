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
    @Test void DEFAULT() {
        String found = new FormatStress().formatMap(MapPrintFormat.DEFAULT);
        assertEquals("{d0: 1.123456, f0: 4.123456, c0: -324, str0: \"asdf\", arrStr0: [\"cat\", \"dog\"]," +
                " arrD0: [0.456, -0.312, 567.3], arrF0: [0.216, -0.804, 3744.429932], " +
                "arrI0: [-4, 95, 0], inner: {x: 40, y: 9.34}}", found);
    }

    @Test void YAML() {
        var stress = new FormatStress();
        String found = stress.formatMap(MapPrintFormat.YAML);
        assertEquals("{d0: 1.123456, f0: 4.123456, c0: -324, str0: \"asdf\", arrStr0: [\"cat\", \"dog\"]," +
                " arrD0: [0.456, -0.312, 567.3], arrF0: [0.216, -0.804, 3744.429932], arrI0: [-4, 95, 0], " +
                "inner: {x: 40, y: 9.34}}", found);
    }

    @Test void JSON() {
        var stress = new FormatStress();
        String found = stress.formatMap(MapPrintFormat.JSON);
        assertEquals("{\"d0\": 1.123456, \"f0\": 4.123456, \"c0\": -324, \"str0\": \"asdf\", " +
                "\"arrStr0\": [\"cat\", \"dog\"], \"arrD0\": [0.456, -0.312, 567.3], " +
                "\"arrF0\": [0.216, -0.804, 3744.429932], \"arrI0\": [-4, 95, 0], " +
                "\"inner\": {\"x\": 40, \"y\": 9.34}}", found);
    }

    @Test void JAVA() {
        String found = new FormatStress().formatMap(MapPrintFormat.JAVA);
        assertEquals("Map.of(\"d0\", 1.123456, \"f0\", 4.123456, \"c0\", -324, \"str0\", \"asdf\", " +
                "\"arrStr0\", List.of(\"cat\", \"dog\"), \"arrD0\", List.of(0.456, -0.312, 567.3), " +
                "\"arrF0\", List.of(0.216, -0.804, 3744.429932), \"arrI0\", List.of(-4, 95, 0), \"inner\", " +
                "Map.of(\"x\", 40, \"y\", 9.34))", found);
    }

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
        assertEquals("foo: [1, 2, 3.123], ", builder.toString());

        builder.delete(0, builder.length());
        alg.pair(builder, "foo", new double[]{1, 2, 3.1234}, false);
        assertEquals("foo: [1, 2, 3.123]", builder.toString());
    }

    @Test void pair_builder_float_array() {
        var builder = new StringBuilder();
        var alg = new MapPrintFormat();
        alg.precision = 3;
        alg.pair(builder, "foo", new float[]{1, 2, 3.1234f}, true);
        assertEquals("foo: [1, 2, 3.123], ", builder.toString());

        builder.delete(0, builder.length());
        alg.pair(builder, "foo", new float[]{1, 2, 3.1234f}, false);
        assertEquals("foo: [1, 2, 3.123]", builder.toString());
    }

    @Test void tostring_MapPrintFormat() {
        String found = MapPrintFormat.DEFAULT.toString(new WorkObj());
        assertEquals("WorkObj foo", found);
    }

    @Test void setTo() {checkSetTo(MatrixPrintFormat.class);}

    private static class WorkObj implements MapFormattable {
        @Override public String formatMap( MapPrintFormat format ) {
            return "foo";
        }
    }

    public static class InnerStress implements MapFormattable {
        public int x = 40;
        public double y = 9.34;

        @Override public String formatMap( MapPrintFormat format ) {
            return MapPrintReflect.formatMap(this, format);
        }
    }

    public static class FormatStress implements MapFormattable {
        public double d0 = 1.123456;
        public float f0 = 4.123456f;
        public int c0 = -324;
        public String str0 = "asdf";
        public String[] arrStr0 = new String[]{"cat", "dog"};
        public double[] arrD0 = new double[]{0.456, -0.312, 567.3};
        public float[] arrF0 = new float[]{0.216f, -0.804f, 3744.43f};
        public int[] arrI0 = new int[]{-4, 95, 0};
        public InnerStress inner = new InnerStress();

        @Override public String formatMap( MapPrintFormat format ) {
            return MapPrintReflect.formatMap(this, format);
        }
    }
}
