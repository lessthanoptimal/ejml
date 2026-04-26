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

import static org.ejml.UtilEjml.fancy2LengthExp;

/// Describes how a matrix is formatted when converted into a string. By default, it will
/// print a matrix into the standard Java format.
public class MatrixPrintFormat extends PrintFormat {
    /// Default valued used in toString and other location. Modifying this will modify the formatting in many locations
    /// Only the end user should be tweaking this and not any library and its subject to change.
    public final static MatrixPrintFormat DEFAULT = new MatrixPrintFormat();

    /// Number of significant digits it will display
    @Getter @Setter public String colSeparator = ", ";
    @Getter @Setter public String rowSeparator = ",\n";
    @Getter @Setter public String rowPrefix = "{";
    @Getter @Setter public String rowSuffix = "}";
    @Getter @Setter public String prefix = "[";
    @Getter @Setter public String suffix = "]";

    /// If true it will align the columns
    @Getter @Setter boolean aligned = true;

    public MatrixPrintFormat() {}

    public MatrixPrintFormat( int precision,
                              String colSeparator,
                              String rowSeparator,
                              String rowPrefix,
                              String rowSuffix,
                              String prefix,
                              String suffix ) {
        this.precision = precision;
        this.colSeparator = colSeparator;
        this.rowSeparator = rowSeparator;
        this.rowPrefix = rowPrefix;
        this.rowSuffix = rowSuffix;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    // Applies padding before a row. Needed for alignment
    public void rowPadding( boolean firstRow, StringBuilder builder ) {
        if (firstRow || !aligned)
            return;
        for (int j = 0; j < prefix.length(); j++) {
            builder.append(' ');
        }
    }

    /// Prints a formated row in a matrix where it will respect the request to align
    /// elements in the same column
    public void row( StringBuilder builder, int size, RowAccess access ) {
        // Maximum size a number can be, including negative symbol
        int numChars = aligned ? fancy2LengthExp(precision) + 1 : 0;
        builder.append(rowPrefix);
        for (int i = 0; i < size; i++) {
            double v = access.get(i);
            String word = f(v);
            builder.append(word);
            for (int j = word.length(); j < numChars; j++) {
                builder.append(' ');
            }
            if (i < size - 1)
                builder.append(colSeparator);
        }
        builder.append(rowSuffix);
    }

    public MatrixPrintFormat fsetPrecision( int precision ) {
        this.precision = precision;
        return this;
    }

    public MatrixPrintFormat fsetColSeparator( String colSeparator ) {
        this.colSeparator = colSeparator;
        return this;
    }

    public MatrixPrintFormat fsetRowSeparator( String rowSeparator ) {
        this.rowSeparator = rowSeparator;
        return this;
    }

    public MatrixPrintFormat fsetRowPrefix( String rowPrefix ) {
        this.rowPrefix = rowPrefix;
        return this;
    }

    public MatrixPrintFormat fsetRowSuffix( String rowSuffix ) {
        this.rowSuffix = rowSuffix;
        return this;
    }

    public MatrixPrintFormat fsetPrefix( String prefix ) {
        this.prefix = prefix;
        return this;
    }

    public MatrixPrintFormat fsetSuffix( String suffix ) {
        this.suffix = suffix;
        return this;
    }

    public MatrixPrintFormat setTo( MatrixPrintFormat src ) {
        this.precision = src.precision;
        this.colSeparator = src.colSeparator;
        this.rowSeparator = src.rowSeparator;
        this.rowPrefix = src.rowPrefix;
        this.rowSuffix = src.rowSuffix;
        this.prefix = src.prefix;
        this.suffix = src.suffix;
        this.decimal = src.decimal;
        return this;
    }

    @FunctionalInterface public interface RowAccess {
        double get( int i );
    }
}
