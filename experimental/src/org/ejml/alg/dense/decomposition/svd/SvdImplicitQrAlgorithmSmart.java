/*
 * Copyright (c) 2009-2013, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.svd;

import org.ejml.alg.dense.decomposition.svd.implicitqr.SvdImplicitQrAlgorithm;
import org.ejml.data.DenseMatrix64F;


/**
 * @author Peter Abeles
 */
public class SvdImplicitQrAlgorithmSmart extends SvdImplicitQrAlgorithm {

    SmartRotatorUpdate smartU = new SmartRotatorUpdate();
    SmartRotatorUpdate smartV = new SmartRotatorUpdate();

    @Override
    public void setUt(DenseMatrix64F ut) {
        super.setUt(ut);
        if(Ut != null )
            smartU.init(Ut);
    }

    @Override
    public void setVt(DenseMatrix64F vt) {
        super.setVt(vt);
        if(Vt != null )
            smartV.init(Vt);
    }


    @Override
    protected void updateRotator( DenseMatrix64F Q , int m, int n, double c, double s) {
        if( Q == smartU.getR() ) {
            smartU.update(m,n,c,s);
        } else if( Q == smartV.getR() ) {
            smartV.update(m,n,c,s);
        } else {
            throw new RuntimeException("Unknown");
        }
    }
}
