/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row;

import org.ejml.data.ZMatrixRMaj;

/**
 * @author Peter Abeles
 */
public class NormOps_ZDRM {
    /**
     * <p>
     * Computes the Frobenius matrix norm:<br>
     * <br>
     * normF = Sqrt{  &sum;<sub>i=1:m</sub> &sum;<sub>j=1:n</sub> { a<sub>ij</sub><sup>2</sup>}   }
     * </p>
     * <p>
     * This is equivalent to the element wise p=2 norm.
     * </p>
     *
     * @param a The matrix whose norm is computed.  Not modified.
     * @return The norm's value.
     */
    public static double normF( ZMatrixRMaj a ) {
        double total = 0;

        double scale = CommonOps_ZDRM.elementMaxAbs(a);

        if( scale == 0.0 )
            return 0.0;

        final int size = a.getDataLength();

        for( int i = 0; i < size; i += 2 ) {
            double real = a.data[i]/scale;
            double imag = a.data[i+1]/scale;

            total += real*real + imag*imag;
        }

        return scale* Math.sqrt(total);
    }
}
