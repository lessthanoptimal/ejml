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

import java.io.FileNotFoundException;
import java.io.PrintStream;


/**
 * @author Peter Abeles
 */
public class CodeGeneratorUnrolledMatrixMatrixMult {
    // the largest dimension it will generate
    int maxDimen;

    PrintStream stream;

    public CodeGeneratorUnrolledMatrixMatrixMult( String fileName , int maxDimen )
            throws FileNotFoundException {
        stream = new PrintStream(fileName);
        this.maxDimen = maxDimen;
    }

    public void print() {
        printBeginning();
        printSetup();
        printMainFunction();
        for( int i = 1; i < maxDimen+1; i++ ) {
            printMultOp(i,true,false);
            printMultOp(i,false,false);
            printMultOp(i,true,true);
            printMultOp(i,false,true);
            printMultTransA(i,false,false);
            printMultTransAB(i,false,false);
        }
        printEnd();
    }

    private void printBeginning() {
     String message =
             "/*\n" +
                     " * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.\n" +
                     " *\n" +
                     " * This file is part of Efficient Java Matrix Library (EJML).\n" +
                     " *\n" +
                     " * EJML is free software: you can redistribute it and/or modify\n" +
                     " * it under the terms of the GNU Lesser General Public License as\n" +
                     " * published by the Free Software Foundation, either version 3\n" +
                     " * of the License, or (at your option) any later version.\n" +
                     " *\n" +
                     " * EJML is distributed in the hope that it will be useful,\n" +
                     " * but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
                     " * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
                     " * GNU Lesser General Public License for more details.\n" +
                     " *\n" +
                     " * You should have received a copy of the GNU Lesser General Public\n" +
                     " * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.\n" +
                     " */\n" +
                     "\n" +
                     "package org.ejml.alg.dense.mult;\n" +
                     "\n" +
                     "import org.ejml.data.DenseMatrix64F;\n" +
                     "\n" +
                     "\n" +
                     "/**\n" +
                     " * This file has been automatically generated.  Do not modify directly.\n"+
                     " * \n"+
                     " * @author Peter Abeles\n" +
                     " */\n" +
                     "public class UnrolledMatrixMult {\n" +
                     "\n" +
                     "    public static int NUM_UNROLLED = "+maxDimen+";\n" +
                     "\n" +
                     "    public static Mult[] mult;\n"+
                     "    public static Mult[] multAdd;\n"+
                     "    public static MultS[] multS;\n"+
                     "    public static MultS[] multAddS;\n"+
                     "    public static Mult[] multTransA;\n"+
                     "    public static Mult[] multTransAB;\n";

        stream.println(message);
    }

    private void printSetup() {
        stream.print("    static {\n" +
                "        mult = new Mult[NUM_UNROLLED+1 ];\n" +
                "        multAdd = new Mult[NUM_UNROLLED+1 ];\n" +
                "        multS = new MultS[NUM_UNROLLED+1 ];\n" +
                "        multAddS = new MultS[NUM_UNROLLED+1 ];\n" +
                "        multTransA = new Mult[NUM_UNROLLED+1 ];\n" +
                "        multTransAB = new Mult[NUM_UNROLLED+1 ];\n"+
                "\n");

        stream.print("        declareMult();\n");
        stream.print("        declareMultAdd();\n");
        stream.print("        declareMultScale();\n");
        stream.print("        declareMultAddScale();\n");
        stream.print("        declareMultTransA();\n");
        stream.print("        declareMultTransAB();\n");
        stream.print("    }\n\n");

        declareFunction("mult","Mult");
        declareFunction("multAdd","MultAdd");
        declareFunction("multS","MultScale");
        declareFunction("multAddS","MultAddScale");
        declareFunction("multTransA","MultTransA");
        declareFunction("multTransAB","MultTransAB");
    }

    private void declareFunction( String nameArray , String nameClass ) {
        stream.print("    static void declare"+nameClass+"(){\n");
        for( int i = 1; i < maxDimen+1; i++ ) {
            stream.print("        "+nameArray+"["+i+"] = new "+nameClass+i+"();\n");
        }
        stream.print("    }\n\n");
    }


    private void printMainFunction() {
        printMainFunc("mult","mult","mult",false);
        printMainFunc("mult","multS","mult",true);
        printMainFunc("multAdd","multAdd","mult",false);
        printMainFunc("multAdd","multAddS","mult",true);
        printMainFunc("multTransA","multTransA","mult",false);
        printMainFunc("multTransAB","multTransAB","mult",false);
    }

    private void printMainFunc( String nameFunc , String nameArray , String nameOp , boolean hasAlpha )
    {
        stream.print("    public static void "+nameFunc+"( ");
        if( hasAlpha )
            stream.print("double alpha ,");
        stream.print("DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c ) {\n");

        stream.print(
                "        if( a.numCols != b.numRows ) {\n" +
                "            throw new MatrixDimensionException(\"The 'a' and 'b' matrices do not have compatible dimensions\");\n" +
                "        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {\n" +
                "            throw new MatrixDimensionException(\"The results matrix does not have the desired dimensions\");\n" +
                "        }\n\n");

        stream.print("        "+nameArray+"[b.numRows]."+nameOp+"(");
        if( hasAlpha )
            stream.print("alpha,");
        stream.print("a,b,c);\n");
        stream.print("    }\n\n");
    }

    private void printMultOp( int num , boolean add , boolean scale ) {

        // create the name of the inner class
        String nameClass = "Mult";
        if( add ) nameClass += "Add";
        if( scale ) nameClass += "Scale";
        nameClass += num;

        String op = add ? "+=" : "=";

        String header;
        String interfaceType;
        String val;
        if( scale ) {
            val = " alpha*val";
            interfaceType = "MultS";
            header = "        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )\n";
        } else {
            val = " val";
            interfaceType = "Mult";
            header = "        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )\n";
        }

        stream.print(
                "    public static class "+nameClass+" implements "+interfaceType+" {\n" +
                        header +
                        "        {\n" +
                        "            double dataA[] = a.data;\n" +
                        "            double dataB[] = b.data;\n" +
                        "            double dataC[] = c.data;\n" +
                        "\n" +
                        "            for( int j = 0; j < b.numCols; j++ ) {\n"+
                        "                int iterB = j;\n");

        for( int i = 0; i < num-1; i++ ) {
            stream.print("                double b"+i+" = dataB[iterB]; iterB += b.numCols;\n");
        }
        stream.print("                double b"+(num-1)+" = dataB[iterB];\n");

        stream.print("\n" +
                        "                int iterA = 0;\n" +
                        "                for( int i = 0; i < a.numRows; i++ ) {\n" +
                        "                    double val=0;\n");
        for( int i = 0; i < num-1; i++ ) {
            stream.print("                    val  += dataA[iterA++]*b"+i+";\n");
        }
        stream.print("                    val  += dataA[iterA]*b"+(num-1)+";\n");

        stream.print("\n" +
                "                    dataC[i*c.numCols+j] "+op+val+";\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n\n");
    }

    private void printMultTransA( int num , boolean add , boolean scale ) {

        String nameOp = "MultTransA"+num;

        stream.print(
                "    public static class "+nameOp+" implements Mult {\n" +
                "        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )\n" +
                "        {\n" +
                "            int cIndex = 0;\n" +
                "\n" +
                "            for( int i = 0; i < a.numCols; i++ ) {\n" +
                "                int indexA = i;\n");

        for( int i = 0; i < num-1; i++ ) {
            stream.print("                double a"+i+" = a.data[indexA]; indexA += a.numCols;\n");
        }
        stream.print("                double a"+(num-1)+" = a.data[indexA];\n");

        stream.print(
                "\n" +
                "                for( int j = 0; j < b.numCols; j++ ) {\n" +
                "                    int indexB = j;\n" +
                "                    double total = 0;\n" +
                "\n");
        for( int i = 0; i < num-1; i++ ) {
            stream.print("                    total += a"+i+"*b.data[indexB]; indexB += b.numCols;\n");
        }
        stream.print("                    total += a"+(num-1)+"*b.data[indexB];\n");

        stream.print(
                "\n" +
                "                    c.data[cIndex++] = total;\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n\n");
    }

    private void printMultTransAB( int num , boolean add , boolean scale ) {

        String nameOp = "MultTransAB"+num;

        stream.print(
                "    public static class "+nameOp+" implements Mult {\n" +
                "        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )\n" +
                "        {\n" +
                "            double dataA[] = a.data;\n" +
                "            double dataB[] = b.data;\n" +
                "            double dataC[] = c.data;\n"+
                "            int cIndex = 0;\n" +
                "\n" +
                "            for( int i = 0; i < a.numCols; i++ ) {\n" +
                "                int indexA = i;\n");

        for( int i = 0; i < num-1; i++ ) {
            stream.print("                double a"+i+" = dataA[indexA]; indexA += a.numCols;\n");
        }
        stream.print("                double a"+(num-1)+" = dataA[indexA];\n");

        stream.print(
                "\n" +
                "                int indexB = 0;\n"+
                "                int endB = b.numRows*b.numCols;\n" +
                "                while( indexB != endB ) {\n" +
                "                    double total = 0;\n" +
                "\n");
        for( int i = 0; i < num; i++ ) {
            stream.print("                    total += a"+i+"*dataB[indexB++];\n");
        }

        stream.print(
                "\n" +
                "                    dataC[cIndex++] = total;\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n\n");
    }

    private void printEnd() {
        stream.print("\n" +
                "    public static interface Mult\n" +
                "    {\n" +
                "        public void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c );\n" +
                "    }\n" +
                "\n"+
                "    public static interface MultS\n" +
                "    {\n" +
                "        public void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c );\n" +
                "    }\n" +
                "}");

        stream.close();
    }

    public static void main( String args[] ) throws FileNotFoundException {
        CodeGeneratorUnrolledMatrixMatrixMult gen =
                new CodeGeneratorUnrolledMatrixMatrixMult("UnrolledMatrixMult.java",20);

        gen.print();
    }
}
