/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.ops;

import org.ejml.data.Matrix;
import org.ejml.data.MatrixType;
import org.junit.jupiter.api.Test;

import static org.ejml.data.MatrixType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Peter Abeles
 */
public class TestConvertMatrixType {

    /**
     * Sees if it crashed when trying to convert
     */
    @Test
    public void basicCheckAll() {
        MatrixType types[] = new MatrixType[]{DDRM,FDRM,ZDRM,CDRM,DSCC,FSCC};

        for( MatrixType a : types ) {
            Matrix matA = a.create(4,6);

            for( MatrixType b : types ) {
                // can't convert complex to real
                if( !a.isReal() && b.isReal() )
                    continue;

                Matrix matB = ConvertMatrixType.convert(matA,b);

                if( matB == null ) {
                    System.out.println(a+" -> "+b);
                    fail("returned null");
                }

                assertEquals(matA.getNumRows(),matB.getNumRows());
                assertEquals(matA.getNumCols(),matB.getNumCols());
            }
        }
    }
}