/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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

import static org.ejml.ops.DMonoids.*;

/**
 * as defined in the graphblas c-api (https://people.eecs.berkeley.edu/~aydin/GraphBLAS_API_C_v13.pdf)
 * p. 27-28
 * Note: some don’t have a multiplicative annihilator (thus no "true" semi-rings")
 */
public final class DSemiRings {
    public static final DSemiRing PLUS_TIMES = new DSemiRing(PLUS, TIMES);
    public static final DSemiRing MIN_PLUS = new DSemiRing(MIN, PLUS);
    public static final DSemiRing MAX_PLUS = new DSemiRing(MAX, PLUS);
    public static final DSemiRing MIN_TIMES = new DSemiRing(MIN, TIMES);
    public static final DSemiRing MIN_MAX = new DSemiRing(MIN, MAX);
    public static final DSemiRing MAX_MIN = new DSemiRing(MAX, MIN);
    public static final DSemiRing MAX_TIMES = new DSemiRing(MAX, TIMES);
    public static final DSemiRing PLUS_MIN = new DSemiRing(PLUS, MIN);

    public static final DSemiRing OR_AND = new DSemiRing(OR, AND);
    public static final DSemiRing AND_OR = new DSemiRing(AND, OR);
    public static final DSemiRing XOR_AND = new DSemiRing(XOR, AND);
    public static final DSemiRing XNOR_OR = new DSemiRing(XNOR, OR);

    // only private as they have no identity element, hence can only be used for add
    private static final DMonoid FIRST = new DMonoid(Double.NaN, ( x, y ) -> x);
    private static final DMonoid SECOND = new DMonoid(Double.NaN, ( x, y ) -> y);

    // semi-rings with no multiplicative annihilator
    public static final DSemiRing MIN_FIRST = new DSemiRing(MIN, FIRST);
    public static final DSemiRing MIN_SECOND = new DSemiRing(MIN, SECOND);
    public static final DSemiRing MAX_FIRST = new DSemiRing(MAX, FIRST);
    public static final DSemiRing MAX_SECOND = new DSemiRing(MAX, SECOND);
}
