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

import org.ejml.ops.DoubleSemiRing;
import org.ejml.ops.PreDefinedDoubleSemiRings;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashMap;

/**
 * @author Florentin Doerre
 */
public class BenchmarkMatrixMatrixMultWithSemiRing extends BaseBenchmarkMatrixMatrixMult {
    private static final String PLUS_TIMES = "Plus, Times";
    private static final String OR_AND = "Or, And";
    private static final String MIN_MAX = "Min, Max";

    HashMap<String, DoubleSemiRing> semiRings = new HashMap<>() {{
        put(PLUS_TIMES, PreDefinedDoubleSemiRings.PLUS_TIMES);
        put(OR_AND, PreDefinedDoubleSemiRings.OR_AND);
        put(MIN_MAX, PreDefinedDoubleSemiRings.MIN_MAX);
    }};

    @Param({PLUS_TIMES, OR_AND, MIN_MAX})
    private String semiRing;

    @Benchmark
    public void matrixMatrix(Blackhole bh) {
        CommonOpsWithSemiRing_DSCC.mult(matrix, otherMatrix, result, semiRings.get(semiRing));
        bh.consume(result);
    }
}
