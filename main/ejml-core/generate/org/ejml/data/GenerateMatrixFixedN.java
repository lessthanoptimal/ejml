/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.data;

import org.ejml.CodeGeneratorBase;

import java.io.FileNotFoundException;

/**
 * @author Peter Abeles
 */
public class GenerateMatrixFixedN extends CodeGeneratorBase {

    String classPreamble = "DMatrix";

    @Override
    public void generate() throws FileNotFoundException {
        for (int dimension = 2; dimension <= 6; dimension++) {
            print(dimension);
        }
    }

    public void print( int dimen ) throws FileNotFoundException {
        String className = classPreamble + dimen;

        setOutputFile(className);

        out.print("import org.ejml.ops.MatrixIO;\n" +
                "\n" +
                "/**\n" +
                " * Fixed sized vector with " + dimen + " elements.  Can represent a " + dimen + " x 1 or 1 x " + dimen + " matrix, context dependent.\n" +
                standardClassDocClosing("Peter Abeles") +
                "public class " + className + " implements DMatrixFixed {\n");
        printClassParam(dimen);
        out.print("\n" +
                "    public " + className + "() {}\n" +
                "\n" +
                "    public " + className + "( ");
        printFunctionParam(dimen);
        out.print(" ) {\n");
        printSetFromParam(dimen, "");
        out.print("    }\n" +
                "\n" +
                "    public " + className + "( " + className + " o ) {\n");
        printSetFromParam(dimen, "o.");
        out.print("    }\n" +
                "\n");
        printZero(dimen);
        out.print("    public void setTo( ");
        printFunctionParam(dimen);
        out.print(" ) {\n");
        printSetFromParam(dimen, "");
        out.print("    }\n\n");
        out.print("    public void setTo( int offset , double[] array ) {\n");
        for (int i = 0; i < dimen; i++) {
            out.print("        this.a" + (i + 1) + " = array[offset+" + i + "];\n");
        }
        out.print("    }\n");
        out.print("\n" +
                "    @Override public double get( int row, int col ) {return unsafe_get(row,col);}\n" +
                "\n" +
                "    @Override public double unsafe_get( int row, int col ) {\n" +
                "        if (row != 0 && col != 0)\n" +
                "            throw new IllegalArgumentException(\"Row or column must be zero since this is a vector\");\n" +
                "\n" +
                "        int w = Math.max(row,col);\n" +
                "\n");
        setGetter(dimen);
        out.print("        } else {\n" +
                "            throw new IllegalArgumentException(\"Out of range.  \"+w);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    @Override public void set( int row, int col, double val ) {\n" +
                "        unsafe_set(row,col,val);\n" +
                "    }\n" +
                "\n" +
                "    @Override public void unsafe_set( int row, int col, double val ) {\n" +
                "        if (row != 0 && col != 0)\n" +
                "            throw new IllegalArgumentException(\"Row or column must be zero since this is a vector\");\n" +
                "\n" +
                "        int w = Math.max(row,col);\n" +
                "\n");
        setSetter(dimen);
        out.print("        } else {\n" +
                "            throw new IllegalArgumentException(\"Out of range.  \"+w);\n" +
                "        }\n" +
                "    }\n" +
                "\n");
        printSetMatrix(dimen);
        out.print("    @Override public int getNumRows() {return " + dimen + ";}\n" +
                "\n" +
                "    @Override public int getNumCols() {return 1;}\n" +
                "\n" +
                "    @Override public int getNumElements() {return " + dimen + ";}\n" +
                "\n" +
                "    @Override public <T extends Matrix> T copy() {\n" +
                "        return (T)new " + className + "(this);\n" +
                "    }\n" +
                "\n" +
                "    @Override public void print() {\n" +
                "        MatrixIO.printFancy(System.out, this, MatrixIO.DEFAULT_LENGTH);\n" +
                "    }\n" +
                "\n" +
                "    @Override public void print( String format ) {\n" +
                "        MatrixIO.print(System.out, this, format);\n" +
                "    }\n" +
                "\n" +
                "    @Override public <T extends Matrix> T createLike() {return (T)new " + className + "();}\n" +
                "\n" +
                "    @Override public MatrixType getType() {return MatrixType.UNSPECIFIED;}\n" +
                "}\n\n");
    }

    private void printClassParam( int dimen ) {
        out.print("    public double ");
        for (int i = 1; i <= dimen; i++) {
            out.print("a" + i);
            if (i < dimen)
                out.print(",");
            else
                out.print(";\n");
        }
    }

    private void printFunctionParam( int dimen ) {
        for (int i = 1; i <= dimen; i++) {
            out.print("double a" + i);
            if (i < dimen)
                out.print(", ");
        }
    }

    private void printSetFromParam( int dimen, String prefix ) {
        for (int i = 1; i <= dimen; i++) {
            out.println("        this.a" + i + " = " + prefix + "a" + i + ";");
        }
    }

    private void printSetMatrix( int dimen ) {
        out.print("    @Override public void setTo( Matrix original ) {\n" +
                "        DMatrix m = (DMatrix)original;\n" +
                "\n" +
                "        if (m.getNumCols() == 1 && m.getNumRows() == " + dimen + ") {\n");
        for (int i = 0; i < dimen; i++) {
            out.print("            a" + (i + 1) + " = m.get(" + i + ",0);\n");
        }
        out.print("        } else if (m.getNumRows() == 1 && m.getNumCols() == " + dimen + ") {\n");
        for (int i = 0; i < dimen; i++) {
            out.print("            a" + (i + 1) + " = m.get(0," + i + ");\n");
        }
        out.print("        } else {\n" +
                "            throw new IllegalArgumentException(\"Incompatible shape\");\n" +
                "        }\n" +
                "    }\n\n");
    }

    private void setGetter( int dimen ) {
        for (int i = 0; i < dimen; i++) {
            if (i == 0)
                out.print("        if (w == 0) {\n");
            else
                out.print("        } else if (w == " + i + ") {\n");
            out.print("            return a" + (i + 1) + ";\n");
        }
    }

    private void setSetter( int dimen ) {
        for (int i = 0; i < dimen; i++) {
            if (i == 0)
                out.print("        if (w == 0) {\n");
            else
                out.print("        } else if (w == " + i + ") {\n");
            out.print("            a" + (i + 1) + " = val;\n");
        }
    }

    private void printZero( int dimen ) {
        out.print(
                "    @Override public void zero() {\n");
        for (int y = 1; y <= dimen; y++) {
            out.println("        a" + y + " = 0.0;");
        }
        out.print("    }\n\n");
    }

    public static void main( String[] args ) throws FileNotFoundException {
        new GenerateMatrixFixedN().generate();
    }
}
