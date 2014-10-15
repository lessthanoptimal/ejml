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

package org.ejml.alg.dense.mult;

import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.ejml.ops.CMatrixFeatures;
import org.ejml.ops.CRandomMatrices;
import org.ejml.ops.ComplexMath64F;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestCMatrixMatrixMult {

    Random rand = new Random(234);

    @Test
    public void mult_reorder() {
        for (int i = 1; i < 10; i++) {
            for (int j = 1; j < 10; j++) {
                CDenseMatrix64F A = CRandomMatrices.createRandom(i,j,-1,1,rand);
                for (int k = 1; k < 10; k++) {
                    CDenseMatrix64F B = CRandomMatrices.createRandom(j, k, -1, 1, rand);
                    CDenseMatrix64F found = CRandomMatrices.createRandom(i, k, -1, 1, rand);
                    CDenseMatrix64F expected = multiply(A, B);

                    CMatrixMatrixMult.mult_reorder(A, B, found);

                    assertTrue(i+" "+j+" "+k,CMatrixFeatures.isEquals(expected, found, 1e-8));
                }
            }
        }
    }

    @Test
    public void mult_small() {
        for (int i = 1; i < 10; i++) {
            for (int j = 1; j < 10; j++) {
                CDenseMatrix64F A = CRandomMatrices.createRandom(i,j,-1,1,rand);
                for (int k = 1; k < 10; k++) {
                    CDenseMatrix64F B = CRandomMatrices.createRandom(j, k, -1, 1, rand);
                    CDenseMatrix64F found = CRandomMatrices.createRandom(i, k, -1, 1, rand);
                    CDenseMatrix64F expected = multiply(A, B);

                    CMatrixMatrixMult.mult_small(A, B, found);

                    assertTrue(i+" "+j+" "+k,CMatrixFeatures.isEquals(expected, found, 1e-8));
                }
            }
        }
    }

    public static CDenseMatrix64F multiply( CDenseMatrix64F A , CDenseMatrix64F B ) {
        CDenseMatrix64F C = new CDenseMatrix64F(A.numRows,B.numCols);

        Complex64F a = new Complex64F();
        Complex64F b = new Complex64F();
        Complex64F m = new Complex64F();

        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < B.numCols; j++) {
                Complex64F sum = new Complex64F();

                for (int k = 0; k < A.numCols; k++) {
                    A.get(i,k,a);
                    B.get(k,j,b);

                    ComplexMath64F.multiply(a,b,m);
                    sum.real += m.real;
                    sum.imaginary += m.imaginary;
                }

                C.set(i,j,sum.real,sum.imaginary);
            }
        }

        return C;
    }
}