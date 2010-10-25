/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.block.decomposition.qr;

import org.ejml.data.D1Submatrix64F;

/**
 *
 * <p>
 * Contains various helper functions for performing a block matrix QR decomposition.
 * </p>
 *
 * <p>
 * Compute W and Y:<br>
 * <br>
 * Y = v<sup>(1)</sup><br>
 * W = -&beta;<sub>1</sub>v<sup>(1)</sup><br>
 * for j=2:r<br>
 * &nbsp;&nbsp;z = -&beta;(I +WY<sup>T</sup>)v<sup>(j)</sup> <br>
 * &nbsp;&nbsp;W = [W z]<br>
 * &nbsp;&nbsp;Y = [Y v<sup>(j)</sup>]<br>
 * end<br>
 * <br>
 * where v<sup>(.)</sup> are the house holder vectors, and r is the block length.
 * </p>
 *
 * @author Peter Abeles
 */
public class BlockHouseHolder {

    /**
     * A is a submatrix whose columns contain the householder vectors.
     *
     * @param A Input matrix containing householder vectors.  Not modified.
     * @param W Resulting W matrix. Modified.
     */
    public static void computeW_Column( D1Submatrix64F A , D1Submatrix64F W ) {

    }
}
