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

package org.ejml.alg.fixed;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixFixed_F64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.Matrix_F64;
import org.ejml.ops.ConvertMatrixStruct_F64;
import org.ejml.ops.MatrixFeatures_R64;
import org.ejml.ops.RandomMatrices_R64;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class CompareFixed_F64 {
    Random rand = new Random(234);

    Class classFixed;
    Class classDense;
    int N;

    public CompareFixed_F64(Class classFixed, Class classDense ) {
        this.classFixed = classFixed;
        this.classDense = classDense;

        String name = classFixed.getName();
        N = Integer.parseInt(name.charAt(name.length()-5)+"");
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
            if(DMatrixFixed_F64.class.isAssignableFrom(c))
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
            if( Matrix_F64.class.isAssignableFrom(typesFixed[i]) ) {
                if( !Matrix_F64.class.isAssignableFrom(typesCommon[i]) ) {
                    return false;
                }
            }
        }

        Class returnFixed = fixed.getReturnType();
        Class returnCommon = common.getReturnType();

        if( returnFixed == returnCommon )
            return true;

        if( Matrix_F64.class.isAssignableFrom(returnFixed) &&
                Matrix_F64.class.isAssignableFrom(returnCommon) )
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

                if( !checkEquivalent(retFixed,retCommon) )
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
            if(DMatrixFixed_F64.class.isAssignableFrom(typesFixed[i])) {
                DMatrixFixed_F64 f = null;
                try {
                    f = (DMatrixFixed_F64)typesFixed[i].newInstance();
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                DMatrixRow_F64 m = RandomMatrices_R64.createRandom(f.getNumRows(), f.getNumCols(), -1,1,rand);

                ConvertMatrixStruct_F64.convert(m, f);
                inputsFixed[i] = f;
                inputsCommon[i] = m;
            } else if( double.class == typesFixed[i] ) {
                inputsFixed[i] = 2.5;
                inputsCommon[i] = 2.5;
            } else if( int.class == typesFixed[i] ) {
                inputsFixed[i] = 1;  // handle tailored towards extractRow and extractCol
                inputsCommon[i] = 1;
            }
        }
    }

    private boolean handleSpecialCase( String name , Class[] typesFixed , Object[] inputsFixed, Object[] inputsCommon ) {
        if( "mult".compareTo(name) == 0 ) {
            try {
                DMatrixFixed_F64 f = (DMatrixFixed_F64)typesFixed[0].newInstance();

                // see if it's a vector
                if( f.getNumCols() == 1 || f.getNumRows() == 1  ) {
                    // swap the type of vector

                    declareParamStandard(typesFixed,inputsFixed,inputsCommon);
                    DMatrixRow_F64 a = (DMatrixRow_F64)inputsCommon[0];
                    DMatrixRow_F64 b = (DMatrixRow_F64)inputsCommon[2];

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
        } else if( Double.class == a.getClass() ) {
            double valA = ((Double)a).doubleValue();
            double valB = ((Double)b).doubleValue();

            return Math.abs(valA-valB) < UtilEjml.TEST_F64;
        } else if(DMatrixFixed_F64.class.isAssignableFrom(a.getClass()) ) {
            DMatrixRow_F64 bb = (DMatrixRow_F64)b;

            DMatrixFixed_F64 f = (DMatrixFixed_F64)a;
            DMatrixRow_F64 m = new DMatrixRow_F64(f.getNumRows(),f.getNumCols());
            ConvertMatrixStruct_F64.convert(f,m);
            m.numRows = bb.numRows;
            m.numCols = bb.numCols;

            return MatrixFeatures_R64.isIdentical(m, bb, UtilEjml.TEST_F64);

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
