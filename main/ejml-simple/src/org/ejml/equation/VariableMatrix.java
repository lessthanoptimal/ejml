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

package org.ejml.equation;

import org.ejml.data.DMatrixRMaj;

/**
 * Storage for {@link DMatrixRMaj matrix} type variables.
 *
 * @author Peter Abeles
 */
public class VariableMatrix extends Variable {
    public DMatrixRMaj matrix;

    /**
     * If true then the matrix is dynamically resized to match the output of a function
     */
    public boolean temp;

    /**
     * Initializes the matrix variable.  If null then the variable will be a reference one.  If not null then
     * it will be assignment.
     * @param matrix Matrix.
     */
    public VariableMatrix(DMatrixRMaj matrix) {
        super(VariableType.MATRIX);
        this.matrix = matrix;
    }

    public static VariableMatrix createTemp() {
        VariableMatrix ret = new VariableMatrix(new DMatrixRMaj(1,1));
        ret.setTemp(true);
        return ret;
    }

    public boolean isTemp() {
        return temp;
    }

    public void setTemp(boolean temp) {
        this.temp = temp;
    }
}
