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
public class MatrixUnroll_Mult_DDRM {
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
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M2 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M3 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M4 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M5 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M6 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M7 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M8 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++],a8  = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;total += a8  * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M9 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++],a8  = A[indexA++],a9  = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;total += a8  * B[indexB];indexB += colB;
                    total += a9  * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M10 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++],a8  = A[indexA++],a9  = A[indexA++],a10 = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;total += a8  * B[indexB];indexB += colB;
                    total += a9  * B[indexB];indexB += colB;total += a10 * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M11 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++],a8  = A[indexA++],a9  = A[indexA++],a10 = A[indexA++];
                double a11 = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;total += a8  * B[indexB];indexB += colB;
                    total += a9  * B[indexB];indexB += colB;total += a10 * B[indexB];indexB += colB;
                    total += a11 * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M12 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++],a8  = A[indexA++],a9  = A[indexA++],a10 = A[indexA++];
                double a11 = A[indexA++],a12 = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;total += a8  * B[indexB];indexB += colB;
                    total += a9  * B[indexB];indexB += colB;total += a10 * B[indexB];indexB += colB;
                    total += a11 * B[indexB];indexB += colB;total += a12 * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M13 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++],a8  = A[indexA++],a9  = A[indexA++],a10 = A[indexA++];
                double a11 = A[indexA++],a12 = A[indexA++],a13 = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;total += a8  * B[indexB];indexB += colB;
                    total += a9  * B[indexB];indexB += colB;total += a10 * B[indexB];indexB += colB;
                    total += a11 * B[indexB];indexB += colB;total += a12 * B[indexB];indexB += colB;
                    total += a13 * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M14 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++],a8  = A[indexA++],a9  = A[indexA++],a10 = A[indexA++];
                double a11 = A[indexA++],a12 = A[indexA++],a13 = A[indexA++],a14 = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;total += a8  * B[indexB];indexB += colB;
                    total += a9  * B[indexB];indexB += colB;total += a10 * B[indexB];indexB += colB;
                    total += a11 * B[indexB];indexB += colB;total += a12 * B[indexB];indexB += colB;
                    total += a13 * B[indexB];indexB += colB;total += a14 * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M15 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++],a8  = A[indexA++],a9  = A[indexA++],a10 = A[indexA++];
                double a11 = A[indexA++],a12 = A[indexA++],a13 = A[indexA++],a14 = A[indexA++],a15 = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;total += a8  * B[indexB];indexB += colB;
                    total += a9  * B[indexB];indexB += colB;total += a10 * B[indexB];indexB += colB;
                    total += a11 * B[indexB];indexB += colB;total += a12 * B[indexB];indexB += colB;
                    total += a13 * B[indexB];indexB += colB;total += a14 * B[indexB];indexB += colB;
                    total += a15 * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M16 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++],a8  = A[indexA++],a9  = A[indexA++],a10 = A[indexA++];
                double a11 = A[indexA++],a12 = A[indexA++],a13 = A[indexA++],a14 = A[indexA++],a15 = A[indexA++];
                double a16 = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;total += a8  * B[indexB];indexB += colB;
                    total += a9  * B[indexB];indexB += colB;total += a10 * B[indexB];indexB += colB;
                    total += a11 * B[indexB];indexB += colB;total += a12 * B[indexB];indexB += colB;
                    total += a13 * B[indexB];indexB += colB;total += a14 * B[indexB];indexB += colB;
                    total += a15 * B[indexB];indexB += colB;total += a16 * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M17 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++],a8  = A[indexA++],a9  = A[indexA++],a10 = A[indexA++];
                double a11 = A[indexA++],a12 = A[indexA++],a13 = A[indexA++],a14 = A[indexA++],a15 = A[indexA++];
                double a16 = A[indexA++],a17 = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;total += a8  * B[indexB];indexB += colB;
                    total += a9  * B[indexB];indexB += colB;total += a10 * B[indexB];indexB += colB;
                    total += a11 * B[indexB];indexB += colB;total += a12 * B[indexB];indexB += colB;
                    total += a13 * B[indexB];indexB += colB;total += a14 * B[indexB];indexB += colB;
                    total += a15 * B[indexB];indexB += colB;total += a16 * B[indexB];indexB += colB;
                    total += a17 * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M18 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++],a8  = A[indexA++],a9  = A[indexA++],a10 = A[indexA++];
                double a11 = A[indexA++],a12 = A[indexA++],a13 = A[indexA++],a14 = A[indexA++],a15 = A[indexA++];
                double a16 = A[indexA++],a17 = A[indexA++],a18 = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;total += a8  * B[indexB];indexB += colB;
                    total += a9  * B[indexB];indexB += colB;total += a10 * B[indexB];indexB += colB;
                    total += a11 * B[indexB];indexB += colB;total += a12 * B[indexB];indexB += colB;
                    total += a13 * B[indexB];indexB += colB;total += a14 * B[indexB];indexB += colB;
                    total += a15 * B[indexB];indexB += colB;total += a16 * B[indexB];indexB += colB;
                    total += a17 * B[indexB];indexB += colB;total += a18 * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M19 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++],a8  = A[indexA++],a9  = A[indexA++],a10 = A[indexA++];
                double a11 = A[indexA++],a12 = A[indexA++],a13 = A[indexA++],a14 = A[indexA++],a15 = A[indexA++];
                double a16 = A[indexA++],a17 = A[indexA++],a18 = A[indexA++],a19 = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;total += a8  * B[indexB];indexB += colB;
                    total += a9  * B[indexB];indexB += colB;total += a10 * B[indexB];indexB += colB;
                    total += a11 * B[indexB];indexB += colB;total += a12 * B[indexB];indexB += colB;
                    total += a13 * B[indexB];indexB += colB;total += a14 * B[indexB];indexB += colB;
                    total += a15 * B[indexB];indexB += colB;total += a16 * B[indexB];indexB += colB;
                    total += a17 * B[indexB];indexB += colB;total += a18 * B[indexB];indexB += colB;
                    total += a19 * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M20 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++],a8  = A[indexA++],a9  = A[indexA++],a10 = A[indexA++];
                double a11 = A[indexA++],a12 = A[indexA++],a13 = A[indexA++],a14 = A[indexA++],a15 = A[indexA++];
                double a16 = A[indexA++],a17 = A[indexA++],a18 = A[indexA++],a19 = A[indexA++],a20 = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;total += a8  * B[indexB];indexB += colB;
                    total += a9  * B[indexB];indexB += colB;total += a10 * B[indexB];indexB += colB;
                    total += a11 * B[indexB];indexB += colB;total += a12 * B[indexB];indexB += colB;
                    total += a13 * B[indexB];indexB += colB;total += a14 * B[indexB];indexB += colB;
                    total += a15 * B[indexB];indexB += colB;total += a16 * B[indexB];indexB += colB;
                    total += a17 * B[indexB];indexB += colB;total += a18 * B[indexB];indexB += colB;
                    total += a19 * B[indexB];indexB += colB;total += a20 * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M21 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++],a8  = A[indexA++],a9  = A[indexA++],a10 = A[indexA++];
                double a11 = A[indexA++],a12 = A[indexA++],a13 = A[indexA++],a14 = A[indexA++],a15 = A[indexA++];
                double a16 = A[indexA++],a17 = A[indexA++],a18 = A[indexA++],a19 = A[indexA++],a20 = A[indexA++];
                double a21 = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;total += a8  * B[indexB];indexB += colB;
                    total += a9  * B[indexB];indexB += colB;total += a10 * B[indexB];indexB += colB;
                    total += a11 * B[indexB];indexB += colB;total += a12 * B[indexB];indexB += colB;
                    total += a13 * B[indexB];indexB += colB;total += a14 * B[indexB];indexB += colB;
                    total += a15 * B[indexB];indexB += colB;total += a16 * B[indexB];indexB += colB;
                    total += a17 * B[indexB];indexB += colB;total += a18 * B[indexB];indexB += colB;
                    total += a19 * B[indexB];indexB += colB;total += a20 * B[indexB];indexB += colB;
                    total += a21 * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M22 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++],a8  = A[indexA++],a9  = A[indexA++],a10 = A[indexA++];
                double a11 = A[indexA++],a12 = A[indexA++],a13 = A[indexA++],a14 = A[indexA++],a15 = A[indexA++];
                double a16 = A[indexA++],a17 = A[indexA++],a18 = A[indexA++],a19 = A[indexA++],a20 = A[indexA++];
                double a21 = A[indexA++],a22 = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;total += a8  * B[indexB];indexB += colB;
                    total += a9  * B[indexB];indexB += colB;total += a10 * B[indexB];indexB += colB;
                    total += a11 * B[indexB];indexB += colB;total += a12 * B[indexB];indexB += colB;
                    total += a13 * B[indexB];indexB += colB;total += a14 * B[indexB];indexB += colB;
                    total += a15 * B[indexB];indexB += colB;total += a16 * B[indexB];indexB += colB;
                    total += a17 * B[indexB];indexB += colB;total += a18 * B[indexB];indexB += colB;
                    total += a19 * B[indexB];indexB += colB;total += a20 * B[indexB];indexB += colB;
                    total += a21 * B[indexB];indexB += colB;total += a22 * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M23 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++],a8  = A[indexA++],a9  = A[indexA++],a10 = A[indexA++];
                double a11 = A[indexA++],a12 = A[indexA++],a13 = A[indexA++],a14 = A[indexA++],a15 = A[indexA++];
                double a16 = A[indexA++],a17 = A[indexA++],a18 = A[indexA++],a19 = A[indexA++],a20 = A[indexA++];
                double a21 = A[indexA++],a22 = A[indexA++],a23 = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;total += a8  * B[indexB];indexB += colB;
                    total += a9  * B[indexB];indexB += colB;total += a10 * B[indexB];indexB += colB;
                    total += a11 * B[indexB];indexB += colB;total += a12 * B[indexB];indexB += colB;
                    total += a13 * B[indexB];indexB += colB;total += a14 * B[indexB];indexB += colB;
                    total += a15 * B[indexB];indexB += colB;total += a16 * B[indexB];indexB += colB;
                    total += a17 * B[indexB];indexB += colB;total += a18 * B[indexB];indexB += colB;
                    total += a19 * B[indexB];indexB += colB;total += a20 * B[indexB];indexB += colB;
                    total += a21 * B[indexB];indexB += colB;total += a22 * B[indexB];indexB += colB;
                    total += a23 * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

    public static class M24 implements MatrixMultUnrolled {
        @Override
        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  
                         final double[] C, final int offsetC, final int rowA, final int colB) {
            int indexA = offsetA;
            int cIndex = offsetC;
            for( int i = 0; i < rowA; i++ ) {
                double a1  = A[indexA++],a2  = A[indexA++],a3  = A[indexA++],a4  = A[indexA++],a5  = A[indexA++];
                double a6  = A[indexA++],a7  = A[indexA++],a8  = A[indexA++],a9  = A[indexA++],a10 = A[indexA++];
                double a11 = A[indexA++],a12 = A[indexA++],a13 = A[indexA++],a14 = A[indexA++],a15 = A[indexA++];
                double a16 = A[indexA++],a17 = A[indexA++],a18 = A[indexA++],a19 = A[indexA++],a20 = A[indexA++];
                double a21 = A[indexA++],a22 = A[indexA++],a23 = A[indexA++],a24 = A[indexA++];

                for( int j = 0; j < colB; j++ ) {
                    int indexB = offsetB+j;
                    double total = 0;

                    total += a1  * B[indexB];indexB += colB;total += a2  * B[indexB];indexB += colB;
                    total += a3  * B[indexB];indexB += colB;total += a4  * B[indexB];indexB += colB;
                    total += a5  * B[indexB];indexB += colB;total += a6  * B[indexB];indexB += colB;
                    total += a7  * B[indexB];indexB += colB;total += a8  * B[indexB];indexB += colB;
                    total += a9  * B[indexB];indexB += colB;total += a10 * B[indexB];indexB += colB;
                    total += a11 * B[indexB];indexB += colB;total += a12 * B[indexB];indexB += colB;
                    total += a13 * B[indexB];indexB += colB;total += a14 * B[indexB];indexB += colB;
                    total += a15 * B[indexB];indexB += colB;total += a16 * B[indexB];indexB += colB;
                    total += a17 * B[indexB];indexB += colB;total += a18 * B[indexB];indexB += colB;
                    total += a19 * B[indexB];indexB += colB;total += a20 * B[indexB];indexB += colB;
                    total += a21 * B[indexB];indexB += colB;total += a22 * B[indexB];indexB += colB;
                    total += a23 * B[indexB];indexB += colB;total += a24 * B[indexB];indexB += colB;

                    C[ cIndex++ ] = total;
                }
            }
        }
    }

}
