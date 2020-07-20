/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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
package org.ejml.bench.mult;

import org.ejml.bench.BaseBenchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;

import java.util.Arrays;

/**
 * @author Florentin Doerre
 */
public class BaseBenchmarkMatrixVectorMult extends BaseBenchmark {
    protected double[] inputVector;
    protected double[] output;

    @Override
    @Setup(Level.Invocation)
    public void setup() {
        super.setup();
        inputVector = new double[matrix.numRows];
        output = new double[matrix.numRows];
        // fast init and actual values are not relevant for the benchmark
        Arrays.fill(output, 22);
    }
}
