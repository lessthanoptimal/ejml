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

/// Interface for objects which can be converted into a string formatted matrix
public interface MatrixFormattable {
    /// Customizable formatting for converting a Matrix into a string that uses {@link MatrixPrintFormat#DEFAULT}
    default String format() {return format(MatrixPrintFormat.DEFAULT);}

    /// Customizable formatting for converting a Matrix into a string
    String format( MatrixPrintFormat format );
}
