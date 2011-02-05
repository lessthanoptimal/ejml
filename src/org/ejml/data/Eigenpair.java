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

package org.ejml.data;


/**
 * An eigenpair is a set composed of an eigenvalue and an eigenvector.  In this library since only real
 * matrices are supported, all eigenpairs are real valued.
 *
 * @author Peter Abeles
 */
public class Eigenpair {
    public double value;
    public DenseMatrix64F vector;

    public Eigenpair( double value , DenseMatrix64F vector ) {
        this.value = value;
        this.vector = vector;
    }
}
