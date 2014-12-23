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

package org.ejml.factory;


/**
 * This exception is thrown if an operation can not be finished because the matrix is singular.
 * It is a RuntimeException to allow the code to be written cleaner and also because singular
 * matrices are not always detected.  Forcing an exception to be caught provides a false sense
 * of security.
 *
 * @author Peter Abeles
 */
public class SingularMatrixException extends RuntimeException {

    public SingularMatrixException() {
    }

    public SingularMatrixException(String message) {
        super(message);
    }
}
