/*
 * Copyright (c) 2009-2013, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.*;
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
public class TestFixedOps3 {

    Random rand = new Random(234);

    @Test
    public void dot() {
        FixedMatrix3_64F a = new FixedMatrix3_64F(1,2,3);
        FixedMatrix3_64F b = new FixedMatrix3_64F(2,3,4);

        double found = FixedOps3.dot(a,b);

        assertEquals(2+6+12,found,1e-8);
    }

    @Test
    public void diag() {
        FixedMatrix3x3_64F m = new FixedMatrix3x3_64F(1,2,3,4,5,6,7,8,9);
        FixedMatrix3_64F found = new FixedMatrix3_64F();

        FixedOps3.diag(m,found);

        assertEquals(1,found.a1,1e-8);
        assertEquals(5,found.a2,1e-8);
        assertEquals(9,found.a3,1e-8);
    }

    /**
     * Compares equivalent functions in FixedOps to CommonOps.  Inputs are randomly generated
     */
    @Test
    public void compareToCommonOps() {
        Method[] methods = FixedOps3.class.getMethods();

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

        assertEquals(0,numFailed);
        assertEquals(2,numNotMatched);
        assertEquals(28,numPassed);
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

        Class returnFixed = fixed.getReturnType();
        Class returnCommon = common.getReturnType();

        if( returnFixed == returnCommon )
            return true;

        if( Matrix64F.class.isAssignableFrom(returnFixed) &&
                Matrix64F.class.isAssignableFrom(returnCommon) )
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
            if(FixedMatrix3x3_64F.class == typesFixed[i]) {
                DenseMatrix64F m = RandomMatrices.createRandom(3, 3, rand);
                FixedMatrix3x3_64F f = new FixedMatrix3x3_64F();

                inputsFixed[i] = ConvertMatrixType.convert(m, f);
                inputsCommon[i] = m;
            } else if(FixedMatrix3_64F.class == typesFixed[i]) {
                DenseMatrix64F m = RandomMatrices.createRandom(3,1,rand);
                FixedMatrix3_64F f = new FixedMatrix3_64F();

                inputsFixed[i] = ConvertMatrixType.convert(m,f);
                inputsCommon[i] = m;
            } else if( double.class == typesFixed[i] ) {
                inputsFixed[i] = 2.5;
                inputsCommon[i] = 2.5;
            }
        }
    }

    private boolean handleSpecialCase( String name , Class[] typesFixed , Object[] inputsFixed, Object[] inputsCommon ) {
        if( "mult".compareTo(name) == 0 ) {
            if( FixedMatrix3_64F.class == typesFixed[0] ) {
                // swap the type of vector

                declareParamStandard(typesFixed,inputsFixed,inputsCommon);
                DenseMatrix64F a = (DenseMatrix64F)inputsCommon[0];
                DenseMatrix64F b = (DenseMatrix64F)inputsCommon[2];

                a.numRows=1; a.numCols=3;
                b.numRows=1; b.numCols=3;
                return true;
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
        } else if(FixedMatrix3x3_64F.class == a.getClass() ) {
            FixedMatrix3x3_64F f = (FixedMatrix3x3_64F)a;
            DenseMatrix64F m = new DenseMatrix64F(3,3);
            ConvertMatrixType.convert(f,m);

            return MatrixFeatures.isIdentical(m,(DenseMatrix64F)b,1e-8);

        } else if(FixedMatrix3_64F.class == a.getClass()) {
            DenseMatrix64F bb = (DenseMatrix64F)b;

            FixedMatrix3_64F f = (FixedMatrix3_64F)a;
            DenseMatrix64F m = new DenseMatrix64F(bb.numRows,bb.numCols);
            ConvertMatrixType.convert(f,m);

            return MatrixFeatures.isIdentical(m,bb,1e-8);
        } else if( Boolean.class == a.getClass() ) {
            return ((Boolean)a).booleanValue()  == ((Boolean)b).booleanValue();
        } else {
            fail("Not sure what this is");
        }
        return true;
    }

}
