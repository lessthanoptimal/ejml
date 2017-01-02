/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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

package org.ejml.example;

import org.ejml.alg.fixed.FixedOps3_D64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.FixedMatrix3_64F;
import org.ejml.data.FixedMatrix3x3_64F;
import org.ejml.ops.ConvertMatrixType_F64;
import org.ejml.simple.SimpleMatrix;

/**
 * In some applications a small fixed sized matrix can speed things up a lot, e.g. 8 times faster.  One application
 * which uses small matrices is graphics and rigid body motion, which extensively uses 3x3 and 4x4 matrices.  This
 * example is to show some examples of how you can use a fixed sized matrix.
 *
 * @author Peter Abeles
 */
public class ExampleFixedSizedMatrix {

    public static void main( String args[] ) {
        // declare the matrix
        FixedMatrix3x3_64F a = new FixedMatrix3x3_64F();
        FixedMatrix3x3_64F b = new FixedMatrix3x3_64F();

        // Can assign values the usual way
        for( int i = 0; i < 3; i++ ) {
            for( int j = 0; j < 3; j++ ) {
                a.set(i,j,i+j+1);
            }
        }

        // Direct manipulation of each value is the fastest way to assign/read values
        a.a11 = 12;
        a.a23 = 64;

        // can print the usual way too
        a.print();

        // most of the standard operations are support
        FixedOps3_D64.transpose(a,b);
        b.print();

        System.out.println("Determinant = "+ FixedOps3_D64.det(a));

        // matrix-vector operations are also supported
        // Constructors for vectors and matrices can be used to initialize its value
        FixedMatrix3_64F v = new FixedMatrix3_64F(1,2,3);
        FixedMatrix3_64F result = new FixedMatrix3_64F();

        FixedOps3_D64.mult(a,v,result);

        // Conversion into DenseMatrix64F can also be done
        DenseMatrix64F dm = ConvertMatrixType_F64.convert(a,null);

        dm.print();

        // This can be useful if you need do more advanced operations
        SimpleMatrix sv = SimpleMatrix.wrap(dm).svd().getV();

        // can then convert it back into a fixed matrix
        FixedMatrix3x3_64F fv = ConvertMatrixType_F64.convert(sv.getMatrix(),(FixedMatrix3x3_64F)null);

        System.out.println("Original simple matrix and converted fixed matrix");
        sv.print();
        fv.print();
    }
}
