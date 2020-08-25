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

package org.ejml.dense.row.mult;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Peter Abeles
 */
public class TestMatrixMatrixMult_MT_DDRM {
    Random rand = new Random(3245);
    int size = 200;
    int expectedFunctions = 24;
    int numTrials = 2;
    Class singleClass = MatrixMatrixMult_DDRM.class;
    Class threadedClass = MatrixMatrixMult_MT_DDRM.class;

    @Test
    void compareToSingle() {
        Method[] methods = threadedClass.getDeclaredMethods();
        int found = 0;
        for( Method tm : methods ) {
            if(!isTestMethod(tm))
                continue;

//            System.out.println("Looking at "+tm.getName()+" length="+tm.getParameterTypes().length);

            Method sm = findMatch(tm);
            if( sm == null ) {
                fail("Failed to find match for "+tm.getName()+" args.length "+tm.getParameterTypes().length);
                return;
            }
            compareBothMethods(tm,sm);
            found++;
        }
        assertEquals(found,expectedFunctions);
    }

    protected boolean isTestMethod( Method m ) {
        Class[] params = m.getParameterTypes();
        if( params.length == 0 )
            return false;

        if (!Modifier.isStatic(m.getModifiers()))
            return false;

        if (!Modifier.isPublic(m.getModifiers()))
            return false;

        for( Class p : params ) {
            if( DMatrix.class.isAssignableFrom(p) )
                return true;
        }
        return false;
    }

    private @Nullable Method findMatch(Method threadedM ) {
        for( Method m : singleClass.getMethods()) {
            if( isMatch(threadedM,m)) {
                return m;
            }
        }
        return null;
    }

    private boolean isMatch( Method fixed , Method common ) {
        if( fixed.getName().compareTo(common.getName()) != 0 )
            return false;

        Class[] typesFixed = fixed.getParameterTypes();
        Class[] typesCommon = common.getParameterTypes();

        if( typesFixed.length != typesCommon.length )
            return false;

        for (int i = 0; i < typesFixed.length; i++) {
            if( DMatrix.class.isAssignableFrom(typesFixed[i]) ) {
                if( !DMatrix.class.isAssignableFrom(typesCommon[i]) ) {
                    return false;
                }
            }
        }

        Class returnFixed = fixed.getReturnType();
        Class returnCommon = common.getReturnType();

        if( returnFixed == returnCommon )
            return true;

        if( DMatrix.class.isAssignableFrom(returnFixed) &&
                DMatrix.class.isAssignableFrom(returnCommon) )
            return true;

        // some "common" functions return the output as a convenience. Assume this to be the case
        if( returnFixed.getSimpleName().equals("void") && DMatrix.class.isAssignableFrom(returnCommon))
            return true;

        return false;
    }

    private boolean compareBothMethods(Method threaded , Method single ) {
        Class[] typesThreaded = threaded.getParameterTypes();
        Object[] inputsThreaded = new Object[ typesThreaded.length ];
        Object[] inputsSingle = new Object[ typesThreaded.length ];

        for (int trail = 0; trail < numTrials; trail++) {
            try {
                declareParamStandard(typesThreaded,inputsThreaded,inputsSingle);
                Object retThread = threaded.invoke(null,inputsThreaded);
                Object retSingle = single.invoke(null,inputsSingle);

                // If "common" returns the output matrix don't require the "fixed" implement to also
                boolean ignoreReturn = retThread == null
                        && retSingle != null && DMatrix.class.isAssignableFrom(retSingle.getClass());

                if( !ignoreReturn && !checkEquivalent(retThread,retSingle) )
                    return false;

                for( int i = 0; i < inputsThreaded.length; i++ ) {
                    if( !checkEquivalent(inputsThreaded[i],inputsSingle[i]) )
                        return false;
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
                fail("IllegalAccessException");
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                fail("InvocationTargetException");
            }
        }

        return true;
    }

    private void declareParamStandard(Class[] typesThreaded, Object[] inputsThreaded, Object[] inputsSingle) {
        for( int i = 0; i < typesThreaded.length; i++ ) {
            if(typesThreaded[i].isAssignableFrom(DMatrixRMaj.class)) {
                DMatrixRMaj m = new DMatrixRMaj(size,size);
                RandomMatrices_DDRM.fillUniform(m,-1,1,rand);
                inputsThreaded[i] = m.copy();
                inputsSingle[i] = m;
            } else if( double.class == typesThreaded[i] ) {
                inputsThreaded[i] = 2.5;
                inputsSingle[i] = 2.5;
            } else if( int.class == typesThreaded[i] ) {
                inputsThreaded[i] = 1;  // handle tailored towards extractRow and extractCol
                inputsSingle[i] = 1;
            }
        }
    }

    private boolean checkEquivalent( Object a , Object b ) {
        if( a == null ) {
            return b == null;
        } else if( Double.class == a.getClass() ) {
            double valA = ((Double)a).doubleValue();
            double valB = ((Double)b).doubleValue();

            return Math.abs(valA-valB) < UtilEjml.TEST_F64;
        } else if(DMatrixRMaj.class.isAssignableFrom(a.getClass()) ) {
            DMatrixRMaj bb = (DMatrixRMaj)b;
            DMatrixRMaj aa = (DMatrixRMaj)a;
            return MatrixFeatures_DDRM.isIdentical(aa, bb, UtilEjml.TEST_F64);

        } else if( Boolean.class == a.getClass() ) {
            return true;
        } else if( Integer.class == a.getClass() ) {
            return true;
        } else {
            fail("Not sure what this is");
        }
        return true;
    }
}
