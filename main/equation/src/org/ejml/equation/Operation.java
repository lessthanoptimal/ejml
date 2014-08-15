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

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.NormOps;

import java.util.List;

/**
 * Performs math operations.
 *
 * @author Peter Abeles
 */
// TODO sum
// TODO max,maxAbs
// TODO pow
// TODO dot <-- requires two inputs
// TODO solve ? or should that be kept outside since it would be the only function on the line?
abstract class Operation {

    public abstract void process();

    /**
     * If the variable is a local temporary variable it will be resized so that the operation can complete.  If not
     * temporary then it will not be reshaped
     * @param mat Variable containing the matrix
     * @param numRows Desired number of rows
     * @param numCols Desired number of columns
     */
    protected void resize( VariableMatrix mat , int numRows , int numCols ) {
        if( mat.isTemp() ) {
            mat.matrix.reshape(numRows,numCols);
        }
    }

    public static Info multiply(final Variable A, final Variable B, ManagerTempVariables manager) {

        Info ret = new Info();

        if( A instanceof VariableMatrix && B instanceof VariableMatrix ) {
            final VariableMatrix output = manager.createMatrix();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableMatrix mA = (VariableMatrix)A;
                    VariableMatrix mB = (VariableMatrix)B;

                    resize(output,mA.matrix.numRows,mB.matrix.numCols);
                    CommonOps.mult(mA.matrix,mB.matrix,output.matrix);
                }
            };
        } else if( A instanceof VariableScalar && B instanceof VariableScalar ) {
            final VariableDouble output = manager.createDouble();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableScalar mA = (VariableScalar)A;
                    VariableScalar mB = (VariableScalar)B;

                    output.value = mA.getDouble()*mB.getDouble();
                }
            };
        } else {
            final VariableMatrix output = manager.createMatrix();
            ret.output = output;
            final VariableMatrix m;
            final VariableScalar s;

            if( A instanceof VariableMatrix ) {
                m = (VariableMatrix)A;
                s = (VariableScalar)B;
            } else {
                m = (VariableMatrix)B;
                s = (VariableScalar)A;
            }

            ret.op = new Operation() {
                @Override
                public void process() {
                    output.matrix.reshape(m.matrix.numRows,m.matrix.numCols);
                    CommonOps.scale(s.getDouble(),m.matrix,output.matrix);
                }
            };
        }

        return ret;
    }

    public static Info divide(final Variable A, final Variable B, ManagerTempVariables manager) {

        Info ret = new Info();

        if( A instanceof VariableMatrix && B instanceof VariableMatrix ) {
            throw new RuntimeException("matrix division not supported.  use solve() or inv() explicitly.  solve is typically better");
        } else if( A instanceof VariableMatrix ) {
            final VariableMatrix output = manager.createMatrix();
            final VariableMatrix m = (VariableMatrix)A;
            final VariableScalar s = (VariableScalar)B;
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    output.matrix.reshape(m.matrix.numRows,m.matrix.numCols);
                    CommonOps.divide(s.getDouble(),m.matrix,output.matrix);
                }
            };
        } else if( A instanceof VariableMatrix ) {
            throw new RuntimeException("scalar divided by Matrix is undefined");
        } else {
            final VariableDouble output = manager.createDouble();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableDouble mA = (VariableDouble)A;
                    VariableDouble mB = (VariableDouble)B;

                    output.value = mA.value/mB.value;
                }
            };
        }

        return ret;
    }

    public static Info add(final Variable A, final Variable B, ManagerTempVariables manager) {
        Info ret = new Info();

        if( A instanceof VariableMatrix && B instanceof VariableMatrix ) {
            final VariableMatrix output = manager.createMatrix();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableMatrix mA = (VariableMatrix)A;
                    VariableMatrix mB = (VariableMatrix)B;

                    resize(output,mA.matrix.numRows,mA.matrix.numCols);
                    CommonOps.add(mA.matrix, mB.matrix, output.matrix);
                }
            };
        } else if( A instanceof VariableDouble && B instanceof VariableDouble ) {
            final VariableDouble output = manager.createDouble();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableScalar mA = (VariableScalar)A;
                    VariableScalar mB = (VariableScalar)B;

                    output.value = mA.getDouble() + mB.getDouble();
                }
            };
        } else {
            final VariableMatrix output = manager.createMatrix();
            ret.output = output;
            final VariableMatrix m;
            final VariableScalar s;

            if( A instanceof VariableMatrix ) {
                m = (VariableMatrix)A;
                s = (VariableScalar)B;
            } else {
                m = (VariableMatrix)B;
                s = (VariableScalar)A;
            }

            ret.op = new Operation() {
                @Override
                public void process() {
                    output.matrix.reshape(m.matrix.numRows,m.matrix.numCols);
                    CommonOps.add(m.matrix, s.getDouble(), output.matrix);
                }
            };
        }

        return ret;
    }

    public static Info subtract(final Variable A, final Variable B, ManagerTempVariables manager) {
        Info ret = new Info();

        if( A instanceof VariableMatrix && B instanceof VariableMatrix ) {
            final VariableMatrix output = manager.createMatrix();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableMatrix mA = (VariableMatrix)A;
                    VariableMatrix mB = (VariableMatrix)B;

                    resize(output,mA.matrix.numRows,mA.matrix.numCols);
                    CommonOps.sub(mA.matrix, mB.matrix, output.matrix);
                }
            };
        } else if( A instanceof VariableScalar && B instanceof VariableScalar ) {
            final VariableDouble output = manager.createDouble();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableScalar mA = (VariableScalar)A;
                    VariableScalar mB = (VariableScalar)B;

                    output.value = mA.getDouble() - mB.getDouble();
                }
            };
        } else {
            final VariableMatrix output = manager.createMatrix();
            ret.output = output;
            final VariableMatrix m;
            final VariableScalar s;

            if( A instanceof VariableMatrix ) {
                // matrix - value
                m = (VariableMatrix)A;
                s = (VariableScalar)B;

                ret.op = new Operation() {
                    @Override
                    public void process() {
                        output.matrix.reshape(m.matrix.numRows,m.matrix.numCols);
                        CommonOps.add(m.matrix, -s.getDouble(), output.matrix);
                    }
                };
            } else {
                // value - matrix
                m = (VariableMatrix)B;
                s = (VariableScalar)A;

                ret.op = new Operation() {
                    @Override
                    public void process() {
                        output.matrix.reshape(m.matrix.numRows,m.matrix.numCols);
                        output.matrix.set(m.matrix);
                        CommonOps.changeSign(output.matrix);
                        CommonOps.add(output.matrix,s.getDouble());
                    }
                };
            }
        }

        return ret;
    }

    public static Info elementMult( final Variable A , final Variable B , ManagerTempVariables manager ) {
        Info ret = new Info();

        if( A instanceof VariableMatrix && B instanceof VariableMatrix ) {
            final VariableMatrix output = manager.createMatrix();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableMatrix mA = (VariableMatrix)A;
                    VariableMatrix mB = (VariableMatrix)B;

                    resize(output,mA.matrix.numRows,mA.matrix.numCols);
                    CommonOps.elementMult(mA.matrix, mB.matrix, output.matrix);
                }
            };
        } else {
            throw new RuntimeException("Both inputs must be matrices for element wise multiplication");
        }

        return ret;
    }

    public static Info elementDivision( final Variable A , final Variable B , ManagerTempVariables manager ) {
        Info ret = new Info();

        if( A instanceof VariableMatrix && B instanceof VariableMatrix ) {
            final VariableMatrix output = manager.createMatrix();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableMatrix mA = (VariableMatrix)A;
                    VariableMatrix mB = (VariableMatrix)B;

                    resize(output,mA.matrix.numRows,mA.matrix.numCols);
                    CommonOps.elementDiv(mA.matrix, mB.matrix, output.matrix);
                }
            };
        } else {
            throw new RuntimeException("Both inputs must be matrices for element wise multiplication");
        }

        return ret;
    }

    public static Operation copy( final Variable src , final Variable dst ) {
        if( src instanceof VariableMatrix && dst instanceof VariableMatrix ) {
            return new Operation() {
                @Override
                public void process() {
                    ((VariableMatrix)dst).matrix.set(((VariableMatrix)src).matrix);
                }
            };
        } else if( src instanceof VariableScalar && dst instanceof VariableDouble ) {
            return new Operation() {
                @Override
                public void process() {
                    ((VariableDouble)dst).value = ((VariableScalar)src).getDouble();
                }
            };
        } else {
            throw new RuntimeException("Both variables must be the same type. "+src.getClass().getSimpleName()+" "+dst.getClass().getSimpleName());
        }
    }

    public static Info transpose( final Variable A , ManagerTempVariables manager) {
        Info ret = new Info();

        if( A instanceof VariableMatrix ) {
            final VariableMatrix output = manager.createMatrix();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableMatrix mA = (VariableMatrix)A;
                    output.matrix.reshape(mA.matrix.numCols,mA.matrix.numRows);
                    CommonOps.transpose(mA.matrix,output.matrix);
                }
            };
        } else {
            throw new RuntimeException("Transpose only makes sense for a matrix");
        }
        return ret;
    }

    /**
     * Matrix inverse
     */
    public static Info inv( final Variable A , ManagerTempVariables manager) {
        Info ret = new Info();

        if( A instanceof VariableMatrix ) {
            final VariableMatrix output = manager.createMatrix();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableMatrix mA = (VariableMatrix)A;
                    output.matrix.reshape(mA.matrix.numRows,mA.matrix.numCols);
                    if( !CommonOps.invert(mA.matrix,output.matrix) )
                        throw new RuntimeException("Inverse failed!");
                }
            };
        } else {
            final VariableDouble output = manager.createDouble();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableScalar mA = (VariableScalar)A;
                    output.value = 1.0/mA.getDouble();
                }
            };
        }

        return ret;
    }

    /**
     * Matrix pseudo-inverse
     */
    public static Info pinv( final Variable A , ManagerTempVariables manager) {
        Info ret = new Info();

        if( A instanceof VariableMatrix ) {
            final VariableMatrix output = manager.createMatrix();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableMatrix mA = (VariableMatrix)A;
                    output.matrix.reshape(mA.matrix.numRows,mA.matrix.numCols);
                    CommonOps.pinv(mA.matrix,output.matrix);
                }
            };
        } else {
            final VariableDouble output = manager.createDouble();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableScalar mA = (VariableScalar)A;
                    output.value = 1.0/mA.getDouble();
                }
            };
        }

        return ret;
    }

    /**
     * Matrix determinant
     */
    public static Info det( final Variable A , ManagerTempVariables manager) {
        Info ret = new Info();

        final VariableDouble output = manager.createDouble();
        ret.output = output;

        if( A instanceof VariableMatrix ) {
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableMatrix mA = (VariableMatrix)A;
                    output.value = CommonOps.det(mA.matrix);
                }
            };
        } else {
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableScalar mA = (VariableScalar)A;
                    output.value = mA.getDouble();
                }
            };
        }

        return ret;
    }

    public static Info trace( final Variable A , ManagerTempVariables manager) {
        Info ret = new Info();
        final VariableDouble output = manager.createDouble();
        ret.output = output;

        if( A instanceof VariableMatrix ) {
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableMatrix mA = (VariableMatrix)A;
                    output.value=CommonOps.trace(mA.matrix);
                }
            };
        } else {
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableScalar mA = (VariableScalar)A;
                    output.value = mA.getDouble();
                }
            };
        }

        return ret;
    }

    public static Info normF( final Variable A , ManagerTempVariables manager) {
        Info ret = new Info();
        final VariableDouble output = manager.createDouble();
        ret.output = output;

        if( A instanceof VariableMatrix ) {
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableMatrix mA = (VariableMatrix)A;
                    output.value= NormOps.normF(mA.matrix);
                }
            };
        } else {
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableScalar mA = (VariableScalar)A;
                    output.value = Math.abs(mA.getDouble());
                }
            };
        }

        return ret;
    }

    /**
     * Returns an identity matrix
     */
    public static Info eye( final Variable A , ManagerTempVariables manager) {
        Info ret = new Info();
        final VariableMatrix output = manager.createMatrix();
        ret.output = output;

        if( A instanceof VariableMatrix ) {
            ret.op = new Operation() {
                @Override
                public void process() {
                    DenseMatrix64F mA = ((VariableMatrix)A).matrix;
                    output.matrix.reshape(mA.numRows,mA.numCols);
                    CommonOps.setIdentity(output.matrix);
                }
            };
        } else if( A instanceof VariableInteger ) {
            ret.op = new Operation() {
                @Override
                public void process() {
                    int N = ((VariableInteger)A).value;
                    output.matrix.reshape(N,N);
                    CommonOps.setIdentity(output.matrix);
                }
            };
        } else {
            throw new RuntimeException("Unsupported variable type "+A);
        }

        return ret;
    }

    /**
     * Kronecker product
     */
    public static Info kron( final Variable A , final Variable B, ManagerTempVariables manager) {
        Info ret = new Info();
        final VariableMatrix output = manager.createMatrix();
        ret.output = output;

        if( A instanceof VariableMatrix && B instanceof VariableMatrix ) {
            ret.op = new Operation() {
                @Override
                public void process() {
                    DenseMatrix64F mA = ((VariableMatrix)A).matrix;
                    DenseMatrix64F mB = ((VariableMatrix)B).matrix;
                    output.matrix.reshape(mA.numRows * mB.numRows, mA.numCols * mB.numCols);
                    CommonOps.kron(mA, mB, output.matrix);
                }
            };
        } else {
            throw new RuntimeException("Both inputs must be matrices ");
        }

        return ret;
    }

    public static Info catV( final List<Variable> inputs, ManagerTempVariables manager) {
        Info ret = new Info();
        final VariableMatrix output = manager.createMatrix();
        ret.output = output;

        if (inputs.size() == 0)
            throw new RuntimeException("There must be at least one input to catV");

        for (int i = 0; i < inputs.size(); i++) {
            if (!(inputs.get(i) instanceof VariableMatrix))
                throw new RuntimeException("All inputs to catV must be a matrix");
        }

        ret.op = new Operation() {
            @Override
            public void process() {
                int numRows = 0, numCols;

                numCols = ((VariableMatrix) inputs.get(0)).matrix.numCols;

                for (int i = 0; i < inputs.size(); i++) {
                    DenseMatrix64F m = ((VariableMatrix) inputs.get(i)).matrix;
                    numRows += m.numRows;
                }

                output.matrix.reshape(numRows, numCols);

                int y = 0;
                for (int i = 0; i < inputs.size(); i++) {
                    DenseMatrix64F m = ((VariableMatrix) inputs.get(i)).matrix;
                    CommonOps.insert(m,output.matrix, y, 0);
                    y += m.numRows;
                }
            }
        };
        return ret;
    }


    public static Info catH( final List<Variable> inputs, ManagerTempVariables manager) {
        Info ret = new Info();
        final VariableMatrix output = manager.createMatrix();
        ret.output = output;

        if( inputs.size() == 0 )
            throw new RuntimeException("There must be at least one input to catH");

        for (int i = 0; i < inputs.size(); i++) {
            if( !(inputs.get(i) instanceof VariableMatrix) )
                throw new RuntimeException("All inputs to catH must be a matrix");
        }

        ret.op = new Operation() {
            @Override
            public void process() {
                int numRows,numCols=0;

                numRows = ((VariableMatrix)inputs.get(0)).matrix.numRows;

                for (int i = 0; i < inputs.size(); i++) {
                    DenseMatrix64F m = ((VariableMatrix)inputs.get(i)).matrix;
                    numCols += m.numCols;
                }

                output.matrix.reshape(numRows,numCols);

                int x = 0;
                for (int i = 0; i < inputs.size(); i++) {
                    DenseMatrix64F m = ((VariableMatrix)inputs.get(i)).matrix;
                    CommonOps.insert(m,output.matrix,0,x);
                    x += m.numCols;
                }
            }
        };

        return ret;
    }

    public static Info extract( final List<Variable> inputs, ManagerTempVariables manager) {
        Info ret = new Info();
        final VariableMatrix output = manager.createMatrix();
        ret.output = output;

        if( inputs.size() != 5 )
            throw new RuntimeException("Five inputs expected for sub");

        if(  !(inputs.get(0) instanceof VariableMatrix))
            throw new RuntimeException("First parameter must be a matrix.");

        for (int i = 1; i < inputs.size(); i++) {
            if( !(inputs.get(i) instanceof VariableInteger) )
                throw new RuntimeException("Last 4 parameters must be integers for sub");
        }

        ret.op = new Operation() {
            @Override
            public void process() {

                DenseMatrix64F A = ((VariableMatrix)inputs.get(0)).matrix;

                int row0 = ((VariableInteger)inputs.get(1)).value;
                int row1 = ((VariableInteger)inputs.get(2)).value+1;
                int col0 = ((VariableInteger)inputs.get(3)).value;
                int col1 = ((VariableInteger)inputs.get(4)).value+1;

                output.matrix.reshape(row1-row0,col1-col0);
                CommonOps.extract(A,row0,row1,col0,col1,output.matrix,0,0);
            }
        };

        return ret;
    }

    public static class Info
    {
        public Operation op;
        public Variable output;
    }
}
