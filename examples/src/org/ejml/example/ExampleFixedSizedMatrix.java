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

package org.ejml.example;

import org.ejml.data.DMatrix3;
import org.ejml.data.DMatrix3x3;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.fixed.CommonOps_DDF3;
import org.ejml.ops.ConvertDMatrixStruct;
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
        DMatrix3x3 a = new DMatrix3x3();
        DMatrix3x3 b = new DMatrix3x3();

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
        CommonOps_DDF3.transpose(a,b);
        b.print();

        System.out.println("Determinant = "+ CommonOps_DDF3.det(a));

        // matrix-vector operations are also supported
        // Constructors for vectors and matrices can be used to initialize its value
        DMatrix3 v = new DMatrix3(1,2,3);
        DMatrix3 result = new DMatrix3();

        CommonOps_DDF3.mult(a,v,result);

        // Conversion into DMatrixRMaj can also be done
        DMatrixRMaj dm = ConvertDMatrixStruct.convert(a,null);

        dm.print();

        // This can be useful if you need do more advanced operations
        SimpleMatrix sv = SimpleMatrix.wrap(dm).svd().getV();

        // can then convert it back into a fixed matrix
        DMatrix3x3 fv = ConvertDMatrixStruct.convert(sv.matrix_F64(),(DMatrix3x3)null);

        System.out.println("Original simple matrix and converted fixed matrix");
        sv.print();
        fv.print();
    }
}
