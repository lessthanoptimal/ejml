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

public final class PreDefinedDoubleMonoids {
    public static final DoubleMonoid AND = new DoubleMonoid(1, (a, b) -> (a == 0 || b == 0) ? 0 : 1);
    public static final DoubleMonoid OR = new DoubleMonoid(0, (a, b) -> (a != 0 || b != 0) ? 1 : 0);
    public static final DoubleMonoid XOR = new DoubleMonoid(0, (a, b) -> ((a == 0 && b == 0) || (a != 0 && b != 0)) ? 0 : 1);
    public static final DoubleMonoid XNOR = new DoubleMonoid(0, (a, b) -> ((a == 0 && b == 0) || (a != 0 && b != 0)) ? 1 : 0);

    public static final DoubleMonoid PLUS = new DoubleMonoid(0, Double::sum);
    public static final DoubleMonoid MULT = new DoubleMonoid(1, (a, b) -> a * b);

    // TODO: performance incr. worth not using safe Math.min/max?
    public final static DoubleMonoid MIN = new DoubleMonoid(Double.MAX_VALUE, (a, b) -> (a <= b) ? a : b);
    public final static DoubleMonoid MAX = new DoubleMonoid(Double.MIN_VALUE, (a, b) -> (a >= b) ? a : b);
}