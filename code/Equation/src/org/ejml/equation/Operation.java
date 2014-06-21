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

package org.ejml.equation;

import org.ejml.ops.CommonOps;

/**
 * @author Peter Abeles
 */
public abstract class Operation {
    public abstract void process();

    protected void resize( VariableMatrix mat , int numRows , int numCols ) {
        if( mat.isTemp() ) {
            mat.matrix.reshape(numRows,numCols);
        }
    }

    public static Operation mMult( final VariableMatrix A , final VariableMatrix B , final VariableMatrix output ) {
        return new Operation() {
            @Override
            public void process() {
                resize(output,A.matrix.numRows,B.matrix.numCols);
                CommonOps.mult(A.matrix,B.matrix,output.matrix);
            }
        };
    }

    public static Operation mAdd( final VariableMatrix A , final VariableMatrix B , final VariableMatrix output ) {
        return new Operation() {
            @Override
            public void process() {
                resize(output,A.matrix.numRows,A.matrix.numCols);
                CommonOps.add(A.matrix,B.matrix,output.matrix);
            }
        };
    }

    public static Operation mSub( final VariableMatrix A , final VariableMatrix B , final VariableMatrix output ) {
        return new Operation() {
            @Override
            public void process() {
                resize(output,A.matrix.numRows,A.matrix.numCols);
                CommonOps.sub(A.matrix, B.matrix, output.matrix);
            }
        };
    }

    public static Operation copy( final VariableMatrix src , final VariableMatrix dst ) {
        return new Operation() {
            @Override
            public void process() {
                dst.matrix.set(src.matrix);
            }
        };
    }
}
