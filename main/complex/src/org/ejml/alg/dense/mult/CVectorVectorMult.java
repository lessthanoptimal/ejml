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

/**
 * Operations that involve multiplication of two vectors.
 *
 * @author Peter Abeles
 */
public class CVectorVectorMult {
    /**
     * <p>
     * Computes the inner product of the two vectors.  In geometry this is known as the dot product.<br>
     * <br>
     * &sum;<sub>k=1:n</sub> x<sub>k</sub> * y<sub>k</sub><br>
     * where x and y are vectors with n elements.
     * </p>
     *
     * <p>
     * These functions are often used inside of highly optimized code and therefor sanity checks are
     * kept to a minimum.  It is not recommended that any of these functions be used directly.
     * </p>
     *
     * @param x A vector with n elements. Not modified.
     * @param y A vector with n elements. Not modified.
     * @return The inner product of the two vectors.
     */
    public static Complex64F innerProd( CDenseMatrix64F x, CDenseMatrix64F y , Complex64F output )
    {
        if( output == null )
            output = new Complex64F();
        else {
            output.real = output.imaginary = 0;
        }

        int m = x.getDataLength();

        for( int i = 0; i < m; i += 2 ) {
            double realX = x.data[i];
            double imagX = x.data[i+1];

            double realY = y.data[i];
            double imagY = y.data[i+1];

            output.real += realX*realY - imagX*imagY;
            output.imaginary += realX*imagY + imagX*realY;
        }

        return output;
    }
}
