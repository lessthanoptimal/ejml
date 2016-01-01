/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Definition of a macro.  Each input will replace the word of its name
 *
 * @author Peter Abeles
 */
public class Macro {
    String name;
    List<String> inputs = new ArrayList<String>();
    TokenList tokens;

    public TokenList execute( List<TokenList.Token> replacements ) {
        TokenList output = new TokenList();

        TokenList.Token t = tokens.getFirst();
        while( t != null ) {
            if( t.word != null ) {
                boolean matched = false;
                for (int i = 0; i < inputs.size(); i++) {
                    if( inputs.get(i).equals(t.word)) {
                        output.insert(output.last,replacements.get(i).copy());
                        matched = true;
                        break;
                    }
                }
                if( !matched ) {
                    output.insert(output.last,t.copy());
                }
            } else {
                output.insert(output.last,t.copy());
            }
            t = t.next;
        }
        return output;
    }

    public class Assign extends Operation {

        HashMap<String,Macro> macros;
        protected Assign( HashMap<String,Macro> macros ) {
            super("Macro:"+Macro.this.name);
            this.macros = macros;
        }

        @Override
        public void process() {
            macros.put(Macro.this.name,Macro.this);
        }
    }

    public Operation createOperation(HashMap<String,Macro> macros ) {
        return new Assign(macros);
    }
}
