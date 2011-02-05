/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
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
