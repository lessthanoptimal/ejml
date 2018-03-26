/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

import java.util.ArrayList;
import java.util.List;

/**
 * Contains a sequence of operations.  This is the final result of compiling the equation.  Once created it can
 * be invoked an arbitrary number of times by invoking {@link #perform()}.
 *
 * @author Peter Abeles
 */
public class Sequence {
    // List of in sequence operations which the equation string described
    List<Operation> operations = new ArrayList<>();

    // Variable containing the output of the sequence
    Variable output;

    public void addOperation( Operation operation ) {
        operations.add(operation);
    }

    /**
     * Executes the sequence of operations
     */
    public void perform() {
        for (int i = 0; i < operations.size(); i++) {
            operations.get(i).process();
        }
    }
}
