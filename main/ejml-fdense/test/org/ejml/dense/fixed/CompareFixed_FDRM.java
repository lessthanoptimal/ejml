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
import org.ejml.data.FMatrix;
import org.ejml.data.FMatrixFixed;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.ops.ConvertFMatrixStruct;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Peter Abeles
 */
@SuppressWarnings("rawtypes")
public class CompareFixed_FDRM {
    Random rand = new Random(234);

    Class classFixed;
    Class classDense;
    int N;

    public CompareFixed_FDRM(Class classFixed, Class classDense ) {
        this.classFixed = classFixed;
        this.classDense = classDense;

        String name = classFixed.getName();
        N = Integer.parseInt(name.charAt(name.length()-1)+"");
    }

    /**
     * Compares equivalent functions in FixedOps to CommonOps.  Inputs are randomly generated
     */
    public void compareToCommonOps( int expectedPassed , int expectedNotMatched ) {
        Method[] methods = classFixed.getMethods();

        int numNotMatched = 0;
        int numPassed = 0;
        int numFailed = 0;

        for( Method fixedM : methods ) {
            if( !isValid(fixedM))
                continue;

            Method commonM = null;
            for( Method m : classDense.getMethods()) {
                if( isMatch(fixedM,m)) {
                    commonM = m;
                    break;
                }
            }

            if( commonM == null ) {
//                System.out.println("not matched: "+fixedM.getName());
                numNotMatched++;
                continue;
            }

            if( compareToCommon(fixedM,commonM) ) {
                numPassed++;
            } else {
                numFailed++;
                System.out.println("Failed comparison to common: "+fixedM);
            }
        }

        assertEquals(0,numFailed);
        assertEquals(expectedNotMatched,numNotMatched);
        assertEquals(expectedPassed,numPassed);
    }

    /**
     * Checks to see if it is a valid Method which can be checked
     */
    private boolean isValid( Method m ) {
        Class[] types = m.getParameterTypes();

        for( Class c : types ) {
            if(FMatrixFixed.class.isAssignableFrom(c))
                return true;
        }
        return false;
    }

    private boolean isMatch( Method fixed , Method common ) {
        if( fixed.getName().compareTo(common.getName()) != 0 )
            return false;

        Class[] typesFixed = fixed.getParameterTypes();
        Class[] typesCommon = common.getParameterTypes();

        if( typesFixed.length != typesCommon.length )
            return false;

        for (int i = 0; i < typesFixed.length; i++) {
            if( FMatrix.class.isAssignableFrom(typesFixed[i]) ) {
                if( !FMatrix.class.isAssignableFrom(typesCommon[i]) ) {
                    return false;
                }
            }
        }

        Class returnFixed = fixed.getReturnType();
        Class returnCommon = common.getReturnType();

        if( returnFixed == returnCommon )
            return true;

        if( FMatrix.class.isAssignableFrom(returnFixed) &&
                FMatrix.class.isAssignableFrom(returnCommon) )
            return true;

        // some "common" functions return the output as a convenience. Assume this to be the case
        if( returnFixed.getSimpleName().equals("void") && FMatrix.class.isAssignableFrom(returnCommon))
            return true;

        return false;
    }

    private boolean compareToCommon( Method fixed , Method common ) {
        Class[] typesFixed = fixed.getParameterTypes();
        Object[] inputsFixed = new Object[ typesFixed.length ];
        Object[] inputsCommon = new Object[ typesFixed.length ];

        for (int trail = 0; trail < 10; trail++) {
            if( !handleSpecialCase(fixed.getName(),typesFixed,inputsFixed,inputsCommon) )
                declareParamStandard(typesFixed, inputsFixed, inputsCommon);

            try {
                Object retFixed = fixed.invoke(null,inputsFixed);
                Object retCommon = common.invoke(null,inputsCommon);

                // If "common" returns the output matrix don't require the "fixed" implement to also
                boolean ignoreReturn = retFixed == null
                        && retCommon != null && FMatrix.class.isAssignableFrom(retCommon.getClass());

                if( !ignoreReturn && !checkEquivalent(retFixed,retCommon) )
                    return false;

                for( int i = 0; i < inputsFixed.length; i++ ) {
                    if( !checkEquivalent(inputsFixed[i],inputsCommon[i]) )
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

    private void declareParamStandard(Class[] typesFixed, Object[] inputsFixed, Object[] inputsCommon) {
        for( int i = 0; i < typesFixed.length; i++ ) {
            if(FMatrixFixed.class.isAssignableFrom(typesFixed[i])) {
                FMatrixFixed f = null;
                try {
                    f = (FMatrixFixed)typesFixed[i].newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                FMatrixRMaj m = RandomMatrices_FDRM.rectangle(f.getNumRows(), f.getNumCols(), -1,1,rand);

                ConvertFMatrixStruct.convert(m, f);
                inputsFixed[i] = f;
                inputsCommon[i] = m;
            } else if( float.class == typesFixed[i] ) {
                inputsFixed[i] = 2.5f;
                inputsCommon[i] = 2.5f;
            } else if( int.class == typesFixed[i] ) {
                inputsFixed[i] = 1;  // handle tailored towards extractRow and extractCol
                inputsCommon[i] = 1;
            }
        }
    }

    private boolean handleSpecialCase( String name , Class[] typesFixed , Object[] inputsFixed, Object[] inputsCommon ) {
        if( "mult".compareTo(name) == 0 ) {
            int offset = typesFixed[0] == float.class ? 1 : 0;
            try {
                FMatrixFixed f = (FMatrixFixed)typesFixed[offset].newInstance();

                // see if it's a vector
                if( f.getNumCols() == 1 || f.getNumRows() == 1  ) {
                    // swap the type of vector

                    declareParamStandard(typesFixed,inputsFixed,inputsCommon);
                    FMatrixRMaj a = (FMatrixRMaj)inputsCommon[offset];
                    FMatrixRMaj b = (FMatrixRMaj)inputsCommon[2+offset];

                    a.numRows=f.getNumCols(); a.numCols=f.getNumRows();
                    b.numRows=f.getNumCols(); b.numCols=f.getNumRows();
                    return true;
                }
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }


        }

        return false;
    }

    private boolean checkEquivalent( Object a , Object b ) {
        if( a == null ) {
            return b == null;
        } else if( Float.class == a.getClass() ) {
            float valA = ((Float)a).floatValue();
            float valB = ((Float)b).floatValue();

            return Math.abs(valA-valB) < UtilEjml.TEST_F32;
        } else if(FMatrixFixed.class.isAssignableFrom(a.getClass()) ) {
            FMatrixRMaj bb = (FMatrixRMaj)b;

            FMatrixFixed f = (FMatrixFixed)a;
            FMatrixRMaj m = new FMatrixRMaj(f.getNumRows(),f.getNumCols());
            ConvertFMatrixStruct.convert(f,m);
            m.numRows = bb.numRows;
            m.numCols = bb.numCols;

            return MatrixFeatures_FDRM.isIdentical(m, bb, UtilEjml.TEST_F32);

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
