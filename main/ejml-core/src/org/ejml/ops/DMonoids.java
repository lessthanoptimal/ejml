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

package org.ejml.ops;

/**
 * as defined in the graphblas c-api (https://people.eecs.berkeley.edu/~aydin/GraphBLAS_API_C_v13.pdf)
 * p. 26
 */
public final class DMonoids {
    public static final DMonoid AND = new DMonoid(1, (a, b) -> (a == 0 || b == 0) ? 0 : 1);
    public static final DMonoid OR = new DMonoid(0, (a, b) -> (a != 0 || b != 0) ? 1 : 0);
    public static final DMonoid XOR = new DMonoid(0, (a, b) -> ((a == 0 && b == 0) || (a != 0 && b != 0)) ? 0 : 1);
    public static final DMonoid XNOR = new DMonoid(0, (a, b) -> ((a == 0 && b == 0) || (a != 0 && b != 0)) ? 1 : 0);

    public static final DMonoid PLUS = new DMonoid(0, Double::sum);
    public static final DMonoid TIMES = new DMonoid(1, (a, b) -> a * b);

    public final static DMonoid MIN = new DMonoid(Double.MAX_VALUE, (a, b) -> (a <= b) ? a : b);
    public final static DMonoid MAX = new DMonoid(Double.MIN_VALUE, (a, b) -> (a >= b) ? a : b);
}
