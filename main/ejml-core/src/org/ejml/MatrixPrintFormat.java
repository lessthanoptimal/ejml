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

/// Describes how a matrix is formatted when converted into a string.
///
/// Controls the precision, column separator, row separator, row prefix/suffix,
/// matrix prefix/suffix, and column alignment. Predefined formats are provided
/// for common targets ({@link #DEFAULT}, {@link #JAVA}, {@link #CPP}, {@link #PYTHON});
/// custom formats can be built via the constructor or fluent setters.
///
/// Default formatting:
/// ```
/// [{1            , 0.0000013456 , 3            },
///  {3.2          , 4.45983445   , 1.2345e+15   }]
/// ```
public class MatrixPrintFormat extends PrintFormat {
    /// Default formatting. Modifying this class will change formatting for the entire application, including relevant
    /// toString().
    ///
    /// ```
    /// [{1            , 0.0000013456 , 3            },
    ///  {3.2          , 4.45983445   , 1.2345e+15   }]
    /// ```
    public final static MatrixPrintFormat DEFAULT = new MatrixPrintFormat();

    /// Default for compact single line. Modifying this class will change formatting for the entire application.
    ///
    /// ```
    /// [{1, 0.000001, 3},{3.2, 4.459834, 1.2345e+15}]
    /// ```
    public final static MatrixPrintFormat LINE = new MatrixPrintFormat().withAligned(false).withRowSeparator(",");

    /// Formatting for Java style double[][]. Modifying this class will change formatting for the entire application.
    /// ```
    /// {{1            , 0.0000013456 , 3            },
    ///  {3.2          , 4.45983445   , 1.2345e+15   }}
    /// ```
    public final static MatrixPrintFormat JAVA = new MatrixPrintFormat(DEFAULT_PRECISION, ", ", ",\n", "{", "}", "{", "}");

    /// Formatting for CPP double array. Modifying this class will change formatting for the entire application.
    ///
    /// ```
    /// {{1            , 0.0000013456 , 3            },
    ///  {3.2          , 4.45983445   , 1.2345e+15   }}
    /// ```
    public final static MatrixPrintFormat CPP = JAVA;
    /// Python Numpy style array. Modifying this class will change formatting for the entire application.
    /// ```
    /// [[1,0.0000013456,3], [3.2,4.45983445,1.2345e+15]]
    /// ```
    public final static MatrixPrintFormat PYTHON = new MatrixPrintFormat(DEFAULT_PRECISION, ",", ", ", "[", "]", "[", "]").withAligned(false);
    /// Matlab style matrix:
    /// ```
    /// [1            0.0000013456 3            ;
    ///  3.2          4.45983445   1.2345e+15   ]
    /// ```
    public final static MatrixPrintFormat MATLAB = new MatrixPrintFormat(DEFAULT_PRECISION, "  ", ";\n", "", "", "[", "]");

    @Getter @Setter public String colSeparator = ", ";
    @Getter @Setter public String rowSeparator = ",\n";
    @Getter @Setter public String rowPrefix = "{";
    @Getter @Setter public String rowSuffix = "}";
    @Getter @Setter public String prefix = "[";
    @Getter @Setter public String suffix = "]";

    /// If true it will align the columns
    @Getter @Setter public boolean aligned = true;

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

    /// Applies padding before a row. Needed for alignment
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
            String word = aligned ? f(v, numChars) : f(v);
            builder.append(word);
            for (int j = word.length(); j < numChars; j++) {
                builder.append(' ');
            }
            if (i < size - 1)
                builder.append(colSeparator);
        }
        builder.append(rowSuffix);
    }

    public MatrixPrintFormat withPrecision( int precision ) {
        this.precision = precision;
        return this;
    }

    public MatrixPrintFormat withColSeparator( String colSeparator ) {
        this.colSeparator = colSeparator;
        return this;
    }

    public MatrixPrintFormat withRowSeparator( String rowSeparator ) {
        this.rowSeparator = rowSeparator;
        return this;
    }

    public MatrixPrintFormat withRowPrefix( String rowPrefix ) {
        this.rowPrefix = rowPrefix;
        return this;
    }

    public MatrixPrintFormat withRowSuffix( String rowSuffix ) {
        this.rowSuffix = rowSuffix;
        return this;
    }

    public MatrixPrintFormat withPrefix( String prefix ) {
        this.prefix = prefix;
        return this;
    }

    public MatrixPrintFormat withSuffix( String suffix ) {
        this.suffix = suffix;
        return this;
    }

    public MatrixPrintFormat withAligned( boolean aligned ) {
        this.aligned = aligned;
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

    /// Convince function to implement to String with. Places the class name before the formated String.
    public String toString( MatrixFormattable o ) {
        return o.getClass().getSimpleName()+" "+o.format();
    }

    @FunctionalInterface public interface RowAccess {
        double get( int i );
    }
}
