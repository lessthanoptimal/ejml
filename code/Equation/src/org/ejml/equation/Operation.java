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
import org.ejml.ops.NormOps;

/**
 * @author Peter Abeles
 */
public abstract class Operation {

    public static String functionNames[] = new String[]{"inv","det","normF","trace"};

    public abstract void process();

    protected void resize( VariableMatrix mat , int numRows , int numCols ) {
        if( mat.isTemp() ) {
            mat.matrix.reshape(numRows,numCols);
        }
    }

    public static Info mMult( final Variable A , final Variable B , ManagerTempVariables manager ) {

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
        } else if( A instanceof VariableDouble && B instanceof VariableDouble ) {
            final VariableDouble output = manager.createDouble();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableDouble mA = (VariableDouble)A;
                    VariableDouble mB = (VariableDouble)B;

                    output.scalar = mA.scalar*mB.scalar;
                }
            };
        } else {
            final VariableMatrix output = manager.createMatrix();
            ret.output = output;
            final VariableMatrix m;
            final VariableDouble s;

            if( A instanceof VariableMatrix ) {
                m = (VariableMatrix)A;
                s = (VariableDouble)B;
            } else {
                m = (VariableMatrix)B;
                s = (VariableDouble)A;
            }

            ret.op = new Operation() {
                @Override
                public void process() {
                    output.matrix.reshape(m.matrix.numRows,m.matrix.numCols);
                    CommonOps.scale(s.scalar,m.matrix,output.matrix);
                }
            };
        }

        return ret;
    }

    public static Info mDiv( final Variable A , final Variable B , ManagerTempVariables manager ) {

        Info ret = new Info();

        if( A instanceof VariableMatrix && B instanceof VariableMatrix ) {
            throw new RuntimeException("matrix division not supported.  use inv() explicitly");
        } else if( A instanceof VariableMatrix ) {
            final VariableMatrix output = manager.createMatrix();
            final VariableMatrix m = (VariableMatrix)A;
            final VariableDouble s = (VariableDouble)B;
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    output.matrix.reshape(m.matrix.numRows,m.matrix.numCols);
                    CommonOps.divide(s.scalar,m.matrix,output.matrix);
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

                    output.scalar = mA.scalar/mB.scalar;
                }
            };
        }

        return ret;
    }

    public static Info mAdd( final Variable A , final Variable B , ManagerTempVariables manager ) {
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
                    VariableDouble mA = (VariableDouble)A;
                    VariableDouble mB = (VariableDouble)B;

                    output.scalar = mA.scalar + mB.scalar;
                }
            };
        } else {
            final VariableMatrix output = manager.createMatrix();
            ret.output = output;
            final VariableMatrix m;
            final VariableDouble s;

            if( A instanceof VariableMatrix ) {
                m = (VariableMatrix)A;
                s = (VariableDouble)B;
            } else {
                m = (VariableMatrix)B;
                s = (VariableDouble)A;
            }

            ret.op = new Operation() {
                @Override
                public void process() {
                    output.matrix.reshape(m.matrix.numRows,m.matrix.numCols);
                    CommonOps.add(m.matrix, s.scalar, output.matrix);
                }
            };
        }

        return ret;
    }

    public static Info mSub( final Variable A , final Variable B , ManagerTempVariables manager ) {
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
        } else if( A instanceof VariableDouble && B instanceof VariableDouble ) {
            final VariableDouble output = manager.createDouble();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableDouble mA = (VariableDouble)A;
                    VariableDouble mB = (VariableDouble)B;

                    output.scalar = mA.scalar - mB.scalar;
                }
            };
        } else {
            final VariableMatrix output = manager.createMatrix();
            ret.output = output;
            final VariableMatrix m;
            final VariableDouble s;

            if( A instanceof VariableMatrix ) {
                // matrix - value
                m = (VariableMatrix)A;
                s = (VariableDouble)B;

                ret.op = new Operation() {
                    @Override
                    public void process() {
                        output.matrix.reshape(m.matrix.numRows,m.matrix.numCols);
                        CommonOps.add(m.matrix, -s.scalar, output.matrix);
                    }
                };
            } else {
                // value - matrix
                m = (VariableMatrix)B;
                s = (VariableDouble)A;

                ret.op = new Operation() {
                    @Override
                    public void process() {
                        output.matrix.reshape(m.matrix.numRows,m.matrix.numCols);
                        output.matrix.set(m.matrix);
                        CommonOps.changeSign(output.matrix);
                        CommonOps.add(output.matrix,s.scalar);
                    }
                };
            }
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
        } else if( src instanceof VariableDouble && dst instanceof VariableDouble ) {
            return new Operation() {
                @Override
                public void process() {
                    ((VariableDouble)dst).scalar = ((VariableDouble)src).scalar;
                }
            };
        } else {
            throw new RuntimeException("Both variables must be the same type. "+src.getClass().getSimpleName()+" "+dst.getClass().getSimpleName());
        }
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
                    CommonOps.invert(mA.matrix,output.matrix);
                }
            };
        } else {
            final VariableDouble output = manager.createDouble();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableDouble mA = (VariableDouble)A;
                    output.scalar = 1.0/mA.scalar;
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

        if( A instanceof VariableMatrix ) {
            final VariableMatrix output = manager.createMatrix();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableMatrix mA = (VariableMatrix)A;
                    output.matrix.reshape(mA.matrix.numRows,mA.matrix.numCols);
                    CommonOps.invert(mA.matrix,output.matrix);
                }
            };
        } else {
            final VariableDouble output = manager.createDouble();
            ret.output = output;
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableDouble mA = (VariableDouble)A;
                    output.scalar = 1.0/mA.scalar;
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
                    output.scalar=CommonOps.trace(mA.matrix);
                }
            };
        } else {
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableDouble mA = (VariableDouble)A;
                    output.scalar = mA.scalar;
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
                    output.scalar= NormOps.normF(mA.matrix);
                }
            };
        } else {
            ret.op = new Operation() {
                @Override
                public void process() {
                    VariableDouble mA = (VariableDouble)A;
                    output.scalar = mA.scalar;
                }
            };
        }

        return ret;
    }

    public static class Info
    {
        public Operation op;
        public Variable output;
    }
}
