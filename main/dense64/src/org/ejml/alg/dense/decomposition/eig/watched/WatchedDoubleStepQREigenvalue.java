/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.eig.watched;

import org.ejml.alg.dense.decomposition.eig.EigenvalueExtractor;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;


/**
 * @author Peter Abeles
 */
public class WatchedDoubleStepQREigenvalue implements EigenvalueExtractor {

    WatchedDoubleStepQREigen implicitQR;

    int splits[];
    int numSplits;

    int x1;
    int x2;

    public WatchedDoubleStepQREigenvalue() {
        implicitQR = new WatchedDoubleStepQREigen();
    }

    public void setup( DenseMatrix64F A ) {
        implicitQR.setup(A);
        implicitQR.setQ(null);

        splits = new int[ A.numRows ];
        numSplits = 0;
    }

    @Override
    public boolean process(DenseMatrix64F origA) {
        setup(origA);

        x1 = 0;
        x2 = origA.numRows-1;

        while( implicitQR.numEigen < origA.numRows ) {
            if( implicitQR.steps > implicitQR.maxIterations )
                return false;

            implicitQR.incrementSteps();

            if( x2 < x1 ) {
                moveToNextSplit();
            } else if( x2-x1 == 0 ) {
//                implicitQR.A.print();
                implicitQR.addEigenAt(x1);
                x2--;
            } else if( x2-x1 == 1 ) {
//                implicitQR.A.print();
                implicitQR.addComputedEigen2x2(x1,x2);
                x2 -= 2;
            } else if( implicitQR.steps-implicitQR.lastExceptional > implicitQR.exceptionalThreshold ) {
                // see if the matrix blew up
                if( Double.isNaN(implicitQR.A.get(x2,x2))) {
                    return false;
                }

                implicitQR.exceptionalShift(x1,x2);
            } else if( implicitQR.isZero(x2,x2-1) ) {
//                implicitQR.A.print();
                implicitQR.addEigenAt(x2);
                x2--;
            }else {
                performIteration();
            }
        }

        return true;
    }

    private void moveToNextSplit() {
        if( numSplits <= 0 )
            throw new RuntimeException("bad");

        x2 = splits[--numSplits];

        if( numSplits > 0 ) {
            x1 = splits[numSplits-1]+1;
        } else {
            x1 = 0;
        }
    }

    private void performIteration() {
        boolean changed = false;

        // see if it can perform a split
        for( int i = x2; i > x1; i-- ) {
            if( implicitQR.isZero(i,i-1)) {
                x1 = i;
                splits[numSplits++] = i-1;
                changed = true;
                // reduce the scope of what it is looking at
                break;
            }
        }

        if( !changed )
            implicitQR.implicitDoubleStep(x1,x2);
    }

    @Override
    public int getNumberOfEigenvalues() {
        return implicitQR.getNumberOfEigenvalues();
    }

    @Override
    public Complex64F[] getEigenvalues() {
        return implicitQR.getEigenvalues();
    }

    public WatchedDoubleStepQREigen getImplicitQR() {
        return implicitQR;
    }
}