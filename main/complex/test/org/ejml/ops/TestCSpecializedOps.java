/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestCSpecializedOps {

    Random rand = new Random(234);

    @Test
    public void pivotMatrix() {
        int pivots[] = new int[]{1,0,3,2};

        CDenseMatrix64F A = CRandomMatrices.createRandom(4,4,-1,-1,rand);
        CDenseMatrix64F P = CSpecializedOps.pivotMatrix(null,pivots,4,false);
        CDenseMatrix64F Pt = CSpecializedOps.pivotMatrix(null,pivots,4,true);

        CDenseMatrix64F B = new CDenseMatrix64F(4,4);

        // see if it swapped the rows
        CCommonOps.mult(P, A, B);

        for( int i = 0; i < 4; i++ ) {
            int index = pivots[i];
            for( int j = 0; j < 4; j++ ) {
                double real = A.getReal(index,j);
                double img = A.getImaginary(index, j);

                assertEquals(real,B.getReal(i, j),1e-8);
                assertEquals(img,B.getImaginary(i, j),1e-8);
            }
        }

        // see if it transposed
        CCommonOps.transpose(P,B);

        assertTrue(CMatrixFeatures.isIdentical(B, Pt, 1e-8));
    }


    @Test
    public void elementDiagMaxMagnitude2() {
        CDenseMatrix64F A = CRandomMatrices.createRandom(4,5,-1,1,rand);

        Complex64F a = new Complex64F();

        double expected = 0;
        for (int i = 0; i < 4; i++) {
            A.get(i,i,a);
            if( a.getMagnitude2() > expected )
                expected = a.getMagnitude2();
        }

        double found = CSpecializedOps.elementDiagMaxMagnitude2(A);
        assertEquals(expected, found, 1e-8);
    }

    @Test
    public void qualityTriangular() {
        CDenseMatrix64F A = CRandomMatrices.createRandom(4,4,-1,-1,rand);

        double max = Math.sqrt(CSpecializedOps.elementDiagMaxMagnitude2(A));

        Complex64F a = new Complex64F();
        Complex64F tmp = new Complex64F();
        Complex64F total = new Complex64F(1,0);
        for (int i = 0; i < 4; i++) {
            A.get(i,i,a);
            a.real /= max;
            a.imaginary /= max;

            ComplexMath64F.multiply(total,a,tmp);
            total.set(tmp);
        }
        double expected = total.getMagnitude();

        double found = CSpecializedOps.qualityTriangular(A);
        assertEquals(expected,found,1e-8);
    }
}