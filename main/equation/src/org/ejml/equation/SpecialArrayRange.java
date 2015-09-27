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
 * Specifies elements inside an array.  Can refer to the beginning and end of the array
 *
 * @author Peter Abeles
 */
public class SpecialArrayRange {

    VariableInteger start;
    VariableInteger step;

    public SpecialArrayRange(TokenList.Token start, TokenList.Token step) {
        this.start = start == null ? null : (VariableInteger)start.getVariable();
        this.step = step == null ? null : (VariableInteger)step.getVariable();
    }

    public boolean isAll() {
        return start == null;
    }

    public int getStart() {
        return start.value;
    }

    public int getStep() {
        if( step == null )
            return 1;
        else
            return step.value;
    }
}
