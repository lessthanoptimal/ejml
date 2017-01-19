/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

import org.ejml.ops.ComplexMathC;

/**
 * <p>
 * {@link CComplex} number in polar notation.<br>
 * z = r*(cos(&theta;) + i*sin(&theta;))<br>
 * where r and &theta; are polar coordinate parameters
 * </p>
 * @author Peter Abeles
 */
public class CComplexPolar {
    public float r;
	public float theta;

	public CComplexPolar(float r, float theta) {
		this.r = r;
		this.theta = theta;
	}

	public CComplexPolar(CComplex n ) {
		ComplexMathC.convert(n, this);
	}

	public CComplexPolar() {
	}

	public CComplex toStandard() {
		CComplex ret = new CComplex();
		ComplexMathC.convert(this, ret);
		return ret;
	}

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }

    public float getTheta() {
        return theta;
    }

    public void setTheta(float theta) {
        this.theta = theta;
    }

    public String toString() {
		return "( r = "+r+" theta = "+theta+" )";
	}
}
