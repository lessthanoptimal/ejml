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

package org.ejml.dense.row.decomposition.qr;

import org.ejml.concurrency.EjmlConcurrency;


/**
 * <p>
 * Concurrent extension of {@link QRDecompositionHouseholderColumn_DDRM}.
 * </p>
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class QRDecompositionHouseholderColumn_MT_DDRM extends QRDecompositionHouseholderColumn_DDRM
{

    // NOTE: Concurrent getQ()
    // Making the rank1Update concurrent did not speed up the decomposition and used 4 cores instead of 1
    // Can't make the outlier most loop concurrent since it modifies the entire Q

    @Override
    protected void updateA( int w )
    {
        final double[] u = dataQR[w];

        EjmlConcurrency.loopFor(w+1,numCols,j->{
            final double[] colQ = dataQR[j];
            double val = colQ[w];

            for( int k = w+1; k < numRows; k++ ) {
                val += u[k]*colQ[k];
            }
            val *= gamma;

            colQ[w] -= val;
            for( int i = w+1; i < numRows; i++ ) {
                colQ[i] -= u[i]*val;
            }
        });
    }
}