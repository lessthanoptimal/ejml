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

/// Describes a matrix is formatted when converted into a string. By default, it will
/// print a matrix into the standard Java format.
public class MatrixPrintFormat {
    /// Number of significant digits it will display
    @Getter @Setter int precision = 6;
    @Getter @Setter String colSeperator = ", ";
    @Getter @Setter String rowSeperator = ",\n";
    @Getter @Setter String rowPrefix = "{";
    @Getter @Setter String rowSuffix = "}";
    @Getter @Setter String prefix = "{";
    @Getter @Setter String suffix = "}";

    public MatrixPrintFormat() {}

    public MatrixPrintFormat( int precision,
                              String colSeperator,
                              String rowSeperator,
                              String rowPrefix,
                              String rowSuffix,
                              String prefix,
                              String suffix ) {
        this.precision = precision;
        this.colSeperator = colSeperator;
        this.rowSeperator = rowSeperator;
        this.rowPrefix = rowPrefix;
        this.rowSuffix = rowSuffix;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public MatrixPrintFormat fsetPrecision( int precision ) {
        this.precision = precision;
        return this;
    }

    public MatrixPrintFormat fsetColSeperator( String colSeperator) {
        this.colSeperator = colSeperator;
        return this;
    }

    public MatrixPrintFormat fsetRowSeperator( String rowSeperator) {
        this.rowSeperator = rowSeperator;
        return this;
    }

    public MatrixPrintFormat fsetRowPrefix( String rowPrefix) {
        this.rowPrefix = rowPrefix;
        return this;
    }

    public MatrixPrintFormat fsetRowSuffix( String rowSuffix) {
        this.rowSuffix = rowSuffix;
        return this;
    }

    public MatrixPrintFormat fsetPrefix( String prefix) {
        this.prefix = prefix;
        return this;
    }

    public MatrixPrintFormat fsetSuffix( String suffix) {
        this.suffix = suffix;
        return this;
    }
}
