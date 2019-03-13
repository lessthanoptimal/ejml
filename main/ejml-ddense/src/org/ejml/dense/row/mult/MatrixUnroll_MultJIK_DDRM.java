/*
 * Copyright (c) 2009-2019, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.mult;

import org.ejml.interfaces.mult.MatrixMultUnrolled;

/**
 * Matrix multiplication for an inner sub-matrix with an the inner most loop unrolled.
 *
 * @author Peter Abeles
 */
public class MatrixUnroll_MultJIK_DDRM {
    public static final int MAX = 40;
    public static final MatrixMultUnrolled[] ops = new MatrixMultUnrolled[]{
            new M1(),
            new M2(),
            new M3(),
            new M4(),
            new M5(),
            new M6(),
            new M7(),
            new M8(),
            new M9(),
            new M10(),
            new M11(),
            new M12(),
            new M13(),
            new M14(),
            new M15(),
            new M16(),
            new M17(),
            new M18(),
            new M19(),
            new M20(),
            new M21(),
            new M22(),
            new M23(),
            new M24(),
    };

    public static boolean mult( double[] A, int offsetA,  double[] B, int offsetB,  double[] C, int offsetC,
                                int rowA, int colA, int colB)
    {
        if( colA <= MAX ) {
            ops[colA-1].mult(A,offsetA,B,offsetB,C,offsetC,rowA,colB);
            return true;
        }
        return false;
    }

    public static class M1 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M2 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M3 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M4 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M5 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M6 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M7 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB],b7  = B[idxB+6*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M8 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB],b7  = B[idxB+6*colB],b8  = B[idxB+7*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; total += A[idxA++] * b8 ; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M9 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB],b7  = B[idxB+6*colB],b8  = B[idxB+7*colB],b9  = B[idxB+8*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; total += A[idxA++] * b8 ; total += A[idxA++] * b9 ; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M10 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB],b7  = B[idxB+6*colB],b8  = B[idxB+7*colB],b9  = B[idxB+8*colB],b10 = B[idxB+9*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; total += A[idxA++] * b8 ; total += A[idxA++] * b9 ; 
                    total += A[idxA++] * b10; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M11 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB],b7  = B[idxB+6*colB],b8  = B[idxB+7*colB],b9  = B[idxB+8*colB],b10 = B[idxB+9*colB];
                double b11  = B[idxB+10*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; total += A[idxA++] * b8 ; total += A[idxA++] * b9 ; 
                    total += A[idxA++] * b10; total += A[idxA++] * b11; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M12 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB],b7  = B[idxB+6*colB],b8  = B[idxB+7*colB],b9  = B[idxB+8*colB],b10 = B[idxB+9*colB];
                double b11  = B[idxB+10*colB],b12 = B[idxB+11*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; total += A[idxA++] * b8 ; total += A[idxA++] * b9 ; 
                    total += A[idxA++] * b10; total += A[idxA++] * b11; total += A[idxA++] * b12; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M13 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB],b7  = B[idxB+6*colB],b8  = B[idxB+7*colB],b9  = B[idxB+8*colB],b10 = B[idxB+9*colB];
                double b11  = B[idxB+10*colB],b12 = B[idxB+11*colB],b13 = B[idxB+12*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; total += A[idxA++] * b8 ; total += A[idxA++] * b9 ; 
                    total += A[idxA++] * b10; total += A[idxA++] * b11; total += A[idxA++] * b12; 
                    total += A[idxA++] * b13; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M14 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB],b7  = B[idxB+6*colB],b8  = B[idxB+7*colB],b9  = B[idxB+8*colB],b10 = B[idxB+9*colB];
                double b11  = B[idxB+10*colB],b12 = B[idxB+11*colB],b13 = B[idxB+12*colB],b14 = B[idxB+13*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; total += A[idxA++] * b8 ; total += A[idxA++] * b9 ; 
                    total += A[idxA++] * b10; total += A[idxA++] * b11; total += A[idxA++] * b12; 
                    total += A[idxA++] * b13; total += A[idxA++] * b14; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M15 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB],b7  = B[idxB+6*colB],b8  = B[idxB+7*colB],b9  = B[idxB+8*colB],b10 = B[idxB+9*colB];
                double b11  = B[idxB+10*colB],b12 = B[idxB+11*colB],b13 = B[idxB+12*colB],b14 = B[idxB+13*colB],b15 = B[idxB+14*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; total += A[idxA++] * b8 ; total += A[idxA++] * b9 ; 
                    total += A[idxA++] * b10; total += A[idxA++] * b11; total += A[idxA++] * b12; 
                    total += A[idxA++] * b13; total += A[idxA++] * b14; total += A[idxA++] * b15; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M16 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB],b7  = B[idxB+6*colB],b8  = B[idxB+7*colB],b9  = B[idxB+8*colB],b10 = B[idxB+9*colB];
                double b11  = B[idxB+10*colB],b12 = B[idxB+11*colB],b13 = B[idxB+12*colB],b14 = B[idxB+13*colB],b15 = B[idxB+14*colB];
                double b16  = B[idxB+15*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; total += A[idxA++] * b8 ; total += A[idxA++] * b9 ; 
                    total += A[idxA++] * b10; total += A[idxA++] * b11; total += A[idxA++] * b12; 
                    total += A[idxA++] * b13; total += A[idxA++] * b14; total += A[idxA++] * b15; 
                    total += A[idxA++] * b16; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M17 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB],b7  = B[idxB+6*colB],b8  = B[idxB+7*colB],b9  = B[idxB+8*colB],b10 = B[idxB+9*colB];
                double b11  = B[idxB+10*colB],b12 = B[idxB+11*colB],b13 = B[idxB+12*colB],b14 = B[idxB+13*colB],b15 = B[idxB+14*colB];
                double b16  = B[idxB+15*colB],b17 = B[idxB+16*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; total += A[idxA++] * b8 ; total += A[idxA++] * b9 ; 
                    total += A[idxA++] * b10; total += A[idxA++] * b11; total += A[idxA++] * b12; 
                    total += A[idxA++] * b13; total += A[idxA++] * b14; total += A[idxA++] * b15; 
                    total += A[idxA++] * b16; total += A[idxA++] * b17; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M18 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB],b7  = B[idxB+6*colB],b8  = B[idxB+7*colB],b9  = B[idxB+8*colB],b10 = B[idxB+9*colB];
                double b11  = B[idxB+10*colB],b12 = B[idxB+11*colB],b13 = B[idxB+12*colB],b14 = B[idxB+13*colB],b15 = B[idxB+14*colB];
                double b16  = B[idxB+15*colB],b17 = B[idxB+16*colB],b18 = B[idxB+17*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; total += A[idxA++] * b8 ; total += A[idxA++] * b9 ; 
                    total += A[idxA++] * b10; total += A[idxA++] * b11; total += A[idxA++] * b12; 
                    total += A[idxA++] * b13; total += A[idxA++] * b14; total += A[idxA++] * b15; 
                    total += A[idxA++] * b16; total += A[idxA++] * b17; total += A[idxA++] * b18; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M19 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB],b7  = B[idxB+6*colB],b8  = B[idxB+7*colB],b9  = B[idxB+8*colB],b10 = B[idxB+9*colB];
                double b11  = B[idxB+10*colB],b12 = B[idxB+11*colB],b13 = B[idxB+12*colB],b14 = B[idxB+13*colB],b15 = B[idxB+14*colB];
                double b16  = B[idxB+15*colB],b17 = B[idxB+16*colB],b18 = B[idxB+17*colB],b19 = B[idxB+18*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; total += A[idxA++] * b8 ; total += A[idxA++] * b9 ; 
                    total += A[idxA++] * b10; total += A[idxA++] * b11; total += A[idxA++] * b12; 
                    total += A[idxA++] * b13; total += A[idxA++] * b14; total += A[idxA++] * b15; 
                    total += A[idxA++] * b16; total += A[idxA++] * b17; total += A[idxA++] * b18; 
                    total += A[idxA++] * b19; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M20 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB],b7  = B[idxB+6*colB],b8  = B[idxB+7*colB],b9  = B[idxB+8*colB],b10 = B[idxB+9*colB];
                double b11  = B[idxB+10*colB],b12 = B[idxB+11*colB],b13 = B[idxB+12*colB],b14 = B[idxB+13*colB],b15 = B[idxB+14*colB];
                double b16  = B[idxB+15*colB],b17 = B[idxB+16*colB],b18 = B[idxB+17*colB],b19 = B[idxB+18*colB],b20 = B[idxB+19*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; total += A[idxA++] * b8 ; total += A[idxA++] * b9 ; 
                    total += A[idxA++] * b10; total += A[idxA++] * b11; total += A[idxA++] * b12; 
                    total += A[idxA++] * b13; total += A[idxA++] * b14; total += A[idxA++] * b15; 
                    total += A[idxA++] * b16; total += A[idxA++] * b17; total += A[idxA++] * b18; 
                    total += A[idxA++] * b19; total += A[idxA++] * b20; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M21 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB],b7  = B[idxB+6*colB],b8  = B[idxB+7*colB],b9  = B[idxB+8*colB],b10 = B[idxB+9*colB];
                double b11  = B[idxB+10*colB],b12 = B[idxB+11*colB],b13 = B[idxB+12*colB],b14 = B[idxB+13*colB],b15 = B[idxB+14*colB];
                double b16  = B[idxB+15*colB],b17 = B[idxB+16*colB],b18 = B[idxB+17*colB],b19 = B[idxB+18*colB],b20 = B[idxB+19*colB];
                double b21  = B[idxB+20*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; total += A[idxA++] * b8 ; total += A[idxA++] * b9 ; 
                    total += A[idxA++] * b10; total += A[idxA++] * b11; total += A[idxA++] * b12; 
                    total += A[idxA++] * b13; total += A[idxA++] * b14; total += A[idxA++] * b15; 
                    total += A[idxA++] * b16; total += A[idxA++] * b17; total += A[idxA++] * b18; 
                    total += A[idxA++] * b19; total += A[idxA++] * b20; total += A[idxA++] * b21; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M22 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB],b7  = B[idxB+6*colB],b8  = B[idxB+7*colB],b9  = B[idxB+8*colB],b10 = B[idxB+9*colB];
                double b11  = B[idxB+10*colB],b12 = B[idxB+11*colB],b13 = B[idxB+12*colB],b14 = B[idxB+13*colB],b15 = B[idxB+14*colB];
                double b16  = B[idxB+15*colB],b17 = B[idxB+16*colB],b18 = B[idxB+17*colB],b19 = B[idxB+18*colB],b20 = B[idxB+19*colB];
                double b21  = B[idxB+20*colB],b22 = B[idxB+21*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; total += A[idxA++] * b8 ; total += A[idxA++] * b9 ; 
                    total += A[idxA++] * b10; total += A[idxA++] * b11; total += A[idxA++] * b12; 
                    total += A[idxA++] * b13; total += A[idxA++] * b14; total += A[idxA++] * b15; 
                    total += A[idxA++] * b16; total += A[idxA++] * b17; total += A[idxA++] * b18; 
                    total += A[idxA++] * b19; total += A[idxA++] * b20; total += A[idxA++] * b21; 
                    total += A[idxA++] * b22; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M23 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1   = B[idxB+0*colB],b2  = B[idxB+1*colB],b3  = B[idxB+2*colB],b4  = B[idxB+3*colB],b5  = B[idxB+4*colB];
                double b6   = B[idxB+5*colB],b7  = B[idxB+6*colB],b8  = B[idxB+7*colB],b9  = B[idxB+8*colB],b10 = B[idxB+9*colB];
                double b11  = B[idxB+10*colB],b12 = B[idxB+11*colB],b13 = B[idxB+12*colB],b14 = B[idxB+13*colB],b15 = B[idxB+14*colB];
                double b16  = B[idxB+15*colB],b17 = B[idxB+16*colB],b18 = B[idxB+17*colB],b19 = B[idxB+18*colB],b20 = B[idxB+19*colB];
                double b21  = B[idxB+20*colB],b22 = B[idxB+21*colB],b23 = B[idxB+22*colB];
                int idxA = offsetA;
                for( int i = 0; i < rowA; i++ ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; total += A[idxA++] * b8 ; total += A[idxA++] * b9 ; 
                    total += A[idxA++] * b10; total += A[idxA++] * b11; total += A[idxA++] * b12; 
                    total += A[idxA++] * b13; total += A[idxA++] * b14; total += A[idxA++] * b15; 
                    total += A[idxA++] * b16; total += A[idxA++] * b17; total += A[idxA++] * b18; 
                    total += A[idxA++] * b19; total += A[idxA++] * b20; total += A[idxA++] * b21; 
                    total += A[idxA++] * b22; total += A[idxA++] * b23; 
                    C[ i*colB+j ] = total;
                }
            }
        }
    }

    public static class M24 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            for( int j = 0; j < colB; j++ ) {
                int idxB = offsetB+j;
                double b1  = B[idxB];idxB += colB;
                double b2  = B[idxB];idxB += colB;
                double b3  = B[idxB];idxB += colB;
                double b4  = B[idxB];idxB += colB;
                double b5  = B[idxB];idxB += colB;
                double b6  = B[idxB];idxB += colB;
                double b7  = B[idxB];idxB += colB;
                double b8  = B[idxB];idxB += colB;
                double b9  = B[idxB];idxB += colB;
                double b10 = B[idxB];idxB += colB;
                double b11 = B[idxB];idxB += colB;
                double b12 = B[idxB];idxB += colB;
                double b13 = B[idxB];idxB += colB;
                double b14 = B[idxB];idxB += colB;
                double b15 = B[idxB];idxB += colB;
                double b16 = B[idxB];idxB += colB;
                double b17 = B[idxB];idxB += colB;
                double b18 = B[idxB];idxB += colB;
                double b19 = B[idxB];idxB += colB;
                double b20 = B[idxB];idxB += colB;
                double b21 = B[idxB];idxB += colB;
                double b22 = B[idxB];idxB += colB;
                double b23 = B[idxB];idxB += colB;
                double b24 = B[idxB];

                int idxA = offsetA;
                int end = idxA + rowA*24;
                int idxC = 0;
                while( idxA < end ) {
                    double total = 0;
                    total += A[idxA++] * b1 ; total += A[idxA++] * b2 ; total += A[idxA++] * b3 ; 
                    total += A[idxA++] * b4 ; total += A[idxA++] * b5 ; total += A[idxA++] * b6 ; 
                    total += A[idxA++] * b7 ; total += A[idxA++] * b8 ; total += A[idxA++] * b9 ; 
                    total += A[idxA++] * b10; total += A[idxA++] * b11; total += A[idxA++] * b12; 
                    total += A[idxA++] * b13; total += A[idxA++] * b14; total += A[idxA++] * b15; 
                    total += A[idxA++] * b16; total += A[idxA++] * b17; total += A[idxA++] * b18; 
                    total += A[idxA++] * b19; total += A[idxA++] * b20; total += A[idxA++] * b21; 
                    total += A[idxA++] * b22; total += A[idxA++] * b23; total += A[idxA++] * b24; 
                    C[idxC++] = total;
                    idxC += colB;
                }
            }
        }
    }

}
