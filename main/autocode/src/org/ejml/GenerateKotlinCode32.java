/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

import com.peterabeles.autocode.ConvertFile32From64;
import com.peterabeles.autocode.Language;

import java.io.File;

/**
 * Applications which will auto generate 32F code from 64F inside the core module
 * @author Peter Abeles
 */
public class GenerateKotlinCode32 extends GenerateCode32{

    public GenerateKotlinCode32() {
        super("kt",new ConvertFile32From64(Language.KOTLIN,false));

        String[] sufficeRoot = new String[]{"DRM","DMA","DRB","SCC","STL","DF2","DF3","DF4","DF5","DF6","TRIPLET"};

        suffices64.add("_DDRB_to_DDRM");
        suffices64.add("_F64");
        suffices32.add("_FDRB_to_FDRM");
        suffices32.add("_F32");

        for( String suffice : sufficeRoot ) {
            suffices64.add("_D"+suffice);
            suffices32.add("_F"+suffice);
            suffices64.add("_Z"+suffice);
            suffices32.add("_C"+suffice);
        }

        suffices64.add("Features_D");
        suffices32.add("Features_F");

        prefix64.add("DGrow");
        prefix32.add("FGrow");
        prefix64.add("DScalar");
        prefix32.add("FScalar");
        prefix64.add("DMatrix");
        prefix32.add("FMatrix");
        prefix64.add("ZMatrix");
        prefix32.add("CMatrix");
        prefix64.add("DEigen");
        prefix32.add("FEigen");
        prefix64.add("DSubmatrix");
        prefix32.add("FSubmatrix");
        prefix64.add("DConvert");
        prefix32.add("FConvert");
        prefix64.add("GenericTestsDMatrix");
        prefix32.add("GenericTestsFMatrix");

        int N = prefix64.size();
        for (int i = 0; i < N; i++) {
            prefix64.add("Test"+prefix64.get(i));
            prefix32.add("Test"+prefix32.get(i));
        }

        converter.replacePattern("DoubleStep", "FIXED_STEP");
        converter.replacePattern("Double", "Float");

        for( String suffice : sufficeRoot) {
            converter.replacePattern("_D"+suffice, "_F"+suffice);
            converter.replacePattern("_Z"+suffice, "_C"+suffice);
            converter.replacePattern("MatrixType.D"+suffice, "MatrixType.F"+suffice);
            converter.replacePattern("MatrixType.Z"+suffice, "MatrixType.C"+suffice);
            converter.replacePattern(".getD"+suffice, ".getF"+suffice);
            converter.replacePattern(".getZ"+suffice, ".getC"+suffice);
        }

        converter.replacePattern("DScalar", "FScalar");
        converter.replacePattern("DConvert", "FConvert");
        converter.replacePattern("DGrowArray", "FGrowArray");
        converter.replacePattern("DMatrix", "FMatrix");
        converter.replacePattern("DSubmatrix", "FSubmatrix");
        converter.replacePattern("DEigen", "FEigen");
        converter.replacePattern("ZComplex", "CComplex");
        converter.replacePattern("ZMatrix", "CMatrix");
        converter.replacePattern("ZSubmatrix", "CSubmatrix");
        converter.replacePattern("Features_D;", "Features_F;");
        converter.replacePattern("Features_D.", "Features_F.");
        converter.replacePattern("DDRM", "FDRM");

        converter.replacePattern("F64", "F32");
        converter.replacePattern("random64", "random32");
        converter.replacePattern("64-bit", "32-bit");
        converter.replacePattern("UtilEjml.PI", "UtilEjml.F_PI");
        converter.replacePattern("UtilEjml.EPS", "UtilEjml.F_EPS");

        converter.replacePatternAfter("FIXED_STEP", "DoubleStep");
    }

    public static void main(String args[] ) {
        String path = findPathToProjectRoot();
        System.out.println("Path to project root: "+path);

        String[] coreDir = new String[]{
                "main/ejml-kotlin/src"
        };

        GenerateKotlinCode32 app = new GenerateKotlinCode32();
        for( String dir : coreDir ) {
            app.process(new File(path,dir) );
        }
    }
}
