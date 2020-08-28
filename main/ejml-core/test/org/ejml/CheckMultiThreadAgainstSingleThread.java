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

package org.ejml;

import org.ejml.data.*;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Generic class for comparing concurrent implementations of static functions against the single thread equivalent
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class CheckMultiThreadAgainstSingleThread {
    protected Random rand = new Random(3245);
    protected int size = 200;
    protected int numTrials = 2;
    int expectedFunctions;
    Class singleClass;
    Class threadedClass;

    protected CheckMultiThreadAgainstSingleThread(Class singleClass, Class threadedClass, int expectedFunctions) {
        this.singleClass = singleClass;
        this.threadedClass = threadedClass;
        this.expectedFunctions = expectedFunctions;
    }

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
            if( Matrix.class.isAssignableFrom(p) || Submatrix.class.isAssignableFrom(p) )
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
            if( Matrix.class.isAssignableFrom(typesFixed[i]) != Matrix.class.isAssignableFrom(typesCommon[i])) {
                return false;
            }
            if( Submatrix.class.isAssignableFrom(typesFixed[i]) != Submatrix.class.isAssignableFrom(typesCommon[i])) {
                return false;
            }
        }

        Class returnFixed = fixed.getReturnType();
        Class returnCommon = common.getReturnType();

        return returnFixed == returnCommon;
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
                        && retSingle != null && Matrix.class.isAssignableFrom(retSingle.getClass());

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

    protected Submatrix createSubmatrix( long seed ) {
        throw new RuntimeException("Must override this function if submatrices are involved");
    }

    protected void compareSubmatrices( Submatrix subA , Submatrix subB ) {
        throw new RuntimeException("Must override this function if submatrices are involved");
    }

    protected void declareParamStandard(Class[] typesThreaded, Object[] inputsThreaded, Object[] inputsSingle) {
        for( int i = 0; i < typesThreaded.length; i++ ) {
            if(typesThreaded[i].isAssignableFrom(FMatrixRMaj.class)) {
                FMatrixRMaj m = new FMatrixRMaj(size, size);
                RandomMatrices_FDRM.fillUniform(m, -1, 1, rand);
                inputsThreaded[i] = m.copy();
                inputsSingle[i] = m;
            } else if(typesThreaded[i].isAssignableFrom(DMatrixRMaj.class)) {
                DMatrixRMaj m = new DMatrixRMaj(size,size);
                RandomMatrices_DDRM.fillUniform(m,-1,1,rand);
                inputsThreaded[i] = m.copy();
                inputsSingle[i] = m;
            } else if(Submatrix.class.isAssignableFrom(typesThreaded[i])) {
                long seed = rand.nextLong();
                inputsThreaded[i] = createSubmatrix(seed);
                inputsSingle[i] = createSubmatrix(seed);
            } else if( float.class == typesThreaded[i] ) {
                inputsThreaded[i] = 2.5f;
                inputsSingle[i] = 2.5f;
            } else if( double.class == typesThreaded[i] ) {
                inputsThreaded[i] = 2.5;
                inputsSingle[i] = 2.5;
            } else if( int.class == typesThreaded[i] ) {
                inputsThreaded[i] = 1;  // handle tailored towards extractRow and extractCol
                inputsSingle[i] = 1;
            }
        }
    }

    protected boolean checkEquivalent( Object a , Object b ) {
        if( a == null ) {
            return b == null;
        } else if( Double.class == a.getClass() ) {
            double valA = ((Double)a).doubleValue();
            double valB = ((Double)b).doubleValue();

            return Math.abs(valA-valB) < UtilEjml.TEST_F64;
        } else if( Float.class == a.getClass() ) {
            double valA = ((Float)a).floatValue();
            double valB = ((Float)b).floatValue();

            return Math.abs(valA-valB) < UtilEjml.TEST_F32;
        } else if(Submatrix.class.isAssignableFrom(a.getClass()) ) {
            compareSubmatrices((Submatrix)a,(Submatrix)b);
        } else if(FMatrixRMaj.class.isAssignableFrom(a.getClass()) ) {
            FMatrixRMaj bb = (FMatrixRMaj)b;
            FMatrixRMaj aa = (FMatrixRMaj)a;
            return MatrixFeatures_FDRM.isIdentical(aa, bb, UtilEjml.TEST_F32);
        } else if(DMatrixRMaj.class.isAssignableFrom(a.getClass()) ) {
            DMatrixRMaj bb = (DMatrixRMaj)b;
            DMatrixRMaj aa = (DMatrixRMaj)a;
            return MatrixFeatures_DDRM.isIdentical(aa, bb, UtilEjml.TEST_F64);
        } else if(FMatrixRBlock.class.isAssignableFrom(a.getClass()) ) {
            FMatrixRBlock bb = (FMatrixRBlock)b;
            FMatrixRBlock aa = (FMatrixRBlock)a;
            return MatrixFeatures_FDRM.isIdentical(aa, bb, UtilEjml.TEST_F32);
        } else if(DMatrixRBlock.class.isAssignableFrom(a.getClass()) ) {
            DMatrixRBlock bb = (DMatrixRBlock)b;
            DMatrixRBlock aa = (DMatrixRBlock)a;
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
