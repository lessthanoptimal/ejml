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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/// Generates a default [MapFormattable#formatMap] implementation via reflection.
/// Handles primitives, boxed primitives, String, primitive numeric arrays, object
/// arrays, and nested [MapFormattable] instances. Anything else falls back to
/// `toString()`.
public final class MapPrintReflect {

    /// Cache of printable fields per class so reflection lookup happens once.
    private static final Map<Class<?>, Field[]> FIELD_CACHE = new ConcurrentHashMap<>();

    private MapPrintReflect() {}

    /// Reflection-based formatMap using [MapPrintFormat#DEFAULT].
    public static String formatMap( Object object ) {
        return formatMap(object, MapPrintFormat.DEFAULT);
    }

    /// Reflection-based [MapFormattable#formatMap(MapPrintFormat)]. Walks every
    /// non-static, non-synthetic field of the object's class and its superclasses.
    public static String formatMap( Object object, MapPrintFormat format ) {
        Field[] fields = getFields(object.getClass());

        // Resolve values first, dropping anything unreadable, so the trailing-separator
        // ("is there a following pair?") logic stays correct even if a field is skipped.
        List<String> names = new ArrayList<>(fields.length);
        List<Object> values = new ArrayList<>(fields.length);
        for (Field field : fields) {
            try {
                values.add(field.get(object));
                names.add(field.getName());
            } catch (IllegalAccessException ignore) { /* skip */ }
        }

        var builder = new StringBuilder();
        builder.append(format.itemPrefix);
        for (int i = 0; i < names.size(); i++) {
            boolean isMore = i + 1 < names.size();
            appendValue(builder, format, names.get(i), values.get(i), isMore);
        }
        builder.append(format.itemSuffix);
        return builder.toString();
    }

    private static Field[] getFields( Class<?> type ) {
        Field[] cached = FIELD_CACHE.get(type);
        if (cached != null)
            return cached;

        List<Field> list = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.isSynthetic())
                    continue;
                try {
                    f.setAccessible(true);
                } catch (RuntimeException e) {
                    continue; // e.g. InaccessibleObjectException under the module system
                }
                list.add(f);
            }
        }
        Field[] array = list.toArray(new Field[0]);
        FIELD_CACHE.put(type, array);
        return array;
    }

    private static void appendValue( StringBuilder b, MapPrintFormat format,
                                     String name, Object value, boolean isMore ) {
        if (value == null) {
            b.append(format.pair(name, "null", isMore));
        } else if (value instanceof MapFormattable mf) {
            appendKey(b, format, name);
            b.append(mf.formatMap(format));   // recurse
            appendTrailing(b, format, isMore);
        } else if (value instanceof Number n) {
            format.pair(b, name, n.doubleValue(), isMore);
        } else if (value instanceof double[] d) {
            format.pair(b, name, d, isMore);
        } else if (value instanceof float[] f) {
            format.pair(b, name, f, isMore);
        } else if (value.getClass().isArray()) {
            appendArray(b, format, name, value, isMore);
        } else if (value instanceof String s) {
            b.append(format.pair(name, s, isMore));
        } else {
            // Boolean, Character, enum, or any other object
            b.append(format.pair(name, value.toString(), isMore));
        }
    }

    private static void appendArray( StringBuilder b, MapPrintFormat format,
                                     String name, Object array, boolean isMore ) {
        int len = Array.getLength(array);
        Class<?> comp = array.getClass().getComponentType();

        // Numeric primitive arrays (int/long/short/byte) -> reuse the built-in double[] path.
        if (comp.isPrimitive() && comp != boolean.class && comp != char.class) {
            double[] copy = new double[len];
            for (int i = 0; i < len; i++)
                copy[i] = Array.getDouble(array, i); // widening conversion
            format.pair(b, name, copy, isMore);
            return;
        }

        // boolean[], char[], String[], Object[], MapFormattable[], ...
        appendKey(b, format, name);
        b.append(format.itemPrefix);
        for (int i = 0; i < len; i++) {
            if (i > 0) b.append(format.pairSeparator);
            b.append(formatElement(format, Array.get(array, i)));
        }
        b.append(format.itemSuffix);
        appendTrailing(b, format, isMore);
    }

    private static String formatElement( MapPrintFormat format, Object el ) {
        if (el == null) return "null";
        if (el instanceof MapFormattable mf) return mf.formatMap(format);
        return el.toString();
    }

    private static void appendKey( StringBuilder b, MapPrintFormat format, String name ) {
        b.append(format.keyPrefix).append(name).append(format.keySuffix)
                .append(format.valueSeparator);
    }

    private static void appendTrailing( StringBuilder b, MapPrintFormat format, boolean isMore ) {
        if (isMore) b.append(format.pairSeparator);
    }
}