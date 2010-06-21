/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.dense.mult;

import org.ejml.data.DenseMatrix64F;


/**
 * This file has been automatically generated.  Do not modify directly.
 * 
 * @author Peter Abeles
 */
public class UnrolledMatrixMult {

    public static int NUM_UNROLLED = 20;

    public static Mult[] mult;
    public static Mult[] multAdd;
    public static MultS[] multS;
    public static MultS[] multAddS;
    public static Mult[] multTransA;
    public static Mult[] multTransAB;

    static {
        mult = new Mult[NUM_UNROLLED+1 ];
        multAdd = new Mult[NUM_UNROLLED+1 ];
        multS = new MultS[NUM_UNROLLED+1 ];
        multAddS = new MultS[NUM_UNROLLED+1 ];
        multTransA = new Mult[NUM_UNROLLED+1 ];
        multTransAB = new Mult[NUM_UNROLLED+1 ];

        declareMult();
        declareMultAdd();
        declareMultScale();
        declareMultAddScale();
        declareMultTransA();
        declareMultTransAB();
    }

    static void declareMult(){
        mult[1] = new Mult1();
        mult[2] = new Mult2();
        mult[3] = new Mult3();
        mult[4] = new Mult4();
        mult[5] = new Mult5();
        mult[6] = new Mult6();
        mult[7] = new Mult7();
        mult[8] = new Mult8();
        mult[9] = new Mult9();
        mult[10] = new Mult10();
        mult[11] = new Mult11();
        mult[12] = new Mult12();
        mult[13] = new Mult13();
        mult[14] = new Mult14();
        mult[15] = new Mult15();
        mult[16] = new Mult16();
        mult[17] = new Mult17();
        mult[18] = new Mult18();
        mult[19] = new Mult19();
        mult[20] = new Mult20();
    }

    static void declareMultAdd(){
        multAdd[1] = new MultAdd1();
        multAdd[2] = new MultAdd2();
        multAdd[3] = new MultAdd3();
        multAdd[4] = new MultAdd4();
        multAdd[5] = new MultAdd5();
        multAdd[6] = new MultAdd6();
        multAdd[7] = new MultAdd7();
        multAdd[8] = new MultAdd8();
        multAdd[9] = new MultAdd9();
        multAdd[10] = new MultAdd10();
        multAdd[11] = new MultAdd11();
        multAdd[12] = new MultAdd12();
        multAdd[13] = new MultAdd13();
        multAdd[14] = new MultAdd14();
        multAdd[15] = new MultAdd15();
        multAdd[16] = new MultAdd16();
        multAdd[17] = new MultAdd17();
        multAdd[18] = new MultAdd18();
        multAdd[19] = new MultAdd19();
        multAdd[20] = new MultAdd20();
    }

    static void declareMultScale(){
        multS[1] = new MultScale1();
        multS[2] = new MultScale2();
        multS[3] = new MultScale3();
        multS[4] = new MultScale4();
        multS[5] = new MultScale5();
        multS[6] = new MultScale6();
        multS[7] = new MultScale7();
        multS[8] = new MultScale8();
        multS[9] = new MultScale9();
        multS[10] = new MultScale10();
        multS[11] = new MultScale11();
        multS[12] = new MultScale12();
        multS[13] = new MultScale13();
        multS[14] = new MultScale14();
        multS[15] = new MultScale15();
        multS[16] = new MultScale16();
        multS[17] = new MultScale17();
        multS[18] = new MultScale18();
        multS[19] = new MultScale19();
        multS[20] = new MultScale20();
    }

    static void declareMultAddScale(){
        multAddS[1] = new MultAddScale1();
        multAddS[2] = new MultAddScale2();
        multAddS[3] = new MultAddScale3();
        multAddS[4] = new MultAddScale4();
        multAddS[5] = new MultAddScale5();
        multAddS[6] = new MultAddScale6();
        multAddS[7] = new MultAddScale7();
        multAddS[8] = new MultAddScale8();
        multAddS[9] = new MultAddScale9();
        multAddS[10] = new MultAddScale10();
        multAddS[11] = new MultAddScale11();
        multAddS[12] = new MultAddScale12();
        multAddS[13] = new MultAddScale13();
        multAddS[14] = new MultAddScale14();
        multAddS[15] = new MultAddScale15();
        multAddS[16] = new MultAddScale16();
        multAddS[17] = new MultAddScale17();
        multAddS[18] = new MultAddScale18();
        multAddS[19] = new MultAddScale19();
        multAddS[20] = new MultAddScale20();
    }

    static void declareMultTransA(){
        multTransA[1] = new MultTransA1();
        multTransA[2] = new MultTransA2();
        multTransA[3] = new MultTransA3();
        multTransA[4] = new MultTransA4();
        multTransA[5] = new MultTransA5();
        multTransA[6] = new MultTransA6();
        multTransA[7] = new MultTransA7();
        multTransA[8] = new MultTransA8();
        multTransA[9] = new MultTransA9();
        multTransA[10] = new MultTransA10();
        multTransA[11] = new MultTransA11();
        multTransA[12] = new MultTransA12();
        multTransA[13] = new MultTransA13();
        multTransA[14] = new MultTransA14();
        multTransA[15] = new MultTransA15();
        multTransA[16] = new MultTransA16();
        multTransA[17] = new MultTransA17();
        multTransA[18] = new MultTransA18();
        multTransA[19] = new MultTransA19();
        multTransA[20] = new MultTransA20();
    }

    static void declareMultTransAB(){
        multTransAB[1] = new MultTransAB1();
        multTransAB[2] = new MultTransAB2();
        multTransAB[3] = new MultTransAB3();
        multTransAB[4] = new MultTransAB4();
        multTransAB[5] = new MultTransAB5();
        multTransAB[6] = new MultTransAB6();
        multTransAB[7] = new MultTransAB7();
        multTransAB[8] = new MultTransAB8();
        multTransAB[9] = new MultTransAB9();
        multTransAB[10] = new MultTransAB10();
        multTransAB[11] = new MultTransAB11();
        multTransAB[12] = new MultTransAB12();
        multTransAB[13] = new MultTransAB13();
        multTransAB[14] = new MultTransAB14();
        multTransAB[15] = new MultTransAB15();
        multTransAB[16] = new MultTransAB16();
        multTransAB[17] = new MultTransAB17();
        multTransAB[18] = new MultTransAB18();
        multTransAB[19] = new MultTransAB19();
        multTransAB[20] = new MultTransAB20();
    }

    public static void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c ) {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        mult[b.numRows].mult(a,b,c);
    }

    public static void mult( double alpha ,DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c ) {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        multS[b.numRows].mult(alpha,a,b,c);
    }

    public static void multAdd( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c ) {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        multAdd[b.numRows].mult(a,b,c);
    }

    public static void multAdd( double alpha ,DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c ) {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        multAddS[b.numRows].mult(alpha,a,b,c);
    }

    public static void multTransA( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c ) {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        multTransA[b.numRows].mult(a,b,c);
    }

    public static void multTransAB( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c ) {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        multTransAB[b.numRows].mult(a,b,c);
    }

    public static class MultAdd1 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult1 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale1 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale1 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA1 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB1 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd2 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult2 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale2 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale2 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA2 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB2 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd3 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult3 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale3 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale3 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA3 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB3 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd4 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult4 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale4 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale4 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA4 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA]; indexA += a.numCols;
                double a3 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB]; indexB += b.numCols;
                    total += a3*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB4 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA]; indexA += a.numCols;
                double a3 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];
                    total += a3*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd5 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult5 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale5 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale5 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA5 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA]; indexA += a.numCols;
                double a3 = a.data[indexA]; indexA += a.numCols;
                double a4 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB]; indexB += b.numCols;
                    total += a3*b.data[indexB]; indexB += b.numCols;
                    total += a4*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB5 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA]; indexA += a.numCols;
                double a3 = dataA[indexA]; indexA += a.numCols;
                double a4 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];
                    total += a3*dataB[indexB++];
                    total += a4*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd6 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult6 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale6 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale6 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA6 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA]; indexA += a.numCols;
                double a3 = a.data[indexA]; indexA += a.numCols;
                double a4 = a.data[indexA]; indexA += a.numCols;
                double a5 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB]; indexB += b.numCols;
                    total += a3*b.data[indexB]; indexB += b.numCols;
                    total += a4*b.data[indexB]; indexB += b.numCols;
                    total += a5*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB6 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA]; indexA += a.numCols;
                double a3 = dataA[indexA]; indexA += a.numCols;
                double a4 = dataA[indexA]; indexA += a.numCols;
                double a5 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];
                    total += a3*dataB[indexB++];
                    total += a4*dataB[indexB++];
                    total += a5*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd7 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult7 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale7 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale7 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA7 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA]; indexA += a.numCols;
                double a3 = a.data[indexA]; indexA += a.numCols;
                double a4 = a.data[indexA]; indexA += a.numCols;
                double a5 = a.data[indexA]; indexA += a.numCols;
                double a6 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB]; indexB += b.numCols;
                    total += a3*b.data[indexB]; indexB += b.numCols;
                    total += a4*b.data[indexB]; indexB += b.numCols;
                    total += a5*b.data[indexB]; indexB += b.numCols;
                    total += a6*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB7 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA]; indexA += a.numCols;
                double a3 = dataA[indexA]; indexA += a.numCols;
                double a4 = dataA[indexA]; indexA += a.numCols;
                double a5 = dataA[indexA]; indexA += a.numCols;
                double a6 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];
                    total += a3*dataB[indexB++];
                    total += a4*dataB[indexB++];
                    total += a5*dataB[indexB++];
                    total += a6*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd8 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult8 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale8 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale8 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA8 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA]; indexA += a.numCols;
                double a3 = a.data[indexA]; indexA += a.numCols;
                double a4 = a.data[indexA]; indexA += a.numCols;
                double a5 = a.data[indexA]; indexA += a.numCols;
                double a6 = a.data[indexA]; indexA += a.numCols;
                double a7 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB]; indexB += b.numCols;
                    total += a3*b.data[indexB]; indexB += b.numCols;
                    total += a4*b.data[indexB]; indexB += b.numCols;
                    total += a5*b.data[indexB]; indexB += b.numCols;
                    total += a6*b.data[indexB]; indexB += b.numCols;
                    total += a7*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB8 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA]; indexA += a.numCols;
                double a3 = dataA[indexA]; indexA += a.numCols;
                double a4 = dataA[indexA]; indexA += a.numCols;
                double a5 = dataA[indexA]; indexA += a.numCols;
                double a6 = dataA[indexA]; indexA += a.numCols;
                double a7 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];
                    total += a3*dataB[indexB++];
                    total += a4*dataB[indexB++];
                    total += a5*dataB[indexB++];
                    total += a6*dataB[indexB++];
                    total += a7*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd9 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult9 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale9 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale9 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA9 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA]; indexA += a.numCols;
                double a3 = a.data[indexA]; indexA += a.numCols;
                double a4 = a.data[indexA]; indexA += a.numCols;
                double a5 = a.data[indexA]; indexA += a.numCols;
                double a6 = a.data[indexA]; indexA += a.numCols;
                double a7 = a.data[indexA]; indexA += a.numCols;
                double a8 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB]; indexB += b.numCols;
                    total += a3*b.data[indexB]; indexB += b.numCols;
                    total += a4*b.data[indexB]; indexB += b.numCols;
                    total += a5*b.data[indexB]; indexB += b.numCols;
                    total += a6*b.data[indexB]; indexB += b.numCols;
                    total += a7*b.data[indexB]; indexB += b.numCols;
                    total += a8*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB9 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA]; indexA += a.numCols;
                double a3 = dataA[indexA]; indexA += a.numCols;
                double a4 = dataA[indexA]; indexA += a.numCols;
                double a5 = dataA[indexA]; indexA += a.numCols;
                double a6 = dataA[indexA]; indexA += a.numCols;
                double a7 = dataA[indexA]; indexA += a.numCols;
                double a8 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];
                    total += a3*dataB[indexB++];
                    total += a4*dataB[indexB++];
                    total += a5*dataB[indexB++];
                    total += a6*dataB[indexB++];
                    total += a7*dataB[indexB++];
                    total += a8*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd10 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult10 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale10 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale10 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA10 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA]; indexA += a.numCols;
                double a3 = a.data[indexA]; indexA += a.numCols;
                double a4 = a.data[indexA]; indexA += a.numCols;
                double a5 = a.data[indexA]; indexA += a.numCols;
                double a6 = a.data[indexA]; indexA += a.numCols;
                double a7 = a.data[indexA]; indexA += a.numCols;
                double a8 = a.data[indexA]; indexA += a.numCols;
                double a9 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB]; indexB += b.numCols;
                    total += a3*b.data[indexB]; indexB += b.numCols;
                    total += a4*b.data[indexB]; indexB += b.numCols;
                    total += a5*b.data[indexB]; indexB += b.numCols;
                    total += a6*b.data[indexB]; indexB += b.numCols;
                    total += a7*b.data[indexB]; indexB += b.numCols;
                    total += a8*b.data[indexB]; indexB += b.numCols;
                    total += a9*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB10 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA]; indexA += a.numCols;
                double a3 = dataA[indexA]; indexA += a.numCols;
                double a4 = dataA[indexA]; indexA += a.numCols;
                double a5 = dataA[indexA]; indexA += a.numCols;
                double a6 = dataA[indexA]; indexA += a.numCols;
                double a7 = dataA[indexA]; indexA += a.numCols;
                double a8 = dataA[indexA]; indexA += a.numCols;
                double a9 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];
                    total += a3*dataB[indexB++];
                    total += a4*dataB[indexB++];
                    total += a5*dataB[indexB++];
                    total += a6*dataB[indexB++];
                    total += a7*dataB[indexB++];
                    total += a8*dataB[indexB++];
                    total += a9*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd11 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult11 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale11 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale11 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA11 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA]; indexA += a.numCols;
                double a3 = a.data[indexA]; indexA += a.numCols;
                double a4 = a.data[indexA]; indexA += a.numCols;
                double a5 = a.data[indexA]; indexA += a.numCols;
                double a6 = a.data[indexA]; indexA += a.numCols;
                double a7 = a.data[indexA]; indexA += a.numCols;
                double a8 = a.data[indexA]; indexA += a.numCols;
                double a9 = a.data[indexA]; indexA += a.numCols;
                double a10 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB]; indexB += b.numCols;
                    total += a3*b.data[indexB]; indexB += b.numCols;
                    total += a4*b.data[indexB]; indexB += b.numCols;
                    total += a5*b.data[indexB]; indexB += b.numCols;
                    total += a6*b.data[indexB]; indexB += b.numCols;
                    total += a7*b.data[indexB]; indexB += b.numCols;
                    total += a8*b.data[indexB]; indexB += b.numCols;
                    total += a9*b.data[indexB]; indexB += b.numCols;
                    total += a10*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB11 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA]; indexA += a.numCols;
                double a3 = dataA[indexA]; indexA += a.numCols;
                double a4 = dataA[indexA]; indexA += a.numCols;
                double a5 = dataA[indexA]; indexA += a.numCols;
                double a6 = dataA[indexA]; indexA += a.numCols;
                double a7 = dataA[indexA]; indexA += a.numCols;
                double a8 = dataA[indexA]; indexA += a.numCols;
                double a9 = dataA[indexA]; indexA += a.numCols;
                double a10 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];
                    total += a3*dataB[indexB++];
                    total += a4*dataB[indexB++];
                    total += a5*dataB[indexB++];
                    total += a6*dataB[indexB++];
                    total += a7*dataB[indexB++];
                    total += a8*dataB[indexB++];
                    total += a9*dataB[indexB++];
                    total += a10*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd12 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult12 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale12 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale12 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA12 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA]; indexA += a.numCols;
                double a3 = a.data[indexA]; indexA += a.numCols;
                double a4 = a.data[indexA]; indexA += a.numCols;
                double a5 = a.data[indexA]; indexA += a.numCols;
                double a6 = a.data[indexA]; indexA += a.numCols;
                double a7 = a.data[indexA]; indexA += a.numCols;
                double a8 = a.data[indexA]; indexA += a.numCols;
                double a9 = a.data[indexA]; indexA += a.numCols;
                double a10 = a.data[indexA]; indexA += a.numCols;
                double a11 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB]; indexB += b.numCols;
                    total += a3*b.data[indexB]; indexB += b.numCols;
                    total += a4*b.data[indexB]; indexB += b.numCols;
                    total += a5*b.data[indexB]; indexB += b.numCols;
                    total += a6*b.data[indexB]; indexB += b.numCols;
                    total += a7*b.data[indexB]; indexB += b.numCols;
                    total += a8*b.data[indexB]; indexB += b.numCols;
                    total += a9*b.data[indexB]; indexB += b.numCols;
                    total += a10*b.data[indexB]; indexB += b.numCols;
                    total += a11*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB12 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA]; indexA += a.numCols;
                double a3 = dataA[indexA]; indexA += a.numCols;
                double a4 = dataA[indexA]; indexA += a.numCols;
                double a5 = dataA[indexA]; indexA += a.numCols;
                double a6 = dataA[indexA]; indexA += a.numCols;
                double a7 = dataA[indexA]; indexA += a.numCols;
                double a8 = dataA[indexA]; indexA += a.numCols;
                double a9 = dataA[indexA]; indexA += a.numCols;
                double a10 = dataA[indexA]; indexA += a.numCols;
                double a11 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];
                    total += a3*dataB[indexB++];
                    total += a4*dataB[indexB++];
                    total += a5*dataB[indexB++];
                    total += a6*dataB[indexB++];
                    total += a7*dataB[indexB++];
                    total += a8*dataB[indexB++];
                    total += a9*dataB[indexB++];
                    total += a10*dataB[indexB++];
                    total += a11*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd13 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult13 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale13 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale13 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA13 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA]; indexA += a.numCols;
                double a3 = a.data[indexA]; indexA += a.numCols;
                double a4 = a.data[indexA]; indexA += a.numCols;
                double a5 = a.data[indexA]; indexA += a.numCols;
                double a6 = a.data[indexA]; indexA += a.numCols;
                double a7 = a.data[indexA]; indexA += a.numCols;
                double a8 = a.data[indexA]; indexA += a.numCols;
                double a9 = a.data[indexA]; indexA += a.numCols;
                double a10 = a.data[indexA]; indexA += a.numCols;
                double a11 = a.data[indexA]; indexA += a.numCols;
                double a12 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB]; indexB += b.numCols;
                    total += a3*b.data[indexB]; indexB += b.numCols;
                    total += a4*b.data[indexB]; indexB += b.numCols;
                    total += a5*b.data[indexB]; indexB += b.numCols;
                    total += a6*b.data[indexB]; indexB += b.numCols;
                    total += a7*b.data[indexB]; indexB += b.numCols;
                    total += a8*b.data[indexB]; indexB += b.numCols;
                    total += a9*b.data[indexB]; indexB += b.numCols;
                    total += a10*b.data[indexB]; indexB += b.numCols;
                    total += a11*b.data[indexB]; indexB += b.numCols;
                    total += a12*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB13 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA]; indexA += a.numCols;
                double a3 = dataA[indexA]; indexA += a.numCols;
                double a4 = dataA[indexA]; indexA += a.numCols;
                double a5 = dataA[indexA]; indexA += a.numCols;
                double a6 = dataA[indexA]; indexA += a.numCols;
                double a7 = dataA[indexA]; indexA += a.numCols;
                double a8 = dataA[indexA]; indexA += a.numCols;
                double a9 = dataA[indexA]; indexA += a.numCols;
                double a10 = dataA[indexA]; indexA += a.numCols;
                double a11 = dataA[indexA]; indexA += a.numCols;
                double a12 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];
                    total += a3*dataB[indexB++];
                    total += a4*dataB[indexB++];
                    total += a5*dataB[indexB++];
                    total += a6*dataB[indexB++];
                    total += a7*dataB[indexB++];
                    total += a8*dataB[indexB++];
                    total += a9*dataB[indexB++];
                    total += a10*dataB[indexB++];
                    total += a11*dataB[indexB++];
                    total += a12*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd14 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult14 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale14 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale14 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA14 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA]; indexA += a.numCols;
                double a3 = a.data[indexA]; indexA += a.numCols;
                double a4 = a.data[indexA]; indexA += a.numCols;
                double a5 = a.data[indexA]; indexA += a.numCols;
                double a6 = a.data[indexA]; indexA += a.numCols;
                double a7 = a.data[indexA]; indexA += a.numCols;
                double a8 = a.data[indexA]; indexA += a.numCols;
                double a9 = a.data[indexA]; indexA += a.numCols;
                double a10 = a.data[indexA]; indexA += a.numCols;
                double a11 = a.data[indexA]; indexA += a.numCols;
                double a12 = a.data[indexA]; indexA += a.numCols;
                double a13 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB]; indexB += b.numCols;
                    total += a3*b.data[indexB]; indexB += b.numCols;
                    total += a4*b.data[indexB]; indexB += b.numCols;
                    total += a5*b.data[indexB]; indexB += b.numCols;
                    total += a6*b.data[indexB]; indexB += b.numCols;
                    total += a7*b.data[indexB]; indexB += b.numCols;
                    total += a8*b.data[indexB]; indexB += b.numCols;
                    total += a9*b.data[indexB]; indexB += b.numCols;
                    total += a10*b.data[indexB]; indexB += b.numCols;
                    total += a11*b.data[indexB]; indexB += b.numCols;
                    total += a12*b.data[indexB]; indexB += b.numCols;
                    total += a13*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB14 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA]; indexA += a.numCols;
                double a3 = dataA[indexA]; indexA += a.numCols;
                double a4 = dataA[indexA]; indexA += a.numCols;
                double a5 = dataA[indexA]; indexA += a.numCols;
                double a6 = dataA[indexA]; indexA += a.numCols;
                double a7 = dataA[indexA]; indexA += a.numCols;
                double a8 = dataA[indexA]; indexA += a.numCols;
                double a9 = dataA[indexA]; indexA += a.numCols;
                double a10 = dataA[indexA]; indexA += a.numCols;
                double a11 = dataA[indexA]; indexA += a.numCols;
                double a12 = dataA[indexA]; indexA += a.numCols;
                double a13 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];
                    total += a3*dataB[indexB++];
                    total += a4*dataB[indexB++];
                    total += a5*dataB[indexB++];
                    total += a6*dataB[indexB++];
                    total += a7*dataB[indexB++];
                    total += a8*dataB[indexB++];
                    total += a9*dataB[indexB++];
                    total += a10*dataB[indexB++];
                    total += a11*dataB[indexB++];
                    total += a12*dataB[indexB++];
                    total += a13*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd15 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult15 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale15 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale15 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA15 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA]; indexA += a.numCols;
                double a3 = a.data[indexA]; indexA += a.numCols;
                double a4 = a.data[indexA]; indexA += a.numCols;
                double a5 = a.data[indexA]; indexA += a.numCols;
                double a6 = a.data[indexA]; indexA += a.numCols;
                double a7 = a.data[indexA]; indexA += a.numCols;
                double a8 = a.data[indexA]; indexA += a.numCols;
                double a9 = a.data[indexA]; indexA += a.numCols;
                double a10 = a.data[indexA]; indexA += a.numCols;
                double a11 = a.data[indexA]; indexA += a.numCols;
                double a12 = a.data[indexA]; indexA += a.numCols;
                double a13 = a.data[indexA]; indexA += a.numCols;
                double a14 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB]; indexB += b.numCols;
                    total += a3*b.data[indexB]; indexB += b.numCols;
                    total += a4*b.data[indexB]; indexB += b.numCols;
                    total += a5*b.data[indexB]; indexB += b.numCols;
                    total += a6*b.data[indexB]; indexB += b.numCols;
                    total += a7*b.data[indexB]; indexB += b.numCols;
                    total += a8*b.data[indexB]; indexB += b.numCols;
                    total += a9*b.data[indexB]; indexB += b.numCols;
                    total += a10*b.data[indexB]; indexB += b.numCols;
                    total += a11*b.data[indexB]; indexB += b.numCols;
                    total += a12*b.data[indexB]; indexB += b.numCols;
                    total += a13*b.data[indexB]; indexB += b.numCols;
                    total += a14*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB15 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA]; indexA += a.numCols;
                double a3 = dataA[indexA]; indexA += a.numCols;
                double a4 = dataA[indexA]; indexA += a.numCols;
                double a5 = dataA[indexA]; indexA += a.numCols;
                double a6 = dataA[indexA]; indexA += a.numCols;
                double a7 = dataA[indexA]; indexA += a.numCols;
                double a8 = dataA[indexA]; indexA += a.numCols;
                double a9 = dataA[indexA]; indexA += a.numCols;
                double a10 = dataA[indexA]; indexA += a.numCols;
                double a11 = dataA[indexA]; indexA += a.numCols;
                double a12 = dataA[indexA]; indexA += a.numCols;
                double a13 = dataA[indexA]; indexA += a.numCols;
                double a14 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];
                    total += a3*dataB[indexB++];
                    total += a4*dataB[indexB++];
                    total += a5*dataB[indexB++];
                    total += a6*dataB[indexB++];
                    total += a7*dataB[indexB++];
                    total += a8*dataB[indexB++];
                    total += a9*dataB[indexB++];
                    total += a10*dataB[indexB++];
                    total += a11*dataB[indexB++];
                    total += a12*dataB[indexB++];
                    total += a13*dataB[indexB++];
                    total += a14*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd16 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult16 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale16 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale16 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA16 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA]; indexA += a.numCols;
                double a3 = a.data[indexA]; indexA += a.numCols;
                double a4 = a.data[indexA]; indexA += a.numCols;
                double a5 = a.data[indexA]; indexA += a.numCols;
                double a6 = a.data[indexA]; indexA += a.numCols;
                double a7 = a.data[indexA]; indexA += a.numCols;
                double a8 = a.data[indexA]; indexA += a.numCols;
                double a9 = a.data[indexA]; indexA += a.numCols;
                double a10 = a.data[indexA]; indexA += a.numCols;
                double a11 = a.data[indexA]; indexA += a.numCols;
                double a12 = a.data[indexA]; indexA += a.numCols;
                double a13 = a.data[indexA]; indexA += a.numCols;
                double a14 = a.data[indexA]; indexA += a.numCols;
                double a15 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB]; indexB += b.numCols;
                    total += a3*b.data[indexB]; indexB += b.numCols;
                    total += a4*b.data[indexB]; indexB += b.numCols;
                    total += a5*b.data[indexB]; indexB += b.numCols;
                    total += a6*b.data[indexB]; indexB += b.numCols;
                    total += a7*b.data[indexB]; indexB += b.numCols;
                    total += a8*b.data[indexB]; indexB += b.numCols;
                    total += a9*b.data[indexB]; indexB += b.numCols;
                    total += a10*b.data[indexB]; indexB += b.numCols;
                    total += a11*b.data[indexB]; indexB += b.numCols;
                    total += a12*b.data[indexB]; indexB += b.numCols;
                    total += a13*b.data[indexB]; indexB += b.numCols;
                    total += a14*b.data[indexB]; indexB += b.numCols;
                    total += a15*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB16 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA]; indexA += a.numCols;
                double a3 = dataA[indexA]; indexA += a.numCols;
                double a4 = dataA[indexA]; indexA += a.numCols;
                double a5 = dataA[indexA]; indexA += a.numCols;
                double a6 = dataA[indexA]; indexA += a.numCols;
                double a7 = dataA[indexA]; indexA += a.numCols;
                double a8 = dataA[indexA]; indexA += a.numCols;
                double a9 = dataA[indexA]; indexA += a.numCols;
                double a10 = dataA[indexA]; indexA += a.numCols;
                double a11 = dataA[indexA]; indexA += a.numCols;
                double a12 = dataA[indexA]; indexA += a.numCols;
                double a13 = dataA[indexA]; indexA += a.numCols;
                double a14 = dataA[indexA]; indexA += a.numCols;
                double a15 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];
                    total += a3*dataB[indexB++];
                    total += a4*dataB[indexB++];
                    total += a5*dataB[indexB++];
                    total += a6*dataB[indexB++];
                    total += a7*dataB[indexB++];
                    total += a8*dataB[indexB++];
                    total += a9*dataB[indexB++];
                    total += a10*dataB[indexB++];
                    total += a11*dataB[indexB++];
                    total += a12*dataB[indexB++];
                    total += a13*dataB[indexB++];
                    total += a14*dataB[indexB++];
                    total += a15*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd17 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB]; iterB += b.numCols;
                double b16 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;
                    val  += dataA[iterA++]*b16;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult17 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB]; iterB += b.numCols;
                double b16 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;
                    val  += dataA[iterA++]*b16;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale17 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB]; iterB += b.numCols;
                double b16 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;
                    val  += dataA[iterA++]*b16;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale17 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB]; iterB += b.numCols;
                double b16 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;
                    val  += dataA[iterA++]*b16;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA17 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA]; indexA += a.numCols;
                double a3 = a.data[indexA]; indexA += a.numCols;
                double a4 = a.data[indexA]; indexA += a.numCols;
                double a5 = a.data[indexA]; indexA += a.numCols;
                double a6 = a.data[indexA]; indexA += a.numCols;
                double a7 = a.data[indexA]; indexA += a.numCols;
                double a8 = a.data[indexA]; indexA += a.numCols;
                double a9 = a.data[indexA]; indexA += a.numCols;
                double a10 = a.data[indexA]; indexA += a.numCols;
                double a11 = a.data[indexA]; indexA += a.numCols;
                double a12 = a.data[indexA]; indexA += a.numCols;
                double a13 = a.data[indexA]; indexA += a.numCols;
                double a14 = a.data[indexA]; indexA += a.numCols;
                double a15 = a.data[indexA]; indexA += a.numCols;
                double a16 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB]; indexB += b.numCols;
                    total += a3*b.data[indexB]; indexB += b.numCols;
                    total += a4*b.data[indexB]; indexB += b.numCols;
                    total += a5*b.data[indexB]; indexB += b.numCols;
                    total += a6*b.data[indexB]; indexB += b.numCols;
                    total += a7*b.data[indexB]; indexB += b.numCols;
                    total += a8*b.data[indexB]; indexB += b.numCols;
                    total += a9*b.data[indexB]; indexB += b.numCols;
                    total += a10*b.data[indexB]; indexB += b.numCols;
                    total += a11*b.data[indexB]; indexB += b.numCols;
                    total += a12*b.data[indexB]; indexB += b.numCols;
                    total += a13*b.data[indexB]; indexB += b.numCols;
                    total += a14*b.data[indexB]; indexB += b.numCols;
                    total += a15*b.data[indexB]; indexB += b.numCols;
                    total += a16*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB17 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA]; indexA += a.numCols;
                double a3 = dataA[indexA]; indexA += a.numCols;
                double a4 = dataA[indexA]; indexA += a.numCols;
                double a5 = dataA[indexA]; indexA += a.numCols;
                double a6 = dataA[indexA]; indexA += a.numCols;
                double a7 = dataA[indexA]; indexA += a.numCols;
                double a8 = dataA[indexA]; indexA += a.numCols;
                double a9 = dataA[indexA]; indexA += a.numCols;
                double a10 = dataA[indexA]; indexA += a.numCols;
                double a11 = dataA[indexA]; indexA += a.numCols;
                double a12 = dataA[indexA]; indexA += a.numCols;
                double a13 = dataA[indexA]; indexA += a.numCols;
                double a14 = dataA[indexA]; indexA += a.numCols;
                double a15 = dataA[indexA]; indexA += a.numCols;
                double a16 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];
                    total += a3*dataB[indexB++];
                    total += a4*dataB[indexB++];
                    total += a5*dataB[indexB++];
                    total += a6*dataB[indexB++];
                    total += a7*dataB[indexB++];
                    total += a8*dataB[indexB++];
                    total += a9*dataB[indexB++];
                    total += a10*dataB[indexB++];
                    total += a11*dataB[indexB++];
                    total += a12*dataB[indexB++];
                    total += a13*dataB[indexB++];
                    total += a14*dataB[indexB++];
                    total += a15*dataB[indexB++];
                    total += a16*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd18 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB]; iterB += b.numCols;
                double b16 = dataB[iterB]; iterB += b.numCols;
                double b17 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;
                    val  += dataA[iterA++]*b16;
                    val  += dataA[iterA++]*b17;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult18 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB]; iterB += b.numCols;
                double b16 = dataB[iterB]; iterB += b.numCols;
                double b17 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;
                    val  += dataA[iterA++]*b16;
                    val  += dataA[iterA++]*b17;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale18 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB]; iterB += b.numCols;
                double b16 = dataB[iterB]; iterB += b.numCols;
                double b17 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;
                    val  += dataA[iterA++]*b16;
                    val  += dataA[iterA++]*b17;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale18 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB]; iterB += b.numCols;
                double b16 = dataB[iterB]; iterB += b.numCols;
                double b17 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;
                    val  += dataA[iterA++]*b16;
                    val  += dataA[iterA++]*b17;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA18 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA]; indexA += a.numCols;
                double a3 = a.data[indexA]; indexA += a.numCols;
                double a4 = a.data[indexA]; indexA += a.numCols;
                double a5 = a.data[indexA]; indexA += a.numCols;
                double a6 = a.data[indexA]; indexA += a.numCols;
                double a7 = a.data[indexA]; indexA += a.numCols;
                double a8 = a.data[indexA]; indexA += a.numCols;
                double a9 = a.data[indexA]; indexA += a.numCols;
                double a10 = a.data[indexA]; indexA += a.numCols;
                double a11 = a.data[indexA]; indexA += a.numCols;
                double a12 = a.data[indexA]; indexA += a.numCols;
                double a13 = a.data[indexA]; indexA += a.numCols;
                double a14 = a.data[indexA]; indexA += a.numCols;
                double a15 = a.data[indexA]; indexA += a.numCols;
                double a16 = a.data[indexA]; indexA += a.numCols;
                double a17 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB]; indexB += b.numCols;
                    total += a3*b.data[indexB]; indexB += b.numCols;
                    total += a4*b.data[indexB]; indexB += b.numCols;
                    total += a5*b.data[indexB]; indexB += b.numCols;
                    total += a6*b.data[indexB]; indexB += b.numCols;
                    total += a7*b.data[indexB]; indexB += b.numCols;
                    total += a8*b.data[indexB]; indexB += b.numCols;
                    total += a9*b.data[indexB]; indexB += b.numCols;
                    total += a10*b.data[indexB]; indexB += b.numCols;
                    total += a11*b.data[indexB]; indexB += b.numCols;
                    total += a12*b.data[indexB]; indexB += b.numCols;
                    total += a13*b.data[indexB]; indexB += b.numCols;
                    total += a14*b.data[indexB]; indexB += b.numCols;
                    total += a15*b.data[indexB]; indexB += b.numCols;
                    total += a16*b.data[indexB]; indexB += b.numCols;
                    total += a17*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB18 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA]; indexA += a.numCols;
                double a3 = dataA[indexA]; indexA += a.numCols;
                double a4 = dataA[indexA]; indexA += a.numCols;
                double a5 = dataA[indexA]; indexA += a.numCols;
                double a6 = dataA[indexA]; indexA += a.numCols;
                double a7 = dataA[indexA]; indexA += a.numCols;
                double a8 = dataA[indexA]; indexA += a.numCols;
                double a9 = dataA[indexA]; indexA += a.numCols;
                double a10 = dataA[indexA]; indexA += a.numCols;
                double a11 = dataA[indexA]; indexA += a.numCols;
                double a12 = dataA[indexA]; indexA += a.numCols;
                double a13 = dataA[indexA]; indexA += a.numCols;
                double a14 = dataA[indexA]; indexA += a.numCols;
                double a15 = dataA[indexA]; indexA += a.numCols;
                double a16 = dataA[indexA]; indexA += a.numCols;
                double a17 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];
                    total += a3*dataB[indexB++];
                    total += a4*dataB[indexB++];
                    total += a5*dataB[indexB++];
                    total += a6*dataB[indexB++];
                    total += a7*dataB[indexB++];
                    total += a8*dataB[indexB++];
                    total += a9*dataB[indexB++];
                    total += a10*dataB[indexB++];
                    total += a11*dataB[indexB++];
                    total += a12*dataB[indexB++];
                    total += a13*dataB[indexB++];
                    total += a14*dataB[indexB++];
                    total += a15*dataB[indexB++];
                    total += a16*dataB[indexB++];
                    total += a17*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd19 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB]; iterB += b.numCols;
                double b16 = dataB[iterB]; iterB += b.numCols;
                double b17 = dataB[iterB]; iterB += b.numCols;
                double b18 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;
                    val  += dataA[iterA++]*b16;
                    val  += dataA[iterA++]*b17;
                    val  += dataA[iterA++]*b18;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult19 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB]; iterB += b.numCols;
                double b16 = dataB[iterB]; iterB += b.numCols;
                double b17 = dataB[iterB]; iterB += b.numCols;
                double b18 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;
                    val  += dataA[iterA++]*b16;
                    val  += dataA[iterA++]*b17;
                    val  += dataA[iterA++]*b18;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale19 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB]; iterB += b.numCols;
                double b16 = dataB[iterB]; iterB += b.numCols;
                double b17 = dataB[iterB]; iterB += b.numCols;
                double b18 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;
                    val  += dataA[iterA++]*b16;
                    val  += dataA[iterA++]*b17;
                    val  += dataA[iterA++]*b18;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale19 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB]; iterB += b.numCols;
                double b16 = dataB[iterB]; iterB += b.numCols;
                double b17 = dataB[iterB]; iterB += b.numCols;
                double b18 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;
                    val  += dataA[iterA++]*b16;
                    val  += dataA[iterA++]*b17;
                    val  += dataA[iterA++]*b18;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA19 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA]; indexA += a.numCols;
                double a3 = a.data[indexA]; indexA += a.numCols;
                double a4 = a.data[indexA]; indexA += a.numCols;
                double a5 = a.data[indexA]; indexA += a.numCols;
                double a6 = a.data[indexA]; indexA += a.numCols;
                double a7 = a.data[indexA]; indexA += a.numCols;
                double a8 = a.data[indexA]; indexA += a.numCols;
                double a9 = a.data[indexA]; indexA += a.numCols;
                double a10 = a.data[indexA]; indexA += a.numCols;
                double a11 = a.data[indexA]; indexA += a.numCols;
                double a12 = a.data[indexA]; indexA += a.numCols;
                double a13 = a.data[indexA]; indexA += a.numCols;
                double a14 = a.data[indexA]; indexA += a.numCols;
                double a15 = a.data[indexA]; indexA += a.numCols;
                double a16 = a.data[indexA]; indexA += a.numCols;
                double a17 = a.data[indexA]; indexA += a.numCols;
                double a18 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB]; indexB += b.numCols;
                    total += a3*b.data[indexB]; indexB += b.numCols;
                    total += a4*b.data[indexB]; indexB += b.numCols;
                    total += a5*b.data[indexB]; indexB += b.numCols;
                    total += a6*b.data[indexB]; indexB += b.numCols;
                    total += a7*b.data[indexB]; indexB += b.numCols;
                    total += a8*b.data[indexB]; indexB += b.numCols;
                    total += a9*b.data[indexB]; indexB += b.numCols;
                    total += a10*b.data[indexB]; indexB += b.numCols;
                    total += a11*b.data[indexB]; indexB += b.numCols;
                    total += a12*b.data[indexB]; indexB += b.numCols;
                    total += a13*b.data[indexB]; indexB += b.numCols;
                    total += a14*b.data[indexB]; indexB += b.numCols;
                    total += a15*b.data[indexB]; indexB += b.numCols;
                    total += a16*b.data[indexB]; indexB += b.numCols;
                    total += a17*b.data[indexB]; indexB += b.numCols;
                    total += a18*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB19 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA]; indexA += a.numCols;
                double a3 = dataA[indexA]; indexA += a.numCols;
                double a4 = dataA[indexA]; indexA += a.numCols;
                double a5 = dataA[indexA]; indexA += a.numCols;
                double a6 = dataA[indexA]; indexA += a.numCols;
                double a7 = dataA[indexA]; indexA += a.numCols;
                double a8 = dataA[indexA]; indexA += a.numCols;
                double a9 = dataA[indexA]; indexA += a.numCols;
                double a10 = dataA[indexA]; indexA += a.numCols;
                double a11 = dataA[indexA]; indexA += a.numCols;
                double a12 = dataA[indexA]; indexA += a.numCols;
                double a13 = dataA[indexA]; indexA += a.numCols;
                double a14 = dataA[indexA]; indexA += a.numCols;
                double a15 = dataA[indexA]; indexA += a.numCols;
                double a16 = dataA[indexA]; indexA += a.numCols;
                double a17 = dataA[indexA]; indexA += a.numCols;
                double a18 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];
                    total += a3*dataB[indexB++];
                    total += a4*dataB[indexB++];
                    total += a5*dataB[indexB++];
                    total += a6*dataB[indexB++];
                    total += a7*dataB[indexB++];
                    total += a8*dataB[indexB++];
                    total += a9*dataB[indexB++];
                    total += a10*dataB[indexB++];
                    total += a11*dataB[indexB++];
                    total += a12*dataB[indexB++];
                    total += a13*dataB[indexB++];
                    total += a14*dataB[indexB++];
                    total += a15*dataB[indexB++];
                    total += a16*dataB[indexB++];
                    total += a17*dataB[indexB++];
                    total += a18*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }

    public static class MultAdd20 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB]; iterB += b.numCols;
                double b16 = dataB[iterB]; iterB += b.numCols;
                double b17 = dataB[iterB]; iterB += b.numCols;
                double b18 = dataB[iterB]; iterB += b.numCols;
                double b19 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;
                    val  += dataA[iterA++]*b16;
                    val  += dataA[iterA++]*b17;
                    val  += dataA[iterA++]*b18;
                    val  += dataA[iterA++]*b19;

                    dataC[i*c.numCols+j] += val;
                }
            }
        }
    }

    public static class Mult20 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB]; iterB += b.numCols;
                double b16 = dataB[iterB]; iterB += b.numCols;
                double b17 = dataB[iterB]; iterB += b.numCols;
                double b18 = dataB[iterB]; iterB += b.numCols;
                double b19 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;
                    val  += dataA[iterA++]*b16;
                    val  += dataA[iterA++]*b17;
                    val  += dataA[iterA++]*b18;
                    val  += dataA[iterA++]*b19;

                    dataC[i*c.numCols+j] = val;
                }
            }
        }
    }

    public static class MultAddScale20 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB]; iterB += b.numCols;
                double b16 = dataB[iterB]; iterB += b.numCols;
                double b17 = dataB[iterB]; iterB += b.numCols;
                double b18 = dataB[iterB]; iterB += b.numCols;
                double b19 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;
                    val  += dataA[iterA++]*b16;
                    val  += dataA[iterA++]*b17;
                    val  += dataA[iterA++]*b18;
                    val  += dataA[iterA++]*b19;

                    dataC[i*c.numCols+j] += alpha*val;
                }
            }
        }
    }

    public static class MultScale20 implements MultS {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;

            for( int j = 0; j < b.numCols; j++ ) {
                int iterB = j;
                double b0 = dataB[iterB]; iterB += b.numCols;
                double b1 = dataB[iterB]; iterB += b.numCols;
                double b2 = dataB[iterB]; iterB += b.numCols;
                double b3 = dataB[iterB]; iterB += b.numCols;
                double b4 = dataB[iterB]; iterB += b.numCols;
                double b5 = dataB[iterB]; iterB += b.numCols;
                double b6 = dataB[iterB]; iterB += b.numCols;
                double b7 = dataB[iterB]; iterB += b.numCols;
                double b8 = dataB[iterB]; iterB += b.numCols;
                double b9 = dataB[iterB]; iterB += b.numCols;
                double b10 = dataB[iterB]; iterB += b.numCols;
                double b11 = dataB[iterB]; iterB += b.numCols;
                double b12 = dataB[iterB]; iterB += b.numCols;
                double b13 = dataB[iterB]; iterB += b.numCols;
                double b14 = dataB[iterB]; iterB += b.numCols;
                double b15 = dataB[iterB]; iterB += b.numCols;
                double b16 = dataB[iterB]; iterB += b.numCols;
                double b17 = dataB[iterB]; iterB += b.numCols;
                double b18 = dataB[iterB]; iterB += b.numCols;
                double b19 = dataB[iterB];

                int iterA = 0;
                for( int i = 0; i < a.numRows; i++ ) {
                    double val=0;
                    val  += dataA[iterA++]*b0;
                    val  += dataA[iterA++]*b1;
                    val  += dataA[iterA++]*b2;
                    val  += dataA[iterA++]*b3;
                    val  += dataA[iterA++]*b4;
                    val  += dataA[iterA++]*b5;
                    val  += dataA[iterA++]*b6;
                    val  += dataA[iterA++]*b7;
                    val  += dataA[iterA++]*b8;
                    val  += dataA[iterA++]*b9;
                    val  += dataA[iterA++]*b10;
                    val  += dataA[iterA++]*b11;
                    val  += dataA[iterA++]*b12;
                    val  += dataA[iterA++]*b13;
                    val  += dataA[iterA++]*b14;
                    val  += dataA[iterA++]*b15;
                    val  += dataA[iterA++]*b16;
                    val  += dataA[iterA++]*b17;
                    val  += dataA[iterA++]*b18;
                    val  += dataA[iterA++]*b19;

                    dataC[i*c.numCols+j] = alpha*val;
                }
            }
        }
    }

    public static class MultTransA20 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = a.data[indexA]; indexA += a.numCols;
                double a1 = a.data[indexA]; indexA += a.numCols;
                double a2 = a.data[indexA]; indexA += a.numCols;
                double a3 = a.data[indexA]; indexA += a.numCols;
                double a4 = a.data[indexA]; indexA += a.numCols;
                double a5 = a.data[indexA]; indexA += a.numCols;
                double a6 = a.data[indexA]; indexA += a.numCols;
                double a7 = a.data[indexA]; indexA += a.numCols;
                double a8 = a.data[indexA]; indexA += a.numCols;
                double a9 = a.data[indexA]; indexA += a.numCols;
                double a10 = a.data[indexA]; indexA += a.numCols;
                double a11 = a.data[indexA]; indexA += a.numCols;
                double a12 = a.data[indexA]; indexA += a.numCols;
                double a13 = a.data[indexA]; indexA += a.numCols;
                double a14 = a.data[indexA]; indexA += a.numCols;
                double a15 = a.data[indexA]; indexA += a.numCols;
                double a16 = a.data[indexA]; indexA += a.numCols;
                double a17 = a.data[indexA]; indexA += a.numCols;
                double a18 = a.data[indexA]; indexA += a.numCols;
                double a19 = a.data[indexA];

                for( int j = 0; j < b.numCols; j++ ) {
                    int indexB = j;
                    double total = 0;

                    total += a0*b.data[indexB]; indexB += b.numCols;
                    total += a1*b.data[indexB]; indexB += b.numCols;
                    total += a2*b.data[indexB]; indexB += b.numCols;
                    total += a3*b.data[indexB]; indexB += b.numCols;
                    total += a4*b.data[indexB]; indexB += b.numCols;
                    total += a5*b.data[indexB]; indexB += b.numCols;
                    total += a6*b.data[indexB]; indexB += b.numCols;
                    total += a7*b.data[indexB]; indexB += b.numCols;
                    total += a8*b.data[indexB]; indexB += b.numCols;
                    total += a9*b.data[indexB]; indexB += b.numCols;
                    total += a10*b.data[indexB]; indexB += b.numCols;
                    total += a11*b.data[indexB]; indexB += b.numCols;
                    total += a12*b.data[indexB]; indexB += b.numCols;
                    total += a13*b.data[indexB]; indexB += b.numCols;
                    total += a14*b.data[indexB]; indexB += b.numCols;
                    total += a15*b.data[indexB]; indexB += b.numCols;
                    total += a16*b.data[indexB]; indexB += b.numCols;
                    total += a17*b.data[indexB]; indexB += b.numCols;
                    total += a18*b.data[indexB]; indexB += b.numCols;
                    total += a19*b.data[indexB];

                    c.data[cIndex++] = total;
                }
            }
        }
    }

    public static class MultTransAB20 implements Mult {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
        {
            double dataA[] = a.data;
            double dataB[] = b.data;
            double dataC[] = c.data;
            int cIndex = 0;

            for( int i = 0; i < a.numCols; i++ ) {
                int indexA = i;
                double a0 = dataA[indexA]; indexA += a.numCols;
                double a1 = dataA[indexA]; indexA += a.numCols;
                double a2 = dataA[indexA]; indexA += a.numCols;
                double a3 = dataA[indexA]; indexA += a.numCols;
                double a4 = dataA[indexA]; indexA += a.numCols;
                double a5 = dataA[indexA]; indexA += a.numCols;
                double a6 = dataA[indexA]; indexA += a.numCols;
                double a7 = dataA[indexA]; indexA += a.numCols;
                double a8 = dataA[indexA]; indexA += a.numCols;
                double a9 = dataA[indexA]; indexA += a.numCols;
                double a10 = dataA[indexA]; indexA += a.numCols;
                double a11 = dataA[indexA]; indexA += a.numCols;
                double a12 = dataA[indexA]; indexA += a.numCols;
                double a13 = dataA[indexA]; indexA += a.numCols;
                double a14 = dataA[indexA]; indexA += a.numCols;
                double a15 = dataA[indexA]; indexA += a.numCols;
                double a16 = dataA[indexA]; indexA += a.numCols;
                double a17 = dataA[indexA]; indexA += a.numCols;
                double a18 = dataA[indexA]; indexA += a.numCols;
                double a19 = dataA[indexA];

                int indexB = 0;
                int endB = b.numRows*b.numCols;
                while( indexB != endB ) {
                    double total = 0;

                    total += a0*dataB[indexB++];
                    total += a1*dataB[indexB++];
                    total += a2*dataB[indexB++];
                    total += a3*dataB[indexB++];
                    total += a4*dataB[indexB++];
                    total += a5*dataB[indexB++];
                    total += a6*dataB[indexB++];
                    total += a7*dataB[indexB++];
                    total += a8*dataB[indexB++];
                    total += a9*dataB[indexB++];
                    total += a10*dataB[indexB++];
                    total += a11*dataB[indexB++];
                    total += a12*dataB[indexB++];
                    total += a13*dataB[indexB++];
                    total += a14*dataB[indexB++];
                    total += a15*dataB[indexB++];
                    total += a16*dataB[indexB++];
                    total += a17*dataB[indexB++];
                    total += a18*dataB[indexB++];
                    total += a19*dataB[indexB++];

                    dataC[cIndex++] = total;
                }
            }
        }
    }


    public static interface Mult
    {
        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c );
    }

    public static interface MultS
    {
        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c );
    }
}