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

import lombok.Getter;
import lombok.Setter;

/// Describes how to format an object when it's converted into a map style string.
public class MapPrintFormat extends PrintFormat {
    /// Default valued used in toString and other location. Modifying this will modify the formatting in many locations
    /// Only the end user should be tweaking this and not any library and its subject to change.
    ///
    /// ```
    /// [{row: 3, col: 1, value: 3.966},
    /// {row: 1, col: 2, value: 1.2345},
    /// {row: 0, col: 4, value: 2.1}]
    /// ```
    public final static MapPrintFormat DEFAULT = new MapPrintFormat();

    /// Python Dict style:
    ///
    /// ```
    /// [{"row": 3, "col": 1, "value": 3.966},
    /// {"row": 1, "col": 2, "value": 1.2345},
    /// {"row": 0, "col": 4, "value": 2.1}]
    /// ```
    public final static MapPrintFormat PYTHON = new MapPrintFormat(
            DEFAULT_PRECISION, ": ", ", ", "{", "}", ",\n", "[", "]").withKeyPrefix("\"").withKeySuffix("\"");

    /// JSON style:
    ///
    /// ```
    /// [{"row": 3, "col": 1, "value": 3.966},
    /// {"row": 1, "col": 2, "value": 1.2345},
    /// {"row": 0, "col": 4, "value": 2.1}]
    /// ```
    public final static MapPrintFormat JSON = PYTHON;

    /// YAML style:
    ///
    /// ```
    /// - row: 3
    ///   col: 1
    ///   value: 3.966
    /// - row: 1
    ///   col: 2
    ///   value: 1.2345
    /// - row: 0
    ///   col: 4
    ///   value: 2.1
    /// ```
    public final static MapPrintFormat YAML = new MapPrintFormat(
            DEFAULT_PRECISION, ": ", "\n  ", "- ", "", "\n", "", "");

    /// Java style:
    ///
    /// ```
    /// List.of(Map.of("row", 3, "col", 1, "value", 3.966),
    ///         Map.of("row", 1, "col", 2, "value", 1.2345),
    ///         Map.of("row", 0, "col", 4, "value", 2.1))
    /// ```
    ///
    public final static MapPrintFormat JAVA = new MapPrintFormat(DEFAULT_PRECISION, ", ", ", ", "Map.of(", ")", ",\n        ", "List.of(", ")")
            .withKeyPrefix("\"").withKeySuffix("\"");

    /// Separator that splits key-value pairs
    @Getter @Setter public String valueSeparator = ": ";
    /// Separator between two key-value pairs
    @Getter @Setter public String pairSeparator = ", ";
    /// Prefix for an item in the list. If a single item, this is still applied.
    @Getter @Setter public String itemPrefix = "{";
    /// Suffix for an item in the list. If a single item, this is still applied.
    @Getter @Setter public String itemSuffix = "}";
    /// Separator applied after each item in the list
    @Getter @Setter public String itemSeparator = ",\n";
    /// Prefix applied before the list
    @Getter @Setter public String listPrefix = "[";
    /// Prefix applied after the list
    @Getter @Setter public String listSuffix = "]";
    /// Prefix added before each key. Use to quote keys for languages that require it.
    @Getter @Setter public String keyPrefix = "";
    /// Suffix added after each key.
    @Getter @Setter public String keySuffix = "";

    public MapPrintFormat() {}

    public MapPrintFormat( int precision, String valueSeparator, String pairSeparator,
                           String itemPrefix, String itemSuffix, String itemSeparator,
                           String listPrefix, String listSuffix ) {
        this.precision = precision;
        this.valueSeparator = valueSeparator;
        this.pairSeparator = pairSeparator;
        this.itemPrefix = itemPrefix;
        this.itemSuffix = itemSuffix;
        this.itemSeparator = itemSeparator;
        this.listPrefix = listPrefix;
        this.listSuffix = listSuffix;
    }

    /// Shorthand for adding a pair using [StringBuilder]
    ///
    /// @param isMore Are there more pairs that need to be added. If true it will add a separator
    public void pair( StringBuilder builder, String name, double value, boolean isMore ) {
        builder.append(keyPrefix).append(name).append(keySuffix);
        builder.append(valueSeparator);
        builder.append(f(value));
        if (isMore)
            builder.append(pairSeparator);
    }

    /// Shorthand for adding a pair as [String].
    ///
    /// @param isMore Are there more pairs that need to be added. If true it will add a separator
    public String pair( String name, double value, boolean isMore ) {
        String txt = keyPrefix + name + keySuffix + valueSeparator + f(value);
        return txt + (isMore ? pairSeparator : "");
    }

    public String pair( String name, String value, boolean isMore ) {
        String txt = keyPrefix + name + keySuffix + valueSeparator + value;
        return txt + (isMore ? pairSeparator : "");
    }

    public void pair( StringBuilder builder, String name, double[] values, boolean isMore ) {
        builder.append(keyPrefix).append(name).append(keySuffix);
        builder.append(valueSeparator);
        builder.append(itemPrefix);
        f(builder, pairSeparator, values);
        builder.append(itemSuffix);
        if (isMore)
            builder.append(pairSeparator);
    }

    public void pair( StringBuilder builder, String name, float[] values, boolean isMore ) {
        builder.append(keyPrefix).append(name).append(keySuffix);
        builder.append(valueSeparator);
        builder.append(itemPrefix);
        f(builder, pairSeparator, values);
        builder.append(itemSuffix);
        if (isMore)
            builder.append(pairSeparator);
    }

    /// Sets values relevant when printing a single item
    public MapPrintFormat setSingle( String valueSeparator, String pairSeparator,
                                     String itemPrefix, String itemSuffix ) {
        this.valueSeparator = valueSeparator;
        this.pairSeparator = pairSeparator;
        this.itemPrefix = itemPrefix;
        this.itemSuffix = itemSuffix;
        return this;
    }

    /// Creates an analogous matrix formatter. Used when you need to mix formats.
    public MatrixPrintFormat convertToMatrix() {
        var out = new MatrixPrintFormat();
        out.precision = precision;
        out.decimal = decimal;
        out.prefix = listPrefix;
        out.suffix = listSuffix;
        out.rowPrefix = itemPrefix;
        out.rowSuffix = itemSuffix;
        out.rowSeparator = itemSeparator;
        out.colSeparator = pairSeparator;
        out.aligned = false;
        return out;
    }

    public MapPrintFormat withPrecision( int precision ) {
        this.precision = precision;
        return this;
    }

    public MapPrintFormat withItemPrefix( String txt ) {
        this.itemPrefix = txt;
        return this;
    }

    public MapPrintFormat withItemSuffix( String txt ) {
        this.itemSuffix = txt;
        return this;
    }

    public MapPrintFormat withKeyPrefix( String txt ) {
        this.keyPrefix = txt;
        return this;
    }

    public MapPrintFormat withKeySuffix( String txt ) {
        this.keySuffix = txt;
        return this;
    }

    /// Convince function to implement to String with. Places the class name before the formated String.
    public String toString( MapFormattable o ) {
        return o.getClass().getSimpleName() + " " + o.formatMap();
    }

    public MapPrintFormat setTo( MapPrintFormat src ) {
        this.precision = src.precision;
        this.valueSeparator = src.valueSeparator;
        this.pairSeparator = src.pairSeparator;
        this.itemPrefix = src.itemPrefix;
        this.itemSuffix = src.itemSuffix;
        this.itemSeparator = src.itemSeparator;
        this.listPrefix = src.listPrefix;
        this.listSuffix = src.listSuffix;
        this.decimal = src.decimal;
        this.keyPrefix = src.keyPrefix;
        this.keySuffix = src.keySuffix;
        return this;
    }
}
