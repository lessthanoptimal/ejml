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

package org.ejml.interfaces.mult;

/**
 * Interface for matrix matrix multiplication when one of the rows or columns has been unrolled. This
 * works for inner
 *
 * @author Peter Abeles
 */
public interface MatrixMultUnrolled {
    void mult( final double[] A , final int offsetA ,
               final double[] B , final int offsetB ,
               final double[] C , final int offsetC ,
               final int lengthA , final int lengthB );
}
