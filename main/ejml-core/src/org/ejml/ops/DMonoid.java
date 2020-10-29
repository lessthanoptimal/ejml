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

/**
 * An algebraic structure with a single associative binary operation and an identity element
 */
public class DMonoid {
    /**
     * neutral-element/identity for `func`
     */
    public final double id;
    public final DOperatorBinary func;

    public DMonoid( double id, DOperatorBinary func ) {
        this.id = id;
        this.func = func;
    }

    DMonoid( DOperatorBinary func ) {
        this(0, func);
    }
}
