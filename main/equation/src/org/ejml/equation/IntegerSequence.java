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
import java.util.List;

/**
 * Interface for an ordered sequence of integer values
 *
 * @author Peter Abeles
 */
public interface IntegerSequence {

    int length();

    void initialize();

    int next();

    boolean hasNext();

    Type getType();

    enum Type {
        EXPLICIT,
        FOR,
        COMBINED
    }

    /**
     * An array of integers which was explicitly specified
     */
    class Explicit implements IntegerSequence {

        List<VariableInteger> sequence = new ArrayList<VariableInteger>();
        int where;

        public Explicit(TokenList.Token start , TokenList.Token end) {
            TokenList.Token t = start;
            while( true ) {
                sequence.add( (VariableInteger)t.getVariable() );
                if( t == end ) {
                    break;
                } else {
                    t = t.next;
                }
            }
        }

        public Explicit(TokenList.Token single ) {
            sequence.add( (VariableInteger)single.getVariable() );
        }

        @Override
        public int length() {
            return sequence.size();
        }

        @Override
        public void initialize() {
            where = 0;
        }

        @Override
        public int next() {
            return sequence.get(where++).value;
        }

        @Override
        public boolean hasNext() {
            return where < sequence.size();
        }

        @Override
        public Type getType() {
            return Type.EXPLICIT;
        }

        public List<VariableInteger> getSequence() {
            return sequence;
        }
    }

    /**
     * A sequence of integers which has been specified using a start number, end number, and step size.
     *
     * 2:3:21 = 2 5 8 11 14 17 20
     */
    class For implements IntegerSequence {

        VariableInteger start;
        VariableInteger step;
        VariableInteger end;

        int valStart;
        int valStep;
        int valEnd;
        int where;
        int length;

        public For(TokenList.Token start, TokenList.Token step, TokenList.Token end) {
            this.start = (VariableInteger)start.getVariable();
            this.step = step == null ? null : (VariableInteger)step.getVariable();
            this.end = (VariableInteger)end.getVariable();
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public void initialize() {
            valStart = start.value;
            valEnd = end.value;
            if( step == null ) {
                valStep = 1;
            } else {
                valStep = step.value;
            }

            if( valStart <= 0 ) {
                throw new RuntimeException("step size must be a positive integer");
            }
            if( valEnd < valStart ) {
                throw new RuntimeException("end value must be >= the start value");
            }

            where = 0;
            length = (valEnd-valStart)/valStep  + 1;

        }

        @Override
        public int next() {
            return valStart + valStep*where++;
        }

        @Override
        public boolean hasNext() {
            return where < length;
        }

        public int getStart() {
            return valStart;
        }

        public int getStep() {
            return valStep;
        }

        public int getEnd() {
            return valEnd;
        }

        @Override
        public Type getType() {
            return Type.FOR;
        }
    }

    /**
     * This is a sequence of sequences
     */
    class Combined implements IntegerSequence {

        List<IntegerSequence> sequences = new ArrayList<IntegerSequence>();

        int which;

        public Combined(TokenList.Token start, TokenList.Token end) {

            TokenList.Token t = start;
            do {
                if( t.getVariable().getType() == VariableType.SCALAR ) {
                    sequences.add( new IntegerSequence.Explicit(t));
                } else if( t.getVariable().getType() == VariableType.INTEGER_SEQUENCE ) {
                    sequences.add( ((VariableIntegerSequence)t.getVariable()).sequence );
                } else {
                    throw new RuntimeException("Unexpected token type");
                }
                t = t.next;
            } while( t != null && t.previous != end);
        }

        @Override
        public int length() {
            int total = 0;
            for (int i = 0; i < sequences.size(); i++) {
                total += sequences.get(i).length();
            }
            return total;
        }

        @Override
        public void initialize() {
            which = 0;
            for (int i = 0; i < sequences.size(); i++) {
                sequences.get(i).initialize();
            }
        }

        @Override
        public int next() {
            int output = sequences.get(which).next();

            if( !sequences.get(which).hasNext() ) {
                which++;
            }

            return output;
        }

        @Override
        public boolean hasNext() {
            return which < sequences.size();
        }

        @Override
        public Type getType() {
            return Type.COMBINED;
        }
    }
}
