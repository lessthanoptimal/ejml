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
public class GenerateMatrixFixedNxN extends CodeGeneratorBase {

    String classPreamble = "DMatrix";

    @Override
    public void generate() throws FileNotFoundException {
        for (int dimension = 2; dimension <= 6; dimension++) {
            print(dimension);
        }
    }

    public void print( int dimen ) throws FileNotFoundException {
        String className = classPreamble + dimen + "x" + dimen;

        setOutputFile(className);

        out.println("import org.ejml.ops.MatrixIO;\n\n" +
                "/**\n" +
                " * Fixed sized " + dimen + " by " + className + " matrix.  The matrix is stored as class variables for very fast read/write.  aXY is the\n" +
                " * value of row = X and column = Y.\n" +
                standardClassDocClosing("Peter Abeles") +
                "public class " + className + " implements DMatrixFixed {\n");
        printClassParam(dimen);
        out.print("\n" +
                "    public " + className + "() {}\n" +
                "\n" +
                "    public " + className);
        printFunctionParam(13 + className.length(), dimen);
        printSetFromParam(dimen, "");
        out.print("    }\n" +
                "\n" +
                "    public " + className + "( " + className + " o ) {\n");
        printSetFromParam(dimen, "o.");
        out.print("    }\n" +
                "\n");
        printZero(dimen);
        out.print("    public void setTo");
        printFunctionParam(23, dimen);
        printSetFromParam(dimen, "");
        out.print("    }\n\n");
        out.print("    public void setTo( int offset , double[] a ) {\n");
        printSetFromArray(dimen);
        out.print("    }\n\n");
        out.print("    @Override public double get( int row, int col ) {\n" +
                "        return unsafe_get(row,col);\n" +
                "    }\n" +
                "\n" +
                "    @Override public double unsafe_get( int row, int col ) {\n");
        setGetter(dimen);
        out.print("        throw new IllegalArgumentException(\"Row and/or column out of range. \"+row+\" \"+col);\n" +
                "    }\n" +
                "\n" +
                "    @Override public void set( int row, int col, double val ) {\n" +
                "        unsafe_set(row,col,val);\n" +
                "    }\n" +
                "\n" +
                "    @Override public void unsafe_set( int row, int col, double val ) {\n");
        setSetter(dimen);
        out.print("        throw new IllegalArgumentException(\"Row and/or column out of range. \"+row+\" \"+col);\n" +
                "    }\n" +
                "\n");
        printSetMatrix(dimen);
        out.print("    @Override public int getNumRows() {return " + dimen + ";}\n" +
                "\n" +
                "    @Override public int getNumCols() {return " + dimen + ";}\n" +
                "\n" +
                "    @Override public int getNumElements() {return " + (dimen*dimen) + ";}\n" +
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
                "    @Override public MatrixType getType() {return MatrixType.UNSPECIFIED;}" +
                "}\n\n");
    }

    private void printClassParam( int dimen ) {
        for (int y = 1; y <= dimen; y++) {
            out.print("    public double ");
            for (int x = 1; x <= dimen; x++) {
                out.print("a" + y + "" + x);
                if (x != dimen)
                    out.print(",");
                else
                    out.println(";");
            }
        }
    }

    private void printFunctionParam( int spaces, int dimen ) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            s.append(" ");
        }
        for (int y = 1; y <= dimen; y++) {
            if (y == 1)
                out.print("( ");
            else
                out.print(s);
            for (int x = 1; x <= dimen; x++) {
                out.print("double a" + y + "" + x);
                if (x != dimen)
                    out.print(", ");
                else if (y != dimen)
                    out.println(",");
                else
                    out.println(" ) {");
            }
        }
    }

    private void printSetFromParam( int dimen, String prefix ) {
        for (int y = 1; y <= dimen; y++) {
            out.print("       ");
            for (int x = 1; x <= dimen; x++) {
                out.print(" this.a" + y + "" + x + " = " + prefix + "a" + y + "" + x + ";");
            }
            out.println();
        }
    }

    private void printSetFromArray( int dimen ) {
        for (int y = 1; y <= dimen; y++) {
            out.print("       ");
            for (int x = 1; x <= dimen; x++) {
                out.print(" this.a" + y + "" + x + " = a[offset + " + ((y - 1)*dimen + x - 1) + "];");
            }
            out.println();
        }
    }

    private void setGetter( int dimen ) {
        for (int y = 1; y <= dimen; y++) {
            if (y == 1)
                out.print("        if (row == 0) {\n");
            else
                out.print("        } else if (row == " + (y - 1) + ") {\n");
            for (int x = 1; x <= dimen; x++) {
                if (x == 1)
                    out.print("            if (col == 0) {\n");
                else
                    out.print("            } else if (col == " + (x - 1) + ") {\n");
                out.print("                return a" + y + "" + x + ";\n");
            }
            out.print("            }\n");
        }
        out.print("        }\n");
    }

    private void setSetter( int dimen ) {
        for (int y = 1; y <= dimen; y++) {
            if (y == 1)
                out.print("        if (row == 0) {\n");
            else
                out.print("        } else if (row == " + (y - 1) + ") {\n");
            for (int x = 1; x <= dimen; x++) {
                if (x == 1)
                    out.print("            if (col == 0) {\n");
                else
                    out.print("            } else if (col == " + (x - 1) + ") {\n");
                out.print("                a" + y + "" + x + " = val; return;\n");
            }
            out.print("            }\n");
        }
        out.print("        }\n");
    }

    private void printSetMatrix( int dimen ) {
        out.print("    @Override public void setTo( Matrix original ) {\n" +
                "        if (original.getNumCols() != " + dimen + " || original.getNumRows() != " + dimen + ")\n" +
                "            throw new IllegalArgumentException(\"Rows and/or columns do not match\");\n" +
                "        DMatrix m = (DMatrix)original;\n" +
                "        \n");
        for (int y = 1; y <= dimen; y++) {
            for (int x = 1; x <= dimen; x++) {
                out.print("        a" + y + "" + x + " = m.get(" + (y - 1) + "," + (x - 1) + ");\n");
            }
        }
        out.print("    }\n\n");
    }

    private void printZero( int dimen ) {
        out.print(
                "    @Override public void zero() {\n");
        for (int y = 1; y <= dimen; y++) {
            out.print("       ");
            for (int x = 1; x <= dimen; x++) {
                out.print(" a" + y + "" + x + " = 0.0;");
            }
            out.println();
        }
        out.print("    }\n\n");
    }

    public static void main( String[] args ) throws FileNotFoundException {
        new GenerateMatrixFixedNxN().generate();
    }
}
