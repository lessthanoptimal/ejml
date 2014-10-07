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

package org.ejml.alg.fixed;

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.FixedMatrix64F;
import org.ejml.data.RealMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.ConvertMatrixType;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public abstract class CompareFixedToCommonOps {

    Random rand = new Random(234);

    Class classFixed;
    int N;

    public CompareFixedToCommonOps(Class classFixed) {
        this.classFixed = classFixed;

        N = Integer.parseInt(classFixed.getSimpleName().charAt(8)+"");
    }

    /**
     * Compares equivalent functions in FixedOps to CommonOps.  Inputs are randomly generated
     */
    @Test
    public void compareToCommonOps() {
        Method[] methods = classFixed.getMethods();

        int numNotMatched = 0;
        int numPassed = 0;
        int numFailed = 0;

        for( Method fixedM : methods ) {
            if( !isValid(fixedM))
                continue;

            Method commonM = null;
            for( Method m : CommonOps.class.getMethods()) {
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
                System.out.println("Failed comparision to common: "+fixedM);
            }
        }

        int numExpected = 28;
        if( N > GenerateFixedOps.maxInverseSize ) {
            numExpected -= 2;
        }

        assertEquals(0,numFailed);
        assertEquals(2,numNotMatched);
        assertEquals(numExpected,numPassed);
    }

    /**
     * Checks to see if it is a valid Method which can be checked
     */
    private boolean isValid( Method m ) {
        Class[] types = m.getParameterTypes();

        for( Class c : types ) {
            if(FixedMatrix64F.class.isAssignableFrom(c))
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
            if( RealMatrix64F.class.isAssignableFrom(typesFixed[i]) ) {
                if( !RealMatrix64F.class.isAssignableFrom(typesCommon[i]) ) {
                    return false;
                }
            }
        }

        Class returnFixed = fixed.getReturnType();
        Class returnCommon = common.getReturnType();

        if( returnFixed == returnCommon )
            return true;

        if( RealMatrix64F.class.isAssignableFrom(returnFixed) &&
                RealMatrix64F.class.isAssignableFrom(returnCommon) )
            return true;

        return false;
    }

    private boolean compareToCommon( Method fixed , Method common ) {
        Class[] typesFixed = fixed.getParameterTypes();
        Object[] inputsFixed = new Object[ typesFixed.length ];
        Object[] inputsCommon = new Object[ typesFixed.length ];

        if( !handleSpecialCase(fixed.getName(),typesFixed,inputsFixed,inputsCommon) )
            declareParamStandard(typesFixed, inputsFixed, inputsCommon);

        try {
            Object retFixed = fixed.invoke(null,inputsFixed);
            Object retCommon = common.invoke(null,inputsCommon);

            checkEquivalent(retFixed,retCommon);

            for( int i = 0; i < inputsFixed.length; i++ ) {
                if( !checkEquivalent(inputsFixed[i],inputsCommon[i]) )
                    return false;
            }

        } catch (IllegalAccessException e) {
            fail("IllegalAccessException");
        } catch (InvocationTargetException e) {
            fail("InvocationTargetException");
        }

        return true;
    }

    private void declareParamStandard(Class[] typesFixed, Object[] inputsFixed, Object[] inputsCommon) {
        for( int i = 0; i < typesFixed.length; i++ ) {
            if(FixedMatrix64F.class.isAssignableFrom(typesFixed[i])) {
                FixedMatrix64F f = null;
                try {
                    f = (FixedMatrix64F)typesFixed[i].newInstance();
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                DenseMatrix64F m = RandomMatrices.createRandom(f.getNumRows(), f.getNumCols(), rand);

                ConvertMatrixType.convert(m, f);
                inputsFixed[i] = f;
                inputsCommon[i] = m;
            } else if( double.class == typesFixed[i] ) {
                inputsFixed[i] = 2.5;
                inputsCommon[i] = 2.5;
            }
        }
    }

    private boolean handleSpecialCase( String name , Class[] typesFixed , Object[] inputsFixed, Object[] inputsCommon ) {
        if( "mult".compareTo(name) == 0 ) {
            try {
                FixedMatrix64F f = (FixedMatrix64F)typesFixed[0].newInstance();

                // see if it's a vector
                if( f.getNumCols() == 1 || f.getNumRows() == 1  ) {
                    // swap the type of vector

                    declareParamStandard(typesFixed,inputsFixed,inputsCommon);
                    DenseMatrix64F a = (DenseMatrix64F)inputsCommon[0];
                    DenseMatrix64F b = (DenseMatrix64F)inputsCommon[2];

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

            return Math.abs(valA-valB) < 1e-8;
        } else if(FixedMatrix64F.class.isAssignableFrom(a.getClass()) ) {
            DenseMatrix64F bb = (DenseMatrix64F)b;

            FixedMatrix64F f = (FixedMatrix64F)a;
            DenseMatrix64F m = new DenseMatrix64F(f.getNumRows(),f.getNumCols());
            ConvertMatrixType.convert(f,m);
            m.numRows = bb.numRows;
            m.numCols = bb.numCols;

            return MatrixFeatures.isIdentical(m, bb, 1e-8);

        } else if( Boolean.class == a.getClass() ) {
            return ((Boolean)a).booleanValue()  == ((Boolean)b).booleanValue();
        } else {
            fail("Not sure what this is");
        }
        return true;
    }
}
