/*
 * Copyright (c) 2009-2019, Peter Abeles. All Rights Reserved.
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

import org.ejml.ops.MatrixIO;

import java.text.DecimalFormat;

/**
 * Convenience class for fancy print designed to make it less verbose
 *
 * @author Peter Abeles
 */
public class FancyPrint {
    DecimalFormat format = new DecimalFormat("#");
    int length= MatrixIO.DEFAULT_LENGTH;
    int significant=4;

    public FancyPrint(DecimalFormat format, int length, int significant) {
        this.format = format;
        this.length = length;
        this.significant = significant;
    }

    public FancyPrint() {
    }

    /**
     * @see UtilEjml#fancyStringF(double, DecimalFormat, int, int)
     */
    public String sf(double value ) {
        return UtilEjml.fancyStringF(value,format,length,significant);
    }

    /**
     * @see UtilEjml#fancyString(double, DecimalFormat, int, int)
     */
    public String s(double value ) {
        return UtilEjml.fancyString(value,format,length,significant);
    }

    /**
     * Fancy print without a space added to positive numbers
     */
    public String p(double value ) {
        return UtilEjml.fancyString(value,format,false,length,significant);
    }

}
