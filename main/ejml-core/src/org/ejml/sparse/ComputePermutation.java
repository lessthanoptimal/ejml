/*
 * Copyright (c) 2009-2019, Peter Abeles. All Rights Reserved.
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

package org.ejml.sparse;

import org.ejml.data.IGrowArray;
import org.ejml.data.Matrix;

/**
 * @author Peter Abeles
 */
public abstract class ComputePermutation<T extends Matrix> {

    protected IGrowArray prow;
    protected IGrowArray pcol;

    public ComputePermutation( boolean hasRow , boolean hasCol ) {
        if( hasRow )
            prow = new IGrowArray();
        if( hasCol )
            pcol = new IGrowArray();
    }

    public abstract void process(T m );

    /**
     * Returns row permutation
     */
    public IGrowArray getRow() {
        return prow;
    }

    /**
     * Returns column permutation
     */
    public IGrowArray getColumn() {
        return pcol;
    }

    public boolean hasRowPermutation() {
        return prow != null;
    }

    public  boolean hasColumnPermutation() {
        return pcol != null;
    }
}
