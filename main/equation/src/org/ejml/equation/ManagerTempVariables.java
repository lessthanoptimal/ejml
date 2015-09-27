/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

/**
 * Manages the creation and recycling of temporary variables used to store intermediate results.  The user
 * cannot directly access these variables
 *
 * @author Peter Abeles
 */
// TODO add function to purge temporary variables.  basicaly resize and redeclare their array to size 1
public class ManagerTempVariables {

    public VariableMatrix createMatrix() {
        return VariableMatrix.createTemp();
    }

    public VariableDouble createDouble() {
        return new VariableDouble(0);
    }

    public VariableDouble createDouble( double value ) {
        return new VariableDouble(value);
    }

    public VariableInteger createInteger() {
        return createInteger(0);
    }

    public VariableInteger createInteger( int value ) {
        return new VariableInteger(value);
    }

    public VariableIntegerSequence createIntegerSequence( IntegerSequence sequence ) {
        return new VariableIntegerSequence(sequence);
    }
}
