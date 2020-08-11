/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.linsol.svd;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.SingularOps_FDRM;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.interfaces.SolveNullSpace;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F32;

/**
 * @author Peter Abeles
 */
public class SolveNullSpaceSvd_FDRM implements SolveNullSpace<FMatrixRMaj> {

    boolean compact = true;
    SingularValueDecomposition_F32<FMatrixRMaj> svd = DecompositionFactory_FDRM.svd(1,1,false,true,compact);
    FMatrixRMaj V;

    @Override
    public boolean process(FMatrixRMaj input, int numberOfSingular, FMatrixRMaj nullspace) {
        if( input.numCols > input.numRows ) {
            if( compact ) {
                svd = DecompositionFactory_FDRM.svd(1, 1, false, true, false);
                compact = false;
            }
        } else {
            if( !compact ) {
                svd = DecompositionFactory_FDRM.svd(1, 1, false, true, true);
                compact = true;
            }
        }

        if( !svd.decompose(input))
            return false;

        float []singularValues = svd.getSingularValues();
        V = svd.getV(V,false);

        SingularOps_FDRM.descendingOrder(null,false,singularValues,svd.numberOfSingularValues(),V,false);

        nullspace.reshape(V.numRows,numberOfSingular);
        CommonOps_FDRM.extract(V,0,V.numRows,V.numCols-numberOfSingular,V.numCols,nullspace,0,0);

        return true;
    }

    @Override
    public boolean inputModified() {
        return svd.inputModified();
    }

    public SingularValueDecomposition_F32<FMatrixRMaj> getSvd() {
        return svd;
    }

    public float[] getSingularValues() {
        return svd.getSingularValues();
    }
}
