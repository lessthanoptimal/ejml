/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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
package org.ejml.interfaces.decomposition;

import org.ejml.data.Matrix;

/**
 * <p>
 * Implementation of {@link QRPDecomposition} for 32-bit floats
 * </p>
 *
 * @author Peter Abeles
 */
public interface QRPDecomposition_F32<T extends Matrix>
        extends QRPDecomposition<T>
{
    /**
     * <p>
     * Specifies the threshold used to flag a column as being singular. The specified threshold is relative
     * and will very depending on the system. The default value is UtilEJML.F_EPS.
     * </p>
     *
     * @param threshold Singular threshold.
     */
    void setSingularThreshold(float threshold);
}
