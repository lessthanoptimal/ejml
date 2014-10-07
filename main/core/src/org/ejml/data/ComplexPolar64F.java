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

package org.ejml.data;

import org.ejml.ops.ComplexMath64F;

/**
 * <p>
 * {@link Complex64F} number in polar notation.<br>
 * z = r*(cos(&theta;) + i*sin(&theta;))<br>
 * where r and &theta; are polar coordinate parameters
 * </p>
 * @author Peter Abeles
 */
public class ComplexPolar64F {
    public double r;
	public double theta;

	public ComplexPolar64F(double r, double theta) {
		this.r = r;
		this.theta = theta;
	}

	public ComplexPolar64F( Complex64F n ) {
		ComplexMath64F.convert(n, this);
	}

	public ComplexPolar64F() {
	}

	public Complex64F toStandard() {
		Complex64F ret = new Complex64F();
		ComplexMath64F.convert(this, ret);
		return ret;
	}

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    public double getTheta() {
        return theta;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public String toString() {
		return "( r = "+r+" theta = "+theta+" )";
	}
}
