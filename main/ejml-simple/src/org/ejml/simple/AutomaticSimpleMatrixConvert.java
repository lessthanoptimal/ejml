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

package org.ejml.simple;

import org.ejml.data.Matrix;
import org.ejml.data.MatrixType;
import org.ejml.ops.ConvertMatrixType;

/**
 * Converts a matrix type into the most common format to make sure data types are compatible
 * 
 * @author Peter Abeles
 */
public class AutomaticSimpleMatrixConvert {
    MatrixType commonType;

    public void specify0( SimpleBase a , SimpleBase ...inputs ) {
        SimpleBase array[] = new SimpleBase[inputs.length+1];
        System.arraycopy(inputs,0,array,0,inputs.length);
        array[inputs.length] = a;
        specify(inputs);
    }

    public void specify( SimpleBase ...inputs ) {
        boolean dense=false;
        boolean real=true;
        int bits=32;
        
        for( SimpleBase s : inputs ) {
            MatrixType t = s.mat.getType();
            if( t.isDense() )
                dense = true;
            if( !t.isReal())
                real = false;
            if( t.getBits() == 64 )
                bits=64;
        }

        commonType = MatrixType.lookup(dense,real,bits);
    }

    public <T extends SimpleBase<T>>T convert( SimpleBase matrix ) {
        if( matrix.getType() == commonType )
            return (T)matrix;

        if( !matrix.getType().isDense() && commonType.isDense() ) {
            System.err.println("\n***** WARNING *****\n");
            System.err.println("Converting a sparse to dense matrix automatically.");
            System.err.println("Current auto convert code isn't that smart and this might have been available");
        }

        Matrix m = ConvertMatrixType.convert(matrix.mat,commonType);
        if( m == null )
            throw new IllegalArgumentException("Conversion from "+matrix.getType()+" to "+commonType+" not possible");

        return (T)matrix.wrapMatrix(m);
    }
}
