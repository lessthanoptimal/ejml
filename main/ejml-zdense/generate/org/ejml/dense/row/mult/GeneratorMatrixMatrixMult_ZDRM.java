/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

import org.ejml.CodeGeneratorBase;

import java.io.FileNotFoundException;

/**
 * @author Peter Abeles
 */
public class GeneratorMatrixMatrixMult_ZDRM extends CodeGeneratorBase {

    @Override
    public void generate() throws FileNotFoundException {
        setOutputFile("MatrixMatrixMult_ZDRM");
        String preamble =
                "import org.ejml.MatrixDimensionException;\n" +
                "import org.ejml.data.ZMatrixRMaj;\n" +
                "import org.ejml.dense.row.CommonOps_ZDRM;\n" +
                "import org.jetbrains.annotations.Nullable;\n" +
                "\n" +
                "//CONCURRENT_INLINE import org.ejml.concurrency.EjmlConcurrency;\n" +
                "\n" +
                "/**\n" +
                " * <p>Matrix multiplication routines for complex row matrices in a row-major format.</p>\n" +
                " * \n" +
                standardClassDocClosing("Peter Abeles") +
                "@SuppressWarnings(\"Duplicates\")\n" +
                "public class "+className+" {\n";

        out.print(preamble);

        for( int i = 0; i < 2; i++ ) {
            boolean alpha = i == 1;
            for( int j = 0; j < 2; j++ ) {
                boolean add = j == 1;
                printMult_reroder(alpha,add);
                out.print("\n");
                printMult_small(alpha,add);
                out.print("\n");
                printMultTransA_reorder(alpha,add);
                out.print("\n");
                printMultTransA_small(alpha,add);
                out.print("\n");
                printMultTransB(alpha,add);
                out.print("\n");
                printMultTransAB(alpha,add);
                out.print("\n");
                out.println("    //CONCURRENT_OMIT_BEGIN");
                printMultTransAB_aux(alpha,add);
                out.println("    //CONCURRENT_OMIT_END");
                out.print("\n");
            }
        }
        out.print("}\n");
    }

    public void printMult_reroder( boolean alpha , boolean add ) {
        String header,valLine;

        header = makeHeader("mult","reorder",add,alpha, false, false,false);

        String tempVars = "";

        if( alpha ) {
            tempVars = "            double realTmp,imagTmp;";
            valLine = "            realTmp = a.data[indexA++];\n" +
                      "            imagTmp = a.data[indexA++];\n" +
                      "            realA = realAlpha*realTmp - imagAlpha*imagTmp;\n" +
                      "            imagA = realAlpha*imagTmp + imagAlpha*realTmp;\n";
        } else {
            valLine = "                realA = a.data[indexA++];\n" +
                      "                imagA = a.data[indexA++];\n";
        }

        String assignment = add ? "+=" : "=";

        String foo = header + makeBoundsCheck(false,false, null)+handleZeros(add) +
                "\n" +
                "        int strideA = a.getRowStride();\n" +
                "        int strideB = b.getRowStride();\n" +
                "        int strideC = c.getRowStride();\n" +
                "        int endOfKLoop = b.numRows*strideB;\n" +
                "\n" +
                "        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, a.numRows, i -> {\n" +
                "        for (int i = 0; i < a.numRows; i++) {\n" +
                tempVars +
                "            double realA, imagA;\n" +
                "            int indexCbase = i*strideC;\n" +
                "            int indexA = i*strideA;\n" +
                "\n" +
                "            // need to assign c.data to a value initially\n" +
                "            int indexB = 0;\n" +
                "            int indexC = indexCbase;\n" +
                "            int end = indexB + strideB;\n" +
                "\n" +
                valLine +
                "\n" +
                "            while (indexB < end) {\n" +
                "                double realB = b.data[indexB++];\n" +
                "                double imagB = b.data[indexB++];\n" +
                "\n" +
                "                c.data[indexC++] "+assignment+" realA*realB - imagA*imagB;\n" +
                "                c.data[indexC++] "+assignment+" realA*imagB + imagA*realB;\n" +
                "            }\n" +
                "\n" +
                "            // now add to it\n" +
                "            while (indexB != endOfKLoop) { // k loop\n" +
                "                indexC = indexCbase;\n" +
                "                end = indexB + strideB;\n" +
                "\n" +
                valLine +
                "\n" +
                "                while (indexB < end) { // j loop\n" +
                "                    double realB = b.data[indexB++];\n" +
                "                    double imagB = b.data[indexB++];\n" +
                "\n" +
                "                    c.data[indexC++] += realA*realB - imagA*imagB;\n" +
                "                    c.data[indexC++] += realA*imagB + imagA*realB;\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "        //CONCURRENT_ABOVE });\n" +
                "    }\n";

        out.print(foo);
    }

    public void printMult_small( boolean alpha , boolean add ) {
        String header,valLine;

        header = makeHeader("mult","small",add,alpha, false, false,false);

        String assignment = add ? "+=" : "=";

        if( alpha ) {
            valLine = "                c.data[indexC++] "+assignment+" realAlpha*realTotal - imagAlpha*imagTotal;\n" +
                      "                c.data[indexC++] "+assignment+" realAlpha*imagTotal + imagAlpha*realTotal;\n";
        } else {
            valLine = "                c.data[indexC++] "+assignment+" realTotal;\n" +
                      "                c.data[indexC++] "+assignment+" imagTotal;\n";
        }

        String foo =
                header + makeBoundsCheck(false,false, null)+
                        "        int strideA = a.getRowStride();\n" +
                        "        int strideB = b.getRowStride();\n" +
                        "\n" +
                        "        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, a.numRows, i -> {\n" +
                        "        for (int i = 0; i < a.numRows; i++) {\n" +
                        "            int aIndexStart = i*strideA;\n" +
                        "            int indexC = i*strideB;\n" +
                        "            for (int j = 0; j < b.numCols; j++) {\n" +
                        "                double realTotal = 0;\n" +
                        "                double imagTotal = 0;\n" +
                        "\n" +
                        "                int indexA = aIndexStart;\n" +
                        "                int indexB = j*2;\n" +
                        "                int end = indexA + strideA;\n" +
                        "                while (indexA < end) {\n" +
                        "                    double realA = a.data[indexA++];\n" +
                        "                    double imagA = a.data[indexA++];\n" +
                        "\n" +
                        "                    double realB = b.data[indexB];\n" +
                        "                    double imagB = b.data[indexB + 1];\n" +
                        "\n" +
                        "                    realTotal += realA*realB - imagA*imagB;\n" +
                        "                    imagTotal += realA*imagB + imagA*realB;\n" +
                        "\n" +
                        "                    indexB += strideB;\n" +
                        "                }\n" +
                        "\n" +
                        valLine +
                        "            }\n" +
                        "        }\n" +
                        "        //CONCURRENT_ABOVE });\n" +
                        "    }\n";

        out.print(foo);
    }

    public void printMultTransA_reorder( boolean alpha , boolean add ) {
        String header,valLine1,valLine2;

        header = makeHeader("mult","reorder",add,alpha, false, true,false);

        String assignment = add ? "+=" : "=";

        String tempVars = "";

        if( alpha ) {
            tempVars = "            double realTmp,imagTmp;\n";
            valLine1 = "            realTmp = a.data[i*2];\n" +
                       "            imagTmp = a.data[i*2 + 1];\n" +
                       "            realA = realAlpha*realTmp + imagAlpha*imagTmp;\n" +
                       "            imagA = realAlpha*imagTmp - imagAlpha*realTmp;\n";

            valLine2 = "            realTmp = a.getReal(k, i);\n" +
                       "            imagTmp = a.getImag(k, i);\n" +
                       "            realA = realAlpha*realTmp + imagAlpha*imagTmp;\n" +
                       "            imagA = realAlpha*imagTmp - imagAlpha*realTmp;\n";
        } else {
            valLine1 = "            realA = a.data[i*2];\n" +
                       "            imagA = a.data[i*2 + 1];\n";
            valLine2 = "            realA = a.getReal(k, i);\n" +
                       "            imagA = a.getImag(k, i);\n";
        }

        String foo =
                header + makeBoundsCheck(true,false, null)+handleZeros(add)+
                        "\n" +
                        "        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, a.numCols, i -> {\n" +
                        "        for (int i = 0; i < a.numCols; i++) {\n" +
                        "            double realA, imagA;\n" +
                        tempVars +
                        "            int indexC_start = i*c.numCols*2;\n" +
                        "\n" +
                        "            // first assign R\n" +
                        valLine1 +
                        "            int indexB = 0;\n" +
                        "            int end = indexB+b.numCols*2;\n" +
                        "            int indexC = indexC_start;\n" +
                        "            while( indexB < end ) {\n" +
                        "                double realB = b.data[indexB++];\n" +
                        "                double imagB = b.data[indexB++];\n" +
                        "                c.data[indexC++] "+assignment+" realA*realB + imagA*imagB;\n" +
                        "                c.data[indexC++] "+assignment+" realA*imagB - imagA*realB;\n" +
                        "            }\n" +
                        "            // now increment it\n" +
                        "            for (int k = 1; k < a.numRows; k++) {\n" +
                        valLine2+
                        "                end = indexB + b.numCols*2;\n" +
                        "                indexC = indexC_start;\n" +
                        "                // this is the loop for j\n" +
                        "                while (indexB < end) {\n" +
                        "                    double realB = b.data[indexB++];\n" +
                        "                    double imagB = b.data[indexB++];\n" +
                        "                    c.data[indexC++] += realA*realB + imagA*imagB;\n" +
                        "                    c.data[indexC++] += realA*imagB - imagA*realB;\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "        //CONCURRENT_ABOVE });\n" +
                        "    }\n";
        out.print(foo);
    }

    public void printMultTransA_small( boolean alpha , boolean add ) {
        String header,valLine;

        header = makeHeader("mult","small",add,alpha, false, true,false);

        String assignment = add ? "+=" : "=";

        if( alpha ) {
            valLine = "                c.data[indexC++] "+assignment+" realAlpha*realTotal - imagAlpha*imagTotal;\n" +
                      "                c.data[indexC++] "+assignment+" realAlpha*imagTotal + imagAlpha*realTotal;\n";
        } else {
            valLine = "                c.data[indexC++] "+assignment+" realTotal;\n" +
                      "                c.data[indexC++] "+assignment+" imagTotal;\n";
        }

        String foo =
                header + makeBoundsCheck(true,false, null)+
                        "\n" +
                        "        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, a.numCols, i -> {\n" +
                        "        for (int i = 0; i < a.numCols; i++) {\n" +
                        "            int indexC = i*2*b.numCols;\n" +
                        "            for (int j = 0; j < b.numCols; j++) {\n" +
                        "                int indexA = i*2;\n" +
                        "                int indexB = j*2;\n" +
                        "                int end = indexB + b.numRows*b.numCols*2;\n" +
                        "\n" +
                        "                double realTotal = 0;\n" +
                        "                double imagTotal = 0;\n" +
                        "\n" +
                        "                // loop for k\n" +
                        "                for (; indexB < end; indexB += b.numCols*2) {\n" +
                        "                    double realA = a.data[indexA];\n" +
                        "                    double imagA = a.data[indexA+1];\n" +
                        "                    double realB = b.data[indexB];\n" +
                        "                    double imagB = b.data[indexB+1];\n" +
                        "                    realTotal += realA*realB + imagA*imagB;\n" +
                        "                    imagTotal += realA*imagB - imagA*realB;\n" +
                        "                    indexA += a.numCols*2;\n" +
                        "                }\n" +
                        "\n" +
                        valLine +
                        "            }\n" +
                        "        }\n" +
                        "        //CONCURRENT_ABOVE });\n" +
                        "    }\n";

         out.print(foo);
    }

    public void printMultTransB( boolean alpha , boolean add ) {
        String header,valLine;

        header = makeHeader("mult",null,add,alpha, false, false,true);

        String assignment = add ? "+=" : "=";

        if( alpha ) {
            valLine = "                c.data[indexC++] "+assignment+" realAlpha*realTotal - imagAlpha*imagTotal;\n" +
                      "                c.data[indexC++] "+assignment+" realAlpha*imagTotal + imagAlpha*realTotal;\n";
        } else {
            valLine = "                c.data[indexC++] "+assignment+" realTotal;\n" +
                      "                c.data[indexC++] "+assignment+" imagTotal;\n";
        }

        String foo =
                header + makeBoundsCheck(false,true, null)+
                        "\n" +
                        "        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, a.numRows, xA -> {\n" +
                        "        for (int xA = 0; xA < a.numRows; xA++) {\n" +
                        "            int indexC = xA*b.numRows*2;\n" +
                        "            int aIndexStart = xA*a.numCols*2;\n" +
                        "            int end = aIndexStart + b.numCols*2;\n" +
                        "            int indexB = 0;\n"+
                        "            for (int xB = 0; xB < b.numRows; xB++) {\n" +
                        "                int indexA = aIndexStart;\n" +
                        "\n" +
                        "                double realTotal = 0;\n" +
                        "                double imagTotal = 0;\n" +
                        "\n" +
                        "                while (indexA < end) {\n" +
                        "                    double realA = a.data[indexA++];\n" +
                        "                    double imagA = a.data[indexA++];\n" +
                        "                    double realB = b.data[indexB++];\n" +
                        "                    double imagB = b.data[indexB++];\n" +
                        "                    realTotal += realA*realB + imagA*imagB;\n" +
                        "                    imagTotal += imagA*realB - realA*imagB;\n" +
                        "                }\n" +
                        "\n" +
                        valLine +
                        "            }\n" +
                        "        }\n" +
                        "        //CONCURRENT_ABOVE });\n" +
                        "    }\n";
        out.print(foo);
    }

    public void printMultTransAB( boolean alpha , boolean add ) {
        String header,valLine;

        header = makeHeader("mult",null,add,alpha, false, true,true);

        String assignment = add ? "+=" : "=";

        if( alpha ) {
            valLine = "                c.data[indexC++] "+assignment+" realAlpha*realTotal - imagAlpha*imagTotal;\n" +
                      "                c.data[indexC++] "+assignment+" realAlpha*imagTotal + imagAlpha*realTotal;\n";
        } else {
            valLine = "                c.data[indexC++] "+assignment+" realTotal;\n" +
                      "                c.data[indexC++] "+assignment+" imagTotal;\n";
        }

        String foo =
                header + makeBoundsCheck(true,true, null)+
                        "\n" +
                        "        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, a.numCols, i -> {\n" +
                        "        for (int i = 0; i < a.numCols; i++) {\n" +
                        "            int indexC = i*b.numRows*2;\n" +
                        "            int indexB = 0;\n"+
                        "            for (int j = 0; j < b.numRows; j++) {\n" +
                        "                int indexA = i*2;\n" +
                        "                int end = indexB + b.numCols*2;\n" +
                        "\n" +
                        "                double realTotal = 0;\n" +
                        "                double imagTotal = 0;\n" +
                        "\n" +
                        "                for (; indexB<end; ) {\n" +
                        "                    double realA = a.data[indexA];\n" +
                        "                    double imagA = -a.data[indexA + 1];\n" +
                        "                    double realB = b.data[indexB++];\n" +
                        "                    double imagB = -b.data[indexB++];\n" +
                        "                    realTotal += realA*realB - imagA*imagB;\n" +
                        "                    imagTotal += realA*imagB + imagA*realB;\n" +
                        "                    indexA += a.numCols*2;\n" +
                        "                }\n" +
                        "\n" +
                        valLine+
                        "            }\n" +
                        "        }\n"+
                        "        //CONCURRENT_ABOVE });\n" +
                        "    }\n";
        out.print(foo);
    }

    public void printMultTransAB_aux( boolean alpha , boolean add ) {
        String header,valLine;

        header = makeHeader("mult","aux",add,alpha, true, true,true);

        String assignment = add ? "+=" : "=";

        if( alpha ) {
            valLine = "                c.data[indexC++] "+assignment+" realAlpha*realTotal - imagAlpha*imagTotal;\n" +
                      "                c.data[indexC++] "+assignment+" realAlpha*imagTotal + imagAlpha*realTotal;\n";
        } else {
            valLine = "                c.data[indexC++] "+assignment+" realTotal;\n" +
                      "                c.data[indexC++] "+assignment+" imagTotal;\n";
        }

        String foo =
                header + makeBoundsCheck(true,true, "a.numRows")+handleZeros(add)+
                        "        int indexC = 0;\n" +
                        "        for (int i = 0; i < a.numCols; i++) {\n" +
                        "            int indexA = i*2;\n" +
                        "            for (int k = 0; k < b.numCols; k++) {\n" +
                        "                aux[k*2]     = a.data[indexA];\n" +
                        "                aux[k*2 + 1] = a.data[indexA + 1];\n" +
                        "                indexA += a.numCols*2;\n" +
                        "            }\n" +
                        "\n" +
                        "            for (int j = 0; j < b.numRows; j++) {\n" +
                        "                int indexAux = 0;\n" +
                        "                int indexB = j*b.numCols*2;\n" +
                        "                double realTotal = 0;\n" +
                        "                double imagTotal = 0;\n" +
                        "\n" +
                        "                for (int k = 0; k < b.numCols; k++) {\n" +
                        "                    double realA = aux[indexAux++];\n" +
                        "                    double imagA = -aux[indexAux++];\n" +
                        "                    double realB = b.data[indexB++];\n" +
                        "                    double imagB = -b.data[indexB++];\n" +
                        "                    realTotal += realA*realB - imagA*imagB;\n" +
                        "                    imagTotal += realA*imagB + imagA*realB;\n" +
                        "                }\n" +
                        valLine +
                        "            }\n" +
                        "        }\n"+
                        "    }\n";
        out.print(foo);
    }

    private String makeBoundsCheck(boolean tranA, boolean tranB, String auxLength)
    {
        String a_numCols = tranA ? "a.numRows" : "a.numCols";
        String a_numRows = tranA ? "a.numCols" : "a.numRows";
        String b_numCols = tranB ? "b.numRows" : "b.numCols";
        String b_numRows = tranB ? "b.numCols" : "b.numRows";

        String ret =
                "        if (a == c || b == c)\n" +
                        "            throw new IllegalArgumentException(\"Neither 'a' or 'b' can be the same matrix as 'c'\");\n"+
                        "        else if ("+a_numCols+" != "+b_numRows+") {\n" +
                        "            throw new MatrixDimensionException(\"The 'a' and 'b' matrices do not have compatible dimensions\");\n" +
                        "        } else if ("+a_numRows+" != c.numRows || "+b_numCols+" != c.numCols) {\n" +
                        "            throw new MatrixDimensionException(\"The results matrix does not have the desired dimensions\");\n" +
                        "        }\n" +
                        "\n";

        if( auxLength != null ) {
            ret += "        if (aux == null) aux = new double[ "+auxLength+"*2 ];\n\n";
        }

        return ret;
    }

    private String handleZeros( boolean add ) {

        String fill = add ? "" : "            CommonOps_ZDRM.fill(c, 0, 0);\n";

        String ret =
                "        if (a.numCols == 0 || a.numRows == 0) {\n" +
                        fill +
                        "            return;\n" +
                        "        }\n";
        return ret;
    }

    private String makeHeader(String nameOp, String variant,
                              boolean add, boolean hasAlpha, boolean hasAux,
                              boolean tranA, boolean tranB) {
        if( add ) nameOp += "Add";

        // make the op name
        if( tranA && tranB ) {
            nameOp += "TransAB";
        } else if( tranA ) {
            nameOp += "TransA";
        } else if( tranB ) {
            nameOp += "TransB";
        }

        String ret = "    public static void " + nameOp;

        if( variant != null ) ret += "_"+variant+"( ";
        else ret += "( ";

        if( hasAlpha ) ret += "double realAlpha, double imagAlpha, ";

        if( hasAux ) {
            ret += "ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c, @Nullable double[] aux ) {\n";
        } else {
            ret += "ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c ) {\n";
        }

        return ret;
    }

    public static void main(String[] args) throws FileNotFoundException {
        new GeneratorMatrixMatrixMult_ZDRM().generate();
    }
}
