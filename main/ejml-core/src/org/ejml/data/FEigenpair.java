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

package org.ejml.data;


/**
 * An eigenpair is a set composed of an eigenvalue and an eigenvector.  In this library since only real
 * matrices are supported, all eigenpairs are real valued.
 *
 * @author Peter Abeles
 */
public class FEigenpair {
    public float value;
    public FMatrixRMaj vector;

    public FEigenpair(float value, FMatrixRMaj vector) {
        this.value = value;
        this.vector = vector;
    }
}
