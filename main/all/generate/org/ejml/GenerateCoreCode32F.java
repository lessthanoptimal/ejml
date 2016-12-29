/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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

import com.peterabeles.auto64fto32f.ConvertFile32From64;
import com.peterabeles.auto64fto32f.RecursiveConvert;

import java.io.File;

/**
 * Applications which will auto generate 32F code from 64F inside the core module
 * @author Peter Abeles
 */
public class GenerateCoreCode32F extends RecursiveConvert {


    public GenerateCoreCode32F(ConvertFile32From64 converter) {
        super(converter);
    }

    public static void main(String args[] ) {
        String directories[] = new String[]{
                "main/core/src/org/ejml/data",
//                "main/core/test/org/ejml/data", // TODO uncomment when full support is added for 32F
                "main/core/src/org/ejml/ops",
                "main/core/test/org/ejml/ops"
        };

        ConvertFile32From64 converter = new ConvertFile32From64(false);

        converter.replacePattern("/\\*\\*/double", "FIXED_DOUBLE");
        converter.replacePattern("double", "float");
        converter.replacePattern("Double", "Float");
        converter.replacePattern("64F", "32F");
        converter.replacePattern("64-bit", "32-bit");
        converter.replacePattern("UtilEjml.PI", "UtilEjml.F_PI");
        converter.replacePattern("UtilEjml.EPS", "UtilEjml.F_EPS");
        converter.replacePattern("UtilEjml.TEST_64F", "UtilEjml.TEST_32F");

        converter.replaceStartsWith("Math.sqrt", "(float)Math.sqrt");
        converter.replaceStartsWith("Math.pow", "(float)Math.pow");
        converter.replaceStartsWith("Math.sin", "(float)Math.sin");
        converter.replaceStartsWith("Math.cos", "(float)Math.cos");
        converter.replaceStartsWith("Math.tan", "(float)Math.tan");
        converter.replaceStartsWith("Math.atan", "(float)Math.atan");

        converter.replacePatternAfter("FIXED_DOUBLE", "/\\*\\*/double");


        GenerateCoreCode32F app = new GenerateCoreCode32F(converter);
        app.setSuffice("64F","32F");
        for( String dir : directories ) {
            app.process(new File(dir) );
        }
    }
}
