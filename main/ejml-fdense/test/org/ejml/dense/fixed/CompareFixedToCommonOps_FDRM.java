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

package org.ejml.dense.fixed;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixFixed;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.dense.row.mult.VectorVectorMult_FDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F32;
import org.ejml.ops.ConvertFMatrixStruct;
import org.ejml.ops.MatrixFeatures_F;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Peter Abeles
 */
public abstract class CompareFixedToCommonOps_FDRM extends CompareFixed_FDRM {

    public CompareFixedToCommonOps_FDRM(Class classFixed) {
        super(classFixed, CommonOps_FDRM.class);
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
        compareToCommonOps(numExpected,4);
    }

    @Test
    public void cholesky_lower() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        CholeskyDecomposition_F32<FMatrixRMaj> chol = DecompositionFactory_FDRM.chol(true);
        FMatrixRMaj A = RandomMatrices_FDRM.symmetricPosDef(N, rand);

        assertTrue(chol.decompose(A.copy()));
        FMatrixRMaj expected = chol.getT(null);

        Method[] methods = classFixed.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if( !methods[i].getName().equals("cholL"))
                continue;
            Class<?>[] parameters = methods[i].getParameterTypes();
            FMatrixFixed _A = (FMatrixFixed)parameters[0].newInstance();

            ConvertFMatrixStruct.convert(A,_A);
            Object[] inputsFixed = new Object[1];
            inputsFixed[0] = _A;

            methods[i].invoke(null,inputsFixed);
            ConvertFMatrixStruct.convert(_A,A);
            assertTrue(MatrixFeatures_FDRM.isIdentical(expected,A, UtilEjml.TEST_F32 ));
            return;
        }
        fail("No match found");
    }

    @Test
    public void cholesky_upper() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        CholeskyDecomposition_F32<FMatrixRMaj> chol = DecompositionFactory_FDRM.chol(false);
        FMatrixRMaj A = RandomMatrices_FDRM.symmetricPosDef(N, rand);

        assertTrue(chol.decompose(A.copy()));
        FMatrixRMaj expected = chol.getT(null);

        Method[] methods = classFixed.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if( !methods[i].getName().equals("cholU"))
                continue;
            Class<?>[] parameters = methods[i].getParameterTypes();
            FMatrixFixed _A = (FMatrixFixed)parameters[0].newInstance();

            ConvertFMatrixStruct.convert(A,_A);
            Object[] inputsFixed = new Object[1];
            inputsFixed[0] = _A;

            methods[i].invoke(null,inputsFixed);
            ConvertFMatrixStruct.convert(_A,A);
            assertTrue(MatrixFeatures_FDRM.isIdentical(expected,A, UtilEjml.TEST_F32 ));
            return;
        }
        fail("No match found");
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

        float alpha = 1.5f;
        float beta = -0.7f;
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(N, N, rand);
        FMatrixRMaj u = RandomMatrices_FDRM.rectangle(N, 1, rand);
        FMatrixRMaj v = RandomMatrices_FDRM.rectangle(N, 1, rand);
        FMatrixRMaj C = RandomMatrices_FDRM.rectangle(N, N, rand);

        FMatrixFixed _C;
        try {
            Class<?>[] parameters = match.getParameterTypes();
            FMatrixFixed _A = (FMatrixFixed)parameters[1].newInstance();
            FMatrixFixed _u = (FMatrixFixed)parameters[3].newInstance();
            FMatrixFixed _v = (FMatrixFixed)parameters[4].newInstance();
            _C = (FMatrixFixed)parameters[5].newInstance();

            ConvertFMatrixStruct.convert(A,_A);
            ConvertFMatrixStruct.convert(u,_u);
            ConvertFMatrixStruct.convert(v,_v);
            ConvertFMatrixStruct.convert(C,_C);

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

        CommonOps_FDRM.scale(alpha,A,C);
        VectorVectorMult_FDRM.addOuterProd(beta,u,v,C);

        assertTrue(MatrixFeatures_F.isIdentical(C,_C, UtilEjml.TEST_F32));
    }
}
