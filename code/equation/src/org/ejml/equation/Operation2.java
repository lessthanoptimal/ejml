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

/**
 * @author Peter Abeles
 */
public interface Operation2 {

    public Signature[] getSignature();

    public Operation.Info initialize( Variable... inputs );

    void process();

    public static class Signature {
        public TokenList.Type type;

        public Signature(TokenList.Type type) {
            this.type = type;
        }
    }

    public static class SSymbol extends Signature
    {
        public Symbol sym;

        public SSymbol(Symbol sym) {
            super(TokenList.Type.SYMBOL);

            this.sym = sym;
        }
    }

    public static class SVariable extends Signature
    {
        public Class<Variable> type;

        public SVariable(Class<Variable> type) {
            super(TokenList.Type.VARIABLE);
            this.type = type;
        }
    }

    public static class SFunction extends Signature
    {
        public String name;
        public Class<Variable>[] arguments;

        public SFunction(String name, Class<Variable>... arguments) {
            super(TokenList.Type.FUNCTION);
            this.name = name;
            this.arguments = arguments;
        }
    }
}
