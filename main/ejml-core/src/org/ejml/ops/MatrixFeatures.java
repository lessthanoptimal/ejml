/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

/**
 * Determines which features a matrix has that do not rely on inner data type
 *
 * @author Peter Abeles
 */
public class MatrixFeatures {
    /**
     * Checks to see if the matrix is a vector or not.
     *
     * @param mat A matrix. Not modified.
     * @return True if it is a vector and false if it is not.
     */
    public static boolean isVector( Matrix mat ) {
        return (mat.getNumCols() == 1 || mat.getNumRows() == 1);
    }

    /**
     * Checks to see if it is a square matrix. A square matrix has the same number of rows and columns.
     *
     * @param mat A matrix. Not modified.
     * @return True if it is a square matrix and false if it is not.
     */
    public static boolean isSquare( Matrix mat ) {
        return mat.getNumCols() == mat.getNumRows();
    }
}
