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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Peter Abeles
 */
// TODO How to handle case where the operation is a mix of low and high priority operations
// a + b*c -> is the operation.  eq is a + b*c*d  .  The pattern doesn't match since *d is high priority
// However, if a*b*c -> is the operator it will work on eq: a*b*c*d since it doesn't change anything
// Maybe have something that understands the order of operations and see if there is something that would preempt
// and change the input to an operator?
public class OperationParser {

    Splay root = new Splay();

    public void addOperation( Operation2 op ) {
        root.add( op , 0 );
    }

    public void parse( TokenList tokens, Sequence sequence ) {
       TokenList.Token t = tokens.first;

        while( t != null ) {
            // find the longest pattern which matches

            // find all the variables and provide it as input

            // remove the tokens and place the output in their place
        }
    }

    protected static class Splay
    {
        Map<Symbol,Splay> symbols = new HashMap<Symbol, Splay>();
        List<VariableItem> variables = new ArrayList<VariableItem>();

        // Operation which matches this pattern
        Operation2 op;

        public void add( Operation2 op , int index ) {
            Operation2.Signature signature[] = op.getSignature();

            if( index == signature.length ) {
                if( this.op == null)
                    this.op = op;
                else {
                    throw new RuntimeException("Multiple ops with same signature");
                }
            } else {
                Splay splay = next( signature[index]);
                if( splay == null ) {
                    splay = add( signature[index]);
                }
                splay.add( op , ++index );
            }
        }

        /**
         * Creates a new splay and adds a reference in the current one.  Assumes no such reference for the signature
         * already exists
         * @param p Reference to next splay
         * @return The new splay
         */
        public Splay add( Operation2.Signature p ) {

            Splay splay = new Splay();

            switch( p.type ) {
                case SYMBOL:
                    symbols.put(((Operation2.SSymbol)p).sym,splay);
                    break;

                case VARIABLE: {
                    Class target = ((Operation2.SVariable)p).type;
                    variables.add( new VariableItem(target,splay));
                }break;

                case FUNCTION:
                    throw new RuntimeException("Function not yet supported");

                default:
                    throw new RuntimeException("Unknown type "+p.type);
            }

            return splay;
        }

        public Splay next( Operation2.Signature p ) {
            switch( p.type ) {
                case SYMBOL:
                    return symbols.get(((Operation2.SSymbol)p).sym);
                case VARIABLE: {
                    Class target = ((Operation2.SVariable)p).type;
                    for (int i = 0; i < variables.size(); i++) {
                        VariableItem vi = variables.get(i);
                        if( vi.type == target ) {
                            return vi.splay;
                        }
                    }
                    return null;
                }

                case FUNCTION:
                    throw new RuntimeException("Function not yet supported");

                default:
                    throw new RuntimeException("Unknown type "+p.type);
            }
        }
    }

    private static class VariableItem
    {
        Class type;
        Splay splay;

        private VariableItem(Class type, Splay splay) {
            this.type = type;
            this.splay = splay;
        }
    }
}
