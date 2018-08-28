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

package org.ejml.dense.fixed;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixFixed;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.ejml.ops.ConvertDMatrixStruct;
import org.ejml.ops.MatrixFeatures_D;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public abstract class CompareFixedToCommonOps_DDRM extends CompareFixed_DDRM {

    public CompareFixedToCommonOps_DDRM(Class classFixed) {
        super(classFixed, CommonOps_DDRM.class);
    }

    /**
     * Compares equivalent functions in FixedOps to CommonOps.  Inputs are randomly generated
     */
    @Test
    public void compareToCommonOps() {
        int numExpected = 63;
        if( N > UtilEjml.maxInverseSize ) {
            numExpected -= 2;
        }
        compareToCommonOps(numExpected,2);
    }

    @Test
    public void multAddOuter() {
        Method[] methods = classFixed.getMethods();
        Method match = null;
        for (int i = 0; i < methods.length; i++) {
            if( methods[i].getName().equals("multAddOuter")) {
                if( match == null ) {
                    match = methods[i];
                } else {
                    throw new RuntimeException("Multiple Matches");
                }
            }
        }

        double alpha = 1.5;
        double beta = -0.7;
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(N, N, rand);
        DMatrixRMaj u = RandomMatrices_DDRM.rectangle(N, 1, rand);
        DMatrixRMaj v = RandomMatrices_DDRM.rectangle(N, 1, rand);
        DMatrixRMaj C = RandomMatrices_DDRM.rectangle(N, N, rand);

        DMatrixFixed _C;
        try {
            Class<?> parameters[] = match.getParameterTypes();
            DMatrixFixed _A = (DMatrixFixed)parameters[1].newInstance();
            DMatrixFixed _u = (DMatrixFixed)parameters[3].newInstance();
            DMatrixFixed _v = (DMatrixFixed)parameters[4].newInstance();
            _C = (DMatrixFixed)parameters[5].newInstance();

            ConvertDMatrixStruct.convert(A,_A);
            ConvertDMatrixStruct.convert(u,_u);
            ConvertDMatrixStruct.convert(v,_v);
            ConvertDMatrixStruct.convert(C,_C);

            Object[] inputsFixed = new Object[6];
            inputsFixed[0] = alpha;
            inputsFixed[1] = _A;
            inputsFixed[2] = beta;
            inputsFixed[3] = _u;
            inputsFixed[4] = _v;
            inputsFixed[5] = _C;

            match.invoke(null,inputsFixed);
        } catch( Exception e ) {
            throw new RuntimeException(e);
        }

        CommonOps_DDRM.scale(alpha,A,C);
        VectorVectorMult_DDRM.addOuterProd(beta,u,v,C);

        assertTrue(MatrixFeatures_D.isIdentical(C,_C, UtilEjml.TEST_F64));
    }
}
