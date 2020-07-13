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

import static org.ejml.ops.PreDefinedDoubleMonoids.*;

/**
 * as defined in the graphblas c-api (https://people.eecs.berkeley.edu/~aydin/GraphBLAS_API_C_v13.pdf)
 * p. 27-28
 * Note: some don’t have a multiplicative annihilator (thus no "true" semi-rings")
 */
public final class PreDefinedDoubleSemiRings {
    public static final DoubleSemiRing PLUS_TIMES = new DoubleSemiRing(PLUS, TIMES);
    public static final DoubleSemiRing MIN_PLUS = new DoubleSemiRing(MIN, PLUS);
    public static final DoubleSemiRing MAX_PLUS = new DoubleSemiRing(MAX, PLUS);
    public static final DoubleSemiRing MIN_TIMES = new DoubleSemiRing(MIN, TIMES);
    public static final DoubleSemiRing MIN_MAX = new DoubleSemiRing(MIN, MAX);
    public static final DoubleSemiRing MAX_MIN = new DoubleSemiRing(MAX, MIN);
    public static final DoubleSemiRing MAX_TIMES = new DoubleSemiRing(MAX, TIMES);
    public static final DoubleSemiRing PLUS_MIN = new DoubleSemiRing(PLUS, MIN);

    public static final DoubleSemiRing OR_AND = new DoubleSemiRing(OR, AND);
    public static final DoubleSemiRing AND_OR = new DoubleSemiRing(AND, OR);
    public static final DoubleSemiRing XOR_AND = new DoubleSemiRing(XOR, AND);
    public static final DoubleSemiRing XNOR_OR = new DoubleSemiRing(XNOR, OR);
}